package com.example.server.core.admin.quanlytrahang.service;

import com.example.server.core.admin.quanlytrahang.dto.request.SanPhamTraRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.TaoPhieuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.DuyetPhieuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.HoanTienTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.KiemTraPhieuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.KiemTraSanPhamTraRequest;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.NhanVien;
import com.example.server.entity.PhieuTraHang;
import com.example.server.entity.PhieuTraHangChiTiet;
import com.example.server.entity.ThanhToan;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.LichSuPhieuTraHangRepository;
import com.example.server.repository.NhanVienRepository;
import com.example.server.repository.PhieuTraHangChiTietRepository;
import com.example.server.repository.PhieuTraHangRepository;
import com.example.server.repository.ThanhToanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraHangServiceTest {

    @Mock
    private HoaDonRepository hoaDonRepository;
    @Mock
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Mock
    private PhieuTraHangRepository phieuTraHangRepository;
    @Mock
    private PhieuTraHangChiTietRepository phieuTraHangChiTietRepository;
    @Mock
    private LichSuPhieuTraHangRepository lichSuPhieuTraHangRepository;
    @Mock
    private ThanhToanRepository thanhToanRepository;
    @Mock
    private NhanVienRepository nhanVienRepository;
    @Mock
    private GiayChiTietRepository giayChiTietRepository;

    private TraHangService traHangService;

    @BeforeEach
    void setUp() {
        traHangService = new TraHangService(
                hoaDonRepository,
                hoaDonChiTietRepository,
                phieuTraHangRepository,
                phieuTraHangChiTietRepository,
                lichSuPhieuTraHangRepository,
                thanhToanRepository,
                nhanVienRepository,
                giayChiTietRepository
        );
    }

    @Test
    void taoPhieuTinhTienDuKienSauKhiPhanBoGiamGia() {
        UUID nhanVienId = UUID.randomUUID();
        NhanVien nhanVien = new NhanVien();
        nhanVien.setId(nhanVienId);

        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(10);
        hoaDon.setTrangThai(5);
        hoaDon.setTongTienHang(new BigDecimal("1000000"));
        hoaDon.setTienGiam(new BigDecimal("100000"));

        GiayChiTiet giayChiTiet = new GiayChiTiet();
        giayChiTiet.setId(21);

        HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
        hoaDonChiTiet.setId(11);
        hoaDonChiTiet.setHoaDon(hoaDon);
        hoaDonChiTiet.setGiayChiTiet(giayChiTiet);
        hoaDonChiTiet.setSoLuong(2);
        hoaDonChiTiet.setGiaDonVi(new BigDecimal("500000"));

        when(hoaDonRepository.findDetailById(10)).thenReturn(Optional.of(hoaDon));
        when(hoaDonChiTietRepository.findById(11)).thenReturn(Optional.of(hoaDonChiTiet));
        when(phieuTraHangChiTietRepository.sumSoLuongDangXuLyByHoaDonChiTietId(11)).thenReturn(0);
        when(nhanVienRepository.findById(nhanVienId)).thenReturn(Optional.of(nhanVien));
        when(phieuTraHangRepository.save(any(PhieuTraHang.class))).thenAnswer(invocation -> {
            PhieuTraHang phieu = invocation.getArgument(0);
            phieu.setId(1);
            return phieu;
        });

        TaoPhieuTraHangRequest request = new TaoPhieuTraHangRequest(
                10,
                "KHONG_VUA",
                "Khách cần đổi cỡ nhưng phiên bản đầu chỉ hỗ trợ trả hàng",
                2,
                List.of(new SanPhamTraRequest(11, 1, "Sản phẩm còn nguyên"))
        );

        var response = traHangService.taoPhieu(request, nhanVienId);

        ArgumentCaptor<PhieuTraHang> phieuCaptor = ArgumentCaptor.forClass(PhieuTraHang.class);
        verify(phieuTraHangRepository).save(phieuCaptor.capture());
        assertEquals(1, phieuCaptor.getValue().getTrangThai());
        assertEquals(new BigDecimal("450000.00"), phieuCaptor.getValue().getTongTienDuKien());
        assertEquals(new BigDecimal("450000.00"), response.tongTienDuKien());

        ArgumentCaptor<PhieuTraHangChiTiet> chiTietCaptor =
                ArgumentCaptor.forClass(PhieuTraHangChiTiet.class);
        verify(phieuTraHangChiTietRepository).save(chiTietCaptor.capture());
        assertEquals(1, chiTietCaptor.getValue().getSoLuongTra());
        assertEquals(new BigDecimal("500000"), chiTietCaptor.getValue().getThanhTien());
        assertEquals(BigDecimal.ZERO, chiTietCaptor.getValue().getSoTienHoan());

        verify(lichSuPhieuTraHangRepository).save(any());
    }

    @Test
    void duyetPhieuTraTaiCuaHangChuyenThangSangDaNhanHang() {
        UUID nhanVienId = UUID.randomUUID();
        NhanVien nhanVien = new NhanVien();
        nhanVien.setId(nhanVienId);
        HoaDon hoaDon = hoaDon(5);
        PhieuTraHang phieu = phieuTraHang(hoaDon, 1);

        when(phieuTraHangRepository.findById(1)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findById(nhanVienId)).thenReturn(Optional.of(nhanVien));
        when(phieuTraHangChiTietRepository.findByPhieuTraHangIdOrderByIdAsc(1))
                .thenReturn(List.of());

        var response = traHangService.duyetPhieu(
                1,
                new DuyetPhieuTraHangRequest(true, "Đã nhận hàng tại cửa hàng"),
                nhanVienId
        );

        assertEquals(4, phieu.getTrangThai());
        assertEquals(4, response.trangThai());
        verify(phieuTraHangRepository).save(phieu);
        verify(lichSuPhieuTraHangRepository).save(any());
    }

    @Test
    void kiemTraHangChotSoTienHoanTheoSoLuongChapNhan() {
        UUID nhanVienId = UUID.randomUUID();
        NhanVien nhanVien = new NhanVien();
        nhanVien.setId(nhanVienId);
        HoaDon hoaDon = hoaDon(5);
        PhieuTraHang phieu = phieuTraHang(hoaDon, 5);

        GiayChiTiet bienThe = new GiayChiTiet();
        bienThe.setId(21);
        PhieuTraHangChiTiet chiTiet = chiTietTraHang(phieu, bienThe);
        chiTiet.setId(31);
        chiTiet.setSoLuongTra(1);
        chiTiet.setGiaBan(new BigDecimal("500000"));

        when(phieuTraHangRepository.findById(1)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findById(nhanVienId)).thenReturn(Optional.of(nhanVien));
        when(phieuTraHangChiTietRepository.findByPhieuTraHangIdOrderByIdAsc(1))
                .thenReturn(List.of(chiTiet));

        var response = traHangService.kiemTraHang(
                1,
                new KiemTraPhieuTraHangRequest(List.of(
                        new KiemTraSanPhamTraRequest(
                                31,
                                1,
                                1,
                                "Còn nguyên tem",
                                true
                        )
                ), "Đủ điều kiện hoàn tiền"),
                nhanVienId
        );

        assertEquals(6, phieu.getTrangThai());
        assertEquals(new BigDecimal("450000.00"), phieu.getTongTienThucTe());
        assertEquals(new BigDecimal("450000.00"), chiTiet.getSoTienHoan());
        assertEquals(1, chiTiet.getSoLuongChapNhan());
        assertEquals(0, chiTiet.getSoLuongTuChoi());
        assertEquals(6, response.trangThai());
    }

    @Test
    void hoanTienTaoGiaoDichMoiVaNhapLaiTonKhoMotLan() {
        UUID nhanVienId = UUID.randomUUID();
        NhanVien nhanVien = new NhanVien();
        nhanVien.setId(nhanVienId);
        HoaDon hoaDon = hoaDon(5);
        PhieuTraHang phieu = phieuTraHang(hoaDon, 6);
        phieu.setTongTienThucTe(new BigDecimal("450000.00"));

        GiayChiTiet bienThe = new GiayChiTiet();
        bienThe.setId(21);
        bienThe.setSoLuong(5);
        PhieuTraHangChiTiet chiTiet = chiTietTraHang(phieu, bienThe);
        chiTiet.setSoLuongChapNhan(1);
        chiTiet.setNhapLaiTonKho(true);
        chiTiet.setDaCapNhatTon(false);

        ThanhToan giaoDichGoc = new ThanhToan();
        giaoDichGoc.setId(41);
        giaoDichGoc.setHoaDon(hoaDon);
        giaoDichGoc.setSoTien(new BigDecimal("900000"));
        giaoDichGoc.setTrangThai(1);
        giaoDichGoc.setLoaiGiaoDich(1);
        giaoDichGoc.setHinhThuc(2);

        when(phieuTraHangRepository.findById(1)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findById(nhanVienId)).thenReturn(Optional.of(nhanVien));
        when(phieuTraHangChiTietRepository.findByPhieuTraHangIdOrderByIdAsc(1))
                .thenReturn(List.of(chiTiet));
        when(thanhToanRepository
                .findFirstByHoaDonIdAndLoaiGiaoDichAndTrangThaiOrderByNgayThanhToanDesc(
                        hoaDon.getId(), 1, 1
                ))
                .thenReturn(Optional.of(giaoDichGoc));

        var response = traHangService.hoanTien(
                1,
                new HoanTienTraHangRequest(2, "RF-001", "Hoàn tiền qua ngân hàng"),
                nhanVienId
        );

        assertEquals(7, phieu.getTrangThai());
        assertEquals(6, bienThe.getSoLuong());
        assertEquals(true, chiTiet.getDaCapNhatTon());
        assertEquals(1, giaoDichGoc.getTrangThai());
        assertEquals(7, response.trangThai());

        ArgumentCaptor<ThanhToan> thanhToanCaptor = ArgumentCaptor.forClass(ThanhToan.class);
        verify(thanhToanRepository).save(thanhToanCaptor.capture());
        assertEquals(2, thanhToanCaptor.getValue().getLoaiGiaoDich());
        assertEquals(new BigDecimal("450000.00"), thanhToanCaptor.getValue().getSoTien());
        assertSame(giaoDichGoc, thanhToanCaptor.getValue().getGiaoDichGoc());
        verify(giayChiTietRepository).save(bienThe);
        verify(hoaDonRepository, never()).save(any());
    }

    private HoaDon hoaDon(int trangThai) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(10);
        hoaDon.setMa("HD001");
        hoaDon.setTrangThai(trangThai);
        hoaDon.setTongTienHang(new BigDecimal("1000000"));
        hoaDon.setTienGiam(new BigDecimal("100000"));
        return hoaDon;
    }

    private PhieuTraHang phieuTraHang(HoaDon hoaDon, int trangThai) {
        PhieuTraHang phieu = new PhieuTraHang();
        phieu.setId(1);
        phieu.setMa("TH001");
        phieu.setHoaDon(hoaDon);
        phieu.setLoaiYeuCau(2);
        phieu.setHinhThucHoan(2);
        phieu.setTongTienDuKien(new BigDecimal("450000.00"));
        phieu.setTongTienThucTe(BigDecimal.ZERO);
        phieu.setTongTienHoan(BigDecimal.ZERO);
        phieu.setTrangThai(trangThai);
        return phieu;
    }

    private PhieuTraHangChiTiet chiTietTraHang(
            PhieuTraHang phieu,
            GiayChiTiet bienThe
    ) {
        PhieuTraHangChiTiet chiTiet = new PhieuTraHangChiTiet();
        chiTiet.setPhieuTraHang(phieu);
        chiTiet.setGiayChiTiet(bienThe);
        chiTiet.setSoLuongTra(1);
        chiTiet.setSoLuongNhan(0);
        chiTiet.setSoLuongChapNhan(0);
        chiTiet.setSoLuongTuChoi(0);
        chiTiet.setGiaBan(new BigDecimal("500000"));
        chiTiet.setThanhTien(new BigDecimal("500000"));
        chiTiet.setSoTienHoan(BigDecimal.ZERO);
        chiTiet.setNhapLaiTonKho(false);
        chiTiet.setDaCapNhatTon(false);
        chiTiet.setTrangThai(1);
        return chiTiet;
    }
}
