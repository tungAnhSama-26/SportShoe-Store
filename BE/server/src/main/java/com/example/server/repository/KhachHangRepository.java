package com.example.server.repository;

import com.example.server.entity.KhachHang;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhachHangRepository extends JpaRepository<KhachHang, UUID> {
}
