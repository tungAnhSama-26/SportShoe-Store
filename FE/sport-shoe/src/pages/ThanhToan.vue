<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter, onBeforeRouteLeave } from 'vue-router';
import { dongBoGiaGio, layDiaChiKhachHang, layThongTinKhach, layKhachId, coPhienKhachHang, datHang, xoaGioHang, kiemTraVoucher, layVoucherKhaDung, taoMaVnPay, trangThaiVnPay, huyVnPay, tinhPhiVanChuyen } from '../services/gio-hang';
import { layPhuongXaHaiCap, layTinhThanhHaiCap } from '../services/dia-chi';
import { chuanHoaDiaChi, dinhDangDiaChi, doiChieuDiaChiHaiCap, taoPayloadDiaChi } from '../utils/dia-chi';
import { ketNoiSanPhamRealtime } from '../services/san-pham-realtime';
import { gioHangStore } from '../stores/gio-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showWarning, showSuccess, showError, showConfirm, showBigSuccess } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import { resolveHinhAnh } from '../utils/resolve-image';
import anhMacDinh from '../assets/login-shoe.png';
import logoGhn from '../assets/logo/Logo-GHN-Blue-Orange.webp';
import logoVietQr from '../assets/logo/6793a971ea52dda5c8bfec82_vietqr.webp';

const router = useRouter();

const gio = ref({ id: null, items: [], tongSoLuong: 0, tongTien: 0 });
const diaChiList = ref([]);
const diaChiChonId = ref(null);
const dangTai = ref(true);
const khachHangIdPhien = ref(coPhienKhachHang() ? layKhachId() : null);
const daDangNhap = computed(() => Boolean(khachHangIdPhien.value));

const form = ref({
  hoTen: '',
  sdt: '',
  email: '',
  tinhThanh: '',
  tinhThanhCode: '',
  phuongXaCode: '',
  phuongXa: '',
  diaChiCuThe: '',
});

// ===== Danh mục hành chính Việt Nam hai cấp =====
const dsTinh = ref([]);
const dsXa = ref([]);
const diaChiChon = ref({ tinhThanhCode: null, phuongXaCode: null });

let phienDongBoDiaChi = 0;

async function onChonTinh(tinhThanhCode) {
  phienDongBoDiaChi += 1;
  const opt = dsTinh.value.find((t) => String(t.code) === String(tinhThanhCode));
  diaChiChon.value = { tinhThanhCode: opt?.code ?? null, phuongXaCode: null };
  form.value.tinhThanhCode = opt?.code ? String(opt.code) : '';
  form.value.tinhThanh = opt?.ten || '';
  form.value.phuongXa = '';
  dsXa.value = [];
  if (opt?.code) {
    try { dsXa.value = await layPhuongXaHaiCap(opt.code); } catch { dsXa.value = []; }
  }
}

function onChonXa(wardCode) {
  const opt = dsXa.value.find((x) => String(x.code) === String(wardCode));
  diaChiChon.value = { ...diaChiChon.value, phuongXaCode: opt?.code ?? null };
  form.value.phuongXaCode = opt?.code || '';
  form.value.phuongXa = opt?.ten || '';
}

// Đối chiếu địa chỉ chỉ lưu tên với mã hành chính hiện hành để chọn sẵn dropdown.
async function dongBoDiaChiHaiCap(dc) {
  const phienHienTai = ++phienDongBoDiaChi;
  diaChiChon.value = { tinhThanhCode: null, phuongXaCode: null };
  dsXa.value = [];
  try {
    if (!dsTinh.value.length) dsTinh.value = await layTinhThanhHaiCap();
    const ketQua = await doiChieuDiaChiHaiCap(dc, dsTinh.value, layPhuongXaHaiCap);
    if (phienHienTai !== phienDongBoDiaChi) return;
    dsXa.value = ketQua.danhSachPhuongXa;
    Object.assign(form.value, ketQua.diaChi);
    diaChiChon.value = {
      tinhThanhCode: ketQua.diaChi.tinhThanhCode || null,
      phuongXaCode: ketQua.diaChi.phuongXaCode || null,
    };
  } catch {
    // Không đối chiếu được thì giữ mã trống để người dùng chọn lại địa chỉ hiện hành.
  }
}

const hinhThucThanhToan = ref('COD');
const dangDat = ref(false);
const daDatHang = ref(false);

// Voucher
const maVoucher = ref('');
const voucher = ref(null); // { ma, ten, tienGiam, tongTienHang, tongTienSauGiam }
const dangApVoucher = ref(false);

// Danh sách voucher khả dụng (toàn sàn + voucher riêng được gửi cho khách)
const dsVoucher = ref([]);
const hienDsVoucher = ref(false);
const dangTaiVoucher = ref(false);

// Voucher tốt nhất = phiếu đủ điều kiện, giảm nhiều nhất (BE đã sắp xếp -> lấy cái đủ điều kiện đầu tiên).
const voucherTotNhat = computed(() =>
  dsVoucher.value.find((v) => v.apDung && Number(v.tienGiam) > 0) || null,
);

function moTaGiamVoucher(v) {
  if (Number(v.loai) === 1) {
    return `Giảm ${Number(v.giaTri)}%` + (v.giamToiDa ? ` (tối đa ${dinhDangTienViet(v.giamToiDa)})` : '');
  }
  return `Giảm ${dinhDangTienViet(v.giaTri)}`;
}

async function moDanhSachVoucher() {
  hienDsVoucher.value = true;
  if (dangTaiVoucher.value) return;
  dangTaiVoucher.value = true;
  try {
    // Luôn tải mới: BE chỉ trả phiếu đang hoạt động -> phiếu đã bị ngừng tự không hiện.
    dsVoucher.value = await layVoucherKhaDung();
  } catch {
    dsVoucher.value = [];
  } finally {
    dangTaiVoucher.value = false;
  }
}

// Đồng bộ lại danh sách voucher (vd admin vừa ngừng 1 phiếu -> phiếu đó tự ẩn khỏi danh sách).
async function taiLaiDsVoucherNeuMo() {
  if (dangHienQr.value) return;
  if (!hienDsVoucher.value && !dsVoucher.value.length) return;
  try {
    dsVoucher.value = await layVoucherKhaDung();
  } catch {
    // lỗi mạng -> giữ danh sách hiện tại
  }
}

async function chonVoucher(v) {
  if (!v.apDung) return;
  maVoucher.value = v.ma;
  hienDsVoucher.value = false;
  await apVoucher();
}

// VNPay (giả lập)
const qrVnPay = ref(null); // { token, qrData, maGiaoDich, hetHanLuc }
// Khi mã QR đang mở, voucher + hàng đã bị BE giữ chỗ (trừ lượt) cho phiên này.
// Vì vậy toàn bộ giao diện phía sau phải "đứng im": không đồng bộ lại giỏ, không kiểm tra
// lại voucher (kiểm sẽ báo "đã dùng/hết hạn" do chính phiên này giữ), không tính lại phí ship.
const dangHienQr = computed(() => Boolean(qrVnPay.value));

// Khóa cuộn trang nền khi QR đang mở để nền thực sự đứng yên.
watch(dangHienQr, (dangMo) => {
  document.body.style.overflow = dangMo ? 'hidden' : '';
});

let pollTimer = null;
let countdownTimer = null;
const soGiayQrConLai = ref(0);
const thoiGianQrConLai = computed(() => {
  const phut = Math.floor(soGiayQrConLai.value / 60);
  const giay = soGiayQrConLai.value % 60;
  return `${String(phut).padStart(2, '0')}:${String(giay).padStart(2, '0')}`;
});

// Phí vận chuyển (GHN) - tính lại mỗi khi địa chỉ thay đổi.
const phiShip = ref(null); // { phiVanChuyen, uocTinh, moTa, nguonTinhPhi, giaCu }
const dangTinhPhi = ref(false);
const loiPhiShip = ref('');
const phiShipSo = computed(() => Number(phiShip.value?.phiVanChuyen || 0));

const tienHang = computed(() =>
  voucher.value ? Number(voucher.value.tongTienSauGiam) : Number(gio.value.tongTien || 0)
);
const tongThanhToan = computed(() => tienHang.value + phiShipSo.value);

// Tổng cân nặng đơn (gram) = tổng (cân nặng mỗi giày × số lượng).
const tongCanNang = computed(() =>
  (gio.value.items || []).reduce(
    (tong, it) => tong + Number(it.canNang || 0) * Number(it.soLuong || 0),
    0,
  ),
);

let phiTimer = null;
async function capNhatPhiShip() {
  // Số tiền trên mã QR đã chốt -> không được đổi phí ship khi QR còn hiệu lực.
  if (dangHienQr.value) return;
  const f = form.value;
  if (!f.tinhThanh.trim() || !f.phuongXa.trim() || !f.diaChiCuThe.trim()) {
    phiShip.value = null;
    loiPhiShip.value = '';
    return;
  }
  dangTinhPhi.value = true;
  loiPhiShip.value = '';
  try {
    const res = await tinhPhiVanChuyen(taoPayloadDiaChi(f));
    if (res && res.phiVanChuyen != null) {
      phiShip.value = res;
    } else {
      phiShip.value = {
        phiVanChuyen: 35000,
        uocTinh: false,
        moTa: '',
        nguonTinhPhi: 'DEFAULT_FALLBACK',
      };
    }
  } catch (error) {
    // Khi API GHN bị lỗi hoặc tắt mạng trên máy local -> đặt phí mặc định 35k và ẩn logo GHN
    phiShip.value = {
      phiVanChuyen: 35000,
      uocTinh: false,
      moTa: '',
      nguonTinhPhi: 'DEFAULT_FALLBACK',
    };
    loiPhiShip.value = '';
  } finally {
    dangTinhPhi.value = false;
  }
}

// Địa chỉ đổi -> tính lại phí ship (debounce 600ms để tránh gọi liên tục khi gõ).
watch(
  () => [form.value.tinhThanh, form.value.phuongXa, form.value.diaChiCuThe],
  () => {
    if (dangHienQr.value) return;
    if (phiTimer) clearTimeout(phiTimer);
    phiTimer = setTimeout(capNhatPhiShip, 600);
  }
);

let ngatRealtimeSP = null;
let timerDongBoSP = null;

function lenLichDongBo() {
  // QR đang mở -> nền đóng băng, mọi tín hiệu realtime/focus đều bỏ qua.
  if (dangHienQr.value) return;
  if (timerDongBoSP) clearTimeout(timerDongBoSP);
  timerDongBoSP = setTimeout(async () => {
    if (qrVnPay.value?.token) {
      await kiemTraTrangThaiVnPay();
    }
    await reSyncGio();
  }, 200);
}

function dongBoKhiQuayLaiTab() {
  if (document.visibilityState === 'visible') {
    if (qrVnPay.value?.token) {
      kiemTraTrangThaiVnPay();
    }
    lenLichDongBo();
  }
}

onMounted(() => {
  tai();
  // Realtime: admin đổi giá / ngừng bán / ngừng phiếu / bán tại quầy -> đồng bộ lại giỏ + kiểm QR ngay lập tức.
  ngatRealtimeSP = ketNoiSanPhamRealtime({ onSanPhamThayDoi: lenLichDongBo });
  // Dự phòng khi SSE lỡ tín hiệu: quay lại tab/cửa sổ này thì kiểm lại ngay (không cần reload).
  window.addEventListener('focus', () => {
    if (qrVnPay.value?.token) {
      kiemTraTrangThaiVnPay();
    }
    lenLichDongBo();
  });
  document.addEventListener('visibilitychange', dongBoKhiQuayLaiTab);
});

async function reSyncGio() {
  if (dangHienQr.value) return;
  try {
    const ketQua = await dongBoGiaGio();
    gio.value = ketQua;
    gioHangStore.datSoLuong(gio.value.tongSoLuong);
    if (ketQua.removedNames && ketQua.removedNames.length > 0) {
      for (const tenSP of ketQua.removedNames) {
        showWarning(`Sản phẩm "${tenSP}" đã ngừng hoạt động, vui lòng chọn sản phẩm khác.`);
      }
    }
  } catch {
    // bỏ qua lỗi mạng -> giữ giỏ hiện tại
  }
  // Phiếu đang áp vừa bị ngừng -> gỡ + báo ngay (không đợi tới lúc bấm thanh toán).
  // Không kiểm tra khi đang mở QR thanh toán vì voucher đã được khóa trên đơn.
  if (!qrVnPay.value) {
    await kiemTraLaiVoucher();
  }
  // Danh sách voucher: phiếu vừa bị ad ngừng tự ẩn khỏi danh sách (không hiện disabled nữa).
  await taiLaiDsVoucherNeuMo();
}

onUnmounted(() => {
  document.body.style.overflow = '';
  if (qrVnPay.value?.token) {
    huyVnPay(qrVnPay.value.token);
  }
  dungPoll();
  ngatRealtimeSP?.();
  if (timerDongBoSP) clearTimeout(timerDongBoSP);
  window.removeEventListener('focus', lenLichDongBo);
  document.removeEventListener('visibilitychange', dongBoKhiQuayLaiTab);
});

onBeforeRouteLeave(() => {
  if (qrVnPay.value?.token) {
    huyVnPay(qrVnPay.value.token);
  }
  return true;
});

async function tai() {
  dangTai.value = true;
  try {
    const [g, dc, tinh] = await Promise.all([
      dongBoGiaGio(),
      layDiaChiKhachHang(),
      layTinhThanhHaiCap().catch(() => []),
    ]);
    gio.value = g;
    gioHangStore.datSoLuong(g.tongSoLuong);
    if (g.removedNames && g.removedNames.length > 0) {
      for (const tenSP of g.removedNames) {
        showWarning(`Sản phẩm "${tenSP}" đã ngừng hoạt động, vui lòng chọn sản phẩm khác.`);
      }
    }
    diaChiList.value = dc;
    dsTinh.value = tinh;

    const macDinh = dc.find((d) => d.laMacDinh) || dc[0];
    if (macDinh) {
      await chonDiaChi(macDinh);
    } else {
      // Chưa có địa chỉ lưu sẵn: lấy tên + SĐT từ thông tin khách.
      const kh = layThongTinKhach();
      form.value.hoTen = kh?.hoTen || '';
      form.value.sdt = kh?.sdt || '';
    }
  } catch {
    gio.value = { id: null, items: [], tongSoLuong: 0, tongTien: 0 };
  } finally {
    dangTai.value = false;
  }
}

async function chonDiaChi(dc) {
  diaChiChonId.value = dc.id;
  form.value = {
    hoTen: dc.hoTen || '',
    sdt: dc.sdt || '',
    email: form.value.email || '',
    ...chuanHoaDiaChi(dc),
  };
  await dongBoDiaChiHaiCap(dc);
}

const diaChiDayDu = computed(() => {
  const f = form.value;
  return dinhDangDiaChi(f);
});

async function apVoucher() {
  if (!maVoucher.value.trim()) return showWarning('Vui lòng nhập mã giảm giá.');
  dangApVoucher.value = true;
  try {
    voucher.value = await kiemTraVoucher(maVoucher.value.trim());
    showSuccess('Áp mã giảm giá thành công!');
  } catch (e) {
    voucher.value = null;
    showError(getDisplayErrorMessage(e, 'Mã giảm giá không hợp lệ'));
  } finally {
    dangApVoucher.value = false;
  }
}

function boVoucher() {
  voucher.value = null;
  maVoucher.value = '';
}

// Kiểm tra lại phiếu đang áp còn hiệu lực không (vd admin vừa ngừng hoạt động phiếu).
// Trả về true nếu hợp lệ; nếu không -> gỡ phiếu + báo và trả về false (chặn thanh toán).
async function kiemTraLaiVoucher() {
  // Đang trong phiên thanh toán QR: voucher đã khóa vào đơn hàng, không kiểm lại hay gỡ giữa chừng
  if (qrVnPay.value) return true;
  if (!voucher.value) return true;
  // Voucher đã bị trừ lượt lúc sinh mã QR -> kiểm lại lúc này chắc chắn báo sai.
  if (dangHienQr.value) return true;
  // Giỏ đang có SP ngừng bán/hết -> đó là lỗi sản phẩm (chuanBi sẽ ném), không phải voucher.
  // Bỏ qua kiểm voucher để phần kiểm sản phẩm báo đúng thông điệp.
  if (coSanPhamKhongBan()) return true;
  try {
    voucher.value = await kiemTraVoucher(voucher.value.ma);
    return true;
  } catch {
    voucher.value = null;
    maVoucher.value = '';
    showError('Phiếu giảm giá đã bị ngừng hoạt động, vui lòng chọn phiếu khác.');
    return false;
  }
}

function taoPayload() {
  const f = form.value;
  return {
    tenNguoiNhan: f.hoTen.trim(),
    sdtNguoiNhan: f.sdt.trim(),
    diaChiGiaoHang: taoPayloadDiaChi(f),
    hinhThucThanhToan: hinhThucThanhToan.value,
    maPhieuGiamGia: voucher.value?.ma || null,
    // Khách vãng lai có thể nhập email để nhận xác nhận đơn (tùy chọn).
    emailNguoiNhan: f.email.trim() || null,
  };
}

function hopLeThongTin() {
  const f = form.value;
  if (!f.hoTen.trim()) { showWarning('Vui lòng nhập tên người nhận.'); return false; }
  if (!/^\d{10}$/.test(f.sdt.trim())) { showWarning('Số điện thoại phải gồm 10 chữ số.'); return false; }
  if (!f.tinhThanh.trim() || !f.phuongXa.trim() || !f.diaChiCuThe.trim()) {
    showWarning('Vui lòng nhập đầy đủ địa chỉ giao hàng.');
    return false;
  }
  // Khách vãng lai bắt buộc nhập email vì đây là đầu mối duy nhất để nhận xác nhận / theo dõi đơn.
  if (!daDangNhap.value && !f.email.trim()) {
    showWarning('Vui lòng nhập email để nhận xác nhận và theo dõi đơn hàng.');
    return false;
  }
  if (f.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email.trim())) {
    showWarning('Email không hợp lệ.');
    return false;
  }
  return true;
}

async function hoanTatDatHang(maHoaDon) {
  daDatHang.value = true;
  xoaGioHang();
  gioHangStore.datSoLuong(0);
  // Popup to giữa màn báo đặt hàng thành công (giống các màn khác), popup sống qua điều hướng.
  showBigSuccess(`Mã đơn hàng của bạn: <b>${maHoaDon}</b>`, 'Đặt hàng thành công!');

  // Mọi khách (đăng nhập hay vãng lai, COD hay VietQR) đều vào màn cảm ơn kèm mã đơn.
  // Màn này có sẵn thanh tiến trình trạng thái nên khách theo dõi được đơn ngay tại đây.
  await router.push({ name: 'tra-cuu-don', query: { ma: maHoaDon, moi: '1' } });
}

// 1 item đã ngừng bán (admin ngừng SP/biến thể) hoặc hết hàng.
function itemKhongBan(it) {
  return it.conBan === false || Number(it.tonKho) <= 0;
}
// Giỏ có sản phẩm đã ngừng bán hoặc hết hàng -> không cho đặt.
function coSanPhamKhongBan() {
  return (gio.value.items || []).some(itemKhongBan);
}

async function datHangMoi() {
  if (!hopLeThongTin()) return;
  if (!phiShip.value) await capNhatPhiShip();
  if (!phiShip.value) {
    return showError(loiPhiShip.value || 'Chưa tính được phí vận chuyển GHN cho địa chỉ này.');
  }
  // Đồng bộ giỏ mới nhất để biết SP còn bán không (tránh trạng thái cũ -> báo nhầm voucher).
  try { gio.value = await dongBoGiaGio(); } catch { /* lỗi mạng -> dùng trạng thái hiện có */ }
  if (coSanPhamKhongBan()) {
    return showError('Trong giỏ có sản phẩm đã hết hàng hoặc ngừng hoạt động, vui lòng kiểm tra lại giỏ hàng.');
  }
  // Phiếu đang áp có thể vừa bị admin ngừng -> kiểm tra lại trước khi thanh toán.
  if (!(await kiemTraLaiVoucher())) return;
  // Hộp xác nhận trước khi đặt/thanh toán.
  const xacNhan = await showConfirm(
    `Đặt hàng với tổng cộng <b>${dinhDangTienViet(tongThanhToan.value)}</b>?`,
    'Xác nhận đặt hàng',
    hinhThucThanhToan.value !== 'COD' ? 'Thanh toán' : 'Đặt hàng',
    'Hủy',
  );
  if (!xacNhan) return;
  // Chốt chủ đơn ngay trước lúc gửi request để không phụ thuộc localStorage thay đổi trong khi chờ thanh toán.
  const khachHangIdKhiDat = coPhienKhachHang() ? layKhachId() : null;
  khachHangIdPhien.value = khachHangIdKhiDat;
  if (hinhThucThanhToan.value === 'VNPAY' || hinhThucThanhToan.value === 'VIETQR') {
    return moThanhToanVnPay();
  }
  dangDat.value = true;
  try {
    const kq = await datHang(taoPayload());
    await hoanTatDatHang(kq.maHoaDon);
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể đặt hàng. Vui lòng thử lại.'));
  } finally {
    dangDat.value = false;
  }
}

// --- VNPay: hiện QR hoặc link redirect ---
async function moThanhToanVnPay() {
  dangDat.value = true;
  try {
    qrVnPay.value = await taoMaVnPay(taoPayload());
    if (qrVnPay.value && qrVnPay.value.qrData && qrVnPay.value.qrData.startsWith('http') && !qrVnPay.value.qrData.includes('qr.sepay.vn')) {
      window.open(qrVnPay.value.qrData, '_blank');
    }
    batDauPoll();
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể tạo mã thanh toán'));
  } finally {
    dangDat.value = false;
  }
}

// qrData từ backend đã là ảnh VietQR (SePay) -> dùng trực tiếp làm src.
const anhQrVnPay = computed(() => (qrVnPay.value ? qrVnPay.value.qrData : ''));

async function kiemTraTrangThaiVnPay() {
  if (!qrVnPay.value?.token) return;
  try {
    const tt = await trangThaiVnPay(qrVnPay.value.token);
    if (!tt) return;
    if (tt.trangThai === 'DA_THANH_TOAN') {
      dungPoll();
      const ma = tt.maHoaDon;
      qrVnPay.value = null;
      await hoanTatDatHang(ma);
    } else if (tt.trangThai === 'THAT_BAI') {
      dungPoll();
      qrVnPay.value = null;
      showError(tt.message || 'Số lượng sản phẩm hiện không còn đủ để đáp ứng đơn hàng. Phiên giao dịch đã được hủy, vui lòng chọn lại sản phẩm khác.');
      await reSyncGio();
    } else if (tt.trangThai === 'HET_HAN' || tt.trangThai === 'KHONG_TON_TAI') {
      await ngatHetHan();
    }
  } catch {
    // bỏ qua, thử lại lần poll sau
  }
}

function batDauPoll() {
  dungPoll();
  const capNhatDemNguoc = () => {
    const hetHan = qrVnPay.value?.hetHanLuc ? new Date(qrVnPay.value.hetHanLuc).getTime() : 0;
    soGiayQrConLai.value = Math.max(0, Math.ceil((hetHan - Date.now()) / 1000));
    if (hetHan && soGiayQrConLai.value <= 0) ngatHetHan();
  };
  capNhatDemNguoc();
  countdownTimer = setInterval(capNhatDemNguoc, 1000);
  pollTimer = setInterval(async () => {
    await kiemTraTrangThaiVnPay();
  }, 1500);
}

async function ngatHetHan() {
  const token = qrVnPay.value?.token;
  dungPoll();
  qrVnPay.value = null;
  // Nhả phiên ngay: BE hoàn lại lượt voucher và số lượng đang giữ, không đợi job dọn 60s.
  if (token) {
    try { await huyVnPay(token); } catch { /* job dọn phiên của BE sẽ xử lý nốt */ }
  }
  showError('Phiên thanh toán không còn hiệu lực, vui lòng đặt lại đơn.');
  await reSyncGio();
}

function dungPoll() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
}

async function dongVnPay() {
  const token = qrVnPay.value?.token;
  dungPoll();
  qrVnPay.value = null;
  try {
    await huyVnPay(token);
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể hủy phiên thanh toán'));
  } finally {
    await reSyncGio();
  }
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) event.target.src = anhMacDinh;
}
</script>

<template>
  <main class="invoice-flat bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-7xl px-6 lg:px-10 pt-10">
      <h1 class="text-3xl font-bold text-slate-900 mb-8">Thanh toán</h1>

      <div v-if="dangTai" class="py-24 text-center text-sm text-slate-400">Đang tải...</div>

      <div v-else-if="!gio.items.length" class="py-24 text-center">
        <p class="text-sm text-slate-500 mb-4">Giỏ hàng trống, không có gì để thanh toán.</p>
        <router-link to="/khachhang/san-pham" class="inline-flex rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white hover:bg-primary/90">Mua sắm ngay</router-link>
      </div>

      <div v-else class="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8">
        <!-- Thông tin giao hàng -->
        <div class="space-y-6">
          <!-- Chọn địa chỉ đã lưu -->
          <section v-if="diaChiList.length" class="rounded-2xl bg-white border border-slate-100 p-6 shadow-sm">
            <h2 class="text-base font-bold text-slate-900 mb-4">Chọn địa chỉ đã lưu</h2>
            <div class="space-y-3">
              <label
                v-for="dc in diaChiList"
                :key="dc.id"
                class="flex cursor-pointer gap-3 rounded-xl border p-4 transition"
                :class="diaChiChonId === dc.id ? 'border-primary bg-primary/5' : 'border-slate-200 hover:border-slate-300'"
              >
                <input type="radio" :value="dc.id" :checked="diaChiChonId === dc.id" @change="chonDiaChi(dc)" class="mt-1 text-primary focus:ring-primary/30" />
                <div class="text-sm">
                  <p class="font-semibold text-slate-800">
                    {{ dc.hoTen }} · {{ dc.sdt }}
                    <span v-if="dc.laMacDinh" class="ml-2 rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-600">Mặc định</span>
                  </p>
                  <p class="mt-1 text-slate-500">{{ dinhDangDiaChi(dc) }}</p>
                </div>
              </label>
            </div>
          </section>

          <!-- Form thông tin người nhận -->
          <section class="rounded-2xl bg-white border border-slate-100 p-6 shadow-sm">
            <h2 class="text-base font-bold text-slate-900 mb-1">Thông tin người nhận</h2>
            <p v-if="!daDangNhap" class="mb-4 text-xs text-slate-400">
              Bạn đang đặt hàng không cần tài khoản. Vui lòng nhập đầy đủ thông tin nhận hàng và email để nhận xác nhận / theo dõi đơn.
            </p>
            <div v-else class="mb-4"></div>
            <div class="grid gap-4 sm:grid-cols-2">
              <label class="space-y-1.5">
                <span class="text-sm font-medium text-slate-600">Họ và tên người nhận <span class="text-rose-500">*</span></span>
                <input v-model="form.hoTen" type="text" placeholder="Nhập họ tên" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
              </label>
              <label class="space-y-1.5">
                <span class="text-sm font-medium text-slate-600">Số điện thoại <span class="text-rose-500">*</span></span>
                <input v-model="form.sdt" type="tel" maxlength="10" placeholder="Nhập SĐT" @input="form.sdt = form.sdt.replace(/\D/g, '')" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
              </label>
              <label class="space-y-1.5">
                <span class="text-sm font-medium text-slate-600">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
                <select :value="diaChiChon.tinhThanhCode ?? ''" @change="onChonTinh($event.target.value)" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 bg-white">
                  <option value="" disabled>{{ dsTinh.length ? 'Chọn tỉnh/thành' : 'Đang tải...' }}</option>
                  <option v-for="t in dsTinh" :key="t.code" :value="t.code">{{ t.ten }}</option>
                </select>
              </label>
              <label class="space-y-1.5">
                <span class="text-sm font-medium text-slate-600">Phường/Xã <span class="text-rose-500">*</span></span>
                <select :value="diaChiChon.phuongXaCode ?? ''" @change="onChonXa($event.target.value)" :disabled="!dsXa.length" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 bg-white disabled:bg-slate-50 disabled:text-slate-400">
                  <option value="" disabled>{{ diaChiChon.tinhThanhCode ? 'Chọn phường/xã' : 'Chọn tỉnh/thành trước' }}</option>
                  <option v-for="x in dsXa" :key="x.code" :value="x.code">{{ x.ten }}</option>
                </select>
              </label>
              <label class="space-y-1.5 sm:col-span-2">
                <span class="text-sm font-medium text-slate-600">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
                <input v-model="form.diaChiCuThe" type="text" placeholder="Số nhà, tên đường..." class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
              </label>
              <label v-if="!daDangNhap" class="space-y-1.5 sm:col-span-2">
                <span class="text-sm font-medium text-slate-600">Email nhận xác nhận đơn <span class="text-rose-500">*</span></span>
                <input v-model="form.email" type="email" placeholder="email@example.com" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
                <span class="block text-xs text-slate-400">Xác nhận & chi tiết đơn sẽ gửi về email này. Bạn dùng email để theo dõi đơn (không có tài khoản nên hãy lưu lại).</span>
              </label>
            </div>
            <p v-if="diaChiDayDu" class="mt-4 rounded-xl bg-slate-50 px-4 py-3 text-sm text-slate-500">
              Giao đến: <span class="font-medium text-slate-700">{{ diaChiDayDu }}</span>
            </p>
          </section>

          <!-- Phương thức thanh toán -->
          <section class="rounded-2xl bg-white border border-slate-100 p-6 shadow-sm">
            <h2 class="text-base font-bold text-slate-900 mb-4">Phương thức thanh toán</h2>
            <label
              class="flex cursor-pointer items-center gap-3 rounded-xl border p-4 transition"
              :class="hinhThucThanhToan === 'COD' ? 'border-primary bg-primary/5' : 'border-slate-200 hover:border-slate-300'"
            >
              <input type="radio" value="COD" v-model="hinhThucThanhToan" class="text-primary focus:ring-primary/30" />
              <span class="rounded bg-slate-100 px-2 py-0.5 text-xs font-semibold text-slate-600 border border-slate-200 uppercase tracking-wider shrink-0">COD</span>
              <span class="text-sm font-medium text-slate-700">Thanh toán khi nhận hàng</span>
            </label>
            
            <label
              class="mt-3 flex cursor-pointer items-center gap-3 rounded-xl border p-4 transition"
              :class="hinhThucThanhToan === 'VIETQR' ? 'border-primary bg-primary/5' : 'border-slate-200 hover:border-slate-300'"
            >
              <input type="radio" value="VIETQR" v-model="hinhThucThanhToan" class="text-primary focus:ring-primary/30" />
              <img :src="logoVietQr" alt="VietQR" class="h-6 object-contain shrink-0" />
            </label>

            <label
              class="mt-3 flex cursor-pointer items-center gap-3 rounded-xl border p-4 transition"
              :class="hinhThucThanhToan === 'VNPAY' ? 'border-primary bg-primary/5' : 'border-slate-200 hover:border-slate-300'"
            >
              <input type="radio" value="VNPAY" v-model="hinhThucThanhToan" class="text-primary focus:ring-primary/30" />
              <img src="https://static.cdnlogo.com/logos/v/99/vnpay.svg" alt="VNPAY" class="h-4 object-contain shrink-0" />
            </label>
          </section>
        </div>

        <!-- Tóm tắt đơn -->
        <aside class="h-fit rounded-2xl bg-white border border-slate-100 p-6 shadow-sm">
          <h2 class="text-base font-bold text-slate-900 mb-4">Đơn hàng ({{ gio.tongSoLuong }} sản phẩm)</h2>
          <div class="space-y-3 max-h-72 overflow-y-auto pr-1">
            <div v-for="item in gio.items" :key="item.id" class="flex gap-3" :class="{ 'opacity-60': itemKhongBan(item) }">
              <img :src="resolveHinhAnh(item.hinhAnh) || anhMacDinh" :alt="item.tenSanPham" class="h-14 w-14 shrink-0 rounded-lg object-cover bg-slate-50" :class="{ grayscale: itemKhongBan(item) }" @error="xuLyAnhLoi" />
              <div class="flex-1 text-sm">
                <p class="font-medium text-slate-800 line-clamp-1">{{ item.tenSanPham }}</p>
                <p class="text-xs text-slate-400">{{ item.mauSac }} · {{ item.kichCo }} · x{{ item.soLuong }}</p>
                <p v-if="itemKhongBan(item)" class="mt-0.5 text-[11px] font-semibold text-rose-500">
                  {{ Number(item.tonKho) <= 0 ? 'Đã hết hàng' : 'Đã ngừng hoạt động' }}
                </p>
              </div>
              <p class="text-sm font-semibold text-slate-700">{{ dinhDangTienViet(Number(item.giaBan) * Number(item.soLuong)) }}</p>
            </div>
          </div>
          <!-- Mã giảm giá (khách vãng lai dùng được voucher toàn sàn) -->
          <div class="mt-4 border-t border-slate-100 pt-4">
            <div v-if="!voucher">
              <button
                @click="moDanhSachVoucher"
                class="flex w-full items-center justify-between rounded-xl border border-dashed border-slate-300 px-4 py-3 text-sm font-semibold text-slate-600 transition hover:border-primary hover:text-primary"
              >
                <span class="flex items-center gap-2">
                  <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 5H5a2 2 0 0 0-2 2v3a2 2 0 0 1 0 4v3a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-3a2 2 0 0 1 0-4V7a2 2 0 0 0-2-2h-4" /><path d="M9 3v18" /></svg>
                  Chọn voucher
                </span>
                <svg class="h-4 w-4 text-slate-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m9 18 6-6-6-6" /></svg>
              </button>

              <!-- Danh sách voucher khả dụng -->
              <div v-if="hienDsVoucher" class="mt-2 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-lg">
                <div class="flex items-center justify-between border-b border-slate-100 px-3 py-2">
                  <span class="text-xs font-semibold text-slate-500">Voucher có thể dùng</span>
                  <button @click="hienDsVoucher = false" class="text-xs font-medium text-slate-400 hover:text-slate-600">Đóng</button>
                </div>
                <p v-if="dangTaiVoucher" class="px-3 py-5 text-center text-xs text-slate-400">Đang tải...</p>
                <p v-else-if="!dsVoucher.length" class="px-3 py-5 text-center text-xs text-slate-400">Bạn chưa có voucher nào dùng được.</p>
                <ul v-else class="max-h-64 divide-y divide-slate-50 overflow-y-auto">
                  <li v-for="v in dsVoucher" :key="v.phieuId">
                    <button
                      @click="chonVoucher(v)"
                      :disabled="!v.apDung"
                      class="flex w-full items-start gap-3 px-3 py-2.5 text-left transition hover:bg-primary/5 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-transparent"
                    >
                      <div class="min-w-0 flex-1">
                        <div class="flex flex-wrap items-center gap-1.5">
                          <span class="text-sm font-semibold text-slate-800">{{ moTaGiamVoucher(v) }}</span>
                          <span v-if="voucherTotNhat && v.phieuId === voucherTotNhat.phieuId" class="rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-bold text-amber-700">⭐ Tốt nhất</span>
                          <span v-if="v.rieng" class="rounded-full bg-primary/10 px-2 py-0.5 text-[10px] font-bold text-primary">Của bạn</span>
                        </div>
                        <p class="mt-0.5 text-xs text-slate-500">Mã: <span class="font-medium text-slate-700">{{ v.ma }}</span></p>
                        <p v-if="v.giaTriToiThieu" class="text-[11px]" :class="v.apDung ? 'text-slate-400' : 'text-rose-500'">
                          Đơn tối thiểu {{ dinhDangTienViet(v.giaTriToiThieu) }}{{ v.apDung ? '' : ' (chưa đủ)' }}
                        </p>
                      </div>
                      <span v-if="v.apDung && v.tienGiam > 0" class="shrink-0 text-xs font-semibold text-emerald-600">-{{ dinhDangTienViet(v.tienGiam) }}</span>
                    </button>
                  </li>
                </ul>
              </div>
            </div>
            <div v-else class="flex items-center justify-between rounded-xl bg-emerald-50 px-3 py-2.5">
              <span class="text-sm font-semibold text-emerald-700">✓ Đã áp mã {{ voucher.ma }}</span>
              <button @click="boVoucher" class="text-xs font-medium text-rose-500 hover:underline">Bỏ</button>
            </div>
          </div>

          <!-- Tổng tiền -->
          <div class="mt-4 space-y-2 border-t border-slate-100 pt-4">
            <div class="flex items-center justify-between text-sm text-slate-500">
              <span>Tạm tính</span>
              <span>{{ dinhDangTienViet(gio.tongTien) }}</span>
            </div>
            <div v-if="voucher" class="flex items-center justify-between text-sm font-medium text-emerald-600">
              <span>Giảm giá</span>
              <span>-{{ dinhDangTienViet(voucher.tienGiam) }}</span>
            </div>
            <div class="flex items-center justify-between text-sm text-slate-500">
              <span class="flex items-center gap-1.5">
                Phí vận chuyển
                <img v-if="phiShip && phiShip.nguonTinhPhi === 'GHN_LIVE'" :src="logoGhn" alt="GHN" class="h-4 w-auto object-contain" />
              </span>
              <span v-if="dangTinhPhi" class="text-slate-400">Đang tính...</span>
              <span v-else-if="phiShip" class="font-semibold text-slate-700">{{ dinhDangTienViet(phiShipSo) }}</span>
              <span v-else class="text-xs" :class="loiPhiShip ? 'text-rose-500' : 'text-slate-400'">
                {{ loiPhiShip || 'Nhập địa chỉ để tính' }}
              </span>
            </div>
            <div class="flex items-center justify-between pt-1">
              <span class="text-sm font-semibold text-slate-700">Tổng cộng</span>
              <span class="text-xl font-bold text-primary">{{ dinhDangTienViet(tongThanhToan) }}</span>
            </div>
          </div>
          <button
            @click="datHangMoi"
            :disabled="dangDat || coSanPhamKhongBan()"
            class="mt-6 w-full rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3.5 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5 disabled:opacity-60 disabled:cursor-not-allowed disabled:hover:translate-y-0"
          >
            {{ dangDat ? 'Đang xử lý...' : (hinhThucThanhToan !== 'COD' ? 'Thanh toán' : 'Đặt hàng') }}
          </button>
          <p v-if="coSanPhamKhongBan()" class="mt-2 text-center text-xs font-medium text-rose-500">
            Có sản phẩm đã hết hàng hoặc ngừng hoạt động. Vui lòng quay lại giỏ hàng để kiểm tra.
          </p>
          <router-link to="/khachhang/gio-hang" class="mt-3 block text-center text-sm font-medium text-slate-500 hover:text-primary">Quay lại giỏ hàng</router-link>
        </aside>
      </div>
    </div>

    <!-- Modal QR chuyển khoản VietQR (SePay) -->
    <Teleport to="body">
      <div v-if="qrVnPay" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
        <div class="w-full max-w-sm rounded-3xl bg-white p-7 text-center shadow-2xl">
          <template v-if="anhQrVnPay.startsWith('http') && !anhQrVnPay.includes('qr.sepay.vn') && !anhQrVnPay.includes('vietqr.io')">
            <h3 class="text-lg font-bold text-slate-900">Thanh toán trực tuyến VNPAY</h3>
            <p class="mt-2 text-sm text-slate-400">Đơn hàng: <b>{{ qrVnPay.maGiaoDich }}</b></p>
            <div class="mt-6 mb-6 flex flex-col items-center justify-center min-h-[140px]">
              <a :href="anhQrVnPay" target="_blank" class="inline-flex items-center justify-center gap-2 rounded-2xl bg-sky-600 hover:bg-sky-700 text-white px-6 py-3 text-sm font-bold shadow-md transition duration-200">
                Thanh toán trên VNPAY
              </a>
              <p class="mt-3 text-xs text-slate-400 text-center max-w-[280px]">
                Nếu trình duyệt không tự mở cổng thanh toán, vui lòng nhấp vào nút xanh phía trên để thực hiện.
              </p>
            </div>
          </template>
          <template v-else>
            <h3 class="text-lg font-bold text-slate-900">Quét VietQR để chuyển khoản</h3>
            <p class="mt-1 text-sm text-slate-400">Nội dung CK: <b>{{ qrVnPay.maGiaoDich }}</b></p>
            <div class="mt-5 flex justify-center">
              <img :src="anhQrVnPay" alt="VietQR" class="h-64 w-64 rounded-xl border border-slate-100" />
            </div>
            <p class="mt-3 text-xs text-slate-400">Mở app ngân hàng, quét mã — số tiền & nội dung tự điền. Đơn sẽ tự tạo sau khi nhận được tiền.</p>
          </template>
          <div class="mt-4 flex items-center justify-center gap-2 text-sm text-slate-500">
            <span class="inline-block h-2 w-2 animate-pulse rounded-full bg-emerald-500"></span>
            Đang chờ thanh toán...
            <span v-if="soGiayQrConLai > 0" class="font-mono font-bold text-rose-500">({{ thoiGianQrConLai }})</span>
          </div>
          <p class="mt-2 text-center text-[11px] text-slate-400">Mã QR có hiệu lực trong 5 phút. Vui lòng thanh toán trước khi hết hạn.</p>
          <button @click="dongVnPay" class="mt-5 w-full rounded-2xl border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50">
            Hủy
          </button>
        </div>
      </div>
    </Teleport>
  </main>
</template>
<style scoped>
.invoice-flat :deep([class*="rounded-"]:not(.rounded-full)) {
  border-radius: 6px !important;
}
  </style>
