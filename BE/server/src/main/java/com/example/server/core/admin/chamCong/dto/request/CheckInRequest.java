package com.example.server.core.admin.chamCong.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CheckInRequest(
    @NotNull(message = "Mã nhân viên không được để trống")
    UUID nhanVienId
) {
}
