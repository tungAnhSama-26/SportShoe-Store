package com.example.server.core.admin.nhanVien.dto.request;

import com.example.server.infrastructure.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CaLamRequest(
        String id,
        @NotBlank(message = "Tên ca không được để trống")
        @Size(min = 3, max = 100, message = "Tên ca phải từ 3 đến 100 ký tự")
        @Pattern(regexp = ValidationPatterns.SHIFT_NAME, message = "Tên ca không được có khoảng trắng ở đầu, cuối hoặc chứa ký tự đặc biệt")
        String ten,
        @NotBlank(message = "Giờ bắt đầu không được để trống") String gioBatDau,
        @NotBlank(message = "Giờ kết thúc không được để trống") String gioKetThuc,
        @NotNull(message = "Trạng thái không được để trống") Boolean trangThai
) {}
