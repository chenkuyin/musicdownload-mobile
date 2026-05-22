# Fly.io 部署指南

## 1. 安装 Fly CLI

### Windows (PowerShell)
```powershell
# 使用 PowerShell 安装
iwr https://fly.io/install.ps1 -useb | iex

# 或者手动下载
# 访问 https://github.com/superfly/flyctl/releases 下载 Windows 版本
# 解压后将 flyctl.exe 所在目录添加到系统 PATH
```

### 验证安装
```bash
flyctl version
```

## 2. 登录 Fly.io

```bash
flyctl auth login
```

这会打开浏览器让你登录/注册 Fly.io 账号。

## 3. 部署应用

进入 backend 目录：

```bash
cd C:\Users\chent\AI\musicDownload-mobile\backend
```

首次部署（创建应用）：

```bash
flyctl launch
```

按提示操作：
- 选择应用名称（或留空自动生成）
- 选择部署区域（推荐 hkg - 香港，延迟低）
- 选择不创建 PostgreSQL（本项目不需要）
- 选择不创建 Redis（本项目不需要）

后续更新部署：

```bash
flyctl deploy
```

## 4. 查看应用状态

```bash
# 查看应用状态
flyctl status

# 查看日志
flyctl logs

# 打开应用 URL
flyctl open
```

## 5. 配置说明

已创建的文件：

- `Dockerfile` - 容器构建配置
- `fly.toml` - Fly.io 应用配置
- `.dockerignore` - Docker 构建忽略文件

### 免费额度

- 每月 $5 免费额度
- `shared-cpu-1x` + 512MB 内存 约 $1.94/月
- 可以 24x7 持续运行，不会休眠

## 6. 更新 Android App 配置

部署成功后，Fly.io 会分配一个域名，如：
`https://musicdownload-api.fly.dev`

在 Android App 的设置中，将服务器地址改为这个域名。

## 7. 常见问题

### 构建失败
检查 Dockerfile 和 requirements.txt 是否正确。

### 内存不足
如果下载大文件时崩溃，可以修改 `fly.toml`：
```toml
[[vm]]
  size = 'shared-cpu-1x'
  memory = '1gb'  # 增加到 1GB
```

### 文件不持久化
免费实例重启后下载的文件会丢失。如需持久化存储，需要配置 Fly Volumes（付费功能）。

## 8. 销毁应用（如需）

```bash
flyctl apps destroy musicdownload-api
```
