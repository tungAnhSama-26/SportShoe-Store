package com.example.server.repository;

import com.example.server.entity.ChatLieu;
import com.example.server.entity.enums.ActiveStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatLieuRepository extends JpaRepository<ChatLieu, UUID> {

    List<ChatLieu> findByDeletedFalseAndStatusOrderByNameAsc(ActiveStatus status);
}
