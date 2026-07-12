# 食刻小馆后端项目 Codex 开发规范

> 本文件是 Codex 在本仓库中工作的最高优先级项目级说明。
> 开始任何编码任务前，先阅读本文件以及 `docs/` 下的产品需求、数据库设计和接口设计。
> 若现有代码与本文件冲突，优先保持现有可运行代码的一致性，并在修改说明中指出冲突。
> 不要为了“看起来高级”引入本项目当前不需要的复杂架构。

## 1. 项目概述

项目名称：食刻小馆

项目类型：单店铺微信点餐小程序后端。

主要角色：

- 普通用户：微信快捷登录、浏览商品、购物车、堂食或配送下单、查看订单、取消待接单订单、确认收货。
- 商家：账号密码登录、处理订单、管理商品、控制营业状态，同时承担制作和自配送工作。

核心业务闭环：

```text
用户选择堂食或配送
→ 浏览商品并加入购物车
→ 提交订单
→ 商家接单
→ 商家开始制作
→ 堂食订单进入待取餐，或配送订单进入配送中
→ 订单完成
```

第一版明确不做：

- 微信支付和其他支付系统
- 第三方配送平台
- 独立骑手端
- 多门店
- 优惠券、积分、会员等级
- 秒杀、拼团等营销系统
- 地图实时轨迹
- 消息队列
- 微服务
- 分库分表
- Elasticsearch
- Kubernetes
- 复杂权限系统
- WebSocket 实时提醒

第一版优先保证业务正确、接口稳定、状态流转安全和云服务器可部署。

---

## 2. 技术栈

### 2.1 基础环境

- Java：JDK 21
- Spring Boot：3.5.x
- 构建工具：Maven
- Web：Spring MVC
- 数据访问：MyBatis-Plus
- 复杂查询：MyBatis Mapper XML
- 数据库：MySQL 8.4
- 缓存和会话：Redis
- 接口文档：OpenAPI 3 + Swagger UI
- OpenAPI 实现：`springdoc-openapi-starter-webmvc-ui`
- 参数校验：Jakarta Validation
- 密码加密：Spring Security Crypto BCrypt
- JSON：Jackson
- 日志：SLF4J + Logback
- 测试：JUnit 5 + Spring Boot Test
- 部署：Docker + Docker Compose + Nginx
- 时区：Asia/Shanghai
- 字符集：UTF-8 / utf8mb4

### 2.2 建议依赖

优先使用 Spring Boot BOM 管理依赖版本，不要随意为 Spring 官方依赖指定版本。

核心依赖建议：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>

<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
</dependency>

<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-jsqlparser</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

规则：

- 使用与 Spring Boot 3.5.x 兼容的稳定版本。
- MyBatis-Plus 使用 Boot 3 Starter，不使用 Boot 4 Starter。
- MyBatis-Plus 3.5.9 及以上使用分页插件时，引入 `mybatis-plus-jsqlparser`。
- 不使用旧版 Springfox。
- Swagger 注解使用 `io.swagger.v3.oas.annotations`。
- 未经明确需求，不增加 Hutool、Guava、MapStruct、Redisson、Sa-Token、Shiro 等额外框架。
- 仅为使用 BCrypt 引入 `spring-security-crypto`，不因此自动引入完整 Spring Security 鉴权体系。
- 能用 JDK、Spring 和现有依赖完成的功能，不增加新依赖。

---

## 3. 总体架构

使用前后端分离的单体 Spring Boot 应用。

推荐包名：

```text
com.shike.ordering
```

推荐项目名：

```text
shike-ordering-server
```

推荐数据库名：

```text
shike_ordering
```

推荐代码结构：

```text
src/main/java/com/shike/ordering
├── ShikeOrderingApplication.java
├── common
│   ├── constant
│   ├── enums
│   ├── exception
│   ├── result
│   ├── util
│   └── validation
├── config
├── auth
│   ├── annotation
│   ├── filter
│   ├── model
│   └── service
├── controller
│   ├── common
│   ├── user
│   └── merchant
├── service
│   ├── user
│   └── merchant
├── service/impl
│   ├── user
│   └── merchant
├── mapper
├── entity
├── dto
│   ├── common
│   ├── user
│   └── merchant
├── vo
│   ├── common
│   ├── user
│   └── merchant
├── converter
└── task

src/main/resources
├── mapper
├── db
│   ├── schema.sql
│   └── data.sql
├── application.yml
├── application-dev.yml
├── application-test.yml
└── application-prod.yml
```

不要按照功能拆分为多个 Maven 微服务模块。

如果项目已经存在结构，优先遵循现有结构，不要为了匹配本示例进行大范围无价值重构。

---

## 4. 分层职责

### Controller

Controller 只负责：

- 接收请求
- 参数校验
- 获取当前登录身份
- 调用 Service
- 返回统一结果

Controller 不允许：

- 直接调用 Mapper
- 编写 SQL
- 编写复杂业务判断
- 手动管理事务
- 直接返回 Entity
- 捕获所有异常后返回固定失败信息

### Service

Service 负责：

- 业务校验
- 权限校验
- 订单状态流转
- 事务边界
- 多表写入
- Redis 与数据库协作
- Entity、DTO、VO 之间的转换调度

涉及多表写入、订单状态修改、库存扣减时必须使用事务。

默认使用：

```java
@Transactional(rollbackFor = Exception.class)
```

查询方法无必要时不添加事务；复杂只读事务可使用：

```java
@Transactional(readOnly = true)
```

### Mapper

MyBatis-Plus `BaseMapper` 用于：

- 单表按 ID 查询
- 简单条件查询
- 单表新增
- 单表更新
- 单表逻辑删除
- 简单分页

Mapper XML 用于：

- 订单列表多表查询
- 订单详情及明细查询
- 工作台统计
- 按用户、手机号、订单号组合查询
- 商品销量统计
- 聚合查询
- 多条件动态查询
- 需要明确控制 SQL 的查询

不要为了使用 MyBatis-Plus 而拼接难以阅读的超长 Wrapper。

### DTO 和 VO

- 请求参数使用 DTO。
- 返回数据使用 VO。
- Entity 只映射数据库表。
- 禁止直接向前端返回 Entity。
- DTO、VO 和 Entity 不要互相继承。
- 金额字段使用 `BigDecimal`。
- 时间字段使用 `LocalDateTime`。
- 枚举对外返回稳定的 code 和 description，不直接依赖枚举序号。

---

## 5. API 规范

### 5.1 路径前缀

```text
/api/common/**
/api/user/**
/api/merchant/**
```

示例：

```text
POST   /api/user/auth/wechat-login
GET    /api/user/products
GET    /api/user/products/{id}
GET    /api/user/cart
POST   /api/user/cart/items
PUT    /api/user/cart/items/{id}
DELETE /api/user/cart/items/{id}
POST   /api/user/orders
GET    /api/user/orders
GET    /api/user/orders/{id}
PUT    /api/user/orders/{id}/cancel
PUT    /api/user/orders/{id}/confirm-receipt

POST   /api/merchant/auth/login
POST   /api/merchant/auth/logout
GET    /api/merchant/dashboard
GET    /api/merchant/orders
GET    /api/merchant/orders/{id}
PUT    /api/merchant/orders/{id}/accept
PUT    /api/merchant/orders/{id}/reject
PUT    /api/merchant/orders/{id}/start-cooking
PUT    /api/merchant/orders/{id}/ready-for-pickup
PUT    /api/merchant/orders/{id}/start-delivery
PUT    /api/merchant/orders/{id}/mark-delivered
PUT    /api/merchant/orders/{id}/complete

GET    /api/merchant/products
POST   /api/merchant/products
PUT    /api/merchant/products/{id}
PUT    /api/merchant/products/{id}/status
DELETE /api/merchant/products/{id}

GET    /api/merchant/shop
PUT    /api/merchant/shop
PUT    /api/merchant/shop/business-status
```

禁止设计一个通用的接口让前端随意传目标订单状态，例如：

```text
PUT /api/merchant/orders/{id}/status
```

每个状态操作必须有独立业务接口，并在后端校验当前状态、订单类型和操作角色。

### 5.2 统一返回结果

所有 Controller 接口必须使用统一返回对象，禁止各模块自行定义不同响应结构。

必须建立：

```text
Result<T>
PageResult<T>
ErrorCode
```

`Result<T>` 建议结构：

```java
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private String requestId;
    private LocalDateTime timestamp;
}
```

成功响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1001
  },
  "requestId": "8c8b9c27bafd4d6f",
  "timestamp": "2026-07-12T16:30:00"
}
```

失败响应示例：

```json
{
  "code": 60002,
  "message": "订单状态已变化，请刷新后重试",
  "data": null,
  "requestId": "8c8b9c27bafd4d6f",
  "timestamp": "2026-07-12T16:30:00"
}
```

`Result<T>` 至少提供：

```java
Result.success()
Result.success(T data)
Result.success(String message, T data)
Result.failure(ErrorCode errorCode)
Result.failure(Integer code, String message)
```

统一约定：

- `code = 0` 表示业务成功，非 0 表示业务失败。
- `message` 使用简洁、稳定的中文信息。
- `data` 无内容时返回 `null`，不要返回空字符串。
- `requestId` 用于日志追踪，由过滤器生成并写入 MDC。
- `timestamp` 由服务端生成。
- Controller 不直接返回 `Map<String, Object>` 作为正式接口结果。
- Controller 不直接返回字符串表示成功或失败。
- Controller 不直接返回 Entity。
- 除文件下载、图片流、健康检查等特殊接口外，所有接口统一返回 `Result<T>`。
- 分页接口统一返回 `Result<PageResult<T>>`。
- 业务失败同时使用稳定业务错误码和合理 HTTP 状态码。
- 参数错误使用 HTTP 400。
- 未登录或 Token 失效使用 HTTP 401。
- 无权限使用 HTTP 403。
- 数据不存在使用 HTTP 404。
- 状态冲突、重复操作使用 HTTP 409。
- 未知服务器异常使用 HTTP 500。
- 不向前端返回堆栈、SQL、数据库表名、包名、文件路径和敏感配置。

`PageResult<T>` 建议结构：

```java
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Long current;
    private Long size;
    private Long pages;
}
```

分页参数规则：

- `current` 默认 1。
- `size` 默认 10。
- `size` 最大 100。
- 非法分页参数由参数校验或全局异常处理器统一转换。
- 排序字段必须使用后端白名单，不允许前端字段直接拼接 SQL。

### 5.3 参数校验

DTO 使用 Jakarta Validation：

- `@NotNull`
- `@NotBlank`
- `@Size`
- `@Min`
- `@Max`
- `@DecimalMin`
- `@Pattern`

Controller 参数使用 `@Valid` 或 `@Validated`。

所有校验错误由全局异常处理器统一转换。

---

## 6. Swagger / OpenAPI 规范

使用：

```text
OpenAPI 3
springdoc-openapi
Swagger UI
```

不使用旧版 Springfox 和 Swagger 2 注解。

Controller 使用：

- `@Tag`
- `@Operation`
- `@Parameter`
- `@ApiResponse`

DTO 和 VO 使用：

- `@Schema`

要求：

- 每个公开接口必须有接口名称和简要说明。
- 请求字段和响应字段必须有中文说明。
- 枚举字段写明可选值及含义。
- 登录接口写明 Token 返回方式。
- 需要登录的接口在 OpenAPI 中配置 Bearer Token。
- 用户端、商家端、公共接口应分组展示。
- Swagger UI 仅在 dev 和 test 环境开放。
- prod 环境默认关闭 Swagger UI 和 `/v3/api-docs`；确需开放时必须增加访问控制。
- Swagger 文档不能代替代码参数校验和权限校验。

默认文档地址：

```text
/swagger-ui.html
/v3/api-docs
```

---

## 7. 登录、Token 与 Redis 会话

### 7.1 Token 方案

本项目不采用纯 JWT 无状态登录。

第一版使用：

```text
安全随机 Token + Redis 会话
```

请求头：

```text
Authorization: Bearer <token>
```

Token 要求：

- 使用 `SecureRandom` 生成至少 256 bit 随机值。
- 使用 Base64 URL-safe 或十六进制编码。
- 禁止使用可预测的用户 ID、时间戳或简单 UUID 拼接身份信息。
- Token 只返回给客户端一次。
- 日志中不得输出完整 Token。
- Token 在 Redis 中保存登录身份。
- 退出登录时删除 Redis 会话。
- 账号禁用或修改密码后，能够让旧 Token 失效。

建议会话：

```text
auth:user:session:{token}
auth:merchant:session:{token}
```

Redis value 至少包含：

```json
{
  "principalId": 1,
  "principalType": "USER",
  "shopId": 1,
  "loginTime": "2026-07-12T00:00:00"
}
```

默认有效期：

- 普通用户：7 天
- 商家：12 小时

第一版采用固定过期时间，不做每次请求自动续期，避免会话永不过期。

### 7.2 普通用户微信登录

流程：

```text
小程序调用 wx.login 获取 code
→ 前端将 code 发送给后端
→ 后端调用微信 code2Session
→ 获取 openid
→ 查询或创建用户
→ 创建 Redis 会话
→ 返回 Token 和用户基础信息
```

规则：

- 前端传来的 openid 不可信，不能直接作为登录依据。
- `appid` 和 `secret` 只能通过环境变量或生产配置注入。
- 不允许提交到 Git。
- 微信接口调用封装为独立 Client 或 Service，便于测试和替换。
- dev 环境可提供显式开启的模拟登录功能。
- 模拟登录必须默认关闭，prod 环境禁止启用。

### 7.3 商家登录

流程：

```text
账号密码
→ 查询商家账号
→ 检查账号状态和登录限制
→ 校验密码
→ 创建 Redis 会话
→ 返回 Token 和商家信息
```

登录限制建议：

- 15 分钟内连续失败 5 次，锁定 30 分钟。
- Redis Key 示例：`auth:merchant:fail:{username}`。
- 登录成功后清除失败次数。
- 错误信息统一返回“账号或密码错误”，避免泄露账号是否存在。
- 账号禁用时可以返回明确的账号禁用提示。

### 7.4 权限控制

至少区分：

```text
USER
MERCHANT
```

规则：

- `/api/user/**` 只能由 USER 访问，登录接口和公开商品接口除外。
- `/api/merchant/**` 只能由 MERCHANT 访问，商家登录接口除外。
- 商家操作任何数据时必须校验 `shopId`。
- 不允许仅依赖前端隐藏按钮实现权限控制。
- 当前登录身份通过统一上下文获取，不允许 Controller 自行解析 Redis value。
- 可使用 Spring MVC Interceptor 或 Spring Security 自定义过滤器；全项目只能选一种主鉴权链路，不要重复实现两套。

默认建议：使用自定义 `OncePerRequestFilter` 完成 Token 提取、Redis 会话校验和身份上下文设置。

---

## 8. 商家密码 BCrypt 约定

商家密码统一使用 BCrypt，不使用弱密码哈希或自定义拼接哈希方案。

依赖：

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

建议通过 Spring Bean 统一提供：

```java
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

数据库字段：

```text
password_hash
password_algorithm
```

字段约定：

- `password_hash` 保存 BCrypt 完整结果。
- BCrypt 结果已经包含算法版本、cost 和随机盐值，不需要单独保存盐值字段。
- `password_algorithm` 可保存 `BCRYPT`，为以后迁移算法保留空间。
- `password_hash` 使用 `VARCHAR(100)` 或更长字段。

密码写入：

```java
String passwordHash = passwordEncoder.encode(rawPassword);
```

密码校验：

```java
boolean matched = passwordEncoder.matches(rawPassword, passwordHash);
```

要求：

- BCrypt strength 默认使用 12。
- 如果实际云服务器压测发现登录耗时明显过高，可以调整为 10，但不得低于 10。
- 每次调用 `encode` 都会产生不同哈希，这是正常现象。
- 不允许通过再次 encode 后比较两个字符串判断密码是否正确。
- 不需要自行生成或保存盐值。
- 明文密码和密码哈希不得写入日志。
- 新增账号、重置密码和修改密码必须统一使用 `PasswordEncoder`。
- 不允许在 Controller 中直接处理密码加密。
- 不自行编写 BCrypt 算法或重复封装底层实现。
- 登录失败统一提示“账号或密码错误”，避免泄露账号是否存在。
- 修改密码后删除该商家的全部 Redis 登录会话，使旧 Token 失效。
- 测试数据中的默认密码必须先使用 BCrypt 编码再写入数据库。
- SQL 脚本不得包含真实生产密码。
- 后续升级 cost 时，可在登录成功后判断旧哈希参数并渐进式重新编码。

密码规则：

- 长度为 8 至 20 位。
- 至少包含字母和数字。
- 首尾空格不得被静默保留。
- 不允许使用商家账号作为密码。
- 修改密码时，新密码不能与当前密码相同。

---

## 9. 数据库规范

### 9.1 通用约定

- 数据库：MySQL 8.4
- 存储引擎：InnoDB
- 字符集：utf8mb4
- 表名和字段名：snake_case
- Java 字段名：camelCase
- 主键：`BIGINT UNSIGNED AUTO_INCREMENT`
- 金额：`DECIMAL(10,2)`，Java 使用 `BigDecimal`
- 数量：整数
- 时间：`DATETIME(3)`，Java 使用 `LocalDateTime`
- 布尔值：`TINYINT`
- 状态值：`TINYINT` 或 `SMALLINT`
- 所有表明确创建时间和更新时间
- 业务表按需要增加创建人和更新人
- 不使用数据库外键，关联完整性由应用和索引保证
- 关键查询字段必须创建索引
- 唯一业务字段使用唯一索引
- SQL 必须显式列名，不使用 `SELECT *`

### 9.2 核心表

第一版至少包含：

```text
user
merchant_account
shop
category
product
product_specification
product_taste
shopping_cart
address
orders
order_item
order_status_log
```

### 9.3 逻辑删除

适合逻辑删除：

- category
- product
- address

不允许删除或逻辑删除：

- orders
- order_item
- order_status_log

订单必须永久保留业务记录，只能通过订单状态表示取消或拒绝。

删除商品时：

- 已被历史订单引用的商品不能物理删除。
- 逻辑删除不影响历史订单明细。
- 订单明细必须保存商品名称、图片、规格、口味、单价等快照。

### 9.4 索引建议

至少考虑：

```text
user(openid) UNIQUE
merchant_account(username) UNIQUE
category(shop_id, status, sort)
product(shop_id, category_id, status, deleted)
shopping_cart(user_id, product_id)
address(user_id, deleted)
orders(order_no) UNIQUE
orders(user_id, create_time)
orders(shop_id, status, create_time)
orders(shop_id, order_type, status, create_time)
order_item(order_id)
order_status_log(order_id, create_time)
```

索引名称使用：

```text
uk_表_字段
idx_表_字段
```

不要盲目为每个字段建立索引。

---

## 10. 订单模型与状态机

### 10.1 订单类型

```text
DINE_IN = 1
DELIVERY = 2
```

### 10.2 订单状态

建议稳定编码：

```text
PENDING_ACCEPT    = 1  待接单
ACCEPTED          = 2  已接单
COOKING           = 3  制作中
READY_FOR_PICKUP  = 4  待取餐
DELIVERING        = 5  配送中
DELIVERED         = 6  已送达
COMPLETED         = 7  已完成
CANCELLED         = 8  已取消
REJECTED          = 9  已拒单
```

编码一旦进入数据库，不得随意调整含义。

### 10.3 堂食状态流转

```text
PENDING_ACCEPT
→ ACCEPTED
→ COOKING
→ READY_FOR_PICKUP
→ COMPLETED
```

异常流转：

```text
PENDING_ACCEPT → CANCELLED
PENDING_ACCEPT → REJECTED
```

### 10.4 配送状态流转

```text
PENDING_ACCEPT
→ ACCEPTED
→ COOKING
→ DELIVERING
→ DELIVERED
→ COMPLETED
```

异常流转：

```text
PENDING_ACCEPT → CANCELLED
PENDING_ACCEPT → REJECTED
```

### 10.5 操作权限

普通用户：

- 只能取消自己的 `PENDING_ACCEPT` 订单。
- 只能对自己的配送订单执行确认收货。
- 只有 `DELIVERED` 的配送订单可以确认收货。
- 用户不能直接修改订单状态字段。

商家：

- 只能操作所属店铺订单。
- `PENDING_ACCEPT` 可以接单或拒单。
- `ACCEPTED` 可以开始制作。
- 堂食 `COOKING` 可以进入 `READY_FOR_PICKUP`。
- 堂食 `READY_FOR_PICKUP` 可以进入 `COMPLETED`。
- 配送 `COOKING` 可以进入 `DELIVERING`。
- 配送 `DELIVERING` 可以进入 `DELIVERED`。
- 配送 `DELIVERED` 等待用户确认，第一版商家不能直接跳过确认完成。

### 10.6 并发与幂等

状态修改必须防止重复操作和并发覆盖。

推荐方式：

```sql
UPDATE orders
SET status = #{targetStatus},
    version = version + 1,
    update_time = NOW(3)
WHERE id = #{id}
  AND shop_id = #{shopId}
  AND status = #{expectedStatus}
  AND version = #{version}
```

规则：

- 更新影响行数为 0 时，返回订单状态已变化。
- 每次状态变化必须写入 `order_status_log`。
- 状态更新和日志写入必须处于同一事务。
- 不允许先查询状态再无条件更新。
- 重复请求不得产生重复状态日志。
- 创建订单支持幂等键，避免用户连续点击生成重复订单。

---

## 11. 下单与金额规则

### 11.1 金额计算

前端传来的价格和总金额不可信。

后端必须重新计算：

```text
商品小计 = 后端当前有效单价 × 数量
商品总额 = 所有商品小计之和
实付金额 = 商品总额 + 配送费 + 包装费
```

第一版没有支付系统，但仍保留：

- `total_amount`
- `delivery_fee`
- `package_fee`
- `pay_amount`

不要增加支付状态和支付流水表，除非后续明确接入支付。

金额计算要求：

- 全程使用 `BigDecimal`。
- 禁止使用 `double` 或 `float`。
- 所有金额统一保留两位小数。
- 对比金额使用 `compareTo`。
- 不相信购物车中保存的历史价格。
- 提交订单时重新查询商品状态、价格和库存。

### 11.2 堂食订单

必须包含：

- 联系人
- 联系电话
- 桌号或“暂未入座”
- 商品
- 备注可选

校验：

- 桌号为空时，`noSeatYet` 必须为 true。
- 不需要地址和配送费。
- 店铺休息或堂食关闭时不能提交。

### 11.3 配送订单

必须包含：

- 地址 ID
- 收货人
- 联系电话
- 地址快照
- 商品
- 配送费
- 备注可选

校验：

- 地址必须属于当前用户。
- 店铺休息或配送关闭时不能提交。
- 商品金额必须达到起送金额。
- 第一版配送范围只做文本提示，不实现地图距离校验。
- 订单保存地址快照，后续修改地址不能影响历史订单。

### 11.4 商品和库存

下单时必须校验：

- 商品存在
- 未删除
- 已上架
- 未售罄
- 属于当前店铺
- 规格和口味属于该商品
- 数量大于 0
- 库存充足

库存扣减使用条件更新，避免超卖：

```sql
UPDATE product
SET stock = stock - #{quantity}
WHERE id = #{productId}
  AND stock >= #{quantity}
  AND status = 1
  AND deleted = 0
```

任一商品扣减失败，整个订单事务回滚。

取消待接单订单是否恢复库存：

- 第一版默认恢复库存。
- 恢复库存和取消状态必须在同一事务。
- 拒单默认恢复库存。
- 不允许重复恢复库存。

---

## 12. 订单编号

订单主键使用数据库自增 ID，订单号使用独立业务编号。

推荐格式：

```text
yyyyMMdd + 8位当日流水号
```

示例：

```text
2026071200000001
```

Redis Key：

```text
order:no:20260712
```

生成规则：

- 使用 Redis `INCR` 保证当日递增。
- 首次生成时设置 3 天过期时间。
- 订单号字段建立唯一索引。
- Redis 异常时不得静默生成可能重复的订单号。
- 必要时可以降级为基于时间和安全随机数的唯一订单号，但必须通过唯一索引兜底。

---

## 13. Redis 使用规范

Redis 只用于适合缓存、会话、限流和幂等的数据，不作为订单最终数据源。

建议 Key：

```text
auth:user:session:{token}
auth:merchant:session:{token}
auth:merchant:fail:{username}
shop:status:{shopId}
shop:config:{shopId}
order:submit:idempotency:{userId}:{key}
order:no:{yyyyMMdd}
```

规则：

- Key 必须包含业务前缀。
- 所有临时 Key 必须设置 TTL。
- Redis value 尽量保持结构简单。
- 不在 Redis 中保存正式订单作为唯一数据。
- 不在 Redis 中保存明文密码。
- 缓存删除失败不能破坏数据库事务一致性。
- 店铺状态以 MySQL 为最终数据源，Redis 为缓存。
- 店铺设置更新成功后删除或更新缓存。
- 第一版不引入 Redisson。
- 简单原子操作使用 `StringRedisTemplate`。
- 多步原子操作优先使用 Lua 脚本。
- 不使用 Redis 分布式锁代替数据库条件更新和唯一索引。

---

## 14. MyBatis-Plus 与 Mapper XML 规范

### 14.1 MyBatis-Plus

允许使用：

- `BaseMapper`
- `LambdaQueryWrapper`
- `LambdaUpdateWrapper`
- `IService` 和 `ServiceImpl`，仅在能保持代码清晰时使用
- `Page`
- `PaginationInnerInterceptor`
- 自动填充
- 逻辑删除

限制：

- Wrapper 条件超过约 8 个、涉及多表或聚合时，优先写 XML。
- 不在 Controller 中创建 Wrapper。
- 不使用字符串字段名 Wrapper。
- 不使用 `last()` 拼接前端参数。
- 不使用 `${}` 接收用户输入。
- 不滥用通用 Service，让业务语义消失。

### 14.2 Mapper XML

XML 位置：

```text
src/main/resources/mapper/**/*Mapper.xml
```

规则：

- namespace 与 Mapper 接口全限定名一致。
- statement id 与接口方法名一致。
- 参数使用 `#{}`。
- 只有受后端白名单严格控制的场景才能使用 `${}`。
- 动态条件使用 `<if>`、`<where>`、`<choose>`。
- 返回复杂 VO 时定义清晰的 `resultMap`。
- 避免 N+1 查询。
- 订单列表不要为每条订单单独查询明细。
- 分页查询必须保证 count SQL 和列表 SQL 语义一致。
- SQL 中所有表必须使用明确别名。
- 多表中同名字段必须显式别名。
- 复杂 SQL 需要简短中文注释说明目的。

---

## 15. 全局异常处理与错误码

所有接口错误必须由全局异常处理器转换为统一 `Result<?>` 响应。

禁止在每个 Controller 中重复编写：

```java
try {
    // ...
} catch (Exception e) {
    return Result.failure(...);
}
```

只有需要业务补偿、资源释放或异常转换时，Service 才进行局部捕获；捕获后不能吞掉异常。

### 15.1 核心类

必须建立：

```text
ErrorCode
BusinessException
UnauthorizedException
ForbiddenException
ResourceNotFoundException
OrderStateConflictException
GlobalExceptionHandler
```

全局异常处理器使用：

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
}
```

业务异常建议携带：

```java
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final HttpStatus httpStatus;
}
```

错误码建议使用枚举统一维护：

```java
public enum ErrorCode {
    SUCCESS(0, "success", HttpStatus.OK),
    PARAM_ERROR(10001, "参数错误", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(20001, "未登录或登录已过期", HttpStatus.UNAUTHORIZED);
}
```

禁止：

- 在业务代码中散落大量硬编码错误码。
- 多个异常类重复定义相同错误码。
- 将 `exception.getMessage()` 无条件返回给前端。
- 将数据库、Redis 或微信接口的原始异常信息返回给前端。
- 未知异常返回 HTTP 200。
- Controller 捕获所有异常后统一返回“操作失败”。

### 15.2 必须处理的异常

`GlobalExceptionHandler` 至少处理：

- `MethodArgumentNotValidException`：JSON DTO 校验失败。
- `BindException`：对象参数绑定失败。
- `ConstraintViolationException`：路径参数或查询参数校验失败。
- `HttpMessageNotReadableException`：JSON 格式、日期或枚举转换失败。
- `MissingServletRequestParameterException`：缺少请求参数。
- `MethodArgumentTypeMismatchException`：参数类型不匹配。
- `HttpRequestMethodNotSupportedException`：请求方法不支持。
- `HttpMediaTypeNotSupportedException`：Content-Type 不支持。
- `NoResourceFoundException`：接口或静态资源不存在。
- `BusinessException`：通用业务异常。
- `UnauthorizedException`：未登录或 Token 失效。
- `ForbiddenException`：无权限或数据不属于当前身份。
- `ResourceNotFoundException`：业务数据不存在。
- `OrderStateConflictException`：订单状态冲突。
- `DuplicateKeyException`：唯一索引冲突。
- `DataIntegrityViolationException`：数据完整性异常。
- Redis 访问异常：转换为稳定业务错误，不泄露 Redis 内部错误。
- 微信接口调用异常：转换为稳定业务错误。
- `Exception`：最终兜底。

### 15.3 参数错误信息

DTO 校验失败时：

- 第一版默认返回第一条明确错误，方便前端直接提示。
- 优先使用校验注解中的中文 `message`。
- 不返回 Java 字段绑定堆栈。

示例：

```json
{
  "code": 10001,
  "message": "联系电话不能为空",
  "data": null,
  "requestId": "8c8b9c27bafd4d6f",
  "timestamp": "2026-07-12T16:30:00"
}
```

### 15.4 错误码划分

```text
10000 通用与参数
20000 登录与用户
30000 商家
40000 商品与分类
50000 购物车与地址
60000 订单
70000 店铺
80000 文件上传
90000 外部服务
```

示例：

```text
0      success
10001  参数错误
10002  请求格式错误
10003  请求方法不支持
10004  接口不存在
20001  未登录或登录已过期
20002  无权限访问
30001  商家账号或密码错误
30002  商家账号已禁用
30003  登录失败次数过多
40001  商品不存在
40002  商品已下架
40003  商品已售罄
50001  购物车为空
50002  地址不存在
60001  订单不存在
60002  订单状态已变化
60003  无权操作该订单
60004  重复提交订单
70001  店铺休息中
70002  堂食暂停
70003  配送暂停
80001  文件类型不支持
80002  文件大小超出限制
90001  微信服务调用失败
99999  系统繁忙，请稍后重试
```

### 15.5 HTTP 状态码映射

```text
业务成功                  → HTTP 200
参数错误                  → HTTP 400
未登录、Token 失效        → HTTP 401
无权限                    → HTTP 403
资源不存在                → HTTP 404
状态冲突、重复操作        → HTTP 409
未知服务器异常            → HTTP 500
```

Controller 返回成功结果。

全局异常处理器返回错误结果，并通过 `ResponseEntity<Result<?>>` 设置正确 HTTP 状态码。

### 15.6 未知异常

未知异常必须：

- 生成或读取当前 requestId。
- 使用 `log.error` 记录完整异常堆栈。
- 记录请求方法、URI 和必要的脱敏身份信息。
- 前端统一返回错误码 `99999`。
- 前端统一提示“系统繁忙，请稍后重试”。
- 不返回异常类名、堆栈、SQL、文件路径和内部服务地址。

### 15.7 requestId 与 MDC

建议增加 `RequestIdFilter`：

- 优先读取请求头 `X-Request-Id`。
- 不存在时由服务端生成。
- 写入 MDC，键名统一为 `requestId`。
- 响应体和响应头中都携带 requestId。
- 请求结束后清理 MDC，防止线程复用导致串号。
- 所有日志格式包含 requestId。

---

## 16. 日志规范

使用 SLF4J 参数化日志：

```java
log.info("merchant accepted order, merchantId={}, orderId={}", merchantId, orderId);
```

禁止：

```java
log.info("merchant accepted order " + orderId);
```

必须记录：

- 登录成功和失败，不记录密码
- 账号锁定
- 订单创建
- 订单状态流转
- 商家拒单
- 商品上下架
- 店铺营业状态变更
- 关键外部接口失败
- 未知异常

禁止记录：

- 明文密码
- 完整 Token
- 微信 secret
- 数据库密码
- Redis 密码
- 完整手机号和完整地址的大量日志

手机号日志需要脱敏，例如：

```text
138****0000
```

---

## 17. 配置与环境变量

配置分层：

```text
application.yml
application-dev.yml
application-test.yml
application-prod.yml
```

公共配置放在 `application.yml`。

敏感配置通过环境变量：

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD

REDIS_HOST
REDIS_PORT
REDIS_PASSWORD

WECHAT_APP_ID
WECHAT_APP_SECRET

UPLOAD_PATH
PUBLIC_FILE_BASE_URL
```

规则：

- 不提交真实密码和 secret。
- 提供 `.env.example`，只写变量名和示例占位符。
- 提供 `application-dev.example.yml` 时不得包含真实凭据。
- prod 配置默认关闭 SQL 调试输出。
- prod 配置默认关闭 Swagger。
- 数据源必须设置合理连接池大小和超时。
- MySQL JDBC URL 明确设置字符集、时区和 SSL 选项。
- 容器环境使用服务名访问 MySQL 和 Redis，不使用 `localhost`。

---

## 18. 文件上传

第一版使用统一 `StorageService` 抽象。

实现至少允许：

- dev：本地目录存储
- prod：可先使用服务器挂载目录，通过 Nginx 提供静态文件
- 后续：可替换为 OSS/COS，不影响业务层

规则：

- 数据库只保存文件 URL 或相对路径。
- 不把图片二进制存入 MySQL。
- 校验文件大小、扩展名和 MIME 类型。
- 文件名由服务端生成，不直接使用用户文件名。
- 防止目录穿越。
- 商品图片删除时先确认没有仍在使用。
- 上传接口只允许商家访问。
- 生产文件目录必须挂载持久化卷。
- 不把上传目录打进应用 JAR。

---

## 19. 测试要求

每个功能至少覆盖核心业务测试。

优先测试：

- 商家密码 BCrypt 编码与校验
- Token 创建、过期和退出登录
- 商家登录失败次数限制
- 堂食订单状态流转
- 配送订单状态流转
- 用户取消订单权限
- 用户确认收货权限
- 商家跨店铺操作拦截
- 重复接单和并发状态更新
- 下单金额后端重算
- 商品下架和售罄校验
- 库存扣减和恢复
- 起送金额校验
- 店铺营业状态校验

测试命名：

```text
方法名_场景_预期结果
```

示例：

```text
acceptOrder_whenOrderIsPending_shouldSucceed
acceptOrder_whenOrderAlreadyAccepted_shouldThrowConflict
confirmReceipt_whenOrderBelongsToOtherUser_shouldBeForbidden
```

完成修改后至少运行：

```bash
mvn test
mvn clean package
```

若受环境限制无法运行测试，必须明确说明：

- 未运行的命令
- 原因
- 可能风险
- 用户可以如何验证

不得谎称测试通过。

---

## 20. Docker 与部署

目标部署结构：

```text
微信小程序
→ HTTPS
→ Nginx
→ Spring Boot
→ MySQL
→ Redis
```

Docker Compose 建议服务：

```text
nginx
app
mysql
redis
```

规则：

- MySQL 和 Redis 不暴露到公网。
- Spring Boot 8080 不直接暴露公网，只允许 Nginx 访问。
- 公网只开放 22、80、443。
- Nginx 负责 HTTPS 和反向代理。
- HTTP 自动跳转 HTTPS。
- MySQL、Redis、上传文件使用持久化卷。
- 容器之间通过 Compose 服务名访问。
- 应用提供健康检查接口，例如 `/actuator/health` 或自定义轻量接口。
- 不把生产密码硬编码进 compose 文件。
- 提供数据库备份说明。
- 数据库迁移脚本应可重复管理，禁止上线后手工随意改表。
- 第一版可使用 `schema.sql` 管理初始化；结构稳定后优先引入 Flyway。

微信小程序上线相关内容必须在部署文档中提醒：

- 后端必须使用 HTTPS。
- 请求域名需要在微信小程序后台配置。
- 正式域名、证书和备案等事项由实际云环境决定。
- 不要把测试 IP 地址写死在前端代码中。

---

## 21. 编码风格

### Java

- 类名使用 PascalCase。
- 方法和变量使用 camelCase。
- 常量使用 UPPER_SNAKE_CASE。
- 包名全小写。
- 一个公共类一个文件。
- 方法尽量保持单一职责。
- 避免超过 4 层嵌套。
- 复杂条件提取为有语义的方法。
- 不写无意义注释。
- 注释说明“为什么”，而不是复述代码。
- 公共业务规则和状态含义使用中文注释。
- 不使用魔法数字，使用枚举或常量。
- 不返回 null 集合，返回空集合。
- `Optional` 主要用于返回值，不作为 Entity 字段或 DTO 字段。
- 使用构造器注入，不使用字段注入。
- 新代码优先使用 `final` 表达不可变依赖。
- 时间统一由可注入 `Clock` 或时间工具获取时，优先便于测试。
- 禁止静态全局保存当前用户。
- 当前登录身份使用请求作用域上下文，并在请求结束时清理。

### Lombok

允许使用：

- `@Getter`
- `@Setter`
- `@Builder`
- `@RequiredArgsConstructor`
- `@Slf4j`

谨慎使用：

- `@Data`

Entity 不建议使用 `@Data`，避免自动生成不合适的 `equals`、`hashCode` 和 `toString`。

密码、Token 等敏感字段不能被 `toString` 输出。

---

## 22. 安全规则

必须做到：

- 所有商家接口校验商家登录状态。
- 所有用户订单接口校验订单归属。
- 商家操作订单时校验店铺归属。
- 前端金额、状态、用户 ID 和店铺 ID 均不可信。
- 使用参数化 SQL。
- 上传文件进行类型和大小校验。
- 登录接口进行失败次数限制。
- 对创建订单等关键写接口提供幂等保护。
- 隐私数据最小化返回。
- 配置文件中不出现真实凭据。
- 生产错误响应不泄露内部细节。
- Swagger 生产环境默认关闭。
- CORS 只允许明确来源；微信小程序本身不依赖浏览器 CORS，开发 Swagger 时可以只开放本地来源。

不要自行实现“通用加密算法框架”。密码统一通过 Spring `PasswordEncoder` 使用 BCrypt 处理。

---

## 23. 开发顺序

Codex 不要一次性生成全部业务代码。

按以下阶段实现，每个阶段结束后保证可编译、可测试、可运行。

### 阶段 1：项目骨架

- Maven 项目
- 环境配置
- 统一返回结果 `Result<T>` 和 `PageResult<T>`
- `@RestControllerAdvice` 全局异常处理器
- Swagger
- MyBatis-Plus
- Redis
- 基础 Docker Compose
- 健康检查

### 阶段 2：数据库与基础数据

- 建表脚本
- Entity
- Mapper
- 枚举
- 自动填充
- MyBatis-Plus 分页配置
- 基础测试数据

### 阶段 3：登录鉴权

- 微信登录 Client 抽象
- dev 模拟微信登录
- 用户 Token 会话
- 商家 BCrypt 密码登录
- 商家登录失败限制
- 权限拦截
- 退出登录

### 阶段 4：店铺、分类和商品

- 店铺查询和设置
- 分类管理
- 商品 CRUD
- 商品上下架
- 售罄
- 商品列表和详情
- 图片上传

### 阶段 5：购物车和地址

- 购物车增删改查
- 地址增删改查
- 默认地址

### 阶段 6：下单

- 堂食下单
- 配送下单
- 金额重算
- 库存扣减
- 订单编号
- 订单明细快照
- 幂等保护

### 阶段 7：订单流转

- 用户订单列表和详情
- 商家订单列表和详情
- 接单
- 拒单
- 开始制作
- 待取餐
- 开始配送
- 标记已送达
- 用户确认收货
- 状态日志
- 并发控制

### 阶段 8：部署完善

- Nginx
- HTTPS 配置示例
- 生产环境变量
- 持久化卷
- 数据库备份说明
- 部署文档

---

## 24. Codex 工作规则

执行任何任务时：

1. 先阅读本文件和相关需求文档。
2. 检查现有代码，不重复创建已有能力。
3. 对复杂任务先给出简短计划。
4. 优先做最小、完整、可验证的修改。
5. 不未经说明改变既有接口、表结构和枚举编码。
6. 不大范围重命名或格式化无关文件。
7. 不删除用户已有代码，除非任务明确要求。
8. 不使用伪代码代替被要求实现的功能。
9. 不留下无法编译的半成品。
10. 修改数据库结构时同步更新 SQL 和文档。
11. 修改接口时同步更新 Swagger 注解和必要测试。
12. 修改订单状态规则时同步更新状态校验、状态日志和测试。
13. 新增环境变量时同步更新 `.env.example`。
14. 新增依赖前先说明用途，并确认现有依赖不能完成。
15. 完成后运行测试和构建。
16. 最终说明修改了哪些文件、如何验证、还有什么风险。
17. 不声称未实际运行的命令已经成功。
18. 遇到需求不明确时，优先根据产品文档和本文件采用最保守实现，并在结果中记录假设。
19. 安全、数据一致性和订单状态正确性优先于代码量和开发速度。
20. 不主动加入第一版明确不需要的功能。

---

## 25. 完成标准

一个任务只有同时满足以下条件才算完成：

- 代码可以编译。
- 核心测试通过，或明确说明未运行原因。
- 接口符合统一 `Result<T>` 返回格式。
- 所有接口错误由全局异常处理器统一转换，不在 Controller 重复 try-catch。
- 参数校验完整。
- 权限和数据归属校验完整。
- 数据库操作使用正确事务。
- 复杂 SQL 放在 Mapper XML。
- Swagger 文档同步。
- 没有提交真实密码、secret、Token。
- 没有输出敏感日志。
- 没有破坏既有状态编码。
- 没有引入无关复杂框架。
- 提供清晰的运行和验证方式。

---

## 26. 当前已经确定的产品与技术决策

以下内容视为已确认，不要重复询问：

- 单店铺。
- 用户和商家共用一个微信小程序。
- 普通用户使用微信快捷登录。
- 商家使用账号密码登录。
- 商家同时承担接单、制作和配送。
- 支持堂食和商家自配送。
- 不设计独立骑手端。
- 第一版不接支付。
- 后端使用 Java 和 Spring Boot。
- 使用 MyBatis-Plus 处理简单 CRUD。
- 复杂查询使用 Mapper XML。
- 使用 MySQL。
- 使用 Redis。
- 接口文档使用 OpenAPI 3 和 Swagger UI。
- 商家密码使用 BCrypt，默认 strength 为 12。
- 登录使用随机 Token + Redis 会话。
- 后端部署到云服务器。
- 使用 Docker Compose 和 Nginx。
- 第一版不使用微服务、消息队列和 WebSocket。

如果后续用户明确改变以上决策，以最新明确要求为准，并同步更新本文件。
