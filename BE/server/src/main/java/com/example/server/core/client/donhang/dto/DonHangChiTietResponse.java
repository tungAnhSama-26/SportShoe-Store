package com.example.server.core.client.donhang.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Chi tiết đơn hàng: thông tin nhận hàng, danh sách sản phẩm và phân tích giá
 * (tạm tính theo giá gốc, giảm từ đợt giảm giá, giảm từ voucher, tổng thanh toán).
 */
public record DonHangChiTietResponse(
        Integer id,
        String ma,
        Instant ngayLap,
        Integer trangThai,
        String trangThaiText,
        boolean daNhanHang,
        String tenNguoiNhan,
        String sdtNguoiNhan,
        String diaChiGiaoHang,
        String maPhieuGiamGia,
        List<DongSanPham> sanPhams,
        BigDecimal tamTinh,
        BigDecimal giamDotGiamGia,
        BigDecimal giamVoucher,
        BigDecimal phiVanChuyen,
        BigDecimal tongThanhToan,
        Instant ngayCapNhat,
        List<LichSuTrangThai> lichSuTrangThai,
        Integer phieuTraHangId,
        Integer trangThaiTraHang,
        String trangThaiTraHangText,
        List<LichSuTraHang> lichSuTraHang
) {
    public record LichSuTrangThai(
            String trangThai,
            Instant ngayTao,
            String maNhanVien
    ) {}

    public record LichSuTraHang(
            Integer trangThai,
            Instant ngayTao
    ) {}

    public record DongSanPham(
            Integer hoaDonChiTietId,
            Integer giayId,
            String tenSanPham,
            String mauSac,
            String kichCo,
            String hinhAnh,
            BigDecimal giaNiemYet,
            BigDecimal giaDonVi,
            Integer soLuong,
            BigDecimal thanhTien,
            boolean daDanhGia,
            Integer soSao,
            String noiDungDanhGia
    ) {}
}
