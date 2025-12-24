-- 新增 include_historical_payments 欄位到 subscriptions 表
ALTER TABLE subscriptions
ADD COLUMN include_historical_payments TINYINT(1) DEFAULT 0
COMMENT '是否計入歷史支出（0=否，只計算今天之後；1=是，從起始日開始計算）';

-- 為現有訂閱設置預設值為 false (0)
UPDATE subscriptions
SET include_historical_payments = 0
WHERE include_historical_payments IS NULL;
