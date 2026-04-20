package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ApDungPhieuGiamGiaRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoItemRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.ThanhToanTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoChiTietResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoDongSanPhamResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoTomTatResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.KhachHangTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.PhieuGiamGiaTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.SanPhamTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThanhToanTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThongTinPhieuGiamGiaHoaDonResponse;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.LichSuHoaDon;
import com.example.server.entity.PhieuGiamGia;
import com.example.server.entity.PhieuGiamGiaKhachHang;
import com.example.server.entity.ThanhToan;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.LichSuHoaDonRepository;
import com.example.server.repository.PhieuGiamGiaKhachHangRepository;
import com.example.server.repository.PhieuGiamGiaRepository;
import com.example.server.repository.ThanhToanRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BanHangTaiQuayService {

    private static final int SO_SAN_PHAM_TIM_TOI_DA = 50;
    private static final int KENH_BAN_TAI_QUAY = 1;
    private static final int TRANG_THAI_HOA_DON_CHO_XAC_NHAN = 1;
    private static final int TRANG_THAI_HOA_DON_HOAN_THANH = 5;
    private static final int TRANG_THAI_HOA_DON_HUY = 6;
    private static final int HINH_THUC_TIEN_MAT = 1;
    private static final int HINH_THUC_CHUYEN_KHOAN = 2;
    private static final int HINH_THUC_VI = 3;
    private static final int LOAI_PHIEU_PHAN_TRAM = 1;
    private static final int LOAI_PHIEU_TIEN_MAT = 2;
    private static final int LOAI_PHIEU_MIEN_PHI_VAN_CHUYEN = 3;
    private static final int TRANG_THAI_PHIEU_HOAT_DONG = 1;
    private static final int TRANG_THAI_PHIEU_THEO_KH_DA_DUNG = 0;
    private static final int TRANG_THAI_PHIEU_THEO_KH_CHUA_DUNG = 1;
    private static final String DIA_CHI_TAI_QUAY = "Mua tai quay";

    private final KhachHangRepository khachHangRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;

    public BanHangTaiQuayService(
            KhachHangRepository khachHangRepository,
            GiayChiTietRepository giayChiTietRepository,
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanRepository thanhToanRepository,
            LichSuHoaDonRepository lichSuHoaDonRepository,
            PhieuGiamGiaRepository phieuGiamGiaRepository,
            PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository
    ) {
        this.khachHangRepository = khachHangRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.lichSuHoaDonRepository = lichSuHoaDonRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.phieuGiamGiaKhachHangRepository = phieuGiamGiaKhachHangRepository;
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
                .limit(SO_SAN_PHAM_TIM_TOI_DA)
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

    @Transactional(readOnly = true)
    public List<PhieuGiamGiaTaiQuayResponse> timPhieuGiamGia(
            String keyword,
            Integer hoaDonId,
            UUID khachHangId,
            BigDecimal tongTienHang
    ) {
        HoaDon hoaDonHienTai = layHoaDonTaiQuayNeuCo(hoaDonId);
        BigDecimal tongTienHangHienTai = tongTienHang != null
                ? tongTienHang
                : (hoaDonHienTai != null ? hoaDonHienTai.getTongTienHang() : BigDecimal.ZERO);

        if (tongTienHangHienTai == null || tongTienHangHienTai.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        KhachHang khachHang = timKhachHang(khachHangId);
        List<PhieuGiamGiaTaiQuayResponse> ketQua = new ArrayList<>();

        for (PhieuGiamGia phieuGiamGia : phieuGiamGiaRepository.searchByKeyword(
                chuanHoaTuKhoa(keyword),
                PageRequest.of(0, 20)
        )) {
            try {
                validatePhieuGiamGia(phieuGiamGia, khachHang, tongTienHangHienTai, hoaDonHienTai);
                BigDecimal soTienGiam = tinhSoTienGiam(phieuGiamGia, tongTienHangHienTai);
                BigDecimal tongTienSauGiam = tongTienHangHienTai.subtract(soTienGiam);
                ketQua.add(mapPhieuGiamGiaTaiQuayResponse(
                        new PhieuGiamGiaDuocApDung(phieuGiamGia, soTienGiam, tongTienSauGiam)
                ));
            } catch (BusinessException exception) {
                // Bo qua cac phieu khong hop le voi gio hang hien tai.
            }

            if (ketQua.size() >= 8) {
                break;
            }
        }

        return ketQua;
    }

    @Transactional(readOnly = true)
    public PhieuGiamGiaTaiQuayResponse apDungPhieuGiamGia(ApDungPhieuGiamGiaRequest request) {
        HoaDon hoaDonHienTai = layHoaDonTaiQuayNeuCo(request.hoaDonId());
        BigDecimal tongTienHang = xacDinhTongTienHangKhiApPhieu(request, hoaDonHienTai);
        KhachHang khachHang = timKhachHang(request.khachHangId());
        PhieuGiamGiaDuocApDung phieuGiamGia = tinhPhieuGiamGiaHopLe(
                request.maPhieuGiamGia(),
                khachHang,
                tongTienHang,
                true,
                hoaDonHienTai
        );
        return mapPhieuGiamGiaTaiQuayResponse(phieuGiamGia);
    }

    @Transactional
    public HoaDonChoChiTietResponse taoHoaDonCho(TaoHoaDonChoRequest request) {
        HoaDon savedHoaDon = taoHoaDon(
                request.khachHangId(),
                request.tenKhachHang(),
                request.soDienThoai(),
                request.maPhieuGiamGia(),
                request.items(),
                TRANG_THAI_HOA_DON_CHO_XAC_NHAN,
                "Hoa don cho tao tu man hinh ban hang tai quay"
        );
        luuLichSuHoaDon(savedHoaDon, TRANG_THAI_HOA_DON_CHO_XAC_NHAN, savedHoaDon.getGhiChu());
        List<HoaDonChiTiet> savedItems = hoaDonChiTietRepository.findByHoaDonIdWithProduct(savedHoaDon.getId());

        return mapHoaDonChiTiet(savedHoaDon, savedItems);
    }

    @Transactional
    public void huyHoaDonCho(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hoa don khong ton tai"));

        if (!kenhBanTaiQuay(hoaDon.getKenhBan())) {
            throw new BusinessException("Chi ho tro huy hoa don tai quay");
        }

        if (!trangThaiHoaDonCho(hoaDon.getTrangThai())) {
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

        if (hoaDon.getPhieuGiamGia() != null) {
            giaiPhongPhieuGiamGia(hoaDon.getPhieuGiamGia(), hoaDon.getKhachHang());
            hoaDon.setPhieuGiamGia(null);
            hoaDon.setTienGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(hoaDon.getTongTienHang());
        }

        hoaDon.setTrangThai(TRANG_THAI_HOA_DON_HUY);
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDon.setGhiChu("Hoa don cho da bi huy");
        hoaDonRepository.save(hoaDon);
        luuLichSuHoaDon(hoaDon, TRANG_THAI_HOA_DON_HUY, hoaDon.getGhiChu());
    }

    @Transactional
    public ThanhToanTaiQuayResponse thanhToanTaiQuay(ThanhToanTaiQuayRequest request) {
        HoaDon hoaDon = request.hoaDonId() == null
                ? taoHoaDon(
                request.khachHangId(),
                request.tenKhachHang(),
                request.soDienThoai(),
                request.maPhieuGiamGia(),
                request.items(),
                TRANG_THAI_HOA_DON_HOAN_THANH,
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

        hoaDon.setTrangThai(TRANG_THAI_HOA_DON_HOAN_THANH);
        hoaDon.setNgayThanhToan(Instant.now());
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hoaDon);
        luuLichSuHoaDon(hoaDon, TRANG_THAI_HOA_DON_HOAN_THANH, request.ghiChu());

        return new ThanhToanTaiQuayResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null,
                hoaDon.getTongTienHang(),
                hoaDon.getTienGiam(),
                tongTien,
                tienKhachDua,
                tienThua,
                request.hinhThucThanhToan(),
                hoaDon.getTenNguoiNhan(),
                hoaDon.getSdtNguoiNhan(),
                mapThongTinPhieuGiamGiaHoaDon(hoaDon),
                hoaDon.getNgayThanhToan()
        );
    }

    @Transactional(readOnly = true)
    public List<HoaDonChoTomTatResponse> layDanhSachHoaDonCho() {
        return hoaDonRepository.findTop10ByKenhBanAndTrangThaiOrderByNgayTaoDesc(
                        KENH_BAN_TAI_QUAY,
                        TRANG_THAI_HOA_DON_CHO_XAC_NHAN
                )
                .stream()
                .map(hoaDon -> {
                    List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId());
                    int tongSanPham = items.stream().mapToInt(HoaDonChiTiet::getSoLuong).sum();
                    return new HoaDonChoTomTatResponse(
                            hoaDon.getId(),
                            hoaDon.getMa(),
                            hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null,
                            hoaDon.getTenNguoiNhan(),
                            hoaDon.getSdtNguoiNhan(),
                            tongSanPham,
                            hoaDon.getTongTienHang(),
                            hoaDon.getTienGiam(),
                            hoaDon.getTongTienThanhToan(),
                            mapThongTinPhieuGiamGiaHoaDon(hoaDon),
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

    private HoaDon layHoaDonTaiQuayNeuCo(Integer hoaDonId) {
        if (hoaDonId == null) {
            return null;
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hoa don khong ton tai"));

        if (!kenhBanTaiQuay(hoaDon.getKenhBan())) {
            throw new BusinessException("Chi ho tro ap dung phieu giam gia cho hoa don tai quay");
        }

        return hoaDon;
    }

    private HoaDonChiTiet taoDongHoaDon(Integer chiTietId, Integer soLuong) {
        GiayChiTiet giayChiTiet = layGiayChiTietHopLe(chiTietId, soLuong);

        giayChiTiet.setSoLuong(giayChiTiet.getSoLuong() - soLuong);
        giayChiTiet.setNgayCapNhat(Instant.now());
        giayChiTietRepository.save(giayChiTiet);

        HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
        hoaDonChiTiet.setGiayChiTiet(giayChiTiet);
        hoaDonChiTiet.setSoLuong(soLuong);
        hoaDonChiTiet.setGiaDonVi(giayChiTiet.getGiaBan());
        hoaDonChiTiet.setThanhTien(giayChiTiet.getGiaBan().multiply(BigDecimal.valueOf(soLuong.longValue())));
        hoaDonChiTiet.setTrangThai(1);
        hoaDonChiTiet.setNgayTao(Instant.now());
        return hoaDonChiTiet;
    }

    private GiayChiTiet layGiayChiTietHopLe(Integer chiTietId, Integer soLuong) {
        GiayChiTiet giayChiTiet = giayChiTietRepository.findById(chiTietId)
                .orElseThrow(() -> new ResourceNotFoundException("San pham chi tiet khong ton tai"));

        if (giayChiTiet.getKichHoat() == null || giayChiTiet.getKichHoat() != 1) {
            throw new BusinessException("San pham da ngung kinh doanh");
        }

        if (giayChiTiet.getSoLuong() == null || giayChiTiet.getSoLuong() < soLuong) {
            throw new BusinessException("So luong ton khong du cho san pham " + giayChiTiet.getGiay().getTen());
        }

        return giayChiTiet;
    }

    private BigDecimal xacDinhTongTienHangKhiApPhieu(ApDungPhieuGiamGiaRequest request, HoaDon hoaDonHienTai) {
        if (request.items() != null && !request.items().isEmpty()) {
            return tinhTongTienHangTam(request.items());
        }

        if (hoaDonHienTai != null) {
            return hoaDonHienTai.getTongTienHang();
        }

        throw new BusinessException("Hoa don phai co it nhat mot san pham");
    }

    private BigDecimal tinhTongTienHangTam(List<TaoHoaDonChoItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Hoa don phai co it nhat mot san pham");
        }

        validateDuplicateItems(items);

        return items.stream()
                .map(item -> {
                    GiayChiTiet giayChiTiet = giayChiTietRepository.findById(item.chiTietId())
                            .orElseThrow(() -> new ResourceNotFoundException("San pham chi tiet khong ton tai"));
                    if (giayChiTiet.getKichHoat() == null || giayChiTiet.getKichHoat() != 1) {
                        throw new BusinessException("San pham da ngung kinh doanh");
                    }
                    return giayChiTiet.getGiaBan().multiply(BigDecimal.valueOf(item.soLuong().longValue()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private HoaDon taoHoaDon(
            UUID khachHangId,
            String tenKhachHangInput,
            String soDienThoaiInput,
            String maPhieuGiamGia,
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
        hoaDon.setTenNguoiNhan(tenKhachHang);
        hoaDon.setSdtNguoiNhan(soDienThoai);
        hoaDon.setDiaChiGiaoHang(DIA_CHI_TAI_QUAY);
        hoaDon.setNgayLap(Instant.now());
        hoaDon.setTrangThai(trangThai);
        hoaDon.setTongTienHang(tongTienHang);
        hoaDon.setGhiChu(ghiChu);
        hoaDon.setNgayTao(Instant.now());
        ganPhieuGiamGiaChoHoaDon(hoaDon, maPhieuGiamGia, khachHang, tongTienHang);
        hoaDon.setKhachHang(khachHang);
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

        if (!kenhBanTaiQuay(hoaDon.getKenhBan())) {
            throw new BusinessException("Chi ho tro thanh toan hoa don tai quay");
        }

        if (!trangThaiHoaDonCho(hoaDon.getTrangThai())) {
            throw new BusinessException("Hoa don nay khong o trang thai cho thanh toan");
        }

        KhachHang khachHang = timKhachHang(request.khachHangId());
        String tenKhachHang = layTenKhachHang(khachHang, request.tenKhachHang());
        String soDienThoai = laySoDienThoai(khachHang, request.soDienThoai());

        ganPhieuGiamGiaChoHoaDon(hoaDon, request.maPhieuGiamGia(), khachHang, hoaDon.getTongTienHang());
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTenNguoiNhan(tenKhachHang);
        hoaDon.setSdtNguoiNhan(soDienThoai);
        hoaDon.setGhiChu(request.ghiChu());
        hoaDon.setNgayCapNhat(Instant.now());
        return hoaDonRepository.save(hoaDon);
    }

    private void ganPhieuGiamGiaChoHoaDon(
            HoaDon hoaDon,
            String maPhieuGiamGia,
            KhachHang khachHang,
            BigDecimal tongTienHang
    ) {
        PhieuGiamGia phieuDangGan = hoaDon.getPhieuGiamGia();
        KhachHang khachHangDangGan = hoaDon.getKhachHang();

        if (phieuDangGan != null) {
            giaiPhongPhieuGiamGia(phieuDangGan, khachHangDangGan);
        }

        PhieuGiamGiaDuocApDung phieuMoi = tinhPhieuGiamGiaHopLe(
                maPhieuGiamGia,
                khachHang,
                tongTienHang,
                false,
                hoaDon
        );
        if (phieuMoi == null) {
            hoaDon.setPhieuGiamGia(null);
            hoaDon.setTienGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(tongTienHang);
            return;
        }

        datChoPhieuGiamGia(phieuMoi.phieuGiamGia(), khachHang);
        hoaDon.setPhieuGiamGia(phieuMoi.phieuGiamGia());
        hoaDon.setTienGiam(phieuMoi.soTienGiam());
        hoaDon.setTongTienThanhToan(phieuMoi.tongTienSauGiam());
    }

    private PhieuGiamGiaDuocApDung tinhPhieuGiamGiaHopLe(
            String maPhieuGiamGia,
            KhachHang khachHang,
            BigDecimal tongTienHang,
            boolean batBuocNhapMa,
            HoaDon hoaDonHienTai
    ) {
        String maPhieu = maPhieuGiamGia == null ? null : maPhieuGiamGia.trim();
        if (maPhieu == null || maPhieu.isBlank()) {
            if (batBuocNhapMa) {
                throw new BusinessException("Vui long nhap ma phieu giam gia");
            }
            return null;
        }

        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findByMaIgnoreCase(maPhieu)
                .orElseThrow(() -> new BusinessException("Khong tim thay phieu giam gia phu hop"));

        validatePhieuGiamGia(phieuGiamGia, khachHang, tongTienHang, hoaDonHienTai);
        BigDecimal soTienGiam = tinhSoTienGiam(phieuGiamGia, tongTienHang);
        BigDecimal tongTienSauGiam = tongTienHang.subtract(soTienGiam);

        return new PhieuGiamGiaDuocApDung(phieuGiamGia, soTienGiam, tongTienSauGiam);
    }

    private void validatePhieuGiamGia(
            PhieuGiamGia phieuGiamGia,
            KhachHang khachHang,
            BigDecimal tongTienHang,
            HoaDon hoaDonHienTai
    ) {
        Instant now = Instant.now();
        boolean dangGanChoHoaDonHienTai = hoaDonHienTai != null
                && hoaDonHienTai.getPhieuGiamGia() != null
                && hoaDonHienTai.getPhieuGiamGia().getId().equals(phieuGiamGia.getId());
        boolean dangGanCungKhachHang = dangGanChoHoaDonHienTai
                && khachHang != null
                && hoaDonHienTai.getKhachHang() != null
                && hoaDonHienTai.getKhachHang().getId().equals(khachHang.getId());

        if (phieuGiamGia.getTrangThai() == null || phieuGiamGia.getTrangThai() != TRANG_THAI_PHIEU_HOAT_DONG) {
            throw new BusinessException("Phieu giam gia hien khong hoat dong");
        }

        if (phieuGiamGia.getNgayBatDau() != null && now.isBefore(phieuGiamGia.getNgayBatDau())) {
            throw new BusinessException("Phieu giam gia chua den thoi gian ap dung");
        }

        if (phieuGiamGia.getNgayKetThuc() != null && now.isAfter(phieuGiamGia.getNgayKetThuc())) {
            throw new BusinessException("Phieu giam gia da het han");
        }

        int soLuongConLai = (phieuGiamGia.getSoLuong() == null ? 0 : phieuGiamGia.getSoLuong())
                - (phieuGiamGia.getSoLuongDaDung() == null ? 0 : phieuGiamGia.getSoLuongDaDung())
                + (dangGanChoHoaDonHienTai ? 1 : 0);
        if (soLuongConLai <= 0) {
            throw new BusinessException("Phieu giam gia da het luot su dung");
        }

        if (phieuGiamGia.getLoai() != null && phieuGiamGia.getLoai() == LOAI_PHIEU_MIEN_PHI_VAN_CHUYEN) {
            throw new BusinessException("Phieu mien phi van chuyen khong ap dung cho ban hang tai quay");
        }

        if (phieuGiamGia.getGiaTriToiThieu() != null && tongTienHang.compareTo(phieuGiamGia.getGiaTriToiThieu()) < 0) {
            throw new BusinessException("Hoa don chua dat gia tri toi thieu de ap dung phieu");
        }

        if (phieuGiamGiaKhachHangRepository.existsByPhieuGiamGiaId(phieuGiamGia.getId())) {
            if (khachHang == null) {
                throw new BusinessException("Phieu giam gia nay chi danh cho khach hang cu the");
            }

            PhieuGiamGiaKhachHang phieuTheoKhach = phieuGiamGiaKhachHangRepository
                    .findByPhieuGiamGiaIdAndKhachHangId(phieuGiamGia.getId(), khachHang.getId())
                    .orElseThrow(() -> new BusinessException("Khach hang hien tai khong duoc ap dung phieu nay"));

            if (phieuTheoKhach.getTrangThai() == null
                    || (phieuTheoKhach.getTrangThai() != TRANG_THAI_PHIEU_THEO_KH_CHUA_DUNG && !dangGanCungKhachHang)) {
                throw new BusinessException("Phieu giam gia nay da duoc su dung cho khach hang nay");
            }
        }
    }

    private BigDecimal tinhSoTienGiam(PhieuGiamGia phieuGiamGia, BigDecimal tongTienHang) {
        BigDecimal soTienGiam;

        if (phieuGiamGia.getLoai() != null && phieuGiamGia.getLoai() == LOAI_PHIEU_PHAN_TRAM) {
            soTienGiam = tongTienHang
                    .multiply(phieuGiamGia.getGiaTri())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (phieuGiamGia.getGiamToiDa() != null && soTienGiam.compareTo(phieuGiamGia.getGiamToiDa()) > 0) {
                soTienGiam = phieuGiamGia.getGiamToiDa();
            }
        } else if (phieuGiamGia.getLoai() != null && phieuGiamGia.getLoai() == LOAI_PHIEU_TIEN_MAT) {
            soTienGiam = phieuGiamGia.getGiaTri();
        } else {
            throw new BusinessException("Loai phieu giam gia khong duoc ho tro");
        }

        if (soTienGiam.compareTo(BigDecimal.ZERO) < 0) {
            soTienGiam = BigDecimal.ZERO;
        }

        if (soTienGiam.compareTo(tongTienHang) > 0) {
            soTienGiam = tongTienHang;
        }

        return soTienGiam;
    }

    private void datChoPhieuGiamGia(PhieuGiamGia phieuGiamGia, KhachHang khachHang) {
        phieuGiamGia.setSoLuongDaDung((phieuGiamGia.getSoLuongDaDung() == null ? 0 : phieuGiamGia.getSoLuongDaDung()) + 1);
        phieuGiamGia.setNgayCapNhat(Instant.now());
        phieuGiamGiaRepository.save(phieuGiamGia);
        capNhatPhieuTheoKhach(phieuGiamGia, khachHang, TRANG_THAI_PHIEU_THEO_KH_DA_DUNG, Instant.now());
    }

    private void giaiPhongPhieuGiamGia(PhieuGiamGia phieuGiamGia, KhachHang khachHang) {
        int soLuongDaDung = phieuGiamGia.getSoLuongDaDung() == null ? 0 : phieuGiamGia.getSoLuongDaDung();
        phieuGiamGia.setSoLuongDaDung(Math.max(soLuongDaDung - 1, 0));
        phieuGiamGia.setNgayCapNhat(Instant.now());
        phieuGiamGiaRepository.save(phieuGiamGia);
        capNhatPhieuTheoKhach(phieuGiamGia, khachHang, TRANG_THAI_PHIEU_THEO_KH_CHUA_DUNG, null);
    }

    private void capNhatPhieuTheoKhach(
            PhieuGiamGia phieuGiamGia,
            KhachHang khachHang,
            Integer trangThai,
            Instant ngaySuDung
    ) {
        if (!phieuGiamGiaKhachHangRepository.existsByPhieuGiamGiaId(phieuGiamGia.getId())) {
            return;
        }

        if (khachHang == null) {
            throw new BusinessException("Phieu giam gia nay can thong tin khach hang");
        }

        PhieuGiamGiaKhachHang phieuTheoKhach = phieuGiamGiaKhachHangRepository
                .findByPhieuGiamGiaIdAndKhachHangId(phieuGiamGia.getId(), khachHang.getId())
                .orElseThrow(() -> new BusinessException("Khach hang hien tai khong duoc ap dung phieu nay"));

        phieuTheoKhach.setTrangThai(trangThai);
        phieuTheoKhach.setNgaySuDung(ngaySuDung);
        phieuGiamGiaKhachHangRepository.save(phieuTheoKhach);
    }

    private boolean kenhBanTaiQuay(Integer kenhBan) {
        return kenhBan != null && kenhBan == KENH_BAN_TAI_QUAY;
    }

    private boolean trangThaiHoaDonCho(Integer trangThai) {
        return trangThai != null && trangThai == TRANG_THAI_HOA_DON_CHO_XAC_NHAN;
    }

    private void luuLichSuHoaDon(HoaDon hoaDon, Integer trangThai, String ghiChu) {
        if (hoaDon == null || trangThai == null) {
            return;
        }

        LichSuHoaDon lichSuHoaDon = new LichSuHoaDon();
        lichSuHoaDon.setHoaDon(hoaDon);
        lichSuHoaDon.setNhanVien(hoaDon.getNhanVien());
        lichSuHoaDon.setTrangThai(labelTrangThaiHoaDon(trangThai));
        lichSuHoaDon.setGhiChu(ghiChu);
        lichSuHoaDon.setNgayTao(Instant.now());
        lichSuHoaDonRepository.save(lichSuHoaDon);
    }

    private String labelTrangThaiHoaDon(Integer trangThai) {
        return switch (trangThai) {
            case TRANG_THAI_HOA_DON_CHO_XAC_NHAN -> "Ch\u1edd x\u00e1c nh\u1eadn";
            case TRANG_THAI_HOA_DON_HOAN_THANH -> "Ho\u00e0n th\u00e0nh";
            case TRANG_THAI_HOA_DON_HUY -> "H\u1ee7y";
            default -> "Ch\u1edd x\u00e1c nh\u1eadn";
        };
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
                hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null,
                hoaDon.getTenNguoiNhan(),
                hoaDon.getSdtNguoiNhan(),
                hoaDon.getTongTienHang(),
                hoaDon.getTienGiam(),
                hoaDon.getTongTienThanhToan(),
                mapThongTinPhieuGiamGiaHoaDon(hoaDon),
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

    private ThongTinPhieuGiamGiaHoaDonResponse mapThongTinPhieuGiamGiaHoaDon(HoaDon hoaDon) {
        if (hoaDon.getPhieuGiamGia() == null) {
            return null;
        }

        return new ThongTinPhieuGiamGiaHoaDonResponse(
                hoaDon.getPhieuGiamGia().getMa(),
                hoaDon.getPhieuGiamGia().getTen(),
                hoaDon.getTienGiam()
        );
    }

    private PhieuGiamGiaTaiQuayResponse mapPhieuGiamGiaTaiQuayResponse(PhieuGiamGiaDuocApDung phieuGiamGia) {
        return new PhieuGiamGiaTaiQuayResponse(
                phieuGiamGia.phieuGiamGia().getId(),
                phieuGiamGia.phieuGiamGia().getMa(),
                phieuGiamGia.phieuGiamGia().getTen(),
                phieuGiamGia.phieuGiamGia().getLoai(),
                phieuGiamGia.phieuGiamGia().getGiaTri(),
                phieuGiamGia.phieuGiamGia().getGiaTriToiThieu(),
                phieuGiamGia.phieuGiamGia().getGiamToiDa(),
                phieuGiamGia.soTienGiam(),
                phieuGiamGia.tongTienHang(),
                phieuGiamGia.tongTienSauGiam()
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

    private record PhieuGiamGiaDuocApDung(
            PhieuGiamGia phieuGiamGia,
            BigDecimal soTienGiam,
            BigDecimal tongTienSauGiam
    ) {
        private BigDecimal tongTienHang() {
            return tongTienSauGiam.add(soTienGiam);
        }
    }
}
