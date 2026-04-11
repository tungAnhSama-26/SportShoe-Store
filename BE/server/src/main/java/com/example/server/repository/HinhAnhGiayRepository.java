package com.example.server.repository;

import com.example.server.entity.HinhAnhGiay;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HinhAnhGiayRepository extends JpaRepository<HinhAnhGiay, Integer> {

    List<HinhAnhGiay> findByGiayChiTietIdInAndTrangThaiOrderByLaHinhChinhDescNgayTaoAsc(
            Collection<Integer> giayChiTietIds,
            Integer trangThai
    );
}
