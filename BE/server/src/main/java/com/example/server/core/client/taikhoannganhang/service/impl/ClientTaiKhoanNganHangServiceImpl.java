package com.example.server.core.client.taikhoannganhang.service.impl;

import com.example.server.core.client.taikhoannganhang.dto.TaiKhoanNganHangRequest;
import com.example.server.core.client.taikhoannganhang.dto.TaiKhoanNganHangResponse;
import com.example.server.core.client.taikhoannganhang.service.ClientTaiKhoanNganHangService;
import com.example.server.entity.KhachHang;
import com.example.server.entity.TaiKhoanNganHang;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.TaiKhoanNganHangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientTaiKhoanNganHangServiceImpl implements ClientTaiKhoanNganHangService {

    private final TaiKhoanNganHangRepository repository;
    private final KhachHangRepository khachHangRepository;

    public ClientTaiKhoanNganHangServiceImpl(TaiKhoanNganHangRepository repository, KhachHangRepository khachHangRepository) {
        this.repository = repository;
        this.khachHangRepository = khachHangRepository;
    }

    private KhachHang findKhachHang(UUID khachHangId) {
        return khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng"));
    }

    private TaiKhoanNganHangResponse toResponse(TaiKhoanNganHang entity) {
        return new TaiKhoanNganHangResponse(
                entity.getId(),
                entity.getKhachHang().getId(),
                entity.getTenNganHang(),
                entity.getSoTaiKhoan(),
                entity.getTenChuTaiKhoan(),
                entity.getChiNhanh(),
                entity.getLaMacDinh(),
                entity.getNgayTao()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaiKhoanNganHangResponse> layDanhSach(UUID khachHangId) {
        findKhachHang(khachHangId); // Kiểm tra khách hàng tồn tại
        return repository.findByKhachHangIdOrderByLaMacDinhDescNgayTaoDesc(khachHangId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TaiKhoanNganHangResponse themMoi(UUID khachHangId, TaiKhoanNganHangRequest request) {
        KhachHang khachHang = findKhachHang(khachHangId);
        List<TaiKhoanNganHang> existingAccounts = repository.findByKhachHangIdOrderByLaMacDinhDescNgayTaoDesc(khachHangId);

        boolean setAsDefault = request.laMacDinh() != null ? request.laMacDinh() : false;
        // Nếu là tài khoản đầu tiên, bắt buộc đặt làm mặc định
        if (existingAccounts.isEmpty()) {
            setAsDefault = true;
        }

        if (setAsDefault) {
            resetDefaultAccounts(khachHangId);
        }

        TaiKhoanNganHang entity = new TaiKhoanNganHang();
        entity.setKhachHang(khachHang);
        entity.setTenNganHang(request.tenNganHang().trim());
        entity.setSoTaiKhoan(request.soTaiKhoan().trim());
        entity.setTenChuTaiKhoan(request.tenChuTaiKhoan().trim().toUpperCase());
        entity.setChiNhanh(request.chiNhanh() != null ? request.chiNhanh().trim() : null);
        entity.setLaMacDinh(setAsDefault);
        entity.setNgayTao(Instant.now());
        entity.setDeleted(false);

        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public TaiKhoanNganHangResponse capNhat(UUID khachHangId, Integer id, TaiKhoanNganHangRequest request) {
        findKhachHang(khachHangId);
        TaiKhoanNganHang entity = repository.findByIdAndKhachHangId(id, khachHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản ngân hàng"));

        boolean setAsDefault = request.laMacDinh() != null ? request.laMacDinh() : false;

        if (setAsDefault && !entity.getLaMacDinh()) {
            resetDefaultAccounts(khachHangId);
            entity.setLaMacDinh(true);
        } else if (!setAsDefault && entity.getLaMacDinh()) {
            // Không được tự bỏ mặc định nếu đây là tài khoản duy nhất
            List<TaiKhoanNganHang> list = repository.findByKhachHangIdOrderByLaMacDinhDescNgayTaoDesc(khachHangId);
            if (list.size() > 1) {
                entity.setLaMacDinh(false);
                // Chọn một tài khoản khác làm mặc định (ví dụ tài khoản đầu tiên còn lại)
                Optional<TaiKhoanNganHang> other = list.stream().filter(x -> !x.getId().equals(id)).findFirst();
                if (other.isPresent()) {
                    other.get().setLaMacDinh(true);
                    repository.save(other.get());
                }
            } else {
                entity.setLaMacDinh(true); // Vẫn bắt buộc giữ là mặc định
            }
        }

        entity.setTenNganHang(request.tenNganHang().trim());
        entity.setSoTaiKhoan(request.soTaiKhoan().trim());
        entity.setTenChuTaiKhoan(request.tenChuTaiKhoan().trim().toUpperCase());
        entity.setChiNhanh(request.chiNhanh() != null ? request.chiNhanh().trim() : null);
        entity.setNgayCapNhat(Instant.now());

        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void xoa(UUID khachHangId, Integer id) {
        findKhachHang(khachHangId);
        TaiKhoanNganHang entity = repository.findByIdAndKhachHangId(id, khachHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản ngân hàng"));

        entity.setDeleted(true);
        entity.setNgayCapNhat(Instant.now());
        repository.save(entity);

        // Nếu tài khoản bị xóa là tài khoản mặc định, chọn tài khoản khác làm mặc định
        if (entity.getLaMacDinh()) {
            entity.setLaMacDinh(false);
            List<TaiKhoanNganHang> remaining = repository.findByKhachHangIdOrderByLaMacDinhDescNgayTaoDesc(khachHangId);
            if (!remaining.isEmpty()) {
                TaiKhoanNganHang newDefault = remaining.get(0);
                newDefault.setLaMacDinh(true);
                repository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional
    public TaiKhoanNganHangResponse datMacDinh(UUID khachHangId, Integer id) {
        findKhachHang(khachHangId);
        TaiKhoanNganHang entity = repository.findByIdAndKhachHangId(id, khachHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản ngân hàng"));

        if (!entity.getLaMacDinh()) {
            resetDefaultAccounts(khachHangId);
            entity.setLaMacDinh(true);
            entity.setNgayCapNhat(Instant.now());
            repository.save(entity);
        }

        return toResponse(entity);
    }

    private void resetDefaultAccounts(UUID khachHangId) {
        List<TaiKhoanNganHang> list = repository.findByKhachHangIdOrderByLaMacDinhDescNgayTaoDesc(khachHangId);
        boolean changed = false;
        for (TaiKhoanNganHang account : list) {
            if (account.getLaMacDinh()) {
                account.setLaMacDinh(false);
                repository.save(account);
                changed = true;
            }
        }
        if (changed) {
            repository.flush();
        }
    }
}
