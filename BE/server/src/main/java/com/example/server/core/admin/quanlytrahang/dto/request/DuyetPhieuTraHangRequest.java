package com.example.server.core.admin.quanlytrahang.dto.request;

import jakarta.validation.constraints.Size;

public record DuyetPhieuTraHangRequest(
        boolean nhanHangTrucTiep,

        @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
        String ghiChu
) {
}
