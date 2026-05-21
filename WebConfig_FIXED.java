package com.library_web.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.web.cors.allowed-origins}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Cho phép các domain cụ thể
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://is-203-quan-ly-thu-vien-2e4f5aq1r.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // ✅ Thêm OPTIONS
                .allowedHeaders("*") // Cho phép tất cả header
                .allowCredentials(true) // Cho phép gửi cookie nếu cần
                .maxAge(3600); // Cache CORS config trong 1 giờ
    }

    /**
     * ✅ NEW: Serve static files từ folder uploads/
     * Điều này cho phép truy cập: http://localhost:8080/uploads/avatars/image.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploads folder as static resources
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600); // Cache 1 giờ

        // Cũng có thể serve từ classpath nếu cần
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }

}

