package com.example.server.repository;

import com.example.server.entity.PhieuGiamGia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {

    Optional<PhieuGiamGia> findByMaIgnoreCase(String ma);

    @Query("""
            select p
            from PhieuGiamGia p
            where p.trangThai = 1
              and (
                :keyword is null
                or lower(p.ma) like lower(concat('%', :keyword, '%'))
                or lower(p.ten) like lower(concat('%', :keyword, '%'))
              )
            order by p.ngayTao desc
            """)
    List<PhieuGiamGia> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
