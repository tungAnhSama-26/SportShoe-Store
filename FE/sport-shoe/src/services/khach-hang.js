import { apiRequest, uploadFileRequest } from "./api-client";
import { chuanHoaDiaChi, taoPayloadDiaChi } from "../utils/dia-chi";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể hoàn tất thao tác khách hàng lúc này. Vui lòng thử lại.",
    ...init,
  });
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

const chuanHoaBanGhiDiaChi = (record) => ({ ...record, ...chuanHoaDiaChi(record) });
const payloadDiaChi = (record) => ({ hoTen: record.hoTen, sdt: record.sdt, diaChi: taoPayloadDiaChi(record), laMacDinh: record.laMacDinh });

export async function layDanhSachDiaChi(khachHangId) {
  const data = await request(`/admin/khach-hang/${khachHangId}/dia-chi`);
  return Array.isArray(data) ? data.map(chuanHoaBanGhiDiaChi) : [];
}

export function themDiaChi(khachHangId, payload) {
  return request(`/admin/khach-hang/${khachHangId}/dia-chi`, {
    method: "POST",
    body: JSON.stringify(payloadDiaChi(payload)),
  }).then(chuanHoaBanGhiDiaChi);
}

export function capNhatDiaChi(diaChiId, payload) {
  return request(`/admin/khach-hang/dia-chi/${diaChiId}`, {
    method: "PUT",
    body: JSON.stringify(payloadDiaChi(payload)),
  }).then(chuanHoaBanGhiDiaChi);
}

export function xoaDiaChi(diaChiId) {
  return request(`/admin/khach-hang/dia-chi/${diaChiId}`, { method: "DELETE" });
}

export function datMacDinhDiaChi(diaChiId) {
  return request(`/admin/khach-hang/dia-chi/${diaChiId}/mac-dinh`, { method: "PATCH" });
}

export function layHoaDonTheoKhachHang(khachHangId) {
  return request(`/admin/khach-hang/${khachHangId}/hoa-don`);
}

export async function uploadFile(file) {
  return uploadFileRequest(file, "Không thể tải ảnh khách hàng lên lúc này");
}
