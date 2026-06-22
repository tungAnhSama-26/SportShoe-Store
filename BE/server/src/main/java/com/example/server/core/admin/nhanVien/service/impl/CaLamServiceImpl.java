package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.CaLamRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.CaLamResponse;
import com.example.server.core.admin.nhanVien.service.CaLamService;
import com.example.server.entity.CaLam;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.CaLamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CaLamServiceImpl implements CaLamService {

    private final CaLamRepository caLamRepository;

    public CaLamServiceImpl(CaLamRepository caLamRepository) {
        this.caLamRepository = caLamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaLamResponse> layDanhSachCaLam() {
        return caLamRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CaLamResponse taoCaLam(CaLamRequest request) {
        if (request.id() != null && caLamRepository.existsById(request.id())) {
            throw new BusinessException("Mã ca làm việc đã tồn tại");
        }
        
        CaLam caLam = new CaLam();
        String id = request.id();
        if (id == null || id.trim().isEmpty()) {
            long count = caLamRepository.count() + 1;
            id = "CA" + String.format("%05d", count);
            while (caLamRepository.existsById(id)) {
                count++;
                id = "CA" + String.format("%05d", count);
            }
        }
        caLam.setId(id);
        caLam.setTen(request.ten());
        caLam.setGioBatDau(request.gioBatDau());
        caLam.setGioKetThuc(request.gioKetThuc());
        caLam.setTrangThai(request.trangThai());

        caLamRepository.save(caLam);
        return toResponse(caLam);
    }

    @Override
    @Transactional
    public CaLamResponse capNhatCaLam(String id, CaLamRequest request) {
        CaLam caLam = caLamRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ca làm việc"));

        caLam.setTen(request.ten());
        caLam.setGioBatDau(request.gioBatDau());
        caLam.setGioKetThuc(request.gioKetThuc());
        caLam.setTrangThai(request.trangThai());

        caLamRepository.save(caLam);
        return toResponse(caLam);
    }

    @Override
    @Transactional
    public void xoaCaLam(String id) {
        if (!caLamRepository.existsById(id)) {
            throw new BusinessException("Không tìm thấy ca làm việc");
        }
        if (List.of("sang", "chieu", "toi").contains(id.trim().toLowerCase())) {
            throw new BusinessException("Không thể xóa ca làm việc mặc định");
        }
        caLamRepository.deleteById(id);
    }

    private CaLamResponse toResponse(CaLam caLam) {
        return new CaLamResponse(
                caLam.getId(),
                caLam.getTen(),
                caLam.getGioBatDau(),
                caLam.getGioKetThuc(),
                caLam.getTrangThai()
        );
    }
}
