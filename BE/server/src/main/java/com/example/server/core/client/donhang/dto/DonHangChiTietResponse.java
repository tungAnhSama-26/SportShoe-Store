package com.example.server.core.client.donhang.dto;

import com.example.server.infrastructure.address.DiaChiHaiCapResponse;
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
        DiaChiHaiCapResponse diaChiGiaoHang,
        String maPhieuGiamGia,
        List<DongSanPham> sanPhams,
        BigDecimal tamTinh,
        BigDecimal giamDotGiamGia,
        BigDecimal giamVoucher,
        BigDecimal phiVanChuyen,
        BigDecimal tongThanhToan,
        Instant ngayCapNhat,
        List<LichSuTrangThai> lichSuTrangThai,
        /** "CHUYEN_KHOAN" hoặc "COD" - để FE biết ràng buộc chỉnh sửa. */
        String hinhThucThanhToan,
        /** Khách được phép hủy đơn (chỉ khi đang chờ xác nhận). */
        boolean coTheHuy,
        /** Khách được sửa thông tin giao hàng (COD + chờ xác nhận). */
        boolean coTheCapNhatGiaoHang,
        /** Khách được sửa số lượng sản phẩm (COD + chờ xác nhận). */
        boolean coTheCapNhatSoLuong,
        Instant ngayGiao,
        Integer soLanSuaDiaChi
) {
    public record LichSuTrangThai(
            String trangThai,
            Instant ngayTao,
            String maNhanVien,
            String ghiChu
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
            String noiDungDanhGia,
            String phanHoiDanhGia,
            Instant ngayPhanHoiDanhGia
    ) {}
}
