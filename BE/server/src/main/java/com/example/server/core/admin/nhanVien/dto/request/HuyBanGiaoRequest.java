package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.Size;

public record HuyBanGiaoRequest(
        @Size(max = 255) String lyDo
) {}
