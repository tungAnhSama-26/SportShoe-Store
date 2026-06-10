package com.example.server.core.client.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record KiemTraVoucherRequest(
        @NotNull(message = "Thiếu thông tin khách hàng")
        UUID khachHangId,

        @NotBlank(message = "Vui lòng nhập mã giảm giá")
        String maPhieu
) {}
