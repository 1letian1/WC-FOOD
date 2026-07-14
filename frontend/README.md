# 食刻小馆 uni-app 前端

基于 Vue 3 + uni-app 的微信点餐前端，包含用户端和商家端，接口直接对接仓库中的 Spring Boot 后端。

## 启动

```bash
cd frontend
npm install
copy .env.example .env
npm run dev:h5
```

微信小程序开发：

```bash
npm run dev:mp-weixin
```

然后在微信开发者工具中导入 `frontend/dist/dev/mp-weixin`。正式构建使用：

```bash
npm run build:h5
npm run build:mp-weixin
```

微信小程序发布前，需要在 `src/manifest.json` 中填写真实 AppID，并在微信公众平台配置后端 HTTPS 域名为 request/uploadFile 合法域名。

## 联调配置

- `VITE_API_BASE_URL`：后端地址，默认 `http://localhost:8080`。
- `VITE_WECHAT_MOCK_CODE`：仅用于 H5 本地联调；后端同时需要显式开启 `WECHAT_MOCK_ENABLED=true`。
- 用户 Token 和商家 Token 分别保存，不共享登录态。
- 后端图片字段既支持完整 URL，也支持 `/images/...` 相对路径。

## 页面范围

- 用户端：微信登录、首页、规格口味、购物车、堂食/配送下单、订单列表与详情、取消订单、确认收货、资料和地址管理。
- 商家端：账号登录、工作台、订单筛选与完整履约、商品管理与图片上传、店铺设置、改密和退出登录。
