package com.example.server.core.client.chatbot.dto;

public record ClientChatRequest(
        Integer sessionId,
        String message,
        String customerName,
        String phoneNumber
) {
}
