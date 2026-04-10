package com.example.server.repository;

import com.example.server.entity.PhieuGiamGiaKhachHang;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhieuGiamGiaKhachHangRepository extends JpaRepository<PhieuGiamGiaKhachHang, Integer> {

    boolean existsByPhieuGiamGiaId(Integer phieuGiamGiaId);

    Optional<PhieuGiamGiaKhachHang> findByPhieuGiamGiaIdAndKhachHangId(Integer phieuGiamGiaId, UUID khachHangId);
}
