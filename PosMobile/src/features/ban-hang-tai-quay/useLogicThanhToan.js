import { useState, useMemo, useCallback, useEffect } from 'react';
import { showError } from '../../utils/alert';
import { dinhDangSo, dinhDangTienNhap, layChuSoTien } from './TienTe';
import { validateThanhToan } from './ValidateThanhToan';
import { PHUONG_THUC_THANH_TOAN } from './Enum';

export function useLogicThanhToan({ cartItems, khachCanTra, hoaDonChoDaChon }) {
  const [phuongThucThanhToan, setPhuongThucThanhToan] = useState(PHUONG_THUC_THANH_TOAN.TIEN_MAT);
  const [tienKhachDua, setTienKhachDua] = useState("");
  const [daSuaTienKhachDua, setDaSuaTienKhachDua] = useState(false);
  const [tienMatKetHop, setTienMatKetHop] = useState("");
  const [tienChuyenKhoanKetHop, setTienChuyenKhoanKetHop] = useState("");
  const [ghiChuThanhToan, setGhiChuThanhToan] = useState("");
  const [hienThiMaQrLon, setHienThiMaQrLon] = useState(false);

  useEffect(() => {
    setPhuongThucThanhToan(PHUONG_THUC_THANH_TOAN.TIEN_MAT);
    setTienKhachDua("");
    setDaSuaTienKhachDua(false);
    setTienMatKetHop("");
    setTienChuyenKhoanKetHop("");
    setGhiChuThanhToan("");
    setHienThiMaQrLon(false);
  }, [hoaDonChoDaChon?.id]);

  const tienMatThanhToan = useMemo(() => Number(layChuSoTien(tienMatKetHop)) || 0, [tienMatKetHop]);
  const tienChuyenKhoanThanhToan = useMemo(() => Number(layChuSoTien(tienChuyenKhoanKetHop)) || 0, [tienChuyenKhoanKetHop]);

  const tienKhachThanhToan = useMemo(() => {
    if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.KET_HOP) {
      return tienMatThanhToan + tienChuyenKhoanThanhToan;
    }
    const parsed = Number(layChuSoTien(tienKhachDua));
    return Number.isFinite(parsed) ? parsed : 0;
  }, [phuongThucThanhToan, tienKhachDua, tienMatThanhToan, tienChuyenKhoanThanhToan]);

  const tienThua = useMemo(() => {
    if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT) {
      return Math.max(tienKhachThanhToan - khachCanTra, 0);
    }
    if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.KET_HOP) {
      const tongDua = tienMatThanhToan + tienChuyenKhoanThanhToan;
      return Math.max(tongDua - khachCanTra, 0);
    }
    return 0;
  }, [phuongThucThanhToan, tienKhachThanhToan, tienMatThanhToan, tienChuyenKhoanThanhToan, khachCanTra]);

  const thongBaoLoiThanhToan = useMemo(() => {
    if (!cartItems.length || khachCanTra <= 0) {
      return "";
    }
    if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT) {
      if (!tienKhachDua.trim()) {
        return "";
      }
      if (tienKhachThanhToan <= 0) {
        return "Số tiền khách đưa phải lớn hơn 0.";
      }
      if (tienKhachThanhToan < khachCanTra) {
        return "Số tiền khách đưa phải lớn hơn hoặc bằng khách cần trả.";
      }
    }
    if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.KET_HOP) {
      const tongDua = tienMatThanhToan + tienChuyenKhoanThanhToan;
      if (tongDua < khachCanTra) {
        return "Tổng tiền mặt + chuyển khoản phải lớn hơn hoặc bằng tổng tiền khách cần trả.";
      }
    }
    return "";
  }, [phuongThucThanhToan, cartItems.length, khachCanTra, tienKhachDua, tienKhachThanhToan, tienMatThanhToan, tienChuyenKhoanThanhToan]);

  const capNhatTienKhachThanhToan = useCallback((isPaymentMethodChange = false, force = false) => {
    if (!cartItems.length) {
      setTienKhachDua("");
      setTienMatKetHop("");
      setTienChuyenKhoanKetHop("");
      return;
    }
    if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN) {
      setTienKhachDua(dinhDangSo(khachCanTra));
      return;
    }
    if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.KET_HOP) {
      if (force || isPaymentMethodChange) {
        setTienMatKetHop("");
        setTienChuyenKhoanKetHop(dinhDangSo(khachCanTra));
      }
      return;
    }
    
    if (force || isPaymentMethodChange) {
      setDaSuaTienKhachDua(false);
      setTienKhachDua(dinhDangSo(khachCanTra));
    } else if (!daSuaTienKhachDua) {
      setTienKhachDua(dinhDangSo(khachCanTra));
    }
  }, [cartItems.length, phuongThucThanhToan, khachCanTra, daSuaTienKhachDua]);

  const kiemTraLoiThanhToan = useCallback(() => {
    return validateThanhToan(phuongThucThanhToan, tienKhachDua, thongBaoLoiThanhToan);
  }, [phuongThucThanhToan, tienKhachDua, thongBaoLoiThanhToan]);

  const dinhDangTienKhachDua = useCallback((value) => {
    if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN) {
      setTienKhachDua(dinhDangSo(khachCanTra));
      return;
    }
    setTienKhachDua(dinhDangTienNhap(value));
  }, [phuongThucThanhToan, khachCanTra]);

  const xuLyTienKhachDuaInput = useCallback((value) => {
    setDaSuaTienKhachDua(true);
    setTienKhachDua(value);
    dinhDangTienKhachDua(value);
  }, [dinhDangTienKhachDua]);

  const xuLyTienMatKetHopInput = useCallback((value) => {
    const formatted = dinhDangTienNhap(value);
    setTienMatKetHop(formatted);
    const parsedMat = Number(layChuSoTien(formatted)) || 0;
    const conLai = Math.max(khachCanTra - parsedMat, 0);
    setTienChuyenKhoanKetHop(dinhDangSo(conLai));
  }, [khachCanTra]);

  const xuLyTienChuyenKhoanKetHopInput = useCallback((value) => {
    setTienChuyenKhoanKetHop(dinhDangTienNhap(value));
  }, []);

  useEffect(() => {
    capNhatTienKhachThanhToan(false, false);
  }, [khachCanTra, cartItems.length, capNhatTienKhachThanhToan]);

  const resetThanhToan = useCallback(() => {
    setPhuongThucThanhToan(PHUONG_THUC_THANH_TOAN.TIEN_MAT);
    setTienKhachDua("");
    setDaSuaTienKhachDua(false);
    setTienMatKetHop("");
    setTienChuyenKhoanKetHop("");
    setGhiChuThanhToan("");
  }, []);

  return {
    phuongThucThanhToan,
    setPhuongThucThanhToan,
    tienKhachDua,
    setTienKhachDua,
    tienMatKetHop,
    setTienMatKetHop,
    tienChuyenKhoanKetHop,
    setTienChuyenKhoanKetHop,
    tienMatThanhToan,
    tienChuyenKhoanThanhToan,
    setDaSuaTienKhachDua,
    ghiChuThanhToan,
    setGhiChuThanhToan,
    tienKhachThanhToan,
    tienThua,
    thongBaoLoiThanhToan,
    hienThiMaQrLon,
    setHienThiMaQrLon,
    capNhatTienKhachThanhToan: (force = false) => capNhatTienKhachThanhToan(false, force),
    kiemTraLoiThanhToan,
    xuLyTienKhachDuaInput,
    xuLyTienMatKetHopInput,
    xuLyTienChuyenKhoanKetHopInput,
    resetThanhToan
  };
}
