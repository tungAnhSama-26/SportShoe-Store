package com.example.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ca_lam")
public class CaLam {
    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @NotNull
    @Column(name = "ten", nullable = false)
    private String ten;

    @NotNull
    @Column(name = "gio_bat_dau", nullable = false, length = 10)
    private String gioBatDau;

    @NotNull
    @Column(name = "gio_ket_thuc", nullable = false, length = 10)
    private String gioKetThuc;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai = true;

    public String getTen() {
        if (ten != null) {
            if ("chieu".equalsIgnoreCase(id) && (ten.contains("?") || "Ca chieu".equalsIgnoreCase(ten))) {
                return "Ca chiều";
            }
            if ("toi".equalsIgnoreCase(id) && (ten.contains("?") || "Ca toi".equalsIgnoreCase(ten))) {
                return "Ca tối";
            }
            if ("sang".equalsIgnoreCase(id) && (ten.contains("?") || "Ca sang".equalsIgnoreCase(ten))) {
                return "Ca sáng";
            }
            return ten.replace("chi?u", "chiều").replace("Chi?u", "Chiều")
                    .replace("t?i", "tối").replace("T?i", "Tối")
                    .replace("s?ng", "sáng").replace("S?ng", "Sáng");
        }
        return ten;
    }
}
