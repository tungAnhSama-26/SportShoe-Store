package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.request.DoiBienTheTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoChiTietResponse;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.VanChuyenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class HoaDonChoTaiQuayService {

    private static final int KENH_BAN_TAI_QUAY = 1;
    private static final int TRANG_THAI_HOA_DON_CHO_TAI_QUAY = 11;
    private static final int TRANG_THAI_HOA_DON_HUY = 6;
    private static final String GHI_CHU_TAO_HOA_DON_TAI_QUAY = "Hóa đơn chờ tạo từ màn hình bán hàng tại quầy";

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final VanChuyenRepository vanChuyenRepository;

    private final XacThucTaiQuayService validationUseCase;
    private final TrangThaiHoaDonTaiQuayService invoiceStateUseCase;
    private final HoaDonTaiQuayService invoiceUseCase;
    private final PhieuGiamGiaTaiQuayService voucherUseCase;
    private final TonKhoTaiQuayService inventoryUseCase;

    public HoaDonChoTaiQuayService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            GiayChiTietRepository giayChiTietRepository,
            VanChuyenRepository vanChuyenRepository,
            XacThucTaiQuayService validationUseCase,
            TrangThaiHoaDonTaiQuayService invoiceStateUseCase,
            HoaDonTaiQuayService invoiceUseCase,
            PhieuGiamGiaTaiQuayService voucherUseCase,
            TonKhoTaiQuayService inventoryUseCase
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.vanChuyenRepository = vanChuyenRepository;
        this.validationUseCase = validationUseCase;
        this.invoiceStateUseCase = invoiceStateUseCase;
        this.invoiceUseCase = invoiceUseCase;
        this.voucherUseCase = voucherUseCase;
        this.inventoryUseCase = inventoryUseCase;
    }

    @Transactional
    public HoaDonChoChiTietResponse taoHoaDonCho(TaoHoaDonChoRequest request) {
        long soLuongHoaDonCho = hoaDonRepository.countByKenhBanAndTrangThai(KENH_BAN_TAI_QUAY, TRANG_THAI_HOA_DON_CHO_TAI_QUAY);
        if (soLuongHoaDonCho >= 5) {
            throw new BusinessException("Đã đạt giới hạn tối đa 5 hóa đơn chờ");
        }

        @SuppressWarnings("unused")
        HoaDon savedHoaDon = invoiceUseCase.taoHoaDon(
                request.khachHangId(),
                request.tenKhachHang(),
                request.soDienThoai(),
                request.maPhieuGiamGia(),
                request.thongTinGiaoHang(),
                request.items(),
                TRANG_THAI_HOA_DON_CHO_TAI_QUAY,
                GHI_CHU_TAO_HOA_DON_TAI_QUAY
        );
        invoiceUseCase.luuLichSuHoaDon(savedHoaDon, TRANG_THAI_HOA_DON_CHO_TAI_QUAY, savedHoaDon.getGhiChu());
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
            throw new BusinessException("Hóa đơn này không còn ở trạng thái chờ (đã thanh toán hoặc hủy)");
        }

        // 1. Validate duplicate items in request
        validationUseCase.validateDuplicateItems(request.items() != null ? request.items() : new ArrayList<>());

        List<HoaDonChiTiet> oldItems = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDonId);
        Set<Integer> bypassActiveCheckIds = new HashSet<>();
        for (HoaDonChiTiet item : oldItems) {
            bypassActiveCheckIds.add(item.getGiayChiTiet().getId());
        }

        // Map request items by chiTietId for easy lookup
        Map<Integer, com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoItemRequest> requestedItemsMap = new HashMap<>();
        if (request.items() != null) {
            for (var itemReq : request.items()) {
                requestedItemsMap.put(itemReq.chiTietId(), itemReq);
            }
        }

        List<HoaDonChiTiet> chiTietCanLuu = new ArrayList<>();
        Set<Integer> processedChiTietIds = new HashSet<>();

        // 2. Process existing items (update quantity and stock, or delete)
        for (HoaDonChiTiet oldItem : oldItems) {
            Integer chiTietId = oldItem.getGiayChiTiet().getId();
            GiayChiTiet giayChiTiet = oldItem.getGiayChiTiet();

            if (requestedItemsMap.containsKey(chiTietId)) {
                var reqItem = requestedItemsMap.get(chiTietId);
                int newQty = reqItem.soLuong();
                int oldQty = oldItem.getSoLuong();
                int diff = newQty - oldQty;

                if (diff != 0) {
                    boolean bypassActiveCheck = bypassActiveCheckIds.contains(chiTietId);
                    if (diff > 0) {
                        inventoryUseCase.deductStock(giayChiTiet, diff, bypassActiveCheck);
                    } else {
                        inventoryUseCase.restoreStock(giayChiTiet, -diff);
                    }
                    giayChiTietRepository.save(giayChiTiet);

                    oldItem.setSoLuong(newQty);
                    BigDecimal giaDonVi = (reqItem.giaBan() != null) ? reqItem.giaBan() : oldItem.getGiaDonVi();
                    oldItem.setGiaDonVi(giaDonVi);
                    oldItem.setThanhTien(giaDonVi.multiply(BigDecimal.valueOf(newQty)));
                } else {
                    // Update price if it has changed
                    BigDecimal giaDonVi = (reqItem.giaBan() != null) ? reqItem.giaBan() : oldItem.getGiaDonVi();
                    if (giaDonVi.compareTo(oldItem.getGiaDonVi()) != 0) {
                        oldItem.setGiaDonVi(giaDonVi);
                        oldItem.setThanhTien(giaDonVi.multiply(BigDecimal.valueOf(oldQty)));
                    }
                }

                chiTietCanLuu.add(hoaDonChiTietRepository.save(oldItem));
                processedChiTietIds.add(chiTietId);
            } else {
                // Item was removed from cart, restore stock and delete
                inventoryUseCase.restoreStock(giayChiTiet, oldItem.getSoLuong());
                giayChiTietRepository.save(giayChiTiet);
                hoaDonChiTietRepository.delete(oldItem);
            }
        }

        // 3. Add new items
        if (request.items() != null) {
            for (var reqItem : request.items()) {
                if (!processedChiTietIds.contains(reqItem.chiTietId())) {
                    HoaDonChiTiet newItem = invoiceUseCase.taoDongHoaDon(reqItem, null);
                    newItem.setHoaDon(hoaDon);
                    chiTietCanLuu.add(hoaDonChiTietRepository.save(newItem));
                }
            }
        }

        // Flush changes to database
        giayChiTietRepository.flush();
        hoaDonChiTietRepository.flush();



        BigDecimal tongTienHang = chiTietCanLuu.stream()
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

        return invoiceUseCase.mapHoaDonChiTiet(savedHoaDon, chiTietCanLuu, vanChuyenRepository.findByHoaDonId(savedHoaDon.getId()).orElse(null));
    }

    @Transactional
    public HoaDonChoChiTietResponse doiBienThe(Integer hoaDonChiTietId, DoiBienTheTaiQuayRequest request) {
        HoaDonChiTiet oldItem = hoaDonChiTietRepository.findById(hoaDonChiTietId)
                .orElseGet(() -> hoaDonChiTietRepository.findAll().stream()
                        .filter(h -> h.getId().equals(hoaDonChiTietId) || h.getGiayChiTiet().getId().equals(hoaDonChiTietId))
                        .findFirst()
                        .orElse(null));

        if (oldItem == null) {
            throw new ResourceNotFoundException("Chi tiết hóa đơn không tồn tại");
        }

        HoaDon hoaDon = oldItem.getHoaDon();
        if (!invoiceStateUseCase.trangThaiHoaDonCho(hoaDon.getTrangThai())) {
            throw new BusinessException("Hóa đơn này không còn ở trạng thái chờ");
        }

        GiayChiTiet bienTheMoi = giayChiTietRepository.findById(request.giayChiTietMoiId())
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể mới không tồn tại"));

        int soLuong = (request.soLuong() != null && request.soLuong() > 0) ? request.soLuong() : oldItem.getSoLuong();

        // Stock adjustment: restore old, deduct new
        inventoryUseCase.restoreStock(oldItem.getGiayChiTiet(), oldItem.getSoLuong());
        inventoryUseCase.deductStock(bienTheMoi, soLuong, false);

        giayChiTietRepository.save(oldItem.getGiayChiTiet());
        giayChiTietRepository.save(bienTheMoi);

        oldItem.setGiayChiTiet(bienTheMoi);
        oldItem.setSoLuong(soLuong);
        BigDecimal giaBan = bienTheMoi.getGiaBan();
        oldItem.setGiaDonVi(giaBan);
        oldItem.setThanhTien(giaBan.multiply(BigDecimal.valueOf(soLuong)));
        hoaDonChiTietRepository.save(oldItem);

        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId());
        BigDecimal tongTienHang = items.stream()
                .map(HoaDonChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        hoaDon.setTongTienHang(tongTienHang);
        hoaDon.setTongTienThanhToan(tongTienHang);
        if (hoaDon.getPhieuGiamGia() != null) {
            voucherUseCase.giaiPhongPhieuGiamGia(hoaDon.getPhieuGiamGia(), hoaDon.getKhachHang());
            voucherUseCase.ganPhieuGiamGiaChoHoaDon(hoaDon, hoaDon.getPhieuGiamGia().getMa(), hoaDon.getKhachHang(), tongTienHang);
        }

        hoaDon.setNgayCapNhat(Instant.now());
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        return invoiceUseCase.mapHoaDonChiTiet(savedHoaDon, items, vanChuyenRepository.findByHoaDonId(savedHoaDon.getId()).orElse(null));
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
            item.setTrangThai(0);
            inventoryUseCase.restoreStock(item.getGiayChiTiet(), item.getSoLuong());
            giayChiTietRepository.save(item.getGiayChiTiet());
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

    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ 1 lần để dọn dẹp
    @Transactional
    public void cleanupExpiredPendingInvoices() {
        Instant moc = Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS);
        List<HoaDon> expiredInvoices = hoaDonRepository.findExpiredPendingInvoices(KENH_BAN_TAI_QUAY, TRANG_THAI_HOA_DON_CHO_TAI_QUAY, moc);
        for (HoaDon hd : expiredInvoices) {
            huyHoaDonCho(hd.getId());
        }
    }
}
