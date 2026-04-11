export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface HoaDonSanPham {
  id: number;
  tenSanPham: string;
  phanLoai: string;
  mauSac: string;
  kichCo: string;
  soLuong: number;
  donGia: number;
  thanhTien: number;
  hinhAnh: string;
}

export interface HoaDonThanhToan {
  id: number;
  maGiaoDich: string;
  loaiGiaoDich: string;
  phuongThucThanhToan: string;
  trangThaiThanhToan: string;
  thoiGian: string;
  tongTien: number;
  ghiChu: string;
}

export interface HoaDonItem {
  id: number;
  maHoaDon: string;
  tenKhachHang: string;
  tenNhanVien: string;
  tongTien: number;
  ngayTao: string;
  loaiDon: "Tại cửa hàng" | "Online";
  trangThai: "Chờ xác nhận" | "Đã xác nhận" | "Chờ vận chuyển" | "Vận chuyển" | "Đã hoàn thành" | "Hủy";
}

export interface HoaDonDetail extends HoaDonItem {
  soDienThoai: string;
  email: string;
  diaChi: string;
  ghiChu: string;
  phiVanChuyen: number;
  voucher: string;
  giamGia: number;
  donViVanChuyen: string;
  maVanDon: string;
  lichSuThanhToan: HoaDonThanhToan[];
  sanPham: HoaDonSanPham[];
  lichSuHoaDon: HoaDonHistory[];
}

export interface HoaDonHistory {
  id: number;
  tenNhanVien: string;
  trangThai: string;
  ngayTao: string;
  ghiChu: string;
}

export interface HoaDonFilters {
  keyword?: string;
  loaiDon?: string;
  trangThai?: string;
  tuNgay?: string;
  denNgay?: string;
}

export interface CapNhatTrangThaiHoaDonPayload {
  trangThai: HoaDonDetail["trangThai"];
  ghiChu?: string;
  donViVanChuyen?: string;
  maVanDon?: string;
}

export interface CapNhatSanPhamHoaDonRequest {
  items: {
    chiTietId: number;
    soLuong: number;
  }[];
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

  const payload = (await response.json()) as ApiResponse<T>;
  if (!response.ok) {
    throw new Error(payload.message || "Không thể kết nối đến máy chủ");
  }
  return payload.data;
}

export function layDanhSachHoaDon(filters?: HoaDonFilters) {
  const params = new URLSearchParams();
  if (filters?.keyword?.trim()) params.set("keyword", filters.keyword.trim());
  if (filters?.loaiDon?.trim()) params.set("loaiDon", filters.loaiDon.trim());
  if (filters?.trangThai?.trim() && filters.trangThai.trim() !== "Tất cả") params.set("trangThai", filters.trangThai.trim());
  if (filters?.tuNgay?.trim()) params.set("tuNgay", filters.tuNgay.trim());
  if (filters?.denNgay?.trim()) params.set("denNgay", filters.denNgay.trim());
  const query = params.toString();
  return request<HoaDonItem[]>(`/admin/hoa-don${query ? `?${query}` : ""}`);
}

export function layChiTietHoaDon(id: number) {
  return request<HoaDonDetail>(`/admin/hoa-don/${id}`);
}

export function capNhatTrangThaiHoaDon(id: number, payload: CapNhatTrangThaiHoaDonPayload) {
  return request<HoaDonDetail>(`/admin/hoa-don/${id}/trang-thai`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
}

export function capNhatSanPhamHoaDon(id: number, payload: CapNhatSanPhamHoaDonRequest) {
  return request<HoaDonDetail>(`/admin/hoa-don/${id}/san-pham`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}
