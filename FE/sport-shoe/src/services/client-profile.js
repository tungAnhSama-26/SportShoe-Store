import { apiRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể hoàn tất thao tác hồ sơ khách hàng lúc này. Vui lòng thử lại.",
    ...init,
  });
}

// --- Client Profile Info ---
export function layProfileKhachHang(khachHangId) {
  return request(`/client/khach-hang/${khachHangId}`);
}

export function capNhatProfileKhachHang(khachHangId, payload) {
  return request(`/client/khach-hang/${khachHangId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function doiMatKhauProfileKhachHang(khachHangId, payload) {
  return request(`/client/khach-hang/${khachHangId}/doi-mat-khau`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

// --- Customer Address Management ---
export function layDanhSachDiaChiProfile(khachHangId) {
  return request(`/client/khach-hang/${khachHangId}/dia-chi`);
}

export function themDiaChiProfile(khachHangId, payload) {
  return request(`/client/khach-hang/${khachHangId}/dia-chi`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function capNhatDiaChiProfile(khachHangId, diaChiId, payload) {
  return request(`/client/khach-hang/${khachHangId}/dia-chi/${diaChiId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function xoaDiaChiProfile(khachHangId, diaChiId) {
  return request(`/client/khach-hang/${khachHangId}/dia-chi/${diaChiId}`, {
    method: "DELETE",
  });
}

export function datMacDinhDiaChiProfile(khachHangId, diaChiId) {
  return request(`/client/khach-hang/${khachHangId}/dia-chi/${diaChiId}/mac-dinh`, {
    method: "PATCH",
  });
}

// --- Bank Account Management ---
export function layDanhSachTaiKhoanNganHang(khachHangId) {
  return request(`/client/khach-hang/${khachHangId}/tai-khoan-ngan-hang`);
}

export function themTaiKhoanNganHang(khachHangId, payload) {
  return request(`/client/khach-hang/${khachHangId}/tai-khoan-ngan-hang`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function capNhatTaiKhoanNganHang(khachHangId, id, payload) {
  return request(`/client/khach-hang/${khachHangId}/tai-khoan-ngan-hang/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function xoaTaiKhoanNganHang(khachHangId, id) {
  return request(`/client/khach-hang/${khachHangId}/tai-khoan-ngan-hang/${id}`, {
    method: "DELETE",
  });
}

export function datMacDinhTaiKhoanNganHang(khachHangId, id) {
  return request(`/client/khach-hang/${khachHangId}/tai-khoan-ngan-hang/${id}/mac-dinh`, {
    method: "POST",
  });
}
