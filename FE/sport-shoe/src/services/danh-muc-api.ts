import { apiRequest, uploadFileRequest } from './api-client'
const BASE = '/admin/danh-muc'

export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
  first: boolean
  last: boolean
}

// ─── Entity types ─────────────────────────────────────────────────────────────

export interface LoaiGiayItem {
  id: number; ma: string; ten: string; moTa?: string
  trangThai: number; ngayTao: string; ngayCapNhat?: string
}
export interface ThuongHieuItem {
  id: number; ma: string; ten: string; xuatXu?: string
  logoUrl?: string; website?: string; moTa?: string
  trangThai: number; ngayTao: string; ngayCapNhat?: string
}
export interface DeGiayItem {
  id: number; ma: string; ten: string; moTa?: string
  trangThai: number; ngayTao: string; ngayCapNhat?: string
}
export interface ChatLieuGiayItem {
  id: number; ma: string; ten: string; moTa?: string
  trangThai: number; ngayTao: string; ngayCapNhat?: string
}
export interface CoGiayItem {
  id: number; ma: string; ten: string; moTa?: string
  trangThai: number; ngayTao: string; ngayCapNhat?: string
}
export interface CongNgheDemItem {
  id: number; ma: string; ten: string; moTa?: string
  trangThai: number; ngayTao: string; ngayCapNhat?: string
}
export interface MauSacItem {
  id: number; ma: string; ten: string; maMauHex?: string
  trangThai: number; ngayTao: string; ngayCapNhat?: string
}
export interface KichCoItem {
  id: number; giaTri: string; ghiChu?: string
  trangThai: number; ngayTao: string; ngayCapNhat?: string
}
export interface TrongLuongItem {
  id: number; ma: string; giaTri: number; moTa?: string
  trangThai: number; ngayTao: string; ngayCapNhat?: string
}

// ─── Generic request helper ───────────────────────────────────────────────────

async function req<T>(path: string, init?: RequestInit): Promise<T> {
  return apiRequest(`${BASE}${path}`, {
    fallbackMessage:
      'Không thể hoàn tất thao tác danh mục lúc này. Vui lòng thử lại.',
    ...init,
  })
}

function buildListUrl(path: string, kw?: string, page = 0, size = 10) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (kw) params.set('keyword', kw)
  return `${path}?${params}`
}

// ─── API objects ──────────────────────────────────────────────────────────────

export const loaiGiayApi = {
  list: (kw?: string, page = 0, size = 10) =>
    req<PageResponse<LoaiGiayItem>>(buildListUrl('/loai-giay', kw, page, size)),
  create: (body: object) => req<LoaiGiayItem>('/loai-giay', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: object) => req<LoaiGiayItem>(`/loai-giay/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  toggleStatus: (id: number, trangThai: number) =>
    req<void>(`/loai-giay/${id}/trang-thai`, { method: 'PATCH', body: JSON.stringify({ trangThai }) }),
  delete: (id: number) => req<void>(`/loai-giay/${id}`, { method: 'DELETE' }),
}

export const thuongHieuApi = {
  list: (kw?: string, page = 0, size = 10) =>
    req<PageResponse<ThuongHieuItem>>(buildListUrl('/thuong-hieu', kw, page, size)),
  create: (body: object) => req<ThuongHieuItem>('/thuong-hieu', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: object) => req<ThuongHieuItem>(`/thuong-hieu/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  toggleStatus: (id: number, trangThai: number) =>
    req<void>(`/thuong-hieu/${id}/trang-thai`, { method: 'PATCH', body: JSON.stringify({ trangThai }) }),
  delete: (id: number) => req<void>(`/thuong-hieu/${id}`, { method: 'DELETE' }),
  uploadFile: async (file: File) => {
    return uploadFileRequest(file, 'Không thể tải ảnh thương hiệu lên lúc này') as Promise<string>
  }
}

export const deGiayApi = {
  list: (kw?: string, page = 0, size = 10) =>
    req<PageResponse<DeGiayItem>>(buildListUrl('/de-giay', kw, page, size)),
  create: (body: object) => req<DeGiayItem>('/de-giay', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: object) => req<DeGiayItem>(`/de-giay/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  toggleStatus: (id: number, trangThai: number) =>
    req<void>(`/de-giay/${id}/trang-thai`, { method: 'PATCH', body: JSON.stringify({ trangThai }) }),
  delete: (id: number) => req<void>(`/de-giay/${id}`, { method: 'DELETE' }),
}

export const chatLieuGiayApi = {
  list: (kw?: string, page = 0, size = 10) =>
    req<PageResponse<ChatLieuGiayItem>>(buildListUrl('/chat-lieu-giay', kw, page, size)),
  create: (body: object) => req<ChatLieuGiayItem>('/chat-lieu-giay', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: object) => req<ChatLieuGiayItem>(`/chat-lieu-giay/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  toggleStatus: (id: number, trangThai: number) =>
    req<void>(`/chat-lieu-giay/${id}/trang-thai`, { method: 'PATCH', body: JSON.stringify({ trangThai }) }),
  delete: (id: number) => req<void>(`/chat-lieu-giay/${id}`, { method: 'DELETE' }),
}

export const coGiayApi = {
  list: (kw?: string, page = 0, size = 10) =>
    req<PageResponse<CoGiayItem>>(buildListUrl('/co-giay', kw, page, size)),
  create: (body: object) => req<CoGiayItem>('/co-giay', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: object) => req<CoGiayItem>(`/co-giay/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  toggleStatus: (id: number, trangThai: number) =>
    req<void>(`/co-giay/${id}/trang-thai`, { method: 'PATCH', body: JSON.stringify({ trangThai }) }),
  delete: (id: number) => req<void>(`/co-giay/${id}`, { method: 'DELETE' }),
}

export const congNgheDemApi = {
  list: (kw?: string, page = 0, size = 10) =>
    req<PageResponse<CongNgheDemItem>>(buildListUrl('/cong-nghe-dem', kw, page, size)),
  create: (body: object) => req<CongNgheDemItem>('/cong-nghe-dem', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: object) => req<CongNgheDemItem>(`/cong-nghe-dem/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  toggleStatus: (id: number, trangThai: number) =>
    req<void>(`/cong-nghe-dem/${id}/trang-thai`, { method: 'PATCH', body: JSON.stringify({ trangThai }) }),
  delete: (id: number) => req<void>(`/cong-nghe-dem/${id}`, { method: 'DELETE' }),
}

export const mauSacApi = {
  list: (kw?: string, page = 0, size = 10) =>
    req<PageResponse<MauSacItem>>(buildListUrl('/mau-sac', kw, page, size)),
  create: (body: object) => req<MauSacItem>('/mau-sac', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: object) => req<MauSacItem>(`/mau-sac/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  toggleStatus: (id: number, trangThai: number) =>
    req<void>(`/mau-sac/${id}/trang-thai`, { method: 'PATCH', body: JSON.stringify({ trangThai }) }),
  delete: (id: number) => req<void>(`/mau-sac/${id}`, { method: 'DELETE' }),
}

export const kichCoApi = {
  list: (kw?: string, page = 0, size = 10) =>
    req<PageResponse<KichCoItem>>(buildListUrl('/kich-co', kw, page, size)),
  create: (body: object) => req<KichCoItem>('/kich-co', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: object) => req<KichCoItem>(`/kich-co/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  toggleStatus: (id: number, trangThai: number) =>
    req<void>(`/kich-co/${id}/trang-thai`, { method: 'PATCH', body: JSON.stringify({ trangThai }) }),
  delete: (id: number) => req<void>(`/kich-co/${id}`, { method: 'DELETE' }),
}

export const trongLuongApi = {
  list: (kw?: string, page = 0, size = 10) =>
    req<PageResponse<TrongLuongItem>>(buildListUrl('/trong-luong', kw, page, size)),
  create: (body: object) => req<TrongLuongItem>('/trong-luong', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: object) => req<TrongLuongItem>(`/trong-luong/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  toggleStatus: (id: number, trangThai: number) =>
    req<void>(`/trong-luong/${id}/trang-thai`, { method: 'PATCH', body: JSON.stringify({ trangThai }) }),
  delete: (id: number) => req<void>(`/trong-luong/${id}`, { method: 'DELETE' }),
}
