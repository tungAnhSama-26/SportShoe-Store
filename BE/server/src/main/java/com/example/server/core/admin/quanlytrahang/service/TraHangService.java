package com.example.server.core.admin.quanlytrahang.service;

import com.example.server.core.admin.quanlytrahang.domain.TinhTienHoanTraHang;
import com.example.server.core.admin.quanlytrahang.domain.TraHangValidator;
import com.example.server.core.admin.quanlytrahang.domain.TrangThaiPhieuTraHang;
import com.example.server.core.admin.quanlytrahang.domain.GiaoDichHoanTienFactory;
import com.example.server.core.admin.quanlytrahang.dto.request.DuyetPhieuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.CapNhatVanChuyenTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.GhiChuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.HoanTienTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.KiemTraPhieuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.KiemTraSanPhamTraRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.SanPhamTraRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.TaoPhieuTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.request.TuChoiTraHangRequest;
import com.example.server.core.admin.quanlytrahang.dto.response.TraHangResponse;
import com.example.server.core.refund.RefundBankAccountResolver;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.LichSuPhieuTraHang;
import com.example.server.entity.NhanVien;
import com.example.server.entity.PhieuTraHang;
import com.example.server.entity.PhieuTraHangChiTiet;
import com.example.server.entity.ThanhToan;
import com.example.server.entity.TaiKhoanNganHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HinhAnhTraHangRepository;
import com.example.server.repository.LichSuPhieuTraHangRepository;
import com.example.server.repository.NhanVienRepository;
import com.example.server.repository.PhieuTraHangChiTietRepository;
import com.example.server.repository.PhieuTraHangRepository;
import com.example.server.repository.ThanhToanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TraHangService {

    private static final int LOAI_YEU_CAU_TRA_HANG_HOAN_TIEN = 2;

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final PhieuTraHangRepository phieuTraHangRepository;
    private final PhieuTraHangChiTietRepository phieuTraHangChiTietRepository;
    private final LichSuPhieuTraHangRepository lichSuPhieuTraHangRepository;
    private final HinhAnhTraHangRepository hinhAnhTraHangRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final NhanVienRepository nhanVienRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final RefundBankAccountResolver refundBankAccountResolver;

    public TraHangService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            PhieuTraHangRepository phieuTraHangRepository,
            PhieuTraHangChiTietRepository phieuTraHangChiTietRepository,
            LichSuPhieuTraHangRepository lichSuPhieuTraHangRepository,
            HinhAnhTraHangRepository hinhAnhTraHangRepository,
            ThanhToanRepository thanhToanRepository,
            NhanVienRepository nhanVienRepository,
            GiayChiTietRepository giayChiTietRepository,
            RefundBankAccountResolver refundBankAccountResolver
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.phieuTraHangRepository = phieuTraHangRepository;
        this.phieuTraHangChiTietRepository = phieuTraHangChiTietRepository;
        this.lichSuPhieuTraHangRepository = lichSuPhieuTraHangRepository;
        this.hinhAnhTraHangRepository = hinhAnhTraHangRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.giayChiTietRepository = giayChiTietRepository;
        this.refundBankAccountResolver = refundBankAccountResolver;
    }

    @Transactional
    public TraHangResponse taoPhieu(TaoPhieuTraHangRequest request, UUID nhanVienId) {
        HoaDon hoaDon = hoaDonRepository.findDetailById(request.hoaDonId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn"));
        TraHangValidator.kiemTraTrangThaiHoaDon(hoaDon.getTrangThai());

        NhanVien nhanVien = nhanVienRepository.findById(nhanVienId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên xử lý"));

        Instant now = Instant.now();
        PhieuTraHang phieu = new PhieuTraHang();
        phieu.setMa(taoMaPhieu());
        phieu.setHoaDon(hoaDon);
        phieu.setKhachHang(hoaDon.getKhachHang());
        phieu.setNhanVien(nhanVien);
        phieu.setLyDo(request.moTa());
        phieu.setHinhThucHoan(request.hinhThucHoan());
        phieu.setLoaiYeuCau(LOAI_YEU_CAU_TRA_HANG_HOAN_TIEN);
        phieu.setLyDoMa(request.lyDoMa().trim().toUpperCase(Locale.ROOT));
        phieu.setMoTa(request.moTa());
        phieu.setTongTienHoan(BigDecimal.ZERO);
        phieu.setTongTienDuKien(BigDecimal.ZERO);
        phieu.setTongTienThucTe(BigDecimal.ZERO);
        phieu.setTrangThai(TrangThaiPhieuTraHang.CHO_DUYET.getMa());
        phieu.setNgayTao(now);
        phieu.setNgayCapNhat(now);

        List<PhieuTraHangChiTiet> chiTietDaTao = new ArrayList<>();
        BigDecimal tongTienDuKien = BigDecimal.ZERO;
        for (SanPhamTraRequest sanPhamRequest : request.sanPhams()) {
            HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository
                    .findById(sanPhamRequest.hoaDonChiTietId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm trong hóa đơn"));
            if (!hoaDon.getId().equals(hoaDonChiTiet.getHoaDon().getId())) {
                throw new BusinessException("Sản phẩm trả không thuộc hóa đơn đã chọn");
            }

            int soLuongDaTra = phieuTraHangChiTietRepository
                    .sumSoLuongDangXuLyByHoaDonChiTietId(hoaDonChiTiet.getId());
            TraHangValidator.kiemTraSoLuong(
                    hoaDonChiTiet.getSoLuong(),
                    sanPhamRequest.soLuong(),
                    soLuongDaTra
            );

            BigDecimal thanhTien = hoaDonChiTiet.getGiaDonVi()
                    .multiply(BigDecimal.valueOf(sanPhamRequest.soLuong()));
            BigDecimal tienDuKien = TinhTienHoanTraHang.tinh(
                    hoaDonChiTiet.getGiaDonVi(),
                    sanPhamRequest.soLuong(),
                    hoaDon.getTongTienHang(),
                    hoaDon.getTienGiam()
            );

            PhieuTraHangChiTiet chiTiet = new PhieuTraHangChiTiet();
            chiTiet.setPhieuTraHang(phieu);
            chiTiet.setHoaDonChiTiet(hoaDonChiTiet);
            chiTiet.setGiayChiTiet(hoaDonChiTiet.getGiayChiTiet());
            chiTiet.setSoLuongTra(sanPhamRequest.soLuong());
            chiTiet.setSoLuongNhan(0);
            chiTiet.setSoLuongChapNhan(0);
            chiTiet.setSoLuongTuChoi(0);
            chiTiet.setGiaBan(hoaDonChiTiet.getGiaDonVi());
            chiTiet.setThanhTien(thanhTien);
            chiTiet.setSoTienHoan(BigDecimal.ZERO);
            chiTiet.setNhapLaiTonKho(false);
            chiTiet.setDaCapNhatTon(false);
            chiTiet.setTrangThai(1);
            chiTiet.setGhiChu(sanPhamRequest.ghiChu());
            chiTiet.setNgayTao(now);
            chiTietDaTao.add(chiTiet);
            tongTienDuKien = tongTienDuKien.add(tienDuKien);
        }

        phieu.setTongTienDuKien(tongTienDuKien);
        phieuTraHangRepository.save(phieu);
        chiTietDaTao.forEach(phieuTraHangChiTietRepository::save);
        luuLichSu(phieu, nhanVien, null, phieu.getTrangThai(), "Tạo phiếu trả hàng", request.moTa());
        return toResponse(phieu, chiTietDaTao);
    }

    @Transactional(readOnly = true)
    public List<TraHangResponse> layDanhSach(String keyword, Integer trangThai) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return phieuTraHangRepository.search(normalizedKeyword, trangThai).stream()
                .map(phieu -> toResponse(phieu, layDanhSachChiTiet(phieu.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public TraHangResponse layChiTiet(Integer phieuId) {
        PhieuTraHang phieu = phieuTraHangRepository.findDetailById(phieuId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiếu trả hàng"));
        return toResponse(phieu, layDanhSachChiTiet(phieuId));
    }

    @Transactional
    public TraHangResponse duyetPhieu(
            Integer phieuId,
            DuyetPhieuTraHangRequest request,
            UUID nhanVienId
    ) {
        PhieuTraHang phieu = layPhieu(phieuId);
        NhanVien nhanVien = layNhanVien(nhanVienId);
        TrangThaiPhieuTraHang trangThaiMoi = request.nhanHangTrucTiep()
                ? TrangThaiPhieuTraHang.DA_NHAN_HANG
                : TrangThaiPhieuTraHang.CHO_KHACH_GUI_HANG;

        chuyenTrangThai(phieu, trangThaiMoi, nhanVien, "Duyệt phiếu trả hàng", request.ghiChu());
        Instant now = Instant.now();
        phieu.setNgayDuyet(now);
        if (request.nhanHangTrucTiep()) {
            phieu.setNgayNhanHang(now);
        }
        phieuTraHangRepository.save(phieu);
        return toResponse(phieu, layDanhSachChiTiet(phieuId));
    }

    @Transactional
    public TraHangResponse xacNhanKhachGuiHang(
            Integer phieuId,
            CapNhatVanChuyenTraHangRequest request,
            UUID nhanVienId
    ) {
        PhieuTraHang phieu = layPhieu(phieuId);
        NhanVien nhanVien = layNhanVien(nhanVienId);
        phieu.setDonViVanChuyen(request.donViVanChuyen().trim());
        phieu.setMaVanDonHoan(request.maVanDonHoan().trim());
        phieu.setTrangThaiVanChuyen(1);
        phieu.setNgayGuiHang(Instant.now());
        chuyenTrangThai(
                phieu,
                TrangThaiPhieuTraHang.DANG_HOAN_HANG,
                nhanVien,
                "Xác nhận khách đã gửi hàng",
                request.ghiChu()
        );
        phieuTraHangRepository.save(phieu);
        return toResponse(phieu, layDanhSachChiTiet(phieuId));
    }

    @Transactional
    public TraHangResponse xacNhanDaNhanHang(
            Integer phieuId,
            GhiChuTraHangRequest request,
            UUID nhanVienId
    ) {
        PhieuTraHang phieu = layPhieu(phieuId);
        NhanVien nhanVien = layNhanVien(nhanVienId);
        phieu.setNgayNhanHang(Instant.now());
        phieu.setTrangThaiVanChuyen(2);
        chuyenTrangThai(
                phieu,
                TrangThaiPhieuTraHang.DA_NHAN_HANG,
                nhanVien,
                "Xác nhận đã nhận hàng trả",
                request.ghiChu()
        );
        phieuTraHangRepository.save(phieu);
        return toResponse(phieu, layDanhSachChiTiet(phieuId));
    }

    @Transactional
    public TraHangResponse batDauKiemTra(
            Integer phieuId,
            GhiChuTraHangRequest request,
            UUID nhanVienId
    ) {
        PhieuTraHang phieu = layPhieu(phieuId);
        NhanVien nhanVien = layNhanVien(nhanVienId);
        chuyenTrangThai(
                phieu,
                TrangThaiPhieuTraHang.DANG_KIEM_TRA,
                nhanVien,
                "Bắt đầu kiểm tra hàng trả",
                request.ghiChu()
        );
        phieuTraHangRepository.save(phieu);
        return toResponse(phieu, layDanhSachChiTiet(phieuId));
    }

    @Transactional
    public TraHangResponse tuChoi(
            Integer phieuId,
            TuChoiTraHangRequest request,
            UUID nhanVienId
    ) {
        PhieuTraHang phieu = layPhieu(phieuId);
        NhanVien nhanVien = layNhanVien(nhanVienId);
        phieu.setLyDoTuChoi(request.lyDo().trim());
        chuyenTrangThai(
                phieu,
                TrangThaiPhieuTraHang.TU_CHOI,
                nhanVien,
                "Từ chối yêu cầu trả hàng",
                request.lyDo()
        );
        phieuTraHangRepository.save(phieu);
        return toResponse(phieu, layDanhSachChiTiet(phieuId));
    }

    @Transactional
    public TraHangResponse huy(
            Integer phieuId,
            GhiChuTraHangRequest request,
            UUID nhanVienId
    ) {
        PhieuTraHang phieu = layPhieu(phieuId);
        NhanVien nhanVien = layNhanVien(nhanVienId);
        chuyenTrangThai(
                phieu,
                TrangThaiPhieuTraHang.DA_HUY,
                nhanVien,
                "Hủy phiếu trả hàng",
                request.ghiChu()
        );
        phieuTraHangRepository.save(phieu);
        return toResponse(phieu, layDanhSachChiTiet(phieuId));
    }

    @Transactional
    public TraHangResponse kiemTraHang(
            Integer phieuId,
            KiemTraPhieuTraHangRequest request,
            UUID nhanVienId
    ) {
        PhieuTraHang phieu = layPhieu(phieuId);
        NhanVien nhanVien = layNhanVien(nhanVienId);
        TrangThaiPhieuTraHang trangThaiHienTai = TrangThaiPhieuTraHang.tuMa(phieu.getTrangThai());
        if (trangThaiHienTai != TrangThaiPhieuTraHang.DANG_KIEM_TRA) {
            throw new BusinessException("Phiếu trả hàng chưa ở trạng thái đang kiểm tra");
        }

        List<PhieuTraHangChiTiet> chiTiet = layDanhSachChiTiet(phieuId);
        Map<Integer, KiemTraSanPhamTraRequest> ketQuaTheoChiTiet = request.sanPhams().stream()
                .collect(Collectors.toMap(
                        KiemTraSanPhamTraRequest::chiTietTraHangId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
        if (ketQuaTheoChiTiet.size() != chiTiet.size()) {
            throw new BusinessException("Vui lòng kiểm tra đầy đủ các sản phẩm trong phiếu trả hàng");
        }

        BigDecimal tongTienThucTe = BigDecimal.ZERO;
        for (PhieuTraHangChiTiet dong : chiTiet) {
            KiemTraSanPhamTraRequest ketQua = ketQuaTheoChiTiet.get(dong.getId());
            if (ketQua == null) {
                throw new BusinessException("Thiếu kết quả kiểm tra cho sản phẩm trả hàng");
            }
            if (ketQua.soLuongNhan() > dong.getSoLuongTra()) {
                throw new BusinessException("Số lượng nhận không được vượt quá số lượng yêu cầu trả");
            }
            if (ketQua.soLuongChapNhan() > ketQua.soLuongNhan()) {
                throw new BusinessException("Số lượng chấp nhận không được vượt quá số lượng đã nhận");
            }

            BigDecimal soTienHoan = ketQua.soLuongChapNhan() > 0
                    ? TinhTienHoanTraHang.tinh(
                            dong.getGiaBan(),
                            ketQua.soLuongChapNhan(),
                            phieu.getHoaDon().getTongTienHang(),
                            phieu.getHoaDon().getTienGiam()
                    )
                    : BigDecimal.ZERO;
            dong.setSoLuongNhan(ketQua.soLuongNhan());
            dong.setSoLuongChapNhan(ketQua.soLuongChapNhan());
            dong.setSoLuongTuChoi(ketQua.soLuongNhan() - ketQua.soLuongChapNhan());
            dong.setTinhTrangSanPham(ketQua.tinhTrangSanPham());
            dong.setNhapLaiTonKho(
                    ketQua.soLuongChapNhan() > 0 && Boolean.TRUE.equals(ketQua.nhapLaiTonKho())
            );
            dong.setSoTienHoan(soTienHoan);
            dong.setTrangThai(ketQua.soLuongChapNhan() > 0 ? 1 : 0);
            phieuTraHangChiTietRepository.save(dong);
            tongTienThucTe = tongTienThucTe.add(soTienHoan);
        }

        phieu.setTongTienThucTe(tongTienThucTe);
        phieu.setTongTienHoan(tongTienThucTe);
        phieu.setNgayKiemTra(Instant.now());
        TrangThaiPhieuTraHang trangThaiMoi = tongTienThucTe.compareTo(BigDecimal.ZERO) > 0
                ? TrangThaiPhieuTraHang.CHO_HOAN_TIEN
                : TrangThaiPhieuTraHang.TU_CHOI;
        chuyenTrangThai(phieu, trangThaiMoi, nhanVien, "Hoàn tất kiểm tra hàng trả", request.ghiChu());
        phieuTraHangRepository.save(phieu);
        return toResponse(phieu, chiTiet);
    }

    @Transactional
    public TraHangResponse hoanTien(
            Integer phieuId,
            HoanTienTraHangRequest request,
            UUID nhanVienId
    ) {
        PhieuTraHang phieu = layPhieu(phieuId);
        NhanVien nhanVien = layNhanVien(nhanVienId);
        if (TrangThaiPhieuTraHang.tuMa(phieu.getTrangThai())
                != TrangThaiPhieuTraHang.CHO_HOAN_TIEN) {
            throw new BusinessException("Phiếu trả hàng chưa sẵn sàng để hoàn tiền");
        }
        if (phieu.getTongTienThucTe() == null
                || phieu.getTongTienThucTe().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Phiếu trả hàng không có số tiền cần hoàn");
        }

        ThanhToan giaoDichGoc = thanhToanRepository
                .findFirstByHoaDonIdAndLoaiGiaoDichAndTrangThaiOrderByNgayThanhToanDesc(
                        phieu.getHoaDon().getId(),
                        1,
                        1
                )
                .orElseThrow(() -> new BusinessException(
                        "Không tìm thấy giao dịch thanh toán gốc đã thành công"
                ));
        String maGiaoDich = request.maGiaoDich() == null || request.maGiaoDich().isBlank()
                ? "RF" + System.currentTimeMillis()
                : request.maGiaoDich().trim();
        ThanhToan giaoDichHoan = GiaoDichHoanTienFactory.tao(
                giaoDichGoc,
                phieu,
                phieu.getTongTienThucTe(),
                request.hinhThucHoan(),
                maGiaoDich,
                request.ghiChu()
        );
        TaiKhoanNganHang taiKhoanNhan = refundBankAccountResolver.resolve(
                phieu.getKhachHang(),
                request.taiKhoanNganHangId(),
                Integer.valueOf(2).equals(request.hinhThucHoan())
        );
        if (taiKhoanNhan != null) {
            giaoDichHoan.setNganHang(taiKhoanNhan.getTenNganHang());
            giaoDichHoan.setNoiDungCk(
                    "STK: " + taiKhoanNhan.getSoTaiKhoan()
                            + " - Chủ TK: " + taiKhoanNhan.getTenChuTaiKhoan()
            );
        }
        giaoDichHoan.setNhanVien(nhanVien);
        thanhToanRepository.save(giaoDichHoan);

        List<PhieuTraHangChiTiet> chiTiet = layDanhSachChiTiet(phieuId);
        for (PhieuTraHangChiTiet dong : chiTiet) {
            if (Boolean.TRUE.equals(dong.getNhapLaiTonKho())
                    && !Boolean.TRUE.equals(dong.getDaCapNhatTon())
                    && dong.getSoLuongChapNhan() > 0) {
                GiayChiTiet bienThe = dong.getGiayChiTiet();
                bienThe.setSoLuong(bienThe.getSoLuong() + dong.getSoLuongChapNhan());
                bienThe.setNgayCapNhat(Instant.now());
                giayChiTietRepository.save(bienThe);
                dong.setDaCapNhatTon(true);
                phieuTraHangChiTietRepository.save(dong);
            }
        }

        phieu.setHinhThucHoan(request.hinhThucHoan());
        phieu.setNgayHoanTat(Instant.now());
        chuyenTrangThai(
                phieu,
                TrangThaiPhieuTraHang.HOAN_TAT,
                nhanVien,
                "Hoàn tiền trả hàng",
                request.ghiChu()
        );
        phieuTraHangRepository.save(phieu);
        HoaDon hoaDon = phieu.getHoaDon();
        hoaDon.setTrangThai(5);
        hoaDon.setNgayCapNhat(Instant.now());
        hoaDonRepository.save(hoaDon);
        return toResponse(phieu, chiTiet);
    }

    private PhieuTraHang layPhieu(Integer phieuId) {
        return phieuTraHangRepository.findById(phieuId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiếu trả hàng"));
    }

    private NhanVien layNhanVien(UUID nhanVienId) {
        return nhanVienRepository.findById(nhanVienId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên xử lý"));
    }

    private List<PhieuTraHangChiTiet> layDanhSachChiTiet(Integer phieuId) {
        return phieuTraHangChiTietRepository.findByPhieuTraHangIdOrderByIdAsc(phieuId);
    }

    private void chuyenTrangThai(
            PhieuTraHang phieu,
            TrangThaiPhieuTraHang trangThaiMoi,
            NhanVien nhanVien,
            String hanhDong,
            String ghiChu
    ) {
        TrangThaiPhieuTraHang trangThaiCu = TrangThaiPhieuTraHang.tuMa(phieu.getTrangThai());
        trangThaiCu.kiemTraCoTheChuyenSang(trangThaiMoi);
        phieu.setTrangThai(trangThaiMoi.getMa());
        phieu.setNhanVien(nhanVien);
        phieu.setNgayCapNhat(Instant.now());
        luuLichSu(
                phieu,
                nhanVien,
                trangThaiCu.getMa(),
                trangThaiMoi.getMa(),
                hanhDong,
                ghiChu
        );
    }

    private void luuLichSu(
            PhieuTraHang phieu,
            NhanVien nhanVien,
            Integer trangThaiCu,
            Integer trangThaiMoi,
            String hanhDong,
            String ghiChu
    ) {
        LichSuPhieuTraHang lichSu = new LichSuPhieuTraHang();
        lichSu.setPhieuTraHang(phieu);
        lichSu.setNhanVien(nhanVien);
        lichSu.setTrangThaiCu(trangThaiCu);
        lichSu.setTrangThaiMoi(trangThaiMoi);
        lichSu.setHanhDong(hanhDong);
        lichSu.setGhiChu(ghiChu);
        lichSu.setNgayTao(Instant.now());
        lichSuPhieuTraHangRepository.save(lichSu);
    }

    private TraHangResponse toResponse(
            PhieuTraHang phieu,
            List<PhieuTraHangChiTiet> chiTiet
    ) {
        return new TraHangResponse(
                phieu.getId(),
                phieu.getMa(),
                phieu.getHoaDon().getId(),
                phieu.getHoaDon().getMa(),
                phieu.getTrangThai(),
                TrangThaiPhieuTraHang.tuMa(phieu.getTrangThai()).getTen(),
                phieu.getLoaiYeuCau(),
                phieu.getLyDoMa(),
                phieu.getMoTa(),
                phieu.getHinhThucHoan(),
                phieu.getTongTienDuKien(),
                phieu.getTongTienThucTe(),
                phieu.getNhanVien() != null ? phieu.getNhanVien().getMa() : null,
                phieu.getHoaDon().getTenNguoiNhan(),
                phieu.getHoaDon().getSdtNguoiNhan(),
                phieu.getDonViVanChuyen(),
                phieu.getMaVanDonHoan(),
                phieu.getLyDoTuChoi(),
                phieu.getNgayTao(),
                phieu.getNgayCapNhat(),
                hinhAnhTraHangRepository.findByPhieuTraHangIdOrderByNgayTaoAsc(phieu.getId())
                        .stream()
                        .map(hinhAnh -> hinhAnh.getUrl())
                        .toList(),
                chiTiet.stream().map(this::toChiTietResponse).toList(),
                lichSuPhieuTraHangRepository.findByPhieuTraHangIdOrderByNgayTaoAsc(phieu.getId())
                        .stream()
                        .map(lichSu -> new TraHangResponse.LichSuTraHangResponse(
                                lichSu.getId(),
                                lichSu.getTrangThaiCu(),
                                lichSu.getTrangThaiMoi(),
                                TrangThaiPhieuTraHang.tuMa(lichSu.getTrangThaiMoi()).getTen(),
                                lichSu.getHanhDong(),
                                lichSu.getGhiChu(),
                                lichSu.getNhanVien() != null ? lichSu.getNhanVien().getMa() : null,
                                lichSu.getNgayTao()
                        ))
                        .toList()
        );
    }

    private TraHangResponse.ChiTietTraHangResponse toChiTietResponse(PhieuTraHangChiTiet chiTiet) {
        GiayChiTiet bienThe = chiTiet.getGiayChiTiet();
        return new TraHangResponse.ChiTietTraHangResponse(
                chiTiet.getId(),
                chiTiet.getHoaDonChiTiet() != null ? chiTiet.getHoaDonChiTiet().getId() : null,
                bienThe.getId(),
                bienThe.getGiay() != null ? bienThe.getGiay().getTen() : null,
                bienThe.getMaBienThe(),
                bienThe.getMauSac() != null ? bienThe.getMauSac().getTen() : null,
                bienThe.getKichCo() != null ? bienThe.getKichCo().getGiaTri() : null,
                chiTiet.getSoLuongTra(),
                chiTiet.getSoLuongNhan(),
                chiTiet.getSoLuongChapNhan(),
                chiTiet.getSoLuongTuChoi(),
                chiTiet.getGiaBan(),
                chiTiet.getThanhTien(),
                chiTiet.getSoTienHoan(),
                chiTiet.getTinhTrangSanPham(),
                chiTiet.getNhapLaiTonKho(),
                chiTiet.getGhiChu()
        );
    }

    private String taoMaPhieu() {
        return "TH" + System.currentTimeMillis()
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
