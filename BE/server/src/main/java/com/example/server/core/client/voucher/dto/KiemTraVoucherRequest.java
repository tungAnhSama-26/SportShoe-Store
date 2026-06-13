package com.example.server.core.client.voucher.dto;

import com.example.server.core.client.dathang.dto.DatHangItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record KiemTraVoucherRequest(
        @NotNull(message = "Thiếu thông tin khách hàng")
        UUID khachHangId,

        @NotEmpty(message = "Giỏ hàng đang trống")
        List<@Valid DatHangItemRequest> sanPhams,

        @NotBlank(message = "Vui lòng nhập mã giảm giá")
        String maPhieu
) {}
