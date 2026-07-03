package com.example.server.infrastructure.websocket;

import com.example.server.infrastructure.sse.SseNotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final SseNotificationService sseNotificationService;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate, SseNotificationService sseNotificationService) {
        this.messagingTemplate = messagingTemplate;
        this.sseNotificationService = sseNotificationService;
    }

    public <T> void sendToTopic(String topic, String type, T payload) {
        messagingTemplate.convertAndSend(topic, WebSocketMessage.of(type, topic, payload));
        sseNotificationService.sendToTopic(topic, type, payload);
    }

    public <T> void sendToUserQueue(String userIdentifier, String queue, String type, T payload) {
        messagingTemplate.convertAndSendToUser(userIdentifier, queue, WebSocketMessage.of(type, queue, payload));
    }
}
