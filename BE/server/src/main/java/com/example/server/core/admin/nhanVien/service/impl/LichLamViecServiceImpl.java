package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.PhanCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.LichLamViecResponse;
import com.example.server.core.admin.nhanVien.service.LichLamViecService;
import com.example.server.entity.CaLam;
import com.example.server.entity.LichLamViec;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.CaLamRepository;
import com.example.server.repository.LichLamViecRepository;
import com.example.server.repository.NhanVienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LichLamViecServiceImpl implements LichLamViecService {

    private static final int MAX_NHAN_VIEN_MOI_CA = 3;

    private final LichLamViecRepository lichLamViecRepository;
    private final NhanVienRepository nhanVienRepository;
    private final CaLamRepository caLamRepository;

    public LichLamViecServiceImpl(
            LichLamViecRepository lichLamViecRepository,
            NhanVienRepository nhanVienRepository,
            CaLamRepository caLamRepository
    ) {
        this.lichLamViecRepository = lichLamViecRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.caLamRepository = caLamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichLamViecResponse> layLichLamViecTheoTuan(LocalDate tuNgay, LocalDate denNgay) {
        return lichLamViecRepository.findByNgayBetween(tuNgay, denNgay).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public LichLamViecResponse phanCa(PhanCaRequest request) {
        NhanVien nhanVien = nhanVienRepository.findById(request.nhanVienId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));
        if (!Integer.valueOf(1).equals(nhanVien.getTrangThai())) {
            throw new BusinessException("Không thể phân ca cho nhân viên đã nghỉ việc");
        }

        String caLamId = request.caLamId().trim();
        CaLam caLam = caLamRepository.findByIdForUpdate(caLamId)
                .or(() -> caLamRepository.findByIdForUpdate(caLamId.toLowerCase()))
                .filter(ca -> Boolean.TRUE.equals(ca.getTrangThai()))
                .orElseThrow(() -> new BusinessException("Ca làm việc không tồn tại hoặc đã ngừng hoạt động"));

        if (lichLamViecRepository.existsByNhanVienIdAndNgayAndCaLamId(
                request.nhanVienId(), request.ngay(), caLam.getId())) {
            throw new BusinessException("Nhân viên đã được phân vào ca này trong ngày đã chọn");
        }

        long soNhanVien = lichLamViecRepository.countByNgayAndCaLamId(request.ngay(), caLam.getId());
        if (soNhanVien >= MAX_NHAN_VIEN_MOI_CA) {
            String ngay = request.ngay().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            throw new BusinessException("Ca " + caLam.getTen() + " ngày " + ngay
                    + " đã đạt số lượng tối đa " + MAX_NHAN_VIEN_MOI_CA + " người");
        }

        kiemTraKhongChongGio(
                lichLamViecRepository.findByNhanVienIdAndNgay(request.nhanVienId(), request.ngay()),
                caLam
        );

        LichLamViec lich = new LichLamViec();
        lich.setNhanVien(nhanVien);
        lich.setNgay(request.ngay());
        lich.setCaLam(caLam);
        return toResponse(lichLamViecRepository.save(lich));
    }

    @Override
    @Transactional
    public void xoaLich(UUID id) {
        LichLamViec lich = lichLamViecRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch làm việc không tồn tại"));
        lichLamViecRepository.delete(lich);
    }

    @Override
    @Transactional
    public void xepCaTuDong(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new BusinessException("Khoảng thời gian không hợp lệ");
        }

        List<CaLam> cacCa = caLamRepository.findAll().stream()
                .filter(ca -> Boolean.TRUE.equals(ca.getTrangThai()))
                .sorted(Comparator.comparing(ca -> LocalTime.parse(ca.getGioBatDau())))
                .toList();
        if (cacCa.isEmpty()) {
            throw new BusinessException("Chưa có ca làm việc đang hoạt động");
        }

        lichLamViecRepository.deleteByNgayBetween(tuNgay, denNgay);
        lichLamViecRepository.flush();

        List<NhanVien> nhanViens = nhanVienRepository.findAll().stream()
                .filter(nv -> Integer.valueOf(1).equals(nv.getTrangThai()))
                .filter(nv -> Integer.valueOf(2).equals(nv.getVaiTro()))
                .toList();

        int chuKy = cacCa.size() + 1; // thêm một lượt nghỉ để duy trì xoay ca an toàn
        for (LocalDate ngay = tuNgay; !ngay.isAfter(denNgay); ngay = ngay.plusDays(1)) {
            Map<String, Integer> soNguoiTheoCa = new HashMap<>();
            for (int i = 0; i < nhanViens.size(); i++) {
                int viTri = (i + ngay.getDayOfYear()) % chuKy;
                if (viTri == cacCa.size()) {
                    continue;
                }
                CaLam caLam = cacCa.get(viTri);
                int soNguoi = soNguoiTheoCa.getOrDefault(caLam.getId(), 0);
                if (soNguoi < MAX_NHAN_VIEN_MOI_CA) {
                    saveLich(nhanViens.get(i), ngay, caLam);
                    soNguoiTheoCa.put(caLam.getId(), soNguoi + 1);
                }
            }
        }
    }

    private void kiemTraKhongChongGio(List<LichLamViec> lichTrongNgay, CaLam caMoi) {
        LocalTime batDauMoi = LocalTime.parse(caMoi.getGioBatDau());
        LocalTime ketThucMoi = LocalTime.parse(caMoi.getGioKetThuc());
        for (LichLamViec lich : lichTrongNgay) {
            CaLam caCu = lich.getCaLam();
            LocalTime batDauCu = LocalTime.parse(caCu.getGioBatDau());
            LocalTime ketThucCu = LocalTime.parse(caCu.getGioKetThuc());
            if (batDauMoi.isBefore(ketThucCu) && batDauCu.isBefore(ketThucMoi)) {
                throw new BusinessException("Ca " + caMoi.getTen() + " bị chồng giờ với " + caCu.getTen());
            }
        }
    }

    private void saveLich(NhanVien nhanVien, LocalDate ngay, CaLam caLam) {
        LichLamViec lich = new LichLamViec();
        lich.setNhanVien(nhanVien);
        lich.setNgay(ngay);
        lich.setCaLam(caLam);
        lichLamViecRepository.save(lich);
    }

    private LichLamViecResponse toResponse(LichLamViec lich) {
        return new LichLamViecResponse(
                lich.getId(),
                lich.getNhanVien().getId(),
                lich.getNgay(),
                lich.getCaLam().getId()
        );
    }
}
