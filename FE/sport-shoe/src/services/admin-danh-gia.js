import { apiRequest, buildQuery } from "./api-client";

// Bảng sản phẩm có đánh giá (tìm theo tên/mã).
export async function laySanPhamCoDanhGia(keyword) {
  return apiRequest(`/admin/danh-gia/san-pham${buildQuery({ keyword })}`, {
    authScope: "admin",
    fallbackMessage: "Không thể tải danh sách sản phẩm có đánh giá",
  });
}

// Toàn bộ đánh giá của shop kèm thông tin sản phẩm (màn "Tất cả đánh giá").
// boLoc: { trangThai: 1|0|null, tuNgay: 'yyyy-MM-dd', denNgay: 'yyyy-MM-dd' }
export async function layTatCaDanhGia(boLoc = {}) {
  return apiRequest(`/admin/danh-gia/tat-ca${buildQuery(boLoc)}`, {
    authScope: "admin",
    fallbackMessage: "Không thể tải tất cả đánh giá",
  });
}

// Đánh giá của 1 sản phẩm (mới nhất trước), cùng bộ lọc như trên.
export async function layDanhGiaTheoSanPham(giayId, boLoc = {}) {
  return apiRequest(`/admin/danh-gia/san-pham/${giayId}${buildQuery(boLoc)}`, {
    authScope: "admin",
    fallbackMessage: "Không thể tải đánh giá sản phẩm",
  });
}

// Khôi phục đánh giá đã ẩn (kể cả do AI ẩn nhầm).
export async function khoiPhucDanhGia(id) {
  return apiRequest(`/admin/danh-gia/${id}/khoi-phuc`, {
    method: "POST",
    authScope: "admin",
    fallbackMessage: "Không thể khôi phục đánh giá",
  });
}

// AI tổng hợp đánh giá: giayId null -> toàn shop.
export async function tongHopDanhGiaAI(giayId) {
  return apiRequest(`/admin/danh-gia/ai/tong-hop${buildQuery({ giayId })}`, {
    authScope: "admin",
    fallbackMessage: "AI không tổng hợp được, thử lại sau",
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
