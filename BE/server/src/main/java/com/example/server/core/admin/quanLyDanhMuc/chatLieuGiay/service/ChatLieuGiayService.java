package com.example.server.core.admin.quanLyDanhMuc.chatLieuGiay.service;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.chatLieuGiay.dto.request.ChatLieuGiayRequest;
import com.example.server.core.admin.quanLyDanhMuc.chatLieuGiay.dto.response.ChatLieuGiayResponse;
import com.example.server.entity.ChatLieuGiay;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.ChatLieuGiayRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatLieuGiayService {

    private final ChatLieuGiayRepository chatLieuGiayRepository;

    public ChatLieuGiayService(ChatLieuGiayRepository chatLieuGiayRepository) {
        this.chatLieuGiayRepository = chatLieuGiayRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatLieuGiayResponse> danhSachChatLieuGiay(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(chatLieuGiayRepository.search(kw, pageable).map(this::toChatLieuGiay));
    }

    @Transactional(readOnly = true)
    public ChatLieuGiayResponse chiTietChatLieuGiay(Integer id) {
        return toChatLieuGiay(chatLieuGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chất liệu giày #" + id + " không tồn tại")));
    }

    @Transactional
    public ChatLieuGiayResponse taoChatLieuGiay(ChatLieuGiayRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (chatLieuGiayRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("Mã chất liệu giày '" + ma + "' đã tồn tại");
        }

        String ten = req.ten().trim();
        var existingOpt = chatLieuGiayRepository.findByTenIgnoreCase(req.ten().trim());
        if (existingOpt.isPresent()) {
            ChatLieuGiay existing = existingOpt.get();
            if (existing.getTrangThai() != null && existing.getTrangThai() == 1) {
                throw new BusinessException("Chất liệu giày '" + ten + "' đã tồn tại và đang hoạt động.");
            } else {
                throw new BusinessException("Chất liệu giày '" + ten + "' đã tồn tại nhưng đang ngừng hoạt động. Vui lòng kích hoạt lại trong Quản lý thuộc tính.");
            }
        }

        var entity = new ChatLieuGiay();
        entity.setMa(ma);
        entity.setTen(ten);
        entity.setMoTa(req.moTa());
        entity.setTrangThai(1);
        entity.setNgayTao(Instant.now());
        return toChatLieuGiay(chatLieuGiayRepository.save(entity));
    }

    @Transactional
    public ChatLieuGiayResponse capNhatChatLieuGiay(Integer id, ChatLieuGiayRequest req) {
        var entity = chatLieuGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chất liệu giày #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (chatLieuGiayRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("Mã chất liệu giày '" + ma + "' đã tồn tại");
        }

        String ten = req.ten().trim();
        if (chatLieuGiayRepository.existsByTenIgnoreCaseAndIdNot(ten, id)) {
            throw new BusinessException("Chất liệu giày '" + ten + "' đã tồn tại");
        }

        entity.setMa(ma);
        entity.setTen(req.ten().trim());
        entity.setMoTa(req.moTa());
        entity.setNgayCapNhat(Instant.now());
        return toChatLieuGiay(entity);
    }

    @Transactional
    public void doiTrangThaiChatLieuGiay(Integer id, DoiTrangThaiDanhMucRequest req) {
        var entity = chatLieuGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chất liệu giày #" + id + " không tồn tại"));
        entity.setTrangThai(req.trangThai());
        entity.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaChatLieuGiay(Integer id) {
        if (!chatLieuGiayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Chất liệu giày #" + id + " không tồn tại");
        }
        chatLieuGiayRepository.deleteById(id);
    }

    private ChatLieuGiayResponse toChatLieuGiay(ChatLieuGiay entity) {
        return new ChatLieuGiayResponse(
                entity.getId(),
                entity.getMa(),
                entity.getTen(),
                entity.getMoTa(),
                entity.getTrangThai(),
                entity.getNgayTao(),
                entity.getNgayCapNhat()
        );
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
