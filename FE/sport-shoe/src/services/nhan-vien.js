import { apiRequest, uploadFileRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể hoàn tất thao tác nhân viên lúc này. Vui lòng thử lại.",
    ...init,
  });
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

export function layHoSoNhanVien() {
  return request("/nhanvien/profile");
}

export function capNhatHoSoNhanVien(payload) {
  return request("/nhanvien/profile", {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function doiMatKhauHoSoNhanVien(matKhauMoi) {
  return request("/nhanvien/profile/mat-khau", {
    method: "PATCH",
    body: JSON.stringify({ matKhauMoi }),
  });
}

export function xoaNhanVien(id) {
  return request(`/admin/nhan-vien/${id}`, { method: "DELETE" });
}

export async function uploadFile(file) {
  return uploadFileRequest(file, "Không thể tải ảnh nhân viên lên lúc này");
}
