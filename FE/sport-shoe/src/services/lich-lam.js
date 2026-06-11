import { apiRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể xử lý thông tin lịch làm việc. Vui lòng thử lại.",
    ...init,
  });
}

export function layLichLamViec(tuNgay, denNgay) {
  return request(`/admin/lich-lam-viec?tuNgay=${tuNgay}&denNgay=${denNgay}`);
}

export function phanCa(payload) {
  return request("/admin/lich-lam-viec", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function xepCaTuDong(tuNgay, denNgay) {
  return request(`/admin/lich-lam-viec/auto-assign?tuNgay=${tuNgay}&denNgay=${denNgay}`, {
    method: "POST",
  });
}
