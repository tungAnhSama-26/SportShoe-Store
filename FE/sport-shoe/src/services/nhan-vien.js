import { createRequestError, sanitizeErrorMessage } from "../utils/error-message";
import { getAuthHeaders } from "./auth";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ??
  "http://localhost:8080/api/v1";

async function request(path, init) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json", ...getAuthHeaders(), ...(init?.headers ?? {}) },
    ...init,
  });
  const payload = await response.json();
  if (!response.ok) {
    const requestError = createRequestError(
      payload.message,
      "Khong the hoan tat thao tac nhan vien luc nay. Vui long thu lai.",
      payload.errors,
    );
    requestError.status = response.status;
    throw requestError;
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

export function layNhanVienTheoCccd(cccd) {
  return request(`/admin/nhan-vien/cccd/${encodeURIComponent(String(cccd ?? "").trim())}`);
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
    headers: getAuthHeaders(),
    body: formData,
  });
  const result = await response.json();
  if (!response.ok) {
    throw new Error(
      sanitizeErrorMessage(result.message, "Khong the tai anh nhan vien len luc nay"),
    );
  }
  return result.data.url;
}
