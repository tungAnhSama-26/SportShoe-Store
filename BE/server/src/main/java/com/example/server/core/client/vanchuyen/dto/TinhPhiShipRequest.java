package com.example.server.core.client.vanchuyen.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Yêu cầu tính phí vận chuyển cho giỏ hàng của khách tới một địa chỉ nhận. */
public record TinhPhiShipRequest(
        @NotNull UUID khachHangId,
        String tinhThanh,
        String quanHuyen,
        String phuongXa,
        String diaChiCuThe
) {
}
