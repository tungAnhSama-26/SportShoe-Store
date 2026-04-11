package com.example.server.repository;

import com.example.server.entity.VanChuyen;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VanChuyenRepository extends JpaRepository<VanChuyen, Integer> {

    Optional<VanChuyen> findByHoaDonId(Integer hoaDonId);

    List<VanChuyen> findByHoaDonIdIn(Collection<Integer> hoaDonIds);
}
