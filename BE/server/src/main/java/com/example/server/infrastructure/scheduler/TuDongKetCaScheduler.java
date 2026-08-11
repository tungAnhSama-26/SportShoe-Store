package com.example.server.infrastructure.scheduler;

import com.example.server.core.admin.nhanVien.service.GiaoCaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TuDongKetCaScheduler {

    private final GiaoCaService giaoCaService;

    /**
     * Tác vụ tự động quét và kết thúc các ca làm việc còn đang mở hoặc chờ bàn giao.
     * Chạy định kỳ vào đúng 00:00:00 (12h đêm) mỗi ngày theo múi giờ Việt Nam.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void tuDongKetCaMoiDem() {
        log.info("[Scheduler] Bắt đầu quét và tự động kết ca lúc 00:00...");
        try {
            giaoCaService.tuDongKetCaChuaDong();
            log.info("[Scheduler] Hoàn tất tự động kết ca lúc 00:00.");
        } catch (Exception e) {
            log.error("[Scheduler] Lỗi khi thực hiện tự động kết ca: {}", e.getMessage(), e);
        }
    }
}
