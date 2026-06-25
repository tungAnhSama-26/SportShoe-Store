package com.example.server.core.admin.quanlydanhgia.controller;

import com.example.server.core.admin.quanlydanhgia.dto.AdminDanhGiaResponse;
import com.example.server.core.admin.quanlydanhgia.dto.PhanHoiRequest;
import com.example.server.core.admin.quanlydanhgia.dto.SanPhamCoDanhGiaResponse;
import com.example.server.core.admin.quanlydanhgia.service.AdminDanhGiaService;
import com.example.server.core.client.danhgia.dto.DanhGiaCongKhaiResponse;
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

/** Quản lý đánh giá phía admin: bảng sản phẩm, xem đánh giá, xóa mềm, phản hồi. */
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

    /** Toàn bộ đánh giá của một sản phẩm (mới nhất trước). Đồng thời đánh dấu đã xem. */
    @GetMapping("/san-pham/{giayId}")
    public ResponseEntity<ApiResponse<List<AdminDanhGiaResponse>>> theoSanPham(@PathVariable Integer giayId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy đánh giá thành công",
                service.layTheoSanPham(giayId)));
    }

    /** Tổng số đánh giá chưa xem (cho chuông thông báo). */
    @GetMapping("/chua-xem")
    public ResponseEntity<ApiResponse<Long>> chuaXem() {
        return ResponseEntity.ok(ApiResponse.success("OK", service.demChuaXem()));
    }

    /** Toàn bộ đánh giá của shop (màn "Tất cả đánh giá", kèm thông tin sản phẩm). */
    @GetMapping("/tat-ca")
    public ResponseEntity<ApiResponse<List<DanhGiaCongKhaiResponse>>> tatCa() {
        return ResponseEntity.ok(ApiResponse.success("Lấy tất cả đánh giá thành công", service.layTatCa()));
    }

    /** Xóa mềm một đánh giá. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoa(@PathVariable Integer id) {
        service.xoaMem(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa đánh giá", null));
    }

    /** Phản hồi một đánh giá (1 lần/đánh giá). */
    @PostMapping("/{id}/phan-hoi")
    public ResponseEntity<ApiResponse<AdminDanhGiaResponse>> phanHoi(
            @PathVariable Integer id, @Valid @RequestBody PhanHoiRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đã gửi phản hồi", service.phanHoi(id, request.noiDung())));
    }
}
