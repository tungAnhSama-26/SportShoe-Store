package com.example.server.core.khachhang.danhmuc;

import com.example.server.entity.enums.ActiveStatus;
import com.example.server.infrastructure.dto.CatalogOptionResponse;
import com.example.server.infrastructure.dto.CatalogOptionsResponse;
import com.example.server.repository.ChatLieuRepository;
import com.example.server.repository.KichCoRepository;
import com.example.server.repository.LoaiGiayRepository;
import com.example.server.repository.MauSacRepository;
import com.example.server.repository.ThuongHieuRepository;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class KhachHangDanhMucService {

    private final ThuongHieuRepository thuongHieuRepository;
    private final LoaiGiayRepository loaiGiayRepository;
    private final ChatLieuRepository chatLieuRepository;
    private final MauSacRepository mauSacRepository;
    private final KichCoRepository kichCoRepository;

    public KhachHangDanhMucService(
            ThuongHieuRepository thuongHieuRepository,
            LoaiGiayRepository loaiGiayRepository,
            ChatLieuRepository chatLieuRepository,
            MauSacRepository mauSacRepository,
            KichCoRepository kichCoRepository
    ) {
        this.thuongHieuRepository = thuongHieuRepository;
        this.loaiGiayRepository = loaiGiayRepository;
        this.chatLieuRepository = chatLieuRepository;
        this.mauSacRepository = mauSacRepository;
        this.kichCoRepository = kichCoRepository;
    }

    public CatalogOptionsResponse getOptions() {
        return new CatalogOptionsResponse(
                thuongHieuRepository.findByDeletedFalseAndStatusOrderByNameAsc(ActiveStatus.ACTIVE)
                        .stream()
                        .map(item -> new CatalogOptionResponse(item.getId(), item.getCode(), item.getName(), item.getDescription()))
                        .collect(Collectors.toList()),
                loaiGiayRepository.findByDeletedFalseAndStatusOrderByNameAsc(ActiveStatus.ACTIVE)
                        .stream()
                        .map(item -> new CatalogOptionResponse(item.getId(), item.getCode(), item.getName(), item.getDescription()))
                        .collect(Collectors.toList()),
                chatLieuRepository.findByDeletedFalseAndStatusOrderByNameAsc(ActiveStatus.ACTIVE)
                        .stream()
                        .map(item -> new CatalogOptionResponse(item.getId(), item.getCode(), item.getName(), item.getDescription()))
                        .collect(Collectors.toList()),
                mauSacRepository.findByDeletedFalseOrderByNameAsc()
                        .stream()
                        .map(item -> new CatalogOptionResponse(item.getId(), item.getCode(), item.getName(), item.getHexCode()))
                        .collect(Collectors.toList()),
                kichCoRepository.findByDeletedFalseOrderByValueAsc()
                        .stream()
                        .map(item -> new CatalogOptionResponse(item.getId(), item.getValue(), item.getValue(), item.getNote()))
                        .collect(Collectors.toList())
        );
    }
}
