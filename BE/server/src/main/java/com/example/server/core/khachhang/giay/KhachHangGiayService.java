package com.example.server.core.khachhang.giay;

import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.entity.ChatLieu;
import com.example.server.entity.DotGiamGia;
import com.example.server.entity.Giay;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HinhAnhGiay;
import com.example.server.entity.LoaiGiay;
import com.example.server.entity.ThuongHieu;
import com.example.server.infrastructure.dto.CatalogOptionResponse;
import com.example.server.infrastructure.dto.ProductDetailResponse;
import com.example.server.infrastructure.dto.ProductFilterRequest;
import com.example.server.infrastructure.dto.ProductImageResponse;
import com.example.server.infrastructure.dto.ProductSummaryResponse;
import com.example.server.infrastructure.dto.ProductVariantResponse;
import com.example.server.infrastructure.dto.PromotionResponse;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.GiayRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import com.example.server.utils.GiaySpecifications;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class KhachHangGiayService {

    private final GiayRepository giayRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final HinhAnhGiayRepository hinhAnhGiayRepository;

    public KhachHangGiayService(
            GiayRepository giayRepository,
            GiayChiTietRepository giayChiTietRepository,
            HinhAnhGiayRepository hinhAnhGiayRepository
    ) {
        this.giayRepository = giayRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
    }

    public Page<ProductSummaryResponse> getProducts(ProductFilterRequest filter) {
        PageRequest pageRequest = PageRequest.of(
                filter.resolvedPage(),
                filter.resolvedSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return giayRepository.findAll(
                GiaySpecifications.activeCatalog(
                        filter.keyword(),
                        filter.brandId(),
                        filter.categoryId(),
                        filter.materialId(),
                        filter.gender(),
                        filter.minPrice(),
                        filter.maxPrice()
                ),
                pageRequest
        ).map(this::toSummaryResponse);
    }

    public ProductDetailResponse getProductDetail(UUID productId) {
        Giay giay = giayRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<GiayChiTiet> bienThe = giayChiTietRepository.findByGiayIdAndDeletedFalseOrderByCreatedAtAsc(productId);
        Map<UUID, List<HinhAnhGiay>> hinhAnhTheoBienThe = getImagesByVariant(bienThe);

        return new ProductDetailResponse(
                giay.getId(),
                giay.getCode(),
                giay.getName(),
                giay.getDescription(),
                enumName(giay.getGender()),
                enumName(giay.getStatus()),
                toOption(giay.getThuongHieu()),
                giay.getLoaiGiay() == null ? null : toOption(giay.getLoaiGiay()),
                giay.getChatLieu() == null ? null : toOption(giay.getChatLieu()),
                giay.getDotGiamGia() == null ? null : toPromotion(giay.getDotGiamGia()),
                bienThe.stream()
                        .map(item -> toVariantResponse(item, hinhAnhTheoBienThe.getOrDefault(item.getId(), List.of())))
                        .toList()
        );
    }

    private Map<UUID, List<HinhAnhGiay>> getImagesByVariant(List<GiayChiTiet> bienThe) {
        if (bienThe.isEmpty()) {
            return Map.of();
        }
        List<UUID> variantIds = bienThe.stream().map(GiayChiTiet::getId).toList();
        return hinhAnhGiayRepository.findByGiayChiTietIdInAndDeletedFalseOrderByPrimaryImageDescCreatedAtAsc(variantIds)
                .stream()
                .collect(Collectors.groupingBy(image -> image.getGiayChiTiet().getId()));
    }

    private ProductSummaryResponse toSummaryResponse(Giay giay) {
        List<GiayChiTiet> bienThe = giayChiTietRepository.findByGiayIdAndDeletedFalseOrderByCreatedAtAsc(giay.getId());
        Map<UUID, List<HinhAnhGiay>> hinhAnhTheoBienThe = getImagesByVariant(bienThe);

        BigDecimal minPrice = bienThe.stream()
                .map(GiayChiTiet::getSalePrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = bienThe.stream()
                .map(GiayChiTiet::getSalePrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        String thumbnail = bienThe.stream()
                .map(GiayChiTiet::getId)
                .map(hinhAnhTheoBienThe::get)
                .filter(images -> images != null && !images.isEmpty())
                .map(images -> images.get(0).getUrl())
                .findFirst()
                .orElse(null);

        return new ProductSummaryResponse(
                giay.getId(),
                giay.getCode(),
                giay.getName(),
                enumName(giay.getGender()),
                enumName(giay.getStatus()),
                toOption(giay.getThuongHieu()),
                giay.getLoaiGiay() == null ? null : toOption(giay.getLoaiGiay()),
                giay.getChatLieu() == null ? null : toOption(giay.getChatLieu()),
                minPrice,
                maxPrice,
                thumbnail
        );
    }

    private ProductVariantResponse toVariantResponse(GiayChiTiet bienThe, List<HinhAnhGiay> hinhAnh) {
        return new ProductVariantResponse(
                bienThe.getId(),
                bienThe.getVariantCode(),
                bienThe.getSku(),
                bienThe.getQuantity(),
                bienThe.getOriginalPrice(),
                bienThe.getSalePrice(),
                bienThe.isActive(),
                new CatalogOptionResponse(bienThe.getMauSac().getId(), bienThe.getMauSac().getCode(), bienThe.getMauSac().getName(), bienThe.getMauSac().getHexCode()),
                new CatalogOptionResponse(bienThe.getKichCo().getId(), bienThe.getKichCo().getValue(), bienThe.getKichCo().getValue(), bienThe.getKichCo().getNote()),
                hinhAnh.stream()
                        .map(image -> new ProductImageResponse(
                                image.getId(),
                                enumName(image.getImageType()),
                                image.getUrl(),
                                image.getDescription(),
                                image.isPrimaryImage()
                        ))
                        .toList()
        );
    }

    private CatalogOptionResponse toOption(ThuongHieu thuongHieu) {
        return new CatalogOptionResponse(thuongHieu.getId(), thuongHieu.getCode(), thuongHieu.getName(), thuongHieu.getDescription());
    }

    private CatalogOptionResponse toOption(LoaiGiay loaiGiay) {
        return new CatalogOptionResponse(loaiGiay.getId(), loaiGiay.getCode(), loaiGiay.getName(), loaiGiay.getDescription());
    }

    private CatalogOptionResponse toOption(ChatLieu chatLieu) {
        return new CatalogOptionResponse(chatLieu.getId(), chatLieu.getCode(), chatLieu.getName(), chatLieu.getDescription());
    }

    private PromotionResponse toPromotion(DotGiamGia dotGiamGia) {
        return new PromotionResponse(
                dotGiamGia.getId(),
                dotGiamGia.getCode(),
                dotGiamGia.getName(),
                dotGiamGia.getDiscountPercent(),
                dotGiamGia.getDiscountAmount(),
                dotGiamGia.getStartAt(),
                dotGiamGia.getEndAt(),
                dotGiamGia.isActive()
        );
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
