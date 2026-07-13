package com.shike.ordering.controller.user;

import com.shike.ordering.common.result.Result;
import com.shike.ordering.dto.user.OrderCreateDTO;
import com.shike.ordering.service.user.OrderCreationService;
import com.shike.ordering.vo.user.OrderCreateVO;
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
    private final OrderCreationService service;

    @Operation(summary = "创建堂食或配送订单", description = "服务端重算金额、扣减库存并保存商品和地址快照")
    @PostMapping
    public Result<OrderCreateVO> create(
            @Parameter(description = "客户端生成的随机幂等键，成功重试返回同一订单", required = true)
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "幂等键不能为空") @Size(min = 8, max = 64, message = "幂等键长度必须为8至64位")
            @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "幂等键格式不正确") String idempotencyKey,
            @Valid @RequestBody OrderCreateDTO request) {
        return Result.success(service.create(request, idempotencyKey));
    }
}
