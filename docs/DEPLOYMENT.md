# 食刻小馆生产部署与运维

本文对应 M8 的生产交付。目标链路为：微信小程序 → HTTPS → Nginx → Spring Boot → MySQL/Redis。真实域名、证书、微信凭据和服务器参数必须由部署环境提供，仓库只保留占位配置。

## 1. 部署前检查

- Linux 云服务器已安装 Docker Engine 与 Docker Compose v2。
- 域名已按所在地要求完成备案并解析到服务器。
- 服务器安全组和主机防火墙仅对公网开放 22、80、443；22 应限制管理来源。
- 已取得与 `DOMAIN` 一致的有效 HTTPS 证书。
- 已取得微信小程序正式 AppID/Secret，且未写入仓库或聊天记录。
- 生产数据库不导入 `src/main/resources/db/data.sql` 中的演示账号和示例数据。

## 2. 环境变量清单

复制 `.env.example` 为 `.env`，替换所有 `change_me`。`.env` 已被 Git 忽略，文件权限建议设为 `600`。

| 变量 | 必填 | 说明 |
|---|---|---|
| `DOMAIN` | 是 | API 正式域名，不含协议 |
| `APP_VERSION` | 是 | 镜像版本标签，建议使用发布版本或 Commit ID |
| `DEFAULT_SHOP_ID` | 是 | 第一版默认店铺 ID |
| `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | 是 | 应用数据库和最小权限账号 |
| `MYSQL_ROOT_PASSWORD` | 是 | 仅用于数据库容器初始化和受控运维 |
| `DB_POOL_MAX_SIZE` / `DB_POOL_MIN_IDLE` | 否 | 连接池上限和最小空闲连接，默认 10/2 |
| `REDIS_PASSWORD` | 是 | Redis 访问密码 |
| `USER_SESSION_TTL` / `MERCHANT_SESSION_TTL` | 否 | 固定会话有效期，默认 7 天/12 小时 |
| `WECHAT_APP_ID` / `WECHAT_APP_SECRET` | 是 | 微信 code2Session 凭据 |
| `CORS_ALLOWED_ORIGINS` | 否 | 仅浏览器管理端需要；小程序请求不依赖 CORS |
| `LOG_LEVEL_ROOT` | 否 | 生产默认 `INFO` |
| `BACKUP_DIR` / `BACKUP_RETENTION_DAYS` | 否 | 数据库备份目录和本机保留天数 |

生产 Compose 强制使用服务名 `mysql`、`redis` 互联，并固定关闭 Swagger 与微信模拟登录。应用上传目录固定为 `/app/uploads`，公开地址为 `https://<DOMAIN>/files`。

## 3. 证书与首次启动

将证书链和私钥分别放置为：

```text
deploy/certs/fullchain.pem
deploy/certs/privkey.pem
```

先检查最终配置，不要在输出中公开环境变量值：

```bash
docker compose --env-file .env -f compose.prod.yml config --quiet
docker compose --env-file .env -f compose.prod.yml build app
```

首次创建数据库后，只执行结构脚本：

```bash
set -a
. ./.env
set +a
docker compose --env-file .env -f compose.prod.yml up -d mysql redis
docker compose --env-file .env -f compose.prod.yml exec -T \
  -e MYSQL_PWD="$DB_PASSWORD" mysql \
  mysql --default-character-set=utf8mb4 -u "$DB_USERNAME" "$DB_NAME" \
  < src/main/resources/db/schema.sql
```

然后启动完整服务：

```bash
docker compose --env-file .env -f compose.prod.yml up -d
docker compose --env-file .env -f compose.prod.yml ps
```

`mysql`、`redis` 和 `app:8080` 没有宿主机端口映射；只有 Nginx 映射 80/443。MySQL、Redis、上传文件分别使用 `mysql-data`、`redis-data`、`uploads` 持久化卷。

## 4. 健康检查与联调

```bash
curl -fsS "https://$DOMAIN/api/common/health"
curl -I "http://$DOMAIN/api/common/health"
docker compose --env-file .env -f compose.prod.yml ps
docker compose --env-file .env -f compose.prod.yml logs --tail=200 app nginx
```

验收结果应满足：HTTPS 健康接口返回 `code=0`；HTTP 返回 301 并跳转 HTTPS；四个容器均为 healthy；日志中包含 requestId，且不出现密码、完整 Token、微信 Secret、完整手机号或地址。

契约联调按以下顺序执行，并保存订单号与 requestId：

1. 堂食：用户登录 → 加购物车 → 创建堂食订单 → 商家接单 → 开始制作 → 待取餐 → 商家完成。
2. 配送：用户登录 → 创建地址 → 加购物车 → 创建配送订单 → 商家接单 → 开始制作 → 开始配送 → 标记送达 → 用户确认收货。
3. 分别查询用户订单详情和商家订单详情，确认最终状态、金额快照、商品快照及状态日志顺序一致。

真实微信联调前，在微信公众平台将 `https://<DOMAIN>` 配置为 request 合法域名；如图片使用同域名，也确认下载/上传相关域名配置。域名必须备案、证书有效且不能使用测试 IP；前端不得写死 IP 或 HTTP 地址。

## 5. 备份、恢复与演练

每日低峰期执行数据库备份，并将备份异地复制：

```bash
chmod 700 deploy/scripts/*.sh
./deploy/scripts/backup.sh
gzip -t backups/shike_ordering_YYYYMMDD_HHMMSS.sql.gz
```

上传文件需同步备份；Redis 只保存会话、缓存和临时幂等数据，不作为订单最终数据源：

```bash
docker run --rm \
  -v shike-ordering_uploads:/source:ro \
  -v "$PWD/backups:/backup" \
  alpine tar -czf "/backup/uploads_$(date +%Y%m%d_%H%M%S).tar.gz" -C /source .
```

正式恢复前进入维护窗口，停止入口和应用，先备份当前库，再显式确认恢复：

```bash
docker compose --env-file .env -f compose.prod.yml stop nginx app
./deploy/scripts/backup.sh
RESTORE_CONFIRM=YES ./deploy/scripts/restore.sh backups/<backup.sql.gz>
docker compose --env-file .env -f compose.prod.yml up -d app nginx
curl -fsS "https://$DOMAIN/api/common/health"
```

至少每季度做一次不覆盖生产库的恢复演练：复制 `.env` 为仅限运维读取的 `.env.restore`，把 `DB_NAME` 改为临时恢复库；创建该空库后用 `ENV_FILE=.env.restore RESTORE_CONFIRM=YES` 执行恢复。核对核心表存在、订单/明细/日志数量合理、抽样订单金额与状态日志一致，记录耗时和结果，随后删除临时库。任何恢复失败都不得直接反复覆盖生产库，应保留原备份和错误输出后分析。

## 6. 升级、回滚与日常运维

升级前先备份数据库和上传卷，执行 `mvn test`、`mvn clean package`、Compose 配置校验，再设置新的 `APP_VERSION` 构建启动。上线后检查健康接口、登录、下单查询和容器日志。

应用回滚只切回上一镜像版本；涉及表结构变化时必须使用与该版本匹配且经过演练的迁移/恢复方案，禁止线上手工改表。第一版仍使用版本化 `schema.sql` 初始化，全量结构开始持续演进后再迁移到 Flyway。

常用命令：

```bash
docker compose --env-file .env -f compose.prod.yml logs -f --tail=200 app
docker compose --env-file .env -f compose.prod.yml restart app
docker compose --env-file .env -f compose.prod.yml ps
docker system df
```

建议监控：HTTPS 可用性、容器健康状态、磁盘/卷容量、数据库连接数、5xx 比例、Redis 可用性、备份生成时间与异地复制结果。证书到期前完成续签并重新加载 Nginx。
