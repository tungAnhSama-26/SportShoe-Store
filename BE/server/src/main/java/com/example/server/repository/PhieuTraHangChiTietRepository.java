package com.example.server.repository;

import com.example.server.entity.PhieuTraHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface PhieuTraHangChiTietRepository extends JpaRepository<PhieuTraHangChiTiet, Integer> {

    List<PhieuTraHangChiTiet> findByPhieuTraHangIdOrderByIdAsc(Integer phieuTraHangId);

    @Query("""
            select coalesce(sum(ct.soLuongTra), 0)
            from PhieuTraHangChiTiet ct
            where ct.hoaDonChiTiet.id = :hoaDonChiTietId
              and ct.phieuTraHang.trangThai not in (8, 9)
            """)
    Integer sumSoLuongDangXuLyByHoaDonChiTietId(
            @Param("hoaDonChiTietId") Integer hoaDonChiTietId
    );

    @Query("""
            select ct.giayChiTiet.giay.id, sum(ct.soLuongChapNhan)
            from PhieuTraHangChiTiet ct
            where ct.phieuTraHang.trangThai = 7
              and ct.phieuTraHang.ngayHoanTat >= :tuNgay
              and ct.phieuTraHang.ngayHoanTat < :denNgay
            group by ct.giayChiTiet.giay.id
            """)
    List<Object[]> sumReturnedQuantityGroupedByGiayIdWithDates(
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay
    );
}
