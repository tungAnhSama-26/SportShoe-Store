package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record PhanCaRequest(
        @NotNull(message = "Nhân viên không được để trống") UUID nhanVienId,
        @NotNull(message = "Ngày không được để trống") LocalDate ngay,
        String ca // "sang", "chieu", "toi" or null (to delete)
) {}
