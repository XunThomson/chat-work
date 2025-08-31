package com.xun.CORS;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 指定应用到哪些路径
                .allowedOrigins(
                        "http://localhost:8080",
                        "http://localhost:8081",
                        "http://localhost:5500",
                        "http://localhost:3691",
                        "http://192.168.92.104:3690",
                        "http://192.168.92.104:3691",
                        "http://192.168.92.104",
                        "http://192.168.92.104:80"
                ) // 允许的源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许的头信息
                .exposedHeaders("Content-Type")
                .allowCredentials(true); // 是否支持凭证
    }
}
