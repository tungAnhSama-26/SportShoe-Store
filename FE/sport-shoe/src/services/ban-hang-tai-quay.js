import { apiRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage:
      "Không thể hoàn tất thao tác bán hàng tại quầy lúc này. Vui lòng thử lại.",
    ...init,
  });
}
function timKhachHangTheoSoDienThoai(phone) {
  const params = new URLSearchParams();
  if (phone.trim()) {
    params.set("phone", phone.trim());
  }
  return request(`/admin/ban-hang-tai-quay/khach-hang?${params.toString()}`);
}
function timSanPhamTaiQuay(keyword) {
  const params = new URLSearchParams();
  if (keyword && keyword.trim()) {
    params.set("keyword", keyword.trim());
  }
  return request(`/admin/ban-hang-tai-quay/san-pham?${params.toString()}`);
}
function layDanhSachHoaDonCho() {
  return request("/admin/ban-hang-tai-quay/hoa-don-cho");
}
function layChiTietHoaDonCho(id) {
  return request(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}`);
}
function taoHoaDonCho(payload) {
  return request("/admin/ban-hang-tai-quay/hoa-don-cho", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
function capNhatHoaDonCho(id, payload) {
  return request(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}`, {
    method: "PATCH",
    body: JSON.stringify(payload)
  });
}
function apDungPhieuGiamGiaTaiQuay(payload) {
  return request("/admin/ban-hang-tai-quay/phieu-giam-gia/ap-dung", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
function tinhPhiVanChuyenTaiQuay(payload) {
  return request("/admin/ban-hang-tai-quay/phi-van-chuyen/ghn", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
function timPhieuGiamGiaTaiQuay(params) {
  const searchParams = new URLSearchParams();
  if (params.keyword?.trim()) {
    searchParams.set("keyword", params.keyword.trim());
  }
  if (params.hoaDonId != null) {
    searchParams.set("hoaDonId", String(params.hoaDonId));
  }
  if (params.khachHangId) {
    searchParams.set("khachHangId", params.khachHangId);
  }
  if (params.tongTienHang != null) {
    searchParams.set("tongTienHang", String(params.tongTienHang));
  }
  return request(`/admin/ban-hang-tai-quay/phieu-giam-gia?${searchParams.toString()}`);
}
function thanhToanTaiQuay(payload) {
  return request("/admin/ban-hang-tai-quay/thanh-toan", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
function taoQrChuyenKhoan(id, soTien) {
  const params = new URLSearchParams();
  if (soTien != null) {
    params.set("soTien", String(soTien));
  }
  const query = params.toString();
  return request(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}/qr-chuyen-khoan${query ? `?${query}` : ""}`, {
    method: "POST"
  });
}
function layTrangThaiChuyenKhoan(id) {
  return request(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}/trang-thai-chuyen-khoan`);
}
function huyHoaDonCho(id) {
  return request(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}/huy`, {
    method: "PATCH"
  });
}
function doiBienTheHoaDonChiTiet(id, payload) {
  return request(`/admin/ban-hang-tai-quay/hoa-don-chi-tiet/${id}/doi-bien-the`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export {
  apDungPhieuGiamGiaTaiQuay,
  doiBienTheHoaDonChiTiet,
  huyHoaDonCho,
  layChiTietHoaDonCho,
  layDanhSachHoaDonCho,
  layTrangThaiChuyenKhoan,
  taoQrChuyenKhoan,
  tinhPhiVanChuyenTaiQuay,
  taoHoaDonCho,
  capNhatHoaDonCho,
  thanhToanTaiQuay,
  timKhachHangTheoSoDienThoai,
  timPhieuGiamGiaTaiQuay,
  timSanPhamTaiQuay
};
