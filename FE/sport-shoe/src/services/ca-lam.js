import { apiRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể xử lý thông tin ca làm việc. Vui lòng thử lại.",
    ...init,
  });
}

export function layDanhSachCaLam() {
  return request("/admin/ca-lam");
}

export function taoCaLam(payload) {
  return request("/admin/ca-lam", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function capNhatCaLam(id, payload) {
  return request(`/admin/ca-lam/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function xoaCaLam(id) {
  return request(`/admin/ca-lam/${id}`, {
    method: "DELETE",
  });
}
