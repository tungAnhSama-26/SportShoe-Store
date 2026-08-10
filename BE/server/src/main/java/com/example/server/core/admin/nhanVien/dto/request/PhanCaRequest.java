package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public record PhanCaRequest(
        @NotNull(message = "Nhân viên không được để trống") UUID nhanVienId,
        @NotNull(message = "Ngày không được để trống") LocalDate ngay,
        @NotBlank(message = "Ca làm việc không được để trống") String caLamId
) {}
