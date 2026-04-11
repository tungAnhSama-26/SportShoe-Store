package com.example.server.core.admin.quanlyhoadon.service;

import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatSanPhamHoaDonRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatTrangThaiHoaDonRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonDetailResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonSummaryResponse;
import java.time.LocalDate;
import java.util.List;

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
}
