package com.example.server.core.admin.thongbao.aspect;

import com.example.server.core.admin.thongbao.service.ThongBaoService;
import com.example.server.entity.GiayChiTiet;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TonKhoThongBaoAspect {

    private final ThongBaoService thongBaoService;

    public TonKhoThongBaoAspect(ThongBaoService thongBaoService) {
        this.thongBaoService = thongBaoService;
    }

    @AfterReturning(
            pointcut = "execution(* com.example.server.repository.GiayChiTietRepository.save(..)) && args(giayChiTiet)",
            returning = "saved"
    )
    public void afterGiayChiTietSaved(Object giayChiTiet, Object saved) {
        if (saved instanceof GiayChiTiet gct) {
            if (gct.getSoLuong() != null && gct.getSoLuong() <= 5) {
                try {
                    String tenGiay = gct.getGiay() != null ? gct.getGiay().getTen() : "Sản phẩm";
                    String giayId = gct.getGiay() != null ? String.valueOf(gct.getGiay().getId()) : "";
                    thongBaoService.taoThongBao(
                            "Cảnh báo hết hàng",
                            "Sản phẩm \"" + tenGiay + "\" (Mã: " + gct.getMaBienThe() + ") sắp hết hàng, chỉ còn lại " + gct.getSoLuong() + " sản phẩm.",
                            "STOCK",
                            "/admin/bien-the-san-pham?variant=" + gct.getMaBienThe()
                    );
                } catch (Exception e) {
                    System.err.println("[TonKhoThongBaoAspect] Lỗi tạo thông báo tồn kho: " + e.getMessage());
                }
            }
        }
    }
}
