package com.shike.ordering.controller.merchant;

import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.common.result.Result;
import com.shike.ordering.dto.merchant.ProductQueryDTO;
import com.shike.ordering.dto.merchant.ProductSaveDTO;
import com.shike.ordering.service.merchant.MerchantProductService;
import com.shike.ordering.vo.common.ProductDetailVO;
import com.shike.ordering.vo.common.ProductSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家端-商品管理")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/merchant/products")
@RequiredArgsConstructor
public class MerchantProductController {
    private final MerchantProductService service;

    @Operation(summary = "分页查询本店商品")
    @GetMapping
    public Result<PageResult<ProductSummaryVO>> list(@Valid @ModelAttribute ProductQueryDTO query) {
        return Result.success(service.list(query));
    }

    @Operation(summary = "查询本店商品详情")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable @Positive(message = "商品ID必须为正数") Long id) {
        return Result.success(service.detail(id));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Result<ProductDetailVO> create(@Valid @RequestBody ProductSaveDTO request) {
        return Result.success(service.create(request));
    }

    @Operation(summary = "编辑商品及规格口味")
    @PutMapping("/{id}")
    public Result<ProductDetailVO> update(@PathVariable @Positive(message = "商品ID必须为正数") Long id,
                                          @Valid @RequestBody ProductSaveDTO request) {
        return Result.success(service.update(id, request));
    }

    @Operation(summary = "上架商品")
    @PutMapping("/{id}/on-sale")
    public Result<ProductDetailVO> onSale(@PathVariable @Positive(message = "商品ID必须为正数") Long id) {
        return Result.success(service.onSale(id));
    }

    @Operation(summary = "下架商品")
    @PutMapping("/{id}/off-sale")
    public Result<ProductDetailVO> offSale(@PathVariable @Positive(message = "商品ID必须为正数") Long id) {
        return Result.success(service.offSale(id));
    }

    @Operation(summary = "标记商品售罄")
    @PutMapping("/{id}/sold-out")
    public Result<ProductDetailVO> soldOut(@PathVariable @Positive(message = "商品ID必须为正数") Long id) {
        return Result.success(service.soldOut(id));
    }

    @Operation(summary = "逻辑删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive(message = "商品ID必须为正数") Long id) {
        service.delete(id);
        return Result.success();
    }
}
