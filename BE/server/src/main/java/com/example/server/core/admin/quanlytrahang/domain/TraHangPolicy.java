package com.example.server.core.admin.quanlytrahang.domain;

import com.example.server.entity.HoaDon;
import com.example.server.entity.PhieuTraHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.VanChuyenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class TraHangPolicy {

    private final int soNgayGuiYeuCau;
    private final int soNgayGuiHangVeCuaHang;
    private final VanChuyenRepository vanChuyenRepository;

    public TraHangPolicy(
            @Value("${app.returns.request-window-days:3}") int soNgayGuiYeuCau,
            @Value("${app.returns.shipment-window-days:7}") int soNgayGuiHangVeCuaHang,
            VanChuyenRepository vanChuyenRepository
    ) {
        this.soNgayGuiYeuCau = Math.max(1, soNgayGuiYeuCau);
        this.soNgayGuiHangVeCuaHang = Math.max(1, soNgayGuiHangVeCuaHang);
        this.vanChuyenRepository = vanChuyenRepository;
    }

    public void kiemTraHoaDonChoAdmin(HoaDon hoaDon) {
        TraHangValidator.kiemTraTrangThaiHoaDon(hoaDon.getTrangThai());
        kiemTraThoiHan(hoaDon);
    }

    public void kiemTraHoaDonChoKhachHang(HoaDon hoaDon) {
        if (hoaDon.getTrangThai() == null || (hoaDon.getTrangThai() != 4 && hoaDon.getTrangThai() != 5)) {
            throw new BusinessException("Đơn hàng chưa giao hàng hoặc hoàn thành, không thể yêu cầu trả hàng/hoàn tiền");
        }
        kiemTraThoiHan(hoaDon);
    }

    private void kiemTraThoiHan(HoaDon hoaDon) {
        Instant ngayGiaoThat = vanChuyenRepository.findByHoaDonId(hoaDon.getId())
                .map(com.example.server.entity.VanChuyen::getNgayGiaoThat)
                .orElse(null);
        Instant ngayThanhToan = hoaDon.getNgayThanhToan();
        Instant mocTinh = ngayGiaoThat;
        if (mocTinh != null && ngayThanhToan != null) {
            if (ngayThanhToan.isAfter(mocTinh)) {
                mocTinh = ngayThanhToan;
            }
        } else if (mocTinh == null) {
            mocTinh = ngayThanhToan != null ? ngayThanhToan : (hoaDon.getNgayCapNhat() != null ? hoaDon.getNgayCapNhat() : hoaDon.getNgayTao());
        }
        if (mocTinh == null) {
            throw new BusinessException("Không xác định được thời điểm hoàn thành hóa đơn");
        }
        Instant hanCuoi = mocTinh.plus(Duration.ofDays(soNgayGuiYeuCau));
        if (Instant.now().isAfter(hanCuoi)) {
            throw new BusinessException(
                    "Đã quá thời hạn " + soNgayGuiYeuCau + " ngày để yêu cầu trả hàng/hoàn tiền"
            );
        }
    }

    public Instant mocQuaHanGuiHang() {
        return Instant.now().minus(Duration.ofDays(soNgayGuiHangVeCuaHang));
    }

    public boolean daQuaHanGuiHang(PhieuTraHang phieu) {
        return phieu.getNgayDuyet() != null
                && phieu.getNgayGuiHang() == null
                && phieu.getNgayDuyet().plus(Duration.ofDays(soNgayGuiHangVeCuaHang)).isBefore(Instant.now());
    }

    public int getSoNgayGuiHangVeCuaHang() {
        return soNgayGuiHangVeCuaHang;
    }
}

