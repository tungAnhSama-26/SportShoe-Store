package com.example.server.infrastructure.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String type,
        String url,
        String description,
        boolean primary
) {
}
