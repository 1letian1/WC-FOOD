package com.shike.ordering.service.merchant;

import com.shike.ordering.dto.merchant.ShopUpdateDTO;
import com.shike.ordering.vo.merchant.ShopVO;

public interface MerchantShopService {
    ShopVO getShop();
    ShopVO updateShop(ShopUpdateDTO request);
    ShopVO updateBusinessStatus(Integer status);
    ShopVO updateDineInStatus(Boolean enabled);
    ShopVO updateDeliveryStatus(Boolean enabled);
}
