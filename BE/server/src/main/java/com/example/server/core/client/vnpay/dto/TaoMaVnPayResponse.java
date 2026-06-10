package com.example.server.core.client.vnpay.dto;

/** Thông tin mã QR thanh toán VNPay (giả lập). */
public record TaoMaVnPayResponse(
        String token,
        String qrData,
        String maGiaoDich
) {}
