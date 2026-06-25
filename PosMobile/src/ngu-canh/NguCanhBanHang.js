import React, { createContext, useContext } from 'react';
import { useLogicBanHangTaiQuay } from '../features/ban-hang-tai-quay/useLogicBanHangTaiQuay';

const NguCanhBanHang = createContext(undefined);

export const ProviderBanHang = ({ children }) => {
  const logic = useLogicBanHangTaiQuay();

  return (
    <NguCanhBanHang.Provider value={logic}>
      {children}
    </NguCanhBanHang.Provider>
  );
};

export const suDungBanHang = () => {
  const context = useContext(NguCanhBanHang);
  if (!context) {
    throw new Error('suDungBanHang phai duoc dung trong ProviderBanHang');
  }
  return context;
};
