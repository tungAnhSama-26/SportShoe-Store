package com.example.server.core.admin.quanlyhoadon.dto.responsse;

import java.math.BigDecimal;

public record TinhPhiVanChuyenGhnResponse(
        BigDecimal phiVanChuyen,
        Integer total,
        Integer serviceFee,
        Integer insuranceFee,
        Integer pickStationFee,
        Integer couponValue,
        Integer toDistrictId,
        String toWardCode,
        String matchedProvinceName,
        String matchedDistrictName,
        String matchedWardName
) {
}
