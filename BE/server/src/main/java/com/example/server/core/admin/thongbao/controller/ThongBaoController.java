package com.example.server.core.admin.thongbao.controller;

import com.example.server.core.admin.thongbao.dto.ThongBaoResponse;
import com.example.server.core.admin.thongbao.service.ThongBaoService;
import com.example.server.infrastructure.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/thong-bao")
public class ThongBaoController {

    private final ThongBaoService thongBaoService;

    public ThongBaoController(ThongBaoService thongBaoService) {
        this.thongBaoService = thongBaoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ThongBaoResponse>>> layDanhSach(
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "15") int kichThuoc
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách thông báo thành công",
                thongBaoService.layDanhSach(trang, kichThuoc)
        ));
    }

    @GetMapping("/chua-doc-count")
    public ResponseEntity<ApiResponse<Long>> demChuaDoc() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy số lượng thông báo chưa đọc thành công",
                thongBaoService.demChuaDoc()
        ));
    }

    @PutMapping("/{id}/doc")
    public ResponseEntity<ApiResponse<ThongBaoResponse>> docThongBao(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đánh dấu thông báo đã đọc thành công",
                thongBaoService.docThongBao(id)
        ));
    }

    @PutMapping("/doc-tat-ca")
    public ResponseEntity<ApiResponse<Void>> docTatCa() {
        thongBaoService.docTatCa();
        return ResponseEntity.ok(ApiResponse.success(
                "Đánh dấu tất cả thông báo đã đọc thành công",
                null
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaThongBao(@PathVariable UUID id) {
        thongBaoService.xoaThongBao(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa thông báo thành công",
                null
        ));
    }
}
