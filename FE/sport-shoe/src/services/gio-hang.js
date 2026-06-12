import { apiRequest, API_BASE_URL } from "./api-client";

// Lấy id khách hàng từ phiên đăng nhập (giỏ hàng server-side cần đăng nhập).
export function layKhachId() {
  try {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw)?.id ?? null : null;
  } catch {
    return null;
  }
}

const GIO_RONG = { id: null, items: [], tongSoLuong: 0, tongTien: 0 };

export async function layGioHang() {
  const id = layKhachId();
  if (!id) return GIO_RONG;
  return apiRequest(`/client/gio-hang?khachHangId=${id}`, {
    authenticated: false,
    fallbackMessage: "Không thể tải giỏ hàng",
  });
}

export async function themVaoGio(giayChiTietId, soLuong = 1) {
  const id = layKhachId();
  if (!id) throw new Error("Vui lòng đăng nhập để mua hàng.");
  return apiRequest(`/client/gio-hang/them`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id, giayChiTietId, soLuong }),
    fallbackMessage: "Không thể thêm vào giỏ hàng",
  });
}

export async function capNhatSoLuong(itemId, soLuong) {
  const id = layKhachId();
  return apiRequest(`/client/gio-hang/chi-tiet/${itemId}`, {
    method: "PUT",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id, soLuong }),
    fallbackMessage: "Không thể cập nhật giỏ hàng",
  });
}

export async function xoaItemGio(itemId) {
  return apiRequest(`/client/gio-hang/chi-tiet/${itemId}`, {
    method: "DELETE",
    authenticated: false,
    fallbackMessage: "Không thể xóa khỏi giỏ hàng",
  });
}

// Lấy danh sách địa chỉ giao hàng của khách (cho trang thanh toán).
export async function layDiaChiKhachHang() {
  const id = layKhachId();
  if (!id) return [];
  // Phiên đăng nhập cũ (trước khi có customerToken) -> bỏ qua để không bị chặn 403,
  // khách tự nhập địa chỉ; đăng nhập lại là có token và load được địa chỉ đã lưu.
  if (!localStorage.getItem("customerToken")) return [];
  const data = await apiRequest(`/client/khach-hang/${id}/dia-chi`, {
    authenticated: true,
    fallbackMessage: "Không thể tải địa chỉ",
  });
  return Array.isArray(data) ? data : [];
}

// Đặt hàng từ giỏ. payload: { tenNguoiNhan, sdtNguoiNhan, tinhThanh, quanHuyen, phuongXa, diaChiCuThe, hinhThucThanhToan, ghiChu }
export async function datHang(payload) {
  const id = layKhachId();
  if (!id) throw new Error("Vui lòng đăng nhập.");
  return apiRequest(`/client/dat-hang`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id, ...payload }),
    fallbackMessage: "Không thể đặt hàng",
  });
}

// Giữ hàng tạm khi vào thanh toán (trừ tồn tạm 90 giây).
export async function giuHang() {
  const id = layKhachId();
  if (!id) return;
  return apiRequest(`/client/gio-hang/giu-hang?khachHangId=${id}`, {
    method: "POST",
    authenticated: false,
    fallbackMessage: "Không thể giữ hàng",
  });
}

// Hủy giữ hàng khi rời thanh toán (hoàn tồn). Bỏ qua lỗi (best-effort).
export async function huyGiuHang() {
  const id = layKhachId();
  if (!id) return;
  try {
    await apiRequest(`/client/gio-hang/huy-giu?khachHangId=${id}`, {
      method: "POST",
      authenticated: false,
      fallbackMessage: "",
    });
  } catch {
    // bỏ qua
  }
}

// Hủy giữ hàng khi đóng tab/đột ngột - gửi không chờ phản hồi.
export function huyGiuHangBeacon() {
  const id = layKhachId();
  if (!id || typeof navigator === "undefined" || !navigator.sendBeacon) return;
  navigator.sendBeacon(`${API_BASE_URL}/client/gio-hang/huy-giu?khachHangId=${id}`);
}

// VNPay giả lập: tạo mã QR cho đơn (chưa tạo đơn).
export async function taoMaVnPay(payload) {
  const id = layKhachId();
  if (!id) throw new Error("Vui lòng đăng nhập.");
  return apiRequest(`/client/vnpay/tao-ma`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id, ...payload }),
    fallbackMessage: "Không thể tạo mã thanh toán",
  });
}

// Poll trạng thái thanh toán VNPay.
export async function trangThaiVnPay(token) {
  return apiRequest(`/client/vnpay/trang-thai/${token}`, {
    authenticated: false,
    fallbackMessage: "",
  });
}

// Tính phí vận chuyển (GHN) cho giỏ hiện tại tới địa chỉ nhận.
// Trả về { phiVanChuyen, uocTinh, moTa } hoặc null nếu chưa đăng nhập.
export async function tinhPhiVanChuyen({ tinhThanh, quanHuyen, phuongXa, diaChiCuThe }) {
  const id = layKhachId();
  if (!id) return null;
  return apiRequest(`/client/phi-van-chuyen`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id, tinhThanh, quanHuyen, phuongXa, diaChiCuThe }),
    fallbackMessage: "Không thể tính phí vận chuyển",
  });
}

// Danh sách voucher khách có thể dùng cho giỏ hiện tại (toàn sàn + voucher riêng được gửi).
export async function layVoucherKhaDung() {
  const id = layKhachId();
  if (!id) return [];
  const data = await apiRequest(`/client/voucher/kha-dung?khachHangId=${id}`, {
    authenticated: false,
    fallbackMessage: "Không thể tải danh sách voucher",
  });
  return Array.isArray(data) ? data : [];
}

// Kiểm tra / áp mã giảm giá trên giỏ hiện tại.
export async function kiemTraVoucher(maPhieu) {
  const id = layKhachId();
  if (!id) throw new Error("Vui lòng đăng nhập.");
  return apiRequest(`/client/voucher/kiem-tra`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id, maPhieu }),
    fallbackMessage: "Không thể áp mã giảm giá",
  });
}

// Lấy thông tin khách hàng từ phiên đăng nhập.
export function layThongTinKhach() {
  try {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}
