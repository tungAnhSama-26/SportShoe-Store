package com.example.server.core.admin.banHangTaiQuay.dto.request;

import com.example.server.infrastructure.address.DiaChiHaiCapRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TinhPhiVanChuyenTaiQuayRequest(
        @Valid DiaChiHaiCapRequest diaChiGiaoHang,
        Integer serviceId,
        Integer serviceTypeId,
        Integer length,
        Integer width,
        Integer height,
        Integer weight,
        Integer insuranceValue,
        String coupon,
        @NotEmpty(message = "Danh sách sản phẩm không được để trống")
        List<@Valid TaoHoaDonChoItemRequest> items
) {
}
