package com.example.server.infrastructure.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public <T> void sendToTopic(String topic, String type, T payload) {
        messagingTemplate.convertAndSend(topic, WebSocketMessage.of(type, topic, payload));
    }

    public <T> void sendToUserQueue(String userIdentifier, String queue, String type, T payload) {
        messagingTemplate.convertAndSendToUser(userIdentifier, queue, WebSocketMessage.of(type, queue, payload));
    }
}
