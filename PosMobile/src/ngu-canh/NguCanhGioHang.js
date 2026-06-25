import React, { createContext, useState, useContext } from 'react';

const NguCanhGioHang = createContext(undefined);

export const ProviderGioHang = ({ children }) => {
  const [sanPhamTrongGio, setSanPhamTrongGio] = useState([]);

  const themVaoGio = (sanPham) => {
    setSanPhamTrongGio(prev => {
      const tonTai = prev.find(item => item.chiTietId === sanPham.chiTietId);
      if (tonTai) {
        return prev.map(item =>
          item.chiTietId === sanPham.chiTietId ? { ...item, soLuong: item.soLuong + 1 } : item
        );
      }
      return [...prev, { ...sanPham, soLuong: 1 }];
    });
  };

  const xoaKhoiGio = (chiTietId) => {
    setSanPhamTrongGio(prev => prev.filter(item => item.chiTietId !== chiTietId));
  };

  const capNhatSoLuong = (chiTietId, soLuong) => {
    if (soLuong <= 0) {
      xoaKhoiGio(chiTietId);
      return;
    }
    setSanPhamTrongGio(prev =>
      prev.map(item => (item.chiTietId === chiTietId ? { ...item, soLuong } : item))
    );
  };

  const xoaTatCa = () => {
    setSanPhamTrongGio([]);
  };

  const tongTien = sanPhamTrongGio.reduce((sum, item) => sum + (item.giaBan || 0) * item.soLuong, 0);

  return (
    <NguCanhGioHang.Provider
      value={{
        sanPhamTrongGio,
        themVaoGio,
        xoaKhoiGio,
        capNhatSoLuong,
        xoaTatCa,
        tongTien,
      }}
    >
      {children}
    </NguCanhGioHang.Provider>
  );
};

export const suDungGioHang = () => {
  const context = useContext(NguCanhGioHang);
  if (!context) {
    throw new Error('suDungGioHang phai duoc dung trong ProviderGioHang');
  }
  return context;
};
