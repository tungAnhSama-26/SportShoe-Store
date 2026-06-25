package com.example.server.core.client.chatbot.dto;

public record ClientChatResponse(
        Integer sessionId,
        String response,
        Integer trangThai
) {
}
