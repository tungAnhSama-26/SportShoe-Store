package com.example.server.core.client.dathang.service;

import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayInventoryUseCase;
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
    private final BanHangTaiQuayInventoryUseCase inventoryUseCase;
    private final QuanLySanPhamService quanLySanPhamService;

    public ClientCheckoutItemService(
            GiayChiTietRepository giayChiTietRepository,
            BanHangTaiQuayInventoryUseCase inventoryUseCase,
            QuanLySanPhamService quanLySanPhamService
    ) {
        this.giayChiTietRepository = giayChiTietRepository;
        this.inventoryUseCase = inventoryUseCase;
        this.quanLySanPhamService = quanLySanPhamService;
    }

    @Transactional(readOnly = true)
    public KetQua chuanBi(List<DatHangItemRequest> requests) {
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
            inventoryUseCase.validateAvailable(bienThe, soLuongTheoBienThe.get(id));
            bienThes.add(bienThe);
        }

        Map<Integer, BigDecimal> giaHienTai = quanLySanPhamService.layGiaSauGiam(bienThes);
        List<HoaDonChiTiet> chiTiets = new ArrayList<>();
        BigDecimal tongTienHang = BigDecimal.ZERO;
        Instant now = Instant.now();

        for (GiayChiTiet bienThe : bienThes) {
            int soLuong = soLuongTheoBienThe.get(bienThe.getId());
            BigDecimal gia = giaHienTai.getOrDefault(bienThe.getId(), bienThe.getGiaBan());
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
