#!/bin/bash
# ============================================
# SubCycle 資料庫初始化腳本 (Unix/Linux/Mac)
# ============================================

echo ""
echo "========================================"
echo "SubCycle 資料庫初始化"
echo "========================================"
echo ""

# 設定預設值
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_HOST="${MYSQL_HOST:-localhost}"
DB_NAME="subcycle"

# 詢問 MySQL 連線資訊
read -p "請輸入 MySQL 使用者名稱 [預設: $MYSQL_USER]: " input_user
MYSQL_USER="${input_user:-$MYSQL_USER}"

read -p "請輸入 MySQL 主機 [預設: $MYSQL_HOST]: " input_host
MYSQL_HOST="${input_host:-$MYSQL_HOST}"

read -sp "請輸入 MySQL 密碼: " MYSQL_PASSWORD
echo ""

# 執行資料庫初始化
echo ""
echo "[1/2] 建立資料庫架構..."
mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" < schema_minimal.sql

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ 資料庫架構建立失敗！"
    exit 1
fi

echo "✓ 資料庫架構建立成功"

echo ""
echo "[2/2] 載入初始資料..."
mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$DB_NAME" < seed_minimal.sql

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ 初始資料載入失敗！"
    exit 1
fi

echo "✓ 初始資料載入成功"

echo ""
echo "========================================"
echo "✓ 資料庫初始化完成！"
echo "========================================"
echo ""
echo "📧 測試帳號: demo@subcycle.com"
echo "🔑 測試密碼: password123"
echo ""
echo "⚠  注意：記得修改密碼加密方式"
echo ""
