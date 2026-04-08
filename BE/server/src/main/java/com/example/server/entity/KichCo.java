package com.example.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "kich_co")
public class KichCo extends BaseEntity {

    @Column(name = "gia_tri", nullable = false, unique = true, length = 20)
    private String value;

    @Column(name = "ghi_chu", length = 200)
    private String note;
}
