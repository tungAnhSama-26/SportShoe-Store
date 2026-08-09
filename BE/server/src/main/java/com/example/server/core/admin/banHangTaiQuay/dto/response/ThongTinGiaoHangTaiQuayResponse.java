package com.example.server.core.admin.banHangTaiQuay.dto.response;

import com.example.server.infrastructure.address.DiaChiHaiCapResponse;
import java.math.BigDecimal;

public record ThongTinGiaoHangTaiQuayResponse(
        Boolean giaoHang,
        String tenNguoiNhan,
        String soDienThoaiNguoiNhan,
        DiaChiHaiCapResponse diaChiGiaoHang,
        BigDecimal phiVanChuyen,
        String donViVanChuyen
) {
}
