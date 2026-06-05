package com.example.server.repository;

import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaKhachHangResponse;
import com.example.server.entity.PhieuGiamGiaKhachHang;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PhieuGiamGiaKhachHangRepository extends JpaRepository<PhieuGiamGiaKhachHang, Integer> {

    boolean existsByPhieuGiamGiaId(Integer phieuGiamGiaId);

    long countByPhieuGiamGiaId(Integer phieuGiamGiaId);

    Optional<PhieuGiamGiaKhachHang> findByPhieuGiamGiaIdAndKhachHangId(Integer phieuGiamGiaId, UUID khachHangId);

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaKhachHangResponse(
    phieuGGKH.id,phieuGGKH.phieuGiamGia.id,phieuGGKH.khachHang.id,phieuGGKH.phieuGiamGia.ma,phieuGGKH.phieuGiamGia.ten,phieuGGKH.khachHang.hoTen,
    phieuGGKH.khachHang.email,
    phieuGGKH.ngaySuDung,phieuGGKH.trangThai,phieuGGKH.ngayTao
    )
    FROM PhieuGiamGiaKhachHang phieuGGKH JOIN KhachHang khachHang
    ON phieuGGKH.khachHang.id = khachHang.id
    ORDER BY phieuGGKH.ngayTao DESC, phieuGGKH.id DESC
""")
    List<QuanLyPhieuGiamGiaKhachHangResponse> hienThiPhieuGiamGiaKhachHang();

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaKhachHangResponse(
    phieuGGKH.id,phieuGGKH.phieuGiamGia.id,phieuGGKH.khachHang.id,phieuGGKH.phieuGiamGia.ma,phieuGGKH.phieuGiamGia.ten,phieuGGKH.khachHang.hoTen,
    phieuGGKH.khachHang.email,
    phieuGGKH.ngaySuDung,phieuGGKH.trangThai,phieuGGKH.ngayTao
    )
    FROM PhieuGiamGiaKhachHang phieuGGKH JOIN KhachHang khachHang
    ON phieuGGKH.khachHang.id = khachHang.id
    WHERE phieuGGKH.id = ?1
""")
    QuanLyPhieuGiamGiaKhachHangResponse detailPhieuGiamGiaKhachHang(Integer id);

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaKhachHangResponse(
    phieuGGKH.id,phieuGGKH.phieuGiamGia.id,phieuGGKH.khachHang.id,phieuGGKH.phieuGiamGia.ma,phieuGGKH.phieuGiamGia.ten,phieuGGKH.khachHang.hoTen,
    phieuGGKH.khachHang.email,
    phieuGGKH.ngaySuDung,phieuGGKH.trangThai,phieuGGKH.ngayTao
    )
    FROM PhieuGiamGiaKhachHang phieuGGKH JOIN KhachHang khachHang
    ON phieuGGKH.khachHang.id = khachHang.id
    ORDER BY phieuGGKH.ngayTao DESC, phieuGGKH.id DESC
""")
    Page<QuanLyPhieuGiamGiaKhachHangResponse> phantrangPhieuGiamGiaKhachHang(Pageable pageable);

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaKhachHangResponse(
    phieuGGKH.id,phieuGGKH.phieuGiamGia.id,phieuGGKH.khachHang.id,phieuGGKH.phieuGiamGia.ma,phieuGGKH.phieuGiamGia.ten,phieuGGKH.khachHang.hoTen,
    phieuGGKH.khachHang.email,
    phieuGGKH.ngaySuDung,phieuGGKH.trangThai,phieuGGKH.ngayTao
    )
    FROM PhieuGiamGiaKhachHang phieuGGKH JOIN KhachHang khachHang
    ON phieuGGKH.khachHang.id = khachHang.id
    WHERE (:keyword IS NULL OR LOWER(phieuGGKH.phieuGiamGia.ma) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(phieuGGKH.phieuGiamGia.ten) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(khachHang.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:trangThai IS NULL OR phieuGGKH.trangThai = :trangThai)
    ORDER BY phieuGGKH.ngayTao DESC, phieuGGKH.id DESC
""")
    Page<QuanLyPhieuGiamGiaKhachHangResponse> timKiemVaPhanTrang(
            @Param("keyword") String keyword, 
            @Param("trangThai") Integer trangThai, 
            Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE PhieuGiamGiaKhachHang pgk
        SET pgk.trangThai = (
            SELECT pg.trangThai FROM PhieuGiamGia pg WHERE pg.id = pgk.phieuGiamGia.id
        )
        WHERE (
            (SELECT pg.trangThai FROM PhieuGiamGia pg WHERE pg.id = pgk.phieuGiamGia.id) = 2
            AND pgk.trangThai != 2
        ) OR (
            pgk.trangThai != 0 
            AND pgk.trangThai != (
                SELECT pg.trangThai FROM PhieuGiamGia pg WHERE pg.id = pgk.phieuGiamGia.id
            )
        )
    """)
    void dongBoTrangThaiTuPhieuGiamGia();

    /**
     * Lấy tất cả liên kết khách hàng của một phiếu giảm giá (dùng để gửi email khi phiếu được cập nhật).
     */
    @Query("""
        SELECT pgk FROM PhieuGiamGiaKhachHang pgk
        JOIN FETCH pgk.khachHang
        JOIN FETCH pgk.phieuGiamGia
        WHERE pgk.phieuGiamGia.id = :phieuGiamGiaId
    """)
    List<PhieuGiamGiaKhachHang> findAllByPhieuGiamGiaId(@Param("phieuGiamGiaId") Integer phieuGiamGiaId);

    /**
     * Lấy các liên kết đã hết hạn (trangThai = 2) mà khách hàng chưa sử dụng (ngaySuDung IS NULL).
     * Dùng cho scheduler để gửi email thông báo hết hạn.
     */
    @Query("""
        SELECT pgk FROM PhieuGiamGiaKhachHang pgk
        JOIN FETCH pgk.khachHang
        JOIN FETCH pgk.phieuGiamGia
        WHERE pgk.trangThai = 2
          AND pgk.ngaySuDung IS NULL
    """)
    List<PhieuGiamGiaKhachHang> findExpiredUnused();
}
