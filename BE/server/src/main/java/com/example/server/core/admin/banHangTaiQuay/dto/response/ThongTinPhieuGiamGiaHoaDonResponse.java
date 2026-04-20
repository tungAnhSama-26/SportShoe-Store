package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.math.BigDecimal;

public record ThongTinPhieuGiamGiaHoaDonResponse(
        String ma,
        String ten,
        BigDecimal soTienGiam
) {
}
