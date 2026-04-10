package com.example.server.core.admin.banHangTaiQuay;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record KhachHangTaiQuayResponse(
        UUID id,
        String hoTen,
        String sdt,
        String email
) {
}

record SanPhamTaiQuayResponse(
        Integer chiTietId,
        String maSanPham,
        String tenSanPham,
        String sku,
        String maBienThe,
        Integer soLuongTon,
        BigDecimal giaBan,
        String loaiGiay,
        String thuongHieu,
        String deGiay,
        String coGiay,
        String congNgheDem,
        String mauSac,
        String kichCo,
        String trongLuong
) {
}

record TaoHoaDonChoRequest(
        UUID khachHangId,
        String tenKhachHang,
        String soDienThoai,
        @NotEmpty(message = "Danh sach san pham khong duoc de trong")
        List<TaoHoaDonChoItemRequest> items
) {
}

record TaoHoaDonChoItemRequest(
        @NotNull(message = "Chi tiet san pham khong duoc de trong")
        Integer chiTietId,
        @NotNull(message = "So luong khong duoc de trong")
        @Min(value = 1, message = "So luong phai lon hon 0")
        Integer soLuong
) {
}

record HoaDonChoTomTatResponse(
        Integer id,
        String ma,
        String tenKhachHang,
        String soDienThoai,
        Integer tongSanPham,
        BigDecimal tongTien,
        Instant ngayTao
) {
}

record HoaDonChoChiTietResponse(
        Integer id,
        String ma,
        String tenKhachHang,
        String soDienThoai,
        BigDecimal tongTien,
        Instant ngayTao,
        List<HoaDonChoDongSanPhamResponse> items
) {
}

record HoaDonChoDongSanPhamResponse(
        Integer chiTietId,
        String maSanPham,
        String tenSanPham,
        Integer soLuong,
        BigDecimal giaBan,
        BigDecimal thanhTien
) {
}

record ThanhToanTaiQuayRequest(
        Integer hoaDonId,
        UUID khachHangId,
        String tenKhachHang,
        String soDienThoai,
        @NotNull(message = "Hinh thuc thanh toan khong duoc de trong")
        Integer hinhThucThanhToan,
        BigDecimal tienKhachDua,
        String ghiChu,
        List<TaoHoaDonChoItemRequest> items
) {
}

record ThanhToanTaiQuayResponse(
        Integer hoaDonId,
        String maHoaDon,
        BigDecimal tongTien,
        BigDecimal tienKhachDua,
        BigDecimal tienThua,
        Integer hinhThucThanhToan,
        String tenKhachHang,
        String soDienThoai,
        Instant ngayThanhToan
) {
}
