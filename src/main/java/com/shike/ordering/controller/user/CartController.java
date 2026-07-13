package com.shike.ordering.controller.user;

import com.shike.ordering.common.result.Result;
import com.shike.ordering.dto.user.CartItemAddDTO;
import com.shike.ordering.dto.user.CartItemQuantityDTO;
import com.shike.ordering.service.user.CartService;
import com.shike.ordering.vo.user.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-购物车")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/user/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService service;
    @Operation(summary = "查询本人购物车", description = "返回当前商品价格预览；下单时服务端仍会重新校验和计算")
    @GetMapping public Result<CartVO> getCart() { return Result.success(service.getCart()); }
    @Operation(summary = "加入购物车", description = "相同商品、规格和口味组合会合并数量")
    @PostMapping("/items") public Result<CartVO> add(@Valid @RequestBody CartItemAddDTO request) { return Result.success(service.add(request)); }
    @Operation(summary = "修改本人购物车条目数量")
    @PutMapping("/items/{id}")
    public Result<CartVO> updateQuantity(@PathVariable @Positive(message = "购物车条目ID必须为正数") Long id,
                                         @Valid @RequestBody CartItemQuantityDTO request) {
        return Result.success(service.updateQuantity(id, request));
    }
    @Operation(summary = "删除本人购物车条目")
    @DeleteMapping("/items/{id}")
    public Result<Void> delete(@PathVariable @Positive(message = "购物车条目ID必须为正数") Long id) {
        service.delete(id); return Result.success();
    }
    @Operation(summary = "清空本人购物车")
    @DeleteMapping("/items") public Result<Void> clear() { service.clear(); return Result.success(); }
}
