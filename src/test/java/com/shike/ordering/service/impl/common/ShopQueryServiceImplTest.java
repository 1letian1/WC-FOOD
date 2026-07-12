package com.shike.ordering.service.impl.common;

import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.converter.ShopConverter;
import com.shike.ordering.dto.common.ShopQueryDTO;
import com.shike.ordering.entity.Shop;
import com.shike.ordering.mapper.ShopMapper;
import com.shike.ordering.vo.common.ShopPublicVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShopQueryServiceImplTest {
    private ShopMapper shopMapper;
    private ShopQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        shopMapper = mock(ShopMapper.class);
        service = new ShopQueryServiceImpl(shopMapper, new ShopConverter());
    }

    @Test
    void getPublicShop_whenShopExists_shouldReturnPublicView() {
        Shop shop = shop();
        when(shopMapper.selectPublicShopById(1L)).thenReturn(shop);

        ShopPublicVO result = service.getPublicShop(new ShopQueryDTO(1L));

        assertThat(result.name()).isEqualTo("食刻小馆");
        assertThat(result.businessStatus().code()).isEqualTo(1);
        assertThat(result.businessStatus().description()).isEqualTo("营业中");
        assertThat(result.deliveryFee()).isEqualByComparingTo("3.00");
        verify(shopMapper).selectPublicShopById(1L);
    }

    @Test
    void getPublicShop_whenShopMissing_shouldThrowStableNotFoundError() {
        when(shopMapper.selectPublicShopById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.getPublicShop(new ShopQueryDTO(1L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("code")
                .isEqualTo(70005);
    }

    private Shop shop() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setName("食刻小馆");
        shop.setPhone("13800000000");
        shop.setAddress("测试地址");
        shop.setBusinessHours("09:00-22:00");
        shop.setBusinessStatus(1);
        shop.setDineInEnabled(true);
        shop.setDeliveryEnabled(true);
        shop.setDeliveryFee(new BigDecimal("3.00"));
        shop.setMinDeliveryAmount(new BigDecimal("20.00"));
        shop.setPackageFee(BigDecimal.ZERO);
        shop.setEstimatedDeliveryMinutes(30);
        return shop;
    }
}
