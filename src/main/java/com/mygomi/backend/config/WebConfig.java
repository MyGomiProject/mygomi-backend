package com.mygomi.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 게시글 이미지 + 신고 첨부파일 모두 서빙 (C:/mygomi-uploads/ 하위 전체)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/mygomi-uploads/");
    }
}
