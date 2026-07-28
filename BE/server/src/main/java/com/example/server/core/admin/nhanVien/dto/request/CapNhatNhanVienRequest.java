package com.example.server.core.admin.nhanVien.dto.request;

import com.example.server.infrastructure.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record CapNhatNhanVienRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(min = 4, max = 99, message = "Họ tên phải lớn hơn 3 và nhỏ hơn 100 ký tự")
        @Pattern(regexp = ValidationPatterns.FULL_NAME, message = "Họ tên chỉ được chứa chữ cái và khoảng trắng, không có khoảng trắng ở đầu hoặc cuối")
        String hoTen,
        @NotBlank @Size(max = 100) String tenDangNhap,
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Email không quá 100 ký tự")
        String email,
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = ValidationPatterns.VN_PHONE, message = "Số điện thoại phải bắt đầu bằng 03, 05, 07, 08 hoặc 09 và có đúng 10 chữ số")
        String sdt,
        @Size(max = 10) String gioiTinh,
        @Past(message = "Ngày sinh không được là ngày trong tương lai") LocalDate ngaySinh,
        @Size(max = 200) String diaChi,
        String hinhAnh,
        @NotNull Integer vaiTro
) {}
