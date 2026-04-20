package com.example.server.core.admin.quanLySanPham;

import com.example.server.entity.*;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.*;
import com.example.server.utils.GiaySpecifications;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuanLySanPhamService {

    private final GiayRepository giayRepository;
    private final GiayChiTietRepository giayChiTietRepository;
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

    public QuanLySanPhamService(
            GiayRepository giayRepository,
            GiayChiTietRepository giayChiTietRepository,
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
            CongNgheDemRepository congNgheDemRepository
    ) {
        this.giayRepository = giayRepository;
        this.giayChiTietRepository = giayChiTietRepository;
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
    }

    // ─── Danh mục ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DanhMucSanPhamResponse layDanhMuc() {
        var loaiGiay = loaiGiayRepository.findAll().stream()
                .filter(l -> l.getTrangThai() == 1)
                .map(l -> new LoaiGiayOption(l.getId(), l.getTen())).toList();
        var thuongHieu = thuongHieuRepository.findAll().stream()
                .filter(t -> t.getTrangThai() == 1)
                .map(t -> new ThuongHieuOption(t.getId(), t.getTen(), t.getLogoUrl())).toList();
        var mauSac = mauSacRepository.findAll().stream()
                .filter(m -> m.getTrangThai() == 1)
                .map(m -> new MauSacOption(m.getId(), m.getTen(), m.getMaMauHex())).toList();
        var kichCo = kichCoRepository.findAll().stream()
                .filter(k -> k.getTrangThai() == 1)
                .map(k -> new KichCoOption(k.getId(), k.getGiaTri())).toList();
        var deGiay = deGiayRepository.findAll().stream()
                .filter(d -> d.getTrangThai() == 1)
                .map(d -> new DeGiayOption(d.getId(), d.getTen())).toList();
        var coGiay = coGiayRepository.findAll().stream()
                .filter(c -> c.getTrangThai() == 1)
                .map(c -> new CoGiayOption(c.getId(), c.getTen())).toList();
        var chatLieuGiay = chatLieuGiayRepository.findAll().stream()
                .filter(c -> c.getTrangThai() == 1)
                .map(c -> new ChatLieuGiayOption(c.getId(), c.getTen())).toList();
        var trongLuong = trongLuongRepository.findAll().stream()
                .filter(t -> t.getTrangThai() == 1)
                .map(t -> new TrongLuongOption(t.getId(), t.getMa(), t.getGiaTri())).toList();
        var congNgheDem = congNgheDemRepository.findAll().stream()
                .filter(c -> c.getTrangThai() == 1)
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
                giaMin, giaMax, tongBienThe, tongSoLuong,
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
        return PageResponse.from(page.map(item -> toChiTietListItem(item, imageMap)));
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

    private ChiTietSanPhamListItemResponse toChiTietListItem(GiayChiTiet gct, Map<Integer, String> imageMap) {
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
                gct.getGiaBan(),
                gct.getKichHoat(),
                imageMap.get(gct.getId()),
                gct.getNgayTao(),
                gct.getNgayCapNhat()
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
        giay.setMoTa(req.moTa());
        giay.setTrangThai(1);
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
                    taoMaGiayTuDong(),
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
                    taoMaGiayTuDong(),
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
        giay.setMoTa(req.moTa());
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
        System.out.println(">>> DOI TRANG THAI GIAY #" + id + " -> " + req.trangThai());
        var giay = giayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giày #" + id + " không tồn tại"));
        giay.setTrangThai(req.trangThai());
        giay.setNgayCapNhat(Instant.now());
        if (req.trangThai() != 0) {
            updateTrangThaiTuSoLuong(id);
        }
    }

    @Transactional
    public void xoaGiay(Integer id) {
        System.out.println(">>> TOGGLE TRANG THAI GIAY #" + id);
        try {
            var giay = giayRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Giày #" + id + " không tồn tại"));
            
            if (giay.getTrangThai() == 0) {
                giay.setTrangThai(1); 
                updateTrangThaiTuSoLuong(id);
            } else {
                giay.setTrangThai(0);
            }
            giay.setNgayCapNhat(Instant.now());
        } catch (Exception e) {
            System.err.println("!!! LOI XOA GIAY: " + e.getMessage());
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
        return giayChiTietRepository.findByGiayIdEager(giayId).stream()
                .map(this::toBienThe).toList();
    }

    @Transactional
    public BienTheResponse taoBienThe(Integer giayId, TaoBienTheRequest req) {
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
        gct.setKichHoat(req.soLuong() > 0 ? 1 : 2);
        gct.setNgayTao(Instant.now());

        var saved = giayChiTietRepository.save(gct);
        updateTrangThaiTuSoLuong(giayId);
        return toBienThe(saved);
    }

    @Transactional
    public BienTheResponse capNhatBienThe(Integer id, CapNhatBienTheRequest req) {
        var gct = giayChiTietRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể #" + id + " không tồn tại"));
        gct.setSoLuong(req.soLuong());
        gct.setGiaGoc(req.giaGoc());
        gct.setGiaBan(req.giaBan());
        gct.setKichHoat(req.kichHoat());
        if (req.kichHoat() == 1 && req.soLuong() <= 0) {
            gct.setKichHoat(2);
        } else if (req.kichHoat() == 2 && req.soLuong() > 0) {
            gct.setKichHoat(1);
        }
        gct.setNgayCapNhat(Instant.now());
        var saved = giayChiTietRepository.save(gct);
        updateTrangThaiTuSoLuong(saved.getGiay().getId());
        return toBienThe(saved);
    }

    @Transactional
    public void xoaBienThe(Integer id) {
        var gct = giayChiTietRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể #" + id + " không tồn tại"));
        Integer giayId = gct.getGiay().getId();
        giayChiTietRepository.deleteById(id);
        updateTrangThaiTuSoLuong(giayId);
    }

    private BienTheResponse toBienThe(GiayChiTiet gct) {
        return new BienTheResponse(
                gct.getId(), gct.getMaBienThe(), gct.getSku(),
                gct.getSoLuong(), gct.getGiaGoc(), gct.getGiaBan(), gct.getKichHoat(),
                gct.getMauSac().getId(), gct.getMauSac().getTen(), gct.getMauSac().getMaMauHex(),
                gct.getKichCo().getId(), gct.getKichCo().getGiaTri(),
                gct.getNgayTao(), gct.getNgayCapNhat()
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

    private void updateTrangThaiTuSoLuong(Integer giayId) {
        var giay = giayRepository.findById(giayId).orElse(null);
        if (giay == null || giay.getTrangThai() == 0) return;

        Long totalQty = giayChiTietRepository.sumSoLuongByGiayId(giayId);
        int newStatus = (totalQty != null && totalQty > 0) ? 1 : 2;

        if (giay.getTrangThai() != newStatus) {
            giay.setTrangThai(newStatus);
            giay.setNgayCapNhat(Instant.now());
        }
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
