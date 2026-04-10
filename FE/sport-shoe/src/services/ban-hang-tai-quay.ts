export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface ApiErrorResponse {
  code?: string;
  message?: string;
  errors?: Record<string, string>;
  timestamp?: string;
}

export interface KhachHangTaiQuay {
  id: string;
  hoTen: string;
  sdt: string;
  email?: string | null;
}

export interface SanPhamTaiQuay {
  chiTietId: number;
  maSanPham: string;
  tenSanPham: string;
  sku: string;
  maBienThe: string;
  soLuongTon: number;
  giaBan: number;
  loaiGiay?: string | null;
  thuongHieu?: string | null;
  deGiay?: string | null;
  coGiay?: string | null;
  congNgheDem?: string | null;
  mauSac?: string | null;
  kichCo?: string | null;
  trongLuong?: string | null;
}

export interface HoaDonChoTomTat {
  id: number;
  ma: string;
  tenKhachHang: string;
  soDienThoai: string;
  tongSanPham: number;
  tongTien: number;
  ngayTao: string;
}

export interface HoaDonChoChiTietItem {
  chiTietId: number;
  maSanPham: string;
  tenSanPham: string;
  soLuong: number;
  giaBan: number;
  thanhTien: number;
}

export interface HoaDonChoChiTiet {
  id: number;
  ma: string;
  tenKhachHang: string;
  soDienThoai: string;
  tongTien: number;
  ngayTao: string;
  items: HoaDonChoChiTietItem[];
}

export interface TaoHoaDonChoPayload {
  khachHangId: string | null;
  tenKhachHang: string;
  soDienThoai: string;
  items: Array<{
    chiTietId: number;
    soLuong: number;
  }>;
}

export interface ThanhToanTaiQuayPayload {
  hoaDonId: number | null;
  khachHangId: string | null;
  tenKhachHang: string;
  soDienThoai: string;
  hinhThucThanhToan: number;
  tienKhachDua: number;
  ghiChu?: string;
  items: Array<{
    chiTietId: number;
    soLuong: number;
  }>;
}

export interface ThanhToanTaiQuayResponse {
  hoaDonId: number;
  maHoaDon: string;
  tongTien: number;
  tienKhachDua: number;
  tienThua: number;
  hinhThucThanhToan: number;
  tenKhachHang: string;
  soDienThoai: string;
  ngayThanhToan: string;
}

const API_BASE_URL =
  (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, "") ??
  "http://localhost:8080/api/v1";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  const payload = (await response.json()) as ApiResponse<T> | ApiErrorResponse;

  if (!response.ok) {
    throw new Error(payload.message || "Khong the ket noi den may chu");
  }

  return (payload as ApiResponse<T>).data;
}

export function timKhachHangTheoSoDienThoai(phone: string) {
  const params = new URLSearchParams();
  if (phone.trim()) {
    params.set("phone", phone.trim());
  }
  return request<KhachHangTaiQuay[]>(`/admin/ban-hang-tai-quay/khach-hang?${params.toString()}`);
}

export function timSanPhamTaiQuay(keyword: string) {
  const params = new URLSearchParams();
  if (keyword.trim()) {
    params.set("keyword", keyword.trim());
  }
  return request<SanPhamTaiQuay[]>(`/admin/ban-hang-tai-quay/san-pham?${params.toString()}`);
}

export function layDanhSachHoaDonCho() {
  return request<HoaDonChoTomTat[]>("/admin/ban-hang-tai-quay/hoa-don-cho");
}

export function layChiTietHoaDonCho(id: number) {
  return request<HoaDonChoChiTiet>(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}`);
}

export function taoHoaDonCho(payload: TaoHoaDonChoPayload) {
  return request<HoaDonChoChiTiet>("/admin/ban-hang-tai-quay/hoa-don-cho", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function thanhToanTaiQuay(payload: ThanhToanTaiQuayPayload) {
  return request<ThanhToanTaiQuayResponse>("/admin/ban-hang-tai-quay/thanh-toan", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function huyHoaDonCho(id: number) {
  return request<void>(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}/huy`, {
    method: "PATCH",
  });
}
