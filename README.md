# 食刻小馆后端

单店铺微信点餐小程序的 Spring Boot 后端。当前已完成 M1—M7，包括基础工程、数据库、双端登录与权限、店铺/商品、购物车/地址、安全下单、订单查询与完整状态流转。

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

用户商品接口为 `GET /api/user/products` 和 `GET /api/user/products/{id}`。商家登录后可通过 `/api/merchant/shop`、`/api/merchant/categories`、`/api/merchant/products` 管理本店数据；图片上传接口为 `POST /api/merchant/files/images`，支持 JPEG、PNG、GIF，最大 5MB。开发环境文件写入 `UPLOAD_PATH`，返回地址以 `PUBLIC_FILE_BASE_URL` 为前缀。

dev 环境需要模拟微信登录时，显式设置 `WECHAT_MOCK_ENABLED=true`；该开关默认关闭且 prod 配置强制关闭。用户登录使用 `POST /api/user/auth/wechat-login`，商家登录使用 `POST /api/merchant/auth/login`，登录后的接口通过 `Authorization: Bearer <token>` 访问。

验证命令：

```bash
mvn test
mvn clean package
```

用户可通过 `/api/user/orders` 创建、筛选和查看本人订单，并执行取消待接单订单、确认配送收货；商家可通过 `/api/merchant/orders` 查询和履约本店订单，通过 `/api/merchant/dashboard` 查看工作台统计。所有状态动作均为独立接口。

生产环境默认关闭 Swagger。示例账号、联系电话、地址和图片路径必须在部署前替换；M8 将继续完成联调质量与生产部署材料。
