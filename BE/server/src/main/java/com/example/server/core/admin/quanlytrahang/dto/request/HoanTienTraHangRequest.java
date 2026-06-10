package com.example.server.core.admin.quanlytrahang.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HoanTienTraHangRequest(
        @NotNull(message = "Vui lòng chọn hình thức hoàn tiền")
        Integer hinhThucHoan,

        @Size(max = 200, message = "Mã giao dịch không được vượt quá 200 ký tự")
        String maGiaoDich,

        @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
        String ghiChu
) {
}
