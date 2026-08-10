package com.example.server.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@Table(name = "nhan_vien")
public class NhanVien {
    @Id
    @ColumnDefault("newid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 20)
    @NotNull
    @Column(name = "ma", nullable = false, length = 20)
    private String ma;

    @Size(max = 100)
    @NotNull
    @Column(name = "ten_dang_nhap", nullable = false, length = 100)
    private String tenDangNhap;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Size(max = 100)
    @NotNull
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @Size(max = 20)
    @Column(name = "sdt", length = 20)
    private String sdt;

    @Size(max = 10)
    @Nationalized
    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "tinhThanh", column = @Column(name = "tinh_thanh", length = 100)),
            @AttributeOverride(name = "phuongXa", column = @Column(name = "phuong_xa", length = 100)),
            @AttributeOverride(name = "diaChiCuThe", column = @Column(name = "dia_chi", length = 200))
    })
    private DiaChiHaiCap diaChi;

    @NotNull
    @ColumnDefault("2")
    @Column(name = "vai_tro", nullable = false)
    private Integer vaiTro;

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

    @Column(name = "face_descriptor", columnDefinition = "NVARCHAR(MAX)")
    private String faceDescriptor;

}
