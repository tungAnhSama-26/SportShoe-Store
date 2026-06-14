package com.example.server.repository;

import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaResponse;
import com.example.server.entity.PhieuGiamGia;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;

public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {

    Optional<PhieuGiamGia> findByMaIgnoreCase(String ma);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PhieuGiamGia p where lower(p.ma) = lower(:ma)")
    Optional<PhieuGiamGia> findByMaIgnoreCaseForUpdate(@Param("ma") String ma);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PhieuGiamGia p where p.id = :id")
    Optional<PhieuGiamGia> findByIdForUpdate(@Param("id") Integer id);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);

    /** Voucher theo loại phiếu (1=toàn sàn, 2=cá nhân) và trạng thái. Dùng liệt kê voucher công khai khả dụng. */
    List<PhieuGiamGia> findByLoaiPhieuAndTrangThai(Integer loaiPhieu, Integer trangThai);

    boolean existsByTenIgnoreCase(String ten);

    boolean existsByTenIgnoreCaseAndIdNot(String ten, Integer id);

    @Query("""
            select p
            from PhieuGiamGia p
            where p.trangThai = 1
              and (
                :keyword is null
                or lower(p.ma) like lower(concat('%', :keyword, '%'))
                or lower(p.ten) like lower(concat('%', :keyword, '%'))
              )
            order by p.ngayTao desc, p.id desc
            """)
    List<PhieuGiamGia> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaResponse(
    phieuGG.id,phieuGG.ma,phieuGG.ten,phieuGG.loai,phieuGG.loaiPhieu,phieuGG.giaTri,phieuGG.giaTriToiThieu,
    phieuGG.giamToiDa,phieuGG.ngayBatDau,phieuGG.ngayKetThuc,phieuGG.soLuong,
    phieuGG.soLuongDaDung,phieuGG.trangThai,phieuGG.ngayTao
    )
    FROM PhieuGiamGia phieuGG 
    ORDER BY phieuGG.ngayTao DESC, phieuGG.id DESC
""")
    List<QuanLyPhieuGiamGiaResponse> hienThiPhieuGiamGia();

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaResponse(
    phieuGG.id,phieuGG.ma,phieuGG.ten,phieuGG.loai,phieuGG.loaiPhieu,phieuGG.giaTri,phieuGG.giaTriToiThieu,
    phieuGG.giamToiDa,phieuGG.ngayBatDau,phieuGG.ngayKetThuc,phieuGG.soLuong,
    phieuGG.soLuongDaDung,phieuGG.trangThai,phieuGG.ngayTao
    )
    FROM PhieuGiamGia phieuGG
    WHERE phieuGG.id = ?1 
""")
    QuanLyPhieuGiamGiaResponse DetailPhieuGiamGia(Integer id);

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaResponse(
    phieuGG.id,phieuGG.ma,phieuGG.ten,phieuGG.loai,phieuGG.loaiPhieu,phieuGG.giaTri,phieuGG.giaTriToiThieu,
    phieuGG.giamToiDa,phieuGG.ngayBatDau,phieuGG.ngayKetThuc,phieuGG.soLuong,
    phieuGG.soLuongDaDung,phieuGG.trangThai,phieuGG.ngayTao
    )
    FROM PhieuGiamGia phieuGG 
    ORDER BY phieuGG.ngayTao DESC, phieuGG.id DESC
""")
    Page<QuanLyPhieuGiamGiaResponse> phantrangThaiPhieuGiamGia(Pageable pageable);

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaResponse(
    phieuGG.id,phieuGG.ma,phieuGG.ten,phieuGG.loai,phieuGG.loaiPhieu,phieuGG.giaTri,phieuGG.giaTriToiThieu,
    phieuGG.giamToiDa,phieuGG.ngayBatDau,phieuGG.ngayKetThuc,phieuGG.soLuong,
    phieuGG.soLuongDaDung,phieuGG.trangThai,phieuGG.ngayTao
    )
    FROM PhieuGiamGia phieuGG 
    WHERE (:keyword IS NULL 
        OR LOWER(phieuGG.ma) LIKE LOWER(CONCAT('%', :keyword, '%')) 
        OR LOWER(phieuGG.ten) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR CAST(phieuGG.giaTri AS string) LIKE CONCAT('%', :keyword, '%')
        OR CAST(phieuGG.soLuong AS string) LIKE CONCAT('%', :keyword, '%'))
    AND (:trangThai IS NULL OR phieuGG.trangThai = :trangThai)
    AND (:loai IS NULL OR phieuGG.loai = :loai)
    AND (CAST(:tuNgay AS timestamp) IS NULL OR phieuGG.ngayBatDau >= :tuNgay)
    AND (CAST(:denNgay AS timestamp) IS NULL OR phieuGG.ngayKetThuc <= :denNgay)
    ORDER BY phieuGG.ngayTao DESC, phieuGG.id DESC
""")
    Page<QuanLyPhieuGiamGiaResponse> timKiemVaPhanTrang(
            @Param("keyword") String keyword, 
            @Param("trangThai") Integer trangThai, 
            @Param("loai") Integer loai, 
            @Param("tuNgay") java.time.Instant tuNgay, 
            @Param("denNgay") java.time.Instant denNgay, 
            Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE PhieuGiamGia p
        SET p.trangThai = 
            CASE 
                WHEN p.ngayKetThuc < CURRENT_TIMESTAMP THEN 2
                WHEN p.soLuong > 0 AND p.soLuong != 999999 AND p.soLuongDaDung >= p.soLuong THEN 3
                WHEN p.ngayBatDau > CURRENT_TIMESTAMP THEN 4
                ELSE 1
            END
        WHERE (p.ngayKetThuc < CURRENT_TIMESTAMP AND p.trangThai != 2)
           OR (
               p.trangThai != 0 
               AND p.trangThai != (
                   CASE 
                       WHEN p.ngayKetThuc < CURRENT_TIMESTAMP THEN 2
                       WHEN p.soLuong > 0 AND p.soLuong != 999999 AND p.soLuongDaDung >= p.soLuong THEN 3
                       WHEN p.ngayBatDau > CURRENT_TIMESTAMP THEN 4
                       ELSE 1
                   END
               )
           )
    """)
    void capNhatTrangThaiTuDong();
}
