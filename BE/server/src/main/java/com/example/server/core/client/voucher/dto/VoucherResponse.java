package com.example.server.core.client.voucher.dto;

import java.math.BigDecimal;

/** Kết quả kiểm tra/áp mã giảm giá. */
public record VoucherResponse(
        Integer phieuId,
        String ma,
        String ten,
        BigDecimal tienGiam,
        BigDecimal tongTienHang,
        BigDecimal tongTienSauGiam
) {}
