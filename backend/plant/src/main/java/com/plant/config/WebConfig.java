package com.plant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.localhost.url}")
    private String localhostUrl;

    @Value("${app.frontend.www.url}")
    private String frontendWwwUrl;

    // Allow localhost ports for frontend to send requets to the backend
    // Currently it's localhost, will be changed to something else once hosted
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

}
