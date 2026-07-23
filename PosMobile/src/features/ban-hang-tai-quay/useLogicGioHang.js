import { useState, useMemo, useCallback } from 'react';
import { showError, showWarning } from '../../utils/alert';
import { validateGioHang as doValidateGioHang } from './ValidateGioHang';

export function useLogicGioHang({
  danhDauCanTinhLaiPhiVanChuyen,
  capNhatTienKhachThanhToan,
  danhDauCanApDungLaiPhieu,
  dongBoSanPhamSauKhiThemVaoGio,
  xoaPhanHoi
}) {
  const [cartItems, setCartItems] = useState([]);

  const tongSoLuong = useMemo(() => {
    return cartItems.reduce((total, item) => total + item.soLuong, 0);
  }, [cartItems]);

  const tongTien = useMemo(() => {
    return cartItems.reduce((total, item) => total + item.soLuong * item.giaBan, 0);
  }, [cartItems]);

  const sanPhamKhongHopLe = useMemo(() => {
    return cartItems.find((item) => !Number.isInteger(Number(item.soLuong)) || Number(item.soLuong) <= 0) ?? null;
  }, [cartItems]);

  const sanPhamValidationMessage = useMemo(() => {
    if (sanPhamKhongHopLe) {
      return `Số lượng của sản phẩm ${sanPhamKhongHopLe.tenSanPham} phải lớn hơn 0.`;
    }
    return "";
  }, [sanPhamKhongHopLe]);

  const validateGioHang = useCallback((isPayment = false) => {
    return doValidateGioHang(sanPhamValidationMessage, isPayment, cartItems.length);
  }, [sanPhamValidationMessage, cartItems]);

  const taoDanhSachSanPhamThanhToan = useCallback(() => {
    return cartItems.map((item) => ({
      chiTietId: item.chiTietId,
      soLuong: item.soLuong,
      giaBan: item.giaBan
    }));
  }, [cartItems]);

  const soLuongDaChon = useCallback((chiTietId) => {
    return cartItems.filter((item) => Number(item.chiTietId) === Number(chiTietId)).reduce((sum, item) => sum + item.soLuong, 0);
  }, [cartItems]);

  const soLuongDaLuu = useCallback((chiTietId) => {
    return cartItems.filter((item) => Number(item.chiTietId) === Number(chiTietId)).reduce((sum, item) => sum + (item.soLuongBanDau || 0), 0);
  }, [cartItems]);

  const soLuongConLai = useCallback((chiTietId, soLuongTon) => {
    return Math.max(Number(soLuongTon) - soLuongDaChon(chiTietId), 0);
  }, [soLuongDaChon]);

  const themSanPham = useCallback((product, quantity = 1, options = {}) => {
    const soLuongCoTheThem = soLuongConLai(product.chiTietId, product.soLuongTon);
    if (quantity > soLuongCoTheThem) {
      showWarning(`Sản phẩm ${product.tenSanPham} đã vượt giới hạn tồn kho`);
      return false;
    }

    const exactItem = cartItems.find(
      (item) => Number(item.chiTietId) === Number(product.chiTietId) && Number(item.giaBan) === Number(product.giaBan)
    );
    const anyExistingItem = cartItems.find(
      (item) => Number(item.chiTietId) === Number(product.chiTietId)
    );
    
    let resultStatus = "added";
    let oldPrice = null;

    if (exactItem) {
      resultStatus = "incremented";
      setCartItems(prev => prev.map(item => 
        item.cartItemId === exactItem.cartItemId
          ? { ...item, soLuong: item.soLuong + quantity }
          : item
      ));
    } else {
      if (anyExistingItem) {
        resultStatus = "price_updated";
        oldPrice = anyExistingItem.giaBan;
      }
      
      setCartItems(prev => [
        ...prev,
        {
          cartItemId: Date.now().toString() + Math.random().toString(),
          chiTietId: product.chiTietId,
          maSanPham: product.maSanPham,
          tenSanPham: product.tenSanPham,
          sku: product.sku,
          mauSac: product.mauSac,
          kichCo: product.kichCo,
          hinhAnh: product.hinhAnh || "",
          soLuong: quantity,
          soLuongBanDau: 0,
          giaBan: product.giaBan,
          oldPrice: oldPrice,
          soLuongTon: product.soLuongTon
        }
      ]);
    }
    
    if (dongBoSanPhamSauKhiThemVaoGio) dongBoSanPhamSauKhiThemVaoGio(options);
    if (danhDauCanApDungLaiPhieu) danhDauCanApDungLaiPhieu();
    if (danhDauCanTinhLaiPhiVanChuyen) danhDauCanTinhLaiPhiVanChuyen();
    if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
    if (xoaPhanHoi) xoaPhanHoi();
    
    return {
      status: resultStatus,
      oldPrice: oldPrice,
      newPrice: product.giaBan,
      tenSanPham: product.tenSanPham
    };
  }, [cartItems, soLuongConLai, dongBoSanPhamSauKhiThemVaoGio, danhDauCanApDungLaiPhieu, danhDauCanTinhLaiPhiVanChuyen, capNhatTienKhachThanhToan, xoaPhanHoi]);

  const tangSoLuong = useCallback((cartItemId) => {
    let reachedLimit = "";
    setCartItems(prev => {
      let limitHit = "";
      const newItems = prev.map((item) => {
        if (item.cartItemId !== cartItemId) {
          return item;
        }
        // we use a fresh calculation based on current state inside setState
        const currentSelected = prev.filter(p => Number(p.chiTietId) === Number(item.chiTietId)).reduce((sum, p) => sum + p.soLuong, 0);
        const conLai = Math.max(Number(item.soLuongTon) - currentSelected, 0);
        
        if (conLai <= 0) {
          limitHit = item.tenSanPham;
          return item;
        }
        return { ...item, soLuong: item.soLuong + 1 };
      });
      reachedLimit = limitHit;
      return newItems;
    });

    if (reachedLimit) {
      setTimeout(() => showWarning(`Sản phẩm ${reachedLimit} đã vượt giới hạn tồn kho`), 0);
      return;
    }
    if (danhDauCanApDungLaiPhieu) danhDauCanApDungLaiPhieu();
    if (danhDauCanTinhLaiPhiVanChuyen) danhDauCanTinhLaiPhiVanChuyen();
    if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
  }, [danhDauCanApDungLaiPhieu, danhDauCanTinhLaiPhiVanChuyen, capNhatTienKhachThanhToan]);

  const giamSoLuong = useCallback((cartItemId) => {
    setCartItems(prev => prev.map((item) => item.cartItemId === cartItemId ? { ...item, soLuong: item.soLuong - 1 } : item).filter((item) => item.soLuong > 0));
    if (danhDauCanApDungLaiPhieu) danhDauCanApDungLaiPhieu();
    if (danhDauCanTinhLaiPhiVanChuyen) danhDauCanTinhLaiPhiVanChuyen();
    if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
  }, [danhDauCanApDungLaiPhieu, danhDauCanTinhLaiPhiVanChuyen, capNhatTienKhachThanhToan]);

  const xoaSanPham = useCallback((cartItemId) => {
    setCartItems(prev => prev.filter((item) => item.cartItemId !== cartItemId));
    if (danhDauCanApDungLaiPhieu) danhDauCanApDungLaiPhieu();
    if (danhDauCanTinhLaiPhiVanChuyen) danhDauCanTinhLaiPhiVanChuyen();
    if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
  }, [danhDauCanApDungLaiPhieu, danhDauCanTinhLaiPhiVanChuyen, capNhatTienKhachThanhToan]);

  const capNhatSoLuong = useCallback((cartItemId, newQuantity) => {
    let newQuantityNum = parseInt(newQuantity, 10);
    if (isNaN(newQuantityNum) || newQuantityNum <= 0) {
      newQuantityNum = 1;
    }

    let reachedLimit = "";
    setCartItems(prev => {
      let limitHit = "";
      const newItems = prev.map((item) => {
        if (item.cartItemId !== cartItemId) {
          return item;
        }
        const currentSelectedExcludingThis = prev.filter(p => Number(p.chiTietId) === Number(item.chiTietId) && p.cartItemId !== cartItemId).reduce((sum, p) => sum + p.soLuong, 0);
        const conLaiThem = Math.max(Number(item.soLuongTon) - currentSelectedExcludingThis - item.soLuong, 0);
        const maxAllowed = item.soLuong + conLaiThem;

        if (newQuantityNum > maxAllowed) {
          limitHit = item.tenSanPham;
          return { ...item, soLuong: maxAllowed };
        }
        return { ...item, soLuong: newQuantityNum };
      });
      reachedLimit = limitHit;
      return newItems;
    });

    if (reachedLimit) {
      setTimeout(() => showWarning(`Sản phẩm ${reachedLimit} đã vượt giới hạn tồn kho`), 0);
    }

    if (danhDauCanApDungLaiPhieu) danhDauCanApDungLaiPhieu();
    if (danhDauCanTinhLaiPhiVanChuyen) danhDauCanTinhLaiPhiVanChuyen();
    if (capNhatTienKhachThanhToan) capNhatTienKhachThanhToan();
  }, [danhDauCanApDungLaiPhieu, danhDauCanTinhLaiPhiVanChuyen, capNhatTienKhachThanhToan]);

  return {
    cartItems,
    setCartItems,
    tongSoLuong,
    tongTien,
    sanPhamValidationMessage,
    validateGioHang,
    taoDanhSachSanPhamThanhToan,
    soLuongConLai,
    themSanPham,
    tangSoLuong,
    giamSoLuong,
    xoaSanPham,
    capNhatSoLuong,
  };
}
