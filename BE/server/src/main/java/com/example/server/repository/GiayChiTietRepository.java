package com.example.server.repository;

import com.example.server.entity.GiayChiTiet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GiayChiTietRepository extends JpaRepository<GiayChiTiet, Integer> {

    @Query("""
            select gct
            from GiayChiTiet gct
            join fetch gct.giay g
            join fetch g.thuongHieu th
            join fetch g.loaiGiay lg
            join fetch gct.mauSac ms
            join fetch gct.kichCo kc
            left join fetch g.giayThuocTinh gtt
            left join fetch gtt.deGiay dg
            left join fetch gtt.coGiay cg
            left join fetch gtt.congNgheDem cnd
            left join fetch gtt.trongLuong tl
            where gct.kichHoat = 1
              and g.trangThai = 1
              and (
                :keyword is null
                or lower(g.ma) like lower(concat('%', :keyword, '%'))
                or lower(g.ten) like lower(concat('%', :keyword, '%'))
                or lower(gct.sku) like lower(concat('%', :keyword, '%'))
                or lower(gct.maBienThe) like lower(concat('%', :keyword, '%'))
              )
            order by g.ten asc, gct.sku asc
            """)
    List<GiayChiTiet> searchForCounterSale(@Param("keyword") String keyword);

    @Query("""
            from GiayChiTiet gct
            join fetch gct.giay g
            join fetch g.thuongHieu th
            where gct.kichHoat = 1
              and g.trangThai = 1
            order by g.ten asc
            """)
    List<GiayChiTiet> findAllForThongKe();
}
