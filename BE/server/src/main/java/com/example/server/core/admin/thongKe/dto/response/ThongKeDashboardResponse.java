package com.example.server.core.admin.thongKe.dto.response;

import java.util.List;

public record ThongKeDashboardResponse(
        ThongKeBoLocDaApDungResponse boLoc,
        ThongKeTongQuanResponse tongQuan,
        List<ThuongHieuThongKeFilterResponse> thuongHieus,
        List<ThongKeGiaTriTheoKyResponse> bieuDoBanHang,
        List<ThongKeThuongHieuResponse> bieuDoThuongHieu,
        List<ThongKeSanPhamResponse> sanPhams
) {
}
