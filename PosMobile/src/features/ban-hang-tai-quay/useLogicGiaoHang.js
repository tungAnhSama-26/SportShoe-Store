import { useMemo, useCallback, useEffect, useRef, useState } from 'react';
import { tinhPhiVanChuyenTaiQuay } from '../../api/dichVuBanHang';
import { showError } from '../../utils/alert';
import { chuanHoaDiaChi, diaChiHopLe, dinhDangDiaChi } from '../../utils/diaChi';

function taoKhoaNoiDungDiaChi(value) {
  const diaChi = chuanHoaDiaChi(value);
  const chuanHoaChuoi = (text) => String(text || '').trim().replace(/\s+/g, ' ').toLocaleLowerCase('vi-VN');
  return JSON.stringify([
    chuanHoaChuoi(diaChi.tinhThanh),
    chuanHoaChuoi(diaChi.phuongXa),
    chuanHoaChuoi(diaChi.diaChiCuThe)
  ]);
}

function layDiaChiDungDeTinhPhi(value) {
  const diaChi = chuanHoaDiaChi(value);
  return {
    tinhThanh: diaChi.tinhThanh,
    phuongXa: diaChi.phuongXa,
    diaChiCuThe: diaChi.diaChiCuThe
  };
}

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
  cartItems,
  onPhiVanChuyenDaTinh
}) {
  const [nguonTinhPhi, setNguonTinhPhi] = useState('');
  const [moTaPhi, setMoTaPhi] = useState('');
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
    if (diaChiHopLe(diaChiGiaoHang)) {
      return chuanHoaDiaChi(diaChiGiaoHang);
    }
    if (khachHangDuocChon?.diaChiMacDinh) {
      return khachHangDuocChon.diaChiMacDinh;
    }
    return chuanHoaDiaChi(hoaDonChoDaChon?.thongTinGiaoHang?.diaChiGiaoHang);
  }, [diaChiGiaoHang, khachHangDuocChon, hoaDonChoDaChon]);

  const duDieuKienTinhPhiVanChuyen = useMemo(
    () => choPhepGiaoHang &&
      cartItems.length > 0 &&
      diaChiHopLe(diaChiGiaoHangHienThi),
    [choPhepGiaoHang, cartItems.length, diaChiGiaoHangHienThi]
  );

  const coTheTinhPhiVanChuyen = duDieuKienTinhPhiVanChuyen && !dangTinhPhiVanChuyen;

  const coThongTinGiaoHangHopLe = useMemo(
    () => !choPhepGiaoHang ||
      (
        Boolean(tenNguoiNhanGiaoHangHienThi) &&
        Boolean(soDienThoaiNguoiNhanGiaoHangHienThi) &&
        diaChiHopLe(diaChiGiaoHangHienThi) &&
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
    nguonTinhPhi,
    moTaPhi,
    coTheTinhPhi: coTheTinhPhiVanChuyen,
    serviceTypeId: cauHinhGiaoHang.serviceTypeId,
    length: cauHinhGiaoHang.length,
    width: cauHinhGiaoHang.width,
    height: cauHinhGiaoHang.height,
    weight: cauHinhGiaoHang.weight
  }), [choPhepGiaoHang, tenNguoiNhanGiaoHangHienThi, soDienThoaiNguoiNhanGiaoHangHienThi, diaChiGiaoHangHienThi, donViVanChuyen, phiVanChuyen, diaChiDaXacNhan, daTinhPhiVanChuyen, dangTinhPhiVanChuyen, nguonTinhPhi, moTaPhi, coTheTinhPhiVanChuyen, cauHinhGiaoHang]);

  const danhDauCanTinhLaiPhiVanChuyen = useCallback(() => {
    if (!choPhepGiaoHang) {
      return;
    }
    setPhiVanChuyen(0);
    setDiaChiDaXacNhan("");
    setDaTinhPhiVanChuyen(false);
    setNguonTinhPhi('');
    setMoTaPhi('');
    onPhiVanChuyenDaTinh?.(null);
  }, [choPhepGiaoHang, setPhiVanChuyen, setDiaChiDaXacNhan, setDaTinhPhiVanChuyen, onPhiVanChuyenDaTinh]);

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
      tenNguoiNhan: tenNguoiNhanGiaoHangHienThi || null,
      soDienThoaiNguoiNhan: soDienThoaiNguoiNhanGiaoHangHienThi || null,
      diaChiGiaoHang: diaChiHopLe(diaChiGiaoHangHienThi) ? chuanHoaDiaChi(diaChiGiaoHangHienThi) : null,
      phiVanChuyen: phiVanChuyen,
      donViVanChuyen: donViVanChuyen || "GHN"
    };
  }, [choPhepGiaoHang, tenNguoiNhanGiaoHangHienThi, soDienThoaiNguoiNhanGiaoHangHienThi, diaChiGiaoHangHienThi, phiVanChuyen, donViVanChuyen]);

  const capNhatThongTinGiaoHang = useCallback((patch) => {
    let canTinhLai = false;

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
      const newDiaChi = chuanHoaDiaChi(patch.diaChiGiaoHang);
      const diaChiHienTai = chuanHoaDiaChi(diaChiGiaoHang);
      if (JSON.stringify(diaChiHienTai) !== JSON.stringify(newDiaChi)) {
        canTinhLai = taoKhoaNoiDungDiaChi(diaChiHienTai) !== taoKhoaNoiDungDiaChi(newDiaChi);
        setDiaChiGiaoHang(newDiaChi);
      }
    }
    
    let hasCauHinhChange = false;
    let newCauHinh = { ...cauHinhGiaoHang };
    if (Object.prototype.hasOwnProperty.call(patch, "serviceTypeId")) {
      newCauHinh.serviceTypeId = Number(patch.serviceTypeId) || 2;
      if (newCauHinh.serviceTypeId !== cauHinhGiaoHang.serviceTypeId) hasCauHinhChange = true;
    }
    if (Object.prototype.hasOwnProperty.call(patch, "length")) {
      newCauHinh.length = Number(patch.length) || 30;
      if (newCauHinh.length !== cauHinhGiaoHang.length) hasCauHinhChange = true;
    }
    if (Object.prototype.hasOwnProperty.call(patch, "width")) {
      newCauHinh.width = Number(patch.width) || 20;
      if (newCauHinh.width !== cauHinhGiaoHang.width) hasCauHinhChange = true;
    }
    if (Object.prototype.hasOwnProperty.call(patch, "height")) {
      newCauHinh.height = Number(patch.height) || 12;
      if (newCauHinh.height !== cauHinhGiaoHang.height) hasCauHinhChange = true;
    }
    if (Object.prototype.hasOwnProperty.call(patch, "weight")) {
      newCauHinh.weight = Number(patch.weight) || 500;
      if (newCauHinh.weight !== cauHinhGiaoHang.weight) hasCauHinhChange = true;
    }
    if (hasCauHinhChange) {
      canTinhLai = true;
      setCauHinhGiaoHang(newCauHinh);
    }
    
    if (Object.prototype.hasOwnProperty.call(patch, "phiVanChuyen")) {
      const phiMoi = Number(patch.phiVanChuyen) || 0;
      setPhiVanChuyen(phiMoi);
      setDaTinhPhiVanChuyen(true);
      setNguonTinhPhi("MANUAL");
      setMoTaPhi("Phí giao hàng được nhập thủ công");
      onPhiVanChuyenDaTinh?.(phiMoi);
    }

    if (!patch.giaoHang && Object.prototype.hasOwnProperty.call(patch, "giaoHang")) {
      setPhiVanChuyen(0);
      setDiaChiDaXacNhan("");
      setDaTinhPhiVanChuyen(false);
      onPhiVanChuyenDaTinh?.(null);
      return;
    }

    if (canTinhLai) {
      // Vì setState là async, ta gọi hàm báo cần tính lại. Hook useEffect bên dưới sẽ trigger khi dependencies thay đổi
      if (patch.giaoHang || choPhepGiaoHang) {
         setPhiVanChuyen(0);
         setDiaChiDaXacNhan("");
         setDaTinhPhiVanChuyen(false);
         onPhiVanChuyenDaTinh?.(null);
      }
    }
  }, [choPhepGiaoHang, diaChiGiaoHang, cauHinhGiaoHang, setChoPhepGiaoHang, setTenNguoiNhanGiaoHang, setSdtNguoiNhanGiaoHang, setDiaChiGiaoHang, setCauHinhGiaoHang, setPhiVanChuyen, setDiaChiDaXacNhan, setDaTinhPhiVanChuyen, onPhiVanChuyenDaTinh]);

  const feeRequestInFlightRef = useRef(false);

  const xuLyTinhPhiVanChuyen = useCallback(async () => {
    if (feeRequestInFlightRef.current) return;
    if (!coTheTinhPhiVanChuyen) {
      if (!choPhepGiaoHang || !diaChiHopLe(diaChiGiaoHangHienThi)) {
        setPhiVanChuyen(0);
        setDonViVanChuyen("");
        setDaTinhPhiVanChuyen(true);
        setDangTinhPhiVanChuyen(false);
        onPhiVanChuyenDaTinh?.(null);
        return;
      }
      return;
    }
    feeRequestInFlightRef.current = true;
    setDangTinhPhiVanChuyen(true);
    try {
      const items = cartItems.map(item => ({
        chiTietId: item.chiTietId,
        soLuong: item.soLuong
      }));
      const response = await tinhPhiVanChuyenTaiQuay({
        diaChiGiaoHang: chuanHoaDiaChi(diaChiGiaoHangHienThi),
        serviceTypeId: cauHinhGiaoHang.serviceTypeId,
        length: cauHinhGiaoHang.length,
        width: cauHinhGiaoHang.width,
        height: cauHinhGiaoHang.height,
        weight: cauHinhGiaoHang.weight,
        items: items
      });
      // Extract data
      const result = response?.data || response;
      const phiMoi = Number(result.phiVanChuyen ?? result.total ?? 0);
      setPhiVanChuyen(phiMoi);
      onPhiVanChuyenDaTinh?.(phiMoi);
      setDiaChiDaXacNhan(dinhDangDiaChi(result.diaChiDaDoiSoat));
      setNguonTinhPhi(result.nguonTinhPhi || 'GHN_LIVE');
      setMoTaPhi(result.nguonTinhPhi === 'GHN_CACHE'
        ? (result.giaCu ? 'Phí GHN từ cache cũ (ước tính)' : 'Phí GHN đã lưu gần nhất (ước tính)')
        : result.nguonTinhPhi === 'GHN_PUBLIC_TARIFF'
          ? 'Phí offline ước tính theo bảng giá công khai GHN'
          : (result.uocTinh ? 'Phí GHN ước tính theo các tuyến cũ' : 'Phí GHN'));
      setDaTinhPhiVanChuyen(true);
    } catch (error) {
      setPhiVanChuyen(0);
      setDiaChiDaXacNhan("");
      setDaTinhPhiVanChuyen(false);
      setNguonTinhPhi('');
      setMoTaPhi('');
      onPhiVanChuyenDaTinh?.(null);
      showError(error instanceof Error ? error.message : "Không thể tính phí vận chuyển");
    } finally {
      feeRequestInFlightRef.current = false;
      setDangTinhPhiVanChuyen(false);
    }
  }, [coTheTinhPhiVanChuyen, choPhepGiaoHang, diaChiGiaoHangHienThi, setPhiVanChuyen, setDonViVanChuyen, setDaTinhPhiVanChuyen, setDangTinhPhiVanChuyen, cartItems, cauHinhGiaoHang, onPhiVanChuyenDaTinh]);

  const timeoutRef = useRef(null);
  const lastAutoFeeKeyRef = useRef("");

  const dependenciesTinhPhi = useMemo(() => {
    return JSON.stringify({
      diaChi: layDiaChiDungDeTinhPhi(diaChiGiaoHangHienThi),
      items: cartItems
        .map(item => ({ id: item.chiTietId, sl: item.soLuong }))
        .sort((a, b) => String(a.id).localeCompare(String(b.id))),
      giaoHang: choPhepGiaoHang,
      cauHinh: {
        serviceTypeId: cauHinhGiaoHang.serviceTypeId,
        length: cauHinhGiaoHang.length,
        width: cauHinhGiaoHang.width,
        height: cauHinhGiaoHang.height,
        weight: cauHinhGiaoHang.weight
      }
    });
  }, [diaChiGiaoHangHienThi, cartItems, choPhepGiaoHang, cauHinhGiaoHang]);

  const latestXuLyTinhPhiRef = useRef(xuLyTinhPhiVanChuyen);
  useEffect(() => {
    latestXuLyTinhPhiRef.current = xuLyTinhPhiVanChuyen;
  }, [xuLyTinhPhiVanChuyen]);

  useEffect(() => {
    if (!duDieuKienTinhPhiVanChuyen) {
      lastAutoFeeKeyRef.current = "";
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
      return undefined;
    }

    if (daTinhPhiVanChuyen) {
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
      return undefined;
    }

    if (lastAutoFeeKeyRef.current === dependenciesTinhPhi) return undefined;
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    timeoutRef.current = setTimeout(() => {
      if (lastAutoFeeKeyRef.current === dependenciesTinhPhi) return;
      lastAutoFeeKeyRef.current = dependenciesTinhPhi;
      latestXuLyTinhPhiRef.current().catch(() => {});
    }, 800);

    return () => {
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
    };
  }, [dependenciesTinhPhi, duDieuKienTinhPhiVanChuyen, daTinhPhiVanChuyen]);

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
