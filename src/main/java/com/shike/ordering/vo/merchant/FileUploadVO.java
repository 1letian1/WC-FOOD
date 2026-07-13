package com.shike.ordering.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "图片上传结果")
public record FileUploadVO(@Schema(description = "公开访问地址") String url,
                           @Schema(description = "服务端相对路径") String path) { }
