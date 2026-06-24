package com.example.server.core.client.chatbot.dto;

import java.time.Instant;

public record ChatbotSessionDto(
        Integer id,
        String tenKhachHang,
        String soDienThoai,
        Integer trangThai,
        Instant ngayTao,
        Instant ngayCapNhat
) {
}
