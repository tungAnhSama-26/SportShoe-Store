package com.example.server.infrastructure.config;


import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import com.example.server.infrastructure.security.ratelimit.RateLimitInterceptor;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;
    private final RealtimeUpdateInterceptor realtimeUpdateInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public CorsConfig(AppProperties appProperties, RealtimeUpdateInterceptor realtimeUpdateInterceptor, RateLimitInterceptor rateLimitInterceptor) {
        this.appProperties = appProperties;
        this.realtimeUpdateInterceptor = realtimeUpdateInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(realtimeUpdateInterceptor)
                .addPathPatterns("/api/v1/admin/san-pham/**", "/api/v1/admin/san-pham-chi-tiet/**", "/api/v1/admin/danh-muc/**", "/api/v1/admin/ban-hang/**", "/api/v1/admin/ban-hang-tai-quay/**");
        
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOriginPatterns = resolveAllowedOriginPatterns(appProperties.cors().allowedOrigins());
        registry.addMapping(appProperties.apiBasePath() + "/**")
                .allowedOrigins()
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    private String[] resolveAllowedOriginPatterns(List<String> configuredOrigins) {
        if (configuredOrigins == null || configuredOrigins.isEmpty()) {
            return new String[]{
                    "http://localhost:*",
                    "http://127.0.0.1:*"
            };
        }
        return configuredOrigins.toArray(String[]::new);
    }
}
