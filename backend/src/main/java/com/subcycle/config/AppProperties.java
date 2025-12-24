package com.subcycle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 應用程式配置屬性
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 應用程式名稱
     */
    private String name = "SubCycle";

    /**
     * 郵件用的 Logo URL
     */
    private String logoUrl = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
