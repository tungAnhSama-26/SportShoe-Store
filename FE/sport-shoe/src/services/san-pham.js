import { apiRequest, buildQuery } from "./api-client";

// Ảnh thay thế khi sản phẩm chưa có hình.
const ANH_MAC_DINH =
  "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80";

// Lấy sản phẩm nổi bật (backend đã xếp hạng: bán chạy > giảm giá sâu > đánh giá cao & nhiều).
// Cùng cấu trúc dữ liệu với trang danh sách sản phẩm.
async function laySanPhamNoiBat(limit = 8) {
  const data = await apiRequest(`/client/san-pham/noi-bat${buildQuery({ limit })}`, {
    authenticated: false,
    fallbackMessage: "Không thể tải sản phẩm nổi bật",
  });
  return Array.isArray(data) ? data.map(mapSanPhamDayDu) : [];
}

const NHAN_GIOI_TINH = { 1: "Nam", 2: "Nữ", 3: "Unisex" };

// Map đầy đủ thuộc tính cho trang danh sách sản phẩm (để lọc theo nhiều tiêu chí).
// item = ClientSanPhamResponse { thongTin, hinhAnhSanPham, giaHienThiMin, giaNiemYetHienThi, coGiam, mauSac, kichCo }
function mapSanPhamDayDu(item) {
  const g = item.thongTin ?? item;
  const giaNiemYet = Number(item.giaNiemYetHienThi ?? g.giaGocMin ?? g.giaMin ?? g.giaMax ?? 0);
  // Có đợt giảm giá khi giá hiển thị (sau giảm) thấp hơn giá niêm yết.
  const giaHienThi = item.giaHienThiMin != null ? Number(item.giaHienThiMin) : giaNiemYet;
  const coGiam = Boolean(item.coGiam) && giaHienThi < giaNiemYet;
  const phanTramGiam =
    coGiam && giaNiemYet > 0 ? Math.round(((giaNiemYet - giaHienThi) / giaNiemYet) * 100) : 0;
  return {
    id: g.id,
    ten: g.ten,
    // hinhAnhSanPham = ảnh của biến thể giá thấp nhất (backend đã chọn); fallback ảnh gốc rồi ảnh mặc định.
    hinhAnh: item.hinhAnhSanPham || g.hinhAnh || ANH_MAC_DINH,
    gia: giaHienThi,
    giaCu: coGiam ? giaNiemYet : null,
    nhan: coGiam && phanTramGiam > 0 ? `-${phanTramGiam}%` : null,
    thuongHieu: g.thuongHieu || "",
    loaiGiay: g.loaiGiay || "",
    chatLieu: g.chatLieu || "",
    deGiay: g.deGiay || "",
    coGiay: g.coGiay || "",
    congNgheDem: g.congNgheDem || "",
    trongLuong: g.trongLuong || "",
    gioiTinhNhan: NHAN_GIOI_TINH[g.gioiTinh] || "Khác",
    mauSac: Array.isArray(item.mauSac) ? item.mauSac : [],
    kichCo: Array.isArray(item.kichCo) ? item.kichCo : [],
    soSao: Number(item.soSaoTrungBinh || 0),
    soDanhGia: Number(item.soDanhGia || 0),
    daBan: Number(item.daBan || 0),
  };
}

// Lấy tất cả sản phẩm đang bán từ DB (public). Dùng cho trang danh sách sản phẩm.
async function layTatCaSanPham() {
  const data = await apiRequest(`/client/san-pham`, {
    authenticated: false,
    fallbackMessage: "Không thể tải danh sách sản phẩm",
  });
  return Array.isArray(data) ? data.map(mapSanPhamDayDu) : [];
}

// Lấy chi tiết 1 sản phẩm kèm danh sách biến thể (màu, size, giá, tồn, ảnh).
async function layChiTietSanPham(id) {
  return apiRequest(`/client/san-pham/${id}`, {
    authenticated: false,
    fallbackMessage: "Không thể tải chi tiết sản phẩm",
  });
}

// Lấy đánh giá của sản phẩm: { diemTrungBinh, soLuong, danhSach: [...] }.
async function layDanhGia(giayId) {
  return apiRequest(`/client/san-pham/${giayId}/danh-gia`, {
    authenticated: false,
    fallbackMessage: "Không thể tải đánh giá sản phẩm",
  });
}

// Số liệu thật cho banner trang chủ: { soKhachHang, soSanPham, diemTrungBinh, soDanhGia }.
async function layThongKeTrangChu() {
  return apiRequest(`/client/thong-ke`, {
    authenticated: false,
    fallbackMessage: "Không thể tải thống kê",
  });
}

// Chuyển dữ liệu hãng (ThuongHieuNoiBatResponse) sang trường mà card ở trang chủ dùng.
function mapHangNoiBat(h) {
  return {
    id: h.id,
    ten: h.ten,
    moTa: h.moTa || "",
    hinhAnh: h.logoUrl || ANH_MAC_DINH,
  };
}

// Trang Đánh giá công khai: { danhSach, trang, tongTrang, tongSo }. soSao=null -> tất cả.
async function layDanhGiaCongKhai({ soSao = null, trang = 0, kichThuoc = 10 } = {}) {
  const query = buildQuery({ soSao: soSao || undefined, trang, kichThuoc });
  return apiRequest(`/client/danh-gia/cong-khai${query}`, {
    authenticated: false,
    fallbackMessage: "Không thể tải đánh giá",
  });
}

// Lấy các hãng nổi bật nhất từ DB (public, không cần đăng nhập).
async function layHangNoiBat(limit = 4) {
  const data = await apiRequest(`/client/thuong-hieu/noi-bat${buildQuery({ limit })}`, {
    authenticated: false,
    fallbackMessage: "Không thể tải danh sách hãng",
  });
  return Array.isArray(data) ? data.map(mapHangNoiBat) : [];
}

export {
  laySanPhamNoiBat,
  layHangNoiBat,
  layTatCaSanPham,
  layChiTietSanPham,
  layDanhGia,
  layDanhGiaCongKhai,
  layThongKeTrangChu,
};
