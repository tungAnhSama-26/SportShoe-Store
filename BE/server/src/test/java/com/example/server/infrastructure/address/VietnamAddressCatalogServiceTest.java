package com.example.server.infrastructure.address;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VietnamAddressCatalogServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void taiDuocSnapshotDongGoiKhiTatMang() {
        VietnamAddressCatalogService service = new VietnamAddressCatalogService(
                "http://127.0.0.1:1/api",
                temporaryDirectory.resolve("khong-ton-tai.json").toString()
        );

        var provinces = service.layDanhSachTinhThanh();
        int totalWards = provinces.stream()
                .mapToInt(province -> service.layDanhSachPhuongXa(province.code()).size())
                .sum();

        assertThat(provinces).hasSize(34);
        assertThat(totalWards).isEqualTo(3321);
        assertThat(provinces).anyMatch(value -> "79".equals(value.code())
                && "Thành phố Hồ Chí Minh".equals(value.ten()));
        assertThat(provinces.stream()
                .flatMap(value -> service.layDanhSachPhuongXa(value.code()).stream()))
                .anyMatch(value -> "đặc khu".equalsIgnoreCase(value.loai()));
    }

    @Test
    void doiSoatPhuongXaPhaiThuocDungTinh() {
        VietnamAddressCatalogService service = new VietnamAddressCatalogService(
                "http://127.0.0.1:1/api",
                temporaryDirectory.resolve("khong-ton-tai-2.json").toString()
        );
        DiaChiHaiCapRequest input = new DiaChiHaiCapRequest(
                "1",
                "Hà Nội",
                "166",
                "Phường Cầu Giấy",
                "Số 1 đường Trần Thái Tông"
        );

        var result = service.doiSoat(input);

        assertThat(result.tinhThanh().ten()).isEqualTo("Thành phố Hà Nội");
        assertThat(result.phuongXa().ten()).isEqualTo("Phường Cầu Giấy");
        assertThat(result.diaChiCuThe()).isEqualTo("Số 1 đường Trần Thái Tông");
    }
}
