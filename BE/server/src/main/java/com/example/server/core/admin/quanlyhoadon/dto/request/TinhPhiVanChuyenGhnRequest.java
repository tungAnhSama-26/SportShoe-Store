package com.example.server.core.admin.quanlyhoadon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TinhPhiVanChuyenGhnRequest(
        @NotNull(message = "Ma quan/huyen GHN khong duoc de trong")
        Integer toDistrictId,

        @NotBlank(message = "Ma phuong/xa GHN khong duoc de trong")
        String toWardCode,

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
