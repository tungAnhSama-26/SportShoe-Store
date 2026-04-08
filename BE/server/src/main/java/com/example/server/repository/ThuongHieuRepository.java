package com.example.server.repository;

import com.example.server.entity.ThuongHieu;
import com.example.server.entity.enums.ActiveStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, UUID> {

    List<ThuongHieu> findByDeletedFalseAndStatusOrderByNameAsc(ActiveStatus status);
}
