package com.example.server.core.client.danhgia.service;

import com.example.server.core.client.danhgia.dto.DanhGiaResponse;
import com.example.server.core.client.danhgia.dto.DanhGiaTongHopResponse;
import com.example.server.core.client.danhgia.dto.TaoDanhGiaRequest;
import com.example.server.entity.DanhGia;
import com.example.server.entity.Giay;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DanhGiaRepository;
import com.example.server.repository.GiayRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.KhachHangRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientDanhGiaService {

    private static final int TRANG_THAI_HOAN_THANH = 5;

    private final DanhGiaRepository danhGiaRepository;
    private final GiayRepository giayRepository;
    private final KhachHangRepository khachHangRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    public ClientDanhGiaService(
            DanhGiaRepository danhGiaRepository,
            GiayRepository giayRepository,
            KhachHangRepository khachHangRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository
    ) {
        this.danhGiaRepository = danhGiaRepository;
        this.giayRepository = giayRepository;
        this.khachHangRepository = khachHangRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
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
                        dg.getNgayTao()))
                .toList();
        double trungBinh = ds.isEmpty()
                ? 0
                : ds.stream().mapToInt(DanhGia::getSoSao).average().orElse(0);
        // Làm tròn 1 chữ số thập phân.
        double diemTrungBinh = Math.round(trungBinh * 10.0) / 10.0;
        return new DanhGiaTongHopResponse(diemTrungBinh, ds.size(), danhSach);
    }

    @Transactional
    public DanhGiaResponse tao(Integer giayId, TaoDanhGiaRequest request) {
        Giay giay = giayRepository.findById(giayId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        KhachHang khachHang = khachHangRepository.findById(request.khachHangId())
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));

        DanhGia danhGia = new DanhGia();
        danhGia.setGiay(giay);
        danhGia.setKhachHang(khachHang);
        danhGia.setSoSao(request.soSao());
        danhGia.setNoiDung(request.noiDung() != null && !request.noiDung().isBlank()
                ? request.noiDung().trim() : null);
        danhGia.setTrangThai(1);
        danhGia.setNgayTao(Instant.now());

        DanhGia saved = danhGiaRepository.save(danhGia);
        return new DanhGiaResponse(
                saved.getId(),
                khachHang.getHoTen(),
                saved.getSoSao(),
                saved.getNoiDung(),
                saved.getNgayTao());
    }

    /**
     * Đánh giá theo dòng hóa đơn chi tiết: chỉ người đã mua + đã nhận hàng mới được đánh giá,
     * và mỗi dòng hóa đơn chi tiết chỉ đánh giá 1 lần.
     */
    @Transactional
    public DanhGiaResponse taoTheoHoaDonChiTiet(UUID khachHangId, Integer hoaDonChiTietId, Integer soSao, String noiDung) {
        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(hoaDonChiTietId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm trong đơn không tồn tại"));
        HoaDon hd = ct.getHoaDon();

        if (hd.getKhachHang() == null || !hd.getKhachHang().getId().equals(khachHangId)) {
            throw new BusinessException("Bạn chỉ có thể đánh giá sản phẩm mình đã mua");
        }
        if (hd.getTrangThai() == null || hd.getTrangThai() != TRANG_THAI_HOAN_THANH
                || !Boolean.TRUE.equals(hd.getDaNhanHang())) {
            throw new BusinessException("Bạn cần xác nhận đã nhận hàng trước khi đánh giá");
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
        danhGia.setTrangThai(1);
        danhGia.setNgayTao(Instant.now());

        DanhGia saved = danhGiaRepository.save(danhGia);
        return new DanhGiaResponse(
                saved.getId(), khachHang.getHoTen(), saved.getSoSao(), saved.getNoiDung(), saved.getNgayTao());
    }
}
