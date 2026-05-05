package com.example.server.infrastructure.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String apiBasePath, Cors cors, Pagination pagination, WebSocket websocket) {

    public AppProperties {
        apiBasePath = apiBasePath == null || apiBasePath.isBlank() ? "/api/v1" : apiBasePath;
        cors = cors == null ? new Cors(new ArrayList<>()) : cors;
        pagination = pagination == null ? new Pagination(0, 10, 100) : pagination;
        websocket = websocket == null ? new WebSocket("/ws", "/topic", "/queue", "/app", new ArrayList<>()) : websocket;
    }

    public record Cors(List<String> allowedOrigins) {

        public Cors {
            allowedOrigins = allowedOrigins == null ? new ArrayList<>() : allowedOrigins;
        }
    }

    public record Pagination(int defaultPage, int defaultSize, int maxSize) {

        public Pagination {
            defaultPage = defaultPage < 0 ? 0 : defaultPage;
            defaultSize = defaultSize <= 0 ? 10 : defaultSize;
            maxSize = maxSize <= 0 ? 100 : maxSize;
        }
    }

    public record WebSocket(
            String endpoint,
            String topicPrefix,
            String queuePrefix,
            String applicationPrefix,
            List<String> allowedOrigins
    ) {

        public WebSocket {
            endpoint = endpoint == null || endpoint.isBlank() ? "/ws" : endpoint;
            topicPrefix = topicPrefix == null || topicPrefix.isBlank() ? "/topic" : topicPrefix;
            queuePrefix = queuePrefix == null || queuePrefix.isBlank() ? "/queue" : queuePrefix;
            applicationPrefix = applicationPrefix == null || applicationPrefix.isBlank() ? "/app" : applicationPrefix;
            allowedOrigins = allowedOrigins == null ? new ArrayList<>() : allowedOrigins;
        }
    }
}
