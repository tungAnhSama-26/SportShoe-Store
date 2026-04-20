package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HoaDonChoChiTietResponse(
        Integer id,
        String ma,
        UUID khachHangId,
        String tenKhachHang,
        String soDienThoai,
        BigDecimal tongTienHang,
        BigDecimal tienGiam,
        BigDecimal tongTien,
        ThongTinPhieuGiamGiaHoaDonResponse phieuGiamGia,
        Instant ngayTao,
        List<HoaDonChoDongSanPhamResponse> items
) {
}
