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
      "Không thể hoàn tất thao tác nhân viên lúc này. Vui lòng thử lại.",
      payload.errors,
    );
  }
  return payload.data;
}

export function layDanhSachNhanVien(filters) {
  const params = new URLSearchParams();
  if (filters?.keyword?.trim()) params.set("keyword", filters.keyword.trim());
  if (filters?.vaiTro != null) params.set("vaiTro", String(filters.vaiTro));
  if (filters?.trangThai != null) params.set("trangThai", String(filters.trangThai));
  const q = params.toString();
  return request(`/admin/nhan-vien${q ? `?${q}` : ""}`);
}

export function layChiTietNhanVien(id) {
  return request(`/admin/nhan-vien/${id}`);
}

export function taoNhanVien(payload) {
  return request("/admin/nhan-vien", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function capNhatNhanVien(id, payload) {
  return request(`/admin/nhan-vien/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function doiTrangThaiNhanVien(id, trangThai) {
  return request(`/admin/nhan-vien/${id}/trang-thai`, {
    method: "PATCH",
    body: JSON.stringify({ trangThai }),
  });
}

export function doiMatKhauNhanVien(id, matKhauMoi) {
  return request(`/admin/nhan-vien/${id}/mat-khau`, {
    method: "PATCH",
    body: JSON.stringify({ matKhauMoi }),
  });
}

export function xoaNhanVien(id) {
  return request(`/admin/nhan-vien/${id}`, { method: "DELETE" });
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
      sanitizeErrorMessage(result.message, "Không thể tải ảnh nhân viên lên lúc này"),
    );
  }
  return result.data.url;
}
