package com.subcycle.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    /**
     * 存儲每個 IP 或用戶的 Bucket
     */
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * 獲取或創建 Bucket
     * 默認限制：每分鐘 100 個請求
     */
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * 創建新的 Bucket
     * 配置：每分鐘允許 100 個請求
     */
    private Bucket createNewBucket() {
        // 每分鐘補充 100 個令牌
        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillIntervally(100, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * 創建嚴格的 Bucket（用於敏感操作）
     * 配置：每分鐘允許 10 個請求
     */
    public Bucket createStrictBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * 清理緩存中的過期 Bucket
     */
    public void cleanupCache() {
        // 可以定期調用此方法清理緩存
        // 這裡簡單實現，生產環境可以使用 Caffeine 或 Redis
        if (cache.size() > 10000) {
            cache.clear();
        }
    }
}
