<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter, onBeforeRouteLeave } from 'vue-router';
import { dongBoGiaGio, layDiaChiKhachHang, layThongTinKhach, layKhachId, datHang, xoaGioHang, kiemTraVoucher, layVoucherKhaDung, taoMaVnPay, trangThaiVnPay, tinhPhiVanChuyen, layTinhGhn, layHuyenGhn, layXaGhn } from '../services/gio-hang';
import { ketNoiSanPhamRealtime } from '../services/san-pham-realtime';
import { gioHangStore } from '../stores/gio-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showWarning, showSuccess, showError } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import anhMacDinh from '../assets/login-shoe.png';
import logoGhn from '../assets/logo/Logo-GHN-Blue-Orange.webp';

const router = useRouter();

const gio = ref({ id: null, items: [], tongSoLuong: 0, tongTien: 0 });
const diaChiList = ref([]);
const diaChiChonId = ref(null);
const dangTai = ref(true);
const daDangNhap = computed(() => Boolean(layKhachId()));

const form = ref({
  hoTen: '',
  sdt: '',
  email: '',
  tinhThanh: '',
  quanHuyen: '',
  phuongXa: '',
  diaChiCuThe: '',
});

// ===== Địa giới GHN (dropdown đổ tầng: tỉnh -> huyện -> xã) =====
const dsTinh = ref([]);
const dsHuyen = ref([]);
const dsXa = ref([]);
const ghn = ref({ tinhId: null, huyenId: null, wardCode: null });

function chuanHoaTen(s) {
  return String(s || '')
    .normalize('NFD').replace(/[̀-ͯ]/g, '')
    .replace(/đ/gi, 'd')
    .toLowerCase()
    .replace(/\b(tinh|thanh pho|tp|quan|huyen|thi xa|phuong|xa|thi tran)\b/g, ' ')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
}

function timTheoTen(list, ten, layTen) {
  const muc = chuanHoaTen(ten);
  if (!muc) return null;
  return list.find((o) => chuanHoaTen(layTen(o)) === muc)
    || list.find((o) => {
      const t = chuanHoaTen(layTen(o));
      return t && (t.includes(muc) || muc.includes(t));
    })
    || null;
}

async function onChonTinh(tinhId) {
  const opt = dsTinh.value.find((t) => String(t.id) === String(tinhId));
  ghn.value = { tinhId: opt?.id ?? null, huyenId: null, wardCode: null };
  form.value.tinhThanh = opt?.ten || '';
  form.value.quanHuyen = '';
  form.value.phuongXa = '';
  dsHuyen.value = [];
  dsXa.value = [];
  if (opt?.id) {
    try { dsHuyen.value = await layHuyenGhn(opt.id); } catch { dsHuyen.value = []; }
  }
}

async function onChonHuyen(huyenId) {
  const opt = dsHuyen.value.find((h) => String(h.id) === String(huyenId));
  ghn.value = { ...ghn.value, huyenId: opt?.id ?? null, wardCode: null };
  form.value.quanHuyen = opt?.ten || '';
  form.value.phuongXa = '';
  dsXa.value = [];
  if (opt?.id) {
    try { dsXa.value = await layXaGhn(opt.id); } catch { dsXa.value = []; }
  }
}

function onChonXa(wardCode) {
  const opt = dsXa.value.find((x) => String(x.code) === String(wardCode));
  ghn.value = { ...ghn.value, wardCode: opt?.code ?? null };
  form.value.phuongXa = opt?.ten || '';
}

// Map địa chỉ đã lưu (dạng text) -> ID GHN để chọn sẵn dropdown (best-effort).
async function dongBoDiaChiGhn(dc) {
  ghn.value = { tinhId: null, huyenId: null, wardCode: null };
  dsHuyen.value = [];
  dsXa.value = [];
  try {
    if (!dsTinh.value.length) dsTinh.value = await layTinhGhn();
    const tinh = timTheoTen(dsTinh.value, dc.tinhThanh, (o) => o.ten);
    if (!tinh) return;
    ghn.value.tinhId = tinh.id;
    dsHuyen.value = await layHuyenGhn(tinh.id);
    const huyen = timTheoTen(dsHuyen.value, dc.quanHuyen, (o) => o.ten);
    if (!huyen) return;
    ghn.value.huyenId = huyen.id;
    dsXa.value = await layXaGhn(huyen.id);
    const xa = timTheoTen(dsXa.value, dc.phuongXa, (o) => o.ten);
    if (xa) ghn.value.wardCode = xa.code;
  } catch {
    // Không map được -> giữ ID null, GHN dò theo tên (có thể ra phí ước tính).
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
const qrVnPay = ref(null); // { token, qrData, maGiaoDich }
let pollTimer = null;

// Phí vận chuyển (GHN) - tính lại mỗi khi địa chỉ thay đổi.
const phiShip = ref(null); // { phiVanChuyen, uocTinh, moTa }
const dangTinhPhi = ref(false);
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
  const f = form.value;
  if (!f.tinhThanh.trim() || !f.quanHuyen.trim() || !f.phuongXa.trim()) {
    phiShip.value = null;
    return;
  }
  dangTinhPhi.value = true;
  try {
    phiShip.value = await tinhPhiVanChuyen({
      tinhThanh: f.tinhThanh.trim(),
      quanHuyen: f.quanHuyen.trim(),
      phuongXa: f.phuongXa.trim(),
      diaChiCuThe: f.diaChiCuThe.trim(),
      toDistrictId: ghn.value.huyenId,
      toWardCode: ghn.value.wardCode,
    });
  } catch {
    phiShip.value = null;
  } finally {
    dangTinhPhi.value = false;
  }
}

// Địa chỉ đổi -> tính lại phí ship (debounce 600ms để tránh gọi liên tục khi gõ).
watch(
  () => [form.value.tinhThanh, form.value.quanHuyen, form.value.phuongXa, form.value.diaChiCuThe],
  () => {
    if (phiTimer) clearTimeout(phiTimer);
    phiTimer = setTimeout(capNhatPhiShip, 600);
  }
);

let ngatRealtimeSP = null;
let timerDongBoSP = null;

function lenLichDongBo() {
  if (timerDongBoSP) clearTimeout(timerDongBoSP);
  timerDongBoSP = setTimeout(reSyncGio, 300);
}

function dongBoKhiQuayLaiTab() {
  if (document.visibilityState === 'visible') lenLichDongBo();
}

onMounted(() => {
  tai();
  // Realtime: admin đổi giá / ngừng bán / ngừng phiếu -> đồng bộ lại giỏ + phiếu (ngầm).
  ngatRealtimeSP = ketNoiSanPhamRealtime({ onSanPhamThayDoi: lenLichDongBo });
  // Dự phòng khi SSE lỡ tín hiệu: quay lại tab/cửa sổ này thì kiểm lại ngay (không cần reload).
  window.addEventListener('focus', lenLichDongBo);
  document.addEventListener('visibilitychange', dongBoKhiQuayLaiTab);
});

async function reSyncGio() {
  try {
    gio.value = await dongBoGiaGio();
  } catch {
    // bỏ qua lỗi mạng -> giữ giỏ hiện tại
  }
  // Phiếu đang áp vừa bị ngừng -> gỡ + báo ngay (không đợi tới lúc bấm thanh toán).
  await kiemTraLaiVoucher();
  // Danh sách voucher: phiếu vừa bị ad ngừng tự ẩn khỏi danh sách (không hiện disabled nữa).
  await taiLaiDsVoucherNeuMo();
}

onUnmounted(() => {
  dungPoll();
  ngatRealtimeSP?.();
  if (timerDongBoSP) clearTimeout(timerDongBoSP);
  window.removeEventListener('focus', lenLichDongBo);
  document.removeEventListener('visibilitychange', dongBoKhiQuayLaiTab);
});

onBeforeRouteLeave(() => {
  return true;
});

async function tai() {
  dangTai.value = true;
  try {
    const [g, dc, tinh] = await Promise.all([
      dongBoGiaGio(),
      layDiaChiKhachHang(),
      layTinhGhn().catch(() => []),
    ]);
    gio.value = g;
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
    tinhThanh: dc.tinhThanh || '',
    quanHuyen: dc.quanHuyen || '',
    phuongXa: dc.phuongXa || '',
    diaChiCuThe: dc.diaChiCuThe || '',
  };
  await dongBoDiaChiGhn(dc);
}

const diaChiDayDu = computed(() => {
  const f = form.value;
  return [f.diaChiCuThe, f.phuongXa, f.quanHuyen, f.tinhThanh].filter(Boolean).join(', ');
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
  if (!voucher.value) return true;
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
    tinhThanh: f.tinhThanh.trim(),
    quanHuyen: f.quanHuyen.trim(),
    phuongXa: f.phuongXa.trim(),
    diaChiCuThe: f.diaChiCuThe.trim(),
    hinhThucThanhToan: hinhThucThanhToan.value,
    maPhieuGiamGia: voucher.value?.ma || null,
    toDistrictId: ghn.value.huyenId,
    toWardCode: ghn.value.wardCode,
    // Khách vãng lai có thể nhập email để nhận xác nhận đơn (tùy chọn).
    emailNguoiNhan: f.email.trim() || null,
  };
}

function hopLeThongTin() {
  const f = form.value;
  if (!f.hoTen.trim()) { showWarning('Vui lòng nhập tên người nhận.'); return false; }
  if (!/^\d{10}$/.test(f.sdt.trim())) { showWarning('Số điện thoại phải gồm 10 chữ số.'); return false; }
  if (!f.tinhThanh.trim() || !f.quanHuyen.trim() || !f.phuongXa.trim() || !f.diaChiCuThe.trim()) {
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
  // Thay vì về trang sản phẩm -> sang màn cảm ơn + tra cứu đơn (hiện mã đơn + chi tiết hóa đơn).
  router.push({ path: '/khachhang/tra-cuu-don', query: { ma: maHoaDon, moi: '1' } });
}

// Giỏ có sản phẩm đã ngừng bán hoặc hết hàng -> không cho đặt.
function coSanPhamKhongBan() {
  return (gio.value.items || []).some(
    (it) => it.conBan === false || Number(it.tonKho) <= 0,
  );
}

async function datHangMoi() {
  if (!hopLeThongTin()) return;
  // Đồng bộ giỏ mới nhất để biết SP còn bán không (tránh trạng thái cũ -> báo nhầm voucher).
  try { gio.value = await dongBoGiaGio(); } catch { /* lỗi mạng -> dùng trạng thái hiện có */ }
  if (coSanPhamKhongBan()) {
    return showError('Trong giỏ có sản phẩm đã hết hàng hoặc ngừng bán. Vui lòng quay lại giỏ hàng để xóa.');
  }
  // Phiếu đang áp có thể vừa bị admin ngừng -> kiểm tra lại trước khi thanh toán.
  if (!(await kiemTraLaiVoucher())) return;
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

function batDauPoll() {
  dungPoll();
  // Không còn đếm ngược: mã QR sống đến khi khách thanh toán hoặc tự hủy
  // (tồn kho chỉ trừ khi nhân viên/admin xác nhận đơn nên không cần hết hạn).
  pollTimer = setInterval(async () => {
    if (!qrVnPay.value) return;
    try {
      const tt = await trangThaiVnPay(qrVnPay.value.token);
      if (tt.trangThai === 'DA_THANH_TOAN') {
        dungPoll();
        const ma = tt.maHoaDon;
        qrVnPay.value = null;
        await hoanTatDatHang(ma);
      } else if (tt.trangThai === 'HET_HAN' || tt.trangThai === 'KHONG_TON_TAI') {
        ngatHetHan();
      }
    } catch {
      // bỏ qua, thử lại lần poll sau
    }
  }, 2000);
}

function ngatHetHan() {
  dungPoll();
  qrVnPay.value = null;
  showError('Phiên thanh toán không còn hiệu lực, vui lòng đặt lại đơn.');
}

function dungPoll() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}

function dongVnPay() {
  dungPoll();
  qrVnPay.value = null;
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) event.target.src = anhMacDinh;
}
</script>

<template>
  <main class="invoice-flat bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-7xl px-6 lg:px-10 pt-10">
      <h1 class="text-3xl font-bold text-slate-900 mb-2">Thanh toán</h1>
      <p class="text-sm text-slate-400 mb-8">Bước 1: Thông tin giao hàng</p>

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
                  <p class="mt-1 text-slate-500">{{ [dc.diaChiCuThe, dc.phuongXa, dc.quanHuyen, dc.tinhThanh].filter(Boolean).join(', ') }}</p>
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
                <select :value="ghn.tinhId ?? ''" @change="onChonTinh($event.target.value)" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 bg-white">
                  <option value="" disabled>{{ dsTinh.length ? 'Chọn tỉnh/thành' : 'Đang tải...' }}</option>
                  <option v-for="t in dsTinh" :key="t.id" :value="t.id">{{ t.ten }}</option>
                </select>
              </label>
              <label class="space-y-1.5">
                <span class="text-sm font-medium text-slate-600">Quận/Huyện <span class="text-rose-500">*</span></span>
                <select :value="ghn.huyenId ?? ''" @change="onChonHuyen($event.target.value)" :disabled="!dsHuyen.length" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 bg-white disabled:bg-slate-50 disabled:text-slate-400">
                  <option value="" disabled>{{ ghn.tinhId ? 'Chọn quận/huyện' : 'Chọn tỉnh/thành trước' }}</option>
                  <option v-for="h in dsHuyen" :key="h.id" :value="h.id">{{ h.ten }}</option>
                </select>
              </label>
              <label class="space-y-1.5">
                <span class="text-sm font-medium text-slate-600">Phường/Xã <span class="text-rose-500">*</span></span>
                <select :value="ghn.wardCode ?? ''" @change="onChonXa($event.target.value)" :disabled="!dsXa.length" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 bg-white disabled:bg-slate-50 disabled:text-slate-400">
                  <option value="" disabled>{{ ghn.huyenId ? 'Chọn phường/xã' : 'Chọn quận/huyện trước' }}</option>
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
              <img src="https://img.vietqr.io/assets/images/vietqr.png" alt="VietQR" class="h-5 object-contain shrink-0" />
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
            <div v-for="item in gio.items" :key="item.id" class="flex gap-3">
              <img :src="item.hinhAnh || anhMacDinh" :alt="item.tenSanPham" class="h-14 w-14 shrink-0 rounded-lg object-cover bg-slate-50" @error="xuLyAnhLoi" />
              <div class="flex-1 text-sm">
                <p class="font-medium text-slate-800 line-clamp-1">{{ item.tenSanPham }}</p>
                <p class="text-xs text-slate-400">{{ item.mauSac }} · {{ item.kichCo }} · x{{ item.soLuong }}</p>
              </div>
              <p class="text-sm font-semibold text-slate-700">{{ dinhDangTienViet(Number(item.giaBan) * Number(item.soLuong)) }}</p>
            </div>
          </div>
          <!-- Mã giảm giá (khách vãng lai dùng được voucher toàn sàn) -->
          <div class="mt-4 border-t border-slate-100 pt-4">
            <div v-if="!voucher">
              <div class="flex gap-2">
                <input
                  v-model="maVoucher"
                  type="text"
                  placeholder="Nhập hoặc chọn mã giảm giá"
                  class="h-10 flex-1 rounded-xl border border-slate-200 px-3 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
                  @focus="moDanhSachVoucher"
                  @keyup.enter="apVoucher"
                />
                <button @click="apVoucher" :disabled="dangApVoucher" class="rounded-xl bg-slate-800 px-4 text-sm font-semibold text-white transition hover:bg-slate-700 disabled:opacity-60">
                  {{ dangApVoucher ? '...' : 'Áp dụng' }}
                </button>
              </div>

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
              <span>Cân nặng</span>
              <span class="font-medium text-slate-700">{{ tongCanNang.toLocaleString('vi-VN') }} g</span>
            </div>
            <div class="flex items-center justify-between text-sm text-slate-500">
              <span class="flex items-center gap-1.5">
                Phí vận chuyển
                <img :src="logoGhn" alt="GHN" class="h-4 w-auto object-contain" />
                <span v-if="phiShip?.uocTinh" class="text-xs text-slate-400">(ước tính)</span>
              </span>
              <span v-if="dangTinhPhi" class="text-slate-400">Đang tính...</span>
              <span v-else-if="phiShip" class="font-semibold text-slate-700">{{ dinhDangTienViet(phiShipSo) }}</span>
              <span v-else class="text-xs text-slate-400">Nhập địa chỉ để tính</span>
            </div>
            <div class="flex items-center justify-between pt-1">
              <span class="text-sm font-semibold text-slate-700">Tổng cộng</span>
              <span class="text-xl font-bold text-primary">{{ dinhDangTienViet(tongThanhToan) }}</span>
            </div>
          </div>
          <button @click="datHangMoi" :disabled="dangDat" class="mt-6 w-full rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3.5 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5 disabled:opacity-60 disabled:translate-y-0">
            {{ dangDat ? 'Đang xử lý...' : (hinhThucThanhToan !== 'COD' ? 'Thanh toán' : 'Đặt hàng') }}
          </button>
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
          </div>
          <p class="mt-2 text-center text-[11px] text-slate-400">Mã QR có hiệu lực đến khi bạn thanh toán hoặc nhấn Hủy.</p>
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
