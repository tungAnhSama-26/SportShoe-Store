package com.example.server.core.client.donhang.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Một đơn hàng trong danh sách "Đơn hàng của bạn". */
public record DonHangTomTatResponse(
        Integer id,
        String ma,
        Instant ngayLap,
        Integer trangThai,
        String trangThaiText,
        int soLuong,
        BigDecimal tongThanhToan,
        List<DongSanPhamTomTat> sanPhams,
        Integer phieuTraHangId,
        Integer trangThaiTraHang,
        String trangThaiTraHangText,
        Instant ngayCapNhat
) {
    public record DongSanPhamTomTat(
            Integer hoaDonChiTietId,
            Integer giayChiTietId,
            String ten,
            String mauSac,
            String kichCo,
            String hinhAnh,
            BigDecimal giaNiemYet,
            BigDecimal giaDonVi,
            int soLuong,
            BigDecimal thanhTien
    ) {}
}
