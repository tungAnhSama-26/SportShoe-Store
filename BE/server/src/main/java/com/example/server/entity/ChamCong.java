package com.example.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cham_cong", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nhan_vien_id", "ngay", "ca"})
})
public class ChamCong {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id", nullable = false)
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lich_lam_viec_id")
    private LichLamViec lichLamViec;

    @NotNull
    @Column(name = "ngay", nullable = false)
    private LocalDate ngay;

    @NotNull
    @Column(name = "ca", nullable = false, length = 10)
    private String ca; // 'sang', 'chieu', 'toi'

    @Column(name = "thoi_gian_vao")
    private Instant thoiGianVao;

    @Column(name = "thoi_gian_ra")
    private Instant thoiGianRa;

    @Column(name = "trang_thai_vao", length = 20)
    private String trangThaiVao; // 'DUNG_GIO', 'MUON'

    @Column(name = "trang_thai_ra", length = 20)
    private String trangThaiRa; // 'DUNG_GIO', 'SOM'

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;
}
