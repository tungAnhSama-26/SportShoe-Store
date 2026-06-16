package com.example.server.core.admin.quanlytrahang.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TaoPhieuTraHangRequest(
        @NotNull(message = "Vui lòng chọn hóa đơn")
        Integer hoaDonId,

        @NotBlank(message = "Vui lòng chọn lý do trả hàng")
        @Size(max = 50, message = "Mã lý do không được vượt quá 50 ký tự")
        String lyDoMa,

        @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
        String moTa,

        @NotNull(message = "Vui lòng chọn hình thức hoàn tiền")
        @Min(value = 1, message = "Hình thức hoàn tiền không hợp lệ")
        @Max(value = 3, message = "Hình thức hoàn tiền không hợp lệ")
        Integer hinhThucHoan,

        @NotEmpty(message = "Vui lòng chọn ít nhất một sản phẩm cần trả")
        @Size(max = 100, message = "Phiếu trả hàng không được vượt quá 100 dòng sản phẩm")
        List<@Valid SanPhamTraRequest> sanPhams
) {
}
