package com.example.server.repository;

import com.example.server.entity.KhachHang;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KhachHangRepository extends JpaRepository<KhachHang, UUID> {

    @Query("""
            select kh
            from KhachHang kh
            where kh.trangThai = 1
              and (
                :keyword is null
                or kh.sdt like concat('%', :keyword, '%')
                or lower(kh.hoTen) like lower(concat('%', :keyword, '%'))
              )
            order by kh.ngayTao desc
            """)
    List<KhachHang> searchByKeyword(@Param("keyword") String keyword);

    @Query("""
            select count(kh)
            from KhachHang kh
            where kh.trangThai = 1
              and (:fromDate is null or kh.ngayTao >= :fromDate)
              and (:toDate is null or kh.ngayTao < :toDate)
            """)
    long countNewCustomers(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    List<KhachHang> findByTrangThai(Integer trangThai);

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByEmail(String email);

    Optional<KhachHang> findByEmail(String email);

    @Query("SELECT kh.email FROM KhachHang kh WHERE kh.email IS NOT NULL AND kh.trangThai = 1")
    List<String> findAllActiveEmails();
}
