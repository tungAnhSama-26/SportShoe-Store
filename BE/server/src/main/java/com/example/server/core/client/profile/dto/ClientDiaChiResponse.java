package com.example.server.core.client.profile.dto;

import com.example.server.infrastructure.address.DiaChiHaiCapResponse;

public record ClientDiaChiResponse(
        Integer id,
        String hoTen,
        String sdt,
        DiaChiHaiCapResponse diaChi,
        Boolean laMacDinh
) {
}
