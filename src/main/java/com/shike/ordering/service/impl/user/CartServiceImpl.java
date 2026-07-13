package com.shike.ordering.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.config.ShopProperties;
import com.shike.ordering.dto.user.CartItemAddDTO;
import com.shike.ordering.dto.user.CartItemQuantityDTO;
import com.shike.ordering.entity.*;
import com.shike.ordering.mapper.*;
import com.shike.ordering.service.user.CartService;
import com.shike.ordering.vo.user.CartItemVO;
import com.shike.ordering.vo.user.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private static final int MAX_QUANTITY = 99;
    private final ShoppingCartMapper cartMapper;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductSpecificationMapper specificationMapper;
    private final ProductTasteMapper tasteMapper;
    private final UserMapper userMapper;
    private final ShopProperties shopProperties;

    @Override
    public CartVO getCart() {
        List<CartItemVO> items = cartMapper.selectUserCart(userId());
        int totalQuantity = items.stream().mapToInt(CartItemVO::getQuantity).sum();
        BigDecimal totalAmount = items.stream().map(CartItemVO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartVO(items, totalQuantity, totalAmount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartVO add(CartItemAddDTO request) {
        Long userId = userId();
        userMapper.selectIdForUpdate(userId);
        Product product = requireAvailableProduct(request.productId());
        long specificationId = validateSpecification(product.getId(), request.specificationId());
        long tasteId = validateTaste(product.getId(), request.tasteId());
        ShoppingCart item = cartMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId).eq(ShoppingCart::getProductId, product.getId())
                .eq(ShoppingCart::getSpecificationId, specificationId).eq(ShoppingCart::getTasteId, tasteId));
        int quantity = request.quantity() + (item == null ? 0 : item.getQuantity());
        validateQuantity(quantity, product.getStock());
        if (item == null) {
            item = new ShoppingCart();
            item.setUserId(userId);
            item.setShopId(product.getShopId());
            item.setProductId(product.getId());
            item.setSpecificationId(specificationId);
            item.setTasteId(tasteId);
            item.setQuantity(quantity);
            cartMapper.insert(item);
        } else {
            item.setQuantity(quantity);
            cartMapper.updateById(item);
        }
        return getCart();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartVO updateQuantity(Long id, CartItemQuantityDTO request) {
        Long userId = userId();
        userMapper.selectIdForUpdate(userId);
        ShoppingCart item = requireOwned(id, userId);
        Product product = requireAvailableProduct(item.getProductId());
        validateSpecification(product.getId(), item.getSpecificationId() == 0 ? null : item.getSpecificationId());
        validateTaste(product.getId(), item.getTasteId() == 0 ? null : item.getTasteId());
        validateQuantity(request.quantity(), product.getStock());
        item.setQuantity(request.quantity());
        cartMapper.updateById(item);
        return getCart();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = userId();
        userMapper.selectIdForUpdate(userId);
        int deleted = cartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getId, id).eq(ShoppingCart::getUserId, userId));
        if (deleted == 0) throw new ResourceNotFoundException(ErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear() {
        Long userId = userId();
        userMapper.selectIdForUpdate(userId);
        cartMapper.delete(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));
    }

    private Product requireAvailableProduct(Long productId) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, productId).eq(Product::getShopId, shopProperties.defaultShopId()));
        if (product == null) throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        if (product.getStatus() == 0) throw new BusinessException(ErrorCode.PRODUCT_OFF_SALE);
        if (product.getStatus() == 2 || product.getStock() <= 0) throw new BusinessException(ErrorCode.PRODUCT_SOLD_OUT);
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getId, product.getCategoryId()).eq(Category::getShopId, product.getShopId())
                .eq(Category::getStatus, 1));
        if (category == null) throw new BusinessException(ErrorCode.PRODUCT_OFF_SALE);
        return product;
    }

    private long validateSpecification(Long productId, Long specificationId) {
        if (specificationId == null) {
            Long enabledCount = specificationMapper.selectCount(new LambdaQueryWrapper<ProductSpecification>()
                    .eq(ProductSpecification::getProductId, productId).eq(ProductSpecification::getStatus, 1));
            if (enabledCount > 0) throw new BusinessException(ErrorCode.PRODUCT_SPECIFICATION_INVALID);
            return 0L;
        }
        ProductSpecification specification = specificationMapper.selectOne(new LambdaQueryWrapper<ProductSpecification>()
                .eq(ProductSpecification::getId, specificationId).eq(ProductSpecification::getProductId, productId)
                .eq(ProductSpecification::getStatus, 1));
        if (specification == null) throw new BusinessException(ErrorCode.PRODUCT_SPECIFICATION_INVALID);
        return specificationId;
    }

    private long validateTaste(Long productId, Long tasteId) {
        if (tasteId == null) return 0L;
        ProductTaste taste = tasteMapper.selectOne(new LambdaQueryWrapper<ProductTaste>()
                .eq(ProductTaste::getId, tasteId).eq(ProductTaste::getProductId, productId)
                .eq(ProductTaste::getStatus, 1));
        if (taste == null) throw new BusinessException(ErrorCode.PRODUCT_TASTE_INVALID);
        return tasteId;
    }

    private ShoppingCart requireOwned(Long id, Long userId) {
        ShoppingCart item = cartMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getId, id).eq(ShoppingCart::getUserId, userId));
        if (item == null) throw new ResourceNotFoundException(ErrorCode.CART_ITEM_NOT_FOUND);
        return item;
    }

    private void validateQuantity(int quantity, int stock) {
        if (quantity > MAX_QUANTITY) throw new BusinessException(ErrorCode.CART_QUANTITY_EXCEEDED);
        if (quantity > stock) throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
    }

    private Long userId() { return PrincipalContext.require(PrincipalType.USER).principalId(); }
}
