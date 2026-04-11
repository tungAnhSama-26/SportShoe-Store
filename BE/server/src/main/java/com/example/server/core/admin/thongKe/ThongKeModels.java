package com.example.server.core.admin.thongKe;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

record ThongKeDashboardResponse(
        ThongKeBoLocDaApDungResponse boLoc,
        ThongKeTongQuanResponse tongQuan,
        List<ThuongHieuThongKeFilterResponse> thuongHieus,
        List<ThongKeGiaTriTheoKyResponse> bieuDoBanHang,
        List<ThongKeThuongHieuResponse> bieuDoThuongHieu,
        List<ThongKeSanPhamResponse> sanPhams
) {
}

record ThongKeBoLocDaApDungResponse(
        String kyThongKe,
        LocalDate tuNgay,
        LocalDate denNgay,
        Integer thuongHieuId,
        String keyword
) {
}

record ThongKeTongQuanResponse(
        BigDecimal tongDoanhThu,
        Long tongDonHang,
        Long sanPhamDaBan,
        Long khachMoi
) {
}

record ThuongHieuThongKeFilterResponse(
        Integer id,
        String ma,
        String ten
) {
}

record ThongKeGiaTriTheoKyResponse(
        String nhan,
        Long soLuongBan
) {
}

record ThongKeThuongHieuResponse(
        Integer thuongHieuId,
        String tenThuongHieu,
        Long tongTonKho
) {
}

record ThongKeSanPhamResponse(
        Integer stt,
        Integer sanPhamId,
        String maSanPham,
        String tenSanPham,
        String thuongHieu,
        Long daBan,
        BigDecimal doanhThu,
        Long tonKho
) {
}
