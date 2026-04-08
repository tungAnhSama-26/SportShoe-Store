package com.example.server.repository;

import com.example.server.entity.MauSac;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MauSacRepository extends JpaRepository<MauSac, UUID> {

    List<MauSac> findByDeletedFalseOrderByNameAsc();
}
