import React, { useEffect, useState } from 'react';
import ManHinhBanHang from './src/man-hinh/ManHinhBanHang';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { ActivityIndicator, View, Platform } from 'react-native';
import axios from 'axios';
import Toast from 'react-native-toast-message';

const API_BASE_URL = Platform.OS === 'android' ? 'http://10.0.2.2:8080/api/v1' : 'http://localhost:8080/api/v1';

export default function App() {
  const [dangKiemTra, setDangKiemTra] = useState(true);

  useEffect(() => {
    const autoLogin = async () => {
      try {
        const token = await AsyncStorage.getItem('adminToken');
        if (!token) {
          // Auto login to get token so APIs work without showing login screen
          const res = await axios.post(`${API_BASE_URL}/auth/admin/login`, { username: 'nv001', password: '123456' });
          const data = res.data;
          if (data && data.data && data.data.token) {
            await AsyncStorage.setItem('adminToken', data.data.token);
          } else if (data && data.token) {
            await AsyncStorage.setItem('adminToken', data.token);
          }
        }
      } catch (e) {
        console.error("Auto login failed", e);
      } finally {
        setDangKiemTra(false);
      }
    };
    autoLogin();
  }, []);

  if (dangKiemTra) {
    return <View style={{ flex: 1, justifyContent: 'center' }}><ActivityIndicator size="large" /></View>;
  }

  return (
    <SafeAreaProvider>
      <ManHinhBanHang />
      <Toast />
    </SafeAreaProvider>
  );
}
