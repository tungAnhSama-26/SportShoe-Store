package com.example.server.repository;

import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaResponse;
import com.example.server.entity.DotGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, Integer> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE DotGiamGia d 
        SET d.kichHoat = CASE 
            WHEN d.ngayKetThuc <= CURRENT_DATE THEN 2 
            WHEN d.ngayBatDau > CURRENT_DATE THEN 4 
            ELSE 1 
        END 
        WHERE d.kichHoat != 0 
        AND d.kichHoat != CASE 
            WHEN d.ngayKetThuc <= CURRENT_DATE THEN 2 
            WHEN d.ngayBatDau > CURRENT_DATE THEN 4 
            ELSE 1 
        END
    """)
    int capNhatTrangThaiTuDong();

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaResponse(
    dotGiamGia.id,dotGiamGia.ma,dotGiamGia.ten,dotGiamGia.moTa,dotGiamGia.loaiGiam,dotGiamGia.giaTriGiam,
    dotGiamGia.ngayBatDau,dotGiamGia.ngayKetThuc,dotGiamGia.kichHoat,dotGiamGia.ngayTao,dotGiamGia.ngayCapNhat
    )
    FROM DotGiamGia dotGiamGia
""")
    List<QuanLyDotGiamGiaResponse> hienThiDotGiamGia();

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaResponse(
    dotGiamGia.id,dotGiamGia.ma,dotGiamGia.ten,dotGiamGia.moTa,dotGiamGia.loaiGiam,dotGiamGia.giaTriGiam,
    dotGiamGia.ngayBatDau,dotGiamGia.ngayKetThuc,dotGiamGia.kichHoat,dotGiamGia.ngayTao,dotGiamGia.ngayCapNhat
    )
    FROM DotGiamGia dotGiamGia
    WHERE dotGiamGia.id = ?1
""")
    QuanLyDotGiamGiaResponse detailDotGiamGia(Integer id);

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaResponse(
    dotGiamGia.id,dotGiamGia.ma,dotGiamGia.ten,dotGiamGia.moTa,dotGiamGia.loaiGiam,dotGiamGia.giaTriGiam,
    dotGiamGia.ngayBatDau,dotGiamGia.ngayKetThuc,dotGiamGia.kichHoat,dotGiamGia.ngayTao,dotGiamGia.ngayCapNhat
    )
    FROM DotGiamGia dotGiamGia
""")
    Page<QuanLyDotGiamGiaResponse> phanTrangDotGiamGia(Pageable pageable);

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaResponse(
    dotGiamGia.id,dotGiamGia.ma,dotGiamGia.ten,dotGiamGia.moTa,dotGiamGia.loaiGiam,dotGiamGia.giaTriGiam,
    dotGiamGia.ngayBatDau,dotGiamGia.ngayKetThuc,dotGiamGia.kichHoat,dotGiamGia.ngayTao,dotGiamGia.ngayCapNhat
    )
    FROM DotGiamGia dotGiamGia
    WHERE (:keyword IS NULL 
        OR LOWER(dotGiamGia.ma) LIKE LOWER(CONCAT('%', :keyword, '%')) 
        OR LOWER(dotGiamGia.ten) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(dotGiamGia.moTa) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR CAST(dotGiamGia.giaTriGiam AS string) LIKE CONCAT('%', :keyword, '%'))
    AND (:trangThai IS NULL OR dotGiamGia.kichHoat = :trangThai)
    AND (:loaiGiam IS NULL OR dotGiamGia.loaiGiam = :loaiGiam)
    AND (CAST(:tuNgay AS date) IS NULL OR dotGiamGia.ngayBatDau >= CAST(:tuNgay AS date))
    AND (CAST(:denNgay AS date) IS NULL OR dotGiamGia.ngayKetThuc <= CAST(:denNgay AS date))
""")
    Page<QuanLyDotGiamGiaResponse> timKiemVaPhanTrang(
            @Param("keyword") String keyword, 
            @Param("trangThai") Integer trangThai, 
            @Param("loaiGiam") Integer loaiGiam, 
            @Param("tuNgay") java.time.LocalDate tuNgay, 
            @Param("denNgay") java.time.LocalDate denNgay, 
            Pageable pageable);

    boolean existsByTenIgnoreCase(String ten);
    boolean existsByTenIgnoreCaseAndIdNot(String ten, Integer id);
}
