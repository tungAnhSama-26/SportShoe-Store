package com.example.server.infrastructure.websocket;

import com.example.server.infrastructure.AppProperties;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AppProperties appProperties;

    public WebSocketConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(
                appProperties.websocket().topicPrefix(),
                appProperties.websocket().queuePrefix()
        );
        registry.setApplicationDestinationPrefixes(appProperties.websocket().applicationPrefix());
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] allowedOriginPatterns = resolveAllowedOriginPatterns();
        registry.addEndpoint(appProperties.websocket().endpoint())
                .setAllowedOriginPatterns(allowedOriginPatterns);
        registry.addEndpoint(appProperties.websocket().endpoint())
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .withSockJS();
    }

    private String[] resolveAllowedOriginPatterns() {
        List<String> websocketOrigins = appProperties.websocket().allowedOrigins();
        if (websocketOrigins != null && !websocketOrigins.isEmpty()) {
            return websocketOrigins.toArray(String[]::new);
        }
        List<String> corsOrigins = appProperties.cors().allowedOrigins();
        if (corsOrigins != null && !corsOrigins.isEmpty()) {
            return corsOrigins.toArray(String[]::new);
        }
        return new String[]{
                "http://localhost:*",
                "http://127.0.0.1:*"
        };
    }
}
