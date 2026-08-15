package com.example.server.repository;

import com.example.server.entity.HoaDon;
import java.util.List;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    List<HoaDon> findTop10ByKenhBanAndTrangThaiOrderByNgayTaoDesc(Integer kenhBan, Integer trangThai);

    long countByKenhBanAndTrangThai(Integer kenhBan, Integer trangThai);

    @Query("""
            select hd from HoaDon hd
            where hd.kenhBan = :kenhBan
              and hd.trangThai = :trangThai
              and hd.ngayTao <= :moc
            """)
    List<HoaDon> findExpiredPendingInvoices(
            @Param("kenhBan") Integer kenhBan,
            @Param("trangThai") Integer trangThai,
            @Param("moc") Instant moc
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select distinct hd from HoaDon hd
            join ThanhToan t on t.hoaDon.id = hd.id
            left join fetch hd.khachHang
            left join fetch hd.phieuGiamGia
            where hd.kenhBan = 2
              and hd.trangThai = 11
              and t.trangThai = 0
              and t.loaiGiaoDich = 1
              and hd.ngayTao < :moc
            """)
    List<HoaDon> findExpiredOnlineQrForUpdate(@Param("moc") Instant moc);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select distinct hd from HoaDon hd
            join ThanhToan t on t.hoaDon.id = hd.id
            left join fetch hd.khachHang
            left join fetch hd.phieuGiamGia
            where hd.kenhBan = 2
              and hd.trangThai = 11
              and t.trangThai = 0
              and t.loaiGiaoDich = 1
              and ((:khachHangId is not null and hd.khachHang.id = :khachHangId)
                   or (:khachHangId is null and hd.khachHang is null and hd.sdtNguoiNhan = :sdt))
            """)
    List<HoaDon> findOnlineQrChoTheoChuSoHuuForUpdate(
            @Param("khachHangId") UUID khachHangId,
            @Param("sdt") String sdt
    );

    /** Đơn đã giao và thanh toán thành công nhưng chưa bấm nhận quá 3 ngày kể từ khi THỎA MÃN CẢ HAI điều kiện. */
    @Query("""
            select hd from HoaDon hd
            where hd.trangThai = 4
              and hd.ngayThanhToan is not null
              and hd.ngayThanhToan <= :moc
              and coalesce(
                  (select vc.ngayGiaoThat from VanChuyen vc where vc.hoaDon.id = hd.id),
                  hd.ngayCapNhat,
                  hd.ngayTao
              ) <= :moc
            """)
    List<HoaDon> findDonDaGiaoDaThanhToanQuaHan(
            @Param("moc") Instant moc
    );


    /** Hóa đơn đang là "giỏ hàng" của khách (kênh online, trạng thái giỏ). */
    @Query("""
            select hd from HoaDon hd
            where hd.khachHang.id = :khachHangId
              and hd.kenhBan = :kenhBan
              and hd.trangThai = :trangThai
            """)
    java.util.Optional<HoaDon> findGioHang(
            @Param("khachHangId") UUID khachHangId,
            @Param("kenhBan") Integer kenhBan,
            @Param("trangThai") Integer trangThai
    );

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
              and not (hd.kenhBan = 2 and hd.trangThai = 11)
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
            select hd
            from HoaDon hd
            left join fetch hd.khachHang
            left join fetch hd.nhanVien
            left join fetch hd.phieuGiamGia
            where upper(hd.ma) = upper(:ma)
            """)
    java.util.Optional<HoaDon> findDetailByMa(@Param("ma") String ma);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select hd
            from HoaDon hd
            left join fetch hd.khachHang
            left join fetch hd.nhanVien
            left join fetch hd.phieuGiamGia
            where hd.id = :id
            """)
    java.util.Optional<HoaDon> findDetailByIdForUpdate(@Param("id") Integer id);

    @Query("""
            select distinct hd
            from HoaDon hd
            left join fetch hd.khachHang kh
            left join fetch hd.nhanVien nv
            left join fetch hd.phieuGiamGia pgg
            where kh.id = :khachHangId
              and not (hd.kenhBan = 2 and hd.trangThai = 11)
            order by hd.ngayTao desc
            """)
    List<HoaDon> findByKhachHangId(@Param("khachHangId") UUID khachHangId);

    @Query("""
            select hd.trangThai, count(distinct hd.id)
            from HoaDon hd
            join HoaDonChiTiet hdct on hdct.hoaDon.id = hd.id
            join hdct.giayChiTiet gct
            join gct.giay g
            where (coalesce(hd.ngayThanhToan, hd.ngayLap) >= :tuNgay)
              and (coalesce(hd.ngayThanhToan, hd.ngayLap) < :denNgay)
              and (:brandId is null or g.thuongHieu.id = :brandId)
              and (:keyword is null
                   or lower(g.ma) like concat('%', :keyword, '%')
                   or lower(g.ten) like concat('%', :keyword, '%'))
            group by hd.trangThai
            """)
    List<Object[]> countByTrangThaiWithFilters(
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay,
            @Param("brandId") Integer brandId,
            @Param("keyword") String keyword
    );
}
