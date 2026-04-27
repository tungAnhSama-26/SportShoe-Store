import { createRequestError, sanitizeErrorMessage } from "../utils/error-message";
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ??
  "http://localhost:8080/api/v1";

async function request(path, init) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
    ...init,
  });
  const payload = await response.json();
  if (!response.ok) {
    throw createRequestError(
      payload.message,
      "Không thể hoàn tất thao tác khách hàng lúc này. Vui lòng thử lại.",
      payload.errors,
    );
  }
  return payload.data;
}

export function layDanhSachKhachHang(filters) {
  const params = new URLSearchParams();
  if (filters?.keyword?.trim()) params.set("keyword", filters.keyword.trim());
  if (filters?.trangThai != null) params.set("trangThai", String(filters.trangThai));
  const q = params.toString();
  return request(`/admin/khach-hang${q ? `?${q}` : ""}`);
}

export function layChiTietKhachHang(id) {
  return request(`/admin/khach-hang/${id}`);
}

export function taoKhachHang(payload) {
  return request("/admin/khach-hang", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function capNhatKhachHang(id, payload) {
  return request(`/admin/khach-hang/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function doiTrangThaiKhachHang(id, trangThai) {
  return request(`/admin/khach-hang/${id}/trang-thai`, {
    method: "PATCH",
    body: JSON.stringify({ trangThai }),
  });
}

export function doiMatKhauKhachHang(id, matKhauMoi) {
  return request(`/admin/khach-hang/${id}/mat-khau`, {
    method: "PATCH",
    body: JSON.stringify({ matKhauMoi }),
  });
}

export function xoaKhachHang(id) {
  return request(`/admin/khach-hang/${id}`, { method: "DELETE" });
}

// --- Address Management ---

export function layDanhSachDiaChi(khachHangId) {
  return request(`/admin/khach-hang/${khachHangId}/dia-chi`);
}

export function themDiaChi(khachHangId, payload) {
  return request(`/admin/khach-hang/${khachHangId}/dia-chi`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function capNhatDiaChi(diaChiId, payload) {
  return request(`/admin/khach-hang/dia-chi/${diaChiId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function xoaDiaChi(diaChiId) {
  return request(`/admin/khach-hang/dia-chi/${diaChiId}`, { method: "DELETE" });
}

export async function uploadFile(file) {
  const formData = new FormData();
  formData.append("file", file);
  const response = await fetch(`${API_BASE_URL}/upload`, {
    method: "POST",
    body: formData,
  });
  const result = await response.json();
  if (!response.ok) {
    throw new Error(
      sanitizeErrorMessage(result.message, "Không thể tải ảnh khách hàng lên lúc này"),
    );
  }
  return result.data.url;
}
