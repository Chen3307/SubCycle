@echo off
REM ============================================
REM SubCycle 資料庫初始化腳本 (Windows)
REM ============================================
echo.
echo ========================================
echo SubCycle 資料庫初始化
echo ========================================
echo.

REM 設定 MySQL 連線資訊
set /p MYSQL_USER="請輸入 MySQL 使用者名稱 (預設: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_HOST="請輸入 MySQL 主機 (預設: localhost): "
if "%MYSQL_HOST%"=="" set MYSQL_HOST=localhost

echo.
echo 請輸入 MySQL 密碼:

REM 執行資料庫初始化
echo.
echo [1/2] 建立資料庫架構...
mysql -h %MYSQL_HOST% -u %MYSQL_USER% -p < schema_minimal.sql

if %errorlevel% neq 0 (
    echo.
    echo ❌ 資料庫架構建立失敗！
    pause
    exit /b 1
)

echo ✓ 資料庫架構建立成功
echo.
echo [2/2] 載入初始資料...
mysql -h %MYSQL_HOST% -u %MYSQL_USER% -p subcycle < seed_minimal.sql

if %errorlevel% neq 0 (
    echo.
    echo ❌ 初始資料載入失敗！
    pause
    exit /b 1
)

echo ✓ 初始資料載入成功
echo.
echo ========================================
echo ✓ 資料庫初始化完成！
echo ========================================
echo.
echo 📧 測試帳號: demo@subcycle.com
echo 🔑 測試密碼: password123
echo.
echo ⚠  注意：記得修改密碼加密方式
echo.
pause
