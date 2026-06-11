package com.example.server.core.client.donhang.service;

import com.example.server.core.client.donhang.dto.DonHangChiTietResponse;
import com.example.server.core.client.donhang.dto.DonHangChiTietResponse.DongSanPham;
import com.example.server.core.client.donhang.dto.DonHangChiTietResponse.LichSuTraHang;
import com.example.server.core.client.donhang.dto.DonHangChiTietResponse.LichSuTrangThai;
import com.example.server.core.client.donhang.dto.DonHangTomTatResponse;
import com.example.server.entity.DanhGia;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.PhieuTraHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DanhGiaRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.LichSuHoaDonRepository;
import com.example.server.repository.LichSuPhieuTraHangRepository;
import com.example.server.repository.PhieuTraHangRepository;
import com.example.server.repository.VanChuyenRepository;
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
    private final PhieuTraHangRepository phieuTraHangRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final VanChuyenRepository vanChuyenRepository;
    private final LichSuPhieuTraHangRepository lichSuPhieuTraHangRepository;

    public ClientXemDonHangService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            DanhGiaRepository danhGiaRepository,
            PhieuTraHangRepository phieuTraHangRepository,
            LichSuHoaDonRepository lichSuHoaDonRepository,
            VanChuyenRepository vanChuyenRepository,
            LichSuPhieuTraHangRepository lichSuPhieuTraHangRepository
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.danhGiaRepository = danhGiaRepository;
        this.phieuTraHangRepository = phieuTraHangRepository;
        this.lichSuHoaDonRepository = lichSuHoaDonRepository;
        this.vanChuyenRepository = vanChuyenRepository;
        this.lichSuPhieuTraHangRepository = lichSuPhieuTraHangRepository;
    }

    @Transactional(readOnly = true)
    public List<DonHangTomTatResponse> danhSach(UUID khachHangId) {
        List<DonHangTomTatResponse> result = new ArrayList<>();
        for (HoaDon hd : hoaDonRepository.findByKhachHangId(khachHangId)) {
            if (hd.getTrangThai() != null && hd.getTrangThai() == TRANG_THAI_GIO) {
                continue; // bỏ qua giỏ hàng
            }
            List<HoaDonChiTiet> dong = hoaDonChiTietRepository.findGioItems(hd.getId());
            int soLuong = dong.stream()
                    .mapToInt(ct -> ct.getSoLuong() == null ? 0 : ct.getSoLuong())
                    .sum();

            List<DonHangTomTatResponse.DongSanPhamTomTat> sanPhams = new ArrayList<>();
            for (HoaDonChiTiet ct : dong) {
                GiayChiTiet gct = ct.getGiayChiTiet();
                sanPhams.add(new DonHangTomTatResponse.DongSanPhamTomTat(
                        ct.getId(),
                        gct.getId(),
                        gct.getGiay().getTen(),
                        gct.getMauSac().getTen(),
                        gct.getKichCo().getGiaTri(),
                        gct.getGiay().getHinhAnh(),
                        gct.getGiaBan(),
                        ct.getGiaDonVi(),
                        ct.getSoLuong() == null ? 0 : ct.getSoLuong(),
                        ct.getThanhTien()
                ));
            }

            // Check return slip for this invoice
            Integer phieuTraHangId = null;
            Integer trangThaiTraHang = null;
            String trangThaiTraHangText = null;
            var phieuOpt = phieuTraHangRepository.findFirstByHoaDonIdOrderByNgayTaoDesc(hd.getId());
            if (phieuOpt.isPresent()) {
                PhieuTraHang phieu = phieuOpt.get();
                phieuTraHangId = phieu.getId();
                trangThaiTraHang = phieu.getTrangThai();
                trangThaiTraHangText = nhanTrangThaiTraHang(phieu.getTrangThai());
            }

            result.add(new DonHangTomTatResponse(
                    hd.getId(), hd.getMa(), hd.getNgayLap(),
                    hd.getTrangThai(), nhanTrangThai(hd.getTrangThai()),
                    soLuong, hd.getTongTienThanhToan(), sanPhams,
                    phieuTraHangId, trangThaiTraHang, trangThaiTraHangText,
                    hd.getNgayCapNhat()));
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
        BigDecimal phiVanChuyen = vanChuyenRepository.findByHoaDonId(hd.getId())
                .map(vc -> vc.getPhiVanChuyen() == null ? BigDecimal.ZERO : vc.getPhiVanChuyen())
                .orElse(BigDecimal.ZERO);
        String maPhieu = hd.getPhieuGiamGia() != null ? hd.getPhieuGiamGia().getMa() : null;

        // Check return slip for this invoice
        Integer phieuTraHangId = null;
        Integer trangThaiTraHang = null;
        String trangThaiTraHangText = null;
        List<LichSuTraHang> lichSuTraHang = List.of();
        var phieuOpt = phieuTraHangRepository.findFirstByHoaDonIdOrderByNgayTaoDesc(hd.getId());
        if (phieuOpt.isPresent()) {
            PhieuTraHang phieu = phieuOpt.get();
            phieuTraHangId = phieu.getId();
            trangThaiTraHang = phieu.getTrangThai();
            trangThaiTraHangText = nhanTrangThaiTraHang(phieu.getTrangThai());
            lichSuTraHang = lichSuPhieuTraHangRepository
                    .findByPhieuTraHangIdOrderByNgayTaoAsc(phieu.getId())
                    .stream()
                    .map(lichSu -> new LichSuTraHang(
                            lichSu.getTrangThaiMoi(),
                            lichSu.getNgayTao()
                    ))
                    .toList();
        }

        List<LichSuTrangThai> lichSuTrangThai = lichSuHoaDonRepository
                .findByHoaDonIdOrderByNgayTaoDesc(hd.getId())
                .stream()
                .map(lichSu -> new LichSuTrangThai(
                        lichSu.getTrangThai(),
                        lichSu.getNgayTao(),
                        lichSu.getNhanVien() != null ? lichSu.getNhanVien().getMa() : "Hệ thống"))
                .toList();

        return new DonHangChiTietResponse(
                hd.getId(), hd.getMa(), hd.getNgayLap(),
                hd.getTrangThai(), nhanTrangThai(hd.getTrangThai()),
                Boolean.TRUE.equals(hd.getDaNhanHang()),
                hd.getTenNguoiNhan(), hd.getSdtNguoiNhan(), hd.getDiaChiGiaoHang(),
                maPhieu, sanPhams,
                tamTinh, giamDot, giamVoucher, phiVanChuyen, hd.getTongTienThanhToan(),
                hd.getNgayCapNhat(), lichSuTrangThai,
                phieuTraHangId, trangThaiTraHang, trangThaiTraHangText, lichSuTraHang);
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
            case 9 -> "Đã xác nhận";
            case 2 -> "Chờ lấy hàng";
            case 3 -> "Đang giao hàng";
            case 4 -> "Đã giao hàng";
            case 5 -> "Hoàn thành";
            case 6 -> "Đã hủy";
            case 7 -> "Yêu cầu hủy";
            case 10 -> "Giao hàng thất bại";
            default -> "Không xác định";
        };
    }

    private String nhanTrangThaiTraHang(Integer trangThai) {
        if (trangThai == null) return null;
        return switch (trangThai) {
            case 1 -> "Chờ duyệt";
            case 2 -> "Chờ khách gửi hàng";
            case 3 -> "Đang hoàn hàng";
            case 4 -> "Đã nhận hàng";
            case 5 -> "Đang kiểm tra";
            case 6 -> "Chờ hoàn tiền";
            case 7 -> "Đã hoàn tiền";
            case 8 -> "Từ chối";
            case 9 -> "Đã hủy";
            case 10 -> "Hoàn hàng thất bại";
            default -> "Không xác định";
        };
    }
}
