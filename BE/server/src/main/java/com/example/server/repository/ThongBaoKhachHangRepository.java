package com.example.server.repository;

import com.example.server.entity.ThongBaoKhachHang;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThongBaoKhachHangRepository extends JpaRepository<ThongBaoKhachHang, Integer> {

    /** Thông báo của 1 khách trong khoảng còn hiệu lực (sau mốc "sau"), mới nhất trước. */
    List<ThongBaoKhachHang> findByKhachHangIdAndNgayTaoAfterOrderByNgayTaoDesc(UUID khachHangId, Instant sau);

    /** Số thông báo CHƯA XEM còn hiệu lực (cho số nhỏ cạnh chuông). */
    long countByKhachHangIdAndDaXemFalseAndNgayTaoAfter(UUID khachHangId, Instant sau);

    /** Khách mở chuông -> đánh dấu tất cả là đã xem. */
    @Modifying
    @Query("update ThongBaoKhachHang t set t.daXem = true where t.khachHangId = :khachHangId and t.daXem = false")
    int danhDauDaXemTatCa(@Param("khachHangId") UUID khachHangId);

    /** Dọn thông báo quá hạn (scheduler chạy định kỳ, quá 3 ngày là xóa). */
    @Modifying
    @Query("delete from ThongBaoKhachHang t where t.ngayTao < :truoc")
    int xoaTruocMoc(@Param("truoc") Instant truoc);
}
