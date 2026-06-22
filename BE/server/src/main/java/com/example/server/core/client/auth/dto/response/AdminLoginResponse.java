package com.example.server.core.client.auth.dto.response;

import java.util.UUID;
import java.time.Instant;

public record AdminLoginResponse(
        String token,
        String tokenType,
        UUID id,
        String ma,
        String tenDangNhap,
        String hoTen,
        String email,
        Integer vaiTro,
        String tenVaiTro,
        String hinhAnh,
        Boolean batBuocDoiMatKhau,
        Instant hanDoiMatKhau,
        String faceDescriptor
) {
}
