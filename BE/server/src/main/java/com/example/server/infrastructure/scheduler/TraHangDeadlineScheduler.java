package com.example.server.infrastructure.scheduler;

import com.example.server.core.admin.quanlytrahang.domain.TraHangPolicy;
import com.example.server.core.admin.quanlytrahang.domain.TrangThaiPhieuTraHang;
import com.example.server.core.admin.quanlytrahang.service.TraHangService;
import com.example.server.repository.PhieuTraHangRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TraHangDeadlineScheduler {

    private final PhieuTraHangRepository phieuTraHangRepository;
    private final TraHangService traHangService;
    private final TraHangPolicy traHangPolicy;

    public TraHangDeadlineScheduler(
            PhieuTraHangRepository phieuTraHangRepository,
            TraHangService traHangService,
            TraHangPolicy traHangPolicy
    ) {
        this.phieuTraHangRepository = phieuTraHangRepository;
        this.traHangService = traHangService;
        this.traHangPolicy = traHangPolicy;
    }

    @Scheduled(fixedDelayString = "${app.returns.expiration-check-ms:60000}")
    public void huyPhieuQuaHanGuiHang() {
        List<Integer> phieuIds = phieuTraHangRepository.findIdsQuaHanGuiHang(
                TrangThaiPhieuTraHang.CHO_KHACH_GUI_HANG.getMa(),
                traHangPolicy.mocQuaHanGuiHang()
        );

        for (Integer phieuId : phieuIds) {
            try {
                traHangService.huyQuaHanGuiHang(phieuId);
            } catch (RuntimeException exception) {
                log.warn("Không thể tự động hủy phiếu trả hàng quá hạn id={}", phieuId, exception);
            }
        }
    }
}
