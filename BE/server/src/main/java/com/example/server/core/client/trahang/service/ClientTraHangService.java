package com.example.server.core.client.trahang.service;

import com.example.server.core.admin.quanlytrahang.domain.TraHangPolicy;
import com.example.server.core.client.trahang.dto.ClientYeuCauTraHangRequest;
import com.example.server.core.realtime.hoadon.HoaDonRealtimePublisher;
import com.example.server.entity.*;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientTraHangService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final PhieuTraHangRepository phieuTraHangRepository;
    private final PhieuTraHangChiTietRepository phieuTraHangChiTietRepository;
    private final LichSuPhieuTraHangRepository lichSuPhieuTraHangRepository;
    private final HinhAnhTraHangRepository hinhAnhTraHangRepository;
    private final HoaDonRealtimePublisher hoaDonRealtimePublisher;
    private final TraHangPolicy traHangPolicy;

    public ClientTraHangService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            PhieuTraHangRepository phieuTraHangRepository,
            PhieuTraHangChiTietRepository phieuTraHangChiTietRepository,
            LichSuPhieuTraHangRepository lichSuPhieuTraHangRepository,
            HinhAnhTraHangRepository hinhAnhTraHangRepository,
            HoaDonRealtimePublisher hoaDonRealtimePublisher,
            TraHangPolicy traHangPolicy
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.phieuTraHangRepository = phieuTraHangRepository;
        this.phieuTraHangChiTietRepository = phieuTraHangChiTietRepository;
        this.lichSuPhieuTraHangRepository = lichSuPhieuTraHangRepository;
        this.hinhAnhTraHangRepository = hinhAnhTraHangRepository;
        this.hoaDonRealtimePublisher = hoaDonRealtimePublisher;
        this.traHangPolicy = traHangPolicy;
    }

    @Transactional
    public void yeuCauTraHang(ClientYeuCauTraHangRequest request, UUID khachHangId) {
        HoaDon hoaDon = hoaDonRepository.findById(request.hoaDonId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn"));

        if (hoaDon.getKhachHang() == null || !hoaDon.getKhachHang().getId().equals(khachHangId)) {
            throw new BusinessException("Hóa đơn này không thuộc về bạn");
        }

        traHangPolicy.kiemTraHoaDonChoKhachHang(hoaDon);

        // Kiểm tra xem đã có phiếu trả hàng đang chờ xử lý hoặc đã duyệt chưa
        Optional<PhieuTraHang> phieuCu = phieuTraHangRepository.findFirstByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId());
        if (phieuCu.isPresent()) {
            int status = phieuCu.get().getTrangThai();
            if (status != 7 && status != 9 && status != 8) {
                throw new BusinessException("Đơn hàng này đã có yêu cầu trả hàng/hoàn tiền đang được xử lý");
            }
        }
        List<Integer> hoaDonChiTietIds = request.sanPhams().stream()
                .map(ClientYeuCauTraHangRequest.SanPhamTraItem::hoaDonChiTietId)
                .toList();
        if (new HashSet<>(hoaDonChiTietIds).size() != hoaDonChiTietIds.size()) {
            throw new BusinessException("Danh sách trả hàng không được chứa sản phẩm trùng lặp");
        }

        Instant now = Instant.now();
        PhieuTraHang phieu = new PhieuTraHang();
        phieu.setMa("PTH" + System.currentTimeMillis());
        phieu.setHoaDon(hoaDon);
        phieu.setKhachHang(hoaDon.getKhachHang());
        phieu.setNhanVien(null);
        phieu.setLyDo(request.moTa());
        phieu.setHinhThucHoan(request.hinhThucHoan());
        phieu.setLoaiYeuCau(2); // 2: Trả hàng/Hoàn tiền
        phieu.setLyDoMa(request.lyDoMa().trim().toUpperCase(Locale.ROOT));
        phieu.setMoTa(request.moTa());
        phieu.setTongTienHoan(BigDecimal.ZERO);
        phieu.setTongTienDuKien(BigDecimal.ZERO);
        phieu.setTongTienThucTe(BigDecimal.ZERO);
        phieu.setTrangThai(1); // 1: Chờ duyệt (CHO_DUYET)
        phieu.setNgayTao(now);
        phieu.setNgayCapNhat(now);

        List<PhieuTraHangChiTiet> chiTietList = new ArrayList<>();
        BigDecimal tongTienDuKien = BigDecimal.ZERO;

        for (ClientYeuCauTraHangRequest.SanPhamTraItem spItem : request.sanPhams()) {
            HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(spItem.hoaDonChiTietId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm trong hóa đơn"));

            if (!hoaDon.getId().equals(hdct.getHoaDon().getId())) {
                throw new BusinessException("Sản phẩm không thuộc về hóa đơn này");
            }

            // Tính số lượng đã trả trước đó
            int soLuongDaTra = phieuTraHangChiTietRepository.sumSoLuongDangXuLyByHoaDonChiTietId(hdct.getId());
            int soLuongConLai = (hdct.getSoLuong() == null ? 0 : hdct.getSoLuong()) - soLuongDaTra;
            if (spItem.soLuong() > soLuongConLai) {
                throw new BusinessException("Số lượng yêu cầu trả vượt quá số lượng sản phẩm còn lại có thể trả");
            }

            BigDecimal thanhTien = hdct.getGiaDonVi().multiply(BigDecimal.valueOf(spItem.soLuong()));
            BigDecimal tongTienHang = hoaDon.getTongTienHang() == null ? BigDecimal.ZERO : hoaDon.getTongTienHang();
            BigDecimal tienGiam = hoaDon.getTienGiam() == null ? BigDecimal.ZERO : hoaDon.getTienGiam();
            
            BigDecimal tienDuKien = thanhTien;
            if (tongTienHang.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal tyLe = thanhTien.divide(tongTienHang, 4, java.math.RoundingMode.HALF_UP);
                BigDecimal giamPhanBo = tienGiam.multiply(tyLe);
                tienDuKien = thanhTien.subtract(giamPhanBo).max(BigDecimal.ZERO);
            }

            PhieuTraHangChiTiet ct = new PhieuTraHangChiTiet();
            ct.setPhieuTraHang(phieu);
            ct.setHoaDonChiTiet(hdct);
            ct.setGiayChiTiet(hdct.getGiayChiTiet());
            ct.setSoLuongTra(spItem.soLuong());
            ct.setSoLuongNhan(0);
            ct.setSoLuongChapNhan(0);
            ct.setSoLuongTuChoi(0);
            ct.setGiaBan(hdct.getGiaDonVi());
            ct.setThanhTien(thanhTien);
            ct.setSoTienHoan(BigDecimal.ZERO);
            ct.setNhapLaiTonKho(false);
            ct.setDaCapNhatTon(false);
            ct.setTrangThai(1);
            ct.setGhiChu(spItem.ghiChu());
            ct.setNgayTao(now);

            chiTietList.add(ct);
            tongTienDuKien = tongTienDuKien.add(tienDuKien);
        }

        phieu.setTongTienDuKien(tongTienDuKien);
        PhieuTraHang savedPhieu = phieuTraHangRepository.save(phieu);
        chiTietList.forEach(ct -> ct.setPhieuTraHang(savedPhieu));
        phieuTraHangChiTietRepository.saveAll(chiTietList);

        // Lưu hình ảnh nếu có
        if (request.hinhAnhs() != null) {
            for (String url : request.hinhAnhs()) {
                HinhAnhTraHang ha = new HinhAnhTraHang();
                ha.setPhieuTraHang(savedPhieu);
                ha.setPhieuTraHangChiTiet(null);
                ha.setUrl(url);
                ha.setLoaiAnh(1); // 1: Khách hàng upload
                ha.setNgayTao(now);
                hinhAnhTraHangRepository.save(ha);
            }
        }

        // Lưu lịch sử
        LichSuPhieuTraHang ls = new LichSuPhieuTraHang();
        ls.setPhieuTraHang(savedPhieu);
        ls.setNhanVien(null);
        ls.setTrangThaiCu(null);
        ls.setTrangThaiMoi(1); // Chờ duyệt
        ls.setHanhDong("Khách hàng gửi yêu cầu trả hàng/hoàn tiền");
        ls.setGhiChu(request.moTa());
        ls.setNgayTao(now);
        lichSuPhieuTraHangRepository.save(ls);
        hoaDonRealtimePublisher.publishAfterCommit(hoaDon, "TRA_HANG");
    }
}
