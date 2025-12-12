# SubCycle - 訂閱管理系統

一個完整的訂閱與帳單追蹤系統，使用 Vue 3 (前端) + Spring Boot (後端) + MySQL (資料庫) 建立。

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
├── database/         # MySQL 資料庫腳本
│   ├── schema_minimal.sql      # 資料庫結構（極精簡版）
│   ├── seed_minimal.sql        # 初始測試資料
│   ├── init_db.bat            # Windows 初始化腳本
│   ├── init_db.sh             # Mac/Linux 初始化腳本
│   └── README.md              # 資料庫文件
│
└── README.md         # 專案總覽（本文件）
```

---

## 快速開始

### 前置需求

- **Node.js 18+** （前端）
- **Java 17+** （後端）
- **Maven 3.6+** （後端）
- **MySQL 8.0+** （資料庫）

### 1. 建立資料庫

```bash
cd database

# Windows
init_db.bat

# Mac/Linux
chmod +x init_db.sh
./init_db.sh
```

### 2. 啟動後端 API

```bash
cd backend

# 1. 修改資料庫密碼
# 編輯 src/main/resources/application.properties
# 將 spring.datasource.password 改成你的 MySQL 密碼

# 2. 啟動 Spring Boot
mvn spring-boot:run
```

後端 API 將在 http://localhost:8080 啟動

### 3. 啟動前端應用

```bash
cd frontend

# 安裝依賴（首次執行）
npm install

# 啟動開發伺服器
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

- **Spring Boot 3.2** - Java 框架
- **Spring Data JPA** - ORM 資料持久化
- **MySQL** - 關聯式資料庫
- **Maven** - 專案管理工具

### 資料庫

- **MySQL 8.0+**
- **4 張核心資料表**：users, categories, subscriptions, notifications
- **3 個統計視圖**：使用者統計、即將到期訂閱、類別統計

---

## 功能特色

### 1. 會員系統

- 使用者註冊 / 登入
- JWT 認證（規劃中）
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

---

## 測試連線

### 測試後端 API

1. **基本測試**

   ```
   http://localhost:8080/api/test
   ```

2. **資料庫連線測試**

   ```
   http://localhost:8080/api/test/db
   ```

3. **查詢使用者**
   ```
   http://localhost:8080/api/test/users
   ```

### 測試資料

- **Email**: `demo@subcycle.com`
- **密碼**: `password123`
- **預設訂閱**: Netflix, Spotify, Google One

---

## 詳細文件

- [前端文件](./frontend/README.md) - Vue.js 應用程式說明
- [後端文件](./backend/README.md) - Spring Boot API 說明
- [資料庫文件](./database/README.md) - MySQL 資料庫架構

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

### 資料庫管理

```bash
# 重置資料庫
mysql -u root -p subcycle < database/schema_minimal.sql
mysql -u root -p subcycle < database/seed_minimal.sql

# 備份資料庫
mysqldump -u root -p subcycle > backup.sql
```

---

## 開發中功能

- [ ] JWT 認證整合
- [ ] 密碼加密 (bcrypt)
- [ ] 郵件提醒功能
- [ ] 多幣別支援
- [ ] 匯出報表 (PDF/Excel)
- [ ] 手機版響應式優化
- [ ] Docker 容器化部署

---

## 常見問題

### Q: 前端無法連接後端？

確認後端已啟動在 port 8080，並檢查 CORS 設定。

### Q: 資料庫連線失敗？

檢查 `backend/src/main/resources/application.properties` 中的資料庫密碼是否正確。

### Q: 如何修改 Logo 大小？

參考 [前端文件](./frontend/README.md#-修改-logo-大小)。

---

## License

MIT

---

**最後更新：2025-11-27**
