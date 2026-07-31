package com.example.server.core.admin.khachHang.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaoKhachHangRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(max = 50, message = "Tên đăng nhập không quá 50 ký tự")
        String tenDangNhap,

        @NotBlank(message = "Họ tên không được để trống")
        @Size(min = 3, max = 100, message = "Họ tên phải có từ 3 đến 100 ký tự")
        String hoTen,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 100, message = "Email không quá 100 ký tự")
        String email,

        @Pattern(regexp = "^$|^(0|\\+84)[35789]\\d{8}$", message = "Số điện thoại không đúng định dạng")
        @Size(max = 20, message = "SĐT không quá 20 ký tự")
        String sdt,

        @Min(value = 0, message = "Giới tính không hợp lệ")
        @Max(value = 2, message = "Giới tính không hợp lệ")
        Integer gioiTinh,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 255, message = "Mật khẩu phải có từ 6 đến 255 ký tự")
        String matKhau,

        String hinhAnh,

        LocalDate ngaySinh
) {}
