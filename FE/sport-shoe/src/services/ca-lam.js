import { apiRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể xử lý thông tin ca làm việc. Vui lòng thử lại.",
    ...init,
  });
}

export function normalizeShiftName(id, name) {
  if (!name) return name;
  const str = String(name).trim();
  const idStr = String(id || "").toLowerCase();
  if (idStr === "chieu" || str.toLowerCase().includes("chi?u") || str.toLowerCase() === "ca chieu") {
    return "Ca chiều";
  }
  if (idStr === "toi" || str.toLowerCase().includes("t?i") || str.toLowerCase() === "ca toi") {
    return "Ca tối";
  }
  if (idStr === "sang" || str.toLowerCase().includes("s?ng") || str.toLowerCase() === "ca sang") {
    return "Ca sáng";
  }
  return str
    .replace(/chi\?u/gi, "chiều")
    .replace(/t\?i/gi, "tối")
    .replace(/s\?ng/gi, "sáng")
    .replace(/l\?m/gi, "làm");
}

export async function layDanhSachCaLam() {
  const data = await request("/admin/ca-lam");
  if (Array.isArray(data)) {
    return data.map((item) => ({
      ...item,
      ten: normalizeShiftName(item.id, item.ten),
    }));
  }
  return data;
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

export function doiTrangThaiCaLam(id, trangThai) {
  return request(`/admin/ca-lam/${id}/trang-thai`, {
    method: "PATCH",
    body: JSON.stringify({ trangThai }),
  });
}

export function xoaCaLam(id) {
  return request(`/admin/ca-lam/${id}`, {
    method: "DELETE",
  });
}
