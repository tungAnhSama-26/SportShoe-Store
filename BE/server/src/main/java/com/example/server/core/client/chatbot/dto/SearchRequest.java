package com.example.server.core.client.chatbot.dto;

public record SearchRequest(
        String keyword,
        String color,
        String category,
        String brand,
        String size,
        Boolean onSale
) {
}
