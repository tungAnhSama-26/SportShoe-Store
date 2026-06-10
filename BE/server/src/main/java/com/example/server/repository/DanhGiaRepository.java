package com.example.server.repository;

import com.example.server.entity.DanhGia;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {

    /** Đánh giá đang hiển thị của một sản phẩm, kèm thông tin khách hàng, mới nhất trước. */
    @Query("""
            select dg from DanhGia dg
            join fetch dg.khachHang kh
            where dg.giay.id = :giayId and dg.trangThai = 1
            order by dg.ngayTao desc
            """)
    List<DanhGia> findByGiayId(@Param("giayId") Integer giayId);

    /** Đã có đánh giá cho dòng hóa đơn chi tiết này chưa (mỗi dòng chỉ đánh giá 1 lần). */
    boolean existsByHoaDonChiTietId(Integer hoaDonChiTietId);

    /** Đánh giá theo danh sách dòng hóa đơn chi tiết (để hiển thị đã đánh giá hay chưa). */
    @Query("select dg from DanhGia dg where dg.hoaDonChiTiet.id in :ids")
    List<DanhGia> findByHoaDonChiTietIdIn(@Param("ids") Collection<Integer> ids);
}
