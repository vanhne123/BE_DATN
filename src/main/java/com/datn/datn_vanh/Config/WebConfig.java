package com.datn.datn_vanh.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000") // chỉ định origin chính xác
                        .allowedMethods("*")
                        .allowedHeaders("*") // cho phép tất cả headers, bao gồm Authorization
                        .allowCredentials(true); // cần thiết nếu dùng cookie hoặc token
            }
        };
    }
}
