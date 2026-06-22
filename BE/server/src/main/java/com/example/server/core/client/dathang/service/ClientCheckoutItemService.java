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

    private static final int MAX_MOI_SAN_PHAM = 10;

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
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống");
        }

        Map<Integer, Integer> soLuongTheoBienThe = new LinkedHashMap<>();
        for (DatHangItemRequest item : requests) {
            int tong = soLuongTheoBienThe.merge(item.giayChiTietId(), item.soLuong(), Integer::sum);
            if (tong > MAX_MOI_SAN_PHAM) {
                throw new BusinessException(
                        "Mỗi sản phẩm chỉ được mua tối đa " + MAX_MOI_SAN_PHAM + " sản phẩm");
            }
        }

        List<GiayChiTiet> bienThes = new ArrayList<>();
        for (Integer id : soLuongTheoBienThe.keySet()) {
            GiayChiTiet bienThe = giayChiTietRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Biến thể sản phẩm không tồn tại: " + id));
            // Chặn đặt sản phẩm đã ngừng bán (admin ẩn biến thể) dù còn tồn kho.
            if (!Integer.valueOf(1).equals(bienThe.getKichHoat())) {
                throw new BusinessException(
                        "Sản phẩm \"" + bienThe.getGiay().getTen() + "\" đã ngừng bán");
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

    public record KetQua(List<HoaDonChiTiet> chiTiets, BigDecimal tongTienHang) {
    }
}
