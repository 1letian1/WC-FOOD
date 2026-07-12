# 食刻小馆数据库设计

## 1. 总体约定

- MySQL 8.4、InnoDB、`utf8mb4`；库名 `shike_ordering`。
- 主键统一 `BIGINT UNSIGNED AUTO_INCREMENT`；Java 使用 `Long`。
- 金额 `DECIMAL(10,2)`，时间 `DATETIME(3)`；所有表含 `create_time`、`update_time`。
- 不创建数据库外键，但本文“外键”表示逻辑关联；应用事务与索引保证一致性。
- 所有 SQL 显式列名。`category/product/address` 逻辑删除；订单域永不删除。
- 状态码进入数据库后不可重排；并发更新使用 `version`。

## 2. 表关系概览

```text
shop ─┬─ merchant_account
      ├─ category ─ product ─┬─ product_specification
      │                      └─ product_taste
      └─ orders ─┬─ order_item
                 └─ order_status_log

user ─┬─ address
      ├─ shopping_cart
      └─ orders
```

## 3. 表与字段设计

下表中未逐行重复的公共字段为：`id`、`create_time`、`update_time`。字符串长度可在建表阶段依据实际中文字符上限微调，但不得降低安全字段容量。

### 3.1 user

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| openid | VARCHAR(64) | NOT NULL，微信小程序内用户唯一标识，唯一索引 |
| nickname | VARCHAR(64) | 可空 |
| avatar_url | VARCHAR(500) | 可空 |
| phone | VARCHAR(20) | 可空，不默认唯一 |
| status | TINYINT | NOT NULL，1 正常/0 禁用 |
| last_login_time | DATETIME(3) | 可空 |

索引：`uk_user_openid(openid)`。

### 3.2 merchant_account

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| shop_id | BIGINT UNSIGNED | NOT NULL，逻辑关联 shop |
| username | VARCHAR(64) | NOT NULL，唯一 |
| password_hash | VARCHAR(100) | NOT NULL，BCrypt 完整结果 |
| password_algorithm | VARCHAR(20) | NOT NULL，默认 BCRYPT |
| merchant_name | VARCHAR(64) | NOT NULL |
| avatar_url | VARCHAR(500) | 可空 |
| role | VARCHAR(32) | NOT NULL，第一版固定 MERCHANT |
| status | TINYINT | NOT NULL，1 正常/0 禁用 |
| session_version | INT UNSIGNED | NOT NULL DEFAULT 1，改密/禁用后使旧会话失效 |
| last_login_time | DATETIME(3) | 可空 |

索引：`uk_merchant_account_username(username)`、`idx_merchant_account_shop_status(shop_id,status)`。

### 3.3 shop

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| name | VARCHAR(100) | NOT NULL |
| logo_url | VARCHAR(500) | 可空 |
| phone | VARCHAR(20) | NOT NULL |
| address | VARCHAR(255) | NOT NULL |
| notice | VARCHAR(500) | 可空 |
| business_hours | VARCHAR(255) | NOT NULL，第一版展示配置 |
| business_status | TINYINT | NOT NULL，1 营业/0 休息 |
| dine_in_enabled | TINYINT | NOT NULL |
| delivery_enabled | TINYINT | NOT NULL |
| delivery_fee | DECIMAL(10,2) | NOT NULL DEFAULT 0.00 |
| min_delivery_amount | DECIMAL(10,2) | NOT NULL DEFAULT 0.00 |
| package_fee | DECIMAL(10,2) | NOT NULL DEFAULT 0.00 |
| delivery_range | VARCHAR(500) | 可空，仅文本提示 |
| estimated_delivery_minutes | INT UNSIGNED | 可空 |
| version | INT UNSIGNED | NOT NULL DEFAULT 0 |

约束：金额均非负；第一版应用层确保只有一个有效店铺。

### 3.4 category

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| shop_id | BIGINT UNSIGNED | NOT NULL |
| name | VARCHAR(64) | NOT NULL |
| status | TINYINT | NOT NULL，1 启用/0 禁用 |
| sort | INT | NOT NULL DEFAULT 0 |
| deleted | TINYINT | NOT NULL DEFAULT 0 |

索引：`uk_category_shop_name_deleted(shop_id,name,deleted)`、`idx_category_shop_status_sort(shop_id,status,sort)`。

### 3.5 product

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| shop_id | BIGINT UNSIGNED | NOT NULL |
| category_id | BIGINT UNSIGNED | NOT NULL |
| name | VARCHAR(100) | NOT NULL |
| image_url | VARCHAR(500) | NOT NULL |
| description | VARCHAR(255) | 可空 |
| detail | TEXT | 可空 |
| price | DECIMAL(10,2) | NOT NULL，> 0 |
| original_price | DECIMAL(10,2) | 可空，非负 |
| stock | INT UNSIGNED | NOT NULL DEFAULT 0，商品级库存 |
| status | TINYINT | NOT NULL，0 下架/1 上架/2 售罄 |
| recommended | TINYINT | NOT NULL DEFAULT 0 |
| deleted | TINYINT | NOT NULL DEFAULT 0 |
| version | INT UNSIGNED | NOT NULL DEFAULT 0 |

索引：`idx_product_shop_category_status_deleted(shop_id,category_id,status,deleted)`、`idx_product_shop_recommended_status(shop_id,recommended,status,deleted)`。商品名不强制唯一。

### 3.6 product_specification

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| product_id | BIGINT UNSIGNED | NOT NULL |
| name | VARCHAR(64) | NOT NULL |
| price_delta | DECIMAL(10,2) | NOT NULL DEFAULT 0.00 |
| status | TINYINT | NOT NULL |
| sort | INT | NOT NULL DEFAULT 0 |
| deleted | TINYINT | NOT NULL DEFAULT 0 |

索引：`uk_product_spec_product_name_deleted(product_id,name,deleted)`、`idx_product_spec_product_status_sort(product_id,status,sort)`。

### 3.7 product_taste

字段与规格类似：`product_id`、`name VARCHAR(64)`、`status`、`sort`、`deleted`。第一版口味不加价。索引：`uk_product_taste_product_name_deleted`、`idx_product_taste_product_status_sort`。

### 3.8 shopping_cart

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| user_id | BIGINT UNSIGNED | NOT NULL |
| shop_id | BIGINT UNSIGNED | NOT NULL |
| product_id | BIGINT UNSIGNED | NOT NULL |
| specification_id | BIGINT UNSIGNED | NOT NULL DEFAULT 0，0 表示无规格 |
| taste_id | BIGINT UNSIGNED | NOT NULL DEFAULT 0，0 表示无口味 |
| quantity | INT UNSIGNED | NOT NULL，> 0 |

索引：`uk_cart_user_product_option(user_id,product_id,specification_id,taste_id)`、`idx_cart_user_shop(user_id,shop_id)`。不保存可信结算价；查询时关联当前商品计算预览。

### 3.9 address

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| user_id | BIGINT UNSIGNED | NOT NULL |
| contact_name | VARCHAR(64) | NOT NULL |
| gender | TINYINT | 可空 |
| phone | VARCHAR(20) | NOT NULL |
| area | VARCHAR(120) | NOT NULL |
| detail | VARCHAR(255) | NOT NULL |
| house_number | VARCHAR(100) | 可空 |
| tag | VARCHAR(32) | 可空 |
| is_default | TINYINT | NOT NULL DEFAULT 0 |
| deleted | TINYINT | NOT NULL DEFAULT 0 |

索引：`idx_address_user_deleted_default(user_id,deleted,is_default)`。设置默认地址时事务内先清除其他默认项。

### 3.10 orders

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| order_no | VARCHAR(32) | NOT NULL，业务唯一号 |
| idempotency_key | VARCHAR(64) | NOT NULL |
| user_id / shop_id | BIGINT UNSIGNED | NOT NULL |
| order_type | TINYINT | NOT NULL，1 堂食/2 配送 |
| status | TINYINT | NOT NULL，1—9 稳定编码 |
| total_amount | DECIMAL(10,2) | NOT NULL，商品总额 |
| delivery_fee | DECIMAL(10,2) | NOT NULL DEFAULT 0.00 |
| package_fee | DECIMAL(10,2) | NOT NULL DEFAULT 0.00 |
| pay_amount | DECIMAL(10,2) | NOT NULL；无支付系统，仅订单应付快照 |
| contact_name | VARCHAR(64) | NOT NULL |
| contact_phone | VARCHAR(20) | NOT NULL |
| table_no | VARCHAR(32) | 堂食可空 |
| no_seat_yet | TINYINT | NOT NULL DEFAULT 0 |
| address_id | BIGINT UNSIGNED | 配送原地址引用，可空 |
| address_area/detail | VARCHAR(120)/VARCHAR(255) | 配送快照 |
| address_house_number | VARCHAR(100) | 可空快照 |
| delivery_range_snapshot | VARCHAR(500) | 可空 |
| remark | VARCHAR(500) | 可空 |
| reject_reason | VARCHAR(255) | 可空 |
| version | INT UNSIGNED | NOT NULL DEFAULT 0 |
| accept_time / cooking_time | DATETIME(3) | 可空 |
| ready_time / delivery_time / delivered_time | DATETIME(3) | 可空 |
| cancel_time / finish_time | DATETIME(3) | 可空 |

索引：`uk_orders_order_no(order_no)`、`uk_orders_user_idempotency(user_id,idempotency_key)`、`idx_orders_user_create(user_id,create_time)`、`idx_orders_shop_status_create(shop_id,status,create_time)`、`idx_orders_shop_type_status_create(shop_id,order_type,status,create_time)`、`idx_orders_shop_phone(shop_id,contact_phone)`。

### 3.11 order_item

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| order_id / product_id | BIGINT UNSIGNED | NOT NULL |
| product_name | VARCHAR(100) | NOT NULL，快照 |
| product_image_url | VARCHAR(500) | NOT NULL，快照 |
| specification_id | BIGINT UNSIGNED | 可空 |
| specification_name | VARCHAR(64) | 可空快照 |
| taste_id | BIGINT UNSIGNED | 可空 |
| taste_name | VARCHAR(64) | 可空快照 |
| unit_price | DECIMAL(10,2) | NOT NULL，最终单价快照 |
| quantity | INT UNSIGNED | NOT NULL，> 0 |
| amount | DECIMAL(10,2) | NOT NULL，单价×数量 |

索引：`idx_order_item_order(order_id)`、`idx_order_item_product_create(product_id,create_time)`。

### 3.12 order_status_log

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| order_id | BIGINT UNSIGNED | NOT NULL |
| from_status / to_status | TINYINT | from 初始日志可空，to 非空 |
| operator_type | VARCHAR(20) | USER/MERCHANT/SYSTEM |
| operator_id | BIGINT UNSIGNED | 可空（系统操作） |
| reason | VARCHAR(255) | 可空、已脱敏 |
| order_version | INT UNSIGNED | NOT NULL，状态变更后的版本 |

索引：`uk_order_status_log_order_version(order_id,order_version)` 防重复日志，`idx_order_status_log_order_create(order_id,create_time)`。

## 4. 关键约束与事务

- 下单：查询有效商品/选项 → 后端重算 → 条件扣库存 → 插入订单及明细/初始日志，同一事务；幂等唯一索引兜底。
- 状态变更：`id + owner/shop + expected_status + version` 条件更新；成功后写唯一版本日志。
- 取消/拒单：状态更新、逐项恢复库存、日志同事务；只有从 `PENDING_ACCEPT` 成功转换者恢复。
- 商品/地址逻辑删除不影响订单快照；历史订单不依赖当前名称、价格或地址。
- MySQL CHECK 可作为防御，但业务错误仍由 Service 前置校验并返回稳定错误码。

## 5. Redis 设计

| Key | Value/操作 | TTL |
|---|---|---|
| `auth:user:session:{token}` | 用户 Session JSON | 7 天固定 |
| `auth:merchant:session:{token}` | 商家 Session JSON，含 sessionVersion | 12 小时固定 |
| `auth:merchant:fail:{username}` | 失败计数/窗口 | 15 分钟 |
| `auth:merchant:lock:{username}` | 锁定标记 | 30 分钟 |
| `auth:merchant:tokens:{merchantId}` | 该商家 Token 集合，支持全部失效 | 不短于会话 TTL，并同步清理 |
| `shop:status:{shopId}` | 营业开关缓存 | 建议 5 分钟 |
| `shop:config:{shopId}` | 展示/配送配置缓存 | 建议 10 分钟 |
| `order:submit:idempotency:{userId}:{key}` | PROCESSING/订单号；Lua 原子占位 | 建议 24 小时 |
| `order:no:{yyyyMMdd}` | `INCR` 当日流水 | 首次设置 3 天 |

Redis 故障不能造成静默重复订单号；数据库唯一索引是最终兜底。店铺配置以 MySQL 为准，更新提交后删除/更新缓存。第一版不使用 Redisson 和分布式锁。

## 6. 数据初始化与迁移

- 初始脚本只放示例店铺、分类、商品与 BCrypt 测试账号，不含真实生产密码。
- 脚本明确版本并在全新 MySQL 8.4 验证；禁止线上手工随意改表。
- 第一版可维护 `schema.sql/data.sql`；开始持续升级后引入 Flyway，并为每次结构变化提供不可变迁移文件。

<!-- 文档结束 -->
