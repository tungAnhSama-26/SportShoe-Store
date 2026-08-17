package com.example.server.core.client.dathang.service;

import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.dathang.dto.DatHangResponse;
import com.example.server.core.client.vanchuyen.dto.TinhPhiShipRequest;
import com.example.server.core.client.vanchuyen.service.ClientPhiVanChuyenService;
import com.example.server.core.client.voucher.service.ClientVoucherService;
import com.example.server.core.realtime.hoadon.HoaDonRealtimePublisher;
import com.example.server.core.realtime.sanpham.SanPhamRealtimePublisher;

import com.example.server.core.admin.thongbao.service.ThongBaoService;
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
import com.example.server.infrastructure.address.DiaChiHaiCapMapper;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.service.EmailService;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.service.EmailService;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private final ThongBaoService thongBaoService;
    private final SanPhamRealtimePublisher sanPhamRealtimePublisher;

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
            EmailService emailService,
            ThongBaoService thongBaoService,
            SanPhamRealtimePublisher sanPhamRealtimePublisher
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
        this.thongBaoService = thongBaoService;
        this.sanPhamRealtimePublisher = sanPhamRealtimePublisher;
    }

    @Transactional
    public DatHangResponse datHang(DatHangRequest request) {
        return datHang(request, null);
    }

    @Transactional
    public DatHangResponse datHang(DatHangRequest request, String maGiaoDich) {
        return datHang(request, maGiaoDich, null);
    }

    public record KhoaThanhToan(Map<Integer, BigDecimal> giaSanPham, BigDecimal tienGiamVoucher) {}

    @Transactional
    public DatHangResponse datHang(DatHangRequest request, String maGiaoDich, KhoaThanhToan khoa) {
        return datHang(request, maGiaoDich, khoa, false);
    }

    /**
     * @param khoa     snapshot khóa lúc tạo mã QR; null nếu COD/đặt thường.
     * @param daGiuCho true khi tồn đã được giữ chỗ (trừ kho) lúc tạo mã QR -> không kiểm/trừ lại,
     *                 và đánh dấu đơn ĐÃ TRỪ KHO (nhân viên xác nhận sẽ không trừ trùng).
     */
    @Transactional
    public DatHangResponse datHang(
            DatHangRequest request, String maGiaoDich, KhoaThanhToan khoa, boolean daGiuCho) {
        return taoHoaDon(request, maGiaoDich, null, khoa, daGiuCho, TRANG_THAI_CHO_XAC_NHAN, true);
    }

    /** Tạo hóa đơn online chờ thanh toán làm bản ghi giữ hàng bền vững cho phiên QR. */
    @Transactional
    public DatHangResponse taoHoaDonChoThanhToan(
            DatHangRequest request,
            String maGiaoDich,
            String token,
            KhoaThanhToan khoa
    ) {
        return taoHoaDon(request, maGiaoDich, token, khoa, false, 11, false);
    }

    private DatHangResponse taoHoaDon(
            DatHangRequest request,
            String maGiaoDich,
            String tokenThanhToan,
            KhoaThanhToan khoa,
            boolean daGiuCho,
            int trangThaiHoaDon,
            boolean kichHoatDon
    ) {
        Map<Integer, BigDecimal> giaKhoa = khoa == null ? null : khoa.giaSanPham();
        ClientCheckoutItemService.KetQua checkout =
                checkoutItemService.chuanBi(request.sanPhams(), giaKhoa, daGiuCho);
        List<HoaDonChiTiet> dong = checkout.chiTiets();
        // Khách vãng lai (chưa đăng nhập) -> khachHangId null -> hóa đơn không gắn khách (khách lẻ).
        KhachHang khachHang = request.khachHangId() == null
                ? null
                : khachHangRepository.findById(request.khachHangId())
                        .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));
        Instant now = Instant.now();

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMa(taoMaHoaDon());
        hoaDon.setKenhBan(KENH_BAN_ONLINE);
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTrangThai(trangThaiHoaDon);
        hoaDon.setTongTienHang(checkout.tongTienHang());
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(checkout.tongTienHang());
        hoaDon.setNgayLap(now);
        hoaDon.setNgayTao(now);
        hoaDon.setNgayCapNhat(now);
        // Đơn QR đã giữ chỗ tồn -> đánh dấu đã trừ kho để nhân viên xác nhận không trừ trùng.
        hoaDon.setDaTruKho(daGiuCho);

        hoaDon.setTenNguoiNhan(request.tenNguoiNhan().trim());
        hoaDon.setSdtNguoiNhan(request.sdtNguoiNhan().trim());
        hoaDon.setDiaChiGiaoHang(DiaChiHaiCapMapper.toEntity(request.diaChiGiaoHang()));
        String rawGhiChu = request.ghiChu() != null ? request.ghiChu().trim() : "";
        if (khachHang == null && request.emailNguoiNhan() != null && !request.emailNguoiNhan().isBlank()) {
            rawGhiChu = "[GuestEmail:" + request.emailNguoiNhan().trim() + "] " + rawGhiChu;
        }
        hoaDon.setGhiChu(rawGhiChu.trim().isEmpty() ? null : rawGhiChu.trim());

        BigDecimal tongTienHang = checkout.tongTienHang();
        BigDecimal tienGiam = BigDecimal.ZERO;
        // Khách có tài khoản: dùng cả voucher cá nhân + toàn sàn. Khách vãng lai (khachHang null):
        // chỉ voucher toàn sàn (voucher cá nhân sẽ bị từ chối "chỉ dành cho thành viên" khi validate).
        if (request.maPhieuGiamGia() != null && !request.maPhieuGiamGia().isBlank()) {
            if (khoa != null) {
                // Luồng QR: voucher đã khóa lúc tạo mã. Chỉ áp khi lúc đó còn hợp lệ (tiền giảm > 0);
                // áp theo tiền giảm đã khóa, KHÔNG kiểm tra lại hiệu lực (dù sau đó bị hủy vẫn giữ).
                BigDecimal giamKhoa = khoa.tienGiamVoucher() == null
                        ? BigDecimal.ZERO : khoa.tienGiamVoucher();
                if (giamKhoa.signum() > 0) {
                    tienGiam = voucherService.apDungVoucherDaKhoa(
                            hoaDon, request.maPhieuGiamGia(), khachHang, giamKhoa);
                    hoaDon.setTienGiam(tienGiam);
                }
                // giamKhoa == 0 -> mã không hợp lệ lúc tạo QR -> không áp (đúng quy tắc "mất").
            } else {
                tienGiam = voucherService.apDungVaoHoaDon(
                        hoaDon,
                        request.maPhieuGiamGia(),
                        hoaDon.getKhachHang(),
                        tongTienHang
                );
                hoaDon.setTienGiam(tienGiam);
            }
        }

        // Tính lại phí vận chuyển từ địa chỉ và danh sách sản phẩm ngay trước khi lưu đơn.
        BigDecimal phiShip = tinhPhiShip(request);
        hoaDon.setTongTienThanhToan(
                tongTienHang.subtract(tienGiam).max(BigDecimal.ZERO).add(phiShip));

        String hinhThuc = chuanHoaHinhThucThanhToan(request.hinhThucThanhToan());
        if (kichHoatDon && "VNPAY".equals(hinhThuc)) {
            hoaDon.setNgayThanhToan(now);
        }
        hoaDonRepository.save(hoaDon);
        for (HoaDonChiTiet chiTiet : dong) {
            chiTiet.setHoaDon(hoaDon);
        }
        hoaDonChiTietRepository.saveAll(dong);
        luuVanChuyen(hoaDon, phiShip, now);
        taoGiaoDichThanhToan(
                hoaDon, hinhThuc, maGiaoDich, tokenThanhToan, kichHoatDon, now);
        sanPhamRealtimePublisher.phatSauCommit(kichHoatDon ? "DON_ONLINE_GIU_HANG" : "QR_GIU_HANG");
        if (kichHoatDon) {
            hoaDonRealtimePublisher.publishAfterCommit(hoaDon, "TAO_MOI");
        }

        // Trigger notification for new online order:
        if (kichHoatDon) {
            thongBaoService.taoThongBao(
                    "Đơn hàng online mới",
                    "Có đơn hàng mới #" + hoaDon.getMa() + " từ khách hàng " + (khachHang != null ? khachHang.getHoTen() : hoaDon.getTenNguoiNhan()) + ". Tổng tiền: " + String.format("%,.0f", hoaDon.getTongTienThanhToan()) + "đ",
                    "ORDER",
                    "/admin/hoa-don/" + hoaDon.getId()
            );
        }

        // Email xác nhận: khách có tài khoản -> email tài khoản; khách vãng lai -> email tự nhập (nếu có).
        String emailNhan = khachHang != null && khachHang.getEmail() != null && !khachHang.getEmail().isBlank()
                ? khachHang.getEmail()
                : request.emailNguoiNhan();
        String tenNhan = khachHang != null ? khachHang.getHoTen() : hoaDon.getTenNguoiNhan();
        if (kichHoatDon) {
            guiEmailXacNhanDon(hoaDon, emailNhan, tenNhan, dong, hinhThuc, phiShip);
        }

        return new DatHangResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                hoaDon.getTongTienThanhToan(),
                hoaDon.getTrangThai(),
                hinhThuc
        );
    }

    /**
     * Chuyển hóa đơn QR đã giữ hàng sang Chờ xác nhận. Không trừ tồn kho;
     * nhân viên xác nhận đơn mới thực hiện trừ kho như luồng hiện tại.
     */
    @Transactional
    public String xacNhanHoaDonChoThanhToan(String token) {
        ThanhToan thamChieu = thanhToanRepository
                .findByNoiDungCkAndLoaiGiaoDich(token, LOAI_GIAO_DICH_THANH_TOAN)
                .orElseThrow(() -> new BusinessException("Mã thanh toán không tồn tại hoặc đã hết hạn"));
        HoaDon hoaDon = hoaDonRepository.findDetailByIdForUpdate(thamChieu.getHoaDon().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn giữ hàng không tồn tại"));
        ThanhToan thanhToan = thanhToanRepository.findByTokenForUpdate(token)
                .orElseThrow(() -> new BusinessException("Mã thanh toán không tồn tại hoặc đã hết hạn"));

        if (thanhToan.getTrangThai() != null && thanhToan.getTrangThai() == TRANG_THAI_DA_THANH_TOAN) {
            return hoaDon.getMa();
        }
        if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != 11
                || thanhToan.getTrangThai() == null || thanhToan.getTrangThai() != TRANG_THAI_CHO_THANH_TOAN) {
            throw new BusinessException("Phiên thanh toán không còn hiệu lực");
        }
        Instant now = Instant.now();
        if (hoaDon.getNgayTao().plus(java.time.Duration.ofMinutes(15)).isBefore(now)) {
            throw new BusinessException("Phiên thanh toán đã hết hạn");
        }

        hoaDon.setTrangThai(TRANG_THAI_CHO_XAC_NHAN);
        hoaDon.setNgayThanhToan(now);
        hoaDon.setNgayCapNhat(now);
        thanhToan.setTrangThai(TRANG_THAI_DA_THANH_TOAN);
        thanhToan.setNgayThanhToan(now);
        thanhToan.setGhiChu("Khách hàng đã thanh toán trực tuyến");
        thanhToanRepository.save(thanhToan);
        hoaDonRepository.save(hoaDon);

        hoaDonRealtimePublisher.publishAfterCommit(hoaDon, "TAO_MOI");
        sanPhamRealtimePublisher.phatSauCommit("QR_DA_THANH_TOAN");
        thongBaoService.taoThongBao(
                "Đơn hàng online mới",
                "Có đơn hàng mới #" + hoaDon.getMa() + " từ khách hàng "
                        + (hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getHoTen() : hoaDon.getTenNguoiNhan())
                        + ". Tổng tiền: " + String.format("%,.0f", hoaDon.getTongTienThanhToan()) + "đ",
                "ORDER",
                "/admin/hoa-don/" + hoaDon.getId()
        );

        List<HoaDonChiTiet> dong = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());
        String emailNhan = hoaDon.getKhachHang() != null
                ? hoaDon.getKhachHang().getEmail() : layGuestEmail(hoaDon.getGhiChu());
        String tenNhan = hoaDon.getKhachHang() != null
                ? hoaDon.getKhachHang().getHoTen() : hoaDon.getTenNguoiNhan();
        BigDecimal phiShip = vanChuyenRepository.findByHoaDonId(hoaDon.getId())
                .map(VanChuyen::getPhiVanChuyen).orElse(BigDecimal.ZERO);
        guiEmailXacNhanDon(hoaDon, emailNhan, tenNhan, dong, "VNPAY", phiShip);
        return hoaDon.getMa();
    }

    private String layGuestEmail(String ghiChu) {
        if (ghiChu == null || !ghiChu.startsWith("[GuestEmail:")) {
            return null;
        }
        int ketThuc = ghiChu.indexOf(']');
        return ketThuc > 12 ? ghiChu.substring(12, ketThuc).trim() : null;
    }

    /** Gửi email xác nhận đơn hàng cho khách (chạy ở luồng nền, lỗi không chặn đặt hàng). */
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
                DiaChiHaiCapMapper.format(hoaDon.getDiaChiGiaoHang()),
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
                request.diaChiGiaoHang()
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
        if ("VIETQR".equals(normalized)) {
            return "VNPAY";
        }
        if (!"COD".equals(normalized) && !"VNPAY".equals(normalized)) {
            throw new BusinessException("Hình thức thanh toán không hợp lệ");
        }
        return normalized;
    }

    private void taoGiaoDichThanhToan(
            HoaDon hoaDon,
            String hinhThuc,
            String maGiaoDich,
            String tokenThanhToan,
            boolean daThanhToan,
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
        thanhToan.setNoiDungCk(tokenThanhToan != null && !tokenThanhToan.isBlank()
                ? tokenThanhToan.trim()
                : (laVnPay ? "Thanh toán VNPay " : "Thanh toán COD ") + hoaDon.getMa());
        thanhToan.setTrangThai(laVnPay && daThanhToan
                ? TRANG_THAI_DA_THANH_TOAN : TRANG_THAI_CHO_THANH_TOAN);
        thanhToan.setLoaiGiaoDich(LOAI_GIAO_DICH_THANH_TOAN);
        thanhToan.setNgayThanhToan(laVnPay && daThanhToan ? now : null);
        thanhToan.setNgayTao(now);
        thanhToan.setGhiChu(laVnPay && daThanhToan
                ? "Khách hàng đã thanh toán trực tuyến qua VNPay"
                : (laVnPay ? "Chờ thanh toán trực tuyến" : "Chờ thanh toán khi nhận hàng"));
        thanhToanRepository.save(thanhToan);
    }
}
