package com.example.server.core.admin.quanlydanhgia.service;

import com.example.server.core.admin.quanlydanhgia.dto.AdminDanhGiaResponse;
import com.example.server.core.admin.quanlydanhgia.dto.SanPhamCoDanhGiaResponse;
import com.example.server.entity.DanhGia;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DanhGiaRepository;
import com.example.server.repository.GiayRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDanhGiaService {

    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private final DanhGiaRepository danhGiaRepository;
    private final GiayRepository giayRepository;
    private final DanhGiaAiService danhGiaAiService;

    public AdminDanhGiaService(
            DanhGiaRepository danhGiaRepository,
            GiayRepository giayRepository,
            DanhGiaAiService danhGiaAiService
    ) {
        this.danhGiaRepository = danhGiaRepository;
        this.giayRepository = giayRepository;
        this.danhGiaAiService = danhGiaAiService;
    }

    /** Bảng sản phẩm có đánh giá + thống kê (số đánh giá, điểm TB, chưa xem), tìm theo tên/mã. */
    @Transactional(readOnly = true)
    public List<SanPhamCoDanhGiaResponse> laySanPhamCoDanhGia(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        return danhGiaRepository.thongKeSanPhamCoDanhGia(kw.isEmpty() ? null : kw).stream()
                .map(r -> new SanPhamCoDanhGiaResponse(
                        (Integer) r[0],
                        (String) r[1],
                        (String) r[2],
                        (String) r[3],
                        ((Number) r[4]).longValue(),
                        r[5] == null ? 0 : Math.round(((Number) r[5]).doubleValue() * 10.0) / 10.0,
                        (Instant) r[6],
                        r[7] == null ? 0 : ((Number) r[7]).longValue()))
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
        return danhGiaRepository.locChoAdmin(
                        giayId == null ? -1 : giayId,
                        trangThai == null ? -1 : trangThai,
                        tu, den).stream()
                .map(this::toResponse)
                .toList();
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

    /** AI tổng hợp đánh giá của 1 sản phẩm (giayId != null) hoặc toàn shop (giayId = null). */
    @Transactional(readOnly = true)
    public String tongHopAi(Integer giayId) {
        String tenSanPham = giayId == null ? null
                : giayRepository.findById(giayId).map(g -> g.getTen()).orElse(null);
        return danhGiaAiService.tongHop(giayId, tenSanPham);
    }

    private AdminDanhGiaResponse toResponse(DanhGia dg) {
        return new AdminDanhGiaResponse(
                dg.getId(),
                dg.getKhachHang().getHoTen(),
                dg.getSoSao(),
                dg.getNoiDung(),
                dg.getMedia(),
                dg.getNgayTao(),
                dg.getPhanHoi(),
                dg.getNgayPhanHoi(),
                dg.getTrangThai(),
                dg.getLyDoAn(),
                dg.getGiay().getId(),
                dg.getGiay().getTen(),
                dg.getGiay().getHinhAnh());
    }
}
