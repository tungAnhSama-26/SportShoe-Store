package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.math.BigDecimal;

public record ThongTinGiaoHangTaiQuayResponse(
        Boolean giaoHang,
        String tenNguoiNhan,
        String soDienThoaiNguoiNhan,
        String diaChiGiaoHang,
        BigDecimal phiVanChuyen,
        String donViVanChuyen
) {
}
