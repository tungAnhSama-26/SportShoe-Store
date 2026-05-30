package com.example.server.core.admin.khachHang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DiaChiRequest(
        @NotBlank(message = "Họ tên người nhận không được để trống")
        @Size(max = 100)
        String hoTen,

        @NotBlank(message = "SĐT người nhận không được để trống")
        @Pattern(regexp = "^(0|\\+84)[35789]\\d{8}$", message = "Số điện thoại không đúng định dạng")
        @Size(max = 20)
        String sdt,

        @NotBlank(message = "Tỉnh/Thành không được để trống")
        @Size(max = 100)
        String tinhThanh,

        @NotBlank(message = "Quận/Huyện không được để trống")
        @Size(max = 100)
        String quanHuyen,

        @NotBlank(message = "Phường/Xã không được để trống")
        @Size(max = 100)
        String phuongXa,

        @NotBlank(message = "Địa chỉ cụ thể không được để trống")
        @Size(max = 300)
        String diaChiCuThe,

        @NotNull
        Boolean laMacDinh
) {}
