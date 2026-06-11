package com.example.server.core.client.taikhoannganhang.service;

import com.example.server.core.client.taikhoannganhang.dto.TaiKhoanNganHangRequest;
import com.example.server.core.client.taikhoannganhang.dto.TaiKhoanNganHangResponse;

import java.util.List;
import java.util.UUID;

public interface ClientTaiKhoanNganHangService {

    List<TaiKhoanNganHangResponse> layDanhSach(UUID khachHangId);

    TaiKhoanNganHangResponse themMoi(UUID khachHangId, TaiKhoanNganHangRequest request);

    TaiKhoanNganHangResponse capNhat(UUID khachHangId, Integer id, TaiKhoanNganHangRequest request);

    void xoa(UUID khachHangId, Integer id);

    TaiKhoanNganHangResponse datMacDinh(UUID khachHangId, Integer id);
}
