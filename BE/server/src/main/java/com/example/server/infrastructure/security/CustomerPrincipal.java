package com.example.server.infrastructure.security;

import java.util.UUID;

public record CustomerPrincipal(
        UUID id,
        String tenDangNhap,
        String hoTen,
        String role
) {
}
