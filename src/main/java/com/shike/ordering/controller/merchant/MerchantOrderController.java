package com.shike.ordering.controller.merchant;

import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.common.result.Result;
import com.shike.ordering.dto.merchant.MerchantOrderQueryDTO;
import com.shike.ordering.dto.merchant.OrderRejectDTO;
import com.shike.ordering.service.merchant.MerchantOrderService;
import com.shike.ordering.vo.common.OrderDetailVO;
import com.shike.ordering.vo.common.OrderSummaryVO;
import com.shike.ordering.vo.merchant.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家端-订单履约")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantOrderController {
    private final MerchantOrderService service;

    @Operation(summary = "查询工作台订单统计")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() { return Result.success(service.dashboard()); }

    @Operation(summary = "分页查询本店订单")
    @GetMapping("/orders")
    public Result<PageResult<OrderSummaryVO>> list(@Valid @ModelAttribute MerchantOrderQueryDTO query) {
        return Result.success(service.list(query));
    }

    @Operation(summary = "查询本店订单详情")
    @GetMapping("/orders/{id}")
    public Result<OrderDetailVO> detail(@PathVariable @Positive(message = "订单ID必须为正数") Long id) {
        return Result.success(service.detail(id));
    }

    @Operation(summary = "接单")
    @PutMapping("/orders/{id}/accept")
    public Result<Void> accept(@PathVariable @Positive(message = "订单ID必须为正数") Long id) {
        service.accept(id); return Result.success();
    }

    @Operation(summary = "拒单", description = "仅待接单订单可拒绝，并在同一事务恢复库存")
    @PutMapping("/orders/{id}/reject")
    public Result<Void> reject(@PathVariable @Positive(message = "订单ID必须为正数") Long id,
                               @Valid @RequestBody OrderRejectDTO request) {
        service.reject(id, request.reason()); return Result.success();
    }

    @Operation(summary = "开始制作")
    @PutMapping("/orders/{id}/start-cooking")
    public Result<Void> startCooking(@PathVariable @Positive(message = "订单ID必须为正数") Long id) {
        service.startCooking(id); return Result.success();
    }

    @Operation(summary = "堂食订单标记待取餐")
    @PutMapping("/orders/{id}/ready-for-pickup")
    public Result<Void> ready(@PathVariable @Positive(message = "订单ID必须为正数") Long id) {
        service.readyForPickup(id); return Result.success();
    }

    @Operation(summary = "配送订单开始配送")
    @PutMapping("/orders/{id}/start-delivery")
    public Result<Void> delivery(@PathVariable @Positive(message = "订单ID必须为正数") Long id) {
        service.startDelivery(id); return Result.success();
    }

    @Operation(summary = "配送订单标记已送达")
    @PutMapping("/orders/{id}/mark-delivered")
    public Result<Void> delivered(@PathVariable @Positive(message = "订单ID必须为正数") Long id) {
        service.markDelivered(id); return Result.success();
    }

    @Operation(summary = "完成堂食订单")
    @PutMapping("/orders/{id}/complete")
    public Result<Void> complete(@PathVariable @Positive(message = "订单ID必须为正数") Long id) {
        service.complete(id); return Result.success();
    }
}
