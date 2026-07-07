package com.example.server.repository;

import com.example.server.entity.DanhGia;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {

    /** Đánh giá đang hiển thị của toàn shop (lọc theo số sao nếu có), mới nhất trước - cho trang Đánh giá công khai. */
    @Query("""
            select dg from DanhGia dg
            join fetch dg.khachHang kh
            join fetch dg.giay g
            where dg.trangThai = 1
              and (:soSao is null or dg.soSao = :soSao)
            order by dg.ngayTao desc
            """)
    Page<DanhGia> findCongKhai(@Param("soSao") Integer soSao, Pageable pageable);

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

    /** Điểm trung bình + tổng số đánh giá đang hiển thị của toàn cửa hàng (1 dòng). */
    @Query("select avg(dg.soSao), count(dg) from DanhGia dg where dg.trangThai = 1")
    List<Object[]> thongKeTongQuan();

    /** Điểm sao trung bình + số lượt đánh giá (đang hiển thị) theo từng sản phẩm. */
    @Query("""
            select dg.giay.id, avg(dg.soSao), count(dg)
            from DanhGia dg
            where dg.giay.id in :giayIds and dg.trangThai = 1
            group by dg.giay.id
            """)
    List<Object[]> thongKeSaoTheoGiay(@Param("giayIds") Collection<Integer> giayIds);

    /** Danh sách sản phẩm có đánh giá + thống kê (cho màn quản lý đánh giá admin), SP có đánh giá mới nhất lên đầu. */
    @Query("""
            select g.id, g.ma, g.ten, g.hinhAnh,
                   count(dg), avg(dg.soSao), max(dg.ngayTao),
                   sum(case when dg.daXem = false then 1 else 0 end)
            from DanhGia dg
            join dg.giay g
            where dg.trangThai = 1
              and (:keyword is null
                   or lower(g.ten) like lower(concat('%', :keyword, '%'))
                   or lower(g.ma) like lower(concat('%', :keyword, '%')))
            group by g.id, g.ma, g.ten, g.hinhAnh
            order by max(dg.ngayTao) desc
            """)
    List<Object[]> thongKeSanPhamCoDanhGia(@Param("keyword") String keyword);

    /** Tổng số đánh giá đang hiển thị mà admin chưa xem (cho chuông thông báo). */
    @Query("select count(dg) from DanhGia dg where dg.trangThai = 1 and dg.daXem = false")
    long demChuaXem();

    /** Toàn bộ đánh giá đang hiển thị của shop, mới nhất trước (màn "Tất cả đánh giá" của admin). */
    @Query("""
            select dg from DanhGia dg
            join fetch dg.khachHang kh
            join fetch dg.giay g
            where dg.trangThai = 1
            order by dg.ngayTao desc
            """)
    List<DanhGia> findTatCaCongKhai();

    /**
     * Lọc đánh giá cho màn quản lý admin: theo sản phẩm (-1 = mọi SP), trạng thái
     * (-1 = cả hiển thị lẫn đã ẩn) và khoảng thời gian tạo. Mới nhất trước.
     */
    @Query("""
            select dg from DanhGia dg
            join fetch dg.khachHang kh
            join fetch dg.giay g
            where (:giayId = -1 or g.id = :giayId)
              and (:trangThai = -1 or dg.trangThai = :trangThai)
              and dg.ngayTao >= :tuNgay and dg.ngayTao < :denNgay
            order by dg.ngayTao desc
            """)
    List<DanhGia> locChoAdmin(
            @Param("giayId") int giayId,
            @Param("trangThai") int trangThai,
            @Param("tuNgay") java.time.Instant tuNgay,
            @Param("denNgay") java.time.Instant denNgay);

    /** Đánh dấu đã xem cho toàn bộ đánh giá đang hiển thị (khi admin mở màn Tất cả đánh giá). */
    @org.springframework.data.jpa.repository.Modifying
    @Query("update DanhGia dg set dg.daXem = true where dg.trangThai = 1 and dg.daXem = false")
    int danhDauDaXemTatCa();

    /** Đánh dấu đã xem cho toàn bộ đánh giá đang hiển thị của một sản phẩm. */
    @org.springframework.data.jpa.repository.Modifying
    @Query("update DanhGia dg set dg.daXem = true where dg.giay.id = :giayId and dg.trangThai = 1 and dg.daXem = false")
    int danhDauDaXemTheoSanPham(@Param("giayId") Integer giayId);
}
