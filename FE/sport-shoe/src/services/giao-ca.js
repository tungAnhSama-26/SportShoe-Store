import { apiRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể xử lý thông tin giao ca. Vui lòng thử lại.",
    ...init,
  });
}

export function layCaHoatDong() {
  return request("/admin/giao-ca/active");
}

export function moCa(payload) {
  return request("/admin/giao-ca/open", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function layThongTinGiaoCaCurrent() {
  return request("/admin/giao-ca/current-stats");
}

export function banGiaoCa(payload) {
  return request("/admin/giao-ca/handover", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function layCaChoXacNhan() {
  return request("/admin/giao-ca/pending-handovers");
}

export function xacNhanBanGiao(id, payload) {
  return request(`/admin/giao-ca/confirm-handover/${id}`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function layLichSuGiaoCa(filters) {
  const params = new URLSearchParams();
  if (filters?.nhanVienId) params.set("nhanVienId", filters.nhanVienId);
  if (filters?.trangThai) params.set("trangThai", filters.trangThai);
  if (filters?.tuNgay) params.set("tuNgay", filters.tuNgay);
  if (filters?.denNgay) params.set("denNgay", filters.denNgay);
  if (filters?.page != null) params.set("page", String(filters.page));
  if (filters?.size != null) params.set("size", String(filters.size));
  const q = params.toString();
  return request(`/admin/giao-ca/history${q ? `?${q}` : ""}`);
}
