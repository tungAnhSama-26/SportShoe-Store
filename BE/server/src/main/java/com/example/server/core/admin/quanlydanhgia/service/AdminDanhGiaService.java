package com.example.server.core.admin.quanlydanhgia.service;

import com.example.server.core.admin.quanlydanhgia.dto.AdminDanhGiaResponse;
import com.example.server.core.admin.quanlydanhgia.dto.SanPhamCoDanhGiaResponse;
import com.example.server.core.client.danhgia.dto.DanhGiaCongKhaiResponse;
import com.example.server.entity.DanhGia;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DanhGiaRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDanhGiaService {

    private final DanhGiaRepository danhGiaRepository;

    public AdminDanhGiaService(DanhGiaRepository danhGiaRepository) {
        this.danhGiaRepository = danhGiaRepository;
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
     * Toàn bộ đánh giá đang hiển thị của một sản phẩm, mới nhất trước.
     * Khi admin mở xem -> đánh dấu các đánh giá của sản phẩm này là "đã xem" (cho chuông thông báo).
     */
    @Transactional
    public List<AdminDanhGiaResponse> layTheoSanPham(Integer giayId) {
        danhGiaRepository.danhDauDaXemTheoSanPham(giayId);
        return danhGiaRepository.findByGiayId(giayId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Tổng số đánh giá chưa xem (cho chuông thông báo). */
    @Transactional(readOnly = true)
    public long demChuaXem() {
        return danhGiaRepository.demChuaXem();
    }

    /**
     * Toàn bộ đánh giá của shop kèm thông tin sản phẩm (màn "Tất cả đánh giá"), mới nhất trước.
     * Mở màn này coi như đã xem hết -> đánh dấu đã xem toàn bộ (cho chuông thông báo).
     */
    @Transactional
    public List<DanhGiaCongKhaiResponse> layTatCa() {
        danhGiaRepository.danhDauDaXemTatCa();
        return danhGiaRepository.findTatCaCongKhai().stream()
                .map(dg -> new DanhGiaCongKhaiResponse(
                        dg.getId(),
                        dg.getKhachHang().getHoTen(),
                        dg.getSoSao(),
                        dg.getNoiDung(),
                        dg.getMedia(),
                        dg.getNgayTao(),
                        dg.getPhanHoi(),
                        dg.getNgayPhanHoi(),
                        dg.getGiay().getId(),
                        dg.getGiay().getTen(),
                        dg.getGiay().getHinhAnh()))
                .toList();
    }

    /** Xóa mềm: ẩn đánh giá khỏi mọi nơi (trang SP, trang công khai, bảng admin). */
    @Transactional
    public void xoaMem(Integer danhGiaId) {
        DanhGia dg = danhGiaRepository.findById(danhGiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));
        dg.setTrangThai(0);
        dg.setNgayCapNhat(Instant.now());
        danhGiaRepository.save(dg);
    }

    /** Phản hồi đánh giá - mỗi đánh giá chỉ phản hồi 1 lần. */
    @Transactional
    public AdminDanhGiaResponse phanHoi(Integer danhGiaId, String noiDung) {
        DanhGia dg = danhGiaRepository.findById(danhGiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));
        if (dg.getTrangThai() != null && dg.getTrangThai() == 0) {
            throw new BusinessException("Đánh giá đã bị xóa, không thể phản hồi");
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

    private AdminDanhGiaResponse toResponse(DanhGia dg) {
        return new AdminDanhGiaResponse(
                dg.getId(),
                dg.getKhachHang().getHoTen(),
                dg.getSoSao(),
                dg.getNoiDung(),
                dg.getMedia(),
                dg.getNgayTao(),
                dg.getPhanHoi(),
                dg.getNgayPhanHoi());
    }
}
