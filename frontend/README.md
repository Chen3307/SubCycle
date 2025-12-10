# SubCycle Frontend - Vue.js Application

SubCycle 訂閱管理系統的前端應用程式

## 🚀 快速開始

### 前置需求

- **Node.js 18+**
- **npm** 或 **yarn**

### 1. 安裝依賴

```bash
cd frontend
npm install
```

### 2. 啟動開發伺服器

```bash
npm run dev
```

應用程式將在 http://localhost:5173 啟動

### 3. 建置正式版本

```bash
npm run build
```

建置完成的檔案會在 `dist` 資料夾中。

---

## 📁 專案結構

```
frontend/
├── public/               # 靜態資源
│   └── logo.svg
├── src/
│   ├── assets/           # 圖片、樣式等資源
│   ├── components/       # Vue 元件
│   │   └── icons/        # 圖示元件
│   ├── router/           # Vue Router 路由設定
│   ├── stores/           # Pinia 狀態管理
│   │   └── auth.js       # 認證狀態
│   ├── views/            # 頁面元件
│   │   ├── HomeView.vue      # 首頁
│   │   └── LoginView.vue     # 登入頁
│   ├── App.vue           # 根元件
│   └── main.js           # 入口檔案
├── index.html            # HTML 模板
├── vite.config.js        # Vite 設定
├── package.json          # 專案依賴
└── README.md             # 說明文件
```

---

## 🛠️ 技術棧

- **Vue 3** - 前端框架
- **Vue Router** - 路由管理
- **Pinia** - 狀態管理
- **Element Plus** - UI 元件庫
- **Vite** - 建置工具

---

## 🔗 API 連接

前端會連接到後端 API (預設: http://localhost:8080)

確保後端服務已啟動：
```bash
cd ../backend
mvn spring-boot:run
```

---

## 📝 可用的腳本

- **`npm run dev`** - 啟動開發伺服器
- **`npm run build`** - 建置正式版本
- **`npm run preview`** - 預覽建置結果

---

## 🎨 修改 Logo 大小

### HomeView 導航列 Logo
位置：`src/views/HomeView.vue:111-114`
```css
.nav-logo {
  width: 80px;   /* 調整寬度 */
  height: 80px;  /* 調整高度 */
}
```

### LoginView 卡片 Logo
位置：`src/views/LoginView.vue:195-201`
```css
.logo-img {
  width: 120px;   /* 調整寬度 */
  height: 120px;  /* 調整高度 */
}
```

---

## 📱 頁面說明

### 首頁 (HomeView)
- Landing Page
- 功能介紹
- 導航列（登入/註冊）

### 登入頁 (LoginView)
- 使用者登入
- 測試登入功能
- 整合 auth store

---

**最後更新：2025-11-27**
