package com.shike.ordering.controller.merchant;

import com.shike.ordering.auth.service.AuthenticationService;
import com.shike.ordering.common.result.Result;
import com.shike.ordering.dto.merchant.ChangePasswordDTO;
import com.shike.ordering.dto.merchant.MerchantLoginDTO;
import com.shike.ordering.vo.merchant.MerchantLoginVO;
import com.shike.ordering.vo.merchant.MerchantProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家端-登录与账号")
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantAuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "商家账号密码登录", description = "连续失败5次后锁定30分钟")
    @PostMapping("/auth/login")
    public Result<MerchantLoginVO> login(@Valid @RequestBody MerchantLoginDTO request) {
        return Result.success(authenticationService.loginMerchant(request.username(), request.password()));
    }

    @Operation(summary = "退出商家登录")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        authenticationService.logout();
        return Result.success();
    }

    @Operation(summary = "查询当前商家资料")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public Result<MerchantProfileVO> profile() {
        return Result.success(authenticationService.currentMerchant());
    }

    @Operation(summary = "修改商家密码", description = "成功后该商家的全部旧 Token 立即失效")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        authenticationService.changeMerchantPassword(request.currentPassword(), request.newPassword());
        return Result.success();
    }

}
