package com.example.server.repository;

import com.example.server.entity.ThuongHieu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {

    List<ThuongHieu> findByTrangThaiOrderByTenAsc(Integer trangThai);
}
