package com.example.server.infrastructure.config;

import com.example.server.infrastructure.websocket.WebSocketNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RealtimeUpdateInterceptor implements HandlerInterceptor {

    private final WebSocketNotificationService webSocketNotificationService;

    public RealtimeUpdateInterceptor(WebSocketNotificationService webSocketNotificationService) {
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex == null && response.getStatus() >= 200 && response.getStatus() < 300) {
            String method = request.getMethod();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || 
                "DELETE".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
                String uri = request.getRequestURI();
                if (uri.startsWith("/api/v1/admin/san-pham") || uri.startsWith("/api/v1/admin/san-pham-chi-tiet") || uri.startsWith("/api/v1/admin/dot-giam-gia")) {
                    webSocketNotificationService.sendToTopic("/topic/admin/san-pham", "PRODUCT_CHANGED", "Product Update");
                } else if (uri.startsWith("/api/v1/admin/danh-muc")) {
                    webSocketNotificationService.sendToTopic("/topic/admin/thuoc-tinh", "ATTRIBUTE_CHANGED", "Attribute Update");
                }
            }
        }
    }
}
