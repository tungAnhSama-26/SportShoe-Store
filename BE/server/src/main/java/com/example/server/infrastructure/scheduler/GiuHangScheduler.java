package com.example.server.infrastructure.scheduler;

import com.example.server.core.client.giohang.service.ClientGioHangService;
import com.example.server.entity.HoaDon;
import com.example.server.repository.HoaDonRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tự động hoàn tồn kho cho các giỏ đang giữ hàng đã quá hạn (khách thoát đột ngột,
 * đóng tab... mà client không kịp gọi hủy giữ). Chạy định kỳ.
 */
@Component
public class GiuHangScheduler {

    private static final Logger log = LoggerFactory.getLogger(GiuHangScheduler.class);

    private final HoaDonRepository hoaDonRepository;
    private final ClientGioHangService gioHangService;

    public GiuHangScheduler(HoaDonRepository hoaDonRepository, ClientGioHangService gioHangService) {
        this.hoaDonRepository = hoaDonRepository;
        this.gioHangService = gioHangService;
    }

    /** Mỗi 30 giây: hoàn tồn các giỏ giữ hàng đã quá hạn. */
    @Scheduled(fixedDelay = 30_000)
    public void hoanGiuHangQuaHan() {
        List<HoaDon> quaHan = hoaDonRepository.findByTrangThaiAndHanGiuHangIsNotNullAndHanGiuHangBefore(
                ClientGioHangService.TRANG_THAI_GIO, Instant.now());
        for (HoaDon gio : quaHan) {
            try {
                gioHangService.hoanTonChoHoaDon(gio);
            } catch (Exception e) {
                log.error("Không thể hoàn giữ hàng cho hóa đơn {}", gio.getId(), e);
            }
        }
    }
}
