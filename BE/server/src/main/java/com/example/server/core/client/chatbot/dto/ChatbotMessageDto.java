package com.example.server.core.client.chatbot.dto;

import java.time.Instant;

public record ChatbotMessageDto(
        Integer id,
        String nguoiGui,
        String noiDung,
        Instant ngayTao,
        String maNhanVien
) {
}
