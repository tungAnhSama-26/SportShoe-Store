package com.example.server.repository;

import com.example.server.entity.Giay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GiayRepository extends JpaRepository<Giay, Integer>, JpaSpecificationExecutor<Giay> {

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);
}
