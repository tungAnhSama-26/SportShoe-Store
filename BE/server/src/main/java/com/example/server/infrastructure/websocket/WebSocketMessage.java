package com.example.server.infrastructure.websocket;

import java.time.Instant;

public record WebSocketMessage<T>(
        String type,
        String destination,
        T payload,
        Instant timestamp
) {

    public static <T> WebSocketMessage<T> of(String type, String destination, T payload) {
        return new WebSocketMessage<>(type, destination, payload, Instant.now());
    }
}
