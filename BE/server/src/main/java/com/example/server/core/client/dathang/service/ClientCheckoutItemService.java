package com.example.server.core.client.dathang.service;

import com.example.server.core.admin.banHangTaiQuay.service.TonKhoTaiQuayService;
import com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService;
import com.example.server.core.client.dathang.dto.DatHangItemRequest;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.GiayChiTietRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientCheckoutItemService {

    private final GiayChiTietRepository giayChiTietRepository;
    private final TonKhoTaiQuayService tonKhoService;
    private final QuanLySanPhamService quanLySanPhamService;

    public ClientCheckoutItemService(
            GiayChiTietRepository giayChiTietRepository,
            TonKhoTaiQuayService tonKhoService,
            QuanLySanPhamService quanLySanPhamService
    ) {
        this.giayChiTietRepository = giayChiTietRepository;
        this.tonKhoService = tonKhoService;
        this.quanLySanPhamService = quanLySanPhamService;
    }

    @Transactional(readOnly = true)
    public KetQua chuanBi(List<DatHangItemRequest> requests) {
        return chuanBi(requests, null);
    }

    /**
     * Chuẩn bị dòng hóa đơn từ giỏ.
     *
     * @param giaKhoa giá đã KHÓA theo biến thể (snapshot lúc tạo mã QR VNPAY/VietQR). Nếu có giá
     *                khóa cho biến thể thì dùng đúng giá đó, không tính lại theo đợt giảm hiện tại
     *                -> giá sản phẩm trong đơn không bị đổi dù đợt giảm thay đổi lúc khách đang trả.
     */
    @Transactional(readOnly = true)
    public KetQua chuanBi(List<DatHangItemRequest> requests, Map<Integer, BigDecimal> giaKhoa) {
        return chuanBi(requests, giaKhoa, false);
    }

    /**
     * @param boQuaKiemTon true khi tồn đã được GIỮ CHỖ trước (trừ kho lúc tạo mã QR) -> không
     *                     kiểm lại tồn/ngừng bán nữa khi tạo đơn lúc thanh toán thành công.
     */
    @Transactional(readOnly = true)
    public KetQua chuanBi(
            List<DatHangItemRequest> requests, Map<Integer, BigDecimal> giaKhoa, boolean boQuaKiemTon) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống");
        }

        Map<Integer, Integer> soLuongTheoBienThe = new LinkedHashMap<>();
        for (DatHangItemRequest item : requests) {
            soLuongTheoBienThe.merge(item.giayChiTietId(), item.soLuong(), Integer::sum);
        }

        List<GiayChiTiet> bienThes = new ArrayList<>();
        for (Integer id : soLuongTheoBienThe.keySet()) {
            GiayChiTiet bienThe = giayChiTietRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Biến thể sản phẩm không tồn tại: " + id));
            if (!boQuaKiemTon) {
                // Chặn đặt sản phẩm đã ngừng bán (admin ẩn biến thể) dù còn tồn kho.
                if (!Integer.valueOf(1).equals(bienThe.getKichHoat())) {
                    throw new BusinessException(
                            "Sản phẩm \"" + bienThe.getGiay().getTen() + "\" đã ngừng bán");
                }
                tonKhoService.validateAvailable(bienThe, soLuongTheoBienThe.get(id));
            }
            tonKhoService.validateAvailable(bienThe, soLuongTheoBienThe.get(id));
            bienThes.add(bienThe);
        }

        Map<Integer, BigDecimal> giaHienTai = quanLySanPhamService.layGiaSauGiam(bienThes);
        List<HoaDonChiTiet> chiTiets = new ArrayList<>();
        BigDecimal tongTienHang = BigDecimal.ZERO;
        Instant now = Instant.now();

        for (GiayChiTiet bienThe : bienThes) {
            int soLuong = soLuongTheoBienThe.get(bienThe.getId());
            BigDecimal gia = giaKhoa != null && giaKhoa.get(bienThe.getId()) != null
                    ? giaKhoa.get(bienThe.getId())
                    : giaHienTai.getOrDefault(bienThe.getId(), bienThe.getGiaBan());
            BigDecimal thanhTien = gia.multiply(BigDecimal.valueOf(soLuong));

            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setGiayChiTiet(bienThe);
            chiTiet.setSoLuong(soLuong);
            chiTiet.setGiaDonVi(gia);
            chiTiet.setThanhTien(thanhTien);
            chiTiet.setTrangThai(1);
            chiTiet.setNgayTao(now);
            chiTiets.add(chiTiet);
            tongTienHang = tongTienHang.add(thanhTien);
        }

        return new KetQua(chiTiets, tongTienHang);
    }

    /**
     * Giữ chỗ tồn kho: trừ kho ngay (có khóa hàng) cho danh sách sản phẩm. Dùng lúc tạo mã QR
     * để tránh oversell + đảm bảo lúc thanh toán xong tạo đơn không lỗi thiếu hàng.
     * @return map biến thể -> số lượng đã giữ (để hoàn lại nếu hết hạn/không thanh toán).
     */
    @Transactional
    public Map<Integer, Integer> giuChoTonKho(List<DatHangItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống");
        }
        Map<Integer, Integer> soLuongTheoBienThe = new LinkedHashMap<>();
        for (DatHangItemRequest item : requests) {
            soLuongTheoBienThe.merge(item.giayChiTietId(), item.soLuong(), Integer::sum);
        }
        Map<Integer, Integer> daGiu = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> e : soLuongTheoBienThe.entrySet()) {
            GiayChiTiet bienThe = giayChiTietRepository.findByIdForUpdate(e.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Biến thể sản phẩm không tồn tại: " + e.getKey()));
            tonKhoService.deductStock(bienThe, e.getValue());
            giayChiTietRepository.save(bienThe);
            daGiu.put(e.getKey(), e.getValue());
        }
        return daGiu;
    }

    /** Hoàn lại tồn đã giữ chỗ (khi phiên QR hết hạn / khách không thanh toán). */
    @Transactional
    public void hoanGiuCho(Map<Integer, Integer> daGiu) {
        if (daGiu == null || daGiu.isEmpty()) {
            return;
        }
        for (Map.Entry<Integer, Integer> e : daGiu.entrySet()) {
            GiayChiTiet bienThe = giayChiTietRepository.findByIdForUpdate(e.getKey()).orElse(null);
            if (bienThe == null) {
                continue;
            }
            int ton = bienThe.getSoLuong() == null ? 0 : bienThe.getSoLuong();
            bienThe.setSoLuong(ton + e.getValue());
            giayChiTietRepository.save(bienThe);
        }
    }

    public record KetQua(List<HoaDonChiTiet> chiTiets, BigDecimal tongTienHang) {
    }
}
