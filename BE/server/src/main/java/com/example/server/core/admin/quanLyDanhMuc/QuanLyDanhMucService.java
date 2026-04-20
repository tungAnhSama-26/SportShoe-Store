package com.example.server.core.admin.quanLyDanhMuc;

import com.example.server.entity.*;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.*;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuanLyDanhMucService {

    private final LoaiGiayRepository loaiGiayRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final ChatLieuGiayRepository chatLieuGiayRepository;
    private final DeGiayRepository deGiayRepository;
    private final CoGiayRepository coGiayRepository;
    private final CongNgheDemRepository congNgheDemRepository;
    private final MauSacRepository mauSacRepository;
    private final KichCoRepository kichCoRepository;
    private final TrongLuongRepository trongLuongRepository;

    public QuanLyDanhMucService(
            LoaiGiayRepository loaiGiayRepository,
            ThuongHieuRepository thuongHieuRepository,
            ChatLieuGiayRepository chatLieuGiayRepository,
            DeGiayRepository deGiayRepository,
            CoGiayRepository coGiayRepository,
            CongNgheDemRepository congNgheDemRepository,
            MauSacRepository mauSacRepository,
            KichCoRepository kichCoRepository,
            TrongLuongRepository trongLuongRepository
    ) {
        this.loaiGiayRepository = loaiGiayRepository;
        this.thuongHieuRepository = thuongHieuRepository;
        this.chatLieuGiayRepository = chatLieuGiayRepository;
        this.deGiayRepository = deGiayRepository;
        this.coGiayRepository = coGiayRepository;
        this.congNgheDemRepository = congNgheDemRepository;
        this.mauSacRepository = mauSacRepository;
        this.kichCoRepository = kichCoRepository;
        this.trongLuongRepository = trongLuongRepository;
    }

    // ─── Loại Giày ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<LoaiGiayResponse> danhSachLoaiGiay(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(loaiGiayRepository.search(kw, pageable).map(this::toLoaiGiay));
    }

    @Transactional(readOnly = true)
    public LoaiGiayResponse chiTietLoaiGiay(Integer id) {
        return toLoaiGiay(loaiGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loại giày #" + id + " không tồn tại")));
    }

    @Transactional
    public LoaiGiayResponse taoLoaiGiay(LoaiGiayRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (loaiGiayRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("Mã loại giày '" + ma + "' đã tồn tại");
        }
        var e = new LoaiGiay();
        e.setMa(ma); e.setTen(req.ten().trim()); e.setMoTa(req.moTa());
        e.setTrangThai(1); e.setNgayTao(Instant.now());
        return toLoaiGiay(loaiGiayRepository.save(e));
    }

    @Transactional
    public LoaiGiayResponse capNhatLoaiGiay(Integer id, LoaiGiayRequest req) {
        var e = loaiGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loại giày #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (loaiGiayRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("Mã loại giày '" + ma + "' đã tồn tại");
        }
        e.setMa(ma); e.setTen(req.ten().trim()); e.setMoTa(req.moTa());
        e.setNgayCapNhat(Instant.now());
        return toLoaiGiay(e);
    }

    @Transactional
    public void doiTrangThaiLoaiGiay(Integer id, DoiTrangThaiDanhMucRequest req) {
        var e = loaiGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loại giày #" + id + " không tồn tại"));
        e.setTrangThai(req.trangThai()); e.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaLoaiGiay(Integer id) {
        if (!loaiGiayRepository.existsById(id)) throw new ResourceNotFoundException("Loại giày #" + id + " không tồn tại");
        loaiGiayRepository.deleteById(id);
    }

    private LoaiGiayResponse toLoaiGiay(LoaiGiay e) {
        return new LoaiGiayResponse(e.getId(), e.getMa(), e.getTen(), e.getMoTa(), e.getTrangThai(), e.getNgayTao(), e.getNgayCapNhat());
    }

    // ─── Thương Hiệu ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<ThuongHieuResponse> danhSachThuongHieu(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(thuongHieuRepository.search(kw, pageable).map(this::toThuongHieu));
    }

    @Transactional(readOnly = true)
    public ThuongHieuResponse chiTietThuongHieu(Integer id) {
        return toThuongHieu(thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu #" + id + " không tồn tại")));
    }

    @Transactional
    public ThuongHieuResponse taoThuongHieu(ThuongHieuRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (thuongHieuRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("Mã thương hiệu '" + ma + "' đã tồn tại");
        }
        var e = new ThuongHieu();
        e.setMa(ma); e.setTen(req.ten().trim()); e.setXuatXu(req.xuatXu());
        e.setLogoUrl(req.logoUrl()); e.setWebsite(req.website()); e.setMoTa(req.moTa());
        e.setTrangThai(1); e.setNgayTao(Instant.now());
        return toThuongHieu(thuongHieuRepository.save(e));
    }

    @Transactional
    public ThuongHieuResponse capNhatThuongHieu(Integer id, ThuongHieuRequest req) {
        var e = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (thuongHieuRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("Mã thương hiệu '" + ma + "' đã tồn tại");
        }
        e.setMa(ma); e.setTen(req.ten().trim()); e.setXuatXu(req.xuatXu());
        e.setLogoUrl(req.logoUrl()); e.setWebsite(req.website()); e.setMoTa(req.moTa());
        e.setNgayCapNhat(Instant.now());
        return toThuongHieu(e);
    }

    @Transactional
    public void doiTrangThaiThuongHieu(Integer id, DoiTrangThaiDanhMucRequest req) {
        var e = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu #" + id + " không tồn tại"));
        e.setTrangThai(req.trangThai()); e.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaThuongHieu(Integer id) {
        if (!thuongHieuRepository.existsById(id)) throw new ResourceNotFoundException("Thương hiệu #" + id + " không tồn tại");
        thuongHieuRepository.deleteById(id);
    }

    private ThuongHieuResponse toThuongHieu(ThuongHieu e) {
        return new ThuongHieuResponse(e.getId(), e.getMa(), e.getTen(), e.getXuatXu(), e.getLogoUrl(), e.getWebsite(), e.getMoTa(), e.getTrangThai(), e.getNgayTao(), e.getNgayCapNhat());
    }

    // ─── Đế Giày ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<ChatLieuGiayResponse> danhSachChatLieuGiay(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(chatLieuGiayRepository.search(kw, pageable).map(this::toChatLieuGiay));
    }

    @Transactional(readOnly = true)
    public ChatLieuGiayResponse chiTietChatLieuGiay(Integer id) {
        return toChatLieuGiay(chatLieuGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cháº¥t liá»‡u giÃ y #" + id + " khÃ´ng tá»“n táº¡i")));
    }

    @Transactional
    public ChatLieuGiayResponse taoChatLieuGiay(ChatLieuGiayRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (chatLieuGiayRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("MÃ£ cháº¥t liá»‡u giÃ y '" + ma + "' Ä‘Ã£ tá»“n táº¡i");
        }
        var e = new ChatLieuGiay();
        e.setMa(ma);
        e.setTen(req.ten().trim());
        e.setMoTa(req.moTa());
        e.setTrangThai(1);
        e.setNgayTao(Instant.now());
        return toChatLieuGiay(chatLieuGiayRepository.save(e));
    }

    @Transactional
    public ChatLieuGiayResponse capNhatChatLieuGiay(Integer id, ChatLieuGiayRequest req) {
        var e = chatLieuGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cháº¥t liá»‡u giÃ y #" + id + " khÃ´ng tá»“n táº¡i"));
        String ma = req.ma().trim().toUpperCase();
        if (chatLieuGiayRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("MÃ£ cháº¥t liá»‡u giÃ y '" + ma + "' Ä‘Ã£ tá»“n táº¡i");
        }
        e.setMa(ma);
        e.setTen(req.ten().trim());
        e.setMoTa(req.moTa());
        e.setNgayCapNhat(Instant.now());
        return toChatLieuGiay(e);
    }

    @Transactional
    public void doiTrangThaiChatLieuGiay(Integer id, DoiTrangThaiDanhMucRequest req) {
        var e = chatLieuGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cháº¥t liá»‡u giÃ y #" + id + " khÃ´ng tá»“n táº¡i"));
        e.setTrangThai(req.trangThai());
        e.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaChatLieuGiay(Integer id) {
        if (!chatLieuGiayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cháº¥t liá»‡u giÃ y #" + id + " khÃ´ng tá»“n táº¡i");
        }
        chatLieuGiayRepository.deleteById(id);
    }

    private ChatLieuGiayResponse toChatLieuGiay(ChatLieuGiay e) {
        return new ChatLieuGiayResponse(e.getId(), e.getMa(), e.getTen(), e.getMoTa(), e.getTrangThai(), e.getNgayTao(), e.getNgayCapNhat());
    }

    @Transactional(readOnly = true)
    public PageResponse<DeGiayResponse> danhSachDeGiay(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(deGiayRepository.search(kw, pageable).map(this::toDeGiay));
    }

    @Transactional(readOnly = true)
    public DeGiayResponse chiTietDeGiay(Integer id) {
        return toDeGiay(deGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đế giày #" + id + " không tồn tại")));
    }

    @Transactional
    public DeGiayResponse taoDeGiay(DeGiayRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (deGiayRepository.existsByMaIgnoreCase(ma)) throw new BusinessException("Mã đế giày '" + ma + "' đã tồn tại");
        var e = new DeGiay();
        e.setMa(ma); e.setTen(req.ten().trim()); e.setMoTa(req.moTa());
        e.setTrangThai(1); e.setNgayTao(Instant.now());
        return toDeGiay(deGiayRepository.save(e));
    }

    @Transactional
    public DeGiayResponse capNhatDeGiay(Integer id, DeGiayRequest req) {
        var e = deGiayRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Đế giày #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (deGiayRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) throw new BusinessException("Mã đế giày '" + ma + "' đã tồn tại");
        e.setMa(ma); e.setTen(req.ten().trim()); e.setMoTa(req.moTa()); e.setNgayCapNhat(Instant.now());
        return toDeGiay(e);
    }

    @Transactional
    public void doiTrangThaiDeGiay(Integer id, DoiTrangThaiDanhMucRequest req) {
        var e = deGiayRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Đế giày #" + id + " không tồn tại"));
        e.setTrangThai(req.trangThai()); e.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaDeGiay(Integer id) {
        if (!deGiayRepository.existsById(id)) throw new ResourceNotFoundException("Đế giày #" + id + " không tồn tại");
        deGiayRepository.deleteById(id);
    }

    private DeGiayResponse toDeGiay(DeGiay e) {
        return new DeGiayResponse(e.getId(), e.getMa(), e.getTen(), e.getMoTa(), e.getTrangThai(), e.getNgayTao(), e.getNgayCapNhat());
    }

    // ─── Cổ Giày ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<CoGiayResponse> danhSachCoGiay(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(coGiayRepository.search(kw, pageable).map(this::toCoGiay));
    }

    @Transactional(readOnly = true)
    public CoGiayResponse chiTietCoGiay(Integer id) {
        return toCoGiay(coGiayRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cổ giày #" + id + " không tồn tại")));
    }

    @Transactional
    public CoGiayResponse taoCoGiay(CoGiayRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (coGiayRepository.existsByMaIgnoreCase(ma)) throw new BusinessException("Mã cổ giày '" + ma + "' đã tồn tại");
        var e = new CoGiay();
        e.setMa(ma); e.setTen(req.ten().trim()); e.setMoTa(req.moTa());
        e.setTrangThai(1); e.setNgayTao(Instant.now());
        return toCoGiay(coGiayRepository.save(e));
    }

    @Transactional
    public CoGiayResponse capNhatCoGiay(Integer id, CoGiayRequest req) {
        var e = coGiayRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cổ giày #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (coGiayRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) throw new BusinessException("Mã cổ giày '" + ma + "' đã tồn tại");
        e.setMa(ma); e.setTen(req.ten().trim()); e.setMoTa(req.moTa()); e.setNgayCapNhat(Instant.now());
        return toCoGiay(e);
    }

    @Transactional
    public void doiTrangThaiCoGiay(Integer id, DoiTrangThaiDanhMucRequest req) {
        var e = coGiayRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cổ giày #" + id + " không tồn tại"));
        e.setTrangThai(req.trangThai()); e.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaCoGiay(Integer id) {
        if (!coGiayRepository.existsById(id)) throw new ResourceNotFoundException("Cổ giày #" + id + " không tồn tại");
        coGiayRepository.deleteById(id);
    }

    private CoGiayResponse toCoGiay(CoGiay e) {
        return new CoGiayResponse(e.getId(), e.getMa(), e.getTen(), e.getMoTa(), e.getTrangThai(), e.getNgayTao(), e.getNgayCapNhat());
    }

    // ─── Công Nghệ Đệm ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<CongNgheDemResponse> danhSachCongNgheDem(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(congNgheDemRepository.search(kw, pageable).map(this::toCongNgheDem));
    }

    @Transactional(readOnly = true)
    public CongNgheDemResponse chiTietCongNgheDem(Integer id) {
        return toCongNgheDem(congNgheDemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Công nghệ đệm #" + id + " không tồn tại")));
    }

    @Transactional
    public CongNgheDemResponse taoCongNgheDem(CongNgheDemRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (congNgheDemRepository.existsByMaIgnoreCase(ma)) throw new BusinessException("Mã công nghệ đệm '" + ma + "' đã tồn tại");
        var e = new CongNgheDem();
        e.setMa(ma); e.setTen(req.ten().trim()); e.setMoTa(req.moTa());
        e.setTrangThai(1); e.setNgayTao(Instant.now());
        return toCongNgheDem(congNgheDemRepository.save(e));
    }

    @Transactional
    public CongNgheDemResponse capNhatCongNgheDem(Integer id, CongNgheDemRequest req) {
        var e = congNgheDemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Công nghệ đệm #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (congNgheDemRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) throw new BusinessException("Mã công nghệ đệm '" + ma + "' đã tồn tại");
        e.setMa(ma); e.setTen(req.ten().trim()); e.setMoTa(req.moTa()); e.setNgayCapNhat(Instant.now());
        return toCongNgheDem(e);
    }

    @Transactional
    public void doiTrangThaiCongNgheDem(Integer id, DoiTrangThaiDanhMucRequest req) {
        var e = congNgheDemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Công nghệ đệm #" + id + " không tồn tại"));
        e.setTrangThai(req.trangThai()); e.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaCongNgheDem(Integer id) {
        if (!congNgheDemRepository.existsById(id)) throw new ResourceNotFoundException("Công nghệ đệm #" + id + " không tồn tại");
        congNgheDemRepository.deleteById(id);
    }

    private CongNgheDemResponse toCongNgheDem(CongNgheDem e) {
        return new CongNgheDemResponse(e.getId(), e.getMa(), e.getTen(), e.getMoTa(), e.getTrangThai(), e.getNgayTao(), e.getNgayCapNhat());
    }

    // ─── Màu Sắc ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<MauSacResponse> danhSachMauSac(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(mauSacRepository.search(kw, pageable).map(this::toMauSac));
    }

    @Transactional(readOnly = true)
    public MauSacResponse chiTietMauSac(Integer id) {
        return toMauSac(mauSacRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Màu sắc #" + id + " không tồn tại")));
    }

    @Transactional
    public MauSacResponse taoMauSac(MauSacRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (mauSacRepository.existsByMaIgnoreCase(ma)) throw new BusinessException("Mã màu sắc '" + ma + "' đã tồn tại");
        var e = new MauSac();
        e.setMa(ma); e.setTen(req.ten().trim()); e.setMaMauHex(req.maMauHex());
        e.setTrangThai(1); e.setNgayTao(Instant.now());
        return toMauSac(mauSacRepository.save(e));
    }

    @Transactional
    public MauSacResponse capNhatMauSac(Integer id, MauSacRequest req) {
        var e = mauSacRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Màu sắc #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (mauSacRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) throw new BusinessException("Mã màu sắc '" + ma + "' đã tồn tại");
        e.setMa(ma); e.setTen(req.ten().trim()); e.setMaMauHex(req.maMauHex()); e.setNgayCapNhat(Instant.now());
        return toMauSac(e);
    }

    @Transactional
    public void doiTrangThaiMauSac(Integer id, DoiTrangThaiDanhMucRequest req) {
        var e = mauSacRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Màu sắc #" + id + " không tồn tại"));
        e.setTrangThai(req.trangThai()); e.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaMauSac(Integer id) {
        if (!mauSacRepository.existsById(id)) throw new ResourceNotFoundException("Màu sắc #" + id + " không tồn tại");
        mauSacRepository.deleteById(id);
    }

    private MauSacResponse toMauSac(MauSac e) {
        return new MauSacResponse(e.getId(), e.getMa(), e.getTen(), e.getMaMauHex(), e.getTrangThai(), e.getNgayTao(), e.getNgayCapNhat());
    }

    // ─── Kích Cỡ ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<KichCoResponse> danhSachKichCo(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(kichCoRepository.search(kw, pageable).map(this::toKichCo));
    }

    @Transactional(readOnly = true)
    public KichCoResponse chiTietKichCo(Integer id) {
        return toKichCo(kichCoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Kích cỡ #" + id + " không tồn tại")));
    }

    @Transactional
    public KichCoResponse taoKichCo(KichCoRequest req) {
        String giaTri = req.giaTri().trim();
        if (kichCoRepository.existsByGiaTriIgnoreCase(giaTri)) throw new BusinessException("Kích cỡ '" + giaTri + "' đã tồn tại");
        var e = new KichCo();
        e.setGiaTri(giaTri); e.setGhiChu(req.ghiChu());
        e.setTrangThai(1); e.setNgayTao(Instant.now());
        return toKichCo(kichCoRepository.save(e));
    }

    @Transactional
    public KichCoResponse capNhatKichCo(Integer id, KichCoRequest req) {
        var e = kichCoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Kích cỡ #" + id + " không tồn tại"));
        String giaTri = req.giaTri().trim();
        if (kichCoRepository.existsByGiaTriIgnoreCaseAndIdNot(giaTri, id)) throw new BusinessException("Kích cỡ '" + giaTri + "' đã tồn tại");
        e.setGiaTri(giaTri); e.setGhiChu(req.ghiChu()); e.setNgayCapNhat(Instant.now());
        return toKichCo(e);
    }

    @Transactional
    public void doiTrangThaiKichCo(Integer id, DoiTrangThaiDanhMucRequest req) {
        var e = kichCoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Kích cỡ #" + id + " không tồn tại"));
        e.setTrangThai(req.trangThai()); e.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaKichCo(Integer id) {
        if (!kichCoRepository.existsById(id)) throw new ResourceNotFoundException("Kích cỡ #" + id + " không tồn tại");
        kichCoRepository.deleteById(id);
    }

    private KichCoResponse toKichCo(KichCo e) {
        return new KichCoResponse(e.getId(), e.getGiaTri(), e.getGhiChu(), e.getTrangThai(), e.getNgayTao(), e.getNgayCapNhat());
    }

    // ─── Trọng Lượng ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<TrongLuongResponse> danhSachTrongLuong(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(trongLuongRepository.search(kw, pageable).map(this::toTrongLuong));
    }

    @Transactional(readOnly = true)
    public TrongLuongResponse chiTietTrongLuong(Integer id) {
        return toTrongLuong(trongLuongRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Trọng lượng #" + id + " không tồn tại")));
    }

    @Transactional
    public TrongLuongResponse taoTrongLuong(TrongLuongRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (trongLuongRepository.existsByMaIgnoreCase(ma)) throw new BusinessException("Mã trọng lượng '" + ma + "' đã tồn tại");
        var e = new TrongLuong();
        e.setMa(ma); e.setGiaTri(req.giaTri()); e.setMoTa(req.moTa());
        e.setTrangThai(1); e.setNgayTao(Instant.now());
        return toTrongLuong(trongLuongRepository.save(e));
    }

    @Transactional
    public TrongLuongResponse capNhatTrongLuong(Integer id, TrongLuongRequest req) {
        var e = trongLuongRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Trọng lượng #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (trongLuongRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) throw new BusinessException("Mã trọng lượng '" + ma + "' đã tồn tại");
        e.setMa(ma); e.setGiaTri(req.giaTri()); e.setMoTa(req.moTa()); e.setNgayCapNhat(Instant.now());
        return toTrongLuong(e);
    }

    @Transactional
    public void doiTrangThaiTrongLuong(Integer id, DoiTrangThaiDanhMucRequest req) {
        var e = trongLuongRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Trọng lượng #" + id + " không tồn tại"));
        e.setTrangThai(req.trangThai()); e.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaTrongLuong(Integer id) {
        if (!trongLuongRepository.existsById(id)) throw new ResourceNotFoundException("Trọng lượng #" + id + " không tồn tại");
        trongLuongRepository.deleteById(id);
    }

    private TrongLuongResponse toTrongLuong(TrongLuong e) {
        return new TrongLuongResponse(e.getId(), e.getMa(), e.getGiaTri(), e.getMoTa(), e.getTrangThai(), e.getNgayTao(), e.getNgayCapNhat());
    }

    // ─── Utils ───────────────────────────────────────────────────────────────

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
