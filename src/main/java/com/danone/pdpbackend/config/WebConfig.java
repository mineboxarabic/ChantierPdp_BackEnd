package com.danone.pdpbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@CrossOrigin(origins = "http://localhost:5173")
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Allow all paths
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:5173/",
                        "http://10.7.21.52:5173",
                        "http://172.25.32.1:5173"
                ) // List all allowed origins in one call
                .allowedMethods("*") // Allow all HTTP methods (GET, POST, etc.)
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true); // Allow credentials (cookies, etc.)
    }
}