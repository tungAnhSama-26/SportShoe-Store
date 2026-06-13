package com.example.server.core.client.giohang.service;

import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayInventoryUseCase;
import com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService;
import com.example.server.core.client.giohang.dto.CapNhatSoLuongRequest;
import com.example.server.core.client.giohang.dto.GioHangItemResponse;
import com.example.server.core.client.giohang.dto.GioHangResponse;
import com.example.server.core.client.giohang.dto.ThemVaoGioRequest;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.KhachHangRepository;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Giỏ hàng phía khách = một HÓA ĐƠN đang mở (kênh online, trạng thái "giỏ hàng").
 * Thêm/sửa/xóa trong giỏ KHÔNG trừ tồn kho; tồn kho chỉ bị trừ khi đặt hàng.
 */
@Service
public class ClientGioHangService {

    /** Kênh bán online. */
    public static final int KENH_BAN_ONLINE = 2;
    /** Trạng thái hóa đơn đang là "giỏ hàng" (chưa đặt). */
    public static final int TRANG_THAI_GIO = 0;
    /** Thời gian giữ hàng tạm khi vào thanh toán (giây). */
    public static final int THOI_GIAN_GIU_GIAY = 90;
    /** Giới hạn số lượng mỗi sản phẩm trong giỏ (chống lạm dụng giữ hàng). */
    public static final int MAX_MOI_SAN_PHAM = 10;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final BanHangTaiQuayInventoryUseCase inventoryUseCase;
    private final QuanLySanPhamService quanLySanPhamService;

    public ClientGioHangService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            GiayChiTietRepository giayChiTietRepository,
            KhachHangRepository khachHangRepository,
            BanHangTaiQuayInventoryUseCase inventoryUseCase,
            QuanLySanPhamService quanLySanPhamService
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.khachHangRepository = khachHangRepository;
        this.inventoryUseCase = inventoryUseCase;
        this.quanLySanPhamService = quanLySanPhamService;
    }

    @Transactional(readOnly = true)
    public GioHangResponse layGio(UUID khachHangId) {
        return timGioHang(khachHangId)
                .map(this::toResponse)
                .orElseGet(() -> new GioHangResponse(null, List.of(), 0, BigDecimal.ZERO));
    }

    @Transactional
    public GioHangResponse them(ThemVaoGioRequest request) {
        HoaDon gio = layHoacTaoGio(request.khachHangId());
        // Sửa giỏ trong khi đang giữ hàng -> coi như rời thanh toán, hoàn tồn về cũ.
        if (gio.getHanGiuHang() != null) {
            hoanTonChoHoaDon(gio);
        }
        GiayChiTiet gct = giayChiTietRepository.findById(request.giayChiTietId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        HoaDonChiTiet ct = hoaDonChiTietRepository
                .findByHoaDonIdAndGiayChiTietId(gio.getId(), gct.getId())
                .orElse(null);
        int soLuongMoi = request.soLuong() + (ct != null ? ct.getSoLuong() : 0);
        if (soLuongMoi > MAX_MOI_SAN_PHAM) {
            throw new BusinessException("Mỗi sản phẩm chỉ được mua tối đa " + MAX_MOI_SAN_PHAM + " sản phẩm");
        }
        // Kiểm tra tồn hiện tại (KHÔNG trừ kho ở bước này).
        inventoryUseCase.validateAvailable(gct, soLuongMoi);

        // Giá chốt vào giỏ = giá sau đợt giảm giá tại thời điểm thêm.
        BigDecimal gia = quanLySanPhamService.layGiaSauGiam(List.of(gct))
                .getOrDefault(gct.getId(), gct.getGiaBan());
        if (ct == null) {
            ct = new HoaDonChiTiet();
            ct.setHoaDon(gio);
            ct.setGiayChiTiet(gct);
            ct.setTrangThai(1);
            ct.setNgayTao(Instant.now());
        }
        ct.setSoLuong(soLuongMoi);
        ct.setGiaDonVi(gia);
        ct.setThanhTien(gia.multiply(BigDecimal.valueOf(soLuongMoi)));
        hoaDonChiTietRepository.save(ct);

        capNhatTongTien(gio);
        return toResponse(gio);
    }

    @Transactional
    public GioHangResponse capNhatSoLuong(Integer itemId, CapNhatSoLuongRequest request) {
        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục giỏ hàng không tồn tại"));
        HoaDon gio = ct.getHoaDon();
        if (gio.getHanGiuHang() != null) {
            hoanTonChoHoaDon(gio);
        }

        if (request.soLuong() <= 0) {
            hoaDonChiTietRepository.delete(ct);
            hoaDonChiTietRepository.flush();
            capNhatTongTien(gio);
            return toResponse(gio);
        }
        if (request.soLuong() > MAX_MOI_SAN_PHAM) {
            throw new BusinessException("Mỗi sản phẩm chỉ được mua tối đa " + MAX_MOI_SAN_PHAM + " sản phẩm");
        }

        inventoryUseCase.validateAvailable(ct.getGiayChiTiet(), request.soLuong());
        ct.setSoLuong(request.soLuong());
        ct.setThanhTien(ct.getGiaDonVi().multiply(BigDecimal.valueOf(request.soLuong())));
        hoaDonChiTietRepository.save(ct);

        capNhatTongTien(gio);
        return toResponse(gio);
    }

    @Transactional
    public GioHangResponse xoa(Integer itemId) {
        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục giỏ hàng không tồn tại"));
        HoaDon gio = ct.getHoaDon();
        if (gio.getHanGiuHang() != null) {
            hoanTonChoHoaDon(gio);
        }
        hoaDonChiTietRepository.delete(ct);
        hoaDonChiTietRepository.flush();
        capNhatTongTien(gio);
        return toResponse(gio);
    }

    /**
     * Khách vào trang thanh toán: chỉ KIỂM TRA tồn kho còn đủ, KHÔNG trừ kho.
     * Tồn kho chỉ bị trừ khi nhân viên xác nhận đơn (bên quản lý hóa đơn).
     */
    @Transactional(readOnly = true)
    public void giuHang(UUID khachHangId) {
        HoaDon gio = timGioHang(khachHangId)
                .orElseThrow(() -> new BusinessException("Giỏ hàng đang trống"));
        List<HoaDonChiTiet> dong = hoaDonChiTietRepository.findByHoaDonId(gio.getId());
        if (dong.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống");
        }
        for (HoaDonChiTiet ct : dong) {
            inventoryUseCase.validateAvailable(ct.getGiayChiTiet(), ct.getSoLuong());
        }
    }

    /** Hủy giữ hàng (khách rời thanh toán): hoàn tồn nếu đang giữ. */
    @Transactional
    public void huyGiuHang(UUID khachHangId) {
        timGioHang(khachHangId)
                .filter(gio -> gio.getHanGiuHang() != null)
                .ifPresent(this::hoanTonChoHoaDon);
    }

    /** Cộng trả lại tồn kho cho các dòng của hóa đơn và xóa hạn giữ. */
    @Transactional
    public void hoanTonChoHoaDon(HoaDon gio) {
        for (HoaDonChiTiet ct : hoaDonChiTietRepository.findByHoaDonId(gio.getId())) {
            GiayChiTiet gct = ct.getGiayChiTiet();
            int ton = gct.getSoLuong() == null ? 0 : gct.getSoLuong();
            gct.setSoLuong(ton + ct.getSoLuong());
            gct.setNgayCapNhat(Instant.now());
            giayChiTietRepository.save(gct);
        }
        gio.setHanGiuHang(null);
        hoaDonRepository.save(gio);
    }

    /** Tìm hóa đơn đang là giỏ của khách (có thể không có). */
    public java.util.Optional<HoaDon> timGioHang(UUID khachHangId) {
        return hoaDonRepository.findGioHang(khachHangId, KENH_BAN_ONLINE, TRANG_THAI_GIO);
    }

    private HoaDon layHoacTaoGio(UUID khachHangId) {
        return timGioHang(khachHangId).orElseGet(() -> {
            KhachHang kh = khachHangRepository.findById(khachHangId)
                    .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));
            HoaDon gio = new HoaDon();
            gio.setMa(taoMaHoaDon());
            gio.setKhachHang(kh);
            gio.setKenhBan(KENH_BAN_ONLINE);
            gio.setTrangThai(TRANG_THAI_GIO);
            gio.setTenNguoiNhan("");
            gio.setSdtNguoiNhan("");
            gio.setDiaChiGiaoHang("");
            gio.setTongTienHang(BigDecimal.ZERO);
            gio.setTienGiam(BigDecimal.ZERO);
            gio.setTongTienThanhToan(BigDecimal.ZERO);
            gio.setNgayLap(Instant.now());
            gio.setNgayTao(Instant.now());
            return hoaDonRepository.save(gio);
        });
    }

    private void capNhatTongTien(HoaDon gio) {
        List<HoaDonChiTiet> dong = hoaDonChiTietRepository.findByHoaDonId(gio.getId());
        BigDecimal tong = dong.stream()
                .map(HoaDonChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        gio.setTongTienHang(tong);
        gio.setTongTienThanhToan(tong.subtract(gio.getTienGiam() == null ? BigDecimal.ZERO : gio.getTienGiam()));
        gio.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(gio);
    }

    private String taoMaHoaDon() {
        return "HD" + Long.toString(System.currentTimeMillis(), 36).toUpperCase()
                + String.format("%03d", SECURE_RANDOM.nextInt(1000));
    }

    private GioHangResponse toResponse(HoaDon gio) {
        List<HoaDonChiTiet> dong = hoaDonChiTietRepository.findGioItems(gio.getId());
        List<GioHangItemResponse> items = new ArrayList<>();
        int tongSoLuong = 0;
        BigDecimal tongTien = BigDecimal.ZERO;
        for (HoaDonChiTiet ct : dong) {
            GiayChiTiet gct = ct.getGiayChiTiet();
            items.add(new GioHangItemResponse(
                    ct.getId(),
                    gct.getId(),
                    gct.getGiay().getId(),
                    gct.getGiay().getTen(),
                    gct.getMauSac().getTen(),
                    gct.getKichCo().getGiaTri(),
                    gct.getGiay().getHinhAnh(),
                    ct.getGiaDonVi(),
                    ct.getSoLuong(),
                    gct.getSoLuong()
            ));
            tongSoLuong += ct.getSoLuong();
            tongTien = tongTien.add(ct.getThanhTien());
        }
        return new GioHangResponse(gio.getId(), items, tongSoLuong, tongTien);
    }
}
