package com.example.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mau_sac")
public class MauSac extends BaseEntity {

    @Column(name = "ma", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "ten", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "ma_mau_hex", length = 7)
    private String hexCode;
}
