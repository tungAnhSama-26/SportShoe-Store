package com.example.server.core.admin.quanLySanPham;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
        BigDecimal giaGocMin,
        BigDecimal giaGocMax,
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
        @Size(max = 100) String ma,
        @NotBlank @Size(min = 3, max = 300) String ten,
        @NotNull @Positive Integer thuongHieuId,
        @NotNull @Positive Integer loaiGiayId,
        @Min(1) @Max(3) Integer gioiTinh,
        String chatLieu,
        @Positive Integer chatLieuGiayId,
        @Size(max = 2000) String moTa,
        @Positive Integer deGiayId,
        @Positive Integer coGiayId,
        @Positive Integer congNgheDemId,
        @Positive Integer trongLuongId
) {}

record CapNhatGiayRequest(
        @NotBlank @Size(min = 3, max = 300) String ten,
        @NotNull @Positive Integer thuongHieuId,
        @NotNull @Positive Integer loaiGiayId,
        @Min(1) @Max(3) Integer gioiTinh,
        String chatLieu,
        @Positive Integer chatLieuGiayId,
        @Size(max = 2000) String moTa,
        @Positive Integer deGiayId,
        @Positive Integer coGiayId,
        @Positive Integer congNgheDemId,
        @Positive Integer trongLuongId
) {}

record TaoChiTietSanPhamRequest(
        @Positive Integer giayId,
        @Size(max = 100) String ma,
        @Size(min = 3, max = 300) String ten,
        @Positive Integer thuongHieuId,
        @Positive Integer loaiGiayId,
        @Min(1) @Max(3) Integer gioiTinh,
        String chatLieu,
        @Positive Integer chatLieuGiayId,
        @Size(max = 2000) String moTa,
        @Positive Integer deGiayId,
        @Positive Integer coGiayId,
        @Positive Integer congNgheDemId,
        @Positive Integer trongLuongId,
        @NotNull @Positive Integer mauSacId,
        @NotNull @Positive Integer kichCoId,
        @NotNull @Min(0) Integer soLuong,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaGoc,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaBan
) {}

record TaoChiTietSanPhamHangLoatItemRequest(
        @NotNull @Positive Integer mauSacId,
        @NotNull @Positive Integer kichCoId,
        @NotNull @Min(0) Integer soLuong,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaGoc,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaBan
) {}

record TaoChiTietSanPhamHangLoatRequest(
        @Positive Integer giayId,
        @Size(max = 100) String ma,
        @Size(min = 3, max = 300) String ten,
        @Positive Integer thuongHieuId,
        @Positive Integer loaiGiayId,
        @Min(1) @Max(3) Integer gioiTinh,
        String chatLieu,
        @Positive Integer chatLieuGiayId,
        @Size(max = 2000) String moTa,
        @Positive Integer deGiayId,
        @Positive Integer coGiayId,
        @Positive Integer congNgheDemId,
        @Positive Integer trongLuongId,
        @NotEmpty java.util.List<@Valid TaoChiTietSanPhamHangLoatItemRequest> bienThes
) {}

record DoiTrangThaiRequest(@NotNull @Min(0) @Max(2) Integer trangThai) {}

record DoiTrangThaiBienTheRequest(@NotNull Integer kichHoat) {}

record TaoBienTheRequest(
        @NotNull @Positive Integer mauSacId,
        @NotNull @Positive Integer kichCoId,
        @NotNull @Min(0) Integer soLuong,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaGoc,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaBan
) {}

record CapNhatBienTheRequest(
        @NotNull @Min(0) Integer soLuong,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaGoc,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaBan,
        @NotNull Integer kichHoat
) {}

record ThemHinhAnhRequest(
        @NotBlank String url,
        Integer loaiHinh,
        @Size(max = 300) String moTa
) {}
