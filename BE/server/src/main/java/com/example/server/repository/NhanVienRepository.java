package com.example.server.repository;

import com.example.server.entity.NhanVien;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhanVienRepository extends JpaRepository<NhanVien, UUID> {
}
