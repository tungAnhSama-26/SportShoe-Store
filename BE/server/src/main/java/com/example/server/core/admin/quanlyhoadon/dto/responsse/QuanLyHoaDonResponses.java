package com.example.server.core.admin.quanlyhoadon.dto.responsse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
            String emailKhachHang,
            Integer phieuTraHangId,
            Integer trangThaiPhieuTraHang,
            String trangThaiPhieuTraHangText,
            String phuongThucThanhToan
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
            Integer giayChiTietId,
            String maBienThe,
            String tenSanPham,
            String phanLoai,
            String mauSac,
            String kichCo,
            Integer soLuong,
            BigDecimal donGia,
            BigDecimal giaBanChiTiet,
            BigDecimal thanhTien,
            String hinhAnh,
            String tenDotGiamGia,
            BigDecimal giaTriGiamDotGiamGia
    ) {
    }


    public record HoaDonDetailResponse(
            Integer id,
            UUID khachHangId,
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
            String lyDoGiaoHangThatBai,
            List<HoaDonPaymentHistoryResponse> lichSuThanhToan,
            List<HoaDonProductResponse> sanPham,
            List<HoaDonHistoryResponse> lichSuHoaDon,
            Integer phieuTraHangId,
            String maPhieuTraHang,
            Integer trangThaiPhieuTraHang,
            String trangThaiPhieuTraHangText
    ) {
    }
}
