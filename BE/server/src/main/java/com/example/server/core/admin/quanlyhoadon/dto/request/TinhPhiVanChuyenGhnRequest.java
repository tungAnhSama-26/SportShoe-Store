package com.example.server.core.admin.quanlyhoadon.dto.request;

public record TinhPhiVanChuyenGhnRequest(
        Integer toDistrictId,
        String toWardCode,
        String toAddress,
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
