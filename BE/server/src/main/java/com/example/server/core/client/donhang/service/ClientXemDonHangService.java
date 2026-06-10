package com.example.server.core.client.donhang.service;

import com.example.server.core.client.donhang.dto.DonHangChiTietResponse;
import com.example.server.core.client.donhang.dto.DonHangChiTietResponse.DongSanPham;
import com.example.server.core.client.donhang.dto.DonHangTomTatResponse;
import com.example.server.entity.DanhGia;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DanhGiaRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientXemDonHangService {

    /** Trạng thái "giỏ hàng" (chưa đặt) - không hiện trong danh sách đơn. */
    private static final int TRANG_THAI_GIO = 0;

    /** Trạng thái đơn đã hoàn thành (giao xong) - mới được xác nhận nhận hàng / đánh giá. */
    private static final int TRANG_THAI_HOAN_THANH = 5;

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final DanhGiaRepository danhGiaRepository;

    public ClientXemDonHangService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            DanhGiaRepository danhGiaRepository
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.danhGiaRepository = danhGiaRepository;
    }

    @Transactional(readOnly = true)
    public List<DonHangTomTatResponse> danhSach(UUID khachHangId) {
        List<DonHangTomTatResponse> result = new ArrayList<>();
        for (HoaDon hd : hoaDonRepository.findByKhachHangId(khachHangId)) {
            if (hd.getTrangThai() != null && hd.getTrangThai() == TRANG_THAI_GIO) {
                continue; // bỏ qua giỏ hàng
            }
            int soLuong = hoaDonChiTietRepository.findByHoaDonId(hd.getId()).stream()
                    .mapToInt(ct -> ct.getSoLuong() == null ? 0 : ct.getSoLuong())
                    .sum();
            result.add(new DonHangTomTatResponse(
                    hd.getId(), hd.getMa(), hd.getNgayLap(),
                    hd.getTrangThai(), nhanTrangThai(hd.getTrangThai()),
                    soLuong, hd.getTongTienThanhToan()));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DonHangChiTietResponse chiTiet(UUID khachHangId, Integer id) {
        HoaDon hd = hoaDonRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        if (hd.getKhachHang() == null || !hd.getKhachHang().getId().equals(khachHangId)) {
            throw new BusinessException("Bạn không có quyền xem đơn hàng này");
        }

        List<HoaDonChiTiet> dong = hoaDonChiTietRepository.findGioItems(hd.getId());

        // Map dòng hóa đơn chi tiết -> đánh giá (nếu đã đánh giá).
        List<Integer> ctIds = dong.stream().map(HoaDonChiTiet::getId).toList();
        Map<Integer, DanhGia> danhGiaMap = new HashMap<>();
        if (!ctIds.isEmpty()) {
            for (DanhGia dg : danhGiaRepository.findByHoaDonChiTietIdIn(ctIds)) {
                if (dg.getHoaDonChiTiet() != null) {
                    danhGiaMap.put(dg.getHoaDonChiTiet().getId(), dg);
                }
            }
        }

        List<DongSanPham> sanPhams = new ArrayList<>();
        BigDecimal tamTinh = BigDecimal.ZERO;
        for (HoaDonChiTiet ct : dong) {
            GiayChiTiet gct = ct.getGiayChiTiet();
            BigDecimal giaNiemYet = gct.getGiaBan(); // giá niêm yết hiện tại của biến thể
            BigDecimal giaDonVi = ct.getGiaDonVi();   // giá đã trả (sau đợt giảm giá) lúc đặt
            int sl = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
            tamTinh = tamTinh.add(giaNiemYet.multiply(BigDecimal.valueOf(sl)));
            DanhGia dg = danhGiaMap.get(ct.getId());
            sanPhams.add(new DongSanPham(
                    ct.getId(),
                    gct.getGiay().getId(),
                    gct.getGiay().getTen(),
                    gct.getMauSac().getTen(),
                    gct.getKichCo().getGiaTri(),
                    gct.getGiay().getHinhAnh(),
                    giaNiemYet, giaDonVi, sl, ct.getThanhTien(),
                    dg != null,
                    dg != null ? dg.getSoSao() : null,
                    dg != null ? dg.getNoiDung() : null));
        }

        BigDecimal tongTienHang = hd.getTongTienHang() == null ? BigDecimal.ZERO : hd.getTongTienHang();
        BigDecimal giamDot = tamTinh.subtract(tongTienHang).max(BigDecimal.ZERO);
        BigDecimal giamVoucher = hd.getTienGiam() == null ? BigDecimal.ZERO : hd.getTienGiam();
        String maPhieu = hd.getPhieuGiamGia() != null ? hd.getPhieuGiamGia().getMa() : null;

        return new DonHangChiTietResponse(
                hd.getId(), hd.getMa(), hd.getNgayLap(),
                hd.getTrangThai(), nhanTrangThai(hd.getTrangThai()),
                Boolean.TRUE.equals(hd.getDaNhanHang()),
                hd.getTenNguoiNhan(), hd.getSdtNguoiNhan(), hd.getDiaChiGiaoHang(),
                maPhieu, sanPhams,
                tamTinh, giamDot, giamVoucher, hd.getTongTienThanhToan());
    }

    /** Khách xác nhận đã nhận hàng (đơn phải đã hoàn thành). */
    @Transactional
    public void xacNhanDaNhanHang(UUID khachHangId, Integer id) {
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        if (hd.getKhachHang() == null || !hd.getKhachHang().getId().equals(khachHangId)) {
            throw new BusinessException("Bạn không có quyền thao tác đơn hàng này");
        }
        if (hd.getTrangThai() == null || hd.getTrangThai() != TRANG_THAI_HOAN_THANH) {
            throw new BusinessException("Đơn hàng chưa hoàn thành, chưa thể xác nhận đã nhận hàng");
        }
        hd.setDaNhanHang(true);
        hd.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hd);
    }

    private String nhanTrangThai(Integer trangThai) {
        if (trangThai == null) {
            return "Không xác định";
        }
        return switch (trangThai) {
            case 1 -> "Chờ xác nhận";
            case 2 -> "Đã xác nhận";
            case 3, 4 -> "Đang giao";
            case 5 -> "Hoàn thành";
            case 6 -> "Đã hủy";
            default -> "Đang xử lý";
        };
    }
}
