package com.example.server.repository;

import com.example.server.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NhanVienRepository extends JpaRepository<NhanVien, UUID> {
    boolean existsByMa(String ma);
    Optional<NhanVien> findByMa(String ma);
    boolean existsByEmail(String email);
    boolean existsByTenDangNhapIgnoreCase(String tenDangNhap);
    Optional<NhanVien> findByEmail(String email);
    Optional<NhanVien> findByTenDangNhapIgnoreCase(String tenDangNhap);
}
