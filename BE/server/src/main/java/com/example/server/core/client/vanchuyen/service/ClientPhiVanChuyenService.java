package com.example.server.core.client.vanchuyen.service;

import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.core.client.giohang.service.ClientGioHangService;
import com.example.server.core.client.vanchuyen.dto.PhiVanChuyenResponse;
import com.example.server.core.client.vanchuyen.dto.TinhPhiShipRequest;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.HoaDonChiTietRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tính phí vận chuyển cho giỏ hàng phía khách bằng cách TÁI DÙNG {@link GhnShippingService}.
 *
 * <p>Service này chỉ ĐỌC giỏ hàng (hóa đơn đang mở) để lấy danh sách sản phẩm; không
 * chỉnh sửa hóa đơn. Nếu GHN chưa được cấu hình hoặc địa chỉ chưa khớp dữ liệu GHN,
 * trả về một mức phí ước tính để giao diện thanh toán vẫn hiển thị được tiền ship.</p>
 */
@Service
public class ClientPhiVanChuyenService {

    /** Phí ước tính khi không gọi được GHN (đồng). */
    private static final BigDecimal PHI_MAC_DINH = BigDecimal.valueOf(30_000);

    private final GhnShippingService ghnShippingService;
    private final ClientGioHangService gioHangService;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    public ClientPhiVanChuyenService(
            GhnShippingService ghnShippingService,
            ClientGioHangService gioHangService,
            HoaDonChiTietRepository hoaDonChiTietRepository
    ) {
        this.ghnShippingService = ghnShippingService;
        this.gioHangService = gioHangService;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
    }

    @Transactional(readOnly = true)
    public PhiVanChuyenResponse tinhPhi(TinhPhiShipRequest request) {
        HoaDon gio = gioHangService.timGioHang(request.khachHangId())
                .orElseThrow(() -> new BusinessException("Giỏ hàng đang trống"));
        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByHoaDonId(gio.getId());
        if (items.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống");
        }

        String toAddress = Stream.of(
                        request.diaChiCuThe(),
                        request.phuongXa(),
                        request.quanHuyen(),
                        request.tinhThanh()
                )
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
        if (toAddress.isBlank()) {
            return new PhiVanChuyenResponse(PHI_MAC_DINH, true, "Phí ước tính");
        }

        TinhPhiVanChuyenGhnRequest ghnRequest = new TinhPhiVanChuyenGhnRequest(
                null, null, toAddress,
                null, null, null, null, null, null, null, null
        );
        try {
            TinhPhiVanChuyenGhnResponse ghn = ghnShippingService.tinhPhi(gio, items, ghnRequest);
            BigDecimal phi = ghn.phiVanChuyen() != null ? ghn.phiVanChuyen() : PHI_MAC_DINH;
            return new PhiVanChuyenResponse(phi, false, "Phí GHN");
        } catch (RuntimeException e) {
            // GHN chưa cấu hình token/shop-id hoặc không khớp địa chỉ -> dùng phí ước tính.
            return new PhiVanChuyenResponse(PHI_MAC_DINH, true, "Phí ước tính");
        }
    }
}
