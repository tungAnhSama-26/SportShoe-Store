package com.example.server.core.realtime.sanpham;

import java.time.Instant;

/** Sự kiện thay đổi catalog (ngừng bán / đổi giá / đợt giảm giá...) để giỏ hàng tự đồng bộ lại. */
public record SanPhamRealtimeEvent(
        String eventId,
        String loaiSuKien,
        Instant thoiGian
) {
}
