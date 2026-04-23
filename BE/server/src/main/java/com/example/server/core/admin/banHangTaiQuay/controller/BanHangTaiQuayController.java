package com.example.server.core.admin.banHangTaiQuay.controller;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ApDungPhieuGiamGiaRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.ThanhToanTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TinhPhiVanChuyenTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoChiTietResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoTomTatResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.KhachHangTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.PhieuGiamGiaTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.SanPhamTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThanhToanTaiQuayResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.banHangTaiQuay.service.BanHangTaiQuayService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ban-hang-tai-quay")
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

    @GetMapping("/phieu-giam-gia")
    public ResponseEntity<ApiResponse<List<PhieuGiamGiaTaiQuayResponse>>> timPhieuGiamGia(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "hoaDonId", required = false) Integer hoaDonId,
            @RequestParam(name = "khachHangId", required = false) UUID khachHangId,
            @RequestParam(name = "tongTienHang", required = false) BigDecimal tongTienHang
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tim kiem phieu giam gia thanh cong",
                banHangTaiQuayService.timPhieuGiamGia(keyword, hoaDonId, khachHangId, tongTienHang)
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

    @PostMapping("/phieu-giam-gia/ap-dung")
    public ResponseEntity<ApiResponse<PhieuGiamGiaTaiQuayResponse>> apDungPhieuGiamGia(
            @Valid @RequestBody ApDungPhieuGiamGiaRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Ap dung phieu giam gia thanh cong",
                banHangTaiQuayService.apDungPhieuGiamGia(request)
        ));
    }

    @PostMapping("/phi-van-chuyen/ghn")
    public ResponseEntity<ApiResponse<TinhPhiVanChuyenGhnResponse>> tinhPhiVanChuyenGhn(
            @Valid @RequestBody TinhPhiVanChuyenTaiQuayRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tinh phi van chuyen GHN thanh cong",
                banHangTaiQuayService.tinhPhiVanChuyenGhn(request)
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
