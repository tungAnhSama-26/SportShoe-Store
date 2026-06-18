package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ApDungPhieuGiamGiaRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoItemRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.ThanhToanTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.ThongTinGiaoHangTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TinhPhiVanChuyenTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoChiTietResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoDongSanPhamResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoTomTatResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.KhachHangTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.PhieuGiamGiaTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.SanPhamTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThanhToanTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThongTinGiaoHangTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThongTinPhieuGiamGiaHoaDonResponse;
import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayInventoryUseCase;
import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayInvoiceStateUseCase;
import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayPaymentUseCase;
import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayPricingUseCase;
import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayShippingUseCase;
import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayValidationUseCase;
import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.LichSuHoaDon;
import com.example.server.entity.PhieuGiamGia;
import com.example.server.entity.PhieuGiamGiaKhachHang;
import com.example.server.entity.ThanhToan;
import com.example.server.entity.VanChuyen;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.LichSuHoaDon;
import com.example.server.entity.NhanVien;
import com.example.server.entity.PhieuGiamGia;
import com.example.server.entity.PhieuGiamGiaKhachHang;
import com.example.server.entity.ThanhToan;
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
import com.example.server.repository.PhieuGiamGiaKhachHangRepository;
import com.example.server.repository.PhieuGiamGiaRepository;
import com.example.server.repository.ThanhToanRepository;
import com.example.server.repository.VanChuyenRepository;
import com.example.server.repository.DiaChiKhachHangRepository;
import com.example.server.entity.DiaChiKhachHang;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.server.entity.DotGiamGia;
import com.example.server.entity.DotGiamGiaSanPham;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import java.time.LocalDate;

@Service
public class BanHangTaiQuayService {

    private static final int SO_SAN_PHAM_TIM_TOI_DA = 50;
    private static final int KENH_BAN_TAI_QUAY = 1;
    private static final int TRANG_THAI_HOA_DON_CHO_XAC_NHAN = 1;
    private static final int TRANG_THAI_HOA_DON_CHO_GIAO_HANG = 2;
    private static final int TRANG_THAI_HOA_DON_HOAN_THANH = 5;
    private static final int TRANG_THAI_HOA_DON_HUY = 6;
    private static final int TRANG_THAI_VAN_CHUYEN_CHO_XU_LY = 1;
    private static final int HINH_THUC_TIEN_MAT = 1;
    private static final int HINH_THUC_CHUYEN_KHOAN = 2;
    private static final int HINH_THUC_VI = 3;
    private static final int LOAI_PHIEU_PHAN_TRAM = 1;
    private static final int LOAI_PHIEU_TIEN_MAT = 2;
    private static final int LOAI_PHIEU_MIEN_PHI_VAN_CHUYEN = 3;
    private static final int TRANG_THAI_PHIEU_HOAT_DONG = 1;
    private static final int TRANG_THAI_PHIEU_THEO_KH_DA_DUNG = 0;
    private static final int TRANG_THAI_PHIEU_THEO_KH_CHUA_DUNG = 1;
    private static final String DIA_CHI_TAI_QUAY = "Mua tại quầy";
    private static final String DIA_CHI_TAI_QUAY_KHONG_DAU = "Mua tại quầy";
    private static final String GHI_CHU_TAO_HOA_DON_TAI_QUAY = "Hóa  ơn chờ tạo từ màn hình bán hàng tại quầy";
    private static final String KHACH_VANG_LAI = "Khách vãng lai";
    private static final String KHONG_CO = "Không có";
    private static final String KHONG_CO_KHONG_DAU = "Không có";
    private static final String MA_HOA_DON_TAM_PREFIX = "HD";

    private final KhachHangRepository khachHangRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final HinhAnhGiayRepository hinhAnhGiayRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final NhanVienRepository nhanVienRepository;
    private final VanChuyenRepository vanChuyenRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final GhnShippingService ghnShippingService;
    private final BanHangTaiQuayValidationUseCase validationUseCase;
    private final BanHangTaiQuayPricingUseCase pricingUseCase;
    private final BanHangTaiQuayInventoryUseCase inventoryUseCase;
    private final BanHangTaiQuayPaymentUseCase paymentUseCase;
    private final BanHangTaiQuayShippingUseCase shippingUseCase;
    private final BanHangTaiQuayInvoiceStateUseCase invoiceStateUseCase;
    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;

    public BanHangTaiQuayService(
            KhachHangRepository khachHangRepository,
            GiayChiTietRepository giayChiTietRepository,
            HinhAnhGiayRepository hinhAnhGiayRepository,
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanRepository thanhToanRepository,
            LichSuHoaDonRepository lichSuHoaDonRepository,
            NhanVienRepository nhanVienRepository,
            VanChuyenRepository vanChuyenRepository,
            PhieuGiamGiaRepository phieuGiamGiaRepository,
            PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository,
            GhnShippingService ghnShippingService,
            BanHangTaiQuayValidationUseCase validationUseCase,
            BanHangTaiQuayPricingUseCase pricingUseCase,
            BanHangTaiQuayInventoryUseCase inventoryUseCase,
            BanHangTaiQuayPaymentUseCase paymentUseCase,
            BanHangTaiQuayShippingUseCase shippingUseCase,
            BanHangTaiQuayInvoiceStateUseCase invoiceStateUseCase,
            DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository,
            DiaChiKhachHangRepository diaChiKhachHangRepository
    ) {
        this.khachHangRepository = khachHangRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.lichSuHoaDonRepository = lichSuHoaDonRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.vanChuyenRepository = vanChuyenRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.phieuGiamGiaKhachHangRepository = phieuGiamGiaKhachHangRepository;
        this.ghnShippingService = ghnShippingService;
        this.validationUseCase = validationUseCase;
        this.pricingUseCase = pricingUseCase;
        this.inventoryUseCase = inventoryUseCase;
        this.paymentUseCase = paymentUseCase;
        this.shippingUseCase = shippingUseCase;
        this.invoiceStateUseCase = invoiceStateUseCase;
        this.dotGiamGiaSanPhamRepository = dotGiamGiaSanPhamRepository;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
    }

    @Transactional(readOnly = true)
    public List<KhachHangTaiQuayResponse> timKhachHangTheoTuKhoa(String keyword) {
        return khachHangRepository.searchByKeyword(chuanHoaTuKhoa(keyword))
                .stream()
                .limit(10)
                .map(khachHang -> {
                    DiaChiKhachHang diaChiMacDinh = diaChiKhachHangRepository
                            .findFirstByKhachHangIdAndLaMacDinhTrue(khachHang.getId())
                            .orElse(null);
                    String diaChiMacDinhText = diaChiMacDinh != null
                            ? diaChiMacDinh.getDiaChiCuThe() + ", " + diaChiMacDinh.getPhuongXa() + ", "
                                    + diaChiMacDinh.getQuanHuyen() + ", " + diaChiMacDinh.getTinhThanh()
                            : null;
                    return new KhachHangTaiQuayResponse(
                            khachHang.getId(),
                            khachHang.getHoTen(),
                            khachHang.getSdt(),
                            khachHang.getEmail(),
                            diaChiMacDinhText
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<PhieuGiamGiaTaiQuayResponse> timPhieuGiamGia(
            String keyword,
            Integer hoaDonId,
            UUID khachHangId,
            BigDecimal tongTienHang
    ) {
        HoaDon hoaDonHienTai = layHoaDonTaiQuayNeuCo(hoaDonId);
        BigDecimal tongTienHangHienTai = tongTienHang != null
                ? tongTienHang
                : (hoaDonHienTai != null ? hoaDonHienTai.getTongTienHang() : BigDecimal.ZERO);

        if (tongTienHangHienTai == null || tongTienHangHienTai.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        KhachHang khachHang = timKhachHang(khachHangId);
        List<PhieuGiamGiaTaiQuayResponse> ketQua = new ArrayList<>();

        for (PhieuGiamGia phieuGiamGia : phieuGiamGiaRepository.searchByKeyword(
                chuanHoaTuKhoa(keyword),
                PageRequest.of(0, 20)
        )) {
            try {
                validatePhieuGiamGia(phieuGiamGia, khachHang, tongTienHangHienTai, hoaDonHienTai);
                BigDecimal soTienGiam = pricingUseCase.tinhSoTienGiam(phieuGiamGia, tongTienHangHienTai);
                BigDecimal tongTienSauGiam = tongTienHangHienTai.subtract(soTienGiam);
                ketQua.add(mapPhieuGiamGiaTaiQuayResponse(
                        new PhieuGiamGiaDuocApDung(phieuGiamGia, soTienGiam, tongTienSauGiam)
                ));
            } catch (BusinessException exception) {
                // Bo qua cac phieu khong hop le voi gio hang hien tai.
            }

            if (ketQua.size() >= 8) {
                break;
            }
        }

        return ketQua;
    }

    @Transactional(readOnly = true)
    public PhieuGiamGiaTaiQuayResponse apDungPhieuGiamGia(ApDungPhieuGiamGiaRequest request) {
        HoaDon hoaDonHienTai = layHoaDonTaiQuayNeuCo(request.hoaDonId());
        BigDecimal tongTienHang = xacDinhTongTienHangKhiApPhieu(request, hoaDonHienTai);
        KhachHang khachHang = timKhachHang(request.khachHangId());
        PhieuGiamGiaDuocApDung phieuGiamGia = tinhPhieuGiamGiaHopLe(
                request.maPhieuGiamGia(),
                khachHang,
                tongTienHang,
                true,
                hoaDonHienTai
        );
        return mapPhieuGiamGiaTaiQuayResponse(phieuGiamGia);
    }

    @Transactional(readOnly = true)
    public TinhPhiVanChuyenGhnResponse tinhPhiVanChuyenGhn(TinhPhiVanChuyenTaiQuayRequest request) {
        List<HoaDonChiTiet> items = taoDanhSachDongHoaDonTam(request.items());
        HoaDon hoaDonTam = new HoaDon();
        hoaDonTam.setTongTienHang(
                items.stream()
                        .map(HoaDonChiTiet::getThanhTien)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        return ghnShippingService.tinhPhi(
                hoaDonTam,
                items,
                new TinhPhiVanChuyenGhnRequest(
                        request.toDistrictId(),
                        request.toWardCode(),
                        request.toAddress(),
                        request.serviceId(),
                        request.serviceTypeId(),
                        request.length(),
                        request.width(),
                        request.height(),
                        request.weight(),
                        request.insuranceValue(),
                        request.coupon()
                )
        );
    }

    @Transactional
    public HoaDonChoChiTietResponse taoHoaDonCho(TaoHoaDonChoRequest request) {
        long soLuongHoaDonCho = hoaDonRepository.countByKenhBanAndTrangThai(KENH_BAN_TAI_QUAY, TRANG_THAI_HOA_DON_CHO_XAC_NHAN);
        if (soLuongHoaDonCho >= 5) {
            throw new BusinessException("Đã đạt giới hạn tối đa 5 hóa đơn chờ");
        }

        HoaDon savedHoaDon = taoHoaDon(
                request.khachHangId(),
                request.tenKhachHang(),
                request.soDienThoai(),
                request.maPhieuGiamGia(),
                request.thongTinGiaoHang(),
                request.items(),
                TRANG_THAI_HOA_DON_CHO_XAC_NHAN,
                GHI_CHU_TAO_HOA_DON_TAI_QUAY
        );
        luuLichSuHoaDon(savedHoaDon, TRANG_THAI_HOA_DON_CHO_XAC_NHAN, savedHoaDon.getGhiChu());
        List<HoaDonChiTiet> savedItems = hoaDonChiTietRepository.findByHoaDonIdWithProduct(savedHoaDon.getId());

        return mapHoaDonChiTiet(savedHoaDon, savedItems, vanChuyenRepository.findByHoaDonId(savedHoaDon.getId()).orElse(null));
    }

    @Transactional
    public HoaDonChoChiTietResponse capNhatHoaDonCho(Integer hoaDonId, TaoHoaDonChoRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại"));

        if (!invoiceStateUseCase.kenhBanTaiQuay(hoaDon.getKenhBan())) {
            throw new BusinessException("Chỉ hỗ trợ cập nhật hóa đơn tại quầy");
        }

        if (!invoiceStateUseCase.trangThaiHoaDonCho(hoaDon.getTrangThai())) {
            throw new BusinessException("Chỉ được cập nhật hóa đơn đang chờ");
        }

        // Return old items to stock and delete them
        List<HoaDonChiTiet> oldItems = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDonId);
        for (HoaDonChiTiet item : oldItems) {
            GiayChiTiet giayChiTiet = item.getGiayChiTiet();
            giayChiTiet.setSoLuong((giayChiTiet.getSoLuong() == null ? 0 : giayChiTiet.getSoLuong()) + item.getSoLuong());
            giayChiTiet.setNgayCapNhat(Instant.now());
            giayChiTietRepository.save(giayChiTiet);
            hoaDonChiTietRepository.delete(item);
        }
        hoaDonChiTietRepository.flush();

        // Add new items
        validationUseCase.validateDuplicateItems(request.items() != null ? request.items() : new java.util.ArrayList<>());
        List<HoaDonChiTiet> chiTietTam = request.items() != null && !request.items().isEmpty() ? request.items().stream()
                .map(item -> taoDongHoaDon(item.chiTietId(), item.soLuong()))
                .toList() : new java.util.ArrayList<>();

        BigDecimal tongTienHang = chiTietTam.stream()
                .map(HoaDonChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        KhachHang khachHang = request.khachHangId() != null ? timKhachHang(request.khachHangId()) : null;
        String tenKhachHang = layTenKhachHang(khachHang, request.tenKhachHang());
        String soDienThoai = laySoDienThoai(khachHang, request.soDienThoai());

        if (hoaDon.getPhieuGiamGia() != null) {
            giaiPhongPhieuGiamGia(hoaDon.getPhieuGiamGia(), hoaDon.getKhachHang());
        }

        hoaDon.setTongTienHang(tongTienHang);
        hoaDon.setTongTienThanhToan(tongTienHang);
        
        ganPhieuGiamGiaChoHoaDon(hoaDon, request.maPhieuGiamGia(), khachHang, tongTienHang);
        hoaDon.setKhachHang(khachHang);
        apDungThongTinGiaoHangChoHoaDon(hoaDon, request.thongTinGiaoHang(), tenKhachHang, soDienThoai);
        
        hoaDon.setNgayCapNhat(Instant.now());
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        dongBoVanChuyen(savedHoaDon, request.thongTinGiaoHang());

        List<HoaDonChiTiet> chiTietCanLuu = new java.util.ArrayList<>();
        for (HoaDonChiTiet item : chiTietTam) {
            item.setHoaDon(savedHoaDon);
            chiTietCanLuu.add(hoaDonChiTietRepository.save(item));
        }

        return mapHoaDonChiTiet(savedHoaDon, chiTietCanLuu, vanChuyenRepository.findByHoaDonId(savedHoaDon.getId()).orElse(null));
    }

    @Transactional
    public void huyHoaDonCho(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại"));

        if (!invoiceStateUseCase.kenhBanTaiQuay(hoaDon.getKenhBan())) {
            throw new BusinessException("Chỉ hỗ trợ hủy hóa đơn tại quầy");
        }

        if (!invoiceStateUseCase.trangThaiHoaDonCho(hoaDon.getTrangThai())) {
            throw new BusinessException("Chỉ được hủy hóa đơn đang chờ");
        }

        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDonId);
        for (HoaDonChiTiet item : items) {
            GiayChiTiet giayChiTiet = item.getGiayChiTiet();
            giayChiTiet.setSoLuong((giayChiTiet.getSoLuong() == null ? 0 : giayChiTiet.getSoLuong()) + item.getSoLuong());
            giayChiTiet.setNgayCapNhat(Instant.now());
            giayChiTietRepository.save(giayChiTiet);
            item.setTrangThai(0);
            hoaDonChiTietRepository.save(item);
        }

        if (hoaDon.getPhieuGiamGia() != null) {
            giaiPhongPhieuGiamGia(hoaDon.getPhieuGiamGia(), hoaDon.getKhachHang());
            hoaDon.setPhieuGiamGia(null);
            hoaDon.setTienGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(hoaDon.getTongTienHang());
        }

        hoaDon.setTrangThai(TRANG_THAI_HOA_DON_HUY);
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDon.setGhiChu("Hoa don cho da bi huy");
        NhanVien currentEmp = resolveNhanVienDangDangNhap();
        if (currentEmp != null) {
            hoaDon.setNhanVien(currentEmp);
        }
        hoaDonRepository.save(hoaDon);
        luuLichSuHoaDon(hoaDon, TRANG_THAI_HOA_DON_HUY, hoaDon.getGhiChu());
    }

    @Transactional
    public ThanhToanTaiQuayResponse thanhToanTaiQuay(ThanhToanTaiQuayRequest request) {
        if (request.hoaDonId() == null && (request.items() == null || request.items().isEmpty())) {
            throw new BusinessException("Hóa đơn phải có ít nhất một sản phẩm để thanh toán");
        }
        paymentUseCase.validateTienKhachDua(request.tienKhachDua());
        Integer trangThaiSauThanhToan = invoiceStateUseCase.xacDinhTrangThaiSauThanhToan(request.thongTinGiaoHang());
        HoaDon hoaDon = request.hoaDonId() == null
                ? taoHoaDon(
                request.khachHangId(),
                request.tenKhachHang(),
                request.soDienThoai(),
                request.maPhieuGiamGia(),
                request.thongTinGiaoHang(),
                request.items(),
                trangThaiSauThanhToan,
                request.ghiChu()
        )
                : thanhToanHoaDonCho(request);

        BigDecimal tongTien = hoaDon.getTongTienThanhToan();
        BigDecimal tienKhachDua = paymentUseCase.xacDinhTienKhachDua(request.hinhThucThanhToan(), request.tienKhachDua(), tongTien);
        BigDecimal tienThua = paymentUseCase.tinhTienThua(request.hinhThucThanhToan(), tienKhachDua, tongTien);

        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setHoaDon(hoaDon);
        thanhToan.setNhanVien(hoaDon.getNhanVien());
        thanhToan.setHinhThuc(paymentUseCase.mapHinhThucThanhToan(request.hinhThucThanhToan()));
        thanhToan.setSoTien(tongTien);
        thanhToan.setTienThoiLai(tienThua);
        thanhToan.setCongThanhToan(paymentUseCase.resolveCongThanhToan(request.hinhThucThanhToan()));
        thanhToan.setNgayThanhToan(Instant.now());
        thanhToan.setTrangThai(1);
        thanhToan.setLoaiGiaoDich(1); // 1: Thanh toan
        thanhToan.setGhiChu(request.ghiChu());
        thanhToan.setNgayTao(Instant.now());
        thanhToanRepository.save(thanhToan);

        hoaDon.setTrangThai(trangThaiSauThanhToan);
        hoaDon.setNgayThanhToan(Instant.now());
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hoaDon);
        luuLichSuHoaDon(hoaDon, trangThaiSauThanhToan, request.ghiChu());

        return new ThanhToanTaiQuayResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null,
                hoaDon.getTongTienHang(),
                hoaDon.getTienGiam(),
                tongTien,
                tienKhachDua,
                tienThua,
                request.hinhThucThanhToan(),
                resolveTenKhachHangHoaDon(hoaDon),
                resolveSoDienThoaiKhachHangHoaDon(hoaDon),
                mapThongTinGiaoHangHoaDon(hoaDon, vanChuyenRepository.findByHoaDonId(hoaDon.getId()).orElse(null)),
                mapThongTinPhieuGiamGiaHoaDon(hoaDon),
                hoaDon.getNgayThanhToan()
        );
    }

    @Transactional(readOnly = true)
    public List<HoaDonChoTomTatResponse> layDanhSachHoaDonCho() {
        List<HoaDon> hoaDons = hoaDonRepository.findTop10ByKenhBanAndTrangThaiOrderByNgayTaoDesc(
                        KENH_BAN_TAI_QUAY,
                        TRANG_THAI_HOA_DON_CHO_XAC_NHAN
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

    @Transactional(readOnly = true)
    public HoaDonChoChiTietResponse layChiTietHoaDonCho(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại"));
        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDonId);
        return mapHoaDonChiTiet(hoaDon, items, vanChuyenRepository.findByHoaDonId(hoaDonId).orElse(null));
    }

    private NhanVien resolveNhanVienDangDangNhap() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal principal) {
            return nhanVienRepository.findById(principal.id()).orElse(null);
        }
        return null;
    }

    private void luuLichSuHoaDon(HoaDon hoaDon, Integer trangThai, String ghiChu) {
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setNhanVien(resolveNhanVienDangDangNhap());
        lichSu.setTrangThai(invoiceStateUseCase.labelTrangThaiHoaDon(trangThai));
        lichSu.setGhiChu(ghiChu);
        lichSu.setNgayTao(Instant.now());
        lichSuHoaDonRepository.save(lichSu);
    }

    private HoaDon taoHoaDon(
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
                .map(item -> taoDongHoaDon(item.chiTietId(), item.soLuong()))
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
        
        ganPhieuGiamGiaChoHoaDon(hoaDon, maPhieuGiamGia, khachHang, tongTienHang);
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

        List<HoaDonChiTiet> chiTietCanLuu = new ArrayList<>();
        for (HoaDonChiTiet item : chiTietTam) {
            item.setHoaDon(savedHoaDon);
            chiTietCanLuu.add(hoaDonChiTietRepository.save(item));
        }

        return savedHoaDon;
    }

    private HoaDon thanhToanHoaDonCho(ThanhToanTaiQuayRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(request.hoaDonId())
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại"));

        if (!invoiceStateUseCase.kenhBanTaiQuay(hoaDon.getKenhBan())) {
            throw new BusinessException("Chỉ hỗ trợ thanh toán hóa đơn tại quầy");
        }

        if (!invoiceStateUseCase.trangThaiHoaDonCho(hoaDon.getTrangThai())) {
            throw new BusinessException("Hóa đơn này không ở trạng thái chờ thanh toán");
        }

        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId());
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Hóa đơn phải có ít nhất một sản phẩm để thanh toán");
        }

        KhachHang khachHang = timKhachHang(request.khachHangId());
        String tenKhachHang = layTenKhachHang(khachHang, request.tenKhachHang());
        String soDienThoai = laySoDienThoai(khachHang, request.soDienThoai());

        if (hoaDon.getPhieuGiamGia() != null) {
            giaiPhongPhieuGiamGia(hoaDon.getPhieuGiamGia(), hoaDon.getKhachHang());
        }
        ganPhieuGiamGiaChoHoaDon(hoaDon, request.maPhieuGiamGia(), khachHang, hoaDon.getTongTienHang());
        hoaDon.setKhachHang(khachHang);
        apDungThongTinGiaoHangChoHoaDon(hoaDon, request.thongTinGiaoHang(), tenKhachHang, soDienThoai);
        hoaDon.setGhiChu(request.ghiChu());
        
        NhanVien currentEmp = resolveNhanVienDangDangNhap();
        if (currentEmp != null) {
            hoaDon.setNhanVien(currentEmp);
        }
        
        hoaDon.setNgayCapNhat(Instant.now());
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        dongBoVanChuyen(savedHoaDon, request.thongTinGiaoHang());
        return savedHoaDon;
    }

    private void ganPhieuGiamGiaChoHoaDon(HoaDon hoaDon, String maPhieuGiamGia, KhachHang khachHang, BigDecimal tongTienHang) {
        if (maPhieuGiamGia == null || maPhieuGiamGia.isBlank()) {
            hoaDon.setPhieuGiamGia(null);
            hoaDon.setTienGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(tongTienHang);
            return;
        }

        PhieuGiamGiaDuocApDung phieuApDung = tinhPhieuGiamGiaHopLe(
                maPhieuGiamGia,
                khachHang,
                tongTienHang,
                false,
                hoaDon
        );

        PhieuGiamGia pgg = phieuGiamGiaRepository.findByIdForUpdate(phieuApDung.phieuGiamGia().getId())
                .orElseThrow(() -> new BusinessException("Phiếu giảm giá không tồn tại"));
        int tongSoLuong = pgg.getSoLuong() == null ? 0 : pgg.getSoLuong();
        int daDung = pgg.getSoLuongDaDung() == null ? 0 : pgg.getSoLuongDaDung();
        if (tongSoLuong != 999999 && daDung >= tongSoLuong) {
            throw new BusinessException("Phiếu giảm giá đã hết lượt sử dụng");
        }
        pgg.setSoLuongDaDung(daDung + 1);
        phieuGiamGiaRepository.save(pgg);

        if (khachHang != null) {
            PhieuGiamGiaKhachHang pggh = phieuGiamGiaKhachHangRepository
                    .findByPhieuGiamGiaIdAndKhachHangId(pgg.getId(), khachHang.getId())
                    .orElse(null);
            if (pggh != null) {
                pggh.setTrangThai(TRANG_THAI_PHIEU_THEO_KH_DA_DUNG);
                pggh.setNgaySuDung(Instant.now());
                phieuGiamGiaKhachHangRepository.save(pggh);
            }
        }

        hoaDon.setPhieuGiamGia(pgg);
        hoaDon.setTienGiam(phieuApDung.soTienGiam());
        hoaDon.setTongTienThanhToan(phieuApDung.tongTienSauGiam());
    }

    private void giaiPhongPhieuGiamGia(PhieuGiamGia phieuGiamGia, KhachHang khachHang) {
        if (phieuGiamGia == null) {
            return;
        }
        phieuGiamGia.setSoLuongDaDung(Math.max(
                0,
                (phieuGiamGia.getSoLuongDaDung() == null ? 0 : phieuGiamGia.getSoLuongDaDung()) - 1
        ));
        phieuGiamGiaRepository.save(phieuGiamGia);

        if (khachHang != null) {
            PhieuGiamGiaKhachHang pggh = phieuGiamGiaKhachHangRepository
                    .findByPhieuGiamGiaIdAndKhachHangId(phieuGiamGia.getId(), khachHang.getId())
                    .orElse(null);
            if (pggh != null) {
                pggh.setTrangThai(TRANG_THAI_PHIEU_THEO_KH_CHUA_DUNG);
                pggh.setNgaySuDung(null);
                phieuGiamGiaKhachHangRepository.save(pggh);
            }
        }
    }

    private PhieuGiamGiaDuocApDung tinhPhieuGiamGiaHopLe(
            String maPhieuGiamGia,
            KhachHang khachHang,
            BigDecimal tongTienHang,
            boolean validateSoLuong,
            HoaDon hoaDon
    ) {
        if (maPhieuGiamGia == null || maPhieuGiamGia.isBlank()) {
            throw new BusinessException("Mã phiếu giảm giá không được để trống");
        }

        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findByMaIgnoreCase(maPhieuGiamGia)
                .orElseThrow(() -> new BusinessException("Phiếu giảm giá không tồn tại"));

        validatePhieuGiamGia(phieuGiamGia, khachHang, tongTienHang, hoaDon);

        boolean isAlreadyApplied = hoaDon != null && hoaDon.getPhieuGiamGia() != null && hoaDon.getPhieuGiamGia().getId().equals(phieuGiamGia.getId());

        int tongSoLuong = phieuGiamGia.getSoLuong() == null ? 0 : phieuGiamGia.getSoLuong();
        int daDung = phieuGiamGia.getSoLuongDaDung() == null ? 0 : phieuGiamGia.getSoLuongDaDung();
        if (validateSoLuong && !isAlreadyApplied && tongSoLuong != 999999 && daDung >= tongSoLuong) {
            throw new BusinessException("Phiếu giảm giá đã hết lượt sử dụng");
        }

        BigDecimal soTienGiam = pricingUseCase.tinhSoTienGiam(phieuGiamGia, tongTienHang);
        BigDecimal tongTienSauGiam = tongTienHang.subtract(soTienGiam).max(BigDecimal.ZERO);

        return new PhieuGiamGiaDuocApDung(phieuGiamGia, soTienGiam, tongTienSauGiam);
    }

    private void validatePhieuGiamGia(
            PhieuGiamGia phieuGiamGia,
            KhachHang khachHang,
            BigDecimal tongTienHang,
            HoaDon hoaDon
    ) {
        if (phieuGiamGia.getTrangThai() == null || phieuGiamGia.getTrangThai() != TRANG_THAI_PHIEU_HOAT_DONG) {
            throw new BusinessException("Phiếu giảm giá không hoạt động");
        }

        Instant now = Instant.now();
        if (phieuGiamGia.getNgayBatDau() != null && now.isBefore(phieuGiamGia.getNgayBatDau())) {
            throw new BusinessException("Phiếu giảm giá chưa đến thời gian áp dụng");
        }

        if (phieuGiamGia.getNgayKetThuc() != null && now.isAfter(phieuGiamGia.getNgayKetThuc())) {
            throw new BusinessException("Phiếu giảm giá đã hết hạn sử dụng");
        }

        if (phieuGiamGia.getGiaTriToiThieu() != null && tongTienHang.compareTo(phieuGiamGia.getGiaTriToiThieu()) < 0) {
            throw new BusinessException("Giá trị đơn hàng chưa đạt tối thiểu " + phieuGiamGia.getGiaTriToiThieu());
        }

        boolean isAlreadyApplied = hoaDon != null && hoaDon.getPhieuGiamGia() != null && hoaDon.getPhieuGiamGia().getId().equals(phieuGiamGia.getId());

        if (phieuGiamGia.getLoaiPhieu() != null && phieuGiamGia.getLoaiPhieu() == 2) {
            if (khachHang == null) {
                throw new BusinessException("Phiếu giảm giá này chỉ áp dụng cho khách hàng thành viên");
            }
            PhieuGiamGiaKhachHang pggh = phieuGiamGiaKhachHangRepository
                    .findByPhieuGiamGiaIdAndKhachHangId(phieuGiamGia.getId(), khachHang.getId())
                    .orElseThrow(() -> new BusinessException("Khách hàng không sở hữu phiếu giảm giá này"));

            if (!isAlreadyApplied && pggh.getTrangThai() != TRANG_THAI_PHIEU_THEO_KH_CHUA_DUNG) {
                throw new BusinessException("Phiếu giảm giá đã được khách hàng sử dụng");
            }
        }
    }

    private BigDecimal xacDinhTongTienHangKhiApPhieu(ApDungPhieuGiamGiaRequest request, HoaDon hoaDonHienTai) {
        if (request.items() != null && !request.items().isEmpty()) {
            List<HoaDonChiTiet> itemsTam = taoDanhSachDongHoaDonTam(request.items());
            return itemsTam.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        if (hoaDonHienTai != null) {
            return hoaDonHienTai.getTongTienHang();
        }

        return BigDecimal.ZERO;
    }

    private HoaDon layHoaDonTaiQuayNeuCo(Integer hoaDonId) {
        if (hoaDonId == null) {
            return null;
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại"));

        if (!invoiceStateUseCase.kenhBanTaiQuay(hoaDon.getKenhBan())) {
            throw new BusinessException("Chỉ hỗ trợ áp dụng phiếu giảm giá cho hóa đơn tại quầy");
        }

        return hoaDon;
    }

    private HoaDonChiTiet taoDongHoaDon(Integer chiTietId, Integer soLuong) {
        GiayChiTiet giayChiTiet = layGiayChiTietHopLe(chiTietId, soLuong);

        inventoryUseCase.deductStock(giayChiTiet, soLuong);
        giayChiTietRepository.save(giayChiTiet);

        BigDecimal giaThucTe = layGiaBanThucTe(giayChiTiet);
        HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
        hoaDonChiTiet.setGiayChiTiet(giayChiTiet);
        hoaDonChiTiet.setSoLuong(soLuong);
        hoaDonChiTiet.setGiaDonVi(giaThucTe);
        hoaDonChiTiet.setThanhTien(giaThucTe.multiply(BigDecimal.valueOf(soLuong.longValue())));
        hoaDonChiTiet.setTrangThai(1);
        hoaDonChiTiet.setNgayTao(Instant.now());
        return hoaDonChiTiet;
    }

    private GiayChiTiet layGiayChiTietHopLe(Integer chiTietId, Integer soLuong) {
        GiayChiTiet giayChiTiet = giayChiTietRepository.findById(chiTietId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm chi tiết không tồn tại"));

        inventoryUseCase.validateAvailable(giayChiTiet, soLuong);
        return giayChiTiet;
    }

    private HoaDonChoChiTietResponse mapHoaDonChiTiet(HoaDon hoaDon, List<HoaDonChiTiet> items, VanChuyen vanChuyen) {
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
                        .map(item -> new HoaDonChoDongSanPhamResponse(
                                item.getGiayChiTiet().getId(),
                                item.getGiayChiTiet().getGiay().getMa(),
                                item.getGiayChiTiet().getGiay().getTen(),
                                item.getSoLuong(),
                                item.getGiaDonVi(),
                                item.getThanhTien()
                        ))
                        .toList()
        );
    }

    private List<HoaDonChiTiet> taoDanhSachDongHoaDonTam(List<TaoHoaDonChoItemRequest> items) {
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
        BigDecimal giaThucTe = layGiaBanThucTe(giayChiTiet);
        HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
        hoaDonChiTiet.setGiayChiTiet(giayChiTiet);
        hoaDonChiTiet.setSoLuong(item.soLuong());
        hoaDonChiTiet.setGiaDonVi(giaThucTe);
        hoaDonChiTiet.setThanhTien(giaThucTe.multiply(BigDecimal.valueOf(item.soLuong().longValue())));
        hoaDonChiTiet.setTrangThai(1);
        hoaDonChiTiet.setNgayTao(Instant.now());
        return hoaDonChiTiet;
    }

    private void apDungThongTinGiaoHangChoHoaDon(
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

        String diaChiGiaoHang = giaoHang ? shippingUseCase.requireDiaChiGiaoHang(thongTinGiaoHang) : DIA_CHI_TAI_QUAY;
        BigDecimal phiVanChuyen = giaoHang ? shippingUseCase.resolvePhiVanChuyen(thongTinGiaoHang) : BigDecimal.ZERO;

        hoaDon.setTenNguoiNhan(tenNguoiNhan);
        hoaDon.setSdtNguoiNhan(soDienThoaiNguoiNhan);
        hoaDon.setDiaChiGiaoHang(diaChiGiaoHang);
        hoaDon.setTongTienThanhToan(pricingUseCase.defaultMoney(hoaDon.getTongTienThanhToan()).add(phiVanChuyen).max(BigDecimal.ZERO));
    }

    private void dongBoVanChuyen(HoaDon hoaDon, ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang) {
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

    private PhieuGiamGiaTaiQuayResponse mapPhieuGiamGiaTaiQuayResponse(PhieuGiamGiaDuocApDung phieuGiamGia) {
        return new PhieuGiamGiaTaiQuayResponse(
                phieuGiamGia.phieuGiamGia().getId(),
                phieuGiamGia.phieuGiamGia().getMa(),
                phieuGiamGia.phieuGiamGia().getTen(),
                phieuGiamGia.phieuGiamGia().getLoai(),
                phieuGiamGia.phieuGiamGia().getGiaTri(),
                phieuGiamGia.phieuGiamGia().getGiaTriToiThieu(),
                phieuGiamGia.phieuGiamGia().getGiamToiDa(),
                phieuGiamGia.soTienGiam(),
                phieuGiamGia.tongTienHang(),
                phieuGiamGia.tongTienSauGiam()
        );
    }

    private KhachHang timKhachHang(UUID khachHangId) {
        if (khachHangId == null) {
            return null;
        }
        return khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));
    }

    private String layTenKhachHang(KhachHang khachHang, String tenKhachHang) {
        if (khachHang != null && khachHang.getHoTen() != null && !khachHang.getHoTen().isBlank()) {
            return khachHang.getHoTen();
        }
        if (tenKhachHang != null && !tenKhachHang.isBlank()) {
            return tenKhachHang.trim();
        }
        return "";
    }

    private String laySoDienThoai(KhachHang khachHang, String soDienThoai) {
        if (khachHang != null && khachHang.getSdt() != null && !khachHang.getSdt().isBlank()) {
            return khachHang.getSdt();
        }
        if (soDienThoai != null && !soDienThoai.isBlank()) {
            return soDienThoai.trim();
        }
        return "";
    }

    private String resolveTenKhachHangHoaDon(HoaDon hoaDon) {
        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getHoTen() != null && !hoaDon.getKhachHang().getHoTen().isBlank()) {
            return hoaDon.getKhachHang().getHoTen();
        }
        return normalizeLegacyDisplayValue(hoaDon.getTenNguoiNhan());
    }

    private String resolveSoDienThoaiKhachHangHoaDon(HoaDon hoaDon) {
        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getSdt() != null && !hoaDon.getKhachHang().getSdt().isBlank()) {
            return hoaDon.getKhachHang().getSdt();
        }
        return normalizeLegacyDisplayValue(hoaDon.getSdtNguoiNhan());
    }

    private ThongTinGiaoHangTaiQuayResponse mapThongTinGiaoHangHoaDon(HoaDon hoaDon, VanChuyen vanChuyen) {
        boolean giaoHang = hoaDon.getDiaChiGiaoHang() != null && !laDiaChiTaiQuay(hoaDon.getDiaChiGiaoHang());
        return new ThongTinGiaoHangTaiQuayResponse(
                giaoHang,
                normalizeLegacyDisplayValue(hoaDon.getTenNguoiNhan()),
                normalizeLegacyDisplayValue(hoaDon.getSdtNguoiNhan()),
                giaoHang ? normalizeLegacyDisplayValue(hoaDon.getDiaChiGiaoHang()) : "",
                vanChuyen != null ? pricingUseCase.defaultMoney(vanChuyen.getPhiVanChuyen()) : BigDecimal.ZERO,
                vanChuyen != null ? vanChuyen.getDonViVanChuyen() : null
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
            case "hoa don cho tao tu man hinh ban hang tai quay" -> GHI_CHU_TAO_HOA_DON_TAI_QUAY;
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

    private String taoMaHoaDon(Integer hoaDonId) {
        return String.format("%s%06d", MA_HOA_DON_TAM_PREFIX, hoaDonId);
    }

    private String chuanHoaTuKhoa(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private BigDecimal layGiaBanThucTe(GiayChiTiet gct) {
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

    private record PhieuGiamGiaDuocApDung(
            PhieuGiamGia phieuGiamGia,
            BigDecimal soTienGiam,
            BigDecimal tongTienSauGiam
    ) {
        private BigDecimal tongTienHang() {
            return tongTienSauGiam.add(soTienGiam);
        }
    }
}

