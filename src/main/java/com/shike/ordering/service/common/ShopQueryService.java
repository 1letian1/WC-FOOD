package com.shike.ordering.service.common;

import com.shike.ordering.dto.common.ShopQueryDTO;
import com.shike.ordering.vo.common.ShopPublicVO;
import jakarta.validation.Valid;

public interface ShopQueryService {
    ShopPublicVO getPublicShop(@Valid ShopQueryDTO query);
}
