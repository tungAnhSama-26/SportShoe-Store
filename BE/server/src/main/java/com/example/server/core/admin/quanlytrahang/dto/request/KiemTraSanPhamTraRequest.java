package com.example.server.core.admin.quanlytrahang.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record KiemTraSanPhamTraRequest(
        @NotNull(message = "Thiếu chi tiết sản phẩm trả")
        Integer chiTietTraHangId,

        @NotNull(message = "Vui lòng nhập số lượng đã nhận")
        @Min(value = 0, message = "Số lượng đã nhận không được âm")
        Integer soLuongNhan,

        @NotNull(message = "Vui lòng nhập số lượng chấp nhận")
        @Min(value = 0, message = "Số lượng chấp nhận không được âm")
        Integer soLuongChapNhan,

        @Size(max = 500, message = "Tình trạng sản phẩm không được vượt quá 500 ký tự")
        String tinhTrangSanPham,

        @NotNull(message = "Vui lòng xác định có nhập lại tồn kho hay không")
        Boolean nhapLaiTonKho,

        @Size(max = 10, message = "Chỉ được tải tối đa 10 hình ảnh minh chứng")
        List<
                @Size(max = 1000, message = "Đường dẫn hình ảnh không được vượt quá 1000 ký tự")
                String
        > hinhAnhs
) {
}
