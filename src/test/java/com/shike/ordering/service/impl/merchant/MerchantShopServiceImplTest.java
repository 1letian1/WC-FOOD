package com.shike.ordering.service.impl.merchant;

import com.shike.ordering.auth.model.CurrentPrincipal;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.converter.ShopConverter;
import com.shike.ordering.entity.Shop;
import com.shike.ordering.mapper.ShopMapper;
import com.shike.ordering.service.common.ShopCacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;

class MerchantShopServiceImplTest {
    private final ShopMapper shopMapper = mock(ShopMapper.class);
    private final ShopCacheService cacheService = mock(ShopCacheService.class);
    private final MerchantShopServiceImpl service =
            new MerchantShopServiceImpl(shopMapper, new ShopConverter(), cacheService);

    @BeforeEach void setPrincipal() {
        PrincipalContext.set(new CurrentPrincipal(2L, PrincipalType.MERCHANT, 7L, "token"));
    }
    @AfterEach void clearPrincipal() { PrincipalContext.clear(); }

    @Test
    void updateBusinessStatus_shouldUseSessionShopAndEvictCache() {
        Shop shop = shop();
        when(shopMapper.selectById(7L)).thenReturn(shop);
        when(shopMapper.updateById(shop)).thenReturn(1);

        service.updateBusinessStatus(0);

        verify(shopMapper).selectById(7L);
        verify(cacheService).evict(7L);
    }

    private Shop shop() {
        Shop shop = new Shop();
        shop.setId(7L);
        shop.setName("店铺");
        shop.setPhone("13800000000");
        shop.setAddress("地址");
        shop.setBusinessHours("09:00-22:00");
        shop.setBusinessStatus(1);
        shop.setDineInEnabled(true);
        shop.setDeliveryEnabled(true);
        shop.setDeliveryFee(BigDecimal.ZERO);
        shop.setMinDeliveryAmount(BigDecimal.ZERO);
        shop.setPackageFee(BigDecimal.ZERO);
        shop.setVersion(0);
        return shop;
    }
}
