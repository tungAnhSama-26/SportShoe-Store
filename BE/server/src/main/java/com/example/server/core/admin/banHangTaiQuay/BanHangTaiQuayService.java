package com.example.server.core.admin.banHangTaiQuay;

import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.ThanhToan;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.ThanhToanRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BanHangTaiQuayService {

    private static final int KENH_BAN_TAI_QUAY = 1;
    private static final int TRANG_THAI_HOA_DON_CHO = 1;
    private static final int TRANG_THAI_DA_THANH_TOAN = 2;
    private static final int TRANG_THAI_DA_HUY = 5;
    private static final int HINH_THUC_TIEN_MAT = 1;
    private static final int HINH_THUC_CHUYEN_KHOAN = 2;
    private static final int HINH_THUC_VI = 3;
    private static final String DIA_CHI_TAI_QUAY = "Mua tai quay";

    private final KhachHangRepository khachHangRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanRepository thanhToanRepository;

    public BanHangTaiQuayService(
            KhachHangRepository khachHangRepository,
            GiayChiTietRepository giayChiTietRepository,
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanRepository thanhToanRepository
    ) {
        this.khachHangRepository = khachHangRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanRepository = thanhToanRepository;
    }

    @Transactional(readOnly = true)
    public List<KhachHangTaiQuayResponse> timKhachHangTheoTuKhoa(String keyword) {
        return khachHangRepository.searchByKeyword(chuanHoaTuKhoa(keyword))
                .stream()
                .limit(10)
                .map(khachHang -> new KhachHangTaiQuayResponse(
                        khachHang.getId(),
                        khachHang.getHoTen(),
                        khachHang.getSdt(),
                        khachHang.getEmail()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SanPhamTaiQuayResponse> timSanPham(String keyword) {
        return giayChiTietRepository.searchForCounterSale(chuanHoaTuKhoa(keyword))
                .stream()
                .limit(10)
                .map(chiTiet -> new SanPhamTaiQuayResponse(
                        chiTiet.getId(),
                        chiTiet.getGiay().getMa(),
                        chiTiet.getGiay().getTen(),
                        chiTiet.getSku(),
                        chiTiet.getMaBienThe(),
                        chiTiet.getSoLuong(),
                        chiTiet.getGiaBan(),
                        chiTiet.getGiay().getLoaiGiay() != null ? chiTiet.getGiay().getLoaiGiay().getTen() : null,
                        chiTiet.getGiay().getThuongHieu() != null ? chiTiet.getGiay().getThuongHieu().getTen() : null,
                        chiTiet.getGiay().getGiayThuocTinh() != null && chiTiet.getGiay().getGiayThuocTinh().getDeGiay() != null
                                ? chiTiet.getGiay().getGiayThuocTinh().getDeGiay().getTen() : null,
                        chiTiet.getGiay().getGiayThuocTinh() != null && chiTiet.getGiay().getGiayThuocTinh().getCoGiay() != null
                                ? chiTiet.getGiay().getGiayThuocTinh().getCoGiay().getTen() : null,
                        chiTiet.getGiay().getGiayThuocTinh() != null && chiTiet.getGiay().getGiayThuocTinh().getCongNgheDem() != null
                                ? chiTiet.getGiay().getGiayThuocTinh().getCongNgheDem().getTen() : null,
                        chiTiet.getMauSac() != null ? chiTiet.getMauSac().getTen() : null,
                        chiTiet.getKichCo() != null ? chiTiet.getKichCo().getGiaTri() : null,
                        chiTiet.getGiay().getGiayThuocTinh() != null && chiTiet.getGiay().getGiayThuocTinh().getTrongLuong() != null
                                ? chiTiet.getGiay().getGiayThuocTinh().getTrongLuong().getGiaTri() + " gram" : null
                ))
                .toList();
    }

    @Transactional
    public HoaDonChoChiTietResponse taoHoaDonCho(TaoHoaDonChoRequest request) {
        HoaDon savedHoaDon = taoHoaDon(
                request.khachHangId(),
                request.tenKhachHang(),
                request.soDienThoai(),
                request.items(),
                TRANG_THAI_HOA_DON_CHO,
                "Hoa don cho tao tu man hinh ban hang tai quay"
        );
        List<HoaDonChiTiet> savedItems = hoaDonChiTietRepository.findByHoaDonIdWithProduct(savedHoaDon.getId());

        return mapHoaDonChiTiet(savedHoaDon, savedItems);
    }

    @Transactional
    public void huyHoaDonCho(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hoa don khong ton tai"));

        if (!KENH_BAN_TAI_QUAY_EQUALS(hoaDon.getKenhBan())) {
            throw new BusinessException("Chi ho tro huy hoa don tai quay");
        }

        if (!TRANG_THAI_HOA_DON_CHO_EQUALS(hoaDon.getTrangThai())) {
            throw new BusinessException("Chi duoc huy hoa don dang cho");
        }

        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDonId);
        for (HoaDonChiTiet item : items) {
            GiayChiTiet giayChiTiet = item.getGiayChiTiet();
            giayChiTiet.setSoLuong((giayChiTiet.getSoLuong() == null ? 0 : giayChiTiet.getSoLuong()) + item.getSoLuong());
            giayChiTiet.setNgayCapNhat(Instant.now());
            giayChiTietRepository.save(giayChiTiet);
            item.setTrangThai(0);
            hoaDonChiTietRepository.save(item);
        }

        hoaDon.setTrangThai(TRANG_THAI_DA_HUY);
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDon.setGhiChu("Hoa don cho da bi huy");
        hoaDonRepository.save(hoaDon);
    }

    @Transactional
    public ThanhToanTaiQuayResponse thanhToanTaiQuay(ThanhToanTaiQuayRequest request) {
        HoaDon hoaDon = request.hoaDonId() == null
                ? taoHoaDon(
                request.khachHangId(),
                request.tenKhachHang(),
                request.soDienThoai(),
                request.items(),
                TRANG_THAI_DA_THANH_TOAN,
                request.ghiChu()
        )
                : thanhToanHoaDonCho(request);

        BigDecimal tongTien = hoaDon.getTongTienThanhToan();
        BigDecimal tienKhachDua = xacDinhTienKhachDua(request.hinhThucThanhToan(), request.tienKhachDua(), tongTien);
        BigDecimal tienThua = tinhTienThua(request.hinhThucThanhToan(), tienKhachDua, tongTien);

        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setHoaDon(hoaDon);
        thanhToan.setHinhThuc(mapHinhThucThanhToan(request.hinhThucThanhToan()));
        thanhToan.setSoTien(tongTien);
        thanhToan.setTienThoiLai(tienThua);
        thanhToan.setCongThanhToan(resolveCongThanhToan(request.hinhThucThanhToan()));
        thanhToan.setNgayThanhToan(Instant.now());
        thanhToan.setTrangThai(1);
        thanhToan.setGhiChu(request.ghiChu());
        thanhToan.setNgayTao(Instant.now());
        thanhToanRepository.save(thanhToan);

        hoaDon.setTrangThai(TRANG_THAI_DA_THANH_TOAN);
        hoaDon.setNgayThanhToan(Instant.now());
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hoaDon);

        return new ThanhToanTaiQuayResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                tongTien,
                tienKhachDua,
                tienThua,
                request.hinhThucThanhToan(),
                hoaDon.getTenNguoiNhan(),
                hoaDon.getSdtNguoiNhan(),
                hoaDon.getNgayThanhToan()
        );
    }

    @Transactional(readOnly = true)
    public List<HoaDonChoTomTatResponse> layDanhSachHoaDonCho() {
        return hoaDonRepository.findTop10ByKenhBanAndTrangThaiOrderByNgayTaoDesc(KENH_BAN_TAI_QUAY, TRANG_THAI_HOA_DON_CHO)
                .stream()
                .map(hoaDon -> {
                    List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId());
                    int tongSanPham = items.stream().mapToInt(HoaDonChiTiet::getSoLuong).sum();
                    return new HoaDonChoTomTatResponse(
                            hoaDon.getId(),
                            hoaDon.getMa(),
                            hoaDon.getTenNguoiNhan(),
                            hoaDon.getSdtNguoiNhan(),
                            tongSanPham,
                            hoaDon.getTongTienThanhToan(),
                            hoaDon.getNgayTao()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public HoaDonChoChiTietResponse layChiTietHoaDonCho(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hoa don khong ton tai"));
        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDonId);
        return mapHoaDonChiTiet(hoaDon, items);
    }

    private HoaDonChiTiet taoDongHoaDon(Integer chiTietId, Integer soLuong) {
        GiayChiTiet giayChiTiet = giayChiTietRepository.findById(chiTietId)
                .orElseThrow(() -> new ResourceNotFoundException("San pham chi tiet khong ton tai"));

        if (giayChiTiet.getKichHoat() == null || giayChiTiet.getKichHoat() != 1) {
            throw new BusinessException("San pham da ngung kinh doanh");
        }

        if (giayChiTiet.getSoLuong() == null || giayChiTiet.getSoLuong() < soLuong) {
            throw new BusinessException("So luong ton khong du cho san pham " + giayChiTiet.getGiay().getTen());
        }

        giayChiTiet.setSoLuong(giayChiTiet.getSoLuong() - soLuong);
        giayChiTiet.setNgayCapNhat(Instant.now());
        giayChiTietRepository.save(giayChiTiet);

        HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
        hoaDonChiTiet.setGiayChiTiet(giayChiTiet);
        hoaDonChiTiet.setSoLuong(soLuong);
        hoaDonChiTiet.setGiaDonVi(giayChiTiet.getGiaBan());
        hoaDonChiTiet.setThanhTien(giayChiTiet.getGiaBan().multiply(BigDecimal.valueOf(soLuong)));
        hoaDonChiTiet.setTrangThai(1);
        hoaDonChiTiet.setNgayTao(Instant.now());
        return hoaDonChiTiet;
    }

    private HoaDon taoHoaDon(
            UUID khachHangId,
            String tenKhachHangInput,
            String soDienThoaiInput,
            List<TaoHoaDonChoItemRequest> items,
            Integer trangThai,
            String ghiChu
    ) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Hoa don phai co it nhat mot san pham");
        }

        validateDuplicateItems(items);

        List<HoaDonChiTiet> chiTietTam = items.stream()
                .map(item -> taoDongHoaDon(item.chiTietId(), item.soLuong()))
                .toList();

        BigDecimal tongTienHang = chiTietTam.stream()
                .map(HoaDonChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        KhachHang khachHang = timKhachHang(khachHangId);
        String tenKhachHang = layTenKhachHang(khachHang, tenKhachHangInput);
        String soDienThoai = laySoDienThoai(khachHang, soDienThoaiInput);

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMa(taoMaHoaDon());
        hoaDon.setKenhBan(KENH_BAN_TAI_QUAY);
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTenNguoiNhan(tenKhachHang);
        hoaDon.setSdtNguoiNhan(soDienThoai);
        hoaDon.setDiaChiGiaoHang(DIA_CHI_TAI_QUAY);
        hoaDon.setNgayLap(Instant.now());
        hoaDon.setTrangThai(trangThai);
        hoaDon.setTongTienHang(tongTienHang);
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(tongTienHang);
        hoaDon.setGhiChu(ghiChu);
        hoaDon.setNgayTao(Instant.now());
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        List<HoaDonChiTiet> chiTietCanLuu = new ArrayList<>();
        for (HoaDonChiTiet item : chiTietTam) {
            item.setHoaDon(savedHoaDon);
            chiTietCanLuu.add(hoaDonChiTietRepository.save(item));
        }

        return savedHoaDon;
    }

    private void validateDuplicateItems(List<TaoHoaDonChoItemRequest> items) {
        long distinctCount = items.stream()
                .map(TaoHoaDonChoItemRequest::chiTietId)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            throw new BusinessException("Moi san pham chi duoc xuat hien mot lan trong hoa don");
        }
    }

    private HoaDon thanhToanHoaDonCho(ThanhToanTaiQuayRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(request.hoaDonId())
                .orElseThrow(() -> new ResourceNotFoundException("Hoa don khong ton tai"));

        if (!KENH_BAN_TAI_QUAY_EQUALS(hoaDon.getKenhBan())) {
            throw new BusinessException("Chi ho tro thanh toan hoa don tai quay");
        }

        if (!TRANG_THAI_HOA_DON_CHO_EQUALS(hoaDon.getTrangThai())) {
            throw new BusinessException("Hoa don nay khong o trang thai cho thanh toan");
        }

        return hoaDon;
    }

    private boolean KENH_BAN_TAI_QUAY_EQUALS(Integer kenhBan) {
        return kenhBan != null && kenhBan == KENH_BAN_TAI_QUAY;
    }

    private boolean TRANG_THAI_HOA_DON_CHO_EQUALS(Integer trangThai) {
        return trangThai != null && trangThai == TRANG_THAI_HOA_DON_CHO;
    }

    private BigDecimal xacDinhTienKhachDua(Integer hinhThuc, BigDecimal tienKhachDua, BigDecimal tongTien) {
        if (hinhThuc == null) {
            throw new BusinessException("Hinh thuc thanh toan khong hop le");
        }

        if (hinhThuc == HINH_THUC_TIEN_MAT) {
            if (tienKhachDua == null || tienKhachDua.compareTo(tongTien) < 0) {
                throw new BusinessException("Tien khach dua phai lon hon hoac bang tong tien");
            }
            return tienKhachDua;
        }

        return tienKhachDua == null || tienKhachDua.compareTo(BigDecimal.ZERO) <= 0 ? tongTien : tienKhachDua;
    }

    private BigDecimal tinhTienThua(Integer hinhThuc, BigDecimal tienKhachDua, BigDecimal tongTien) {
        if (hinhThuc != null && hinhThuc == HINH_THUC_TIEN_MAT) {
            return tienKhachDua.subtract(tongTien);
        }
        return BigDecimal.ZERO;
    }

    private Integer mapHinhThucThanhToan(Integer hinhThucUi) {
        if (hinhThucUi == null) {
            throw new BusinessException("Hinh thuc thanh toan khong hop le");
        }
        if (hinhThucUi == 4) {
            return HINH_THUC_CHUYEN_KHOAN;
        }
        if (hinhThucUi < HINH_THUC_TIEN_MAT || hinhThucUi > 4) {
            throw new BusinessException("Hinh thuc thanh toan khong duoc ho tro");
        }
        return hinhThucUi == 3 ? HINH_THUC_VI : hinhThucUi;
    }

    private String resolveCongThanhToan(Integer hinhThucUi) {
        return switch (hinhThucUi) {
            case 2 -> "Chuyen khoan";
            case 3 -> "Vi dien tu";
            case 4 -> "The/POS";
            default -> null;
        };
    }

    private HoaDonChoChiTietResponse mapHoaDonChiTiet(HoaDon hoaDon, List<HoaDonChiTiet> items) {
        return new HoaDonChoChiTietResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                hoaDon.getTenNguoiNhan(),
                hoaDon.getSdtNguoiNhan(),
                hoaDon.getTongTienThanhToan(),
                hoaDon.getNgayTao(),
                items.stream()
                        .map(item -> new HoaDonChoDongSanPhamResponse(
                                item.getGiayChiTiet().getId(),
                                item.getGiayChiTiet().getGiay().getMa(),
                                item.getGiayChiTiet().getGiay().getTen(),
                                item.getSoLuong(),
                                item.getGiaDonVi(),
                                item.getThanhTien()
                        ))
                        .toList()
        );
    }

    private KhachHang timKhachHang(UUID khachHangId) {
        if (khachHangId == null) {
            return null;
        }
        return khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Khach hang khong ton tai"));
    }

    private String layTenKhachHang(KhachHang khachHang, String tenKhachHang) {
        if (khachHang != null && khachHang.getHoTen() != null && !khachHang.getHoTen().isBlank()) {
            return khachHang.getHoTen();
        }
        if (tenKhachHang != null && !tenKhachHang.isBlank()) {
            return tenKhachHang.trim();
        }
        return "Khach le";
    }

    private String laySoDienThoai(KhachHang khachHang, String soDienThoai) {
        if (khachHang != null && khachHang.getSdt() != null && !khachHang.getSdt().isBlank()) {
            return khachHang.getSdt();
        }
        if (soDienThoai != null && !soDienThoai.isBlank()) {
            return soDienThoai.trim();
        }
        return "Khong co";
    }

    private String taoMaHoaDon() {
        return "HDCHO-" + System.currentTimeMillis();
    }

    private String chuanHoaTuKhoa(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
