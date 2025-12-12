# JWT 身分驗證與授權 - 實作指南

## 📋 完成項目

✅ 1. 添加 JWT 和 Spring Security 依賴
✅ 2. 更新 User 實體類實作 UserDetails 接口
✅ 3. 創建 JWT 工具類（生成和驗證 token）
✅ 4. 創建 JWT 認證過濾器
✅ 5. 創建 Spring Security 配置類
✅ 6. 創建認證 API（登入、註冊）
✅ 7. 更新資料庫 schema
✅ 8. 整合前端 API 連接

## 🏗️ 架構說明

### 後端組件

1. **JWT 工具類** (`JwtUtil.java`)
   - 生成 JWT token
   - 驗證 JWT token
   - 從 token 提取用戶資訊

2. **認證過濾器** (`JwtAuthenticationFilter.java`)
   - 攔截每個請求
   - 從 Authorization header 提取 JWT
   - 驗證 token 並設置 SecurityContext

3. **Security 配置** (`SecurityConfig.java`)
   - 配置密碼編碼器（BCrypt）
   - 配置 CORS
   - 設置公開和受保護的端點
   - 整合 JWT 過濾器

4. **認證 API** (`AuthController.java` & `AuthService.java`)
   - POST `/api/auth/register` - 用戶註冊
   - POST `/api/auth/login` - 用戶登入
   - GET `/api/auth/me` - 獲取當前用戶（需認證）

### 前端組件

1. **API 配置** (`frontend/src/api/index.js`)
   - Axios 實例配置
   - 自動添加 JWT token 到請求 header
   - 處理 401 錯誤（token 過期）

2. **Auth Store** (`frontend/src/stores/auth.js`)
   - 用戶登入
   - 用戶註冊
   - Token 管理

## 🚀 測試步驟

### 1. 初始化資料庫

```bash
# Windows
cd database
mysql -u root -p < schema_minimal.sql
```

輸入 MySQL root 密碼：`36781258`

### 2. 啟動後端服務器

```bash
cd backend
mvn spring-boot:run
```

後端將在 `http://localhost:8080` 運行

### 3. 啟動前端服務器

```bash
cd frontend
npm run dev
```

前端將在 `http://localhost:5173` 運行

### 4. 測試 API

#### 註冊新用戶

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "測試用戶"
  }'
```

預期回應：
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "test@example.com",
  "name": "測試用戶"
}
```

#### 用戶登入

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### 訪問受保護的端點

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## 🔐 安全特性

1. **密碼加密**：使用 BCrypt 加密儲存密碼
2. **JWT Token**：有效期 24 小時
3. **無狀態認證**：使用 JWT，不需要 session
4. **CORS 保護**：只允許特定來源訪問 API
5. **角色授權**：支援基於角色的訪問控制（預設 USER 角色）

## 📁 新增檔案

### 後端
- `backend/src/main/java/com/subcycle/security/JwtUtil.java`
- `backend/src/main/java/com/subcycle/security/CustomUserDetailsService.java`
- `backend/src/main/java/com/subcycle/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/subcycle/config/SecurityConfig.java`
- `backend/src/main/java/com/subcycle/controller/AuthController.java`
- `backend/src/main/java/com/subcycle/service/AuthService.java`
- `backend/src/main/java/com/subcycle/dto/LoginRequest.java`
- `backend/src/main/java/com/subcycle/dto/RegisterRequest.java`
- `backend/src/main/java/com/subcycle/dto/AuthResponse.java`

### 前端
- `frontend/src/api/index.js`

### 修改檔案
- `backend/pom.xml` - 添加 JWT 和 Spring Security 依賴
- `backend/src/main/java/com/subcycle/entity/User.java` - 實作 UserDetails
- `backend/src/main/resources/application.properties` - 添加 JWT 配置
- `frontend/src/stores/auth.js` - 整合真實 API
- `database/schema_minimal.sql` - 更新 users 表結構

## 🔧 配置說明

### JWT 配置 (application.properties)

```properties
jwt.secret=mySecretKeyForSubCycleApplicationThatIsAtLeast256BitsLong12345
jwt.expiration=86400000  # 24 小時（毫秒）
```

**注意**：生產環境請使用環境變數設置更安全的 secret key！

### CORS 配置

允許的來源：
- `http://localhost:5173` (Vite)
- `http://localhost:3000` (備用)

## ⚠️ 注意事項

1. **資料庫結構變更**：
   - users 表新增 `role` 欄位
   - users 表新增 `last_login_at` 欄位
   - 移除 `username` 欄位，改用 `email` 作為登入憑證

2. **密碼要求**：
   - 目前沒有密碼強度限制
   - 建議在前端添加密碼驗證規則

3. **Token 管理**：
   - Token 儲存在 localStorage
   - Token 過期時會自動跳轉到登入頁
   - 登出時會清除 token

## 🎯 下一步建議

1. 添加密碼重設功能
2. 添加 email 驗證
3. 實作 refresh token 機制
4. 添加多因素認證（MFA）
5. 實作更細緻的角色權限控制
6. 添加 API 速率限制
7. 實作審計日誌

## 📝 API 端點總覽

| 方法 | 端點 | 說明 | 需要認證 |
|------|------|------|----------|
| POST | /api/auth/register | 用戶註冊 | ❌ |
| POST | /api/auth/login | 用戶登入 | ❌ |
| GET | /api/auth/me | 獲取當前用戶 | ✅ |

## 🐛 常見問題

### 問題：登入後收到 401 錯誤
**解決**：檢查 JWT token 是否正確添加到 Authorization header

### 問題：無法註冊用戶
**解決**：
1. 確認資料庫已正確初始化
2. 檢查 email 是否已被註冊
3. 查看後端日誌獲取詳細錯誤訊息

### 問題：CORS 錯誤
**解決**：確認前端運行在允許的來源（localhost:5173 或 localhost:3000）

---

**完成時間**：2025-12-11
**技術棧**：Spring Boot 3.2.0, Spring Security, JWT (jjwt 0.12.3), Vue 3, Pinia, Axios
