import mayKhachApi from './apiClient';

export const timSanPhamTaiQuay = async (tuKhoa) => {
  const params = new URLSearchParams();
  if (tuKhoa && tuKhoa.trim()) {
    params.set("keyword", tuKhoa.trim());
  }
  return mayKhachApi.get(`/admin/ban-hang-tai-quay/san-pham?${params.toString()}`);
};

export const timKhachHangTheoSoDienThoai = async (sdt) => {
  const params = new URLSearchParams();
  if (sdt.trim()) {
    params.set("phone", sdt.trim());
  }
  return mayKhachApi.get(`/admin/ban-hang-tai-quay/khach-hang?${params.toString()}`);
};

export const layDanhSachHoaDonCho = async () => {
  return mayKhachApi.get("/admin/ban-hang-tai-quay/hoa-don-cho");
};

export const layChiTietHoaDonCho = async (id) => {
  return mayKhachApi.get(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}`);
};

export const taoHoaDonCho = async (duLieu) => {
  return mayKhachApi.post("/admin/ban-hang-tai-quay/hoa-don-cho", duLieu);
};

export const capNhatHoaDonCho = async (id, duLieu) => {
  return mayKhachApi.patch(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}`, duLieu);
};

export const apDungPhieuGiamGiaTaiQuay = async (duLieu) => {
  return mayKhachApi.post("/admin/ban-hang-tai-quay/phieu-giam-gia/ap-dung", duLieu);
};

export const thanhToanTaiQuay = async (duLieu) => {
  return mayKhachApi.post("/admin/ban-hang-tai-quay/thanh-toan", duLieu);
};

export const huyHoaDonCho = async (id) => {
  return mayKhachApi.patch(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}/huy`);
};

export const tinhPhiVanChuyenTaiQuay = async (duLieu) => {
  return mayKhachApi.post("/admin/ban-hang-tai-quay/phi-van-chuyen/ghn", duLieu);
};

export const timPhieuGiamGiaTaiQuay = async (paramsObj) => {
  const searchParams = new URLSearchParams();
  if (paramsObj.keyword?.trim()) {
    searchParams.set("keyword", paramsObj.keyword.trim());
  }
  if (paramsObj.hoaDonId != null) {
    searchParams.set("hoaDonId", String(paramsObj.hoaDonId));
  }
  if (paramsObj.khachHangId) {
    searchParams.set("khachHangId", paramsObj.khachHangId);
  }
  if (paramsObj.tongTienHang != null) {
    searchParams.set("tongTienHang", String(paramsObj.tongTienHang));
  }
  return mayKhachApi.get(`/admin/ban-hang-tai-quay/phieu-giam-gia?${searchParams.toString()}`);
};
