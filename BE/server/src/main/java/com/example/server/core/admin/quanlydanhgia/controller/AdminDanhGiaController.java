package com.example.server.core.admin.quanlydanhgia.controller;

import com.example.server.core.admin.quanlydanhgia.dto.AdminDanhGiaResponse;
import com.example.server.core.admin.quanlydanhgia.dto.PhanHoiRequest;
import com.example.server.core.admin.quanlydanhgia.dto.SanPhamCoDanhGiaResponse;
import com.example.server.core.admin.quanlydanhgia.service.AdminDanhGiaService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quản lý đánh giá phía admin: bảng sản phẩm, xem/lọc đánh giá (thời gian, ẩn/hiện),
 * xóa mềm, khôi phục, phản hồi và AI tổng hợp.
 */
@RestController
@RequestMapping("/api/v1/admin/danh-gia")
public class AdminDanhGiaController {

    private final AdminDanhGiaService service;

    public AdminDanhGiaController(AdminDanhGiaService service) {
        this.service = service;
    }

    /** Bảng sản phẩm có đánh giá (tìm theo tên/mã). */
    @GetMapping("/san-pham")
    public ResponseEntity<ApiResponse<List<SanPhamCoDanhGiaResponse>>> sanPham(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm có đánh giá thành công",
                service.laySanPhamCoDanhGia(keyword)));
    }

    /**
     * Đánh giá của một sản phẩm, lọc theo trạng thái (1=hiển thị, 0=đã ẩn, bỏ trống=tất cả)
     * và khoảng ngày tạo (yyyy-MM-dd). Đồng thời đánh dấu đã xem.
     */
    @GetMapping("/san-pham/{giayId}")
    public ResponseEntity<ApiResponse<List<AdminDanhGiaResponse>>> theoSanPham(
            @PathVariable Integer giayId,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {
        return ResponseEntity.ok(ApiResponse.success("Lấy đánh giá thành công",
                service.layTheoSanPham(giayId, trangThai, tuNgay, denNgay)));
    }

    /** Toàn bộ đánh giá của shop (kèm thông tin sản phẩm), cùng bộ lọc như trên. */
    @GetMapping("/tat-ca")
    public ResponseEntity<ApiResponse<List<AdminDanhGiaResponse>>> tatCa(
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {
        return ResponseEntity.ok(ApiResponse.success("Lấy tất cả đánh giá thành công",
                service.layTatCa(trangThai, tuNgay, denNgay)));
    }

    /** Tổng số đánh giá chưa xem (cho chuông thông báo). */
    @GetMapping("/chua-xem")
    public ResponseEntity<ApiResponse<Long>> chuaXem() {
        return ResponseEntity.ok(ApiResponse.success("OK", service.demChuaXem()));
    }

    /**
     * AI phân tích đánh giá: giayId != null -> 1 sản phẩm; bỏ trống -> toàn shop.
     * loai = tot | khong-tot | tong-the; thoiGian = hom-nay | tuan-nay | thang-nay | nam-nay.
     */
    @GetMapping("/ai/phan-tich")
    public ResponseEntity<ApiResponse<String>> phanTichAi(
            @RequestParam(required = false) Integer giayId,
            @RequestParam(defaultValue = "tong-the") String loai,
            @RequestParam(defaultValue = "hom-nay") String thoiGian) {
        return ResponseEntity.ok(ApiResponse.success("Phân tích thành công",
                service.phanTichAi(giayId, loai, thoiGian)));
    }

    /** Xóa mềm một đánh giá. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoa(@PathVariable Integer id) {
        service.xoaMem(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa đánh giá", null));
    }

    /** Khôi phục đánh giá đã ẩn (kể cả do AI ẩn nhầm). */
    @PostMapping("/{id}/khoi-phuc")
    public ResponseEntity<ApiResponse<AdminDanhGiaResponse>> khoiPhuc(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Đã khôi phục đánh giá", service.khoiPhuc(id)));
    }

    /** Phản hồi một đánh giá (1 lần/đánh giá). */
    @PostMapping("/{id}/phan-hoi")
    public ResponseEntity<ApiResponse<AdminDanhGiaResponse>> phanHoi(
            @PathVariable Integer id, @Valid @RequestBody PhanHoiRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đã gửi phản hồi", service.phanHoi(id, request.noiDung())));
    }
}
