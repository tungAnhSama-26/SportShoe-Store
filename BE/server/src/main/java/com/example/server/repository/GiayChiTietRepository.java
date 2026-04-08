package com.example.server.repository;

import com.example.server.entity.GiayChiTiet;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiayChiTietRepository extends JpaRepository<GiayChiTiet, UUID> {

    @EntityGraph(attributePaths = {"mauSac", "kichCo"})
    List<GiayChiTiet> findByGiayIdAndDeletedFalseOrderByCreatedAtAsc(UUID productId);
}
