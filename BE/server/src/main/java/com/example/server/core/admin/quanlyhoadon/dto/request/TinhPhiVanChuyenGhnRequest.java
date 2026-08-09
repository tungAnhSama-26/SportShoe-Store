package com.example.server.core.admin.quanlyhoadon.dto.request;

import com.example.server.infrastructure.address.DiaChiHaiCapRequest;

public record TinhPhiVanChuyenGhnRequest(
        DiaChiHaiCapRequest diaChiGiaoHang,
        Integer serviceId,
        Integer serviceTypeId,
        Integer length,
        Integer width,
        Integer height,
        Integer weight,
        Integer insuranceValue,
        String coupon
) {
}
