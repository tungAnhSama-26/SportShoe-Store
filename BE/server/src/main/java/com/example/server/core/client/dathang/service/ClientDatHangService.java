package com.example.server.core.client.dathang.service;

import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayInventoryUseCase;
import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.dathang.dto.DatHangResponse;
import com.example.server.core.client.giohang.service.ClientGioHangService;
import com.example.server.core.client.voucher.service.ClientVoucherService;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Đặt hàng từ giỏ (hóa đơn đang mở). Đây là lúc TỒN KHO BỊ TRỪ.
 * - COD: tạo đơn ở trạng thái "Chờ xác nhận" và trừ kho ngay.
 * - VNPay: xử lý sau (trừ kho sau khi thanh toán thành công).
 */
@Service
public class ClientDatHangService {

    /** Hóa đơn chuyển sang "Chờ xác nhận" sau khi đặt. */
    private static final int TRANG_THAI_CHO_XAC_NHAN = 1;

    private final ClientGioHangService gioHangService;
    private final ClientVoucherService voucherService;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final BanHangTaiQuayInventoryUseCase inventoryUseCase;

    public ClientDatHangService(
            ClientGioHangService gioHangService,
            ClientVoucherService voucherService,
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            GiayChiTietRepository giayChiTietRepository,
            BanHangTaiQuayInventoryUseCase inventoryUseCase
    ) {
        this.gioHangService = gioHangService;
        this.voucherService = voucherService;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.inventoryUseCase = inventoryUseCase;
    }

    @Transactional
    public DatHangResponse datHang(DatHangRequest request) {
        HoaDon hoaDon = gioHangService.timGioHang(request.khachHangId())
                .orElseThrow(() -> new BusinessException("Giỏ hàng đang trống"));

        List<HoaDonChiTiet> dong = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());
        if (dong.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống");
        }

        if (hoaDon.getHanGiuHang() != null) {
            // Đang giữ hàng -> tồn kho đã được trừ khi vào thanh toán, chỉ cần chốt đơn.
            hoaDon.setHanGiuHang(null);
        } else {
            // Chưa giữ (hoặc hết hạn đã hoàn): kiểm tra tồn tất cả trước rồi mới trừ.
            for (HoaDonChiTiet ct : dong) {
                inventoryUseCase.validateAvailable(ct.getGiayChiTiet(), ct.getSoLuong());
            }
            for (HoaDonChiTiet ct : dong) {
                GiayChiTiet gct = ct.getGiayChiTiet();
                inventoryUseCase.deductStock(gct, ct.getSoLuong());
                giayChiTietRepository.save(gct);
            }
        }

        // 3. Điền thông tin giao hàng + chuyển trạng thái -> hóa đơn không còn là "giỏ".
        String diaChi = Stream.of(request.diaChiCuThe(), request.phuongXa(), request.quanHuyen(), request.tinhThanh())
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        hoaDon.setTenNguoiNhan(request.tenNguoiNhan().trim());
        hoaDon.setSdtNguoiNhan(request.sdtNguoiNhan().trim());
        hoaDon.setDiaChiGiaoHang(diaChi);
        hoaDon.setGhiChu(request.ghiChu());

        // Áp mã giảm giá (nếu có): gán phiếu + trừ lượt + tính lại tổng thanh toán.
        if (request.maPhieuGiamGia() != null && !request.maPhieuGiamGia().isBlank()) {
            BigDecimal tongTienHang = hoaDon.getTongTienHang() == null ? BigDecimal.ZERO : hoaDon.getTongTienHang();
            BigDecimal tienGiam = voucherService.apDungVaoHoaDon(
                    hoaDon, request.maPhieuGiamGia(), hoaDon.getKhachHang(), tongTienHang);
            hoaDon.setTienGiam(tienGiam);
            hoaDon.setTongTienThanhToan(tongTienHang.subtract(tienGiam).max(BigDecimal.ZERO));
        }

        hoaDon.setTrangThai(TRANG_THAI_CHO_XAC_NHAN);
        hoaDon.setNgayLap(Instant.now());
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hoaDon);

        String hinhThuc = request.hinhThucThanhToan() == null ? "COD" : request.hinhThucThanhToan();
        return new DatHangResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                hoaDon.getTongTienThanhToan(),
                hoaDon.getTrangThai(),
                hinhThuc
        );
    }
}
