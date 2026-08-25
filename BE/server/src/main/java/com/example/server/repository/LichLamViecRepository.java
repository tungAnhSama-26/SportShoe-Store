package com.example.server.repository;

import com.example.server.entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LichLamViecRepository extends JpaRepository<LichLamViec, UUID> {
    List<LichLamViec> findByNgayBetween(LocalDate tuNgay, LocalDate denNgay);
    List<LichLamViec> findByNhanVienIdAndNgay(UUID nhanVienId, LocalDate ngay);
    List<LichLamViec> findByNgayAndCaLamId(LocalDate ngay, String caLamId);
    boolean existsByNhanVienIdAndNgayAndCaLamId(UUID nhanVienId, LocalDate ngay, String caLamId);
    boolean existsByCaLamIdAndNgayGreaterThanEqual(String caLamId, LocalDate ngay);
    long countByNgayBetween(LocalDate tuNgay, LocalDate denNgay);
    void deleteByNgayBetween(LocalDate tuNgay, LocalDate denNgay);
}
