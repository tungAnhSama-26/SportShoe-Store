package com.example.server.repository;

import com.example.server.entity.KhachHang;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KhachHangRepository extends JpaRepository<KhachHang, UUID> {

    @Query("""
            select kh
            from KhachHang kh
            where kh.trangThai = 1
              and (
                :keyword is null
                or kh.sdt like concat('%', :keyword, '%')
                or lower(kh.hoTen) like lower(concat('%', :keyword, '%'))
              )
            order by kh.ngayTao desc
            """)
    List<KhachHang> searchByKeyword(@Param("keyword") String keyword);
}
