package com.example.server.core.client.vanchuyen.dto;

import java.math.BigDecimal;

/**
 * Kết quả tính phí vận chuyển.
 *
 * @param phiVanChuyen số tiền phí ship
 * @param uocTinh true nếu là phí ước tính (GHN chưa khả dụng / địa chỉ chưa khớp), false nếu lấy thật từ GHN
 * @param moTa mô tả nguồn phí
 */
public record PhiVanChuyenResponse(
        BigDecimal phiVanChuyen,
        boolean uocTinh,
        String moTa
) {
}
