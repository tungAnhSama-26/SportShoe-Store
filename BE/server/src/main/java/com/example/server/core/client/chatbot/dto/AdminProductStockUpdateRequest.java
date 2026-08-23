package com.example.server.core.client.chatbot.dto;

public record AdminProductStockUpdateRequest(
        String productName,
        Integer sizeValue,
        String colorName,
        Integer newStock
) {
}
