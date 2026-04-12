package com.example.server.core.admin.quanLySanPham;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
record TrongLuongOption(Integer id, String ma, Integer giaTri) {}
record CongNgheDemOption(Integer id, String ten) {}

record DanhMucSanPhamResponse(
        java.util.List<LoaiGiayOption> loaiGiay,
        java.util.List<ThuongHieuOption> thuongHieu,
        java.util.List<MauSacOption> mauSac,
        java.util.List<KichCoOption> kichCo,
        java.util.List<DeGiayOption> deGiay,
        java.util.List<CoGiayOption> coGiay,
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
        Integer gioiTinh,
        Integer trangThai,
        String hinhAnh,
        BigDecimal giaMin,
        BigDecimal giaMax,
        Long tongBienThe,
        Long tongSoLuong,
        Instant ngayTao
) {}

record ThuocTinhResponse(
        Integer id,
        Integer deGiayId,
        String deGiay,
        Integer coGiayId,
        String coGiay,
        Integer congNgheDemId,
        String congNgheDem,
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
        Instant ngayTao,
        Instant ngayCapNhat
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
        Instant ngayCapNhat
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
        @NotBlank String ma,
        @NotBlank String ten,
        @NotNull Integer thuongHieuId,
        @NotNull Integer loaiGiayId,
        Integer gioiTinh,
        String chatLieu,
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
        String moTa,
        Integer deGiayId,
        Integer coGiayId,
        Integer congNgheDemId,
        Integer trongLuongId
) {}

record DoiTrangThaiRequest(@NotNull Integer trangThai) {}

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
