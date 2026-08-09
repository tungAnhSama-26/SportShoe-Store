package com.example.server.core.client.vanchuyen.service;

import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.core.admin.quanlyhoadon.service.GhnOfflineFeeService;
import com.example.server.core.client.dathang.service.ClientCheckoutItemService;
import com.example.server.core.client.vanchuyen.dto.PhiVanChuyenResponse;
import com.example.server.core.client.vanchuyen.dto.TinhPhiShipRequest;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.infrastructure.exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tính phí vận chuyển cho giỏ hàng phía khách bằng cách TÁI DÙNG {@link GhnShippingService}.
 *
 * <p>Service này chỉ ĐỌC giỏ hàng (hóa đơn đang mở) để lấy danh sách sản phẩm; không
 * chỉnh sửa hóa đơn. Lỗi cấu hình, lỗi ánh xạ hoặc GHN không hỗ trợ tuyến được trả rõ
 * cho client; không âm thầm thay bằng một mức phí mặc định.</p>
 */
@Service
public class ClientPhiVanChuyenService {

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

        if (request.diaChiGiaoHang() == null) {
            throw new BusinessException("Thiếu địa chỉ giao hàng 2 cấp");
        }

        TinhPhiVanChuyenGhnRequest ghnRequest = new TinhPhiVanChuyenGhnRequest(
                request.diaChiGiaoHang(),
                null, null, null, null, null, null, null, null
        );
        TinhPhiVanChuyenGhnResponse ghn = ghnShippingService.tinhPhi(hoaDonTam, items, ghnRequest);
        BigDecimal phi = ghn.phiVanChuyen() != null ? ghn.phiVanChuyen() : BigDecimal.ZERO;
        return new PhiVanChuyenResponse(
                phi,
                ghn.uocTinh(),
                moTaNguonPhi(ghn),
                ghn.nguonTinhPhi(),
                ghn.giaCu(),
                ghn.thoiDiemBaoGia(),
                ghn.ngayHieuLucBangGia()
        );
    }

    private String moTaNguonPhi(TinhPhiVanChuyenGhnResponse response) {
        if (GhnOfflineFeeService.SOURCE_CACHE.equals(response.nguonTinhPhi())) {
            return response.giaCu() ? "Phí GHN từ cache cũ (ước tính)" : "Phí GHN đã lưu gần nhất (ước tính)";
        }
        if (GhnOfflineFeeService.SOURCE_PUBLIC_TARIFF.equals(response.nguonTinhPhi())) {
            return "Phí offline ước tính theo bảng giá công khai GHN";
        }
        return response.uocTinh() ? "Phí GHN ước tính theo các tuyến cũ" : "Phí GHN";
    }
}
