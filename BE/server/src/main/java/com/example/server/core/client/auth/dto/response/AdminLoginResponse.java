package com.example.server.core.client.auth.dto.response;

import java.util.UUID;

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
        Integer trangThai,
        String faceDescriptor
) {
}
