package com.example.server.infrastructure.scheduler;

import com.example.server.core.admin.quanlykhuyenmai.service.DotGiamGiaSanPhamService;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import com.example.server.repository.PhieuGiamGiaRepository;
import com.example.server.repository.PhieuGiamGiaKhachHangRepository;
import com.example.server.repository.DotGiamGiaRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscountScheduler {

    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    private final DotGiamGiaSanPhamService dotGiamGiaSanPhamService;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;

    /**
     * Tác vụ quét và cập nhật lại giá bán trong DB và trạng thái phiếu giảm giá.
     * Chạy mỗi phút 1 lần để đảm bảo giá và trạng thái được cập nhật ngay khi đợt giảm giá bắt đầu hoặc kết thúc.
     */
    @Scheduled(cron = "0 * * * * *")
    public void scanAndSyncPrices() {
        log.info("Bắt đầu quét và đồng bộ giá bán khuyến mãi và phiếu giảm giá...");
        
        try {
            // Lấy danh sách tất cả các ID biến thể giày có tham gia bất kỳ đợt khuyến mãi nào
            List<Integer> giayChiTietIds = dotGiamGiaSanPhamRepository.findDistinctGiayChiTietIds();
            
            if (giayChiTietIds.isEmpty()) {
                log.info("Không có sản phẩm nào cần đồng bộ giá.");
            } else {
                log.info("Đang đồng bộ giá cho {} biến thể sản phẩm...", giayChiTietIds.size());
                for (Integer id : giayChiTietIds) {
                    dotGiamGiaSanPhamService.updateGiaBanForGiayChiTiet(id);
                }
            }
            
            // Cập nhật trạng thái phiếu giảm giá và đợt giảm giá
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
