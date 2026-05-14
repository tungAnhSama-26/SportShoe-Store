import { createRequestError, sanitizeErrorMessage } from "../utils/error-message";
import { getAuthHeaders } from "./auth";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ??
  "http://localhost:8080/api/v1";

async function request(path, init) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders(),
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  const payload = await response.json();
  if (!response.ok) {
    throw createRequestError(
      payload.message,
      "Không thể hoàn tất thao tác hóa đơn lúc này. Vui lòng thử lại.",
      payload.errors,
    );
  }
  return payload.data;
}

export function layDanhSachHoaDon(filters) {
  const params = new URLSearchParams();
  if (filters?.keyword?.trim()) params.set("keyword", filters.keyword.trim());
  if (filters?.loaiDon?.trim()) params.set("loaiDon", filters.loaiDon.trim());
  if (filters?.trangThai?.trim() && filters.trangThai.trim() !== "Tất cả") params.set("trangThai", filters.trangThai.trim());
  if (filters?.tuNgay?.trim()) params.set("tuNgay", filters.tuNgay.trim());
  if (filters?.denNgay?.trim()) params.set("denNgay", filters.denNgay.trim());
  const query = params.toString();
  return request(`/admin/hoa-don${query ? `?${query}` : ""}`);
}

export function layChiTietHoaDon(id) {
  return request(`/admin/hoa-don/${id}`);
}

export function capNhatTrangThaiHoaDon(id, payload) {
  return request(`/admin/hoa-don/${id}/trang-thai`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

export function capNhatSanPhamHoaDon(id, payload) {
  return request(`/admin/hoa-don/${id}/san-pham`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function tinhPhiVanChuyenGhn(id, payload) {
  return request(`/admin/hoa-don/${id}/phi-van-chuyen/ghn`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
