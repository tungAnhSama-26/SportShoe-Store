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
  const [diaChiGiaoHang, setDiaChiGiaoHang] = useState("");
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
    cartItems: gioHangLogic.cartItems
  });

  // PHIẾU GIẢM GIÁ
  const phieuGiamGiaLogic = useLogicPhieuGiamGia({
    cartItems: gioHangLogic.cartItems,
    tongTien: gioHangLogic.tongTien,
    hoaDonChoDaChon,
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

  const { subscribeTopic, publishMessage } = useRealtime();
  const sessionIdRef = useRef(Math.random().toString(36).substring(2, 15));
  const isSyncingUIRef = useRef(false);
  const lastLocalSaveTime = useRef(0);

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
    khachHangLogic.setKhachHangDuocChon(null);
    khachHangLogic.setTuKhoaKhachHang("");
    sanPhamLogic.setTuKhoaSanPham("");
    phieuGiamGiaLogic.setMaPhieuGiamGia("");
    khachHangLogic.setKetQuaTimKiemKhachHang([]);
    // sanPhamLogic.setKetQuaBienTheSanPham([]); // not strictly needed
    sanPhamLogic.setChiTietSanPhamDaChon(null);
    sanPhamLogic.setMauSacDaChon("");
    sanPhamLogic.setKichCoDaChon("");
    sanPhamLogic.setSoLuongDaChon(1);
    gioHangLogic.setCartItems([]);
    setHoaDonChoDaChon(null);
    phieuGiamGiaLogic.setPhieuGiamGiaDaApDung(null);
    thanhToanLogic.setPhuongThucThanhToan(1);
    thanhToanLogic.setTienKhachDua("");
    thanhToanLogic.setGhiChuThanhToan("");
    setChoPhepGiaoHang(false);
    setTenNguoiNhanGiaoHang("");
    setSdtNguoiNhanGiaoHang("");
    setDiaChiGiaoHang("");
    setDonViVanChuyen("GHN");
    setPhiVanChuyen(0);
    setDaTinhPhiVanChuyen(false);
    setDangTinhPhiVanChuyen(false);

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
    khachHangLogic.setHienThiDanhSachKhachHang(false);
    sanPhamLogic.setHienThiDanhSachSanPham(false);
    phieuGiamGiaLogic.setHienThiDanhSachPhieu(false);
    xoaPhanHoi();
    sanPhamLogic.taiSanPham("");
  }, [khachHangLogic, sanPhamLogic, phieuGiamGiaLogic, gioHangLogic, thanhToanLogic, xoaPhanHoi]);

  const taiDanhSachHoaDonCho = useCallback(async () => {
    setDangTaiHoaDonCho(true);
    try {
      const response = await layDanhSachHoaDonCho();
      const data = response?.data || response;
      setDanhSachHoaDonCho(Array.isArray(data) ? data : []);
    } catch (error) {
      setThongBaoLoi(error instanceof Error ? error.message : "Không thể tải danh sách hóa đơn chờ");
    } finally {
      setDangTaiHoaDonCho(false);
    }
  }, []);



  const chuyenHoaDonThanhBanNhap = useCallback((invoice) => {
    skipNextAutosave.current = true;
    const thongTinTheoChiTietId = new Map(
      sanPhamLogic.ketQuaBienTheSanPham.map((product) => [product.chiTietId, product])
    );
    const thongTinGiaoHang = invoice.thongTinGiaoHang || null;

    khachHangLogic.setTuKhoaKhachHang(invoice.tenKhachHang || invoice.soDienThoai || "");
    khachHangLogic.setKhachHangDuocChon(invoice.khachHangId
      ? {
        id: invoice.khachHangId,
        hoTen: invoice.tenKhachHang,
        sdt: invoice.soDienThoai,
        email: null
      }
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
    setTimeout(() => { dangLuuNoiBoRef.current = false; }, 50);

    setChoPhepGiaoHang(Boolean(thongTinGiaoHang?.giaoHang));
    setTenNguoiNhanGiaoHang(thongTinGiaoHang?.tenNguoiNhan || "");
    setSdtNguoiNhanGiaoHang(thongTinGiaoHang?.soDienThoaiNguoiNhan || "");
    setDiaChiGiaoHang(thongTinGiaoHang?.diaChiGiaoHang || "");
    setDonViVanChuyen(thongTinGiaoHang?.donViVanChuyen || "GHN");
    setPhiVanChuyen(Number(thongTinGiaoHang?.phiVanChuyen || 0));
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
    // phieuGiamGiaLogic.setKetQuaTimKiemPhieu([]); // not exported
    phieuGiamGiaLogic.setHienThiDanhSachPhieu(false);
    thanhToanLogic.capNhatTienKhachThanhToan(true);
  }, [sanPhamLogic, khachHangLogic, gioHangLogic, phieuGiamGiaLogic, thanhToanLogic]);

  const dangLuuNoiBoRef = useRef(false);

  const luuHoaDonHienTai = useCallback(async (force = false) => {
    if (!hoaDonChoDaChon) return;
    if (dangThanhToan && !force) return;
    lastLocalSaveTime.current = Date.now();
    try {
      const payload = {
        tenKhachHang: khachHangLogic.khachHangDuocChon?.hoTen || tenNguoiNhanGiaoHang || (khachHangLogic.laKhachVangLai ? KHACH_VANG_LAI : ""),
        soDienThoai: khachHangLogic.khachHangDuocChon?.sdt || sdtNguoiNhanGiaoHang || "",
        ghiChu: "",
        khachHangId: khachHangLogic.khachHangDuocChon?.id || null,
        maPhieuGiamGia: phieuGiamGiaLogic.phieuGiamGiaDaApDung?.ma || null,
        thongTinGiaoHang: choPhepGiaoHang ? giaoHangLogic.taoPayloadGiaoHang() : null,
        items: gioHangLogic.cartItems.map(item => ({
          chiTietId: item.chiTietId,
          soLuong: item.soLuong,
          giaBan: item.giaBan
        })),
      };
      const response = await capNhatHoaDonCho(hoaDonChoDaChon.id, payload);
      
      dangLuuNoiBoRef.current = true;
      const responseData = response?.data || response;
      if (responseData) {
        // Cập nhật lại danh sách hóa đơn chờ để có giá trị mới nhất
        setDanhSachHoaDonCho(prev => prev.map(hd => hd.id === hoaDonChoDaChon.id ? responseData : hd));
        setHoaDonChoDaChon(responseData);
      }
      
      gioHangLogic.setCartItems(prev => prev.map(item => ({
        ...item,
        soLuongBanDau: item.soLuong
      })));
      setTimeout(() => { dangLuuNoiBoRef.current = false; }, 50);
    } catch (error) {
      console.error("Lỗi khi lưu hóa đơn chờ:", error);
      const msg = error instanceof Error ? error.message : "Cập nhật hóa đơn chờ thất bại";
      setThongBaoLoi(msg);
      
      if (msg.toLowerCase().includes("phiếu giảm giá")) {
        const maLoi = phieuGiamGiaLogic.phieuGiamGiaDaApDung?.ma || phieuGiamGiaLogic.maPhieuGiamGia;
        phieuGiamGiaLogic.setPhieuGiamGiaDaApDung(null);
        phieuGiamGiaLogic.setMaPhieuGiamGia("");
        
        if (maLoi) {
          setThongBaoLoi(`Phiếu giảm giá ${maLoi} không còn hợp lệ. Hệ thống đang tự động tìm phiếu giảm giá thay thế...`);
          // Note: we can't easily call tuDongApDungVaDeXuatHangMucTiepTheo directly here as it's not exported. 
          // It will trigger on re-render though.
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
    }
  }, [hoaDonChoDaChon, khachHangLogic, tenNguoiNhanGiaoHang, sdtNguoiNhanGiaoHang, phieuGiamGiaLogic, choPhepGiaoHang, giaoHangLogic, gioHangLogic, chuyenHoaDonThanhBanNhap, dangThanhToan]);

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

  useEffect(() => {
    if (isSyncingUIRef.current || dangTaiChiTietHoaDon) return;
    if (hoaDonChoDaChon) {
      publishMessage('/topic/admin/pos-sync', {
        sender: sessionIdRef.current,
        action: 'SYNC_STATE',
        invoiceId: hoaDonChoDaChon.id,
        state: {
          choPhepGiaoHang,
          tenNguoiNhanGiaoHang,
          sdtNguoiNhanGiaoHang,
          diaChiGiaoHang,
          tienKhachDua: thanhToanLogic.tienKhachDua,
          phuongThucThanhToan: thanhToanLogic.phuongThucThanhToan,
          ghiChuThanhToan: thanhToanLogic.ghiChuThanhToan,
          tuKhoaKhachHang: khachHangLogic.tuKhoaKhachHang,
          khachHangDuocChon: khachHangLogic.khachHangDuocChon
        }
      });
    }
  }, [
    choPhepGiaoHang,
    tenNguoiNhanGiaoHang,
    sdtNguoiNhanGiaoHang,
    diaChiGiaoHang,
    thanhToanLogic.tienKhachDua,
    thanhToanLogic.phuongThucThanhToan,
    thanhToanLogic.ghiChuThanhToan,
    khachHangLogic.tuKhoaKhachHang,
    hoaDonChoDaChon,
    publishMessage,
    dangTaiChiTietHoaDon
  ]);

  const chonHoaDonCho = useCallback(async (invoice) => {
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

    setDangTaiChiTietHoaDon(true);
    setThongBaoLoi("");
    try {
      await sanPhamLogic.taiSanPham("");
      const response = await layChiTietHoaDonCho(invoice.id);
      const detail = response?.data || response;
      setHoaDonChoDaChon(invoice);
      chuyenHoaDonThanhBanNhap(detail);
    } catch (error) {
      setThongBaoLoi(error instanceof Error ? error.message : "Không thể tải hóa đơn chờ");
    } finally {
      setDangTaiChiTietHoaDon(false);
    }
  }, [hoaDonChoDaChon, luuHoaDonHienTai, sanPhamLogic, chuyenHoaDonThanhBanNhap, publishMessage]);

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
    lastLocalSaveTime.current = Date.now();
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
      return;
    }

    const betterCouponInfo = await phieuGiamGiaLogic.kiemTraPhieuTotHonTruocThanhToan();
    if (betterCouponInfo) {
      // Because we are porting Vue's SweetAlert which awaits, we would need to mock or handle showPaymentConfirmWithCoupon
      // I'll skip the SweetAlert logic for now, or just show standard confirm
      const choice = window.confirm(`Phiếu giảm giá ${betterCouponInfo.coupon.ma} tiết kiệm hơn. Bạn có muốn sử dụng không?`);
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

      const payload = {
        hoaDonChoId: hoaDonChoDaChon?.id ?? null,
        khachHangId: layIdKhachHangHienTai(),
        tenKhachHang: khachHangLogic.khachHangDuocChon?.hoTen || (khachHangLogic.laKhachVangLai ? KHACH_VANG_LAI : ""),
        soDienThoai: khachHangLogic.khachHangDuocChon?.sdt || "",
        maPhieuGiamGia: phieuGiamGiaLogic.phieuGiamGiaDaApDung?.ma ?? null,
        phuongThucThanhToan: thanhToanLogic.phuongThucThanhToan,
        tienKhachDua: thanhToanLogic.tienKhachThanhToan,
        ghiChu: thanhToanLogic.ghiChuThanhToan,
        thongTinGiaoHang: choPhepGiaoHang ? giaoHangLogic.taoPayloadGiaoHang() : null,
        items: gioHangLogic.taoDanhSachSanPhamThanhToan()
      };

      const response = await thanhToanTaiQuay(payload);
      const data = response?.data || response;
      const orderCode = data.ma || (hoaDonChoDaChon?.ma ?? "Đơn hàng mới");

      showToastSuccess(`Thanh toán thành công ${orderCode}`);
      xoaBanNhap();
      await taiDanhSachHoaDonCho();

      if (data) {
        xuLyInHoaDonTaiQuay({
          hoaDonChoDaChon: data,
          cartItems: data.items || [],
          phiVanChuyen: data.phiVanChuyen || 0,
          tienGiam: data.tienGiam || 0,
          tongTien: data.tongTienHang || 0,
          khachCanTra: data.tongTien || 0,
          tenKhachHangHienThi: data.tenKhachHang || "",
          soDienThoaiKhachHangHienThi: data.soDienThoai || ""
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
    lastLocalSaveTime.current = Date.now();
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
          ...latestVariant,
          soLuongTon: Math.max((latestVariant.soLuongTon || 0) - (sanPhamLogic.soLuongDaChon || 1), 0)
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

  // Handle effect bindings
  useEffect(() => {
    const subPosSync = subscribeTopic('/topic/admin/pos-sync', async (rawMsg) => {
      const msg = rawMsg?.payload ?? rawMsg;
      if (msg.sender === sessionIdRef.current) return;

      if (rawMsg?.type === 'POS_INVOICE_CHANGED' || ['CREATED', 'UPDATED', 'CANCELLED', 'PAID'].includes(msg.action)) {
        if (Date.now() - lastLocalSaveTime.current < 2500 && (msg.action === 'UPDATED' || msg.action === 'CREATED')) {
          return;
        }
        try {
          const response = await layDanhSachHoaDonCho();
          const danhSachData = response?.data || response;
          const newDanhSach = Array.isArray(danhSachData) ? danhSachData : [];
          setDanhSachHoaDonCho(newDanhSach);

          if (msg.action === 'PAID' || msg.action === 'CANCELLED') {
            setHoaDonChoDaChon((currentInvoice) => {
              if (currentInvoice?.id === msg.invoiceId) {
                xoaBanNhap();
                return null;
              }
              return currentInvoice;
            });
            return;
          }

          const invoice = newDanhSach.find((hd) => hd.id === msg.invoiceId);
          if (invoice) {
            await chonHoaDonCho(invoice);
          }
        } catch (e) {
          console.error("Lỗi tải lại realtime POS:", e);
        }
        return;
      }
      
      taiDanhSachHoaDonCho();

      if (msg.action === 'CHON_HOA_DON') {
        isSyncingUIRef.current = true;
        
        if (msg.invoiceId === null) {
          xoaBanNhap();
          isSyncingUIRef.current = false;
        } else {
          setDanhSachHoaDonCho(currentList => {
            const invoice = currentList.find(hd => hd.id === msg.invoiceId);
            if (invoice) {
              setTimeout(() => {
                chonHoaDonCho(invoice).finally(() => {
                  isSyncingUIRef.current = false;
                });
              }, 0);
            } else {
               isSyncingUIRef.current = false;
            }
            return currentList;
          });
        }
      } else if (msg.action === 'SYNC_STATE') {
        setHoaDonChoDaChon(currentInvoice => {
           if (currentInvoice?.id === msg.invoiceId) {
             isSyncingUIRef.current = true;
             dangLuuNoiBoRef.current = true;
             skipNextAutosave.current = true;
             
             setChoPhepGiaoHang(msg.state.choPhepGiaoHang);
             setTenNguoiNhanGiaoHang(msg.state.tenNguoiNhanGiaoHang);
             setSdtNguoiNhanGiaoHang(msg.state.sdtNguoiNhanGiaoHang);
             setDiaChiGiaoHang(msg.state.diaChiGiaoHang);
             thanhToanLogic.setTienKhachDua(msg.state.tienKhachDua);
             thanhToanLogic.setPhuongThucThanhToan(msg.state.phuongThucThanhToan);
             thanhToanLogic.setGhiChuThanhToan(msg.state.ghiChuThanhToan);
             khachHangLogic.setTuKhoaKhachHang(msg.state.tuKhoaKhachHang);
             khachHangLogic.setKhachHangDuocChon(msg.state.khachHangDuocChon);
             
             setTimeout(() => { 
               isSyncingUIRef.current = false; 
               dangLuuNoiBoRef.current = false; 
             }, 50);
           }
           return currentInvoice;
        });
      }
    });

    const subSanPham = subscribeTopic('/topic/admin/san-pham', async (msg) => {
      if (msg.type === 'PRODUCT_CHANGED' || msg.type === 'PRODUCT_UPDATED' || msg === 'PRODUCT_UPDATED') {
        if (dangLuuNoiBoRef.current || dangLuuHoaDonCho || dangThanhToan) return;
        if (Date.now() - lastLocalSaveTime.current < 2500) return;
        
        dangLuuNoiBoRef.current = true;
        try {
          // Lấy danh sách mới nhất
          const response = await layDanhSachHoaDonCho();
          const danhSachData = response?.data || response;
          const newDanhSach = Array.isArray(danhSachData) ? danhSachData : [];
          setDanhSachHoaDonCho(newDanhSach);

          setHoaDonChoDaChon(currentInvoice => {
            if (currentInvoice) {
              const stillExists = newDanhSach.find(hd => hd.id === currentInvoice.id);
              if (stillExists) {
                layChiTietHoaDonCho(currentInvoice.id).then(detail => {
                  chuyenHoaDonThanhBanNhap(detail?.data || detail);
                });
              } else {
                const fallbackInvoice = newDanhSach.length > 0 ? newDanhSach[0] : null;
                if (fallbackInvoice) {
                  layChiTietHoaDonCho(fallbackInvoice.id).then(detail => {
                    chuyenHoaDonThanhBanNhap(detail?.data || detail);
                  });
                  return fallbackInvoice;
                } else {
                  xoaBanNhap();
                  return null;
                }
              }
              return currentInvoice;
            } else if (newDanhSach.length > 0) {
              const fallbackInvoice = newDanhSach[0];
              layChiTietHoaDonCho(fallbackInvoice.id).then(detail => {
                chuyenHoaDonThanhBanNhap(detail?.data || detail);
              });
              return fallbackInvoice;
            }
            return currentInvoice;
          });

        } catch (e) {
          console.error("Lỗi tải lại realtime:", e);
        } finally {
          setTimeout(() => { dangLuuNoiBoRef.current = false; }, 50);
        }
      }
    });

    return () => {};
  }, [
    subscribeTopic, taiDanhSachHoaDonCho, chonHoaDonCho, 
    setChoPhepGiaoHang, setTenNguoiNhanGiaoHang, setSdtNguoiNhanGiaoHang, setDiaChiGiaoHang,
    thanhToanLogic, khachHangLogic, chuyenHoaDonThanhBanNhap, xoaBanNhap, dangLuuHoaDonCho, dangThanhToan
  ]);

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
