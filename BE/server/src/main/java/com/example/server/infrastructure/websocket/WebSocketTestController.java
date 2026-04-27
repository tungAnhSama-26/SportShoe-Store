package com.example.server.infrastructure.websocket;

import com.example.server.infrastructure.AppProperties;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/ws")
public class WebSocketTestController {

    private final WebSocketNotificationService webSocketNotificationService;
    private final AppProperties appProperties;

    public WebSocketTestController(
            WebSocketNotificationService webSocketNotificationService,
            AppProperties appProperties
    ) {
        this.webSocketNotificationService = webSocketNotificationService;
        this.appProperties = appProperties;
    }

    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Map<String, String>>> broadcast(@Valid @RequestBody BroadcastRequest request) {
        String destination = buildTopic(request.topic());
        webSocketNotificationService.sendToTopic(destination, request.type(), request.payload());
        return ResponseEntity.ok(ApiResponse.success("Broadcasted websocket message", Map.of(
                "destination", destination,
                "type", request.type()
        )));
    }

    private String buildTopic(String topic) {
        String normalizedTopic = topic.startsWith("/") ? topic : "/" + topic;
        return appProperties.websocket().topicPrefix() + normalizedTopic;
    }

    public record BroadcastRequest(
            @NotBlank String topic,
            @NotBlank String type,
            Object payload
    ) {
    }
}
