package com.example.server.core.client.dathang.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

public record DatHangRequest(
        @NotNull(message = "Thiếu thông tin khách hàng")
        UUID khachHangId,

        @NotEmpty(message = "Giỏ hàng đang trống")
        List<@Valid DatHangItemRequest> sanPhams,

        @NotBlank(message = "Vui lòng nhập tên người nhận")
        String tenNguoiNhan,

        @NotBlank(message = "Vui lòng nhập số điện thoại")
        @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải gồm 10 chữ số")
        String sdtNguoiNhan,

        @NotBlank(message = "Vui lòng nhập tỉnh/thành phố")
        String tinhThanh,

        @NotBlank(message = "Vui lòng nhập quận/huyện")
        String quanHuyen,

        @NotBlank(message = "Vui lòng nhập phường/xã")
        String phuongXa,

        @NotBlank(message = "Vui lòng nhập địa chỉ cụ thể")
        String diaChiCuThe,

        // "COD" hoặc "VNPAY" (VNPay xử lý sau).
        String hinhThucThanhToan,

        // Mã giảm giá (tùy chọn).
        String maPhieuGiamGia,

        String ghiChu,

        // GHN: mã quận/huyện + phường/xã (để tính phí chính xác, tùy chọn).
        Integer toDistrictId,
        String toWardCode
) {}
