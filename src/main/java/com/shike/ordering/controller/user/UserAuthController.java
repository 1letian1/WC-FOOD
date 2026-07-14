package com.shike.ordering.controller.user;

import com.shike.ordering.auth.service.AuthenticationService;
import com.shike.ordering.common.result.Result;
import com.shike.ordering.dto.user.UserProfileUpdateDTO;
import com.shike.ordering.dto.user.WechatLoginDTO;
import com.shike.ordering.vo.user.UserLoginVO;
import com.shike.ordering.vo.user.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-登录与资料")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserAuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "微信快捷登录", description = "使用 wx.login 的一次性 code 换取 Token，Token 仅在本响应返回")
    @ApiResponse(responseCode = "200", description = "登录成功")
    @PostMapping("/auth/wechat-login")
    public Result<UserLoginVO> login(@Valid @RequestBody WechatLoginDTO request) {
        return Result.success(authenticationService.loginUser(request.code()));
    }

    @Operation(summary = "退出用户登录")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        authenticationService.logout();
        return Result.success();
    }

    @Operation(summary = "查询当前用户资料")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        return Result.success(authenticationService.currentUser());
    }

    @Operation(summary = "修改当前用户资料", description = "仅更新请求中提供的昵称、头像地址或手机号")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@Valid @RequestBody UserProfileUpdateDTO request) {
        return Result.success(authenticationService.updateCurrentUser(request));
    }
}
