package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotNull;

public record DoiTrangThaiCaLamRequest(
        @NotNull(message = "Trạng thái không được để trống") Boolean trangThai
) {
}
