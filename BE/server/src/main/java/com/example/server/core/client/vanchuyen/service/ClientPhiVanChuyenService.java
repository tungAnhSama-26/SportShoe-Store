package com.example.server.core.client.vanchuyen.service;

import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.core.client.dathang.service.ClientCheckoutItemService;
import com.example.server.core.client.vanchuyen.dto.PhiVanChuyenResponse;
import com.example.server.core.client.vanchuyen.dto.TinhPhiShipRequest;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
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
    private final ClientCheckoutItemService checkoutItemService;

    public ClientPhiVanChuyenService(
            GhnShippingService ghnShippingService,
            ClientCheckoutItemService checkoutItemService
    ) {
        this.ghnShippingService = ghnShippingService;
        this.checkoutItemService = checkoutItemService;
    }

    @Transactional(readOnly = true)
    public PhiVanChuyenResponse tinhPhi(TinhPhiShipRequest request) {
        ClientCheckoutItemService.KetQua checkout = checkoutItemService.chuanBi(request.sanPhams());
        List<HoaDonChiTiet> items = checkout.chiTiets();
        HoaDon hoaDonTam = new HoaDon();
        hoaDonTam.setTongTienHang(checkout.tongTienHang());

        // Ưu tiên mã quận/huyện + phường/xã GHN (chọn từ dropdown) -> phí chính xác, khỏi dò tên.
        Integer districtId = request.toDistrictId();
        String wardCode = request.toWardCode();
        boolean coIdGhn = districtId != null && districtId > 0
                && wardCode != null && !wardCode.isBlank();

        String toAddress = Stream.of(
                        request.diaChiCuThe(),
                        request.phuongXa(),
                        request.quanHuyen(),
                        request.tinhThanh()
                )
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
        if (!coIdGhn && toAddress.isBlank()) {
            return new PhiVanChuyenResponse(PHI_MAC_DINH, true, "Phí ước tính");
        }

        TinhPhiVanChuyenGhnRequest ghnRequest = new TinhPhiVanChuyenGhnRequest(
                coIdGhn ? districtId : null,
                coIdGhn ? wardCode.trim() : null,
                toAddress,
                null, null, null, null, null, null, null, null
        );
        try {
            TinhPhiVanChuyenGhnResponse ghn = ghnShippingService.tinhPhi(hoaDonTam, items, ghnRequest);
            BigDecimal phi = ghn.phiVanChuyen() != null ? ghn.phiVanChuyen() : PHI_MAC_DINH;
            return new PhiVanChuyenResponse(phi, false, "Phí GHN");
        } catch (RuntimeException e) {
            // GHN chưa cấu hình token/shop-id hoặc không khớp địa chỉ -> dùng phí ước tính.
            return new PhiVanChuyenResponse(PHI_MAC_DINH, true, "Phí ước tính");
        }
    }
}
