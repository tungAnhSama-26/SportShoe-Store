package com.example.server.core.admin.banHangTaiQuay.dto.request;

import com.example.server.infrastructure.address.DiaChiHaiCapRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;

public record ThongTinGiaoHangTaiQuayRequest(
        Boolean giaoHang,
        String tenNguoiNhan,
        String soDienThoaiNguoiNhan,
        String email,
        @Valid DiaChiHaiCapRequest diaChiGiaoHang,
        BigDecimal phiVanChuyen,
        String donViVanChuyen
) {
    public ThongTinGiaoHangTaiQuayRequest(
            Boolean giaoHang,
            String tenNguoiNhan,
            String soDienThoaiNguoiNhan,
            @Valid DiaChiHaiCapRequest diaChiGiaoHang,
            BigDecimal phiVanChuyen,
            String donViVanChuyen
    ) {
        this(giaoHang, tenNguoiNhan, soDienThoaiNguoiNhan, null, diaChiGiaoHang, phiVanChuyen, donViVanChuyen);
    }
}
