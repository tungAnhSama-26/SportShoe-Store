package com.example.server.repository;

import com.example.server.entity.DotGiamGia;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, UUID> {

    Optional<DotGiamGia> findByIdAndDeletedFalse(UUID id);
}
