package com.example.server.core.admin.banHangTaiQuay.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record ApDungPhieuGiamGiaRequest(
        Integer hoaDonId,
        UUID khachHangId,
        String maPhieuGiamGia,
        @NotEmpty(message = "Danh sách sản phẩm không được để trống")
        List<@Valid TaoHoaDonChoItemRequest> items
) {
}
