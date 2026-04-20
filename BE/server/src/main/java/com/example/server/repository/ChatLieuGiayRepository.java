package com.example.server.repository;

import com.example.server.entity.ChatLieuGiay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatLieuGiayRepository extends JpaRepository<ChatLieuGiay, Integer> {

    @Query("select c from ChatLieuGiay c where (:kw is null or lower(c.ma) like lower(concat('%',:kw,'%')) or lower(c.ten) like lower(concat('%',:kw,'%')))")
    Page<ChatLieuGiay> search(@Param("kw") String kw, Pageable pageable);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);
}
