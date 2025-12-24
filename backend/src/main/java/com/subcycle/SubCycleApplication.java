package com.subcycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SubCycle 訂閱管理系統 - 主程式
 *
 * @author SubCycle Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class SubCycleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubCycleApplication.class, args);
        System.out.println("""
            
            ======================================
              SubCycle Backend API 已啟動！
              Port: 8080
            ======================================
            """
        );
    }

    /**
     * CORS 設定 - 允許前端 Vue 專案存取 API
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
