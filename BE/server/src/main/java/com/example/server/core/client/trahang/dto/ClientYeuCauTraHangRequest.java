package com.example.server.core.client.trahang.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ClientYeuCauTraHangRequest(
        @NotNull(message = "Vui lòng chọn hóa đơn")
        Integer hoaDonId,

        @NotBlank(message = "Vui lòng chọn lý do trả hàng")
        String lyDoMa,

        @NotBlank(message = "Vui lòng nhập mô tả chi tiết")
        String moTa,

        @NotNull(message = "Vui lòng chọn hình thức nhận tiền hoàn")
        Integer hinhThucHoan,

        @NotEmpty(message = "Vui lòng chọn ít nhất một sản phẩm để trả")
        List<@Valid SanPhamTraItem> sanPhams,

        @NotEmpty(message = "Vui lòng tải lên ít nhất một hình ảnh minh chứng")
        List<@NotBlank(message = "Đường dẫn hình ảnh minh chứng không được để trống") String> hinhAnhs
) {
    public record SanPhamTraItem(
            @NotNull(message = "Vui lòng chọn sản phẩm cần trả")
            Integer hoaDonChiTietId,

            @NotNull(message = "Vui lòng nhập số lượng cần trả")
            @Min(value = 1, message = "Số lượng cần trả phải lớn hơn 0")
            Integer soLuong,

            String ghiChu
    ) {}
}
