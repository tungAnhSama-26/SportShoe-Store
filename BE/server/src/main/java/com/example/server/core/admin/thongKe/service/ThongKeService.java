package com.example.server.core.admin.thongKe.service;

import com.example.server.core.admin.thongKe.dto.request.ThongKeDashboardRequest;
import com.example.server.core.admin.thongKe.dto.response.ThongKeBoLocDaApDungResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeDashboardResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeGiaTriTheoKyResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeNhanVienResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeSanPhamResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeThuongHieuResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeTongQuanResponse;
import com.example.server.core.admin.thongKe.dto.response.ThuongHieuThongKeFilterResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeTheoThoiGianResponse;
import com.example.server.core.admin.thongKe.dto.response.ThongKeTrangThaiDonHangResponse;
import com.example.server.entity.Giay;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.ThuongHieu;
import com.example.server.entity.ThanhToan;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.ThuongHieuRepository;
import com.example.server.repository.ThanhToanRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThongKeService {

    private static final int TRANG_THAI_HOAT_DONG = 1;
    private static final List<Integer> TRANG_THAI_HOA_DON_HOP_LE = List.of(5);
    private static final String NHAN_VIEN_MAC_DINH = "Chưa gán nhân viên";
    private static final ZoneId MUI_GIO_HE_THONG = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_THANG = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_NAM = DateTimeFormatter.ofPattern("yyyy");

    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ThanhToanRepository thanhToanRepository;

    public ThongKeService(
            HoaDonChiTietRepository hoaDonChiTietRepository,
            GiayChiTietRepository giayChiTietRepository,
            KhachHangRepository khachHangRepository,
            ThuongHieuRepository thuongHieuRepository,
            HoaDonRepository hoaDonRepository,
            ThanhToanRepository thanhToanRepository
    ) {
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.khachHangRepository = khachHangRepository;
        this.thuongHieuRepository = thuongHieuRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.thanhToanRepository = thanhToanRepository;
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

        LocalDate homNay = LocalDate.now(MUI_GIO_HE_THONG);
        LocalDate startOfLastYear = homNay.minusYears(1).withDayOfYear(1);
        Instant limitDate = startOfLastYear.atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        if (boLoc.tuNgayInstant().isBefore(limitDate)) {
            limitDate = boLoc.tuNgayInstant();
        }

        List<HoaDonChiTiet> tatCaDongBanHang = hoaDonChiTietRepository.findAllForThongKe(TRANG_THAI_HOA_DON_HOP_LE, limitDate);
        List<GiayChiTiet> tatCaSanPham = giayChiTietRepository.findAllForThongKe();
        List<KhachHang> tatCaKhachHang = khachHangRepository.findByTrangThai(TRANG_THAI_HOAT_DONG);

        Set<Integer> tatCaHoaDonIds = tatCaDongBanHang.stream()
                .map(line -> line.getHoaDon().getId())
                .collect(Collectors.toSet());
        Map<Integer, List<ThanhToan>> thanhToanMap = new HashMap<>();
        if (!tatCaHoaDonIds.isEmpty()) {
            List<ThanhToan> tatCaThanhToan = thanhToanRepository.findByHoaDonIdIn(tatCaHoaDonIds);
            thanhToanMap = tatCaThanhToan.stream()
                    .collect(Collectors.groupingBy(tt -> tt.getHoaDon().getId()));
        }

        Map<Integer, Long> mapSoLuongTra = new HashMap<>();

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
                taoTongQuan(dongBanHangTheoBoLoc, khachMoi, thanhToanMap),
                layThuongHieuBoLoc(),
                taoBieuDoBanHang(dongBanHangTheoBoLoc, boLoc),
                taoBieuDoThuongHieu(sanPhamTheoBoLoc),
                taoThongKeSanPham(dongBanHangTheoBoLoc, sanPhamTheoBoLoc, mapSoLuongTra),
                taoThongKeNhanVien(dongBanHangTheoBoLoc),
                taoThongKeTheoThoiGian(tatCaDongBanHang, boLoc, thanhToanMap),
                taoBieuDoTrangThaiDonHang(boLoc)
        );
    }

    private ThongKeTongQuanResponse taoTongQuan(
            List<HoaDonChiTiet> dongBanHang,
            long khachMoi,
            Map<Integer, List<ThanhToan>> thanhToanMap
    ) {
        BigDecimal tongTienMat = BigDecimal.ZERO;
        BigDecimal tongChuyenKhoan = BigDecimal.ZERO;

        List<HoaDon> uniqueHoaDons = dongBanHang.stream()
                .map(HoaDonChiTiet::getHoaDon)
                .distinct()
                .toList();

        for (HoaDon h : uniqueHoaDons) {
            List<ThanhToan> payments = thanhToanMap.getOrDefault(h.getId(), List.of());
            for (ThanhToan tt : payments) {
                BigDecimal soTien = tt.getSoTien() != null ? tt.getSoTien() : BigDecimal.ZERO;
                if (tt.getLoaiGiaoDich() != null && tt.getLoaiGiaoDich() == 1) { // Thanh toán
                    if (tt.getTrangThai() != null && tt.getTrangThai() == 1) { // Thành công
                        if (tt.getHinhThuc() != null && tt.getHinhThuc() == 1) { // Tiền mặt
                            tongTienMat = tongTienMat.add(soTien);
                        } else if (tt.getHinhThuc() != null) { // Chuyển khoản (hinhThuc != 1)
                            tongChuyenKhoan = tongChuyenKhoan.add(soTien);
                        }
                    }
                } else if (tt.getLoaiGiaoDich() != null && tt.getLoaiGiaoDich() == 2) { // Hoàn tiền
                    if (tt.getTrangThai() != null && tt.getTrangThai() == 5) { // Thành công
                        if (tt.getHinhThuc() != null && tt.getHinhThuc() == 1) { // Tiền mặt
                            tongTienMat = tongTienMat.subtract(soTien);
                        } else if (tt.getHinhThuc() != null) { // Chuyển khoản (hinhThuc != 1)
                            tongChuyenKhoan = tongChuyenKhoan.subtract(soTien);
                        }
                    }
                }
            }
        }

        BigDecimal tongDoanhThu = tongTienMat.add(tongChuyenKhoan).max(BigDecimal.ZERO);
        long tongDonHang = uniqueHoaDons.size();

        long sanPhamDaBan = dongBanHang.stream()
                .map(HoaDonChiTiet::getSoLuong)
                .mapToLong(soLuong -> soLuong == null ? 0L : soLuong.longValue())
                .sum();

        return new ThongKeTongQuanResponse(
                tongDoanhThu,
                tongTienMat.max(BigDecimal.ZERO),
                tongChuyenKhoan.max(BigDecimal.ZERO),
                tongDonHang,
                sanPhamDaBan,
                khachMoi
        );
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
            List<GiayChiTiet> sanPhams,
            Map<Integer, Long> mapSoLuongTra
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
            Long soLuongTra = mapSoLuongTra.getOrDefault(sanPham.sanPhamId(), 0L);
            ketQua.add(new ThongKeSanPhamResponse(
                    index + 1,
                    sanPham.sanPhamId(),
                    sanPham.maSanPham(),
                    sanPham.tenSanPham(),
                    sanPham.thuongHieu(),
                    sanPham.daBan(),
                    sanPham.doanhThu(),
                    sanPham.tonKho(),
                    soLuongTra
            ));
        }

        return ketQua;
    }

    private List<ThongKeNhanVienResponse> taoThongKeNhanVien(List<HoaDonChiTiet> dongBanHang) {
        Map<String, NhanVienThongKe> thongKeNhanVienMap = new LinkedHashMap<>();

        for (HoaDonChiTiet dong : dongBanHang) {
            HoaDon hoaDon = dong.getHoaDon();
            if (hoaDon == null || hoaDon.getId() == null) {
                continue;
            }

            UUID nhanVienId = hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getId() : null;
            String maNhanVien = hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getMa() : null;
            String tenNhanVien = hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getHoTen() : NHAN_VIEN_MAC_DINH;
            Integer rawVaiTro = hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getVaiTro() : null;
            String vaiTro = rawVaiTro == null ? "Đơn chưa gán nhân viên" : (Integer.valueOf(1).equals(rawVaiTro) ? "Quản lý" : "Nhân viên");
            String key = nhanVienId != null ? nhanVienId.toString() : NHAN_VIEN_MAC_DINH;

            NhanVienThongKe thongKe = thongKeNhanVienMap.computeIfAbsent(
                    key,
                    ignored -> new NhanVienThongKe(nhanVienId, maNhanVien, tenNhanVien, vaiTro)
            );
            thongKe.ghiNhanHoaDon(hoaDon.getId());
            thongKe.congSanPhamDaBan(safeLong(dong.getSoLuong()));
            thongKe.congDoanhThu(dong.getThanhTien());
        }

        List<NhanVienThongKe> danhSachSapXep = new ArrayList<>(thongKeNhanVienMap.values());
        danhSachSapXep.sort(Comparator
                .comparing(NhanVienThongKe::doanhThu, Comparator.reverseOrder())
                .thenComparing(Comparator.comparingLong(NhanVienThongKe::tongDonHang).reversed())
                .thenComparing(Comparator.comparingLong(NhanVienThongKe::sanPhamDaBan).reversed())
                .thenComparing(NhanVienThongKe::tenNhanVien, Comparator.nullsLast(String::compareToIgnoreCase)));

        List<ThongKeNhanVienResponse> ketQua = new ArrayList<>();
        for (int index = 0; index < danhSachSapXep.size(); index++) {
            NhanVienThongKe nhanVien = danhSachSapXep.get(index);
            ketQua.add(new ThongKeNhanVienResponse(
                    index + 1,
                    nhanVien.nhanVienId(),
                    nhanVien.maNhanVien(),
                    nhanVien.tenNhanVien(),
                    nhanVien.vaiTro(),
                    nhanVien.tongDonHang(),
                    nhanVien.sanPhamDaBan(),
                    nhanVien.doanhThu()
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

        Integer thuongHieuHopLe = thuongHieuId != null && thuongHieuId >= 0 ? thuongHieuId : null;
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

    private static final class NhanVienThongKe {
        private final UUID nhanVienId;
        private final String maNhanVien;
        private final String tenNhanVien;
        private final String vaiTro;
        private final Set<Integer> hoaDonIds = new LinkedHashSet<>();
        private long sanPhamDaBan;
        private BigDecimal doanhThu = BigDecimal.ZERO;

        private NhanVienThongKe(
                UUID nhanVienId,
                String maNhanVien,
                String tenNhanVien,
                String vaiTro
        ) {
            this.nhanVienId = nhanVienId;
            this.maNhanVien = maNhanVien;
            this.tenNhanVien = tenNhanVien;
            this.vaiTro = vaiTro;
        }

        private void ghiNhanHoaDon(Integer hoaDonId) {
            if (hoaDonId != null) {
                hoaDonIds.add(hoaDonId);
            }
        }

        private void congSanPhamDaBan(long soLuong) {
            this.sanPhamDaBan += soLuong;
        }

        private void congDoanhThu(BigDecimal doanhThu) {
            this.doanhThu = this.doanhThu.add(doanhThu == null ? BigDecimal.ZERO : doanhThu);
        }

        private UUID nhanVienId() {
            return nhanVienId;
        }

        private String maNhanVien() {
            return maNhanVien;
        }

        private String tenNhanVien() {
            return tenNhanVien;
        }

        private String vaiTro() {
            return vaiTro;
        }

        private long tongDonHang() {
            return hoaDonIds.size();
        }

        private long sanPhamDaBan() {
            return sanPhamDaBan;
        }

        private BigDecimal doanhThu() {
            return doanhThu;
        }
    }

    private record PeriodStats(
            BigDecimal revenue,
            BigDecimal actualRevenue,
            long orders,
            double average
    ) {}

    private PeriodStats computePeriodStats(
            List<HoaDonChiTiet> allLines,
            Instant start,
            Instant end,
            BoLocThongKe boLoc,
            Map<Integer, List<ThanhToan>> thanhToanMap
    ) {
        List<HoaDonChiTiet> lines = allLines.stream()
                .filter(line -> {
                    Instant tradeDate = layNgayGiaoDichInstant(line.getHoaDon());
                    if (tradeDate == null || tradeDate.isBefore(start) || !tradeDate.isBefore(end)) {
                        return false;
                    }
                    return khopBoLocSanPham(line.getGiayChiTiet() != null ? line.getGiayChiTiet().getGiay() : null, boLoc);
                })
                .toList();

        List<HoaDon> distinctInvoices = lines.stream()
                .map(HoaDonChiTiet::getHoaDon)
                .distinct()
                .toList();

        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal actualRevenue = BigDecimal.ZERO;

        for (HoaDon h : distinctInvoices) {
            BigDecimal gross = h.getTongTienHang() != null ? h.getTongTienHang() : BigDecimal.ZERO;
            revenue = revenue.add(gross);

            List<ThanhToan> payments = thanhToanMap.getOrDefault(h.getId(), List.of());
            for (ThanhToan tt : payments) {
                BigDecimal soTien = tt.getSoTien() != null ? tt.getSoTien() : BigDecimal.ZERO;
                if (tt.getLoaiGiaoDich() != null && tt.getLoaiGiaoDich() == 1) { // Thanh toán
                    if (tt.getTrangThai() != null && tt.getTrangThai() == 1) { // Thành công
                        actualRevenue = actualRevenue.add(soTien);
                    }
                } else if (tt.getLoaiGiaoDich() != null && tt.getLoaiGiaoDich() == 2) { // Hoàn tiền
                    if (tt.getTrangThai() != null && tt.getTrangThai() == 5) { // Thành công
                        actualRevenue = actualRevenue.subtract(soTien);
                    }
                }
            }
        }
        actualRevenue = actualRevenue.max(BigDecimal.ZERO);

        long orders = distinctInvoices.size();
        double average = orders > 0 ? (actualRevenue.doubleValue() / orders) : 0.0;

        return new PeriodStats(revenue, actualRevenue, orders, average);
    }

    private List<ThongKeTheoThoiGianResponse> taoThongKeTheoThoiGian(
            List<HoaDonChiTiet> allLines,
            BoLocThongKe boLoc,
            Map<Integer, List<ThanhToan>> thanhToanMap
    ) {
        LocalDate homNay = LocalDate.now(MUI_GIO_HE_THONG);

        // Today and Yesterday
        Instant startToday = homNay.atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant endToday = homNay.plusDays(1).atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant startYesterday = homNay.minusDays(1).atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant endYesterday = startToday;

        // This Week and Last Week
        LocalDate startOfWeek = homNay.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        Instant startThisWeek = startOfWeek.atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant endThisWeek = startOfWeek.plusWeeks(1).atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant startLastWeek = startOfWeek.minusWeeks(1).atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant endLastWeek = startThisWeek;

        // This Month and Last Month
        LocalDate startOfMonth = homNay.withDayOfMonth(1);
        Instant startThisMonth = startOfMonth.atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant endThisMonth = startOfMonth.plusMonths(1).atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant startLastMonth = startOfMonth.minusMonths(1).atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant endLastMonth = startThisMonth;

        // This Year and Last Year
        LocalDate startOfYear = homNay.withDayOfYear(1);
        Instant startThisYear = startOfYear.atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant endThisYear = startOfYear.plusYears(1).atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant startLastYear = startOfYear.minusYears(1).atStartOfDay(MUI_GIO_HE_THONG).toInstant();
        Instant endLastYear = startThisYear;

        PeriodStats statsToday = computePeriodStats(allLines, startToday, endToday, boLoc, thanhToanMap);
        PeriodStats statsYesterday = computePeriodStats(allLines, startYesterday, endYesterday, boLoc, thanhToanMap);

        PeriodStats statsThisWeek = computePeriodStats(allLines, startThisWeek, endThisWeek, boLoc, thanhToanMap);
        PeriodStats statsLastWeek = computePeriodStats(allLines, startLastWeek, endLastWeek, boLoc, thanhToanMap);

        PeriodStats statsThisMonth = computePeriodStats(allLines, startThisMonth, endThisMonth, boLoc, thanhToanMap);
        PeriodStats statsLastMonth = computePeriodStats(allLines, startLastMonth, endLastMonth, boLoc, thanhToanMap);

        PeriodStats statsThisYear = computePeriodStats(allLines, startThisYear, endThisYear, boLoc, thanhToanMap);
        PeriodStats statsLastYear = computePeriodStats(allLines, startLastYear, endLastYear, boLoc, thanhToanMap);

        List<ThongKeTheoThoiGianResponse> result = new ArrayList<>();
        result.add(createPeriodResponse("Hôm nay", statsToday, statsYesterday));
        result.add(createPeriodResponse("Tuần này", statsThisWeek, statsLastWeek));
        result.add(createPeriodResponse("Tháng này", statsThisMonth, statsLastMonth));
        result.add(createPeriodResponse("Năm nay", statsThisYear, statsLastYear));

        return result;
    }

    private ThongKeTheoThoiGianResponse createPeriodResponse(String periodName, PeriodStats current, PeriodStats previous) {
        BigDecimal prevRevenue = previous.actualRevenue();
        double growth = 0.0;
        if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = (current.actualRevenue().subtract(prevRevenue))
                    .multiply(BigDecimal.valueOf(100))
                    .divide(prevRevenue, 2, java.math.RoundingMode.HALF_UP)
                    .doubleValue();
        } else if (current.actualRevenue().compareTo(BigDecimal.ZERO) > 0) {
            growth = 100.0;
        }

        return new ThongKeTheoThoiGianResponse(
                periodName,
                current.actualRevenue(),
                current.orders(),
                BigDecimal.valueOf(current.average()),
                growth
        );
    }

    private List<ThongKeTrangThaiDonHangResponse> taoBieuDoTrangThaiDonHang(BoLocThongKe boLoc) {
        List<Object[]> rows = hoaDonRepository.countByTrangThaiWithFilters(
                boLoc.tuNgayInstant(),
                boLoc.denNgayDocQuyenInstant(),
                boLoc.thuongHieuId(),
                boLoc.keyword()
        );

        return rows.stream()
                .map(row -> {
                    Integer trangThaiCode = (Integer) row[0];
                    Long soLuong = (Long) row[1];
                    String trangThaiLabel = switch (trangThaiCode) {
                        case 1 -> "Chờ xác nhận";
                        case 2 -> "Chờ lấy hàng";
                        case 3 -> "Đang giao hàng";
                        case 4 -> "Đã giao hàng";
                        case 5 -> "Hoàn thành";
                        case 6 -> "Hủy";
                        case 7 -> "Yêu cầu hủy";
                        case 8 -> "Cần hoàn tiền";
                        case 9 -> "Đã xác nhận";
                        case 10 -> "Giao hàng thất bại";
                        default -> "Khác";
                    };
                    return new ThongKeTrangThaiDonHangResponse(trangThaiLabel, soLuong);
                })
                .toList();
    }
}
