package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ApDungPhieuGiamGiaRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.ThanhToanTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.TinhPhiVanChuyenTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoChiTietResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoTomTatResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.KhachHangTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.PhieuGiamGiaTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.SanPhamTaiQuayResponse;
import com.example.server.core.admin.banHangTaiQuay.dto.response.ThanhToanTaiQuayResponse;
import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.websocket.WebSocketNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BanHangTaiQuayService {

    private static final String POS_SYNC_TOPIC = "/topic/admin/pos-sync";


    private final KhachHangTaiQuayService customerUseCase;
    private final SanPhamTaiQuayService productUseCase;
    private final PhieuGiamGiaTaiQuayService voucherUseCase;
    private final HoaDonTaiQuayService invoiceUseCase;
    private final HoaDonChoTaiQuayService pendingInvoiceUseCase;
    private final ThucThiThanhToanTaiQuayService paymentExecutionUseCase;
    private final GhnShippingService ghnShippingService;
    private final WebSocketNotificationService webSocketNotificationService;

    public BanHangTaiQuayService(
            KhachHangTaiQuayService customerUseCase,
            SanPhamTaiQuayService productUseCase,
            PhieuGiamGiaTaiQuayService voucherUseCase,
            HoaDonTaiQuayService invoiceUseCase,
            GhnShippingService ghnShippingService,
            WebSocketNotificationService webSocketNotificationService,
            HoaDonChoTaiQuayService pendingInvoiceUseCase,
            ThucThiThanhToanTaiQuayService paymentExecutionUseCase
    ) {
        this.customerUseCase = customerUseCase;
        this.productUseCase = productUseCase;
        this.voucherUseCase = voucherUseCase;
        this.invoiceUseCase = invoiceUseCase;
        this.pendingInvoiceUseCase = pendingInvoiceUseCase;
        this.paymentExecutionUseCase = paymentExecutionUseCase;
        this.ghnShippingService = ghnShippingService;
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @Transactional(readOnly = true)
    public List<KhachHangTaiQuayResponse> timKhachHangTheoTuKhoa(String keyword) {
        return customerUseCase.timKhachHangTheoTuKhoa(keyword);
    }

    @Transactional(readOnly = true)
    public List<SanPhamTaiQuayResponse> timSanPham(String keyword) {
        return productUseCase.timSanPham(keyword);
    }

    @Transactional(readOnly = true)
    public List<PhieuGiamGiaTaiQuayResponse> timPhieuGiamGia(
            String keyword,
            Integer hoaDonId,
            UUID khachHangId,
            BigDecimal tongTienHang
    ) {
        HoaDon hoaDonHienTai = invoiceUseCase.layHoaDonTaiQuayNeuCo(hoaDonId);
        KhachHang khachHang = invoiceUseCase.timKhachHang(khachHangId);
        return voucherUseCase.timPhieuGiamGia(keyword, hoaDonHienTai, khachHang, tongTienHang);
    }

    @Transactional(readOnly = true)
    public PhieuGiamGiaTaiQuayResponse apDungPhieuGiamGia(ApDungPhieuGiamGiaRequest request) {
        HoaDon hoaDonHienTai = invoiceUseCase.layHoaDonTaiQuayNeuCo(request.hoaDonId());
        BigDecimal tongTienHang = voucherUseCase.xacDinhTongTienHangKhiApPhieu(
                request, hoaDonHienTai, invoiceUseCase.taoDanhSachDongHoaDonTam(request.items()));
        KhachHang khachHang = invoiceUseCase.timKhachHang(request.khachHangId());
        PhieuGiamGiaTaiQuayService.PhieuGiamGiaDuocApDung phieuGiamGia = voucherUseCase.tinhPhieuGiamGiaHopLe(
                request.maPhieuGiamGia(),
                khachHang,
                tongTienHang,
                true,
                hoaDonHienTai
        );
        return voucherUseCase.mapPhieuGiamGiaTaiQuayResponse(phieuGiamGia);
    }

    @Transactional(readOnly = true)
    public TinhPhiVanChuyenGhnResponse tinhPhiVanChuyenGhn(TinhPhiVanChuyenTaiQuayRequest request) {
        List<HoaDonChiTiet> items = invoiceUseCase.taoDanhSachDongHoaDonTam(request.items());
        HoaDon hoaDonTam = new HoaDon();
        hoaDonTam.setTongTienHang(
                items.stream()
                        .map(HoaDonChiTiet::getThanhTien)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        return ghnShippingService.tinhPhi(
                hoaDonTam,
                items,
                new TinhPhiVanChuyenGhnRequest(
                        request.diaChiGiaoHang(),
                        request.serviceId(),
                        request.serviceTypeId(),
                        request.length(),
                        request.width(),
                        request.height(),
                        request.weight(),
                        request.insuranceValue(),
                        request.coupon()
                )
        );
    }

    @Transactional
    public HoaDonChoChiTietResponse taoHoaDonCho(TaoHoaDonChoRequest request) {
        HoaDonChoChiTietResponse response = pendingInvoiceUseCase.taoHoaDonCho(request);
        phatRealtimeHoaDonCho("CREATED", response.id());
        return response;
    }

    @Transactional
    public HoaDonChoChiTietResponse capNhatHoaDonCho(Integer hoaDonId, TaoHoaDonChoRequest request) {
        HoaDonChoChiTietResponse response = pendingInvoiceUseCase.capNhatHoaDonCho(hoaDonId, request);
        phatRealtimeHoaDonCho("UPDATED", response.id());
        return response;
    }

    @Transactional
    public HoaDonChoChiTietResponse doiBienThe(Integer hoaDonChiTietId, com.example.server.core.admin.banHangTaiQuay.dto.request.DoiBienTheTaiQuayRequest request) {
        HoaDonChoChiTietResponse response = pendingInvoiceUseCase.doiBienThe(hoaDonChiTietId, request);
        phatRealtimeHoaDonCho("UPDATED", response.id());
        return response;
    }

    @Transactional
    public void huyHoaDonCho(Integer hoaDonId) {
        pendingInvoiceUseCase.huyHoaDonCho(hoaDonId);
        phatRealtimeHoaDonCho("CANCELLED", hoaDonId);
    }

    @Transactional
    public ThanhToanTaiQuayResponse thanhToanTaiQuay(ThanhToanTaiQuayRequest request) {
        ThanhToanTaiQuayResponse response = paymentExecutionUseCase.thanhToanTaiQuay(request);
        phatRealtimeHoaDonCho("PAID", response.hoaDonId());
        return response;
    }

    private void phatRealtimeHoaDonCho(String action, Integer hoaDonId) {
        webSocketNotificationService.sendToTopic(
                POS_SYNC_TOPIC,
                "POS_INVOICE_CHANGED",
                Map.of(
                        "action", action,
                        "invoiceId", hoaDonId
                )
        );
    }

    @Transactional(readOnly = true)
    public List<HoaDonChoTomTatResponse> layDanhSachHoaDonCho() {
        return invoiceUseCase.layDanhSachHoaDonCho();
    }

    @Transactional(readOnly = true)
    public HoaDonChoChiTietResponse layChiTietHoaDonCho(Integer hoaDonId) {
        return invoiceUseCase.layChiTietHoaDonCho(hoaDonId);
    }
}
