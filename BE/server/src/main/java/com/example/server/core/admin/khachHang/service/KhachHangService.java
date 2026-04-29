package com.example.server.core.admin.khachHang.service;

import com.example.server.core.admin.khachHang.dto.request.CapNhatKhachHangRequest;
import com.example.server.core.admin.khachHang.dto.request.DiaChiRequest;
import com.example.server.core.admin.khachHang.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.khachHang.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.khachHang.dto.request.TaoKhachHangRequest;
import com.example.server.core.admin.khachHang.dto.responsse.DiaChiResponse;
import com.example.server.core.admin.khachHang.dto.responsse.KhachHangResponse;

import java.util.List;
import java.util.UUID;

public interface KhachHangService {
    List<KhachHangResponse> layDanhSach(String keyword, Integer trangThai);
    KhachHangResponse layChiTiet(UUID id);
    KhachHangResponse taoKhachHang(TaoKhachHangRequest request);
    KhachHangResponse capNhatKhachHang(UUID id, CapNhatKhachHangRequest request);
    KhachHangResponse doiTrangThai(UUID id, DoiTrangThaiRequest request);
    KhachHangResponse doiMatKhau(UUID id, DoiMatKhauRequest request);
    void xoaKhachHang(UUID id);

    // Address management
    List<DiaChiResponse> layDanhSachDiaChi(UUID khachHangId);
    DiaChiResponse layChiTietDiaChi(Integer diaChiId);
    DiaChiResponse themDiaChi(UUID khachHangId, DiaChiRequest request);
    DiaChiResponse capNhatDiaChi(Integer diaChiId, DiaChiRequest request);
    void xoaDiaChi(Integer diaChiId);
    void datMacDinhDiaChi(Integer diaChiId);
}
