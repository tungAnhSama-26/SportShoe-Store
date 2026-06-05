package com.example.server.infrastructure.scheduler;

import com.example.server.core.admin.quanlykhuyenmai.service.DotGiamGiaSanPhamService;
import com.example.server.entity.PhieuGiamGiaKhachHang;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private final EmailService emailService;

    /**
     * Tracking các ID liên kết (phieu_giam_gia_khach_hang.id) đã được gửi email hết hạn.
     * Dùng in-memory Set để tránh gửi lặp mỗi phút. Reset khi server restart (chấp nhận được).
     */
    private final Set<Integer> sentExpiredEmailIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

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

            // Gửi email thông báo hết hạn cho các khách hàng chưa dùng phiếu đã hết hạn
            sendExpiredVoucherEmails();
            
            log.info("Đồng bộ hoàn tất.");
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đồng bộ: {}", e.getMessage(), e);
        }
    }

    /**
     * Tìm các liên kết hết hạn chưa dùng và gửi email thông báo lần đầu tiên.
     * Những ID đã gửi sẽ được ghi nhớ để không gửi lặp.
     */
    private void sendExpiredVoucherEmails() {
        try {
            List<PhieuGiamGiaKhachHang> expiredList =
                    phieuGiamGiaKhachHangRepository.findExpiredUnused();

            for (PhieuGiamGiaKhachHang lienKet : expiredList) {
                Integer lienKetId = lienKet.getId();
                // Chỉ gửi nếu chưa gửi email hết hạn cho liên kết này
                if (sentExpiredEmailIds.contains(lienKetId)) {
                    continue;
                }
                String email = lienKet.getKhachHang().getEmail();
                if (email != null && !email.isBlank()) {
                    emailService.sendVoucherExpiredEmailAsync(
                            email,
                            lienKet.getKhachHang().getHoTen(),
                            lienKet.getPhieuGiamGia().getMa(),
                            lienKet.getPhieuGiamGia().getTen(),
                            lienKet.getPhieuGiamGia().getNgayKetThuc()
                    );
                    sentExpiredEmailIds.add(lienKetId);
                    log.info("Đã gửi email hết hạn phiếu '{}' tới {}", lienKet.getPhieuGiamGia().getMa(), email);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo phiếu hết hạn: {}", e.getMessage(), e);
        }
    }
}
