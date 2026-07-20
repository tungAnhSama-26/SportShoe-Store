package com.example.server.core.client.danhgia.service;

import com.example.server.core.client.danhgia.dto.DanhGiaCongKhaiPage;
import com.example.server.core.client.danhgia.dto.DanhGiaCongKhaiResponse;
import com.example.server.core.client.danhgia.dto.DanhGiaResponse;
import com.example.server.core.client.danhgia.dto.DanhGiaTongHopResponse;
import com.example.server.entity.DanhGia;
import com.example.server.entity.Giay;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DanhGiaRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientDanhGiaService {

    private static final int TRANG_THAI_HOAN_THANH = 5;

    private final DanhGiaRepository danhGiaRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final com.example.server.core.admin.quanlydanhgia.service.DanhGiaAiService danhGiaAiService;
    private final com.example.server.repository.HinhAnhGiayRepository hinhAnhGiayRepository;

    public ClientDanhGiaService(
            DanhGiaRepository danhGiaRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            com.example.server.core.admin.quanlydanhgia.service.DanhGiaAiService danhGiaAiService,
            com.example.server.repository.HinhAnhGiayRepository hinhAnhGiayRepository
    ) {
        this.danhGiaRepository = danhGiaRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.danhGiaAiService = danhGiaAiService;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
    }

    @Transactional(readOnly = true)
    public DanhGiaTongHopResponse layTheoSanPham(Integer giayId) {
        List<DanhGia> ds = danhGiaRepository.findByGiayId(giayId);
        List<DanhGiaResponse> danhSach = ds.stream()
                .map(dg -> new DanhGiaResponse(
                        dg.getId(),
                        dg.getKhachHang().getHoTen(),
                        dg.getSoSao(),
                        dg.getNoiDung(),
                        dg.getMedia(),
                        dg.getPhanHoi(),
                        dg.getNgayPhanHoi(),
                        dg.getNgayTao()))
                .toList();
        double trungBinh = ds.isEmpty()
                ? 0
                : ds.stream().mapToInt(DanhGia::getSoSao).average().orElse(0);
        // Làm tròn 1 chữ số thập phân.
        double diemTrungBinh = Math.round(trungBinh * 10.0) / 10.0;
        return new DanhGiaTongHopResponse(diemTrungBinh, ds.size(), danhSach);
    }

    /** Danh sách đánh giá công khai toàn shop (lọc số sao nếu có), mới nhất trước, phân trang. */
    @Transactional(readOnly = true)
    public DanhGiaCongKhaiPage layCongKhai(Integer soSao, int trang, int kichThuoc) {
        Pageable pageable = PageRequest.of(Math.max(trang, 0), Math.min(Math.max(kichThuoc, 1), 50));
        Page<DanhGia> page = danhGiaRepository.findCongKhai(soSao, pageable);
        List<Integer> giayIds = page.getContent().stream().map(dg -> dg.getGiay().getId()).distinct().toList();
        java.util.Map<Integer, String> anhChinh = new java.util.LinkedHashMap<>();
        if (!giayIds.isEmpty()) {
            for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayIds(giayIds)) {
                anhChinh.putIfAbsent((Integer) row[0], (String) row[1]);
            }
        }

        List<DanhGiaCongKhaiResponse> danhSach = page.getContent().stream()
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
                        anhChinh.getOrDefault(dg.getGiay().getId(), dg.getGiay().getHinhAnh()),
                        dg.getKhachHang().getHinhAnh()))
                .toList();
        return new DanhGiaCongKhaiPage(danhSach, page.getNumber(), page.getTotalPages(), page.getTotalElements());
    }

    /**
     * Đánh giá theo dòng hóa đơn chi tiết: chỉ người đã mua + đã nhận hàng mới được đánh giá,
     * và mỗi dòng hóa đơn chi tiết chỉ đánh giá 1 lần.
     */
    @Transactional
    public DanhGiaResponse taoTheoHoaDonChiTiet(UUID khachHangId, Integer hoaDonChiTietId, Integer soSao, String noiDung, String media) {
        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(hoaDonChiTietId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm trong đơn không tồn tại"));
        HoaDon hd = ct.getHoaDon();

        if (hd.getKhachHang() == null || !hd.getKhachHang().getId().equals(khachHangId)) {
            throw new BusinessException("Bạn chỉ có thể đánh giá sản phẩm mình đã mua");
        }
        // Chỉ cần đơn đã HOÀN THÀNH là được đánh giá (dù do khách bấm nhận hàng, admin chuyển,
        // hay tự động hoàn thành sau 3 ngày) - không bắt buộc cờ daNhanHang nữa.
        if (hd.getTrangThai() == null || hd.getTrangThai() != TRANG_THAI_HOAN_THANH) {
            throw new BusinessException("Chỉ có thể đánh giá đơn hàng đã hoàn thành");
        }
        if (danhGiaRepository.existsByHoaDonChiTietId(hoaDonChiTietId)) {
            throw new BusinessException("Sản phẩm này trong đơn đã được đánh giá");
        }
        if (soSao == null || soSao < 1 || soSao > 5) {
            throw new BusinessException("Vui lòng chọn số sao từ 1 đến 5");
        }

        Giay giay = ct.getGiayChiTiet().getGiay();
        KhachHang khachHang = hd.getKhachHang();

        DanhGia danhGia = new DanhGia();
        danhGia.setGiay(giay);
        danhGia.setKhachHang(khachHang);
        danhGia.setHoaDonChiTiet(ct);
        danhGia.setSoSao(soSao);
        danhGia.setNoiDung(noiDung != null && !noiDung.isBlank() ? noiDung.trim() : null);
        danhGia.setMedia(media != null && !media.isBlank() ? media.trim() : null);
        danhGia.setTrangThai(1);
        danhGia.setDaXem(false);
        danhGia.setNgayTao(Instant.now());

        DanhGia saved = danhGiaRepository.save(danhGia);
        // AI kiểm duyệt chạy nền sau commit: đánh giá độc hại/spam/không liên quan sẽ tự bị ẩn.
        danhGiaAiService.kiemDuyetSauCommit(saved.getId(), saved.getSoSao(), saved.getNoiDung());
        return new DanhGiaResponse(
                saved.getId(), khachHang.getHoTen(), saved.getSoSao(), saved.getNoiDung(), saved.getMedia(),
                saved.getPhanHoi(), saved.getNgayPhanHoi(), saved.getNgayTao());
    }
}
