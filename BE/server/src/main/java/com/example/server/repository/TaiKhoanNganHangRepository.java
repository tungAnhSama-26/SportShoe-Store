package com.example.server.repository;

import com.example.server.entity.TaiKhoanNganHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaiKhoanNganHangRepository extends JpaRepository<TaiKhoanNganHang, Integer> {

    List<TaiKhoanNganHang> findByKhachHangIdOrderByLaMacDinhDescNgayTaoDesc(UUID khachHangId);

    Optional<TaiKhoanNganHang> findByIdAndKhachHangId(Integer id, UUID khachHangId);

    Optional<TaiKhoanNganHang> findByKhachHangIdAndLaMacDinhTrue(UUID khachHangId);
}
