package com.example.server.repository;

import com.example.server.entity.LoaiGiay;
import com.example.server.entity.enums.ActiveStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoaiGiayRepository extends JpaRepository<LoaiGiay, UUID> {

    List<LoaiGiay> findByDeletedFalseAndStatusOrderByNameAsc(ActiveStatus status);
}
