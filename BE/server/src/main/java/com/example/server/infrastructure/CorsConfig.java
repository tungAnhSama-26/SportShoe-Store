package com.example.server.infrastructure;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public CorsConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = resolveAllowedOrigins(appProperties.cors().allowedOrigins());
        registry.addMapping(appProperties.apiBasePath() + "/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    private String[] resolveAllowedOrigins(List<String> configuredOrigins) {
        if (configuredOrigins == null || configuredOrigins.isEmpty()) {
            return new String[]{"http://localhost:5173", "http://127.0.0.1:5173"};
        }
        return configuredOrigins.toArray(String[]::new);
    }
}
