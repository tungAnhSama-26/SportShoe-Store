package com.example.server.repository;

import com.example.server.entity.CoGiay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoGiayRepository extends JpaRepository<CoGiay, Integer> {

    @Query("select c from CoGiay c where (:kw is null or lower(c.ma) like lower(concat('%',:kw,'%')) or lower(c.ten) like lower(concat('%',:kw,'%')))")
    Page<CoGiay> search(@Param("kw") String kw, Pageable pageable);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);

    boolean existsByTenIgnoreCase(String ten);

    boolean existsByTenIgnoreCaseAndIdNot(String ten, Integer id);
    Optional<CoGiay> findByTenIgnoreCase(String ten);
}