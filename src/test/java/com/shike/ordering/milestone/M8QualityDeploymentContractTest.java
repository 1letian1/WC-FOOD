package com.shike.ordering.milestone;

import com.shike.ordering.config.OpenApiConfig;
import com.shike.ordering.controller.common.HealthController;
import com.shike.ordering.controller.common.ShopController;
import com.shike.ordering.controller.merchant.*;
import com.shike.ordering.controller.user.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class M8QualityDeploymentContractTest {
    private static final Path ROOT = Path.of(".");

    @Test
    void openApi_shouldDocumentEveryEndpointAndStandardResponses() {
        List<Class<?>> controllers = List.of(
                HealthController.class, ShopController.class,
                MerchantAuthController.class, MerchantCategoryController.class, MerchantFileController.class,
                MerchantOrderController.class, MerchantProductController.class, MerchantShopController.class,
                AddressController.class, CartController.class, CategoryController.class, OrderController.class,
                ProductController.class, UserAuthController.class);

        controllers.stream().flatMap(type -> List.of(type.getDeclaredMethods()).stream())
                .filter(method -> AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class) != null)
                .forEach(method -> {
                    Operation operation = AnnotatedElementUtils.findMergedAnnotation(method, Operation.class);
                    assertThat(operation).as(method.toGenericString()).isNotNull();
                    assertThat(operation.summary()).as(method.toGenericString()).isNotBlank();
                });

        OpenApiConfig config = new OpenApiConfig();
        OpenAPI document = config.openAPI();
        io.swagger.v3.oas.models.Operation secured = new io.swagger.v3.oas.models.Operation()
                .summary("测试接口").responses(new ApiResponses())
                .security(List.of(new SecurityRequirement().addList("bearerAuth")));
        document.setPaths(new Paths().addPathItem("/test", new PathItem().get(secured)));

        config.standardResponses().customise(document);

        assertThat(secured.getDescription()).isEqualTo("测试接口");
        assertThat(secured.getResponses()).containsKeys("200", "400", "401", "403", "500");
        assertThat(document.getComponents().getResponses())
                .containsKeys("BadRequest", "Unauthorized", "Forbidden", "NotFound", "Conflict", "ServerError");
        assertThat(document.getComponents().getSecuritySchemes().get("bearerAuth").getScheme()).isEqualTo("bearer");
    }

    @Test
    void logs_shouldNotIncludeKnownSensitiveValues() throws IOException {
        Pattern logCall = Pattern.compile("log\\.(?:trace|debug|info|warn|error)\\([^;]+;", Pattern.DOTALL);
        try (var files = Files.walk(ROOT.resolve("src/main/java"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file, StandardCharsets.UTF_8);
                Matcher matcher = logCall.matcher(source);
                while (matcher.find()) {
                    String call = matcher.group().toLowerCase();
                    int argumentStart = call.indexOf("\",");
                    String loggedValues = argumentStart < 0 ? "" : call.substring(argumentStart + 2);
                    assertThat(loggedValues)
                            .as("sensitive log call in %s", file)
                            .doesNotContain("password", "token", "secret", "phone", "address");
                }
            }
        }
    }

    @Test
    void productionDeployment_shouldKeepOnlyNginxPublicAndPersistState() throws IOException {
        String compose = read("compose.prod.yml");
        String mysql = section(compose, "  mysql:", "  redis:");
        String redis = section(compose, "  redis:", "  app:");
        String app = section(compose, "  app:", "  nginx:");
        String nginx = section(compose, "  nginx:", "networks:");

        assertThat(mysql).doesNotContain("ports:").contains("mysql-data:/var/lib/mysql");
        assertThat(redis).doesNotContain("ports:").contains("redis-data:/data");
        assertThat(app).doesNotContain("ports:").contains("DB_HOST: mysql", "REDIS_HOST: redis", "uploads:/app/uploads");
        assertThat(nginx).contains("80:80", "443:443", "uploads:/srv/uploads:ro");
        assertThat(compose).contains("condition: service_healthy");

        String nginxConfig = read("deploy/nginx/default.conf.template");
        assertThat(nginxConfig).contains("return 301 https://$host$request_uri", "proxy_pass http://app:8080",
                "ssl_protocols TLSv1.2 TLSv1.3", "X-Request-Id $request_id");
        assertThat(read("Dockerfile")).contains("USER app", "ENTRYPOINT");
        assertThat(read(".env.example")).contains("DOMAIN=", "WECHAT_APP_ID=", "WECHAT_APP_SECRET=",
                "DB_PASSWORD=", "REDIS_PASSWORD=", "BACKUP_RETENTION_DAYS=");
        assertThat(Files.isRegularFile(ROOT.resolve("deploy/scripts/backup.sh"))).isTrue();
        assertThat(Files.isRegularFile(ROOT.resolve("deploy/scripts/restore.sh"))).isTrue();
        assertThat(Files.isRegularFile(ROOT.resolve("docs/DEPLOYMENT.md"))).isTrue();
    }

    private String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath), StandardCharsets.UTF_8);
    }

    private String section(String value, String start, String end) {
        int from = value.indexOf(start);
        int to = value.indexOf(end, from + start.length());
        assertThat(from).as("missing section %s", start).isGreaterThanOrEqualTo(0);
        assertThat(to).as("missing section boundary %s", end).isGreaterThan(from);
        return value.substring(from, to);
    }
}
