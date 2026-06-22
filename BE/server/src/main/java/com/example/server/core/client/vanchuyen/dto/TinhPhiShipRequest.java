package com.example.server.core.client.vanchuyen.dto;

import com.example.server.core.client.dathang.dto.DatHangItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/** Yêu cầu tính phí vận chuyển cho giỏ hàng của khách tới một địa chỉ nhận. */
public record TinhPhiShipRequest(
        // Khách vãng lai (chưa đăng nhập) -> null. Service không dùng tới khachHangId khi tính phí.
        UUID khachHangId,
        @NotEmpty List<@Valid DatHangItemRequest> sanPhams,
        String tinhThanh,
        String quanHuyen,
        String phuongXa,
        String diaChiCuThe,
        // GHN: mã quận/huyện + phường/xã (nếu có thì tính phí chính xác, khỏi dò tên).
        Integer toDistrictId,
        String toWardCode
) {
}
