package com.example.server.core.admin.khachHang.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaoKhachHangRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(max = 50, message = "Tên đăng nhập không quá 50 ký tự")
        String tenDangNhap,

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên không quá 100 ký tự")
        String hoTen,

        @Email(message = "Email không hợp lệ")
        @Size(max = 100, message = "Email không quá 100 ký tự")
        String email,

        @Size(max = 20, message = "SĐT không quá 20 ký tự")
        String sdt,

        @NotBlank(message = "Mật khẩu không được để trống")
        String matKhau,

        String hinhAnh,

        LocalDate ngaySinh
) {}
