import { apiRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể xử lý thông tin lịch làm việc. Vui lòng thử lại.",
    ...init,
  });
}

/**
 * Lấy lịch làm việc theo tuần.
 * @param {string} tuNgay  - YYYY-MM-DD
 * @param {string} denNgay - YYYY-MM-DD
 */
export function layLichLamViec(tuNgay, denNgay) {
  return request(`/admin/lich-lam-viec?tuNgay=${tuNgay}&denNgay=${denNgay}`);
}

/**
 * Phân ca thủ công.
 * @param {{ nhanVienId: string, ngay: string, ca: string }} payload
 *   ca: "sang" | "chieu" | "toi" | null (null = xoá ca)
 */
export function phanCa(payload) {
  return request("/admin/lich-lam-viec", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function xoaLichLamViec(id) {
  return request(`/admin/lich-lam-viec/${id}`, {
    method: "DELETE",
  });
}

/**
 * Xếp ca tự động cho khoảng tuần.
 * @param {string} tuNgay  - YYYY-MM-DD
 * @param {string} denNgay - YYYY-MM-DD
 */
export function xepCaTuDong(tuNgay, denNgay) {
  return request(
    `/admin/lich-lam-viec/auto-assign?tuNgay=${tuNgay}&denNgay=${denNgay}`,
    { method: "POST" }
  );
}

