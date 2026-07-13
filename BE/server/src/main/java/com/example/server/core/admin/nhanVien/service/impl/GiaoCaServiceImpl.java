package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.MoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.BanGiaoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.XacNhanBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaStatsResponse;
import com.example.server.core.admin.nhanVien.service.GiaoCaService;
import com.example.server.entity.GiaoCa;
import com.example.server.entity.ChamCong;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.GiaoCaRepository;
import com.example.server.repository.NhanVienRepository;
import com.example.server.repository.ChamCongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.server.core.admin.thongbao.service.ThongBaoService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class GiaoCaServiceImpl implements GiaoCaService {

    private static final List<String> CHUA_KET_THUC_TRANG_THAI = List.of("MO_CA", "CHO_BAN_GIAO");
    private static final String KHONG_CO_QUYEN_MO_CA = "T\u1ea1m th\u1eddi kh\u00f4ng c\u00f3 quy\u1ec1n truy c\u1eadp.";

    private final GiaoCaRepository giaoCaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ChamCongRepository chamCongRepository;
    private final ThongBaoService thongBaoService;

    public GiaoCaServiceImpl(
            GiaoCaRepository giaoCaRepository,
            NhanVienRepository nhanVienRepository,
            ChamCongRepository chamCongRepository,
            ThongBaoService thongBaoService
    ) {
        this.giaoCaRepository = giaoCaRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.chamCongRepository = chamCongRepository;
        this.thongBaoService = thongBaoService;
    }

    @Override
    @Transactional(readOnly = true)
    public GiaoCaResponse layCaHoatDong(UUID nhanVienId) {
        return giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(nhanVienId, "MO_CA")
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

        // Cập nhật ca cũ thành DA_BAN_GIAO
        gc.setTrangThai("DA_BAN_GIAO");
        if (request.ghiChu() != null && !request.ghiChu().isBlank()) {
            gc.setGhiChu(gc.getGhiChu() == null ? request.ghiChu() : gc.getGhiChu() + " | Nhận: " + request.ghiChu());
        }
        giaoCaRepository.save(gc);

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
    public Page<GiaoCaResponse> layLichSuGiaoCa(UUID nhanVienId, String trangThai, Instant tuNgay, Instant denNgay, Pageable pageable) {
        return giaoCaRepository.searchHistory(nhanVienId, trangThai, tuNgay, denNgay, pageable)
                .map(this::mapToResponse);
    }

    private void checkCaChuaKetThucCuaNhanVienKhac(UUID nhanVienId) {
        // Cho phép nhiều nhân viên cùng mở ca để xử lý bán hàng song song
        // hoặc không cản trở nhân viên mới khi ca trước quên đóng.
    }

    private GiaoCaResponse mapToResponse(GiaoCa gc) {
        if (gc == null) return null;
        
        Instant thoiGianChamCong = null;
        if (gc.getNhanVienTrongCa() != null) {
            List<ChamCong> chamCongs = chamCongRepository.findByNhanVienIdAndThoiGianRaIsNull(gc.getNhanVienTrongCa().getId());
            if (chamCongs != null && !chamCongs.isEmpty()) {
                thoiGianChamCong = chamCongs.get(0).getThoiGianVao();
            }
        }
        
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
                thoiGianChamCong,
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
