package com.example.server.core.admin.quanlykhuyenmai.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DotGiamGiaSanPhamBulkRequest {

    @NotNull(message = "Đợt giảm giá không được để trống")
    @Min(value = 1, message = "Đợt giảm giá không hợp lệ")
    private Integer dotGiamGiaId;

    @NotEmpty(message = "Phải chọn ít nhất một biến thể sản phẩm")
    private List<@NotNull(message = "Biến thể sản phẩm không hợp lệ")
            @Min(value = 1, message = "Biến thể sản phẩm không hợp lệ") Integer> giayChiTietIds;
}
