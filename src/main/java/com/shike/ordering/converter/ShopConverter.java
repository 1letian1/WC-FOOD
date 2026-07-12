package com.shike.ordering.converter;

import com.shike.ordering.common.enums.BusinessStatus;
import com.shike.ordering.common.enums.EnabledStatus;
import com.shike.ordering.entity.Shop;
import com.shike.ordering.vo.common.ShopPublicVO;
import com.shike.ordering.vo.common.StatusVO;
import org.springframework.stereotype.Component;

@Component
public class ShopConverter {
    public ShopPublicVO toPublicVO(Shop shop) {
        BusinessStatus businessStatus = BusinessStatus.fromCode(shop.getBusinessStatus());
        EnabledStatus dineInStatus = EnabledStatus.fromBoolean(Boolean.TRUE.equals(shop.getDineInEnabled()));
        EnabledStatus deliveryStatus = EnabledStatus.fromBoolean(Boolean.TRUE.equals(shop.getDeliveryEnabled()));
        return new ShopPublicVO(shop.getId(), shop.getName(), shop.getLogoUrl(), shop.getPhone(), shop.getAddress(),
                shop.getNotice(), shop.getBusinessHours(), status(businessStatus.getCode(), businessStatus.getDescription()),
                status(dineInStatus.getCode(), dineInStatus.getDescription()),
                status(deliveryStatus.getCode(), deliveryStatus.getDescription()), shop.getDeliveryFee(),
                shop.getMinDeliveryAmount(), shop.getPackageFee(), shop.getDeliveryRange(),
                shop.getEstimatedDeliveryMinutes());
    }

    private StatusVO status(int code, String description) {
        return new StatusVO(code, description);
    }
}
