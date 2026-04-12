package com.example.server.repository;

import com.example.server.entity.HinhAnhGiay;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HinhAnhGiayRepository extends JpaRepository<HinhAnhGiay, Integer> {

    List<HinhAnhGiay> findByGiayChiTietIdInAndTrangThaiOrderByLaHinhChinhDescNgayTaoAsc(
            Collection<Integer> giayChiTietIds,
            Integer trangThai
    );

    List<HinhAnhGiay> findByGiayChiTietIdAndTrangThaiOrderByLaHinhChinhDescNgayTaoAsc(
            Integer giayChiTietId,
            Integer trangThai
    );

    @Query("""
            SELECT gct.giay.id, h.url
            FROM HinhAnhGiay h
            JOIN h.giayChiTiet gct
            WHERE gct.giay.id IN :ids
              AND h.laHinhChinh = true
              AND h.trangThai = 1
            """)
    List<Object[]> findMainImageUrlsByGiayIds(@Param("ids") Collection<Integer> ids);
}
