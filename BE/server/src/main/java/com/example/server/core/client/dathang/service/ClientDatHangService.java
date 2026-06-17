package com.example.server.core.client.dathang.service;

import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.dathang.dto.DatHangResponse;
import com.example.server.core.client.vanchuyen.dto.TinhPhiShipRequest;
import com.example.server.core.client.vanchuyen.service.ClientPhiVanChuyenService;
import com.example.server.core.client.voucher.service.ClientVoucherService;
import com.example.server.core.realtime.hoadon.HoaDonRealtimePublisher;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.ThanhToan;
import com.example.server.entity.VanChuyen;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.ThanhToanRepository;
import com.example.server.repository.VanChuyenRepository;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.service.EmailService;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tạo hóa đơn mới từ danh sách sản phẩm khách đã lưu cục bộ.
 * Bước này chỉ kiểm tra tồn; tồn kho được trừ khi nhân viên xác nhận đơn.
 */
@Service
public class ClientDatHangService {

    private static final int KENH_BAN_ONLINE = 2;
    private static final int TRANG_THAI_CHO_XAC_NHAN = 1;
    private static final int HINH_THUC_VNPAY = 3;
    private static final int HINH_THUC_COD = 4;
    private static final int TRANG_THAI_CHO_THANH_TOAN = 0;
    private static final int TRANG_THAI_DA_THANH_TOAN = 1;
    private static final int LOAI_GIAO_DICH_THANH_TOAN = 1;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ClientCheckoutItemService checkoutItemService;
    private final ClientVoucherService voucherService;
    private final ClientPhiVanChuyenService phiVanChuyenService;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final VanChuyenRepository vanChuyenRepository;
    private final HoaDonRealtimePublisher hoaDonRealtimePublisher;
    private final EmailService emailService;

    public ClientDatHangService(
            ClientCheckoutItemService checkoutItemService,
            ClientVoucherService voucherService,
            ClientPhiVanChuyenService phiVanChuyenService,
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            KhachHangRepository khachHangRepository,
            ThanhToanRepository thanhToanRepository,
            VanChuyenRepository vanChuyenRepository,
            HoaDonRealtimePublisher hoaDonRealtimePublisher,
            EmailService emailService
    ) {
        this.emailService = emailService;
        this.checkoutItemService = checkoutItemService;
        this.voucherService = voucherService;
        this.phiVanChuyenService = phiVanChuyenService;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.khachHangRepository = khachHangRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.vanChuyenRepository = vanChuyenRepository;
        this.hoaDonRealtimePublisher = hoaDonRealtimePublisher;
    }

    @Transactional
    public DatHangResponse datHang(DatHangRequest request) {
        return datHang(request, null);
    }

    @Transactional
    public DatHangResponse datHang(DatHangRequest request, String maGiaoDich) {
        ClientCheckoutItemService.KetQua checkout = checkoutItemService.chuanBi(request.sanPhams());
        List<HoaDonChiTiet> dong = checkout.chiTiets();
        KhachHang khachHang = khachHangRepository.findById(request.khachHangId())
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));
        Instant now = Instant.now();

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMa(taoMaHoaDon());
        hoaDon.setKenhBan(KENH_BAN_ONLINE);
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTrangThai(TRANG_THAI_CHO_XAC_NHAN);
        hoaDon.setTongTienHang(checkout.tongTienHang());
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(checkout.tongTienHang());
        hoaDon.setNgayLap(now);
        hoaDon.setNgayTao(now);
        hoaDon.setNgayCapNhat(now);
        hoaDon.setDaNhanHang(false);
        hoaDon.setDaTruKho(false);

        String diaChi = Stream.of(
                        request.diaChiCuThe(),
                        request.phuongXa(),
                        request.quanHuyen(),
                        request.tinhThanh()
                )
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        hoaDon.setTenNguoiNhan(request.tenNguoiNhan().trim());
        hoaDon.setSdtNguoiNhan(request.sdtNguoiNhan().trim());
        hoaDon.setDiaChiGiaoHang(diaChi);
        hoaDon.setGhiChu(request.ghiChu());

        BigDecimal tongTienHang = checkout.tongTienHang();
        BigDecimal tienGiam = BigDecimal.ZERO;
        if (request.maPhieuGiamGia() != null && !request.maPhieuGiamGia().isBlank()) {
            tienGiam = voucherService.apDungVaoHoaDon(
                    hoaDon,
                    request.maPhieuGiamGia(),
                    hoaDon.getKhachHang(),
                    tongTienHang
            );
            hoaDon.setTienGiam(tienGiam);
        }

        // Tính lại phí vận chuyển từ địa chỉ và danh sách sản phẩm ngay trước khi lưu đơn.
        BigDecimal phiShip = tinhPhiShip(request);
        hoaDon.setTongTienThanhToan(
                tongTienHang.subtract(tienGiam).max(BigDecimal.ZERO).add(phiShip));

        String hinhThuc = chuanHoaHinhThucThanhToan(request.hinhThucThanhToan());
        if ("VNPAY".equals(hinhThuc)) {
            hoaDon.setNgayThanhToan(now);
        }
        hoaDonRepository.save(hoaDon);
        for (HoaDonChiTiet chiTiet : dong) {
            chiTiet.setHoaDon(hoaDon);
        }
        hoaDonChiTietRepository.saveAll(dong);
        luuVanChuyen(hoaDon, phiShip, now);
        taoGiaoDichThanhToan(hoaDon, hinhThuc, maGiaoDich, now);
        hoaDonRealtimePublisher.publishAfterCommit(hoaDon, "TAO_MOI");
        guiEmailXacNhanDon(hoaDon, khachHang, dong, hinhThuc, phiShip);

        return new DatHangResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                hoaDon.getTongTienThanhToan(),
                hoaDon.getTrangThai(),
                hinhThuc
        );
    }

    /** Gửi email xác nhận đơn hàng cho khách (chạy ở luồng nền, lỗi không chặn đặt hàng). */
    private void guiEmailXacNhanDon(
            HoaDon hoaDon,
            KhachHang khachHang,
            List<HoaDonChiTiet> dong,
            String hinhThuc,
            BigDecimal phiShip
    ) {
        if (khachHang == null || khachHang.getEmail() == null || khachHang.getEmail().isBlank()) {
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
                khachHang.getEmail(),
                khachHang.getHoTen(),
                khachHang.getEmail(),
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

    /** Phí ship theo địa chỉ nhận (GHN, có phí ước tính dự phòng khi GHN không khả dụng). */
    private BigDecimal tinhPhiShip(DatHangRequest request) {
        BigDecimal phi = phiVanChuyenService.tinhPhi(new TinhPhiShipRequest(
                request.khachHangId(),
                request.sanPhams(),
                request.tinhThanh(),
                request.quanHuyen(),
                request.phuongXa(),
                request.diaChiCuThe(),
                request.toDistrictId(),
                request.toWardCode()
        )).phiVanChuyen();
        return phi == null ? BigDecimal.ZERO : phi.max(BigDecimal.ZERO);
    }

    private String taoMaHoaDon() {
        return "HD" + Long.toString(System.currentTimeMillis(), 36).toUpperCase(Locale.ROOT)
                + String.format("%03d", SECURE_RANDOM.nextInt(1000));
    }

    /** Lưu bản ghi vận chuyển (đơn vị + phí ship) gắn với hóa đơn. */
    private void luuVanChuyen(HoaDon hoaDon, BigDecimal phiShip, Instant now) {
        VanChuyen vanChuyen = vanChuyenRepository.findByHoaDonId(hoaDon.getId()).orElseGet(() -> {
            VanChuyen moi = new VanChuyen();
            moi.setHoaDon(hoaDon);
            moi.setTrangThai(1);
            moi.setNgayTao(now);
            return moi;
        });
        vanChuyen.setDonViVanChuyen("GHN");
        vanChuyen.setPhiVanChuyen(phiShip);
        vanChuyen.setNgayCapNhat(now);
        vanChuyenRepository.save(vanChuyen);
    }

    private String chuanHoaHinhThucThanhToan(String hinhThuc) {
        String normalized = hinhThuc == null || hinhThuc.isBlank()
                ? "COD"
                : hinhThuc.trim().toUpperCase(Locale.ROOT);
        if (!"COD".equals(normalized) && !"VNPAY".equals(normalized)) {
            throw new BusinessException("Hình thức thanh toán không hợp lệ");
        }
        return normalized;
    }

    private void taoGiaoDichThanhToan(
            HoaDon hoaDon,
            String hinhThuc,
            String maGiaoDich,
            Instant now
    ) {
        BigDecimal soTien = hoaDon.getTongTienThanhToan() == null
                ? BigDecimal.ZERO
                : hoaDon.getTongTienThanhToan();
        if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        boolean laVnPay = "VNPAY".equals(hinhThuc);
        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setHoaDon(hoaDon);
        thanhToan.setMaGiaoDich(maGiaoDich != null && !maGiaoDich.isBlank()
                ? maGiaoDich.trim()
                : (laVnPay ? "VNPAY-" : "COD-") + hoaDon.getMa());
        thanhToan.setHinhThuc(laVnPay ? HINH_THUC_VNPAY : HINH_THUC_COD);
        thanhToan.setSoTien(soTien);
        thanhToan.setCongThanhToan(laVnPay ? "VNPay" : "COD");
        thanhToan.setNoiDungCk((laVnPay ? "Thanh toán VNPay " : "Thanh toán COD ") + hoaDon.getMa());
        thanhToan.setTrangThai(laVnPay ? TRANG_THAI_DA_THANH_TOAN : TRANG_THAI_CHO_THANH_TOAN);
        thanhToan.setLoaiGiaoDich(LOAI_GIAO_DICH_THANH_TOAN);
        thanhToan.setNgayThanhToan(laVnPay ? now : null);
        thanhToan.setNgayTao(now);
        thanhToan.setGhiChu(laVnPay
                ? "Khách hàng đã thanh toán trực tuyến qua VNPay"
                : "Chờ thanh toán khi nhận hàng");
        thanhToanRepository.save(thanhToan);
    }
}
