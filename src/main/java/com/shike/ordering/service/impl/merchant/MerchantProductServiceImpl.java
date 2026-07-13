package com.shike.ordering.service.impl.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.converter.ProductViewAssembler;
import com.shike.ordering.dto.merchant.ProductQueryDTO;
import com.shike.ordering.dto.merchant.ProductSaveDTO;
import com.shike.ordering.entity.*;
import com.shike.ordering.mapper.*;
import com.shike.ordering.service.merchant.MerchantProductService;
import com.shike.ordering.vo.common.ProductDetailVO;
import com.shike.ordering.vo.common.ProductSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantProductServiceImpl implements MerchantProductService {
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductSpecificationMapper specificationMapper;
    private final ProductTasteMapper tasteMapper;
    private final ProductViewAssembler assembler;

    @Override
    public PageResult<ProductSummaryVO> list(ProductQueryDTO query) {
        Page<Product> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, shopId())
                .eq(query.categoryId() != null, Product::getCategoryId, query.categoryId())
                .eq(query.status() != null, Product::getStatus, query.status())
                .and(query.keyword() != null && !query.keyword().isBlank(),
                        condition -> condition.like(Product::getName, query.keyword())
                                .or().like(Product::getDescription, query.keyword()))
                .orderByDesc(Product::getId);
        var result = productMapper.selectPage(page, wrapper);
        return new PageResult<>(assembler.summaries(result.getRecords()), result.getTotal(),
                result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override public ProductDetailVO detail(Long id) { return assembler.detail(requireOwned(id), false); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductDetailVO create(ProductSaveDTO request) {
        Long shopId = shopId();
        requireCategory(request.categoryId(), shopId);
        validateRequest(request);
        Product product = new Product();
        product.setShopId(shopId);
        apply(product, request);
        product.setDeleted(0);
        product.setVersion(0);
        productMapper.insert(product);
        syncSpecifications(product.getId(), request.specifications());
        syncTastes(product.getId(), request.tastes());
        log.info("merchant created product, shopId={}, productId={}", shopId, product.getId());
        return assembler.detail(product, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductDetailVO update(Long id, ProductSaveDTO request) {
        Product product = requireOwned(id);
        requireCategory(request.categoryId(), product.getShopId());
        validateRequest(request);
        apply(product, request);
        if (productMapper.updateById(product) != 1) throw new BusinessException(ErrorCode.DATA_CONFLICT);
        syncSpecifications(id, request.specifications());
        syncTastes(id, request.tastes());
        return assembler.detail(product, false);
    }

    @Override public ProductDetailVO onSale(Long id) {
        Product product = requireOwned(id);
        if (product.getStock() <= 0) throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        return updateStatus(product, 1);
    }
    @Override public ProductDetailVO offSale(Long id) { return updateStatus(requireOwned(id), 0); }
    @Override public ProductDetailVO soldOut(Long id) { return updateStatus(requireOwned(id), 2); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Product product = requireOwned(id);
        productMapper.deleteById(product);
        specificationMapper.delete(new LambdaQueryWrapper<ProductSpecification>()
                .eq(ProductSpecification::getProductId, id));
        tasteMapper.delete(new LambdaQueryWrapper<ProductTaste>().eq(ProductTaste::getProductId, id));
        log.info("merchant deleted product, shopId={}, productId={}", product.getShopId(), id);
    }

    private ProductDetailVO updateStatus(Product product, int status) {
        product.setStatus(status);
        if (productMapper.updateById(product) != 1) throw new BusinessException(ErrorCode.DATA_CONFLICT);
        log.info("merchant changed product status, shopId={}, productId={}, status={}",
                product.getShopId(), product.getId(), status);
        return assembler.detail(product, false);
    }

    private Product requireOwned(Long id) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id).eq(Product::getShopId, shopId()));
        if (product == null) throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        return product;
    }

    private void requireCategory(Long categoryId, Long shopId) {
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getId, categoryId).eq(Category::getShopId, shopId));
        if (category == null) throw new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND);
    }

    private void validateRequest(ProductSaveDTO request) {
        if (request.originalPrice() != null && request.originalPrice().compareTo(request.price()) < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (request.status() == 1 && request.stock() <= 0) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }
        ensureUniqueNames(request.specifications().stream().map(ProductSaveDTO.SpecificationInput::name).toList());
        ensureUniqueNames(request.tastes().stream().map(ProductSaveDTO.TasteInput::name).toList());
    }

    private void ensureUniqueNames(List<String> names) {
        Set<String> normalized = new HashSet<>();
        if (names.stream().map(String::trim).anyMatch(name -> !normalized.add(name))) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT);
        }
    }

    private void apply(Product product, ProductSaveDTO request) {
        product.setCategoryId(request.categoryId());
        product.setName(request.name().trim());
        product.setImageUrl(request.imageUrl().trim());
        product.setDescription(trimToNull(request.description()));
        product.setDetail(trimToNull(request.detail()));
        product.setPrice(request.price());
        product.setOriginalPrice(request.originalPrice());
        product.setStock(request.stock());
        product.setStatus(request.status());
        product.setRecommended(request.recommended());
    }

    private void syncSpecifications(Long productId, List<ProductSaveDTO.SpecificationInput> requests) {
        Map<Long, ProductSpecification> existing = specificationMapper.selectList(
                        new LambdaQueryWrapper<ProductSpecification>().eq(ProductSpecification::getProductId, productId))
                .stream().collect(Collectors.toMap(ProductSpecification::getId, item -> item));
        for (ProductSaveDTO.SpecificationInput request : requests) {
            ProductSpecification option = request.id() == null ? new ProductSpecification() : existing.remove(request.id());
            if (option == null) throw new BusinessException(ErrorCode.DATA_CONFLICT);
            option.setProductId(productId);
            option.setName(request.name().trim());
            option.setPriceDelta(request.priceDelta());
            option.setStatus(request.status());
            option.setSort(request.sort());
            if (request.id() == null) { option.setDeleted(0); specificationMapper.insert(option); }
            else specificationMapper.updateById(option);
        }
        if (!existing.isEmpty()) specificationMapper.delete(new LambdaQueryWrapper<ProductSpecification>()
                .eq(ProductSpecification::getProductId, productId).in(ProductSpecification::getId, existing.keySet()));
    }

    private void syncTastes(Long productId, List<ProductSaveDTO.TasteInput> requests) {
        Map<Long, ProductTaste> existing = tasteMapper.selectList(
                        new LambdaQueryWrapper<ProductTaste>().eq(ProductTaste::getProductId, productId))
                .stream().collect(Collectors.toMap(ProductTaste::getId, item -> item));
        for (ProductSaveDTO.TasteInput request : requests) {
            ProductTaste option = request.id() == null ? new ProductTaste() : existing.remove(request.id());
            if (option == null) throw new BusinessException(ErrorCode.DATA_CONFLICT);
            option.setProductId(productId);
            option.setName(request.name().trim());
            option.setStatus(request.status());
            option.setSort(request.sort());
            if (request.id() == null) { option.setDeleted(0); tasteMapper.insert(option); }
            else tasteMapper.updateById(option);
        }
        if (!existing.isEmpty()) tasteMapper.delete(new LambdaQueryWrapper<ProductTaste>()
                .eq(ProductTaste::getProductId, productId).in(ProductTaste::getId, existing.keySet()));
    }

    private Long shopId() { return PrincipalContext.require(PrincipalType.MERCHANT).shopId(); }
    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
