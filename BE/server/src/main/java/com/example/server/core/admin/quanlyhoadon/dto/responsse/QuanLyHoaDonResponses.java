package com.example.server.core.admin.quanlyhoadon.dto.responsse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class QuanLyHoaDonResponses {

    private QuanLyHoaDonResponses() {
    }

    public record HoaDonSummaryResponse(
            Integer id,
            String maHoaDon,
            String tenKhachHang,
            String soDienThoai,
            String maNhanVien,
            BigDecimal tongTien,
            Instant ngayTao,
            String loaiDon,
            String trangThai,
            String maPhieuGiamGia,
            String emailKhachHang
    ) {
    }

    public record HoaDonPaymentHistoryResponse(
            Integer id,
            String maGiaoDich,
            String loaiGiaoDich,
            String phuongThucThanhToan,
            String trangThaiThanhToan,
            Instant thoiGian,
            BigDecimal tongTien,
            String ghiChu
    ) {
    }

    public record HoaDonHistoryResponse(
            Integer id,
            String maNhanVien,
            String tenNhanVien,
            String trangThai,
            Instant ngayTao,
            String ghiChu
    ) {
    }

    public record HoaDonProductResponse(
            Integer id,
            String tenSanPham,
            String phanLoai,
            String mauSac,
            String kichCo,
            Integer soLuong,
            BigDecimal donGia,
            BigDecimal thanhTien,
            String hinhAnh
    ) {
    }

    public record HoaDonDetailResponse(
            Integer id,
            String maHoaDon,
            String tenKhachHang,
            String tenNhanVien,
            String maNhanVien,
            String nguoiTao,
            BigDecimal tongTien,
            Instant ngayTao,
            String loaiDon,
            String trangThai,
            String soDienThoai,
            String email,
            String diaChi,
            String ghiChu,
            BigDecimal phiVanChuyen,
            String voucher,
            BigDecimal giamGia,
            Integer loaiGiamGia,
            BigDecimal giaTriGiamGia,
            String donViVanChuyen,
            String maVanDon,
            List<HoaDonPaymentHistoryResponse> lichSuThanhToan,
            List<HoaDonProductResponse> sanPham,
            List<HoaDonHistoryResponse> lichSuHoaDon
    ) {
    }
}
