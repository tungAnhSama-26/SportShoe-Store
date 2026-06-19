import { apiRequest } from "./api-client";
import { layKhachId } from "./gio-hang";

// Danh sách đơn hàng của khách đang đăng nhập.
export async function layDonHangCuaToi() {
  if (!layKhachId()) return [];
  const data = await apiRequest("/client/don-hang", {
    authScope: "customer",
    fallbackMessage: "Không thể tải đơn hàng",
  });
  return Array.isArray(data) ? data : [];
}

// Chi tiết 1 đơn hàng (sản phẩm, địa chỉ, phân tích giá).
export async function layChiTietDonHang(donId) {
  return apiRequest(`/client/don-hang/${donId}`, {
    authScope: "customer",
    fallbackMessage: "Không thể tải chi tiết đơn hàng",
  });
}

// Khách xác nhận đã nhận hàng.
export async function xacNhanDaNhanHang(donId) {
  return apiRequest(`/client/don-hang/${donId}/da-nhan-hang`, {
    method: "POST",
    authScope: "customer",
    fallbackMessage: "Không thể xác nhận nhận hàng",
  });
}

export async function yeuCauHuyDonHang(donId) {
  return apiRequest(`/client/don-hang/${donId}/yeu-cau-huy`, {
    method: "POST",
    authScope: "customer",
    fallbackMessage: "Không thể hủy đơn hàng",
  });
}

export async function capNhatThongTinGiaoHang(donId, payload) {
  return apiRequest(`/client/don-hang/${donId}/thong-tin-giao-hang`, {
    method: "PUT",
    authScope: "customer",
    body: JSON.stringify(payload),
    fallbackMessage: "Không thể cập nhật thông tin giao hàng",
  });
}

// Khách cập nhật số lượng sản phẩm (chỉ COD + đang chờ xác nhận).
// items: [{ hoaDonChiTietId, soLuong }] - các dòng giữ lại (số lượng >= 1).
export async function capNhatSoLuongDonHang(donId, items) {
  return apiRequest(`/client/don-hang/${donId}/so-luong`, {
    method: "PUT",
    authScope: "customer",
    body: JSON.stringify({ items }),
    fallbackMessage: "Không thể cập nhật số lượng sản phẩm",
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
