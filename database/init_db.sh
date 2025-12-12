#!/bin/bash
# ============================================
# SubCycle 資料庫初始化腳本 (Mac/Linux)
# ============================================

echo "============================================"
echo "SubCycle 資料庫初始化"
echo "============================================"
echo ""

# 檢查 MySQL 是否安裝
if ! command -v mysql &> /dev/null; then
    echo "[錯誤] 找不到 MySQL 指令"
    echo "請確認 MySQL 已安裝並加入到 PATH 環境變數"
    exit 1
fi

echo "[1/3] 準備建立資料庫結構..."
echo ""

# 提示輸入 MySQL root 密碼
read -sp "請輸入 MySQL root 密碼: " MYSQL_PASSWORD
echo ""
echo ""

echo "[2/3] 建立資料庫結構..."
mysql -u root -p"$MYSQL_PASSWORD" < schema_minimal.sql
if [ $? -ne 0 ]; then
    echo ""
    echo "[錯誤] 資料庫結構建立失敗"
    echo "請檢查 MySQL 密碼是否正確"
    exit 1
fi
echo "[完成] 資料庫結構建立完成"
echo ""

echo "[3/3] 插入測試資料..."
mysql -u root -p"$MYSQL_PASSWORD" < seed_minimal.sql
if [ $? -ne 0 ]; then
    echo ""
    echo "[錯誤] 測試資料插入失敗"
    exit 1
fi
echo "[完成] 測試資料插入完成"
echo ""

echo "============================================"
echo "資料庫初始化完成！"
echo "============================================"
echo ""
echo "資料庫名稱: subcycle"
echo "測試帳號: demo@subcycle.com"
echo "測試密碼: password123"
echo ""
echo "請記得修改 backend/src/main/resources/application.properties"
echo "將 spring.datasource.password 改為你的 MySQL 密碼"
echo ""
