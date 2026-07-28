package com.example.server.core.admin.banHangTaiQuay.dto.request;

import jakarta.validation.constraints.NotNull;

public record DoiBienTheTaiQuayRequest(
        @NotNull(message = "ID biến thể mới không được để trống")
        Integer giayChiTietMoiId,
        Integer soLuong
) {
}
