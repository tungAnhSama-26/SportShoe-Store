import React from 'react';
import ManHinhBanHang from './src/man-hinh/ManHinhBanHang';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import Toast from 'react-native-toast-message';

export default function App() {
  return (
    <SafeAreaProvider>
      <ManHinhBanHang />
      <Toast />
    </SafeAreaProvider>
  );
}