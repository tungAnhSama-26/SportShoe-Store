package com.example.server.repository;

import com.example.server.entity.TinNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TinNhanRepository extends JpaRepository<TinNhan, Integer> {
    List<TinNhan> findByCuocHoiThoaiIdOrderByNgayTaoAsc(Integer cuocHoiThoaiId);
}
