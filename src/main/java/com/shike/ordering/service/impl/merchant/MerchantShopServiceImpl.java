package com.shike.ordering.service.impl.merchant;

import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.converter.ShopConverter;
import com.shike.ordering.dto.merchant.ShopUpdateDTO;
import com.shike.ordering.entity.Shop;
import com.shike.ordering.mapper.ShopMapper;
import com.shike.ordering.service.common.ShopCacheService;
import com.shike.ordering.service.merchant.MerchantShopService;
import com.shike.ordering.vo.merchant.ShopVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantShopServiceImpl implements MerchantShopService {
    private final ShopMapper shopMapper;
    private final ShopConverter converter;
    private final ShopCacheService cacheService;

    @Override
    public ShopVO getShop() {
        return converter.toMerchantVO(requireShop());
    }

    @Override
    public ShopVO updateShop(ShopUpdateDTO request) {
        return update(shop -> {
            shop.setName(request.name().trim());
            shop.setLogoUrl(trimToNull(request.logoUrl()));
            shop.setPhone(request.phone().trim());
            shop.setAddress(request.address().trim());
            shop.setNotice(trimToNull(request.notice()));
            shop.setBusinessHours(request.businessHours().trim());
            shop.setDeliveryFee(request.deliveryFee());
            shop.setMinDeliveryAmount(request.minDeliveryAmount());
            shop.setPackageFee(request.packageFee());
            shop.setDeliveryRange(trimToNull(request.deliveryRange()));
            shop.setEstimatedDeliveryMinutes(request.estimatedDeliveryMinutes());
        });
    }

    @Override public ShopVO updateBusinessStatus(Integer status) { return update(shop -> shop.setBusinessStatus(status)); }
    @Override public ShopVO updateDineInStatus(Boolean enabled) { return update(shop -> shop.setDineInEnabled(enabled)); }
    @Override public ShopVO updateDeliveryStatus(Boolean enabled) { return update(shop -> shop.setDeliveryEnabled(enabled)); }

    private ShopVO update(Consumer<Shop> mutation) {
        Shop shop = requireShop();
        mutation.accept(shop);
        if (shopMapper.updateById(shop) != 1) throw new BusinessException(ErrorCode.DATA_CONFLICT);
        cacheService.evict(shop.getId());
        log.info("shop settings changed, shopId={}", shop.getId());
        return converter.toMerchantVO(shop);
    }

    private Shop requireShop() {
        Long shopId = PrincipalContext.require(PrincipalType.MERCHANT).shopId();
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) throw new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND);
        return shop;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
