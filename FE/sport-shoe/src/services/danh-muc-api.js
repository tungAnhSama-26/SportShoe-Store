import { apiRequest, uploadFileRequest } from "./api-client";
const BASE = "/admin/danh-muc";

// ─── Entity types ─────────────────────────────────────────────────────────────

// ─── Generic request helper ───────────────────────────────────────────────────

async function req(path, init) {
  return apiRequest(`${BASE}${path}`, {
    fallbackMessage:
      "Không thể hoàn tất thao tác danh mục lúc này. Vui lòng thử lại.",
    ...init,
  });
}

function buildListUrl(path, kw, page = 0, size = 10) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  if (kw) params.set("keyword", kw);
  return `${path}?${params}`;
}

// ─── API objects ──────────────────────────────────────────────────────────────

export const loaiGiayApi = {
  list: (kw, page = 0, size = 10) =>
    req(buildListUrl("/loai-giay", kw, page, size)),
  create: (body) =>
    req("/loai-giay", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) =>
    req(`/loai-giay/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  toggleStatus: (id, trangThai) =>
    req(`/loai-giay/${id}/trang-thai`, {
      method: "PATCH",
      body: JSON.stringify({ trangThai }),
    }),
  delete: (id) => req(`/loai-giay/${id}`, { method: "DELETE" }),
};

export const thuongHieuApi = {
  list: (kw, page = 0, size = 10) =>
    req(buildListUrl("/thuong-hieu", kw, page, size)),
  create: (body) =>
    req("/thuong-hieu", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) =>
    req(`/thuong-hieu/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  toggleStatus: (id, trangThai) =>
    req(`/thuong-hieu/${id}/trang-thai`, {
      method: "PATCH",
      body: JSON.stringify({ trangThai }),
    }),
  delete: (id) => req(`/thuong-hieu/${id}`, { method: "DELETE" }),
  uploadFile: async (file) => {
    return uploadFileRequest(file, "Không thể tải ảnh thương hiệu lên lúc này");
  },
};

export const deGiayApi = {
  list: (kw, page = 0, size = 10) =>
    req(buildListUrl("/de-giay", kw, page, size)),
  create: (body) =>
    req("/de-giay", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) =>
    req(`/de-giay/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  toggleStatus: (id, trangThai) =>
    req(`/de-giay/${id}/trang-thai`, {
      method: "PATCH",
      body: JSON.stringify({ trangThai }),
    }),
  delete: (id) => req(`/de-giay/${id}`, { method: "DELETE" }),
};

export const chatLieuGiayApi = {
  list: (kw, page = 0, size = 10) =>
    req(buildListUrl("/chat-lieu-giay", kw, page, size)),
  create: (body) =>
    req("/chat-lieu-giay", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) =>
    req(`/chat-lieu-giay/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  toggleStatus: (id, trangThai) =>
    req(`/chat-lieu-giay/${id}/trang-thai`, {
      method: "PATCH",
      body: JSON.stringify({ trangThai }),
    }),
  delete: (id) => req(`/chat-lieu-giay/${id}`, { method: "DELETE" }),
};

export const coGiayApi = {
  list: (kw, page = 0, size = 10) =>
    req(buildListUrl("/co-giay", kw, page, size)),
  create: (body) =>
    req("/co-giay", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) =>
    req(`/co-giay/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  toggleStatus: (id, trangThai) =>
    req(`/co-giay/${id}/trang-thai`, {
      method: "PATCH",
      body: JSON.stringify({ trangThai }),
    }),
  delete: (id) => req(`/co-giay/${id}`, { method: "DELETE" }),
};

export const congNgheDemApi = {
  list: (kw, page = 0, size = 10) =>
    req(buildListUrl("/cong-nghe-dem", kw, page, size)),
  create: (body) =>
    req("/cong-nghe-dem", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) =>
    req(`/cong-nghe-dem/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  toggleStatus: (id, trangThai) =>
    req(`/cong-nghe-dem/${id}/trang-thai`, {
      method: "PATCH",
      body: JSON.stringify({ trangThai }),
    }),
  delete: (id) => req(`/cong-nghe-dem/${id}`, { method: "DELETE" }),
};

export const mauSacApi = {
  list: (kw, page = 0, size = 10) =>
    req(buildListUrl("/mau-sac", kw, page, size)),
  create: (body) =>
    req("/mau-sac", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) =>
    req(`/mau-sac/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  toggleStatus: (id, trangThai) =>
    req(`/mau-sac/${id}/trang-thai`, {
      method: "PATCH",
      body: JSON.stringify({ trangThai }),
    }),
  delete: (id) => req(`/mau-sac/${id}`, { method: "DELETE" }),
};

export const kichCoApi = {
  list: (kw, page = 0, size = 10) =>
    req(buildListUrl("/kich-co", kw, page, size)),
  create: (body) =>
    req("/kich-co", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) =>
    req(`/kich-co/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  toggleStatus: (id, trangThai) =>
    req(`/kich-co/${id}/trang-thai`, {
      method: "PATCH",
      body: JSON.stringify({ trangThai }),
    }),
  delete: (id) => req(`/kich-co/${id}`, { method: "DELETE" }),
};

export const trongLuongApi = {
  list: (kw, page = 0, size = 10) =>
    req(buildListUrl("/trong-luong", kw, page, size)),
  create: (body) =>
    req("/trong-luong", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) =>
    req(`/trong-luong/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  toggleStatus: (id, trangThai) =>
    req(`/trong-luong/${id}/trang-thai`, {
      method: "PATCH",
      body: JSON.stringify({ trangThai }),
    }),
  delete: (id) => req(`/trong-luong/${id}`, { method: "DELETE" }),
};
