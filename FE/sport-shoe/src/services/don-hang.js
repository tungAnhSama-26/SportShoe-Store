import { apiRequest } from "./api-client";
import { layKhachId } from "./gio-hang";

// Danh sách đơn hàng của khách đang đăng nhập.
export async function layDonHangCuaToi() {
  const id = layKhachId();
  if (!id) return [];
  const data = await apiRequest(`/client/don-hang?khachHangId=${id}`, {
    authenticated: false,
    fallbackMessage: "Không thể tải đơn hàng",
  });
  return Array.isArray(data) ? data : [];
}

// Chi tiết 1 đơn hàng (sản phẩm, địa chỉ, phân tích giá).
export async function layChiTietDonHang(donId) {
  const id = layKhachId();
  return apiRequest(`/client/don-hang/${donId}?khachHangId=${id}`, {
    authenticated: false,
    fallbackMessage: "Không thể tải chi tiết đơn hàng",
  });
}

// Khách xác nhận đã nhận hàng.
export async function xacNhanDaNhanHang(donId) {
  const id = layKhachId();
  return apiRequest(`/client/don-hang/${donId}/da-nhan-hang?khachHangId=${id}`, {
    method: "POST",
    authenticated: false,
    fallbackMessage: "Không thể xác nhận nhận hàng",
  });
}

// Gửi đánh giá cho một sản phẩm trong đơn (theo dòng hóa đơn chi tiết).
export async function guiDanhGiaSanPham(hoaDonChiTietId, soSao, noiDung) {
  const id = layKhachId();
  return apiRequest(`/client/danh-gia`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id, hoaDonChiTietId, soSao, noiDung }),
    fallbackMessage: "Không thể gửi đánh giá",
  });
}
