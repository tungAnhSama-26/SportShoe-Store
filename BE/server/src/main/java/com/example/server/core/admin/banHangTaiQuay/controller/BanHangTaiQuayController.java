package com.example.server.core.admin.banHangTaiQuay.controller;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ApDungPhieuGiamGiaRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.DoiBienTheTaiQuayRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
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
                "Tìm kiếm khách hàng thành công",
                banHangTaiQuayService.timKhachHangTheoTuKhoa(phone)
        ));
    }

    @GetMapping("/san-pham")
    public ResponseEntity<ApiResponse<List<SanPhamTaiQuayResponse>>> timSanPham(
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tìm kiếm sản phẩm thành công",
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
                "Tìm kiếm phiếu giảm giá thành công",
                banHangTaiQuayService.timPhieuGiamGia(keyword, hoaDonId, khachHangId, tongTienHang)
        ));
    }

    @GetMapping("/hoa-don-cho")
    public ResponseEntity<ApiResponse<List<HoaDonChoTomTatResponse>>> layDanhSachHoaDonCho() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách hóa đơn chờ thành công",
                banHangTaiQuayService.layDanhSachHoaDonCho()
        ));
    }

    @GetMapping("/hoa-don-cho/{hoaDonId}")
    public ResponseEntity<ApiResponse<HoaDonChoChiTietResponse>> layChiTietHoaDonCho(
            @PathVariable Integer hoaDonId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết hóa đơn chờ thành công",
                banHangTaiQuayService.layChiTietHoaDonCho(hoaDonId)
        ));
    }

    @PostMapping("/phieu-giam-gia/ap-dung")
    public ResponseEntity<ApiResponse<PhieuGiamGiaTaiQuayResponse>> apDungPhieuGiamGia(
            @Valid @RequestBody ApDungPhieuGiamGiaRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Áp dụng phiếu giảm giá thành công",
                banHangTaiQuayService.apDungPhieuGiamGia(request)
        ));
    }

    @PostMapping("/hoa-don-cho")
    public ResponseEntity<ApiResponse<HoaDonChoChiTietResponse>> taoHoaDonCho(
            @Valid @RequestBody TaoHoaDonChoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo hóa đơn chờ thành công",
                banHangTaiQuayService.taoHoaDonCho(request)
        ));
    }

    @PatchMapping("/hoa-don-cho/{hoaDonId}")
    public ResponseEntity<ApiResponse<HoaDonChoChiTietResponse>> capNhatHoaDonCho(
            @PathVariable Integer hoaDonId,
            @Valid @RequestBody TaoHoaDonChoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật hóa đơn chờ thành công",
                banHangTaiQuayService.capNhatHoaDonCho(hoaDonId, request)
        ));
    }

    @PutMapping("/hoa-don-chi-tiet/{id}/doi-bien-the")
    public ResponseEntity<ApiResponse<HoaDonChoChiTietResponse>> doiBienThe(
            @PathVariable Integer id,
            @Valid @RequestBody DoiBienTheTaiQuayRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đổi biến thể sản phẩm thành công",
                banHangTaiQuayService.doiBienThe(id, request)
        ));
    }

    @PostMapping("/thanh-toan")
    public ResponseEntity<ApiResponse<ThanhToanTaiQuayResponse>> thanhToanTaiQuay(
            @Valid @RequestBody ThanhToanTaiQuayRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Thanh toán tại quầy thành công",
                banHangTaiQuayService.thanhToanTaiQuay(request)
        ));
    }

    @PatchMapping("/hoa-don-cho/{hoaDonId}/huy")
    public ResponseEntity<ApiResponse<Void>> huyHoaDonCho(@PathVariable Integer hoaDonId) {
        banHangTaiQuayService.huyHoaDonCho(hoaDonId);
        return ResponseEntity.ok(ApiResponse.success("Hủy hóa đơn chờ thành công", null));
    }

    @PostMapping("/phi-van-chuyen/ghn")
    public ResponseEntity<ApiResponse<TinhPhiVanChuyenGhnResponse>> tinhPhiVanChuyenGhn(
            @Valid @RequestBody TinhPhiVanChuyenTaiQuayRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tính phí vận chuyển GHN thành công",
                banHangTaiQuayService.tinhPhiVanChuyenGhn(request)
        ));
    }
}
