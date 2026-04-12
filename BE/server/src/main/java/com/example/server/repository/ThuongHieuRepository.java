package com.example.server.repository;

import com.example.server.entity.ThuongHieu;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {

    List<ThuongHieu> findByTrangThaiOrderByTenAsc(Integer trangThai);

    @Query("select t from ThuongHieu t where (:kw is null or lower(t.ma) like lower(concat('%',:kw,'%')) or lower(t.ten) like lower(concat('%',:kw,'%')))")
    Page<ThuongHieu> search(@Param("kw") String kw, Pageable pageable);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);
}
