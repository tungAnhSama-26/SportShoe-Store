package com.example.server.core.client.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import com.example.server.infrastructure.address.DiaChiHaiCapRequest;

public record ClientDiaChiRequest(
        @NotBlank(message = "Họ tên người nhận không được để trống")
        @Size(max = 100, message = "Họ tên người nhận không được vượt quá 100 ký tự")
        String hoTen,

        @NotBlank(message = "Số điện thoại người nhận không được để trống")
        @Pattern(regexp = "^(0|\\+84)[35789]\\d{8}$", message = "Số điện thoại không đúng định dạng")
        @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
        String sdt,

        @NotNull(message = "Địa chỉ không được để trống")
        @Valid
        DiaChiHaiCapRequest diaChi,

        @NotNull
        Boolean laMacDinh
) {
}
