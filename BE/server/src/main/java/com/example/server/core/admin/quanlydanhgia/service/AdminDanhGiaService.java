package com.example.server.core.admin.quanlydanhgia.service;

import com.example.server.core.admin.quanlydanhgia.dto.AdminDanhGiaResponse;
import com.example.server.core.admin.quanlydanhgia.dto.SanPhamCoDanhGiaResponse;
import com.example.server.entity.DanhGia;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DanhGiaRepository;
import com.example.server.repository.GiayRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDanhGiaService {

    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private final DanhGiaRepository danhGiaRepository;
    private final GiayRepository giayRepository;
    private final HinhAnhGiayRepository hinhAnhGiayRepository;
    private final DanhGiaAiService danhGiaAiService;
    private final com.example.server.core.client.thongbao.service.ClientThongBaoService clientThongBaoService;

    public AdminDanhGiaService(
            DanhGiaRepository danhGiaRepository,
            GiayRepository giayRepository,
            HinhAnhGiayRepository hinhAnhGiayRepository,
            DanhGiaAiService danhGiaAiService,
            com.example.server.core.client.thongbao.service.ClientThongBaoService clientThongBaoService
    ) {
        this.danhGiaRepository = danhGiaRepository;
        this.giayRepository = giayRepository;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
        this.danhGiaAiService = danhGiaAiService;
        this.clientThongBaoService = clientThongBaoService;
    }

    /** Bảng sản phẩm có đánh giá + thống kê (số đánh giá, điểm TB, chưa xem), tìm theo tên/mã. */
    @Transactional(readOnly = true)
    public List<SanPhamCoDanhGiaResponse> laySanPhamCoDanhGia(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        List<Object[]> rows = danhGiaRepository.thongKeSanPhamCoDanhGia(kw.isEmpty() ? null : kw);
        // Ảnh chính lấy từ biến thể của sản phẩm (giay.hinhAnh thường null), fallback về giay.hinhAnh.
        Map<Integer, String> anhChinh = mapAnhChinhTheoGiay(
                rows.stream().map(r -> (Integer) r[0]).toList());
        return rows.stream()
                .map(r -> {
                    Integer giayId = (Integer) r[0];
                    return new SanPhamCoDanhGiaResponse(
                            giayId,
                            (String) r[1],
                            (String) r[2],
                            anhChinh.getOrDefault(giayId, (String) r[3]),
                            ((Number) r[4]).longValue(),
                            r[5] == null ? 0 : Math.round(((Number) r[5]).doubleValue() * 10.0) / 10.0,
                            (Instant) r[6],
                            r[7] == null ? 0 : ((Number) r[7]).longValue());
                })
                .toList();
    }

    /**
     * Đánh giá của 1 sản phẩm cho admin (lọc trạng thái + khoảng ngày), mới nhất trước.
     * Mở xem -> đánh dấu các đánh giá của sản phẩm là "đã xem" (cho chuông thông báo).
     *
     * @param trangThai null = tất cả; 1 = đang hiển thị; 0 = đã ẩn/xóa.
     * @param tuNgay/denNgay dạng yyyy-MM-dd (null = không giới hạn).
     */
    @Transactional
    public List<AdminDanhGiaResponse> layTheoSanPham(Integer giayId, Integer trangThai, String tuNgay, String denNgay) {
        danhGiaRepository.danhDauDaXemTheoSanPham(giayId);
        return locDanhGia(giayId, trangThai, tuNgay, denNgay);
    }

    /** Toàn bộ đánh giá của shop cho admin (lọc trạng thái + khoảng ngày). Mở xem = đã xem hết. */
    @Transactional
    public List<AdminDanhGiaResponse> layTatCa(Integer trangThai, String tuNgay, String denNgay) {
        danhGiaRepository.danhDauDaXemTatCa();
        return locDanhGia(null, trangThai, tuNgay, denNgay);
    }

    private List<AdminDanhGiaResponse> locDanhGia(Integer giayId, Integer trangThai, String tuNgay, String denNgay) {
        Instant tu = parseNgay(tuNgay, false);
        Instant den = parseNgay(denNgay, true);
        List<DanhGia> ds = danhGiaRepository.locChoAdmin(
                giayId == null ? -1 : giayId,
                trangThai == null ? -1 : trangThai,
                tu, den);
        // Ảnh ưu tiên biến thể khách đã mua (đúng màu/loại), fallback ảnh chính SP -> giay.hinhAnh.
        Map<Integer, String> anhBienThe = mapAnhChinhTheoBienThe(
                ds.stream().map(this::bienTheIdCuaDanhGia).filter(Objects::nonNull).distinct().toList());
        Map<Integer, String> anhGiay = mapAnhChinhTheoGiay(
                ds.stream().map(dg -> dg.getGiay().getId()).distinct().toList());
        return ds.stream()
                .map(dg -> toResponse(dg, layAnhDanhGia(dg, anhBienThe, anhGiay)))
                .toList();
    }

    /** Id biến thể (giay_chi_tiet) mà đánh giá này gắn vào, null nếu đánh giá không có dòng hóa đơn. */
    private Integer bienTheIdCuaDanhGia(DanhGia dg) {
        return dg.getHoaDonChiTiet() != null && dg.getHoaDonChiTiet().getGiayChiTiet() != null
                ? dg.getHoaDonChiTiet().getGiayChiTiet().getId()
                : null;
    }

    /** Chọn ảnh cho 1 đánh giá: ảnh biến thể đã mua -> ảnh chính sản phẩm -> giay.hinhAnh. */
    private String layAnhDanhGia(DanhGia dg, Map<Integer, String> anhBienThe, Map<Integer, String> anhGiay) {
        Integer btId = bienTheIdCuaDanhGia(dg);
        if (btId != null && anhBienThe.get(btId) != null) {
            return anhBienThe.get(btId);
        }
        String anhSp = anhGiay.get(dg.getGiay().getId());
        return anhSp != null ? anhSp : dg.getGiay().getHinhAnh();
    }

    /** Map giayId -> URL ảnh chính (ưu tiên laHinhChinh, lấy dòng đầu). */
    private Map<Integer, String> mapAnhChinhTheoGiay(List<Integer> giayIds) {
        Map<Integer, String> map = new HashMap<>();
        if (!giayIds.isEmpty()) {
            for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayIds(giayIds)) {
                map.putIfAbsent((Integer) row[0], (String) row[1]);
            }
        }
        return map;
    }

    /** Map biến thể (giayChiTietId) -> URL ảnh chính của biến thể đó. */
    private Map<Integer, String> mapAnhChinhTheoBienThe(List<Integer> bienTheIds) {
        Map<Integer, String> map = new HashMap<>();
        if (!bienTheIds.isEmpty()) {
            for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayChiTietIds(bienTheIds)) {
                map.putIfAbsent((Integer) row[0], (String) row[1]);
            }
        }
        return map;
    }

    /** Ảnh sản phẩm cho 1 đánh giá lẻ (dùng khi phản hồi/khôi phục, không batch). */
    private String layAnhDanhGiaDon(DanhGia dg) {
        Integer btId = bienTheIdCuaDanhGia(dg);
        if (btId != null) {
            String url = firstUrl(hinhAnhGiayRepository.findMainImageUrlsByGiayChiTietIds(List.of(btId)));
            if (url != null) {
                return url;
            }
        }
        String url = firstUrl(hinhAnhGiayRepository.findMainImageUrlsByGiayIds(List.of(dg.getGiay().getId())));
        return url != null ? url : dg.getGiay().getHinhAnh();
    }

    private static String firstUrl(List<Object[]> rows) {
        return rows.isEmpty() ? null : (String) rows.get(0)[1];
    }

    /** Ngày yyyy-MM-dd theo giờ VN; cuoiNgay=true -> mốc ĐẦU ngày hôm sau (chặn trên exclusive). */
    private Instant parseNgay(String ngay, boolean cuoiNgay) {
        if (ngay == null || ngay.isBlank()) {
            return cuoiNgay ? Instant.now().plusSeconds(86_400L) : Instant.EPOCH;
        }
        try {
            LocalDate d = LocalDate.parse(ngay.trim());
            return (cuoiNgay ? d.plusDays(1) : d).atStartOfDay(ZONE_VN).toInstant();
        } catch (Exception e) {
            throw new BusinessException("Ngày lọc không hợp lệ (định dạng yyyy-MM-dd)");
        }
    }

    /** Tổng số đánh giá chưa xem (cho chuông thông báo). */
    @Transactional(readOnly = true)
    public long demChuaXem() {
        return danhGiaRepository.demChuaXem();
    }

    /** Xóa mềm: ẩn đánh giá khỏi mọi nơi (trang SP, trang công khai). */
    @Transactional
    public void xoaMem(Integer danhGiaId) {
        DanhGia dg = danhGiaRepository.findById(danhGiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));
        dg.setTrangThai(0);
        dg.setLyDoAn("Quản trị viên xóa");
        dg.setNgayCapNhat(Instant.now());
        danhGiaRepository.save(dg);
        // Báo vào chuông thông báo của khách.
        clientThongBaoService.guiChoKhach(
                dg.getKhachHang().getId(),
                "DANH_GIA",
                "Đánh giá bị ẩn",
                "Đánh giá của bạn đã bị ẩn vì chứa nội dung không phù hợp",
                null);
    }

    /** Khôi phục đánh giá đã ẩn (kể cả do AI ẩn nhầm) -> hiển thị lại. */
    @Transactional
    public AdminDanhGiaResponse khoiPhuc(Integer danhGiaId) {
        DanhGia dg = danhGiaRepository.findById(danhGiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));
        dg.setTrangThai(1);
        dg.setLyDoAn(null);
        dg.setNgayCapNhat(Instant.now());
        return toResponse(danhGiaRepository.save(dg));
    }

    /** Phản hồi đánh giá - mỗi đánh giá chỉ phản hồi 1 lần. */
    @Transactional
    public AdminDanhGiaResponse phanHoi(Integer danhGiaId, String noiDung) {
        DanhGia dg = danhGiaRepository.findById(danhGiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));
        if (dg.getTrangThai() != null && dg.getTrangThai() == 0) {
            throw new BusinessException("Đánh giá đã bị ẩn, không thể phản hồi");
        }
        if (dg.getPhanHoi() != null && !dg.getPhanHoi().isBlank()) {
            throw new BusinessException("Đánh giá này đã được phản hồi");
        }
        if (noiDung == null || noiDung.isBlank()) {
            throw new BusinessException("Nội dung phản hồi không được để trống");
        }
        dg.setPhanHoi(noiDung.trim());
        dg.setNgayPhanHoi(Instant.now());
        dg.setNgayCapNhat(Instant.now());
        return toResponse(danhGiaRepository.save(dg));
    }

    /**
     * AI phân tích đánh giá của 1 sản phẩm (giayId != null) hoặc toàn shop (giayId = null).
     *
     * @param loai     "tot" (4-5 sao) | "khong-tot" (1-3 sao) | "tong-the" (tất cả).
     * @param thoiGian "hom-nay" | "tuan-nay" | "thang-nay" | "nam-nay" (mặc định hôm nay).
     */
    @Transactional(readOnly = true)
    public String phanTichAi(Integer giayId, String loai, String thoiGian) {
        Instant tu = tinhMocThoiGian(thoiGian);
        Instant den = Instant.now().plusSeconds(60);
        // Chỉ phân tích đánh giá đang hiển thị (trangThai=1) trong khoảng thời gian đã chọn.
        List<DanhGia> ds = danhGiaRepository.locChoAdmin(giayId == null ? -1 : giayId, 1, tu, den);

        String loaiChuan = loai == null || loai.isBlank() ? "tong-the" : loai;
        if ("tot".equals(loaiChuan)) {
            ds = ds.stream().filter(dg -> dg.getSoSao() != null && dg.getSoSao() >= 4).toList();
        } else if ("khong-tot".equals(loaiChuan)) {
            ds = ds.stream().filter(dg -> dg.getSoSao() != null && dg.getSoSao() <= 3).toList();
        }

        String tenSanPham = giayId == null ? null
                : giayRepository.findById(giayId).map(g -> g.getTen()).orElse("#" + giayId);
        String boiCanh = (giayId == null ? "toàn bộ cửa hàng" : "sản phẩm \"" + tenSanPham + "\"")
                + ", " + nhanThoiGian(thoiGian);
        return danhGiaAiService.phanTich(ds, loaiChuan, boiCanh);
    }

    /** Mốc bắt đầu của khoảng thời gian phân tích (theo giờ VN). */
    private Instant tinhMocThoiGian(String thoiGian) {
        LocalDate homNay = LocalDate.now(ZONE_VN);
        LocalDate moc = switch (thoiGian == null ? "hom-nay" : thoiGian) {
            case "tuan-nay" -> homNay.with(java.time.DayOfWeek.MONDAY);
            case "thang-nay" -> homNay.withDayOfMonth(1);
            case "nam-nay" -> homNay.withDayOfYear(1);
            default -> homNay; // hom-nay
        };
        return moc.atStartOfDay(ZONE_VN).toInstant();
    }

    private String nhanThoiGian(String thoiGian) {
        return switch (thoiGian == null ? "hom-nay" : thoiGian) {
            case "tuan-nay" -> "trong tuần này";
            case "thang-nay" -> "trong tháng này";
            case "nam-nay" -> "trong năm nay";
            default -> "trong hôm nay";
        };
    }

    private AdminDanhGiaResponse toResponse(DanhGia dg) {
        return toResponse(dg, layAnhDanhGiaDon(dg));
    }

    private AdminDanhGiaResponse toResponse(DanhGia dg, String hinhAnhSanPham) {
        return new AdminDanhGiaResponse(
                dg.getId(),
                dg.getKhachHang() != null ? dg.getKhachHang().getHoTen() : null,
                dg.getKhachHang() != null ? dg.getKhachHang().getHinhAnh() : null,
                dg.getSoSao(),
                dg.getNoiDung(),
                dg.getMedia(),
                dg.getNgayTao(),
                dg.getPhanHoi(),
                dg.getNgayPhanHoi(),
                dg.getTrangThai(),
                dg.getLyDoAn(),
                dg.getGiay() != null ? dg.getGiay().getId() : null,
                dg.getGiay() != null ? dg.getGiay().getTen() : null,
                hinhAnhSanPham);
    }
}
