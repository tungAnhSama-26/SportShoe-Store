package com.example.server.infrastructure.security;

import java.util.UUID;

public record ParsedSubjectToken(
        UUID id,
        String role
) {
}
