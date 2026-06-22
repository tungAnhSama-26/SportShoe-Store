package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "tin_nhan")
public class TinNhan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuoc_hoi_thoai_id", nullable = false)
    private CuocHoiThoai cuocHoiThoai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;

    @Column(name = "nguoi_gui", nullable = false, length = 50)
    private String nguoiGui; // "CUSTOMER", "STAFF", "AI"

    @Column(name = "noi_dung", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;


    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao = Instant.now();
}
