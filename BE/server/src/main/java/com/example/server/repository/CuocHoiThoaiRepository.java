package com.example.server.repository;

import com.example.server.entity.CuocHoiThoai;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CuocHoiThoaiRepository extends JpaRepository<CuocHoiThoai, Integer> {
    List<CuocHoiThoai> findByTrangThaiInOrderByNgayTaoDesc(List<Integer> trangThais);
    List<CuocHoiThoai> findByNhanVienIdAndTrangThai(java.util.UUID nhanVienId, Integer trangThai);
    List<CuocHoiThoai> findByNhanVienIdOrderByNgayTaoDesc(java.util.UUID nhanVienId);
}
