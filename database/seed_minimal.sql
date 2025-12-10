-- ============================================
-- SubCycle 訂閱管理系統 - 極精簡版初始資料
-- ============================================
-- 版本：3.0 (極精簡版)
-- 建立日期：2025-11-25
-- 說明：最基本的測試資料
-- ============================================

USE subcycle;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 測試用使用者
-- ============================================
-- 帳號：demo@subcycle.com
-- 密碼：password123
-- 注意：實際使用請改用 bcrypt 加密密碼
INSERT INTO `users` (`email`, `password`, `name`, `currency`, `notification_days`, `is_active`, `created_at`) VALUES
('demo@subcycle.com', '$2b$10$YourHashedPasswordHere', 'Demo 用戶', 'TWD', 7, TRUE, NOW());

-- ============================================
-- 2. 預設類別（8 個常用類別）
-- ============================================
INSERT INTO `categories` (`user_id`, `name`, `color`, `icon`, `sort_order`)
SELECT id, '影音娛樂', '#409EFF', 'VideoPlay', 1 FROM `users` WHERE email = 'demo@subcycle.com'
UNION ALL
SELECT id, '工作軟體', '#67C23A', 'Briefcase', 2 FROM `users` WHERE email = 'demo@subcycle.com'
UNION ALL
SELECT id, '健康運動', '#F56C6C', 'TrophyBase', 3 FROM `users` WHERE email = 'demo@subcycle.com'
UNION ALL
SELECT id, '學習成長', '#E6A23C', 'Reading', 4 FROM `users` WHERE email = 'demo@subcycle.com'
UNION ALL
SELECT id, '雲端儲存', '#00CED1', 'Folder', 5 FROM `users` WHERE email = 'demo@subcycle.com'
UNION ALL
SELECT id, '購物會員', '#FF69B4', 'ShoppingCart', 6 FROM `users` WHERE email = 'demo@subcycle.com'
UNION ALL
SELECT id, '生活服務', '#9C27B0', 'Service', 7 FROM `users` WHERE email = 'demo@subcycle.com'
UNION ALL
SELECT id, '其他', '#909399', 'More', 99 FROM `users` WHERE email = 'demo@subcycle.com';

-- ============================================
-- 3. 範例訂閱項目（3 個）
-- ============================================
-- Netflix
INSERT INTO `subscriptions` (`user_id`, `category_id`, `name`, `amount`, `currency`, `cycle`, `start_date`, `next_payment_date`, `last_payment_date`, `is_active`, `payment_method`, `total_paid`, `payment_count`)
SELECT
  u.id,
  c.id,
  'Netflix',
  390.00,
  'TWD',
  'monthly',
  '2024-01-01',
  '2025-12-01',
  '2025-11-01',
  TRUE,
  '信用卡',
  4290.00,
  11
FROM `users` u
JOIN `categories` c ON c.user_id = u.id AND c.name = '影音娛樂'
WHERE u.email = 'demo@subcycle.com';

-- Spotify
INSERT INTO `subscriptions` (`user_id`, `category_id`, `name`, `amount`, `currency`, `cycle`, `start_date`, `next_payment_date`, `last_payment_date`, `is_active`, `payment_method`, `total_paid`, `payment_count`)
SELECT
  u.id,
  c.id,
  'Spotify',
  149.00,
  'TWD',
  'monthly',
  '2024-02-15',
  '2025-12-15',
  '2025-11-15',
  TRUE,
  '信用卡',
  1490.00,
  10
FROM `users` u
JOIN `categories` c ON c.user_id = u.id AND c.name = '影音娛樂'
WHERE u.email = 'demo@subcycle.com';

-- Google One
INSERT INTO `subscriptions` (`user_id`, `category_id`, `name`, `amount`, `currency`, `cycle`, `start_date`, `next_payment_date`, `last_payment_date`, `is_active`, `payment_method`, `total_paid`, `payment_count`)
SELECT
  u.id,
  c.id,
  'Google One',
  65.00,
  'TWD',
  'monthly',
  '2024-03-01',
  '2025-12-01',
  '2025-11-01',
  TRUE,
  'Google Pay',
  585.00,
  9
FROM `users` u
JOIN `categories` c ON c.user_id = u.id AND c.name = '雲端儲存'
WHERE u.email = 'demo@subcycle.com';

-- ============================================
-- 4. 範例通知（1 個即將到期提醒）
-- ============================================
INSERT INTO `notifications` (`user_id`, `subscription_id`, `type`, `title`, `message`, `is_read`, `created_at`)
SELECT
  u.id,
  s.id,
  'payment_reminder',
  'Netflix 即將續訂',
  '您的 Netflix 訂閱將在 7 天後（2025-12-01）自動續訂，金額為 NT$ 390。',
  FALSE,
  NOW()
FROM `users` u
JOIN `subscriptions` s ON s.user_id = u.id AND s.name = 'Netflix'
WHERE u.email = 'demo@subcycle.com';

-- ============================================
-- 啟用外鍵檢查
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 驗證資料
-- ============================================
SELECT '✓ 資料載入完成！' AS 'Status';
SELECT CONCAT('用戶：', COUNT(*), ' 個') AS 'Users' FROM users;
SELECT CONCAT('類別：', COUNT(*), ' 個') AS 'Categories' FROM categories;
SELECT CONCAT('訂閱：', COUNT(*), ' 個') AS 'Subscriptions' FROM subscriptions;
SELECT CONCAT('通知：', COUNT(*), ' 個') AS 'Notifications' FROM notifications;

-- ============================================
-- 顯示測試帳號資訊
-- ============================================
SELECT '📧 測試帳號資訊' AS '';
SELECT '帳號：demo@subcycle.com' AS 'Email';
SELECT '密碼：password123' AS 'Password';
SELECT '⚠ 記得修改密碼加密方式！' AS 'Warning';
