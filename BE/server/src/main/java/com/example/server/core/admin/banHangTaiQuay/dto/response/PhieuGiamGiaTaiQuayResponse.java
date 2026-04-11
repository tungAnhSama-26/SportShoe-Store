package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.math.BigDecimal;

public record PhieuGiamGiaTaiQuayResponse(
        Integer id,
        String ma,
        String ten,
        Integer loai,
        BigDecimal giaTri,
        BigDecimal giaTriToiThieu,
        BigDecimal giamToiDa,
        BigDecimal soTienGiam,
        BigDecimal tongTienHang,
        BigDecimal tongTienSauGiam
) {
}
