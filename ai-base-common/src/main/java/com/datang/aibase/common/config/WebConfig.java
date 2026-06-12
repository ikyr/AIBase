package com.datang.aibase.common.config;

import com.datang.aibase.common.security.ApiKeyAuthFilter;
import com.datang.aibase.common.security.ApiKeyStore;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("${CORS_ORIGINS:http://localhost:*,http://10.139.11.100,http://10.139.11.100:*}")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "X-Api-Key", "X-User-Id", "X-Dept-Id")
                .allowCredentials(true);
    }

    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilterRegistration(ApiKeyStore apiKeyStore) {
        FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiKeyAuthFilter(apiKeyStore));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
