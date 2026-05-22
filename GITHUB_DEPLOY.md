# GitHub Actions 自动部署到 Fly.io

由于本地网络限制无法下载 flyctl，使用 GitHub Actions 自动部署是更好的方案。

## 步骤

### 1. 创建 GitHub 仓库

```bash
cd C:\Users\chent\AI\musicDownload-mobile
git init
git add .
git commit -m "Initial commit"
```

在 GitHub 上创建新仓库，然后：

```bash
git remote add origin https://github.com/你的用户名/musicDownload-mobile.git
git push -u origin main
```

### 2. 获取 Fly.io API Token

由于本地无法安装 flyctl，你需要在一台能访问 Fly.io 的机器上操作，或者使用 Fly.io 网页控制台：

**方法 A：使用其他机器**
```bash
# 在有 flyctl 的机器上登录
flyctl auth login

# 创建 API Token
flyctl tokens create deploy -x 999999h
```

**方法 B：直接在 Fly.io 网站创建**
1. 访问 https://fly.io/dashboard
2. 登录你的账号
3. 进入 Account Settings → Tokens
4. 创建 Deploy Token

### 3. 配置 GitHub Secrets

在 GitHub 仓库页面：
1. Settings → Secrets and variables → Actions
2. 点击 "New repository secret"
3. Name: `FLY_API_TOKEN`
4. Value: 上一步获取的 token

### 4. 首次创建应用

如果你有办法临时使用 flyctl（比如在朋友电脑上，或使用 GitHub Codespaces）：

```bash
cd backend
flyctl launch --name musicdownload-api --region hkg
```

或者直接在 `fly.toml` 中确认应用名，GitHub Actions 会自动创建应用。

### 5. 推送代码触发部署

```bash
git add .
git commit -m "Add GitHub Actions deploy"
git push
```

GitHub Actions 会自动运行，将后端部署到 Fly.io。

## 查看部署状态

- 在 GitHub 仓库的 Actions 标签页查看部署日志
- 部署成功后，访问 `https://musicdownload-api.fly.dev`

## 后续更新

每次推送代码到 main 分支，GitHub Actions 会自动重新部署。

## 备选方案：Render.com

如果 Fly.io 部署困难，可以考虑 Render.com：

1. 访问 https://render.com
2. 用 GitHub 账号登录
3. New → Web Service
4. 选择你的 GitHub 仓库
5. 配置：
   - Root Directory: `backend`
   - Build Command: `pip install -r requirements.txt`
   - Start Command: `uvicorn main:app --host 0.0.0.0 --port $PORT`
6. 选择免费计划

Render 免费计划会休眠（15分钟无访问后），但有 512MB 内存，部署更简单。
