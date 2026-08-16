package com.example.server.repository;

import com.example.server.entity    .ThanhToan;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {

    List<ThanhToan> findByHoaDonIdOrderByNgayTaoDesc(Integer hoaDonId);

    List<ThanhToan> findByHoaDonIdInOrderByNgayTaoDesc(Collection<Integer> hoaDonIds);

    List<ThanhToan> findByHoaDonIdAndHinhThucOrderByNgayTaoDesc(Integer hoaDonId, Integer hinhThuc);

    List<ThanhToan> findByTrangThaiAndLoaiGiaoDich(Integer trangThai, Integer loaiGiaoDich);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ThanhToan> findFirstByHoaDonIdAndLoaiGiaoDichAndTrangThaiOrderByNgayThanhToanDesc(
            Integer hoaDonId,
            Integer loaiGiaoDich,
            Integer trangThai
    );

    @Query("select t from ThanhToan t where t.hoaDon.id in :hoaDonIds")
    List<ThanhToan> findByHoaDonIdIn(@Param("hoaDonIds") Collection<Integer> hoaDonIds);

    boolean existsByGiaoDichGocIdAndLoaiGiaoDich(Integer giaoDichGocId, Integer loaiGiaoDich);

    Optional<ThanhToan> findByNoiDungCkAndLoaiGiaoDich(String noiDungCk, Integer loaiGiaoDich);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select t from ThanhToan t
            join fetch t.hoaDon hd
            left join fetch hd.khachHang
            left join fetch hd.phieuGiamGia
            where t.noiDungCk = :token and t.loaiGiaoDich = 1
            """)
    Optional<ThanhToan> findByTokenForUpdate(@Param("token") String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select t from ThanhToan t
            join fetch t.hoaDon hd
            left join fetch hd.khachHang
            left join fetch hd.phieuGiamGia
            where upper(t.maGiaoDich) = upper(:maGiaoDich) and t.loaiGiaoDich = 1
            """)
    Optional<ThanhToan> findByMaGiaoDichForUpdate(@Param("maGiaoDich") String maGiaoDich);

}
