package com.example.server.core.admin.banHangTaiQuay;

import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("${app.api.base-path}/admin/ban-hang-tai-quay")
public class BanHangTaiQuayController {

    private final BanHangTaiQuayService banHangTaiQuayService;

    public BanHangTaiQuayController(BanHangTaiQuayService banHangTaiQuayService) {
        this.banHangTaiQuayService = banHangTaiQuayService;
    }

    @GetMapping("/khach-hang")
    public ResponseEntity<ApiResponse<List<KhachHangTaiQuayResponse>>> timKhachHang(
            @RequestParam(name = "phone", required = false) String phone
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tim kiem khach hang thanh cong",
                banHangTaiQuayService.timKhachHangTheoTuKhoa(phone)
        ));
    }

    @GetMapping("/san-pham")
    public ResponseEntity<ApiResponse<List<SanPhamTaiQuayResponse>>> timSanPham(
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tim kiem san pham thanh cong",
                banHangTaiQuayService.timSanPham(keyword)
        ));
    }

    @GetMapping("/hoa-don-cho")
    public ResponseEntity<ApiResponse<List<HoaDonChoTomTatResponse>>> layDanhSachHoaDonCho() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach hoa don cho thanh cong",
                banHangTaiQuayService.layDanhSachHoaDonCho()
        ));
    }

    @GetMapping("/hoa-don-cho/{hoaDonId}")
    public ResponseEntity<ApiResponse<HoaDonChoChiTietResponse>> layChiTietHoaDonCho(
            @PathVariable Integer hoaDonId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay chi tiet hoa don cho thanh cong",
                banHangTaiQuayService.layChiTietHoaDonCho(hoaDonId)
        ));
    }

    @PostMapping("/hoa-don-cho")
    public ResponseEntity<ApiResponse<HoaDonChoChiTietResponse>> taoHoaDonCho(
            @Valid @RequestBody TaoHoaDonChoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao hoa don cho thanh cong",
                banHangTaiQuayService.taoHoaDonCho(request)
        ));
    }

    @PostMapping("/thanh-toan")
    public ResponseEntity<ApiResponse<ThanhToanTaiQuayResponse>> thanhToanTaiQuay(
            @Valid @RequestBody ThanhToanTaiQuayRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Thanh toan tai quay thanh cong",
                banHangTaiQuayService.thanhToanTaiQuay(request)
        ));
    }

    @PatchMapping("/hoa-don-cho/{hoaDonId}/huy")
    public ResponseEntity<ApiResponse<Void>> huyHoaDonCho(@PathVariable Integer hoaDonId) {
        banHangTaiQuayService.huyHoaDonCho(hoaDonId);
        return ResponseEntity.ok(ApiResponse.success("Huy hoa don cho thanh cong", null));
    }
}
