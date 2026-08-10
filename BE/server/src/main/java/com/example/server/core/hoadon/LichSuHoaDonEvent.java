package com.example.server.core.hoadon;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum LichSuHoaDonEvent {
    CHO_XAC_NHAN("Chờ xác nhận", 1),
    CHO_LAY_HANG("Chờ lấy hàng", 2),
    DANG_GIAO_HANG("Đang giao hàng", 3),
    DA_GIAO_HANG("Đã giao hàng", 4),
    HOAN_THANH("Hoàn thành", 5),
    HUY("Hủy", 6),
    YEU_CAU_HUY("Yêu cầu hủy", 7),
    DA_XAC_NHAN("Đã xác nhận", 9),
    GIAO_HANG_THAT_BAI("Giao hàng thất bại", 10),
    HOA_DON_CHO("Hóa đơn chờ", 11),
    KHACH_DA_NHAN_HANG("Khách hàng đã nhận hàng", null),
    KHACH_SUA_DIA_CHI("Cập nhật thông tin giao hàng", null),
    TU_CHOI_YEU_CAU_HUY("Từ chối yêu cầu hủy", null),
    CHAP_NHAN_YEU_CAU_HUY("Chấp nhận yêu cầu hủy", null);

    private final String nhan;
    private final Integer trangThaiHoaDon;

    LichSuHoaDonEvent(String nhan, Integer trangThaiHoaDon) {
        this.nhan = nhan;
        this.trangThaiHoaDon = trangThaiHoaDon;
    }

    public String ma() {
        return name();
    }

    public String nhan() {
        return nhan;
    }

    public Optional<Integer> trangThaiHoaDon() {
        return Optional.ofNullable(trangThaiHoaDon);
    }

    public boolean laTrangThaiOnDinh() {
        return trangThaiHoaDon != null && this != YEU_CAU_HUY;
    }

    public static String chuanHoaMa(String giaTri) {
        return timTheoGiaTri(giaTri).map(LichSuHoaDonEvent::ma).orElse(giaTri);
    }

    public static String nhanHienThi(String giaTri) {
        return timTheoGiaTri(giaTri).map(LichSuHoaDonEvent::nhan).orElse(giaTri);
    }

    public static Optional<LichSuHoaDonEvent> timTheoGiaTri(String giaTri) {
        if (giaTri == null || giaTri.isBlank()) {
            return Optional.empty();
        }
        String normalized = giaTri.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(event -> event.name().toLowerCase(Locale.ROOT).equals(normalized)
                        || event.nhan.toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst();
    }

    public static LichSuHoaDonEvent tuTrangThaiHoaDon(Integer trangThai) {
        return Arrays.stream(values())
                .filter(LichSuHoaDonEvent::laTrangThaiOnDinh)
                .filter(event -> event.trangThaiHoaDon.equals(trangThai))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Trạng thái hóa đơn không có mã lịch sử ổn định: " + trangThai));
    }

    public static List<String> maTrangThaiOnDinh() {
        return Arrays.stream(values())
                .filter(LichSuHoaDonEvent::laTrangThaiOnDinh)
                .map(LichSuHoaDonEvent::ma)
                .toList();
    }
}
