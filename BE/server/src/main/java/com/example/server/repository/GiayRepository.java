package com.example.server.repository;

import com.example.server.entity.Giay;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GiayRepository extends JpaRepository<Giay, UUID>, JpaSpecificationExecutor<Giay> {

    @EntityGraph(attributePaths = {"thuongHieu", "loaiGiay", "chatLieu", "dotGiamGia"})
    Optional<Giay> findByIdAndDeletedFalse(UUID id);
}
