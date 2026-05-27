package com.example.server.core.admin.quanlyhoadon.service;

import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatSanPhamHoaDonRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatTrangThaiHoaDonRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.XacNhanHoanTienRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.XacNhanThanhToanCodRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonDetailResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonSummaryResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface QuanLyHoaDonService {

    List<HoaDonSummaryResponse> layDanhSachHoaDon(
            String keyword,
            String loaiDon,
            String trangThai,
            LocalDate tuNgay,
            LocalDate denNgay
    );

    HoaDonDetailResponse layChiTietHoaDon(Integer id);

    HoaDonDetailResponse capNhatTrangThaiHoaDon(Integer id, CapNhatTrangThaiHoaDonRequest request);

    HoaDonDetailResponse capNhatSanPhamHoaDon(Integer id, CapNhatSanPhamHoaDonRequest request);

    TinhPhiVanChuyenGhnResponse tinhVaCapNhatPhiVanChuyenGhn(Integer id, TinhPhiVanChuyenGhnRequest request);

    HoaDonDetailResponse xacNhanThanhToanCod(Integer id, XacNhanThanhToanCodRequest request);

    HoaDonDetailResponse xacNhanHoanTien(Integer id, XacNhanHoanTienRequest request);

    List<HoaDonSummaryResponse> layDanhSachHoaDonTheoKhachHang(UUID khachHangId);
}
