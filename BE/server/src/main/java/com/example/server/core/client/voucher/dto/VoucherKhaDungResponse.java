package com.example.server.core.client.voucher.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Một voucher khả dụng để hiển thị cho khách chọn ở trang thanh toán.
 *
 * @param loai kiểu giảm: 1 = phần trăm, 2 = tiền mặt
 * @param tienGiam số tiền giảm ước tính trên giỏ hiện tại
 * @param rieng true nếu là voucher cá nhân được gửi riêng cho khách
 * @param apDung true nếu giỏ hiện tại đã đủ điều kiện tối thiểu để dùng
 */
public record VoucherKhaDungResponse(
        Integer phieuId,
        String ma,
        String ten,
        Integer loai,
        BigDecimal giaTri,
        BigDecimal giamToiDa,
        BigDecimal giaTriToiThieu,
        BigDecimal tienGiam,
        boolean rieng,
        boolean apDung,
        Instant ngayKetThuc
) {}
