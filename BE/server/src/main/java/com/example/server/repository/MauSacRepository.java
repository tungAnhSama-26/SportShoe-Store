package com.example.server.repository;

import com.example.server.entity.MauSac;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MauSacRepository extends JpaRepository<MauSac, Integer> {

    @Query("select m from MauSac m where (:kw is null or lower(m.ma) like lower(concat('%',:kw,'%')) or lower(m.ten) like lower(concat('%',:kw,'%')))")
    Page<MauSac> search(@Param("kw") String kw, Pageable pageable);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);

    boolean existsByTenIgnoreCase(String ten);

    java.util.Optional<MauSac> findByTenIgnoreCase(String ten);

    boolean existsByTenIgnoreCaseAndIdNot(String ten, Integer id);
}
