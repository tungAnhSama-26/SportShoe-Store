package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TuChoiBanGiaoRequest(
        @NotBlank @Size(max = 255) String lyDo
) {}
