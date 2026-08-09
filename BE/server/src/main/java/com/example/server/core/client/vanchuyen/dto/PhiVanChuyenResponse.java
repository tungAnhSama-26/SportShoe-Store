package com.example.server.core.client.vanchuyen.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Kết quả tính phí vận chuyển.
 *
 * @param phiVanChuyen số tiền phí ship
 * @param uocTinh true nếu một đơn vị cấp xã mới ánh xạ tới nhiều tuyến GHN cũ, false nếu có một tuyến duy nhất
 * @param moTa mô tả nguồn phí
 */
public record PhiVanChuyenResponse(
        BigDecimal phiVanChuyen,
        boolean uocTinh,
        String moTa,
        String nguonTinhPhi,
        boolean giaCu,
        Instant thoiDiemBaoGia,
        LocalDate ngayHieuLucBangGia
) {
}
