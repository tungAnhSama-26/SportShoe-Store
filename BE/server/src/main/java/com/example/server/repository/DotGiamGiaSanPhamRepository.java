package com.example.server.repository;

import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse;
import com.example.server.entity.DotGiamGiaSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DotGiamGiaSanPhamRepository extends JpaRepository<DotGiamGiaSanPham, Integer> {
    boolean existsByDotGiamGiaIdAndGiayChiTietId(Integer dotGiamGiaId, Integer giayChiTietId);

    boolean existsByDotGiamGiaIdAndGiayChiTietIdAndIdNot(
            Integer dotGiamGiaId,
            Integer giayChiTietId,
            Integer id
    );

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse(
    dotGiamGiaSP.id, dotGiamGiaSP.dotGiamGia.id, dotGiamGiaSP.giayChiTiet.id, dotGiamGiaSP.giayChiTiet.giay.id, dotGiamGiaSP.dotGiamGia.ma, dotGiamGiaSP.dotGiamGia.ten, 
    dotGiamGiaSP.giayChiTiet.giay.ten, dotGiamGiaSP.giayChiTiet.mauSac.ten, dotGiamGiaSP.giayChiTiet.kichCo.giaTri,
    dotGiamGiaSP.trangThai, dotGiamGiaSP.ngayTao
    )
    FROM DotGiamGiaSanPham dotGiamGiaSP 
    JOIN dotGiamGiaSP.dotGiamGia dotGiamGia
    JOIN dotGiamGiaSP.giayChiTiet gct
""")
    List<QuanLyDotGiamGiaSanPhamResponse> hienThiQuanLyDotGiamGiaSanPham();

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse(
    dotGiamGiaSP.id, dotGiamGiaSP.dotGiamGia.id, dotGiamGiaSP.giayChiTiet.id, dotGiamGiaSP.giayChiTiet.giay.id, dotGiamGiaSP.dotGiamGia.ma, dotGiamGiaSP.dotGiamGia.ten, 
    dotGiamGiaSP.giayChiTiet.giay.ten, dotGiamGiaSP.giayChiTiet.mauSac.ten, dotGiamGiaSP.giayChiTiet.kichCo.giaTri,
    dotGiamGiaSP.trangThai, dotGiamGiaSP.ngayTao
    )
    FROM DotGiamGiaSanPham dotGiamGiaSP 
    JOIN dotGiamGiaSP.dotGiamGia dotGiamGia
    JOIN dotGiamGiaSP.giayChiTiet gct
    WHERE dotGiamGiaSP.id = ?1
""")
    QuanLyDotGiamGiaSanPhamResponse detailQuanLyDotGiamGiaSanPham(Integer id);

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse(
    dotGiamGiaSP.id, dotGiamGiaSP.dotGiamGia.id, dotGiamGiaSP.giayChiTiet.id, dotGiamGiaSP.giayChiTiet.giay.id, dotGiamGiaSP.dotGiamGia.ma, dotGiamGiaSP.dotGiamGia.ten, 
    dotGiamGiaSP.giayChiTiet.giay.ten, dotGiamGiaSP.giayChiTiet.mauSac.ten, dotGiamGiaSP.giayChiTiet.kichCo.giaTri,
    dotGiamGiaSP.trangThai, dotGiamGiaSP.ngayTao
    )
    FROM DotGiamGiaSanPham dotGiamGiaSP 
    JOIN dotGiamGiaSP.dotGiamGia dotGiamGia
    JOIN dotGiamGiaSP.giayChiTiet gct
""")
    Page<QuanLyDotGiamGiaSanPhamResponse> phanTrangQuanLyDotGiamGiaSanPham(Pageable pageable);

    @Query("SELECT d FROM DotGiamGiaSanPham d JOIN FETCH d.dotGiamGia WHERE d.giayChiTiet.id = ?1 AND d.trangThai = 1 AND d.dotGiamGia.kichHoat = 1")
    List<DotGiamGiaSanPham> findActiveByGiayChiTietId(Integer giayChiTietId);

    @Query("SELECT d FROM DotGiamGiaSanPham d JOIN FETCH d.dotGiamGia dg WHERE d.giayChiTiet.id = ?1")
    List<DotGiamGiaSanPham> findAllByGiayChiTietId(Integer giayChiTietId);

    @Query("SELECT d FROM DotGiamGiaSanPham d WHERE d.dotGiamGia.id = ?1")
    List<DotGiamGiaSanPham> findByDotGiamGiaId(Integer dotGiamGiaId);

    @Query("""
            SELECT d
            FROM DotGiamGiaSanPham d
            JOIN FETCH d.dotGiamGia dg
            JOIN FETCH d.giayChiTiet gct
            WHERE gct.id IN :giayChiTietIds
              AND d.trangThai = 1
              AND dg.kichHoat = 1
            """)
    List<DotGiamGiaSanPham> findActiveByGiayChiTietIdIn(@Param("giayChiTietIds") Collection<Integer> giayChiTietIds);

    @Query("SELECT DISTINCT d.giayChiTiet.id FROM DotGiamGiaSanPham d")
    List<Integer> findDistinctGiayChiTietIds();
}
