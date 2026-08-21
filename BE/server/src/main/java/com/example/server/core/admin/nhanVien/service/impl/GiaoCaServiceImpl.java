package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.BanGiaoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.BaoCaoSuCoGiaoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.HuyBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.request.KetCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.MoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.TuChoiBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.request.XacNhanBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaOptionsResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaStatsResponse;
import com.example.server.core.admin.nhanVien.service.GiaoCaService;
import com.example.server.core.admin.nhanVien.service.TrangThaiGiaoCa;
import com.example.server.core.admin.thongbao.service.ThongBaoService;
import com.example.server.entity.CaLam;
import com.example.server.entity.GiaoCa;
import com.example.server.entity.LichLamViec;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.CaLamRepository;
import com.example.server.repository.GiaoCaRepository;
import com.example.server.repository.LichLamViecRepository;
import com.example.server.repository.NhanVienRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class GiaoCaServiceImpl implements GiaoCaService {

    private static final ZoneId MUI_GIO = ZoneId.of("Asia/Bangkok");
    private static final int PHUT_CHO_PHEP_MO_CA = 30;
    private static final List<String> TRANG_THAI_CHUA_KET_THUC = List.of(
            TrangThaiGiaoCa.MO_CA.ma(), TrangThaiGiaoCa.CHO_BAN_GIAO.ma());

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
        NhanVien nhanVien = layNhanVienHoatDong(nhanVienId);
        if (laAdmin(nhanVien)) {
            Optional<GiaoCa> caCuaAdmin = giaoCaRepository
                    .findFirstByNhanVienTrongCaIdAndTrangThaiInOrderByThoiGianVaoDesc(
                            nhanVienId, TRANG_THAI_CHUA_KET_THUC);
            if (caCuaAdmin.isPresent()) {
                return mapToResponse(caCuaAdmin.get());
            }
            return giaoCaRepository
                    .findFirstByTrangThaiInOrderByThoiGianVaoDesc(List.of(TrangThaiGiaoCa.MO_CA.ma()))
                    .map(this::mapToResponse)
                    .orElse(null);
        }
        return giaoCaRepository
                .findFirstByNhanVienTrongCaIdAndTrangThaiInOrderByThoiGianVaoDesc(
                        nhanVienId, TRANG_THAI_CHUA_KET_THUC)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public GiaoCaResponse moCa(UUID nhanVienId, MoCaRequest request) {
        NhanVien nhanVien = layNhanVienHoatDong(nhanVienId);
        if (laAdmin(nhanVien)) {
            kiemTraNhanVienKhongCoCaChuaKetThuc(nhanVienId);
        } else {
            kiemTraKhongCoCaChuaKetThuc();
        }
        LocalTime hienTai = LocalTime.now(MUI_GIO);
        CaLam caLam = xacDinhCaDuocMo(
                nhanVien,
                LocalDate.now(MUI_GIO),
                hienTai,
                request.caLamId(),
                request.lyDoMoMuon());

        String ghiChu = taoGhiChuMoCa(nhanVien, caLam, hienTai, request);
        GiaoCa giaoCa = taoCaMoi(nhanVien, caLam, request.tienDauCa(), ghiChu);
        return mapToResponse(giaoCaRepository.save(giaoCa));
    }

    @Override
    @Transactional(readOnly = true)
    public GiaoCaStatsResponse layThongTinGiaoCaCurrent(UUID nhanVienId) {
        GiaoCa giaoCa = layCaDangMoCoQuyenTruyCap(nhanVienId);
        BigDecimal tienMat = tinhTienMat(giaoCa.getId());
        BigDecimal tienChuyenKhoan = tinhTienChuyenKhoan(giaoCa.getId());
        return new GiaoCaStatsResponse(tienMat, tienChuyenKhoan, giaoCa.getTienDauCa().add(tienMat));
    }

    @Override
    @Transactional
    public GiaoCaResponse banGiaoCa(UUID nhanVienId, BanGiaoCaRequest request) {
        GiaoCa giaoCa = layCaDangMoCuaNhanVien(nhanVienId);
        if (request.nhanVienNhanId().equals(nhanVienId)) {
            throw new BusinessException("Không thể bàn giao ca cho chính mình.");
        }

        GiaoCaOptionsResponse options = taoTuyChonBanGiao(giaoCa);
        if (options.coTheKetCa()) {
            throw new BusinessException("Đây là ca cuối trong ngày. Vui lòng sử dụng chức năng kết ca làm việc.");
        }
        if (options.nhanVienNhanCa().isEmpty()) {
            throw new BusinessException(options.lyDoKhongTheBanGiao());
        }

        NhanVien nguoiNhan = layNhanVienHoatDong(request.nhanVienNhanId());
        boolean hopLe = options.nhanVienNhanCa().stream().anyMatch(item -> item.id().equals(nguoiNhan.getId()));
        if (!hopLe) {
            throw new BusinessException("Nhân viên nhận không thuộc ca tiếp theo và không phải quản trị viên hợp lệ.");
        }

        BigDecimal tienHeThong = giaoCa.getTienDauCa().add(tinhTienMat(giaoCa.getId()));
        BigDecimal chenhLech = request.tienCuoiCaThucTe().subtract(tienHeThong);
        kiemTraLyDoChenhLech(chenhLech, request.lyDoChenhLech());

        giaoCa.setThoiGianRa(Instant.now());
        giaoCa.setTienCuoiCaThucTe(request.tienCuoiCaThucTe());
        giaoCa.setTienNhanKiemDem(null);
        giaoCa.setLyDoChenhLech(request.lyDoChenhLech());
        giaoCa.setNhanVienNhan(nguoiNhan);
        giaoCa.setTrangThai(TrangThaiGiaoCa.CHO_BAN_GIAO.ma());
        giaoCa.setCaChuaKetThuc(1);
        giaoCa.setGhiChu(gioiHanGhiChu(request.ghiChu()));
        GiaoCa daLuu = giaoCaRepository.save(giaoCa);

        guiThongBaoAnToan(
                "Yêu cầu bàn giao ca chờ xác nhận",
                "Nhân viên \"" + giaoCa.getNhanVienTrongCa().getHoTen() + "\" đã gửi yêu cầu bàn giao ca "
                        + giaoCa.getMa() + " cho " + nguoiNhan.getHoTen() + ".",
                "SHIFT",
                "/admin/ban-giao-ca"
        );
        return mapToResponse(daLuu);
    }

    @Override
    @Transactional(readOnly = true)
    public GiaoCaOptionsResponse layTuyChonBanGiao(UUID nhanVienId) {
        return taoTuyChonBanGiao(layCaDangMoCuaNhanVien(nhanVienId));
    }

    @Override
    @Transactional
    public GiaoCaResponse ketCa(UUID nhanVienId, KetCaRequest request) {
        GiaoCa giaoCa = layCaDangMoCuaNhanVien(nhanVienId);
        if (timCaKeTiep(giaoCa.getCaLam()).isPresent()) {
            throw new BusinessException("Vẫn còn ca làm việc tiếp theo. Vui lòng bàn giao cho nhân viên ca tiếp theo.");
        }

        chotSoLieuCa(giaoCa, request.tienCuoiCaThucTe(), request.lyDoChenhLech(), request.ghiChu());
        giaoCa.setTrangThai(TrangThaiGiaoCa.DA_KET_THUC.ma());
        giaoCa.setCaChuaKetThuc(null);
        return mapToResponse(giaoCaRepository.save(giaoCa));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GiaoCaResponse> layCaChoXacNhan(UUID nhanVienId) {
        return giaoCaRepository
                .findByNhanVienNhanIdAndTrangThaiOrderByThoiGianVaoDesc(
                        nhanVienId, TrangThaiGiaoCa.CHO_BAN_GIAO.ma())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public GiaoCaResponse xacNhanBanGiao(
            UUID nhanVienId, UUID giaoCaId, XacNhanBanGiaoRequest request) {
        GiaoCa giaoCa = layCaChoCapNhat(giaoCaId);
        kiemTraDangChoBanGiao(giaoCa);
        if (giaoCa.getNhanVienNhan() == null || !giaoCa.getNhanVienNhan().getId().equals(nhanVienId)) {
            throw new BusinessException("Bạn không phải người nhận bàn giao của ca làm việc này.");
        }

        NhanVien nguoiNhan = layNhanVienHoatDong(nhanVienId);
        if (giaoCaRepository.existsByNhanVienTrongCaIdAndTrangThaiIn(nhanVienId, TRANG_THAI_CHUA_KET_THUC)) {
            throw new BusinessException("Bạn đang có ca làm việc khác chưa kết thúc.");
        }

        CaLam caKeTiep = timCaKeTiep(giaoCa.getCaLam())
                .orElseThrow(() -> new BusinessException("Không còn ca tiếp theo để nhận bàn giao."));
        kiemTraNguoiNhanThuocCa(nguoiNhan, caKeTiep, LocalDate.now(MUI_GIO));

        if (giaoCa.getTienCuoiCaThucTe() == null
                || request.tienNhanKiemDem().compareTo(giaoCa.getTienCuoiCaThucTe()) != 0) {
            throw new BusinessException("Số tiền người nhận kiểm đếm không khớp số tiền người giao bàn giao. Vui lòng từ chối hoặc báo cáo sự cố.");
        }

        giaoCa.setTienNhanKiemDem(request.tienNhanKiemDem());
        giaoCa.setThoiGianXacNhan(Instant.now());
        giaoCa.setTrangThai(TrangThaiGiaoCa.DA_BAN_GIAO.ma());
        giaoCa.setCaChuaKetThuc(null);
        giaoCa.setGhiChu(ghepGhiChu(giaoCa.getGhiChu(), "Nhận: " + request.ghiChu()));
        giaoCaRepository.saveAndFlush(giaoCa);

        GiaoCa caMoi = taoCaMoi(
                nguoiNhan,
                caKeTiep,
                request.tienNhanKiemDem(),
                "Mở ca tự động từ ca bàn giao " + giaoCa.getMa());
        giaoCaRepository.save(caMoi);
        return mapToResponse(giaoCa);
    }

    @Override
    @Transactional
    public GiaoCaResponse huyBanGiao(UUID nhanVienId, UUID giaoCaId, HuyBanGiaoRequest request) {
        GiaoCa giaoCa = layCaChoCapNhat(giaoCaId);
        kiemTraDangChoBanGiao(giaoCa);
        if (!giaoCa.getNhanVienTrongCa().getId().equals(nhanVienId)) {
            throw new BusinessException("Chỉ người giao ca mới được hủy yêu cầu bàn giao.");
        }
        String lyDo = request == null ? null : request.lyDo();
        khoiPhucCaDangMo(giaoCa, "Hủy bàn giao" + noiDungLyDo(lyDo));
        return mapToResponse(giaoCaRepository.save(giaoCa));
    }

    @Override
    @Transactional
    public GiaoCaResponse tuChoiBanGiao(UUID nhanVienId, UUID giaoCaId, TuChoiBanGiaoRequest request) {
        GiaoCa giaoCa = layCaChoCapNhat(giaoCaId);
        kiemTraDangChoBanGiao(giaoCa);
        if (giaoCa.getNhanVienNhan() == null || !giaoCa.getNhanVienNhan().getId().equals(nhanVienId)) {
            throw new BusinessException("Chỉ người được chỉ định nhận ca mới được từ chối.");
        }
        String tenNguoiNhan = giaoCa.getNhanVienNhan().getHoTen();
        khoiPhucCaDangMo(giaoCa, "Từ chối bởi " + tenNguoiNhan + noiDungLyDo(request.lyDo()));
        GiaoCa daLuu = giaoCaRepository.save(giaoCa);
        guiThongBaoAnToan(
                "Yêu cầu bàn giao ca bị từ chối",
                "Ca " + giaoCa.getMa() + " bị " + tenNguoiNhan + " từ chối. Lý do: " + request.lyDo(),
                "SHIFT",
                "/admin/ban-giao-ca"
        );
        return mapToResponse(daLuu);
    }

    @Override
    @Transactional(readOnly = true)
    public void checkActiveShiftOrThrow(UUID nhanVienId) {
        layCaDangMoCoQuyenTruyCap(nhanVienId);
    }

    @Override
    @Transactional
    public void baoCaoSuCo(UUID nhanVienId, UUID giaoCaId, BaoCaoSuCoGiaoCaRequest request) {
        NhanVien nguoiBaoCao = layNhanVienHoatDong(nhanVienId);
        GiaoCa giaoCa = giaoCaId == null ? null : giaoCaRepository.findById(giaoCaId)
                .orElseThrow(() -> new BusinessException("Ca làm việc không tồn tại."));
        if (giaoCa != null) {
            boolean lienQuan = giaoCa.getNhanVienTrongCa().getId().equals(nhanVienId)
                    || (giaoCa.getNhanVienNhan() != null && giaoCa.getNhanVienNhan().getId().equals(nhanVienId))
                    || laAdmin(nguoiBaoCao);
            if (!lienQuan) {
                throw new BusinessException("Bạn không có quyền báo cáo sự cố cho ca này.");
            }
        }
        String tien = request.tienKiemDem() == null ? "Không cung cấp" : request.tienKiemDem().toPlainString();
        String maCa = giaoCa == null ? "CHUA_MO_CA" : giaoCa.getMa();
        String lienKet = giaoCa == null ? "/admin/mo-ca" : "/admin/ban-giao-ca?giaoCaId=" + giaoCa.getId();
        thongBaoService.taoThongBao(
                "Báo cáo sự cố giao ca " + maCa,
                "Người báo cáo: " + nguoiBaoCao.getHoTen() + ". Tiền kiểm đếm: " + tien
                        + ". Nội dung: " + request.noiDung(),
                "SHIFT_INCIDENT",
                lienKet + (lienKet.contains("?") ? "&" : "?") + "suCo=" + System.currentTimeMillis()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GiaoCaResponse> layLichSuGiaoCa(
            UUID nhanVienId, String trangThai, Instant tuNgay, Instant denNgay,
            String keyword, Pageable pageable) {
        String tuKhoa = keyword != null && !keyword.isBlank() ? keyword.trim() : null;
        boolean caSang = false;
        boolean caChieu = false;
        boolean caToi = false;
        if (tuKhoa != null) {
            String chuanHoa = tuKhoa.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
            caSang = chuanHoa.contains("sang") || chuanHoa.equals("ca1") || chuanHoa.equals("ca001");
            caChieu = chuanHoa.contains("chieu") || chuanHoa.equals("ca2") || chuanHoa.equals("ca002");
            caToi = chuanHoa.contains("toi") || chuanHoa.equals("ca3") || chuanHoa.equals("ca003");
        }
        return giaoCaRepository.searchHistory(
                nhanVienId, trangThai, tuNgay, denNgay, tuKhoa, caSang, caChieu, caToi, pageable)
                .map(this::mapToResponse);
    }

    private GiaoCaOptionsResponse taoTuyChonBanGiao(GiaoCa giaoCa) {
        Optional<CaLam> caKeTiepOptional = timCaKeTiep(giaoCa.getCaLam());
        if (caKeTiepOptional.isEmpty()) {
            return new GiaoCaOptionsResponse(true, null, null, List.of());
        }

        CaLam caKeTiep = caKeTiepOptional.get();
        LocalDate ngay = LocalDate.now(MUI_GIO);
        List<GiaoCaOptionsResponse.NhanVienNhanCaResponse> ungVien = nhanVienRepository.findAll().stream()
                .filter(this::dangHoatDong)
                .filter(nhanVien -> !nhanVien.getId().equals(giaoCa.getNhanVienTrongCa().getId()))
                .filter(nhanVien -> laAdmin(nhanVien)
                        || lichLamViecRepository.existsByNhanVienIdAndNgayAndCaLamId(
                                nhanVien.getId(), ngay, caKeTiep.getId()))
                .filter(nhanVien -> !giaoCaRepository.existsByNhanVienTrongCaIdAndTrangThaiIn(
                        nhanVien.getId(), TRANG_THAI_CHUA_KET_THUC))
                .map(nhanVien -> new GiaoCaOptionsResponse.NhanVienNhanCaResponse(
                        nhanVien.getId(), nhanVien.getMa(), nhanVien.getHoTen(), nhanVien.getVaiTro()))
                .toList();

        String lyDo = ungVien.isEmpty()
                ? "Ca tiếp theo chưa có nhân viên đủ điều kiện nhận. Vui lòng liên hệ quản trị viên để bổ sung lịch hoặc nhận ca."
                : null;
        return new GiaoCaOptionsResponse(false, caKeTiep.getId(), lyDo, ungVien);
    }

    private CaLam xacDinhCaDuocMo(
            NhanVien nhanVien,
            LocalDate ngay,
            LocalTime hienTai,
            String caLamId,
            String lyDoMoMuon) {
        if (!laAdmin(nhanVien)) {
            List<LichLamViec> lichTrongNgay = lichLamViecRepository.findByNhanVienIdAndNgay(nhanVien.getId(), ngay);
            if (lichTrongNgay.isEmpty()) {
                throw new BusinessException("Bạn không có lịch làm việc hôm nay.");
            }
            CaLam ca;
            if (caLamId != null && !caLamId.isBlank()) {
                ca = lichTrongNgay.stream()
                        .map(LichLamViec::getCaLam)
                        .filter(item -> item.getId().equalsIgnoreCase(caLamId.trim()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException("Ca được chọn không thuộc lịch làm việc của bạn hôm nay."));
            } else {
                List<CaLam> caPhuHop = lichTrongNgay.stream()
                        .map(LichLamViec::getCaLam)
                        .filter(item -> Boolean.TRUE.equals(item.getTrangThai()))
                        .filter(item -> {
                            LocalTime batDau = LocalTime.parse(item.getGioBatDau()).minusMinutes(PHUT_CHO_PHEP_MO_CA);
                            LocalTime ketThuc = LocalTime.parse(item.getGioKetThuc());
                            return !hienTai.isBefore(batDau) && hienTai.isBefore(ketThuc);
                        })
                        .toList();
                if (caPhuHop.size() != 1) {
                    throw new BusinessException("Bạn có nhiều ca trong ngày. Vui lòng chọn ca làm việc cần mở.");
                }
                ca = caPhuHop.get(0);
            }
            if (!Boolean.TRUE.equals(ca.getTrangThai())) {
                throw new BusinessException("Ca làm việc trong lịch không còn hoạt động.");
            }
            kiemTraKhungMoCaNhanVien(ca, hienTai, lyDoMoMuon);
            return ca;
        }

        if (caLamId == null || caLamId.isBlank()) {
            throw new BusinessException("Quản trị viên phải chọn ca làm việc cần mở.");
        }
        CaLam ca = caLamRepository.findById(caLamId.trim())
                .filter(item -> Boolean.TRUE.equals(item.getTrangThai()))
                .orElseThrow(() -> new BusinessException("Ca làm việc quản trị viên chọn không tồn tại hoặc đã ngừng hoạt động."));
        if (canLyDoMoMuonHoacNgoaiLe(ca, hienTai) && (lyDoMoMuon == null || lyDoMoMuon.isBlank())) {
            throw new BusinessException("Vui lòng nhập lý do mở ca muộn hoặc mở ca ngoài khung giờ.");
        }
        return ca;
    }

    private void kiemTraKhungMoCaNhanVien(CaLam caLam, LocalTime hienTai, String lyDoMoMuon) {
        LocalTime batDau = LocalTime.parse(caLam.getGioBatDau());
        LocalTime somNhat = batDau.minusMinutes(PHUT_CHO_PHEP_MO_CA);
        LocalTime ketThuc = LocalTime.parse(caLam.getGioKetThuc());
        if (hienTai.isBefore(somNhat)) {
            throw new BusinessException("Bạn chỉ được mở " + caLam.getTen()
                    + " từ " + somNhat + " (sớm tối đa 30 phút).");
        }
        if (!hienTai.isBefore(ketThuc)) {
            throw new BusinessException(caLam.getTen() + " đã kết thúc, nhân viên không thể mở ca này.");
        }
        if (hienTai.isAfter(batDau) && (lyDoMoMuon == null || lyDoMoMuon.isBlank())) {
            throw new BusinessException("Bạn đang mở ca muộn. Vui lòng nhập lý do mở ca muộn.");
        }
    }

    private boolean canLyDoMoMuonHoacNgoaiLe(CaLam caLam, LocalTime hienTai) {
        LocalTime batDau = LocalTime.parse(caLam.getGioBatDau());
        LocalTime somNhat = batDau.minusMinutes(PHUT_CHO_PHEP_MO_CA);
        LocalTime ketThuc = LocalTime.parse(caLam.getGioKetThuc());
        return hienTai.isBefore(somNhat) || hienTai.isAfter(batDau) || !hienTai.isBefore(ketThuc);
    }

    private String taoGhiChuMoCa(NhanVien nhanVien, CaLam caLam, LocalTime hienTai, MoCaRequest request) {
        boolean canLyDo = !laAdmin(nhanVien)
                ? hienTai.isAfter(LocalTime.parse(caLam.getGioBatDau()))
                : canLyDoMoMuonHoacNgoaiLe(caLam, hienTai);
        String ghiChuLyDo = canLyDo
                ? "Lý do mở ca muộn/ngoại lệ: " + request.lyDoMoMuon().trim()
                : null;
        return ghepGhiChu(request.ghiChu(), ghiChuLyDo);
    }

    private Optional<CaLam> timCaKeTiep(CaLam caHienTai) {
        List<CaLam> cacCa = cacCaHoatDong();
        if (cacCa.isEmpty()) {
            return Optional.empty();
        }

        LocalTime hienTai = LocalTime.now(MUI_GIO);

        // 1. Dựa theo thời gian thực tế: Tìm ca có giờ kết thúc sau thời điểm hiện tại và khác ca hiện tại
        Optional<CaLam> caTheoThoiGian = cacCa.stream()
                .filter(ca -> !ca.getId().equals(caHienTai.getId()))
                .filter(ca -> {
                    LocalTime gioKetThuc = LocalTime.parse(ca.getGioKetThuc());
                    return gioKetThuc.isAfter(hienTai);
                })
                .min(Comparator.comparing(ca -> LocalTime.parse(ca.getGioBatDau())));

        if (caTheoThoiGian.isPresent()) {
            return caTheoThoiGian;
        }

        // 2. Fallback: Nếu không tìm thấy theo giờ thực tế, lấy theo thứ tự ca liền kề
        for (int i = 0; i < cacCa.size() - 1; i++) {
            if (cacCa.get(i).getId().equals(caHienTai.getId())) {
                return Optional.of(cacCa.get(i + 1));
            }
        }
        return Optional.empty();
    }

    private List<CaLam> cacCaHoatDong() {
        return caLamRepository.findAll().stream()
                .filter(ca -> Boolean.TRUE.equals(ca.getTrangThai()))
                .sorted(Comparator.comparing(ca -> LocalTime.parse(ca.getGioBatDau())))
                .toList();
    }

    private void kiemTraNguoiNhanThuocCa(NhanVien nhanVien, CaLam caLam, LocalDate ngay) {
        if (laAdmin(nhanVien)) return;
        if (!lichLamViecRepository.existsByNhanVienIdAndNgayAndCaLamId(nhanVien.getId(), ngay, caLam.getId())) {
            throw new BusinessException("Bạn không còn thuộc lịch làm việc của ca tiếp theo.");
        }
    }

    private GiaoCa taoCaMoi(NhanVien nhanVien, CaLam caLam, BigDecimal tienDauCa, String ghiChu) {
        GiaoCa giaoCa = new GiaoCa();
        giaoCa.setMa("GC" + System.currentTimeMillis());
        giaoCa.setCaLam(caLam);
        giaoCa.setNhanVienTrongCa(nhanVien);
        giaoCa.setThoiGianVao(Instant.now());
        giaoCa.setTienDauCa(tienDauCa);
        giaoCa.setTrangThai(TrangThaiGiaoCa.MO_CA.ma());
        giaoCa.setCaChuaKetThuc(1);
        giaoCa.setGhiChu(gioiHanGhiChu(ghiChu));
        return giaoCa;
    }

    private void chotSoLieuCa(GiaoCa giaoCa, BigDecimal tienThucTe, String lyDo, String ghiChu) {
        BigDecimal tienHeThong = giaoCa.getTienDauCa().add(tinhTienMat(giaoCa.getId()));
        BigDecimal chenhLech = tienThucTe.subtract(tienHeThong);
        kiemTraLyDoChenhLech(chenhLech, lyDo);
        giaoCa.setThoiGianRa(Instant.now());
        giaoCa.setTienCuoiCaThucTe(tienThucTe);
        giaoCa.setLyDoChenhLech(lyDo);
        giaoCa.setGhiChu(gioiHanGhiChu(ghiChu));
    }

    private void khoiPhucCaDangMo(GiaoCa giaoCa, String ghiChuMoi) {
        giaoCa.setTrangThai(TrangThaiGiaoCa.MO_CA.ma());
        giaoCa.setCaChuaKetThuc(1);
        giaoCa.setNhanVienNhan(null);
        giaoCa.setThoiGianRa(null);
        giaoCa.setThoiGianXacNhan(null);
        giaoCa.setTienCuoiCaThucTe(null);
        giaoCa.setTienNhanKiemDem(null);
        giaoCa.setLyDoChenhLech(null);
        giaoCa.setGhiChu(ghepGhiChu(giaoCa.getGhiChu(), ghiChuMoi));
    }

    private void kiemTraKhongCoCaChuaKetThuc() {
        if (giaoCaRepository.existsByCaChuaKetThuc(1)) {
            throw new BusinessException("Cửa hàng đang có ca làm việc chưa kết thúc. Vui lòng hoàn tất ca hiện tại trước.");
        }
    }

    private void kiemTraNhanVienKhongCoCaChuaKetThuc(UUID nhanVienId) {
        if (giaoCaRepository.existsByNhanVienTrongCaIdAndTrangThaiIn(nhanVienId, TRANG_THAI_CHUA_KET_THUC)) {
            throw new BusinessException("Bạn đang có ca làm việc chưa kết thúc.");
        }
    }

    private GiaoCa layCaDangMoCuaNhanVien(UUID nhanVienId) {
        return giaoCaRepository
                .findByNhanVienTrongCaIdAndTrangThai(nhanVienId, TrangThaiGiaoCa.MO_CA.ma())
                .orElseThrow(() -> new BusinessException("Không tìm thấy ca làm việc đang hoạt động."));
    }

    private GiaoCa layCaDangMoCoQuyenTruyCap(UUID nhanVienId) {
        NhanVien nhanVien = layNhanVienHoatDong(nhanVienId);
        if (laAdmin(nhanVien)) {
            Optional<GiaoCa> caCuaAdmin = giaoCaRepository
                    .findByNhanVienTrongCaIdAndTrangThai(nhanVienId, TrangThaiGiaoCa.MO_CA.ma());
            if (caCuaAdmin.isPresent()) {
                return caCuaAdmin.get();
            }
            return giaoCaRepository
                    .findFirstByTrangThaiInOrderByThoiGianVaoDesc(List.of(TrangThaiGiaoCa.MO_CA.ma()))
                    .orElseThrow(() -> new BusinessException(
                            "Cửa hàng chưa có ca làm việc đang hoạt động. Vui lòng mở ca trước khi thực hiện giao dịch."));
        }
        return layCaDangMoCuaNhanVien(nhanVienId);
    }

    private GiaoCa layCaChoCapNhat(UUID giaoCaId) {
        return giaoCaRepository.findByIdForUpdate(giaoCaId)
                .orElseThrow(() -> new BusinessException("Biên bản giao ca không tồn tại."));
    }

    private void kiemTraDangChoBanGiao(GiaoCa giaoCa) {
        if (!TrangThaiGiaoCa.CHO_BAN_GIAO.ma().equals(giaoCa.getTrangThai())) {
            throw new BusinessException("Ca làm việc không còn ở trạng thái chờ bàn giao.");
        }
    }

    private NhanVien layNhanVienHoatDong(UUID nhanVienId) {
        NhanVien nhanVien = nhanVienRepository.findById(nhanVienId)
                .orElseThrow(() -> new BusinessException("Nhân viên không tồn tại."));
        if (!dangHoatDong(nhanVien)) {
            throw new BusinessException("Tài khoản nhân viên đã ngừng hoạt động.");
        }
        return nhanVien;
    }

    private boolean dangHoatDong(NhanVien nhanVien) {
        return nhanVien.getTrangThai() != null && nhanVien.getTrangThai() == 1;
    }

    private boolean laAdmin(NhanVien nhanVien) {
        return nhanVien.getVaiTro() != null && nhanVien.getVaiTro() == 1;
    }

    private BigDecimal tinhTienMat(UUID giaoCaId) {
        BigDecimal giaTri = giaoCaRepository.calculateTienMatTrongCa(giaoCaId);
        return giaTri == null ? BigDecimal.ZERO : giaTri;
    }

    private BigDecimal tinhTienChuyenKhoan(UUID giaoCaId) {
        BigDecimal giaTri = giaoCaRepository.calculateTienChuyenKhoanTrongCa(giaoCaId);
        return giaTri == null ? BigDecimal.ZERO : giaTri;
    }

    private void kiemTraLyDoChenhLech(BigDecimal chenhLech, String lyDo) {
        if (chenhLech.compareTo(BigDecimal.ZERO) != 0 && (lyDo == null || lyDo.isBlank())) {
            throw new BusinessException("Số tiền chênh lệch khác 0. Vui lòng nhập lý do chênh lệch.");
        }
    }

    private String noiDungLyDo(String lyDo) {
        return lyDo == null || lyDo.isBlank() ? "" : ". Lý do: " + lyDo.trim();
    }

    private String ghepGhiChu(String hienTai, String boSung) {
        if (boSung == null || boSung.isBlank()) return gioiHanGhiChu(hienTai);
        if (hienTai == null || hienTai.isBlank()) return gioiHanGhiChu(boSung.trim());
        return gioiHanGhiChu(hienTai.trim() + " | " + boSung.trim());
    }

    private String gioiHanGhiChu(String ghiChu) {
        if (ghiChu == null) return null;
        String ketQua = ghiChu.trim();
        return ketQua.length() <= 500 ? ketQua : ketQua.substring(0, 500);
    }

    private void guiThongBaoAnToan(String tieuDe, String noiDung, String loai, String link) {
        try {
            thongBaoService.taoThongBao(tieuDe, noiDung, loai, link);
        } catch (Exception exception) {
            System.err.println("[GiaoCa] Không thể gửi thông báo: " + exception.getMessage());
        }
    }

    @Override
    @Transactional
    public void tuDongKetCaChuaDong() {
        List<GiaoCa> caChuaDong = giaoCaRepository.findByTrangThaiIn(
                List.of(TrangThaiGiaoCa.MO_CA.ma(), TrangThaiGiaoCa.CHO_BAN_GIAO.ma(), "0"));

        if (caChuaDong.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        for (GiaoCa giaoCa : caChuaDong) {
            try {
                BigDecimal tienMat = tinhTienMat(giaoCa.getId());
                BigDecimal tienHeThong = (giaoCa.getTienDauCa() != null ? giaoCa.getTienDauCa() : BigDecimal.ZERO).add(tienMat);

                giaoCa.setThoiGianRa(now);
                giaoCa.setTienCuoiCaThucTe(tienHeThong);
                giaoCa.setTrangThai(TrangThaiGiaoCa.DA_KET_THUC.ma());
                giaoCa.setCaChuaKetThuc(null);
                giaoCa.setGhiChu(ghepGhiChu(giaoCa.getGhiChu(), "Hệ thống tự động kết ca lúc 00:00 do nhân viên chưa đóng ca."));
                giaoCaRepository.save(giaoCa);
            } catch (Exception ex) {
                System.err.println("[TuDongKetCa] Lỗi khi tự động kết ca " + giaoCa.getMa() + ": " + ex.getMessage());
            }
        }
    }

    private GiaoCaResponse mapToResponse(GiaoCa giaoCa) {
        if (giaoCa == null) return null;
        BigDecimal tienMat = tinhTienMat(giaoCa.getId());
        BigDecimal tienChuyenKhoan = tinhTienChuyenKhoan(giaoCa.getId());
        BigDecimal tienHeThong = (giaoCa.getTienDauCa() != null ? giaoCa.getTienDauCa() : BigDecimal.ZERO).add(tienMat);
        BigDecimal chenhLech = giaoCa.getTienCuoiCaThucTe() == null
                ? null : giaoCa.getTienCuoiCaThucTe().subtract(tienHeThong);

        return new GiaoCaResponse(
                giaoCa.getId(),
                giaoCa.getMa(),
                giaoCa.getCaLam() != null ? giaoCa.getCaLam().getId() : null,
                giaoCa.getCaLam() != null ? giaoCa.getCaLam().getTen() : null,
                giaoCa.getCaLam() != null ? giaoCa.getCaLam().getGioBatDau() : null,
                giaoCa.getCaLam() != null ? giaoCa.getCaLam().getGioKetThuc() : null,
                giaoCa.getNhanVienTrongCa().getId(),
                giaoCa.getNhanVienTrongCa().getHoTen(),
                giaoCa.getNhanVienTrongCa().getMa(),
                giaoCa.getNhanVienTrongCa().getHinhAnh(),
                giaoCa.getNhanVienTrongCa().getVaiTro(),
                giaoCa.getNhanVienNhan() != null ? giaoCa.getNhanVienNhan().getId() : null,
                giaoCa.getNhanVienNhan() != null ? giaoCa.getNhanVienNhan().getHoTen() : null,
                giaoCa.getNhanVienNhan() != null ? giaoCa.getNhanVienNhan().getMa() : null,
                giaoCa.getThoiGianVao(),
                giaoCa.getThoiGianRa(),
                giaoCa.getThoiGianXacNhan(),
                giaoCa.getTienDauCa(),
                tienMat,
                tienChuyenKhoan,
                giaoCa.getTienCuoiCaThucTe(),
                giaoCa.getTienNhanKiemDem(),
                tienHeThong,
                chenhLech,
                giaoCa.getLyDoChenhLech(),
                giaoCa.getTrangThai(),
                giaoCa.getGhiChu()
        );
    }
}
