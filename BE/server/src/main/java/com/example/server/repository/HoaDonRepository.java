package com.example.server.repository;

import com.example.server.entity.HoaDon;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    List<HoaDon> findTop10ByKenhBanAndTrangThaiOrderByNgayTaoDesc(Integer kenhBan, Integer trangThai);
}
