package com.example.server.core.realtime.sanpham;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Phát tín hiệu catalog định kỳ để giỏ hàng khách tự đồng bộ giá — bắt được đợt giảm giá
 * TỰ kích hoạt / hết hạn theo lịch giờ (không có thao tác admin nào để bắt sự kiện realtime).
 *
 * <p>Tín hiệu chỉ thực sự gửi tới các phiên giỏ đang mở; nếu không ai mở giỏ thì không tốn gì.</p>
 */
@Component
public class SanPhamRealtimeScheduler {

    private final SanPhamRealtimePublisher publisher;

    public SanPhamRealtimeScheduler(SanPhamRealtimePublisher publisher) {
        this.publisher = publisher;
    }

    @Scheduled(fixedRate = 60_000L)
    public void phatDinhKy() {
        publisher.phatSauCommit("DINH_KY");
    }
}
