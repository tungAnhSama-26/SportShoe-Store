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
        Instant ngayCapNhat,
        Instant ngayGiao,
        boolean daThanhToan,
        String hinhThucThanhToan
) {
    public DonHangTomTatResponse(
            Integer id,
            String ma,
            Instant ngayLap,
            Integer trangThai,
            String trangThaiText,
            int soLuong,
            BigDecimal tongThanhToan,
            List<DongSanPhamTomTat> sanPhams,
            Instant ngayCapNhat,
            Instant ngayGiao
    ) {
        this(id, ma, ngayLap, trangThai, trangThaiText, soLuong, tongThanhToan, sanPhams, ngayCapNhat, ngayGiao, false, null);
    }
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
