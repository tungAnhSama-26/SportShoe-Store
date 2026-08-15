package com.example.server.core.admin.nhanVien.dto.responsse;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.UUID;

public record LichLamViecResponse(
        UUID id,
        UUID nhanVienId,
        LocalDate ngay,
        String caLamId
) {
    @JsonProperty("ca")
    public String ca() {
        return caLamId;
    }
}
