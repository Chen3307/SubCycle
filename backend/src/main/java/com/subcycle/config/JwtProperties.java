package com.subcycle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置屬性
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 簽名密鑰
     */
    private String secret = "mySecretKeyForSubCycleApplicationThatIsAtLeast256BitsLong12345";

    /**
     * JWT 過期時間（毫秒）
     * 默認 24 小時 = 86400000 毫秒
     */
    private Long expiration = 86400000L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
}
