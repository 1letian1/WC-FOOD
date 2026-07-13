package com.shike.ordering.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.shike.ordering.auth.model.CurrentPrincipal;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.config.ShopProperties;
import com.shike.ordering.dto.user.CartItemAddDTO;
import com.shike.ordering.entity.Category;
import com.shike.ordering.entity.Product;
import com.shike.ordering.entity.ShoppingCart;
import com.shike.ordering.mapper.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceImplTest {
    private final ShoppingCartMapper cartMapper = mock(ShoppingCartMapper.class);
    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final CategoryMapper categoryMapper = mock(CategoryMapper.class);
    private final ProductSpecificationMapper specificationMapper = mock(ProductSpecificationMapper.class);
    private final ProductTasteMapper tasteMapper = mock(ProductTasteMapper.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final CartServiceImpl service = new CartServiceImpl(cartMapper, productMapper, categoryMapper,
            specificationMapper, tasteMapper, userMapper, new ShopProperties(1L));

    @BeforeEach
    void setPrincipal() {
        PrincipalContext.set(new CurrentPrincipal(12L, PrincipalType.USER, null, "token"));
        when(productMapper.selectOne(any(Wrapper.class))).thenReturn(product(10));
        when(categoryMapper.selectOne(any(Wrapper.class))).thenReturn(new Category());
        when(cartMapper.selectUserCart(12L)).thenReturn(List.of());
    }

    @AfterEach void clearPrincipal() { PrincipalContext.clear(); }

    @Test
    void addSameCombination_shouldMergeQuantityAfterLockingUser() {
        ShoppingCart existing = new ShoppingCart();
        existing.setId(8L);
        existing.setUserId(12L);
        existing.setProductId(3L);
        existing.setSpecificationId(0L);
        existing.setTasteId(0L);
        existing.setQuantity(2);
        when(cartMapper.selectOne(any(Wrapper.class))).thenReturn(existing);

        service.add(new CartItemAddDTO(3L, null, null, 3));

        ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
        verify(cartMapper).updateById(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(5);
        verify(cartMapper, never()).insert(any(ShoppingCart.class));
        InOrder order = inOrder(userMapper, cartMapper);
        order.verify(userMapper).selectIdForUpdate(12L);
        order.verify(cartMapper).selectOne(any(Wrapper.class));
    }

    @Test
    void addSameCombination_whenMergedQuantityExceedsLimit_shouldFailWithoutWriting() {
        ShoppingCart existing = new ShoppingCart();
        existing.setQuantity(98);
        when(cartMapper.selectOne(any(Wrapper.class))).thenReturn(existing);

        assertThatThrownBy(() -> service.add(new CartItemAddDTO(3L, null, null, 2)))
                .isInstanceOf(BusinessException.class).extracting("code").isEqualTo(50004);
        verify(cartMapper, never()).insert(any(ShoppingCart.class));
        verify(cartMapper, never()).updateById(any(ShoppingCart.class));
    }

    @Test
    void deleteOtherUsersItem_shouldReturnNotFound() {
        when(cartMapper.delete(any(Wrapper.class))).thenReturn(0);

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class)
                .extracting("code").isEqualTo(50003);
        verify(cartMapper).delete(any(Wrapper.class));
    }

    private Product product(int stock) {
        Product product = new Product();
        product.setId(3L);
        product.setShopId(1L);
        product.setCategoryId(2L);
        product.setStatus(1);
        product.setStock(stock);
        return product;
    }

}
