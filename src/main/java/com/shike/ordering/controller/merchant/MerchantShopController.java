package com.shike.ordering.controller.merchant;

import com.shike.ordering.common.result.Result;
import com.shike.ordering.dto.merchant.BusinessStatusUpdateDTO;
import com.shike.ordering.dto.merchant.EnabledUpdateDTO;
import com.shike.ordering.dto.merchant.ShopUpdateDTO;
import com.shike.ordering.service.merchant.MerchantShopService;
import com.shike.ordering.vo.merchant.ShopVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家端-店铺设置")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/merchant/shop")
@RequiredArgsConstructor
public class MerchantShopController {
    private final MerchantShopService service;

    @Operation(summary = "查询本店完整设置")
    @GetMapping public Result<ShopVO> get() { return Result.success(service.getShop()); }

    @Operation(summary = "修改本店基础和配送设置")
    @PutMapping public Result<ShopVO> update(@Valid @RequestBody ShopUpdateDTO request) {
        return Result.success(service.updateShop(request));
    }

    @Operation(summary = "修改营业状态")
    @PutMapping("/business-status")
    public Result<ShopVO> businessStatus(@Valid @RequestBody BusinessStatusUpdateDTO request) {
        return Result.success(service.updateBusinessStatus(request.status()));
    }

    @Operation(summary = "修改堂食开放状态")
    @PutMapping("/dine-in-status")
    public Result<ShopVO> dineInStatus(@Valid @RequestBody EnabledUpdateDTO request) {
        return Result.success(service.updateDineInStatus(request.enabled()));
    }

    @Operation(summary = "修改配送开放状态")
    @PutMapping("/delivery-status")
    public Result<ShopVO> deliveryStatus(@Valid @RequestBody EnabledUpdateDTO request) {
        return Result.success(service.updateDeliveryStatus(request.enabled()));
    }
}
