package com.example.server.repository;

import com.example.server.entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LichLamViecRepository extends JpaRepository<LichLamViec, UUID> {
    List<LichLamViec> findByNgayBetween(LocalDate tuNgay, LocalDate denNgay);
    Optional<LichLamViec> findByNhanVienIdAndNgay(UUID nhanVienId, LocalDate ngay);
    long countByNgayAndCa(LocalDate ngay, String ca);
    boolean existsByNhanVienIdAndNgayAndCa(UUID nhanVienId, LocalDate ngay, String ca);
    void deleteByNgayBetween(LocalDate tuNgay, LocalDate denNgay);
}
