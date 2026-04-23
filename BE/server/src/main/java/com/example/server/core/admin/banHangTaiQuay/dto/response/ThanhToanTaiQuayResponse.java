package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ThanhToanTaiQuayResponse(
        Integer hoaDonId,
        String maHoaDon,
        UUID khachHangId,
        BigDecimal tongTienHang,
        BigDecimal tienGiam,
        BigDecimal tongTien,
        BigDecimal tienKhachDua,
        BigDecimal tienThua,
        Integer hinhThucThanhToan,
        String tenKhachHang,
        String soDienThoai,
        ThongTinGiaoHangTaiQuayResponse thongTinGiaoHang,
        ThongTinPhieuGiamGiaHoaDonResponse phieuGiamGia,
        Instant ngayThanhToan
) {
}
