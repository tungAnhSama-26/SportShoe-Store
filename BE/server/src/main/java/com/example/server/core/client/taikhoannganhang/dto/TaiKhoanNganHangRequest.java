package com.example.server.core.client.taikhoannganhang.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaiKhoanNganHangRequest(
        @NotBlank(message = "Tên ngân hàng không được để trống")
        @Size(max = 100, message = "Tên ngân hàng không được vượt quá 100 ký tự")
        String tenNganHang,

        @NotBlank(message = "Số tài khoản không được để trống")
        @Size(max = 50, message = "Số tài khoản không được vượt quá 50 ký tự")
        String soTaiKhoan,

        @NotBlank(message = "Tên chủ tài khoản không được để trống")
        @Size(max = 100, message = "Tên chủ tài khoản không được vượt quá 100 ký tự")
        String tenChuTaiKhoan,

        @Size(max = 150, message = "Chi nhánh không được vượt quá 150 ký tự")
        String chiNhanh,

        Boolean laMacDinh
) {
}
