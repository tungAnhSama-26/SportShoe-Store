package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.PhieuGiamGiaRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaResponse;
import com.example.server.entity.PhieuGiamGia;
import com.example.server.entity.PhieuGiamGiaKhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.PhieuGiamGiaKhachHangRepository;
import com.example.server.repository.PhieuGiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhieuGiamGiaService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Bangkok");
    private static final int UNLIMITED_QUANTITY = 999999;

    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final EmailService emailService;
    private final com.example.server.core.realtime.sanpham.SanPhamRealtimePublisher sanPhamRealtimePublisher;

    public java.util.Map<String, Boolean> checkTenTrung(String ten, Integer id) {
        boolean exists = false;
        if (id != null && id > 0) {
            exists = phieuGiamGiaRepository.existsByTenIgnoreCaseAndIdNot(ten, id);
        } else {
            exists = phieuGiamGiaRepository.existsByTenIgnoreCase(ten);
        }
        return java.util.Map.of("exists", exists);
    }

    public List<QuanLyPhieuGiamGiaResponse> getAll() {
        return phieuGiamGiaRepository.hienThiPhieuGiamGia();
    }

    public QuanLyPhieuGiamGiaResponse getOne(Integer id) {
        return phieuGiamGiaRepository.DetailPhieuGiamGia(id);
    }

    public Page<QuanLyPhieuGiamGiaResponse> phanTrang(String keyword, Integer trangThai, Integer loai, Integer loaiPhieu, LocalDate tuNgay,
            LocalDate denNgay, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Instant start = tuNgay == null ? null : tuNgay.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant end = denNgay == null ? null : denNgay.atTime(LocalTime.MAX).atZone(BUSINESS_ZONE).toInstant();
        return phieuGiamGiaRepository.timKiemVaPhanTrang(keyword, trangThai, loai, loaiPhieu, start, end, pageable);
    }

    public void remove(Integer id) {
        throw new BusinessException("Không được phép xóa phiếu giảm giá. Vui lòng chuyển trạng thái sang ngưng hoạt động.");
    }

    public PhieuGiamGia add(PhieuGiamGiaRequest request) {
        normalize(request);
        validateBusinessRules(request, null, 0);
        PhieuGiamGia phieuGiamGia = new PhieuGiamGia();
        mapRequestToEntity(request, phieuGiamGia);
        phieuGiamGia.setSoLuongDaDung(0);
        phieuGiamGia.setNgayTao(Instant.now());
        PhieuGiamGia saved = phieuGiamGiaRepository.save(phieuGiamGia);
        sanPhamRealtimePublisher.phatSauCommit("PHIEU_GIAM_GIA");
        return saved;
    }

    public PhieuGiamGia update(Integer id, PhieuGiamGiaRequest request) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu giảm giá"));

        if (Integer.valueOf(2).equals(phieuGiamGia.getLoaiPhieu())) {
            Instant now = Instant.now();
            if (phieuGiamGia.getNgayKetThuc() != null && phieuGiamGia.getNgayKetThuc().isBefore(now)) {
                throw new BusinessException("Không thể chỉnh sửa phiếu giảm giá cá nhân đã hết hạn.");
            }
        }

        normalize(request);
        validateBusinessRules(request, id, phieuGiamGia.getSoLuongDaDung());

        // Lưu giữ trạng thái cũ của dữ liệu trước khi mapRequest
        String oldMa = phieuGiamGia.getMa();
        String oldTen = phieuGiamGia.getTen();
        Integer oldLoai = phieuGiamGia.getLoai();
        java.math.BigDecimal oldGiaTri = phieuGiamGia.getGiaTri();
        java.math.BigDecimal oldGiaTriToiThieu = phieuGiamGia.getGiaTriToiThieu();
        java.math.BigDecimal oldGiamToiDa = phieuGiamGia.getGiamToiDa();
        Instant oldNgayBatDau = phieuGiamGia.getNgayBatDau();
        Instant oldNgayKetThuc = phieuGiamGia.getNgayKetThuc();
        Integer oldSoLuong = phieuGiamGia.getSoLuong();
        Integer oldTrangThai = phieuGiamGia.getTrangThai();

        mapRequestToEntity(request, phieuGiamGia);

        PhieuGiamGia saved = phieuGiamGiaRepository.save(phieuGiamGia);
        // Báo realtime để màn thanh toán kiểm tra lại phiếu khách đang áp (vd phiếu bị ngừng).
        sanPhamRealtimePublisher.phatSauCommit("PHIEU_GIAM_GIA");

        // Kiểm tra xem trạng thái thay đổi sang Ngừng hoạt động (0)
        boolean statusChangedToDeactivated = (oldTrangThai == null || oldTrangThai != 0) && (saved.getTrangThai() != null && saved.getTrangThai() == 0);

        // Kiểm tra xem các dữ liệu cốt lõi khác của phiếu có thay đổi không
        boolean dataChanged = false;
        if (!safeEquals(oldMa, saved.getMa())) dataChanged = true;
        else if (!safeEquals(oldTen, saved.getTen())) dataChanged = true;
        else if (!safeEquals(oldLoai, saved.getLoai())) dataChanged = true;
        else if (!safeCompare(oldGiaTri, saved.getGiaTri())) dataChanged = true;
        else if (!safeCompare(oldGiaTriToiThieu, saved.getGiaTriToiThieu())) dataChanged = true;
        else if (!safeCompare(oldGiamToiDa, saved.getGiamToiDa())) dataChanged = true;
        else if (!safeEquals(oldNgayBatDau, saved.getNgayBatDau())) dataChanged = true;
        else if (!safeEquals(oldNgayKetThuc, saved.getNgayKetThuc())) dataChanged = true;
        else if (!safeEquals(oldSoLuong, saved.getSoLuong())) dataChanged = true;

        if (statusChangedToDeactivated) {
            sendDeactivatedEmailsToCustomers(saved);
        } else if (dataChanged) {
            // Chỉ gửi email cập nhật khi thực sự có thay đổi dữ liệu của phiếu
            sendUpdateEmailsToCustomers(saved);
        }

        return saved;
    }

    /**
     * Gửi email thông báo cập nhật phiếu tới tất cả khách hàng được gán phiếu đó.
     * Bỏ qua khách hàng không có email.
     */
    private void sendUpdateEmailsToCustomers(PhieuGiamGia phieuGiamGia) {
        try {
            java.util.List<PhieuGiamGiaKhachHang> lienKetList =
                    phieuGiamGiaKhachHangRepository.findAllByPhieuGiamGiaId(phieuGiamGia.getId());
            for (PhieuGiamGiaKhachHang lienKet : lienKetList) {
                String email = lienKet.getKhachHang().getEmail();
                if (email != null && !email.isBlank()) {
                    emailService.sendVoucherUpdatedEmailAsync(
                            email,
                            lienKet.getKhachHang().getHoTen(),
                            phieuGiamGia.getMa(),
                            phieuGiamGia.getTen(),
                            phieuGiamGia.getLoai(),
                            phieuGiamGia.getGiaTri(),
                            phieuGiamGia.getGiamToiDa(),
                            phieuGiamGia.getGiaTriToiThieu(),
                            phieuGiamGia.getNgayBatDau(),
                            phieuGiamGia.getNgayKetThuc()
                    );
                }
            }
        } catch (Exception e) {
            // Không để lỗi email làm hỏng luồng update chính
            org.slf4j.LoggerFactory.getLogger(PhieuGiamGiaService.class)
                    .error("Lỗi khi gửi email cập nhật phiếu {}: {}", phieuGiamGia.getMa(), e.getMessage());
        }
    }

    /**
     * Gửi email thông báo ngừng hoạt động phiếu tới tất cả khách hàng được gán phiếu đó.
     * Bỏ qua khách hàng không có email.
     */
    private void sendDeactivatedEmailsToCustomers(PhieuGiamGia phieuGiamGia) {
        try {
            java.util.List<PhieuGiamGiaKhachHang> lienKetList =
                    phieuGiamGiaKhachHangRepository.findAllByPhieuGiamGiaId(phieuGiamGia.getId());
            for (PhieuGiamGiaKhachHang lienKet : lienKetList) {
                String email = lienKet.getKhachHang().getEmail();
                if (email != null && !email.isBlank()) {
                    emailService.sendVoucherDeactivatedEmailAsync(
                            email,
                            lienKet.getKhachHang().getHoTen(),
                            phieuGiamGia.getMa(),
                            phieuGiamGia.getTen()
                    );
                }
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PhieuGiamGiaService.class)
                    .error("Lỗi khi gửi email ngừng hoạt động phiếu {}: {}", phieuGiamGia.getMa(), e.getMessage());
        }
    }

    private void mapRequestToEntity(PhieuGiamGiaRequest request, PhieuGiamGia phieuGiamGia) {
        phieuGiamGia.setMa(request.getMa());
        phieuGiamGia.setTen(request.getTen());
        phieuGiamGia.setLoai(request.getLoai());
        phieuGiamGia.setLoaiPhieu(request.getLoaiPhieu() == null ? 1 : request.getLoaiPhieu());
        phieuGiamGia.setGiaTri(request.getGiaTri());
        phieuGiamGia.setGiaTriToiThieu(request.getGiaTriToiThieu());
        phieuGiamGia.setGiamToiDa(request.getGiamToiDa());
        phieuGiamGia.setNgayBatDau(toStartOfDay(request.getNgayBatDau()));
        phieuGiamGia.setNgayKetThuc(toEndOfDay(request.getNgayKetThuc()));
        phieuGiamGia.setSoLuong(request.getSoLuong());

        // Tự động tính toán trạng thái
        Instant now = Instant.now();
        Instant end = phieuGiamGia.getNgayKetThuc();

        if (end != null && end.isBefore(now)) {
            phieuGiamGia.setTrangThai(2);
        } else {
            Integer requestedStatus = request.getTrangThai();
            if (requestedStatus != null && requestedStatus == 0) {
                // FE yêu cầu ngừng hoạt động và chưa hết date
                phieuGiamGia.setTrangThai(0);
            } else {
                Instant start = phieuGiamGia.getNgayBatDau();
                int soLuongDaDung = phieuGiamGia.getSoLuongDaDung();
                int soLuong = phieuGiamGia.getSoLuong();

                if (soLuong != UNLIMITED_QUANTITY && soLuongDaDung >= soLuong) {
                    phieuGiamGia.setTrangThai(3);
                } else if (start != null && start.isAfter(now)) {
                    phieuGiamGia.setTrangThai(4); // Sắp diễn ra
                } else {
                    phieuGiamGia.setTrangThai(1); // Hoạt động
                }
            }
        }

        if (phieuGiamGia.getNgayTao() == null) {
            phieuGiamGia.setNgayTao(Instant.now());
        }

        phieuGiamGia.setNgayCapNhat(phieuGiamGia.getId() == null ? null : Instant.now());
    }

    private void normalize(PhieuGiamGiaRequest request) {
        request.setMa(request.getMa() == null ? null : request.getMa().trim().toUpperCase());
        request.setTen(request.getTen() == null ? null : request.getTen().trim());
        if (request.getGiaTriToiThieu() == null) {
            request.setGiaTriToiThieu(java.math.BigDecimal.ZERO);
        }
        if (request.getGiamToiDa() == null) {
            request.setGiamToiDa(java.math.BigDecimal.ZERO);
        }
    }

    private void validateBusinessRules(
            PhieuGiamGiaRequest request,
            Integer currentId,
            Integer usedQuantity
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        boolean duplicateCode = currentId == null
                ? phieuGiamGiaRepository.existsByMaIgnoreCase(request.getMa())
                : phieuGiamGiaRepository.existsByMaIgnoreCaseAndIdNot(request.getMa(), currentId);
        boolean duplicateName = currentId == null
                ? phieuGiamGiaRepository.existsByTenIgnoreCase(request.getTen())
                : phieuGiamGiaRepository.existsByTenIgnoreCaseAndIdNot(request.getTen(), currentId);

        if (duplicateCode) {
            errors.put("ma", "Mã phiếu giảm giá đã tồn tại");
        }
        if (duplicateName) {
            errors.put("ten", "Tên phiếu giảm giá đã tồn tại");
        }
        if (request.getNgayBatDau() != null
                && request.getNgayKetThuc() != null
                && request.getNgayKetThuc().isBefore(request.getNgayBatDau())) {
            errors.put("ngayKetThuc", "Ngày kết thúc không được trước ngày bắt đầu");
        }
        if (Integer.valueOf(1).equals(request.getLoai())
                && request.getGiaTri() != null
                && request.getGiaTri().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
            errors.put("giaTri", "Phần trăm giảm không được vượt quá 100%");
        }
        if (Integer.valueOf(2).equals(request.getLoai())
                && request.getGiaTriToiThieu() != null
                && request.getGiaTriToiThieu().signum() > 0
                && request.getGiaTri() != null
                && request.getGiaTri().compareTo(request.getGiaTriToiThieu()) > 0) {
            errors.put("giaTri", "Số tiền giảm không được lớn hơn giá trị đơn tối thiểu");
        }
        if (request.getSoLuong() != null
                && request.getSoLuong() != UNLIMITED_QUANTITY
                && request.getSoLuong() < (usedQuantity == null ? 0 : usedQuantity)) {
            errors.put("soLuong", "Số lượng phiếu không được nhỏ hơn số lượng đã sử dụng");
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(
                    com.example.server.infrastructure.exception.ErrorCode.VALIDATION_ERROR,
                    "Vui lòng kiểm tra lại thông tin phiếu giảm giá",
                    errors
            );
        }
    }

    private Instant toStartOfDay(LocalDate value) {
        return value == null ? null : value.atStartOfDay(BUSINESS_ZONE).toInstant();
    }

    private Instant toEndOfDay(LocalDate value) {
        return value == null ? null : value.atTime(LocalTime.MAX).atZone(BUSINESS_ZONE).toInstant();
    }

    private boolean safeEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 == null || o2 == null) return false;
        return o1.equals(o2);
    }

    private boolean safeCompare(java.math.BigDecimal d1, java.math.BigDecimal d2) {
        if (d1 == null && d2 == null) return true;
        if (d1 == null || d2 == null) return false;
        return d1.compareTo(d2) == 0;
    }
}
