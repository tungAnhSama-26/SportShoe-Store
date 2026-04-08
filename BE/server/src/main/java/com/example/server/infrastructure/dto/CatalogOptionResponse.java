package com.example.server.infrastructure.dto;

import java.util.UUID;

public record CatalogOptionResponse(
        UUID id,
        String code,
        String name,
        String extra
) {
}
