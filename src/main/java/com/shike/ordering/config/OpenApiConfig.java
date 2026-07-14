package com.shike.ordering.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        Components components = new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP)
                        .scheme("bearer").bearerFormat("opaque-token")
                        .description("登录接口返回的随机 Token，格式：Bearer <token>"))
                .addResponses("BadRequest", errorResponse("请求参数或格式错误", 10001, "参数错误"))
                .addResponses("Unauthorized", errorResponse("未登录或登录已过期", 20001, "未登录或登录已过期"))
                .addResponses("Forbidden", errorResponse("无权限访问", 20002, "无权限访问"))
                .addResponses("NotFound", errorResponse("业务数据不存在", 10004, "接口或数据不存在"))
                .addResponses("Conflict", errorResponse("状态冲突或重复操作", 60002, "订单状态已变化"))
                .addResponses("ServerError", errorResponse("未知服务器异常", 99999, "系统繁忙，请稍后重试"));
        return new OpenAPI()
                .info(new Info().title("食刻小馆后端 API").version("v1")
                        .description("单店铺微信点餐小程序后端。业务成功 code=0，失败同时使用稳定业务错误码和 HTTP 状态码。"))
                .components(components);
    }

    @Bean
    public OpenApiCustomizer standardResponses() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            if (operation.getDescription() == null || operation.getDescription().isBlank()) {
                operation.setDescription(operation.getSummary());
            }
            operation.getResponses().putIfAbsent("200", new ApiResponse().description("业务处理成功"));
            operation.getResponses().putIfAbsent("400", reference("BadRequest"));
            operation.getResponses().putIfAbsent("500", reference("ServerError"));
            if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                operation.getResponses().putIfAbsent("401", reference("Unauthorized"));
                operation.getResponses().putIfAbsent("403", reference("Forbidden"));
            }
        }));
    }

    @Bean public GroupedOpenApi commonApi() { return group("common", "/api/common/**"); }
    @Bean public GroupedOpenApi userApi() { return group("user", "/api/user/**"); }
    @Bean public GroupedOpenApi merchantApi() { return group("merchant", "/api/merchant/**"); }
    private GroupedOpenApi group(String name, String path) { return GroupedOpenApi.builder().group(name).pathsToMatch(path).build(); }

    private ApiResponse errorResponse(String description, int code, String message) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("code", code);
        example.put("message", message);
        example.put("data", null);
        example.put("requestId", "8c8b9c27bafd4d6f");
        example.put("timestamp", "2026-07-14T10:30:00");
        MediaType mediaType = new MediaType().schema(new ObjectSchema().description("统一错误响应")).example(example);
        return new ApiResponse().description(description).content(new Content().addMediaType("application/json", mediaType));
    }

    private ApiResponse reference(String name) {
        return new ApiResponse().$ref("#/components/responses/" + name);
    }
}
