package com.example.server.core.admin.thongKe.service;

import com.example.server.core.admin.thongKe.dto.request.ThongKeDashboardRequest;
import com.example.server.core.admin.thongKe.dto.response.ThongKeBoLocDaApDungResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeDashboardResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeGiaTriTheoKyResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeSanPhamResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeThuongHieuResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeTongQuanResponse;
import com.example.server.core.admin.thongKe.dto.response.ThuongHieuThongKeFilterResponse;
import com.example.server.entity.Giay;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.ThuongHieu;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.ThuongHieuRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThongKeService {

    private static final int TRANG_THAI_HOAT_DONG = 1;
    private static final List<Integer> TRANG_THAI_HOA_DON_HOP_LE = List.of(2, 3, 4);
    private static final ZoneId MUI_GIO_HE_THONG = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_THANG = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_NAM = DateTimeFormatter.ofPattern("yyyy");

    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final ThuongHieuRepository thuongHieuRepository;

    public ThongKeService(
            HoaDonChiTietRepository hoaDonChiTietRepository,
            GiayChiTietRepository giayChiTietRepository,
            KhachHangRepository khachHangRepository,
            ThuongHieuRepository thuongHieuRepository
    ) {
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.khachHangRepository = khachHangRepository;
        this.thuongHieuRepository = thuongHieuRepository;
    }

    @Transactional(readOnly = true)
    public ThongKeDashboardResponse layDuLieuThongKe(ThongKeDashboardRequest request) {
        BoLocThongKe boLoc = chuanHoaBoLoc(
                request.fromDate(),
                request.toDate(),
                request.brandId(),
                request.keyword(),
                request.periodType()
        );

        List<HoaDonChiTiet> tatCaDongBanHang = hoaDonChiTietRepository.findAllForThongKe(TRANG_THAI_HOA_DON_HOP_LE);
        List<GiayChiTiet> tatCaSanPham = giayChiTietRepository.findAllForThongKe();
        List<KhachHang> tatCaKhachHang = khachHangRepository.findByTrangThai(TRANG_THAI_HOAT_DONG);

        List<HoaDonChiTiet> dongBanHangTheoBoLoc = tatCaDongBanHang.stream()
                .filter(dong -> khopBoLocDongBanHang(dong, boLoc))
                .toList();

        List<GiayChiTiet> sanPhamTheoBoLoc = tatCaSanPham.stream()
                .filter(sanPham -> khopBoLocSanPham(sanPham.getGiay(), boLoc))
                .toList();

        long khachMoi = tatCaKhachHang.stream()
                .filter(khachHang -> namTrongKhoang(khachHang.getNgayTao(), boLoc))
                .count();

        return new ThongKeDashboardResponse(
                new ThongKeBoLocDaApDungResponse(
                        boLoc.kyThongKe().name(),
                        boLoc.tuNgay(),
                        boLoc.denNgay(),
                        boLoc.thuongHieuId(),
                        boLoc.keyword()
                ),
                taoTongQuan(dongBanHangTheoBoLoc, khachMoi),
                layThuongHieuBoLoc(),
                taoBieuDoBanHang(dongBanHangTheoBoLoc, boLoc),
                taoBieuDoThuongHieu(sanPhamTheoBoLoc),
                taoThongKeSanPham(dongBanHangTheoBoLoc, sanPhamTheoBoLoc)
        );
    }

    private ThongKeTongQuanResponse taoTongQuan(List<HoaDonChiTiet> dongBanHang, long khachMoi) {
        BigDecimal tongDoanhThu = dongBanHang.stream()
                .map(HoaDonChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long tongDonHang = dongBanHang.stream()
                .map(HoaDonChiTiet::getHoaDon)
                .map(HoaDon::getId)
                .distinct()
                .count();

        long sanPhamDaBan = dongBanHang.stream()
                .map(HoaDonChiTiet::getSoLuong)
                .mapToLong(soLuong -> soLuong == null ? 0L : soLuong.longValue())
                .sum();

        return new ThongKeTongQuanResponse(tongDoanhThu, tongDonHang, sanPhamDaBan, khachMoi);
    }

    private List<ThuongHieuThongKeFilterResponse> layThuongHieuBoLoc() {
        return thuongHieuRepository.findByTrangThaiOrderByTenAsc(TRANG_THAI_HOAT_DONG)
                .stream()
                .map(this::mapThuongHieuBoLoc)
                .toList();
    }

    private ThuongHieuThongKeFilterResponse mapThuongHieuBoLoc(ThuongHieu thuongHieu) {
        return new ThuongHieuThongKeFilterResponse(
                thuongHieu.getId(),
                thuongHieu.getMa(),
                thuongHieu.getTen()
        );
    }

    private List<ThongKeGiaTriTheoKyResponse> taoBieuDoBanHang(
            List<HoaDonChiTiet> dongBanHang,
            BoLocThongKe boLoc
    ) {
        LinkedHashMap<String, Long> duLieuTheoKy = new LinkedHashMap<>();
        LocalDate conTro = canChinhMocBatDau(boLoc.tuNgay(), boLoc.kyThongKe());
        LocalDate diemDung = boLoc.denNgay();

        while (!conTro.isAfter(diemDung)) {
            duLieuTheoKy.put(dinhDangNhan(conTro, boLoc.kyThongKe()), 0L);
            conTro = tangKy(conTro, boLoc.kyThongKe());
        }

        for (HoaDonChiTiet dong : dongBanHang) {
            LocalDate ngayGiaoDich = layNgayGiaoDich(dong.getHoaDon());
            String nhan = dinhDangNhan(ngayGiaoDich, boLoc.kyThongKe());
            long soLuong = dong.getSoLuong() == null ? 0L : dong.getSoLuong().longValue();
            duLieuTheoKy.computeIfPresent(nhan, (key, value) -> value + soLuong);
        }

        return duLieuTheoKy.entrySet()
                .stream()
                .map(entry -> new ThongKeGiaTriTheoKyResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<ThongKeThuongHieuResponse> taoBieuDoThuongHieu(List<GiayChiTiet> sanPhams) {
        Map<Integer, ThuongHieuTonKho> tonKhoTheoThuongHieu = new LinkedHashMap<>();

        for (GiayChiTiet chiTiet : sanPhams) {
            Giay giay = chiTiet.getGiay();
            ThuongHieu thuongHieu = giay.getThuongHieu();
            if (thuongHieu == null || thuongHieu.getId() == null) {
                continue;
            }

            ThuongHieuTonKho tonKho = tonKhoTheoThuongHieu.computeIfAbsent(
                    thuongHieu.getId(),
                    ignored -> new ThuongHieuTonKho(thuongHieu.getId(), thuongHieu.getTen())
            );
            tonKho.congTonKho(safeLong(chiTiet.getSoLuong()));
        }

        return tonKhoTheoThuongHieu.values()
                .stream()
                .sorted(Comparator
                        .comparingLong(ThuongHieuTonKho::tongTonKho)
                        .reversed()
                        .thenComparing(ThuongHieuTonKho::tenThuongHieu, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(item -> new ThongKeThuongHieuResponse(
                        item.thuongHieuId(),
                        item.tenThuongHieu(),
                        item.tongTonKho()
                ))
                .toList();
    }

    private List<ThongKeSanPhamResponse> taoThongKeSanPham(
            List<HoaDonChiTiet> dongBanHang,
            List<GiayChiTiet> sanPhams
    ) {
        Map<Integer, SanPhamThongKe> thongKeSanPhamMap = new LinkedHashMap<>();

        for (GiayChiTiet chiTiet : sanPhams) {
            Giay giay = chiTiet.getGiay();
            if (giay == null || giay.getId() == null) {
                continue;
            }

            SanPhamThongKe thongKe = thongKeSanPhamMap.computeIfAbsent(
                    giay.getId(),
                    ignored -> new SanPhamThongKe(
                            giay.getId(),
                            giay.getMa(),
                            giay.getTen(),
                            giay.getThuongHieu() != null ? giay.getThuongHieu().getTen() : null
                    )
            );
            thongKe.congTonKho(safeLong(chiTiet.getSoLuong()));
        }

        for (HoaDonChiTiet dong : dongBanHang) {
            Giay giay = dong.getGiayChiTiet() != null ? dong.getGiayChiTiet().getGiay() : null;
            if (giay == null || giay.getId() == null) {
                continue;
            }

            SanPhamThongKe thongKe = thongKeSanPhamMap.computeIfAbsent(
                    giay.getId(),
                    ignored -> new SanPhamThongKe(
                            giay.getId(),
                            giay.getMa(),
                            giay.getTen(),
                            giay.getThuongHieu() != null ? giay.getThuongHieu().getTen() : null
                    )
            );
            thongKe.congDaBan(safeLong(dong.getSoLuong()));
            thongKe.congDoanhThu(dong.getThanhTien());
        }

        List<SanPhamThongKe> danhSachSapXep = new ArrayList<>(thongKeSanPhamMap.values());
        danhSachSapXep.sort(Comparator
                .comparingLong(SanPhamThongKe::daBan)
                .reversed()
                .thenComparing(SanPhamThongKe::doanhThu, Comparator.reverseOrder())
                .thenComparing(SanPhamThongKe::tenSanPham, Comparator.nullsLast(String::compareToIgnoreCase)));

        List<ThongKeSanPhamResponse> ketQua = new ArrayList<>();
        for (int index = 0; index < danhSachSapXep.size(); index++) {
            SanPhamThongKe sanPham = danhSachSapXep.get(index);
            ketQua.add(new ThongKeSanPhamResponse(
                    index + 1,
                    sanPham.sanPhamId(),
                    sanPham.maSanPham(),
                    sanPham.tenSanPham(),
                    sanPham.thuongHieu(),
                    sanPham.daBan(),
                    sanPham.doanhThu(),
                    sanPham.tonKho()
            ));
        }

        return ketQua;
    }

    private boolean khopBoLocDongBanHang(HoaDonChiTiet dong, BoLocThongKe boLoc) {
        HoaDon hoaDon = dong.getHoaDon();
        Giay giay = dong.getGiayChiTiet() != null ? dong.getGiayChiTiet().getGiay() : null;
        if (hoaDon == null || giay == null) {
            return false;
        }

        if (!namTrongKhoang(layNgayGiaoDichInstant(hoaDon), boLoc)) {
            return false;
        }

        return khopBoLocSanPham(giay, boLoc);
    }

    private boolean khopBoLocSanPham(Giay giay, BoLocThongKe boLoc) {
        if (giay == null) {
            return false;
        }

        if (boLoc.thuongHieuId() != null) {
            ThuongHieu thuongHieu = giay.getThuongHieu();
            if (thuongHieu == null || !boLoc.thuongHieuId().equals(thuongHieu.getId())) {
                return false;
            }
        }

        if (boLoc.keyword() == null) {
            return true;
        }

        String ma = giay.getMa() == null ? "" : giay.getMa().toLowerCase(Locale.ROOT);
        String ten = giay.getTen() == null ? "" : giay.getTen().toLowerCase(Locale.ROOT);
        return ma.contains(boLoc.keyword()) || ten.contains(boLoc.keyword());
    }

    private boolean namTrongKhoang(Instant thoiDiem, BoLocThongKe boLoc) {
        return thoiDiem != null
                && !thoiDiem.isBefore(boLoc.tuNgayInstant())
                && thoiDiem.isBefore(boLoc.denNgayDocQuyenInstant());
    }

    private LocalDate layNgayGiaoDich(HoaDon hoaDon) {
        return layNgayGiaoDichInstant(hoaDon).atZone(MUI_GIO_HE_THONG).toLocalDate();
    }

    private Instant layNgayGiaoDichInstant(HoaDon hoaDon) {
        if (hoaDon.getNgayThanhToan() != null) {
            return hoaDon.getNgayThanhToan();
        }
        return hoaDon.getNgayLap();
    }

    private BoLocThongKe chuanHoaBoLoc(
            LocalDate tuNgay,
            LocalDate denNgay,
            Integer thuongHieuId,
            String keyword,
            String kyThongKe
    ) {
        KyThongKe ky = KyThongKe.from(kyThongKe);
        LocalDate homNay = LocalDate.now(MUI_GIO_HE_THONG);
        LocalDate denNgayDaDung = denNgay == null ? homNay : denNgay;
        LocalDate tuNgayMacDinh = switch (ky) {
            case YEAR -> denNgayDaDung.minusYears(4L).withDayOfYear(1);
            case MONTH -> denNgayDaDung.withDayOfYear(1);
            case DAY -> denNgayDaDung.withDayOfMonth(1);
        };
        LocalDate tuNgayDaDung = tuNgay == null ? tuNgayMacDinh : tuNgay;

        if (tuNgayDaDung.isAfter(denNgayDaDung)) {
            LocalDate temp = tuNgayDaDung;
            tuNgayDaDung = denNgayDaDung;
            denNgayDaDung = temp;
        }

        Instant tuNgayInstant = tuNgayDaDung.atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant denNgayDocQuyenInstant = denNgayDaDung.plusDays(1L).atStartOfDay(MUI_GIO_HE_THONG).toInstant();

        Integer thuongHieuHopLe = thuongHieuId != null && thuongHieuId > 0 ? thuongHieuId : null;
        String keywordDaChuanHoa = chuanHoaKeyword(keyword);

        return new BoLocThongKe(
                tuNgayDaDung,
                denNgayDaDung,
                tuNgayInstant,
                denNgayDocQuyenInstant,
                thuongHieuHopLe,
                keywordDaChuanHoa,
                ky
        );
    }

    private String chuanHoaKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String keywordDaChuanHoa = keyword.trim().toLowerCase(Locale.ROOT);
        return keywordDaChuanHoa.isBlank() ? null : keywordDaChuanHoa;
    }

    private LocalDate canChinhMocBatDau(LocalDate ngay, KyThongKe kyThongKe) {
        return switch (kyThongKe) {
            case YEAR -> ngay.withDayOfYear(1);
            case MONTH -> ngay.withDayOfMonth(1);
            case DAY -> ngay;
        };
    }

    private LocalDate tangKy(LocalDate ngay, KyThongKe kyThongKe) {
        return switch (kyThongKe) {
            case YEAR -> ngay.plusYears(1L);
            case MONTH -> ngay.plusMonths(1L);
            case DAY -> ngay.plusDays(1L);
        };
    }

    private String dinhDangNhan(LocalDate ngay, KyThongKe kyThongKe) {
        return switch (kyThongKe) {
            case YEAR -> DINH_DANG_NAM.format(ngay);
            case MONTH -> DINH_DANG_THANG.format(ngay.withDayOfMonth(1));
            case DAY -> DINH_DANG_NGAY.format(ngay);
        };
    }

    private long safeLong(Number value) {
        return value == null ? 0L : value.longValue();
    }

    private enum KyThongKe {
        DAY,
        MONTH,
        YEAR;

        private static KyThongKe from(String value) {
            if (value == null || value.isBlank()) {
                return DAY;
            }

            try {
                return KyThongKe.valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                return DAY;
            }
        }
    }

    private record BoLocThongKe(
            LocalDate tuNgay,
            LocalDate denNgay,
            Instant tuNgayInstant,
            Instant denNgayDocQuyenInstant,
            Integer thuongHieuId,
            String keyword,
            KyThongKe kyThongKe
    ) {
    }

    private static final class ThuongHieuTonKho {
        private final Integer thuongHieuId;
        private final String tenThuongHieu;
        private long tongTonKho;

        private ThuongHieuTonKho(Integer thuongHieuId, String tenThuongHieu) {
            this.thuongHieuId = thuongHieuId;
            this.tenThuongHieu = tenThuongHieu;
        }

        private void congTonKho(long tonKho) {
            this.tongTonKho += tonKho;
        }

        private Integer thuongHieuId() {
            return thuongHieuId;
        }

        private String tenThuongHieu() {
            return tenThuongHieu;
        }

        private long tongTonKho() {
            return tongTonKho;
        }
    }

    private static final class SanPhamThongKe {
        private final Integer sanPhamId;
        private final String maSanPham;
        private final String tenSanPham;
        private final String thuongHieu;
        private long daBan;
        private long tonKho;
        private BigDecimal doanhThu = BigDecimal.ZERO;

        private SanPhamThongKe(
                Integer sanPhamId,
                String maSanPham,
                String tenSanPham,
                String thuongHieu
        ) {
            this.sanPhamId = sanPhamId;
            this.maSanPham = maSanPham;
            this.tenSanPham = tenSanPham;
            this.thuongHieu = thuongHieu;
        }

        private void congDaBan(long soLuong) {
            this.daBan += soLuong;
        }

        private void congTonKho(long tonKho) {
            this.tonKho += tonKho;
        }

        private void congDoanhThu(BigDecimal doanhThu) {
            this.doanhThu = this.doanhThu.add(doanhThu == null ? BigDecimal.ZERO : doanhThu);
        }

        private Integer sanPhamId() {
            return sanPhamId;
        }

        private String maSanPham() {
            return maSanPham;
        }

        private String tenSanPham() {
            return tenSanPham;
        }

        private String thuongHieu() {
            return thuongHieu;
        }

        private long daBan() {
            return daBan;
        }

        private BigDecimal doanhThu() {
            return doanhThu;
        }

        private long tonKho() {
            return tonKho;
        }
    }
}
