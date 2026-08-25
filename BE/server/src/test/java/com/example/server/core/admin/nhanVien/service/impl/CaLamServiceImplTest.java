package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.CaLamRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiTrangThaiCaLamRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.CaLamResponse;
import com.example.server.core.realtime.lichlamviec.LichLamViecRealtimePublisher;
import com.example.server.entity.CaLam;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.CaLamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaLamServiceImplTest {

    @Mock
    private CaLamRepository caLamRepository;

    @Mock
    private LichLamViecRealtimePublisher realtimePublisher;

    @Test
    void taoCaLam_choPhepCaQuaNgayKhiKhongTrungKhoangGio() {
        CaLamServiceImpl service = new CaLamServiceImpl(caLamRepository, realtimePublisher);
        when(caLamRepository.findAll()).thenReturn(List.of(caLam("CA00009", "Ca sáng", "08:00", "12:00", true)));
        when(caLamRepository.count()).thenReturn(0L);
        when(caLamRepository.existsById("CA00001")).thenReturn(false);
        when(caLamRepository.save(any(CaLam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaLamResponse response = service.taoCaLam(new CaLamRequest(
                null,
                "Ca đêm",
                "21:52",
                "03:52",
                true
        ));

        assertEquals("CA00001", response.id());
        assertEquals("21:52", response.gioBatDau());
        assertEquals("03:52", response.gioKetThuc());
        verify(realtimePublisher).phatSauCommit("TAO_CA_LAM");
    }

    @Test
    void taoCaLam_chanCaQuaNgayNeuTrungKhoangGioSauNuaDem() {
        CaLamServiceImpl service = new CaLamServiceImpl(caLamRepository, realtimePublisher);
        when(caLamRepository.findAll()).thenReturn(List.of(caLam("CA00009", "Ca rạng sáng", "02:00", "04:00", true)));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.taoCaLam(new CaLamRequest(
                null,
                "Ca đêm",
                "21:52",
                "03:52",
                true
        )));

        assertTrue(exception.getMessage().contains("bị trùng với ca đang hoạt động"));
    }

    @Test
    void taoCaLam_khongChoGioBatDauVaKetThucTrungNhau() {
        CaLamServiceImpl service = new CaLamServiceImpl(caLamRepository, realtimePublisher);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.taoCaLam(new CaLamRequest(
                null,
                "Ca lỗi",
                "21:52",
                "21:52",
                true
        )));

        assertEquals("Giờ kết thúc không được trùng với giờ bắt đầu!", exception.getMessage());
    }

    @Test
    void doiTrangThaiCaLam_choPhepTatCaCoLichTuongLai() {
        CaLamServiceImpl service = new CaLamServiceImpl(caLamRepository, realtimePublisher);
        CaLam caLam = caLam("CA00009", "Ca tối", "18:00", "22:00", true);
        when(caLamRepository.findById("CA00009")).thenReturn(java.util.Optional.of(caLam));
        when(caLamRepository.save(any(CaLam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaLamResponse response = service.doiTrangThaiCaLam(
                "CA00009",
                new DoiTrangThaiCaLamRequest(false)
        );

        assertEquals(false, response.trangThai());
        verify(realtimePublisher).phatSauCommit("DOI_TRANG_THAI_CA_LAM");
    }

    @Test
    void capNhatCaLam_choPhepTatCaCoLichTuongLai() {
        CaLamServiceImpl service = new CaLamServiceImpl(caLamRepository, realtimePublisher);
        CaLam caLam = caLam("CA00009", "Ca tối", "18:00", "22:00", true);
        when(caLamRepository.findById("CA00009")).thenReturn(java.util.Optional.of(caLam));
        when(caLamRepository.save(any(CaLam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaLamResponse response = service.capNhatCaLam(
                "CA00009",
                new CaLamRequest("CA00009", "Ca tối", "18:00", "22:00", false)
        );

        assertEquals(false, response.trangThai());
        verify(realtimePublisher).phatSauCommit("CAP_NHAT_CA_LAM");
    }

    @Test
    void doiTrangThaiCaLam_chanBatCaNeuTrungKhoangGio() {
        CaLamServiceImpl service = new CaLamServiceImpl(caLamRepository, realtimePublisher);
        CaLam caTat = caLam("CA00009", "Ca tối", "18:00", "22:00", false);
        when(caLamRepository.findById("CA00009")).thenReturn(java.util.Optional.of(caTat));
        when(caLamRepository.findAll()).thenReturn(List.of(caLam("CA00010", "Ca chiều", "17:00", "20:00", true)));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.doiTrangThaiCaLam(
                "CA00009",
                new DoiTrangThaiCaLamRequest(true)
        ));

        assertTrue(exception.getMessage().contains("bị trùng với ca đang hoạt động"));
    }

    private CaLam caLam(String id, String ten, String gioBatDau, String gioKetThuc, boolean trangThai) {
        CaLam caLam = new CaLam();
        caLam.setId(id);
        caLam.setTen(ten);
        caLam.setGioBatDau(gioBatDau);
        caLam.setGioKetThuc(gioKetThuc);
        caLam.setTrangThai(trangThai);
        return caLam;
    }
}
