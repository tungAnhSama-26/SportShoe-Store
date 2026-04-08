package com.example.server.repository;

import com.example.server.entity.HinhAnhGiay;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HinhAnhGiayRepository extends JpaRepository<HinhAnhGiay, UUID> {

    List<HinhAnhGiay> findByGiayChiTietIdInAndDeletedFalseOrderByPrimaryImageDescCreatedAtAsc(List<UUID> variantIds);
}
