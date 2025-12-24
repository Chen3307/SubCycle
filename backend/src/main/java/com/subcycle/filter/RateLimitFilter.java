package com.subcycle.filter;

import com.subcycle.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response,
                                   @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 獲取客戶端標識（IP 地址或用戶 ID）
        String key = getClientKey(request);

        // 獲取該客戶端的 Bucket
        Bucket bucket = rateLimitConfig.resolveBucket(key);

        // 嘗試消耗 1 個令牌
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // 請求通過，添加速率限制相關的響應頭
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // 請求被限流
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String errorMessage = String.format(
                "{\"error\": \"請求過於頻繁\", \"message\": \"已達到速率限制，請在 %d 秒後重試\"}",
                waitForRefill
            );

            response.getWriter().write(errorMessage);
        }
    }

    /**
     * 獲取客戶端標識
     * 優先使用認證用戶 ID，否則使用 IP 地址
     */
    private String getClientKey(HttpServletRequest request) {
        // 如果用戶已登錄，使用用戶名或 ID
        String username = request.getRemoteUser();
        if (username != null) {
            return "user:" + username;
        }

        // 否則使用 IP 地址
        String clientIp = getClientIpAddress(request);
        return "ip:" + clientIp;
    }

    /**
     * 獲取客戶端真實 IP 地址
     * 處理代理和負載均衡器的情況
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 可以排除某些路徑不進行速率限制
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();

        // 排除靜態資源、健康檢查等
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator/health") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg");
    }
}
