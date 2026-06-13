package com.example.server.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.server.repository.PhieuGiamGiaRepository;
import com.example.server.repository.PhieuGiamGiaKhachHangRepository;
import com.example.server.repository.DotGiamGiaRepository;
import com.example.server.repository.GiayChiTietRepository;
import java.time.LocalDate;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscountScheduler {

    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final GiayChiTietRepository giayChiTietRepository;

    /**
     * Tác vụ quét và cập nhật lại giá bán trong DB và trạng thái phiếu giảm giá.
     * Chạy mỗi phút 1 lần để đảm bảo giá và trạng thái được cập nhật ngay khi đợt giảm giá bắt đầu hoặc kết thúc.
     */
    @Scheduled(cron = "0 * * * * *")
    public void scanAndSyncPrices() {
        log.info("Bắt đầu quét và đồng bộ giá bán khuyến mãi và phiếu giảm giá...");
        
        try {
            LocalDate today = LocalDate.now();
            Instant now = Instant.now();

            // 1. Cập nhật ngayCapNhat của các biến thể có đợt giảm giá thay đổi trạng thái (Bulk Update)
            int affectedVariants = giayChiTietRepository.touchAffectedVariants(today, now);
            if (affectedVariants > 0) {
                log.info("Đã đồng bộ thời gian cập nhật cho {} biến thể sản phẩm có khuyến mãi thay đổi trạng thái.", affectedVariants);
            }
            
            // 2. Cập nhật trạng thái phiếu giảm giá và đợt giảm giá (Bulk Update)
            log.info("Đang đồng bộ trạng thái phiếu giảm giá và đợt giảm giá...");
            dotGiamGiaRepository.capNhatTrangThaiTuDong();
            phieuGiamGiaRepository.capNhatTrangThaiTuDong();
            phieuGiamGiaKhachHangRepository.dongBoTrangThaiTuPhieuGiamGia();
            
            log.info("Đồng bộ hoàn tất.");
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đồng bộ: {}", e.getMessage(), e);
        }
    }
}
