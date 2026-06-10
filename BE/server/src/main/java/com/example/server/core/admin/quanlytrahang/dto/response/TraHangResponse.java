package com.example.server.core.admin.quanlytrahang.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TraHangResponse(
        Integer id,
        String ma,
        Integer hoaDonId,
        String maHoaDon,
        Integer trangThai,
        String tenTrangThai,
        Integer loaiYeuCau,
        String lyDoMa,
        String moTa,
        Integer hinhThucHoan,
        BigDecimal tongTienDuKien,
        BigDecimal tongTienThucTe,
        String maNhanVien,
        String tenKhachHang,
        String soDienThoaiKhachHang,
        String donViVanChuyen,
        String maVanDonHoan,
        String lyDoTuChoi,
        Instant ngayTao,
        Instant ngayCapNhat,
        List<ChiTietTraHangResponse> chiTiet,
        List<LichSuTraHangResponse> lichSu
) {
    public record ChiTietTraHangResponse(
            Integer id,
            Integer hoaDonChiTietId,
            Integer giayChiTietId,
            String tenSanPham,
            String maBienThe,
            String mauSac,
            String kichCo,
            Integer soLuongTra,
            Integer soLuongNhan,
            Integer soLuongChapNhan,
            Integer soLuongTuChoi,
            BigDecimal giaBan,
            BigDecimal thanhTien,
            BigDecimal soTienHoan,
            String tinhTrangSanPham,
            Boolean nhapLaiTonKho,
            String ghiChu
    ) {
    }

    public record LichSuTraHangResponse(
            Integer id,
            Integer trangThaiCu,
            Integer trangThaiMoi,
            String tenTrangThaiMoi,
            String hanhDong,
            String ghiChu,
            String maNhanVien,
            Instant ngayTao
    ) {
    }
}
