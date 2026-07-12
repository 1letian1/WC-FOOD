package com.shike.ordering.vo.user;

import com.shike.ordering.vo.common.StatusVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户端商品分类")
public record CategoryVO(
        @Schema(description = "分类ID", example = "1") Long id,
        @Schema(description = "分类名称", example = "热门推荐") String name,
        @Schema(description = "分类状态") StatusVO status,
        @Schema(description = "展示顺序，数值越小越靠前", example = "10") Integer sort) {
}
