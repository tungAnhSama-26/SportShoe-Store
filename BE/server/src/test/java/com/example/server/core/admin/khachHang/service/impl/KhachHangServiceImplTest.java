
package com.example.server.core.admin.khachHang.service.impl;

import com.example.server.core.admin.khachHang.dto.request.CapNhatKhachHangRequest;
import com.example.server.core.admin.khachHang.dto.request.DiaChiRequest;
import com.example.server.core.admin.khachHang.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.khachHang.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.khachHang.dto.request.TaoKhachHangRequest;
import com.example.server.core.admin.khachHang.dto.responsse.DiaChiResponse;
import com.example.server.core.admin.khachHang.dto.responsse.KhachHangResponse;
import com.example.server.entity.DiaChiKhachHang;
import com.example.server.entity.DiaChiHaiCap;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.address.DiaChiHaiCapRequest;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.DiaChiKhachHangRepository;
import com.example.server.repository.KhachHangRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KhachHangServiceImplTest {

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private DiaChiKhachHangRepository diaChiKhachHangRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private KhachHangServiceImpl khachHangService;

    // ===== Helpers dựng dữ liệu =====

    private KhachHang buildKhachHang(UUID id, String tenDangNhap, String hoTen, String email, String sdt, Integer trangThai) {
        KhachHang kh = new KhachHang();
        kh.setId(id);
        kh.setTenDangNhap(tenDangNhap);
        kh.setHoTen(hoTen);
        kh.setEmail(email);
        kh.setSdt(sdt);
        kh.setMatKhau("$2a$10$existinghash");
        kh.setTrangThai(trangThai);
        kh.setNgayTao(Instant.now());
        return kh;
    }

    private DiaChiKhachHang buildDiaChi(Integer id, KhachHang kh, boolean laMacDinh) {
        DiaChiKhachHang dc = new DiaChiKhachHang();
        dc.setId(id);
        dc.setKhachHang(kh);
        dc.setHoTen("Nguyen Van A");
        dc.setSdt("0912345678");
        DiaChiHaiCap diaChi = new DiaChiHaiCap();
        diaChi.setTinhThanh("Hà Nội");
        diaChi.setPhuongXa("Dịch Vọng");
        diaChi.setDiaChiCuThe("Số 1 Đường ABC");
        dc.setDiaChi(diaChi);
        dc.setLaMacDinh(laMacDinh);
        dc.setTrangThai(1);
        dc.setNgayTao(Instant.now());
        return dc;
    }

    private DiaChiRequest buildDiaChiRequest(boolean laMacDinh) {
        return new DiaChiRequest(
                "Nguyen Van B",
                "0987654321",
                new DiaChiHaiCapRequest(null, "Hồ Chí Minh", null, "Bến Nghé", "Số 2 Đường XYZ"),
                laMacDinh
        );
    }

    // ===== taoKhachHang =====

    @Test
    @DisplayName("taoKhachHang - email đã tồn tại thì ném BusinessException")
    void taoKhachHang_emailDaTonTai_thiNemBusinessException() {
        when(khachHangRepository.existsByEmail("trung@example.com")).thenReturn(true);

        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "user01", "Nguyen Van A", "trung@example.com", "0900000001", 1, "matkhau123", null,
                LocalDate.now().minusYears(25)
        );

        assertThatThrownBy(() -> khachHangService.taoKhachHang(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email đã được sử dụng");

        verify(khachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("taoKhachHang - SĐT đã tồn tại thì ném BusinessException")
    void taoKhachHang_sdtDaTonTai_thiNemBusinessException() {
        when(khachHangRepository.existsBySdt("0900000001")).thenReturn(true);

        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "user01", "Nguyen Van A", "a@example.com", "0900000001", 1, "matkhau123", null,
                LocalDate.now().minusYears(25)
        );

        assertThatThrownBy(() -> khachHangService.taoKhachHang(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Số điện thoại đã được sử dụng");

        verify(khachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("taoKhachHang - ngày sinh ở tương lai thì ném BusinessException")
    void taoKhachHang_ngaySinhOTuongLai_thiNemBusinessException() {
        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "user01", "Nguyen Van A", "a@example.com", "0900000001", 1, "matkhau123", null,
                LocalDate.now().plusDays(1)
        );

        assertThatThrownBy(() -> khachHangService.taoKhachHang(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ngày sinh không được ở tương lai");
    }

    @Test
    @DisplayName("taoKhachHang - tuổi nhỏ hơn 18 thì ném BusinessException")
    void taoKhachHang_tuoiNhoHon18_thiNemBusinessException() {
        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "user01", "Nguyen Van A", "a@example.com", "0900000001", 1, "matkhau123", null,
                LocalDate.now().minusYears(17)
        );

        assertThatThrownBy(() -> khachHangService.taoKhachHang(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Khách hàng phải đủ 18 tuổi");
    }

    @Test
    @DisplayName("taoKhachHang - tuổi lớn hơn 100 thì ném BusinessException")
    void taoKhachHang_tuoiLonHon100_thiNemBusinessException() {
        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "user01", "Nguyen Van A", "a@example.com", "0900000001", 1, "matkhau123", null,
                LocalDate.now().minusYears(101)
        );

        assertThatThrownBy(() -> khachHangService.taoKhachHang(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Tuổi khách hàng không hợp lệ (tối đa 100 tuổi)");
    }

    @Test
    @DisplayName("taoKhachHang - tuổi đúng biên 18 thì tạo thành công")
    void taoKhachHang_tuoiDung18_thiTaoThanhCong() {
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordService.hash(anyString())).thenReturn("HASHED");

        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "user18", "Nguyen Van A", "a18@example.com", "0900000018", 1, "matkhau123", null,
                LocalDate.now().minusYears(18)
        );

        KhachHangResponse response = khachHangService.taoKhachHang(request);

        assertThat(response).isNotNull();
        assertThat(response.trangThai()).isEqualTo(1);
    }

    @Test
    @DisplayName("taoKhachHang - tuổi đúng biên 100 thì tạo thành công")
    void taoKhachHang_tuoiDung100_thiTaoThanhCong() {
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordService.hash(anyString())).thenReturn("HASHED");

        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "user100", "Nguyen Van A", "a100@example.com", "0900000100", 1, "matkhau123", null,
                LocalDate.now().minusYears(100)
        );

        KhachHangResponse response = khachHangService.taoKhachHang(request);

        assertThat(response).isNotNull();
        assertThat(response.trangThai()).isEqualTo(1);
    }

    @Test
    @DisplayName("taoKhachHang - happy path: tạo thành công, hash mật khẩu, không gửi email chào mừng")
    void taoKhachHang_hapPath_thiTaoThanhCongVaHashMatKhau() {
        when(passwordService.hash("matkhau123")).thenReturn("HASHED_PW");
        ArgumentCaptor<KhachHang> captor = ArgumentCaptor.forClass(KhachHang.class);
        when(khachHangRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "nguyenvana", " Nguyễn Văn A ", " TEST@Example.com ", " 0912345678", 1, "matkhau123", "anh.png",
                LocalDate.now().minusYears(25)
        );

        KhachHangResponse response = khachHangService.taoKhachHang(request);

        assertThat(response.tenDangNhap()).isEqualTo("nguyenvana");
        assertThat(response.hoTen()).isEqualTo("Nguyễn Văn A");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.sdt()).isEqualTo("0912345678");
        assertThat(response.trangThai()).isEqualTo(1);
        assertThat(response.tenTrangThai()).isEqualTo("Hoạt động");

        KhachHang saved = captor.getValue();
        assertThat(saved.getMatKhau()).isEqualTo("HASHED_PW");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNgayTao()).isNotNull();

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("taoKhachHang - tên đăng nhập gợi ý bị trùng thì tự động thêm hậu số")
    void taoKhachHang_tenDangNhapBiTrung_thiTuDongThemHauSo() {
        when(khachHangRepository.existsByTenDangNhap("an")).thenReturn(true);
        when(khachHangRepository.existsByTenDangNhap("an1")).thenReturn(false);
        when(passwordService.hash(anyString())).thenReturn("HASHED");
        ArgumentCaptor<KhachHang> captor = ArgumentCaptor.forClass(KhachHang.class);
        when(khachHangRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "an", "Nguyen Van A", "an@example.com", "0900000002", 1, "matkhau123", null,
                LocalDate.now().minusYears(25)
        );

        khachHangService.taoKhachHang(request);

        assertThat(captor.getValue().getTenDangNhap()).isEqualTo("an1");
    }

    @Test
    @DisplayName("taoKhachHang - tên đăng nhập gợi ý rỗng thì ném BusinessException")
    void taoKhachHang_tenDangNhapGoiYRong_thiNemBusinessException() {
        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "   ", "Nguyen Van A", "a@example.com", "0900000001", 1, "matkhau123", null,
                LocalDate.now().minusYears(25)
        );

        assertThatThrownBy(() -> khachHangService.taoKhachHang(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Không thể tạo tên đăng nhập cho khách hàng");

        verify(khachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("taoKhachHang - SĐT rỗng/null thì bỏ qua kiểm tra trùng")
    void taoKhachHang_sdtRongHoacNull_thiBoQuaKiemTraTrung() {
        when(passwordService.hash(anyString())).thenReturn("HASHED");
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        TaoKhachHangRequest request = new TaoKhachHangRequest(
                "user02", "Nguyen Van A", "a2@example.com", null, 1, "matkhau123", null,
                LocalDate.now().minusYears(25)
        );

        KhachHangResponse response = khachHangService.taoKhachHang(request);

        assertThat(response.sdt()).isNull();
        verify(khachHangRepository, never()).existsBySdt(any());
    }

    // ===== capNhatKhachHang =====

    @Test
    @DisplayName("capNhatKhachHang - không tìm thấy khách hàng thì ném ResourceNotFoundException")
    void capNhatKhachHang_khongTimThayKhachHang_thiNemResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(khachHangRepository.findById(id)).thenReturn(Optional.empty());

        CapNhatKhachHangRequest request = new CapNhatKhachHangRequest("Nguyen Van A", null, null, null, null, null);

        assertThatThrownBy(() -> khachHangService.capNhatKhachHang(id, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(khachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("capNhatKhachHang - email trùng khách hàng khác thì ném BusinessException")
    void capNhatKhachHang_emailTrungKhachKhac_thiNemBusinessException() {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        KhachHang khA = buildKhachHang(idA, "user1", "Nguyen Van A", "a@example.com", "0900000001", 1);
        KhachHang khB = buildKhachHang(idB, "user2", "Tran Thi B", "trung@example.com", "0900000002", 1);
        when(khachHangRepository.findById(idA)).thenReturn(Optional.of(khA));
        when(khachHangRepository.findByEmail("trung@example.com")).thenReturn(Optional.of(khB));

        CapNhatKhachHangRequest request = new CapNhatKhachHangRequest(
                "Nguyen Van A", "trung@example.com", null, 1, null, null
        );

        assertThatThrownBy(() -> khachHangService.capNhatKhachHang(idA, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email đã được sử dụng");

        verify(khachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("capNhatKhachHang - email trùng nhưng là của chính khách hàng đang sửa thì không lỗi")
    void capNhatKhachHang_emailTrungChinhMinh_thiKhongLoi() {
        UUID id = UUID.randomUUID();
        KhachHang kh = buildKhachHang(id, "user1", "Nguyen Van A", "existing@example.com", "0900000001", 1);
        when(khachHangRepository.findById(id)).thenReturn(Optional.of(kh));
        when(khachHangRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(kh));
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        CapNhatKhachHangRequest request = new CapNhatKhachHangRequest(
                "Nguyen Van A Moi", "existing@example.com", null, 1, null, null
        );

        KhachHangResponse response = khachHangService.capNhatKhachHang(id, request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("existing@example.com");
    }

    @Test
    @DisplayName("capNhatKhachHang - SĐT trùng khách hàng khác thì ném BusinessException")
    void capNhatKhachHang_sdtTrungKhachKhac_thiNemBusinessException() {
        UUID id = UUID.randomUUID();
        KhachHang kh = buildKhachHang(id, "user1", "Nguyen Van A", "a@example.com", "0900000001", 1);
        when(khachHangRepository.findById(id)).thenReturn(Optional.of(kh));
        when(khachHangRepository.existsBySdtAndIdNot("0911111111", id)).thenReturn(true);

        CapNhatKhachHangRequest request = new CapNhatKhachHangRequest(
                "Nguyen Van A", null, "0911111111", 1, null, null
        );

        assertThatThrownBy(() -> khachHangService.capNhatKhachHang(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Số điện thoại đã được sử dụng");

        verify(khachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("capNhatKhachHang - ngày sinh ở tương lai thì ném BusinessException")
    void capNhatKhachHang_ngaySinhOTuongLai_thiNemBusinessException() {
        UUID id = UUID.randomUUID();
        KhachHang kh = buildKhachHang(id, "user1", "Nguyen Van A", "a@example.com", "0900000001", 1);
        when(khachHangRepository.findById(id)).thenReturn(Optional.of(kh));

        CapNhatKhachHangRequest request = new CapNhatKhachHangRequest(
                "Nguyen Van A", null, null, 1, null, LocalDate.now().plusDays(1)
        );

        assertThatThrownBy(() -> khachHangService.capNhatKhachHang(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ngày sinh không được ở tương lai");

        verify(khachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("capNhatKhachHang - happy path: cập nhật đúng các trường")
    void capNhatKhachHang_hapPath_capNhatDungCacTruong() {
        UUID id = UUID.randomUUID();
        KhachHang kh = buildKhachHang(id, "user1", "Ten Cu", "cu@example.com", "0900000000", 1);
        when(khachHangRepository.findById(id)).thenReturn(Optional.of(kh));
        when(khachHangRepository.findByEmail("moi@example.com")).thenReturn(Optional.empty());
        when(khachHangRepository.existsBySdtAndIdNot("0911111111", id)).thenReturn(false);
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        CapNhatKhachHangRequest request = new CapNhatKhachHangRequest(
                " Tên Mới ", " MOI@Example.com ", " 0911111111", 0, "anh-moi.png", LocalDate.now().minusYears(30)
        );

        KhachHangResponse response = khachHangService.capNhatKhachHang(id, request);

        assertThat(response.hoTen()).isEqualTo("Tên Mới");
        assertThat(response.email()).isEqualTo("moi@example.com");
        assertThat(response.sdt()).isEqualTo("0911111111");
        assertThat(response.gioiTinh()).isEqualTo(0);
        assertThat(response.tenGioiTinh()).isEqualTo("Nữ");
        assertThat(response.hinhAnh()).isEqualTo("anh-moi.png");
        assertThat(kh.getNgayCapNhat()).isNotNull();
    }

    // ===== doiTrangThai =====

    @Test
    @DisplayName("doiTrangThai - không tìm thấy khách hàng thì ném ResourceNotFoundException")
    void doiTrangThai_khongTimThayKhachHang_thiNemResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(khachHangRepository.findById(id)).thenReturn(Optional.empty());
        DoiTrangThaiRequest request = new DoiTrangThaiRequest(0);

        assertThatThrownBy(() -> khachHangService.doiTrangThai(id, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(khachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("doiTrangThai - happy path: cập nhật đúng trạng thái mới")
    void doiTrangThai_hapPath_capNhatDungTrangThaiMoi() {
        UUID id = UUID.randomUUID();
        KhachHang kh = buildKhachHang(id, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        when(khachHangRepository.findById(id)).thenReturn(Optional.of(kh));
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        DoiTrangThaiRequest request = new DoiTrangThaiRequest(0);
        KhachHangResponse response = khachHangService.doiTrangThai(id, request);

        assertThat(response.trangThai()).isEqualTo(0);
        assertThat(response.tenTrangThai()).isEqualTo("Khóa");
        assertThat(kh.getNgayCapNhat()).isNotNull();
    }

    // ===== doiMatKhau =====

    @Test
    @DisplayName("doiMatKhau - khách hàng có email thì gửi email thông báo mật khẩu mới")
    void doiMatKhau_khachCoEmail_thiGuiEmailThongBao() {
        UUID id = UUID.randomUUID();
        KhachHang kh = buildKhachHang(id, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        when(khachHangRepository.findById(id)).thenReturn(Optional.of(kh));
        when(passwordService.hash("matkhaumoi123")).thenReturn("HASHED_NEW");
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        DoiMatKhauRequest request = new DoiMatKhauRequest("matkhaumoi123");
        khachHangService.doiMatKhau(id, request);

        assertThat(kh.getMatKhau()).isEqualTo("HASHED_NEW");
        verify(emailService).sendCustomerPasswordChangedEmailAsync("a@example.com", "Nguyen Van A", "user1", "matkhaumoi123");
    }

    @Test
    @DisplayName("doiMatKhau - khách hàng không có email (null) thì không gửi email")
    void doiMatKhau_khachKhongCoEmail_thiKhongGuiEmail() {
        UUID id = UUID.randomUUID();
        KhachHang kh = buildKhachHang(id, "user1", "Nguyen Van A", null, "0900000000", 1);
        when(khachHangRepository.findById(id)).thenReturn(Optional.of(kh));
        when(passwordService.hash(anyString())).thenReturn("HASHED_NEW");
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        DoiMatKhauRequest request = new DoiMatKhauRequest("matkhaumoi123");
        khachHangService.doiMatKhau(id, request);

        verify(emailService, never()).sendCustomerPasswordChangedEmailAsync(any(), any(), any(), any());
    }

    @Test
    @DisplayName("doiMatKhau - khách hàng có email rỗng thì không gửi email")
    void doiMatKhau_khachCoEmailRong_thiKhongGuiEmail() {
        UUID id = UUID.randomUUID();
        KhachHang kh = buildKhachHang(id, "user1", "Nguyen Van A", "", "0900000000", 1);
        when(khachHangRepository.findById(id)).thenReturn(Optional.of(kh));
        when(passwordService.hash(anyString())).thenReturn("HASHED_NEW");
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        DoiMatKhauRequest request = new DoiMatKhauRequest("matkhaumoi123");
        khachHangService.doiMatKhau(id, request);

        verify(emailService, never()).sendCustomerPasswordChangedEmailAsync(any(), any(), any(), any());
    }

    @Test
    @DisplayName("doiMatKhau - không tìm thấy khách hàng thì ném ResourceNotFoundException")
    void doiMatKhau_khongTimThayKhachHang_thiNemResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(khachHangRepository.findById(id)).thenReturn(Optional.empty());
        DoiMatKhauRequest request = new DoiMatKhauRequest("matkhaumoi123");

        assertThatThrownBy(() -> khachHangService.doiMatKhau(id, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(khachHangRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    // ===== xoaKhachHang =====

    @Test
    @DisplayName("xoaKhachHang - happy path: gọi delete đúng đối tượng")
    void xoaKhachHang_hapPath_thiGoiDeleteDungDoiTuong() {
        UUID id = UUID.randomUUID();
        KhachHang kh = buildKhachHang(id, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        when(khachHangRepository.findById(id)).thenReturn(Optional.of(kh));

        khachHangService.xoaKhachHang(id);

        verify(khachHangRepository).delete(kh);
    }

    @Test
    @DisplayName("xoaKhachHang - không tìm thấy khách hàng thì ném ResourceNotFoundException")
    void xoaKhachHang_khongTimThayKhachHang_thiNemResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(khachHangRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> khachHangService.xoaKhachHang(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(khachHangRepository, never()).delete(any());
    }

    // ===== themDiaChi =====

    @Test
    @DisplayName("themDiaChi - không tìm thấy khách hàng thì ném ResourceNotFoundException")
    void themDiaChi_khongTimThayKhachHang_thiNemResourceNotFoundException() {
        UUID khId = UUID.randomUUID();
        when(khachHangRepository.findById(khId)).thenReturn(Optional.empty());
        DiaChiRequest request = buildDiaChiRequest(true);

        assertThatThrownBy(() -> khachHangService.themDiaChi(khId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(diaChiKhachHangRepository);
    }

    @Test
    @DisplayName("themDiaChi - laMacDinh=true thì reset địa chỉ mặc định cũ trước khi thêm")
    void themDiaChi_laMacDinhTrue_thiResetDiaChiMacDinhCuTruocKhiThem() {
        UUID khId = UUID.randomUUID();
        KhachHang kh = buildKhachHang(khId, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        when(khachHangRepository.findById(khId)).thenReturn(Optional.of(kh));

        DiaChiKhachHang diaChiCu = buildDiaChi(1, kh, true);
        when(diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khId)).thenReturn(List.of(diaChiCu));
        when(diaChiKhachHangRepository.save(any(DiaChiKhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        DiaChiRequest request = buildDiaChiRequest(true);
        DiaChiResponse response = khachHangService.themDiaChi(khId, request);

        assertThat(diaChiCu.getLaMacDinh()).isFalse();
        assertThat(response.laMacDinh()).isTrue();
        verify(diaChiKhachHangRepository, times(2)).save(any(DiaChiKhachHang.class));
    }

    @Test
    @DisplayName("themDiaChi - laMacDinh=false thì không reset địa chỉ mặc định cũ")
    void themDiaChi_laMacDinhFalse_thiKhongResetDiaChiMacDinhCu() {
        UUID khId = UUID.randomUUID();
        KhachHang kh = buildKhachHang(khId, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        when(khachHangRepository.findById(khId)).thenReturn(Optional.of(kh));
        when(diaChiKhachHangRepository.save(any(DiaChiKhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        DiaChiRequest request = buildDiaChiRequest(false);
        DiaChiResponse response = khachHangService.themDiaChi(khId, request);

        assertThat(response.laMacDinh()).isFalse();
        verify(diaChiKhachHangRepository, never()).findByKhachHangIdOrderByLaMacDinhDesc(any());
        verify(diaChiKhachHangRepository, times(1)).save(any(DiaChiKhachHang.class));
    }

    // ===== capNhatDiaChi =====

    @Test
    @DisplayName("capNhatDiaChi - không tìm thấy địa chỉ thì ném ResourceNotFoundException")
    void capNhatDiaChi_khongTimThayDiaChi_thiNemResourceNotFoundException() {
        when(diaChiKhachHangRepository.findById(99)).thenReturn(Optional.empty());
        DiaChiRequest request = buildDiaChiRequest(true);

        assertThatThrownBy(() -> khachHangService.capNhatDiaChi(99, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(diaChiKhachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("capNhatDiaChi - laMacDinh=true thì reset địa chỉ mặc định cũ")
    void capNhatDiaChi_laMacDinhTrue_thiResetDiaChiMacDinhCu() {
        UUID khId = UUID.randomUUID();
        KhachHang kh = buildKhachHang(khId, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        DiaChiKhachHang dcDangSua = buildDiaChi(1, kh, false);
        DiaChiKhachHang dcMacDinhCu = buildDiaChi(2, kh, true);

        when(diaChiKhachHangRepository.findById(1)).thenReturn(Optional.of(dcDangSua));
        when(diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khId))
                .thenReturn(List.of(dcMacDinhCu, dcDangSua));
        when(diaChiKhachHangRepository.save(any(DiaChiKhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        DiaChiRequest request = buildDiaChiRequest(true);
        DiaChiResponse response = khachHangService.capNhatDiaChi(1, request);

        assertThat(dcMacDinhCu.getLaMacDinh()).isFalse();
        assertThat(response.laMacDinh()).isTrue();
    }

    @Test
    @DisplayName("capNhatDiaChi - happy path: cập nhật đúng các trường")
    void capNhatDiaChi_hapPath_capNhatDungCacTruong() {
        UUID khId = UUID.randomUUID();
        KhachHang kh = buildKhachHang(khId, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        DiaChiKhachHang dc = buildDiaChi(1, kh, false);
        when(diaChiKhachHangRepository.findById(1)).thenReturn(Optional.of(dc));
        when(diaChiKhachHangRepository.save(any(DiaChiKhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        DiaChiRequest request = new DiaChiRequest(
                "Le Van B",
                "0987000000",
                new DiaChiHaiCapRequest(null, "Đà Nẵng", null, "Thạch Thang", "12 Trần Phú"),
                false
        );
        DiaChiResponse response = khachHangService.capNhatDiaChi(1, request);

        assertThat(response.hoTen()).isEqualTo("Le Van B");
        assertThat(response.sdt()).isEqualTo("0987000000");
        assertThat(response.diaChi().tinhThanh()).isEqualTo("Đà Nẵng");
        assertThat(response.diaChi().phuongXa()).isEqualTo("Thạch Thang");
        assertThat(response.diaChi().diaChiCuThe()).isEqualTo("12 Trần Phú");
        assertThat(dc.getNgayCapNhat()).isNotNull();
    }

    // ===== xoaDiaChi =====

    @Test
    @DisplayName("xoaDiaChi - không tìm thấy địa chỉ thì ném ResourceNotFoundException")
    void xoaDiaChi_khongTimThayDiaChi_thiNemResourceNotFoundException() {
        when(diaChiKhachHangRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> khachHangService.xoaDiaChi(99))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(diaChiKhachHangRepository, never()).delete(any());
    }

    @Test
    @DisplayName("xoaDiaChi - là địa chỉ mặc định và còn địa chỉ khác thì thăng địa chỉ đầu tiên thành mặc định mới")
    void xoaDiaChi_laDiaChiMacDinhVaConDiaChiKhac_thiThangDiaChiDauTienThanhMacDinhMoi() {
        UUID khId = UUID.randomUUID();
        KhachHang kh = buildKhachHang(khId, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        DiaChiKhachHang dcBiXoa = buildDiaChi(1, kh, true);
        DiaChiKhachHang dcConLai = buildDiaChi(2, kh, false);

        when(diaChiKhachHangRepository.findById(1)).thenReturn(Optional.of(dcBiXoa));
        when(diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khId)).thenReturn(List.of(dcConLai));
        when(diaChiKhachHangRepository.save(any(DiaChiKhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        khachHangService.xoaDiaChi(1);

        verify(diaChiKhachHangRepository).delete(dcBiXoa);
        assertThat(dcConLai.getLaMacDinh()).isTrue();
        verify(diaChiKhachHangRepository).save(dcConLai);
    }

    @Test
    @DisplayName("xoaDiaChi - là địa chỉ mặc định và hết địa chỉ khác thì không đặt mặc định mới")
    void xoaDiaChi_laDiaChiMacDinhVaHetDiaChiKhac_thiKhongDatMacDinhMoi() {
        UUID khId = UUID.randomUUID();
        KhachHang kh = buildKhachHang(khId, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        DiaChiKhachHang dcBiXoa = buildDiaChi(1, kh, true);

        when(diaChiKhachHangRepository.findById(1)).thenReturn(Optional.of(dcBiXoa));
        when(diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khId)).thenReturn(List.of());

        khachHangService.xoaDiaChi(1);

        verify(diaChiKhachHangRepository).delete(dcBiXoa);
        verify(diaChiKhachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("xoaDiaChi - không phải mặc định thì không đụng đến địa chỉ khác")
    void xoaDiaChi_khongPhaiMacDinh_thiKhongDongDenDiaChiKhac() {
        UUID khId = UUID.randomUUID();
        KhachHang kh = buildKhachHang(khId, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        DiaChiKhachHang dcBiXoa = buildDiaChi(1, kh, false);

        when(diaChiKhachHangRepository.findById(1)).thenReturn(Optional.of(dcBiXoa));

        khachHangService.xoaDiaChi(1);

        verify(diaChiKhachHangRepository).delete(dcBiXoa);
        verify(diaChiKhachHangRepository, never()).findByKhachHangIdOrderByLaMacDinhDesc(any());
        verify(diaChiKhachHangRepository, never()).save(any());
    }

    // ===== datMacDinhDiaChi =====

    @Test
    @DisplayName("datMacDinhDiaChi - không tìm thấy địa chỉ thì ném ResourceNotFoundException")
    void datMacDinhDiaChi_khongTimThayDiaChi_thiNemResourceNotFoundException() {
        when(diaChiKhachHangRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> khachHangService.datMacDinhDiaChi(99))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(diaChiKhachHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("datMacDinhDiaChi - happy path: reset địa chỉ mặc định cũ rồi đặt địa chỉ mới")
    void datMacDinhDiaChi_hapPath_resetCuVaDatMoi() {
        UUID khId = UUID.randomUUID();
        KhachHang kh = buildKhachHang(khId, "user1", "Nguyen Van A", "a@example.com", "0900000000", 1);
        DiaChiKhachHang dcMoi = buildDiaChi(1, kh, false);
        DiaChiKhachHang dcCu = buildDiaChi(2, kh, true);

        when(diaChiKhachHangRepository.findById(1)).thenReturn(Optional.of(dcMoi));
        when(diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khId)).thenReturn(List.of(dcCu, dcMoi));
        when(diaChiKhachHangRepository.save(any(DiaChiKhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        khachHangService.datMacDinhDiaChi(1);

        assertThat(dcCu.getLaMacDinh()).isFalse();
        assertThat(dcMoi.getLaMacDinh()).isTrue();
        assertThat(dcMoi.getNgayCapNhat()).isNotNull();
        verify(diaChiKhachHangRepository).save(dcCu);
        verify(diaChiKhachHangRepository).save(dcMoi);
    }

    // ===== layDanhSach =====

    @Test
    @DisplayName("layDanhSach - keyword null thì trả về tất cả")
    void layDanhSach_keywordNull_traVeTatCa() {
        KhachHang kh1 = buildKhachHang(UUID.randomUUID(), "user1", "Nguyen Van A", "a@example.com", "0900000001", 1);
        KhachHang kh2 = buildKhachHang(UUID.randomUUID(), "user2", "Tran Thi B", "b@example.com", "0900000002", 0);
        when(khachHangRepository.findAll()).thenReturn(List.of(kh1, kh2));

        List<KhachHangResponse> result = khachHangService.layDanhSach(null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("layDanhSach - lọc theo keyword khớp họ tên, không phân biệt hoa thường")
    void layDanhSach_locTheoKeyword_khopHoTenKhongPhanBietHoaThuong() {
        KhachHang kh1 = buildKhachHang(UUID.randomUUID(), "user1", "Nguyen Van A", "a@example.com", "0900000001", 1);
        KhachHang kh2 = buildKhachHang(UUID.randomUUID(), "user2", "Tran Thi B", "b@example.com", "0900000002", 1);
        when(khachHangRepository.findAll()).thenReturn(List.of(kh1, kh2));

        List<KhachHangResponse> result = khachHangService.layDanhSach("NGUYEN van", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).hoTen()).isEqualTo("Nguyen Van A");
    }

    @Test
    @DisplayName("layDanhSach - lọc theo keyword khớp tên đăng nhập")
    void layDanhSach_locTheoKeyword_khopTenDangNhap() {
        KhachHang kh1 = buildKhachHang(UUID.randomUUID(), "hieplx", "Nguyen Van A", "a@example.com", "0900000001", 1);
        KhachHang kh2 = buildKhachHang(UUID.randomUUID(), "user2", "Tran Thi B", "b@example.com", "0900000002", 1);
        when(khachHangRepository.findAll()).thenReturn(List.of(kh1, kh2));

        List<KhachHangResponse> result = khachHangService.layDanhSach("HIEPLX", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).tenDangNhap()).isEqualTo("hieplx");
    }

    @Test
    @DisplayName("layDanhSach - lọc theo keyword khớp email")
    void layDanhSach_locTheoKeyword_khopEmail() {
        KhachHang kh1 = buildKhachHang(UUID.randomUUID(), "user1", "Nguyen Van A", "hiep@example.com", "0900000001", 1);
        KhachHang kh2 = buildKhachHang(UUID.randomUUID(), "user2", "Tran Thi B", "khac@example.com", "0900000002", 1);
        when(khachHangRepository.findAll()).thenReturn(List.of(kh1, kh2));

        List<KhachHangResponse> result = khachHangService.layDanhSach("HIEP@EXAMPLE.COM", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("hiep@example.com");
    }

    @Test
    @DisplayName("layDanhSach - lọc theo keyword khớp SĐT")
    void layDanhSach_locTheoKeyword_khopSdt() {
        KhachHang kh1 = buildKhachHang(UUID.randomUUID(), "user1", "Nguyen Van A", "a@example.com", "0911222333", 1);
        KhachHang kh2 = buildKhachHang(UUID.randomUUID(), "user2", "Tran Thi B", "b@example.com", "0900000002", 1);
        when(khachHangRepository.findAll()).thenReturn(List.of(kh1, kh2));

        List<KhachHangResponse> result = khachHangService.layDanhSach("0911222333", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sdt()).isEqualTo("0911222333");
    }

    @Test
    @DisplayName("layDanhSach - lọc theo trangThai chỉ trả về đúng trạng thái")
    void layDanhSach_locTheoTrangThai_chiTraVeDungTrangThai() {
        KhachHang kh1 = buildKhachHang(UUID.randomUUID(), "user1", "Nguyen Van A", "a@example.com", "0900000001", 1);
        KhachHang kh2 = buildKhachHang(UUID.randomUUID(), "user2", "Tran Thi B", "b@example.com", "0900000002", 0);
        when(khachHangRepository.findAll()).thenReturn(List.of(kh1, kh2));

        List<KhachHangResponse> result = khachHangService.layDanhSach(null, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).trangThai()).isEqualTo(0);
    }

    @Test
    @DisplayName("layDanhSach - keyword không khớp bất kỳ trường nào thì trả về rỗng")
    void layDanhSach_keywordKhongKhopBatKy_traVeRong() {
        KhachHang kh1 = buildKhachHang(UUID.randomUUID(), "user1", "Nguyen Van A", "a@example.com", "0900000001", 1);
        when(khachHangRepository.findAll()).thenReturn(List.of(kh1));

        List<KhachHangResponse> result = khachHangService.layDanhSach("khongtontai", null);

        assertThat(result).isEmpty();
    }

    // ===== layChiTiet / layChiTietDiaChi =====

    @Test
    @DisplayName("layChiTiet - không tìm thấy thì ném ResourceNotFoundException")
    void layChiTiet_khongTimThay_thiNemResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(khachHangRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> khachHangService.layChiTiet(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("layChiTiet - happy path: trả về đúng thông tin khách hàng")
    void layChiTiet_hapPath_traVeDungThongTin() {
        UUID id = UUID.randomUUID();
        KhachHang kh = buildKhachHang(id, "user1", "Nguyen Van A", "a@example.com", "0900000001", 1);
        when(khachHangRepository.findById(id)).thenReturn(Optional.of(kh));

        KhachHangResponse response = khachHangService.layChiTiet(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.hoTen()).isEqualTo("Nguyen Van A");
    }

    @Test
    @DisplayName("layChiTietDiaChi - không tìm thấy thì ném ResourceNotFoundException")
    void layChiTietDiaChi_khongTimThay_thiNemResourceNotFoundException() {
        when(diaChiKhachHangRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> khachHangService.layChiTietDiaChi(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("layChiTietDiaChi - happy path: trả về đúng thông tin địa chỉ")
    void layChiTietDiaChi_hapPath_traVeDungThongTin() {
        UUID khId = UUID.randomUUID();
        KhachHang kh = buildKhachHang(khId, "user1", "Nguyen Van A", "a@example.com", "0900000001", 1);
        DiaChiKhachHang dc = buildDiaChi(1, kh, true);
        when(diaChiKhachHangRepository.findById(1)).thenReturn(Optional.of(dc));

        DiaChiResponse response = khachHangService.layChiTietDiaChi(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.laMacDinh()).isTrue();
    }
}
