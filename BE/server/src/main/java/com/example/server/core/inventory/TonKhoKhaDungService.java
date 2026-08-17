package com.example.server.core.inventory;

import com.example.server.core.client.dathang.dto.DatHangItemRequest;
import com.example.server.entity.GiayChiTiet;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * @deprecated Đã gỡ bỏ cơ chế giữ hàng online theo yêu cầu.
 */
@Service
@Deprecated
public class TonKhoKhaDungService {

    public static final Duration THOI_GIAN_GIU_QR = Duration.ofMinutes(15);

    public TonKhoKhaDungService() {
    }

    public Map<Integer, GiayChiTiet> khoaVaKiemTra(
            List<DatHangItemRequest> requests,
            Integer loaiTruHoaDonId
    ) {
        return Collections.emptyMap();
    }

    public Map<Integer, Integer> laySoLuongKhaDung(Collection<GiayChiTiet> bienThes) {
        if (bienThes == null || bienThes.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (GiayChiTiet bienThe : bienThes) {
            if (bienThe == null || bienThe.getId() == null) continue;
            int ton = bienThe.getSoLuong() == null ? 0 : bienThe.getSoLuong();
            result.put(bienThe.getId(), ton);
        }
        return result;
    }
}
