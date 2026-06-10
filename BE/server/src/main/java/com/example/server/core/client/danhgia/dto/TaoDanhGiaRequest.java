package com.example.server.core.client.danhgia.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record TaoDanhGiaRequest(
        @NotNull(message = "Thiếu thông tin khách hàng")
        UUID khachHangId,

        @NotNull(message = "Vui lòng chọn số sao")
        @Min(value = 1, message = "Số sao tối thiểu là 1")
        @Max(value = 5, message = "Số sao tối đa là 5")
        Integer soSao,

        @Size(max = 1000, message = "Nội dung đánh giá không quá 1000 ký tự")
        String noiDung
) {}
