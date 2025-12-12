-- ============================================
-- SubCycle 豐富的測試資料
-- 為 demo@subcycle.com 添加更多真實的訂閱數據
-- ============================================

USE subcycle;

-- ============================================
-- 清除舊資料（僅針對 demo 用戶）
-- ============================================
DELETE FROM notifications WHERE user_id = 1;
DELETE FROM payment_history WHERE user_id = 1;
DELETE FROM subscriptions WHERE user_id = 1;
DELETE FROM categories WHERE user_id = 1;

-- ============================================
-- 1. 插入豐富的類別
-- ============================================
INSERT INTO categories (user_id, name, icon, color, sort_order) VALUES
(1, '串流影音', 'play-circle', '#EF4444', 1),
(1, '音樂', 'music', '#F59E0B', 2),
(1, '雲端儲存', 'cloud', '#10B981', 3),
(1, '生產力工具', 'briefcase', '#3B82F6', 4),
(1, '遊戲娛樂', 'gamepad', '#8B5CF6', 5),
(1, '健康運動', 'heart', '#EC4899', 6),
(1, '新聞雜誌', 'newspaper', '#6366F1', 7),
(1, '學習教育', 'book', '#14B8A6', 8),
(1, '設計開發', 'code', '#F97316', 9),
(1, '生活購物', 'shopping-cart', '#84CC16', 10);

-- ============================================
-- 2. 獲取剛插入的類別ID
-- ============================================
SET @cat_streaming = (SELECT id FROM categories WHERE user_id = 1 AND name = '串流影音');
SET @cat_music = (SELECT id FROM categories WHERE user_id = 1 AND name = '音樂');
SET @cat_cloud = (SELECT id FROM categories WHERE user_id = 1 AND name = '雲端儲存');
SET @cat_productivity = (SELECT id FROM categories WHERE user_id = 1 AND name = '生產力工具');
SET @cat_gaming = (SELECT id FROM categories WHERE user_id = 1 AND name = '遊戲娛樂');
SET @cat_health = (SELECT id FROM categories WHERE user_id = 1 AND name = '健康運動');
SET @cat_news = (SELECT id FROM categories WHERE user_id = 1 AND name = '新聞雜誌');
SET @cat_education = (SELECT id FROM categories WHERE user_id = 1 AND name = '學習教育');
SET @cat_design = (SELECT id FROM categories WHERE user_id = 1 AND name = '設計開發');
SET @cat_shopping = (SELECT id FROM categories WHERE user_id = 1 AND name = '生活購物');

-- ============================================
-- 3. 插入豐富的訂閱數據（包含不同週期）
-- ============================================
INSERT INTO subscriptions (user_id, category_id, name, price, billing_cycle, next_payment_date, status, description) VALUES
-- 串流影音 (每月)
(1, @cat_streaming, 'Netflix', 390.00, 'monthly', '2025-12-15', 'active', 'Premium 4K 方案，支援 4 個螢幕同時觀看'),
(1, @cat_streaming, 'Disney+', 270.00, 'monthly', '2025-12-20', 'active', '迪士尼、漫威、星際大戰全系列'),
(1, @cat_streaming, 'HBO GO', 190.00, 'monthly', '2025-12-25', 'active', '好萊塢大片與 HBO 原創影集'),
(1, @cat_streaming, 'friDay影音', 199.00, 'monthly', '2025-12-18', 'active', '台灣本地影音平台'),

-- 音樂 (每月)
(1, @cat_music, 'Spotify', 149.00, 'monthly', '2025-12-14', 'active', 'Premium 個人方案，無廣告播放'),
(1, @cat_music, 'YouTube Premium', 179.00, 'monthly', '2025-12-22', 'active', '無廣告觀看 + YouTube Music'),
(1, @cat_music, 'Apple Music', 150.00, 'monthly', '2026-01-05', 'active', '無損音質，支援空間音訊'),

-- 雲端儲存 (每月、每年)
(1, @cat_cloud, 'Google One', 65.00, 'monthly', '2025-12-28', 'active', '100GB 雲端儲存空間'),
(1, @cat_cloud, 'Dropbox', 330.00, 'monthly', '2026-01-10', 'active', 'Plus 2TB 專業方案'),
(1, @cat_cloud, 'iCloud+', 900.00, 'yearly', '2026-03-15', 'active', '200GB 年付方案，含隱藏郵件功能'),

-- 生產力工具 (每月、每年)
(1, @cat_productivity, 'ChatGPT Plus', 600.00, 'monthly', '2025-12-16', 'active', 'GPT-4 優先存取，無使用限制'),
(1, @cat_productivity, 'Notion', 150.00, 'monthly', '2025-12-19', 'active', 'Personal Pro，無限檔案上傳'),
(1, @cat_productivity, 'Microsoft 365', 2190.00, 'yearly', '2026-02-01', 'active', 'Personal 年付，含 1TB OneDrive'),
(1, @cat_productivity, 'Evernote', 169.00, 'monthly', '2026-01-08', 'active', 'Professional 筆記服務'),

-- 遊戲娛樂 (每月、每季)
(1, @cat_gaming, 'Xbox Game Pass', 490.00, 'monthly', '2025-12-17', 'active', 'Ultimate 方案，含 PC 與主機遊戲'),
(1, @cat_gaming, 'PlayStation Plus', 1350.00, 'quarterly', '2026-02-15', 'active', 'Extra 級別，超過 400 款遊戲'),
(1, @cat_gaming, 'Nintendo Switch Online', 290.00, 'yearly', '2026-04-20', 'active', '擴充包方案，含復古遊戲'),
(1, @cat_gaming, 'Steam 錢包', 500.00, 'monthly', '2025-12-30', 'active', '每月遊戲預算'),

-- 健康運動 (每月、每年)
(1, @cat_health, 'Nike Training Club', 0.00, 'yearly', '2026-05-01', 'active', '免費健身課程（提醒用）'),
(1, @cat_health, 'MyFitnessPal', 320.00, 'yearly', '2026-03-10', 'active', 'Premium 營養追蹤'),
(1, @cat_health, 'Headspace', 399.00, 'yearly', '2026-01-25', 'active', '冥想與正念練習 App'),

-- 新聞雜誌 (每月)
(1, @cat_news, 'The New York Times', 140.00, 'monthly', '2025-12-13', 'active', '數位訂閱，含遊戲與食譜'),
(1, @cat_news, 'Readmoo 讀墨', 199.00, 'monthly', '2026-01-12', 'active', '電子書吃到飽'),
(1, @cat_news, '天下雜誌', 99.00, 'monthly', '2025-12-21', 'active', '數位版訂閱'),

-- 學習教育 (每月、每年)
(1, @cat_education, 'Hahow 好學校', 490.00, 'monthly', '2025-12-24', 'active', '無限會員，所有課程無限看'),
(1, @cat_education, 'Coursera Plus', 1780.00, 'yearly', '2026-06-01', 'active', '超過 7000 門課程無限存取'),
(1, @cat_education, 'Duolingo Super', 420.00, 'yearly', '2026-02-20', 'active', '語言學習進階版'),

-- 設計開發 (每月、每年)
(1, @cat_design, 'GitHub Copilot', 300.00, 'monthly', '2025-12-26', 'active', 'AI 程式碼助手'),
(1, @cat_design, 'Figma', 450.00, 'monthly', '2026-01-03', 'active', 'Professional 設計工具'),
(1, @cat_design, 'Adobe Creative Cloud', 1785.00, 'monthly', '2025-12-27', 'active', '攝影方案，含 Photoshop 與 Lightroom'),

-- 生活購物 (每月、每年)
(1, @cat_shopping, 'Amazon Prime', 170.00, 'monthly', '2026-01-07', 'active', '免運費與 Prime Video'),
(1, @cat_shopping, 'Costco 會員', 1350.00, 'yearly', '2026-08-15', 'active', '金星會員年費'),
(1, @cat_shopping, 'Line 貼圖小舖', 150.00, 'monthly', '2025-12-23', 'active', '每月貼圖預算');

-- ============================================
-- 4. 獲取訂閱ID
-- ============================================
SET @sub_netflix = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'Netflix');
SET @sub_disney = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'Disney+');
SET @sub_spotify = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'Spotify');
SET @sub_youtube = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'YouTube Premium');
SET @sub_google = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'Google One');
SET @sub_dropbox = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'Dropbox');
SET @sub_icloud = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'iCloud+');
SET @sub_chatgpt = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'ChatGPT Plus');
SET @sub_notion = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'Notion');
SET @sub_ms365 = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'Microsoft 365');
SET @sub_xbox = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'Xbox Game Pass');
SET @sub_ps = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'PlayStation Plus');
SET @sub_github = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'GitHub Copilot');
SET @sub_myfit = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'MyFitnessPal');
SET @sub_nyt = (SELECT id FROM subscriptions WHERE user_id = 1 AND name = 'The New York Times');

-- ============================================
-- 5. 插入歷史付款記錄
-- ============================================
INSERT INTO payment_history (user_id, subscription_id, amount, payment_date, status, notes) VALUES
-- 最近 3 個月的付款記錄
-- Netflix
(1, @sub_netflix, 390.00, '2025-11-15', 'completed', NULL),
(1, @sub_netflix, 390.00, '2025-10-15', 'completed', NULL),
(1, @sub_netflix, 390.00, '2025-09-15', 'completed', NULL),

-- Disney+
(1, @sub_disney, 270.00, '2025-11-20', 'completed', NULL),
(1, @sub_disney, 270.00, '2025-10-20', 'completed', NULL),

-- Spotify
(1, @sub_spotify, 149.00, '2025-11-14', 'completed', NULL),
(1, @sub_spotify, 149.00, '2025-10-14', 'completed', NULL),
(1, @sub_spotify, 149.00, '2025-09-14', 'completed', NULL),

-- YouTube Premium
(1, @sub_youtube, 179.00, '2025-11-22', 'completed', NULL),
(1, @sub_youtube, 179.00, '2025-10-22', 'completed', NULL),

-- Google One
(1, @sub_google, 65.00, '2025-11-28', 'completed', NULL),
(1, @sub_google, 65.00, '2025-10-28', 'completed', NULL),

-- ChatGPT Plus
(1, @sub_chatgpt, 600.00, '2025-11-16', 'completed', NULL),
(1, @sub_chatgpt, 600.00, '2025-10-16', 'completed', NULL),

-- Notion
(1, @sub_notion, 150.00, '2025-11-19', 'completed', NULL),
(1, @sub_notion, 150.00, '2025-10-19', 'completed', NULL),

-- Xbox Game Pass
(1, @sub_xbox, 490.00, '2025-11-17', 'completed', NULL),
(1, @sub_xbox, 490.00, '2025-10-17', 'completed', NULL),

-- GitHub Copilot
(1, @sub_github, 300.00, '2025-11-26', 'completed', NULL),
(1, @sub_github, 300.00, '2025-10-26', 'completed', NULL),

-- 年付記錄
(1, @sub_icloud, 900.00, '2025-03-15', 'completed', 'iCloud+ 年費'),
(1, @sub_ms365, 2190.00, '2025-02-01', 'completed', 'Microsoft 365 年費'),
(1, @sub_myfit, 320.00, '2025-03-10', 'completed', 'MyFitnessPal 年費'),

-- 季付記錄
(1, @sub_ps, 1350.00, '2025-11-15', 'completed', 'PlayStation Plus 季費'),

-- 一筆失敗的付款
(1, @sub_dropbox, 330.00, '2025-11-10', 'failed', '信用卡額度不足');

-- ============================================
-- 6. 插入通知（即將到期的提醒）
-- ============================================
INSERT INTO notifications (user_id, subscription_id, type, title, message, is_read, created_at) VALUES
-- 未讀通知（7天內到期）
(1, @sub_nyt, 'payment_due', 'The New York Times 即將扣款', 'The New York Times 將於 1 天後 (12/13) 扣款 NT$140', 0, '2025-12-12 09:00:00'),
(1, @sub_spotify, 'payment_due', 'Spotify 即將扣款', 'Spotify 將於 2 天後 (12/14) 扣款 NT$149', 0, '2025-12-12 09:05:00'),
(1, @sub_netflix, 'payment_due', 'Netflix 即將扣款', 'Netflix 將於 3 天後 (12/15) 扣款 NT$390', 0, '2025-12-12 09:10:00'),
(1, @sub_chatgpt, 'payment_due', 'ChatGPT Plus 即將扣款', 'ChatGPT Plus 將於 4 天後 (12/16) 扣款 NT$600', 0, '2025-12-12 09:15:00'),
(1, @sub_xbox, 'payment_due', 'Xbox Game Pass 即將扣款', 'Xbox Game Pass 將於 5 天後 (12/17) 扣款 NT$490', 0, '2025-12-12 09:20:00'),

-- 已讀通知
(1, NULL, 'general', '歡迎使用 SubCycle', '感謝您使用 SubCycle 訂閱管理系統！開始追蹤您的所有訂閱服務吧！', 1, '2025-12-01 10:00:00'),
(1, @sub_netflix, 'payment_completed', 'Netflix 付款完成', 'Netflix NT$390 已成功扣款', 1, '2025-11-15 08:30:00'),
(1, @sub_chatgpt, 'payment_completed', 'ChatGPT Plus 付款完成', 'ChatGPT Plus NT$600 已成功扣款', 1, '2025-11-16 08:30:00'),
(1, @sub_dropbox, 'general', 'Dropbox 付款失敗', 'Dropbox 付款失敗，請檢查您的付款方式', 1, '2025-11-10 14:20:00');

-- ============================================
-- 顯示測試資料統計
-- ============================================
SELECT '豐富測試資料建立完成！' AS Message;

SELECT
    '統計資訊' AS 類型,
    '數量' AS 值
UNION ALL
SELECT '類別數', COUNT(*) FROM categories WHERE user_id = 1
UNION ALL
SELECT '訂閱數', COUNT(*) FROM subscriptions WHERE user_id = 1
UNION ALL
SELECT '付款記錄', COUNT(*) FROM payment_history WHERE user_id = 1
UNION ALL
SELECT '通知數', COUNT(*) FROM notifications WHERE user_id = 1;

-- 顯示月均支出
SELECT
    '月均總支出' AS 項目,
    CONCAT('NT$ ', FORMAT(SUM(
        CASE
            WHEN s.billing_cycle = 'daily' THEN s.price * 30
            WHEN s.billing_cycle = 'weekly' THEN s.price * 4.33
            WHEN s.billing_cycle = 'monthly' THEN s.price
            WHEN s.billing_cycle = 'quarterly' THEN s.price / 3
            WHEN s.billing_cycle = 'yearly' THEN s.price / 12
        END
    ), 2)) AS 金額
FROM subscriptions s
WHERE s.user_id = 1 AND s.status = 'active';

-- 顯示類別分布
SELECT
    c.name AS 類別,
    COUNT(s.id) AS 訂閱數,
    CONCAT('NT$ ', FORMAT(SUM(
        CASE
            WHEN s.billing_cycle = 'monthly' THEN s.price
            WHEN s.billing_cycle = 'quarterly' THEN s.price / 3
            WHEN s.billing_cycle = 'yearly' THEN s.price / 12
        END
    ), 2)) AS 月均支出
FROM categories c
LEFT JOIN subscriptions s ON c.id = s.category_id AND s.status = 'active'
WHERE c.user_id = 1
GROUP BY c.id, c.name
ORDER BY 訂閱數 DESC;
