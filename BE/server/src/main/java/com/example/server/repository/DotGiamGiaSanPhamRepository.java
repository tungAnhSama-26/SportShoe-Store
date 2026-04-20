package com.example.server.repository;

import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse;
import com.example.server.entity.DotGiamGiaSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DotGiamGiaSanPhamRepository extends JpaRepository<DotGiamGiaSanPham, Integer> {
    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse(
    dotGiamGiaSP.id,dotGiamGiaSP.dotGiamGia.id,dotGiamGiaSP.giay.id,dotGiamGiaSP.dotGiamGia.ten,dotGiamGiaSP.giay.ten,dotGiamGiaSP.trangThai,dotGiamGiaSP.ngayTao
    )
    FROM DotGiamGiaSanPham dotGiamGiaSP 
    JOIN DotGiamGia dotGiamGia ON dotGiamGiaSP.dotGiamGia.id = dotGiamGia.id
    JOIN Giay giay ON dotGiamGiaSP.giay.id = giay.id
""")
    List<QuanLyDotGiamGiaSanPhamResponse> hienThiQuanLyDotGiamGiaSanPham();

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse(
    dotGiamGiaSP.id,dotGiamGiaSP.dotGiamGia.id,dotGiamGiaSP.giay.id,dotGiamGiaSP.dotGiamGia.ten,dotGiamGiaSP.giay.ten,dotGiamGiaSP.trangThai,dotGiamGiaSP.ngayTao
    )
    FROM DotGiamGiaSanPham dotGiamGiaSP 
    JOIN DotGiamGia dotGiamGia ON dotGiamGiaSP.dotGiamGia.id = dotGiamGia.id
    JOIN Giay giay ON dotGiamGiaSP.giay.id = giay.id
    WHERE dotGiamGiaSP.id = ?1
""")
    QuanLyDotGiamGiaSanPhamResponse detailQuanLyDotGiamGiaSanPham(Integer id);

    @Query("""
    SELECT new com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse(
    dotGiamGiaSP.id,dotGiamGiaSP.dotGiamGia.id,dotGiamGiaSP.giay.id,dotGiamGiaSP.dotGiamGia.ten,dotGiamGiaSP.giay.ten,dotGiamGiaSP.trangThai,dotGiamGiaSP.ngayTao
    )
    FROM DotGiamGiaSanPham dotGiamGiaSP 
    JOIN DotGiamGia dotGiamGia ON dotGiamGiaSP.dotGiamGia.id = dotGiamGia.id
    JOIN Giay giay ON dotGiamGiaSP.giay.id = giay.id
""")
    Page<QuanLyDotGiamGiaSanPhamResponse> phanTrangQuanLyDotGiamGiaSanPham(Pageable pageable);

    @Query("SELECT d FROM DotGiamGiaSanPham d JOIN FETCH d.dotGiamGia WHERE d.giay.id = ?1 AND d.trangThai = 1 AND d.dotGiamGia.kichHoat = 1")
    List<DotGiamGiaSanPham> findActiveByGiayId(Integer giayId);

    @Query("SELECT d FROM DotGiamGiaSanPham d WHERE d.dotGiamGia.id = ?1")
    List<DotGiamGiaSanPham> findByDotGiamGiaId(Integer dotGiamGiaId);
}
