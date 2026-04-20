package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record HoaDonChoTomTatResponse(
        Integer id,
        String ma,
        UUID khachHangId,
        String tenKhachHang,
        String soDienThoai,
        Integer tongSanPham,
        BigDecimal tongTienHang,
        BigDecimal tienGiam,
        BigDecimal tongTien,
        ThongTinPhieuGiamGiaHoaDonResponse phieuGiamGia,
        Instant ngayTao
) {
}
