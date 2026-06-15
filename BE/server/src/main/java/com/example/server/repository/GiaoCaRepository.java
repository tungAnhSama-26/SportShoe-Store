package com.example.server.repository;

import com.example.server.entity.GiaoCa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GiaoCaRepository extends JpaRepository<GiaoCa, UUID> {

    Optional<GiaoCa> findByNhanVienTrongCaIdAndTrangThai(UUID nhanVienId, String trangThai);

    List<GiaoCa> findByNhanVienNhanIdAndTrangThaiOrderByThoiGianVaoDesc(UUID nhanVienId, String trangThai);

    @Query("SELECT g FROM GiaoCa g WHERE " +
           "(:nhanVienId IS NULL OR g.nhanVienTrongCa.id = :nhanVienId) AND " +
           "(:trangThai IS NULL OR g.trangThai = :trangThai) AND " +
           "(:tuNgay IS NULL OR g.thoiGianVao >= :tuNgay) AND " +
           "(:denNgay IS NULL OR g.thoiGianVao <= :denNgay) " +
           "ORDER BY g.thoiGianVao DESC")
    Page<GiaoCa> searchHistory(
            @Param("nhanVienId") UUID nhanVienId,
            @Param("trangThai") String trangThai,
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(CASE WHEN t.loaiGiaoDich = 1 THEN t.soTien ELSE -t.soTien END), 0) " +
           "FROM ThanhToan t JOIN t.hoaDon h " +
           "WHERE t.nhanVien.id = :nhanVienId " +
           "AND t.trangThai = 1 " +
           "AND h.kenhBan = 1 " +
           "AND t.hinhThuc = 1 " + // Cash
           "AND t.ngayTao >= :thoiGianVao " +
           "AND (t.ngayTao <= :thoiGianRa OR :thoiGianRa IS NULL)")
    BigDecimal calculateTienMatTrongCa(
            @Param("nhanVienId") UUID nhanVienId,
            @Param("thoiGianVao") Instant thoiGianVao,
            @Param("thoiGianRa") Instant thoiGianRa
    );

    @Query("SELECT COALESCE(SUM(CASE WHEN t.loaiGiaoDich = 1 THEN t.soTien ELSE -t.soTien END), 0) " +
           "FROM ThanhToan t JOIN t.hoaDon h " +
           "WHERE t.nhanVien.id = :nhanVienId " +
           "AND t.trangThai = 1 " +
           "AND h.kenhBan = 1 " +
           "AND t.hinhThuc != 1 " + // Cashless
           "AND t.ngayTao >= :thoiGianVao " +
           "AND (t.ngayTao <= :thoiGianRa OR :thoiGianRa IS NULL)")
    BigDecimal calculateTienChuyenKhoanTrongCa(
            @Param("nhanVienId") UUID nhanVienId,
            @Param("thoiGianVao") Instant thoiGianVao,
            @Param("thoiGianRa") Instant thoiGianRa
    );
}
