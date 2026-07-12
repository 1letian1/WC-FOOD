package com.shike.ordering.controller.user;

import com.shike.ordering.common.result.Result;
import com.shike.ordering.config.ShopProperties;
import com.shike.ordering.dto.user.CategoryQueryDTO;
import com.shike.ordering.service.user.CategoryQueryService;
import com.shike.ordering.vo.user.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户端商品目录")
@RestController
@RequestMapping("/api/user/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryQueryService categoryQueryService;
    private final ShopProperties shopProperties;

    @Operation(summary = "查询商品分类", description = "无需登录，返回默认店铺已启用且未删除的分类")
    @ApiResponse(responseCode = "200", description = "查询成功；没有分类时返回空列表")
    @GetMapping
    public Result<List<CategoryVO>> listCategories() {
        return Result.success(categoryQueryService.listEnabledCategories(
                new CategoryQueryDTO(shopProperties.defaultShopId())));
    }
}
