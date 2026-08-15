package com.example.server.repository;

import com.example.server.entity.HoaDonChiTiet;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    @Query("""
            select hdct
            from HoaDonChiTiet hdct
            join fetch hdct.giayChiTiet gct
            join fetch gct.giay g
            where hdct.hoaDon.id = :hoaDonId
            order by hdct.id asc
            """)
    List<HoaDonChiTiet> findByHoaDonIdWithProduct(@Param("hoaDonId") Integer hoaDonId);

    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);

    /** Các dòng trong giỏ (hóa đơn), kèm sản phẩm/màu/size để hiển thị. */
    @Query("""
            select ct from HoaDonChiTiet ct
            join fetch ct.giayChiTiet gct
            join fetch gct.giay g
            join fetch gct.mauSac ms
            join fetch gct.kichCo kc
            where ct.hoaDon.id = :hoaDonId
            order by ct.id asc
            """)
    List<HoaDonChiTiet> findGioItems(@Param("hoaDonId") Integer hoaDonId);

    java.util.Optional<HoaDonChiTiet> findByHoaDonIdAndGiayChiTietId(Integer hoaDonId, Integer giayChiTietId);

    @Query("""
            select hdct
            from HoaDonChiTiet hdct
            join fetch hdct.hoaDon hd
            where hdct.giayChiTiet.id = :giayChiTietId
              and hd.trangThai in :trangThais
            """)
    List<HoaDonChiTiet> findByGiayChiTietIdAndTrangThaiHoaDon(
            @Param("giayChiTietId") Integer giayChiTietId,
            @Param("trangThais") Collection<Integer> trangThais
    );

    @Query("""
            from HoaDonChiTiet hdct
            join fetch hdct.hoaDon hd
            left join fetch hd.nhanVien nv
            join fetch hdct.giayChiTiet gct
            join fetch gct.giay g
            join fetch g.thuongHieu th
            where hdct.trangThai = 1
              and hd.trangThai in :trangThais
              and (coalesce(hd.ngayThanhToan, hd.ngayLap) >= :limitDate)
            order by hd.ngayTao asc, hd.id asc, hdct.id asc
            """)
    List<HoaDonChiTiet> findAllForThongKe(
            @Param("trangThais") Collection<Integer> trangThais,
            @Param("limitDate") Instant limitDate
    );

    /** Tổng số lượng đã bán theo từng sản phẩm (bỏ giỏ hàng = 0 và đơn đã hủy = 6). */
    @Query("""
            select ct.giayChiTiet.giay.id, sum(ct.soLuong)
            from HoaDonChiTiet ct
            where ct.giayChiTiet.giay.id in :giayIds
              and ct.hoaDon.trangThai not in (0, 6)
            group by ct.giayChiTiet.giay.id
            """)
    List<Object[]> tongDaBanTheoGiay(@Param("giayIds") Collection<Integer> giayIds);

    /**
     * Số lượng biến thể đang được đơn online giữ nhưng chưa trừ khỏi tồn kho thực tế.
     * Hóa đơn QR trạng thái 11 chỉ giữ hàng trong thời hạn 5 phút.
     */
    @Query("""
            select ct.giayChiTiet.id, coalesce(sum(ct.soLuong), 0)
            from HoaDonChiTiet ct
            join ct.hoaDon hd
            where ct.giayChiTiet.id in :giayChiTietIds
              and hd.kenhBan = 2
              and hd.daTruKho = false
              and (
                    hd.trangThai in (1, 7)
                    or (hd.trangThai = 11 and hd.ngayTao >= :mocQrConHan)
              )
              and (:loaiTruHoaDonId is null or hd.id <> :loaiTruHoaDonId)
            group by ct.giayChiTiet.id
            """)
    List<Object[]> tongSoLuongDangGiuTheoBienThe(
            @Param("giayChiTietIds") Collection<Integer> giayChiTietIds,
            @Param("mocQrConHan") Instant mocQrConHan,
            @Param("loaiTruHoaDonId") Integer loaiTruHoaDonId
    );
}
