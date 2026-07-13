package com.shike.ordering.controller.user;

import com.shike.ordering.common.result.Result;
import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.dto.user.OrderCreateDTO;
import com.shike.ordering.dto.user.OrderQueryDTO;
import com.shike.ordering.service.user.OrderCreationService;
import com.shike.ordering.service.user.UserOrderService;
import com.shike.ordering.vo.user.OrderCreateVO;
import com.shike.ordering.vo.common.OrderDetailVO;
import com.shike.ordering.vo.common.OrderSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-订单")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderCreationService creationService;
    private final UserOrderService orderService;

    @Operation(summary = "创建堂食或配送订单", description = "服务端重算金额、扣减库存并保存商品和地址快照")
    @PostMapping
    public Result<OrderCreateVO> create(
            @Parameter(description = "客户端生成的随机幂等键，成功重试返回同一订单", required = true)
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "幂等键不能为空") @Size(min = 8, max = 64, message = "幂等键长度必须为8至64位")
            @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "幂等键格式不正确") String idempotencyKey,
            @Valid @RequestBody OrderCreateDTO request) {
        return Result.success(creationService.create(request, idempotencyKey));
    }
    @Operation(summary = "分页查询本人订单") @GetMapping
    public Result<PageResult<OrderSummaryVO>> list(@Valid @ModelAttribute OrderQueryDTO query) { return Result.success(orderService.list(query)); }
    @Operation(summary = "查询本人订单详情") @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable @jakarta.validation.constraints.Positive(message="订单ID必须为正数") Long id) { return Result.success(orderService.detail(id)); }
    @Operation(summary = "取消待接单订单") @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable @jakarta.validation.constraints.Positive(message="订单ID必须为正数") Long id) { orderService.cancel(id); return Result.success(); }
    @Operation(summary = "确认配送订单收货") @PutMapping("/{id}/confirm-receipt")
    public Result<Void> confirmReceipt(@PathVariable @jakarta.validation.constraints.Positive(message="订单ID必须为正数") Long id) { orderService.confirmReceipt(id); return Result.success(); }
}
