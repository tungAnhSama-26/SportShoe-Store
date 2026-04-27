package com.example.server.core.admin.quanLyDanhMuc;

import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/danh-muc")
public class QuanLyDanhMucController {

    private final QuanLyDanhMucService service;

    public QuanLyDanhMucController(QuanLyDanhMucService service) {
        this.service = service;
    }

    // ─── Loại Giày ───────────────────────────────────────────────────────────

    @GetMapping("/loai-giay")
    public ResponseEntity<ApiResponse<PageResponse<LoaiGiayResponse>>> danhSachLoaiGiay(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách loại giày thành công",
                service.danhSachLoaiGiay(keyword, pageable)));
    }

    @GetMapping("/loai-giay/{id}")
    public ResponseEntity<ApiResponse<LoaiGiayResponse>> chiTietLoaiGiay(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết loại giày thành công", service.chiTietLoaiGiay(id)));
    }

    @PostMapping("/loai-giay")
    public ResponseEntity<ApiResponse<LoaiGiayResponse>> taoLoaiGiay(@Valid @RequestBody LoaiGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo loại giày thành công", service.taoLoaiGiay(req)));
    }

    @PutMapping("/loai-giay/{id}")
    public ResponseEntity<ApiResponse<LoaiGiayResponse>> capNhatLoaiGiay(
            @PathVariable Integer id, @Valid @RequestBody LoaiGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật loại giày thành công", service.capNhatLoaiGiay(id, req)));
    }

    @PatchMapping("/loai-giay/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiLoaiGiay(
            @PathVariable Integer id, @Valid @RequestBody DoiTrangThaiDanhMucRequest req) {
        service.doiTrangThaiLoaiGiay(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/loai-giay/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaLoaiGiay(@PathVariable Integer id) {
        service.xoaLoaiGiay(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa loại giày thành công", null));
    }

    // ─── Thương Hiệu ─────────────────────────────────────────────────────────

    @GetMapping("/thuong-hieu")
    public ResponseEntity<ApiResponse<PageResponse<ThuongHieuResponse>>> danhSachThuongHieu(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thương hiệu thành công",
                service.danhSachThuongHieu(keyword, pageable)));
    }

    @GetMapping("/thuong-hieu/{id}")
    public ResponseEntity<ApiResponse<ThuongHieuResponse>> chiTietThuongHieu(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thương hiệu thành công", service.chiTietThuongHieu(id)));
    }

    @PostMapping("/thuong-hieu")
    public ResponseEntity<ApiResponse<ThuongHieuResponse>> taoThuongHieu(@Valid @RequestBody ThuongHieuRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo thương hiệu thành công", service.taoThuongHieu(req)));
    }

    @PutMapping("/thuong-hieu/{id}")
    public ResponseEntity<ApiResponse<ThuongHieuResponse>> capNhatThuongHieu(
            @PathVariable Integer id, @Valid @RequestBody ThuongHieuRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thương hiệu thành công", service.capNhatThuongHieu(id, req)));
    }

    @PatchMapping("/thuong-hieu/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiThuongHieu(
            @PathVariable Integer id, @Valid @RequestBody DoiTrangThaiDanhMucRequest req) {
        service.doiTrangThaiThuongHieu(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/thuong-hieu/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaThuongHieu(@PathVariable Integer id) {
        service.xoaThuongHieu(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thương hiệu thành công", null));
    }

    // ─── Đế Giày ─────────────────────────────────────────────────────────────

    @GetMapping("/chat-lieu-giay")
    public ResponseEntity<ApiResponse<PageResponse<ChatLieuGiayResponse>>> danhSachChatLieuGiay(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success("Láº¥y danh sÃ¡ch cháº¥t liá»‡u giÃ y thÃ nh cÃ´ng",
                service.danhSachChatLieuGiay(keyword, pageable)));
    }

    @GetMapping("/chat-lieu-giay/{id}")
    public ResponseEntity<ApiResponse<ChatLieuGiayResponse>> chiTietChatLieuGiay(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Láº¥y chi tiáº¿t cháº¥t liá»‡u giÃ y thÃ nh cÃ´ng", service.chiTietChatLieuGiay(id)));
    }

    @PostMapping("/chat-lieu-giay")
    public ResponseEntity<ApiResponse<ChatLieuGiayResponse>> taoChatLieuGiay(@Valid @RequestBody ChatLieuGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Táº¡o cháº¥t liá»‡u giÃ y thÃ nh cÃ´ng", service.taoChatLieuGiay(req)));
    }

    @PutMapping("/chat-lieu-giay/{id}")
    public ResponseEntity<ApiResponse<ChatLieuGiayResponse>> capNhatChatLieuGiay(
            @PathVariable Integer id, @Valid @RequestBody ChatLieuGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cáº­p nháº­t cháº¥t liá»‡u giÃ y thÃ nh cÃ´ng", service.capNhatChatLieuGiay(id, req)));
    }

    @PatchMapping("/chat-lieu-giay/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiChatLieuGiay(
            @PathVariable Integer id, @Valid @RequestBody DoiTrangThaiDanhMucRequest req) {
        service.doiTrangThaiChatLieuGiay(id, req);
        return ResponseEntity.ok(ApiResponse.success("Äá»•i tráº¡ng thÃ¡i thÃ nh cÃ´ng", null));
    }

    @DeleteMapping("/chat-lieu-giay/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaChatLieuGiay(@PathVariable Integer id) {
        service.xoaChatLieuGiay(id);
        return ResponseEntity.ok(ApiResponse.success("XÃ³a cháº¥t liá»‡u giÃ y thÃ nh cÃ´ng", null));
    }

    @GetMapping("/de-giay")
    public ResponseEntity<ApiResponse<PageResponse<DeGiayResponse>>> danhSachDeGiay(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đế giày thành công",
                service.danhSachDeGiay(keyword, pageable)));
    }

    @GetMapping("/de-giay/{id}")
    public ResponseEntity<ApiResponse<DeGiayResponse>> chiTietDeGiay(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đế giày thành công", service.chiTietDeGiay(id)));
    }

    @PostMapping("/de-giay")
    public ResponseEntity<ApiResponse<DeGiayResponse>> taoDeGiay(@Valid @RequestBody DeGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo đế giày thành công", service.taoDeGiay(req)));
    }

    @PutMapping("/de-giay/{id}")
    public ResponseEntity<ApiResponse<DeGiayResponse>> capNhatDeGiay(
            @PathVariable Integer id, @Valid @RequestBody DeGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đế giày thành công", service.capNhatDeGiay(id, req)));
    }

    @PatchMapping("/de-giay/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiDeGiay(
            @PathVariable Integer id, @Valid @RequestBody DoiTrangThaiDanhMucRequest req) {
        service.doiTrangThaiDeGiay(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/de-giay/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaDeGiay(@PathVariable Integer id) {
        service.xoaDeGiay(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đế giày thành công", null));
    }

    // ─── Cổ Giày ─────────────────────────────────────────────────────────────

    @GetMapping("/co-giay")
    public ResponseEntity<ApiResponse<PageResponse<CoGiayResponse>>> danhSachCoGiay(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cổ giày thành công",
                service.danhSachCoGiay(keyword, pageable)));
    }

    @GetMapping("/co-giay/{id}")
    public ResponseEntity<ApiResponse<CoGiayResponse>> chiTietCoGiay(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết cổ giày thành công", service.chiTietCoGiay(id)));
    }

    @PostMapping("/co-giay")
    public ResponseEntity<ApiResponse<CoGiayResponse>> taoCoGiay(@Valid @RequestBody CoGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo cổ giày thành công", service.taoCoGiay(req)));
    }

    @PutMapping("/co-giay/{id}")
    public ResponseEntity<ApiResponse<CoGiayResponse>> capNhatCoGiay(
            @PathVariable Integer id, @Valid @RequestBody CoGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cổ giày thành công", service.capNhatCoGiay(id, req)));
    }

    @PatchMapping("/co-giay/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiCoGiay(
            @PathVariable Integer id, @Valid @RequestBody DoiTrangThaiDanhMucRequest req) {
        service.doiTrangThaiCoGiay(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/co-giay/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaCoGiay(@PathVariable Integer id) {
        service.xoaCoGiay(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa cổ giày thành công", null));
    }

    // ─── Công Nghệ Đệm ───────────────────────────────────────────────────────

    @GetMapping("/cong-nghe-dem")
    public ResponseEntity<ApiResponse<PageResponse<CongNgheDemResponse>>> danhSachCongNgheDem(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách công nghệ đệm thành công",
                service.danhSachCongNgheDem(keyword, pageable)));
    }

    @GetMapping("/cong-nghe-dem/{id}")
    public ResponseEntity<ApiResponse<CongNgheDemResponse>> chiTietCongNgheDem(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết công nghệ đệm thành công", service.chiTietCongNgheDem(id)));
    }

    @PostMapping("/cong-nghe-dem")
    public ResponseEntity<ApiResponse<CongNgheDemResponse>> taoCongNgheDem(@Valid @RequestBody CongNgheDemRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo công nghệ đệm thành công", service.taoCongNgheDem(req)));
    }

    @PutMapping("/cong-nghe-dem/{id}")
    public ResponseEntity<ApiResponse<CongNgheDemResponse>> capNhatCongNgheDem(
            @PathVariable Integer id, @Valid @RequestBody CongNgheDemRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật công nghệ đệm thành công", service.capNhatCongNgheDem(id, req)));
    }

    @PatchMapping("/cong-nghe-dem/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiCongNgheDem(
            @PathVariable Integer id, @Valid @RequestBody DoiTrangThaiDanhMucRequest req) {
        service.doiTrangThaiCongNgheDem(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/cong-nghe-dem/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaCongNgheDem(@PathVariable Integer id) {
        service.xoaCongNgheDem(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa công nghệ đệm thành công", null));
    }

    // ─── Màu Sắc ─────────────────────────────────────────────────────────────

    @GetMapping("/mau-sac")
    public ResponseEntity<ApiResponse<PageResponse<MauSacResponse>>> danhSachMauSac(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách màu sắc thành công",
                service.danhSachMauSac(keyword, pageable)));
    }

    @GetMapping("/mau-sac/{id}")
    public ResponseEntity<ApiResponse<MauSacResponse>> chiTietMauSac(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết màu sắc thành công", service.chiTietMauSac(id)));
    }

    @PostMapping("/mau-sac")
    public ResponseEntity<ApiResponse<MauSacResponse>> taoMauSac(@Valid @RequestBody MauSacRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo màu sắc thành công", service.taoMauSac(req)));
    }

    @PutMapping("/mau-sac/{id}")
    public ResponseEntity<ApiResponse<MauSacResponse>> capNhatMauSac(
            @PathVariable Integer id, @Valid @RequestBody MauSacRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật màu sắc thành công", service.capNhatMauSac(id, req)));
    }

    @PatchMapping("/mau-sac/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiMauSac(
            @PathVariable Integer id, @Valid @RequestBody DoiTrangThaiDanhMucRequest req) {
        service.doiTrangThaiMauSac(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/mau-sac/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaMauSac(@PathVariable Integer id) {
        service.xoaMauSac(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa màu sắc thành công", null));
    }

    // ─── Kích Cỡ ─────────────────────────────────────────────────────────────

    @GetMapping("/kich-co")
    public ResponseEntity<ApiResponse<PageResponse<KichCoResponse>>> danhSachKichCo(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kích cỡ thành công",
                service.danhSachKichCo(keyword, pageable)));
    }

    @GetMapping("/kich-co/{id}")
    public ResponseEntity<ApiResponse<KichCoResponse>> chiTietKichCo(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết kích cỡ thành công", service.chiTietKichCo(id)));
    }

    @PostMapping("/kich-co")
    public ResponseEntity<ApiResponse<KichCoResponse>> taoKichCo(@Valid @RequestBody KichCoRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo kích cỡ thành công", service.taoKichCo(req)));
    }

    @PutMapping("/kich-co/{id}")
    public ResponseEntity<ApiResponse<KichCoResponse>> capNhatKichCo(
            @PathVariable Integer id, @Valid @RequestBody KichCoRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật kích cỡ thành công", service.capNhatKichCo(id, req)));
    }

    @PatchMapping("/kich-co/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiKichCo(
            @PathVariable Integer id, @Valid @RequestBody DoiTrangThaiDanhMucRequest req) {
        service.doiTrangThaiKichCo(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/kich-co/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaKichCo(@PathVariable Integer id) {
        service.xoaKichCo(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa kích cỡ thành công", null));
    }

    // ─── Trọng Lượng ─────────────────────────────────────────────────────────

    @GetMapping("/trong-luong")
    public ResponseEntity<ApiResponse<PageResponse<TrongLuongResponse>>> danhSachTrongLuong(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách trọng lượng thành công",
                service.danhSachTrongLuong(keyword, pageable)));
    }

    @GetMapping("/trong-luong/{id}")
    public ResponseEntity<ApiResponse<TrongLuongResponse>> chiTietTrongLuong(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết trọng lượng thành công", service.chiTietTrongLuong(id)));
    }

    @PostMapping("/trong-luong")
    public ResponseEntity<ApiResponse<TrongLuongResponse>> taoTrongLuong(@Valid @RequestBody TrongLuongRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo trọng lượng thành công", service.taoTrongLuong(req)));
    }

    @PutMapping("/trong-luong/{id}")
    public ResponseEntity<ApiResponse<TrongLuongResponse>> capNhatTrongLuong(
            @PathVariable Integer id, @Valid @RequestBody TrongLuongRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trọng lượng thành công", service.capNhatTrongLuong(id, req)));
    }

    @PatchMapping("/trong-luong/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiTrongLuong(
            @PathVariable Integer id, @Valid @RequestBody DoiTrangThaiDanhMucRequest req) {
        service.doiTrangThaiTrongLuong(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/trong-luong/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaTrongLuong(@PathVariable Integer id) {
        service.xoaTrongLuong(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa trọng lượng thành công", null));
    }
}
