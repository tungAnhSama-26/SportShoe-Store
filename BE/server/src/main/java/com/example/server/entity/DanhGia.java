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
@Table(name = "danh_gia")
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giay_id", nullable = false)
    private Giay giay;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    /** Dòng hóa đơn chi tiết được đánh giá (mỗi dòng chỉ đánh giá 1 lần). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_chi_tiet_id")
    private HoaDonChiTiet hoaDonChiTiet;

    @NotNull
    @Column(name = "so_sao", nullable = false)
    private Integer soSao;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "noi_dung", length = 1000)
    private String noiDung;

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

    /** Ảnh/video kèm đánh giá - lưu chuỗi JSON: [{"url":"..","loai":"image|video"}]. */
    @Nationalized
    @Column(name = "media")
    private String media;

    /** Phản hồi của shop cho đánh giá (mỗi đánh giá chỉ phản hồi 1 lần). */
    @Size(max = 1000)
    @Nationalized
    @Column(name = "phan_hoi", length = 1000)
    private String phanHoi;

    @Column(name = "ngay_phan_hoi")
    private Instant ngayPhanHoi;

    /** Admin đã xem đánh giá này chưa (cho chuông thông báo). */
    @ColumnDefault("0")
    @Column(name = "da_xem")
    private Boolean daXem;
}
