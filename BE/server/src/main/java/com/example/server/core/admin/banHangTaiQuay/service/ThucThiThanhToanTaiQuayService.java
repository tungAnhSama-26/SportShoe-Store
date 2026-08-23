package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.infrastructure.address.DiaChiHaiCapMapper;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ThanhToanTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThanhToanTaiQuayResponse;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.ThanhToan;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.ThanhToanRepository;
import com.example.server.repository.VanChuyenRepository;
import com.example.server.repository.GiaoCaRepository;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.entity.GiaoCa;
import com.example.server.entity.NhanVien;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ThucThiThanhToanTaiQuayService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final VanChuyenRepository vanChuyenRepository;
    private final EmailService emailService;

    private final ThanhToanTaiQuayService paymentUseCase;
    private final TrangThaiHoaDonTaiQuayService invoiceStateUseCase;
    private final HoaDonTaiQuayService invoiceUseCase;
    private final PhieuGiamGiaTaiQuayService voucherUseCase;
    private final GiaoCaRepository giaoCaRepository;
    private final TonKhoTaiQuayService inventoryUseCase;
    private final GiayChiTietRepository giayChiTietRepository;
    private final SanPhamTaiQuayService productUseCase;
    private final com.example.server.repository.HinhAnhGiayRepository hinhAnhGiayRepository;

    public ThucThiThanhToanTaiQuayService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanRepository thanhToanRepository,
            VanChuyenRepository vanChuyenRepository,
            EmailService emailService,
            ThanhToanTaiQuayService paymentUseCase,
            TrangThaiHoaDonTaiQuayService invoiceStateUseCase,
            HoaDonTaiQuayService invoiceUseCase,
            PhieuGiamGiaTaiQuayService voucherUseCase,
            GiaoCaRepository giaoCaRepository,
            TonKhoTaiQuayService inventoryUseCase,
            GiayChiTietRepository giayChiTietRepository,
            SanPhamTaiQuayService productUseCase,
            com.example.server.repository.HinhAnhGiayRepository hinhAnhGiayRepository
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.vanChuyenRepository = vanChuyenRepository;
        this.emailService = emailService;
        this.paymentUseCase = paymentUseCase;
        this.invoiceStateUseCase = invoiceStateUseCase;
        this.invoiceUseCase = invoiceUseCase;
        this.voucherUseCase = voucherUseCase;
        this.giaoCaRepository = giaoCaRepository;
        this.inventoryUseCase = inventoryUseCase;
        this.giayChiTietRepository = giayChiTietRepository;
        this.productUseCase = productUseCase;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
    }

    @Transactional
    public ThanhToanTaiQuayResponse thanhToanTaiQuay(ThanhToanTaiQuayRequest request) {
        if (request.hoaDonId() == null && (request.items() == null || request.items().isEmpty())) {
            throw new BusinessException("Hóa đơn phải có ít nhất một sản phẩm để thanh toán");
        }

        NhanVien currentEmp = invoiceUseCase.resolveNhanVienDangDangNhap();
        if (currentEmp == null) {
            throw new BusinessException("Nhân viên chưa đăng nhập hoặc phiên đăng nhập hết hạn.");
        }
        GiaoCa activeShift = resolveCaThanhToan(currentEmp);
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

        // Stock has already been deducted when items were added to HoaDonCho or during taoHoaDon

        BigDecimal tongTien = hoaDon.getTongTienThanhToan();
        BigDecimal tienKhachDua = paymentUseCase.xacDinhTienKhachDua(request.hinhThucThanhToan(), request.tienKhachDua(), tongTien, request.tienMat(), request.tienChuyenKhoan());
        BigDecimal tienThua = paymentUseCase.tinhTienThua(request.hinhThucThanhToan(), tienKhachDua, tongTien, request.tienMat(), request.tienChuyenKhoan());

        if (request.hinhThucThanhToan() != null && request.hinhThucThanhToan() == 5) {
            BigDecimal tMat = request.tienMat() != null ? request.tienMat() : BigDecimal.ZERO;
            BigDecimal tCk = request.tienChuyenKhoan() != null ? request.tienChuyenKhoan() : BigDecimal.ZERO;

            if (tMat.compareTo(BigDecimal.ZERO) > 0) {
                ThanhToan ttMat = new ThanhToan();
                ttMat.setHoaDon(hoaDon);
                ttMat.setNhanVien(hoaDon.getNhanVien());
                ttMat.setHinhThuc(1);
                ttMat.setSoTien(tMat);
                ttMat.setTienThoiLai(tienThua);
                ttMat.setCongThanhToan("Tien mat");
                ttMat.setNgayThanhToan(Instant.now());
                ttMat.setTrangThai(1);
                ttMat.setLoaiGiaoDich(1);
                ttMat.setGhiChu(request.ghiChu());
                ttMat.setNgayTao(Instant.now());
                thanhToanRepository.save(ttMat);
            }

            if (tCk.compareTo(BigDecimal.ZERO) > 0) {
                ThanhToan ttCk = new ThanhToan();
                ttCk.setHoaDon(hoaDon);
                ttCk.setNhanVien(hoaDon.getNhanVien());
                ttCk.setHinhThuc(2);
                ttCk.setSoTien(tCk);
                ttCk.setTienThoiLai(BigDecimal.ZERO);
                ttCk.setCongThanhToan("Chuyen khoan");
                ttCk.setNgayThanhToan(Instant.now());
                ttCk.setTrangThai(1);
                ttCk.setLoaiGiaoDich(1);
                ttCk.setGhiChu(request.ghiChu());
                ttCk.setNgayTao(Instant.now());
                thanhToanRepository.save(ttCk);
            }
        } else {
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
        }

        hoaDon.setTrangThai(trangThaiSauThanhToan);
        hoaDon.setNgayThanhToan(Instant.now());
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDon.setGiaoCa(activeShift);
        hoaDonRepository.save(hoaDon);
        invoiceUseCase.luuLichSuHoaDon(hoaDon, trangThaiSauThanhToan, request.ghiChu());

        String emailNhan = null;
        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getEmail() != null && !hoaDon.getKhachHang().getEmail().isBlank()) {
            emailNhan = hoaDon.getKhachHang().getEmail().trim();
        } else if (request.thongTinGiaoHang() != null && request.thongTinGiaoHang().email() != null && !request.thongTinGiaoHang().email().isBlank()) {
            emailNhan = request.thongTinGiaoHang().email().trim();
        } else if (request.khachHangId() != null) {
            KhachHang kh = invoiceUseCase.timKhachHang(request.khachHangId());
            if (kh != null && kh.getEmail() != null && !kh.getEmail().isBlank()) {
                emailNhan = kh.getEmail().trim();
            }
        }

        if (emailNhan != null && !emailNhan.isBlank()) {
            String hinhThucEmail = paymentUseCase.resolveCongThanhToan(request.hinhThucThanhToan());
            BigDecimal phiShipEmail = vanChuyenRepository.findByHoaDonId(hoaDon.getId())
                    .map(com.example.server.entity.VanChuyen::getPhiVanChuyen).orElse(BigDecimal.ZERO);
            guiEmailXacNhanDon(hoaDon, emailNhan, invoiceUseCase.resolveTenKhachHangHoaDon(hoaDon),
                    hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId()), hinhThucEmail, phiShipEmail);
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

    private GiaoCa resolveCaThanhToan(NhanVien currentEmp) {
        Optional<GiaoCa> caCuaNhanVien = giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(currentEmp.getId(), "MO_CA");
        if (currentEmp.getVaiTro() != null && currentEmp.getVaiTro() == 1) {
            return caCuaNhanVien
                    .or(() -> giaoCaRepository.findFirstByTrangThaiInOrderByThoiGianVaoDesc(List.of("MO_CA")))
                    .orElse(null);
        }
        return caCuaNhanVien.orElseThrow(() -> new BusinessException(
                "Nhân viên không có ca làm việc nào đang hoạt động. Vui lòng mở ca để thực hiện thanh toán."));
    }

    private HoaDon thanhToanHoaDonCho(ThanhToanTaiQuayRequest request) {
        HoaDon hoaDon = hoaDonRepository.findDetailByIdForUpdate(request.hoaDonId())
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

        for (HoaDonChiTiet item : items) {
            GiayChiTiet gct = giayChiTietRepository.findByIdForUpdate(item.getGiayChiTiet().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm chi tiết không tồn tại"));
            item.setGiayChiTiet(gct);
            if (gct == null || gct.getKichHoat() == null || gct.getKichHoat() != 1 ||
                gct.getGiay() == null || gct.getGiay().getTrangThai() == null || gct.getGiay().getTrangThai() != 1) {
                String tenGiay = gct != null && gct.getGiay() != null ? gct.getGiay().getTen() : "";
                throw new BusinessException("Sản phẩm " + tenGiay + " đã ngừng hoạt động, vui lòng chọn sản phẩm khác");
            }
        }

        BigDecimal tongTienHang = items.stream()
                .map(item -> {
                    item.setThanhTien(item.getGiaDonVi().multiply(BigDecimal.valueOf(item.getSoLuong().longValue())));
                    hoaDonChiTietRepository.save(item);
                    return item.getThanhTien();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        hoaDon.setTongTienHang(tongTienHang);
        hoaDon.setTongTienThanhToan(tongTienHang);

        KhachHang khachHang = invoiceUseCase.timKhachHang(request.khachHangId());
        hoaDon.setKhachHang(khachHang);
        String tenKhachHang = invoiceUseCase.layTenKhachHang(khachHang, request.tenKhachHang());
        String soDienThoai = invoiceUseCase.laySoDienThoai(khachHang, request.soDienThoai());

        if (hoaDon.getPhieuGiamGia() != null) {
            voucherUseCase.giaiPhongPhieuGiamGia(hoaDon.getPhieuGiamGia(), hoaDon.getKhachHang());
        }
        voucherUseCase.ganPhieuGiamGiaChoHoaDon(hoaDon, request.maPhieuGiamGia(), khachHang, tongTienHang);
        invoiceUseCase.apDungThongTinGiaoHangChoHoaDon(hoaDon, request.thongTinGiaoHang(), tenKhachHang, soDienThoai, true);
        hoaDon.setGhiChu(request.ghiChu());
        hoaDon.setNhanVien(invoiceUseCase.resolveNhanVienDangDangNhap());

        hoaDon.setNgayCapNhat(Instant.now());
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        invoiceUseCase.dongBoVanChuyen(savedHoaDon, request.thongTinGiaoHang());
        return savedHoaDon;
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
            String hinhAnh = null;
            if (hinhAnhGiayRepository != null) {
                List<com.example.server.entity.HinhAnhGiay> listAnh = hinhAnhGiayRepository.findByGiayChiTietIdAndTrangThaiOrderByLaHinhChinhDescNgayTaoAsc(gct.getId(), 1);
                if (listAnh != null && !listAnh.isEmpty()) {
                    hinhAnh = listAnh.get(0).getUrl();
                }
            }
            if (hinhAnh == null || hinhAnh.isBlank()) {
                hinhAnh = gct.getGiay().getHinhAnh();
            }
            items.add(new EmailService.DongDonHangEmail(
                    gct.getGiay().getTen(),
                    bienThe,
                    hinhAnh,
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
                DiaChiHaiCapMapper.format(hoaDon.getDiaChiGiaoHang()),
                hinhThuc,
                phiShip,
                hoaDon.getTienGiam(),
                hoaDon.getTongTienHang(),
                hoaDon.getTongTienThanhToan(),
                items
        ));
    }

    @Transactional
    public HoaDon xacNhanThanhToanSePay(String noiDung, long soTien) {
        if (noiDung == null || noiDung.isBlank()) {
            return null;
        }

        String rawContent = noiDung.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(java.util.Locale.ROOT);
        List<HoaDon> hoaDonChos = hoaDonRepository.findByKenhBanAndTrangThai(1, com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.TRANG_THAI_HOA_DON_CHO_TAI_QUAY);

        for (HoaDon hoaDon : hoaDonChos) {
            String maHd = hoaDon.getMa() != null ? hoaDon.getMa().replaceAll("[^a-zA-Z0-9]", "").toUpperCase(java.util.Locale.ROOT) : "";
            if (maHd.isEmpty()) {
                continue;
            }

            if (rawContent.contains(maHd)) {
                BigDecimal tongCanThanhToan = hoaDon.getTongTienThanhToan();
                long soTienKyVong = tongCanThanhToan == null ? 0L : tongCanThanhToan.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
                if (soTienKyVong > 0 && soTien < soTienKyVong) {
                    return null;
                }

                List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId());
                if (items == null || items.isEmpty()) {
                    return null;
                }

                GiaoCa activeShift = null;
                if (hoaDon.getNhanVien() != null) {
                    activeShift = giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(hoaDon.getNhanVien().getId(), "MO_CA").orElse(null);
                }
                if (activeShift == null) {
                    activeShift = giaoCaRepository.findFirstByTrangThaiInOrderByThoiGianVaoDesc(List.of("MO_CA")).orElse(null);
                }

                boolean coGiaoHang = vanChuyenRepository.findByHoaDonId(hoaDon.getId()).isPresent();
                int trangThaiSauThanhToan = coGiaoHang
                        ? com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.TRANG_THAI_HOA_DON_DA_XAC_NHAN
                        : com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.TRANG_THAI_HOA_DON_HOAN_THANH;

                ThanhToan thanhToan = new ThanhToan();
                thanhToan.setHoaDon(hoaDon);
                thanhToan.setNhanVien(hoaDon.getNhanVien());
                thanhToan.setHinhThuc(2); // 2: Chuyen khoan
                thanhToan.setSoTien(tongCanThanhToan);
                thanhToan.setTienThoiLai(BigDecimal.ZERO);
                thanhToan.setCongThanhToan("Chuyen khoan (SePay Webhook)");
                thanhToan.setNgayThanhToan(Instant.now());
                thanhToan.setTrangThai(1);
                thanhToan.setLoaiGiaoDich(1); // 1: Thanh toan
                thanhToan.setMaGiaoDich(noiDung);
                thanhToan.setGhiChu("Thanh toan tu dong qua SePay Webhook (" + noiDung + ")");
                thanhToan.setNgayTao(Instant.now());
                thanhToanRepository.save(thanhToan);

                hoaDon.setTrangThai(trangThaiSauThanhToan);
                hoaDon.setNgayThanhToan(Instant.now());
                hoaDon.setNgayCapNhat(Instant.now());
                if (activeShift != null) {
                    hoaDon.setGiaoCa(activeShift);
                }
                hoaDonRepository.save(hoaDon);

                invoiceUseCase.luuLichSuHoaDon(hoaDon, trangThaiSauThanhToan, "Thanh toan thanh cong qua chuyen khoan SePay Webhook (" + noiDung + ")");

                if (coGiaoHang) {
                    String emailNhan = hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getEmail() : null;
                    if (emailNhan != null && !emailNhan.isBlank()) {
                        BigDecimal phiShip = vanChuyenRepository.findByHoaDonId(hoaDon.getId())
                                .map(com.example.server.entity.VanChuyen::getPhiVanChuyen).orElse(BigDecimal.ZERO);
                        guiEmailXacNhanDon(hoaDon, emailNhan, invoiceUseCase.resolveTenKhachHangHoaDon(hoaDon),
                                items, "Chuyen khoan (SePay)", phiShip);
                    }
                }

                return hoaDon;
            }
        }
        return null;
    }
}
