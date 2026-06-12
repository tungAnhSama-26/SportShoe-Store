package com.example.server.core.client.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientDiaChiRequest(
        @NotBlank(message = "Họ tên người nhận không được để trống")
        @Size(max = 100, message = "Họ tên người nhận không được vượt quá 100 ký tự")
        String hoTen,

        @NotBlank(message = "Số điện thoại người nhận không được để trống")
        @Pattern(regexp = "^(0|\\+84)[35789]\\d{8}$", message = "Số điện thoại không đúng định dạng")
        @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
        String sdt,

        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        @Size(max = 100)
        String tinhThanh,

        @NotBlank(message = "Quận/Huyện không được để trống")
        @Size(max = 100)
        String quanHuyen,

        @NotBlank(message = "Phường/Xã không được để trống")
        @Size(max = 100)
        String phuongXa,

        @NotBlank(message = "Địa chỉ cụ thể không được để trống")
        @Size(max = 300, message = "Địa chỉ cụ thể không được vượt quá 300 ký tự")
        String diaChiCuThe,

        @NotNull
        Boolean laMacDinh
) {
}
