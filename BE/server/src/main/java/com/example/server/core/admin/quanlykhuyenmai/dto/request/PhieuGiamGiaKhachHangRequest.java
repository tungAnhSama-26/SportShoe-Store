package com.example.server.core.admin.quanlykhuyenmai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PhieuGiamGiaKhachHangRequest {

    @NotNull(message = "Phiếu giảm giá không được để trống")
    @Min(value = 1, message = "Phiếu giảm giá không hợp lệ")
    private Integer phieuGiamGiaId;

    @NotBlank(message = "Email khách hàng không được để trống")
    @Email(message = "Email khách hàng không đúng định dạng")
    private String email;

    private LocalDate ngaySuDung;

    @Min(value = 0, message = "Trạng thái không hợp lệ")
    @Max(value = 4, message = "Trạng thái không hợp lệ")
    private Integer trangThai;
}
