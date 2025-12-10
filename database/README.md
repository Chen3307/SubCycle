# SubCycle 資料庫文件

## 📊 資料庫版本選擇

本專案提供**三種資料庫版本**，請根據需求選擇：

### 1. 極精簡版 ⭐ 推薦新手使用
- 📄 檔案：`schema_minimal.sql` + `seed_minimal.sql`
- 🗄️ 資料表：**4 張** (users, categories, subscriptions, notifications)
- ⚡ 特點：最簡單、最容易理解的結構
- 🎯 適合：快速開發、學習專案、MVP 版本
- 🚀 快速安裝：執行 `init_db.bat` (Windows) 或 `init_db.sh` (Mac/Linux)

### 2. 中度簡化版
- 📄 檔案：`schema_simplified.sql` + `seed_simplified.sql`
- 🗄️ 資料表：**7 張**
- ⚡ 特點：保留實用功能（範本、統計），結構清晰
- 🎯 適合：中小型專案

### 3. 完整版（本文檔說明）
- 📄 檔案：`schema.sql` + `seed.sql`
- 🗄️ 資料表：**14 張**
- ⚡ 特點：功能完整，包含後台管理、日誌系統
- 🎯 適合：大型專案、需要完整管理功能

---

## 🚀 快速開始（極精簡版）

### Windows 用戶
```batch
cd database
init_db.bat
```

### Mac / Linux 用戶
```bash
cd database
chmod +x init_db.sh
./init_db.sh
```

### 測試帳號
- 📧 帳號：`demo@subcycle.com`
- 🔑 密碼：`password123`
- ⚠️ 請記得修改密碼加密方式

---

## 目錄（完整版文檔）
- [資料庫架構](#資料庫架構)
- [安裝步驟](#安裝步驟)
- [資料表說明](#資料表說明)
- [索引策略](#索引策略)
- [備份與還原](#備份與還原)
- [效能優化](#效能優化)

---

## 資料庫架構

### 技術規格
- **資料庫引擎**：MySQL 8.0+ / MariaDB 10.5+
- **字符集**：utf8mb4
- **排序規則**：utf8mb4_unicode_ci
- **儲存引擎**：InnoDB

### 資料表總覽

#### 前台使用者相關（7 張表）
1. `users` - 使用者表
2. `user_preferences` - 使用者偏好設定表
3. `categories` - 類別表
4. `subscriptions` - 訂閱項目表
5. `payment_records` - 付款記錄表
6. `notifications` - 通知表
7. `user_statistics` - 使用者統計表

#### 認證與安全（2 張表）
8. `password_resets` - 密碼重置記錄表
9. `email_logs` - 郵件發送記錄表

#### 後台管理相關（5 張表）
10. `admin_users` - 管理員表
11. `system_categories` - 系統預設類別表
12. `subscription_templates` - 訂閱範本表
13. `system_logs` - 系統日誌表
14. `system_settings` - 系統設定表

**總計：14 張資料表**

---

## 安裝步驟

### 1. 建立資料庫

```bash
# 方式一：使用 MySQL 命令列
mysql -u root -p < database/schema.sql

# 方式二：透過 MySQL Workbench 或其他工具匯入 schema.sql
```

### 2. 匯入初始資料

```bash
# 匯入系統預設資料
mysql -u root -p subcycle < database/seed.sql
```

### 3. 驗證安裝

```sql
-- 連接到資料庫
USE subcycle;

-- 檢查資料表數量（應該是 14 張表）
SELECT COUNT(*) AS table_count FROM information_schema.tables
WHERE table_schema = 'subcycle';

-- 檢查系統類別資料（應該有 9 筆）
SELECT COUNT(*) AS category_count FROM system_categories;

-- 檢查訂閱範本資料（應該有 40+ 筆）
SELECT COUNT(*) AS template_count FROM subscription_templates;
```

### 4. 建立資料庫使用者（正式環境）

```sql
-- 建立應用程式專用使用者
CREATE USER 'subcycle_app'@'localhost' IDENTIFIED BY 'your_secure_password';

-- 授予權限
GRANT SELECT, INSERT, UPDATE, DELETE ON subcycle.* TO 'subcycle_app'@'localhost';

-- 套用權限
FLUSH PRIVILEGES;
```

---

## 資料表說明

### 1. users（使用者表）

**用途**：儲存前台使用者的基本資料

**重要欄位**：
- `email` - 電子郵件（唯一，用於登入）
- `password` - 密碼（bcrypt 加密）
- `email_verified` - 郵件是否已驗證
- `is_active` - 帳號是否啟用
- `force_password_change` - 是否強制變更密碼

**關聯**：
- 一對一 → `user_preferences`
- 一對多 → `categories`, `subscriptions`, `notifications`, `user_statistics`

### 2. subscriptions（訂閱項目表）

**用途**：儲存使用者的訂閱項目

**重要欄位**：
- `cycle` - 付款週期（daily/weekly/monthly/quarterly/yearly）
- `next_payment_date` - 下次付款日期（用於提醒）
- `is_active` - 是否啟用
- `auto_renew` - 是否自動續訂

**索引策略**：
- 複合索引 `(user_id, is_active)` - 快速查詢使用者的啟用訂閱
- 單獨索引 `next_payment_date` - 用於排程提醒任務

### 3. payment_records（付款記錄表）

**用途**：記錄所有訂閱的付款歷史

**重要欄位**：
- `status` - 付款狀態（paid/pending/failed/refunded）
- `payment_date` - 付款日期
- `transaction_id` - 交易編號（可與第三方支付串接）

**查詢範例**：
```sql
-- 查詢某使用者的年度支出
SELECT
  YEAR(payment_date) AS year,
  SUM(amount) AS total_spent
FROM payment_records
WHERE user_id = 1 AND status = 'paid'
GROUP BY YEAR(payment_date);
```

### 4. notifications（通知表）

**用途**：管理系統通知

**通知類型**：
- `payment_reminder` - 付款提醒
- `subscription_expired` - 訂閱到期
- `payment_failed` - 付款失敗
- `system` - 系統通知

**查詢範例**：
```sql
-- 查詢未讀通知
SELECT * FROM notifications
WHERE user_id = 1 AND is_read = FALSE
ORDER BY created_at DESC;
```

### 5. password_resets（密碼重置記錄表）

**用途**：管理密碼重置請求

**安全機制**：
- Token 使用 SHA-256 雜湊儲存
- 設定過期時間（預設 1 小時）
- 記錄使用狀態和 IP

**清理策略**：
```sql
-- 定期清理過期的 token（建議設定為 Cron Job）
DELETE FROM password_resets
WHERE expires_at < NOW() OR used_at IS NOT NULL;
```

### 6. admin_users（管理員表）

**用途**：儲存後台管理員資料

**角色權限**：
- `super_admin` - 超級管理員（完整權限）
- `admin` - 一般管理員
- `moderator` - 內容管理員

### 7. subscription_templates（訂閱範本表）

**用途**：提供常見訂閱服務的預設範本

**特色功能**：
- `popularity_score` - 受歡迎程度（用於排序）
- `usage_count` - 使用次數（統計）
- `pricing_tiers` - 定價方案（JSON 格式）

**JSON 範例**：
```json
{
  "tiers": [
    { "name": "基本方案", "price": 149, "features": ["功能1", "功能2"] },
    { "name": "進階方案", "price": 390, "features": ["功能1", "功能2", "功能3"] }
  ]
}
```

### 8. system_logs（系統日誌表）

**用途**：記錄所有重要操作（審計追蹤）

**記錄內容**：
- 管理員操作
- 使用者重要動作
- 系統錯誤

**保留政策**：
```sql
-- 建議定期清理 90 天前的日誌
DELETE FROM system_logs
WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

---

## 索引策略

### 主要索引

#### 高頻查詢索引
```sql
-- users 表
idx_email (email)                    -- 登入查詢
idx_is_active (is_active)            -- 篩選啟用帳號

-- subscriptions 表
idx_user_active (user_id, is_active) -- 複合索引：查詢使用者的啟用訂閱
idx_next_payment_date                -- 提醒排程

-- notifications 表
idx_user_read (user_id, is_read)     -- 複合索引：未讀通知查詢

-- payment_records 表
idx_payment_date (payment_date)      -- 時間範圍查詢
idx_status (status)                  -- 狀態篩選
```

#### 外鍵索引
所有外鍵欄位都自動建立索引以提升關聯查詢效能

### 索引使用建議

1. **避免過度索引**：每個索引都會增加寫入成本
2. **定期分析**：使用 `EXPLAIN` 分析查詢計劃
3. **複合索引順序**：將選擇性高的欄位放前面

```sql
-- 檢查索引使用情況
SELECT
  table_name,
  index_name,
  cardinality
FROM information_schema.statistics
WHERE table_schema = 'subcycle'
ORDER BY table_name, index_name;
```

---

## 備份與還原

### 完整備份

```bash
# 備份整個資料庫（包含結構和資料）
mysqldump -u root -p --databases subcycle > backup_subcycle_$(date +%Y%m%d).sql

# 備份時加上壓縮
mysqldump -u root -p --databases subcycle | gzip > backup_subcycle_$(date +%Y%m%d).sql.gz
```

### 僅備份結構

```bash
# 僅備份資料表結構（不含資料）
mysqldump -u root -p --no-data subcycle > schema_only.sql
```

### 僅備份資料

```bash
# 僅備份資料（不含結構）
mysqldump -u root -p --no-create-info subcycle > data_only.sql
```

### 還原備份

```bash
# 還原完整備份
mysql -u root -p < backup_subcycle_20251124.sql

# 還原壓縮備份
gunzip < backup_subcycle_20251124.sql.gz | mysql -u root -p subcycle
```

### 自動備份腳本

```bash
#!/bin/bash
# backup_subcycle.sh

# 設定變數
DB_NAME="subcycle"
DB_USER="root"
BACKUP_DIR="/var/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=7

# 建立備份目錄
mkdir -p $BACKUP_DIR

# 執行備份
mysqldump -u $DB_USER -p$DB_PASS --databases $DB_NAME | gzip > $BACKUP_DIR/backup_${DB_NAME}_${DATE}.sql.gz

# 刪除超過 7 天的備份
find $BACKUP_DIR -name "backup_${DB_NAME}_*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo "備份完成：backup_${DB_NAME}_${DATE}.sql.gz"
```

**設定 Cron Job（每天凌晨 2 點備份）**：
```bash
crontab -e

# 加入以下行
0 2 * * * /path/to/backup_subcycle.sh >> /var/log/mysql_backup.log 2>&1
```

---

## 效能優化

### 1. 查詢優化

#### 使用 EXPLAIN 分析查詢

```sql
-- 檢查查詢是否使用索引
EXPLAIN SELECT * FROM subscriptions
WHERE user_id = 1 AND is_active = TRUE;
```

#### 避免 SELECT *

```sql
-- ❌ 不好的做法
SELECT * FROM subscriptions WHERE user_id = 1;

-- ✅ 好的做法：只查詢需要的欄位
SELECT id, name, amount, next_payment_date
FROM subscriptions
WHERE user_id = 1;
```

### 2. 分區策略（大型資料集）

當 `payment_records` 資料量超過百萬筆時，考慮按年份分區：

```sql
-- 建立分區表（範例）
ALTER TABLE payment_records
PARTITION BY RANGE (YEAR(payment_date)) (
  PARTITION p2023 VALUES LESS THAN (2024),
  PARTITION p2024 VALUES LESS THAN (2025),
  PARTITION p2025 VALUES LESS THAN (2026),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 3. 定期維護

```sql
-- 分析資料表（更新統計資訊）
ANALYZE TABLE subscriptions, payment_records, notifications;

-- 優化資料表（重整碎片）
OPTIMIZE TABLE subscriptions, payment_records;

-- 檢查資料表狀態
CHECK TABLE subscriptions;
```

### 4. 監控慢查詢

```sql
-- 開啟慢查詢日誌
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;  -- 超過 2 秒視為慢查詢

-- 查看慢查詢日誌位置
SHOW VARIABLES LIKE 'slow_query_log_file';
```

### 5. 連線池設定

在應用程式中使用連線池，建議設定：

```javascript
// Node.js 範例
const pool = mysql.createPool({
  connectionLimit: 10,      // 最大連線數
  host: 'localhost',
  user: 'subcycle_app',
  password: 'password',
  database: 'subcycle',
  waitForConnections: true,
  queueLimit: 0
});
```

---

## 資料庫大小估算

### 預估容量需求

假設：
- 10,000 個活躍使用者
- 每人平均 5 個訂閱
- 每個訂閱每月 1 筆付款記錄

**年度資料量估算**：

| 資料表 | 單筆大小 | 數量 | 總大小 |
|--------|---------|------|--------|
| users | 1 KB | 10,000 | 10 MB |
| subscriptions | 2 KB | 50,000 | 100 MB |
| payment_records | 1 KB | 600,000 | 600 MB |
| notifications | 0.5 KB | 500,000 | 250 MB |
| 其他表 | - | - | 50 MB |

**年度總容量：約 1 GB**

### 成長規劃

- 第 1 年：1 GB
- 第 3 年：3 GB
- 第 5 年：5 GB

建議準備至少 **20 GB** 的儲存空間以應對成長需求。

---

## 常見問題

### Q1: 如何重置資料庫？

```bash
# ⚠️ 警告：此操作會刪除所有資料
mysql -u root -p -e "DROP DATABASE IF EXISTS subcycle;"
mysql -u root -p < database/schema.sql
mysql -u root -p subcycle < database/seed.sql
```

### Q2: 如何修改預設管理員密碼？

```javascript
// 使用 bcrypt 產生密碼雜湊
const bcrypt = require('bcrypt');
const hash = await bcrypt.hash('YourNewPassword', 10);
console.log(hash);
```

```sql
-- 更新資料庫
UPDATE admin_users
SET password = '$2b$10$YourGeneratedHash'
WHERE username = 'admin';
```

### Q3: 如何遷移到其他伺服器？

```bash
# 1. 在舊伺服器匯出
mysqldump -u root -p subcycle > subcycle_export.sql

# 2. 傳輸到新伺服器
scp subcycle_export.sql user@newserver:/path/

# 3. 在新伺服器匯入
mysql -u root -p subcycle < subcycle_export.sql
```

### Q4: 資料庫字符集錯誤？

```sql
-- 檢查字符集
SHOW VARIABLES LIKE 'character%';

-- 修改資料庫字符集
ALTER DATABASE subcycle
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

---

## 版本更新紀錄

### v1.0 (2025-11-24)
- ✅ 初始資料庫架構
- ✅ 14 張資料表
- ✅ 完整索引設計
- ✅ 密碼重置功能
- ✅ 系統日誌機制

---

## 技術支援

如有資料庫相關問題，請聯繫：
- 技術支援：tech@subcycle.com
- 資料庫管理員：dba@subcycle.com

---

**最後更新：2025-11-24**
