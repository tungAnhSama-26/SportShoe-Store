import { apiRequest, uploadFileRequest } from "./api-client";

// ─── Types ───────────────────────────────────────────────────────────────────

// ─── API functions ────────────────────────────────────────────────────────────

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể hoàn tất thao tác sản phẩm lúc này. Vui lòng thử lại.",
    ...init,
  });
}

export function layDanhSachGiay(filters = {}) {
  const params = new URLSearchParams();
  if (filters.keyword) params.set("keyword", filters.keyword);
  if (filters.thuongHieuId != null)
    params.set("thuongHieuId", String(filters.thuongHieuId));
  if (filters.loaiGiayId != null)
    params.set("loaiGiayId", String(filters.loaiGiayId));
  if (filters.trangThai != null)
    params.set("trangThai", String(filters.trangThai));
  if (filters.minPrice != null)
    params.set("minPrice", String(filters.minPrice));
  if (filters.maxPrice != null)
    params.set("maxPrice", String(filters.maxPrice));
  params.set("page", String(filters.page ?? 0));
  params.set("size", String(filters.size ?? 10));
  return request(`/admin/san-pham?${params}`);
}

export function layDanhMuc() {
  return request("/admin/san-pham/danh-muc", {
    headers: {
      "Cache-Control": "no-cache, no-store, must-revalidate",
      Pragma: "no-cache",
      Expires: "0",
    },
  });
}

export function checkTenGiay(ten, id) {
  const params = new URLSearchParams();
  params.set("ten", ten);
  if (id != null) params.set("id", String(id));
  return request(`/admin/san-pham/check-ten?${params}`);
}

export function checkMaGiay(ma, id) {
  const params = new URLSearchParams();
  params.set("ma", ma);
  if (id != null) params.set("id", String(id));
  return request(`/admin/san-pham/check-ma?${params}`);
}

export function checkTrungThuocTinh(body) {
  return request("/admin/san-pham/check-trung-thuoc-tinh", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function chiTietGiay(id) {
  return request(`/admin/san-pham/${id}`);
}

export function layDanhSachChiTietSanPham(filters = {}) {
  const params = new URLSearchParams();
  if (filters.keyword) params.set("keyword", filters.keyword);
  if (filters.giayId != null) params.set("giayId", String(filters.giayId));
  if (filters.mauSacId != null)
    params.set("mauSacId", String(filters.mauSacId));
  if (filters.kichCoId != null)
    params.set("kichCoId", String(filters.kichCoId));
  if (filters.trangThai != null)
    params.set("trangThai", String(filters.trangThai));
  params.set("page", String(filters.page ?? 0));
  params.set("size", String(filters.size ?? 10));
  return request(`/admin/san-pham/chi-tiet?${params}`);
}

export function taoGiay(body) {
  return request("/admin/san-pham", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function taoChiTietSanPham(body) {
  return request("/admin/san-pham/chi-tiet", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function taoChiTietSanPhamHangLoat(body) {
  return request("/admin/san-pham/chi-tiet-hang-loat", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function capNhatGiay(id, body) {
  return request(`/admin/san-pham/${id}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

export function doiTrangThai(id, trangThai) {
  return request(`/admin/san-pham/${id}/trang-thai`, {
    method: "PATCH",
    body: JSON.stringify({ trangThai }),
  });
}

export function xoaGiay(id) {
  return request(`/admin/san-pham/${id}`, { method: "DELETE" });
}

// Biến thể
export function layBienThe(giayId) {
  return request(`/admin/san-pham/${giayId}/bien-the`);
}

export function taoBienThe(giayId, body) {
  return request(`/admin/san-pham/${giayId}/bien-the`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function capNhatBienThe(id, body) {
  return request(`/admin/san-pham/bien-the/${id}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

export function doiTrangThaiBienThe(id, kichHoat) {
  return request(`/admin/san-pham/bien-the/${id}/trang-thai`, {
    method: "PATCH",
    body: JSON.stringify({ kichHoat }),
  });
}

export function xoaBienThe(id) {
  return request(`/admin/san-pham/bien-the/${id}`, { method: "DELETE" });
}

// Hình ảnh
export function layHinhAnh(chiTietId) {
  return request(`/admin/san-pham/bien-the/${chiTietId}/hinh-anh`);
}

export function themHinhAnh(chiTietId, body) {
  return request(`/admin/san-pham/bien-the/${chiTietId}/hinh-anh`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function capNhatHinhAnh(id, body) {
  return request(`/admin/san-pham/hinh-anh/${id}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

export function xoaHinhAnh(id) {
  return request(`/admin/san-pham/hinh-anh/${id}`, { method: "DELETE" });
}

export function datHinhChinh(id) {
  return request(`/admin/san-pham/hinh-anh/${id}/chinh`, { method: "PATCH" });
}

export async function uploadFile(file) {
  return uploadFileRequest(file, "Không thể tải ảnh sản phẩm lên lúc này");
}
