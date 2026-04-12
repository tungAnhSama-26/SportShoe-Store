const BASE = 'http://localhost:8080/api/v1'

// ─── Types ───────────────────────────────────────────────────────────────────

export interface LoaiGiayOption { id: number; ten: string }
export interface ThuongHieuOption { id: number; ten: string; logoUrl?: string }
export interface MauSacOption { id: number; ten: string; maMauHex?: string }
export interface KichCoOption { id: number; giaTri: string }
export interface DeGiayOption { id: number; ten: string }
export interface CoGiayOption { id: number; ten: string }
export interface TrongLuongOption { id: number; ma: string; giaTri: number }
export interface CongNgheDemOption { id: number; ten: string }

export interface DanhMucSanPhamResponse {
  loaiGiay: LoaiGiayOption[]
  thuongHieu: ThuongHieuOption[]
  mauSac: MauSacOption[]
  kichCo: KichCoOption[]
  deGiay: DeGiayOption[]
  coGiay: CoGiayOption[]
  trongLuong: TrongLuongOption[]
  congNgheDem: CongNgheDemOption[]
}

export interface ThuocTinhResponse {
  id: number
  deGiayId?: number; deGiay?: string
  coGiayId?: number; coGiay?: string
  congNgheDemId?: number; congNgheDem?: string
  trongLuongId?: number; trongLuong?: string
}

export interface GiayListItem {
  id: number
  ma: string
  ten: string
  loaiGiay: string
  thuongHieu: string
  gioiTinh?: number
  trangThai: number
  hinhAnh?: string
  giaMin?: number
  giaMax?: number
  tongBienThe?: number
  tongSoLuong?: number
  ngayTao: string
}

export interface GiayDetail {
  id: number
  ma: string
  ten: string
  gioiTinh?: number
  thuongHieuId: number
  thuongHieu: string
  loaiGiayId: number
  loaiGiay: string
  chatLieu?: string
  moTa?: string
  trangThai: number
  thuocTinh?: ThuocTinhResponse
  ngayTao: string
  ngayCapNhat?: string
}

export interface BienThe {
  id: number
  maBienThe: string
  sku: string
  soLuong: number
  giaGoc: number
  giaBan: number
  kichHoat: number
  mauSacId: number
  mauSac: string
  maMauHex?: string
  kichCoId: number
  kichCo: string
  ngayTao: string
  ngayCapNhat?: string
}

export interface HinhAnhGiay {
  id: number
  loaiHinh: number
  url: string
  moTa?: string
  laHinhChinh: boolean
  trangThai: number
  ngayTao: string
}

export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface GiayListFilters {
  keyword?: string
  thuongHieuId?: number | null
  loaiGiayId?: number | null
  trangThai?: number | null
  minPrice?: number | null
  maxPrice?: number | null
  page?: number
  size?: number
}

export interface TaoGiayRequest {
  ma: string
  ten: string
  thuongHieuId: number
  loaiGiayId: number
  gioiTinh?: number
  chatLieu?: string
  moTa?: string
  deGiayId?: number
  coGiayId?: number
  congNgheDemId?: number
  trongLuongId?: number
}

export interface CapNhatGiayRequest {
  ten: string
  thuongHieuId: number
  loaiGiayId: number
  gioiTinh?: number
  chatLieu?: string
  moTa?: string
  deGiayId?: number
  coGiayId?: number
  congNgheDemId?: number
  trongLuongId?: number
}

export interface DoiTrangThaiRequest { trangThai: number }

export interface TaoBienTheRequest {
  mauSacId: number
  kichCoId: number
  soLuong: number
  giaGoc: number
  giaBan: number
}

export interface CapNhatBienTheRequest {
  soLuong: number
  giaGoc: number
  giaBan: number
  kichHoat: number
}

export interface ThemHinhAnhRequest {
  url: string
  loaiHinh?: number
  moTa?: string
}

// ─── API functions ────────────────────────────────────────────────────────────

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || `HTTP ${res.status}`)
  }
  const json = await res.json()
  return json.data
}

export function layDanhSachGiay(filters: GiayListFilters = {}): Promise<PageResponse<GiayListItem>> {
  const params = new URLSearchParams()
  if (filters.keyword) params.set('keyword', filters.keyword)
  if (filters.thuongHieuId != null) params.set('thuongHieuId', String(filters.thuongHieuId))
  if (filters.loaiGiayId != null) params.set('loaiGiayId', String(filters.loaiGiayId))
  if (filters.trangThai != null) params.set('trangThai', String(filters.trangThai))
  if (filters.minPrice != null) params.set('minPrice', String(filters.minPrice))
  if (filters.maxPrice != null) params.set('maxPrice', String(filters.maxPrice))
  params.set('page', String(filters.page ?? 0))
  params.set('size', String(filters.size ?? 10))
  return request<PageResponse<GiayListItem>>(`/admin/san-pham?${params}`)
}

export function layDanhMuc(): Promise<DanhMucSanPhamResponse> {
  return request<DanhMucSanPhamResponse>('/admin/san-pham/danh-muc')
}

export function chiTietGiay(id: number): Promise<GiayDetail> {
  return request<GiayDetail>(`/admin/san-pham/${id}`)
}

export function taoGiay(body: TaoGiayRequest): Promise<GiayDetail> {
  return request<GiayDetail>('/admin/san-pham', { method: 'POST', body: JSON.stringify(body) })
}

export function capNhatGiay(id: number, body: CapNhatGiayRequest): Promise<GiayDetail> {
  return request<GiayDetail>(`/admin/san-pham/${id}`, { method: 'PUT', body: JSON.stringify(body) })
}

export function doiTrangThai(id: number, trangThai: number): Promise<void> {
  return request<void>(`/admin/san-pham/${id}/trang-thai`, { method: 'PATCH', body: JSON.stringify({ trangThai }) })
}

export function xoaGiay(id: number): Promise<void> {
  return request<void>(`/admin/san-pham/${id}`, { method: 'DELETE' })
}

// Biến thể
export function layBienThe(giayId: number): Promise<BienThe[]> {
  return request<BienThe[]>(`/admin/san-pham/${giayId}/bien-the`)
}

export function taoBienThe(giayId: number, body: TaoBienTheRequest): Promise<BienThe> {
  return request<BienThe>(`/admin/san-pham/${giayId}/bien-the`, { method: 'POST', body: JSON.stringify(body) })
}

export function capNhatBienThe(id: number, body: CapNhatBienTheRequest): Promise<BienThe> {
  return request<BienThe>(`/admin/san-pham/bien-the/${id}`, { method: 'PUT', body: JSON.stringify(body) })
}

export function xoaBienThe(id: number): Promise<void> {
  return request<void>(`/admin/san-pham/bien-the/${id}`, { method: 'DELETE' })
}

// Hình ảnh
export function layHinhAnh(chiTietId: number): Promise<HinhAnhGiay[]> {
  return request<HinhAnhGiay[]>(`/admin/san-pham/bien-the/${chiTietId}/hinh-anh`)
}

export function themHinhAnh(chiTietId: number, body: ThemHinhAnhRequest): Promise<HinhAnhGiay> {
  return request<HinhAnhGiay>(`/admin/san-pham/bien-the/${chiTietId}/hinh-anh`, { method: 'POST', body: JSON.stringify(body) })
}

export function xoaHinhAnh(id: number): Promise<void> {
  return request<void>(`/admin/san-pham/hinh-anh/${id}`, { method: 'DELETE' })
}

export function datHinhChinh(id: number): Promise<void> {
  return request<void>(`/admin/san-pham/hinh-anh/${id}/chinh`, { method: 'PATCH' })
}
