package com.example.server.core.admin.nhanVien.controller;

import com.example.server.core.admin.nhanVien.dto.request.MoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.BanGiaoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.XacNhanBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.request.KetCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.HuyBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.request.TuChoiBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.request.BaoCaoSuCoGiaoCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaOptionsResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaStatsResponse;
import com.example.server.core.admin.nhanVien.service.GiaoCaService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.security.AdminPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/giao-ca")
public class GiaoCaController {

    private final GiaoCaService giaoCaService;

    public GiaoCaController(GiaoCaService giaoCaService) {
        this.giaoCaService = giaoCaService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<GiaoCaResponse>> layCaHoatDong() {
        UUID nhanVienId = getPrincipal().id();
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy ca làm việc hoạt động thành công",
                giaoCaService.layCaHoatDong(nhanVienId)
        ));
    }

    @PostMapping("/open")
    public ResponseEntity<ApiResponse<GiaoCaResponse>> moCa(
            @Valid @RequestBody MoCaRequest request
    ) {
        UUID nhanVienId = getPrincipal().id();
        return ResponseEntity.ok(ApiResponse.success(
                "Mở ca làm việc thành công",
                giaoCaService.moCa(nhanVienId, request)
        ));
    }

    @GetMapping("/current-stats")
    public ResponseEntity<ApiResponse<GiaoCaStatsResponse>> layThongTinGiaoCaCurrent() {
        UUID nhanVienId = getPrincipal().id();
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thống kê ca hiện tại thành công",
                giaoCaService.layThongTinGiaoCaCurrent(nhanVienId)
        ));
    }

    @PostMapping("/handover")
    public ResponseEntity<ApiResponse<GiaoCaResponse>> banGiaoCa(
            @Valid @RequestBody BanGiaoCaRequest request
    ) {
        UUID nhanVienId = getPrincipal().id();
        return ResponseEntity.ok(ApiResponse.success(
                "Bàn giao ca thành công. Ca làm việc chuyển sang trạng thái chờ xác nhận",
                giaoCaService.banGiaoCa(nhanVienId, request)
        ));
    }

    @GetMapping("/handover-options")
    public ResponseEntity<ApiResponse<GiaoCaOptionsResponse>> layTuyChonBanGiao() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách nhân viên nhận ca thành công",
                giaoCaService.layTuyChonBanGiao(getPrincipal().id())
        ));
    }

    @PostMapping("/end")
    public ResponseEntity<ApiResponse<GiaoCaResponse>> ketCa(@Valid @RequestBody KetCaRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Kết ca làm việc thành công",
                giaoCaService.ketCa(getPrincipal().id(), request)
        ));
    }

    @GetMapping("/pending-handovers")
    public ResponseEntity<ApiResponse<List<GiaoCaResponse>>> layCaChoXacNhan() {
        UUID nhanVienId = getPrincipal().id();
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách ca chờ xác nhận thành công",
                giaoCaService.layCaChoXacNhan(nhanVienId)
        ));
    }

    @PostMapping("/confirm-handover/{id}")
    public ResponseEntity<ApiResponse<GiaoCaResponse>> xacNhanBanGiao(
            @PathVariable("id") UUID giaoCaId,
            @Valid @RequestBody XacNhanBanGiaoRequest request
    ) {
        UUID nhanVienId = getPrincipal().id();
        return ResponseEntity.ok(ApiResponse.success(
                "Xác nhận bàn giao ca và mở ca mới thành công",
                giaoCaService.xacNhanBanGiao(nhanVienId, giaoCaId, request)
        ));
    }

    @PostMapping("/cancel-handover/{id}")
    public ResponseEntity<ApiResponse<GiaoCaResponse>> huyBanGiao(
            @PathVariable("id") UUID giaoCaId,
            @Valid @RequestBody HuyBanGiaoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã hủy yêu cầu bàn giao và mở lại ca làm việc",
                giaoCaService.huyBanGiao(getPrincipal().id(), giaoCaId, request)
        ));
    }

    @PostMapping("/reject-handover/{id}")
    public ResponseEntity<ApiResponse<GiaoCaResponse>> tuChoiBanGiao(
            @PathVariable("id") UUID giaoCaId,
            @Valid @RequestBody TuChoiBanGiaoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã từ chối nhận bàn giao",
                giaoCaService.tuChoiBanGiao(getPrincipal().id(), giaoCaId, request)
        ));
    }

    @PostMapping("/report-incident/{id}")
    public ResponseEntity<ApiResponse<Void>> baoCaoSuCo(
            @PathVariable("id") UUID giaoCaId,
            @Valid @RequestBody BaoCaoSuCoGiaoCaRequest request
    ) {
        giaoCaService.baoCaoSuCo(getPrincipal().id(), giaoCaId, request);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi báo cáo sự cố đến quản trị viên", null));
    }

    @PostMapping("/report-incident")
    public ResponseEntity<ApiResponse<Void>> baoCaoSuCoChuaMoCa(
            @Valid @RequestBody BaoCaoSuCoGiaoCaRequest request
    ) {
        giaoCaService.baoCaoSuCo(getPrincipal().id(), null, request);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi báo cáo sự cố đến quản trị viên", null));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<GiaoCaResponse>>> layLichSuGiaoCa(
            @RequestParam(name = "nhanVienId", required = false) UUID nhanVienId,
            @RequestParam(name = "trangThai", required = false) String trangThai,
            @RequestParam(name = "tuNgay", required = false) Instant tuNgay,
            @RequestParam(name = "denNgay", required = false) Instant denNgay,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "thoiGianVao"));
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy lịch sử giao ca thành công",
                giaoCaService.layLichSuGiaoCa(nhanVienId, trangThai, tuNgay, denNgay, keyword, pageable)
        ));
    }

    private AdminPrincipal getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal principal) {
            return principal;
        }
        throw new AccessDeniedException("Vui lòng đăng nhập hệ thống admin");
    }
}
