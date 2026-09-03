import { useState, useMemo, useEffect, useCallback, useRef } from 'react';
import { apDungPhieuGiamGiaTaiQuay, timPhieuGiamGiaTaiQuay } from '../../api/dichVuBanHang';
import { showError, showWarning, showToastSuccess as showSuccess } from '../../utils/alert';

export function useLogicPhieuGiamGia({
  cartItems,
  tongTien,
  hoaDonChoDaChon,
  dangTaiChiTietHoaDon,
  khachHangDuocChon,
  layIdKhachHangHienTai,
  taoDanhSachSanPhamThanhToan,
  capNhatTienKhachThanhToan,
  xoaPhanHoi
}) {
  const [maPhieuGiamGia, setMaPhieuGiamGia] = useState("");
  const [phieuGiamGiaDaApDung, setPhieuGiamGiaDaApDung] = useState(null);
  const [dangApDungPhieu, setDangApDungPhieu] = useState(false);

  const [ketQuaTimKiemPhieu, setKetQuaTimKiemPhieu] = useState([]);
  const [dangTaiPhieu, setDangTaiPhieu] = useState(false);
  const [hienThiDanhSachPhieu, setHienThiDanhSachPhieu] = useState(false);

  const [tatCaPhieuKhaDung, setTatCaPhieuKhaDung] = useState([]);
  const [phieuGiamGiaHangMucTiepTheo, setPhieuGiamGiaHangMucTiepTheo] = useState(null);
  const [soTienThieuDeDatHangMuc, setSoTienThieuDeDatHangMuc] = useState(0);
  const [soSanPhamThieuDeDatHangMuc, setSoSanPhamThieuDeDatHangMuc] = useState(0);
  const [soTienGiamCuaHangMucTiepTheo, setSoTienGiamCuaHangMucTiepTheo] = useState(0);

  const [phieuTotHonDeXuat, setPhieuTotHonDeXuat] = useState(null);
  const [danhSachPhieuTotHonDaTuChoi, setDanhSachPhieuTotHonDaTuChoi] = useState(new Set());

  const boDemThoiGianPhieu = useRef(null);
  const boDemThoiGianDanhSachPhieu = useRef(null);

  const tinhToanGiamGia = useCallback((coupon, amount) => {
    if (!coupon) return 0;
    const amountNum = Number(amount) || 0;
    const giaTriNum = Number(coupon.giaTri) || 0;
    if (coupon.loai === 1) { 
      let calculated = (amountNum * giaTriNum) / 100;
      const giamToiDaNum = Number(coupon.giamToiDa) || 0;
      if (coupon.giamToiDa && giamToiDaNum > 0 && calculated > giamToiDaNum) {
        calculated = giamToiDaNum;
      }
      return Math.min(calculated, amountNum);
    }
    if (coupon.loai === 2 || giaTriNum > 0) {
      return Math.min(giaTriNum, amountNum);
    }
    if (coupon.soTienGiam != null && Number(coupon.soTienGiam) > 0) {
      return Math.min(Number(coupon.soTienGiam), amountNum);
    }
    return 0;
  }, []);

  const ketQuaTimKiemPhieuDaSapXep = useMemo(() => {
    return [...ketQuaTimKiemPhieu].sort((a, b) => {
      const validA = tongTien >= (a.giaTriToiThieu || 0);
      const validB = tongTien >= (b.giaTriToiThieu || 0);
      if (validA !== validB) return validA ? -1 : 1;
      
      const getDiscount = (c) => tinhToanGiamGia(c, tongTien);
      return getDiscount(b) - getDiscount(a);
    });
  }, [ketQuaTimKiemPhieu, tongTien, tinhToanGiamGia]);

  const tienGiam = useMemo(() => {
    if (phieuGiamGiaDaApDung) {
      const calculated = tinhToanGiamGia(phieuGiamGiaDaApDung, tongTien);
      if (calculated > 0) return calculated;
      if (phieuGiamGiaDaApDung.soTienGiam != null && Number(phieuGiamGiaDaApDung.soTienGiam) > 0) {
        return Math.min(Number(phieuGiamGiaDaApDung.soTienGiam), tongTien);
      }
      return 0;
    }
    if (maPhieuGiamGia && hoaDonChoDaChon?.tienGiam != null) {
      return hoaDonChoDaChon.tienGiam;
    }
    return 0;
  }, [phieuGiamGiaDaApDung, tongTien, tinhToanGiamGia, maPhieuGiamGia, hoaDonChoDaChon]);

  const tongTienSauGiamHienThi = useMemo(() => Math.max(tongTien - tienGiam, 0), [tongTien, tienGiam]);

  const maPhieuChuaApDung = useMemo(() => Boolean(maPhieuGiamGia.trim()) && !phieuGiamGiaDaApDung, [maPhieuGiamGia, phieuGiamGiaDaApDung]);

  const coTheTimPhieu = useMemo(() => cartItems.length > 0 && tongTien > 0, [cartItems.length, tongTien]);

  const coTheApDungPhieu = useMemo(
    () => Boolean(maPhieuGiamGia.trim()) &&
      cartItems.length > 0 &&
      !dangApDungPhieu &&
      (
        !phieuGiamGiaDaApDung ||
        phieuGiamGiaDaApDung.ma.toLowerCase() !== maPhieuGiamGia.trim().toLowerCase()
      ),
    [maPhieuGiamGia, cartItems.length, dangApDungPhieu, phieuGiamGiaDaApDung]
  );

  const danhDauCanApDungLaiPhieu = useCallback(() => {
    if (!maPhieuGiamGia.trim()) {
      setPhieuGiamGiaDaApDung(null);
      return;
    }
    setPhieuGiamGiaDaApDung(null);
  }, [maPhieuGiamGia]);

  const timKiemPhieu = useCallback(async (keyword) => {
    if (!coTheTimPhieu) {
      setKetQuaTimKiemPhieu([]);
      return;
    }
    setDangTaiPhieu(true);
    try {
      const response = await timPhieuGiamGiaTaiQuay({
        keyword,
        hoaDonId: hoaDonChoDaChon?.id ?? null,
        khachHangId: layIdKhachHangHienTai(),
        tongTienHang: tongTien
      });
      const data = response?.data || response;
      setKetQuaTimKiemPhieu(Array.isArray(data) ? data : []);
    } catch (error) {
      setKetQuaTimKiemPhieu([]);
      showError(error instanceof Error ? error.message : "Không thể tìm phiếu giảm giá");
    } finally {
      setDangTaiPhieu(false);
    }
  }, [coTheTimPhieu, hoaDonChoDaChon, layIdKhachHangHienTai, tongTien]);

  const xuLyKhiFocusPhieu = useCallback(() => {
    if (boDemThoiGianDanhSachPhieu.current) {
      window.clearTimeout(boDemThoiGianDanhSachPhieu.current);
    }
    setHienThiDanhSachPhieu(true);
    timKiemPhieu(maPhieuGiamGia);
  }, [timKiemPhieu, maPhieuGiamGia]);

  const xuLyKhiBlurPhieu = useCallback(() => {
    if (boDemThoiGianDanhSachPhieu.current) {
      window.clearTimeout(boDemThoiGianDanhSachPhieu.current);
    }
    boDemThoiGianDanhSachPhieu.current = window.setTimeout(() => {
      setHienThiDanhSachPhieu(false);
    }, 150);
  }, []);

  const xuLyGoPhieu = useCallback(() => {
    setMaPhieuGiamGia("");
    setPhieuGiamGiaDaApDung(null);
    setKetQuaTimKiemPhieu([]);
    if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
    if (xoaPhanHoi) xoaPhanHoi();
  }, [capNhatTienKhachThanhToan, xoaPhanHoi]);

  const xuLyApDungPhieu = useCallback(async (isAutoRefetch = false, manualMaPhieu = null) => {
    const maPhieuDeApDung = (manualMaPhieu || maPhieuGiamGia).trim();
    if (!maPhieuDeApDung || cartItems.length === 0 || dangApDungPhieu || (phieuGiamGiaDaApDung && phieuGiamGiaDaApDung.ma.toLowerCase() === maPhieuDeApDung.toLowerCase())) {
      if (maPhieuDeApDung && cartItems.length === 0) {
        showError("Vui lòng thêm sản phẩm vào hóa đơn trước khi áp dụng mã");
      }
      return;
    }

    setDangApDungPhieu(true);
    try {
      const response = await apDungPhieuGiamGiaTaiQuay({
        hoaDonId: hoaDonChoDaChon?.id ?? null,
        khachHangId: layIdKhachHangHienTai(),
        maPhieuGiamGia: maPhieuDeApDung,
        items: taoDanhSachSanPhamThanhToan()
      });
      const coupon = response?.data || response;
      setPhieuGiamGiaDaApDung(coupon);
      if (!isAutoRefetch) {
        showSuccess(`Áp dụng mã ${coupon.ma} thành công`);
      }
      setMaPhieuGiamGia(coupon.ma);
      setKetQuaTimKiemPhieu([]);
      setHienThiDanhSachPhieu(false);
      if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
    } catch (error) {
      if (!isAutoRefetch) {
        setPhieuGiamGiaDaApDung(null);
        showError(error instanceof Error ? error.message : "Không thể áp dụng phiếu giảm giá");
      }
    } finally {
      setDangApDungPhieu(false);
    }
  }, [maPhieuGiamGia, cartItems.length, dangApDungPhieu, phieuGiamGiaDaApDung, hoaDonChoDaChon, layIdKhachHangHienTai, taoDanhSachSanPhamThanhToan, capNhatTienKhachThanhToan]);

  const chonPhieuGiamGia = useCallback((coupon) => {
    if (boDemThoiGianDanhSachPhieu.current) {
      window.clearTimeout(boDemThoiGianDanhSachPhieu.current);
    }
    setMaPhieuGiamGia(coupon.ma);
    setHienThiDanhSachPhieu(false);
    if (xoaPhanHoi) xoaPhanHoi();
    if (coupon.ma) {
      void xuLyApDungPhieu(false, coupon.ma);
    }
  }, [xoaPhanHoi, xuLyApDungPhieu]);

  const taiTatCaPhieuKhaDung = useCallback(async () => {
    try {
      const response = await timPhieuGiamGiaTaiQuay({
        keyword: "",
        hoaDonId: hoaDonChoDaChon?.id ?? null,
        khachHangId: layIdKhachHangHienTai(),
        tongTienHang: 999999999
      });
      const data = response?.data || response;
      setTatCaPhieuKhaDung(Array.isArray(data) ? data : []);
    } catch (e) {
      setTatCaPhieuKhaDung([]);
    }
  }, [hoaDonChoDaChon, layIdKhachHangHienTai]);

  const tuChoiPhieuTotHon = useCallback((code) => {
    const ma = code || phieuTotHonDeXuat?.ma;
    if (ma) {
      const newSet = new Set(danhSachPhieuTotHonDaTuChoi);
      newSet.add(ma);
      setDanhSachPhieuTotHonDaTuChoi(newSet);
      setPhieuTotHonDeXuat(null);
    }
  }, [phieuTotHonDeXuat, danhSachPhieuTotHonDaTuChoi]);

  const chapNhanPhieuTotHon = useCallback(() => {
    if (phieuTotHonDeXuat) {
      setMaPhieuGiamGia(phieuTotHonDeXuat.ma);
      setPhieuTotHonDeXuat(null);
      if (!dangApDungPhieu) {
        xuLyApDungPhieu(false, phieuTotHonDeXuat.ma);
      }
    }
  }, [phieuTotHonDeXuat, dangApDungPhieu, xuLyApDungPhieu]);

  const kiemTraPhieuTotHonTruocThanhToan = useCallback(async () => {
    await taiTatCaPhieuKhaDung();
    
    if (!tatCaPhieuKhaDung.length || !tongTien) {
      return null;
    }
    
    const eligible = tatCaPhieuKhaDung.filter(c => tongTien >= (c.giaTriToiThieu || 0));
    let currentBest = null;
    
    if (eligible.length > 0) {
      eligible.sort((a, b) => tinhToanGiamGia(b, tongTien) - tinhToanGiamGia(a, tongTien));
      currentBest = eligible[0];
    }
    
    if (currentBest) {
      const currentBestDiscount = tinhToanGiamGia(currentBest, tongTien);
      const currentDiscount = phieuGiamGiaDaApDung ? tinhToanGiamGia(phieuGiamGiaDaApDung, tongTien) : 0;
      
      if (currentBestDiscount > currentDiscount && (!phieuGiamGiaDaApDung || phieuGiamGiaDaApDung.ma !== currentBest.ma)) {
        if (!danhSachPhieuTotHonDaTuChoi.has(currentBest.ma)) {
          return {
            coupon: currentBest,
            newDiscount: currentBestDiscount,
            oldDiscount: currentDiscount
          };
        }
      }
    }
    
    return null;
  }, [taiTatCaPhieuKhaDung, tatCaPhieuKhaDung, tongTien, tinhToanGiamGia, phieuGiamGiaDaApDung, danhSachPhieuTotHonDaTuChoi]);

  const tuDongApDungVaDeXuatHangMucTiepTheo = useCallback(() => {
    if (!tatCaPhieuKhaDung.length || !tongTien) {
      setPhieuGiamGiaHangMucTiepTheo(null);
      setSoTienThieuDeDatHangMuc(0);
      return;
    }
    
    const eligible = tatCaPhieuKhaDung.filter(c => tongTien >= (c.giaTriToiThieu || 0));
    let currentBest = null;
    let currentBestDiscount = 0;
    
    if (eligible.length > 0) {
      eligible.sort((a, b) => tinhToanGiamGia(b, tongTien) - tinhToanGiamGia(a, tongTien));
      currentBest = eligible[0];
      currentBestDiscount = tinhToanGiamGia(currentBest, tongTien);
    }
    
    currentBestDiscount = Math.max(currentBestDiscount, Number(tienGiam) || 0);
    
    const higher = tatCaPhieuKhaDung.filter(c => (Number(c.giaTriToiThieu) || 0) > Number(tongTien));
    higher.sort((a, b) => (Number(a.giaTriToiThieu) || 0) - (Number(b.giaTriToiThieu) || 0));
    
    let foundNext = null;
    let foundNextDiscount = 0;
    for (const c of higher) {
      const potentialDiscount = tinhToanGiamGia(c, Number(c.giaTriToiThieu) || 0);
      if (potentialDiscount > currentBestDiscount) {
        foundNext = c;
        foundNextDiscount = potentialDiscount;
        break;
      }
    }
    if (!foundNext && higher.length > 0) {
      foundNext = higher[0];
      foundNextDiscount = tinhToanGiamGia(foundNext, Number(foundNext.giaTriToiThieu) || 0);
    }
    
    setPhieuGiamGiaHangMucTiepTheo(foundNext);
    setSoTienGiamCuaHangMucTiepTheo(foundNextDiscount);
    
    const thieuTien = foundNext ? Math.max(0, (Number(foundNext.giaTriToiThieu) || 0) - Number(tongTien)) : 0;
    setSoTienThieuDeDatHangMuc(thieuTien);
    
    if (thieuTien > 0 && cartItems && cartItems.length > 0) {
      const cheapestItemPrice = Math.min(...cartItems.map(i => i.giaDonVi || i.giaBan || 0));
      setSoSanPhamThieuDeDatHangMuc(cheapestItemPrice > 0 ? Math.ceil(thieuTien / cheapestItemPrice) : 1);
    } else {
      setSoSanPhamThieuDeDatHangMuc(0);
    }
    
    if (currentBest) {
      if (!phieuGiamGiaDaApDung) {
        if (!danhSachPhieuTotHonDaTuChoi.has(currentBest.ma)) {
          setMaPhieuGiamGia(currentBest.ma);
          setPhieuGiamGiaDaApDung({
            ...currentBest,
            soTienGiam: currentBestDiscount
          });
          if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
        }
      } else {
        const currentDiscount = tinhToanGiamGia(phieuGiamGiaDaApDung, tongTien);
        if (Number(phieuGiamGiaDaApDung.soTienGiam) !== Number(currentDiscount)) {
          setPhieuGiamGiaDaApDung({
            ...phieuGiamGiaDaApDung,
            soTienGiam: currentDiscount
          });
          if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
        }
      }
    } else {
      if (phieuGiamGiaDaApDung && !dangApDungPhieu) {
        setMaPhieuGiamGia("");
        xuLyGoPhieu();
      }
    }
  }, [tatCaPhieuKhaDung, tongTien, tinhToanGiamGia, tienGiam, cartItems, phieuGiamGiaDaApDung, danhSachPhieuTotHonDaTuChoi, dangApDungPhieu, xuLyApDungPhieu, xuLyGoPhieu]);

  const xoaCacBoDemThoiGianPhieu = useCallback(() => {
    if (boDemThoiGianPhieu.current) window.clearTimeout(boDemThoiGianPhieu.current);
    if (boDemThoiGianDanhSachPhieu.current) window.clearTimeout(boDemThoiGianDanhSachPhieu.current);
  }, []);

  // Effect to watch maPhieuGiamGia change
  useEffect(() => {
    if (boDemThoiGianPhieu.current) {
      window.clearTimeout(boDemThoiGianPhieu.current);
    }
    const trimmed = maPhieuGiamGia.trim();
    if (!trimmed) {
      setPhieuGiamGiaDaApDung(null);
      if (hienThiDanhSachPhieu) {
        boDemThoiGianPhieu.current = window.setTimeout(() => {
          timKiemPhieu("");
        }, 250);
      }
      return;
    }
    if (phieuGiamGiaDaApDung && phieuGiamGiaDaApDung.ma.toLowerCase() !== trimmed.toLowerCase()) {
      setPhieuGiamGiaDaApDung(null);
    }
    boDemThoiGianPhieu.current = window.setTimeout(() => {
      timKiemPhieu(maPhieuGiamGia);
    }, 250);
  }, [maPhieuGiamGia]); // Note: hienThiDanhSachPhieu change shouldn't trigger this, but maPhieuGiamGia should

  // Effect to handle changes in context (tongTien, cartItems, etc)
  useEffect(() => {
    if (dangTaiChiTietHoaDon) return;
    let isMounted = true;

    const checkAndReload = async () => {
      if (!coTheTimPhieu) {
        setKetQuaTimKiemPhieu([]);
        setHienThiDanhSachPhieu(false);
        if (phieuGiamGiaDaApDung && cartItems.length > 0) {
          const ma = phieuGiamGiaDaApDung.ma;
          setPhieuGiamGiaDaApDung(null);
          setMaPhieuGiamGia("");
          showWarning(`Đơn hàng không đủ điều kiện áp dụng phiếu giảm giá ${ma} nữa.`);
          if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
        } else if (phieuGiamGiaDaApDung && cartItems.length === 0) {
          setPhieuGiamGiaDaApDung(null);
          setMaPhieuGiamGia("");
        }
        await taiTatCaPhieuKhaDung();
        if (isMounted) tuDongApDungVaDeXuatHangMucTiepTheo();
        return;
      }

      await taiTatCaPhieuKhaDung();
      if (!isMounted) return;
      tuDongApDungVaDeXuatHangMucTiepTheo();

      if (phieuGiamGiaDaApDung) {
        try {
          const response = await timPhieuGiamGiaTaiQuay({
            keyword: phieuGiamGiaDaApDung.ma,
            hoaDonId: hoaDonChoDaChon?.id ?? null,
            khachHangId: layIdKhachHangHienTai(),
            tongTienHang: tongTien
          });
          const ketQua = response?.data || response;
          const moi = Array.isArray(ketQua) ? ketQua.find(c => String(c.ma).toUpperCase() === String(phieuGiamGiaDaApDung.ma).toUpperCase()) : null;
          if (!moi && isMounted) {
             const ma = phieuGiamGiaDaApDung.ma;
             setPhieuGiamGiaDaApDung(null);
             setMaPhieuGiamGia("");
             showWarning(`Phiếu giảm giá "${ma}" đã ngừng hoạt động hoặc không còn đủ điều kiện áp dụng, hệ thống đã tự động gỡ bỏ.`);
             if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
          } else if (moi && isMounted) {
             const cuGiam = Number(phieuGiamGiaDaApDung.soTienGiam || 0);
             const moiGiam = Number(moi.soTienGiam || 0);
             if (cuGiam !== moiGiam || phieuGiamGiaDaApDung.giaTri !== moi.giaTri) {
                setPhieuGiamGiaDaApDung(moi);
                setMaPhieuGiamGia(moi.ma);
                if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
                if (cuGiam !== moiGiam) {
                   showWarning(`Thông tin phiếu giảm giá "${moi.ma}" đã thay đổi. Số tiền giảm đã được cập nhật từ ${dinhDangTien(cuGiam)} thành ${dinhDangTien(moiGiam)}.`);
                }
             }
          }
        } catch (e) {
          // ignore
        }
      }

      if (!maPhieuGiamGia.trim() && !hienThiDanhSachPhieu) {
        return;
      }
      if (isMounted) timKiemPhieu(maPhieuGiamGia);
    };

    checkAndReload();

    return () => { isMounted = false; };
  }, [coTheTimPhieu, tongTien, khachHangDuocChon, hoaDonChoDaChon?.id, dangTaiChiTietHoaDon]);

  return {
    maPhieuGiamGia, setMaPhieuGiamGia,
    phieuGiamGiaDaApDung, setPhieuGiamGiaDaApDung,
    dangApDungPhieu,
    ketQuaTimKiemPhieu: ketQuaTimKiemPhieuDaSapXep,
    setKetQuaTimKiemPhieu,
    dangTaiPhieu,
    hienThiDanhSachPhieu, setHienThiDanhSachPhieu,
    tienGiam,
    tongTienSauGiamHienThi,
    maPhieuChuaApDung,
    coTheTimPhieu,
    coTheApDungPhieu,
    danhDauCanApDungLaiPhieu,
    timKiemPhieu,
    xuLyKhiFocusPhieu,
    xuLyKhiBlurPhieu,
    chonPhieuGiamGia,
    xuLyApDungPhieu,
    xuLyGoPhieu,
    xoaCacBoDemThoiGianPhieu,
    phieuGiamGiaHangMucTiepTheo,
    setPhieuGiamGiaHangMucTiepTheo,
    soTienThieuDeDatHangMuc,
    soSanPhamThieuDeDatHangMuc,
    soTienGiamCuaHangMucTiepTheo,
    phieuTotHonDeXuat,
    setPhieuTotHonDeXuat,
    tuChoiPhieuTotHon,
    chapNhanPhieuTotHon,
    kiemTraPhieuTotHonTruocThanhToan
  };
}
