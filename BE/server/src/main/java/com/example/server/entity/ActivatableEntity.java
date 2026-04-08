package com.example.server.entity;

import com.example.server.entity.enums.ActiveStatus;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class ActivatableEntity extends BaseEntity {

    @Column(name = "trang_thai", nullable = false)
    private ActiveStatus status = ActiveStatus.ACTIVE;
}
