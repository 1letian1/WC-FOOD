package com.shike.ordering.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class OpenApiConfig {
    @Bean public OpenAPI openAPI() { return new OpenAPI().info(new Info().title("食刻小馆后端 API").version("v1"))
            .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("opaque-token"))); }
    @Bean public GroupedOpenApi commonApi() { return group("common", "/api/common/**"); }
    @Bean public GroupedOpenApi userApi() { return group("user", "/api/user/**"); }
    @Bean public GroupedOpenApi merchantApi() { return group("merchant", "/api/merchant/**"); }
    private GroupedOpenApi group(String name, String path) { return GroupedOpenApi.builder().group(name).pathsToMatch(path).build(); }
}
