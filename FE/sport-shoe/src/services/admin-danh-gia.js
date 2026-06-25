import { apiRequest, buildQuery } from "./api-client";

// Bảng sản phẩm có đánh giá (tìm theo tên/mã).
export async function laySanPhamCoDanhGia(keyword) {
  return apiRequest(`/admin/danh-gia/san-pham${buildQuery({ keyword })}`, {
    authScope: "admin",
    fallbackMessage: "Không thể tải danh sách sản phẩm có đánh giá",
  });
}

// Toàn bộ đánh giá của shop kèm thông tin sản phẩm (màn "Tất cả đánh giá").
export async function layTatCaDanhGia() {
  return apiRequest(`/admin/danh-gia/tat-ca`, {
    authScope: "admin",
    fallbackMessage: "Không thể tải tất cả đánh giá",
  });
}

// Toàn bộ đánh giá của 1 sản phẩm (mới nhất trước).
export async function layDanhGiaTheoSanPham(giayId) {
  return apiRequest(`/admin/danh-gia/san-pham/${giayId}`, {
    authScope: "admin",
    fallbackMessage: "Không thể tải đánh giá sản phẩm",
  });
}

// Xóa mềm 1 đánh giá.
export async function xoaDanhGia(id) {
  return apiRequest(`/admin/danh-gia/${id}`, {
    method: "DELETE",
    authScope: "admin",
    fallbackMessage: "Không thể xóa đánh giá",
  });
}

// Phản hồi 1 đánh giá (1 lần/đánh giá).
export async function phanHoiDanhGia(id, noiDung) {
  return apiRequest(`/admin/danh-gia/${id}/phan-hoi`, {
    method: "POST",
    authScope: "admin",
    body: JSON.stringify({ noiDung }),
    fallbackMessage: "Không thể gửi phản hồi",
  });
}

// Số đánh giá chưa xem (cho chuông thông báo) - dùng ở Đợt 4.
export async function demDanhGiaChuaXem() {
  return apiRequest(`/admin/danh-gia/chua-xem`, {
    authScope: "admin",
    fallbackMessage: "Không thể tải số đánh giá chưa xem",
  });
}
