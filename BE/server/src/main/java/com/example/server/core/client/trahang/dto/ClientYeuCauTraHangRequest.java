package com.example.server.core.client.trahang.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ClientYeuCauTraHangRequest(
        @NotNull(message = "Vui lòng chọn hóa đơn")
        Integer hoaDonId,

        @NotBlank(message = "Vui lòng chọn lý do trả hàng")
        @Size(max = 50, message = "Mã lý do không được vượt quá 50 ký tự")
        String lyDoMa,

        @NotBlank(message = "Vui lòng nhập mô tả chi tiết")
        @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
        String moTa,

        @NotNull(message = "Vui lòng chọn hình thức nhận tiền hoàn")
        @Min(value = 1, message = "Hình thức nhận tiền hoàn không hợp lệ")
        @Max(value = 3, message = "Hình thức nhận tiền hoàn không hợp lệ")
        Integer hinhThucHoan,

        @NotEmpty(message = "Vui lòng chọn ít nhất một sản phẩm để trả")
        @Size(max = 100, message = "Một yêu cầu không được vượt quá 100 dòng sản phẩm")
        List<@Valid SanPhamTraItem> sanPhams,

        @NotEmpty(message = "Vui lòng tải lên ít nhất một hình ảnh minh chứng")
        @Size(max = 10, message = "Chỉ được tải tối đa 10 hình ảnh minh chứng")
        List<
                @NotBlank(message = "Đường dẫn hình ảnh minh chứng không được để trống")
                @Size(max = 1000, message = "Đường dẫn hình ảnh không được vượt quá 1000 ký tự")
                String
                > hinhAnhs
) {
    public record SanPhamTraItem(
            @NotNull(message = "Vui lòng chọn sản phẩm cần trả")
            Integer hoaDonChiTietId,

            @NotNull(message = "Vui lòng nhập số lượng cần trả")
            @Min(value = 1, message = "Số lượng cần trả phải lớn hơn 0")
            Integer soLuong,

            @Size(max = 500, message = "Ghi chú sản phẩm không được vượt quá 500 ký tự")
            String ghiChu
    ) {}
}
