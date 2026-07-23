package com.example.server.core.client.donhang.scheduler;

import com.example.server.core.client.thongbao.service.ClientThongBaoService;
import com.example.server.core.realtime.hoadon.HoaDonRealtimePublisher;
import com.example.server.entity.HoaDon;
import com.example.server.entity.LichSuHoaDon;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.LichSuHoaDonRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tự động chuyển trạng thái sang "Hoàn thành" cho đơn đã giao + thanh toán thành công quá 3 ngày.
 */
@Component
public class TuDongNhanHangScheduler {

    private static final int TRANG_THAI_HOAN_THANH = 5;
    private static final long SO_NGAY_TU_DONG_NHAN = 3;

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonRealtimePublisher hoaDonRealtimePublisher;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final ClientThongBaoService clientThongBaoService;

    public TuDongNhanHangScheduler(
            HoaDonRepository hoaDonRepository,
            HoaDonRealtimePublisher hoaDonRealtimePublisher,
            LichSuHoaDonRepository lichSuHoaDonRepository,
            ClientThongBaoService clientThongBaoService
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonRealtimePublisher = hoaDonRealtimePublisher;
        this.lichSuHoaDonRepository = lichSuHoaDonRepository;
        this.clientThongBaoService = clientThongBaoService;
    }

    /** Chạy mỗi giờ: đơn Đã giao + Đã thanh toán quá 3 ngày -> tự động chuyển Hoàn thành. */
    @Scheduled(fixedRate = 3_600_000L)
    @Transactional
    public void tuDongXacNhanNhanHang() {
        Instant moc = Instant.now().minus(SO_NGAY_TU_DONG_NHAN, ChronoUnit.DAYS);
        List<HoaDon> dsHoaDon =
                hoaDonRepository.findDonDaGiaoDaThanhToanQuaHan(moc);
        for (HoaDon hd : dsHoaDon) {
            Instant now = Instant.now();
            hd.setDaNhanHang(true);
            hd.setTrangThai(TRANG_THAI_HOAN_THANH);
            hd.setNgayCapNhat(now);
            hoaDonRepository.save(hd);

            // Ghi lịch sử hóa đơn
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hd);
            lichSu.setNhanVien(null);
            lichSu.setTrangThai("Hoàn thành");
            lichSu.setGhiChu("Tự động hoàn thành sau 3 ngày giao hàng thành công");
            lichSu.setNgayTao(now);
            lichSuHoaDonRepository.save(lichSu);

            hoaDonRealtimePublisher.publishAfterCommit(hd, "TU_DONG_NHAN_HANG");
            hoaDonRealtimePublisher.publishAfterCommit(hd, "TRANG_THAI");

            // Báo vào chuông thông báo của khách.
            if (hd.getKhachHang() != null) {
                clientThongBaoService.guiChoKhach(
                        hd.getKhachHang().getId(),
                        "DON_HANG",
                        "Cập nhật đơn hàng",
                        "Đơn hàng " + hd.getMa() + " đã tự động chuyển sang \"Hoàn thành\"",
                        "/khachhang/don-hang/" + hd.getId());
            }
        }
    }
}

