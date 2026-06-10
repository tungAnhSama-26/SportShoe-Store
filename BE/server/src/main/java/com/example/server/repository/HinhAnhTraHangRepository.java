package com.example.server.repository;

import com.example.server.entity.HinhAnhTraHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HinhAnhTraHangRepository extends JpaRepository<HinhAnhTraHang, Integer> {

    List<HinhAnhTraHang> findByPhieuTraHangIdOrderByNgayTaoAsc(Integer phieuTraHangId);
}
