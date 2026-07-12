package com.shike.ordering.controller.common;

import com.shike.ordering.common.result.Result;
import com.shike.ordering.config.ShopProperties;
import com.shike.ordering.dto.common.ShopQueryDTO;
import com.shike.ordering.service.common.ShopQueryService;
import com.shike.ordering.vo.common.ShopPublicVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "公共接口")
@RestController
@RequestMapping("/api/common/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ShopQueryService shopQueryService;
    private final ShopProperties shopProperties;

    @Operation(summary = "查询店铺公开信息", description = "无需登录，返回默认单店铺的营业和履约配置")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "404", description = "默认店铺不存在")
    @GetMapping
    public Result<ShopPublicVO> getPublicShop() {
        return Result.success(shopQueryService.getPublicShop(new ShopQueryDTO(shopProperties.defaultShopId())));
    }
}
