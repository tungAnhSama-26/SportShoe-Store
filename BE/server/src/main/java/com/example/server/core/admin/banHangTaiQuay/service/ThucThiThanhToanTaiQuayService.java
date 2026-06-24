package com.example.server.core.admin.banHangTaiQuay.service;

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
import com.example.server.infrastructure.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    public ThucThiThanhToanTaiQuayService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanRepository thanhToanRepository,
            VanChuyenRepository vanChuyenRepository,
            EmailService emailService,
            ThanhToanTaiQuayService paymentUseCase,
            TrangThaiHoaDonTaiQuayService invoiceStateUseCase,
            HoaDonTaiQuayService invoiceUseCase,
            PhieuGiamGiaTaiQuayService voucherUseCase
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
