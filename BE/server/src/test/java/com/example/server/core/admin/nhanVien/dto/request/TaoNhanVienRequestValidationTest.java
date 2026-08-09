package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TaoNhanVienRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void chapNhanNhanVienKhongCoHinhAnh() {
        TaoNhanVienRequest request = taoRequest(null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void chapNhanNhanVienCoHinhAnhRong() {
        TaoNhanVienRequest request = taoRequest("");

        assertThat(validator.validate(request)).isEmpty();
    }

    private TaoNhanVienRequest taoRequest(String hinhAnh) {
        return new TaoNhanVienRequest(
                "Nguyen Van A",
                "nhanvien@example.com",
                "0912345678",
                "Nam",
                LocalDate.of(2000, 1, 1),
                null,
                hinhAnh,
                2,
                null
        );
    }
}
