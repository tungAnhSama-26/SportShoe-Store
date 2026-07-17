package com.example.server.core.client.thongbao.service;

import com.example.server.core.client.thongbao.dto.ThongBaoKhachResponse;
import com.example.server.entity.KhachHang;
import com.example.server.entity.ThongBaoKhachHang;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.ThongBaoKhachHangRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Thông báo cho khách hàng (chuông ở header):
 * trạng thái đơn đổi, voucher mới (công khai + tặng riêng), đợt giảm giá mới, đánh giá bị ẩn.
 * Chỉ giữ 3 ngày gần nhất - quá 3 ngày scheduler tự xóa.
 */
@Service
public class ClientThongBaoService {

    private static final Duration HAN_HIEU_LUC = Duration.ofDays(3);
    private static final int TRANG_THAI_KHACH_HOAT_DONG = 1;

    private final ThongBaoKhachHangRepository thongBaoKhachHangRepository;
    private final KhachHangRepository khachHangRepository;

    public ClientThongBaoService(
            ThongBaoKhachHangRepository thongBaoKhachHangRepository,
            KhachHangRepository khachHangRepository
    ) {
        this.thongBaoKhachHangRepository = thongBaoKhachHangRepository;
        this.khachHangRepository = khachHangRepository;
    }

    // ─── API cho FE ─────────────────────────────────────────────────────────

    /** Danh sách thông báo còn hiệu lực (3 ngày) của khách, mới nhất trước. */
    @Transactional(readOnly = true)
    public List<ThongBaoKhachResponse> layDanhSach(UUID khachHangId) {
        return thongBaoKhachHangRepository
                .findByKhachHangIdAndNgayTaoAfterOrderByNgayTaoDesc(khachHangId, mocHieuLuc())
                .stream()
                .map(t -> new ThongBaoKhachResponse(
                        t.getId(), t.getLoai(), t.getTieuDe(), t.getNoiDung(),
                        t.getLienKet(), t.getDaXem(), t.getNgayTao()))
                .toList();
    }

    /** Số thông báo chưa xem (số nhỏ cạnh chuông). */
    @Transactional(readOnly = true)
    public long demChuaXem(UUID khachHangId) {
        return thongBaoKhachHangRepository
                .countByKhachHangIdAndDaXemFalseAndNgayTaoAfter(khachHangId, mocHieuLuc());
    }

    /** Khách mở chuông -> tất cả thành đã xem. */
    @Transactional
    public void danhDauDaXem(UUID khachHangId) {
        thongBaoKhachHangRepository.danhDauDaXemTatCa(khachHangId);
    }

    // ─── Phát thông báo (gọi từ các nghiệp vụ khác) ─────────────────────────

    /**
     * Gửi thông báo cho 1 khách. Chạy ở transaction RIÊNG + nuốt lỗi để việc
     * gửi thông báo không bao giờ làm hỏng nghiệp vụ chính (đổi trạng thái đơn, tạo voucher...).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guiChoKhach(UUID khachHangId, String loai, String tieuDe, String noiDung, String lienKet) {
        if (khachHangId == null) {
            return;
        }
        try {
            thongBaoKhachHangRepository.save(tao(khachHangId, loai, tieuDe, noiDung, lienKet));
        } catch (Exception e) {
            System.err.println("[THONG BAO KHACH] Lỗi gửi cho khách " + khachHangId + ": " + e.getMessage());
        }
    }

    /** Gửi thông báo cho TẤT CẢ khách đang hoạt động (voucher công khai, đợt giảm giá mới). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guiChoTatCaKhach(String loai, String tieuDe, String noiDung, String lienKet) {
        try {
            List<ThongBaoKhachHang> ds = new ArrayList<>();
            for (KhachHang kh : khachHangRepository.findByTrangThai(TRANG_THAI_KHACH_HOAT_DONG)) {
                ds.add(tao(kh.getId(), loai, tieuDe, noiDung, lienKet));
            }
            thongBaoKhachHangRepository.saveAll(ds);
        } catch (Exception e) {
            System.err.println("[THONG BAO KHACH] Lỗi gửi cho tất cả khách: " + e.getMessage());
        }
    }

    private ThongBaoKhachHang tao(UUID khachHangId, String loai, String tieuDe, String noiDung, String lienKet) {
        ThongBaoKhachHang t = new ThongBaoKhachHang();
        t.setKhachHangId(khachHangId);
        t.setLoai(loai);
        t.setTieuDe(cat(tieuDe, 200));
        t.setNoiDung(cat(noiDung, 500));
        t.setLienKet(cat(lienKet, 200));
        t.setDaXem(false);
        t.setNgayTao(Instant.now());
        return t;
    }

    private static String cat(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    // ─── Mô tả đầy đủ cho nội dung thông báo (bấm vào xem chi tiết) ─────────

    /** Mô tả đầy đủ 1 phiếu giảm giá: mã, mức giảm, điều kiện, hạn dùng. */
    public static String moTaPhieuGiamGia(com.example.server.entity.PhieuGiamGia p) {
        StringBuilder sb = new StringBuilder("Mã: ").append(p.getMa());
        if (Integer.valueOf(1).equals(p.getLoai())) {
            sb.append("\nGiảm: ").append(soGon(p.getGiaTri())).append("%");
            if (p.getGiamToiDa() != null && p.getGiamToiDa().signum() > 0) {
                sb.append(" (tối đa ").append(tien(p.getGiamToiDa())).append(")");
            }
        } else {
            sb.append("\nGiảm: ").append(tien(p.getGiaTri()));
        }
        if (p.getGiaTriToiThieu() != null && p.getGiaTriToiThieu().signum() > 0) {
            sb.append("\nĐơn tối thiểu: ").append(tien(p.getGiaTriToiThieu()));
        }
        if (p.getNgayBatDau() != null && p.getNgayKetThuc() != null) {
            sb.append("\nHiệu lực: ").append(ngay(p.getNgayBatDau()))
                    .append(" - ").append(ngay(p.getNgayKetThuc()));
        }
        sb.append("\nNhập mã này ở bước thanh toán để được giảm nhé!");
        return sb.toString();
    }

    /** Mô tả đầy đủ 1 đợt giảm giá: mức giảm, thời gian áp dụng. */
    public static String moTaDotGiamGia(com.example.server.entity.DotGiamGia d) {
        String muc = Integer.valueOf(1).equals(d.getLoaiGiam())
                ? soGon(d.getGiaTriGiam()) + "%"
                : tien(d.getGiaTriGiam());
        StringBuilder sb = new StringBuilder("Giảm ").append(muc)
                .append(" cho các sản phẩm trong đợt \"").append(d.getTen()).append("\"");
        if (d.getMoTa() != null && !d.getMoTa().isBlank()) {
            sb.append("\n").append(d.getMoTa().trim());
        }
        if (d.getNgayBatDau() != null && d.getNgayKetThuc() != null) {
            sb.append("\nThời gian: ").append(ngay(d.getNgayBatDau()))
                    .append(" - ").append(ngay(d.getNgayKetThuc()));
        }
        sb.append("\nNhanh tay săn deal kẻo lỡ!");
        return sb.toString();
    }

    /** 50000 -> "50.000đ" (kiểu tiền Việt). */
    private static String tien(java.math.BigDecimal v) {
        java.text.DecimalFormatSymbols kyHieu = new java.text.DecimalFormatSymbols();
        kyHieu.setGroupingSeparator('.');
        return new java.text.DecimalFormat("#,###", kyHieu).format(v) + "đ";
    }

    /** 10.00 -> "10" (bỏ số 0 thừa cho phần trăm). */
    private static String soGon(java.math.BigDecimal v) {
        return v == null ? "0" : v.stripTrailingZeros().toPlainString();
    }

    private static String ngay(Instant moc) {
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).format(moc);
    }

    private static String ngay(java.time.LocalDate moc) {
        return moc.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static Instant mocHieuLuc() {
        return Instant.now().minus(HAN_HIEU_LUC);
    }

    // ─── Dọn dẹp ────────────────────────────────────────────────────────────

    /** Mỗi giờ xóa thông báo quá 3 ngày (khách chỉ thấy tối đa 3 ngày gần nhất). */
    @Scheduled(fixedRate = 3_600_000L, initialDelay = 60_000L)
    @Transactional
    public void xoaThongBaoQuaHan() {
        int daXoa = thongBaoKhachHangRepository.xoaTruocMoc(mocHieuLuc());
        if (daXoa > 0) {
            System.out.println("[THONG BAO KHACH] Đã xóa " + daXoa + " thông báo quá 3 ngày");
        }
    }
}
