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

@Service
public class ClientXemDonHangService {

    /** Trạng thái "giỏ hàng" (chưa đặt) - không hiện trong danh sách đơn. */
    private static final int TRANG_THAI_GIO = 0;

    /** Trạng thái đơn đã hoàn thành (giao xong) - mới được xác nhận nhận hàng / đánh giá. */
    private static final int TRANG_THAI_HOAN_THANH = 5;
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
            GiayChiTietRepository giayChiTietRepository
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

            int virtualStatus = hd.getTrangThai();
            String virtualStatusText = nhanTrangThai(hd.getTrangThai());
            if (hd.getTrangThai() == TRANG_THAI_HUY && laCanHoanTien(hd.getId())) {
                virtualStatus = TRANG_THAI_CAN_HOAN_TIEN;
                virtualStatusText = "Cần hoàn tiền";
            }

            result.add(new DonHangTomTatResponse(
                    hd.getId(), hd.getMa(), hd.getNgayLap(),
                    virtualStatus, virtualStatusText,
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
                        lichSu.getNhanVien() != null ? lichSu.getNhanVien().getMa() : "Khách hàng"))
                .toList();

        boolean laCK = laChuyenKhoan(hd.getId());
        boolean dangChoXacNhan = hd.getTrangThai() != null
                && hd.getTrangThai() == TRANG_THAI_CHO_XAC_NHAN;
        boolean coTheSua = dangChoXacNhan && !laCK;

        int virtualStatus = hd.getTrangThai();
        String virtualStatusText = nhanTrangThai(hd.getTrangThai());
        if (hd.getTrangThai() == TRANG_THAI_HUY && laCanHoanTien(hd.getId())) {
            virtualStatus = TRANG_THAI_CAN_HOAN_TIEN;
            virtualStatusText = "Cần hoàn tiền";
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
                dangChoXacNhan, coTheSua, false);
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

        hd.setTenNguoiNhan(request.tenNguoiNhan().trim());
        hd.setSdtNguoiNhan(request.sdtNguoiNhan().trim());
        hd.setDiaChiGiaoHang(request.diaChiGiaoHang().trim());
        hd.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hd);

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hd);
        lichSu.setNhanVien(null);
        lichSu.setTrangThai("Cập nhật thông tin giao hàng");
        lichSu.setGhiChu("Khách hàng cập nhật người nhận và địa chỉ giao hàng");
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
