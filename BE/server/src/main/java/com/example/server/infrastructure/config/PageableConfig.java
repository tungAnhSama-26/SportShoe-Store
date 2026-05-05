package com.example.server.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class PageableConfig {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer(AppProperties appProperties) {
        return resolver -> {
            resolver.setOneIndexedParameters(false);
            resolver.setMaxPageSize(appProperties.pagination().maxSize());
            resolver.setFallbackPageable(PageRequest.of(
                    appProperties.pagination().defaultPage(),
                    appProperties.pagination().defaultSize()
            ));
        };
    }
}
