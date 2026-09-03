package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.math.BigDecimal;

public record ThongTinPhieuGiamGiaHoaDonResponse(
        Integer id,
        String ma,
        String ten,
        Integer loai,
        BigDecimal giaTri,
        BigDecimal giaTriToiThieu,
        BigDecimal giamToiDa,
        BigDecimal soTienGiam
) {
    public ThongTinPhieuGiamGiaHoaDonResponse(String ma, String ten, BigDecimal soTienGiam) {
        this(null, ma, ten, null, null, null, null, soTienGiam);
    }
}

