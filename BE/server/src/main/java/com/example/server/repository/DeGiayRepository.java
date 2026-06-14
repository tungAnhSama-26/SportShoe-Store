package com.example.server.repository;

import com.example.server.entity.DeGiay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeGiayRepository extends JpaRepository<DeGiay, Integer> {

    @Query("select d from DeGiay d where (:kw is null or lower(d.ma) like lower(concat('%',:kw,'%')) or lower(d.ten) like lower(concat('%',:kw,'%')))")
    Page<DeGiay> search(@Param("kw") String kw, Pageable pageable);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);

    boolean existsByTenIgnoreCase(String ten);

    boolean existsByTenIgnoreCaseAndIdNot(String ten, Integer id);
}
