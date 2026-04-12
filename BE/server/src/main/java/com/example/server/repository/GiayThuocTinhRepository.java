package com.example.server.repository;

import com.example.server.entity.GiayThuocTinh;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiayThuocTinhRepository extends JpaRepository<GiayThuocTinh, Integer> {

    Optional<GiayThuocTinh> findByGiayId(Integer giayId);
}
