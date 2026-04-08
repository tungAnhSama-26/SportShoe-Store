package com.example.server.repository;

import com.example.server.entity.KichCo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KichCoRepository extends JpaRepository<KichCo, UUID> {

    List<KichCo> findByDeletedFalseOrderByValueAsc();
}
