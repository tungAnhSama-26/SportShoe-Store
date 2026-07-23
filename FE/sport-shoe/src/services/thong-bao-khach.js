import { apiRequest, buildQuery } from "./api-client";

// Chuông thông báo của khách hàng (chỉ giữ 3 ngày gần nhất, BE tự xóa cũ hơn).

// Danh sách thông báo còn hiệu lực, mới nhất trước.
export async function layThongBaoKhach(khachHangId) {
  return apiRequest(`/client/thong-bao${buildQuery({ khachHangId })}`, {
    fallbackMessage: "Không thể tải thông báo",
  });
}

// Số thông báo chưa xem (số nhỏ cạnh chuông).
export async function demThongBaoChuaXem(khachHangId) {
  return apiRequest(`/client/thong-bao/chua-xem${buildQuery({ khachHangId })}`, {
    fallbackMessage: "Không thể đếm thông báo",
  });
}

// Mở chuông -> đánh dấu tất cả đã xem.
export async function danhDauThongBaoDaXem(khachHangId) {
  return apiRequest(`/client/thong-bao/da-xem${buildQuery({ khachHangId })}`, {
    method: "PUT",
    fallbackMessage: "Không thể cập nhật thông báo",
  });
}
