import { apiRequest } from "./api-client";

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
const GIO_HANG_KEY_PREFIX = "sportshoe-cart:";

function khoaGioHang() {
  const id = layKhachId();
  return id ? `${GIO_HANG_KEY_PREFIX}${id}` : null;
}

function docGioHangLocal() {
  const key = khoaGioHang();
  if (!key) return { ...GIO_RONG, items: [] };
  try {
    const items = JSON.parse(localStorage.getItem(key) || "[]");
    return taoGioHangResponse(Array.isArray(items) ? items : []);
  } catch {
    return { ...GIO_RONG, items: [] };
  }
}

function luuGioHangLocal(items) {
  const key = khoaGioHang();
  if (!key) return;
  localStorage.setItem(key, JSON.stringify(items));
}

function taoGioHangResponse(items) {
  const tongSoLuong = items.reduce((tong, item) => tong + Number(item.soLuong || 0), 0);
  const tongTien = items.reduce(
    (tong, item) => tong + Number(item.giaBan || 0) * Number(item.soLuong || 0),
    0,
  );
  return { id: null, items, tongSoLuong, tongTien };
}

function danhSachDatHang() {
  return docGioHangLocal().items.map((item) => ({
    giayChiTietId: Number(item.giayChiTietId),
    soLuong: Number(item.soLuong),
  }));
}

export async function layGioHang() {
  return docGioHangLocal();
}

export async function themVaoGio(giayChiTietId, soLuong = 1, thongTin = {}) {
  const id = layKhachId();
  if (!id) throw new Error("Vui lòng đăng nhập để mua hàng.");
  const gio = docGioHangLocal();
  const bienTheId = Number(giayChiTietId);
  const hienTai = gio.items.find((item) => Number(item.giayChiTietId) === bienTheId);
  const soLuongMoi = Number(soLuong) + Number(hienTai?.soLuong || 0);
  const tonKho = Number(thongTin.tonKho ?? thongTin.soLuong ?? hienTai?.tonKho ?? 0);

  if (soLuongMoi > 10) {
    throw new Error("Mỗi sản phẩm chỉ được mua tối đa 10 sản phẩm.");
  }
  if (tonKho > 0 && soLuongMoi > tonKho) {
    throw new Error(`Sản phẩm chỉ còn ${tonKho} sản phẩm.`);
  }

  if (hienTai) {
    hienTai.soLuong = soLuongMoi;
    Object.assign(hienTai, thongTin, {
      id: bienTheId,
      giayChiTietId: bienTheId,
      soLuong: soLuongMoi,
      tonKho: tonKho || hienTai.tonKho,
    });
  } else {
    gio.items.push({
      id: bienTheId,
      giayChiTietId: bienTheId,
      giayId: thongTin.giayId ?? null,
      tenSanPham: thongTin.tenSanPham || "Sản phẩm",
      mauSac: thongTin.mauSac || "",
      kichCo: thongTin.kichCo || "",
      hinhAnh: thongTin.hinhAnh || "",
      giaBan: Number(thongTin.giaBan || 0),
      soLuong: Number(soLuong),
      tonKho,
    });
  }
  luuGioHangLocal(gio.items);
  return taoGioHangResponse(gio.items);
}

export async function capNhatSoLuong(itemId, soLuong) {
  const gio = docGioHangLocal();
  const item = gio.items.find((dong) => Number(dong.id) === Number(itemId));
  if (!item) throw new Error("Sản phẩm không còn trong giỏ hàng.");
  if (Number(soLuong) < 1 || Number(soLuong) > 10) {
    throw new Error("Số lượng sản phẩm không hợp lệ.");
  }
  if (Number(item.tonKho) > 0 && Number(soLuong) > Number(item.tonKho)) {
    throw new Error(`Sản phẩm chỉ còn ${item.tonKho} sản phẩm.`);
  }
  item.soLuong = Number(soLuong);
  luuGioHangLocal(gio.items);
  return taoGioHangResponse(gio.items);
}

export async function xoaItemGio(itemId) {
  const gio = docGioHangLocal();
  const items = gio.items.filter((item) => Number(item.id) !== Number(itemId));
  luuGioHangLocal(items);
  return taoGioHangResponse(items);
}

export function xoaGioHang() {
  const key = khoaGioHang();
  if (key) localStorage.removeItem(key);
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
    body: JSON.stringify({ khachHangId: id, sanPhams: danhSachDatHang(), ...payload }),
    fallbackMessage: "Không thể đặt hàng",
  });
}

// Giữ hàng tạm khi vào thanh toán (trừ tồn tạm 90 giây).
export async function giuHang() {
  return undefined;
}

// Hủy giữ hàng khi rời thanh toán (hoàn tồn). Bỏ qua lỗi (best-effort).
export async function huyGiuHang() {
  return undefined;
}

// Hủy giữ hàng khi đóng tab/đột ngột - gửi không chờ phản hồi.
export function huyGiuHangBeacon() {
  return undefined;
}

// VNPay giả lập: tạo mã QR cho đơn (chưa tạo đơn).
export async function taoMaVnPay(payload) {
  const id = layKhachId();
  if (!id) throw new Error("Vui lòng đăng nhập.");
  return apiRequest(`/client/vnpay/tao-ma`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id, sanPhams: danhSachDatHang(), ...payload }),
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
    body: JSON.stringify({
      khachHangId: id,
      sanPhams: danhSachDatHang(),
      tinhThanh,
      quanHuyen,
      phuongXa,
      diaChiCuThe,
    }),
    fallbackMessage: "Không thể tính phí vận chuyển",
  });
}

// Danh sách voucher khách có thể dùng cho giỏ hiện tại (toàn sàn + voucher riêng được gửi).
export async function layVoucherKhaDung() {
  const id = layKhachId();
  if (!id) return [];
  const tongTienHang = docGioHangLocal().tongTien;
  const data = await apiRequest(
    `/client/voucher/kha-dung?khachHangId=${id}&tongTienHang=${encodeURIComponent(tongTienHang)}`,
    {
    authenticated: false,
    fallbackMessage: "Không thể tải danh sách voucher",
    },
  );
  return Array.isArray(data) ? data : [];
}

// Kiểm tra / áp mã giảm giá trên giỏ hiện tại.
export async function kiemTraVoucher(maPhieu) {
  const id = layKhachId();
  if (!id) throw new Error("Vui lòng đăng nhập.");
  return apiRequest(`/client/voucher/kiem-tra`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id, sanPhams: danhSachDatHang(), maPhieu }),
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
