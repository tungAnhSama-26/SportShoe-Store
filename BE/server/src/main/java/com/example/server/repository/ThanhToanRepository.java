package com.example.server.repository;

import com.example.server.entity.ThanhToan;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {

    List<ThanhToan> findByHoaDonIdOrderByNgayTaoDesc(Integer hoaDonId);

    List<ThanhToan> findByHoaDonIdInOrderByNgayTaoDesc(Collection<Integer> hoaDonIds);

    List<ThanhToan> findByHoaDonIdAndHinhThucOrderByNgayTaoDesc(Integer hoaDonId, Integer hinhThuc);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ThanhToan> findFirstByHoaDonIdAndLoaiGiaoDichAndTrangThaiOrderByNgayThanhToanDesc(
            Integer hoaDonId,
            Integer loaiGiaoDich,
            Integer trangThai
    );

    @Query("select t from ThanhToan t where t.hoaDon.id in :hoaDonIds")
    List<ThanhToan> findByHoaDonIdIn(@Param("hoaDonIds") Collection<Integer> hoaDonIds);

    boolean existsByGiaoDichGocIdAndLoaiGiaoDich(Integer giaoDichGocId, Integer loaiGiaoDich);

    boolean existsByPhieuTraHangIdAndLoaiGiaoDich(Integer phieuTraHangId, Integer loaiGiaoDich);
}
