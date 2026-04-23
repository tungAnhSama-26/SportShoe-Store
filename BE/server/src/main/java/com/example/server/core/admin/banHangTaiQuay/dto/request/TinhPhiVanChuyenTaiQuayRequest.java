package com.example.server.core.admin.banHangTaiQuay.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TinhPhiVanChuyenTaiQuayRequest(
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
        String coupon,
        @NotEmpty(message = "Danh sach san pham khong duoc de trong")
        List<TaoHoaDonChoItemRequest> items
) {
}
