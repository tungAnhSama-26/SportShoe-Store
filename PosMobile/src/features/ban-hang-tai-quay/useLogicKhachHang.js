import { useState, useMemo, useEffect, useCallback, useRef } from 'react';
import { showError } from '../../utils/alert';
import { timKhachHangTheoSoDienThoai } from '../../api/dichVuBanHang';
import { KHACH_VANG_LAI, AN_THONG_TIN, CHUA_CHON_KHACH, CHON_KHACH_HOAC_VANG_LAI } from './HangSo';
import { chuanHoaDiaChi, diaChiHopLe } from '../../utils/diaChi';

export function useLogicKhachHang({
  hoaDonChoDaChon,
  setTenNguoiNhanGiaoHang,
  setSdtNguoiNhanGiaoHang,
  diaChiGiaoHang,
  setDiaChiGiaoHang,
  danhDauCanApDungLaiPhieu,
  xoaPhanHoi
}) {
  const [tuKhoaKhachHang, setTuKhoaKhachHang] = useState("");
  const [ketQuaTimKiemKhachHang, setKetQuaTimKiemKhachHang] = useState([]);
  const [khachHangDuocChon, setKhachHangDuocChon] = useState(null);
  const [dangTaiKhachHang, setDangTaiKhachHang] = useState(false);
  const [hienThiDanhSachKhachHang, setHienThiDanhSachKhachHang] = useState(false);

  const boDemThoiGianKhachHang = useRef(null);

  const laKhachVangLai = useMemo(
    () => tuKhoaKhachHang.trim().toLowerCase() === KHACH_VANG_LAI.toLowerCase(),
    [tuKhoaKhachHang]
  );

  const tenKhachHangHienThi = useMemo(() => {
    if (khachHangDuocChon) return khachHangDuocChon.hoTen;
    if (laKhachVangLai) return KHACH_VANG_LAI;
    return hoaDonChoDaChon?.tenKhachHang || CHUA_CHON_KHACH;
  }, [khachHangDuocChon, laKhachVangLai, hoaDonChoDaChon]);

  const soDienThoaiKhachHangHienThi = useMemo(() => {
    if (khachHangDuocChon) return khachHangDuocChon.sdt;
    if (laKhachVangLai) return AN_THONG_TIN;
    return hoaDonChoDaChon?.soDienThoai || CHON_KHACH_HOAC_VANG_LAI;
  }, [khachHangDuocChon, laKhachVangLai, hoaDonChoDaChon]);

  const timKiemKhachHang = useCallback(async (keyword) => {
    if (!keyword.trim() || keyword.trim().toLowerCase() === KHACH_VANG_LAI.toLowerCase()) {
      setKetQuaTimKiemKhachHang([]);
      return;
    }
    setDangTaiKhachHang(true);
    try {
      const response = await timKhachHangTheoSoDienThoai(keyword);
      // Giả sử API trả về mảng trực tiếp hoặc obj.data / obj.content
      const data = response?.data?.content || response?.content || response?.data || response || [];
      setKetQuaTimKiemKhachHang(Array.isArray(data) ? data : []);
    } catch (error) {
      showError(error instanceof Error ? error.message : "Không thể tìm khách hàng");
    } finally {
      setDangTaiKhachHang(false);
    }
  }, []);

  const chonKhachHang = useCallback((customer) => {
    setKhachHangDuocChon(customer);
    setTuKhoaKhachHang(customer.hoTen || "");
    setTenNguoiNhanGiaoHang(prev => prev.trim() ? prev : (customer.hoTen || ""));
    setSdtNguoiNhanGiaoHang(prev => prev.trim() ? prev : (customer.sdt || ""));
    
    if (setDiaChiGiaoHang && customer.diaChiMacDinh) {
      setDiaChiGiaoHang(prev => {
        if (!diaChiHopLe(prev)) {
          return chuanHoaDiaChi(customer.diaChiMacDinh);
        }
        return prev;
      });
    }

    setKetQuaTimKiemKhachHang([]);
    setHienThiDanhSachKhachHang(false);
    if (danhDauCanApDungLaiPhieu) danhDauCanApDungLaiPhieu();
    if (xoaPhanHoi) xoaPhanHoi();
  }, [setTenNguoiNhanGiaoHang, setSdtNguoiNhanGiaoHang, setDiaChiGiaoHang, danhDauCanApDungLaiPhieu, xoaPhanHoi]);

  const boChonKhachHang = useCallback(() => {
    setKhachHangDuocChon(null);
    setTuKhoaKhachHang("");
    setKetQuaTimKiemKhachHang([]);
    setHienThiDanhSachKhachHang(false);
    if (danhDauCanApDungLaiPhieu) danhDauCanApDungLaiPhieu();
    if (xoaPhanHoi) xoaPhanHoi();
  }, [danhDauCanApDungLaiPhieu, xoaPhanHoi]);

  const chonKhachVangLai = useCallback(() => {
    setKhachHangDuocChon(null);
    setTuKhoaKhachHang(KHACH_VANG_LAI);
    if (!hoaDonChoDaChon) {
      setTenNguoiNhanGiaoHang("");
      setSdtNguoiNhanGiaoHang("");
    }
    setKetQuaTimKiemKhachHang([]);
    setHienThiDanhSachKhachHang(false);
    if (danhDauCanApDungLaiPhieu) danhDauCanApDungLaiPhieu();
    if (xoaPhanHoi) xoaPhanHoi();
  }, [hoaDonChoDaChon, setTenNguoiNhanGiaoHang, setSdtNguoiNhanGiaoHang, danhDauCanApDungLaiPhieu, xoaPhanHoi]);

  const moDanhSachKhachHang = useCallback(async () => {
    const keyword = tuKhoaKhachHang.trim();
    if (keyword && keyword.toLowerCase() !== KHACH_VANG_LAI.toLowerCase()) {
      setHienThiDanhSachKhachHang(true);
      await timKiemKhachHang(tuKhoaKhachHang);
      return;
    }
    setHienThiDanhSachKhachHang(false);
  }, [tuKhoaKhachHang, timKiemKhachHang]);

  const dongDanhSachKhachHang = useCallback(() => {
    setTimeout(() => {
      setHienThiDanhSachKhachHang(false);
    }, 150);
  }, []);

  const xoaBoDemThoiGianKhachHang = useCallback(() => {
    if (boDemThoiGianKhachHang.current) {
      clearTimeout(boDemThoiGianKhachHang.current);
    }
  }, []);

  useEffect(() => {
    xoaBoDemThoiGianKhachHang();
    const keyword = tuKhoaKhachHang.trim().toLowerCase();
    
    if (khachHangDuocChon) {
      const tenKhachDangChon = khachHangDuocChon.hoTen?.trim().toLowerCase() ?? "";
      const soDienThoaiDangChon = khachHangDuocChon.sdt?.trim().toLowerCase() ?? "";
      if (keyword !== tenKhachDangChon && keyword !== soDienThoaiDangChon) {
        setKhachHangDuocChon(null);
        if (danhDauCanApDungLaiPhieu) danhDauCanApDungLaiPhieu();
      }
    }

    setHienThiDanhSachKhachHang(tuKhoaKhachHang.trim().length > 0 && keyword !== KHACH_VANG_LAI.toLowerCase());
    
    boDemThoiGianKhachHang.current = setTimeout(() => {
      timKiemKhachHang(tuKhoaKhachHang);
    }, 250);

    return () => xoaBoDemThoiGianKhachHang();
  }, [tuKhoaKhachHang]); // Ignore other deps to replicate Vue's exact watch(tuKhoaKhachHang) behavior

  return {
    tuKhoaKhachHang,
    setTuKhoaKhachHang,
    ketQuaTimKiemKhachHang,
    setKetQuaTimKiemKhachHang,
    khachHangDuocChon,
    setKhachHangDuocChon,
    dangTaiKhachHang,
    hienThiDanhSachKhachHang,
    setHienThiDanhSachKhachHang,
    laKhachVangLai,
    tenKhachHangHienThi,
    soDienThoaiKhachHangHienThi,
    timKiemKhachHang,
    chonKhachHang,
    boChonKhachHang,
    chonKhachVangLai,
    moDanhSachKhachHang,
    dongDanhSachKhachHang,
    xoaBoDemThoiGianKhachHang
  };
}
