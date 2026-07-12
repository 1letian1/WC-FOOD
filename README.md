# 食刻小馆后端

单店铺微信点餐小程序的 Spring Boot 后端。当前里程碑提供基础工程、统一响应与异常、鉴权基础设施、MyBatis-Plus、Redis、OpenAPI、健康检查和数据库初始化骨架。

## 环境与启动

需要 JDK 21、Maven 3.9+、MySQL 8.4 和 Redis。复制 `.env.example` 并在本机设置变量（不要提交真实凭据），然后执行：

```bash
docker compose up -d mysql redis
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

数据库首次启动执行 `src/main/resources/db/schema.sql`。健康检查为 `GET /api/common/health`，dev 环境 Swagger UI 为 `/swagger-ui.html`。

验证命令：

```bash
mvn test
mvn clean package
```

生产环境默认关闭 Swagger。业务模块将在后续里程碑分阶段实现。
