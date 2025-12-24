# SubCycle Backend - Spring Boot API

SubCycle 訂閱管理系統的後端 API 服務

## 🚀 快速開始

### 前置需求

- **Java 17** 或以上版本
- **Maven 3.6+**
- **MySQL 8.0+**
- 已建立 `subcycle\` 資料庫（使用 `/database` 資料夾中的 SQL 檔案）

### 1. 設定資料庫連線

編輯 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/subcycle?useSSL=false&serverTimezone=Asia/Taipei
spring.datasource.username=root
spring.datasource.password=your_password  # 改成你的 MySQL 密碼
```

### 2. 啟動應用程式

**方式一：使用 Maven 命令**

```bash
cd backend
mvn spring-boot:run
```

**方式二：使用 IDE**

- 在 IntelliJ IDEA 或 Eclipse 中開啟 `backend` 資料夾
- 執行 `SubCycleApplication.java` 主程式

---

## 📁 專案結構

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/subcycle/
│   │   │   ├── SubCycleApplication.java      # 主程式
│   │   │   ├── entity/                        # 實體類別（對應資料表）
│   │   │   │   └── User.java
│   │   │   ├── repository/                    # 資料訪問層
│   │   │   │   └── UserRepository.java
│   │   │   └── controller/                    # API 控制器
│   │   └── resources/
│   │       └── application.properties         # 設定檔
└── pom.xml                                    # Maven 設定檔
```

---

## 🔧 常見問題

### Q1: 啟動時出現 "Access denied for user" 錯誤

**原因：** MySQL 密碼不正確

**解決方法：**
修改 `application.properties` 中的密碼：

```properties
spring.datasource.password=你的正確密碼
```

### Q2: 啟動時出現 "Unknown database 'subcycle'" 錯誤

**原因：** 資料庫尚未建立

**解決方法：**
執行資料庫初始化腳本：

```bash
cd ../database
init_db.bat  # Windows
# 或
./init_db.sh  # Mac/Linux
```

### Q3: 出現 "Table 'subcycle.users' doesn't exist" 錯誤

**原因：** 資料表尚未建立

**解決方法：**
手動執行 SQL：

```bash
mysql -u root -p subcycle < ../database/schema_minimal.sql
mysql -u root -p subcycle < ../database/seed_minimal.sql
```

### Q4: Port 8080 已被占用

**解決方法：**
修改 `application.properties` 中的 port：

```properties
server.port=8081  # 改成其他 port
```

---

## 📝 下一步開發

測試連線成功後，你可以：

1. **建立更多 Entity**：

   - `Category.java`
   - `Subscription.java`
   - `Notification.java`

2. **建立對應的 Repository**

3. **建立 Service 層**處理業務邏輯

4. **建立正式的 Controller** 提供 CRUD API

5. **整合前端 Vue.js 專案**

---

## 🛠️ 技術棧

- **Spring Boot 3.2.0**
- **Spring Data JPA** - 資料持久化
- **MySQL Connector** - MySQL 驅動
- **Lombok** - 簡化程式碼
- **Maven** - 專案管理

---

**最後更新：2025-11-27**
