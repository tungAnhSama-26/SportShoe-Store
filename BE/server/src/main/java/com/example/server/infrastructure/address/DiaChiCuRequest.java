package com.example.server.infrastructure.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiaChiCuRequest(
        @NotBlank(message = "Địa chỉ CCCD không được để trống")
        @Size(max = 500, message = "Địa chỉ CCCD không được vượt quá 500 ký tự")
        String diaChiCu
) {
}
