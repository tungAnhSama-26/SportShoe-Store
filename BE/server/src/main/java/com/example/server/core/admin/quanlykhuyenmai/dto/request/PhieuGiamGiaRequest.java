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
public class PhieuGiamGiaRequest {

    @NotBlank(message = "Mã phiếu giảm giá không được để trống")
    @Size(max = 100, message = "Mã phiếu giảm giá không được vượt quá 100 ký tự")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Mã phiếu giảm giá chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới"
    )
    private String ma;

    @NotBlank(message = "Tên phiếu giảm giá không được để trống")
    @Size(max = 200, message = "Tên phiếu giảm giá không được vượt quá 200 ký tự")
    private String ten;

    @NotNull(message = "Loại giảm không được để trống")
    @Min(value = 1, message = "Loại giảm không hợp lệ")
    @Max(value = 2, message = "Loại giảm không hợp lệ")
    private Integer loai;

    @NotNull(message = "Loại phiếu không được để trống")
    @Min(value = 1, message = "Loại phiếu không hợp lệ")
    @Max(value = 2, message = "Loại phiếu không hợp lệ")
    private Integer loaiPhieu;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal giaTri;

    @DecimalMin(value = "0.00", message = "Giá trị đơn tối thiểu không được nhỏ hơn 0")
    private BigDecimal giaTriToiThieu;

    @DecimalMin(value = "0.00", message = "Mức giảm tối đa không được nhỏ hơn 0")
    @DecimalMax(value = "100000000.00", message = "Mức giảm tối đa không được vượt quá 100.000.000 VNĐ")
    private BigDecimal giamToiDa;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate ngayKetThuc;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phiếu phải lớn hơn 0")
    private Integer soLuong;

    private Integer trangThai;
}
