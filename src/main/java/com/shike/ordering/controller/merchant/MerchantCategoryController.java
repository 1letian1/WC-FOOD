package com.shike.ordering.controller.merchant;

import com.shike.ordering.common.result.Result;
import com.shike.ordering.dto.merchant.CategorySaveDTO;
import com.shike.ordering.dto.merchant.CategoryStatusDTO;
import com.shike.ordering.service.merchant.MerchantCategoryService;
import com.shike.ordering.vo.merchant.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "商家端-分类管理")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/merchant/categories")
@RequiredArgsConstructor
public class MerchantCategoryController {
    private final MerchantCategoryService service;

    @Operation(summary = "查询本店分类")
    @GetMapping public Result<List<CategoryVO>> list() { return Result.success(service.list()); }

    @Operation(summary = "新增分类")
    @PostMapping public Result<CategoryVO> create(@Valid @RequestBody CategorySaveDTO request) {
        return Result.success(service.create(request));
    }

    @Operation(summary = "编辑分类")
    @PutMapping("/{id}")
    public Result<CategoryVO> update(@PathVariable @Positive(message = "分类ID必须为正数") Long id,
                                     @Valid @RequestBody CategorySaveDTO request) {
        return Result.success(service.update(id, request));
    }

    @Operation(summary = "启用或停用分类")
    @PutMapping("/{id}/status")
    public Result<CategoryVO> status(@PathVariable @Positive(message = "分类ID必须为正数") Long id,
                                     @Valid @RequestBody CategoryStatusDTO request) {
        return Result.success(service.updateStatus(id, request.status()));
    }

    @Operation(summary = "删除无商品关联的分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive(message = "分类ID必须为正数") Long id) {
        service.delete(id);
        return Result.success();
    }
}
