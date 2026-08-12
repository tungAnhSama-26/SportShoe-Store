import { apiRequest, buildQuery } from "./api-client";

let tinhCache;
const phuongXaCache = new Map();
const TINH_STORAGE_KEY = "sportshoe:dia-chi-v2:tinh-thanh";
const PHUONG_XA_STORAGE_PREFIX = "sportshoe:dia-chi-v2:phuong-xa:";

function docCache(key) {
  try {
    const value = JSON.parse(localStorage.getItem(key) || "null");
    return Array.isArray(value) ? value : null;
  } catch {
    return null;
  }
}

function luuCache(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch {
    // localStorage có thể bị chặn; cache bộ nhớ vẫn hoạt động trong phiên hiện tại.
  }
  return value;
}

export async function layTinhThanhHaiCap() {
  if (!tinhCache) {
    tinhCache = apiRequest("/client/dia-chi/tinh-thanh", {
      authenticated: false,
      fallbackMessage: "Không thể tải danh sách tỉnh/thành",
    }).then((data) => luuCache(TINH_STORAGE_KEY, Array.isArray(data) ? data : [])).catch((error) => {
      const fallback = docCache(TINH_STORAGE_KEY);
      if (fallback) return fallback;
      tinhCache = undefined;
      throw error;
    });
  }
  return tinhCache;
}

export async function layPhuongXaHaiCap(tinhThanhCode) {
  if (!tinhThanhCode) return [];
  const key = String(tinhThanhCode);
  if (!phuongXaCache.has(key)) {
    const storageKey = `${PHUONG_XA_STORAGE_PREFIX}${key}`;
    phuongXaCache.set(key, apiRequest(`/client/dia-chi/phuong-xa${buildQuery({ tinhThanhCode: key })}`, {
      authenticated: false,
      fallbackMessage: "Không thể tải danh sách phường/xã",
    }).then((data) => luuCache(storageKey, Array.isArray(data) ? data : [])).catch((error) => {
      const fallback = docCache(storageKey);
      if (fallback) return fallback;
      phuongXaCache.delete(key);
      throw error;
    }));
  }
  return phuongXaCache.get(key);
}

export async function doiChieuDiaChiCuCccd(diaChiCu, signal) {
  return apiRequest("/client/dia-chi/doi-chieu-dia-chi-cu", {
    method: "POST",
    body: JSON.stringify({ diaChiCu: String(diaChiCu ?? "").trim() }),
    signal,
    authScope: "admin",
    fallbackMessage: "Không thể chuyển đổi địa chỉ trên CCCD",
  });
}
