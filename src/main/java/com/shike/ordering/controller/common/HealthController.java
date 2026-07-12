package com.shike.ordering.controller.common;
import com.shike.ordering.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
@Tag(name = "公共接口")
@RestController @RequestMapping("/api/common")
public class HealthController {
    @Operation(summary = "健康检查", description = "检查应用进程是否可以正常响应")
    @GetMapping("/health")
    public Result<Map<String, String>> health() { return Result.success(Map.of("status", "UP")); }
}
