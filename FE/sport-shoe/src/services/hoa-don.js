import { apiRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể hoàn tất thao tác hóa đơn lúc này. Vui lòng thử lại.",
    ...init,
  });
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

export function xacNhanThanhToanCod(id, payload) {
  return request(`/admin/hoa-don/${id}/thanh-toan-cod`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function xacNhanHoanTien(id, payload) {
  return request(`/admin/hoa-don/${id}/hoan-tien`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
