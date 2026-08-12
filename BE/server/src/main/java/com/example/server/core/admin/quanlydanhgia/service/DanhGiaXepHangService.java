package com.example.server.core.admin.quanlydanhgia.service;

import com.example.server.core.admin.quanlydanhgia.dto.ThongKeXepHangDanhGiaResponse;
import com.example.server.core.admin.quanlydanhgia.dto.XepHangDanhGiaResponse;
import com.example.server.entity.Giay;
import com.example.server.repository.DanhGiaRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DanhGiaXepHangService {

    private static final int GIOI_HAN_XEP_HANG = 5;

    private final DanhGiaRepository danhGiaRepository;

    public DanhGiaXepHangService(DanhGiaRepository danhGiaRepository) {
        this.danhGiaRepository = danhGiaRepository;
    }

    @Transactional(readOnly = true)
    public ThongKeXepHangDanhGiaResponse thongKeTopVaThap() {
        List<XepHangDanhGiaResponse> thongKe = danhGiaRepository.thongKeXepHangDanhGia().stream()
                .map(this::chuyenDoi)
                .filter(item -> item.giay() != null && Integer.valueOf(1).equals(item.giay().getTrangThai()))
                .toList();

        Comparator<XepHangDanhGiaResponse> caoNhat = Comparator
                .comparingDouble(XepHangDanhGiaResponse::diemTrungBinh).reversed()
                .thenComparing(Comparator.comparingLong(XepHangDanhGiaResponse::soDanhGia).reversed())
                .thenComparing(item -> item.giay().getId(), Comparator.nullsLast(Integer::compareTo));

        Comparator<XepHangDanhGiaResponse> thapNhat = Comparator
                .comparingDouble(XepHangDanhGiaResponse::diemTrungBinh)
                .thenComparing(Comparator.comparingLong(XepHangDanhGiaResponse::soDanhGia).reversed())
                .thenComparing(item -> item.giay().getId(), Comparator.nullsLast(Integer::compareTo));

        return new ThongKeXepHangDanhGiaResponse(
                sapXepVaGioiHan(thongKe, caoNhat),
                sapXepVaGioiHan(thongKe, thapNhat)
        );
    }

    private List<XepHangDanhGiaResponse> sapXepVaGioiHan(
            List<XepHangDanhGiaResponse> source,
            Comparator<XepHangDanhGiaResponse> comparator
    ) {
        List<XepHangDanhGiaResponse> result = new ArrayList<>(source);
        result.sort(comparator);
        return List.copyOf(result.subList(0, Math.min(GIOI_HAN_XEP_HANG, result.size())));
    }

    private XepHangDanhGiaResponse chuyenDoi(Object[] row) {
        Giay giay = row != null && row.length > 0 && row[0] instanceof Giay ? (Giay) row[0] : null;
        double diemTrungBinh = row != null && row.length > 1 && row[1] instanceof Number
                ? ((Number) row[1]).doubleValue()
                : 0.0;
        long soDanhGia = row != null && row.length > 2 && row[2] instanceof Number
                ? ((Number) row[2]).longValue()
                : 0L;
        return new XepHangDanhGiaResponse(giay, diemTrungBinh, soDanhGia);
    }
}
