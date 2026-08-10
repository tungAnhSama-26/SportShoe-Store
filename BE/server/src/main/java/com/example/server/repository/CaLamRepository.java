package com.example.server.repository;

import com.example.server.entity.CaLam;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CaLamRepository extends JpaRepository<CaLam, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CaLam c where c.id = :id")
    java.util.Optional<CaLam> findByIdForUpdate(@Param("id") String id);
}
