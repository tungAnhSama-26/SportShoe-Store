package com.example.server.infrastructure.security;

import java.util.UUID;

public record AdminPrincipal(
        UUID id,
        String ma,
        String tenDangNhap,
        String hoTen,
        Integer vaiTro,
        String role
) {
}
