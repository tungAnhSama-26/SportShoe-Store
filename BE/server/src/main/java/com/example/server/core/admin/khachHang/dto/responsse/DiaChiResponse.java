package com.example.server.core.admin.khachHang.dto.responsse;

import com.example.server.infrastructure.address.DiaChiHaiCapResponse;

public record DiaChiResponse(
        Integer id,
        String hoTen,
        String sdt,
        DiaChiHaiCapResponse diaChi,
        Boolean laMacDinh
) {}
