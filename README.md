# 食刻小馆后端

单店铺微信点餐小程序的 Spring Boot 后端。当前已完成 M1 基础工程和 M2 数据库基础能力，包括统一响应与异常、鉴权基础设施、MyBatis-Plus、Redis、OpenAPI、健康检查，以及 12 张核心业务表的实体和 Mapper。

## 环境与启动

需要 JDK 21、Maven 3.9+、MySQL 8.4 和 Redis。复制 `.env.example` 并在本机设置变量（不要提交真实凭据），然后执行：

```bash
docker compose up -d mysql redis
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

数据库首次启动时使用 UTF-8 客户端依次执行结构和开发数据脚本：

```bash
mysql --default-character-set=utf8mb4 -h 127.0.0.1 -u <用户名> -p -e "SOURCE src/main/resources/db/schema.sql; SOURCE src/main/resources/db/data.sql;"
```

健康检查为 `GET /api/common/health`，dev 环境 Swagger UI 为 `/swagger-ui.html`。

初始化结构后可执行 `src/main/resources/db/data.sql` 写入非生产店铺、分类、商品及商家测试账号。测试账号仅用于本地开发：`admin_demo / ShikeTest123`，生产部署必须删除或替换。公开店铺接口为 `GET /api/common/shop`，默认店铺由环境变量 `DEFAULT_SHOP_ID` 指定。

用户端公开分类接口为 `GET /api/user/categories`，仅返回默认店铺已启用且未逻辑删除的分类，并按 `sort`、`id` 稳定排序。

验证命令：

```bash
mvn test
mvn clean package
```

生产环境默认关闭 Swagger。示例账号、联系电话、地址和图片路径必须在部署前替换；登录、商品管理、购物车和订单等业务能力将在后续里程碑分阶段实现。
