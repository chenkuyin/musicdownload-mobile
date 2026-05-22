# Fly.io CLI 安装脚本 (Windows)

$ErrorActionPreference = "Stop"

Write-Host "=== 安装 Fly.io CLI ===" -ForegroundColor Cyan

$installDir = "$env:USERPROFILE\.fly\bin"
$zipPath = "$env:TEMP\flyctl.zip"

# 创建安装目录
New-Item -ItemType Directory -Force -Path $installDir | Out-Null

# 获取最新版本
Write-Host "获取最新版本..." -ForegroundColor Yellow
try {
    $release = Invoke-RestMethod -Uri "https://api.github.com/repos/superfly/flyctl/releases/latest" -TimeoutSec 30
    $version = $release.tag_name
    Write-Host "最新版本: $version" -ForegroundColor Green
} catch {
    Write-Host "无法获取最新版本，使用默认版本 v0.3.104" -ForegroundColor Yellow
    $version = "v0.3.104"
}

# 下载
$downloadUrl = "https://github.com/superfly/flyctl/releases/download/$version/flyctl_${version}_Windows_x86_64.zip"
$downloadUrl = $downloadUrl -replace "v", ""  # 移除版本号中的 v

Write-Host "下载 flyctl..." -ForegroundColor Yellow
Write-Host "URL: $downloadUrl" -ForegroundColor Gray

try {
    Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath -TimeoutSec 120
    Write-Host "✓ 下载完成" -ForegroundColor Green
} catch {
    Write-Host "✗ 下载失败: $_" -ForegroundColor Red
    Write-Host "请手动下载: https://github.com/superfly/flyctl/releases" -ForegroundColor Yellow
    exit 1
}

# 解压
Write-Host "解压..." -ForegroundColor Yellow
try {
    Expand-Archive -Path $zipPath -DestinationPath $installDir -Force
    Write-Host "✓ 解压完成" -ForegroundColor Green
} catch {
    Write-Host "✗ 解压失败: $_" -ForegroundColor Red
    exit 1
}

# 清理
Remove-Item $zipPath -ErrorAction SilentlyContinue

# 添加到 PATH
Write-Host "`n添加到系统 PATH..." -ForegroundColor Yellow
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($currentPath -notlike "*$installDir*") {
    [Environment]::SetEnvironmentVariable("Path", "$currentPath;$installDir", "User")
    Write-Host "✓ 已添加到 PATH (需要重启终端生效)" -ForegroundColor Green
} else {
    Write-Host "✓ 已在 PATH 中" -ForegroundColor Green
}

# 验证
Write-Host "`n验证安装..." -ForegroundColor Yellow
$flyPath = "$installDir\flyctl.exe"
if (Test-Path $flyPath) {
    $ver = & $flyPath version
    Write-Host "✓ 安装成功: $ver" -ForegroundColor Green
    Write-Host "`n使用方法:" -ForegroundColor Cyan
    Write-Host "  1. 重启 PowerShell 终端" -ForegroundColor White
    Write-Host "  2. 运行: flyctl auth login" -ForegroundColor White
    Write-Host "  3. 进入 backend 目录运行: .\deploy.ps1" -ForegroundColor White
} else {
    Write-Host "✗ 安装失败，未找到 flyctl.exe" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== 安装完成 ===" -ForegroundColor Cyan
