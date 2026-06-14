package com.example.server.core.admin.quanlykhuyenmai.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DotGiamGiaRequest {

    @NotBlank(message = "Mã đợt giảm giá không được để trống")
    @Size(max = 100, message = "Mã đợt giảm giá không được vượt quá 100 ký tự")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Mã đợt giảm giá chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới"
    )
    private String ma;

    @NotBlank(message = "Tên đợt giảm giá không được để trống")
    @Size(max = 200, message = "Tên đợt giảm giá không được vượt quá 200 ký tự")
    private String ten;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String moTa;

    @NotNull(message = "Loại giảm không được để trống")
    @Min(value = 1, message = "Loại giảm không hợp lệ")
    @Max(value = 1, message = "Loại giảm không hợp lệ")
    private Integer loaiGiam;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    @DecimalMax(value = "100.00", message = "Phần trăm giảm không được vượt quá 100%")
    private BigDecimal giaTriGiam;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate ngayKetThuc;

    @Min(value = 0, message = "Trạng thái không hợp lệ")
    @Max(value = 4, message = "Trạng thái không hợp lệ")
    private Integer kichHoat;
}
