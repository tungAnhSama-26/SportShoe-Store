package com.example.server.repository;

import com.example.server.entity.CaLam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaLamRepository extends JpaRepository<CaLam, String> {
}
