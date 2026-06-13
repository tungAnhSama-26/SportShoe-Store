package com.example.server.core.client.thongke.controller;

import com.example.server.core.client.thongke.dto.ThongKeTrangChuResponse;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.repository.DanhGiaRepository;
import com.example.server.repository.GiayRepository;
import com.example.server.repository.KhachHangRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Số liệu thật cho banner trang chủ (public): khách hàng, sản phẩm đang bán, đánh giá. */
@RestController
@RequestMapping("/api/v1/client/thong-ke")
public class ClientThongKeController {

    private final KhachHangRepository khachHangRepository;
    private final GiayRepository giayRepository;
    private final DanhGiaRepository danhGiaRepository;

    public ClientThongKeController(
            KhachHangRepository khachHangRepository,
            GiayRepository giayRepository,
            DanhGiaRepository danhGiaRepository
    ) {
        this.khachHangRepository = khachHangRepository;
        this.giayRepository = giayRepository;
        this.danhGiaRepository = danhGiaRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ThongKeTrangChuResponse>> thongKe() {
        long soKhachHang = khachHangRepository.countByTrangThai(1);
        long soSanPham = giayRepository.countByTrangThai(1);

        double diemTrungBinh = 0d;
        long soDanhGia = 0L;
        List<Object[]> tongQuan = danhGiaRepository.thongKeTongQuan();
        if (!tongQuan.isEmpty() && tongQuan.get(0)[0] != null) {
            diemTrungBinh = Math.round(((Number) tongQuan.get(0)[0]).doubleValue() * 10) / 10.0;
            soDanhGia = ((Number) tongQuan.get(0)[1]).longValue();
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Thống kê trang chủ",
                new ThongKeTrangChuResponse(soKhachHang, soSanPham, diemTrungBinh, soDanhGia)));
    }
}
