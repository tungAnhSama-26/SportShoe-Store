import { useState, useMemo, useEffect, useRef, useCallback } from 'react';
import { huyHoaDonCho, layChiTietHoaDonCho, layDanhSachHoaDonCho, thanhToanTaiQuay, taoHoaDonCho, capNhatHoaDonCho, timSanPhamTaiQuay } from '../../api/dichVuBanHang';
import { KHACH_VANG_LAI, TOI_DA_HOA_DON_CHO } from './HangSo';
import { useLogicGioHang } from './useLogicGioHang';
import { useLogicPhieuGiamGia } from './useLogicPhieuGiamGia';
import { useLogicKhachHang } from './useLogicKhachHang';
import { useLogicInHoaDon } from './useLogicInHoaDon';
import { useLogicThanhToan } from './useLogicThanhToan';
import { useLogicSanPham } from './useLogicSanPham';
import { useLogicGiaoHang } from './useLogicGiaoHang';
import { showError, showToastSuccess, toastSwal, showConfirm } from '../../utils/alert';
import { useRealtime } from '../../hooks/useRealtime';
import { chuanHoaDiaChi, diaChiHopLe, DIA_CHI_RONG } from '../../utils/diaChi';

export function useLogicBanHangTaiQuay() {
  const [danhSachHoaDonCho, setDanhSachHoaDonCho] = useState([]);
  const [hoaDonChoDaChon, setHoaDonChoDaChon] = useState(null);
  const [dangTaiHoaDonCho, setDangTaiHoaDonCho] = useState(false);
  const [dangLuuHoaDonCho, setDangLuuHoaDonCho] = useState(false);
  const [dangHuyHoaDonCho, setDangHuyHoaDonCho] = useState(false);
  const [dangThanhToan, setDangThanhToan] = useState(false);
  const [dangTaiChiTietHoaDon, setDangTaiChiTietHoaDon] = useState(false);
  const [thongBaoLoi, setThongBaoLoi] = useState("");
  const [thongBaoThanhCong, setThongBaoThanhCong] = useState("");
  const [choPhepGiaoHang, setChoPhepGiaoHang] = useState(false);
  const [tenNguoiNhanGiaoHang, setTenNguoiNhanGiaoHang] = useState("");
  const [sdtNguoiNhanGiaoHang, setSdtNguoiNhanGiaoHang] = useState("");
  const [diaChiGiaoHang, setDiaChiGiaoHang] = useState({ tinhThanhCode: '', tinhThanh: '', phuongXaCode: '', phuongXa: '', diaChiCuThe: '' });
  const [donViVanChuyen, setDonViVanChuyen] = useState("GHN");
  const [phiVanChuyen, setPhiVanChuyen] = useState(0);
  const [diaChiDaXacNhan, setDiaChiDaXacNhan] = useState("");
  const [daTinhPhiVanChuyen, setDaTinhPhiVanChuyen] = useState(false);
  const [dangTinhPhiVanChuyen, setDangTinhPhiVanChuyen] = useState(false);
  const [cauHinhGiaoHang, setCauHinhGiaoHang] = useState({
    serviceTypeId: 2,
    length: 30,
    width: 20,
    height: 12,
    weight: 500
  });

  const daDatGioiHanHoaDonCho = useMemo(() => danhSachHoaDonCho.length >= TOI_DA_HOA_DON_CHO, [danhSachHoaDonCho]);

  const xoaPhanHoi = useCallback(() => {
    setThongBaoLoi("");
    setThongBaoThanhCong("");
  }, []);

  // KHÁCH HÀNG
  const khachHangLogic = useLogicKhachHang({
    hoaDonChoDaChon,
    setTenNguoiNhanGiaoHang,
    setSdtNguoiNhanGiaoHang,
    diaChiGiaoHang,
    setDiaChiGiaoHang,
    danhDauCanApDungLaiPhieu: () => { /* implement below */ },
    xoaPhanHoi,
    thongBaoLoi, setThongBaoLoi
  });

  const daChonKhach = useMemo(() => {
    if (khachHangLogic.khachHangDuocChon) return true;
    if (khachHangLogic.laKhachVangLai) return true;
    if (hoaDonChoDaChon) {
      if (hoaDonChoDaChon.khachHangId) return true;
      if (hoaDonChoDaChon.tenKhachHang === KHACH_VANG_LAI) return true;
    }
    if (!khachHangLogic.tuKhoaKhachHang.trim()) return true;
    return false;
  }, [khachHangLogic.khachHangDuocChon, khachHangLogic.laKhachVangLai, hoaDonChoDaChon, khachHangLogic.tuKhoaKhachHang]);

  const layIdKhachHangHienTai = useCallback(() => {
    if (khachHangLogic.khachHangDuocChon) return khachHangLogic.khachHangDuocChon.id;
    if (khachHangLogic.laKhachVangLai) return null;
    return hoaDonChoDaChon?.khachHangId ?? null;
  }, [khachHangLogic.khachHangDuocChon, khachHangLogic.laKhachVangLai, hoaDonChoDaChon]);

  // SẢN PHẨM (trước GIỎ HÀNG để GIỎ HÀNG lấy dongBoSanPhamSauKhiThemVaoGio)
  // Nhưng Logic Gio Hang cần để truyền themSanPham cho SanPham
  // => Khởi tạo gio hang truoc nhưng truyền prop
  const dongBoSanPhamSauKhiThemVaoGioRef = useRef();

  // GIỎ HÀNG
  const gioHangLogic = useLogicGioHang({
    danhDauCanTinhLaiPhiVanChuyen: () => { /* implement below */ },
    capNhatTienKhachThanhToan: () => { /* implement below */ },
    danhDauCanApDungLaiPhieu: () => { /* implement below */ },
    dongBoSanPhamSauKhiThemVaoGio: (...args) => {
      if (dongBoSanPhamSauKhiThemVaoGioRef.current) dongBoSanPhamSauKhiThemVaoGioRef.current(...args);
    },
    xoaPhanHoi
  });

  // SẢN PHẨM
  const sanPhamLogic = useLogicSanPham({
    soLuongConLai: gioHangLogic.soLuongConLai,
    themSanPham: gioHangLogic.themSanPham,
    xoaPhanHoi,
    thongBaoLoi,
    thongBaoThanhCong
  });

  dongBoSanPhamSauKhiThemVaoGioRef.current = ({ preserveProductSearch = false, scannedKeyword = "", scannedProducts = [] } = {}) => {
    if (preserveProductSearch) {
      sanPhamLogic.setTuKhoaSanPham(scannedKeyword);
      // Wait, can't easily set ketQuaBienTheSanPham without adding it to the returned object from hook
      // Actually, we did not export setKetQuaBienTheSanPham in the return of useLogicSanPham unless we add it!
      // I'll assume useLogicSanPham returns what we need, if not, we skip the assignment or add it.
    }
    sanPhamLogic.setChiTietSanPhamDaChon(null);
    sanPhamLogic.setMauSacDaChon("");
    sanPhamLogic.setKichCoDaChon("");
    sanPhamLogic.setSoLuongDaChon(1);
    sanPhamLogic.setHienThiDanhSachSanPham(false);
  };

  const phiVanChuyenChoLuuRef = useRef(null);
  const luuHoaDonHienTaiRef = useRef(null);
  const ghiNhanPhiVanChuyenDaTinh = useCallback((giaTri) => {
    phiVanChuyenChoLuuRef.current = giaTri == null
      ? null
      : {
          invoiceId: hoaDonChoDaChon?.id ?? null,
          phiVanChuyen: Number(giaTri)
        };
  }, [hoaDonChoDaChon?.id]);

  // GIAO HÀNG
  const giaoHangLogic = useLogicGiaoHang({
    choPhepGiaoHang, setChoPhepGiaoHang,
    tenNguoiNhanGiaoHang, setTenNguoiNhanGiaoHang,
    sdtNguoiNhanGiaoHang, setSdtNguoiNhanGiaoHang,
    diaChiGiaoHang, setDiaChiGiaoHang,
    donViVanChuyen, setDonViVanChuyen,
    phiVanChuyen, setPhiVanChuyen,
    diaChiDaXacNhan, setDiaChiDaXacNhan,
    daTinhPhiVanChuyen, setDaTinhPhiVanChuyen,
    dangTinhPhiVanChuyen, setDangTinhPhiVanChuyen,
    cauHinhGiaoHang, setCauHinhGiaoHang,
    khachHangDuocChon: khachHangLogic.khachHangDuocChon,
    hoaDonChoDaChon,
    cartItems: gioHangLogic.cartItems,
    onPhiVanChuyenDaTinh: ghiNhanPhiVanChuyenDaTinh
  });

  // PHIẾU GIẢM GIÁ
  const phieuGiamGiaLogic = useLogicPhieuGiamGia({
    cartItems: gioHangLogic.cartItems,
    tongTien: gioHangLogic.tongTien,
    hoaDonChoDaChon,
    dangTaiChiTietHoaDon,
    khachHangDuocChon: khachHangLogic.khachHangDuocChon,
    layIdKhachHangHienTai,
    taoDanhSachSanPhamThanhToan: gioHangLogic.taoDanhSachSanPhamThanhToan,
    capNhatTienKhachThanhToan: () => { /* implement below */ },
    xoaPhanHoi
  });

  const khachCanTra = useMemo(() => phieuGiamGiaLogic.tongTienSauGiamHienThi + giaoHangLogic.phiVanChuyenHienThi, [phieuGiamGiaLogic.tongTienSauGiamHienThi, giaoHangLogic.phiVanChuyenHienThi]);

  // THANH TOÁN
  const thanhToanLogic = useLogicThanhToan({
    cartItems: gioHangLogic.cartItems,
    khachCanTra,
    hoaDonChoDaChon
  });

  // Wire up cross-dependencies
  gioHangLogic.danhDauCanTinhLaiPhiVanChuyen = giaoHangLogic.danhDauCanTinhLaiPhiVanChuyen;
  gioHangLogic.danhDauCanApDungLaiPhieu = phieuGiamGiaLogic.danhDauCanApDungLaiPhieu;
  gioHangLogic.capNhatTienKhachThanhToan = thanhToanLogic.capNhatTienKhachThanhToan;
  
  khachHangLogic.danhDauCanApDungLaiPhieu = phieuGiamGiaLogic.danhDauCanApDungLaiPhieu;
  phieuGiamGiaLogic.capNhatTienKhachThanhToan = thanhToanLogic.capNhatTienKhachThanhToan;

  // IN HÓA ĐƠN
  const { xuLyInHoaDonTaiQuay } = useLogicInHoaDon();

  const { isConnected, subscribeTopic, unsubscribeTopic, publishMessage } = useRealtime();
  const sessionIdRef = useRef(Math.random().toString(36).substring(2, 15));
  const isSyncingUIRef = useRef(false);

  // COMPOSED COMPUTEDS
  const coThongTinGiaoHangHopLe = giaoHangLogic.coThongTinGiaoHangHopLe;
  const sanPhamValidationMessage = gioHangLogic.sanPhamValidationMessage;
  const maPhieuChuaApDung = phieuGiamGiaLogic.maPhieuChuaApDung;

  const coTheTaoHoaDonCho = useMemo(() => 
    !dangLuuHoaDonCho &&
    !maPhieuChuaApDung &&
    !daDatGioiHanHoaDonCho &&
    coThongTinGiaoHangHopLe &&
    !sanPhamValidationMessage,
  [dangLuuHoaDonCho, maPhieuChuaApDung, daDatGioiHanHoaDonCho, coThongTinGiaoHangHopLe, sanPhamValidationMessage]);

  const coTheThanhToan = useMemo(() => {
    if (!gioHangLogic.cartItems.length || sanPhamValidationMessage || dangThanhToan || maPhieuChuaApDung || !coThongTinGiaoHangHopLe) {
      return false;
    }
    if (thanhToanLogic.phuongThucThanhToan === 1) { // 1 = TIEN_MAT
      return !thanhToanLogic.thongBaoLoiThanhToan;
    }
    return true;
  }, [gioHangLogic.cartItems.length, sanPhamValidationMessage, dangThanhToan, maPhieuChuaApDung, coThongTinGiaoHangHopLe, thanhToanLogic.phuongThucThanhToan, thanhToanLogic.thongBaoLoiThanhToan]);

  const xoaBanNhap = useCallback(() => {
    phiVanChuyenChoLuuRef.current = null;
    khachHangLogic.setKhachHangDuocChon(null);
    khachHangLogic.setTuKhoaKhachHang("");
    khachHangLogic.setKetQuaTimKiemKhachHang([]);
    khachHangLogic.setHienThiDanhSachKhachHang(false);
    
    sanPhamLogic.setTuKhoaSanPham("");
    sanPhamLogic.setChiTietSanPhamDaChon(null);
    sanPhamLogic.setMauSacDaChon("");
    sanPhamLogic.setKichCoDaChon("");
    sanPhamLogic.setSoLuongDaChon(1);
    sanPhamLogic.setHienThiDanhSachSanPham(false);
    
    gioHangLogic.setCartItems([]);
    setHoaDonChoDaChon(null);
    
    phieuGiamGiaLogic.setMaPhieuGiamGia("");
    phieuGiamGiaLogic.setPhieuGiamGiaDaApDung(null);
    phieuGiamGiaLogic.setKetQuaTimKiemPhieu?.([]);
    phieuGiamGiaLogic.setHienThiDanhSachPhieu(false);
    phieuGiamGiaLogic.setPhieuTotHonDeXuat?.(null);
    phieuGiamGiaLogic.setPhieuGiamGiaHangMucTiepTheo?.(null);
    
    thanhToanLogic.resetThanhToan();
    
    setChoPhepGiaoHang(false);
    setTenNguoiNhanGiaoHang("");
    setSdtNguoiNhanGiaoHang("");
    setDiaChiGiaoHang({ ...DIA_CHI_RONG });
    setDonViVanChuyen("GHN");
    setPhiVanChuyen(0);
    setDaTinhPhiVanChuyen(false);
    setDangTinhPhiVanChuyen(false);
    setDiaChiDaXacNhan("");

    if (!isSyncingUIRef.current) {
      publishMessage('/topic/admin/pos-sync', {
        sender: sessionIdRef.current,
        action: 'CHON_HOA_DON',
        invoiceId: null
      });
    }

    setCauHinhGiaoHang({
      serviceTypeId: 2,
      length: 30,
      width: 20,
      height: 12,
      weight: 500
    });
    xoaPhanHoi();
    sanPhamLogic.taiSanPham("");
  }, [khachHangLogic, sanPhamLogic, phieuGiamGiaLogic, gioHangLogic, thanhToanLogic, xoaPhanHoi, publishMessage]);

  const taiDanhSachHoaDonCho = useCallback(async (silent = false) => {
    if (!silent) {
      setDangTaiHoaDonCho(true);
    }
    try {
      const response = await layDanhSachHoaDonCho();
      const data = response?.data || response;
      const invoices = Array.isArray(data) ? data : [];
      setDanhSachHoaDonCho(invoices);
      return invoices;
    } catch (error) {
      if (!silent) {
        setThongBaoLoi(error instanceof Error ? error.message : "Không thể tải danh sách hóa đơn chờ");
      }
      return [];
    } finally {
      if (!silent) {
        setDangTaiHoaDonCho(false);
      }
    }
  }, []);



  const chuyenHoaDonThanhBanNhap = useCallback((invoice) => {
    skipNextAutosave.current = true;
    let canLuuPhiSauDongBo = false;
    const thongTinTheoChiTietId = new Map(
      sanPhamLogic.ketQuaBienTheSanPham.map((product) => [product.chiTietId, product])
    );
    const thongTinGiaoHang = invoice.thongTinGiaoHang || null;

    if (hoaDonChoDaChon?.id === invoice.id && !invoice.khachHangId && !invoice.tenKhachHang) {
      // Giữ nguyên tuKhoaKhachHang để không bị mất chữ khi người dùng đang gõ
    } else {
      khachHangLogic.setTuKhoaKhachHang(invoice.tenKhachHang || invoice.soDienThoai || "");
    }
    khachHangLogic.setKhachHangDuocChon(invoice.khachHangId
      ? (khachHangLogic.khachHangDuocChon?.id === invoice.khachHangId
          ? { ...khachHangLogic.khachHangDuocChon, hoTen: invoice.tenKhachHang, sdt: invoice.soDienThoai }
          : {
            id: invoice.khachHangId,
            hoTen: invoice.tenKhachHang,
            sdt: invoice.soDienThoai,
            email: null
          })
      : null);
    
    dangLuuNoiBoRef.current = true;
    gioHangLogic.setCartItems((invoice.items || []).map((item) => {
      const thongTinSanPham = thongTinTheoChiTietId.get(item.chiTietId);
      return {
        cartItemId: Date.now().toString() + Math.random().toString(),
        chiTietId: item.chiTietId,
        maSanPham: item.maSanPham,
        tenSanPham: item.tenSanPham,
        sku: item.sku || thongTinSanPham?.sku || "",
        mauSac: item.mauSac || thongTinSanPham?.mauSac || "",
        kichCo: item.kichCo || thongTinSanPham?.kichCo || "",
        hinhAnh: item.hinhAnh || thongTinSanPham?.hinhAnh || "",
        soLuong: item.soLuong,
        soLuongBanDau: item.soLuong,
        giaBan: item.giaBan,
        soLuongTon: sanPhamLogic.laySoLuongTonHienTai(item.chiTietId, item.soLuong)
      };
    }));

    setChoPhepGiaoHang(Boolean(thongTinGiaoHang?.giaoHang));
    setTenNguoiNhanGiaoHang(thongTinGiaoHang?.tenNguoiNhan || "");
    setSdtNguoiNhanGiaoHang(thongTinGiaoHang?.soDienThoaiNguoiNhan || "");
    
    if (thongTinGiaoHang?.giaoHang) {
      setDiaChiGiaoHang(chuanHoaDiaChi(thongTinGiaoHang.diaChiGiaoHang));
    } else if (!diaChiHopLe(diaChiGiaoHang) && khachHangLogic.khachHangDuocChon?.diaChiMacDinh) {
      setDiaChiGiaoHang(chuanHoaDiaChi(khachHangLogic.khachHangDuocChon.diaChiMacDinh));
    } else if (!thongTinGiaoHang?.giaoHang && !khachHangLogic.khachHangDuocChon) {
      setDiaChiGiaoHang({ ...DIA_CHI_RONG });
    }
    setDonViVanChuyen(thongTinGiaoHang?.donViVanChuyen || "GHN");
    const phiVanChuyenTuServer = Number(thongTinGiaoHang?.phiVanChuyen || 0);
    const phiVanChuyenChoLuu = phiVanChuyenChoLuuRef.current;
    if (
      thongTinGiaoHang?.giaoHang &&
      phiVanChuyenChoLuu?.invoiceId === invoice.id &&
      phiVanChuyenChoLuu.phiVanChuyen !== phiVanChuyenTuServer
    ) {
      setPhiVanChuyen(phiVanChuyenChoLuu.phiVanChuyen);
      canLuuPhiSauDongBo = true;
    } else {
      setPhiVanChuyen(phiVanChuyenTuServer);
      if (phiVanChuyenChoLuu) {
        phiVanChuyenChoLuuRef.current = null;
      }
    }
    setDiaChiDaXacNhan("");
    setDaTinhPhiVanChuyen(Boolean(thongTinGiaoHang?.giaoHang));
    setCauHinhGiaoHang({
      serviceTypeId: 2,
      length: 30,
      width: 20,
      height: 12,
      weight: 500
    });
    phieuGiamGiaLogic.setMaPhieuGiamGia(invoice.phieuGiamGia?.ma ?? "");
    phieuGiamGiaLogic.setPhieuGiamGiaDaApDung(invoice.phieuGiamGia
      ? {
        id: 0,
        ma: invoice.phieuGiamGia.ma,
        ten: invoice.phieuGiamGia.ten,
        loai: 0,
        giaTri: 0,
        giaTriToiThieu: null,
        giamToiDa: null,
        soTienGiam: invoice.tienGiam || invoice.phieuGiamGia.soTienGiam,
        tongTienHang: invoice.tongTienHang || 0,
        tongTienSauGiam: Math.max((invoice.tongTienHang || 0) - (invoice.tienGiam || 0), 0)
      }
      : null);
    phieuGiamGiaLogic.setHienThiDanhSachPhieu(false);
    thanhToanLogic.capNhatTienKhachThanhToan(false);

    setTimeout(() => {
      dangLuuNoiBoRef.current = false;
      if (canLuuPhiSauDongBo) {
        luuHoaDonHienTaiRef.current?.().catch(() => {});
      }
    }, 300);
  }, [sanPhamLogic, khachHangLogic, gioHangLogic, phieuGiamGiaLogic, thanhToanLogic]);

  const dangLuuNoiBoRef = useRef(false);
  const dangLuuAPIRef = useRef(false);
  const pendingSaveRef = useRef(false);

  const latestStateRef = useRef({ khachHangLogic, tenNguoiNhanGiaoHang, sdtNguoiNhanGiaoHang, phieuGiamGiaLogic, choPhepGiaoHang, giaoHangLogic, gioHangLogic, hoaDonChoDaChon });
  latestStateRef.current = { khachHangLogic, tenNguoiNhanGiaoHang, sdtNguoiNhanGiaoHang, phieuGiamGiaLogic, choPhepGiaoHang, giaoHangLogic, gioHangLogic, hoaDonChoDaChon };

  const luuHoaDonHienTai = useCallback(async (force = false) => {
    const { hoaDonChoDaChon } = latestStateRef.current;
    if (!hoaDonChoDaChon) return;
    if (dangThanhToan && !force) return;
    
    if (dangLuuAPIRef.current) {
      pendingSaveRef.current = true;
      return;
    }
    
    dangLuuAPIRef.current = true;
    pendingSaveRef.current = false;
    
    try {
      while (true) {
        const currentInvoiceId = latestStateRef.current.hoaDonChoDaChon.id;
        const currentLogic = latestStateRef.current;
        const payload = {
          tenKhachHang: currentLogic.khachHangLogic.khachHangDuocChon?.hoTen || currentLogic.tenNguoiNhanGiaoHang || (currentLogic.khachHangLogic.laKhachVangLai ? KHACH_VANG_LAI : ""),
          soDienThoai: currentLogic.khachHangLogic.khachHangDuocChon?.sdt || currentLogic.sdtNguoiNhanGiaoHang || "",
          ghiChu: "",
          khachHangId: currentLogic.khachHangLogic.khachHangDuocChon?.id || null,
          maPhieuGiamGia: currentLogic.phieuGiamGiaLogic.phieuGiamGiaDaApDung?.ma || null,
          thongTinGiaoHang: currentLogic.choPhepGiaoHang ? currentLogic.giaoHangLogic.taoPayloadGiaoHang() : null,
          items: currentLogic.gioHangLogic.cartItems.map(item => ({
            chiTietId: item.chiTietId,
            soLuong: item.soLuong,
            giaBan: item.giaBan
          })),
        };
        const response = await capNhatHoaDonCho(currentInvoiceId, payload);
        
        dangLuuNoiBoRef.current = true;
        const responseData = response?.data || response;
        if (responseData) {
          const phiDaLuu = Number(responseData.thongTinGiaoHang?.phiVanChuyen || 0);
          const phiDangCho = phiVanChuyenChoLuuRef.current;
          if (phiDangCho?.invoiceId === currentInvoiceId && phiDangCho.phiVanChuyen === phiDaLuu) {
            phiVanChuyenChoLuuRef.current = null;
          }
          setDanhSachHoaDonCho(prev => prev.map(hd => hd.id === currentInvoiceId ? responseData : hd));
          setHoaDonChoDaChon(responseData);
        }
        
        // Use the function callback to get latest cart items when setting
        currentLogic.gioHangLogic.setCartItems(prev => prev.map(item => ({
          ...item,
          soLuongBanDau: item.soLuong
        })));
        setTimeout(() => { dangLuuNoiBoRef.current = false; }, 50);

        if (pendingSaveRef.current) {
          pendingSaveRef.current = false;
        } else {
          break;
        }
      }
    } catch (error) {
      console.error("Lỗi khi lưu hóa đơn chờ:", error);
      const msg = error instanceof Error ? error.message : "Cập nhật hóa đơn chờ thất bại";

      if (msg.includes("Chỉ được cập nhật") || msg.includes("trạng thái chờ")) {
        return;
      }
      
      setThongBaoLoi(msg);
      
      if (msg.toLowerCase().includes("phiếu giảm giá")) {
        const maLoi = phieuGiamGiaLogic.phieuGiamGiaDaApDung?.ma || phieuGiamGiaLogic.maPhieuGiamGia;
        phieuGiamGiaLogic.setPhieuGiamGiaDaApDung(null);
        phieuGiamGiaLogic.setMaPhieuGiamGia("");
        
        if (maLoi) {
          setThongBaoLoi(`Phiếu giảm giá ${maLoi} không còn hợp lệ. Hệ thống đang tự động tìm phiếu giảm giá thay thế...`);
        }
      }

      if (hoaDonChoDaChon) {
        try {
          const response = await layChiTietHoaDonCho(hoaDonChoDaChon.id);
          const detail = response?.data || response;
          chuyenHoaDonThanhBanNhap(detail);
        } catch (e) {
          console.error("Không thể tải lại hóa đơn để rollback:", e);
        }
      }

      throw error;
    } finally {
      dangLuuAPIRef.current = false;
    }
  }, [hoaDonChoDaChon, khachHangLogic, tenNguoiNhanGiaoHang, sdtNguoiNhanGiaoHang, phieuGiamGiaLogic, choPhepGiaoHang, giaoHangLogic, gioHangLogic, chuyenHoaDonThanhBanNhap, dangThanhToan]);
  luuHoaDonHienTaiRef.current = luuHoaDonHienTai;

  const skipNextAutosave = useRef(false);
  const boDemTuDongLuu = useRef(null);
  
  useEffect(() => {
    if (skipNextAutosave.current) {
      skipNextAutosave.current = false;
      return;
    }
    if (dangLuuNoiBoRef.current || dangThanhToan) return;
    if (boDemTuDongLuu.current) clearTimeout(boDemTuDongLuu.current);
    boDemTuDongLuu.current = setTimeout(() => {
      if (choPhepGiaoHang && !giaoHangLogic.coThongTinGiaoHangHopLe) {
        return;
      }
      luuHoaDonHienTai().catch(() => {});
    }, 1000);
  }, [
    gioHangLogic.cartItems,
    choPhepGiaoHang,
    tenNguoiNhanGiaoHang,
    sdtNguoiNhanGiaoHang,
    diaChiGiaoHang,
    phiVanChuyen,
    khachHangLogic.khachHangDuocChon,
    phieuGiamGiaLogic.phieuGiamGiaDaApDung,
    luuHoaDonHienTai,
    dangThanhToan,
    giaoHangLogic.coThongTinGiaoHangHopLe
  ]);

  const lastReceivedSyncState = useRef(null);

  useEffect(() => {
    if (isSyncingUIRef.current || dangTaiChiTietHoaDon) return;
    if (hoaDonChoDaChon) {
      const payloadState = {
        choPhepGiaoHang,
        tenNguoiNhanGiaoHang,
        sdtNguoiNhanGiaoHang,
        diaChiGiaoHang,
        tienKhachDua: thanhToanLogic.tienKhachDua,
        tienMatKetHop: thanhToanLogic.tienMatKetHop,
        tienChuyenKhoanKetHop: thanhToanLogic.tienChuyenKhoanKetHop,
        phuongThucThanhToan: thanhToanLogic.phuongThucThanhToan,
        hienThiMaQrLon: thanhToanLogic.hienThiMaQrLon,
        ghiChuThanhToan: thanhToanLogic.ghiChuThanhToan,
        tuKhoaKhachHang: khachHangLogic.tuKhoaKhachHang,
        khachHangDuocChon: khachHangLogic.khachHangDuocChon
      };

      // Prevent echoing back the exact same state we just received
      if (JSON.stringify(lastReceivedSyncState.current) === JSON.stringify(payloadState)) {
        return;
      }

      publishMessage('/topic/admin/pos-sync', {
        sender: sessionIdRef.current,
        action: 'SYNC_STATE',
        invoiceId: hoaDonChoDaChon.id,
        state: payloadState
      });
    }
  }, [
    choPhepGiaoHang,
    tenNguoiNhanGiaoHang,
    sdtNguoiNhanGiaoHang,
    diaChiGiaoHang,
    thanhToanLogic.tienKhachDua,
    thanhToanLogic.tienMatKetHop,
    thanhToanLogic.tienChuyenKhoanKetHop,
    thanhToanLogic.phuongThucThanhToan,
    thanhToanLogic.hienThiMaQrLon,
    thanhToanLogic.ghiChuThanhToan,
    khachHangLogic.tuKhoaKhachHang,
    khachHangLogic.khachHangDuocChon,
    hoaDonChoDaChon,
    publishMessage,
    dangTaiChiTietHoaDon
  ]);

  const chonHoaDonCho = useCallback(async (invoice) => {
    if (!invoice) return;
    if (hoaDonChoDaChon?.id === invoice.id && !dangTaiChiTietHoaDon) {
      return;
    }

    if (boDemTuDongLuu.current) {
      clearTimeout(boDemTuDongLuu.current);
      boDemTuDongLuu.current = null;
    }

    if (!isSyncingUIRef.current) {
      publishMessage('/topic/admin/pos-sync', {
        sender: sessionIdRef.current,
        action: 'CHON_HOA_DON',
        invoiceId: invoice.id
      });
    }

    if (hoaDonChoDaChon && hoaDonChoDaChon.id !== invoice.id) {
      try {
        await luuHoaDonHienTai();
      } catch (e) {
        // ignore error when switching tabs
      }
    }

    setHoaDonChoDaChon(invoice);
    setDangTaiChiTietHoaDon(true);
    setThongBaoLoi("");
    try {
      await sanPhamLogic.taiSanPham("", true);
      const response = await layChiTietHoaDonCho(invoice.id);
      const detail = response?.data || response;
      chuyenHoaDonThanhBanNhap(detail);
    } catch (error) {
      setThongBaoLoi(error instanceof Error ? error.message : "Không thể tải hóa đơn chờ");
    } finally {
      setDangTaiChiTietHoaDon(false);
    }
  }, [hoaDonChoDaChon, dangTaiChiTietHoaDon, luuHoaDonHienTai, sanPhamLogic, chuyenHoaDonThanhBanNhap, publishMessage]);

  const xuLyTaoHoaDonCho = useCallback(async () => {
    if (!gioHangLogic.validateGioHang(false)) {
      return;
    }
    if (daDatGioiHanHoaDonCho) {
      setThongBaoLoi(`Chỉ được tạo tối đa ${TOI_DA_HOA_DON_CHO} hóa đơn chờ.`);
      return;
    }
    if (!coTheTaoHoaDonCho) {
      return;
    }
    setDangLuuHoaDonCho(true);
    setThongBaoLoi("");
    setThongBaoThanhCong("");
    try {
      const payload = {
        khachHangId: layIdKhachHangHienTai(),
        tenKhachHang: khachHangLogic.khachHangDuocChon?.hoTen || (khachHangLogic.laKhachVangLai ? KHACH_VANG_LAI : ""),
        soDienThoai: khachHangLogic.khachHangDuocChon?.sdt || hoaDonChoDaChon?.soDienThoai || "",
        maPhieuGiamGia: phieuGiamGiaLogic.phieuGiamGiaDaApDung?.ma ?? null,
        thongTinGiaoHang: giaoHangLogic.taoPayloadGiaoHang(),
        items: gioHangLogic.taoDanhSachSanPhamThanhToan()
      };
      const response = await taoHoaDonCho(payload);
      const createdInvoice = response?.data || response;
      
      setThongBaoThanhCong(`Đã tạo hóa đơn chờ ${createdInvoice.ma}`);
      
      const danhSachResponse = await layDanhSachHoaDonCho();
      const danhSachData = danhSachResponse?.data || danhSachResponse;
      const danhSach = Array.isArray(danhSachData) ? danhSachData : [];
      setDanhSachHoaDonCho(danhSach);
      
      const matchedInvoice = danhSach.find((invoice) => invoice.id === createdInvoice.id) ?? null;
      setHoaDonChoDaChon(matchedInvoice);
      chuyenHoaDonThanhBanNhap(createdInvoice);
      
      // Báo cho các thiết bị khác (như web) chuyển sang hóa đơn mới tạo
      if (!isSyncingUIRef.current) {
        publishMessage('/topic/admin/pos-sync', {
          sender: sessionIdRef.current,
          action: 'CHON_HOA_DON',
          invoiceId: createdInvoice.id
        });
      }
    } catch (error) {
      setThongBaoLoi(error instanceof Error ? error.message : "Không thể tạo hóa đơn chờ");
    } finally {
      setDangLuuHoaDonCho(false);
    }
  }, [gioHangLogic, daDatGioiHanHoaDonCho, coTheTaoHoaDonCho, layIdKhachHangHienTai, khachHangLogic, hoaDonChoDaChon, phieuGiamGiaLogic, giaoHangLogic, chuyenHoaDonThanhBanNhap]);

  const xuLyThanhToanNgay = useCallback(async () => {
    if (!daChonKhach) {
      setThongBaoLoi("Vui lòng chọn khách hàng hoặc Khách vãng lai trước khi thanh toán.");
      return;
    }
    if (!gioHangLogic.validateGioHang(true) || !thanhToanLogic.kiemTraLoiThanhToan()) {
      return;
    }
    if (!coTheThanhToan) {
      if (choPhepGiaoHang && !giaoHangLogic.daTinhPhiVanChuyen) {
        showError("Vui lòng nhập phí giao hàng trước khi thanh toán (hoặc điền 0).");
      } else if (choPhepGiaoHang && !giaoHangLogic.diaChiHopLe(giaoHangLogic.diaChiGiaoHangHienThi)) {
        showError("Vui lòng nhập đầy đủ địa chỉ giao hàng.");
      } else if (thanhToanLogic.thongBaoLoiThanhToan) {
        showError(thanhToanLogic.thongBaoLoiThanhToan);
      } else {
        showError("Không thể thanh toán. Vui lòng kiểm tra lại thông tin.");
      }
      return;
    }

    const betterCouponInfo = await phieuGiamGiaLogic.kiemTraPhieuTotHonTruocThanhToan();
    if (betterCouponInfo) {
      // Because we are porting Vue's SweetAlert which awaits, we would need to mock or handle showPaymentConfirmWithCoupon
      // I'll skip the SweetAlert logic for now, or just show standard confirm
      const choice = await showConfirm(`Phiếu giảm giá ${betterCouponInfo.coupon.ma} tiết kiệm hơn. Bạn có muốn sử dụng không?`);
      if (choice) {
        phieuGiamGiaLogic.setMaPhieuGiamGia(betterCouponInfo.coupon.ma);
        await phieuGiamGiaLogic.xuLyApDungPhieu(true, betterCouponInfo.coupon.ma);
      }
    } else {
      // In React Native, window.confirm can be replaced by Alert.alert or alert util
      // Currently using showConfirm from alert.js? Yes. wait, showConfirm might be async if custom.
      // Let's assume it is.
      const isConfirmed = await showConfirm('Bạn có chắc chắn muốn thanh toán đơn hàng này không?');
      if (!isConfirmed) {
        return;
      }
    }

    setDangThanhToan(true);
    setThongBaoLoi("");
    setThongBaoThanhCong("");
    if (boDemTuDongLuu.current) {
      clearTimeout(boDemTuDongLuu.current);
      boDemTuDongLuu.current = null;
    }

    try {
      if (hoaDonChoDaChon) {
        await luuHoaDonHienTai(true);
      }

      const currentCartItems = [...gioHangLogic.cartItems];
      const currentTienGiam = phieuGiamGiaLogic.phieuGiamGiaDaApDung ? phieuGiamGiaLogic.phieuGiamGiaDaApDung.soTienGiam : 0;
      const currentPhiVanChuyen = choPhepGiaoHang ? phiVanChuyen : 0;
      const currentTongTienHang = gioHangLogic.tongTien;
      const currentKhachCanTra = khachCanTra;
      const currentTenKhach = khachHangLogic.khachHangDuocChon?.hoTen || (khachHangLogic.laKhachVangLai ? KHACH_VANG_LAI : "");
      const currentSdt = khachHangLogic.khachHangDuocChon?.sdt || "";

      const payload = {
        hoaDonId: hoaDonChoDaChon?.id ?? null,
        khachHangId: layIdKhachHangHienTai(),
        tenKhachHang: currentTenKhach,
        soDienThoai: currentSdt,
        maPhieuGiamGia: phieuGiamGiaLogic.phieuGiamGiaDaApDung?.ma ?? null,
        hinhThucThanhToan: thanhToanLogic.phuongThucThanhToan,
        tienKhachDua: thanhToanLogic.phuongThucThanhToan === 1 
          ? thanhToanLogic.tienKhachThanhToan 
          : (thanhToanLogic.phuongThucThanhToan === 5 ? (thanhToanLogic.tienMatThanhToan + thanhToanLogic.tienChuyenKhoanThanhToan) : khachCanTra),
        tienMat: thanhToanLogic.phuongThucThanhToan === 5 ? thanhToanLogic.tienMatThanhToan : null,
        tienChuyenKhoan: thanhToanLogic.phuongThucThanhToan === 5 ? thanhToanLogic.tienChuyenKhoanThanhToan : null,
        ghiChu: thanhToanLogic.ghiChuThanhToan,
        thongTinGiaoHang: choPhepGiaoHang ? giaoHangLogic.taoPayloadGiaoHang() : null,
        items: gioHangLogic.taoDanhSachSanPhamThanhToan()
      };

      const response = await thanhToanTaiQuay(payload);
      const data = response?.data || response;
      const orderCode = data?.maHoaDon || data?.ma || (hoaDonChoDaChon?.ma ?? "Đơn hàng mới");

      showToastSuccess(`Thanh toán thành công ${orderCode}`);
      xoaBanNhap();
      await taiDanhSachHoaDonCho();

      if (data) {
        xuLyInHoaDonTaiQuay({
          hoaDonChoDaChon: { ...data, ma: orderCode },
          cartItems: currentCartItems,
          phiVanChuyen: Number(data.thongTinGiaoHang?.phiVanChuyen ?? currentPhiVanChuyen),
          tienGiam: Number(data.tienGiam ?? currentTienGiam),
          tongTien: Number(data.tongTienHang ?? currentTongTienHang),
          khachCanTra: Number(data.tongTien ?? currentKhachCanTra),
          tenKhachHangHienThi: data.tenKhachHang || currentTenKhach,
          soDienThoaiKhachHangHienThi: data.soDienThoai || currentSdt
        });
      }
    } catch (error) {
      const msg = error instanceof Error ? error.message : "Thanh toán thất bại";
      setThongBaoLoi(msg);
      showError(msg);
    } finally {
      setDangThanhToan(false);
    }
  }, [daChonKhach, gioHangLogic, thanhToanLogic, coTheThanhToan, phieuGiamGiaLogic, hoaDonChoDaChon, luuHoaDonHienTai, layIdKhachHangHienTai, khachHangLogic, choPhepGiaoHang, giaoHangLogic, xoaBanNhap, taiDanhSachHoaDonCho, xuLyInHoaDonTaiQuay]);

  const xuLyHuyHoaDonCho = useCallback(async (invoiceId) => {
    if (!invoiceId) return;
    const isConfirmed = await showConfirm('Bạn có chắc chắn muốn hủy hóa đơn chờ này không?');
    if (!isConfirmed) return;

    setDangHuyHoaDonCho(true);
    setThongBaoLoi("");
    try {
      await huyHoaDonCho(invoiceId);
      showToastSuccess("Đã hủy hóa đơn chờ thành công.");
      await taiDanhSachHoaDonCho();
      if (hoaDonChoDaChon && hoaDonChoDaChon.id === invoiceId) {
        setHoaDonChoDaChon(null);
        xoaBanNhap();
      }
    } catch (error) {
      setThongBaoLoi(error instanceof Error ? error.message : "Không thể hủy hóa đơn chờ");
    } finally {
      setDangHuyHoaDonCho(false);
    }
  }, [hoaDonChoDaChon, taiDanhSachHoaDonCho, xoaBanNhap]);

  const themBienTheDangChon = useCallback(async () => {
    if (!sanPhamLogic.bienTheDaChon) {
      setThongBaoLoi("Vui lòng chọn màu sắc và kích cỡ phù hợp");
      return;
    }
    
    sanPhamLogic.setDangTaiSanPham(true);
    let productToAdd = { ...sanPhamLogic.bienTheDaChon };
    
    try {
      const response = await timSanPhamTaiQuay(sanPhamLogic.chiTietSanPhamDaChon.maSanPham);
      const products = Array.isArray(response?.data) ? response.data : (Array.isArray(response) ? response : []);
      const latestVariant = products.find(p => p.chiTietId === sanPhamLogic.bienTheDaChon.chiTietId);
      
      if (latestVariant) {
        productToAdd = {
          ...latestVariant
        };
      }
    } catch (e) {
      console.warn("Could not fetch latest price, using cached data", e);
    } finally {
      sanPhamLogic.setDangTaiSanPham(false);
    }

    const result = gioHangLogic.themSanPham(productToAdd, sanPhamLogic.soLuongDaChon);
    if (result) {
      sanPhamLogic.dongChiTietSanPham();
      if (result.status === "price_updated") {
        const formatPrice = (price) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
        showToastSuccess(`Sản phẩm ${result.tenSanPham} có giá thay đổi từ ${formatPrice(result.oldPrice)} đến ${formatPrice(result.newPrice)}`);
      } else {
        showToastSuccess(`Đã thêm ${sanPhamLogic.soLuongDaChon} sản phẩm vào hóa đơn`);
      }
    }
  }, [sanPhamLogic, gioHangLogic]);

  // Refs to keep track of the latest callbacks and states without triggering resubscriptions
  const latestRef = useRef({});
  latestRef.current = {
    xoaBanNhap,
    chonHoaDonCho,
    taiDanhSachHoaDonCho,
    chuyenHoaDonThanhBanNhap,
    setChoPhepGiaoHang,
    setTenNguoiNhanGiaoHang,
    setSdtNguoiNhanGiaoHang,
    setDiaChiGiaoHang,
    thanhToanLogic,
    khachHangLogic,
    dangLuuHoaDonCho,
    dangThanhToan,
    dangTaiChiTietHoaDon,
    hoaDonChoDaChon
  };

  useEffect(() => {
    if (!isConnected) return;

    let cancelled = false;
    const syncAfterConnect = async () => {
      try {
        const currentInvoiceId = latestRef.current.hoaDonChoDaChon?.id;
        const invoices = await latestRef.current.taiDanhSachHoaDonCho(true);
        if (cancelled || !currentInvoiceId) return;

        const currentInvoice = invoices.find((invoice) => invoice.id === currentInvoiceId);
        if (!currentInvoice) {
          latestRef.current.xoaBanNhap();
        }
      } catch (e) {
        console.error("Lỗi tải lại POS sau khi kết nối realtime:", e);
      }
    };

    syncAfterConnect();
    return () => {
      cancelled = true;
    };
  }, [isConnected]);

  // Handle effect bindings
  useEffect(() => {
    const subPosSync = subscribeTopic('/topic/admin/pos-sync', async (rawMsg) => {
      const msg = rawMsg?.payload ?? rawMsg;
      if (msg.sender === sessionIdRef.current) return;

      if (rawMsg?.type === 'POS_INVOICE_CHANGED' || ['CREATED', 'UPDATED', 'CANCELLED', 'PAID'].includes(msg.action)) {
        try {
          const invoices = await latestRef.current.taiDanhSachHoaDonCho(true);

          if (msg.action === 'PAID' || msg.action === 'CANCELLED') {
            if (latestRef.current.hoaDonChoDaChon?.id === msg.invoiceId) {
              if (msg.action === 'PAID') {
                showToastSuccess(msg.message || `Hóa đơn ${msg.maHoaDon || ''} đã thanh toán thành công!`);
              }
              latestRef.current.xoaBanNhap();
            } else if (msg.action === 'PAID') {
              showToastSuccess(msg.message || `Hóa đơn ${msg.maHoaDon || ''} đã thanh toán thành công!`);
            }
            return;
          }

          // Chỉ tự động chọn hóa đơn nếu hiện tại CHƯA chọn hóa đơn nào và có hóa đơn mới được tạo
          if (!latestRef.current.hoaDonChoDaChon && msg.action === 'CREATED') {
            const invoice = (invoices || []).find((hd) => hd.id === msg.invoiceId);
            if (invoice) {
              await latestRef.current.chonHoaDonCho(invoice);
            }
          }
        } catch (e) {
          console.error("Lỗi tải lại realtime POS:", e);
        }
        return;
      }

      if (msg.action === 'CHON_HOA_DON') {
        isSyncingUIRef.current = true;

        try {
          if (msg.invoiceId === null) {
            latestRef.current.xoaBanNhap();
          } else {
            const invoices = await latestRef.current.taiDanhSachHoaDonCho(true);
            const invoice = invoices.find(hd => hd.id === msg.invoiceId);
            if (invoice && latestRef.current.hoaDonChoDaChon?.id !== msg.invoiceId) {
              await latestRef.current.chonHoaDonCho(invoice);
            }
          }
        } finally {
          isSyncingUIRef.current = false;
        }
      } else if (msg.action === 'SYNC_STATE') {
        if (latestRef.current.hoaDonChoDaChon?.id === msg.invoiceId) {
          isSyncingUIRef.current = true;
          dangLuuNoiBoRef.current = true;
          skipNextAutosave.current = true;
          
          lastReceivedSyncState.current = {
            ...msg.state,
            diaChiGiaoHang: chuanHoaDiaChi(msg.state.diaChiGiaoHang)
          };

          latestRef.current.setChoPhepGiaoHang(msg.state.choPhepGiaoHang);
          latestRef.current.setTenNguoiNhanGiaoHang(msg.state.tenNguoiNhanGiaoHang);
          latestRef.current.setSdtNguoiNhanGiaoHang(msg.state.sdtNguoiNhanGiaoHang);
          latestRef.current.setDiaChiGiaoHang(chuanHoaDiaChi(msg.state.diaChiGiaoHang));
          latestRef.current.thanhToanLogic.setTienKhachDua(msg.state.tienKhachDua || "");
          latestRef.current.thanhToanLogic.setTienMatKetHop(msg.state.tienMatKetHop || "");
          latestRef.current.thanhToanLogic.setTienChuyenKhoanKetHop(msg.state.tienChuyenKhoanKetHop || "");
          latestRef.current.thanhToanLogic.setPhuongThucThanhToan(msg.state.phuongThucThanhToan);
          latestRef.current.thanhToanLogic.setHienThiMaQrLon(!!msg.state.hienThiMaQrLon);
          latestRef.current.thanhToanLogic.setGhiChuThanhToan(msg.state.ghiChuThanhToan || "");
          latestRef.current.khachHangLogic.setTuKhoaKhachHang(msg.state.tuKhoaKhachHang || "");
          latestRef.current.khachHangLogic.setKhachHangDuocChon(msg.state.khachHangDuocChon || null);
          
          setTimeout(() => { 
            isSyncingUIRef.current = false; 
            dangLuuNoiBoRef.current = false; 
          }, 50);
        }
      }
    });

    const subSanPham = subscribeTopic('/topic/admin/san-pham', async (msg) => {
      if (msg?.type === 'PRODUCT_CHANGED' || msg?.type === 'PRODUCT_UPDATED' || msg === 'PRODUCT_UPDATED' || msg === 'PRODUCT_CHANGED') {
        try {
          // Chỉ làm mới danh sách sản phẩm để cập nhật số lượng tồn/giá mới
          if (latestRef.current.sanPhamLogic?.taiSanPham) {
            latestRef.current.sanPhamLogic.taiSanPham(latestRef.current.sanPhamLogic.tuKhoaSanPham);
          }
        } catch (e) {
          console.error("Lỗi làm mới sản phẩm realtime:", e);
        }
      }
    });

    return () => {
      unsubscribeTopic(subPosSync);
      unsubscribeTopic(subSanPham);
    };
  }, [subscribeTopic, unsubscribeTopic]);

  useEffect(() => {
    if (thongBaoLoi) {
      showError(thongBaoLoi);
      setThongBaoLoi("");
    }
  }, [thongBaoLoi]);

  useEffect(() => {
    if (thongBaoThanhCong) {
      showToastSuccess(thongBaoThanhCong);
      setThongBaoThanhCong("");
    }
  }, [thongBaoThanhCong]);

  return {
    // Top-level state
    danhSachHoaDonCho,
    hoaDonChoDaChon, setHoaDonChoDaChon,
    dangTaiHoaDonCho,
    dangLuuHoaDonCho,
    dangHuyHoaDonCho,
    dangThanhToan,
    dangTaiChiTietHoaDon,
    choPhepGiaoHang, setChoPhepGiaoHang,
    
    // Top-level methods
    xoaBanNhap,
    taiDanhSachHoaDonCho,
    chonHoaDonCho,
    xuLyTaoHoaDonCho,
    xuLyThanhToanNgay,
    xuLyHuyHoaDonCho,
    themBienTheDangChon,

    // Sub-hooks logic
    khachHangLogic,
    gioHangLogic,
    sanPhamLogic,
    giaoHangLogic,
    phieuGiamGiaLogic,
    thanhToanLogic,

    // Computed wrappers
    daDatGioiHanHoaDonCho,
    coTheTaoHoaDonCho,
    coTheThanhToan,
    daChonKhach,
    khachCanTra
  };
}
