package com.example.server.core.admin.quanlytrahang.controller;

import com.example.server.core.admin.quanlytrahang.dto.request.CapNhatVanChuyenTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.DuyetPhieuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.GhiChuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.HoanTienTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.KiemTraPhieuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.TaoPhieuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.TuChoiTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.response.TraHangResponse;
import com.example.server.core.admin.quanlytrahang.service.TraHangService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.security.AdminPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tra-hang")
public class TraHangController {

    private final TraHangService traHangService;

    public TraHangController(TraHangService traHangService) {
        this.traHangService = traHangService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TraHangResponse>>> layDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách phiếu trả hàng thành công",
                traHangService.layDanhSach(keyword, trangThai)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TraHangResponse>> layChiTiet(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết phiếu trả hàng thành công",
                traHangService.layChiTiet(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TraHangResponse>> taoPhieu(
            @Valid @RequestBody TaoPhieuTraHangRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo phiếu trả hàng thành công",
                traHangService.taoPhieu(request, currentPrincipal().id())
        ));
    }

    @PatchMapping("/{id}/duyet")
    public ResponseEntity<ApiResponse<TraHangResponse>> duyet(
            @PathVariable Integer id,
            @Valid @RequestBody DuyetPhieuTraHangRequest request
    ) {
        return ok("Duyệt phiếu trả hàng thành công",
                traHangService.duyetPhieu(id, request, currentPrincipal().id()));
    }

    @PatchMapping("/{id}/xac-nhan-gui-hang")
    public ResponseEntity<ApiResponse<TraHangResponse>> xacNhanGuiHang(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatVanChuyenTraHangRequest request
    ) {
        return ok("Xác nhận gửi hàng thành công",
                traHangService.xacNhanKhachGuiHang(id, request, currentPrincipal().id()));
    }

    @PatchMapping("/{id}/xac-nhan-da-nhan")
    public ResponseEntity<ApiResponse<TraHangResponse>> xacNhanDaNhan(
            @PathVariable Integer id,
            @Valid @RequestBody GhiChuTraHangRequest request
    ) {
        return ok("Xác nhận đã nhận hàng trả thành công",
                traHangService.xacNhanDaNhanHang(id, request, currentPrincipal().id()));
    }

    @PatchMapping("/{id}/hoan-hang-that-bai")
    public ResponseEntity<ApiResponse<TraHangResponse>> danhDauHoanHangThatBai(
            @PathVariable Integer id,
            @Valid @RequestBody GhiChuTraHangRequest request
    ) {
        return ok("Đã ghi nhận hoàn hàng thất bại",
                traHangService.danhDauHoanHangThatBai(id, request, currentPrincipal().id()));
    }

    @PatchMapping("/{id}/bat-dau-kiem-tra")
    public ResponseEntity<ApiResponse<TraHangResponse>> batDauKiemTra(
            @PathVariable Integer id,
            @Valid @RequestBody GhiChuTraHangRequest request
    ) {
        return ok("Bắt đầu kiểm tra hàng trả thành công",
                traHangService.batDauKiemTra(id, request, currentPrincipal().id()));
    }

    @PatchMapping("/{id}/kiem-tra")
    public ResponseEntity<ApiResponse<TraHangResponse>> kiemTra(
            @PathVariable Integer id,
            @Valid @RequestBody KiemTraPhieuTraHangRequest request
    ) {
        return ok("Cập nhật kết quả kiểm tra thành công",
                traHangService.kiemTraHang(id, request, currentPrincipal().id()));
    }

    @PatchMapping("/{id}/tu-choi")
    public ResponseEntity<ApiResponse<TraHangResponse>> tuChoi(
            @PathVariable Integer id,
            @Valid @RequestBody TuChoiTraHangRequest request
    ) {
        return ok("Từ chối phiếu trả hàng thành công",
                traHangService.tuChoi(id, request, currentPrincipal().id()));
    }

    @PatchMapping("/{id}/huy")
    public ResponseEntity<ApiResponse<TraHangResponse>> huy(
            @PathVariable Integer id,
            @Valid @RequestBody GhiChuTraHangRequest request
    ) {
        return ok("Hủy phiếu trả hàng thành công",
                traHangService.huy(id, request, currentPrincipal().id()));
    }

    @PostMapping("/{id}/hoan-tien")
    public ResponseEntity<ApiResponse<TraHangResponse>> hoanTien(
            @PathVariable Integer id,
            @Valid @RequestBody HoanTienTraHangRequest request
    ) {
        return ok("Hoàn tiền trả hàng thành công",
                traHangService.hoanTien(id, request, currentPrincipal().id()));
    }

    private ResponseEntity<ApiResponse<TraHangResponse>> ok(
            String message,
            TraHangResponse data
    ) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    private AdminPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof AdminPrincipal adminPrincipal) {
            return adminPrincipal;
        }
        throw new BusinessException("Vui lòng đăng nhập hệ thống quản trị");
    }
}
