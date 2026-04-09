package com.example.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "kich_co")
public class KichCo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "gia_tri", nullable = false, length = 20)
    private String giaTri;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ghi_chu", length = 200)
    private String ghiChu;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;

    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;


}