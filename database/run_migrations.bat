@echo off
echo ========================================
echo SubCycle Database Migration Script
echo ========================================
echo.

REM 設定資料庫資訊（請根據您的設定修改）
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=subcycle
set DB_USER=root

echo 即將執行以下 migration:
echo - migration_refresh_tokens.sql
echo.

REM 提示輸入密碼
set /p DB_PASS=請輸入 MySQL root 密碼:

echo.
echo 執行 migration_refresh_tokens.sql...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% < migration_refresh_tokens.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Migration 執行成功！
    echo.
    echo 驗證表格...
    echo SHOW TABLES; | mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME%
) else (
    echo.
    echo [ERROR] Migration 執行失敗！
    echo 請檢查錯誤訊息。
)

echo.
pause
