import { apiRequest, buildQuery } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể hoàn tất thao tác khuyến mãi lúc này. Vui lòng thử lại.",
    ...init,
  });
}

function toQuery(params) {
  return buildQuery(params);
}

function checkTenDotGiamGia(ten, id = null) {
  const params = new URLSearchParams();
  params.set("ten", ten);
  if (id != null) params.set("id", String(id));
  return request(`/admin/dot-giam-gia/check-ten?${params}`);
}

function checkTenPhieuGiamGia(ten, id = null) {
  const params = new URLSearchParams();
  params.set("ten", ten);
  if (id != null) params.set("id", String(id));
  return request(`/admin/phieu-giam-gia/check-ten?${params}`);
}

function checkMaPhieuGiamGia(ma, id = null) {
  const params = new URLSearchParams();
  params.set("ma", ma);
  if (id != null) params.set("id", String(id));
  return request(`/admin/phieu-giam-gia/check-ma?${params}`);
}

function getDotGiamGiaList(filters) {
  const params = new URLSearchParams();
  if (filters?.keyword?.trim()) params.set("keyword", filters.keyword.trim());
  if (filters?.trangThai != null && filters?.trangThai !== "") params.set("trangThai", String(filters.trangThai));
  if (filters?.loaiGiam != null && filters?.loaiGiam !== "") params.set("loaiGiam", String(filters.loaiGiam));
  if (filters?.tuNgay) params.set("tuNgay", filters.tuNgay);
  if (filters?.denNgay) params.set("denNgay", filters.denNgay);
  params.set("pageNo", String(filters?.pageNo ?? 0));
  params.set("pageSize", String(filters?.pageSize ?? 5));
  const q = params.toString();
  return request(`/admin/dot-giam-gia/paging?${q}`);
}

function getDotGiamGiaDetail(id) {
  return request(`/admin/dot-giam-gia/detail/${id}`);
}

function createDotGiamGia(payload) {
  return request("/admin/dot-giam-gia/add", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

function updateDotGiamGia(id, payload) {
  return request(`/admin/dot-giam-gia/update?id=${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

function deleteDotGiamGia(id) {
  return request(`/admin/dot-giam-gia/delete?id=${id}`, {
    method: "DELETE"
  });
}

function getDotGiamGiaSanPhamList() {
  return request("/admin/dot-giam-gia-san-pham");
}

function getDotGiamGiaSanPhamDetail(id) {
  return request(`/admin/dot-giam-gia-san-pham/detail/${id}`);
}

function createDotGiamGiaSanPham(payload) {
  return request("/admin/dot-giam-gia-san-pham/add", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

function updateDotGiamGiaSanPham(id, payload) {
  return request(`/admin/dot-giam-gia-san-pham/update?id=${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

function deleteDotGiamGiaSanPham(id) {
  return request(`/admin/dot-giam-gia-san-pham/delete?id=${id}`, {
    method: "DELETE"
  });
}

function syncDotGiamGiaSanPham(payload) {
  return request("/admin/dot-giam-gia-san-pham/bulk-sync", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

function getPhieuGiamGiaList(filters) {
  const params = new URLSearchParams();
  if (filters?.keyword?.trim()) params.set("keyword", filters.keyword.trim());
  if (filters?.trangThai != null && filters?.trangThai !== "") params.set("trangThai", String(filters.trangThai));
  if (filters?.loai != null && filters?.loai !== "") params.set("loai", String(filters.loai));
  if (filters?.loaiPhieu != null && filters?.loaiPhieu !== "") params.set("loaiPhieu", String(filters.loaiPhieu));
  if (filters?.tuNgay) params.set("tuNgay", filters.tuNgay);
  if (filters?.denNgay) params.set("denNgay", filters.denNgay);
  params.set("pageNo", String(filters?.pageNo ?? 0));
  params.set("pageSize", String(filters?.pageSize ?? 5));
  const q = params.toString();
  return request(`/admin/phieu-giam-gia/paging?${q}`);
}

function getPhieuGiamGiaDetail(id) {
  return request(`/admin/phieu-giam-gia/detail/${id}`);
}

function createPhieuGiamGia(payload) {
  return request("/admin/phieu-giam-gia/add", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

function updatePhieuGiamGia(id, payload) {
  return request(`/admin/phieu-giam-gia/update?id=${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

function deletePhieuGiamGia(id) {
  return request(`/admin/phieu-giam-gia/delete?id=${id}`, {
    method: "DELETE"
  });
}

function getPhieuGiamGiaKhachHangList(filters) {
  const params = new URLSearchParams();
  if (filters?.keyword?.trim()) params.set("keyword", filters.keyword.trim());
  if (filters?.phieuGiamGiaId) params.set("phieuGiamGiaId", String(filters.phieuGiamGiaId));
  if (filters?.trangThai != null && filters?.trangThai !== "") params.set("trangThai", String(filters.trangThai));
  params.set("pageNo", String(filters?.pageNo ?? 0));
  params.set("pageSize", String(filters?.pageSize ?? 5));
  const q = params.toString();
  return request(`/admin/phieu-giam-gia-khach-hang/paging?${q}`);
}

function getPhieuGiamGiaKhachHangDetail(id) {
  return request(`/admin/phieu-giam-gia-khach-hang/detail/${id}`);
}

function createPhieuGiamGiaKhachHang(payload) {
  return request("/admin/phieu-giam-gia-khach-hang/add", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

function updatePhieuGiamGiaKhachHang(id, payload) {
  return request(`/admin/phieu-giam-gia-khach-hang/update?id=${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

function deletePhieuGiamGiaKhachHang(id) {
  return request(`/admin/phieu-giam-gia-khach-hang/delete?id=${id}`, {
    method: "DELETE"
  });
}

function searchSanPhamTaiQuay(keyword) {
  return request(`/admin/ban-hang-tai-quay/san-pham${toQuery({ keyword })}`);
}

function getEmailSuggestions() {
  return request("/admin/phieu-giam-gia-khach-hang/email-suggestions");
}

export {
  checkMaPhieuGiamGia,
  checkTenDotGiamGia,
  checkTenPhieuGiamGia,
  createDotGiamGia,
  createDotGiamGiaSanPham,
  createPhieuGiamGia,
  createPhieuGiamGiaKhachHang,
  deleteDotGiamGia,
  deleteDotGiamGiaSanPham,
  deletePhieuGiamGia,
  deletePhieuGiamGiaKhachHang,
  getEmailSuggestions,
  getDotGiamGiaDetail,
  getDotGiamGiaList,
  getDotGiamGiaSanPhamDetail,
  getDotGiamGiaSanPhamList,
  getPhieuGiamGiaDetail,
  getPhieuGiamGiaKhachHangDetail,
  getPhieuGiamGiaKhachHangList,
  getPhieuGiamGiaList,
  searchSanPhamTaiQuay,
  syncDotGiamGiaSanPham,
  updateDotGiamGia,
  updateDotGiamGiaSanPham,
  updatePhieuGiamGia,
  updatePhieuGiamGiaKhachHang
};
