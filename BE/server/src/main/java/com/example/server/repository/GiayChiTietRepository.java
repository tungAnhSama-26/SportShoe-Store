package com.example.server.repository;

import com.example.server.entity.GiayChiTiet;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface GiayChiTietRepository extends JpaRepository<GiayChiTiet, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select gct from GiayChiTiet gct join fetch gct.giay where gct.id = :id")
    Optional<GiayChiTiet> findByIdForUpdate(@Param("id") Integer id);

    boolean existsByGiayIdAndMauSacIdAndKichCoId(Integer giayId, Integer mauSacId, Integer kichCoId);

    /** Sản phẩm còn ít nhất 1 biến thể đang bán (kichHoat=1) hay không. */
    boolean existsByGiayIdAndKichHoat(Integer giayId, Integer kichHoat);

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
              and gct.soLuong >= 0
              and g.trangThai = 1
              and th.trangThai = 1
              and lg.trangThai = 1
              and ms.trangThai = 1
              and kc.trangThai = 1
              and (dg is null or dg.trangThai = 1)
              and (cg is null or cg.trangThai = 1)
              and (cnd is null or cnd.trangThai = 1)
              and (tl is null or tl.trangThai = 1)
              and (
                :keyword is null
                or lower(g.ma) like lower(concat('%', :keyword, '%'))
                or lower(g.ten) like lower(concat('%', :keyword, '%'))
                or lower(gct.sku) like lower(concat('%', :keyword, '%'))
                or lower(gct.maBienThe) like lower(concat('%', :keyword, '%'))
              )
            order by gct.id desc, g.ten asc
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
                   sum(case when gct.giaBan < gct.giaGoc then 1 else 0 end),
                   min(gct.giaGoc), max(gct.giaGoc)
            from GiayChiTiet gct
            where gct.giay.id in :ids and (gct.kichHoat = 1 or gct.kichHoat = 0)
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
                        or (:trangThai = 2 and gct.kichHoat = 1 and gct.soLuong <= 0)
                        or (:trangThai = 0 and gct.kichHoat = 0)
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
                        or (:trangThai = 2 and gct.kichHoat = 1 and gct.soLuong <= 0)
                        or (:trangThai = 0 and gct.kichHoat = 0)
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

    @Query("select sum(gct.soLuong) from GiayChiTiet gct where gct.giay.id = :giayId and (gct.kichHoat = 1 or gct.kichHoat = 0)")
    Long sumSoLuongByGiayId(@Param("giayId") Integer giayId);

    /** (giayId, tên màu) của các biến thể đang bán - dùng để lọc màu sắc phía khách hàng. */
    @Query("select distinct gct.giay.id, ms.ten from GiayChiTiet gct join gct.mauSac ms "
            + "where gct.giay.id in :ids and gct.kichHoat = 1")
    List<Object[]> findMauSacByGiayIds(@Param("ids") Collection<Integer> ids);

    /** (giayId, giá trị size) của các biến thể đang bán - dùng để lọc kích cỡ phía khách hàng. */
    @Query("select distinct gct.giay.id, kc.giaTri from GiayChiTiet gct join gct.kichCo kc "
            + "where gct.giay.id in :ids and gct.kichHoat = 1")
    List<Object[]> findKichCoByGiayIds(@Param("ids") Collection<Integer> ids);

    /** Biến thể đang bán của nhiều sản phẩm - dùng để tính giá sau giảm cho danh sách. */
    @Query("select gct from GiayChiTiet gct join fetch gct.giay where gct.giay.id in :ids and gct.kichHoat = 1")
    List<GiayChiTiet> findActiveByGiayIds(@Param("ids") Collection<Integer> ids);

    @Modifying
    @Transactional
    @Query("""
        UPDATE GiayChiTiet gct
        SET gct.ngayCapNhat = :now
        WHERE gct.id IN (
            SELECT link.giayChiTiet.id
            FROM DotGiamGiaSanPham link
            JOIN link.dotGiamGia d
            WHERE d.kichHoat != 0
              AND d.kichHoat != CASE
                  WHEN d.ngayKetThuc < :today THEN 2
                  WHEN d.ngayBatDau > :today THEN 4
                  ELSE 1
              END
        )
    """)
    int touchAffectedVariants(@Param("today") java.time.LocalDate today, @Param("now") java.time.Instant now);
}
