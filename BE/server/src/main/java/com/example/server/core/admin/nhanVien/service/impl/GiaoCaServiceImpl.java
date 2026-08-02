package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.MoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.BanGiaoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.XacNhanBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.request.KetCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaOptionsResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaStatsResponse;
import com.example.server.core.admin.nhanVien.service.GiaoCaService;
import com.example.server.entity.GiaoCa;
import com.example.server.entity.NhanVien;
import com.example.server.entity.CaLam;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.GiaoCaRepository;
import com.example.server.repository.NhanVienRepository;
import com.example.server.repository.LichLamViecRepository;
import com.example.server.repository.CaLamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.server.core.admin.thongbao.service.ThongBaoService;

import java.math.BigDecimal;
import java.util.Locale;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GiaoCaServiceImpl implements GiaoCaService {

    private static final List<String> CHUA_KET_THUC_TRANG_THAI = List.of("MO_CA", "CHO_BAN_GIAO");
    private static final String KHONG_CO_QUYEN_MO_CA = "T\u1ea1m th\u1eddi kh\u00f4ng c\u00f3 quy\u1ec1n truy c\u1eadp.";

    private final GiaoCaRepository giaoCaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ThongBaoService thongBaoService;
    private final LichLamViecRepository lichLamViecRepository;
    private final CaLamRepository caLamRepository;

    public GiaoCaServiceImpl(
            GiaoCaRepository giaoCaRepository,
            NhanVienRepository nhanVienRepository,
            ThongBaoService thongBaoService,
            LichLamViecRepository lichLamViecRepository,
            CaLamRepository caLamRepository
    ) {
        this.giaoCaRepository = giaoCaRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.thongBaoService = thongBaoService;
        this.lichLamViecRepository = lichLamViecRepository;
        this.caLamRepository = caLamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public GiaoCaResponse layCaHoatDong(UUID nhanVienId) {
        return giaoCaRepository.findFirstByNhanVienTrongCaIdAndTrangThaiInOrderByThoiGianVaoDesc(nhanVienId, CHUA_KET_THUC_TRANG_THAI)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public void kiemTraQuyenMoCa(UUID nhanVienId) {
        checkCaChuaKetThucCuaNhanVienKhac(nhanVienId);
    }

    @Override
    @Transactional
    public GiaoCaResponse moCa(UUID nhanVienId, MoCaRequest request) {
        checkCaChuaKetThucCuaNhanVienKhac(nhanVienId);

        if (giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(nhanVienId, "MO_CA").isPresent()) {
            throw new BusinessException("Nhân viên đã có ca làm việc đang hoạt động.");
        }

        if (giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(nhanVienId, "CHO_BAN_GIAO").isPresent()) {
            throw new BusinessException("Bạn có ca làm việc đang chờ bàn giao. Vui lòng hoàn tất bàn giao trước.");
        }

        NhanVien nhanVien = nhanVienRepository.findById(nhanVienId)
                .orElseThrow(() -> new BusinessException("Nhân viên không tồn tại."));

        GiaoCa giaoCa = new GiaoCa();
        giaoCa.setMa("GC" + System.currentTimeMillis());
        giaoCa.setNhanVienTrongCa(nhanVien);
        giaoCa.setThoiGianVao(Instant.now());
        giaoCa.setTienDauCa(request.tienDauCa());
        giaoCa.setTienMatTrongCa(BigDecimal.ZERO);
        giaoCa.setTienChuyenKhoanTrongCa(BigDecimal.ZERO);
        giaoCa.setTrangThai("MO_CA");
        giaoCa.setGhiChu(request.ghiChu());

        return mapToResponse(giaoCaRepository.save(giaoCa));
    }

    @Override
    @Transactional(readOnly = true)
    public GiaoCaStatsResponse layThongTinGiaoCaCurrent(UUID nhanVienId) {
        GiaoCa gc = giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(nhanVienId, "MO_CA")
                .orElseThrow(() -> new BusinessException("Nhân viên không có ca làm việc nào đang hoạt động."));

        BigDecimal tienMat = giaoCaRepository.calculateTienMatTrongCa(gc.getId());
        BigDecimal tienCK = giaoCaRepository.calculateTienChuyenKhoanTrongCa(gc.getId());
        BigDecimal tienHeThong = gc.getTienDauCa().add(tienMat);

        return new GiaoCaStatsResponse(tienMat, tienCK, tienHeThong);
    }

    @Override
    @Transactional
    public GiaoCaResponse banGiaoCa(UUID nhanVienId, BanGiaoCaRequest request) {
        GiaoCa gc = giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(nhanVienId, "MO_CA")
                .orElseThrow(() -> new BusinessException("Không tìm thấy ca làm việc đang hoạt động."));

        if (request.nhanVienNhanId().equals(nhanVienId)) {
            throw new BusinessException("Không thể bàn giao ca cho chính mình.");
        }

        NhanVien nhanVienNhan = nhanVienRepository.findById(request.nhanVienNhanId())
                .orElseThrow(() -> new BusinessException("Nhân viên nhận bàn giao không tồn tại."));

        GiaoCaOptionsResponse options = layTuyChonBanGiao(nhanVienId);
        if (options.coTheKetCa()) {
            throw new BusinessException("Đây là ca cuối trong ngày. Vui lòng sử dụng chức năng kết ca làm việc.");
        }
        boolean hopLe = options.nhanVienNhanCa().stream().anyMatch(item -> item.id().equals(nhanVienNhan.getId()));
        if (!hopLe) {
            throw new BusinessException("Nhân viên nhận không thuộc ca kế tiếp và không phải quản trị viên hợp lệ.");
        }

        BigDecimal tienMat = giaoCaRepository.calculateTienMatTrongCa(gc.getId());
        BigDecimal tienCK = giaoCaRepository.calculateTienChuyenKhoanTrongCa(gc.getId());
        BigDecimal tienHeThong = gc.getTienDauCa().add(tienMat);
        BigDecimal tienChenhLech = request.tienCuoiCaThucTe().subtract(tienHeThong);

        if (tienChenhLech.compareTo(BigDecimal.ZERO) != 0 && (request.lyDoChenhLech() == null || request.lyDoChenhLech().isBlank())) {
            throw new BusinessException("Số tiền chênh lệch khác 0. Vui lòng nhập lý do chênh lệch.");
        }

        gc.setThoiGianRa(Instant.now());
        gc.setTienMatTrongCa(tienMat);
        gc.setTienChuyenKhoanTrongCa(tienCK);
        gc.setTienCuoiCaThucTe(request.tienCuoiCaThucTe());
        gc.setTienCuoiCaHeThong(tienHeThong);
        gc.setTienChenhLech(tienChenhLech);
        gc.setLyDoChenhLech(request.lyDoChenhLech());
        gc.setNhanVienNhan(nhanVienNhan);
        gc.setTrangThai("CHO_BAN_GIAO");
        gc.setGhiChu(request.ghiChu());

        GiaoCa saved = giaoCaRepository.save(gc);

        // Trigger shift handover notification
        try {
            String tenNV = saved.getNhanVienTrongCa() != null ? saved.getNhanVienTrongCa().getHoTen() : "Nhân viên";
            thongBaoService.taoThongBao(
                    "Yêu cầu bàn giao ca chờ duyệt",
                    "Nhân viên \"" + tenNV + "\" đã gửi yêu cầu bàn giao ca (Mã ca: " + saved.getMa() + ").",
                    "SHIFT",
                    "/admin/ban-giao-ca"
            );
        } catch (Exception e) {
            System.err.println("[GiaoCa] Lỗi gửi thông báo bàn giao ca: " + e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GiaoCaOptionsResponse layTuyChonBanGiao(UUID nhanVienId) {
        giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(nhanVienId, "MO_CA")
                .orElseThrow(() -> new BusinessException("Không tìm thấy ca làm việc đang hoạt động."));

        LocalDate homNay = LocalDate.now(ZoneId.of("Asia/Bangkok"));
        Optional<CaLam> caKeTiep = timCaKeTiep(nhanVienId, homNay);
        if (caKeTiep.isEmpty()) {
            return new GiaoCaOptionsResponse(true, null, List.of());
        }

        String maCaKeTiep = caKeTiep.get().getId();
        List<GiaoCaOptionsResponse.NhanVienNhanCaResponse> candidates = nhanVienRepository.findAll().stream()
                .filter(nv -> nv.getTrangThai() != null && nv.getTrangThai() == 1)
                .filter(nv -> !nv.getId().equals(nhanVienId))
                .filter(nv -> (nv.getVaiTro() != null && nv.getVaiTro() == 1)
                        || lichLamViecRepository.existsByNhanVienIdAndNgayAndCa(nv.getId(), homNay, maCaKeTiep))
                .filter(nv -> !giaoCaRepository.existsByNhanVienTrongCaIdAndTrangThaiIn(nv.getId(), CHUA_KET_THUC_TRANG_THAI))
                .map(nv -> new GiaoCaOptionsResponse.NhanVienNhanCaResponse(
                        nv.getId(), nv.getMa(), nv.getHoTen(), nv.getVaiTro()))
                .toList();
        return new GiaoCaOptionsResponse(candidates.isEmpty(), maCaKeTiep, candidates);
    }

    @Override
    @Transactional
    public GiaoCaResponse ketCa(UUID nhanVienId, KetCaRequest request) {
        GiaoCa gc = giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(nhanVienId, "MO_CA")
                .orElseThrow(() -> new BusinessException("Không tìm thấy ca làm việc đang hoạt động."));
        if (!layTuyChonBanGiao(nhanVienId).coTheKetCa()) {
            throw new BusinessException("Vẫn còn ca làm việc tiếp theo. Vui lòng bàn giao cho nhân viên ca kế tiếp.");
        }
        chotSoLieuCa(gc, request.tienCuoiCaThucTe(), request.lyDoChenhLech(), request.ghiChu());
        gc.setTrangThai("DA_KET_THUC");
        return mapToResponse(giaoCaRepository.save(gc));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GiaoCaResponse> layCaChoXacNhan(UUID nhanVienId) {
        return giaoCaRepository.findByNhanVienNhanIdAndTrangThaiOrderByThoiGianVaoDesc(nhanVienId, "CHO_BAN_GIAO")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public GiaoCaResponse xacNhanBanGiao(UUID nhanVienId, UUID giaoCaId, XacNhanBanGiaoRequest request) {
        GiaoCa gc = giaoCaRepository.findById(giaoCaId)
                .orElseThrow(() -> new BusinessException("Biên bản giao ca không tồn tại."));

        if (!"CHO_BAN_GIAO".equals(gc.getTrangThai())) {
            throw new BusinessException("Trạng thái ca làm việc không hợp lệ.");
        }

        if (!gc.getNhanVienNhan().getId().equals(nhanVienId)) {
            throw new BusinessException("Bạn không phải người nhận bàn giao của ca làm việc này.");
        }

        if (giaoCaRepository.existsByNhanVienTrongCaIdAndTrangThaiIn(nhanVienId, CHUA_KET_THUC_TRANG_THAI)) {
            throw new BusinessException("Bạn đang có ca làm việc khác chưa kết thúc.");
        }

        // Cập nhật ca cũ thành DA_BAN_GIAO
        gc.setTrangThai("DA_BAN_GIAO");
        if (request.ghiChu() != null && !request.ghiChu().isBlank()) {
            gc.setGhiChu(gc.getGhiChu() == null ? request.ghiChu() : gc.getGhiChu() + " | Nhận: " + request.ghiChu());
        }
        giaoCaRepository.saveAndFlush(gc);

        // Tự động mở ca mới cho nhân viên nhận với tiền đầu ca = tiền thực tế bàn giao
        GiaoCa gcMoi = new GiaoCa();
        gcMoi.setMa("GC" + System.currentTimeMillis());
        gcMoi.setNhanVienTrongCa(gc.getNhanVienNhan());
        gcMoi.setThoiGianVao(Instant.now());
        gcMoi.setTienDauCa(gc.getTienCuoiCaThucTe());
        gcMoi.setTienMatTrongCa(BigDecimal.ZERO);
        gcMoi.setTienChuyenKhoanTrongCa(BigDecimal.ZERO);
        gcMoi.setTrangThai("MO_CA");
        gcMoi.setGhiChu("Mở ca tự động từ ca bàn giao " + gc.getMa());

        giaoCaRepository.save(gcMoi);

        return mapToResponse(gc);
    }

    @Override
    @Transactional(readOnly = true)
    public void checkActiveShiftOrThrow(UUID nhanVienId) {
        if (giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(nhanVienId, "MO_CA").isEmpty()) {
            throw new BusinessException("Bạn cần phải mở ca làm việc trước khi thực hiện giao dịch này.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GiaoCaResponse> layLichSuGiaoCa(UUID nhanVienId, String trangThai, Instant tuNgay, Instant denNgay, String keyword, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        boolean isCaSang = false;
        boolean isCaChieu = false;
        boolean isCaToi = false;

        if (kw != null) {
            String lowerKw = kw.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
            if (lowerKw.equals("ca00001") || lowerKw.equals("ca001") || lowerKw.equals("ca1") || lowerKw.contains("casang") || lowerKw.contains("sang")) {
                isCaSang = true;
            } else if (lowerKw.equals("ca00002") || lowerKw.equals("ca002") || lowerKw.equals("ca2") || lowerKw.contains("cachieu") || lowerKw.contains("chieu")) {
                isCaChieu = true;
            } else if (lowerKw.equals("ca00003") || lowerKw.equals("ca003") || lowerKw.equals("ca3") || lowerKw.contains("catoi") || lowerKw.contains("toi")) {
                isCaToi = true;
            }
        }

        return giaoCaRepository.searchHistory(nhanVienId, trangThai, tuNgay, denNgay, kw, isCaSang, isCaChieu, isCaToi, pageable)
                .map(this::mapToResponse);
    }

    private void checkCaChuaKetThucCuaNhanVienKhac(UUID nhanVienId) {
        if (giaoCaRepository.existsByTrangThaiIn(CHUA_KET_THUC_TRANG_THAI)) {
            throw new BusinessException("Cửa hàng đang có ca làm việc chưa kết thúc. Vui lòng hoàn tất ca hiện tại trước.");
        }
    }

    private Optional<CaLam> timCaKeTiep(UUID nhanVienId, LocalDate ngay) {
        List<CaLam> cacCa = caLamRepository.findAll().stream()
                .filter(ca -> Boolean.TRUE.equals(ca.getTrangThai()))
                .sorted(Comparator.comparing(ca -> LocalTime.parse(ca.getGioBatDau())))
                .toList();
        if (cacCa.isEmpty()) return Optional.empty();

        String maCaHienTai = lichLamViecRepository.findByNhanVienIdAndNgay(nhanVienId, ngay)
                .map(lich -> lich.getCa())
                .orElseGet(() -> {
                    LocalTime now = LocalTime.now(ZoneId.of("Asia/Bangkok"));
                    return cacCa.stream()
                            .filter(ca -> !now.isBefore(LocalTime.parse(ca.getGioBatDau()))
                                    && now.isBefore(LocalTime.parse(ca.getGioKetThuc())))
                            .map(CaLam::getId).findFirst().orElse(null);
                });
        if (maCaHienTai == null) return Optional.empty();
        for (int i = 0; i < cacCa.size() - 1; i++) {
            if (cacCa.get(i).getId().equals(maCaHienTai)) return Optional.of(cacCa.get(i + 1));
        }
        return Optional.empty();
    }

    private void chotSoLieuCa(GiaoCa gc, BigDecimal tienThucTe, String lyDo, String ghiChu) {
        BigDecimal tienMat = giaoCaRepository.calculateTienMatTrongCa(gc.getId());
        BigDecimal tienCK = giaoCaRepository.calculateTienChuyenKhoanTrongCa(gc.getId());
        BigDecimal tienHeThong = gc.getTienDauCa().add(tienMat);
        BigDecimal chenhLech = tienThucTe.subtract(tienHeThong);
        if (chenhLech.compareTo(BigDecimal.ZERO) != 0 && (lyDo == null || lyDo.isBlank())) {
            throw new BusinessException("Số tiền chênh lệch khác 0. Vui lòng nhập lý do chênh lệch.");
        }
        gc.setThoiGianRa(Instant.now());
        gc.setTienMatTrongCa(tienMat);
        gc.setTienChuyenKhoanTrongCa(tienCK);
        gc.setTienCuoiCaThucTe(tienThucTe);
        gc.setTienCuoiCaHeThong(tienHeThong);
        gc.setTienChenhLech(chenhLech);
        gc.setLyDoChenhLech(lyDo);
        gc.setGhiChu(ghiChu);
    }

    private GiaoCaResponse mapToResponse(GiaoCa gc) {
        if (gc == null) return null;
        
        return new GiaoCaResponse(
                gc.getId(),
                gc.getMa(),
                gc.getNhanVienTrongCa().getId(),
                gc.getNhanVienTrongCa().getHoTen(),
                gc.getNhanVienTrongCa().getMa(),
                gc.getNhanVienNhan() != null ? gc.getNhanVienNhan().getId() : null,
                gc.getNhanVienNhan() != null ? gc.getNhanVienNhan().getHoTen() : null,
                gc.getNhanVienNhan() != null ? gc.getNhanVienNhan().getMa() : null,
                gc.getThoiGianVao(),
                gc.getThoiGianRa(),
                gc.getTienDauCa(),
                gc.getTienMatTrongCa(),
                gc.getTienChuyenKhoanTrongCa(),
                gc.getTienCuoiCaThucTe(),
                gc.getTienCuoiCaHeThong(),
                gc.getTienChenhLech(),
                gc.getLyDoChenhLech(),
                gc.getTrangThai(),
                gc.getGhiChu()
        );
    }
}
