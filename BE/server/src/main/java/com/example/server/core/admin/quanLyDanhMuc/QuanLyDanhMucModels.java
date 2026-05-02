package com.example.server.core.admin.quanLyDanhMuc;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

// ─── Loại Giày ───────────────────────────────────────────────────────────────

record LoaiGiayResponse(
        Integer id, String ma, String ten, String moTa,
        Integer trangThai, Instant ngayTao, Instant ngayCapNhat
) {}

record LoaiGiayRequest(
        @NotBlank String ma,
        @NotBlank String ten,
        @Size(max = 500) String moTa
) {}

// ─── Thương Hiệu ─────────────────────────────────────────────────────────────

record ThuongHieuResponse(
        Integer id, String ma, String ten, String xuatXu,
        String logoUrl, String website, String moTa,
        Integer trangThai, Instant ngayTao, Instant ngayCapNhat
) {}

record ThuongHieuRequest(
        @NotBlank String ma,
        @NotBlank String ten,
        String xuatXu,
        String logoUrl,
        String website,
        String moTa
) {}

// ─── Đế Giày ─────────────────────────────────────────────────────────────────

record ChatLieuGiayResponse(
        Integer id, String ma, String ten, String moTa,
        Integer trangThai, Instant ngayTao, Instant ngayCapNhat
) {}

record ChatLieuGiayRequest(
        @NotBlank String ma,
        @NotBlank @Size(max = 100) String ten,
        @Size(max = 300) String moTa
) {}

record DeGiayResponse(
        Integer id, String ma, String ten, String moTa,
        Integer trangThai, Instant ngayTao, Instant ngayCapNhat
) {}

record DeGiayRequest(
        @NotBlank String ma,
        @NotBlank @Size(max = 100) String ten,
        @Size(max = 300) String moTa
) {}

// ─── Cổ Giày ─────────────────────────────────────────────────────────────────

record CoGiayResponse(
        Integer id, String ma, String ten, String moTa,
        Integer trangThai, Instant ngayTao, Instant ngayCapNhat
) {}

record CoGiayRequest(
        @NotBlank String ma,
        @NotBlank @Size(max = 100) String ten,
        @Size(max = 300) String moTa
) {}

// ─── Công Nghệ Đệm ───────────────────────────────────────────────────────────

record CongNgheDemResponse(
        Integer id, String ma, String ten, String moTa,
        Integer trangThai, Instant ngayTao, Instant ngayCapNhat
) {}

record CongNgheDemRequest(
        @NotBlank String ma,
        @NotBlank String ten,
        @Size(max = 500) String moTa
) {}

// ─── Màu Sắc ─────────────────────────────────────────────────────────────────

record MauSacResponse(
        Integer id, String ma, String ten, String maMauHex,
        Integer trangThai, Instant ngayTao, Instant ngayCapNhat
) {}

record MauSacRequest(
        @NotBlank String ma,
        @NotBlank String ten,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã màu chưa đúng định dạng, vui lòng nhập lại") String maMauHex
) {}

// ─── Kích Cỡ ─────────────────────────────────────────────────────────────────

record KichCoResponse(
        Integer id, String giaTri, String ghiChu,
        Integer trangThai, Instant ngayTao, Instant ngayCapNhat
) {}

record KichCoRequest(
        @NotBlank String giaTri,
        String ghiChu
) {}

// ─── Trọng Lượng ─────────────────────────────────────────────────────────────

record TrongLuongResponse(
        Integer id, String ma, Integer giaTri, String moTa,
        Integer trangThai, Instant ngayTao, Instant ngayCapNhat
) {}

record TrongLuongRequest(
        @NotBlank String ma,
        @NotNull @Min(1) Integer giaTri,
        String moTa
) {}

// ─── Common ──────────────────────────────────────────────────────────────────

record DoiTrangThaiDanhMucRequest(@NotNull Integer trangThai) {}
