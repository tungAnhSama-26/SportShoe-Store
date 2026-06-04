package com.example.server.infrastructure.security;

public record ParsedAdminToken(
        AdminPrincipal principal,
        long authVersion
) {
}
