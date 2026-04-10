package com.example.server.repository;

import com.example.server.entity.HoaDonChiTiet;
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
}
