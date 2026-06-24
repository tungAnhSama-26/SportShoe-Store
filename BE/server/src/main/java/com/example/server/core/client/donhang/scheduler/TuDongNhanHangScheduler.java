package com.example.server.core.client.donhang.scheduler;

import com.example.server.core.realtime.hoadon.HoaDonRealtimePublisher;
import com.example.server.entity.HoaDon;
import com.example.server.repository.HoaDonRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tự động xác nhận "Đã nhận hàng" cho đơn online đã Hoàn thành quá 3 ngày mà khách chưa bấm nhận.
 * Sau khi tự nhận, đơn coi như đã nhận hàng (khách không còn yêu cầu trả hàng được nữa).
 */
@Component
public class TuDongNhanHangScheduler {

    private static final int TRANG_THAI_HOAN_THANH = 5;
    private static final long SO_NGAY_TU_DONG_NHAN = 3;

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonRealtimePublisher hoaDonRealtimePublisher;

    public TuDongNhanHangScheduler(
            HoaDonRepository hoaDonRepository,
            HoaDonRealtimePublisher hoaDonRealtimePublisher
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonRealtimePublisher = hoaDonRealtimePublisher;
    }

    /** Chạy mỗi giờ: đơn Hoàn thành quá 3 ngày chưa bấm nhận -> tự đánh dấu "Đã nhận hàng". */
    @Scheduled(fixedRate = 3_600_000L)
    @Transactional
    public void tuDongXacNhanNhanHang() {
        Instant moc = Instant.now().minus(SO_NGAY_TU_DONG_NHAN, ChronoUnit.DAYS);
        List<HoaDon> dsHoaDon =
                hoaDonRepository.findDonHoanThanhChuaNhanQuaHan(TRANG_THAI_HOAN_THANH, moc);
        for (HoaDon hd : dsHoaDon) {
            hd.setDaNhanHang(true);
            hoaDonRepository.save(hd);
            hoaDonRealtimePublisher.publishAfterCommit(hd, "TU_DONG_NHAN_HANG");
        }
    }
}
