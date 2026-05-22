# 部署指南

## Fly.io 自动部署（推荐）

### 1. 创建 GitHub 仓库

访问 https://github.com/new 创建新仓库，例如 `musicdownload-mobile`

### 2. 推送代码到 GitHub

```bash
cd C:\Users\chent\AI\musicDownload-mobile
git remote add origin https://github.com/你的用户名/musicdownload-mobile.git
git branch -M main
git push -u origin main
```

### 3. 获取 Fly.io API Token

**方法 A：使用其他机器（推荐）**
```bash
# 在能访问 Fly.io 的机器上
flyctl auth login
flyctl tokens create deploy -x 999999h
```

**方法 B：Fly.io 网页控制台**
1. 访问 https://fly.io/dashboard
2. 登录账号
3. Account Settings → Tokens
4. Create Deploy Token

### 4. 配置 GitHub Secrets

在 GitHub 仓库页面：
1. Settings → Secrets and variables → Actions
2. 点击 "New repository secret"
3. Name: `FLY_API_TOKEN`
4. Value: 上一步获取的 token

### 5. 触发部署

推送代码后，GitHub Actions 会自动部署：
```bash
git add .
git commit -m "Update for deployment"
git push
```

查看部署状态：
- GitHub 仓库 → Actions 标签页

部署成功后访问：`https://musicdownload-api.fly.dev`

---

## Render.com 部署（备选）

如果 Fly.io 部署困难，可以使用 Render.com：

1. 访问 https://render.com
2. 用 GitHub 账号登录
3. New → Web Service
4. 选择你的 GitHub 仓库
5. 配置：
   - **Name**: musicdownload-api
   - **Root Directory**: `backend`
   - **Build Command**: `pip install -r requirements.txt`
   - **Start Command**: `uvicorn main:app --host 0.0.0.0 --port $PORT`
   - **Plan**: Free
6. 点击 "Create Web Service"

Render 免费计划会休眠（15分钟无访问后），但有 512MB 内存。

---

## 部署配置说明

### Fly.io 配置 (`backend/fly.toml`)

```toml
app = 'musicdownload-api'
primary_region = 'hkg'  # 香港区域，延迟较低

[http_service]
  internal_port = 8080
  force_https = true
  auto_stop_machines = 'off'  # 保持运行，不休眠
  min_machines_running = 1

[[vm]]
  size = 'shared-cpu-1x'
  memory = '512mb'
```

### GitHub Actions 配置 (`.github/workflows/deploy-fly.yml`)

- 触发条件：推送到 main/master 分支且 backend 目录有变更
- 支持手动触发（workflow_dispatch）
- 使用 `--remote-only` 在 Fly.io 远程构建

---

## 常见问题

### Q: 如何更新已部署的服务？
A: 修改代码后推送即可自动部署：
```bash
git add .
git commit -m "Update API"
git push
```

### Q: 如何查看部署日志？
A: 
- Fly.io: `flyctl logs`（需安装 flyctl）
- GitHub: 仓库 → Actions → 点击工作流运行记录

### Q: 如何修改应用名称或区域？
A: 编辑 `backend/fly.toml` 中的 `app` 和 `primary_region`，然后推送。

可用区域：
- `hkg` - 香港（推荐，延迟低）
- `nrt` - 东京
- `sin` - 新加坡
- `lax` - 洛杉矶
- `iad` - 华盛顿

### Q: 内存不足怎么办？
A: 编辑 `fly.toml` 增加内存：
```toml
[[vm]]
  memory = '1gb'  # 增加到 1GB
```

---

## 本地测试 Docker 构建

如果本地有 Docker，可以测试构建：

```bash
cd backend
docker build -t musicdownload-api .
docker run -p 8080:8080 musicdownload-api
```

然后访问 http://localhost:8080
