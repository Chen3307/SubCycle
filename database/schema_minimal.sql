-- ============================================
-- SubCycle 訂閱管理系統 - 極精簡版資料庫架構
-- ============================================
-- 版本：3.0 (極精簡版)
-- 建立日期：2025-11-25
-- 說明：只保留核心功能的最小化版本
-- 資料表數量：4 張
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 建立資料庫
-- ============================================
CREATE DATABASE IF NOT EXISTS subcycle
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE subcycle;

-- ============================================
-- 1. 使用者表
-- ============================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '使用者 ID',
  `email` VARCHAR(255) NOT NULL COMMENT '電子郵件',
  `password` VARCHAR(255) NOT NULL COMMENT '密碼（加密）',
  `name` VARCHAR(100) NOT NULL COMMENT '姓名',
  `avatar` VARCHAR(500) NULL COMMENT '頭像 URL',

  -- 基本設定
  `currency` VARCHAR(3) DEFAULT 'TWD' COMMENT '預設幣別',
  `notification_days` INT DEFAULT 7 COMMENT '提前幾天提醒',

  -- 帳號狀態
  `is_active` BOOLEAN DEFAULT TRUE COMMENT '帳號是否啟用',
  `last_login_at` DATETIME NULL COMMENT '最後登入時間',

  -- 時間戳記
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者表';

-- ============================================
-- 2. 類別表
-- ============================================
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '類別 ID',
  `user_id` INT UNSIGNED NOT NULL COMMENT '使用者 ID',
  `name` VARCHAR(50) NOT NULL COMMENT '類別名稱',
  `color` VARCHAR(7) DEFAULT '#409EFF' COMMENT '顏色代碼',
  `icon` VARCHAR(50) DEFAULT 'More' COMMENT '圖示名稱',
  `sort_order` INT DEFAULT 0 COMMENT '排序順序',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_categories_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='類別表';

-- ============================================
-- 3. 訂閱項目表（整合付款資訊）
-- ============================================
DROP TABLE IF EXISTS `subscriptions`;
CREATE TABLE `subscriptions` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '訂閱 ID',
  `user_id` INT UNSIGNED NOT NULL COMMENT '使用者 ID',
  `category_id` INT UNSIGNED NOT NULL COMMENT '類別 ID',

  -- 基本資訊
  `name` VARCHAR(100) NOT NULL COMMENT '訂閱名稱',
  `description` TEXT NULL COMMENT '描述',
  `logo` VARCHAR(500) NULL COMMENT 'Logo URL',
  `website` VARCHAR(500) NULL COMMENT '官方網站',

  -- 金額與週期
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '金額',
  `currency` VARCHAR(3) DEFAULT 'TWD' COMMENT '幣別',
  `cycle` ENUM('daily', 'weekly', 'monthly', 'quarterly', 'yearly') DEFAULT 'monthly' COMMENT '週期',

  -- 日期管理
  `start_date` DATE NOT NULL COMMENT '開始日期',
  `next_payment_date` DATE NOT NULL COMMENT '下次付款日期',
  `last_payment_date` DATE NULL COMMENT '上次付款日期',

  -- 狀態與設定
  `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否啟用',
  `payment_method` VARCHAR(50) NULL COMMENT '付款方式',
  `notes` TEXT NULL COMMENT '備註',

  -- 統計資訊
  `total_paid` DECIMAL(12, 2) DEFAULT 0.00 COMMENT '累計已付金額',
  `payment_count` INT DEFAULT 0 COMMENT '付款次數',

  -- 時間戳記
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_next_payment_date` (`next_payment_date`),
  KEY `idx_user_active` (`user_id`, `is_active`),
  CONSTRAINT `fk_subscriptions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_subscriptions_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂閱項目表';

-- ============================================
-- 4. 通知表
-- ============================================
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知 ID',
  `user_id` INT UNSIGNED NOT NULL COMMENT '使用者 ID',
  `subscription_id` INT UNSIGNED NULL COMMENT '訂閱 ID',
  `type` ENUM('payment_reminder', 'expired', 'system') DEFAULT 'payment_reminder' COMMENT '通知類型',
  `title` VARCHAR(255) NOT NULL COMMENT '標題',
  `message` TEXT NOT NULL COMMENT '訊息內容',
  `is_read` BOOLEAN DEFAULT FALSE COMMENT '是否已讀',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_notifications_subscription` FOREIGN KEY (`subscription_id`) REFERENCES `subscriptions` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ============================================
-- 建立實用視圖
-- ============================================

-- 使用者統計視圖
CREATE OR REPLACE VIEW `v_user_stats` AS
SELECT
  u.id AS user_id,
  u.name,
  u.email,
  COUNT(s.id) AS total_subscriptions,
  SUM(CASE WHEN s.is_active = TRUE THEN 1 ELSE 0 END) AS active_subscriptions,
  SUM(CASE
    WHEN s.is_active = TRUE THEN
      CASE s.cycle
        WHEN 'daily' THEN s.amount * 30
        WHEN 'weekly' THEN s.amount * 4.33
        WHEN 'monthly' THEN s.amount
        WHEN 'quarterly' THEN s.amount / 3
        WHEN 'yearly' THEN s.amount / 12
      END
    ELSE 0
  END) AS monthly_total,
  SUM(s.total_paid) AS total_spent,
  MAX(s.next_payment_date) AS next_payment_date
FROM users u
LEFT JOIN subscriptions s ON u.id = s.user_id
GROUP BY u.id, u.name, u.email;

-- 即將到期訂閱視圖（7天內）
CREATE OR REPLACE VIEW `v_upcoming_payments` AS
SELECT
  s.id,
  s.user_id,
  s.name,
  s.amount,
  s.currency,
  s.next_payment_date,
  DATEDIFF(s.next_payment_date, CURDATE()) AS days_until_payment,
  c.name AS category_name,
  c.color AS category_color
FROM subscriptions s
JOIN categories c ON s.category_id = c.id
WHERE s.is_active = TRUE
  AND s.next_payment_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
ORDER BY s.next_payment_date ASC;

-- 類別統計視圖
CREATE OR REPLACE VIEW `v_category_stats` AS
SELECT
  c.id AS category_id,
  c.user_id,
  c.name AS category_name,
  c.color,
  c.icon,
  COUNT(s.id) AS subscription_count,
  SUM(CASE
    WHEN s.is_active = TRUE THEN
      CASE s.cycle
        WHEN 'daily' THEN s.amount * 30
        WHEN 'weekly' THEN s.amount * 4.33
        WHEN 'monthly' THEN s.amount
        WHEN 'quarterly' THEN s.amount / 3
        WHEN 'yearly' THEN s.amount / 12
      END
    ELSE 0
  END) AS monthly_total
FROM categories c
LEFT JOIN subscriptions s ON c.id = s.category_id
GROUP BY c.id, c.user_id, c.name, c.color, c.icon
ORDER BY monthly_total DESC;

-- ============================================
-- 啟用外鍵檢查
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 完成提示
-- ============================================
SELECT '✓ 極精簡版資料庫架構建立完成！' AS 'Status';
SELECT '📊 核心資料表：4 張' AS 'Tables';
SELECT '📈 統計視圖：3 個' AS 'Views';
SELECT '🎯 專注核心功能，結構清晰簡單' AS 'Description';
