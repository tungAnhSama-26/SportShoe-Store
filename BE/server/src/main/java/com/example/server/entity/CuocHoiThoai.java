package com.example.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "cuoc_hoi_thoai")
public class CuocHoiThoai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ten_khach_hang", length = 250)
    private String tenKhachHang;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai = 1; // 1: AI, 2: Trợ giúp từ Nhân viên, 3: Đang chat Staff, 4: Đã đóng

    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao = Instant.now();

    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;
}
