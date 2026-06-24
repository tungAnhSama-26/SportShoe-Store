package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ApDungPhieuGiamGiaRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.ThanhToanTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TinhPhiVanChuyenTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoChiTietResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoTomTatResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.KhachHangTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.PhieuGiamGiaTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.SanPhamTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThanhToanTaiQuayResponse;
import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
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
import com.example.server.repository.GiaoCaRepository;
import com.example.server.entity.GiaoCa;
import com.example.server.entity.DiaChiKhachHang;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BanHangTaiQuayService {

    private final KhachHangTaiQuayService customerUseCase;
    private final SanPhamTaiQuayService productUseCase;
    private final PhieuGiamGiaTaiQuayService voucherUseCase;
    private final HoaDonTaiQuayService invoiceUseCase;
    private final GhnShippingService ghnShippingService;
    private final BanHangTaiQuayValidationUseCase validationUseCase;
    private final BanHangTaiQuayPricingUseCase pricingUseCase;
    private final BanHangTaiQuayInventoryUseCase inventoryUseCase;
    private final BanHangTaiQuayPaymentUseCase paymentUseCase;
    private final BanHangTaiQuayShippingUseCase shippingUseCase;
    private final BanHangTaiQuayInvoiceStateUseCase invoiceStateUseCase;
    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final GiaoCaRepository giaoCaRepository;

    public BanHangTaiQuayService(
            KhachHangTaiQuayService customerUseCase,
            SanPhamTaiQuayService productUseCase,
            PhieuGiamGiaTaiQuayService voucherUseCase,
            HoaDonTaiQuayService invoiceUseCase,
            GhnShippingService ghnShippingService,
            BanHangTaiQuayValidationUseCase validationUseCase,
            BanHangTaiQuayPricingUseCase pricingUseCase,
            BanHangTaiQuayInventoryUseCase inventoryUseCase,
            BanHangTaiQuayPaymentUseCase paymentUseCase,
            BanHangTaiQuayShippingUseCase shippingUseCase,
            BanHangTaiQuayInvoiceStateUseCase invoiceStateUseCase,
            DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository,
            DiaChiKhachHangRepository diaChiKhachHangRepository,
            GiaoCaRepository giaoCaRepository
    ) {
        this.customerUseCase = customerUseCase;
        this.productUseCase = productUseCase;
        this.voucherUseCase = voucherUseCase;
        this.invoiceUseCase = invoiceUseCase;
        this.ghnShippingService = ghnShippingService;
        this.validationUseCase = validationUseCase;
        this.pricingUseCase = pricingUseCase;
        this.inventoryUseCase = inventoryUseCase;
        this.paymentUseCase = paymentUseCase;
        this.shippingUseCase = shippingUseCase;
        this.invoiceStateUseCase = invoiceStateUseCase;
        this.dotGiamGiaSanPhamRepository = dotGiamGiaSanPhamRepository;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
        this.giaoCaRepository = giaoCaRepository;
    }

    @Transactional(readOnly = true)
    public List<KhachHangTaiQuayResponse> timKhachHangTheoTuKhoa(String keyword) {
        return customerUseCase.timKhachHangTheoTuKhoa(keyword);
    }

    @Transactional(readOnly = true)
    public List<SanPhamTaiQuayResponse> timSanPham(String keyword) {
        return productUseCase.timSanPham(keyword);
    }

    @Transactional(readOnly = true)
    public List<PhieuGiamGiaTaiQuayResponse> timPhieuGiamGia(
            String keyword,
            Integer hoaDonId,
            UUID khachHangId,
            BigDecimal tongTienHang
    ) {
        HoaDon hoaDonHienTai = invoiceUseCase.layHoaDonTaiQuayNeuCo(hoaDonId);
        KhachHang khachHang = invoiceUseCase.timKhachHang(khachHangId);
        return voucherUseCase.timPhieuGiamGia(keyword, hoaDonHienTai, khachHang, tongTienHang);
    }

    @Transactional(readOnly = true)
    public PhieuGiamGiaTaiQuayResponse apDungPhieuGiamGia(ApDungPhieuGiamGiaRequest request) {
        HoaDon hoaDonHienTai = invoiceUseCase.layHoaDonTaiQuayNeuCo(request.hoaDonId());
        BigDecimal tongTienHang = voucherUseCase.xacDinhTongTienHangKhiApPhieu(request, hoaDonHienTai, invoiceUseCase.taoDanhSachDongHoaDonTam(request.items()));
        KhachHang khachHang = invoiceUseCase.timKhachHang(request.khachHangId());
        PhieuGiamGiaTaiQuayService.PhieuGiamGiaDuocApDung phieuGiamGia = voucherUseCase.tinhPhieuGiamGiaHopLe(
                request.maPhieuGiamGia(),
                khachHang,
                tongTienHang,
                true,
                hoaDonHienTai
        );
        return voucherUseCase.mapPhieuGiamGiaTaiQuayResponse(phieuGiamGia);
    }

    @Transactional(readOnly = true)
    public TinhPhiVanChuyenGhnResponse tinhPhiVanChuyenGhn(TinhPhiVanChuyenTaiQuayRequest request) {
        List<HoaDonChiTiet> items = invoiceUseCase.taoDanhSachDongHoaDonTam(request.items());
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
        return pendingInvoiceUseCase.taoHoaDonCho(request);
    }

    @Transactional
    public HoaDonChoChiTietResponse capNhatHoaDonCho(Integer hoaDonId, TaoHoaDonChoRequest request) {
        return pendingInvoiceUseCase.capNhatHoaDonCho(hoaDonId, request);
    }

    @Transactional
    public void huyHoaDonCho(Integer hoaDonId) {
        pendingInvoiceUseCase.huyHoaDonCho(hoaDonId);
    }

    @Transactional
    public ThanhToanTaiQuayResponse thanhToanTaiQuay(ThanhToanTaiQuayRequest request) {
        if (request.hoaDonId() == null && (request.items() == null || request.items().isEmpty())) {
            throw new BusinessException("Hóa đơn phải có ít nhất một sản phẩm để thanh toán");
        }
        NhanVien currentEmp = resolveNhanVienDangDangNhap();
        if (currentEmp == null) {
            throw new BusinessException("Nhân viên chưa đăng nhập hoặc phiên đăng nhập hết hạn.");
        }
        GiaoCa activeShift = giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(currentEmp.getId(), "MO_CA")
                .orElseThrow(() -> new BusinessException("Nhân viên không có ca làm việc nào đang hoạt động. Vui lòng mở ca để thực hiện thanh toán."));

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
        hoaDon.setGiaoCa(activeShift);
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
        return invoiceUseCase.layDanhSachHoaDonCho();
    }

    @Transactional(readOnly = true)
    public HoaDonChoChiTietResponse layChiTietHoaDonCho(Integer hoaDonId) {
        return invoiceUseCase.layChiTietHoaDonCho(hoaDonId);
    }
}
