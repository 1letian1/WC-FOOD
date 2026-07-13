package com.shike.ordering.controller.user;

import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.common.result.Result;
import com.shike.ordering.dto.user.ProductQueryDTO;
import com.shike.ordering.service.user.ProductQueryService;
import com.shike.ordering.vo.common.ProductDetailVO;
import com.shike.ordering.vo.common.ProductSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端商品目录")
@Validated
@RestController
@RequestMapping("/api/user/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductQueryService service;

    @Operation(summary = "分页浏览商品", description = "无需登录，固定按推荐和商品ID排序")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @GetMapping
    public Result<PageResult<ProductSummaryVO>> list(@Valid @ModelAttribute ProductQueryDTO query) {
        return Result.success(service.list(query));
    }

    @Operation(summary = "查询商品详情", description = "无需登录，返回有效规格和口味")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable @Positive(message = "商品ID必须为正数") Long id) {
        return Result.success(service.detail(id));
    }
}
