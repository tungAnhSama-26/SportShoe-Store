package com.example.server.repository;

import com.example.server.entity.LichSuHoaDon;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Integer> {
    List<LichSuHoaDon> findByHoaDonIdOrderByNgayTaoDesc(Integer hoaDonId);

    List<LichSuHoaDon> findByHoaDonIdInOrderByNgayTaoDesc(Collection<Integer> hoaDonIds);

    boolean existsByHoaDonIdAndTrangThai(Integer hoaDonId, String trangThai);

    Optional<LichSuHoaDon> findFirstByHoaDonIdAndTrangThaiInOrderByNgayTaoDescIdDesc(
            Integer hoaDonId,
            Collection<String> trangThai
    );
}
