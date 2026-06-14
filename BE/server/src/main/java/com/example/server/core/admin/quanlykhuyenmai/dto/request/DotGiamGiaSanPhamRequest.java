package com.example.server.core.admin.quanlykhuyenmai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DotGiamGiaSanPhamRequest {

    @NotNull(message = "Đợt giảm giá không được để trống")
    @Min(value = 1, message = "Đợt giảm giá không hợp lệ")
    private Integer dotGiamGiaId;

    @NotNull(message = "Biến thể sản phẩm không được để trống")
    @Min(value = 1, message = "Biến thể sản phẩm không hợp lệ")
    private Integer giayChiTietId;

    @NotNull(message = "Trạng thái không được để trống")
    @Min(value = 0, message = "Trạng thái không hợp lệ")
    @Max(value = 1, message = "Trạng thái không hợp lệ")
    private Integer trangThai;
}
