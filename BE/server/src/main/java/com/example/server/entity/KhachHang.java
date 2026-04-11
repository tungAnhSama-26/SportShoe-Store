package com.example.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @ColumnDefault("newid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ten_dang_nhap", nullable = false, length = 50)
    private String tenDangNhap;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 20)
    @Column(name = "sdt", length = 20)
    private String sdt;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Size(max = 255)
    @NotNull
    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;

    @Size(max = 500)
    @Column(name = "hinh_anh", length = 500)
    private String hinhAnh;

    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;


}