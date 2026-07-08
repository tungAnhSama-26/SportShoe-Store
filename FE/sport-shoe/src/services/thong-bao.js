import { apiRequest, buildQuery } from "./api-client";

// Lấy danh sách thông báo phân trang
export async function layDanhSachThongBao(trang = 0, kichThuoc = 15) {
  return apiRequest(`/admin/thong-bao${buildQuery({ trang, kichThuoc })}`, {
    authScope: "admin",
    fallbackMessage: "Không thể tải danh sách thông báo",
  });
}

// Lấy số lượng thông báo chưa đọc
export async function demThongBaoChuaDoc() {
  return apiRequest(`/admin/thong-bao/chua-doc-count`, {
    authScope: "admin",
    fallbackMessage: "Không thể lấy số lượng thông báo chưa đọc",
  });
}

// Đánh dấu 1 thông báo đã đọc
export async function docThongBao(id) {
  return apiRequest(`/admin/thong-bao/${id}/doc`, {
    method: "PUT",
    authScope: "admin",
    fallbackMessage: "Không thể đánh dấu đã đọc thông báo này",
  });
}

// Đánh dấu tất cả thông báo là đã đọc
export async function docTatCaThongBao() {
  return apiRequest(`/admin/thong-bao/doc-tat-ca`, {
    method: "PUT",
    authScope: "admin",
    fallbackMessage: "Không thể đánh dấu tất cả đã đọc",
  });
}

// Xóa 1 thông báo
export async function xoaThongBao(id) {
  return apiRequest(`/admin/thong-bao/${id}`, {
    method: "DELETE",
    authScope: "admin",
    fallbackMessage: "Không thể xóa thông báo này",
  });
}
