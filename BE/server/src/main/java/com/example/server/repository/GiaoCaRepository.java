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

    Optional<GiaoCa> findFirstByNhanVienTrongCaIdAndTrangThaiInOrderByThoiGianVaoDesc(UUID nhanVienId, List<String> trangThai);

    boolean existsByTrangThaiIn(List<String> trangThai);

    boolean existsByNhanVienTrongCaIdAndTrangThaiIn(UUID nhanVienId, List<String> trangThai);

    Optional<GiaoCa> findFirstByTrangThaiInOrderByThoiGianVaoDesc(List<String> trangThai);

    List<GiaoCa> findByNhanVienNhanIdAndTrangThaiOrderByThoiGianVaoDesc(UUID nhanVienId, String trangThai);

    @Query("SELECT g FROM GiaoCa g WHERE " +
           "(:nhanVienId IS NULL OR g.nhanVienTrongCa.id = :nhanVienId) AND " +
           "(:trangThai IS NULL OR g.trangThai = :trangThai) AND " +
           "(:tuNgay IS NULL OR g.thoiGianVao >= :tuNgay) AND " +
           "(:denNgay IS NULL OR g.thoiGianVao <= :denNgay)")
    Page<GiaoCa> searchHistory(
            @Param("nhanVienId") UUID nhanVienId,
            @Param("trangThai") String trangThai,
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(CASE WHEN t.loaiGiaoDich = 1 THEN t.soTien ELSE -t.soTien END), 0) " +
           "FROM ThanhToan t JOIN t.hoaDon h " +
           "WHERE h.giaoCa.id = :giaoCaId " +
           "AND t.trangThai = 1 " +
           "AND h.kenhBan = 1 " +
           "AND t.hinhThuc = 1")
    BigDecimal calculateTienMatTrongCa(
            @Param("giaoCaId") UUID giaoCaId
    );

    @Query("SELECT COALESCE(SUM(CASE WHEN t.loaiGiaoDich = 1 THEN t.soTien ELSE -t.soTien END), 0) " +
           "FROM ThanhToan t JOIN t.hoaDon h " +
           "WHERE h.giaoCa.id = :giaoCaId " +
           "AND t.trangThai = 1 " +
           "AND h.kenhBan = 1 " +
           "AND t.hinhThuc != 1")
    BigDecimal calculateTienChuyenKhoanTrongCa(
            @Param("giaoCaId") UUID giaoCaId
    );
}
