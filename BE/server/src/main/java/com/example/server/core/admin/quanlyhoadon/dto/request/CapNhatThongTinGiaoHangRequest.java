package com.example.server.core.admin.quanlyhoadon.dto.request;

import com.example.server.infrastructure.address.DiaChiHaiCapRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CapNhatThongTinGiaoHangRequest(
        @NotBlank(message = "Tên người nhận không được để trống")
        @Size(min = 3, max = 49, message = "Họ và tên người nhận phải từ 3 đến 49 ký tự")
        @Pattern(
                regexp = "^[\\p{L} ]+$",
                message = "Họ và tên người nhận chỉ chứa chữ cái và khoảng trắng"
        )
        String tenNguoiNhan,

        @NotBlank(message = "Số điện thoại người nhận không được để trống")
        @Pattern(
                regexp = "^0[35789]\\d{8}$",
                message = "Số điện thoại người nhận gồm 10 chữ số, bắt đầu bằng 03, 05, 07, 08 hoặc 09"
        )
        String sdtNguoiNhan,

        @NotNull(message = "Địa chỉ giao hàng không được để trống")
        @Valid
        DiaChiHaiCapRequest diaChiGiaoHang
) {
}
