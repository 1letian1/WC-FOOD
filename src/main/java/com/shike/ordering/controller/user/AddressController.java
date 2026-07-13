package com.shike.ordering.controller.user;

import com.shike.ordering.common.result.Result;
import com.shike.ordering.dto.user.AddressSaveDTO;
import com.shike.ordering.service.user.AddressService;
import com.shike.ordering.vo.user.AddressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "用户端-收货地址")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService service;
    @Operation(summary = "查询本人收货地址")
    @GetMapping public Result<List<AddressVO>> list() { return Result.success(service.list()); }
    @Operation(summary = "查询本人收货地址详情")
    @GetMapping("/{id}") public Result<AddressVO> detail(@PathVariable @Positive(message = "地址ID必须为正数") Long id) { return Result.success(service.detail(id)); }
    @Operation(summary = "新增收货地址")
    @PostMapping public Result<AddressVO> create(@Valid @RequestBody AddressSaveDTO request) { return Result.success(service.create(request)); }
    @Operation(summary = "修改本人收货地址")
    @PutMapping("/{id}")
    public Result<AddressVO> update(@PathVariable @Positive(message = "地址ID必须为正数") Long id,
                                    @Valid @RequestBody AddressSaveDTO request) { return Result.success(service.update(id, request)); }
    @Operation(summary = "设为本人唯一默认地址")
    @PutMapping("/{id}/default") public Result<AddressVO> setDefault(@PathVariable @Positive(message = "地址ID必须为正数") Long id) { return Result.success(service.setDefault(id)); }
    @Operation(summary = "逻辑删除本人收货地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive(message = "地址ID必须为正数") Long id) { service.delete(id); return Result.success(); }
}
