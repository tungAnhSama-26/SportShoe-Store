import mayKhachApi from './apiClient';
import AsyncStorage from '@react-native-async-storage/async-storage';

let boNhoTinh = null;
const boNhoPhuongXa = new Map();
const KHOA_TINH = 'sportshoe:dia-chi-v2:tinh-thanh';
const KHOA_PHUONG_XA = 'sportshoe:dia-chi-v2:phuong-xa:';

async function docDuPhong(key) {
  try {
    const value = JSON.parse((await AsyncStorage.getItem(key)) || 'null');
    return Array.isArray(value) ? value : null;
  } catch {
    return null;
  }
}

async function taiVaLuu(request, key) {
  try {
    const data = await request();
    const result = Array.isArray(data) ? data : [];
    await AsyncStorage.setItem(key, JSON.stringify(result));
    return result;
  } catch (error) {
    const fallback = await docDuPhong(key);
    if (fallback) return fallback;
    throw error;
  }
}

export async function layTinhThanhHaiCap() {
  if (!boNhoTinh) {
    boNhoTinh = taiVaLuu(
      () => mayKhachApi.get('/client/dia-chi/tinh-thanh'),
      KHOA_TINH,
    ).catch((error) => {
      boNhoTinh = null;
      throw error;
    });
  }
  return boNhoTinh;
}

export async function layPhuongXaHaiCap(tinhThanhCode) {
  const key = String(tinhThanhCode || '');
  if (!key) return [];
  if (!boNhoPhuongXa.has(key)) {
    const storageKey = `${KHOA_PHUONG_XA}${key}`;
    boNhoPhuongXa.set(key, taiVaLuu(
      () => mayKhachApi.get('/client/dia-chi/phuong-xa', { params: { tinhThanhCode: key } }),
      storageKey,
    ).catch((error) => {
      boNhoPhuongXa.delete(key);
      throw error;
    }));
  }
  return boNhoPhuongXa.get(key);
}
