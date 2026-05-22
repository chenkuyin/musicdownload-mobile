# Fly.io 部署脚本
# 使用前确保已安装 flyctl 并登录

$ErrorActionPreference = "Stop"

Write-Host "=== Music Download API - Fly.io 部署脚本 ===" -ForegroundColor Cyan

# 检查 flyctl
Write-Host "`n[1/4] 检查 flyctl..." -ForegroundColor Yellow
try {
    $version = flyctl version 2>$null
    Write-Host "✓ flyctl 已安装: $version" -ForegroundColor Green
} catch {
    Write-Host "✗ flyctl 未安装" -ForegroundColor Red
    Write-Host "请运行以下命令安装:" -ForegroundColor Yellow
    Write-Host "iwr https://fly.io/install.ps1 -useb | iex" -ForegroundColor White
    exit 1
}

# 检查登录状态
Write-Host "`n[2/4] 检查登录状态..." -ForegroundColor Yellow
try {
    $info = flyctl auth whoami 2>$null
    Write-Host "✓ 已登录: $info" -ForegroundColor Green
} catch {
    Write-Host "✗ 未登录" -ForegroundColor Red
    Write-Host "请运行: flyctl auth login" -ForegroundColor Yellow
    exit 1
}

# 部署
Write-Host "`n[3/4] 开始部署..." -ForegroundColor Yellow
flyctl deploy

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 部署失败" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 部署成功!" -ForegroundColor Green

# 显示信息
Write-Host "`n[4/4] 应用信息:" -ForegroundColor Yellow
flyctl status

Write-Host "`n=== 部署完成 ===" -ForegroundColor Cyan
Write-Host "API 地址: https://musicdownload-api.fly.dev" -ForegroundColor White
Write-Host "查看日志: flyctl logs" -ForegroundColor Gray
