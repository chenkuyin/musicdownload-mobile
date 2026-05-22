@echo off
chcp 65001 >nul
echo 正在启动 Music Download API Server...
echo.

REM 检查 Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Python，请安装 Python 3.8+
    pause
    exit /b 1
)

REM 创建虚拟环境
if not exist "venv" (
    echo 创建虚拟环境...
    python -m venv venv
)

REM 激活虚拟环境
call venv\Scripts\activate.bat

REM 安装依赖
echo 安装依赖...
pip install -r requirements.txt -q

REM 启动服务器
echo.
echo ==========================================
echo   Music Download API Server
echo   地址: http://localhost:8000
echo   文档: http://localhost:8000/docs
echo ==========================================
echo.
python main.py

pause
