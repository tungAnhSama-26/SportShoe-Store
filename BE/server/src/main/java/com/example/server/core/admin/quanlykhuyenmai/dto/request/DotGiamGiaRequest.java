package com.example.server.core.admin.quanlykhuyenmai.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DotGiamGiaRequest {
    @NotBlank(message = "Mã không được để trống")
    private String ma;

    @NotBlank(message = "Tên đợt không được để trống")
    private String ten;

    private String moTa;

    @NotNull(message = "Loại giảm không được để trống")
    private Integer loaiGiam;

    @NotNull(message = "Giá trị giảm không được để trống")
    @Min(value = 1, message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal giaTriGiam;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate ngayKetThuc;

    private Integer kichHoat;

    private LocalDate ngayTao;

    private LocalDate ngayCapNhat;
}
