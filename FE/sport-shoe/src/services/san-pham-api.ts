import { apiRequest, uploadFileRequest } from './api-client'

// ─── Types ───────────────────────────────────────────────────────────────────

export interface LoaiGiayOption { id: number; ten: string }
export interface ThuongHieuOption { id: number; ten: string; logoUrl?: string }
export interface MauSacOption { id: number; ten: string; maMauHex?: string }
export interface KichCoOption { id: number; giaTri: string }
export interface DeGiayOption { id: number; ten: string }
export interface CoGiayOption { id: number; ten: string }
export interface ChatLieuGiayOption { id: number; ten: string }
export interface TrongLuongOption { id: number; ma: string; giaTri: number }
export interface CongNgheDemOption { id: number; ten: string }

export interface DanhMucSanPhamResponse {
  loaiGiay: LoaiGiayOption[]
  thuongHieu: ThuongHieuOption[]
  mauSac: MauSacOption[]
  kichCo: KichCoOption[]
  deGiay: DeGiayOption[]
  coGiay: CoGiayOption[]
  chatLieuGiay: ChatLieuGiayOption[]
  trongLuong: TrongLuongOption[]
  congNgheDem: CongNgheDemOption[]
}

export interface ThuocTinhResponse {
  id: number
  deGiayId?: number; deGiay?: string
  coGiayId?: number; coGiay?: string
  congNgheDemId?: number; congNgheDem?: string
  chatLieuGiayId?: number; chatLieuGiay?: string
  trongLuongId?: number; trongLuong?: string
}

export interface GiayListItem {
  id: number
  ma: string
  ten: string
  loaiGiay: string
  thuongHieu: string
  chatLieu?: string
  deGiay?: string
  coGiay?: string
  congNgheDem?: string
  trongLuong?: string
  gioiTinh?: number
  trangThai: number
  hinhAnh?: string
  giaMin?: number
  giaMax?: number
  giaGocMin?: number
  giaGocMax?: number
  tongBienThe?: number
  tongSoLuong?: number
  ngayTao: string
  coGiamGia?: boolean
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
  hinhAnhs?: HinhAnhGiay[]
  ngayTao: string
  ngayCapNhat?: string
}

export interface TaoChiTietSanPhamResponse {
  giay: GiayDetail
  bienThe: BienThe
  taoMoiSanPham: boolean
}

export interface TaoChiTietSanPhamHangLoatResponse {
  giay: GiayDetail
  bienThes: BienThe[]
  taoMoiSanPham: boolean
}

export interface ChiTietSanPhamListItem {
  id: number
  giayId: number
  maSanPham: string
  maChiTietSanPham: string
  sku: string
  tenSanPham: string
  thuongHieu: string
  loaiGiay: string
  chatLieu?: string
  gioiTinh?: number
  mauSacId: number
  mauSac: string
  maMauHex?: string
  kichCoId: number
  kichCo: string
  soLuong: number
  giaGoc: number
  giaBan: number
  kichHoat: number
  hinhAnh?: string
  ngayTao: string
  ngayCapNhat?: string
  dotGiamGiaId?: number
  maDotGiamGia?: string
  tenDotGiamGia?: string
  loaiGiam?: number
  giaTriGiam?: number
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
  dotGiamGiaId?: number
  maDotGiamGia?: string
  tenDotGiamGia?: string
  loaiGiam?: number
  giaTriGiam?: number
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

export interface ChiTietSanPhamFilters {
  keyword?: string
  giayId?: number | null
  mauSacId?: number | null
  kichCoId?: number | null
  trangThai?: number | null
  page?: number
  size?: number
}

export interface TaoGiayRequest {
  ma?: string
  ten: string
  thuongHieuId: number
  loaiGiayId: number
  gioiTinh?: number
  chatLieu?: string
  chatLieuGiayId?: number
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
  chatLieuGiayId?: number
  moTa?: string
  deGiayId?: number
  coGiayId?: number
  congNgheDemId?: number
  trongLuongId?: number
}

export interface TaoChiTietSanPhamRequest {
  giayId?: number
  ma?: string
  ten?: string
  thuongHieuId?: number
  loaiGiayId?: number
  gioiTinh?: number
  chatLieu?: string
  chatLieuGiayId?: number
  moTa?: string
  deGiayId?: number
  coGiayId?: number
  congNgheDemId?: number
  trongLuongId?: number
  mauSacId: number
  kichCoId: number
  soLuong: number
  giaGoc: number
  giaBan: number
}

export interface TaoChiTietSanPhamHangLoatItemRequest {
  mauSacId: number
  kichCoId: number
  soLuong: number
  giaGoc: number
  giaBan: number
}

export interface TaoChiTietSanPhamHangLoatRequest {
  giayId?: number
  ma?: string
  ten?: string
  thuongHieuId?: number
  loaiGiayId?: number
  gioiTinh?: number
  chatLieu?: string
  chatLieuGiayId?: number
  moTa?: string
  deGiayId?: number
  coGiayId?: number
  congNgheDemId?: number
  trongLuongId?: number
  bienThes: TaoChiTietSanPhamHangLoatItemRequest[]
}

export interface DoiTrangThaiRequest { trangThai: number }
export interface DoiTrangThaiBienTheRequest { kichHoat: number }

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

export interface CapNhatHinhAnhRequest {
  url: string
  loaiHinh?: number
  moTa?: string
}

// ─── API functions ────────────────────────────────────────────────────────────

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  return apiRequest(path, {
    fallbackMessage:
      'Không thể hoàn tất thao tác sản phẩm lúc này. Vui lòng thử lại.',
    ...init,
  })
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

export function checkTenGiay(ten: string, id?: number | null): Promise<{ exists: boolean }> {
  const params = new URLSearchParams()
  params.set('ten', ten)
  if (id != null) params.set('id', String(id))
  return request<{ exists: boolean }>(`/admin/san-pham/check-ten?${params}`)
}

export function chiTietGiay(id: number): Promise<GiayDetail> {
  return request<GiayDetail>(`/admin/san-pham/${id}`)
}

export function layDanhSachChiTietSanPham(
  filters: ChiTietSanPhamFilters = {}
): Promise<PageResponse<ChiTietSanPhamListItem>> {
  const params = new URLSearchParams()
  if (filters.keyword) params.set('keyword', filters.keyword)
  if (filters.giayId != null) params.set('giayId', String(filters.giayId))
  if (filters.mauSacId != null) params.set('mauSacId', String(filters.mauSacId))
  if (filters.kichCoId != null) params.set('kichCoId', String(filters.kichCoId))
  if (filters.trangThai != null) params.set('trangThai', String(filters.trangThai))
  params.set('page', String(filters.page ?? 0))
  params.set('size', String(filters.size ?? 10))
  return request<PageResponse<ChiTietSanPhamListItem>>(`/admin/san-pham/chi-tiet?${params}`)
}

export function taoGiay(body: TaoGiayRequest): Promise<GiayDetail> {
  return request<GiayDetail>('/admin/san-pham', { method: 'POST', body: JSON.stringify(body) })
}

export function taoChiTietSanPham(body: TaoChiTietSanPhamRequest): Promise<TaoChiTietSanPhamResponse> {
  return request<TaoChiTietSanPhamResponse>('/admin/san-pham/chi-tiet', { method: 'POST', body: JSON.stringify(body) })
}

export function taoChiTietSanPhamHangLoat(
  body: TaoChiTietSanPhamHangLoatRequest
): Promise<TaoChiTietSanPhamHangLoatResponse> {
  return request<TaoChiTietSanPhamHangLoatResponse>(
    '/admin/san-pham/chi-tiet-hang-loat',
    { method: 'POST', body: JSON.stringify(body) }
  )
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

export function doiTrangThaiBienThe(id: number, kichHoat: number): Promise<BienThe> {
  return request<BienThe>(`/admin/san-pham/bien-the/${id}/trang-thai`, {
    method: 'PATCH',
    body: JSON.stringify({ kichHoat })
  })
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

export function capNhatHinhAnh(id: number, body: CapNhatHinhAnhRequest): Promise<HinhAnhGiay> {
  return request<HinhAnhGiay>(`/admin/san-pham/hinh-anh/${id}`, { method: 'PUT', body: JSON.stringify(body) })
}

export function xoaHinhAnh(id: number): Promise<void> {
  return request<void>(`/admin/san-pham/hinh-anh/${id}`, { method: 'DELETE' })
}

export function datHinhChinh(id: number): Promise<void> {
  return request<void>(`/admin/san-pham/hinh-anh/${id}/chinh`, { method: 'PATCH' })
}

export async function uploadFile(file: File): Promise<string> {
  return uploadFileRequest(file, 'Không thể tải ảnh sản phẩm lên lúc này') as Promise<string>
}
