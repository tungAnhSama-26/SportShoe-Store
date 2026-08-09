package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.entity.DiaChiHaiCap;
import com.example.server.infrastructure.address.DiaChiHaiCapMapper;

import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoItemRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.ThongTinGiaoHangTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoChiTietResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoDongSanPhamResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoTomTatResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThongTinGiaoHangTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThongTinPhieuGiamGiaHoaDonResponse;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.LichSuHoaDon;
import com.example.server.entity.NhanVien;
import com.example.server.entity.VanChuyen;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.security.AdminPrincipal;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.LichSuHoaDonRepository;
import com.example.server.repository.NhanVienRepository;
import com.example.server.repository.VanChuyenRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class HoaDonTaiQuayService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final VanChuyenRepository vanChuyenRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final HinhAnhGiayRepository hinhAnhGiayRepository;
    private final XacThucTaiQuayService validationUseCase;
    private final TrangThaiHoaDonTaiQuayService invoiceStateUseCase;
    private final GiaCaTaiQuayService pricingUseCase;
    private final GiaoHangTaiQuayService shippingUseCase;
    private final PhieuGiamGiaTaiQuayService voucherUseCase;
    private final SanPhamTaiQuayService productUseCase;
    private final TonKhoTaiQuayService inventoryUseCase;

    public HoaDonTaiQuayService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            LichSuHoaDonRepository lichSuHoaDonRepository,
            VanChuyenRepository vanChuyenRepository,
            NhanVienRepository nhanVienRepository,
            KhachHangRepository khachHangRepository,
            GiayChiTietRepository giayChiTietRepository,
            HinhAnhGiayRepository hinhAnhGiayRepository,
            XacThucTaiQuayService validationUseCase,
            TrangThaiHoaDonTaiQuayService invoiceStateUseCase,
            GiaCaTaiQuayService pricingUseCase,
            GiaoHangTaiQuayService shippingUseCase,
            PhieuGiamGiaTaiQuayService voucherUseCase,
            SanPhamTaiQuayService productUseCase,
            TonKhoTaiQuayService inventoryUseCase
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.lichSuHoaDonRepository = lichSuHoaDonRepository;
        this.vanChuyenRepository = vanChuyenRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.khachHangRepository = khachHangRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
        this.validationUseCase = validationUseCase;
        this.invoiceStateUseCase = invoiceStateUseCase;
        this.pricingUseCase = pricingUseCase;
        this.shippingUseCase = shippingUseCase;
        this.voucherUseCase = voucherUseCase;
        this.productUseCase = productUseCase;
        this.inventoryUseCase = inventoryUseCase;
    }

    public List<HoaDonChoTomTatResponse> layDanhSachHoaDonCho() {
        List<HoaDon> hoaDons = hoaDonRepository.findTop10ByKenhBanAndTrangThaiOrderByNgayTaoDesc(
                        KENH_BAN_TAI_QUAY,
                        TRANG_THAI_HOA_DON_CHO_TAI_QUAY
                )
                .stream()
                .toList();
        Map<Integer, VanChuyen> vanChuyenMap = vanChuyenRepository.findByHoaDonIdIn(
                hoaDons.stream().map(HoaDon::getId).toList()
        ).stream().collect(HashMap::new, (map, vanChuyen) -> map.put(vanChuyen.getHoaDon().getId(), vanChuyen), HashMap::putAll);

        return hoaDons.stream()
                .map(hoaDon -> {
                    List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId());
                    int tongSanPham = items.stream().mapToInt(HoaDonChiTiet::getSoLuong).sum();
                    return new HoaDonChoTomTatResponse(
                            hoaDon.getId(),
                            hoaDon.getMa(),
                            hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null,
                            resolveTenKhachHangHoaDon(hoaDon),
                            resolveSoDienThoaiKhachHangHoaDon(hoaDon),
                            mapThongTinGiaoHangHoaDon(hoaDon, vanChuyenMap.get(hoaDon.getId())),
                            tongSanPham,
                            hoaDon.getTongTienHang(),
                            hoaDon.getTienGiam(),
                            hoaDon.getTongTienThanhToan(),
                            mapThongTinPhieuGiamGiaHoaDon(hoaDon),
                            hoaDon.getNgayTao()
                    );
                })
                .toList();
    }

    public HoaDonChoChiTietResponse layChiTietHoaDonCho(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại"));
        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDonId);
        return mapHoaDonChiTiet(hoaDon, items, vanChuyenRepository.findByHoaDonId(hoaDonId).orElse(null));
    }

    public HoaDon layHoaDonTaiQuayNeuCo(Integer hoaDonId) {
        if (hoaDonId == null) {
            return null;
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại"));

        if (!invoiceStateUseCase.kenhBanTaiQuay(hoaDon.getKenhBan())) {
            throw new BusinessException("Chỉ hỗ trợ cho hóa đơn tại quầy");
        }

        return hoaDon;
    }

    public HoaDon taoHoaDon(
            UUID khachHangId,
            String tenKhachHangInput,
            String soDienThoaiInput,
            String maPhieuGiamGia,
            ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang,
            List<TaoHoaDonChoItemRequest> items,
            Integer trangThai,
            String ghiChu
    ) {
        validationUseCase.validateDuplicateItems(items != null ? items : new ArrayList<>());

        List<HoaDonChiTiet> chiTietTam = items != null && !items.isEmpty() ? items.stream()
                .map(item -> taoDongHoaDon(item))
                .toList() : new ArrayList<>();

        BigDecimal tongTienHang = chiTietTam.stream()
                .map(HoaDonChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        KhachHang khachHang = timKhachHang(khachHangId);
        String tenKhachHang = layTenKhachHang(khachHang, tenKhachHangInput);
        String soDienThoai = laySoDienThoai(khachHang, soDienThoaiInput);

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMa(taoMaHoaDonTam());
        hoaDon.setKenhBan(KENH_BAN_TAI_QUAY);
        hoaDon.setNgayLap(Instant.now());
        hoaDon.setTrangThai(trangThai);
        hoaDon.setTongTienHang(tongTienHang);
        hoaDon.setTongTienThanhToan(tongTienHang);
        hoaDon.setGhiChu(ghiChu);

        voucherUseCase.ganPhieuGiamGiaChoHoaDon(hoaDon, maPhieuGiamGia, khachHang, tongTienHang);
        hoaDon.setKhachHang(khachHang);
        apDungThongTinGiaoHangChoHoaDon(hoaDon, thongTinGiaoHang, tenKhachHang, soDienThoai);

        NhanVien currentEmp = resolveNhanVienDangDangNhap();
        if (currentEmp != null) {
            hoaDon.setNhanVien(currentEmp);
        }

        hoaDon.setNgayTao(Instant.now());
        hoaDon.setNgayCapNhat(Instant.now());

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        dongBoVanChuyen(savedHoaDon, thongTinGiaoHang);

        for (HoaDonChiTiet item : chiTietTam) {
            item.setHoaDon(savedHoaDon);
            hoaDonChiTietRepository.save(item);
        }

        return savedHoaDon;
    }

    public void luuLichSuHoaDon(HoaDon hoaDon, Integer trangThai, String ghiChu) {
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setNhanVien(resolveNhanVienDangDangNhap());
        lichSu.setTrangThai(invoiceStateUseCase.labelTrangThaiHoaDon(trangThai));
        lichSu.setGhiChu(ghiChu);
        lichSu.setNgayTao(Instant.now());
        lichSuHoaDonRepository.save(lichSu);
    }

    public void dongBoVanChuyen(HoaDon hoaDon, ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang) {
        VanChuyen vanChuyen = vanChuyenRepository.findByHoaDonId(hoaDon.getId()).orElse(null);

        if (!shippingUseCase.laDonGiaoHang(thongTinGiaoHang)) {
            if (vanChuyen != null) {
                vanChuyenRepository.delete(vanChuyen);
            }
            return;
        }

        if (vanChuyen == null) {
            vanChuyen = new VanChuyen();
            vanChuyen.setHoaDon(hoaDon);
            vanChuyen.setTrangThai(TRANG_THAI_VAN_CHUYEN_CHO_XU_LY);
            vanChuyen.setNgayTao(Instant.now());
        }

        vanChuyen.setDonViVanChuyen(shippingUseCase.resolveDonViVanChuyen(thongTinGiaoHang));
        vanChuyen.setPhiVanChuyen(shippingUseCase.resolvePhiVanChuyen(thongTinGiaoHang));
        vanChuyen.setNgayCapNhat(Instant.now());
        if (vanChuyen.getTrangThai() == null) {
            vanChuyen.setTrangThai(TRANG_THAI_VAN_CHUYEN_CHO_XU_LY);
        }
        vanChuyenRepository.save(vanChuyen);
    }

    public void apDungThongTinGiaoHangChoHoaDon(
            HoaDon hoaDon,
            ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang,
            String tenMacDinh,
            String soDienThoaiMacDinh
    ) {
        boolean giaoHang = shippingUseCase.laDonGiaoHang(thongTinGiaoHang);
        String tenNguoiNhan = shippingUseCase.resolveGiaTriChuoi(
                giaoHang && thongTinGiaoHang != null ? thongTinGiaoHang.tenNguoiNhan() : null,
                tenMacDinh
        );
        String soDienThoaiNguoiNhan = shippingUseCase.resolveGiaTriChuoi(
                giaoHang && thongTinGiaoHang != null ? thongTinGiaoHang.soDienThoaiNguoiNhan() : null,
                soDienThoaiMacDinh
        );

        if (giaoHang && (soDienThoaiNguoiNhan == null || soDienThoaiNguoiNhan.isBlank() || laGiaTriKhongCo(soDienThoaiNguoiNhan))) {
            throw new BusinessException("Vui lòng nhập số điện thoại người nhận");
        }

        DiaChiHaiCap diaChiGiaoHang = giaoHang ? shippingUseCase.requireDiaChiGiaoHang(thongTinGiaoHang) : null;
        BigDecimal phiVanChuyen = giaoHang ? shippingUseCase.resolvePhiVanChuyen(thongTinGiaoHang) : BigDecimal.ZERO;

        hoaDon.setTenNguoiNhan(tenNguoiNhan);
        hoaDon.setSdtNguoiNhan(soDienThoaiNguoiNhan);
        hoaDon.setDiaChiGiaoHang(diaChiGiaoHang);
        hoaDon.setTongTienThanhToan(pricingUseCase.defaultMoney(hoaDon.getTongTienThanhToan()).add(phiVanChuyen).max(BigDecimal.ZERO));
    }

    public HoaDonChiTiet taoDongHoaDon(TaoHoaDonChoItemRequest itemRequest) {
        return taoDongHoaDon(itemRequest, null);
    }

    public HoaDonChiTiet taoDongHoaDon(TaoHoaDonChoItemRequest itemRequest, java.util.Set<Integer> bypassActiveCheckIds) {
        GiayChiTiet giayChiTiet = layGiayChiTietHopLe(itemRequest.chiTietId(), itemRequest.soLuong(), bypassActiveCheckIds);

        boolean bypassActiveCheck = bypassActiveCheckIds != null && bypassActiveCheckIds.contains(itemRequest.chiTietId());
        inventoryUseCase.deductStock(giayChiTiet, itemRequest.soLuong(), bypassActiveCheck);
        giayChiTietRepository.save(giayChiTiet);

        // Use the price sent from the frontend (may be the locked old price if variant's price changed)
        BigDecimal giaThucTe = productUseCase.layGiaBanThucTe(giayChiTiet);
        BigDecimal giaDonVi = (itemRequest.giaBan() != null) ? itemRequest.giaBan() : giaThucTe;

        HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
        hoaDonChiTiet.setGiayChiTiet(giayChiTiet);
        hoaDonChiTiet.setSoLuong(itemRequest.soLuong());
        hoaDonChiTiet.setGiaDonVi(giaDonVi);
        hoaDonChiTiet.setThanhTien(giaDonVi.multiply(BigDecimal.valueOf(itemRequest.soLuong().longValue())));
        hoaDonChiTiet.setTrangThai(1);
        hoaDonChiTiet.setNgayTao(Instant.now());
        return hoaDonChiTiet;
    }


    public List<HoaDonChiTiet> taoDanhSachDongHoaDonTam(List<TaoHoaDonChoItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        validationUseCase.validateDuplicateItems(items);
        return items.stream()
                .map(this::taoDongHoaDonTam)
                .toList();
    }

    private HoaDonChiTiet taoDongHoaDonTam(TaoHoaDonChoItemRequest item) {
        GiayChiTiet giayChiTiet = layGiayChiTietHopLe(item.chiTietId(), item.soLuong());
        BigDecimal giaThucTe = productUseCase.layGiaBanThucTe(giayChiTiet);
        BigDecimal giaDonVi = (item.giaBan() != null) ? item.giaBan() : giaThucTe;

        HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
        hoaDonChiTiet.setGiayChiTiet(giayChiTiet);
        hoaDonChiTiet.setSoLuong(item.soLuong());
        hoaDonChiTiet.setGiaDonVi(giaDonVi);
        hoaDonChiTiet.setThanhTien(giaDonVi.multiply(BigDecimal.valueOf(item.soLuong().longValue())));
        hoaDonChiTiet.setTrangThai(1);
        hoaDonChiTiet.setNgayTao(Instant.now());
        return hoaDonChiTiet;
    }

    public HoaDonChoChiTietResponse mapHoaDonChiTiet(HoaDon hoaDon, List<HoaDonChiTiet> items, VanChuyen vanChuyen) {
        return new HoaDonChoChiTietResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null,
                resolveTenKhachHangHoaDon(hoaDon),
                resolveSoDienThoaiKhachHangHoaDon(hoaDon),
                mapThongTinGiaoHangHoaDon(hoaDon, vanChuyen),
                hoaDon.getTongTienHang(),
                hoaDon.getTienGiam(),
                hoaDon.getTongTienThanhToan(),
                mapThongTinPhieuGiamGiaHoaDon(hoaDon),
                hoaDon.getNgayTao(),
                items.stream()
                        .map(item -> {
                            String hinhAnh = null;
                            var hinhAnhs = hinhAnhGiayRepository.findByGiayChiTietIdAndTrangThaiOrderByLaHinhChinhDescNgayTaoAsc(item.getGiayChiTiet().getId(), 1);
                            if (hinhAnhs != null && !hinhAnhs.isEmpty()) {
                                hinhAnh = hinhAnhs.get(0).getUrl();
                            }
                            int kichHoat = item.getGiayChiTiet().getKichHoat() != null ? item.getGiayChiTiet().getKichHoat() : 1;
                            int trangThai = item.getGiayChiTiet().getGiay().getTrangThai() != null ? item.getGiayChiTiet().getGiay().getTrangThai() : 1;
                            int trangThaiSanPham = (kichHoat == 0 || trangThai == 0) ? 0 : 1;

                            return new HoaDonChoDongSanPhamResponse(
                                    item.getId(),
                                    item.getGiayChiTiet().getId(),
                                    item.getGiayChiTiet().getGiay().getMa(),
                                    item.getGiayChiTiet().getGiay().getTen(),
                                    item.getGiayChiTiet().getMauSac().getTen(),
                                    item.getGiayChiTiet().getKichCo().getGiaTri(),
                                    item.getGiayChiTiet().getSku(),
                                    hinhAnh,
                                    item.getSoLuong(),
                                    item.getGiayChiTiet().getSoLuong(),
                                    item.getGiaDonVi(),
                                    item.getGiayChiTiet().getGiaBan(),
                                    item.getThanhTien(),
                                    trangThaiSanPham
                            );
                        })
                        .toList()
        );
    }

    public KhachHang timKhachHang(UUID khachHangId) {
        if (khachHangId == null) {
            return null;
        }
        return khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));
    }

    public String layTenKhachHang(KhachHang khachHang, String tenKhachHang) {
        if (khachHang != null && khachHang.getHoTen() != null && !khachHang.getHoTen().isBlank()) {
            return khachHang.getHoTen();
        }
        if (tenKhachHang != null && !tenKhachHang.isBlank()) {
            return tenKhachHang.trim();
        }
        return "";
    }

    public String laySoDienThoai(KhachHang khachHang, String soDienThoai) {
        if (khachHang != null && khachHang.getSdt() != null && !khachHang.getSdt().isBlank()) {
            return khachHang.getSdt();
        }
        if (soDienThoai != null && !soDienThoai.isBlank()) {
            return soDienThoai.trim();
        }
        return "";
    }

    public NhanVien resolveNhanVienDangDangNhap() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal principal) {
            return nhanVienRepository.findById(principal.id()).orElse(null);
        }
        return null;
    }

    public String resolveTenKhachHangHoaDon(HoaDon hoaDon) {
        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getHoTen() != null && !hoaDon.getKhachHang().getHoTen().isBlank()) {
            return hoaDon.getKhachHang().getHoTen();
        }
        return normalizeLegacyDisplayValue(hoaDon.getTenNguoiNhan());
    }

    public String resolveSoDienThoaiKhachHangHoaDon(HoaDon hoaDon) {
        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getSdt() != null && !hoaDon.getKhachHang().getSdt().isBlank()) {
            return hoaDon.getKhachHang().getSdt();
        }
        return normalizeLegacyDisplayValue(hoaDon.getSdtNguoiNhan());
    }

    private GiayChiTiet layGiayChiTietHopLe(Integer chiTietId, Integer soLuong) {
        return layGiayChiTietHopLe(chiTietId, soLuong, null);
    }

    private GiayChiTiet layGiayChiTietHopLe(Integer chiTietId, Integer soLuong, java.util.Set<Integer> bypassActiveCheckIds) {
        GiayChiTiet giayChiTiet = giayChiTietRepository.findById(chiTietId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm chi tiết không tồn tại"));

        boolean bypassActiveCheck = bypassActiveCheckIds != null && bypassActiveCheckIds.contains(chiTietId);
        inventoryUseCase.validateAvailable(giayChiTiet, soLuong, bypassActiveCheck);
        return giayChiTiet;
    }


    private ThongTinGiaoHangTaiQuayResponse mapThongTinGiaoHangHoaDon(HoaDon hoaDon, VanChuyen vanChuyen) {
        boolean giaoHang = hoaDon.getDiaChiGiaoHang() != null;
        return new ThongTinGiaoHangTaiQuayResponse(
                giaoHang,
                normalizeLegacyDisplayValue(hoaDon.getTenNguoiNhan()),
                normalizeLegacyDisplayValue(hoaDon.getSdtNguoiNhan()),
                giaoHang ? DiaChiHaiCapMapper.toResponse(hoaDon.getDiaChiGiaoHang()) : null,
                vanChuyen != null ? pricingUseCase.defaultMoney(vanChuyen.getPhiVanChuyen()) : BigDecimal.ZERO,
                vanChuyen != null ? vanChuyen.getDonViVanChuyen() : null
        );
    }

    private ThongTinPhieuGiamGiaHoaDonResponse mapThongTinPhieuGiamGiaHoaDon(HoaDon hoaDon) {
        if (hoaDon.getPhieuGiamGia() == null) {
            return null;
        }

        return new ThongTinPhieuGiamGiaHoaDonResponse(
                hoaDon.getPhieuGiamGia().getMa(),
                hoaDon.getPhieuGiamGia().getTen(),
                hoaDon.getTienGiam()
        );
    }

    private boolean laDiaChiTaiQuay(String diaChi) {
        String normalized = normalizeTextKey(diaChi);
        return normalized.equals(normalizeTextKey(DIA_CHI_TAI_QUAY))
                || normalized.equals(normalizeTextKey(DIA_CHI_TAI_QUAY_KHONG_DAU));
    }

    private boolean laGiaTriKhongCo(String value) {
        String normalized = normalizeTextKey(value);
        return normalized.equals(normalizeTextKey(KHONG_CO))
                || normalized.equals(normalizeTextKey(KHONG_CO_KHONG_DAU));
    }

    private String normalizeLegacyDisplayValue(String value) {
        if (value == null) {
            return null;
        }

        return switch (normalizeTextKey(value)) {
            case "mua tai quay" -> DIA_CHI_TAI_QUAY;
            case "không có" -> "";
            case "khach le", "khach vang lai" -> "";
            default -> value;
        };
    }

    private String normalizeTextKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String taoMaHoaDonTam() {
        return MA_HOA_DON_TAM_PREFIX + System.currentTimeMillis();
    }
}
