package com.example.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lich_lam_viec", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nhan_vien_id", "ngay"})
})
public class LichLamViec {
    @Id
    @GeneratedValue(generator = "UUID")
//    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id", nullable = false)
    private NhanVien nhanVien;

    @NotNull
    @Column(name = "ngay", nullable = false)
    private LocalDate ngay;

    @NotNull
    @Column(name = "ca", nullable = false, length = 10)
    private String ca; // 'sang', 'chieu', 'toi'
}
