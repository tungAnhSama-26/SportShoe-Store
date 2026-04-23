package com.example.server.core.admin.banHangTaiQuay.dto.request;

import java.math.BigDecimal;

public record ThongTinGiaoHangTaiQuayRequest(
        Boolean giaoHang,
        String tenNguoiNhan,
        String soDienThoaiNguoiNhan,
        String diaChiGiaoHang,
        BigDecimal phiVanChuyen,
        String donViVanChuyen
) {
}
