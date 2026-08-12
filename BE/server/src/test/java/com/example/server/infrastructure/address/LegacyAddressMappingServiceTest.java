package com.example.server.infrastructure.address;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LegacyAddressMappingServiceTest {

    @TempDir
    Path temporaryDirectory;

    private LegacyAddressMappingService service;

    @BeforeEach
    void setUp() {
        String offlineUrl = "http://127.0.0.1:1/api";
        VietnamAddressCatalogService catalogService = new VietnamAddressCatalogService(
                offlineUrl,
                temporaryDirectory.resolve("address-cache.json").toString()
        );
        service = new LegacyAddressMappingService(catalogService, offlineUrl);
    }

    @Test
    void anhXaBacPhuSocSonSangXaDaPhuc() {
        DiaChiCuResponse result = service.doiChieu(
                "Thôn Yên Tàng, Bắc Phú, Sóc Sơn, Hà Nội"
        );

        assertThat(result.daAnhXa()).isTrue();
        assertThat(result.tinhThanhCode()).isEqualTo("1");
        assertThat(result.tinhThanh()).isEqualTo("Thành phố Hà Nội");
        assertThat(result.phuongXaCode()).isEqualTo("430");
        assertThat(result.phuongXa()).isEqualTo("Xã Đa Phúc");
        assertThat(result.diaChiCuThe()).isEqualTo("Thôn Yên Tàng");
    }

    @Test
    void chapNhanTenDonViCoDayDuTienTo() {
        DiaChiCuResponse result = service.doiChieu(
                "Số 15, Xã Bắc Phú, Huyện Sóc Sơn, Thành phố Hà Nội"
        );

        assertThat(result.daAnhXa()).isTrue();
        assertThat(result.phuongXa()).isEqualTo("Xã Đa Phúc");
        assertThat(result.diaChiCuThe()).isEqualTo("Số 15");
    }

    @Test
    void khongTuDoanKhiMotXaCuBiChiaThanhNhieuXaMoi() {
        DiaChiCuResponse result = service.doiChieu(
                "Số 1, Phường Cống Vị, Quận Ba Đình, Thành phố Hà Nội"
        );

        assertThat(result.daAnhXa()).isFalse();
        assertThat(result.tinhThanhCode()).isBlank();
        assertThat(result.phuongXaCode()).isBlank();
        assertThat(result.diaChiCuThe()).isEqualTo("Số 1");
        assertThat(result.thongBao()).contains("Vui lòng chọn thủ công");
    }

    @Test
    void chuoiKhongDuBaCapYeuCauChonThuCong() {
        DiaChiCuResponse result = service.doiChieu("Thôn Yên Tàng, Hà Nội");

        assertThat(result.daAnhXa()).isFalse();
        assertThat(result.diaChiCuThe()).isEqualTo("Thôn Yên Tàng, Hà Nội");
    }
}
