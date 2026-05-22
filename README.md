# Music Downloader Mobile

基于 Python 后端 + Kotlin Android 的音乐下载器移动端方案。

## 项目结构

```
musicDownload-mobile/
├── backend/              # Python FastAPI 后端
│   ├── main.py          # API 服务主文件
│   ├── requirements.txt # Python 依赖
│   └── start.bat        # Windows 启动脚本
│
└── android-app/         # Kotlin Android 应用
    └── app/src/...      # 完整的 Android 项目源码
```

## 快速开始

### 1. 启动后端服务

```bash
cd backend
start.bat
```

服务启动后访问：
- API 地址: http://localhost:8000
- API 文档: http://localhost:8000/docs

### 2. 打开 Android 项目

使用 Android Studio 打开 `android-app` 文件夹，同步 Gradle 后即可运行。

**注意**：默认配置使用 `10.0.2.2:8000` 访问本机后端（Android 模拟器专用）。

如果使用真机，需要在设置中修改服务器地址为电脑的局域网 IP。

## 功能特性

### 后端 API
- 搜索音乐（支持多音源）
- 创建下载任务
- 查询下载进度
- 管理已下载文件

### Android App
- 搜索歌曲（支持音源筛选）
- 下载音乐（显示进度）
- 查看已下载列表
- 服务器地址设置

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Python + FastAPI + musicdl |
| Android | Kotlin + Jetpack Compose + Hilt + Retrofit |

## 注意事项

1. 确保后端服务先于 App 启动
2. 手机和电脑需在同一个局域网（真机测试时）
3. 首次使用请在设置中配置正确的服务器地址
