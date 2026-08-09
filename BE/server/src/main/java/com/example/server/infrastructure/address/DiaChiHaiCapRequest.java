package com.example.server.infrastructure.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiaChiHaiCapRequest(
        @Size(max = 20, message = "Mã tỉnh/thành không được vượt quá 20 ký tự")
        String tinhThanhCode,

        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        @Size(max = 100, message = "Tỉnh/Thành phố không được vượt quá 100 ký tự")
        String tinhThanh,

        @Size(max = 20, message = "Mã phường/xã không được vượt quá 20 ký tự")
        String phuongXaCode,

        @NotBlank(message = "Phường/Xã không được để trống")
        @Size(max = 100, message = "Phường/Xã không được vượt quá 100 ký tự")
        String phuongXa,

        @NotBlank(message = "Địa chỉ cụ thể không được để trống")
        @Size(max = 300, message = "Địa chỉ cụ thể không được vượt quá 300 ký tự")
        String diaChiCuThe
) {
}
