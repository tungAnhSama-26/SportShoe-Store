package com.example.server.core.client.profile.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ClientProfileResponse(
        UUID id,
        String tenDangNhap,
        String hoTen,
        String email,
        String sdt,
        LocalDate ngaySinh,
        Integer gioiTinh,
        String hinhAnh
) {
}
