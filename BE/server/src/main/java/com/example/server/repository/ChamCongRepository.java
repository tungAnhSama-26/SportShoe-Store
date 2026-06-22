package com.example.server.repository;

import com.example.server.entity.ChamCong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChamCongRepository extends JpaRepository<ChamCong, UUID> {
    Optional<ChamCong> findByNhanVienIdAndNgayAndCa(UUID nhanVienId, LocalDate ngay, String ca);
    List<ChamCong> findByNgayBetween(LocalDate tuNgay, LocalDate denNgay);
    boolean existsByLichLamViecId(UUID lichLamViecId);
    boolean existsByNgayBetween(LocalDate tuNgay, LocalDate denNgay);
    List<ChamCong> findByNhanVienIdAndThoiGianRaIsNull(UUID nhanVienId);
    List<ChamCong> findByNgayBetweenAndThoiGianRaIsNull(LocalDate tuNgay, LocalDate denNgay);
}
