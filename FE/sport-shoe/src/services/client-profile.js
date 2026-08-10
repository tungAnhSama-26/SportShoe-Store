import { apiRequest, API_BASE_URL, getAuthHeaders } from "./api-client";
import { chuanHoaDiaChi, taoPayloadDiaChi } from "../utils/dia-chi";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể hoàn tất thao tác hồ sơ khách hàng lúc này. Vui lòng thử lại.",
    ...init,
  });
}

// Upload 1 ảnh (multipart) -> trả về URL đầy đủ của ảnh.
export async function uploadAnh(file, authScope = "customer") {
  const formData = new FormData();
  formData.append("file", file);
  // KHÔNG set Content-Type để trình duyệt tự thêm boundary multipart.
  const res = await fetch(`${API_BASE_URL}/upload`, {
    method: "POST",
    headers: { ...getAuthHeaders(authScope, "/upload") },
    body: formData,
  });
  const json = await res.json().catch(() => null);
  if (!res.ok || json?.success === false) {
    throw new Error(json?.message || "Tải ảnh lên thất bại");
  }
  return json?.data?.url || json?.url || "";
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
const chuanHoaBanGhiDiaChi = (record) => ({ ...record, ...chuanHoaDiaChi(record) });
const payloadDiaChi = (record) => ({ hoTen: record.hoTen, sdt: record.sdt, diaChi: taoPayloadDiaChi(record), laMacDinh: record.laMacDinh });

export async function layDanhSachDiaChiProfile(khachHangId) {
  const data = await request(`/client/khach-hang/${khachHangId}/dia-chi`);
  return Array.isArray(data) ? data.map(chuanHoaBanGhiDiaChi) : [];
}

export function themDiaChiProfile(khachHangId, payload) {
  return request(`/client/khach-hang/${khachHangId}/dia-chi`, {
    method: "POST",
    body: JSON.stringify(payloadDiaChi(payload)),
  }).then(chuanHoaBanGhiDiaChi);
}

export function capNhatDiaChiProfile(khachHangId, diaChiId, payload) {
  return request(`/client/khach-hang/${khachHangId}/dia-chi/${diaChiId}`, {
    method: "PUT",
    body: JSON.stringify(payloadDiaChi(payload)),
  }).then(chuanHoaBanGhiDiaChi);
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
