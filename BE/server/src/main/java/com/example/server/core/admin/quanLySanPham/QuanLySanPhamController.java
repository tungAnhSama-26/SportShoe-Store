package com.example.server.core.admin.quanLySanPham;

import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.api.PageResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/admin/san-pham")
public class QuanLySanPhamController {

    private final QuanLySanPhamService service;

    public QuanLySanPhamController(QuanLySanPhamService service) {
        this.service = service;
    }

    @GetMapping("/danh-muc")
    public ResponseEntity<ApiResponse<DanhMucSanPhamResponse>> layDanhMuc() {
        return ResponseEntity.ok(ApiResponse.success("Lay danh muc thanh cong", service.layDanhMuc()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GiayListItemResponse>>> danhSachGiay(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer thuongHieuId,
            @RequestParam(required = false) Integer loaiGiayId,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach giay thanh cong",
                service.danhSachGiay(keyword, thuongHieuId, loaiGiayId, trangThai, minPrice, maxPrice, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GiayDetailResponse>> chiTietGiay(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lay chi tiet giay thanh cong", service.chiTietGiay(id)));
    }

    @GetMapping("/chi-tiet")
    public ResponseEntity<ApiResponse<PageResponse<ChiTietSanPhamListItemResponse>>> danhSachChiTietSanPham(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer giayId,
            @RequestParam(required = false) Integer mauSacId,
            @RequestParam(required = false) Integer kichCoId,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach chi tiet san pham thanh cong",
                service.danhSachChiTietSanPham(keyword, giayId, mauSacId, kichCoId, trangThai, pageable)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GiayDetailResponse>> taoGiay(@Valid @RequestBody TaoGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tao giay thanh cong", service.taoGiay(req)));
    }

    @PostMapping("/chi-tiet")
    public ResponseEntity<ApiResponse<TaoChiTietSanPhamResponse>> taoChiTietSanPham(
            @Valid @RequestBody TaoChiTietSanPhamRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao san pham va chi tiet san pham thanh cong",
                service.taoChiTietSanPham(req)
        ));
    }

    @PostMapping("/chi-tiet-hang-loat")
    public ResponseEntity<ApiResponse<TaoChiTietSanPhamHangLoatResponse>> taoChiTietSanPhamHangLoat(
            @Valid @RequestBody TaoChiTietSanPhamHangLoatRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tao san pham va danh sach chi tiet san pham thanh cong",
                service.taoChiTietSanPhamHangLoat(req)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GiayDetailResponse>> capNhatGiay(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatGiayRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cap nhat giay thanh cong", service.capNhatGiay(id, req)));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThai(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiRequest req
    ) {
        service.doiTrangThai(id, req);
        return ResponseEntity.ok(ApiResponse.success("Doi trang thai thanh cong", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaGiay(@PathVariable Integer id) {
        service.xoaGiay(id);
        return ResponseEntity.ok(ApiResponse.success("Xoa giay thanh cong", null));
    }

    @GetMapping("/{giayId}/bien-the")
    public ResponseEntity<ApiResponse<List<BienTheResponse>>> danhSachBienThe(@PathVariable Integer giayId) {
        return ResponseEntity.ok(ApiResponse.success("Lay bien the thanh cong", service.danhSachBienThe(giayId)));
    }

    @PostMapping("/{giayId}/bien-the")
    public ResponseEntity<ApiResponse<BienTheResponse>> taoBienThe(
            @PathVariable Integer giayId,
            @Valid @RequestBody TaoBienTheRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success("Tao bien the thanh cong", service.taoBienThe(giayId, req)));
    }

    @PutMapping("/bien-the/{id}")
    public ResponseEntity<ApiResponse<BienTheResponse>> capNhatBienThe(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatBienTheRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cap nhat bien the thanh cong", service.capNhatBienThe(id, req)));
    }

    @PatchMapping("/bien-the/{id}/trang-thai")
    public ResponseEntity<ApiResponse<BienTheResponse>> doiTrangThaiBienThe(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiBienTheRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Doi trang thai bien the thanh cong",
                service.doiTrangThaiBienThe(id, req)
        ));
    }

    @DeleteMapping("/bien-the/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaBienThe(@PathVariable Integer id) {
        service.xoaBienThe(id);
        return ResponseEntity.ok(ApiResponse.success("Xoa bien the thanh cong", null));
    }

    @GetMapping("/bien-the/{id}/hinh-anh")
    public ResponseEntity<ApiResponse<List<HinhAnhGiayResponse>>> danhSachHinhAnh(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Lay hinh anh thanh cong", service.layHinhAnh(id)));
    }

    @PostMapping("/bien-the/{id}/hinh-anh")
    public ResponseEntity<ApiResponse<HinhAnhGiayResponse>> themHinhAnh(
            @PathVariable Integer id,
            @Valid @RequestBody ThemHinhAnhRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success("Them hinh anh thanh cong", service.themHinhAnh(id, req)));
    }

    @DeleteMapping("/hinh-anh/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaHinhAnh(@PathVariable Integer id) {
        service.xoaHinhAnh(id);
        return ResponseEntity.ok(ApiResponse.success("Xoa hinh anh thanh cong", null));
    }

    @PatchMapping("/hinh-anh/{id}/chinh")
    public ResponseEntity<ApiResponse<Void>> datHinhChinh(@PathVariable Integer id) {
        service.datHinhChinh(id);
        return ResponseEntity.ok(ApiResponse.success("Dat hinh chinh thanh cong", null));
    }
}
