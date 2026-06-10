package com.example.server.repository;

import com.example.server.entity.ThanhToan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {

    List<ThanhToan> findByHoaDonIdOrderByNgayTaoDesc(Integer hoaDonId);

    List<ThanhToan> findByHoaDonIdAndHinhThucOrderByNgayTaoDesc(Integer hoaDonId, Integer hinhThuc);

    Optional<ThanhToan> findFirstByHoaDonIdAndLoaiGiaoDichAndTrangThaiOrderByNgayThanhToanDesc(
            Integer hoaDonId,
            Integer loaiGiaoDich,
            Integer trangThai
    );
}
