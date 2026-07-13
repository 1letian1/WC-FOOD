package com.shike.ordering.controller.merchant;

import com.shike.ordering.common.result.Result;
import com.shike.ordering.storage.StorageService;
import com.shike.ordering.vo.merchant.FileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "商家端-文件上传")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/merchant/files")
@RequiredArgsConstructor
public class MerchantFileController {
    private final StorageService storageService;

    @Operation(summary = "上传商品或店铺图片", description = "支持 JPEG、PNG、GIF，最大5MB")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileUploadVO> uploadImage(@RequestPart("file") MultipartFile file) {
        StorageService.StoredFile stored = storageService.storeImage(file);
        return Result.success(new FileUploadVO(stored.url(), stored.path()));
    }
}
