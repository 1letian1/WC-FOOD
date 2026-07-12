# 食刻小馆实施计划

## 1. 实施原则

- 单体 Spring Boot 3.5.x、JDK 21、Maven；不拆微服务。
- 每个阶段做到可编译、可测试、可运行，再进入下一阶段。
- 先守住统一响应、鉴权、数据归属、金额重算、状态机和事务，再完善便利功能。
- 本轮只形成规划；不创建 Controller、Service、Mapper、Entity 或 SQL 业务实现。

## 2. 推荐目录结构

```text
WC-Food/
├── docs/
├── YuanXing/                         # 只读高保真原型参考
├── src/main/java/com/shike/ordering/
│   ├── ShikeOrderingApplication.java
│   ├── common/{constant,enums,exception,result,util,validation}
│   ├── config/
│   ├── auth/{annotation,filter,model,service}
│   ├── controller/{common,user,merchant}
│   ├── service/{user,merchant}
│   ├── service/impl/{user,merchant}
│   ├── mapper/
│   ├── entity/
│   ├── dto/{common,user,merchant}
│   ├── vo/{common,user,merchant}
│   ├── converter/
│   ├── client/wechat/
│   ├── storage/
│   └── task/
├── src/main/resources/{mapper,db}/
├── src/test/
├── deploy/nginx/
├── Dockerfile
├── compose.yml
└── .env.example
```

## 3. 后端模块划分

| 模块 | 职责 |
|---|---|
| common | 统一 `Result/PageResult`、错误码、异常、requestId、通用校验 |
| auth | Token 创建、Redis 会话、身份上下文、权限过滤、退出登录 |
| user auth | 微信 code2Session、用户查询/创建、dev mock |
| merchant auth | BCrypt 登录、失败次数限制、改密和会话失效 |
| shop | 店铺展示、营业/堂食/配送配置与缓存 |
| catalog | 分类、商品、规格、口味、上下架、售罄、图片 |
| cart | 当前用户购物车组合项与金额预览 |
| address | 地址归属、默认地址、逻辑删除 |
| ordering | 下单、金额重算、库存、订单号、快照、幂等 |
| fulfillment | 用户/商家查单、状态操作、状态日志、工作台统计 |
| infrastructure | MyBatis-Plus、Redis、OpenAPI、存储、Docker、Nginx |

## 4. 分阶段里程碑与验收标准

### M1：可运行项目骨架（推荐首先开发）

内容：Maven/JDK 21、环境配置、统一响应与异常、requestId/MDC、OpenAPI 分组、MyBatis-Plus/Redis 配置、健康检查、测试骨架、基础 Compose。

验收：

- `mvn test` 与 `mvn clean package` 成功。
- dev/test 可访问健康检查和 Swagger；prod 默认关闭 Swagger。
- 参数错误和未知接口返回统一结构及正确 HTTP 状态。
- 响应头/响应体均有 requestId，日志结束后清理 MDC。
- 不含真实凭据，不出现业务 Controller/Service 的半成品。

### M2：数据库与基础数据

内容：建表脚本、枚举编码、Entity/Mapper、自动填充、分页、开发测试数据。

当前进度：已完成公开店铺信息查询垂直切片，包括 `shop` 表、稳定营业状态编码、Entity/Mapper/XML、Service、公开 Controller 与测试；M2 其余核心表仍待后续切片完成。

验收：

- MySQL 8.4 全新实例可一次初始化；所有表/索引与 `DATABASE_DESIGN.md` 一致。
- 金额、时间、字符集、逻辑删除及唯一约束正确。
- BCrypt 测试账号使用非生产哈希；订单状态编码测试锁定。

### M3：双端登录与权限

内容：微信 Client 抽象、受控 mock、用户/商家 Redis 会话、BCrypt、失败锁定、身份过滤器、退出和改密失效。

验收：

- 用户/商家 Token 不能跨端访问；过期与退出均返回 401。
- 商家连续失败达到阈值被锁定，成功后清除计数。
- 不可信 openid、shopId、userId 不可从请求覆盖当前身份。
- prod 无法启用模拟微信登录。

### M4：店铺、分类与商品

内容：店铺公共查询/商家设置、分类管理、商品 CRUD/状态、规格口味、图片存储。

验收：

- 休息/堂食暂停/配送暂停准确影响用户可下单能力。
- 商品逻辑删除不影响历史快照；跨店铺操作被拒绝。
- 上传校验类型、MIME、大小和文件名；缓存更新策略可验证。

### M5：购物车与地址

内容：组合项增删改查/清空、地址 CRUD、默认地址。

验收：

- 同用户同商品规格口味不重复建项而是合并数量。
- 只能操作本人数据；数量与分页参数边界校验完整。
- 任一用户至多一个有效默认地址，并发场景有测试。

### M6：安全下单

内容：堂食/配送 DTO、店铺与商品校验、金额重算、条件扣库存、订单号、快照、幂等、购物车清理。

验收：

- 篡改前端金额、用户、店铺、地址均无效。
- 库存不足、下架、售罄、未达起送价、关闭履约方式均失败且不产生半订单。
- 相同幂等键只生成一张订单；任一明细失败整单回滚。
- 堂食不产生配送费/地址，配送保存完整地址和费用快照。

### M7：订单查询与状态流转

内容：用户/商家列表详情、工作台统计、接单、拒单、制作、待取餐、配送、送达、确认收货、取消、库存恢复和日志。

验收：

- 堂食和配送仅允许文档规定的转换；每个动作独立接口。
- 并发重复动作最多一次成功，且只产生一条状态日志。
- 取消/拒单只恢复一次库存；状态与日志/库存同事务。
- 用户与商家数据归属测试、列表复杂查询和分页语义通过。

### M8：联调、质量与部署

内容：完整 OpenAPI、契约联调、日志脱敏、测试完善、镜像、Nginx、HTTPS 示例、备份和运维文档。

验收：

- 用户堂食和配送两条端到端闭环通过。
- Docker Compose 使用服务名互联，MySQL/Redis/8080 不直接暴露公网。
- 仅 80/443（及运维需要的 22）对外；HTTP 跳 HTTPS；数据和上传目录持久化。
- 提供恢复演练步骤、健康检查、环境变量清单和微信域名配置说明。

## 5. 测试策略

- 单元测试：状态机、金额、密码、Token、校验器和转换器。
- Service 集成测试：事务回滚、库存扣减/恢复、幂等、权限归属。
- Mapper 测试：订单筛选、统计、分页 count 一致性、显式列映射。
- API 测试：统一响应、HTTP 状态、鉴权、请求格式和异常脱敏。
- 并发测试：重复接单、取消/拒单竞态、重复提交、默认地址竞态。
- 端到端验收：堂食完整闭环、配送完整闭环、商家拒单闭环。

## 6. 本地开发与云部署

本地建议以 Compose 启动 MySQL 8.4 和 Redis，应用使用 `dev` profile；微信登录用显式环境开关启用 mock。测试 profile 使用隔离数据库/Redis DB 或测试容器策略，不能连接生产资源。

生产链路：微信小程序 → HTTPS → Nginx → Spring Boot → MySQL/Redis。配置全部由环境变量注入，上传、MySQL、Redis 使用持久化卷；应用滚动前先备份数据库。第一版可使用版本化 `schema.sql`，结构进入持续迭代后迁移到 Flyway。

## 7. 依赖与风险控制

- 只采用 AGENTS.md 已批准的核心依赖；文件存储通过接口抽象，不预先引入云厂商 SDK。
- 不用 Redis 锁替代订单条件更新，不用缓存替代 MySQL 最终状态。
- 不为原型中的 UI 库向后端增加依赖。
- 每个里程碑若改变接口、表结构、状态规则或环境变量，同步更新文档、测试与示例配置。

## 8. 当前阻塞项

无阻塞 M1—M7 本地开发的产品问题。M3 的真实微信联调及 M8 上线需要外部提供 AppID/Secret、备案域名、HTTPS 证书、云服务器规格与持久化目录；在提供前使用受控 mock 和占位环境变量推进。

<!-- 文档结束 -->
