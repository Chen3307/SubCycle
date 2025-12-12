-- ============================================
-- 新增密碼重設 Token 表
-- ============================================
USE subcycle;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '使用者ID',
    token VARCHAR(100) NOT NULL UNIQUE COMMENT '重設 Token',
    expiry_date TIMESTAMP NOT NULL COMMENT '過期時間',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_token (token),
    INDEX idx_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密碼重設 Token 表';

SELECT '密碼重設 Token 表建立完成！' AS Message;
