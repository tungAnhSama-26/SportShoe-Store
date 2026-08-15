package com.example.server.core.inventory;

import com.example.server.core.client.dathang.dto.DatHangItemRequest;
import com.example.server.entity.GiayChiTiet;
import com.example.server.infrastructure.exception.InventoryConflictException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Nguồn tính tồn có thể bán dùng chung, không làm thay đổi tồn kho thực tế. */
@Service
public class TonKhoKhaDungService {

    public static final Duration THOI_GIAN_GIU_QR = Duration.ofMinutes(5);

    private final GiayChiTietRepository giayChiTietRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    public TonKhoKhaDungService(
            GiayChiTietRepository giayChiTietRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository
    ) {
        this.giayChiTietRepository = giayChiTietRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
    }

    /** Khóa theo ID tăng dần, sau đó kiểm tra toàn bộ giỏ trong cùng transaction gọi vào. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Map<Integer, GiayChiTiet> khoaVaKiemTra(
            List<DatHangItemRequest> requests,
            Integer loaiTruHoaDonId
    ) {
        Map<Integer, Integer> soLuongYeuCau = gomSoLuong(requests);
        List<Integer> ids = new ArrayList<>(soLuongYeuCau.keySet());
        ids.sort(Comparator.naturalOrder());

        Map<Integer, GiayChiTiet> daKhoa = new LinkedHashMap<>();
        for (Integer id : ids) {
            GiayChiTiet bienThe = giayChiTietRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm không tồn tại: " + id));
            daKhoa.put(id, bienThe);
        }

        Map<Integer, Integer> dangGiu = laySoLuongDangGiu(ids, loaiTruHoaDonId, Instant.now());
        for (Integer id : ids) {
            GiayChiTiet bienThe = daKhoa.get(id);
            int tonThucTe = bienThe.getSoLuong() == null ? 0 : bienThe.getSoLuong();
            int soDangGiu = dangGiu.getOrDefault(id, 0);
            int khaDung = Math.max(tonThucTe - soDangGiu, 0);
            int yeuCau = soLuongYeuCau.get(id);
            if (khaDung < yeuCau) {
                String ten = bienThe.getGiay() == null ? "Sản phẩm" : bienThe.getGiay().getTen();
                String mau = bienThe.getMauSac() == null ? "" : bienThe.getMauSac().getTen();
                String size = bienThe.getKichCo() == null ? "" : String.valueOf(bienThe.getKichCo().getGiaTri());
                throw new InventoryConflictException(
                        "Sản phẩm vừa được khách khác đặt trước; còn " + khaDung + ", bạn yêu cầu " + yeuCau,
                        Map.of(
                                "giayChiTietId", String.valueOf(id),
                                "sanPham", ten,
                                "mauSac", mau,
                                "kichCo", size,
                                "soLuongYeuCau", String.valueOf(yeuCau),
                                "soLuongKhaDung", String.valueOf(khaDung)
                        )
                );
            }
        }
        return daKhoa;
    }

    @Transactional(readOnly = true)
    public Map<Integer, Integer> laySoLuongKhaDung(Collection<GiayChiTiet> bienThes) {
        if (bienThes == null || bienThes.isEmpty()) {
            return Map.of();
        }
        List<Integer> ids = bienThes.stream().map(GiayChiTiet::getId).distinct().toList();
        Map<Integer, Integer> dangGiu = laySoLuongDangGiu(ids, null, Instant.now());
        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (GiayChiTiet bienThe : bienThes) {
            int ton = bienThe.getSoLuong() == null ? 0 : bienThe.getSoLuong();
            result.put(bienThe.getId(), Math.max(ton - dangGiu.getOrDefault(bienThe.getId(), 0), 0));
        }
        return result;
    }

    private Map<Integer, Integer> laySoLuongDangGiu(
            Collection<Integer> ids,
            Integer loaiTruHoaDonId,
            Instant now
    ) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        Instant mocQrConHan = now.minus(THOI_GIAN_GIU_QR);
        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (Object[] row : hoaDonChiTietRepository.tongSoLuongDangGiuTheoBienThe(
                ids, mocQrConHan, loaiTruHoaDonId)) {
            result.put(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
        }
        return result;
    }

    private Map<Integer, Integer> gomSoLuong(List<DatHangItemRequest> requests) {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        if (requests == null) {
            return result;
        }
        for (DatHangItemRequest item : requests) {
            result.merge(item.giayChiTietId(), item.soLuong(), Integer::sum);
        }
        return result;
    }
}
