package com.example.highhillsstudio.HighHillsStudio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Value("${product.upload.dir}")
    private String productUploadDir;

    @Value("${profile.upload.dir}")
    private String profileUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Serve product images
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + productUploadDir);

        // Serve profile images
        registry.addResourceHandler("/uploads/profile-images/**")
                .addResourceLocations("file:" + profileUploadDir);
    }




}
