import { apiRequest } from "./api-client";

// Lấy id khách hàng từ phiên đăng nhập (giỏ hàng server-side cần đăng nhập).
export function layKhachId() {
  const token = localStorage.getItem("customerToken");
  if (!token) return null;

  let tokenKhachHangId = null;
  try {
    const parts = token.split(".");
    if (parts.length !== 3) return null;

    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
    const claims = JSON.parse(atob(padded));
    const hetHan = Number(claims?.exp || 0) * 1000;

    if (claims?.role !== "CUSTOMER" || !claims?.sub || hetHan <= Date.now()) {
      return null;
    }
    tokenKhachHangId = claims.sub;
  } catch {
    return null;
  }

  try {
    const raw = localStorage.getItem("user");
    const userId = raw ? JSON.parse(raw)?.id : null;
    return userId && String(userId) === String(tokenKhachHangId)
      ? userId
      : tokenKhachHangId;
  } catch {
    // Object user có thể chưa đồng bộ; ID trong JWT là nguồn dự phòng của cùng phiên đăng nhập.
    return tokenKhachHangId;
  }
}

export function coPhienKhachHang() {
  return Boolean(localStorage.getItem("customerToken") && layKhachId());
}

const GIO_RONG = { id: null, items: [], tongSoLuong: 0, tongTien: 0 };
const GIO_HANG_KEY_PREFIX = "sportshoe-cart:";
// Khách vãng lai (chưa đăng nhập) vẫn có giỏ riêng lưu cục bộ với khóa "guest".
const KHOA_VANG_LAI = "guest";

function khoaGioHang() {
  const id = layKhachId();
  return `${GIO_HANG_KEY_PREFIX}${id || KHOA_VANG_LAI}`;
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

// Đồng bộ giá giỏ với server: cập nhật giá hiện tại (sau giảm) + giá niêm yết + tồn cho từng item.
// Tự động đẩy sản phẩm đã ngừng hoạt động ra khỏi giỏ hàng.
export async function dongBoGiaGio() {
  const gio = docGioHangLocal();
  if (!gio.items.length) return { ...gio, removedNames: [] };
  const ids = gio.items.map((it) => Number(it.giayChiTietId)).filter(Boolean);
  let ds = [];
  try {
    ds = await apiRequest(`/client/san-pham/dong-bo-gia`, {
      method: "POST",
      authenticated: false,
      body: JSON.stringify({ ids }),
      fallbackMessage: "",
    });
  } catch {
    return { ...taoGioHangResponse(gio.items), removedNames: [] }; // lỗi mạng -> giữ giá cũ
  }
  const theoId = new Map(
    (Array.isArray(ds) ? ds : []).map((x) => [Number(x.giayChiTietId), x]),
  );
  const removedNames = [];
  const validItems = [];

  for (const item of gio.items) {
    // "giá lúc thêm" = mốc để so sánh. Chỉ gạch giá cũ khi giá ĐỔI sau lúc thêm vào giỏ
    // (đợt giảm mới / admin đổi giá gốc), KHÔNG gạch với phần giảm vốn đã có sẵn lúc thêm.
    if (item.giaThem == null) item.giaThem = Number(item.giaBan || 0);
    const moi = theoId.get(Number(item.giayChiTietId));
    if (!moi || moi.conBan === false) {
      // Biến thể không còn tồn tại hoặc đã bị ngừng bán -> tự động đẩy ra khỏi giỏ hàng
      removedNames.push(item.tenSanPham || "Sản phẩm");
      continue;
    }
    item.giaNiemYet = Number(moi.giaNiemYet ?? item.giaBan);
    item.giaBan = Number(moi.giaHienTai ?? item.giaBan); // giá hiện tại (sau đợt giảm)
    if (moi.tonKho != null) item.tonKho = Number(moi.tonKho);
    item.conBan = true;
    if (moi.canNang != null) item.canNang = Number(moi.canNang); // cân nặng 1 SP (gram)
    if (moi.ma) item.ma = moi.ma; // mã sản phẩm để hiển thị ở giỏ
    if (moi.hinhAnh) item.hinhAnh = moi.hinhAnh; // cập nhật ảnh mới nhất của sản phẩm/biến thể
    validItems.push(item);
  }
  luuGioHangLocal(validItems);
  const resp = taoGioHangResponse(validItems);
  resp.removedNames = removedNames;
  return resp;
}

export async function themVaoGio(giayChiTietId, soLuong = 1, thongTin = {}) {
  // Khách vãng lai vẫn thêm được vào giỏ (lưu cục bộ theo khóa "guest").
  const gio = docGioHangLocal();
  const bienTheId = Number(giayChiTietId);
  const hienTai = gio.items.find((item) => Number(item.giayChiTietId) === bienTheId);
  const soLuongMoi = Number(soLuong) + Number(hienTai?.soLuong || 0);
  const tonKho = Number(thongTin.tonKho ?? thongTin.soLuong ?? hienTai?.tonKho ?? 0);

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
      giaThem: Number(thongTin.giaBan || 0), // giá lúc thêm vào giỏ (mốc so sánh khi đổi giá sau này)
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
  if (Number(soLuong) < 1) {
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

// Khi khách vãng lai đăng nhập: gộp giỏ "guest" vào giỏ của tài khoản rồi xóa giỏ guest.
export function chuyenGioHangVangLai(userId) {
  if (!userId) return;
  const guestKey = `${GIO_HANG_KEY_PREFIX}${KHOA_VANG_LAI}`;
  const userKey = `${GIO_HANG_KEY_PREFIX}${userId}`;
  let guestItems = [];
  try {
    guestItems = JSON.parse(localStorage.getItem(guestKey) || "[]");
  } catch {
    guestItems = [];
  }
  if (!Array.isArray(guestItems) || !guestItems.length) {
    localStorage.removeItem(guestKey);
    return;
  }
  let userItems = [];
  try {
    userItems = JSON.parse(localStorage.getItem(userKey) || "[]");
  } catch {
    userItems = [];
  }
  if (!Array.isArray(userItems)) userItems = [];

  for (const gItem of guestItems) {
    const dangCo = userItems.find(
      (u) => Number(u.giayChiTietId) === Number(gItem.giayChiTietId),
    );
    if (dangCo) {
      dangCo.soLuong = Math.min(10, Number(dangCo.soLuong || 0) + Number(gItem.soLuong || 0));
    } else {
      userItems.push(gItem);
    }
  }
  localStorage.setItem(userKey, JSON.stringify(userItems));
  localStorage.removeItem(guestKey);
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

// Đặt hàng từ giỏ với diaChiGiaoHang theo hợp đồng địa chỉ hai cấp.
export async function datHang(payload) {
  // khachHangId null = khách vãng lai (đơn khách lẻ).
  const id = layKhachId();
  return apiRequest(`/client/dat-hang`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id || null, sanPhams: danhSachDatHang(), ...payload }),
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
  // khachHangId null = khách vãng lai.
  const id = layKhachId();
  return apiRequest(`/client/vnpay/tao-ma`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id || null, sanPhams: danhSachDatHang(), ...payload }),
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

export async function huyVnPay(token) {
  if (!token) return;
  return apiRequest(`/client/vnpay/huy/${token}`, {
    method: "POST",
    authenticated: false,
    fallbackMessage: "Không thể hủy phiên thanh toán",
  });
}

// Tính phí vận chuyển (GHN) cho giỏ hiện tại tới địa chỉ nhận.
// Trả về { phiVanChuyen, uocTinh, moTa } hoặc null nếu chưa đăng nhập.
export async function tinhPhiVanChuyen(diaChiGiaoHang) {
  // Khách vãng lai vẫn tính được phí ship (chỉ cần địa chỉ + danh sách sản phẩm).
  const id = layKhachId();
  return apiRequest(`/client/phi-van-chuyen`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({
      khachHangId: id || null,
      sanPhams: danhSachDatHang(),
      diaChiGiaoHang,
    }),
    fallbackMessage: "Không thể tính phí vận chuyển",
  });
}

// Danh sách voucher có thể dùng cho giỏ hiện tại.
// Khách đăng nhập: toàn sàn + voucher riêng được gửi. Khách vãng lai: chỉ voucher toàn sàn.
export async function layVoucherKhaDung() {
  const id = layKhachId();
  const tongTienHang = docGioHangLocal().tongTien;
  const params = new URLSearchParams();
  if (id) params.set("khachHangId", id); // khách vãng lai -> không gửi -> BE trả voucher toàn sàn
  params.set("tongTienHang", String(tongTienHang));
  const data = await apiRequest(`/client/voucher/kha-dung?${params.toString()}`, {
    authenticated: false,
    fallbackMessage: "Không thể tải danh sách voucher",
  });
  return Array.isArray(data) ? data : [];
}

// Kiểm tra / áp mã giảm giá trên giỏ hiện tại (khách vãng lai chỉ áp được voucher toàn sàn).
export async function kiemTraVoucher(maPhieu) {
  const id = layKhachId();
  return apiRequest(`/client/voucher/kiem-tra`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ khachHangId: id || null, sanPhams: danhSachDatHang(), maPhieu }),
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
