package com.example.server.core.client.donhang.dto;

import com.example.server.infrastructure.address.DiaChiHaiCapRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CapNhatThongTinGiaoHangRequest(
        @NotBlank(message = "Tên người nhận không được để trống")
        @Size(max = 100, message = "Tên người nhận không được vượt quá 100 ký tự")
        String tenNguoiNhan,

        @NotBlank(message = "Số điện thoại người nhận không được để trống")
        @Pattern(
                regexp = "^(0|\\+84)[35789]\\d{8}$",
                message = "Số điện thoại người nhận không đúng định dạng"
        )
        @Size(max = 20, message = "Số điện thoại người nhận không được vượt quá 20 ký tự")
        String sdtNguoiNhan,

        @NotNull(message = "Địa chỉ giao hàng không được để trống")
        @Valid
        DiaChiHaiCapRequest diaChiGiaoHang
) {
}
