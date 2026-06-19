package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.util.UUID;

public record KhachHangTaiQuayResponse(
        UUID id,
        String hoTen,
        String sdt,
        String email,
        String diaChiMacDinh
) {
}
