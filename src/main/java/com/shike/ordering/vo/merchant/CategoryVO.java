package com.shike.ordering.vo.merchant;

import com.shike.ordering.vo.common.StatusVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "商家分类信息")
public record CategoryVO(@Schema(description = "分类 ID") Long id,
                         @Schema(description = "分类名称") String name,
                         @Schema(description = "启用状态") StatusVO status,
                         @Schema(description = "排序值") Integer sort) { }
