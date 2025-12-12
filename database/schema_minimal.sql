-- ============================================
-- SubCycle 資料庫結構
-- ============================================

DROP DATABASE IF EXISTS subcycle;
CREATE DATABASE subcycle CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE subcycle;

-- ============================================
-- 1. 使用者表 (users) 👤
-- ============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '信箱',
    password VARCHAR(255) NOT NULL COMMENT '密碼',
    name VARCHAR(100) NOT NULL COMMENT '使用者名稱',
    avatar VARCHAR(500) COMMENT '頭像URL',
    currency VARCHAR(3) DEFAULT 'TWD' COMMENT '預設幣別',
    notification_days INT DEFAULT 7 COMMENT '提前幾天通知',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否啟用',
    last_login_at TIMESTAMP NULL COMMENT '最後登入時間',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者表';

-- ============================================
-- 2. 類別表 (categories) 📁
-- ============================================
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '使用者ID',
    name VARCHAR(50) NOT NULL COMMENT '類別名稱',
    icon VARCHAR(50) DEFAULT 'folder' COMMENT '圖示',
    color VARCHAR(20) DEFAULT '#3B82F6' COMMENT '顏色',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='類別表';

-- ============================================
-- 3. 訂閱表 (subscriptions) 💳 核心表
-- ============================================
CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '使用者ID',
    category_id BIGINT COMMENT '類別ID',
    name VARCHAR(100) NOT NULL COMMENT '訂閱名稱',
    price DECIMAL(10, 2) NOT NULL COMMENT '金額',
    billing_cycle ENUM('daily', 'weekly', 'monthly', 'quarterly', 'yearly') DEFAULT 'monthly' COMMENT '週期',
    next_payment_date DATE NOT NULL COMMENT '下次扣款日',
    status ENUM('active', 'paused', 'cancelled') DEFAULT 'active' COMMENT '狀態',
    description TEXT COMMENT '描述',
    logo_url VARCHAR(500) COMMENT 'Logo URL',
    website_url VARCHAR(500) COMMENT '網站URL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_next_payment_date (next_payment_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂閱表';

-- ============================================
-- 4. 付款歷史表 (payment_history) 💰
-- ============================================
CREATE TABLE payment_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '使用者ID',
    subscription_id BIGINT NOT NULL COMMENT '訂閱ID',
    amount DECIMAL(10, 2) NOT NULL COMMENT '金額',
    payment_date DATE NOT NULL COMMENT '付款日期',
    status ENUM('completed', 'pending', 'failed', 'refunded') DEFAULT 'completed' COMMENT '付款狀態',
    notes TEXT COMMENT '備註',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_subscription_id (subscription_id),
    INDEX idx_payment_date (payment_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='付款歷史表';

-- ============================================
-- 5. 通知表 (notifications) 🔔
-- ============================================
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '使用者ID',
    subscription_id BIGINT COMMENT '訂閱ID',
    type ENUM('payment_due', 'payment_completed', 'subscription_expiring', 'general') DEFAULT 'general' COMMENT '通知類型',
    title VARCHAR(100) NOT NULL COMMENT '標題',
    message VARCHAR(500) COMMENT '內容',
    is_read TINYINT(1) DEFAULT 0 COMMENT '已讀',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ============================================
-- 統計視圖 (Views)
-- ============================================

-- 視圖 1: 使用者訂閱統計
CREATE VIEW user_subscription_stats AS
SELECT
    u.id AS user_id,
    u.name,
    u.email,
    COUNT(s.id) AS total_subscriptions,
    COUNT(CASE WHEN s.status = 'active' THEN 1 END) AS active_subscriptions,
    SUM(CASE
        WHEN s.billing_cycle = 'daily' THEN s.price * 30
        WHEN s.billing_cycle = 'weekly' THEN s.price * 4.33
        WHEN s.billing_cycle = 'monthly' THEN s.price
        WHEN s.billing_cycle = 'quarterly' THEN s.price / 3
        WHEN s.billing_cycle = 'yearly' THEN s.price / 12
    END) AS monthly_total
FROM users u
LEFT JOIN subscriptions s ON u.id = s.user_id AND s.status = 'active'
GROUP BY u.id, u.name, u.email;

-- 視圖 2: 即將到期的訂閱
CREATE VIEW upcoming_subscriptions AS
SELECT
    s.id,
    s.user_id,
    s.name AS subscription_name,
    s.price,
    s.billing_cycle,
    s.next_payment_date,
    c.name AS category_name,
    c.color AS category_color,
    DATEDIFF(s.next_payment_date, CURDATE()) AS days_until_payment
FROM subscriptions s
LEFT JOIN categories c ON s.category_id = c.id
WHERE s.status = 'active'
    AND s.next_payment_date >= CURDATE()
    AND s.next_payment_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)
ORDER BY s.next_payment_date ASC;

-- 視圖 3: 類別統計
CREATE VIEW category_stats AS
SELECT
    c.id AS category_id,
    c.user_id,
    c.name AS category_name,
    c.color,
    c.icon,
    COUNT(s.id) AS subscription_count,
    SUM(CASE
        WHEN s.billing_cycle = 'daily' THEN s.price * 30
        WHEN s.billing_cycle = 'weekly' THEN s.price * 4.33
        WHEN s.billing_cycle = 'monthly' THEN s.price
        WHEN s.billing_cycle = 'quarterly' THEN s.price / 3
        WHEN s.billing_cycle = 'yearly' THEN s.price / 12
    END) AS monthly_total
FROM categories c
LEFT JOIN subscriptions s ON c.id = s.category_id AND s.status = 'active'
GROUP BY c.id, c.user_id, c.name, c.color, c.icon;

-- 視圖 4: 付款統計
CREATE VIEW payment_stats AS
SELECT
    p.user_id,
    COUNT(p.id) AS total_payments,
    SUM(p.amount) AS total_amount,
    COUNT(CASE WHEN p.status = 'completed' THEN 1 END) AS completed_payments,
    COUNT(CASE WHEN p.status = 'failed' THEN 1 END) AS failed_payments,
    MAX(p.payment_date) AS last_payment_date
FROM payment_history p
GROUP BY p.user_id;

-- ============================================
-- 完成訊息
-- ============================================
SELECT '資料庫結構建立完成！' AS Message;
