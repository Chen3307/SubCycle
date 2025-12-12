# SubCycle 資料庫文件

SubCycle 訂閱管理系統的 MySQL 資料庫結構與設定。

## 快速開始

### Windows

```bash
cd database
init_db.bat
```

### Mac / Linux

```bash
cd database
chmod +x init_db.sh
./init_db.sh
```

執行後輸入你的 MySQL root 密碼即可完成資料庫初始化。

---

## 資料庫架構

### 資料表結構

#### 1️⃣ users 👤 - 使用者表

| 欄位 | 類型 | 說明 |
|------|------|------|
| 🔑 id | BIGINT | 主鍵 |
| username | VARCHAR(50) | 帳號 |
| email | VARCHAR(100) | 信箱 |
| password | VARCHAR(255) | 密碼 |
| default_currency | VARCHAR(10) | 預設幣別 (TWD, USD, EUR...) |
| timezone | VARCHAR(50) | 時區 (Asia/Taipei...) |
| notification_enabled | TINYINT(1) | 啟用通知 (0/1) |
| notification_days_before | INT | 提前幾天通知 |
| created_at | TIMESTAMP | 建立時間 |
| updated_at | TIMESTAMP | 更新時間 |

#### 2️⃣ categories 📁 - 類別表

| 欄位 | 類型 | 說明 |
|------|------|------|
| 🔑 id | BIGINT | 主鍵 |
| 🔗 user_id | BIGINT | 使用者ID (→ users) |
| name | VARCHAR(50) | 類別名稱 |
| icon | VARCHAR(50) | 圖示名稱 |
| color | VARCHAR(20) | 顏色代碼 (#HEX) |
| sort_order | INT | 排序順序 |
| created_at | TIMESTAMP | 建立時間 |
| updated_at | TIMESTAMP | 更新時間 |

#### 3️⃣ subscriptions 💳 - 訂閱表 (核心表)

| 欄位 | 類型 | 說明 |
|------|------|------|
| 🔑 id | BIGINT | 主鍵 |
| 🔗 user_id | BIGINT | 使用者ID (→ users) |
| 🔗 category_id | BIGINT | 類別ID (→ categories) |
| name | VARCHAR(100) | 訂閱名稱 |
| price | DECIMAL(10,2) | 金額 |
| billing_cycle | ENUM | 週期 (daily/weekly/monthly/quarterly/yearly) |
| next_payment_date | DATE | 下次扣款日 |
| status | ENUM | 狀態 (active/paused/cancelled) |
| description | TEXT | 描述 |
| logo_url | VARCHAR(500) | Logo URL |
| website_url | VARCHAR(500) | 網站 URL |
| created_at | TIMESTAMP | 建立時間 |
| updated_at | TIMESTAMP | 更新時間 |

#### 4️⃣ payment_history 💰 - 付款歷史表

| 欄位 | 類型 | 說明 |
|------|------|------|
| 🔑 id | BIGINT | 主鍵 |
| 🔗 user_id | BIGINT | 使用者ID (→ users) |
| 🔗 subscription_id | BIGINT | 訂閱ID (→ subscriptions) |
| amount | DECIMAL(10,2) | 金額 |
| payment_date | DATE | 付款日期 |
| status | ENUM | 付款狀態 (completed/pending/failed/refunded) |
| notes | TEXT | 備註 |
| created_at | TIMESTAMP | 建立時間 |

#### 5️⃣ notifications 🔔 - 通知表

| 欄位 | 類型 | 說明 |
|------|------|------|
| 🔑 id | BIGINT | 主鍵 |
| 🔗 user_id | BIGINT | 使用者ID (→ users) |
| 🔗 subscription_id | BIGINT | 訂閱ID (→ subscriptions) |
| type | ENUM | 通知類型 (payment_due/payment_completed/subscription_expiring/general) |
| title | VARCHAR(100) | 標題 |
| message | VARCHAR(500) | 內容 |
| is_read | TINYINT(1) | 已讀 (0/1) |
| created_at | TIMESTAMP | 建立時間 |

---

## 視圖 (Views)

### 1. user_subscription_stats - 使用者訂閱統計

查詢每個使用者的訂閱總數、啟用訂閱數和月均支出。

```sql
SELECT * FROM user_subscription_stats;
```

### 2. upcoming_subscriptions - 即將到期的訂閱

查詢未來 30 天內即將扣款的訂閱。

```sql
SELECT * FROM upcoming_subscriptions;
```

### 3. category_stats - 類別統計

查詢每個類別的訂閱數量和月均支出。

```sql
SELECT * FROM category_stats;
```

### 4. payment_stats - 付款統計

查詢每個使用者的付款統計資料。

```sql
SELECT * FROM payment_stats;
```

---

## 測試資料

初始化後會自動建立 3 個測試使用者：

### 測試帳號 1 (繁體中文)
- **Email**: `demo@subcycle.com`
- **密碼**: `password123`
- **幣別**: TWD (新台幣)
- **訂閱**: 9 個 (Netflix, Disney+, Spotify, YouTube Premium, Google One, Dropbox, ChatGPT Plus, Notion, Xbox Game Pass)

### 測試帳號 2 (英文)
- **Email**: `john@example.com`
- **密碼**: `password123`
- **幣別**: USD (美金)
- **訂閱**: 4 個 (Netflix, Hulu, GitHub Pro, Adobe Creative Cloud)

### 測試帳號 3 (英文)
- **Email**: `mary@example.com`
- **密碼**: `password123`
- **幣別**: EUR (歐元)
- **訂閱**: 2 個 (Amazon Prime, Microsoft 365)

---

## 手動操作

### 手動建立資料庫

```bash
mysql -u root -p < schema_minimal.sql
```

### 手動插入測試資料

```bash
mysql -u root -p < seed_minimal.sql
```

### 刪除資料庫

```sql
DROP DATABASE IF EXISTS subcycle;
```

### 重置資料庫

```bash
# 先刪除再重建
mysql -u root -p < schema_minimal.sql
mysql -u root -p < seed_minimal.sql
```

---

## 資料庫備份與還原

### 備份資料庫

```bash
mysqldump -u root -p subcycle > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 還原資料庫

```bash
mysql -u root -p subcycle < backup_20251210_123456.sql
```

---

## 連線設定

記得修改 `backend/src/main/resources/application.properties` 中的資料庫密碼：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/subcycle
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

---

## 資料庫設計原則

1. **正規化**: 遵循第三正規化 (3NF)，避免資料重複
2. **外鍵約束**: 使用 `ON DELETE CASCADE` 自動清理相關資料
3. **索引優化**: 為常用查詢欄位建立索引
4. **UTF-8編碼**: 支援多國語言和 Emoji
5. **時間戳記**: 記錄資料建立和更新時間

---

## 常見問題

### Q: 初始化失敗，顯示 "Access denied"？

確認 MySQL root 密碼是否正確。

### Q: 如何查看已建立的資料表？

```sql
USE subcycle;
SHOW TABLES;
```

### Q: 如何查看資料表結構？

```sql
DESCRIBE users;
-- 或
SHOW CREATE TABLE users;
```

### Q: 如何清空某個資料表的資料但保留結構？

```sql
TRUNCATE TABLE notifications;
```

---

**最後更新：2025-12-10**
