package com.example.server.repository;

import com.example.server.entity.LichSuPhieuTraHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LichSuPhieuTraHangRepository extends JpaRepository<LichSuPhieuTraHang, Integer> {

    List<LichSuPhieuTraHang> findByPhieuTraHangIdOrderByNgayTaoAsc(Integer phieuTraHangId);
}
