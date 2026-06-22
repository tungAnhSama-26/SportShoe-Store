import { apiRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage: "Không thể xử lý thông tin chấm công. Vui lòng thử lại.",
    ...init,
  });
}

/**
 * Lấy danh sách chấm công theo bộ lọc
 * @param {Object} filters - { tuNgay, denNgay, nhanVienId, trangThai, page, size }
 */
export function layChamCong(filters = {}) {
  const params = new URLSearchParams();
  if (filters.tuNgay) params.set("tuNgay", filters.tuNgay);
  if (filters.denNgay) params.set("denNgay", filters.denNgay);
  if (filters.nhanVienId) params.set("nhanVienId", String(filters.nhanVienId));
  if (filters.trangThai) params.set("trangThai", filters.trangThai);
  if (filters.page != null) params.set("page", String(filters.page));
  if (filters.size != null) params.set("size", String(filters.size));
  const q = params.toString();
  return request(`/admin/cham-cong${q ? `?${q}` : ""}`);
}

/**
 * Check-in thủ công cho nhân viên
 * @param {{ nhanVienId: number, thoiGian?: string }} payload
 */
export function checkIn(payload) {
  return request("/admin/cham-cong/check-in", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

/**
 * Check-out thủ công cho nhân viên
 * @param {{ nhanVienId: number, thoiGian?: string }} payload
 */
export function checkOut(payload) {
  return request("/admin/cham-cong/check-out", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

/**
 * Cập nhật bản ghi chấm công (sửa giờ, trạng thái)
 * @param {number} id
 * @param {Object} payload
 */
export function capNhatChamCong(id, payload) {
  return request(`/admin/cham-cong/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

/**
 * Lấy thống kê chấm công tổng hợp theo khoảng ngày
 * @param {string} tuNgay  YYYY-MM-DD
 * @param {string} denNgay YYYY-MM-DD
 */
export function layThongKeChamCong(tuNgay, denNgay) {
  return request(`/admin/cham-cong/thong-ke?tuNgay=${tuNgay}&denNgay=${denNgay}`);
}

/**
 * Lấy thời gian từ server
 */
export function layServerTime() {
  return request("/admin/cham-cong/server-time");
}
