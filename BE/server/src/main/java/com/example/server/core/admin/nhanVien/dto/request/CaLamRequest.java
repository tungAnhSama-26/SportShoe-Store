package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CaLamRequest(
        String id,
        @NotBlank(message = "Tên ca không được để trống") String ten,
        @NotBlank(message = "Giờ bắt đầu không được để trống") String gioBatDau,
        @NotBlank(message = "Giờ kết thúc không được để trống") String gioKetThuc,
        @NotNull(message = "Trạng thái không được để trống") Boolean trangThai
) {}
