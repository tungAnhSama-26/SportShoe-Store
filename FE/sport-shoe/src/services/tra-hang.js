import { apiRequest, buildQuery } from "./api-client";

const fallbackMessage =
  "Không thể hoàn tất thao tác trả hàng lúc này. Vui lòng thử lại.";

function request(path, init) {
  return apiRequest(path, {
    fallbackMessage,
    ...init,
  });
}

export function layDanhSachTraHang(filters = {}) {
  return request(`/admin/tra-hang${buildQuery(filters)}`);
}

export function layChiTietTraHang(id) {
  return request(`/admin/tra-hang/${id}`);
}

export function taoPhieuTraHang(payload) {
  return request("/admin/tra-hang", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function duyetPhieuTraHang(id, payload) {
  return request(`/admin/tra-hang/${id}/duyet`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

export function xacNhanGuiHangTra(id, payload) {
  return request(`/admin/tra-hang/${id}/xac-nhan-gui-hang`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

export function xacNhanNhanHangTra(id, payload) {
  return request(`/admin/tra-hang/${id}/xac-nhan-da-nhan`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

export function danhDauHoanHangThatBai(id, payload = {}) {
  return request(`/admin/tra-hang/${id}/hoan-hang-that-bai`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

export function batDauKiemTraHang(id, payload = {}) {
  return request(`/admin/tra-hang/${id}/bat-dau-kiem-tra`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

export function capNhatKiemTraHang(id, payload) {
  return request(`/admin/tra-hang/${id}/kiem-tra`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

export function tuChoiTraHang(id, payload) {
  return request(`/admin/tra-hang/${id}/tu-choi`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

export function huyPhieuTraHang(id, payload = {}) {
  return request(`/admin/tra-hang/${id}/huy`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

export function hoanTienTraHang(id, payload) {
  return request(`/admin/tra-hang/${id}/hoan-tien`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
