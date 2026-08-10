package com.example.server.infrastructure.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.server.entity.DiaChiHaiCap;
import com.example.server.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.Test;

class DiaChiHaiCapMapperTest {

    @Test
    void chuyenDoiVaDinhDangDiaChiHaiCap() {
        DiaChiHaiCapRequest request = new DiaChiHaiCapRequest(
                "201", "Hà Nội", "10001", "Phường Dịch Vọng", "Số 1 đường ABC"
        );

        DiaChiHaiCap entity = DiaChiHaiCapMapper.toEntity(request);
        DiaChiHaiCapResponse response = DiaChiHaiCapMapper.toResponse(entity);

        assertThat(response.tinhThanhCode()).isNull();
        assertThat(response.phuongXaCode()).isNull();
        assertThat(response.diaChiDayDu()).isEqualTo("Số 1 đường ABC, Phường Dịch Vọng, Hà Nội");
    }

    @Test
    void khongChapNhanDiaChiThieuPhuongXa() {
        DiaChiHaiCapRequest request = new DiaChiHaiCapRequest(
                null, "Hà Nội", null, " ", "Số 1 đường ABC"
        );

        assertThatThrownBy(() -> DiaChiHaiCapMapper.toEntity(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void soSanhDiaChiKhongPhanBietKhoangTrangVaChuHoa() {
        DiaChiHaiCap left = DiaChiHaiCapMapper.toEntity(new DiaChiHaiCapRequest(
                "201", "Hà Nội", "10001", "Dịch Vọng", "Số 1 đường ABC"
        ));
        DiaChiHaiCap right = DiaChiHaiCapMapper.toEntity(new DiaChiHaiCapRequest(
                "999", "HÀ NỘI", "99999", "dịch vọng", "số 1 đường abc"
        ));

        assertThat(DiaChiHaiCapMapper.same(left, right)).isTrue();
    }
}
