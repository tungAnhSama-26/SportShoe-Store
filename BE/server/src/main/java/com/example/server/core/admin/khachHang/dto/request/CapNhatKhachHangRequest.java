package com.example.server.core.admin.khachHang.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CapNhatKhachHangRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên không quá 100 ký tự")
        String hoTen,

        @Email(message = "Email không hợp lệ")
        @Size(max = 100, message = "Email không quá 100 ký tự")
        String email,

        @Pattern(regexp = "^$|^(0|\\+84)[35789]\\d{8}$", message = "Số điện thoại không đúng định dạng")
        @Size(max = 20, message = "SĐT không quá 20 ký tự")
        String sdt,

        @Min(value = 0, message = "Giới tính không hợp lệ")
        @Max(value = 2, message = "Giới tính không hợp lệ")
        Integer gioiTinh,

        String hinhAnh,

        LocalDate ngaySinh
) {}
