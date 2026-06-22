package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.response.SanPhamTaiQuayResponse;
import com.example.server.entity.DotGiamGia;
import com.example.server.entity.DotGiamGiaSanPham;
import com.example.server.entity.GiayChiTiet;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.SO_SAN_PHAM_TIM_TOI_DA;

@Component
public class SanPhamTaiQuayService {

    private final GiayChiTietRepository giayChiTietRepository;
    private final HinhAnhGiayRepository hinhAnhGiayRepository;
    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;

    public SanPhamTaiQuayService(
            GiayChiTietRepository giayChiTietRepository,
            HinhAnhGiayRepository hinhAnhGiayRepository,
            DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository
    ) {
        this.giayChiTietRepository = giayChiTietRepository;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
        this.dotGiamGiaSanPhamRepository = dotGiamGiaSanPhamRepository;
    }

    public List<SanPhamTaiQuayResponse> timSanPham(String keyword) {
        List<GiayChiTiet> danhSachChiTiet = giayChiTietRepository.searchForCounterSale(chuanHoaTuKhoa(keyword))
                .stream()
                .limit(SO_SAN_PHAM_TIM_TOI_DA)
                .toList();
        Map<Integer, String> hinhAnhMap = buildImageMap(danhSachChiTiet);

        return danhSachChiTiet.stream()
                .map(chiTiet -> new SanPhamTaiQuayResponse(
                        chiTiet.getId(),
                        chiTiet.getGiay().getMa(),
                        chiTiet.getGiay().getTen(),
                        chiTiet.getSku(),
                        chiTiet.getMaBienThe(),
                        chiTiet.getSoLuong(),
                        chiTiet.getGiaGoc(),
                        layGiaBanThucTe(chiTiet),
                        hinhAnhMap.get(chiTiet.getId()),
                        chiTiet.getGiay().getLoaiGiay() != null ? chiTiet.getGiay().getLoaiGiay().getTen() : null,
                        chiTiet.getGiay().getThuongHieu() != null ? chiTiet.getGiay().getThuongHieu().getTen() : null,
                        chiTiet.getGiay().getGiayThuocTinh() != null && chiTiet.getGiay().getGiayThuocTinh().getDeGiay() != null
                                ? chiTiet.getGiay().getGiayThuocTinh().getDeGiay().getTen() : null,
                        chiTiet.getGiay().getGiayThuocTinh() != null && chiTiet.getGiay().getGiayThuocTinh().getCoGiay() != null
                                ? chiTiet.getGiay().getGiayThuocTinh().getCoGiay().getTen() : null,
                        chiTiet.getGiay().getGiayThuocTinh() != null && chiTiet.getGiay().getGiayThuocTinh().getCongNgheDem() != null
                                ? chiTiet.getGiay().getGiayThuocTinh().getCongNgheDem().getTen() : null,
                        chiTiet.getMauSac() != null ? chiTiet.getMauSac().getTen() : null,
                        chiTiet.getKichCo() != null ? chiTiet.getKichCo().getGiaTri() : null,
                        chiTiet.getGiay().getGiayThuocTinh() != null && chiTiet.getGiay().getGiayThuocTinh().getTrongLuong() != null
                                ? chiTiet.getGiay().getGiayThuocTinh().getTrongLuong().getGiaTri() + " gram" : null
                ))
                .toList();
    }

    private Map<Integer, String> buildImageMap(List<GiayChiTiet> danhSachChiTiet) {
        Map<Integer, String> imageMap = new HashMap<>();
        if (danhSachChiTiet.isEmpty()) {
            return imageMap;
        }

        List<Integer> chiTietIds = danhSachChiTiet.stream()
                .map(GiayChiTiet::getId)
                .toList();

        for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayChiTietIds(chiTietIds)) {
            if (row[0] != null && row[1] != null) {
                Integer chiTietId = ((Number) row[0]).intValue();
                imageMap.putIfAbsent(chiTietId, (String) row[1]);
            }
        }
        return imageMap;
    }

    public BigDecimal layGiaBanThucTe(GiayChiTiet gct) {
        if (gct == null) {
            return BigDecimal.ZERO;
        }

        List<DotGiamGiaSanPham> activeDiscounts = dotGiamGiaSanPhamRepository.findActiveByGiayChiTietId(gct.getId());
        if (activeDiscounts == null || activeDiscounts.isEmpty()) {
            return gct.getGiaBan();
        }

        LocalDate now = LocalDate.now();
        BigDecimal giaThapNhat = gct.getGiaBan();

        for (DotGiamGiaSanPham link : activeDiscounts) {
            DotGiamGia dgg = link.getDotGiamGia();
            if (dgg == null || dgg.getKichHoat() == null || dgg.getKichHoat() == 0) {
                continue;
            }
            if (dgg.getNgayBatDau() != null && now.isBefore(dgg.getNgayBatDau())) {
                continue;
            }
            if (dgg.getNgayKetThuc() != null && now.isAfter(dgg.getNgayKetThuc())) {
                continue;
            }

            BigDecimal giaSauGiam = gct.getGiaBan();
            if (dgg.getLoaiGiam() != null && dgg.getLoaiGiam() == 1) { // %
                BigDecimal discountAmount = gct.getGiaBan().multiply(dgg.getGiaTriGiam())
                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                giaSauGiam = gct.getGiaBan().subtract(discountAmount);
            } else if (dgg.getLoaiGiam() != null && dgg.getLoaiGiam() == 2) { // fixed
                giaSauGiam = gct.getGiaBan().subtract(dgg.getGiaTriGiam());
            }

            if (giaSauGiam.compareTo(BigDecimal.ZERO) < 0) {
                giaSauGiam = BigDecimal.ZERO;
            }

            if (giaSauGiam.compareTo(giaThapNhat) < 0) {
                giaThapNhat = giaSauGiam;
            }
        }

        return giaThapNhat;
    }

    private String chuanHoaTuKhoa(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
