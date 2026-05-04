package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaoNhanVienRequest(
        @NotBlank @Size(max = 100) String hoTen,
        @NotBlank @Email @Size(max = 100) String email,
        @Size(max = 20) String sdt,
        @Pattern(regexp = "^\\d{12}$", message = "CCCD phai gom dung 12 chu so") String cccd,
        @Size(max = 10) String gioiTinh,
        LocalDate ngaySinh,
        @Size(max = 200) String diaChi,
        String hinhAnh,
        @NotNull Integer vaiTro
) {}
