package com.example.server.core.client.voucher.dto;

import com.example.server.core.client.dathang.dto.DatHangItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record KiemTraVoucherRequest(
        // Khách vãng lai (chưa đăng nhập) -> null. Khi đó chỉ dùng được voucher toàn sàn.
        UUID khachHangId,

        @NotEmpty(message = "Giỏ hàng đang trống")
        List<@Valid DatHangItemRequest> sanPhams,

        @NotBlank(message = "Vui lòng nhập mã giảm giá")
        String maPhieu
) {}
