package com.example.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "thuong_hieu")
public class ThuongHieu extends ActivatableEntity {

    @Column(name = "ma", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "ten", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "mo_ta", length = 255)
    private String description;
}
