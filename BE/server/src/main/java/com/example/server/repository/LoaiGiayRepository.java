package com.example.server.repository;

import com.example.server.entity.LoaiGiay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoaiGiayRepository extends JpaRepository<LoaiGiay, Integer> {

    @Query("select l from LoaiGiay l where (:kw is null or lower(l.ma) like lower(concat('%',:kw,'%')) or lower(l.ten) like lower(concat('%',:kw,'%')))")
    Page<LoaiGiay> search(@Param("kw") String kw, Pageable pageable);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);
}
