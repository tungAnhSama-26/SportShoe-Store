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
@Table(name = "loai_giay")
public class LoaiGiay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Size(max = 200)
    @NotNull
    @Nationalized
    @Column(name = "ten", nullable = false, length = 200)
    private String ten;

    @Size(max = 500)
    @Nationalized
    @Column(name = "mo_ta", length = 500)
    private String moTa;

    /** Cache phân loại cho gợi ý AI: CSV mã đáp án "mục đích" (di-lam,the-thao,dao-pho,du-tiec). Null = chưa phân loại. */
    @Size(max = 200)
    @Column(name = "nhom_muc_dich", length = 200)
    private String nhomMucDich;

    /** Cache phân loại cho gợi ý AI: CSV mã đáp án "phong cách" (nang-dong,toi-gian,ca-tinh,co-dien). Null = chưa phân loại. */
    @Size(max = 200)
    @Column(name = "nhom_phong_cach", length = 200)
    private String nhomPhongCach;

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