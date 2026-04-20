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
            SELECT h.giayChiTiet.giay.id, h.url
            FROM HinhAnhGiay h
            WHERE h.giayChiTiet.giay.id IN :ids
              AND h.trangThai = 1
              AND h.url IS NOT NULL AND h.url <> ''
            ORDER BY h.laHinhChinh DESC, h.id DESC
            """)
    List<Object[]> findMainImageUrlsByGiayIds(@Param("ids") Collection<Integer> ids);

    @Query("""
            SELECT h.giayChiTiet.id, h.url
            FROM HinhAnhGiay h
            WHERE h.giayChiTiet.id IN :ids
              AND h.trangThai = 1
              AND h.url IS NOT NULL AND h.url <> ''
            ORDER BY h.laHinhChinh DESC, h.id DESC
            """)
    List<Object[]> findMainImageUrlsByGiayChiTietIds(@Param("ids") Collection<Integer> ids);
}
