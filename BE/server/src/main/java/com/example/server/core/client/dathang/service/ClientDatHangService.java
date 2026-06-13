package com.example.server.core.client.dathang.service;

import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayInventoryUseCase;
import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.dathang.dto.DatHangResponse;
import com.example.server.core.client.giohang.service.ClientGioHangService;
import com.example.server.core.client.vanchuyen.dto.TinhPhiShipRequest;
import com.example.server.core.client.vanchuyen.service.ClientPhiVanChuyenService;
import com.example.server.core.client.voucher.service.ClientVoucherService;
<<<<<<< Updated upstream
import com.example.server.core.realtime.hoadon.HoaDonRealtimePublisher;
import com.example.server.entity.GiayChiTiet;
=======
>>>>>>> Stashed changes
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.ThanhToan;
import com.example.server.entity.VanChuyen;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.ThanhToanRepository;
import com.example.server.repository.VanChuyenRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Đặt hàng từ giỏ (hóa đơn đang mở). Đây là lúc tồn kho bị trừ.
 * COD tạo giao dịch chờ thanh toán; VNPay chỉ chốt đơn sau khi phiên QR
 * đã được xác nhận nên giao dịch được ghi nhận thành công ngay.
 */
@Service
public class ClientDatHangService {

    private static final int TRANG_THAI_CHO_XAC_NHAN = 1;
    private static final int HINH_THUC_VNPAY = 3;
    private static final int HINH_THUC_COD = 4;
    private static final int TRANG_THAI_CHO_THANH_TOAN = 0;
    private static final int TRANG_THAI_DA_THANH_TOAN = 1;
    private static final int LOAI_GIAO_DICH_THANH_TOAN = 1;

    private final ClientGioHangService gioHangService;
    private final ClientVoucherService voucherService;
    private final ClientPhiVanChuyenService phiVanChuyenService;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final BanHangTaiQuayInventoryUseCase inventoryUseCase;
    private final ThanhToanRepository thanhToanRepository;
    private final VanChuyenRepository vanChuyenRepository;
    private final HoaDonRealtimePublisher hoaDonRealtimePublisher;

    public ClientDatHangService(
            ClientGioHangService gioHangService,
            ClientVoucherService voucherService,
            ClientPhiVanChuyenService phiVanChuyenService,
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            BanHangTaiQuayInventoryUseCase inventoryUseCase,
            ThanhToanRepository thanhToanRepository,
            VanChuyenRepository vanChuyenRepository,
            HoaDonRealtimePublisher hoaDonRealtimePublisher
    ) {
        this.gioHangService = gioHangService;
        this.voucherService = voucherService;
        this.phiVanChuyenService = phiVanChuyenService;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.inventoryUseCase = inventoryUseCase;
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
        HoaDon hoaDon = gioHangService.timGioHang(request.khachHangId())
                .orElseThrow(() -> new BusinessException("Giỏ hàng đang trống"));

        List<HoaDonChiTiet> dong = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());
        if (dong.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống");
        }

        if (hoaDon.getHanGiuHang() != null) {
            // Giỏ còn giữ hàng theo mô hình cũ (trước khi đổi) -> hoàn tồn đã trừ tạm.
            gioHangService.hoanTonChoHoaDon(hoaDon);
        }
        // KHÔNG trừ tồn kho khi đặt hàng - chỉ kiểm tra còn đủ. Tồn kho sẽ bị trừ
        // khi nhân viên chuyển đơn sang "Đã xác nhận" ở quản lý hóa đơn.
        for (HoaDonChiTiet ct : dong) {
            inventoryUseCase.validateAvailable(ct.getGiayChiTiet(), ct.getSoLuong());
        }
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

        BigDecimal tongTienHang = hoaDon.getTongTienHang() == null
                ? BigDecimal.ZERO
                : hoaDon.getTongTienHang();
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

        // Phí vận chuyển GHN theo địa chỉ nhận (phải tính khi hóa đơn còn là giỏ - trạng thái 0).
        BigDecimal phiShip = tinhPhiShip(request);
        hoaDon.setTongTienThanhToan(
                tongTienHang.subtract(tienGiam).max(BigDecimal.ZERO).add(phiShip));

        String hinhThuc = chuanHoaHinhThucThanhToan(request.hinhThucThanhToan());
        Instant now = Instant.now();
        hoaDon.setTrangThai(TRANG_THAI_CHO_XAC_NHAN);
        hoaDon.setNgayLap(now);
        hoaDon.setNgayCapNhat(now);
        if ("VNPAY".equals(hinhThuc)) {
            hoaDon.setNgayThanhToan(now);
        }
        hoaDonRepository.save(hoaDon);
        luuVanChuyen(hoaDon, phiShip, now);
        taoGiaoDichThanhToan(hoaDon, hinhThuc, maGiaoDich, now);
        hoaDonRealtimePublisher.publishAfterCommit(hoaDon, "TAO_MOI");

        return new DatHangResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                hoaDon.getTongTienThanhToan(),
                hoaDon.getTrangThai(),
                hinhThuc
        );
    }

    /** Phí ship theo địa chỉ nhận (GHN, có phí ước tính dự phòng khi GHN không khả dụng). */
    private BigDecimal tinhPhiShip(DatHangRequest request) {
        BigDecimal phi = phiVanChuyenService.tinhPhi(new TinhPhiShipRequest(
                request.khachHangId(),
                request.tinhThanh(),
                request.quanHuyen(),
                request.phuongXa(),
                request.diaChiCuThe()
        )).phiVanChuyen();
        return phi == null ? BigDecimal.ZERO : phi.max(BigDecimal.ZERO);
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
