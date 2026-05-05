package com.example.server.core.admin.quanLyDanhMuc.kichCo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KichCoRequest(
        @NotBlank @Size(max = 20) String giaTri,
        @Size(max = 200) String ghiChu
) {}
