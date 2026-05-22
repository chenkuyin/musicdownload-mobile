# 部署准备清单

## ✅ 已完成

- [x] Git 仓库初始化
- [x] `.gitignore` 配置（排除 venv、build 等）
- [x] `backend/Dockerfile` - Docker 构建配置
- [x] `backend/.dockerignore` - Docker 构建优化
- [x] `backend/fly.toml` - Fly.io 配置（香港区域）
- [x] `.github/workflows/deploy-fly.yml` - GitHub Actions 自动部署
- [x] `DEPLOY.md` - 详细部署文档

---

## 📋 待完成（需要你操作）

### 1. 创建 GitHub 仓库

访问 https://github.com/new
- Repository name: `musicdownload-mobile`（或其他名称）
- 选择 Public 或 Private
- 不要勾选 "Add a README"（已有 README）

### 2. 推送代码

```powershell
cd C:\Users\chent\AI\musicDownload-mobile
git remote add origin https://github.com/你的用户名/musicdownload-mobile.git
git branch -M main
git push -u origin main
```

### 3. 获取 Fly.io API Token

**方法 A：在其他机器上操作**
```bash
flyctl auth login
flyctl tokens create deploy -x 999999h
```

**方法 B：网页控制台**
1. 访问 https://fly.io/dashboard
2. 登录账号
3. Account Settings → Tokens → Create Deploy Token

### 4. 配置 GitHub Secrets

在 GitHub 仓库页面：
1. Settings → Secrets and variables → Actions
2. New repository secret
3. Name: `FLY_API_TOKEN`
4. Value: 上一步获取的 token

### 5. 触发部署

推送任意更新触发自动部署：
```powershell
git commit --allow-empty -m "Trigger deployment"
git push
```

或在 GitHub 仓库 → Actions → Deploy to Fly.io → Run workflow

---

## 🔧 部署后配置

### 更新 Android App 服务器地址

部署成功后，在 App 设置中修改服务器地址：

| 环境 | 地址 |
|------|------|
| Fly.io | `https://musicdownload-api.fly.dev` |
| Render.com | `https://你的服务名.onrender.com` |
| 本地测试 | `http://10.0.2.2:8000`（模拟器）|

修改路径：App → 设置 → 服务器地址

---

## 📊 部署状态检查

### GitHub Actions
- 地址：`https://github.com/你的用户名/musicdownload-mobile/actions`
- 查看工作流运行状态和日志

### Fly.io 控制台
- 地址：https://fly.io/dashboard
- 查看应用状态、日志、资源使用

### API 测试
部署成功后测试：
```bash
curl https://musicdownload-api.fly.dev/
curl https://musicdownload-api.fly.dev/sources
```

---

## 🆘 故障排查

### 部署失败
1. 检查 GitHub Actions 日志
2. 确认 `FLY_API_TOKEN` 是否正确设置
3. 确认 fly.toml 中的应用名称是否可用

### 应用启动失败
1. 检查 Fly.io 日志：`flyctl logs`（如有 flyctl）
2. 或访问 Fly.io 控制台查看日志
3. 常见原因：内存不足、依赖安装失败

### API 无法访问
1. 检查服务是否运行：`curl https://musicdownload-api.fly.dev/`
2. 检查防火墙/网络设置
3. 确认端口配置正确（8080）

---

## 💰 费用说明

### Fly.io 免费额度
- 共享 CPU：最多 3 个机器同时运行
- 内存：256MB（当前配置 512MB 可能产生费用）
- 带宽：每月 160GB 出站流量

### 建议
如需完全免费，可修改 `fly.toml`：
```toml
[[vm]]
  size = 'shared-cpu-1x'
  memory = '256mb'  # 改为 256MB
```

注意：256MB 内存可能不足以运行 musicdl，建议先用 512MB 测试。

---

## 📱 下一步

部署完成后：
1. 更新 Android App 中的服务器地址
2. 重新编译 APK
3. 测试搜索和下载功能
4. 如需修改 API 地址，编辑 `RetrofitClient.kt` 中的 `BASE_URL`

---

## 🔗 相关链接

- Fly.io 控制台：https://fly.io/dashboard
- GitHub 仓库：（创建后填写）
- API 地址：https://musicdownload-api.fly.dev（部署后）
