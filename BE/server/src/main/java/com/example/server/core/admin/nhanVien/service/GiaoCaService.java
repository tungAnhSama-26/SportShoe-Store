package com.example.server.core.admin.nhanVien.service;

import com.example.server.core.admin.nhanVien.dto.request.MoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.BanGiaoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.XacNhanBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.request.KetCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaOptionsResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GiaoCaService {

    GiaoCaResponse layCaHoatDong(UUID nhanVienId);

    void kiemTraQuyenMoCa(UUID nhanVienId);

    GiaoCaResponse moCa(UUID nhanVienId, MoCaRequest request);

    GiaoCaStatsResponse layThongTinGiaoCaCurrent(UUID nhanVienId);

    GiaoCaResponse banGiaoCa(UUID nhanVienId, BanGiaoCaRequest request);

    GiaoCaOptionsResponse layTuyChonBanGiao(UUID nhanVienId);

    GiaoCaResponse ketCa(UUID nhanVienId, KetCaRequest request);

    List<GiaoCaResponse> layCaChoXacNhan(UUID nhanVienId);

    GiaoCaResponse xacNhanBanGiao(UUID nhanVienId, UUID giaoCaId, XacNhanBanGiaoRequest request);

    void checkActiveShiftOrThrow(UUID nhanVienId);

    Page<GiaoCaResponse> layLichSuGiaoCa(
            UUID nhanVienId,
            String trangThai,
            Instant tuNgay,
            Instant denNgay,
            Pageable pageable
    );
}
