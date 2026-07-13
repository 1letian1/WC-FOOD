package com.shike.ordering.service.impl.common;

import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.converter.ShopConverter;
import com.shike.ordering.dto.common.ShopQueryDTO;
import com.shike.ordering.entity.Shop;
import com.shike.ordering.mapper.ShopMapper;
import com.shike.ordering.service.common.ShopQueryService;
import com.shike.ordering.service.common.ShopCacheService;
import com.shike.ordering.vo.common.ShopPublicVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class ShopQueryServiceImpl implements ShopQueryService {
    private final ShopMapper shopMapper;
    private final ShopConverter shopConverter;
    private final ShopCacheService shopCacheService;

    @Override
    @Transactional(readOnly = true)
    public ShopPublicVO getPublicShop(@Valid ShopQueryDTO query) {
        var cached = shopCacheService.get(query.shopId());
        if (cached.isPresent()) return cached.get();
        Shop shop = shopMapper.selectPublicShopById(query.shopId());
        if (shop == null) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND);
        }
        ShopPublicVO result = shopConverter.toPublicVO(shop);
        shopCacheService.put(query.shopId(), result);
        return result;
    }
}
