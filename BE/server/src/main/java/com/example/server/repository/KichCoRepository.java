package com.example.server.repository;

import com.example.server.entity.KichCo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KichCoRepository extends JpaRepository<KichCo, Integer> {

    @Query("select k from KichCo k where (:kw is null or lower(k.giaTri) like lower(concat('%',:kw,'%')))")
    Page<KichCo> search(@Param("kw") String kw, Pageable pageable);

    boolean existsByGiaTriIgnoreCase(String giaTri);

    boolean existsByGiaTriIgnoreCaseAndIdNot(String giaTri, Integer id);
}
