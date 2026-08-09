package com.example.server.core.client.dathang.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import com.example.server.infrastructure.address.DiaChiHaiCapRequest;
import java.util.List;
import java.util.UUID;

public record DatHangRequest(
        // Khách vãng lai (chưa đăng nhập) -> null. Khách có tài khoản -> id khách.
        UUID khachHangId,

        @NotEmpty(message = "Giỏ hàng đang trống")
        List<@Valid DatHangItemRequest> sanPhams,

        @NotBlank(message = "Vui lòng nhập tên người nhận")
        String tenNguoiNhan,

        @NotBlank(message = "Vui lòng nhập số điện thoại")
        @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải gồm 10 chữ số")
        String sdtNguoiNhan,

        @NotNull(message = "Vui lòng nhập địa chỉ giao hàng")
        @Valid
        DiaChiHaiCapRequest diaChiGiaoHang,

        // "COD" hoặc "VNPAY" (VNPay xử lý sau).
        String hinhThucThanhToan,

        // Mã giảm giá (tùy chọn).
        String maPhieuGiamGia,

        String ghiChu,

        // Email nhận xác nhận đơn cho khách vãng lai (không có tài khoản). Tùy chọn.
        @Email(message = "Email không hợp lệ")
        String emailNguoiNhan
) {}
