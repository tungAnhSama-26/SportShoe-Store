package com.example.server.repository;

import com.example.server.entity.DiaChiKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Integer> {
    List<DiaChiKhachHang> findByKhachHangIdOrderByLaMacDinhDesc(UUID khachHangId);
}
