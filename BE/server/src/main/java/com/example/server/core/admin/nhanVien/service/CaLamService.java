package com.example.server.core.admin.nhanVien.service;

import com.example.server.core.admin.nhanVien.dto.request.CaLamRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiTrangThaiCaLamRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.CaLamResponse;

import java.util.List;

public interface CaLamService {
    List<CaLamResponse> layDanhSachCaLam();
    CaLamResponse taoCaLam(CaLamRequest request);
    CaLamResponse capNhatCaLam(String id, CaLamRequest request);
    CaLamResponse doiTrangThaiCaLam(String id, DoiTrangThaiCaLamRequest request);
    void xoaCaLam(String id);
}
