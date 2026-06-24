import { useState, useMemo, useCallback, useEffect } from 'react';
import { showError } from '../../utils/alert';
import { dinhDangSo, dinhDangTienNhap, layChuSoTien } from './TienTe';
import { validateThanhToan } from './ValidateThanhToan';
import { PHUONG_THUC_THANH_TOAN } from './Enum';

export function useLogicThanhToan({ cartItems, khachCanTra, hoaDonChoDaChon }) {
  const [phuongThucThanhToan, setPhuongThucThanhToan] = useState(PHUONG_THUC_THANH_TOAN.TIEN_MAT);
  const [tienKhachDua, setTienKhachDua] = useState("");
  const [ghiChuThanhToan, setGhiChuThanhToan] = useState("");

  useEffect(() => {
    setPhuongThucThanhToan(PHUONG_THUC_THANH_TOAN.TIEN_MAT);
    setTienKhachDua("");
    setGhiChuThanhToan("");
  }, [hoaDonChoDaChon]);

  const tienKhachThanhToan = useMemo(() => {
    const parsed = Number(layChuSoTien(tienKhachDua));
    return Number.isFinite(parsed) ? parsed : 0;
  }, [tienKhachDua]);

  const tienThua = useMemo(() => {
    if (phuongThucThanhToan !== PHUONG_THUC_THANH_TOAN.TIEN_MAT) {
      return 0;
    }
    return Math.max(tienKhachThanhToan - khachCanTra, 0);
  }, [phuongThucThanhToan, tienKhachThanhToan, khachCanTra]);

  const thongBaoLoiThanhToan = useMemo(() => {
    if (phuongThucThanhToan !== PHUONG_THUC_THANH_TOAN.TIEN_MAT || !cartItems.length || khachCanTra <= 0) {
      return "";
    }
    if (!tienKhachDua.trim()) {
      return "";
    }
    if (tienKhachThanhToan <= 0) {
      return "Số tiền khách đưa phải lớn hơn 0.";
    }
    if (tienKhachThanhToan < khachCanTra) {
      return "Số tiền khách đưa phải lớn hơn hoặc bằng khách cần trả.";
    }
    return "";
  }, [phuongThucThanhToan, cartItems.length, khachCanTra, tienKhachDua, tienKhachThanhToan]);

  const capNhatTienKhachThanhToan = useCallback((isPaymentMethodChange = false, force = false) => {
    if (!cartItems.length) {
      setTienKhachDua("");
      return;
    }
    if (phuongThucThanhToan !== PHUONG_THUC_THANH_TOAN.TIEN_MAT) {
      setTienKhachDua(dinhDangSo(khachCanTra));
      return;
    }
    
    if (force || isPaymentMethodChange) {
      setTienKhachDua("");
    }
  }, [cartItems.length, phuongThucThanhToan, khachCanTra]);

  const kiemTraLoiThanhToan = useCallback(() => {
    return validateThanhToan(phuongThucThanhToan, tienKhachDua, thongBaoLoiThanhToan);
  }, [phuongThucThanhToan, tienKhachDua, thongBaoLoiThanhToan]);

  const dinhDangTienKhachDua = useCallback((value) => {
    if (phuongThucThanhToan !== PHUONG_THUC_THANH_TOAN.TIEN_MAT) {
      setTienKhachDua(dinhDangSo(khachCanTra));
      return;
    }
    setTienKhachDua(dinhDangTienNhap(value));
  }, [phuongThucThanhToan, khachCanTra]);

  const xuLyTienKhachDuaInput = useCallback((value) => {
    setTienKhachDua(value);
    dinhDangTienKhachDua(value);
  }, [dinhDangTienKhachDua]);

  useEffect(() => {
    capNhatTienKhachThanhToan(false, false);
  }, [khachCanTra, capNhatTienKhachThanhToan]);

  useEffect(() => {
    capNhatTienKhachThanhToan(true, false);
  }, [phuongThucThanhToan, capNhatTienKhachThanhToan]);

  return {
    phuongThucThanhToan,
    setPhuongThucThanhToan,
    tienKhachDua,
    setTienKhachDua,
    ghiChuThanhToan,
    setGhiChuThanhToan,
    tienKhachThanhToan,
    tienThua,
    thongBaoLoiThanhToan,
    capNhatTienKhachThanhToan: (force = false) => capNhatTienKhachThanhToan(false, force),
    kiemTraLoiThanhToan,
    xuLyTienKhachDuaInput
  };
}
