# SubCycle 密碼重置功能完整指南

## 目錄
1. [資料表設計](#資料表設計)
2. [前台功能](#前台功能)
3. [後台功能](#後台功能)
4. [API 端點](#api-端點)
5. [環境變數設定](#環境變數設定)
6. [安全性考量](#安全性考量)
7. [部署檢查清單](#部署檢查清單)

---

## 資料表設計

### 1. password_resets（密碼重置記錄表）

```sql
CREATE TABLE password_resets (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  email VARCHAR(255) NOT NULL,
  token VARCHAR(255) NOT NULL UNIQUE COMMENT '重置 token (已雜湊)',
  reset_by_admin_id INT NULL COMMENT '執行重置的管理員 ID',
  type ENUM('user_request', 'admin_reset') DEFAULT 'user_request',
  expires_at DATETIME NOT NULL COMMENT 'Token 過期時間',
  used_at DATETIME NULL COMMENT 'Token 使用時間',
  ip_address VARCHAR(45) NULL COMMENT 'IP 位址',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_token (token),
  INDEX idx_email (email),
  INDEX idx_expires_at (expires_at),

  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (reset_by_admin_id) REFERENCES admin_users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密碼重置記錄表';
```

### 2. email_logs（郵件發送記錄表）

```sql
CREATE TABLE email_logs (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NULL,
  email_to VARCHAR(255) NOT NULL,
  email_type ENUM('password_reset', 'welcome', 'notification') NOT NULL,
  subject VARCHAR(255) NOT NULL,
  status ENUM('pending', 'sent', 'failed') DEFAULT 'pending',
  sent_at DATETIME NULL,
  error_message TEXT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_user_id (user_id),
  INDEX idx_status (status),

  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='郵件發送記錄表';
```

### 3. 修改現有表格

```sql
-- users 表新增欄位
ALTER TABLE users
  ADD COLUMN password_changed_at DATETIME NULL COMMENT '最後密碼變更時間',
  ADD COLUMN force_password_change BOOLEAN DEFAULT FALSE COMMENT '是否強制變更密碼';

-- admin_users 表新增欄位
ALTER TABLE admin_users
  ADD COLUMN password_changed_at DATETIME NULL COMMENT '最後密碼變更時間',
  ADD COLUMN force_password_change BOOLEAN DEFAULT FALSE COMMENT '是否強制變更密碼';
```

---

## 前台功能

### 使用者忘記密碼流程

#### 1. 使用者申請重置
- 訪問 `/forgot-password` 頁面
- 輸入註冊的電子郵件
- 系統發送重置連結到信箱

#### 2. 驗證並重置密碼
- 點擊郵件中的重置連結
- 系統驗證 token 有效性
- 設定新密碼（需符合強度要求）
- 完成後導向登入頁面

#### 3. 已登入使用者變更密碼
- 在帳戶設定頁面選擇「變更密碼」
- 輸入目前密碼和新密碼
- 驗證後更新密碼

### 密碼強度要求
- 至少 8 個字元
- 至少包含一個大寫字母 (A-Z)
- 至少包含一個小寫字母 (a-z)
- 至少包含一個數字 (0-9)
- 建議包含特殊符號以提高強度

---

## 後台功能

### 管理員密碼重置管理

#### 1. 單一使用者重置

**方式一：產生臨時密碼**
- 系統自動產生 8 位隨機密碼
- 使用者登入後強制變更密碼
- 可選擇是否自動寄送郵件通知

**方式二：發送重置連結**
- 系統產生一次性重置連結
- 連結 1 小時後過期
- 使用者自行設定新密碼

#### 2. 批次重置密碼
- 選擇多位使用者
- 統一重置方式
- 自動發送通知郵件

#### 3. 查看重置記錄
- 查看使用者的所有重置記錄
- 包含重置時間、操作人、使用狀態
- 用於審計和問題排查

---

## API 端點

### 前台 API

#### 1. 申請忘記密碼
```
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}

Response:
{
  "success": true,
  "message": "如果該電子郵件存在於我們的系統中，您將收到密碼重置連結"
}
```

#### 2. 驗證重置 Token
```
GET /api/auth/verify-reset-token/:token

Response:
{
  "success": true,
  "email": "user@example.com",
  "name": "使用者名稱"
}
```

#### 3. 執行密碼重置
```
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "reset_token_here",
  "newPassword": "NewPassword123"
}

Response:
{
  "success": true,
  "message": "密碼已成功重置，請使用新密碼登入"
}
```

#### 4. 變更密碼（已登入）
```
POST /api/user/change-password
Authorization: Bearer {token}
Content-Type: application/json

{
  "currentPassword": "OldPassword123",
  "newPassword": "NewPassword123"
}

Response:
{
  "success": true,
  "message": "密碼已成功變更"
}
```

### 後台 API

#### 1. 管理員重置使用者密碼
```
POST /admin/api/users/:userId/reset-password
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "resetType": "temp_password",  // 或 "reset_link"
  "notifyUser": true
}

Response:
{
  "success": true,
  "message": "密碼已重置",
  "tempPassword": "已寄送至使用者信箱"  // 如果 notifyUser = false 則顯示密碼
}
```

#### 2. 批次重置密碼
```
POST /admin/api/users/batch-reset-passwords
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "userIds": [1, 2, 3],
  "resetType": "reset_link"
}

Response:
{
  "success": true,
  "message": "已處理 3 位使用者",
  "results": [
    { "userId": 1, "email": "user1@example.com", "success": true },
    { "userId": 2, "email": "user2@example.com", "success": true },
    { "userId": 3, "email": "user3@example.com", "success": false, "error": "..." }
  ]
}
```

#### 3. 查詢密碼重置記錄
```
GET /admin/api/password-reset-logs?userId=1&page=1&limit=20
Authorization: Bearer {admin_token}

Response:
{
  "logs": [
    {
      "id": 1,
      "user_email": "user@example.com",
      "type": "admin_reset",
      "admin_username": "admin",
      "created_at": "2025-11-24 10:30:00",
      "used_at": "2025-11-24 10:35:00",
      "ip_address": "192.168.1.1"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 50,
    "totalPages": 3
  }
}
```

---

## 環境變數設定

在 `.env` 檔案中設定以下環境變數：

```env
# 前端網址
FRONTEND_URL=https://your-domain.com

# SMTP 郵件伺服器設定
SMTP_HOST=smtp.gmail.com
SMTP_PORT=465
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password
SMTP_FROM=SubCycle <noreply@subcycle.com>

# Token 設定
JWT_SECRET=your-jwt-secret-key
PASSWORD_RESET_TOKEN_EXPIRY=3600  # 1小時（秒）

# 密碼規則
PASSWORD_MIN_LENGTH=8
PASSWORD_REQUIRE_UPPERCASE=true
PASSWORD_REQUIRE_LOWERCASE=true
PASSWORD_REQUIRE_NUMBER=true
PASSWORD_REQUIRE_SPECIAL=false

# 重置限制
RESET_REQUEST_COOLDOWN=300  # 5分鐘（秒）
MAX_RESET_ATTEMPTS_PER_DAY=3
```

---

## 安全性考量

### 1. Token 安全
- ✅ Token 使用 SHA-256 雜湊後存入資料庫
- ✅ Token 長度為 64 字元（32 bytes）
- ✅ Token 設定過期時間（預設 1 小時）
- ✅ Token 使用後立即標記為已使用
- ✅ 定期清理過期的 token

### 2. 防止帳號探測
- ✅ 無論帳號是否存在，都返回相同訊息
- ✅ 回應時間一致，避免時序攻擊
- ✅ 記錄可疑的重複請求

### 3. 速率限制
- ✅ 同一 email 在 5 分鐘內只能申請一次
- ✅ 同一 IP 每小時最多 10 次請求
- ✅ 記錄異常請求模式

### 4. 密碼安全
- ✅ 使用 bcrypt 加密，salt rounds = 10
- ✅ 強制密碼強度檢查
- ✅ 禁止使用近期用過的密碼
- ✅ 管理員重置後強制使用者變更

### 5. 郵件安全
- ✅ 使用 TLS/SSL 加密連線
- ✅ 驗證郵件伺服器身份
- ✅ 記錄所有郵件發送狀態

### 6. 審計日誌
- ✅ 記錄所有密碼重置操作
- ✅ 記錄 IP 位址和時間戳
- ✅ 記錄管理員操作
- ✅ 定期審查異常操作

---

## 部署檢查清單

### 環境設定
- [ ] 設定 `.env` 檔案中的所有環境變數
- [ ] 確認 SMTP 伺服器連線正常
- [ ] 測試郵件發送功能
- [ ] 驗證前端網址設定正確

### 資料庫
- [ ] 建立 `password_resets` 表
- [ ] 建立 `email_logs` 表
- [ ] 修改 `users` 表新增欄位
- [ ] 修改 `admin_users` 表新增欄位
- [ ] 建立必要的索引

### 排程任務
- [ ] 設定定期清理過期 token（建議每小時執行）
```bash
# Cron job 範例
0 * * * * node /path/to/cleanup-expired-resets.js
```

### 功能測試
- [ ] 測試前台忘記密碼流程
- [ ] 測試 token 驗證機制
- [ ] 測試密碼重置功能
- [ ] 測試已登入使用者變更密碼
- [ ] 測試管理員重置使用者密碼
- [ ] 測試批次重置功能
- [ ] 測試郵件發送
- [ ] 測試強制變更密碼機制

### 安全性測試
- [ ] 測試 token 過期機制
- [ ] 測試 token 使用後無法重複使用
- [ ] 測試速率限制
- [ ] 測試密碼強度驗證
- [ ] 測試帳號探測防護

### 監控
- [ ] 設定郵件發送失敗告警
- [ ] 監控異常重置請求
- [ ] 定期審查審計日誌
- [ ] 監控 token 過期率

---

## 使用範例

### 前台組件整合

在登入頁面加入「忘記密碼」連結：

```vue
<template>
  <div class="login-page">
    <el-form>
      <!-- 登入表單 -->
    </el-form>

    <div class="forgot-password-link">
      <router-link to="/forgot-password">
        忘記密碼？
      </router-link>
    </div>
  </div>
</template>
```

### 後台管理選單

在後台側邊欄加入密碼管理選項：

```vue
<el-menu-item index="/admin/users/password-reset">
  <el-icon><Lock /></el-icon>
  <span>使用者密碼管理</span>
</el-menu-item>
```

### 排程任務腳本

`cleanup-expired-resets.js`:
```javascript
import { cleanupExpiredResets } from './user-reset-password.js'

async function runCleanup() {
  try {
    await cleanupExpiredResets()
    console.log('清理完成')
  } catch (error) {
    console.error('清理失敗:', error)
    process.exit(1)
  }
}

runCleanup()
```

---

## 常見問題

### Q1: 使用者收不到重置郵件？
**A:** 檢查以下項目：
1. SMTP 設定是否正確
2. 郵件是否被歸類為垃圾郵件
3. 查看 `email_logs` 表的發送狀態
4. 確認使用者 email 正確

### Q2: Token 驗證失敗？
**A:** 可能原因：
1. Token 已過期（超過 1 小時）
2. Token 已被使用
3. Token 格式錯誤
4. 資料庫記錄不存在

### Q3: 如何調整 Token 過期時間？
**A:** 修改環境變數 `PASSWORD_RESET_TOKEN_EXPIRY`（單位：秒）

### Q4: 如何自訂密碼強度規則？
**A:** 修改 `user-reset-password.js` 中的 `isValidPassword` 函數

### Q5: 如何追蹤異常重置行為？
**A:** 查詢 `password_resets` 表和 `system_logs` 表，關注以下指標：
- 短時間內大量重置請求
- 同一 IP 的重複請求
- Token 未使用率過高

---

## 支援與維護

如有問題或建議，請聯繫：
- 技術支援：tech@subcycle.com
- 安全問題：security@subcycle.com

最後更新：2025-11-24
