package com.example.server.repository;

import com.example.server.entity.PhieuTraHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PhieuTraHangRepository extends JpaRepository<PhieuTraHang, Integer> {

    @Query("""
            select distinct p
            from PhieuTraHang p
            join fetch p.hoaDon hd
            left join fetch p.khachHang kh
            left join fetch p.nhanVien nv
            where (:keyword is null
                or lower(p.ma) like lower(concat('%', :keyword, '%'))
                or lower(hd.ma) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(hd.tenNguoiNhan, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(hd.sdtNguoiNhan, '')) like lower(concat('%', :keyword, '%')))
              and (:trangThai is null or p.trangThai = :trangThai)
            order by p.ngayTao desc, p.id desc
            """)
    List<PhieuTraHang> search(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai
    );

    @Query("""
            select p
            from PhieuTraHang p
            join fetch p.hoaDon hd
            left join fetch p.khachHang
            left join fetch p.nhanVien
            where p.id = :id
            """)
    Optional<PhieuTraHang> findDetailById(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PhieuTraHang p where p.id = :id")
    Optional<PhieuTraHang> findByIdForUpdate(@Param("id") Integer id);

    Optional<PhieuTraHang> findFirstByHoaDonIdOrderByNgayTaoDesc(Integer hoaDonId);

    List<PhieuTraHang> findByHoaDonIdInOrderByNgayTaoDesc(List<Integer> hoaDonIds);

    @Query("""
            select p.id
            from PhieuTraHang p
            where p.trangThai = :trangThai
              and p.ngayDuyet is not null
              and p.ngayDuyet < :mocQuaHan
            order by p.id
            """)
    List<Integer> findIdsQuaHanGuiHang(
            @Param("trangThai") Integer trangThai,
            @Param("mocQuaHan") Instant mocQuaHan
    );
}
