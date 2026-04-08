package com.example.server.infrastructure;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String apiBasePath, Cors cors) {

    public AppProperties {
        apiBasePath = apiBasePath == null || apiBasePath.isBlank() ? "/api/v1" : apiBasePath;
        cors = cors == null ? new Cors(new ArrayList<>()) : cors;
    }

    public record Cors(List<String> allowedOrigins) {

        public Cors {
            allowedOrigins = allowedOrigins == null ? new ArrayList<>() : allowedOrigins;
        }
    }
}
