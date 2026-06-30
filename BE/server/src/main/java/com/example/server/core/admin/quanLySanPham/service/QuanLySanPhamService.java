package com.example.server.core.admin.quanLySanPham.service;

import com.example.server.core.admin.quanLySanPham.dto.request.CapNhatBienTheRequest;
import com.example.server.core.admin.quanLySanPham.dto.request.CapNhatGiayRequest;
import com.example.server.core.admin.quanLySanPham.dto.request.CapNhatHinhAnhRequest;
import com.example.server.core.admin.quanLySanPham.dto.request.DoiTrangThaiBienTheRequest;
import com.example.server.core.admin.quanLySanPham.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.quanLySanPham.dto.request.TaoBienTheRequest;
import com.example.server.core.admin.quanLySanPham.dto.request.TaoChiTietSanPhamHangLoatItemRequest;
import com.example.server.core.admin.quanLySanPham.dto.request.TaoChiTietSanPhamHangLoatRequest;
import com.example.server.core.admin.quanLySanPham.dto.request.TaoChiTietSanPhamRequest;
import com.example.server.core.admin.quanLySanPham.dto.request.TaoGiayRequest;
import com.example.server.core.admin.quanLySanPham.dto.request.ThemHinhAnhRequest;
import com.example.server.core.admin.quanLySanPham.dto.response.BienTheResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.ChatLieuGiayOption;
import com.example.server.core.admin.quanLySanPham.dto.response.ChiTietSanPhamListItemResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.CoGiayOption;
import com.example.server.core.admin.quanLySanPham.dto.response.CongNgheDemOption;
import com.example.server.core.admin.quanLySanPham.dto.response.DanhMucSanPhamResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.DeGiayOption;
import com.example.server.core.admin.quanLySanPham.dto.response.GiayDetailResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.GiayListItemResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.HinhAnhGiayResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.KichCoOption;
import com.example.server.core.admin.quanLySanPham.dto.response.LoaiGiayOption;
import com.example.server.core.admin.quanLySanPham.dto.response.MauSacOption;
import com.example.server.core.admin.quanLySanPham.dto.response.TaoChiTietSanPhamHangLoatResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.TaoChiTietSanPhamResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.ThuocTinhResponse;
import com.example.server.core.admin.quanLySanPham.dto.response.ThuongHieuOption;
import com.example.server.core.admin.quanLySanPham.dto.response.TrongLuongOption;
import com.example.server.entity.*;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ErrorCode;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.*;
import com.example.server.utils.GiaySpecifications;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuanLySanPhamService {

    private static final int TRANG_THAI_NGUNG_KINH_DOANH = 0;
    private static final int TRANG_THAI_KINH_DOANH = 1;
    private static final int TRANG_THAI_HET_HANG = 2;
    private static final int GIOI_TINH_NAM = 1;
    private static final int GIOI_TINH_NU = 2;
    private static final int GIOI_TINH_UNISEX = 3;
    private static final int MIN_PRODUCT_NAME_LENGTH = 3;
    private static final int MAX_PRODUCT_NAME_LENGTH = 300;
    private static final int MAX_PRODUCT_DESCRIPTION_LENGTH = 2000;
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");

    private record ActiveDiscountInfo(
            Integer dotGiamGiaId,
            String maDotGiamGia,
            String tenDotGiamGia,
            Integer loaiGiam,
            BigDecimal giaTriGiam,
            BigDecimal giaSauGiam
    ) {}

    private final GiayRepository giayRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    private final GiayThuocTinhRepository giayThuocTinhRepository;
    private final HinhAnhGiayRepository hinhAnhGiayRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final LoaiGiayRepository loaiGiayRepository;
    private final MauSacRepository mauSacRepository;
    private final KichCoRepository kichCoRepository;
    private final DeGiayRepository deGiayRepository;
    private final CoGiayRepository coGiayRepository;
    private final ChatLieuGiayRepository chatLieuGiayRepository;
    private final TrongLuongRepository trongLuongRepository;
    private final CongNgheDemRepository congNgheDemRepository;
    private final com.example.server.core.realtime.sanpham.SanPhamRealtimePublisher sanPhamRealtimePublisher;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final HoaDonRepository hoaDonRepository;

    public QuanLySanPhamService(
            GiayRepository giayRepository,
            GiayChiTietRepository giayChiTietRepository,
            DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository,
            GiayThuocTinhRepository giayThuocTinhRepository,
            HinhAnhGiayRepository hinhAnhGiayRepository,
            ThuongHieuRepository thuongHieuRepository,
            LoaiGiayRepository loaiGiayRepository,
            MauSacRepository mauSacRepository,
            KichCoRepository kichCoRepository,
            DeGiayRepository deGiayRepository,
            CoGiayRepository coGiayRepository,
            ChatLieuGiayRepository chatLieuGiayRepository,
            TrongLuongRepository trongLuongRepository,
            CongNgheDemRepository congNgheDemRepository,
            com.example.server.core.realtime.sanpham.SanPhamRealtimePublisher sanPhamRealtimePublisher,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            HoaDonRepository hoaDonRepository
    ) {
        this.giayRepository = giayRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.dotGiamGiaSanPhamRepository = dotGiamGiaSanPhamRepository;
        this.giayThuocTinhRepository = giayThuocTinhRepository;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
        this.thuongHieuRepository = thuongHieuRepository;
        this.loaiGiayRepository = loaiGiayRepository;
        this.mauSacRepository = mauSacRepository;
        this.kichCoRepository = kichCoRepository;
        this.deGiayRepository = deGiayRepository;
        this.coGiayRepository = coGiayRepository;
        this.chatLieuGiayRepository = chatLieuGiayRepository;
        this.trongLuongRepository = trongLuongRepository;
        this.congNgheDemRepository = congNgheDemRepository;
        this.sanPhamRealtimePublisher = sanPhamRealtimePublisher;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.hoaDonRepository = hoaDonRepository;
    }

    // ─── Danh mục ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> checkTenTrung(String ten, Integer id) {
        boolean exists = false;
        Integer conflictingId = null;
        if (id != null && id > 0) {
            exists = giayRepository.existsByTenIgnoreCaseAndIdNot(ten, id);
        } else {
            Optional<Giay> conflicting = giayRepository.findFirstByTenIgnoreCase(ten);
            if (conflicting.isPresent()) {
                exists = true;
                conflictingId = conflicting.get().getId();
            }
        }
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("exists", exists);
        if (conflictingId != null) {
            result.put("id", conflictingId);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> checkMaTrung(String ma, Integer id) {
        boolean exists = false;
        Integer conflictingId = null;
        if (id != null && id > 0) {
            exists = giayRepository.existsByMaIgnoreCaseAndIdNot(ma, id);
        } else {
            Optional<Giay> conflicting = giayRepository.findFirstByMaIgnoreCase(ma);
            if (conflicting.isPresent()) {
                exists = true;
                conflictingId = conflicting.get().getId();
            }
        }
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("exists", exists);
        if (conflictingId != null) {
            result.put("id", conflictingId);
        }
        return result;
    }

    public GiayDetailResponse checkTrungThuocTinh(TaoGiayRequest req) {
        List<Giay> existing = giayRepository.findByThuocTinh(
                req.thuongHieuId(),
                req.loaiGiayId(),
                req.gioiTinh(),
                req.chatLieuGiayId(),
                req.deGiayId(),
                req.coGiayId(),
                req.congNgheDemId(),
                req.trongLuongId()
        );
        if (!existing.isEmpty()) {
            return chiTietGiay(existing.get(0).getId());
        }
        return null;
    }

    @Transactional(readOnly = true)
    public DanhMucSanPhamResponse layDanhMuc() {
        var loaiGiay = loaiGiayRepository.findAll().stream()
                .filter(l -> l.getTrangThai() != null && l.getTrangThai() == 1)
                .map(l -> new LoaiGiayOption(l.getId(), l.getTen())).toList();
        var thuongHieu = thuongHieuRepository.findAll().stream()
                .filter(t -> t.getTrangThai() != null && t.getTrangThai() == 1)
                .map(t -> new ThuongHieuOption(t.getId(), t.getTen(), t.getLogoUrl())).toList();
        var mauSac = mauSacRepository.findAll().stream()
                .filter(m -> m.getTrangThai() != null && m.getTrangThai() == 1)
                .map(m -> new MauSacOption(m.getId(), m.getTen(), m.getMaMauHex())).toList();
        var kichCo = kichCoRepository.findAll().stream()
                .filter(k -> k.getTrangThai() != null && k.getTrangThai() == 1)
                .map(k -> new KichCoOption(k.getId(), k.getGiaTri())).toList();
        var deGiay = deGiayRepository.findAll().stream()
                .filter(d -> d.getTrangThai() != null && d.getTrangThai() == 1)
                .map(d -> new DeGiayOption(d.getId(), d.getTen())).toList();
        var coGiay = coGiayRepository.findAll().stream()
                .filter(c -> c.getTrangThai() != null && c.getTrangThai() == 1)
                .map(c -> new CoGiayOption(c.getId(), c.getTen())).toList();
        var chatLieuGiay = chatLieuGiayRepository.findAll().stream()
                .filter(c -> c.getTrangThai() != null && c.getTrangThai() == 1)
                .map(c -> new ChatLieuGiayOption(c.getId(), c.getTen())).toList();
        var trongLuong = trongLuongRepository.findAll().stream()
                .filter(t -> t.getTrangThai() != null && t.getTrangThai() == 1)
                .map(t -> new TrongLuongOption(t.getId(), t.getMa(), t.getGiaTri())).toList();
        var congNgheDem = congNgheDemRepository.findAll().stream()
                .filter(c -> c.getTrangThai() != null && c.getTrangThai() == 1)
                .map(c -> new CongNgheDemOption(c.getId(), c.getTen())).toList();
        return new DanhMucSanPhamResponse(loaiGiay, thuongHieu, mauSac, kichCo, deGiay, coGiay, chatLieuGiay, trongLuong, congNgheDem);
    }

    // ─── Danh sách giày ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<GiayListItemResponse> danhSachGiay(
            String keyword, Integer thuongHieuId, Integer loaiGiayId,
            Integer trangThai, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable
    ) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        var spec = GiaySpecifications.filterAdmin(kw, thuongHieuId, loaiGiayId, trangThai, minPrice, maxPrice);
        var page = giayRepository.findAll(spec, pageable);

        if (page.isEmpty()) {
            return PageResponse.from(page.map(g -> null));
        }

        List<Integer> ids = page.map(Giay::getId).getContent();
        Map<Integer, Object[]> aggMap = buildAggregateMap(ids);
        Map<Integer, String> imgMap = buildImageMap(ids);
        Map<Integer, GiayThuocTinh> thuocTinhMap = buildThuocTinhMap(ids);

        return PageResponse.from(page.map(g -> toListItem(g, aggMap, imgMap, thuocTinhMap)));
    }

    private Map<Integer, Object[]> buildAggregateMap(Collection<Integer> ids) {
        Map<Integer, Object[]> map = new HashMap<>();
        for (Object[] row : giayChiTietRepository.aggregateByGiayIds(ids)) {
            map.put((Integer) row[0], row);
        }
        return map;
    }

    private Map<Integer, String> buildImageMap(Collection<Integer> ids) {
        Map<Integer, String> map = new HashMap<>();
        for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayIds(ids)) {
            if (row[0] != null && row[1] != null) {
                Integer giayId = ((Number) row[0]).intValue();
                map.putIfAbsent(giayId, (String) row[1]);
            }
        }
        return map;
    }

    private Map<Integer, GiayThuocTinh> buildThuocTinhMap(Collection<Integer> ids) {
        Map<Integer, GiayThuocTinh> map = new HashMap<>();
        for (GiayThuocTinh item : giayThuocTinhRepository.findByGiayIdInWithRefs(ids)) {
            map.put(item.getGiay().getId(), item);
        }
        return map;
    }

    private GiayListItemResponse toListItem(
            Giay g,
            Map<Integer, Object[]> aggMap,
            Map<Integer, String> imgMap,
            Map<Integer, GiayThuocTinh> thuocTinhMap
    ) {
        Object[] agg = aggMap.get(g.getId());
        GiayThuocTinh gtt = thuocTinhMap.get(g.getId());
        BigDecimal giaMin = agg != null ? (BigDecimal) agg[1] : null;
        Long tongBienThe = agg != null ? (Long) agg[2] : 0L;
        Long tongSoLuong = agg != null ? (Long) agg[3] : 0L;
        BigDecimal giaMax = agg != null ? (BigDecimal) agg[4] : null;
        Long countGiamGia = agg != null ? (Long) agg[5] : 0L;
        BigDecimal giaGocMin = agg != null ? (BigDecimal) agg[6] : null;
        BigDecimal giaGocMax = agg != null ? (BigDecimal) agg[7] : null;
        return new GiayListItemResponse(
                g.getId(), g.getMa(), g.getTen(),
                g.getLoaiGiay().getTen(), g.getThuongHieu().getTen(),
                g.getChatLieu(),
                gtt != null && gtt.getDeGiay() != null ? gtt.getDeGiay().getTen() : null,
                gtt != null && gtt.getCoGiay() != null ? gtt.getCoGiay().getTen() : null,
                gtt != null && gtt.getCongNgheDem() != null ? gtt.getCongNgheDem().getTen() : null,
                toTrongLuongLabel(gtt),
                g.getGioiTinh(), g.getTrangThai(),
                imgMap.get(g.getId()),
                giaMin, giaMax, giaGocMin, giaGocMax, tongBienThe, tongSoLuong,
                g.getNgayTao(),
                countGiamGia > 0
        );
    }

    private String toTrongLuongLabel(GiayThuocTinh gtt) {
        if (gtt == null || gtt.getTrongLuong() == null) {
            return null;
        }
        TrongLuong trongLuong = gtt.getTrongLuong();
        if (trongLuong.getMa() == null && trongLuong.getGiaTri() == null) {
            return null;
        }
        if (trongLuong.getMa() == null) {
            return trongLuong.getGiaTri() + "g";
        }
        if (trongLuong.getGiaTri() == null) {
            return trongLuong.getMa();
        }
        return trongLuong.getMa() + " - " + trongLuong.getGiaTri() + "g";
    }

    @Transactional(readOnly = true)
    public PageResponse<ChiTietSanPhamListItemResponse> danhSachChiTietSanPham(
            String keyword,
            Integer giayId,
            Integer mauSacId,
            Integer kichCoId,
            Integer trangThai,
            Pageable pageable
    ) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        var page = giayChiTietRepository.findAdminChiTietPage(kw, giayId, mauSacId, kichCoId, trangThai, pageable);

        if (page.isEmpty()) {
            return PageResponse.from(page.map(item -> null));
        }

        List<Integer> chiTietIds = page.map(GiayChiTiet::getId).getContent();
        Map<Integer, String> imageMap = buildChiTietImageMap(chiTietIds);
        Map<Integer, ActiveDiscountInfo> discountMap = buildActiveDiscountInfoMap(page.getContent());
        return PageResponse.from(page.map(item -> toChiTietListItem(item, imageMap, discountMap.get(item.getId()))));
    }

    private Map<Integer, String> buildChiTietImageMap(Collection<Integer> ids) {
        Map<Integer, String> map = new HashMap<>();
        for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayChiTietIds(ids)) {
            if (row[0] != null && row[1] != null) {
                Integer chiTietId = ((Number) row[0]).intValue();
                map.putIfAbsent(chiTietId, (String) row[1]);
            }
        }
        return map;
    }

    private ChiTietSanPhamListItemResponse toChiTietListItem(
            GiayChiTiet gct,
            Map<Integer, String> imageMap,
            ActiveDiscountInfo activeDiscount
    ) {
        Giay giay = gct.getGiay();
        return new ChiTietSanPhamListItemResponse(
                gct.getId(),
                giay.getId(),
                giay.getMa(),
                gct.getMaBienThe(),
                gct.getSku(),
                giay.getTen(),
                giay.getThuongHieu().getTen(),
                giay.getLoaiGiay().getTen(),
                giay.getChatLieu(),
                giay.getGioiTinh(),
                gct.getMauSac().getId(),
                gct.getMauSac().getTen(),
                gct.getMauSac().getMaMauHex(),
                gct.getKichCo().getId(),
                gct.getKichCo().getGiaTri(),
                gct.getSoLuong(),
                gct.getGiaGoc(),
                activeDiscount != null ? activeDiscount.giaSauGiam() : gct.getGiaBan(),
                gct.getKichHoat(),
                imageMap.get(gct.getId()),
                gct.getNgayTao(),
                gct.getNgayCapNhat(),
                activeDiscount != null ? activeDiscount.dotGiamGiaId() : null,
                activeDiscount != null ? activeDiscount.maDotGiamGia() : null,
                activeDiscount != null ? activeDiscount.tenDotGiamGia() : null,
                activeDiscount != null ? activeDiscount.loaiGiam() : null,
                activeDiscount != null ? activeDiscount.giaTriGiam() : null
        );
    }

    // ─── Chi tiết giày ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public GiayDetailResponse chiTietGiay(Integer id) {
        Giay g = giayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giày #" + id + " không tồn tại"));
        GiayThuocTinh gtt = giayThuocTinhRepository.findByGiayId(id).orElse(null);
        ThuocTinhResponse thuocTinh = gtt == null ? null : new ThuocTinhResponse(
                gtt.getId(),
                gtt.getDeGiay() != null ? gtt.getDeGiay().getId() : null,
                gtt.getDeGiay() != null ? gtt.getDeGiay().getTen() : null,
                gtt.getCoGiay() != null ? gtt.getCoGiay().getId() : null,
                gtt.getCoGiay() != null ? gtt.getCoGiay().getTen() : null,
                gtt.getCongNgheDem() != null ? gtt.getCongNgheDem().getId() : null,
                gtt.getCongNgheDem() != null ? gtt.getCongNgheDem().getTen() : null,
                gtt.getChatLieuGiay() != null ? gtt.getChatLieuGiay().getId() : null,
                gtt.getChatLieuGiay() != null ? gtt.getChatLieuGiay().getTen() : null,
                gtt.getTrongLuong() != null ? gtt.getTrongLuong().getId() : null,
                gtt.getTrongLuong() != null ? gtt.getTrongLuong().getMa() : null
        );

        List<Integer> chiTietIds = giayChiTietRepository.findByGiayIdEager(id).stream()
                .map(GiayChiTiet::getId).toList();
        List<HinhAnhGiayResponse> hinhAnhs = hinhAnhGiayRepository.findByGiayChiTietIdInAndTrangThaiOrderByLaHinhChinhDescNgayTaoAsc(chiTietIds, 1)
                .stream().map(h -> new HinhAnhGiayResponse(
                        h.getId(), h.getLoaiHinh(), h.getUrl(), h.getMoTa(),
                        h.getLaHinhChinh(), h.getTrangThai(), h.getNgayTao()
                )).toList();

        return new GiayDetailResponse(
                g.getId(), g.getMa(), g.getTen(), g.getGioiTinh(),
                g.getThuongHieu().getId(), g.getThuongHieu().getTen(),
                g.getLoaiGiay().getId(), g.getLoaiGiay().getTen(),
                g.getChatLieu(), g.getMoTa(), g.getTrangThai(),
                thuocTinh, hinhAnhs, g.getNgayTao(), g.getNgayCapNhat()
        );
    }

    // ─── Tạo / Cập nhật / Xóa giày ──────────────────────────────────────────

    @Transactional
    public GiayDetailResponse taoGiay(TaoGiayRequest req) {
        validateProductPayload(
                null,
                true,
                req.ten(),
                req.thuongHieuId(),
                req.loaiGiayId(),
                req.gioiTinh(),
                req.moTa(),
                req.chatLieuGiayId(),
                req.deGiayId(),
                req.coGiayId(),
                req.congNgheDemId(),
                req.trongLuongId()
        );

        String maGiay = hasText(req.ma()) ? req.ma().trim().toUpperCase() : taoMaGiayTuDong();
        if (giayRepository.existsByMaIgnoreCase(maGiay)) {
            throw new BusinessException("Mã giày '" + maGiay + "' đã tồn tại");
        }
        var thuongHieu = thuongHieuRepository.findById(req.thuongHieuId())
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tồn tại"));
        var loaiGiay = loaiGiayRepository.findById(req.loaiGiayId())
                .orElseThrow(() -> new ResourceNotFoundException("Loại giày không tồn tại"));

        var giay = new Giay();
        giay.setMa(maGiay);
        giay.setTen(req.ten().trim());
        giay.setThuongHieu(thuongHieu);
        giay.setLoaiGiay(loaiGiay);
        giay.setGioiTinh(req.gioiTinh());
        giay.setChatLieu(resolveChatLieuText(req.chatLieu(), req.chatLieuGiayId() != null ? chatLieuGiayRepository.findById(req.chatLieuGiayId()).orElse(null) : null));
        giay.setMoTa(trimToNull(req.moTa()));
        giay.setTrangThai(TRANG_THAI_KINH_DOANH);
        giay.setNgayTao(Instant.now());
        giay = giayRepository.save(giay);

        updateTrangThaiTuSoLuong(giay.getId());
        giay = giayRepository.findById(giay.getId()).orElse(giay); // Refresh after status update

        var gtt = new GiayThuocTinh();
        gtt.setGiay(giay);
        gtt.setDeGiay(req.deGiayId() != null ? deGiayRepository.findById(req.deGiayId()).orElse(null) : null);
        gtt.setCoGiay(req.coGiayId() != null ? coGiayRepository.findById(req.coGiayId()).orElse(null) : null);
        gtt.setCongNgheDem(req.congNgheDemId() != null ? congNgheDemRepository.findById(req.congNgheDemId()).orElse(null) : null);
        gtt.setChatLieuGiay(req.chatLieuGiayId() != null ? chatLieuGiayRepository.findById(req.chatLieuGiayId()).orElse(null) : null);
        gtt.setTrongLuong(req.trongLuongId() != null ? trongLuongRepository.findById(req.trongLuongId()).orElse(null) : null);
        gtt.setTrangThai(1);
        gtt.setNgayTao(Instant.now());
        giayThuocTinhRepository.save(gtt);

        return chiTietGiay(giay.getId());
    }

    @Transactional
    public TaoChiTietSanPhamResponse taoChiTietSanPham(TaoChiTietSanPhamRequest req) {
        boolean taoMoiSanPham = req.giayId() == null;
        validateProductPayload(
                req.giayId(),
                taoMoiSanPham,
                req.ten(),
                req.thuongHieuId(),
                req.loaiGiayId(),
                req.gioiTinh(),
                req.moTa(),
                req.chatLieuGiayId(),
                req.deGiayId(),
                req.coGiayId(),
                req.congNgheDemId(),
                req.trongLuongId()
        );

        GiayDetailResponse giayDetail;
        if (taoMoiSanPham) {
            if (!hasText(req.ten())) {
                throw new BusinessException("Tên sản phẩm không được để trống");
            }
            if (req.thuongHieuId() == null) {
                throw new BusinessException("Chọn thương hiệu cho sản phẩm");
            }
            if (req.loaiGiayId() == null) {
                throw new BusinessException("Chọn loại giày cho sản phẩm");
            }

            giayDetail = taoGiay(new TaoGiayRequest(
                    req.ma(),
                    req.ten().trim(),
                    req.thuongHieuId(),
                    req.loaiGiayId(),
                    req.gioiTinh(),
                    req.chatLieu(),
                    req.chatLieuGiayId(),
                    req.moTa(),
                    req.deGiayId(),
                    req.coGiayId(),
                    req.congNgheDemId(),
                    req.trongLuongId()
            ));
        } else {
            giayDetail = chiTietGiay(req.giayId());
        }

        validateVariantPayload(
                giayDetail.id(),
                req.mauSacId(),
                req.kichCoId(),
                req.soLuong(),
                req.giaGoc(),
                req.giaBan(),
                null
        );

        BienTheResponse bienThe = taoBienThe(giayDetail.id(), new TaoBienTheRequest(
                req.mauSacId(),
                req.kichCoId(),
                req.soLuong(),
                req.giaGoc(),
                req.giaBan()
        ));

        return new TaoChiTietSanPhamResponse(
                chiTietGiay(giayDetail.id()),
                bienThe,
                taoMoiSanPham
        );
    }

    @Transactional
    public TaoChiTietSanPhamHangLoatResponse taoChiTietSanPhamHangLoat(TaoChiTietSanPhamHangLoatRequest req) {
        boolean taoMoiSanPham = req.giayId() == null;
        validateProductPayload(
                req.giayId(),
                taoMoiSanPham,
                req.ten(),
                req.thuongHieuId(),
                req.loaiGiayId(),
                req.gioiTinh(),
                req.moTa(),
                req.chatLieuGiayId(),
                req.deGiayId(),
                req.coGiayId(),
                req.congNgheDemId(),
                req.trongLuongId()
        );

        GiayDetailResponse giayDetail;
        if (taoMoiSanPham) {
            if (!hasText(req.ten())) {
                throw new BusinessException("Tên sản phẩm không được để trống");
            }
            if (req.thuongHieuId() == null) {
                throw new BusinessException("Chọn thương hiệu cho sản phẩm");
            }
            if (req.loaiGiayId() == null) {
                throw new BusinessException("Chọn loại giày cho sản phẩm");
            }

            giayDetail = taoGiay(new TaoGiayRequest(
                    req.ma(),
                    req.ten().trim(),
                    req.thuongHieuId(),
                    req.loaiGiayId(),
                    req.gioiTinh(),
                    req.chatLieu(),
                    req.chatLieuGiayId(),
                    req.moTa(),
                    req.deGiayId(),
                    req.coGiayId(),
                    req.congNgheDemId(),
                    req.trongLuongId()
            ));
        } else {
            giayDetail = chiTietGiay(req.giayId());
        }

        validateBatchVariantPayload(giayDetail.id(), req.bienThes());

        Set<String> seenCombinations = new HashSet<>();
        List<BienTheResponse> createdVariants = new ArrayList<>();
        for (TaoChiTietSanPhamHangLoatItemRequest item : req.bienThes()) {
            String combinationKey = item.mauSacId() + "-" + item.kichCoId();
            if (!seenCombinations.add(combinationKey)) {
                throw new BusinessException("Danh sách CTSP đang bị trùng màu sắc và kích cỡ");
            }

            createdVariants.add(taoBienThe(giayDetail.id(), new TaoBienTheRequest(
                    item.mauSacId(),
                    item.kichCoId(),
                    item.soLuong(),
                    item.giaGoc(),
                    item.giaBan()
            )));
        }

        return new TaoChiTietSanPhamHangLoatResponse(
                chiTietGiay(giayDetail.id()),
                createdVariants,
                taoMoiSanPham
        );
    }

    @Transactional
    public GiayDetailResponse capNhatGiay(Integer id, CapNhatGiayRequest req) {
        validateProductPayload(
                id,
                true,
                req.ten(),
                req.thuongHieuId(),
                req.loaiGiayId(),
                req.gioiTinh(),
                req.moTa(),
                req.chatLieuGiayId(),
                req.deGiayId(),
                req.coGiayId(),
                req.congNgheDemId(),
                req.trongLuongId()
        );

        var giay = giayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giày #" + id + " không tồn tại"));
        var thuongHieu = thuongHieuRepository.findById(req.thuongHieuId())
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tồn tại"));
        var loaiGiay = loaiGiayRepository.findById(req.loaiGiayId())
                .orElseThrow(() -> new ResourceNotFoundException("Loại giày không tồn tại"));

        giay.setTen(req.ten().trim());
        giay.setThuongHieu(thuongHieu);
        giay.setLoaiGiay(loaiGiay);
        giay.setGioiTinh(req.gioiTinh());
        giay.setChatLieu(resolveChatLieuText(req.chatLieu(), req.chatLieuGiayId() != null ? chatLieuGiayRepository.findById(req.chatLieuGiayId()).orElse(null) : null));
        giay.setMoTa(trimToNull(req.moTa()));
        giay.setNgayCapNhat(Instant.now());

        var gtt = giayThuocTinhRepository.findByGiayId(id).orElseGet(() -> {
            var newGtt = new GiayThuocTinh();
            newGtt.setGiay(giay);
            newGtt.setTrangThai(1);
            newGtt.setNgayTao(Instant.now());
            return newGtt;
        });
        gtt.setDeGiay(req.deGiayId() != null ? deGiayRepository.findById(req.deGiayId()).orElse(null) : null);
        gtt.setCoGiay(req.coGiayId() != null ? coGiayRepository.findById(req.coGiayId()).orElse(null) : null);
        gtt.setCongNgheDem(req.congNgheDemId() != null ? congNgheDemRepository.findById(req.congNgheDemId()).orElse(null) : null);
        gtt.setChatLieuGiay(req.chatLieuGiayId() != null ? chatLieuGiayRepository.findById(req.chatLieuGiayId()).orElse(null) : null);
        gtt.setTrongLuong(req.trongLuongId() != null ? trongLuongRepository.findById(req.trongLuongId()).orElse(null) : null);
        gtt.setNgayCapNhat(Instant.now());
        giayThuocTinhRepository.save(gtt);

        updateTrangThaiTuSoLuong(id);
        return chiTietGiay(id);
    }

    @Transactional
    public void doiTrangThai(Integer id, DoiTrangThaiRequest req) {
        if (!isTrangThaiGiayHopLe(req.trangThai())) {
            throw new BusinessException("Trạng thái sản phẩm không hợp lệ");
        }
        var giay = giayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giày #" + id + " không tồn tại"));

        if (req.trangThai() == TRANG_THAI_NGUNG_KINH_DOANH) {
            // Ngừng kinh doanh: CHỈ đổi trạng thái sản phẩm + gỡ khỏi giỏ khách.
            // KHÔNG đổi trạng thái biến thể (giữ nguyên đang bán/ngừng bán của từng biến thể).
            giay.setTrangThai(TRANG_THAI_NGUNG_KINH_DOANH);
            giay.setNgayCapNhat(Instant.now());
            for (GiayChiTiet ct : giayChiTietRepository.findByGiayIdEager(id)) {
                xoaKhoiGioHangCuaKhachHang(ct.getId());
            }
            return;
        }

        // Bật kinh doanh: phải còn ít nhất 1 biến thể đang bán (không tự bật biến thể nữa).
        if (!giayChiTietRepository.existsByGiayIdAndKichHoat(id, 1)) {
            throw new BusinessException(
                    "Tất cả biến thể đang ngừng bán. Hãy bật ít nhất 1 biến thể trước khi kinh doanh sản phẩm.");
        }
        giay.setNgayCapNhat(Instant.now());
        // Tự đặt Kinh doanh / Hết hàng theo tồn của các biến thể đang bán.
        updateTrangThaiTuSoLuong(giay);
    }

    @Transactional
    public void xoaGiay(Integer id) {
        System.out.println(">>> TOGGLE TRANG THAI GIAY #" + id);
        try {
            var giay = giayRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Giày #" + id + " không tồn tại"));

            if (giay.getTrangThai() == TRANG_THAI_NGUNG_KINH_DOANH) {
                giay.setTrangThai(TRANG_THAI_KINH_DOANH);
                List<GiayChiTiet> chiTiets = giayChiTietRepository.findByGiayIdEager(id);
                for (GiayChiTiet ct : chiTiets) {
                    ct.setKichHoat(1);
                    ct.setNgayCapNhat(Instant.now());
                    giayChiTietRepository.save(ct);
                }
                updateTrangThaiTuSoLuong(giay);
            } else {
                giay.setTrangThai(TRANG_THAI_NGUNG_KINH_DOANH);
                List<GiayChiTiet> chiTiets = giayChiTietRepository.findByGiayIdEager(id);
                for (GiayChiTiet ct : chiTiets) {
                    ct.setKichHoat(0);
                    ct.setNgayCapNhat(Instant.now());
                    xoaKhoiGioHangCuaKhachHang(ct.getId());
                    giayChiTietRepository.save(ct);
                }
            }
            giay.setNgayCapNhat(Instant.now());
        } catch (Exception e) {
            System.err.println("!!! LỖI XÓA GIÀY: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("Lỗi xử lý trạng thái: " + e.getMessage());
        }
    }

    // ─── Biến thể ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BienTheResponse> danhSachBienThe(Integer giayId) {
        if (!giayRepository.existsById(giayId)) {
            throw new ResourceNotFoundException("Giày #" + giayId + " không tồn tại");
        }
        List<GiayChiTiet> bienThes = giayChiTietRepository.findByGiayIdEager(giayId);
        Map<Integer, ActiveDiscountInfo> discountMap = buildActiveDiscountInfoMap(bienThes);
        return bienThes.stream()
                .map(item -> toBienThe(item, discountMap.get(item.getId())))
                .toList();
    }

    @Transactional
    public BienTheResponse taoBienThe(Integer giayId, TaoBienTheRequest req) {
        validateVariantPayload(
                giayId,
                req.mauSacId(),
                req.kichCoId(),
                req.soLuong(),
                req.giaGoc(),
                req.giaBan(),
                null
        );

        var giay = giayRepository.findById(giayId)
                .orElseThrow(() -> new ResourceNotFoundException("Giày #" + giayId + " không tồn tại"));
        var mauSac = mauSacRepository.findById(req.mauSacId())
                .orElseThrow(() -> new ResourceNotFoundException("Màu sắc không tồn tại"));
        var kichCo = kichCoRepository.findById(req.kichCoId())
                .orElseThrow(() -> new ResourceNotFoundException("Kích cỡ không tồn tại"));

        if (giayChiTietRepository.existsByGiayIdAndMauSacIdAndKichCoId(giayId, req.mauSacId(), req.kichCoId())) {
            throw new BusinessException("Biến thể " + mauSac.getTen() + " / " + kichCo.getGiaTri() + " đã tồn tại");
        }

        var gct = new GiayChiTiet();
        gct.setGiay(giay);
        gct.setMauSac(mauSac);
        gct.setKichCo(kichCo);
        gct.setSoLuong(req.soLuong());
        gct.setGiaGoc(req.giaGoc());
        gct.setGiaBan(req.giaBan());

        String maBienThe = giay.getMa() + "-" + mauSac.getMa() + "-" + kichCo.getGiaTri();
        gct.setMaBienThe(maBienThe);
        gct.setSku(maBienThe + "-" + System.currentTimeMillis() % 10000);
        gct.setKichHoat(req.soLuong() > 0 ? 1 : 0);
        gct.setNgayTao(Instant.now());

        var saved = giayChiTietRepository.save(gct);
        updateTrangThaiTuSoLuong(giayId);
        return toBienThe(saved, null);
    }

    @Transactional
    public BienTheResponse capNhatBienThe(Integer id, CapNhatBienTheRequest req) {
        validateVariantUpdatePayload(req);

        var gct = giayChiTietRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể #" + id + " không tồn tại"));
        gct.setSoLuong(req.soLuong());
        gct.setGiaGoc(req.giaGoc());
        gct.setGiaBan(req.giaBan());
        gct.setKichHoat(req.kichHoat());
        gct.setNgayCapNhat(Instant.now());
        if (req.kichHoat() != null && req.kichHoat() == 0) {
            xoaKhoiGioHangCuaKhachHang(id);
        }
        var saved = giayChiTietRepository.save(gct);
        updateTrangThaiTuSoLuong(saved.getGiay().getId());
        sanPhamRealtimePublisher.phatSauCommit("CAP_NHAT_BIEN_THE");
        return toBienThe(saved, null);
    }

    @Transactional
    public BienTheResponse doiTrangThaiBienThe(Integer id, DoiTrangThaiBienTheRequest req) {
        if (req.kichHoat() == null || (req.kichHoat() != 1 && req.kichHoat() != 0)) {
            throw new BusinessException("Trạng thái CTSP không hợp lệ");
        }

        var gct = giayChiTietRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể #" + id + " không tồn tại"));

        gct.setKichHoat(req.kichHoat());
        gct.setNgayCapNhat(Instant.now());
        if (req.kichHoat() != null && req.kichHoat() == 0) {
            xoaKhoiGioHangCuaKhachHang(id);
        }
        var saved = giayChiTietRepository.save(gct);
        updateTrangThaiTuSoLuong(saved.getGiay().getId());
        sanPhamRealtimePublisher.phatSauCommit("DOI_TRANG_THAI_BIEN_THE");
        return toBienThe(saved, null);
    }

    @Transactional
    public void xoaBienThe(Integer id) {
        var gct = giayChiTietRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể #" + id + " không tồn tại"));
        Integer giayId = gct.getGiay().getId();
        giayChiTietRepository.deleteById(id);
        xoaKhoiGioHangCuaKhachHang(id);
        updateTrangThaiTuSoLuong(giayId);
        sanPhamRealtimePublisher.phatSauCommit("XOA_BIEN_THE");
    }

    private void xoaKhoiGioHangCuaKhachHang(Integer giayChiTietId) {
        // CHỈ gỡ khỏi giỏ hàng (hóa đơn trạng thái 0). KHÔNG đụng đơn thật (chờ xác nhận = 1...)
        // vì xóa dòng đơn thật vừa sai nghiệp vụ vừa dính khóa ngoại (vd phiếu trả hàng tham chiếu).
        List<HoaDonChiTiet> toDelete = hoaDonChiTietRepository.findByGiayChiTietIdAndTrangThaiHoaDon(giayChiTietId, List.of(0));
        for (HoaDonChiTiet hdct : toDelete) {
            HoaDon hd = hdct.getHoaDon();
            
            BigDecimal currentTongTienHang = hd.getTongTienHang() != null ? hd.getTongTienHang() : BigDecimal.ZERO;
            BigDecimal itemThanhTien = hdct.getThanhTien() != null ? hdct.getThanhTien() : BigDecimal.ZERO;
            
            BigDecimal newTongTienHang = currentTongTienHang.subtract(itemThanhTien);
            if (newTongTienHang.compareTo(BigDecimal.ZERO) < 0) {
                newTongTienHang = BigDecimal.ZERO;
            }
            hd.setTongTienHang(newTongTienHang);

            BigDecimal currentTongTienThanhToan = hd.getTongTienThanhToan() != null ? hd.getTongTienThanhToan() : BigDecimal.ZERO;
            BigDecimal newTongTienThanhToan = currentTongTienThanhToan.subtract(itemThanhTien);
            if (newTongTienThanhToan.compareTo(BigDecimal.ZERO) < 0) {
                newTongTienThanhToan = BigDecimal.ZERO;
            }
            hd.setTongTienThanhToan(newTongTienThanhToan);
            
            if (newTongTienHang.compareTo(BigDecimal.ZERO) == 0) {
                hd.setTienGiam(BigDecimal.ZERO);
                hd.setPhieuGiamGia(null);
                hd.setTongTienThanhToan(BigDecimal.ZERO);
            }
            
            hoaDonChiTietRepository.delete(hdct);
            hoaDonRepository.save(hd);
        }
    }

    private BienTheResponse toBienThe(GiayChiTiet gct, ActiveDiscountInfo activeDiscount) {
        return new BienTheResponse(
                gct.getId(), gct.getMaBienThe(), gct.getSku(),
                gct.getSoLuong(), gct.getGiaGoc(),
                activeDiscount != null ? activeDiscount.giaSauGiam() : gct.getGiaBan(),
                gct.getKichHoat(),
                gct.getMauSac().getId(), gct.getMauSac().getTen(), gct.getMauSac().getMaMauHex(),
                gct.getKichCo().getId(), gct.getKichCo().getGiaTri(),
                gct.getNgayTao(), gct.getNgayCapNhat(),
                activeDiscount != null ? activeDiscount.dotGiamGiaId() : null,
                activeDiscount != null ? activeDiscount.maDotGiamGia() : null,
                activeDiscount != null ? activeDiscount.tenDotGiamGia() : null,
                activeDiscount != null ? activeDiscount.loaiGiam() : null,
                activeDiscount != null ? activeDiscount.giaTriGiam() : null
        );
    }

    // ─── Hình ảnh ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HinhAnhGiayResponse> layHinhAnh(Integer chiTietId) {
        if (!giayChiTietRepository.existsById(chiTietId)) {
            throw new ResourceNotFoundException("Biến thể #" + chiTietId + " không tồn tại");
        }
        return hinhAnhGiayRepository
                .findByGiayChiTietIdAndTrangThaiOrderByLaHinhChinhDescNgayTaoAsc(chiTietId, 1)
                .stream().map(this::toHinhAnh).toList();
    }

    @Transactional
    public HinhAnhGiayResponse themHinhAnh(Integer chiTietId, ThemHinhAnhRequest req) {
        var chiTiet = giayChiTietRepository.findById(chiTietId)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể #" + chiTietId + " không tồn tại"));
        var existingImages = hinhAnhGiayRepository
                .findByGiayChiTietIdAndTrangThaiOrderByLaHinhChinhDescNgayTaoAsc(chiTietId, 1);
        boolean datLamHinhChinh = (req.loaiHinh() != null && req.loaiHinh() == 1) || existingImages.isEmpty();
        var hinh = new HinhAnhGiay();
        hinh.setGiayChiTiet(chiTiet);
        hinh.setUrl(req.url().trim());
        hinh.setLoaiHinh(req.loaiHinh() != null ? req.loaiHinh() : 2);
        hinh.setMoTa(req.moTa());
        hinh.setLaHinhChinh(datLamHinhChinh);
        hinh.setTrangThai(1);
        hinh.setNgayTao(Instant.now());
        var saved = hinhAnhGiayRepository.save(hinh);

        if (datLamHinhChinh) {
            existingImages.forEach(item -> {
                if (!item.getId().equals(saved.getId()) && Boolean.TRUE.equals(item.getLaHinhChinh())) {
                    item.setLaHinhChinh(false);
                    item.setNgayCapNhat(Instant.now());
                }
            });
        }

        return toHinhAnh(saved);
    }

    @Transactional
    public HinhAnhGiayResponse capNhatHinhAnh(Integer id, CapNhatHinhAnhRequest req) {
        var hinh = hinhAnhGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hình ảnh #" + id + " không tồn tại"));

        hinh.setUrl(req.url().trim());
        if (req.loaiHinh() != null) {
            hinh.setLoaiHinh(req.loaiHinh());
        }
        hinh.setMoTa(req.moTa());
        hinh.setNgayCapNhat(Instant.now());

        var saved = hinhAnhGiayRepository.save(hinh);
        return toHinhAnh(saved);
    }

    @Transactional
    public void xoaHinhAnh(Integer id) {
        if (!hinhAnhGiayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hình ảnh #" + id + " không tồn tại");
        }
        hinhAnhGiayRepository.deleteById(id);
    }

    @Transactional
    public void datHinhChinh(Integer id) {
        var hinh = hinhAnhGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hình ảnh #" + id + " không tồn tại"));
        Integer chiTietId = hinh.getGiayChiTiet().getId();
        hinhAnhGiayRepository
                .findByGiayChiTietIdAndTrangThaiOrderByLaHinhChinhDescNgayTaoAsc(chiTietId, 1)
                .forEach(h -> {
                    h.setLaHinhChinh(h.getId().equals(id));
                    h.setNgayCapNhat(Instant.now());
                });
    }

    private HinhAnhGiayResponse toHinhAnh(HinhAnhGiay h) {
        return new HinhAnhGiayResponse(
                h.getId(), h.getLoaiHinh(), h.getUrl(), h.getMoTa(),
                h.getLaHinhChinh(), h.getTrangThai(), h.getNgayTao()
        );
    }
    // ─── Utils ───────────────────────────────────────────────────────────────
    private void validateProductPayload(
            Integer giayId,
            boolean requireProductFields,
            String ten,
            Integer thuongHieuId,
            Integer loaiGiayId,
            Integer gioiTinh,
            String moTa,
            Integer chatLieuGiayId,
            Integer deGiayId,
            Integer coGiayId,
            Integer congNgheDemId,
            Integer trongLuongId
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (giayId != null && !giayRepository.existsById(giayId)) {
            putError(errors, "giayId", "Sản phẩm không tồn tại");
        }

        if (requireProductFields) {
            String normalizedTen = trimToNull(ten);
            if (normalizedTen == null) {
                putError(errors, "ten", "Vui lòng nhập tên sản phẩm");
            } else {
                if (normalizedTen.length() < MIN_PRODUCT_NAME_LENGTH) {
                    putError(errors, "ten", "Tên sản phẩm phải có ít nhất 3 ký tự");
                }
                if (normalizedTen.length() > MAX_PRODUCT_NAME_LENGTH) {
                    putError(errors, "ten", "Tên sản phẩm không được vượt quá 300 ký tự");
                }
            }

            if (thuongHieuId == null) {
                putError(errors, "thuongHieuId", "Vui lòng chọn thương hiệu cho sản phẩm");
            } else {
                var th = thuongHieuRepository.findById(thuongHieuId).orElse(null);
                if (th == null) {
                    putError(errors, "thuongHieuId", "Thương hiệu đã chọn không tồn tại");
                } else if (th.getTrangThai() != null && th.getTrangThai() == 0) {
                    putError(errors, "thuongHieuId", "Thương hiệu đã chọn đang ngừng hoạt động");
                }
            }

            if (loaiGiayId == null) {
                putError(errors, "loaiGiayId", "Vui lòng chọn loại giày cho sản phẩm");
            } else {
                var lg = loaiGiayRepository.findById(loaiGiayId).orElse(null);
                if (lg == null) {
                    putError(errors, "loaiGiayId", "Loại giày đã chọn không tồn tại");
                } else if (lg.getTrangThai() != null && lg.getTrangThai() == 0) {
                    putError(errors, "loaiGiayId", "Loại giày đã chọn đang ngừng hoạt động");
                }
            }
        }

        if (gioiTinh != null && !isGioiTinhHopLe(gioiTinh)) {
            putError(errors, "gioiTinh", "Giới tính chỉ được phép là Nam, Nữ hoặc Unisex");
        }

        String normalizedDescription = trimToNull(moTa);
        if (normalizedDescription != null && normalizedDescription.length() > MAX_PRODUCT_DESCRIPTION_LENGTH) {
            putError(errors, "moTa", "Mô tả không được vượt quá 2000 ký tự");
        }

        if (chatLieuGiayId != null) {
            var cl = chatLieuGiayRepository.findById(chatLieuGiayId).orElse(null);
            if (cl == null) {
                putError(errors, "chatLieuGiayId", "Chất liệu giày đã chọn không tồn tại");
            } else if (cl.getTrangThai() != null && cl.getTrangThai() == 0) {
                putError(errors, "chatLieuGiayId", "Chất liệu giày đã chọn đang ngừng hoạt động");
            }
        }
        
        if (deGiayId != null) {
            var dg = deGiayRepository.findById(deGiayId).orElse(null);
            if (dg == null) {
                putError(errors, "deGiayId", "Đế giày đã chọn không tồn tại");
            } else if (dg.getTrangThai() != null && dg.getTrangThai() == 0) {
                putError(errors, "deGiayId", "Đế giày đã chọn đang ngừng hoạt động");
            }
        }
        
        if (coGiayId != null) {
            var cg = coGiayRepository.findById(coGiayId).orElse(null);
            if (cg == null) {
                putError(errors, "coGiayId", "Cổ giày đã chọn không tồn tại");
            } else if (cg.getTrangThai() != null && cg.getTrangThai() == 0) {
                putError(errors, "coGiayId", "Cổ giày đã chọn đang ngừng hoạt động");
            }
        }
        
        if (congNgheDemId != null) {
            var cn = congNgheDemRepository.findById(congNgheDemId).orElse(null);
            if (cn == null) {
                putError(errors, "congNgheDemId", "Công nghệ đệm đã chọn không tồn tại");
            } else if (cn.getTrangThai() != null && cn.getTrangThai() == 0) {
                putError(errors, "congNgheDemId", "Công nghệ đệm đã chọn đang ngừng hoạt động");
            }
        }
        
        if (trongLuongId != null) {
            var tl = trongLuongRepository.findById(trongLuongId).orElse(null);
            if (tl == null) {
                putError(errors, "trongLuongId", "Trọng lượng đã chọn không tồn tại");
            } else if (tl.getTrangThai() != null && tl.getTrangThai() == 0) {
                putError(errors, "trongLuongId", "Trọng lượng đã chọn đang ngừng hoạt động");
            }
        }

        throwValidationErrors(errors);
    }

    private void validateBatchVariantPayload(Integer giayId, List<TaoChiTietSanPhamHangLoatItemRequest> bienThes) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (bienThes == null || bienThes.isEmpty()) {
            putError(errors, "bienThes", "Vui lòng tạo ít nhất một chi tiết sản phẩm");
            throwValidationErrors(errors);
            return;
        }

        Set<String> seenCombinations = new HashSet<>();
        for (int index = 0; index < bienThes.size(); index++) {
            TaoChiTietSanPhamHangLoatItemRequest item = bienThes.get(index);
            String prefix = "bienThes[" + index + "].";
            validateVariantPayload(
                    errors,
                    giayId,
                    item.mauSacId(),
                    item.kichCoId(),
                    item.soLuong(),
                    item.giaGoc(),
                    item.giaBan(),
                    prefix
            );

            if (item.mauSacId() != null && item.kichCoId() != null) {
                String combinationKey = item.mauSacId() + "-" + item.kichCoId();
                if (!seenCombinations.add(combinationKey)) {
                    putError(errors, "bienThes", "Danh sách chi tiết sản phẩm đang bị trùng màu sắc và kích cỡ");
                    putError(errors, prefix + "kichCoId", "Biến thể màu sắc và kích cỡ này đang bị trùng trong danh sách");
                }
            }
        }

        throwValidationErrors(errors);
    }

    private void validateVariantPayload(
            Integer giayId,
            Integer mauSacId,
            Integer kichCoId,
            Integer soLuong,
            BigDecimal giaGoc,
            BigDecimal giaBan,
            String prefix
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateVariantPayload(errors, giayId, mauSacId, kichCoId, soLuong, giaGoc, giaBan, prefix);
        throwValidationErrors(errors);
    }

    private void validateVariantPayload(
            Map<String, String> errors,
            Integer giayId,
            Integer mauSacId,
            Integer kichCoId,
            Integer soLuong,
            BigDecimal giaGoc,
            BigDecimal giaBan,
            String prefix
    ) {
        String mauSacKey = fieldKey(prefix, "mauSacId");
        String kichCoKey = fieldKey(prefix, "kichCoId");
        String soLuongKey = fieldKey(prefix, "soLuong");
        String giaGocKey = fieldKey(prefix, "giaGoc");
        String giaBanKey = fieldKey(prefix, "giaBan");

        if (mauSacId == null) {
            putError(errors, mauSacKey, "Vui lòng chọn màu sắc");
        } else {
            var ms = mauSacRepository.findById(mauSacId).orElse(null);
            if (ms == null) {
                putError(errors, mauSacKey, "Màu sắc đã chọn không tồn tại");
            } else if (ms.getTrangThai() != null && ms.getTrangThai() == 0) {
                putError(errors, mauSacKey, "Màu sắc đã chọn đang ngừng hoạt động");
            }
        }

        if (kichCoId == null) {
            putError(errors, kichCoKey, "Vui lòng chọn kích cỡ");
        } else {
            var kc = kichCoRepository.findById(kichCoId).orElse(null);
            if (kc == null) {
                putError(errors, kichCoKey, "Kích cỡ đã chọn không tồn tại");
            } else if (kc.getTrangThai() != null && kc.getTrangThai() == 0) {
                putError(errors, kichCoKey, "Kích cỡ đã chọn đang ngừng hoạt động");
            }
        }

        if (soLuong == null) {
            putError(errors, soLuongKey, "Vui lòng nhập số lượng tồn");
        } else if (soLuong < 0) {
            putError(errors, soLuongKey, "Số lượng tồn không được âm");
        }

        if (giaGoc == null) {
            putError(errors, giaGocKey, "Vui lòng nhập giá gốc");
        } else if (giaGoc.compareTo(MIN_PRICE) < 0) {
            putError(errors, giaGocKey, "Giá gốc phải lớn hơn 0");
        }

        if (giaBan == null) {
            putError(errors, giaBanKey, "Vui lòng nhập giá bán");
        } else if (giaBan.compareTo(MIN_PRICE) < 0) {
            putError(errors, giaBanKey, "Giá bán phải lớn hơn 0");
        }

        if (giaGoc != null && giaBan != null && giaGoc.compareTo(giaBan) > 0) {
            putError(errors, giaGocKey, "Giá gốc không được lớn hơn giá bán");
        }

        if (
                giayId != null
                        && mauSacId != null
                        && kichCoId != null
                        && giayRepository.existsById(giayId)
                        && giayChiTietRepository.existsByGiayIdAndMauSacIdAndKichCoId(giayId, mauSacId, kichCoId)
        ) {
            putError(errors, kichCoKey, "Biến thể màu sắc và kích cỡ này đã tồn tại trong sản phẩm");
        }
    }

    private void validateVariantUpdatePayload(CapNhatBienTheRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (req.soLuong() == null) {
            putError(errors, "soLuong", "Vui lòng nhập số lượng tồn");
        } else if (req.soLuong() < 0) {
            putError(errors, "soLuong", "Số lượng tồn không được âm");
        }

        if (req.giaGoc() == null) {
            putError(errors, "giaGoc", "Vui lòng nhập giá gốc");
        } else if (req.giaGoc().compareTo(MIN_PRICE) < 0) {
            putError(errors, "giaGoc", "Giá gốc phải lớn hơn 0");
        }

        if (req.giaBan() == null) {
            putError(errors, "giaBan", "Vui lòng nhập giá bán");
        } else if (req.giaBan().compareTo(MIN_PRICE) < 0) {
            putError(errors, "giaBan", "Giá bán phải lớn hơn 0");
        }

        if (req.giaGoc() != null && req.giaBan() != null && req.giaGoc().compareTo(req.giaBan()) > 0) {
            putError(errors, "giaGoc", "Giá gốc không được lớn hơn giá bán");
        }

        if (req.kichHoat() == null || (req.kichHoat() != 1 && req.kichHoat() != 0)) {
            putError(errors, "kichHoat", "Trạng thái chi tiết sản phẩm không hợp lệ");
        }

        throwValidationErrors(errors);
    }

    private void validateOptionalReference(
            Map<String, String> errors,
            String fieldName,
            Integer value,
            boolean exists,
            String label
    ) {
        if (value != null && !exists) {
            putError(errors, fieldName, label + " đã chọn không tồn tại");
        }
    }

    private void throwValidationErrors(Map<String, String> errors) {
        if (errors != null && !errors.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Dữ liệu đầu vào không hợp lệ",
                    errors
            );
        }
    }

    private void putError(Map<String, String> errors, String fieldName, String message) {
        if (fieldName == null || fieldName.isBlank() || message == null || message.isBlank()) {
            return;
        }
        errors.putIfAbsent(fieldName, message);
    }

    private String fieldKey(String prefix, String fieldName) {
        return prefix == null || prefix.isBlank() ? fieldName : prefix + fieldName;
    }

    private boolean isGioiTinhHopLe(Integer gioiTinh) {
        return gioiTinh == GIOI_TINH_NAM
                || gioiTinh == GIOI_TINH_NU
                || gioiTinh == GIOI_TINH_UNISEX;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void updateTrangThaiTuSoLuong(Integer giayId) {
        var giay = giayRepository.findById(giayId).orElse(null);
        if (giay == null) {
            return;
        }
        updateTrangThaiTuSoLuong(giay);
    }

    /**
     * Đồng bộ trạng thái sản phẩm theo tồn kho: hết tồn -> "Hết hàng", còn tồn -> "Kinh doanh"
     * (giữ nguyên nếu admin đã đặt "Ngừng kinh doanh"). Gọi sau khi trừ/hoàn kho ở đơn online.
     */
    @Transactional
    public void dongBoTrangThaiTheoTonKho(Integer giayId) {
        if (giayId == null) {
            return;
        }
        giayRepository.findById(giayId).ifPresent(giay -> {
            updateTrangThaiTuSoLuong(giay);
            giayRepository.save(giay);
        });
    }

    private void updateTrangThaiTuSoLuong(Giay giay) {
        // Trạng thái sản phẩm suy ra từ biến thể:
        //  - Không còn biến thể nào đang bán -> Ngừng kinh doanh.
        //  - Còn biến thể đang bán + còn tồn   -> Kinh doanh.
        //  - Còn biến thể đang bán + hết tồn   -> Hết hàng.
        int newStatus;
        if (!giayChiTietRepository.existsByGiayIdAndKichHoat(giay.getId(), 1)) {
            newStatus = TRANG_THAI_NGUNG_KINH_DOANH;
        } else {
            newStatus = coTonKho(giay.getId()) ? TRANG_THAI_KINH_DOANH : TRANG_THAI_HET_HANG;
        }

        if (giay.getTrangThai() != newStatus) {
            giay.setTrangThai(newStatus);
            giay.setNgayCapNhat(Instant.now());
        }
    }

    private boolean coTonKho(Integer giayId) {
        Long totalQty = giayChiTietRepository.sumSoLuongByGiayId(giayId);
        return totalQty != null && totalQty > 0;
    }

    private boolean isTrangThaiGiayHopLe(Integer trangThai) {
        return trangThai != null
                && (trangThai == TRANG_THAI_NGUNG_KINH_DOANH
                || trangThai == TRANG_THAI_KINH_DOANH
                || trangThai == TRANG_THAI_HET_HANG);
    }

    private Map<Integer, ActiveDiscountInfo> buildActiveDiscountInfoMap(Collection<GiayChiTiet> chiTiets) {
        Map<Integer, ActiveDiscountInfo> result = new HashMap<>();
        if (chiTiets == null || chiTiets.isEmpty()) {
            return result;
        }

        Map<Integer, GiayChiTiet> chiTietMap = new HashMap<>();
        for (GiayChiTiet item : chiTiets) {
            chiTietMap.put(item.getId(), item);
        }

        LocalDate now = LocalDate.now();
        for (DotGiamGiaSanPham link : dotGiamGiaSanPhamRepository.findActiveByGiayChiTietIdIn(chiTietMap.keySet())) {
            GiayChiTiet gct = chiTietMap.get(link.getGiayChiTiet().getId());
            if (gct == null) {
                continue;
            }

            DotGiamGia dotGiamGia = link.getDotGiamGia();
            if (!isDiscountEffective(dotGiamGia, now)) {
                continue;
            }

            BigDecimal giaSauGiam = calculateDiscountedPrice(gct.getGiaBan(), dotGiamGia);
            ActiveDiscountInfo current = result.get(gct.getId());
            if (current == null || giaSauGiam.compareTo(current.giaSauGiam()) < 0) {
                result.put(
                        gct.getId(),
                        new ActiveDiscountInfo(
                                dotGiamGia.getId(),
                                dotGiamGia.getMa(),
                                dotGiamGia.getTen(),
                                dotGiamGia.getLoaiGiam(),
                                dotGiamGia.getGiaTriGiam(),
                                giaSauGiam
                        )
                );
            }
        }

        return result;
    }

    /**
     * Giá sau giảm (theo đợt giảm giá đang áp dụng) cho từng biến thể.
     * Map chỉ chứa biến thể ĐANG có đợt giảm; biến thể không có thì không xuất hiện.
     */
    public Map<Integer, BigDecimal> layGiaSauGiam(Collection<GiayChiTiet> chiTiets) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        buildActiveDiscountInfoMap(chiTiets).forEach((id, info) -> result.put(id, info.giaSauGiam()));
        return result;
    }

    private boolean isDiscountEffective(DotGiamGia dotGiamGia, LocalDate now) {
        if (dotGiamGia == null || dotGiamGia.getKichHoat() == null || dotGiamGia.getKichHoat() == 0) {
            return false;
        }
        if (dotGiamGia.getNgayBatDau() != null && now.isBefore(dotGiamGia.getNgayBatDau())) {
            return false;
        }
        return dotGiamGia.getNgayKetThuc() == null || !now.isAfter(dotGiamGia.getNgayKetThuc());
    }

    private boolean isDiscountDisplayable(DotGiamGia dotGiamGia, LocalDate now) {
        if (dotGiamGia == null || dotGiamGia.getKichHoat() == null || dotGiamGia.getKichHoat() == 0) {
            return false;
        }
        return dotGiamGia.getNgayKetThuc() == null || !now.isAfter(dotGiamGia.getNgayKetThuc());
    }

    private BigDecimal calculateDiscountedPrice(BigDecimal giaGoc, DotGiamGia dotGiamGia) {
        if (giaGoc == null || dotGiamGia == null || dotGiamGia.getGiaTriGiam() == null) {
            return giaGoc;
        }

        BigDecimal giaSauGiam = giaGoc;
        if (dotGiamGia.getLoaiGiam() != null && dotGiamGia.getLoaiGiam() == 1) {
            BigDecimal discountAmount = giaGoc.multiply(dotGiamGia.getGiaTriGiam())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            giaSauGiam = giaGoc.subtract(discountAmount);
        } else if (dotGiamGia.getLoaiGiam() != null && dotGiamGia.getLoaiGiam() == 2) {
            giaSauGiam = giaGoc.subtract(dotGiamGia.getGiaTriGiam());
        }

        if (giaSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return giaSauGiam;
    }

    private String resolveChatLieuText(String rawChatLieu, ChatLieuGiay chatLieuGiay) {
        if (chatLieuGiay != null) {
            return chatLieuGiay.getTen();
        }
        return hasText(rawChatLieu) ? rawChatLieu.trim() : null;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private String taoMaGiayTuDong() {
        String ma;
        do {
            ma = "G" + String.format("%05d", ThreadLocalRandom.current().nextInt(100000));
        } while (giayRepository.existsByMaIgnoreCase(ma));
        return ma;
    }
}
