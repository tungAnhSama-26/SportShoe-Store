package com.example.server.core.admin.nhanVien.service;

import com.example.server.core.admin.nhanVien.dto.request.CapNhatNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.nhanVien.dto.request.TaoNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.NhanVienResponses.NhanVienResponse;

import java.util.List;
import java.util.UUID;

public interface NhanVienService {
    List<NhanVienResponse> layDanhSach(String keyword, Integer vaiTro, Integer trangThai);
    NhanVienResponse layChiTiet(UUID id);
    NhanVienResponse taoNhanVien(TaoNhanVienRequest request);
    NhanVienResponse capNhatNhanVien(UUID id, CapNhatNhanVienRequest request);
    NhanVienResponse doiTrangThai(UUID id, DoiTrangThaiRequest request);
    NhanVienResponse doiMatKhau(UUID id, DoiMatKhauRequest request);
    void xoaNhanVien(UUID id);
}
