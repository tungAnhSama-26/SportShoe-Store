package com.example.server.core.client.donhang.service;

import com.example.server.core.client.donhang.dto.CapNhatSoLuongRequest;
import com.example.server.core.client.donhang.dto.CapNhatSoLuongResponse;
import com.example.server.core.client.donhang.dto.DonHangChiTietResponse;
import com.example.server.core.client.donhang.dto.CapNhatThongTinGiaoHangRequest;
import com.example.server.core.client.donhang.dto.DonHangChiTietResponse.ChiTietTraHangItem;
import com.example.server.core.client.donhang.dto.DonHangChiTietResponse.DongSanPham;
import com.example.server.core.client.donhang.dto.DonHangChiTietResponse.LichSuTraHang;
import com.example.server.core.client.donhang.dto.DonHangChiTietResponse.LichSuTrangThai;
import com.example.server.core.client.donhang.dto.DonHangTomTatResponse;
import com.example.server.core.realtime.hoadon.HoaDonRealtimePublisher;
import com.example.server.core.admin.thongbao.service.ThongBaoService;
import com.example.server.entity.DanhGia;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HinhAnhTraHang;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.LichSuHoaDon;
import com.example.server.entity.PhieuTraHang;
import com.example.server.entity.PhieuTraHangChiTiet;
import com.example.server.entity.ThanhToan;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DanhGiaRepository;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import com.example.server.repository.HinhAnhTraHangRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.LichSuHoaDonRepository;
import com.example.server.repository.LichSuPhieuTraHangRepository;
import com.example.server.repository.PhieuTraHangChiTietRepository;
import com.example.server.repository.PhieuTraHangRepository;
import com.example.server.repository.ThanhToanRepository;
import com.example.server.repository.VanChuyenRepository;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;

@Service
public class ClientXemDonHangService {

    /** Trạng thái "giỏ hàng" (chưa đặt) - không hiện trong danh sách đơn. */
    private static final int TRANG_THAI_GIO = 0;

    /** Trạng thái đơn đã hoàn thành (giao xong) - mới được xác nhận nhận hàng / đánh giá. */
    private static final int TRANG_THAI_HOAN_THANH = 5;
    private static final int TRANG_THAI_DA_GIAO_HANG = 4;
    private static final int TRANG_THAI_CHO_XAC_NHAN = 1;
    private static final int TRANG_THAI_DA_XAC_NHAN = 9;
    private static final int TRANG_THAI_CHO_LAY_HANG = 2;
    private static final int TRANG_THAI_YEU_CAU_HUY = 7;
    private static final int TRANG_THAI_HUY = 6;
    private static final int TRANG_THAI_CAN_HOAN_TIEN = 8;

    /** Hình thức thanh toán: 3 = chuyển khoản (VietQR/SePay), 4 = COD (tiền mặt). */
    private static final int HINH_THUC_CHUYEN_KHOAN = 3;
    private static final int LOAI_GIAO_DICH_THANH_TOAN = 1;
    private static final int TT_THANH_TOAN_CHO = 0;
    private static final int TT_THANH_TOAN_THANH_CONG = 1;
    private static final int TT_THANH_TOAN_DA_HUY = 3;
    private static final int TT_THANH_TOAN_CAN_HOAN_TIEN = 4;

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final DanhGiaRepository danhGiaRepository;
    private final PhieuTraHangRepository phieuTraHangRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final VanChuyenRepository vanChuyenRepository;
    private final LichSuPhieuTraHangRepository lichSuPhieuTraHangRepository;
    private final HoaDonRealtimePublisher hoaDonRealtimePublisher;
    private final HinhAnhTraHangRepository hinhAnhTraHangRepository;
    private final PhieuTraHangChiTietRepository phieuTraHangChiTietRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final HinhAnhGiayRepository hinhAnhGiayRepository;
    private final ThongBaoService thongBaoService;
    private final GhnShippingService ghnShippingService;

    public ClientXemDonHangService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            DanhGiaRepository danhGiaRepository,
            PhieuTraHangRepository phieuTraHangRepository,
            LichSuHoaDonRepository lichSuHoaDonRepository,
            VanChuyenRepository vanChuyenRepository,
            LichSuPhieuTraHangRepository lichSuPhieuTraHangRepository,
            HoaDonRealtimePublisher hoaDonRealtimePublisher,
            HinhAnhTraHangRepository hinhAnhTraHangRepository,
            PhieuTraHangChiTietRepository phieuTraHangChiTietRepository,
            ThanhToanRepository thanhToanRepository,
            GiayChiTietRepository giayChiTietRepository,
            HinhAnhGiayRepository hinhAnhGiayRepository,
            ThongBaoService thongBaoService,
            GhnShippingService ghnShippingService
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.danhGiaRepository = danhGiaRepository;
        this.phieuTraHangRepository = phieuTraHangRepository;
        this.lichSuHoaDonRepository = lichSuHoaDonRepository;
        this.vanChuyenRepository = vanChuyenRepository;
        this.lichSuPhieuTraHangRepository = lichSuPhieuTraHangRepository;
        this.hoaDonRealtimePublisher = hoaDonRealtimePublisher;
        this.hinhAnhTraHangRepository = hinhAnhTraHangRepository;
        this.phieuTraHangChiTietRepository = phieuTraHangChiTietRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
        this.thongBaoService = thongBaoService;
        this.ghnShippingService = ghnShippingService;
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

            Map<Integer, String> anhBienThe = mapAnhBienTheTheoDong(dong);
            List<DonHangTomTatResponse.DongSanPhamTomTat> sanPhams = new ArrayList<>();
            for (HoaDonChiTiet ct : dong) {
                GiayChiTiet gct = ct.getGiayChiTiet();
                sanPhams.add(new DonHangTomTatResponse.DongSanPhamTomTat(
                        ct.getId(),
                        gct.getId(),
                        gct.getGiay().getTen(),
                        gct.getMauSac().getTen(),
                        gct.getKichCo().getGiaTri(),
                        anhBienThe.getOrDefault(gct.getId(), gct.getGiay().getHinhAnh()),
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

            int virtualStatus = hd.getTrangThai();
            String virtualStatusText = nhanTrangThai(hd.getTrangThai());
            if (hd.getTrangThai() == TRANG_THAI_HUY && laCanHoanTien(hd.getId())) {
                virtualStatus = TRANG_THAI_CAN_HOAN_TIEN;
                virtualStatusText = "Cần hoàn tiền";
            }

            Instant ngayGiaoThat = vanChuyenRepository.findByHoaDonId(hd.getId())
                    .map(com.example.server.entity.VanChuyen::getNgayGiaoThat)
                    .orElse(null);
            Instant ngayThanhToan = hd.getNgayThanhToan();
            Instant ngayGiao = ngayGiaoThat;
            if (ngayGiao != null && ngayThanhToan != null) {
                if (ngayThanhToan.isAfter(ngayGiao)) {
                    ngayGiao = ngayThanhToan;
                }
            } else if (ngayGiao == null) {
                ngayGiao = ngayThanhToan;
            }

            result.add(new DonHangTomTatResponse(
                    hd.getId(), hd.getMa(), hd.getNgayLap(),
                    virtualStatus, virtualStatusText,
                    soLuong, hd.getTongTienThanhToan(), sanPhams,
                    phieuTraHangId, trangThaiTraHang, trangThaiTraHangText,
                    hd.getNgayCapNhat(), ngayGiao));
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
        return buildChiTiet(hd);
    }

    /** Tra cứu đơn hàng theo mã hóa đơn - công khai, không cần đăng nhập (cho cả khách vãng lai). */
    @Transactional(readOnly = true)
    public DonHangChiTietResponse traCuuTheoMa(String ma) {
        if (ma == null || ma.isBlank()) {
            throw new BusinessException("Vui lòng nhập mã hóa đơn");
        }
        HoaDon hd = hoaDonRepository.findDetailByMa(ma.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn hàng với mã \"" + ma.trim() + "\""));
        return buildChiTiet(hd);
    }

    /** Map biến thể -> URL ảnh chính của biến thể đó (ảnh đúng màu/loại khách đã mua). */
    private Map<Integer, String> mapAnhBienTheTheoDong(List<HoaDonChiTiet> dong) {
        List<Integer> bienTheIds = dong.stream()
                .map(ct -> ct.getGiayChiTiet().getId())
                .distinct().toList();
        Map<Integer, String> anh = new HashMap<>();
        if (!bienTheIds.isEmpty()) {
            for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayChiTietIds(bienTheIds)) {
                anh.putIfAbsent((Integer) row[0], (String) row[1]);
            }
        }
        return anh;
    }

    /** Dựng chi tiết đơn từ HoaDon (dùng chung cho xem đơn của khách + tra cứu công khai). */
    private DonHangChiTietResponse buildChiTiet(HoaDon hd) {
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

        // Ảnh chính của từng biến thể (ảnh đúng màu/loại khách đã mua, không phải ảnh gốc SP).
        Map<Integer, String> anhBienThe = mapAnhBienTheTheoDong(dong);

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
                    anhBienThe.getOrDefault(gct.getId(), gct.getGiay().getHinhAnh()),
                    giaNiemYet, giaDonVi, sl, ct.getThanhTien(),
                    dg != null,
                    dg != null ? dg.getSoSao() : null,
                    dg != null ? dg.getNoiDung() : null,
                    dg != null ? dg.getPhanHoi() : null,
                    dg != null ? dg.getNgayPhanHoi() : null));
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
        String lyDoTraHangMa = null;
        String lyDoTraHangMoTa = null;
        BigDecimal tongTienDuKienTra = null;
        BigDecimal tongTienThucTeTra = null;
        List<String> hinhAnhTraHang = List.of();
        List<ChiTietTraHangItem> chiTietTraHang = List.of();

        var phieuOpt = phieuTraHangRepository.findFirstByHoaDonIdOrderByNgayTaoDesc(hd.getId());
        if (phieuOpt.isPresent()) {
            PhieuTraHang phieu = phieuOpt.get();
            phieuTraHangId = phieu.getId();
            trangThaiTraHang = phieu.getTrangThai();
            trangThaiTraHangText = nhanTrangThaiTraHang(phieu.getTrangThai());
            lyDoTraHangMa = phieu.getLyDoMa();
            lyDoTraHangMoTa = phieu.getMoTa();
            tongTienDuKienTra = phieu.getTongTienDuKien();
            tongTienThucTeTra = phieu.getTongTienThucTe();
            hinhAnhTraHang = hinhAnhTraHangRepository
                    .findByPhieuTraHangIdOrderByNgayTaoAsc(phieu.getId())
                    .stream()
                    .map(HinhAnhTraHang::getUrl)
                    .toList();
            chiTietTraHang = phieuTraHangChiTietRepository
                    .findByPhieuTraHangIdOrderByIdAsc(phieu.getId())
                    .stream()
                    .map(ct -> new ChiTietTraHangItem(
                            ct.getHoaDonChiTiet() != null ? ct.getHoaDonChiTiet().getId() : null,
                            ct.getSoLuongTra(),
                            ct.getSoLuongChapNhan(),
                            ct.getGiaBan(),
                            ct.getSoTienHoan()
                    ))
                    .toList();
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
                        lichSu.getNhanVien() != null ? lichSu.getNhanVien().getMa() : (lichSu.getNguoiThaoTac() != null ? lichSu.getNguoiThaoTac() : "Khách hàng"),
                        lichSu.getGhiChu()))
                .toList();

        boolean laCK = laChuyenKhoan(hd.getId());
        boolean dangChoXacNhan = hd.getTrangThai() != null
                && hd.getTrangThai() == TRANG_THAI_CHO_XAC_NHAN;
        boolean coTheSua = dangChoXacNhan && !laCK && (hd.getSoLanSuaDiaChi() == null || hd.getSoLanSuaDiaChi() < 1);

        int virtualStatus = hd.getTrangThai();
        String virtualStatusText = nhanTrangThai(hd.getTrangThai());
        if (hd.getTrangThai() == TRANG_THAI_HUY && laCanHoanTien(hd.getId())) {
            virtualStatus = TRANG_THAI_CAN_HOAN_TIEN;
            virtualStatusText = "Cần hoàn tiền";
        }

        Instant ngayGiaoThat = vanChuyenRepository.findByHoaDonId(hd.getId())
                .map(com.example.server.entity.VanChuyen::getNgayGiaoThat)
                .orElse(null);
        Instant ngayThanhToan = hd.getNgayThanhToan();
        Instant ngayGiao = ngayGiaoThat;
        if (ngayGiao != null && ngayThanhToan != null) {
            if (ngayThanhToan.isAfter(ngayGiao)) {
                ngayGiao = ngayThanhToan;
            }
        } else if (ngayGiao == null) {
            ngayGiao = ngayThanhToan;
        }

        return new DonHangChiTietResponse(
                hd.getId(), hd.getMa(), hd.getNgayLap(),
                virtualStatus, virtualStatusText,
                Boolean.TRUE.equals(hd.getDaNhanHang()),
                hd.getTenNguoiNhan(), hd.getSdtNguoiNhan(), hd.getDiaChiGiaoHang(),
                maPhieu, sanPhams,
                tamTinh, giamDot, giamVoucher, phiVanChuyen, hd.getTongTienThanhToan(),
                hd.getNgayCapNhat(), lichSuTrangThai,
                phieuTraHangId, trangThaiTraHang, trangThaiTraHangText, lichSuTraHang,
                lyDoTraHangMa, lyDoTraHangMoTa, tongTienDuKienTra, tongTienThucTeTra,
                hinhAnhTraHang, chiTietTraHang,
                laCK ? "CHUYEN_KHOAN" : "COD",
                // Khách KHÔNG được phép sửa số lượng sản phẩm (chỉ còn sửa thông tin giao hàng + hủy).
                dangChoXacNhan, coTheSua, false, ngayGiao,
                hd.getSoLanSuaDiaChi() != null ? hd.getSoLanSuaDiaChi() : 0);
    }

    private boolean coThanhToanThanhCong(HoaDon hoaDon) {
        return thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId()).stream()
                .anyMatch(thanhToan ->
                        Objects.equals(thanhToan.getLoaiGiaoDich(), LOAI_GIAO_DICH_THANH_TOAN)
                                && Objects.equals(
                                thanhToan.getTrangThai(),
                                TT_THANH_TOAN_THANH_CONG
                        )
                );
    }

    /** Khách xác nhận đã nhận hàng (đơn phải ở trạng thái Đã giao hàng). */
    @Transactional
    public void xacNhanDaNhanHang(UUID khachHangId, Integer id) {
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        if (hd.getKhachHang() == null || !hd.getKhachHang().getId().equals(khachHangId)) {
            throw new BusinessException("Bạn không có quyền thao tác đơn hàng này");
        }
        if (hd.getTrangThai() == null || hd.getTrangThai() != TRANG_THAI_DA_GIAO_HANG) {
            throw new BusinessException("Đơn hàng chưa ở trạng thái đã giao hàng, chưa thể xác nhận đã nhận hàng");
        }
        
        Instant now = Instant.now();
        hd.setDaNhanHang(true);
        
        // Kiểm tra xem đơn đã có giao dịch thanh toán thành công hay chưa
        boolean daThanhToan = coThanhToanThanhCong(hd);
        if (daThanhToan) {
            hd.setTrangThai(TRANG_THAI_HOAN_THANH);
            hd.setNgayCapNhat(now);
            hoaDonRepository.save(hd);
            
            // Ghi lịch sử hóa đơn: Hoàn thành
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hd);
            lichSu.setNhanVien(null);
            lichSu.setTrangThai("Hoàn thành");
            lichSu.setGhiChu("Khách hàng xác nhận đã nhận hàng và đơn đã được thanh toán");
            lichSu.setNgayTao(now);
            lichSuHoaDonRepository.save(lichSu);
            
            hoaDonRealtimePublisher.publishAfterCommit(hd, "TRANG_THAI");
        } else {
            hd.setNgayCapNhat(now);
            hoaDonRepository.save(hd);
            
            // Ghi lịch sử hóa đơn: Khách hàng xác nhận nhận hàng, chờ thanh toán
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hd);
            lichSu.setNhanVien(null);
            lichSu.setTrangThai("Đã giao hàng");
            lichSu.setGhiChu("Khách hàng xác nhận đã nhận hàng (Đang chờ xác nhận thanh toán)");
            lichSu.setNgayTao(now);
            lichSuHoaDonRepository.save(lichSu);
        }
        
        hoaDonRealtimePublisher.publishAfterCommit(hd, "DA_NHAN_HANG");
    }

    /**
     * Khách tự hủy đơn. Theo nghiệp vụ chỉ cho hủy khi đơn đang "Chờ xác nhận"
     * (áp dụng cho cả COD lẫn chuyển khoản). Nếu đơn đã thanh toán (chuyển khoản)
     * thì đánh dấu giao dịch CẦN HOÀN TIỀN để nhân viên xác nhận hoàn phí ở màn QLHD.
     */
    @Transactional
    public void huyDon(UUID khachHangId, Integer id) {
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        if (hd.getKhachHang() == null || !hd.getKhachHang().getId().equals(khachHangId)) {
            throw new BusinessException("Bạn không có quyền thao tác đơn hàng này");
        }
        if (hd.getTrangThai() == null || hd.getTrangThai() != TRANG_THAI_CHO_XAC_NHAN) {
            throw new BusinessException("Chỉ có thể hủy đơn khi đơn đang chờ xác nhận");
        }

        hoanKhoNeuDaTru(hd);
        boolean canHoanTien = capNhatThanhToanKhiHuyDon(hd);

        hd.setTrangThai(TRANG_THAI_HUY);
        hd.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hd);

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hd);
        lichSu.setNhanVien(null);
        lichSu.setTrangThai("Hủy");
        lichSu.setGhiChu(canHoanTien
                ? "Khách hàng hủy đơn (đã thanh toán) - cần hoàn tiền"
                : "Khách hàng hủy đơn");
        lichSu.setNgayTao(Instant.now());
        lichSuHoaDonRepository.save(lichSu);
        hoaDonRealtimePublisher.publishAfterCommit(hd, "HUY");

        // Trigger cancel order notification
        try {
            String title = canHoanTien ? "Yêu cầu hoàn tiền đơn hủy" : "Đơn hàng online đã hủy";
            String text = canHoanTien 
                    ? "Khách hàng \"" + hd.getTenNguoiNhan() + "\" đã hủy đơn hàng #" + hd.getMa() + " (đã thanh toán). Cần hoàn tiền!"
                    : "Khách hàng \"" + hd.getTenNguoiNhan() + "\" đã hủy đơn hàng #" + hd.getMa() + ".";
            String type = canHoanTien ? "REFUND" : "CANCEL";
            thongBaoService.taoThongBao(title, text, type, "/admin/hoa-don/" + hd.getId());
        } catch (Exception e) {
            System.err.println("[ClientXemDonHangService] Lỗi tạo thông báo hủy đơn: " + e.getMessage());
        }
    }

    @Transactional
    public DonHangChiTietResponse capNhatThongTinGiaoHang(
            UUID khachHangId,
            Integer id,
            CapNhatThongTinGiaoHangRequest request
    ) {
        HoaDon hd = hoaDonRepository.findDetailByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        if (hd.getKhachHang() == null || !hd.getKhachHang().getId().equals(khachHangId)) {
            throw new BusinessException("Bạn không có quyền cập nhật đơn hàng này");
        }

        if (laChuyenKhoan(id)) {
            throw new BusinessException(
                    "Đơn thanh toán chuyển khoản không được phép chỉnh sửa thông tin giao hàng");
        }
        if (hd.getTrangThai() == null || hd.getTrangThai() != TRANG_THAI_CHO_XAC_NHAN) {
            throw new BusinessException(
                    "Chỉ có thể cập nhật thông tin giao hàng khi đơn đang chờ xác nhận");
        }
        if (hd.getSoLanSuaDiaChi() != null && hd.getSoLanSuaDiaChi() >= 1) {
            throw new BusinessException("Bạn chỉ được phép chỉnh sửa thông tin giao hàng tối đa 1 lần.");
        }

        BigDecimal phiShipCu = BigDecimal.ZERO;
        var vcOpt = vanChuyenRepository.findByHoaDonId(id);
        if (vcOpt.isPresent()) {
            phiShipCu = vcOpt.get().getPhiVanChuyen() == null ? BigDecimal.ZERO : vcOpt.get().getPhiVanChuyen();
        }

        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(id);
        BigDecimal phiShipMoi = phiShipCu;
        if (!items.isEmpty()) {
            try {
                TinhPhiVanChuyenGhnRequest ghnReq = new TinhPhiVanChuyenGhnRequest(
                        null, null, request.diaChiGiaoHang().trim(), null, null, null, null, null, null, null, null
                );
                TinhPhiVanChuyenGhnResponse phiGhn = ghnShippingService.tinhPhi(hd, items, ghnReq);
                phiShipMoi = phiGhn.phiVanChuyen();
            } catch (Exception e) {
                System.err.println("[ClientXemDonHangService] Lỗi tính phí vận chuyển GHN mới: " + e.getMessage());
            }
        }

        com.example.server.entity.VanChuyen vanChuyen = vcOpt.orElseGet(() -> {
            com.example.server.entity.VanChuyen created = new com.example.server.entity.VanChuyen();
            created.setHoaDon(hd);
            created.setDonViVanChuyen("GHN");
            created.setTrangThai(0); // Cho_XU_LY
            created.setNgayTao(Instant.now());
            return created;
        });
        vanChuyen.setPhiVanChuyen(phiShipMoi);
        vanChuyen.setNgayCapNhat(Instant.now());
        vanChuyenRepository.save(vanChuyen);

        StringBuilder ghiChuHistory = new StringBuilder("Khách hàng cập nhật thông tin giao hàng:\n");
        String tenCu = hd.getTenNguoiNhan() == null ? "" : hd.getTenNguoiNhan();
        String tenMoi = request.tenNguoiNhan().trim();
        if (!tenCu.equals(tenMoi)) {
            ghiChuHistory.append("- Tên người nhận: '").append(tenCu).append("' -> '").append(tenMoi).append("'\n");
        }

        String sdtCu = hd.getSdtNguoiNhan() == null ? "" : hd.getSdtNguoiNhan();
        String sdtMoi = request.sdtNguoiNhan().trim();
        if (!sdtCu.equals(sdtMoi)) {
            ghiChuHistory.append("- SĐT: '").append(sdtCu).append("' -> '").append(sdtMoi).append("'\n");
        }

        String dcCu = hd.getDiaChiGiaoHang() == null ? "" : hd.getDiaChiGiaoHang();
        String dcMoi = request.diaChiGiaoHang().trim();
        if (!dcCu.equals(dcMoi)) {
            ghiChuHistory.append("- Địa chỉ: '").append(dcCu).append("' -> '").append(dcMoi).append("'\n");
        }

        if (phiShipCu.compareTo(phiShipMoi) != 0) {
            ghiChuHistory.append("- Phí ship: '").append(phiShipCu.intValue()).append("đ' -> '").append(phiShipMoi.intValue()).append("đ'\n");
        }

        String finalGhiChu = ghiChuHistory.toString();
        if (finalGhiChu.equals("Khách hàng cập nhật thông tin giao hàng:\n")) {
            finalGhiChu = "Khách hàng cập nhật người nhận và địa chỉ giao hàng";
        } else if (finalGhiChu.length() > 1000) {
            finalGhiChu = finalGhiChu.substring(0, 995) + "...";
        }

        hd.setTenNguoiNhan(request.tenNguoiNhan().trim());
        hd.setSdtNguoiNhan(request.sdtNguoiNhan().trim());
        hd.setDiaChiGiaoHang(request.diaChiGiaoHang().trim());
        hd.setSoLanSuaDiaChi((hd.getSoLanSuaDiaChi() == null ? 0 : hd.getSoLanSuaDiaChi()) + 1);

        BigDecimal tongHang = hd.getTongTienHang() == null ? BigDecimal.ZERO : hd.getTongTienHang();
        BigDecimal tienGiam = hd.getTienGiam() == null ? BigDecimal.ZERO : hd.getTienGiam();
        hd.setTongTienThanhToan(tongHang.add(phiShipMoi).subtract(tienGiam).max(BigDecimal.ZERO));

        hd.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hd);

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hd);
        lichSu.setNhanVien(null);
        lichSu.setNguoiThaoTac("Khách hàng");
        lichSu.setTrangThai("Cập nhật thông tin giao hàng");
        lichSu.setGhiChu(finalGhiChu);
        lichSu.setNgayTao(Instant.now());
        lichSuHoaDonRepository.save(lichSu);

        hoaDonRealtimePublisher.publishAfterCommit(hd, "THONG_TIN_GIAO_HANG");
        return chiTiet(khachHangId, id);
    }

    /**
     * Khách cập nhật số lượng sản phẩm trong đơn (chỉ COD + đang chờ xác nhận).
     * Danh sách {@code items} là các dòng giữ lại (số lượng &gt;= 1); dòng cũ vắng mặt sẽ bị xóa;
     * đơn không được rỗng. Mục 6: nếu giá biến thể đã đổi so với lúc đặt, cập nhật về giá hiện
     * tại và ghi lịch sử "giá đổi từ X → Y".
     */
    @Transactional
    public CapNhatSoLuongResponse capNhatSoLuong(
            UUID khachHangId, Integer id, CapNhatSoLuongRequest request) {
        HoaDon hd = hoaDonRepository.findDetailByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        if (hd.getKhachHang() == null || !hd.getKhachHang().getId().equals(khachHangId)) {
            throw new BusinessException("Bạn không có quyền cập nhật đơn hàng này");
        }
        if (laChuyenKhoan(id)) {
            throw new BusinessException("Đơn thanh toán chuyển khoản không được phép sửa số lượng");
        }
        if (hd.getTrangThai() == null || hd.getTrangThai() != TRANG_THAI_CHO_XAC_NHAN) {
            throw new BusinessException("Chỉ có thể sửa số lượng khi đơn đang chờ xác nhận");
        }

        long soDong = request.items().stream()
                .map(CapNhatSoLuongRequest.Dong::hoaDonChiTietId).distinct().count();
        if (soDong != request.items().size()) {
            throw new BusinessException("Danh sách sản phẩm bị trùng dòng");
        }

        Map<Integer, HoaDonChiTiet> theoId = hoaDonChiTietRepository.findByHoaDonId(id).stream()
                .collect(Collectors.toMap(HoaDonChiTiet::getId, ct -> ct));

        boolean daTru = Boolean.TRUE.equals(hd.getDaTruKho());
        List<String> doiGia = new ArrayList<>();
        BigDecimal tongTienHang = BigDecimal.ZERO;
        Set<Integer> giuLai = new HashSet<>();

        for (CapNhatSoLuongRequest.Dong dong : request.items()) {
            HoaDonChiTiet ct = theoId.get(dong.hoaDonChiTietId());
            if (ct == null) {
                throw new BusinessException("Dòng sản phẩm không thuộc đơn hàng này");
            }
            GiayChiTiet gct = giayChiTietRepository.findByIdForUpdate(ct.getGiayChiTiet().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy biến thể sản phẩm: " + ct.getGiayChiTiet().getId()));
            int qtyCu = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
            int qtyMoi = dong.soLuong();
            int ton = gct.getSoLuong() == null ? 0 : gct.getSoLuong();

            // Đơn online chờ xác nhận thường CHƯA trừ kho -> chỉ kiểm tra đủ tồn.
            if (daTru) {
                int diff = qtyMoi - qtyCu;
                if (diff > 0 && ton < diff) {
                    throw new BusinessException("Số lượng tồn không đủ cho sản phẩm: " + gct.getGiay().getTen());
                }
                gct.setSoLuong(ton - diff);
                giayChiTietRepository.save(gct);
            } else if (qtyMoi > ton) {
                throw new BusinessException("Số lượng tồn không đủ cho sản phẩm: " + gct.getGiay().getTen());
            }

            // Mục 6: giá biến thể đã đổi so với lúc đặt -> cập nhật giá hiện tại + ghi lại.
            BigDecimal giaCu = ct.getGiaDonVi();
            BigDecimal giaMoi = gct.getGiaBan();
            if (giaCu != null && giaMoi != null && giaCu.compareTo(giaMoi) != 0) {
                doiGia.add(gct.getGiay().getTen() + " (" + gct.getMauSac().getTen() + "/"
                        + gct.getKichCo().getGiaTri() + "): " + tien(giaCu) + " → " + tien(giaMoi));
            }

            ct.setSoLuong(qtyMoi);
            ct.setGiaDonVi(giaMoi);
            ct.setThanhTien(giaMoi.multiply(BigDecimal.valueOf(qtyMoi)));
            hoaDonChiTietRepository.save(ct);
            tongTienHang = tongTienHang.add(ct.getThanhTien());
            giuLai.add(ct.getId());
        }

        // Xóa các dòng không còn giữ lại (hoàn kho nếu đơn đã bị trừ).
        for (HoaDonChiTiet ct : theoId.values()) {
            if (giuLai.contains(ct.getId())) {
                continue;
            }
            if (daTru) {
                GiayChiTiet gct = giayChiTietRepository.findByIdForUpdate(ct.getGiayChiTiet().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy biến thể sản phẩm: " + ct.getGiayChiTiet().getId()));
                gct.setSoLuong((gct.getSoLuong() == null ? 0 : gct.getSoLuong())
                        + (ct.getSoLuong() == null ? 0 : ct.getSoLuong()));
                giayChiTietRepository.save(gct);
            }
            hoaDonChiTietRepository.delete(ct);
        }

        // Tính lại tổng: giữ voucher (kẹp không vượt tiền hàng) + giữ phí ship.
        BigDecimal tienGiam = (hd.getTienGiam() == null ? BigDecimal.ZERO : hd.getTienGiam())
                .min(tongTienHang);
        BigDecimal phiShip = vanChuyenRepository.findByHoaDonId(hd.getId())
                .map(vc -> vc.getPhiVanChuyen() == null ? BigDecimal.ZERO : vc.getPhiVanChuyen())
                .orElse(BigDecimal.ZERO);
        hd.setTongTienHang(tongTienHang);
        hd.setTienGiam(tienGiam);
        hd.setTongTienThanhToan(tongTienHang.subtract(tienGiam).max(BigDecimal.ZERO).add(phiShip));
        hd.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hd);

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hd);
        lichSu.setNhanVien(null);
        lichSu.setTrangThai("Cập nhật số lượng");
        lichSu.setGhiChu(doiGia.isEmpty()
                ? "Khách hàng cập nhật số lượng sản phẩm"
                : "Khách cập nhật số lượng. Giá đổi: " + String.join("; ", doiGia));
        lichSu.setNgayTao(Instant.now());
        lichSuHoaDonRepository.save(lichSu);

        hoaDonRealtimePublisher.publishAfterCommit(hd, "SAN_PHAM");
        return new CapNhatSoLuongResponse(chiTiet(khachHangId, id), doiGia);
    }

    private String tien(BigDecimal v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + "đ";
    }

    /** Đơn thanh toán bằng chuyển khoản (VietQR/SePay)? Dựa trên giao dịch thanh toán gốc. */
    private boolean laChuyenKhoan(Integer hoaDonId) {
        return thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDonId).stream()
                .filter(tt -> Objects.equals(tt.getLoaiGiaoDich(), LOAI_GIAO_DICH_THANH_TOAN))
                .anyMatch(tt -> Objects.equals(tt.getHinhThuc(), HINH_THUC_CHUYEN_KHOAN));
    }

    /** Hoàn lại tồn kho nếu đơn online đã bị trừ kho (đơn chờ xác nhận thường chưa trừ). */
    private void hoanKhoNeuDaTru(HoaDon hd) {
        if (!Boolean.TRUE.equals(hd.getDaTruKho())) {
            return;
        }
        for (HoaDonChiTiet ct : hoaDonChiTietRepository.findByHoaDonId(hd.getId())) {
            GiayChiTiet gct = giayChiTietRepository.findByIdForUpdate(ct.getGiayChiTiet().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy biến thể sản phẩm: " + ct.getGiayChiTiet().getId()));
            int ton = gct.getSoLuong() == null ? 0 : gct.getSoLuong();
            gct.setSoLuong(ton + (ct.getSoLuong() == null ? 0 : ct.getSoLuong()));
            giayChiTietRepository.save(gct);
        }
        hd.setDaTruKho(false);
    }

    /**
     * Cập nhật giao dịch thanh toán khi hủy đơn:
     * - đang chờ thanh toán (COD) -&gt; đã hủy;
     * - đã thanh toán (chuyển khoản) -&gt; cần hoàn tiền.
     * @return true nếu có giao dịch cần hoàn tiền.
     */
    private boolean capNhatThanhToanKhiHuyDon(HoaDon hd) {
        boolean canHoanTien = false;
        for (ThanhToan tt : thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hd.getId())) {
            if (!Objects.equals(tt.getLoaiGiaoDich(), LOAI_GIAO_DICH_THANH_TOAN)) {
                continue;
            }
            if (Objects.equals(tt.getTrangThai(), TT_THANH_TOAN_CHO)) {
                tt.setTrangThai(TT_THANH_TOAN_DA_HUY);
                tt.setGhiChu(noiGhiChu(tt, "Đã hủy do khách hủy đơn"));
                thanhToanRepository.save(tt);
            } else if (Objects.equals(tt.getTrangThai(), TT_THANH_TOAN_THANH_CONG)) {
                tt.setTrangThai(TT_THANH_TOAN_CAN_HOAN_TIEN);
                tt.setGhiChu(noiGhiChu(tt, "Khách hủy đơn sau khi đã thanh toán, cần hoàn tiền"));
                thanhToanRepository.save(tt);
                canHoanTien = true;
            } else if (Objects.equals(tt.getTrangThai(), TT_THANH_TOAN_CAN_HOAN_TIEN)) {
                canHoanTien = true;
            }
        }
        return canHoanTien;
    }

    private String noiGhiChu(ThanhToan tt, String moi) {
        String cu = tt.getGhiChu();
        return (cu == null || cu.isBlank()) ? moi : cu + " | " + moi;
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
            case 8 -> "Cần hoàn tiền";
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

    private boolean laCanHoanTien(Integer hoaDonId) {
        return thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDonId)
                .stream()
                .anyMatch(tt -> Objects.equals(tt.getTrangThai(), TT_THANH_TOAN_CAN_HOAN_TIEN));
    }
}
