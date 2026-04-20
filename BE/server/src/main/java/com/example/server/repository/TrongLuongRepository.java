package com.example.server.repository;

import com.example.server.entity.TrongLuong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrongLuongRepository extends JpaRepository<TrongLuong, Integer> {

    @Query("select t from TrongLuong t where (:kw is null or lower(t.ma) like lower(concat('%',:kw,'%')))")
    Page<TrongLuong> search(@Param("kw") String kw, Pageable pageable);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);
}
