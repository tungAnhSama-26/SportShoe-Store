package com.example.server.core.realtime.hoadon;

import java.time.Instant;
import java.util.UUID;

public record HoaDonRealtimeEvent(
        String eventId,
        Integer hoaDonId,
        String maHoaDon,
        UUID khachHangId,
        Integer trangThai,
        String loaiSuKien,
        Instant thoiGian
) {
}
