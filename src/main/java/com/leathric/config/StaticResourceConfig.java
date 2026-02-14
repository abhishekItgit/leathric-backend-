package com.leathric.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();

        // Expose local uploads folder so stored product images are reachable over HTTP.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
