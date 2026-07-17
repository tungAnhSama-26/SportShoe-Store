package com.example.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.UUID;

/**
 * Thông báo cho khách hàng (chuông ở header màn khách).
 * Loại: DON_HANG (trạng thái đơn đổi), VOUCHER (phiếu mới), GIAM_GIA (đợt giảm giá mới),
 * DANH_GIA (đánh giá bị ẩn). Quá 3 ngày sẽ bị scheduler xóa.
 */
@Getter
@Setter
@Entity
@Table(name = "thong_bao_khach_hang")
public class ThongBaoKhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "khach_hang_id", nullable = false)
    private UUID khachHangId;

    @NotNull
    @Size(max = 30)
    @Column(name = "loai", nullable = false, length = 30)
    private String loai;

    @NotNull
    @Size(max = 200)
    @Nationalized
    @Column(name = "tieu_de", nullable = false, length = 200)
    private String tieuDe;

    @Size(max = 500)
    @Nationalized
    @Column(name = "noi_dung", length = 500)
    private String noiDung;

    @Size(max = 200)
    @Nationalized
    @Column(name = "lien_ket", length = 200)
    private String lienKet;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "da_xem", nullable = false)
    private Boolean daXem = false;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;
}
