package com.example.server.core.admin.quanlyhoadon.service.impl;

import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatSanPhamHoaDonRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatTrangThaiHoaDonRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.XacNhanHoanTienRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.XacNhanThanhToanCodRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonDetailResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonHistoryResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonPaymentHistoryResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonProductResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonSummaryResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.core.admin.quanlyhoadon.service.QuanLyHoaDonService;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HinhAnhGiay;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.LichSuHoaDon;
import com.example.server.entity.NhanVien;
import com.example.server.entity.ThanhToan;
import com.example.server.entity.VanChuyen;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.security.AdminPrincipal;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.LichSuHoaDonRepository;
import com.example.server.repository.NhanVienRepository;
import com.example.server.repository.ThanhToanRepository;
import com.example.server.repository.VanChuyenRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuanLyHoaDonServiceImpl implements QuanLyHoaDonService {

    private static final int KENH_BAN_TAI_QUAY = 1;
    private static final int TRANG_THAI_CHO_XAC_NHAN = 1;
    private static final int TRANG_THAI_DA_XAC_NHAN = 9;
    private static final int TRANG_THAI_CHO_GIAO_HANG = 2;
    private static final int TRANG_THAI_DANG_VAN_CHUYEN = 3;
    private static final int TRANG_THAI_DA_GIAO_HANG = 4;
    private static final int TRANG_THAI_HOAN_THANH = 5;
    private static final int TRANG_THAI_HUY = 6;
    private static final int TRANG_THAI_YEU_CAU_HUY = 7;
    private static final int TRANG_THAI_CAN_HOAN_TIEN = 8;
    private static final int TRANG_THAI_GIAO_HANG_THAT_BAI = 10;
    
    private static final int TRANG_THAI_VAN_CHUYEN_CHO_XU_LY = 1;
    private static final int TRANG_THAI_VAN_CHUYEN_DANG_GIAO = 2;
    private static final int TRANG_THAI_VAN_CHUYEN_HOAN_THANH = 3;
    private static final int TRANG_THAI_VAN_CHUYEN_GIAO_THAT_BAI = 4;
    private static final int HINH_THUC_THANH_TOAN_TIEN_MAT = 1;
    private static final int HINH_THUC_THANH_TOAN_CHUYEN_KHOAN = 2;
    private static final int HINH_THUC_THANH_TOAN_VI = 3;
    private static final int HINH_THUC_THANH_TOAN_COD = 4;
    private static final int TRANG_THAI_THANH_TOAN_CHO_THANH_TOAN = 0;
    private static final int TRANG_THAI_THANH_TOAN_THANH_CONG = 1;
    private static final int TRANG_THAI_THANH_TOAN_THAT_BAI = 2;
    private static final int TRANG_THAI_THANH_TOAN_DA_HUY = 3;
    private static final int TRANG_THAI_THANH_TOAN_CAN_HOAN_TIEN = 4;
    private static final int TRANG_THAI_THANH_TOAN_DA_HOAN_TIEN = 5;
    private static final int TRANG_THAI_HINH_ANH_HOAT_DONG = 1;
    private static final String DIA_CHI_TAI_QUAY = "Mua tại quầy";
    private static final String DIA_CHI_TAI_QUAY_KHONG_DAU = "Mua tại quầy";
    private static final String GHI_CHU_TAO_HOA_DON_TAI_QUAY = "Hóa đơn chờ tạo từ màn hình bán hàng tại quầy";
    private static final String KHACH_VANG_LAI = "Khách vãng lai";
    private static final String KHONG_CO = "Không có";

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final VanChuyenRepository vanChuyenRepository;
    private final HinhAnhGiayRepository hinhAnhGiayRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final NhanVienRepository nhanVienRepository;
    private final GhnShippingService ghnShippingService;

    public QuanLyHoaDonServiceImpl(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanRepository thanhToanRepository,
            VanChuyenRepository vanChuyenRepository,
            HinhAnhGiayRepository hinhAnhGiayRepository,
            LichSuHoaDonRepository lichSuHoaDonRepository,
            GiayChiTietRepository giayChiTietRepository,
            NhanVienRepository nhanVienRepository,
            GhnShippingService ghnShippingService
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.vanChuyenRepository = vanChuyenRepository;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
        this.lichSuHoaDonRepository = lichSuHoaDonRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.ghnShippingService = ghnShippingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoaDonSummaryResponse> layDanhSachHoaDon(
            String keyword,
            String loaiDon,
            String trangThai,
            LocalDate tuNgay,
            LocalDate denNgay
    ) {
        Integer kenhBan = mapLoaiDonToKenhBan(loaiDon);
        Integer trangThaiDb = mapTrangThaiFilterToDb(trangThai);
        Instant tuNgayValue = tuNgay != null ? tuNgay.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant denNgayValue = denNgay != null ? denNgay.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC) : null;

        List<HoaDon> hoaDons = hoaDonRepository.searchInvoices(
                null, // Bỏ tìm kiếm trực tiếp trên SQL vì thiếu bảng phụ (thanhToan)
                kenhBan,
                trangThaiDb,
                tuNgayValue,
                denNgayValue
        );

        Map<Integer, VanChuyen> vanChuyenMap = vanChuyenRepository.findByHoaDonIdIn(
                hoaDons.stream().map(HoaDon::getId).toList()
        ).stream().collect(Collectors.toMap(vanChuyen -> vanChuyen.getHoaDon().getId(), Function.identity()));

        Map<Integer, ThanhToan> latestThanhToanMap = new HashMap<>();
        Map<Integer, LichSuHoaDon> latestLichSuNhanVienMap = new HashMap<>();
        for (HoaDon hoaDon : hoaDons) {
            List<ThanhToan> thanhToans = thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId());
            if (!thanhToans.isEmpty()) {
                latestThanhToanMap.put(hoaDon.getId(), thanhToans.get(0));
            }
            lichSuHoaDonRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId()).stream()
                    .filter(lichSu -> lichSu.getNhanVien() != null)
                    .findFirst()
                    .ifPresent(lichSu -> latestLichSuNhanVienMap.put(hoaDon.getId(), lichSu));
        }

        String searchKeyword = normalize(keyword);

        return hoaDons.stream()
                .filter(hoaDon -> matchKeyword(searchKeyword, hoaDon, latestThanhToanMap.get(hoaDon.getId()), latestLichSuNhanVienMap.get(hoaDon.getId())))
                .filter(hoaDon -> matchLoaiDon(loaiDon, hoaDon))
                .filter(hoaDon -> matchDerivedStatus(trangThai, hoaDon, vanChuyenMap.get(hoaDon.getId())))
                .map(hoaDon -> new HoaDonSummaryResponse(
                        hoaDon.getId(),
                        hoaDon.getMa(),
                        resolveTenKhachHang(hoaDon),
                        resolveSoDienThoai(hoaDon),
                        resolveMaNhanVien(hoaDon, latestThanhToanMap.get(hoaDon.getId()), latestLichSuNhanVienMap.get(hoaDon.getId())),
                        hoaDon.getTongTienThanhToan(),
                        hoaDon.getNgayTao(),
                    mapLoaiDon(hoaDon),
                    resolveTrangThaiHoaDon(hoaDon, vanChuyenMap.get(hoaDon.getId())),
                    hoaDon.getPhieuGiamGia() != null ? hoaDon.getPhieuGiamGia().getMa() : null,
                    resolveEmail(hoaDon.getKhachHang())
            ))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoaDonSummaryResponse> layDanhSachHoaDonTheoKhachHang(java.util.UUID khachHangId) {
        List<HoaDon> hoaDons = hoaDonRepository.findByKhachHangId(khachHangId);

        Map<Integer, VanChuyen> vanChuyenMap = vanChuyenRepository.findByHoaDonIdIn(
                hoaDons.stream().map(HoaDon::getId).toList()
        ).stream().collect(Collectors.toMap(vc -> vc.getHoaDon().getId(), Function.identity()));

        return hoaDons.stream()
                .map(hoaDon -> new HoaDonSummaryResponse(
                        hoaDon.getId(),
                        hoaDon.getMa(),
                        resolveTenKhachHang(hoaDon),
                        resolveSoDienThoai(hoaDon),
                        null,
                        hoaDon.getTongTienThanhToan(),
                        hoaDon.getNgayTao(),
                        mapLoaiDon(hoaDon),
                        resolveTrangThaiHoaDon(hoaDon, vanChuyenMap.get(hoaDon.getId())),
                        hoaDon.getPhieuGiamGia() != null ? hoaDon.getPhieuGiamGia().getMa() : null,
                        resolveEmail(hoaDon.getKhachHang())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDonDetailResponse layChiTietHoaDon(Integer id) {
        HoaDon hoaDon = findHoaDon(id);
        return mapHoaDonDetail(hoaDon);
    }

    @Override
    @Transactional
    public HoaDonDetailResponse capNhatTrangThaiHoaDon(Integer id, CapNhatTrangThaiHoaDonRequest request) {
        HoaDon hoaDon = findHoaDon(id);
        ensureHoaDonEditable(hoaDon);
        ganNhanVienXuLyNeuChuaCo(hoaDon);
        VanChuyen vanChuyen = vanChuyenRepository.findByHoaDonId(id).orElse(null);
        String trangThai = normalizeLabel(request.trangThai());

        switch (trangThai) {
            case "Chờ xác nhận" -> hoaDon.setTrangThai(TRANG_THAI_CHO_XAC_NHAN);
            case "Đã xác nhận" -> hoaDon.setTrangThai(TRANG_THAI_DA_XAC_NHAN);
            case "Chờ lấy hàng" -> {
                hoaDon.setTrangThai(TRANG_THAI_CHO_GIAO_HANG);
                vanChuyen = upsertVanChuyen(hoaDon, vanChuyen, request, TRANG_THAI_VAN_CHUYEN_CHO_XU_LY);
            }
            case "Chờ giao hàng" -> {
                hoaDon.setTrangThai(TRANG_THAI_DANG_VAN_CHUYEN);
                vanChuyen = upsertVanChuyen(hoaDon, vanChuyen, request, TRANG_THAI_VAN_CHUYEN_DANG_GIAO);
                if (vanChuyen.getNgayGui() == null) {
                    vanChuyen.setNgayGui(Instant.now());
                }
            }
            case "Đã giao hàng" -> {
                hoaDon.setTrangThai(TRANG_THAI_DA_GIAO_HANG);
                vanChuyen = upsertVanChuyen(hoaDon, vanChuyen, request, TRANG_THAI_VAN_CHUYEN_HOAN_THANH);
                if (vanChuyen.getNgayGiaoThat() == null) {
                    vanChuyen.setNgayGiaoThat(Instant.now());
                }
            }
            case "Giao hàng thất bại" -> {
                hoaDon.setTrangThai(TRANG_THAI_GIAO_HANG_THAT_BAI);
                vanChuyen = upsertVanChuyen(hoaDon, vanChuyen, request, TRANG_THAI_VAN_CHUYEN_GIAO_THAT_BAI);
                if (vanChuyen.getNgayGui() == null) {
                    vanChuyen.setNgayGui(Instant.now());
                }
                xuLyThanhToanKhiGiaoThatBai(hoaDon);
            }
            case "Hoàn thành" -> {
                if (coThanhToanCodDangCho(hoaDon)) {
                    throw new BusinessException("Vui long xac nhan thanh toan COD truoc khi hoan thanh hoa don");
                }
                hoaDon.setTrangThai(TRANG_THAI_HOAN_THANH);
                hoaDon.setNgayThanhToan(hoaDon.getNgayThanhToan() == null ? Instant.now() : hoaDon.getNgayThanhToan());
                if (!isTaiQuay(hoaDon) || vanChuyen != null) {
                    vanChuyen = upsertVanChuyen(hoaDon, vanChuyen, request, TRANG_THAI_VAN_CHUYEN_HOAN_THANH);
                    if (vanChuyen.getNgayGiaoThat() == null) {
                        vanChuyen.setNgayGiaoThat(Instant.now());
                    }
                    if (vanChuyen.getNgayGui() == null) {
                        vanChuyen.setNgayGui(Instant.now());
                    }
                }
            }
            case "Hủy" -> {
                hoaDon.setTrangThai(TRANG_THAI_HUY);
                capNhatThanhToanKhiHuyDon(hoaDon);
            }
            case "Yêu cầu hủy" -> hoaDon.setTrangThai(TRANG_THAI_YEU_CAU_HUY);
            case "Cần hoàn tiền" -> {
                hoaDon.setTrangThai(TRANG_THAI_CAN_HOAN_TIEN);
                danhDauCanHoanTienNeuDaThanhToan(hoaDon);
            }
            default -> throw new BusinessException("Trạng thái hóa đơn không hợp lệ");
        }

        if (request.ghiChu() != null && !request.ghiChu().isBlank()) {
            hoaDon.setGhiChu(request.ghiChu().trim());
        }
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hoaDon);
        if (vanChuyen != null) {
            vanChuyenRepository.save(vanChuyen);
        }

        ghiLichSuHoaDon(hoaDon, trangThai, request.ghiChu());

        return mapHoaDonDetail(findHoaDon(id));
    }

    @Override
    @Transactional
    public HoaDonDetailResponse capNhatSanPhamHoaDon(Integer id, CapNhatSanPhamHoaDonRequest request) {
        HoaDon hoaDon = findHoaDon(id);
        ensureHoaDonEditable(hoaDon);
        ganNhanVienXuLyNeuChuaCo(hoaDon);
        if (hoaDon.getTrangThai() != TRANG_THAI_CHO_XAC_NHAN) {
            throw new BusinessException("Chi co the cap nhat san pham khi hoa don dang o trang thai cho xac nhan");
        }

        List<HoaDonChiTiet> existingItems = hoaDonChiTietRepository.findByHoaDonId(id);
        Map<Integer, HoaDonChiTiet> existingMap = existingItems.stream()
                .collect(Collectors.toMap(item -> item.getGiayChiTiet().getId(), Function.identity()));

        BigDecimal tongTienHang = BigDecimal.ZERO;

        for (CapNhatSanPhamHoaDonRequest.SanPhamItemRequest itemRequest : request.items()) {
            GiayChiTiet giayChiTiet = giayChiTietRepository.findById(itemRequest.chiTietId())
                    .orElseThrow(() -> new ResourceNotFoundException("San pham chi tiet không tồn tại: " + itemRequest.chiTietId()));

            HoaDonChiTiet chiTiet = existingMap.get(itemRequest.chiTietId());
            int diff = itemRequest.soLuong() - (chiTiet != null ? chiTiet.getSoLuong() : 0);

            if (giayChiTiet.getSoLuong() < diff) {
                throw new BusinessException("Số lượng tồn không đủ cho sản phẩm: " + giayChiTiet.getGiay().getTen());
            }

            giayChiTiet.setSoLuong(giayChiTiet.getSoLuong() - diff);
            giayChiTietRepository.save(giayChiTiet);

            if (chiTiet == null) {
                chiTiet = new HoaDonChiTiet();
                chiTiet.setHoaDon(hoaDon);
                chiTiet.setGiayChiTiet(giayChiTiet);
                chiTiet.setNgayTao(Instant.now());
            }

            chiTiet.setSoLuong(itemRequest.soLuong());
            chiTiet.setGiaDonVi(giayChiTiet.getGiaBan());
            chiTiet.setThanhTien(giayChiTiet.getGiaBan().multiply(BigDecimal.valueOf(itemRequest.soLuong())));
            chiTiet.setTrangThai(1);
            hoaDonChiTietRepository.save(chiTiet);

            tongTienHang = tongTienHang.add(chiTiet.getThanhTien());
            existingMap.remove(itemRequest.chiTietId());
        }

        // Remove items not in the request
        for (HoaDonChiTiet itemToRemove : existingMap.values()) {
            GiayChiTiet giayChiTiet = itemToRemove.getGiayChiTiet();
            giayChiTiet.setSoLuong(giayChiTiet.getSoLuong() + itemToRemove.getSoLuong());
            giayChiTietRepository.save(giayChiTiet);
            hoaDonChiTietRepository.delete(itemToRemove);
        }

        hoaDon.setTongTienHang(tongTienHang);
        BigDecimal giamGia = hoaDon.getTienGiam() != null ? hoaDon.getTienGiam() : BigDecimal.ZERO;
        hoaDon.setTongTienThanhToan(tongTienHang.subtract(giamGia).max(BigDecimal.ZERO));
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hoaDon);

        ghiLichSuHoaDon(hoaDon, "Cập nhật sản phẩm", "Thay đổi danh sách sản phẩm trong hóa đơn");

        return mapHoaDonDetail(findHoaDon(id));
    }

    @Override
    @Transactional
    public HoaDonDetailResponse xacNhanThanhToanCod(Integer id, XacNhanThanhToanCodRequest request) {
        HoaDon hoaDon = findHoaDon(id);
        NhanVien nhanVienXuLy = ganNhanVienXuLyNeuChuaCo(hoaDon);
        ThanhToan thanhToan = timThanhToanCodDangCho(hoaDon)
                .orElseThrow(() -> new BusinessException("Hóa đơn không có thanh toán COD đang chờ"));

        Integer hinhThuc = request.hinhThucThanhToan();
        if (!Objects.equals(hinhThuc, HINH_THUC_THANH_TOAN_TIEN_MAT)
                && !Objects.equals(hinhThuc, HINH_THUC_THANH_TOAN_CHUYEN_KHOAN)) {
            throw new BusinessException("COD chi ho tro xac nhan bang tien mat hoac chuyen khoan");
        }

        BigDecimal tongTien = defaultMoney(thanhToan.getSoTien());
        BigDecimal tienKhachDua = request.tienKhachDua() != null ? request.tienKhachDua() : tongTien;
        if (Objects.equals(hinhThuc, HINH_THUC_THANH_TOAN_TIEN_MAT) && tienKhachDua.compareTo(tongTien) < 0) {
            throw new BusinessException("Tien khach dua phai lon hon hoac bang so tien can thanh toan");
        }
        if (tienKhachDua.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Tiền khách đưa không được âm");
        }

        Instant now = Instant.now();
        thanhToan.setHinhThuc(hinhThuc);
        thanhToan.setTienThoiLai(Objects.equals(hinhThuc, HINH_THUC_THANH_TOAN_TIEN_MAT)
                ? tienKhachDua.subtract(tongTien)
                : BigDecimal.ZERO);
        thanhToan.setCongThanhToan(Objects.equals(hinhThuc, HINH_THUC_THANH_TOAN_CHUYEN_KHOAN) ? "COD - Chuyen khoan" : "COD - Tien mat");
        thanhToan.setNgayThanhToan(now);
        if (nhanVienXuLy != null) {
            thanhToan.setNhanVien(nhanVienXuLy);
        }
        thanhToan.setTrangThai(TRANG_THAI_THANH_TOAN_THANH_CONG);
        thanhToan.setGhiChu(request.ghiChu() != null && !request.ghiChu().isBlank()
                ? request.ghiChu().trim()
                : "COD - Da thu tien khi giao hang");
        thanhToanRepository.save(thanhToan);

        hoaDon.setNgayThanhToan(hoaDon.getNgayThanhToan() == null ? now : hoaDon.getNgayThanhToan());
        hoaDon.setNgayCapNhat(now);
        hoaDonRepository.save(hoaDon);
        ghiLichSuHoaDon(hoaDon, "Xác nhận thanh toán COD", thanhToan.getGhiChu());

        return mapHoaDonDetail(findHoaDon(id));
    }

    @Override
    @Transactional
    public HoaDonDetailResponse xacNhanHoanTien(Integer id, XacNhanHoanTienRequest request) {
        HoaDon hoaDon = findHoaDon(id);
        NhanVien nhanVienXuLy = ganNhanVienXuLyNeuChuaCo(hoaDon);
        ThanhToan thanhToan = timThanhToanCanHoanTien(hoaDon)
                .orElseThrow(() -> new BusinessException("Hóa đơn không có thanh toán cần hoàn tiền"));

        Integer hinhThuc = request.hinhThucHoanTien();
        if (!Objects.equals(hinhThuc, HINH_THUC_THANH_TOAN_TIEN_MAT)
                && !Objects.equals(hinhThuc, HINH_THUC_THANH_TOAN_CHUYEN_KHOAN)
                && !Objects.equals(hinhThuc, HINH_THUC_THANH_TOAN_VI)) {
            throw new BusinessException("Hình thức hoàn tiền không hợp lệ");
        }

        BigDecimal soTienCanHoan = defaultMoney(thanhToan.getSoTien());
        BigDecimal soTienHoan = request.soTienHoan() != null ? request.soTienHoan() : soTienCanHoan;
        if (soTienHoan.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("So tien hoan phai lon hon 0");
        }
        if (soTienHoan.compareTo(soTienCanHoan) != 0) {
            throw new BusinessException("So tien hoan phai bang so tien can hoan");
        }

        Instant now = Instant.now();
        thanhToan.setHinhThuc(hinhThuc);
        thanhToan.setSoTien(soTienHoan);
        thanhToan.setCongThanhToan("Hoan tien - " + mapPhuongThucThanhToan(hinhThuc));
        if (request.maGiaoDichHoan() != null && !request.maGiaoDichHoan().isBlank()) {
            thanhToan.setMaGiaoDich(request.maGiaoDichHoan().trim());
        }
        thanhToan.setNgayThanhToan(now);
        if (nhanVienXuLy != null) {
            thanhToan.setNhanVien(nhanVienXuLy);
        }
        thanhToan.setTrangThai(TRANG_THAI_THANH_TOAN_DA_HOAN_TIEN);
        String ghiChu = request.ghiChu() != null && !request.ghiChu().isBlank()
                ? request.ghiChu().trim()
                : "Da hoan tien cho khach hang";
        thanhToan.setGhiChu(taoGhiChuThanhToan(thanhToan, ghiChu));
        thanhToanRepository.save(thanhToan);

        if (Objects.equals(hoaDon.getTrangThai(), TRANG_THAI_CAN_HOAN_TIEN)) {
            hoaDon.setTrangThai(TRANG_THAI_HUY);
        }
        hoaDon.setNgayCapNhat(now);
        hoaDonRepository.save(hoaDon);
        ghiLichSuHoaDon(hoaDon, "Xác nhận hoàn tiền", ghiChu);

        return mapHoaDonDetail(findHoaDon(id));
    }

    @Override
    @Transactional
    public TinhPhiVanChuyenGhnResponse tinhVaCapNhatPhiVanChuyenGhn(Integer id, TinhPhiVanChuyenGhnRequest request) {
        HoaDon hoaDon = findHoaDon(id);
        ensureHoaDonEditable(hoaDon);
        ganNhanVienXuLyNeuChuaCo(hoaDon);
    if (isTaiQuay(hoaDon) && !isDonGiaoHang(hoaDon)) {
        throw new BusinessException("Hóa đơn tại quầy không cần tính phí vận chuyển GHN");
    }

        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(id);
        if (items.isEmpty()) {
            throw new BusinessException("Hoa don chua co san pham de tinh phi van chuyen");
        }

        TinhPhiVanChuyenGhnResponse phiGhn = ghnShippingService.tinhPhi(hoaDon, items, request);
        VanChuyen vanChuyen = vanChuyenRepository.findByHoaDonId(id).orElseGet(() -> {
            VanChuyen created = new VanChuyen();
            created.setHoaDon(hoaDon);
            created.setDonViVanChuyen("GHN");
            created.setTrangThai(TRANG_THAI_VAN_CHUYEN_CHO_XU_LY);
            created.setNgayTao(Instant.now());
            return created;
        });

        vanChuyen.setDonViVanChuyen("GHN");
        vanChuyen.setPhiVanChuyen(phiGhn.phiVanChuyen());
        vanChuyen.setNgayCapNhat(Instant.now());
        vanChuyenRepository.save(vanChuyen);

        hoaDon.setTongTienThanhToan(
                defaultMoney(hoaDon.getTongTienHang())
                        .add(phiGhn.phiVanChuyen())
                        .subtract(defaultMoney(hoaDon.getTienGiam()))
                        .max(BigDecimal.ZERO)
        );
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hoaDon);
        ghiLichSuHoaDon(hoaDon, "Cập nhật phí vận chuyển", "Tính lại phí vận chuyển GHN");

        return phiGhn;
    }

    private HoaDonDetailResponse mapHoaDonDetail(HoaDon hoaDon) {
        List<HoaDonChiTiet> hoaDonChiTiets = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId());
        List<ThanhToan> thanhToans = thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId());
        VanChuyen vanChuyen = vanChuyenRepository.findByHoaDonId(hoaDon.getId()).orElse(null);
        Map<Integer, String> hinhAnhMap = hinhAnhGiayRepository
                .findByGiayChiTietIdInAndTrangThaiOrderByLaHinhChinhDescNgayTaoAsc(
                        hoaDonChiTiets.stream().map(item -> item.getGiayChiTiet().getId()).toList(),
                        TRANG_THAI_HINH_ANH_HOAT_DONG
                )
                .stream()
                .filter(hinh -> hinh.getGiayChiTiet() != null && hinh.getGiayChiTiet().getId() != null)
                .collect(Collectors.toMap(
                        hinh -> hinh.getGiayChiTiet().getId(),
                        HinhAnhGiay::getUrl,
                        (oldValue, newValue) -> oldValue
                ));

        List<LichSuHoaDon> lichSuHoaDons = lichSuHoaDonRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId());
        ThanhToan thanhToanCoNhanVien = thanhToans.stream()
                .filter(thanhToan -> thanhToan.getNhanVien() != null)
                .findFirst()
                .orElse(null);
        LichSuHoaDon lichSuCoNhanVien = lichSuHoaDons.stream()
                .filter(lichSu -> lichSu.getNhanVien() != null)
                .findFirst()
                .orElse(null);
        String tenNhanVien = resolveTenNhanVien(hoaDon, thanhToanCoNhanVien, lichSuCoNhanVien);
        String maNhanVien = resolveMaNhanVien(hoaDon, thanhToanCoNhanVien, lichSuCoNhanVien);

        return new HoaDonDetailResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                resolveTenKhachHang(hoaDon),
                tenNhanVien,
                maNhanVien,
                resolveNguoiTaoHoaDon(hoaDon, maNhanVien),
                hoaDon.getTongTienThanhToan(),
                hoaDon.getNgayTao(),
            mapLoaiDon(hoaDon),
            resolveTrangThaiHoaDon(hoaDon, vanChuyen),
                safeValue(hoaDon.getSdtNguoiNhan()),
                resolveEmail(hoaDon.getKhachHang()),
                safeValue(hoaDon.getDiaChiGiaoHang()),
                safeValue(hoaDon.getGhiChu()),
                vanChuyen != null ? defaultMoney(vanChuyen.getPhiVanChuyen()) : BigDecimal.ZERO,
                hoaDon.getPhieuGiamGia() != null ? hoaDon.getPhieuGiamGia().getMa() : "Không áp dụng",
                defaultMoney(hoaDon.getTienGiam()),
                vanChuyen != null ? safeValue(vanChuyen.getDonViVanChuyen()) : "",
                vanChuyen != null ? safeValue(vanChuyen.getMaVanDon()) : "",
                thanhToans.stream().map(this::mapThanhToan).toList(),
                hoaDonChiTiets.stream().map(item -> mapSanPham(item, hinhAnhMap)).toList(),
                lichSuHoaDons.stream()
                        .map(this::mapLichSu).toList()
        );
    }

    private HoaDonHistoryResponse mapLichSu(LichSuHoaDon lichSu) {
        return new HoaDonHistoryResponse(
                lichSu.getId(),
                lichSu.getNhanVien() != null ? lichSu.getNhanVien().getMa() : "Hệ thống",
                lichSu.getNhanVien() != null ? lichSu.getNhanVien().getHoTen() : "Hệ thống",
                normalizeLegacyDisplayValue(lichSu.getTrangThai()),
                lichSu.getNgayTao(),
                normalizeLegacyDisplayValue(lichSu.getGhiChu())
        );
    }

    private void ghiLichSuHoaDon(HoaDon hoaDon, String trangThai, String ghiChu) {
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setNhanVien(resolveNhanVienDangDangNhap(hoaDon));
        lichSu.setTrangThai(trangThai);
        lichSu.setGhiChu(ghiChu);
        lichSu.setNgayTao(Instant.now());
        lichSuHoaDonRepository.save(lichSu);
    }

    private NhanVien ganNhanVienXuLyNeuChuaCo(HoaDon hoaDon) {
        NhanVien nhanVien = resolveNhanVienDangDangNhap(hoaDon);
        if (hoaDon.getNhanVien() == null && nhanVien != null) {
            hoaDon.setNhanVien(nhanVien);
        }
        return nhanVien;
    }

    private NhanVien resolveNhanVienDangDangNhap(HoaDon hoaDon) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal principal) {
            return nhanVienRepository.findById(principal.id()).orElse(hoaDon.getNhanVien());
        }
        return hoaDon.getNhanVien();
    }

    private HoaDonPaymentHistoryResponse mapThanhToan(ThanhToan thanhToan) {
        return new HoaDonPaymentHistoryResponse(
                thanhToan.getId(),
                safeValue(thanhToan.getMaGiaoDich()),
                mapLoaiGiaoDich(thanhToan),
                mapPhuongThucThanhToan(thanhToan.getHinhThuc()),
                mapTrangThaiThanhToan(thanhToan.getTrangThai()),
                thanhToan.getNgayThanhToan() != null ? thanhToan.getNgayThanhToan() : thanhToan.getNgayTao(),
                defaultMoney(thanhToan.getSoTien()),
                safeValue(thanhToan.getGhiChu())
        );
    }

    private boolean coThanhToanCodDangCho(HoaDon hoaDon) {
        return timThanhToanCodDangCho(hoaDon).isPresent();
    }

    private Optional<ThanhToan> timThanhToanCodDangCho(HoaDon hoaDon) {
        return thanhToanRepository.findByHoaDonIdAndHinhThucOrderByNgayTaoDesc(hoaDon.getId(), HINH_THUC_THANH_TOAN_COD)
                .stream()
                .filter(thanhToan -> Objects.equals(thanhToan.getTrangThai(), TRANG_THAI_THANH_TOAN_CHO_THANH_TOAN))
                .findFirst();
    }

    private Optional<ThanhToan> timThanhToanCanHoanTien(HoaDon hoaDon) {
        return thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId())
                .stream()
                .filter(thanhToan -> Objects.equals(thanhToan.getTrangThai(), TRANG_THAI_THANH_TOAN_CAN_HOAN_TIEN))
                .findFirst();
    }

    private void capNhatThanhToanKhiHuyDon(HoaDon hoaDon) {
        List<ThanhToan> thanhToans = thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId());
        for (ThanhToan thanhToan : thanhToans) {
            if (Objects.equals(thanhToan.getTrangThai(), TRANG_THAI_THANH_TOAN_CHO_THANH_TOAN)) {
                thanhToan.setTrangThai(TRANG_THAI_THANH_TOAN_DA_HUY);
                thanhToan.setGhiChu(taoGhiChuThanhToan(thanhToan, "Đã hủy do hóa đơn bị hủy"));
                thanhToanRepository.save(thanhToan);
            } else if (Objects.equals(thanhToan.getTrangThai(), TRANG_THAI_THANH_TOAN_THANH_CONG)) {
                thanhToan.setTrangThai(TRANG_THAI_THANH_TOAN_CAN_HOAN_TIEN);
                thanhToan.setGhiChu(taoGhiChuThanhToan(thanhToan, "Đơn hủy sau khi đã thanh toán, cần hoàn tiền"));
                thanhToanRepository.save(thanhToan);
            }
        }
    }

    private void danhDauCanHoanTienNeuDaThanhToan(HoaDon hoaDon) {
        thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId()).stream()
                .filter(thanhToan -> Objects.equals(thanhToan.getTrangThai(), TRANG_THAI_THANH_TOAN_THANH_CONG))
                .forEach(thanhToan -> {
                    thanhToan.setTrangThai(TRANG_THAI_THANH_TOAN_CAN_HOAN_TIEN);
                    thanhToan.setGhiChu(taoGhiChuThanhToan(thanhToan, "Cần hoàn tiền cho hóa đơn"));
                    thanhToanRepository.save(thanhToan);
                });
    }

    private void xuLyThanhToanKhiGiaoThatBai(HoaDon hoaDon) {
        List<ThanhToan> thanhToans = thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId());
        for (ThanhToan thanhToan : thanhToans) {
            if (Objects.equals(thanhToan.getTrangThai(), TRANG_THAI_THANH_TOAN_CHO_THANH_TOAN)) {
                thanhToan.setTrangThai(TRANG_THAI_THANH_TOAN_DA_HUY);
                thanhToan.setGhiChu(taoGhiChuThanhToan(thanhToan, "Giao hàng thất bại, hủy giao dịch chờ thanh toán"));
                thanhToanRepository.save(thanhToan);
            } else if (Objects.equals(thanhToan.getTrangThai(), TRANG_THAI_THANH_TOAN_THANH_CONG)) {
                thanhToan.setTrangThai(TRANG_THAI_THANH_TOAN_CAN_HOAN_TIEN);
                thanhToan.setGhiChu(taoGhiChuThanhToan(thanhToan, "Giao hàng thất bại, cần hoàn tiền cho khách"));
                thanhToanRepository.save(thanhToan);
            }
        }
    }

    private String taoGhiChuThanhToan(ThanhToan thanhToan, String ghiChuMoi) {
        String ghiChuCu = thanhToan.getGhiChu();
        if (ghiChuCu == null || ghiChuCu.isBlank()) {
            return ghiChuMoi;
        }
        if (ghiChuCu.contains(ghiChuMoi)) {
            return ghiChuCu;
        }
        return ghiChuCu + " - " + ghiChuMoi;
    }

    private HoaDonProductResponse mapSanPham(HoaDonChiTiet item, Map<Integer, String> hinhAnhMap) {
        return new HoaDonProductResponse(
                item.getId(),
                item.getGiayChiTiet().getGiay().getTen(),
                item.getGiayChiTiet().getGiay().getLoaiGiay() != null ? item.getGiayChiTiet().getGiay().getLoaiGiay().getTen() : "",
                item.getGiayChiTiet().getMauSac() != null ? item.getGiayChiTiet().getMauSac().getTen() : "",
                item.getGiayChiTiet().getKichCo() != null ? item.getGiayChiTiet().getKichCo().getGiaTri() : "",
                item.getSoLuong(),
                defaultMoney(item.getGiaDonVi()),
                defaultMoney(item.getThanhTien()),
                hinhAnhMap.getOrDefault(item.getGiayChiTiet().getId(), "")
        );
    }

    private VanChuyen upsertVanChuyen(
            HoaDon hoaDon,
            VanChuyen current,
            CapNhatTrangThaiHoaDonRequest request,
            Integer trangThai
    ) {
        VanChuyen vanChuyen = current != null ? current : new VanChuyen();
        vanChuyen.setHoaDon(hoaDon);
        vanChuyen.setDonViVanChuyen(
                request.donViVanChuyen() != null && !request.donViVanChuyen().isBlank()
                        ? request.donViVanChuyen().trim()
                        : Optional.ofNullable(vanChuyen.getDonViVanChuyen()).filter(value -> !value.isBlank()).orElse("Chưa cập nhật")
        );
        vanChuyen.setMaVanDon(
                request.maVanDon() != null && !request.maVanDon().isBlank()
                        ? request.maVanDon().trim()
                        : vanChuyen.getMaVanDon()
        );
        vanChuyen.setPhiVanChuyen(vanChuyen.getPhiVanChuyen() == null ? BigDecimal.ZERO : vanChuyen.getPhiVanChuyen());
        vanChuyen.setTrangThai(trangThai);
        vanChuyen.setGhiChu(request.ghiChu());
        vanChuyen.setNgayCapNhat(Instant.now());
        if (vanChuyen.getNgayTao() == null) {
            vanChuyen.setNgayTao(Instant.now());
        }
        return vanChuyen;
    }

    private HoaDon findHoaDon(Integer id) {
        return hoaDonRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hoa don không tồn tại"));
    }

    private void ensureHoaDonEditable(HoaDon hoaDon) {
        if (hoaDon.getTrangThai() != null && hoaDon.getTrangThai() == TRANG_THAI_HOAN_THANH) {
            throw new BusinessException("Hóa đơn đã hoàn thành, không thể chỉnh sửa");
        }
    }

    private String resolveTrangThaiHoaDon(HoaDon hoaDon, VanChuyen vanChuyen) {
        if (hoaDon.getTrangThai() != null) {
            switch (hoaDon.getTrangThai()) {
                case TRANG_THAI_CHO_XAC_NHAN: return "Chờ xác nhận";
                case TRANG_THAI_DA_XAC_NHAN: return "Đã xác nhận";
                case TRANG_THAI_CHO_GIAO_HANG: return "Chờ lấy hàng";
                case TRANG_THAI_DANG_VAN_CHUYEN: return "Chờ giao hàng";
                case TRANG_THAI_DA_GIAO_HANG: return "Đã giao hàng";
                case TRANG_THAI_GIAO_HANG_THAT_BAI: return "Giao hàng thất bại";
                case TRANG_THAI_HOAN_THANH: return "Hoàn thành";
                case TRANG_THAI_HUY: return "Hủy";
                case TRANG_THAI_YEU_CAU_HUY: return "Yêu cầu hủy";
                case TRANG_THAI_CAN_HOAN_TIEN: return "Cần hoàn tiền";
                default: return "Chờ xác nhận";
            }
        }
        return "Chờ xác nhận";
    }

    private boolean matchDerivedStatus(String trangThaiFilter, HoaDon hoaDon, VanChuyen vanChuyen) {
        if (trangThaiFilter == null || trangThaiFilter.isBlank() || "Tất cả".equalsIgnoreCase(trangThaiFilter.trim())) {
            return true;
        }
        return resolveTrangThaiHoaDon(hoaDon, vanChuyen).equalsIgnoreCase(normalizeLabel(trangThaiFilter));
    }

    private boolean matchKeyword(String keyword, HoaDon hoaDon, ThanhToan thanhToan) {
        return matchKeyword(keyword, hoaDon, thanhToan, null);
    }

    private boolean matchKeyword(String keyword, HoaDon hoaDon, ThanhToan thanhToan, LichSuHoaDon lichSu) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String ma = normalize(hoaDon.getMa());
        String maNhanVien = normalize(resolveMaNhanVien(hoaDon, thanhToan, lichSu));
        
        return (ma != null && ma.contains(keyword)) ||
               (maNhanVien != null && maNhanVien.contains(keyword));
    }

    private Integer mapLoaiDonToKenhBan(String loaiDon) {
        if (loaiDon == null || loaiDon.isBlank()) {
            return null;
        }
        String normalized = loaiDon.trim();
        return "Cửa hàng".equalsIgnoreCase(normalized)
                || "Offline".equalsIgnoreCase(normalized)
                || "Tại cửa hàng".equalsIgnoreCase(normalized)
                ? KENH_BAN_TAI_QUAY
                : null;
    }

    private boolean matchLoaiDon(String loaiDon, HoaDon hoaDon) {
        if (loaiDon == null || loaiDon.isBlank()) {
            return true;
        }
        String normalized = loaiDon.trim();
        return mapLoaiDon(hoaDon).equalsIgnoreCase(normalized)
                || (("Offline".equalsIgnoreCase(normalized) || "Tại cửa hàng".equalsIgnoreCase(normalized))
                && isTaiQuay(hoaDon) && !isDonGiaoHang(hoaDon))
                || ("Online".equalsIgnoreCase(normalized) && !isTaiQuay(hoaDon));
    }

    private Integer mapTrangThaiFilterToDb(String trangThai) {
        if (trangThai == null || trangThai.isBlank() || "Tất cả".equalsIgnoreCase(trangThai.trim())) {
            return null;
        }
        String normalized = normalizeLabel(trangThai);
        return switch (normalized) {
            case "Chờ xác nhận" -> TRANG_THAI_CHO_XAC_NHAN;
            case "Đã xác nhận" -> TRANG_THAI_DA_XAC_NHAN;
            case "Chờ lấy hàng" -> TRANG_THAI_CHO_GIAO_HANG;
            case "Chờ giao hàng" -> TRANG_THAI_DANG_VAN_CHUYEN;
            case "Đã giao hàng" -> TRANG_THAI_DA_GIAO_HANG;
            case "Giao hàng thất bại" -> TRANG_THAI_GIAO_HANG_THAT_BAI;
            case "Hoàn thành" -> TRANG_THAI_HOAN_THANH;
            case "Hủy" -> TRANG_THAI_HUY;
            case "Yêu cầu hủy" -> TRANG_THAI_YEU_CAU_HUY;
            case "Cần hoàn tiền" -> TRANG_THAI_CAN_HOAN_TIEN;
            default -> null;
        };
    }

private String mapLoaiDon(HoaDon hoaDon) {
    return isTaiQuay(hoaDon) && !isDonGiaoHang(hoaDon) ? "Cửa hàng" : "Trực tuyến";
}

private boolean isTaiQuay(HoaDon hoaDon) {
    return isTaiQuay(hoaDon.getKenhBan());
}

private boolean isTaiQuay(Integer kenhBan) {
    return kenhBan != null && kenhBan == KENH_BAN_TAI_QUAY;
}

private boolean isDonGiaoHang(HoaDon hoaDon) {
    String diaChi = safeValue(hoaDon.getDiaChiGiaoHang());
    return !diaChi.isBlank() && !laDiaChiTaiQuay(diaChi);
}

    private String resolveTenKhachHang(HoaDon hoaDon) {
        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getHoTen() != null && !hoaDon.getKhachHang().getHoTen().isBlank()) {
            return hoaDon.getKhachHang().getHoTen();
        }
        return safeValue(hoaDon.getTenNguoiNhan());
    }

    private String resolveEmail(KhachHang khachHang) {
        if (khachHang == null || khachHang.getEmail() == null || khachHang.getEmail().isBlank()) {
            return KHONG_CO;
        }
        return khachHang.getEmail();
    }

    private String resolveSoDienThoai(HoaDon hoaDon) {
        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getSdt() != null && !hoaDon.getKhachHang().getSdt().isBlank()) {
            return hoaDon.getKhachHang().getSdt();
        }
        return safeValue(hoaDon.getSdtNguoiNhan());
    }

    private String resolveNguoiTaoHoaDon(HoaDon hoaDon, String maNhanVienXuLy) {
        if (hoaDon.getKenhBan() == null) {
            return "Hệ thống";
        }
        if (isTaiQuay(hoaDon)) {
            return isMeaningfulValue(maNhanVienXuLy) ? maNhanVienXuLy : "Hệ thống";
        }

        String tenKhachHang = resolveTenKhachHang(hoaDon);
        return isMeaningfulValue(tenKhachHang) ? tenKhachHang : "Khách hàng";
    }

    private String resolveTenNhanVien(HoaDon hoaDon, ThanhToan thanhToan) {
        return resolveTenNhanVien(hoaDon, thanhToan, null);
    }

    private String resolveTenNhanVien(HoaDon hoaDon, ThanhToan thanhToan, LichSuHoaDon lichSu) {
        if (hoaDon.getNhanVien() != null && hoaDon.getNhanVien().getHoTen() != null && !hoaDon.getNhanVien().getHoTen().isBlank()) {
            return hoaDon.getNhanVien().getHoTen();
        }
        String tenNhanVienThanhToan = resolveTenNhanVien(thanhToan);
        if (!Objects.equals(tenNhanVienThanhToan, "Chưa cập nhật")) {
            return tenNhanVienThanhToan;
        }
        if (lichSu != null && lichSu.getNhanVien() != null && lichSu.getNhanVien().getHoTen() != null && !lichSu.getNhanVien().getHoTen().isBlank()) {
            return lichSu.getNhanVien().getHoTen();
        }
        return "Chưa cập nhật";
    }

    private String resolveMaNhanVien(HoaDon hoaDon, ThanhToan thanhToan) {
        return resolveMaNhanVien(hoaDon, thanhToan, null);
    }

    private String resolveMaNhanVien(HoaDon hoaDon, ThanhToan thanhToan, LichSuHoaDon lichSu) {
        if (hoaDon.getNhanVien() != null && hoaDon.getNhanVien().getMa() != null && !hoaDon.getNhanVien().getMa().isBlank()) {
            return hoaDon.getNhanVien().getMa();
        }
        if (thanhToan != null && thanhToan.getNhanVien() != null && thanhToan.getNhanVien().getMa() != null && !thanhToan.getNhanVien().getMa().isBlank()) {
            return thanhToan.getNhanVien().getMa();
        }
        if (lichSu != null && lichSu.getNhanVien() != null && lichSu.getNhanVien().getMa() != null && !lichSu.getNhanVien().getMa().isBlank()) {
            return lichSu.getNhanVien().getMa();
        }
        return "Chưa cập nhật";
    }

    private String resolveTenNhanVien(ThanhToan thanhToan) {
        if (thanhToan == null || thanhToan.getNhanVien() == null || thanhToan.getNhanVien().getHoTen() == null || thanhToan.getNhanVien().getHoTen().isBlank()) {
            return "Chưa cập nhật";
        }
        return thanhToan.getNhanVien().getHoTen();
    }

    private String mapLoaiGiaoDich(ThanhToan thanhToan) {
        if (Objects.equals(thanhToan.getHinhThuc(), HINH_THUC_THANH_TOAN_COD)) {
            return "Thanh toán COD";
        }
        if (thanhToan.getHoaDon() != null && !isTaiQuay(thanhToan.getHoaDon())
                && thanhToan.getCongThanhToan() != null && !thanhToan.getCongThanhToan().isBlank()) {
            return "Thanh toán đơn trực tuyến";
        }
        if (thanhToan.getHoaDon() != null && isTaiQuay(thanhToan.getHoaDon())) {
            return "Thanh toán đơn cửa hàng";
        }
        return thanhToan.getTienThoiLai() != null && thanhToan.getTienThoiLai().compareTo(BigDecimal.ZERO) > 0
                ? "Thanh toán toàn phần"
                : "Thanh toán";
    }

    private String mapPhuongThucThanhToan(Integer hinhThuc) {
        if (hinhThuc == null) {
            return "Chưa cập nhật";
        }
        return switch (hinhThuc) {
            case HINH_THUC_THANH_TOAN_TIEN_MAT -> "Tiền mặt";
            case HINH_THUC_THANH_TOAN_CHUYEN_KHOAN -> "Chuyển khoản";
            case HINH_THUC_THANH_TOAN_VI -> "Ví điện tử";
            case HINH_THUC_THANH_TOAN_COD -> "COD";
            default -> "Khác";
        };
    }

    private String mapTrangThaiThanhToan(Integer trangThai) {
        if (trangThai == null) {
            return "Chờ thanh toán";
        }
        return switch (trangThai) {
            case TRANG_THAI_THANH_TOAN_THANH_CONG -> "Đã thanh toán";
            case TRANG_THAI_THANH_TOAN_THAT_BAI -> "Thanh toán thất bại";
            case TRANG_THAI_THANH_TOAN_DA_HUY -> "Đã hủy";
            case TRANG_THAI_THANH_TOAN_CAN_HOAN_TIEN -> "Cần hoàn tiền";
            case TRANG_THAI_THANH_TOAN_DA_HOAN_TIEN -> "Đã hoàn tiền";
            default -> "Chờ thanh toán";
        };
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeLabel(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isMeaningfulValue(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = normalizeTextKey(value);
        return !normalized.equals(normalizeTextKey(KHONG_CO))
                && !normalized.equals(normalizeTextKey(KHACH_VANG_LAI))
                && !normalized.equals("chua cap nhat")
                && !normalized.equals("chưa cập nhật");
    }

    private boolean laDiaChiTaiQuay(String diaChi) {
        String normalized = normalizeTextKey(diaChi);
        return normalized.equals(normalizeTextKey(DIA_CHI_TAI_QUAY))
                || normalized.equals(normalizeTextKey(DIA_CHI_TAI_QUAY_KHONG_DAU));
    }

    private String normalizeLegacyDisplayValue(String value) {
        if (value == null) {
            return null;
        }

        return switch (normalizeTextKey(value)) {
            case "1" -> "Chờ xác nhận";
            case "2" -> "Chờ lấy hàng";
            case "3" -> "Chờ giao hàng";
            case "4" -> "Đã giao hàng";
            case "5" -> "Hoàn thành";
            case "6" -> "Hủy";
            case "7" -> "Yêu cầu hủy";
            case "8" -> "Cần hoàn tiền";
            case "9" -> "Đã xác nhận";
            case "10" -> "Giao hàng thất bại";
            case "mua tai quay" -> DIA_CHI_TAI_QUAY;
            case "không có" -> KHONG_CO;
            case "khach le", "khach vang lai" -> KHACH_VANG_LAI;
            case "hoa don cho tao tu man hinh ban hang tai quay" -> GHI_CHU_TAO_HOA_DON_TAI_QUAY;
            default -> value;
        };
    }

    private String normalizeTextKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String safeValue(String value) {
        return value == null ? "" : normalizeLegacyDisplayValue(value);
    }
}
