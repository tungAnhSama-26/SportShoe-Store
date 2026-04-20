package com.example.server.repository;

import com.example.server.entity.GiayChiTiet;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GiayChiTietRepository extends JpaRepository<GiayChiTiet, Integer> {

    boolean existsByGiayIdAndMauSacIdAndKichCoId(Integer giayId, Integer mauSacId, Integer kichCoId);

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

    @Query("""
            select gct from GiayChiTiet gct
            join fetch gct.mauSac
            join fetch gct.kichCo
            where gct.giay.id = :giayId
            order by gct.id asc
            """)
    List<GiayChiTiet> findByGiayIdEager(@Param("giayId") Integer giayId);

    @Query("""
            select gct.giay.id, min(gct.giaBan), count(gct), sum(gct.soLuong), max(gct.giaBan),
                   sum(case when gct.giaBan < gct.giaGoc then 1 else 0 end)
            from GiayChiTiet gct
            where gct.giay.id in :ids and (gct.kichHoat = 1 or gct.kichHoat = 2)
            group by gct.giay.id
            """)
    List<Object[]> aggregateByGiayIds(@Param("ids") Collection<Integer> ids);

    @Query(
            value = """
                    select gct
                    from GiayChiTiet gct
                    join fetch gct.giay g
                    join fetch g.thuongHieu th
                    join fetch g.loaiGiay lg
                    join fetch gct.mauSac ms
                    join fetch gct.kichCo kc
                    where (:keyword is null
                        or lower(g.ma) like lower(concat('%', :keyword, '%'))
                        or lower(g.ten) like lower(concat('%', :keyword, '%'))
                        or lower(gct.maBienThe) like lower(concat('%', :keyword, '%'))
                        or lower(gct.sku) like lower(concat('%', :keyword, '%')))
                      and (:giayId is null or g.id = :giayId)
                      and (:mauSacId is null or ms.id = :mauSacId)
                      and (:kichCoId is null or kc.id = :kichCoId)
                      and (
                        :trangThai is null
                        or (:trangThai = 1 and gct.kichHoat = 1 and gct.soLuong > 0)
                        or (:trangThai = 2 and (gct.kichHoat <> 1 or gct.soLuong <= 0))
                      )
                    """,
            countQuery = """
                    select count(gct)
                    from GiayChiTiet gct
                    join gct.giay g
                    join gct.mauSac ms
                    join gct.kichCo kc
                    where (:keyword is null
                        or lower(g.ma) like lower(concat('%', :keyword, '%'))
                        or lower(g.ten) like lower(concat('%', :keyword, '%'))
                        or lower(gct.maBienThe) like lower(concat('%', :keyword, '%'))
                        or lower(gct.sku) like lower(concat('%', :keyword, '%')))
                      and (:giayId is null or g.id = :giayId)
                      and (:mauSacId is null or ms.id = :mauSacId)
                      and (:kichCoId is null or kc.id = :kichCoId)
                      and (
                        :trangThai is null
                        or (:trangThai = 1 and gct.kichHoat = 1 and gct.soLuong > 0)
                        or (:trangThai = 2 and (gct.kichHoat <> 1 or gct.soLuong <= 0))
                      )
                    """
    )
    Page<GiayChiTiet> findAdminChiTietPage(
            @Param("keyword") String keyword,
            @Param("giayId") Integer giayId,
            @Param("mauSacId") Integer mauSacId,
            @Param("kichCoId") Integer kichCoId,
            @Param("trangThai") Integer trangThai,
            Pageable pageable
    );

    @Query("select sum(gct.soLuong) from GiayChiTiet gct where gct.giay.id = :giayId and (gct.kichHoat = 1 or gct.kichHoat = 2)")
    Long sumSoLuongByGiayId(@Param("giayId") Integer giayId);
}
