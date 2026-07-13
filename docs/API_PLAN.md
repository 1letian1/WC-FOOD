# 食刻小馆 API 规划

## 1. 通用契约

- 前缀：`/api/common/**`、`/api/user/**`、`/api/merchant/**`。
- 鉴权：`Authorization: Bearer <token>`；公开接口例外。
- 普通响应为 `Result<T>`，分页为 `Result<PageResult<T>>`；`current=1`、`size=10`、最大 100。
- 响应固定含 `code/message/data/requestId/timestamp`，响应头同时携带 `X-Request-Id`。
- OpenAPI 3 按公共、用户、商家分组；dev/test 开放，prod 默认关闭。
- 不返回 Entity、任意 Map 或原始异常；枚举返回稳定 code 和 description。

## 2. 公共接口

| 方法与路径 | 认证 | 用途 |
|---|---|---|
| GET `/api/common/health` | 否 | 轻量健康检查 |
| GET `/api/common/shop` | 否 | 店铺公开信息、营业与履约开关 |

## 3. 用户端接口

### 3.1 登录与资料

| 方法与路径 | 用途 |
|---|---|
| POST `/api/user/auth/wechat-login` | `code` 换登录身份；返回一次 Token 和用户信息 |
| POST `/api/user/auth/logout` | 删除当前会话 |
| GET `/api/user/profile` | 当前用户资料 |
| PUT `/api/user/profile` | 修改允许的昵称、头像、手机号 |

### 3.2 商品目录

| 方法与路径 | 用途 |
|---|---|
| GET `/api/user/categories` | 有效分类列表 |
| GET `/api/user/products` | 按分类/关键词分页浏览 |
| GET `/api/user/products/{id}` | 商品详情、有效规格和口味 |

商品浏览可公开；购物车和下单必须 USER 登录。排序参数映射后端白名单。

### 3.3 购物车与地址

| 方法与路径 | 用途 |
|---|---|
| GET `/api/user/cart` | 本人购物车及服务端预览金额 |
| POST `/api/user/cart/items` | 商品、规格、口味、数量；相同组合合并 |
| PUT `/api/user/cart/items/{id}` | 修改本人条目数量 |
| DELETE `/api/user/cart/items/{id}` | 删除本人条目 |
| DELETE `/api/user/cart/items` | 清空本人购物车 |
| GET `/api/user/addresses` | 地址列表 |
| GET `/api/user/addresses/{id}` | 本人地址详情 |
| POST `/api/user/addresses` | 新增地址 |
| PUT `/api/user/addresses/{id}` | 修改本人地址 |
| PUT `/api/user/addresses/{id}/default` | 事务内设为唯一默认地址 |
| DELETE `/api/user/addresses/{id}` | 逻辑删除本人地址 |

购物车新增请求中的 `quantity` 表示本次增加数量，修改请求中的 `quantity` 表示修改后的绝对数量，均为 1—99；相同商品、规格和口味组合在同一用户事务内合并。购物车响应使用当前商品基础价与规格加价计算预览金额，并返回当前是否可结算，但正式下单仍重新查询商品、价格和库存。地址性别可空，`1` 表示先生、`2` 表示女士；默认地址变更按用户串行化，任一用户至多一个有效默认地址。

### 3.4 订单

| 方法与路径 | 用途/约束 |
|---|---|
| POST `/api/user/orders` | 创建堂食/配送订单；请求头 `Idempotency-Key` 必填 |
| GET `/api/user/orders` | 本人订单分页，支持状态/类型筛选 |
| GET `/api/user/orders/{id}` | 本人订单详情与状态日志 |
| PUT `/api/user/orders/{id}/cancel` | 仅本人 `PENDING_ACCEPT` |
| PUT `/api/user/orders/{id}/confirm-receipt` | 仅本人配送 `DELIVERED` |
| POST `/api/user/orders/{id}/reorder` | 后续功能：有效项回填购物车 |

创建订单使用交叉校验：堂食必须有联系人、电话及桌号或 `noSeatYet=true`；配送必须有本人 `addressId`。请求不接受金额、状态、userId、shopId 作为可信数据。

创建请求统一包含 `orderType`、本次结算的 `cartItemIds`、`noSeatYet` 和可选备注。堂食请求额外包含联系人、手机号及桌号或暂未入座状态，且不得包含地址；配送请求只提交本人 `addressId`，联系人、手机号和地址快照均从地址表读取，且不得夹带堂食字段。`Idempotency-Key` 为 8—64 位字母、数字、下划线或短横线；成功重试返回原订单，处理中返回 60004。服务端使用当前商品与规格价格重算，配送订单校验起送金额并收取配送费，包装费按店铺当前配置保存快照；只在成功后清理本次提交的购物车条目。

## 4. 商家端接口

### 4.1 登录、工作台与账号

| 方法与路径 | 用途 |
|---|---|
| POST `/api/merchant/auth/login` | 账号密码登录；统一错误提示和失败限制 |
| POST `/api/merchant/auth/logout` | 删除当前会话 |
| GET `/api/merchant/profile` | 当前商家资料 |
| PUT `/api/merchant/password` | 修改密码，成功后全部旧会话失效 |
| GET `/api/merchant/dashboard` | 今日订单额及各待处理状态统计 |

### 4.2 订单

| 方法与路径 | 合法动作 |
|---|---|
| GET `/api/merchant/orders` | 类型/状态/订单号/电话/用户名称分页筛选 |
| GET `/api/merchant/orders/{id}` | 本店订单详情 |
| PUT `/api/merchant/orders/{id}/accept` | 1 → 2 |
| PUT `/api/merchant/orders/{id}/reject` | 1 → 9，原因必填并恢复库存 |
| PUT `/api/merchant/orders/{id}/start-cooking` | 2 → 3 |
| PUT `/api/merchant/orders/{id}/ready-for-pickup` | 仅堂食 3 → 4 |
| PUT `/api/merchant/orders/{id}/start-delivery` | 仅配送 3 → 5 |
| PUT `/api/merchant/orders/{id}/mark-delivered` | 仅配送 5 → 6 |
| PUT `/api/merchant/orders/{id}/complete` | 仅堂食 4 → 7 |

每个动作从会话取 merchantId/shopId，使用预期状态与版本条件更新；不提供通用 `/status` 接口。配送商家没有 6→7 权限。

### 4.3 分类、商品与店铺

| 方法与路径 | 用途 |
|---|---|
| GET/POST `/api/merchant/categories` | 分类列表/新增 |
| PUT `/api/merchant/categories/{id}` | 编辑分类 |
| PUT `/api/merchant/categories/{id}/status` | 启停分类 |
| DELETE `/api/merchant/categories/{id}` | 逻辑删除 |
| GET/POST `/api/merchant/products` | 商品分页/新增 |
| GET `/api/merchant/products/{id}` | 商品详情 |
| PUT `/api/merchant/products/{id}` | 编辑商品及规格口味 |
| PUT `/api/merchant/products/{id}/on-sale` | 上架 |
| PUT `/api/merchant/products/{id}/off-sale` | 下架 |
| PUT `/api/merchant/products/{id}/sold-out` | 标记售罄 |
| DELETE `/api/merchant/products/{id}` | 逻辑删除 |
| GET `/api/merchant/shop` | 本店完整配置 |
| PUT `/api/merchant/shop` | 修改基础与配送配置 |
| PUT `/api/merchant/shop/business-status` | 营业/休息 |
| PUT `/api/merchant/shop/dine-in-status` | 开关堂食 |
| PUT `/api/merchant/shop/delivery-status` | 开关配送 |
| POST `/api/merchant/files/images` | 上传 JPEG/PNG/GIF 图片，最大 5MB |

商品新增和编辑一次提交基础字段及规格、口味列表。编辑已有规格或口味时传对应 `id`，新增项不传 `id`；请求中缺失的旧项逻辑删除。所有 `id` 必须属于当前会话店铺及当前商品。

## 5. 参数校验

- 文本 trim 后校验；密码首尾空格直接判无效。密码 8—20 位、至少字母和数字、不等于账号或旧密码。
- 手机号默认 `^1[3-9]\d{9}$`；名称、备注、拒单原因和地址均设置长度上限。
- ID 为正数；数量建议 1—99；库存非负；价格大于 0，费用非负且最多两位小数。
- `current≥1`、`size=1..100`；排序字段与方向使用白名单。
- 文件校验大小、扩展名、实际 MIME；服务端随机文件名并阻断目录穿越。
- Service 再校验归属、营业状态、商品状态、规格口味关联、金额和库存。

## 6. 统一错误码与 HTTP 状态

| 范围 | 模块 | 典型错误 |
|---|---|---|
| 10000 | 通用参数 | 10001 参数错误、10002 格式错误、10003 方法错误、10004 接口不存在 |
| 20000 | 用户/会话 | 20001 未登录或过期、20002 无权限 |
| 30000 | 商家 | 30001 账号或密码错误、30002 禁用、30003 锁定 |
| 40000 | 商品分类 | 40001 商品不存在、40002 下架、40003 售罄、40004 库存不足、40005 分类不存在、40006 规格不可用、40007 口味不可用 |
| 50000 | 购物车地址 | 50001 购物车为空、50002 地址不存在、50003 购物车条目不存在、50004 购物车数量超过99 |
| 60000 | 订单 | 60001 不存在、60002 状态变化、60003 无权、60004 重复提交 |
| 70000 | 店铺 | 70001 休息、70002 堂食暂停、70003 配送暂停、70004 未达起送额 |
| 70005 | 店铺查询 | 70005 店铺不存在 |
| 80000 | 文件 | 80001 类型不支持、80002 超限、80003 保存失败 |
| 90000 | 外部服务 | 90001 微信服务失败、90002 Redis 临时不可用 |
| 99999 | 未知异常 | 系统繁忙，请稍后重试 |

HTTP：成功 200，参数 400，未登录 401，无权限 403，不存在 404，状态冲突/重复操作 409，未知异常 500。全局异常处理器返回 `ResponseEntity<Result<?>>`，不暴露内部异常。

## 7. 安全、跨域、幂等与并发

- 自定义 `OncePerRequestFilter` 作为唯一鉴权链路；建立请求身份上下文并在 finally 清理。
- Token 至少 256 bit、URL-safe、固定 TTL；密码 BCrypt strength 12；不记录密码或完整 Token。
- 下单：客户端随机键 + Redis 24 小时原子占位 + `orders(user_id,idempotency_key)` 唯一约束；事务提交后占位值更新为订单号，回滚后释放；成功重试返回同一订单，处理中返回 409。
- 状态动作：数据库按 owner/shop、expectedStatus、version 条件更新；0 行返回 409，不写日志、不恢复库存。
- 后端以 BigDecimal 重算金额并条件扣库存；订单、明细、日志、库存同事务。
- 数据归属条件直接进入查询；前端 userId/shopId 不可信。
- CORS 只允许明确本地开发来源；正式微信小程序依赖 HTTPS 请求域名配置。
- 分类、商品、地址逻辑删除；订单、明细、状态日志永久保留。

## 8. OpenAPI、测试与验收

每个公开接口提供中文名称、字段 Schema、枚举含义、响应示例和 Bearer 声明。接口测试至少覆盖：成功、参数错误、未登录、越权、不存在、重复提交、状态冲突、未知异常脱敏，以及 prod 关闭 Swagger。

本地使用 dev/test profile 联调；云端经 Nginx HTTPS 反代，应用端口、MySQL、Redis 不直接暴露公网。请求域名、上传公开基址和真实微信配置全部由环境变量注入。

## 9. 当前待确认

没有阻塞接口骨架和核心闭环的问题。真实微信联调前需取得 AppID/Secret；生产域名与上传公开基址在部署前确定。规格加价、包装费和预计时间已按 `PROJECT_ANALYSIS.md` 的保守默认规划，后续变化须同步数据库、API、测试与产品说明。

<!-- 文档结束 -->
