package com.example.server.core.client.vanchuyen.dto;

import com.example.server.core.client.dathang.dto.DatHangItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/** Yêu cầu tính phí vận chuyển cho giỏ hàng của khách tới một địa chỉ nhận. */
public record TinhPhiShipRequest(
        @NotNull UUID khachHangId,
        @NotEmpty List<@Valid DatHangItemRequest> sanPhams,
        String tinhThanh,
        String quanHuyen,
        String phuongXa,
        String diaChiCuThe
) {
}
