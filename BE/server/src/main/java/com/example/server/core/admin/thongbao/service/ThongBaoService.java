package com.example.server.core.admin.thongbao.service;

import com.example.server.core.admin.thongbao.dto.ThongBaoResponse;
import com.example.server.entity.ThongBao;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.websocket.WebSocketNotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ThongBaoService {

    private final WebSocketNotificationService webSocketNotificationService;
    private final com.example.server.repository.PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final ObjectMapper objectMapper;
    private final File storageFile;
    private static final int MAX_NOTIFICATIONS_LIMIT = 500; // Giới hạn lưu tối đa 500 thông báo gần nhất để tránh phình file

    public ThongBaoService(
            WebSocketNotificationService webSocketNotificationService,
            com.example.server.repository.PhieuGiamGiaRepository phieuGiamGiaRepository
    ) {
        this.webSocketNotificationService = webSocketNotificationService;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        // Thiết lập đường dẫn lưu trữ file notifications.json
        String userDir = System.getProperty("user.dir");
        File dataDir = new File(userDir, "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        this.storageFile = new File(dataDir, "notifications.json");
        if (!storageFile.exists()) {
            try {
                objectMapper.writeValue(storageFile, new ArrayList<ThongBao>());
            } catch (IOException e) {
                System.err.println("[ThongBaoService] Lỗi tạo file notifications.json: " + e.getMessage());
            }
        }
    }

    private synchronized List<ThongBao> readFromFile() {
        try {
            if (!storageFile.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(storageFile, new TypeReference<List<ThongBao>>() {});
        } catch (IOException e) {
            System.err.println("[ThongBaoService] Lỗi đọc file notifications.json: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private synchronized void writeToFile(List<ThongBao> list) {
        try {
            // Giới hạn số lượng thông báo để file không phình quá to
            List<ThongBao> targetList = list;
            if (list.size() > MAX_NOTIFICATIONS_LIMIT) {
                targetList = list.stream()
                        .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao())) // Mới nhất trước
                        .limit(MAX_NOTIFICATIONS_LIMIT)
                        .collect(Collectors.toList());
            }
            objectMapper.writeValue(storageFile, targetList);
        } catch (IOException e) {
            System.err.println("[ThongBaoService] Lỗi ghi file notifications.json: " + e.getMessage());
        }
    }

    /** Lấy danh sách thông báo phân trang, mới nhất lên đầu. */
    public Page<ThongBaoResponse> layDanhSach(int trang, int kichThuoc) {
        List<ThongBao> all = readFromFile();
        
        // Sắp xếp mới nhất lên đầu
        all.sort((a, b) -> b.getNgayTao().compareTo(a.getNgayTao()));

        int start = trang * kichThuoc;
        int end = Math.min(start + kichThuoc, all.size());
        
        List<ThongBaoResponse> pagedList = new ArrayList<>();
        if (start < all.size()) {
            pagedList = all.subList(start, end).stream()
                    .map(tb -> new ThongBaoResponse(
                            tb.getId(),
                            tb.getTieuDe(),
                            tb.getNoiDung(),
                            tb.getLoai(),
                            tb.getLink(),
                            tb.getDaDoc(),
                            tb.getNgayTao()
                    ))
                    .collect(Collectors.toList());
        }

        Pageable pageable = PageRequest.of(trang, kichThuoc);
        return new PageImpl<>(pagedList, pageable, all.size());
    }

    /** Đếm số thông báo chưa đọc. */
    public long demChuaDoc() {
        return readFromFile().stream()
                .filter(tb -> tb.getDaDoc() == null || !tb.getDaDoc())
                .count();
    }

    /** Tạo mới một thông báo và gửi realtime qua WebSocket. */
    public ThongBaoResponse taoThongBao(String tieuDe, String noiDung, String loai, String link) {
        List<ThongBao> all = readFromFile();

        // Kiểm tra xem đã có thông báo chưa đọc có cùng link này chưa để tránh spam
        if (link != null) {
            boolean unreadExists = all.stream()
                    .anyMatch(t -> link.equals(t.getLink()) && (t.getDaDoc() == null || !t.getDaDoc()));
            if (unreadExists) {
                return null; // Trả về null để tránh tạo trùng và tránh NPE ở các cuộc gọi
            }
        }

        ThongBao tb = new ThongBao();
        tb.setId(UUID.randomUUID());
        tb.setTieuDe(tieuDe);
        tb.setNoiDung(noiDung);
        tb.setLoai(loai);
        tb.setLink(link);
        tb.setDaDoc(false);
        tb.setNgayTao(Instant.now());

        all.add(tb);
        writeToFile(all);

        ThongBaoResponse response = new ThongBaoResponse(
                tb.getId(),
                tb.getTieuDe(),
                tb.getNoiDung(),
                tb.getLoai(),
                tb.getLink(),
                tb.getDaDoc(),
                tb.getNgayTao()
        );

        // Gửi realtime qua WebSocket sau khi transaction commit nếu có active transaction, hoặc phát luôn
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()
                && org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            webSocketNotificationService.sendToTopic("/topic/admin/notifications", "NEW_NOTIFICATION", response);
                        }
                    }
            );
        } else {
            webSocketNotificationService.sendToTopic("/topic/admin/notifications", "NEW_NOTIFICATION", response);
        }

        return response;
    }

    /** Đánh dấu một thông báo đã đọc. */
    public ThongBaoResponse docThongBao(UUID id) {
        List<ThongBao> all = readFromFile();
        ThongBao target = null;
        for (ThongBao tb : all) {
            if (tb.getId() != null && tb.getId().equals(id)) {
                target = tb;
                break;
            }
        }

        if (target == null) {
            throw new ResourceNotFoundException("Thông báo không tồn tại");
        }

        if (target.getDaDoc() == null || !target.getDaDoc()) {
            target.setDaDoc(true);
            target.setNgayCapNhat(Instant.now());
            writeToFile(all);
        }

        return new ThongBaoResponse(
                target.getId(),
                target.getTieuDe(),
                target.getNoiDung(),
                target.getLoai(),
                target.getLink(),
                target.getDaDoc(),
                target.getNgayTao()
        );
    }

    /** Đánh dấu tất cả thông báo là đã đọc. */
    public void docTatCa() {
        List<ThongBao> all = readFromFile();
        Instant now = Instant.now();
        boolean changed = false;
        for (ThongBao tb : all) {
            if (tb.getDaDoc() == null || !tb.getDaDoc()) {
                tb.setDaDoc(true);
                tb.setNgayCapNhat(now);
                changed = true;
            }
        }
        if (changed) {
            writeToFile(all);
        }
        webSocketNotificationService.sendToTopic("/topic/admin/notifications", "ALL_READ", null);
    }

    /** Xóa một thông báo bằng ID. */
    public void xoaThongBao(UUID id) {
        List<ThongBao> all = readFromFile();
        boolean removed = all.removeIf(tb -> tb.getId() != null && tb.getId().equals(id));
        if (removed) {
            writeToFile(all);
        }
    }

    private boolean checkLinkExists(String link) {
        if (link == null) return false;
        return readFromFile().stream()
                .anyMatch(tb -> link.equals(tb.getLink()));
    }

    /** Kiểm tra và gửi cảnh báo về các voucher sắp hết hạn hoặc sắp dùng hết. */
    public void checkVoucherCanhBao() {
        Instant now = Instant.now();
        Instant threeDaysLater = now.plus(java.time.Duration.ofDays(3));

        // Quét tất cả voucher
        java.util.List<com.example.server.entity.PhieuGiamGia> activeVouchers = phieuGiamGiaRepository.findAll();
        for (com.example.server.entity.PhieuGiamGia voucher : activeVouchers) {
            // Bỏ qua phiếu giảm giá tặng riêng cho cá nhân khách hàng (loaiPhieu = 2)
            if (voucher.getLoaiPhieu() != null && voucher.getLoaiPhieu() == 2) {
                continue;
            }

            // Chỉ kiểm tra voucher công khai đang hoạt động (trangThai = 1)
            if (voucher.getTrangThai() != null && voucher.getTrangThai() == 1) {
                String link = "/admin/phieu-giam-gia/" + voucher.getId();

                // 1. Cảnh báo hết hạn (còn dưới 3 ngày)
                if (voucher.getNgayKetThuc() != null && voucher.getNgayKetThuc().isAfter(now) && voucher.getNgayKetThuc().isBefore(threeDaysLater)) {
                    if (!checkLinkExists(link)) {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                .withZone(java.time.ZoneId.of("Asia/Bangkok"));
                        String formattedDate = formatter.format(voucher.getNgayKetThuc());
                        
                        taoThongBao(
                                "Phiếu giảm giá sắp hết hạn",
                                "Phiếu giảm giá \"" + voucher.getTen() + "\" (Mã: " + voucher.getMa() + ") sẽ hết hạn vào lúc " + formattedDate + ".",
                                "VOUCHER",
                                link
                        );
                    }
                }

                // 2. Cảnh báo hết số lượng (còn lại <= 5)
                if (voucher.getSoLuong() != null && voucher.getSoLuongDaDung() != null) {
                    int conLai = voucher.getSoLuong() - voucher.getSoLuongDaDung();
                    if (conLai >= 0 && conLai <= 5 && voucher.getSoLuong() != 999999) { // 999999 là voucher vô tận
                        String lowQtyLink = link + "?low-stock=true"; // Dùng link khác một chút để phân biệt
                        if (!checkLinkExists(lowQtyLink)) {
                            taoThongBao(
                                    "Phiếu giảm giá sắp hết lượt",
                                    "Phiếu giảm giá \"" + voucher.getTen() + "\" (Mã: " + voucher.getMa() + ") sắp hết lượt sử dụng, chỉ còn lại " + conLai + " lượt.",
                                    "VOUCHER",
                                    lowQtyLink
                            );
                        }
                    }
                }
            }
        }
    }
}
