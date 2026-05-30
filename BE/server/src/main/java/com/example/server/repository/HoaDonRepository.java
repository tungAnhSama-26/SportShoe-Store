package com.example.server.repository;

import com.example.server.entity.HoaDon;
import java.util.List;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    List<HoaDon> findTop10ByKenhBanAndTrangThaiOrderByNgayTaoDesc(Integer kenhBan, Integer trangThai);

    @Query("""
            select distinct hd
            from HoaDon hd
            left join fetch hd.khachHang kh
            left join fetch hd.nhanVien nv
            left join fetch hd.phieuGiamGia pgg
            where (:keyword is null
                or lower(hd.ma) like concat('%', :keyword, '%')
                or lower(hd.tenNguoiNhan) like concat('%', :keyword, '%')
                or lower(coalesce(nv.hoTen, '')) like concat('%', :keyword, '%')
                or lower(coalesce(kh.hoTen, '')) like concat('%', :keyword, '%')
                or lower(coalesce(hd.sdtNguoiNhan, '')) like concat('%', :keyword, '%'))
              and (:kenhBan is null or hd.kenhBan = :kenhBan)
              and (:trangThai is null or hd.trangThai = :trangThai)
              and (:tuNgay is null or hd.ngayTao >= :tuNgay)
              and (:denNgay is null or hd.ngayTao <= :denNgay)
            order by hd.ngayTao asc, hd.id asc
            """)
    List<HoaDon> searchInvoices(
            @Param("keyword") String keyword,
            @Param("kenhBan") Integer kenhBan,
            @Param("trangThai") Integer trangThai,
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay
    );

    @Query("""
            select hd
            from HoaDon hd
            left join fetch hd.khachHang
            left join fetch hd.nhanVien
            left join fetch hd.phieuGiamGia
            where hd.id = :id
            """)
    java.util.Optional<HoaDon> findDetailById(@Param("id") Integer id);

    @Query("""
            select distinct hd
            from HoaDon hd
            left join fetch hd.khachHang kh
            left join fetch hd.nhanVien nv
            left join fetch hd.phieuGiamGia pgg
            where kh.id = :khachHangId
            order by hd.ngayTao desc
            """)
    List<HoaDon> findByKhachHangId(@Param("khachHangId") UUID khachHangId);
}
