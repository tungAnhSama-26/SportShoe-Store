package com.example.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "dia_chi_khach_hang")
public class DiaChiKhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Size(max = 20)
    @NotNull
    @Column(name = "sdt", nullable = false, length = 20)
    private String sdt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "tinhThanh", column = @Column(name = "tinh_thanh", nullable = false, length = 100)),
            @AttributeOverride(name = "phuongXa", column = @Column(name = "phuong_xa", nullable = false, length = 100)),
            @AttributeOverride(name = "diaChiCuThe", column = @Column(name = "dia_chi_cu_the", nullable = false, length = 300))
    })
    private DiaChiHaiCap diaChi;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "la_mac_dinh", nullable = false)
    private Boolean laMacDinh;

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
