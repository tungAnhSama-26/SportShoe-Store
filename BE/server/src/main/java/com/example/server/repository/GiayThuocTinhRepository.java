package com.example.server.repository;

import com.example.server.entity.GiayThuocTinh;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GiayThuocTinhRepository extends JpaRepository<GiayThuocTinh, Integer> {

    Optional<GiayThuocTinh> findByGiayId(Integer giayId);

    @Query("""
            select gtt
            from GiayThuocTinh gtt
            left join fetch gtt.deGiay
            left join fetch gtt.coGiay
            left join fetch gtt.congNgheDem
            left join fetch gtt.chatLieuGiay
            left join fetch gtt.trongLuong
            where gtt.giay.id in :giayIds
            """)
    List<GiayThuocTinh> findByGiayIdInWithRefs(@Param("giayIds") Collection<Integer> giayIds);
}
