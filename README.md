# SubCycle - 訂閱管理系統

一個完整的訂閱與帳單追蹤系統，使用 Vue 3 (前端) + Spring Boot (後端) 建立。

## 專案結構

```
SubCycle/
├── frontend/          # Vue 3 前端應用程式
│   ├── src/          # 原始碼
│   ├── public/       # 靜態資源
│   └── README.md     # 前端說明文件
│
├── backend/          # Spring Boot 後端 API
│   ├── src/          # Java 原始碼
│   └── README.md     # 後端說明文件
│
└── README.md         # 專案總覽（本文件）
```

---

## 快速開始

### 方法一：手動安裝

#### 前置需求

- **Node.js 18+** （前端）
- **Java 21+** （後端）
- **Maven 3.6+** （後端）

### 1. 設定環境變數並啟動後端 API

```bash
cd backend

# 1. 複製環境變數範本檔案
# Windows:
copy .env.example .env
# Mac/Linux:
# cp .env.example .env

# 2. 編輯 .env 檔案，填入實際的配置資訊
# - JWT_SECRET: JWT 加密金鑰（至少 32 個字元）
# - MAIL_USERNAME: Gmail 帳號（選用，用於郵件通知功能）
# - MAIL_PASSWORD: Gmail 應用程式密碼（選用）

# 3. 啟動 Spring Boot
mvn spring-boot:run
```

後端 API 將在 http://localhost:8080 啟動

### 2. 設定前端環境並啟動應用

```bash
cd frontend

# 1. 複製環境變數範本檔案
# Windows:
copy .env.example .env
# Mac/Linux:
# cp .env.example .env

# 2. 安裝依賴（首次執行）
npm install

# 3. 啟動開發伺服器
npm run dev
```

前端應用將在 http://localhost:5173 啟動

---

## 技術

### 前端

- **Vue 3** - 前端框架 (Composition API)
- **Vue Router** - 路由管理

- **Pinia** - 狀態管理
- **Element Plus** - UI 元件庫
- **Chart.js** - 資料視覺化
- **FullCalendar** - 行事曆
- **Vite** - 建置工具

### 後端

- **Spring Boot 3.5.9** - Java 框架
- **Spring Security** - 安全認證
- **JWT** - Token 認證
- **Spring Mail** - 郵件發送
- **Apache POI** - Excel 生成
- **Bucket4j** - API 限流
- **Maven** - 專案管理工具

## 功能特色

### 1. 會員系統

- 使用者註冊 / 登入
- JWT + Refresh Token 認證
- 密碼加密 (BCrypt)
- 個人資料管理

### 2. 儀表板

- 總月均支出統計
- 下 30 天應付總額
- 訂閱項目總數
- 支出類別圓餅圖
- 即將到期訂閱列表

### 3. 訂閱管理

- CRUD 訂閱項目
- 支援多種付款週期（日/週/月/季/年）
- 自動計算月均支出
- 類別分類管理

### 4. 付款行事曆

- 月曆視圖顯示扣款日期
- 支援月視圖和週視圖
- 未來 12 個月的付款預測

### 5. 類別管理

- 自定義支出類別
- 類別顏色設定
- 統計資料查看

### 6. Email 通知系統

- 訂閱續訂提醒
- 付款成功通知
- 歡迎郵件
- 精美的 HTML 模板

### 7. 數據導出

- 導出訂閱列表 (PDF/Excel)
- 導出付款歷史 (PDF/Excel)
- 包含統計數據和總計
- 支持自定義日期範圍

### 8. API 安全

- Rate Limiting (請求限流)
- 每分鐘 100 請求限制
- 基於用戶/IP 的限流
- 防止 API 濫用

---

## 詳細文件

- [前端文件](./frontend/README.md) - Vue.js 應用程式說明
- [後端文件](./backend/README.md) - Spring Boot API 說明
- [JWT 認證指南](./JWT_AUTHENTICATION_GUIDE.md) - JWT 認證實作說明
- [第四優先功能說明](./FEATURES_TIER4.md) - Email、導出、限流

---

## 開發指南

### 前端開發

```bash
cd frontend
npm run dev      # 啟動開發伺服器
npm run build    # 建置正式版本
```

### 後端開發

```bash
cd backend
mvn spring-boot:run    # 啟動應用程式
mvn clean install      # 編譯並安裝
```

---

## 已完成功能 ✅

- [x] JWT 認證整合
- [x] 密碼加密 (BCrypt)
- [x] 郵件提醒功能
- [x] 匯出報表 (Excel)
- [x] API Rate Limiting

## 開發中功能

- [ ] 多幣別支援
- [ ] 手機版響應式優化
- [ ] 自動續訂提醒排程
- [ ] 數據分析儀表板增強

---

## 常見問題

### Q: 前端無法連接後端？

確認後端已啟動在 port 8080，並檢查 CORS 設定。



## License

MIT

---

## API 端點

### 認證相關

- `POST /api/auth/register` - 用戶註冊
- `POST /api/auth/login` - 用戶登入

### 訂閱管理

- `GET /api/subscriptions` - 獲取訂閱列表
- `POST /api/subscriptions` - 創建訂閱
- `PUT /api/subscriptions/{id}` - 更新訂閱
- `DELETE /api/subscriptions/{id}` - 刪除訂閱

### 數據導出

- `GET /api/export/subscriptions/excel` - 導出訂閱 Excel
  完整 API 文檔請訪問：http://localhost:8080/swagger-ui.html
