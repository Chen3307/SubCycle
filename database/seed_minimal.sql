-- ============================================
-- SubCycle 測試資料（符合目前後端欄位）
-- ============================================

USE subcycle;

-- ============================================
-- 1. 插入測試使用者
--    密碼為純文字，後端啟動後 PasswordMigrationRunner 會自動 bcrypt
-- ============================================
INSERT INTO users (email, password, name, currency, notification_days, role, is_active) VALUES
('demo@subcycle.com', 'password123', 'Demo 用戶', 'TWD', 7, 'USER', 1),
('john@example.com', 'password123', 'John', 'USD', 7, 'USER', 1),
('mary@example.com', 'password123', 'Mary', 'EUR', 5, 'USER', 1);

-- ============================================
-- 2. 插入類別
-- ============================================
INSERT INTO categories (user_id, name, icon, color, sort_order) VALUES
-- demo 的類別
(1, '串流影音', 'play-circle', '#EF4444', 1),
(1, '音樂', 'music', '#F59E0B', 2),
(1, '雲端儲存', 'cloud', '#10B981', 3),
(1, '生產力工具', 'briefcase', '#3B82F6', 4),
(1, '遊戲', 'gamepad', '#8B5CF6', 5),

-- john 的類別
(2, 'Streaming', 'play-circle', '#EF4444', 1),
(2, 'Software', 'code', '#3B82F6', 2),

-- mary 的類別
(3, 'Entertainment', 'star', '#F59E0B', 1),
(3, 'Productivity', 'briefcase', '#10B981', 2),

-- 新增 demo 類別：健康
(1, '健康', 'heart', '#F56C6C', 6);

-- ============================================
-- 3. 插入訂閱
-- ============================================
INSERT INTO subscriptions (user_id, category_id, name, price, billing_cycle, next_payment_date, status, description) VALUES
-- demo 的訂閱
(1, 1, 'Netflix', 390.00, 'monthly', '2025-12-15', 'active', 'Premium 4K 方案'),
(1, 1, 'Disney+', 270.00, 'monthly', '2025-12-20', 'active', '標準方案'),
(1, 2, 'Spotify', 149.00, 'monthly', '2025-12-25', 'active', 'Premium 個人方案'),
(1, 2, 'YouTube Premium', 179.00, 'monthly', '2025-12-28', 'active', '無廣告＋音樂'),
(1, 3, 'Google One', 65.00, 'monthly', '2026-01-05', 'active', '100GB 儲存空間'),
(1, 3, 'Dropbox', 330.00, 'monthly', '2026-01-10', 'active', 'Plus 2TB 方案'),
(1, 4, 'ChatGPT Plus', 600.00, 'monthly', '2025-12-18', 'active', 'GPT-4 存取'),
(1, 4, 'Notion', 150.00, 'monthly', '2026-01-15', 'active', 'Personal Pro'),
(1, 5, 'Xbox Game Pass', 490.00, 'monthly', '2025-12-22', 'active', 'Ultimate 方案'),

-- john 的訂閱
(2, 6, 'Netflix', 15.99, 'monthly', '2025-12-12', 'active', 'Standard Plan'),
(2, 6, 'Hulu', 12.99, 'monthly', '2025-12-18', 'active', 'Ad-free Plan'),
(2, 7, 'GitHub Pro', 4.00, 'monthly', '2026-01-01', 'active', 'Developer Plan'),
(2, 7, 'Adobe Creative Cloud', 54.99, 'monthly', '2025-12-25', 'active', 'All Apps'),

-- mary 的訂閱
(3, 8, 'Amazon Prime', 8.99, 'monthly', '2025-12-20', 'active', 'Prime Video'),
(3, 9, 'Microsoft 365', 6.99, 'monthly', '2026-01-08', 'active', 'Personal');

-- ============================================
-- 4. 插入付款歷史
-- ============================================
INSERT INTO payment_history (user_id, subscription_id, amount, payment_date, status) VALUES
-- demo 的付款記錄
(1, 1, 390.00, '2025-11-15', 'completed'),
(1, 1, 390.00, '2025-10-15', 'completed'),
(1, 2, 270.00, '2025-11-20', 'completed'),
(1, 3, 149.00, '2025-11-25', 'completed'),
(1, 4, 179.00, '2025-11-28', 'completed'),
(1, 5, 65.00, '2025-12-05', 'completed'),
(1, 6, 330.00, '2025-12-10', 'completed'),
(1, 7, 600.00, '2025-11-18', 'completed'),
(1, 8, 150.00, '2025-12-15', 'pending'),

-- john 的付款記錄
(2, 10, 15.99, '2025-11-12', 'completed'),
(2, 11, 12.99, '2025-11-18', 'completed'),
(2, 12, 4.00, '2026-01-01', 'completed'),
(2, 13, 54.99, '2025-11-25', 'completed'),

-- mary 的付款記錄
(3, 14, 8.99, '2025-11-20', 'completed'),
(3, 15, 6.99, '2025-12-08', 'completed');

-- ============================================
-- 5. 插入通知
-- ============================================
INSERT INTO notifications (user_id, subscription_id, type, title, message, is_read) VALUES
-- demo 的通知
(1, 1, 'payment_due', 'Netflix 即將扣款', 'Netflix 將於 3 天後 (12/15) 扣款 NT$390', 0),
(1, 7, 'payment_due', 'ChatGPT Plus 即將扣款', 'ChatGPT Plus 將於 5 天後 (12/18) 扣款 NT$600', 0),
(1, 2, 'payment_due', 'Disney+ 即將扣款', 'Disney+ 將於 8 天後 (12/20) 扣款 NT$270', 0),
(1, NULL, 'general', '歡迎使用 SubCycle', '感謝您使用 SubCycle 訂閱管理系統！', 1),

-- john 的通知
(2, 10, 'payment_completed', 'Netflix 付款完成', 'Netflix $15.99 已成功扣款', 1),
(2, 12, 'payment_due', 'GitHub Pro 即將扣款', 'GitHub Pro 將於 19 天後 (1/1) 扣款 $4.00', 0),

-- mary 的通知
(3, 14, 'payment_due', 'Amazon Prime 即將扣款', 'Amazon Prime 將於 8 天後 (12/20) 扣款 $8.99', 0);

-- ============================================
-- 顯示測試資料統計
-- ============================================
SELECT '測試資料建立完成！' AS Message;
SELECT
    '使用者' AS 類別,
    COUNT(*) AS 數量
FROM users
UNION ALL
SELECT '類別', COUNT(*) FROM categories
UNION ALL
SELECT '訂閱', COUNT(*) FROM subscriptions
UNION ALL
SELECT '付款記錄', COUNT(*) FROM payment_history
UNION ALL
SELECT '通知', COUNT(*) FROM notifications;
