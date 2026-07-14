# HTTPS 证书目录

生产启动前在本目录放置当前域名的证书文件：

- `fullchain.pem`：完整证书链
- `privkey.pem`：证书私钥

证书和私钥已被 `.gitignore` 排除，不得提交到仓库。文件应只允许部署账号读取。
