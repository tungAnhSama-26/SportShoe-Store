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
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.ThanhToan;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.ThanhToanRepository;
import com.example.server.repository.VanChuyenRepository;
import com.example.server.infrastructure.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BanHangTaiQuayService {

    private static final int KENH_BAN_TAI_QUAY = 1;
    private static final int TRANG_THAI_HOA_DON_CHO_XAC_NHAN = 1;
    private static final int TRANG_THAI_HOA_DON_HUY = 6;
    private static final String GHI_CHU_TAO_HOA_DON_TAI_QUAY = "Hóa đơn chờ tạo từ màn hình bán hàng tại quầy";

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final VanChuyenRepository vanChuyenRepository;
    private final GhnShippingService ghnShippingService;
    private final EmailService emailService;

    private final XacThucTaiQuayService validationUseCase;
    private final ThanhToanTaiQuayService paymentUseCase;
    private final TrangThaiHoaDonTaiQuayService invoiceStateUseCase;
    private final KhachHangTaiQuayService customerUseCase;
    private final SanPhamTaiQuayService productUseCase;
    private final PhieuGiamGiaTaiQuayService voucherUseCase;
    private final HoaDonTaiQuayService invoiceUseCase;

    public BanHangTaiQuayService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanRepository thanhToanRepository,
            GiayChiTietRepository giayChiTietRepository,
            VanChuyenRepository vanChuyenRepository,
            GhnShippingService ghnShippingService,
            EmailService emailService,
            XacThucTaiQuayService validationUseCase,
            ThanhToanTaiQuayService paymentUseCase,
            TrangThaiHoaDonTaiQuayService invoiceStateUseCase,
            KhachHangTaiQuayService customerUseCase,
            SanPhamTaiQuayService productUseCase,
            PhieuGiamGiaTaiQuayService voucherUseCase,
            HoaDonTaiQuayService invoiceUseCase
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.vanChuyenRepository = vanChuyenRepository;
        this.ghnShippingService = ghnShippingService;
        this.emailService = emailService;
        this.validationUseCase = validationUseCase;
        this.paymentUseCase = paymentUseCase;
        this.invoiceStateUseCase = invoiceStateUseCase;
        this.customerUseCase = customerUseCase;
        this.productUseCase = productUseCase;
        this.voucherUseCase = voucherUseCase;
        this.invoiceUseCase = invoiceUseCase;
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
        long soLuongHoaDonCho = hoaDonRepository.countByKenhBanAndTrangThai(KENH_BAN_TAI_QUAY, TRANG_THAI_HOA_DON_CHO_XAC_NHAN);
        if (soLuongHoaDonCho >= 5) {
            throw new BusinessException("Đã đạt giới hạn tối đa 5 hóa đơn chờ");
        }

        HoaDon savedHoaDon = invoiceUseCase.taoHoaDon(
                request.khachHangId(),
                request.tenKhachHang(),
                request.soDienThoai(),
                request.maPhieuGiamGia(),
                request.thongTinGiaoHang(),
                request.items(),
                TRANG_THAI_HOA_DON_CHO_XAC_NHAN,
                GHI_CHU_TAO_HOA_DON_TAI_QUAY
        );
        invoiceUseCase.luuLichSuHoaDon(savedHoaDon, TRANG_THAI_HOA_DON_CHO_XAC_NHAN, savedHoaDon.getGhiChu());
        List<HoaDonChiTiet> savedItems = hoaDonChiTietRepository.findByHoaDonIdWithProduct(savedHoaDon.getId());

        return invoiceUseCase.mapHoaDonChiTiet(savedHoaDon, savedItems, vanChuyenRepository.findByHoaDonId(savedHoaDon.getId()).orElse(null));
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

        List<HoaDonChiTiet> oldItems = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDonId);
        for (HoaDonChiTiet item : oldItems) {
            GiayChiTiet giayChiTiet = item.getGiayChiTiet();
            giayChiTiet.setSoLuong((giayChiTiet.getSoLuong() == null ? 0 : giayChiTiet.getSoLuong()) + item.getSoLuong());
            giayChiTiet.setNgayCapNhat(Instant.now());
            giayChiTietRepository.save(giayChiTiet);
            hoaDonChiTietRepository.delete(item);
        }
        hoaDonChiTietRepository.flush();

        validationUseCase.validateDuplicateItems(request.items() != null ? request.items() : new ArrayList<>());
        List<HoaDonChiTiet> chiTietTam = request.items() != null && !request.items().isEmpty() ? request.items().stream()
                .map(item -> invoiceUseCase.taoDongHoaDon(item))
                .toList() : new ArrayList<>();

        BigDecimal tongTienHang = chiTietTam.stream()
                .map(HoaDonChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        KhachHang khachHang = request.khachHangId() != null ? invoiceUseCase.timKhachHang(request.khachHangId()) : null;
        String tenKhachHang = invoiceUseCase.layTenKhachHang(khachHang, request.tenKhachHang());
        String soDienThoai = invoiceUseCase.laySoDienThoai(khachHang, request.soDienThoai());

        if (hoaDon.getPhieuGiamGia() != null) {
            voucherUseCase.giaiPhongPhieuGiamGia(hoaDon.getPhieuGiamGia(), hoaDon.getKhachHang());
        }

        hoaDon.setTongTienHang(tongTienHang);
        hoaDon.setTongTienThanhToan(tongTienHang);

        voucherUseCase.ganPhieuGiamGiaChoHoaDon(hoaDon, request.maPhieuGiamGia(), khachHang, tongTienHang);
        hoaDon.setKhachHang(khachHang);
        invoiceUseCase.apDungThongTinGiaoHangChoHoaDon(hoaDon, request.thongTinGiaoHang(), tenKhachHang, soDienThoai);

        hoaDon.setNgayCapNhat(Instant.now());
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        invoiceUseCase.dongBoVanChuyen(savedHoaDon, request.thongTinGiaoHang());

        List<HoaDonChiTiet> chiTietCanLuu = new ArrayList<>();
        for (HoaDonChiTiet item : chiTietTam) {
            item.setHoaDon(savedHoaDon);
            chiTietCanLuu.add(hoaDonChiTietRepository.save(item));
        }

        return invoiceUseCase.mapHoaDonChiTiet(savedHoaDon, chiTietCanLuu, vanChuyenRepository.findByHoaDonId(savedHoaDon.getId()).orElse(null));
    }

    @Transactional
    public void huyHoaDonCho(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại"));

        if (!invoiceStateUseCase.kenhBanTaiQuay(hoaDon.getKenhBan())) {
            throw new BusinessException("Chỉ hỗ trợ hủy hóa đơn tại quầy");
        }

        if (!invoiceStateUseCase.trangThaiHoaDonCho(hoaDon.getTrangThai())) {
            throw new BusinessException("Chỉ được hủy hóa đơn đang chờ, status=" + hoaDon.getTrangThai());
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
            voucherUseCase.giaiPhongPhieuGiamGia(hoaDon.getPhieuGiamGia(), hoaDon.getKhachHang());
            hoaDon.setPhieuGiamGia(null);
            hoaDon.setTienGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(hoaDon.getTongTienHang());
        }

        hoaDon.setTrangThai(TRANG_THAI_HOA_DON_HUY);
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDon.setGhiChu("Hoa don cho da bi huy");
        hoaDon.setNhanVien(invoiceUseCase.resolveNhanVienDangDangNhap());
        hoaDonRepository.save(hoaDon);
        invoiceUseCase.luuLichSuHoaDon(hoaDon, TRANG_THAI_HOA_DON_HUY, hoaDon.getGhiChu());
    }

    @Transactional
    public ThanhToanTaiQuayResponse thanhToanTaiQuay(ThanhToanTaiQuayRequest request) {
        if (request.hoaDonId() == null && (request.items() == null || request.items().isEmpty())) {
            throw new BusinessException("Hóa đơn phải có ít nhất một sản phẩm để thanh toán");
        }
        paymentUseCase.validateTienKhachDua(request.tienKhachDua());
        Integer trangThaiSauThanhToan = invoiceStateUseCase.xacDinhTrangThaiSauThanhToan(request.thongTinGiaoHang());
        HoaDon hoaDon = request.hoaDonId() == null
                ? invoiceUseCase.taoHoaDon(
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
        invoiceUseCase.luuLichSuHoaDon(hoaDon, trangThaiSauThanhToan, request.ghiChu());

        if (request.thongTinGiaoHang() != null && Boolean.TRUE.equals(request.thongTinGiaoHang().giaoHang())) {
            String emailNhan = hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getEmail() : null;
            if (emailNhan != null && !emailNhan.isBlank()) {
                String hinhThucEmail = paymentUseCase.resolveCongThanhToan(request.hinhThucThanhToan());
                BigDecimal phiShipEmail = vanChuyenRepository.findByHoaDonId(hoaDon.getId())
                        .map(c -> c.getPhiVanChuyen()).orElse(BigDecimal.ZERO);
                guiEmailXacNhanDon(hoaDon, emailNhan, invoiceUseCase.resolveTenKhachHangHoaDon(hoaDon), 
                    hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId()), hinhThucEmail, phiShipEmail);
            }
        }

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
                invoiceUseCase.resolveTenKhachHangHoaDon(hoaDon),
                invoiceUseCase.resolveSoDienThoaiKhachHangHoaDon(hoaDon),
                invoiceUseCase.mapHoaDonChiTiet(hoaDon, new ArrayList<>(), vanChuyenRepository.findByHoaDonId(hoaDon.getId()).orElse(null)).thongTinGiaoHang(), // cheat
                invoiceUseCase.mapHoaDonChiTiet(hoaDon, new ArrayList<>(), null).phieuGiamGia(), // cheat
                hoaDon.getNgayThanhToan()
        );
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

        KhachHang khachHang = invoiceUseCase.timKhachHang(request.khachHangId());
        String tenKhachHang = invoiceUseCase.layTenKhachHang(khachHang, request.tenKhachHang());
        String soDienThoai = invoiceUseCase.laySoDienThoai(khachHang, request.soDienThoai());

        if (hoaDon.getPhieuGiamGia() != null) {
            voucherUseCase.giaiPhongPhieuGiamGia(hoaDon.getPhieuGiamGia(), hoaDon.getKhachHang());
        }
        voucherUseCase.ganPhieuGiamGiaChoHoaDon(hoaDon, request.maPhieuGiamGia(), khachHang, hoaDon.getTongTienHang());
        hoaDon.setKhachHang(khachHang);
        invoiceUseCase.apDungThongTinGiaoHangChoHoaDon(hoaDon, request.thongTinGiaoHang(), tenKhachHang, soDienThoai);
        hoaDon.setGhiChu(request.ghiChu());
        hoaDon.setNhanVien(invoiceUseCase.resolveNhanVienDangDangNhap());

        hoaDon.setNgayCapNhat(Instant.now());
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        invoiceUseCase.dongBoVanChuyen(savedHoaDon, request.thongTinGiaoHang());
        return savedHoaDon;
    }

    @Transactional(readOnly = true)
    public List<HoaDonChoTomTatResponse> layDanhSachHoaDonCho() {
        return invoiceUseCase.layDanhSachHoaDonCho();
    }

    @Transactional(readOnly = true)
    public HoaDonChoChiTietResponse layChiTietHoaDonCho(Integer hoaDonId) {
        return invoiceUseCase.layChiTietHoaDonCho(hoaDonId);
    }

    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ 1 lần để dọn dẹp
    @Transactional
    public void cleanupExpiredPendingInvoices() {
        // Hóa đơn chờ tạo quá 1 ngày (24 giờ) sẽ tự động bị hủy và hoàn lại số lượng
        Instant moc = Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS);
        List<HoaDon> expiredInvoices = hoaDonRepository.findExpiredPendingInvoices(KENH_BAN_TAI_QUAY, TRANG_THAI_HOA_DON_CHO_XAC_NHAN, moc);
        for (HoaDon hd : expiredInvoices) {
            huyHoaDonCho(hd.getId());
        }
    }

    private void guiEmailXacNhanDon(
            HoaDon hoaDon,
            String emailNhan,
            String tenNhan,
            List<HoaDonChiTiet> dong,
            String hinhThuc,
            BigDecimal phiShip
    ) {
        if (emailNhan == null || emailNhan.isBlank()) {
            return;
        }
        List<EmailService.DongDonHangEmail> items = new ArrayList<>();
        for (HoaDonChiTiet ct : dong) {
            GiayChiTiet gct = ct.getGiayChiTiet();
            String bienThe = gct.getMauSac().getTen() + " / Size " + gct.getKichCo().getGiaTri();
            items.add(new EmailService.DongDonHangEmail(
                    gct.getGiay().getTen(),
                    bienThe,
                    gct.getGiay().getHinhAnh(),
                    ct.getSoLuong() == null ? 0 : ct.getSoLuong(),
                    ct.getGiaDonVi(),
                    ct.getThanhTien()
            ));
        }
        emailService.sendOrderConfirmationEmailAsync(new EmailService.DonHangEmail(
                emailNhan,
                tenNhan,
                emailNhan,
                hoaDon.getMa(),
                hoaDon.getNgayLap(),
                hoaDon.getTenNguoiNhan(),
                hoaDon.getSdtNguoiNhan(),
                hoaDon.getDiaChiGiaoHang(),
                hinhThuc,
                phiShip,
                hoaDon.getTienGiam(),
                hoaDon.getTongTienHang(),
                hoaDon.getTongTienThanhToan(),
                items
        ));
    }
}
