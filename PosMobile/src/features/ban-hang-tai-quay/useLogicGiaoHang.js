import { useMemo, useCallback, useEffect, useRef } from 'react';
import { tinhPhiVanChuyenTaiQuay } from '../../api/dichVuBanHang';
import { showError } from '../../utils/alert';

export function useLogicGiaoHang({
  choPhepGiaoHang,
  setChoPhepGiaoHang,
  tenNguoiNhanGiaoHang,
  setTenNguoiNhanGiaoHang,
  sdtNguoiNhanGiaoHang,
  setSdtNguoiNhanGiaoHang,
  diaChiGiaoHang,
  setDiaChiGiaoHang,
  donViVanChuyen,
  setDonViVanChuyen,
  phiVanChuyen,
  setPhiVanChuyen,
  diaChiDaXacNhan,
  setDiaChiDaXacNhan,
  daTinhPhiVanChuyen,
  setDaTinhPhiVanChuyen,
  dangTinhPhiVanChuyen,
  setDangTinhPhiVanChuyen,
  cauHinhGiaoHang,
  setCauHinhGiaoHang,
  khachHangDuocChon,
  hoaDonChoDaChon,
  cartItems
}) {
  const tenNguoiNhanGiaoHangHienThi = useMemo(() => {
    if (tenNguoiNhanGiaoHang.trim()) {
      return tenNguoiNhanGiaoHang.trim();
    }
    if (khachHangDuocChon?.hoTen) {
      return khachHangDuocChon.hoTen;
    }
    return hoaDonChoDaChon?.thongTinGiaoHang?.tenNguoiNhan || "";
  }, [tenNguoiNhanGiaoHang, khachHangDuocChon, hoaDonChoDaChon]);

  const soDienThoaiNguoiNhanGiaoHangHienThi = useMemo(() => {
    if (sdtNguoiNhanGiaoHang.trim()) {
      return sdtNguoiNhanGiaoHang.trim();
    }
    if (khachHangDuocChon?.sdt) {
      return khachHangDuocChon.sdt;
    }
    return hoaDonChoDaChon?.thongTinGiaoHang?.soDienThoaiNguoiNhan || "";
  }, [sdtNguoiNhanGiaoHang, khachHangDuocChon, hoaDonChoDaChon]);

  const phiVanChuyenHienThi = useMemo(() => choPhepGiaoHang ? phiVanChuyen : 0, [choPhepGiaoHang, phiVanChuyen]);

  const diaChiGiaoHangHienThi = useMemo(() => {
    if (diaChiGiaoHang && typeof diaChiGiaoHang === 'string' && diaChiGiaoHang.trim()) {
      return diaChiGiaoHang.trim();
    }
    if (khachHangDuocChon?.diaChiMacDinh) {
      return khachHangDuocChon.diaChiMacDinh;
    }
    return hoaDonChoDaChon?.thongTinGiaoHang?.diaChiGiaoHang || "";
  }, [diaChiGiaoHang, khachHangDuocChon, hoaDonChoDaChon]);

  const coTheTinhPhiVanChuyen = useMemo(
    () => choPhepGiaoHang &&
      cartItems.length > 0 &&
      Boolean(diaChiGiaoHangHienThi.trim()) &&
      !dangTinhPhiVanChuyen,
    [choPhepGiaoHang, cartItems.length, diaChiGiaoHangHienThi, dangTinhPhiVanChuyen]
  );

  const coThongTinGiaoHangHopLe = useMemo(
    () => !choPhepGiaoHang ||
      (
        Boolean(tenNguoiNhanGiaoHangHienThi) &&
        Boolean(soDienThoaiNguoiNhanGiaoHangHienThi) &&
        Boolean(diaChiGiaoHangHienThi.trim()) &&
        daTinhPhiVanChuyen
      ),
    [choPhepGiaoHang, tenNguoiNhanGiaoHangHienThi, soDienThoaiNguoiNhanGiaoHangHienThi, diaChiGiaoHangHienThi, daTinhPhiVanChuyen]
  );

  const thongTinGiaoHang = useMemo(() => ({
    giaoHang: choPhepGiaoHang,
    tenNguoiNhan: tenNguoiNhanGiaoHangHienThi,
    soDienThoaiNguoiNhan: soDienThoaiNguoiNhanGiaoHangHienThi,
    diaChiGiaoHang: diaChiGiaoHangHienThi,
    donViVanChuyen: donViVanChuyen,
    phiVanChuyen: phiVanChuyen,
    diaChiDaDo: diaChiDaXacNhan,
    daTinhPhi: daTinhPhiVanChuyen,
    dangTinhPhi: dangTinhPhiVanChuyen,
    coTheTinhPhi: coTheTinhPhiVanChuyen,
    serviceTypeId: cauHinhGiaoHang.serviceTypeId,
    length: cauHinhGiaoHang.length,
    width: cauHinhGiaoHang.width,
    height: cauHinhGiaoHang.height,
    weight: cauHinhGiaoHang.weight
  }), [choPhepGiaoHang, tenNguoiNhanGiaoHangHienThi, soDienThoaiNguoiNhanGiaoHangHienThi, diaChiGiaoHangHienThi, donViVanChuyen, phiVanChuyen, diaChiDaXacNhan, daTinhPhiVanChuyen, dangTinhPhiVanChuyen, coTheTinhPhiVanChuyen, cauHinhGiaoHang]);

  const danhDauCanTinhLaiPhiVanChuyen = useCallback(() => {
    if (!choPhepGiaoHang) {
      return;
    }
    setPhiVanChuyen(0);
    setDiaChiDaXacNhan("");
    setDaTinhPhiVanChuyen(false);
  }, [choPhepGiaoHang, setPhiVanChuyen, setDiaChiDaXacNhan, setDaTinhPhiVanChuyen]);

  const taoPayloadGiaoHang = useCallback(() => {
    if (!choPhepGiaoHang) {
      return {
        giaoHang: false,
        tenNguoiNhan: null,
        soDienThoaiNguoiNhan: null,
        diaChiGiaoHang: null,
        phiVanChuyen: 0,
        donViVanChuyen: null
      };
    }

    return {
      giaoHang: true,
      tenNguoiNhan: tenNguoiNhanGiaoHangHienThi,
      soDienThoaiNguoiNhan: soDienThoaiNguoiNhanGiaoHangHienThi,
      diaChiGiaoHang: diaChiGiaoHangHienThi.trim(),
      phiVanChuyen: phiVanChuyen,
      donViVanChuyen: donViVanChuyen || "GHN"
    };
  }, [choPhepGiaoHang, tenNguoiNhanGiaoHangHienThi, soDienThoaiNguoiNhanGiaoHangHienThi, diaChiGiaoHangHienThi, phiVanChuyen, donViVanChuyen]);

  const capNhatThongTinGiaoHang = useCallback((patch) => {
    const canTinhLai = [
      "diaChiGiaoHang",
      "serviceTypeId",
      "length",
      "width",
      "height",
      "weight"
    ].some((key) => Object.prototype.hasOwnProperty.call(patch, key));

    if (Object.prototype.hasOwnProperty.call(patch, "giaoHang")) {
      setChoPhepGiaoHang(Boolean(patch.giaoHang));
    }
    if (Object.prototype.hasOwnProperty.call(patch, "tenNguoiNhan")) {
      setTenNguoiNhanGiaoHang(patch.tenNguoiNhan ?? "");
    }
    if (Object.prototype.hasOwnProperty.call(patch, "soDienThoaiNguoiNhan")) {
      setSdtNguoiNhanGiaoHang(patch.soDienThoaiNguoiNhan ?? "");
    }
    if (Object.prototype.hasOwnProperty.call(patch, "diaChiGiaoHang")) {
      setDiaChiGiaoHang(patch.diaChiGiaoHang ?? "");
    }
    
    let hasCauHinhChange = false;
    let newCauHinh = { ...cauHinhGiaoHang };
    if (Object.prototype.hasOwnProperty.call(patch, "serviceTypeId")) {
      newCauHinh.serviceTypeId = Number(patch.serviceTypeId) || 2;
      hasCauHinhChange = true;
    }
    if (Object.prototype.hasOwnProperty.call(patch, "length")) {
      newCauHinh.length = Number(patch.length) || 30;
      hasCauHinhChange = true;
    }
    if (Object.prototype.hasOwnProperty.call(patch, "width")) {
      newCauHinh.width = Number(patch.width) || 20;
      hasCauHinhChange = true;
    }
    if (Object.prototype.hasOwnProperty.call(patch, "height")) {
      newCauHinh.height = Number(patch.height) || 12;
      hasCauHinhChange = true;
    }
    if (Object.prototype.hasOwnProperty.call(patch, "weight")) {
      newCauHinh.weight = Number(patch.weight) || 500;
      hasCauHinhChange = true;
    }
    if (hasCauHinhChange) {
      setCauHinhGiaoHang(newCauHinh);
    }
    
    if (Object.prototype.hasOwnProperty.call(patch, "phiVanChuyen")) {
      setPhiVanChuyen(Number(patch.phiVanChuyen) || 0);
    }

    if (!patch.giaoHang && Object.prototype.hasOwnProperty.call(patch, "giaoHang")) {
      setPhiVanChuyen(0);
      setDiaChiDaXacNhan("");
      setDaTinhPhiVanChuyen(false);
      return;
    }

    if (canTinhLai) {
      // Vì setState là async, ta gọi hàm báo cần tính lại. Hook useEffect bên dưới sẽ trigger khi dependencies thay đổi
      if (patch.giaoHang || choPhepGiaoHang) {
         setPhiVanChuyen(0);
         setDiaChiDaXacNhan("");
         setDaTinhPhiVanChuyen(false);
      }
    }
  }, [choPhepGiaoHang, cauHinhGiaoHang, setChoPhepGiaoHang, setTenNguoiNhanGiaoHang, setSdtNguoiNhanGiaoHang, setDiaChiGiaoHang, setCauHinhGiaoHang, setPhiVanChuyen, setDiaChiDaXacNhan, setDaTinhPhiVanChuyen]);

  const xuLyTinhPhiVanChuyen = useCallback(async () => {
    if (!coTheTinhPhiVanChuyen) {
      if (!choPhepGiaoHang || !diaChiGiaoHangHienThi.trim()) {
        setPhiVanChuyen(0);
        setDonViVanChuyen("");
        setDaTinhPhiVanChuyen(true);
        setDangTinhPhiVanChuyen(false);
        return;
      }
      return;
    }
    setDangTinhPhiVanChuyen(true);
    try {
      const items = cartItems.map(item => ({
        chiTietId: item.chiTietId,
        soLuong: item.soLuong
      }));
      const response = await tinhPhiVanChuyenTaiQuay({
        toAddress: diaChiGiaoHangHienThi,
        serviceTypeId: cauHinhGiaoHang.serviceTypeId,
        length: cauHinhGiaoHang.length,
        width: cauHinhGiaoHang.width,
        height: cauHinhGiaoHang.height,
        weight: cauHinhGiaoHang.weight,
        items: items
      });
      // Extract data
      const result = response?.data || response;
      setPhiVanChuyen(result.phiVanChuyen || 0);
      setDiaChiDaXacNhan(result.diaChiDaDo || "");
      setDaTinhPhiVanChuyen(true);
    } catch (error) {
      setPhiVanChuyen(0);
      setDiaChiDaXacNhan("");
      setDaTinhPhiVanChuyen(false);
      showError(error instanceof Error ? error.message : "Không thể tính phí vận chuyển");
    } finally {
      setDangTinhPhiVanChuyen(false);
    }
  }, [coTheTinhPhiVanChuyen, choPhepGiaoHang, diaChiGiaoHangHienThi, setPhiVanChuyen, setDonViVanChuyen, setDaTinhPhiVanChuyen, setDangTinhPhiVanChuyen, cartItems, cauHinhGiaoHang]);

  const timeoutRef = useRef(null);

  useEffect(() => {
    if (coTheTinhPhiVanChuyen) {
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
      timeoutRef.current = setTimeout(() => {
        xuLyTinhPhiVanChuyen().catch(() => {});
      }, 800);
    }
    return () => {
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
    };
  }, [diaChiGiaoHangHienThi, cartItems, choPhepGiaoHang, cauHinhGiaoHang, coTheTinhPhiVanChuyen]);

  return {
    tenNguoiNhanGiaoHangHienThi,
    soDienThoaiNguoiNhanGiaoHangHienThi,
    phiVanChuyenHienThi,
    coTheTinhPhiVanChuyen,
    coThongTinGiaoHangHopLe,
    thongTinGiaoHang,
    danhDauCanTinhLaiPhiVanChuyen,
    taoPayloadGiaoHang,
    capNhatThongTinGiaoHang,
    xuLyTinhPhiVanChuyen
  };
}
