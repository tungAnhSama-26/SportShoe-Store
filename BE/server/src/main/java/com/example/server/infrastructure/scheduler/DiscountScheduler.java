package com.example.server.infrastructure.scheduler;

import com.example.server.core.admin.quanlykhuyenmai.service.DotGiamGiaSanPhamService;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscountScheduler {

    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    private final DotGiamGiaSanPhamService dotGiamGiaSanPhamService;

    /**
     * Tác vụ quét và cập nhật lại giá bán trong DB.
     * Chạy mỗi phút 1 lần để đảm bảo giá được cập nhật ngay khi đợt giảm giá bắt đầu hoặc kết thúc.
     */
    @Scheduled(cron = "0 * * * * *")
    public void scanAndSyncPrices() {
        log.info("Bắt đầu quét và đồng bộ giá bán khuyến mãi...");
        
        try {
            // Lấy danh sách tất cả các ID biến thể giày có tham gia bất kỳ đợt khuyến mãi nào
            List<Integer> giayChiTietIds = dotGiamGiaSanPhamRepository.findDistinctGiayChiTietIds();
            
            if (giayChiTietIds.isEmpty()) {
                log.info("Không có sản phẩm nào cần đồng bộ giá.");
                return;
            }

            log.info("Đang đồng bộ giá cho {} biến thể sản phẩm...", giayChiTietIds.size());
            
            for (Integer id : giayChiTietIds) {
                dotGiamGiaSanPhamService.updateGiaBanForGiayChiTiet(id);
            }
            
            log.info("Đồng bộ giá bán hoàn tất.");
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đồng bộ giá khuyến mãi: {}", e.getMessage(), e);
        }
    }
}
