package com.example.server.core.client.chatbot.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(
        Integer id,
        String ma,
        String ten,
        String moTa,
        String hinhAnh,
        BigDecimal giaBan,
        List<String> mauSacs,
        List<String> kichCos,
        Long soLuongTon,
        Long daBan
) {
}
