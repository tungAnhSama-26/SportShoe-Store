package com.example.server.core.admin.quanLySanPham;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

// ─── Options (for dropdowns) ─────────────────────────────────────────────────

record LoaiGiayOption(Integer id, String ten) {}
record ThuongHieuOption(Integer id, String ten, String logoUrl) {}
record MauSacOption(Integer id, String ten, String maMauHex) {}
record KichCoOption(Integer id, String giaTri) {}
record DeGiayOption(Integer id, String ten) {}
record CoGiayOption(Integer id, String ten) {}
record ChatLieuGiayOption(Integer id, String ten) {}
record TrongLuongOption(Integer id, String ma, Integer giaTri) {}
record CongNgheDemOption(Integer id, String ten) {}

record DanhMucSanPhamResponse(
        java.util.List<LoaiGiayOption> loaiGiay,
        java.util.List<ThuongHieuOption> thuongHieu,
        java.util.List<MauSacOption> mauSac,
        java.util.List<KichCoOption> kichCo,
        java.util.List<DeGiayOption> deGiay,
        java.util.List<CoGiayOption> coGiay,
        java.util.List<ChatLieuGiayOption> chatLieuGiay,
        java.util.List<TrongLuongOption> trongLuong,
        java.util.List<CongNgheDemOption> congNgheDem
) {}

// ─── Product list & detail ───────────────────────────────────────────────────

record GiayListItemResponse(
        Integer id,
        String ma,
        String ten,
        String loaiGiay,
        String thuongHieu,
        String chatLieu,
        String deGiay,
        String coGiay,
        String congNgheDem,
        String trongLuong,
        Integer gioiTinh,
        Integer trangThai,
        String hinhAnh,
        BigDecimal giaMin,
        BigDecimal giaMax,
        Long tongBienThe,
        Long tongSoLuong,
        Instant ngayTao,
        Boolean coGiamGia
) {}

record ThuocTinhResponse(
        Integer id,
        Integer deGiayId,
        String deGiay,
        Integer coGiayId,
        String coGiay,
        Integer congNgheDemId,
        String congNgheDem,
        Integer chatLieuGiayId,
        String chatLieuGiay,
        Integer trongLuongId,
        String trongLuong
) {}

record GiayDetailResponse(
        Integer id,
        String ma,
        String ten,
        Integer gioiTinh,
        Integer thuongHieuId,
        String thuongHieu,
        Integer loaiGiayId,
        String loaiGiay,
        String chatLieu,
        String moTa,
        Integer trangThai,
        ThuocTinhResponse thuocTinh,
        java.util.List<HinhAnhGiayResponse> hinhAnhs,
        Instant ngayTao,
        Instant ngayCapNhat
) {}

record TaoChiTietSanPhamResponse(
        GiayDetailResponse giay,
        BienTheResponse bienThe,
        Boolean taoMoiSanPham
) {}

record TaoChiTietSanPhamHangLoatResponse(
        GiayDetailResponse giay,
        java.util.List<BienTheResponse> bienThes,
        Boolean taoMoiSanPham
) {}

record ChiTietSanPhamListItemResponse(
        Integer id,
        Integer giayId,
        String maSanPham,
        String maChiTietSanPham,
        String sku,
        String tenSanPham,
        String thuongHieu,
        String loaiGiay,
        String chatLieu,
        Integer gioiTinh,
        Integer mauSacId,
        String mauSac,
        String maMauHex,
        Integer kichCoId,
        String kichCo,
        Integer soLuong,
        BigDecimal giaGoc,
        BigDecimal giaBan,
        Integer kichHoat,
        String hinhAnh,
        Instant ngayTao,
        Instant ngayCapNhat,
        Integer dotGiamGiaId,
        String maDotGiamGia,
        String tenDotGiamGia,
        Integer loaiGiam,
        BigDecimal giaTriGiam
) {}

// ─── Biến thể ────────────────────────────────────────────────────────────────

record BienTheResponse(
        Integer id,
        String maBienThe,
        String sku,
        Integer soLuong,
        BigDecimal giaGoc,
        BigDecimal giaBan,
        Integer kichHoat,
        Integer mauSacId,
        String mauSac,
        String maMauHex,
        Integer kichCoId,
        String kichCo,
        Instant ngayTao,
        Instant ngayCapNhat,
        Integer dotGiamGiaId,
        String maDotGiamGia,
        String tenDotGiamGia,
        Integer loaiGiam,
        BigDecimal giaTriGiam
) {}

// ─── Hình ảnh ────────────────────────────────────────────────────────────────

record HinhAnhGiayResponse(
        Integer id,
        Integer loaiHinh,
        String url,
        String moTa,
        Boolean laHinhChinh,
        Integer trangThai,
        Instant ngayTao
) {}

// ─── Requests ────────────────────────────────────────────────────────────────

record TaoGiayRequest(
        String ma,
        @NotBlank String ten,
        @NotNull Integer thuongHieuId,
        @NotNull Integer loaiGiayId,
        Integer gioiTinh,
        String chatLieu,
        Integer chatLieuGiayId,
        String moTa,
        Integer deGiayId,
        Integer coGiayId,
        Integer congNgheDemId,
        Integer trongLuongId
) {}

record CapNhatGiayRequest(
        @NotBlank String ten,
        @NotNull Integer thuongHieuId,
        @NotNull Integer loaiGiayId,
        Integer gioiTinh,
        String chatLieu,
        Integer chatLieuGiayId,
        String moTa,
        Integer deGiayId,
        Integer coGiayId,
        Integer congNgheDemId,
        Integer trongLuongId
) {}

record TaoChiTietSanPhamRequest(
        Integer giayId,
        String ten,
        Integer thuongHieuId,
        Integer loaiGiayId,
        Integer gioiTinh,
        String chatLieu,
        Integer chatLieuGiayId,
        String moTa,
        Integer deGiayId,
        Integer coGiayId,
        Integer congNgheDemId,
        Integer trongLuongId,
        @NotNull Integer mauSacId,
        @NotNull Integer kichCoId,
        @NotNull @Min(0) Integer soLuong,
        @NotNull BigDecimal giaGoc,
        @NotNull BigDecimal giaBan
) {}

record TaoChiTietSanPhamHangLoatItemRequest(
        @NotNull Integer mauSacId,
        @NotNull Integer kichCoId,
        @NotNull @Min(0) Integer soLuong,
        @NotNull BigDecimal giaGoc,
        @NotNull BigDecimal giaBan
) {}

record TaoChiTietSanPhamHangLoatRequest(
        Integer giayId,
        String ten,
        Integer thuongHieuId,
        Integer loaiGiayId,
        Integer gioiTinh,
        String chatLieu,
        Integer chatLieuGiayId,
        String moTa,
        Integer deGiayId,
        Integer coGiayId,
        Integer congNgheDemId,
        Integer trongLuongId,
        @NotEmpty java.util.List<@Valid TaoChiTietSanPhamHangLoatItemRequest> bienThes
) {}

record DoiTrangThaiRequest(@NotNull Integer trangThai) {}

record DoiTrangThaiBienTheRequest(@NotNull Integer kichHoat) {}

record TaoBienTheRequest(
        @NotNull Integer mauSacId,
        @NotNull Integer kichCoId,
        @NotNull @Min(0) Integer soLuong,
        @NotNull BigDecimal giaGoc,
        @NotNull BigDecimal giaBan
) {}

record CapNhatBienTheRequest(
        @NotNull @Min(0) Integer soLuong,
        @NotNull BigDecimal giaGoc,
        @NotNull BigDecimal giaBan,
        @NotNull Integer kichHoat
) {}

record ThemHinhAnhRequest(
        @NotBlank String url,
        Integer loaiHinh,
        @Size(max = 300) String moTa
) {}
