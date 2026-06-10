<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRouter, onBeforeRouteLeave } from 'vue-router';
import { layGioHang, layDiaChiKhachHang, layThongTinKhach, layKhachId, datHang, giuHang, huyGiuHang, huyGiuHangBeacon, kiemTraVoucher, taoMaVnPay, trangThaiVnPay } from '../services/gio-hang';
import { gioHangStore } from '../stores/gio-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showWarning, showSuccess, showError } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import anhMacDinh from '../assets/login-shoe.png';

const router = useRouter();

const gio = ref({ id: null, items: [], tongSoLuong: 0, tongTien: 0 });
const diaChiList = ref([]);
const diaChiChonId = ref(null);
const dangTai = ref(true);
const daDangNhap = computed(() => Boolean(layKhachId()));

const form = ref({
  hoTen: '',
  sdt: '',
  tinhThanh: '',
  quanHuyen: '',
  phuongXa: '',
  diaChiCuThe: '',
});

const hinhThucThanhToan = ref('COD');
const dangDat = ref(false);
const daDatHang = ref(false);

// Voucher
const maVoucher = ref('');
const voucher = ref(null); // { ma, ten, tienGiam, tongTienHang, tongTienSauGiam }
const dangApVoucher = ref(false);

// VNPay (giả lập)
const qrVnPay = ref(null); // { token, qrData, maGiaoDich }
let pollTimer = null;
const tongThanhToan = computed(() =>
  voucher.value ? Number(voucher.value.tongTienSauGiam) : Number(gio.value.tongTien || 0)
);

onMounted(async () => {
  await tai();
  if (gio.value.items.length) {
    try {
      // Giữ hàng tạm (trừ tồn) trong 90 giây.
      await giuHang();
    } catch (e) {
      showError(getDisplayErrorMessage(e, 'Một số sản phẩm trong giỏ đã hết hàng.'));
      router.push('/gio-hang');
      return;
    }
  }
  window.addEventListener('beforeunload', onTruocKhiDongTab);
});

onUnmounted(() => {
  window.removeEventListener('beforeunload', onTruocKhiDongTab);
  dungPoll();
});

function onTruocKhiDongTab() {
  if (!daDatHang.value) huyGiuHangBeacon();
}

// Rời trang thanh toán (back, chuyển trang) mà chưa đặt -> hoàn tồn ngay.
onBeforeRouteLeave(() => {
  if (!daDatHang.value) huyGiuHang();
  return true;
});

async function tai() {
  dangTai.value = true;
  try {
    const [g, dc] = await Promise.all([layGioHang(), layDiaChiKhachHang()]);
    gio.value = g;
    diaChiList.value = dc;

    const macDinh = dc.find((d) => d.laMacDinh) || dc[0];
    if (macDinh) {
      chonDiaChi(macDinh);
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

function chonDiaChi(dc) {
  diaChiChonId.value = dc.id;
  form.value = {
    hoTen: dc.hoTen || '',
    sdt: dc.sdt || '',
    tinhThanh: dc.tinhThanh || '',
    quanHuyen: dc.quanHuyen || '',
    phuongXa: dc.phuongXa || '',
    diaChiCuThe: dc.diaChiCuThe || '',
  };
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
  return true;
}

async function hoanTatDatHang(maHoaDon) {
  daDatHang.value = true; // đã thành đơn -> không hủy giữ hàng khi rời trang
  gioHangStore.datSoLuong(0);
  await showSuccess(`Đặt hàng thành công! Mã đơn: ${maHoaDon}. Cảm ơn bạn đã mua hàng.`, 'Thành công');
  router.push('/san-pham');
}

async function datHangMoi() {
  if (!hopLeThongTin()) return;
  if (hinhThucThanhToan.value === 'VNPAY') {
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

// --- VNPay (giả lập): hiện QR, quét xong tự tạo đơn ---
async function moThanhToanVnPay() {
  dangDat.value = true;
  try {
    qrVnPay.value = await taoMaVnPay(taoPayload());
    batDauPoll();
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể tạo mã thanh toán'));
  } finally {
    dangDat.value = false;
  }
}

const anhQrVnPay = computed(() =>
  qrVnPay.value
    ? `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(qrVnPay.value.qrData)}`
    : ''
);

function batDauPoll() {
  dungPoll();
  pollTimer = setInterval(async () => {
    if (!qrVnPay.value) return;
    try {
      const tt = await trangThaiVnPay(qrVnPay.value.token);
      if (tt.trangThai === 'DA_THANH_TOAN') {
        dungPoll();
        const ma = tt.maHoaDon;
        qrVnPay.value = null;
        await hoanTatDatHang(ma);
      }
    } catch {
      // bỏ qua, thử lại lần poll sau
    }
  }, 2000);
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
  <main class="bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-7xl px-6 lg:px-10 pt-10">
      <h1 class="text-3xl font-bold text-slate-900 mb-2">Thanh toán</h1>
      <p class="text-sm text-slate-400 mb-8">Bước 1: Thông tin giao hàng</p>

      <div v-if="!daDangNhap" class="py-24 text-center">
        <p class="text-sm text-slate-500 mb-4">Vui lòng đăng nhập để thanh toán.</p>
        <router-link to="/login" class="inline-flex rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white hover:bg-primary/90">Đăng nhập</router-link>
      </div>

      <div v-else-if="dangTai" class="py-24 text-center text-sm text-slate-400">Đang tải...</div>

      <div v-else-if="!gio.items.length" class="py-24 text-center">
        <p class="text-sm text-slate-500 mb-4">Giỏ hàng trống, không có gì để thanh toán.</p>
        <router-link to="/san-pham" class="inline-flex rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white hover:bg-primary/90">Mua sắm ngay</router-link>
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
            <h2 class="text-base font-bold text-slate-900 mb-4">Thông tin người nhận</h2>
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
                <input v-model="form.tinhThanh" type="text" placeholder="Tỉnh/Thành" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
              </label>
              <label class="space-y-1.5">
                <span class="text-sm font-medium text-slate-600">Quận/Huyện <span class="text-rose-500">*</span></span>
                <input v-model="form.quanHuyen" type="text" placeholder="Quận/Huyện" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
              </label>
              <label class="space-y-1.5">
                <span class="text-sm font-medium text-slate-600">Phường/Xã <span class="text-rose-500">*</span></span>
                <input v-model="form.phuongXa" type="text" placeholder="Phường/Xã" class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
              </label>
              <label class="space-y-1.5 sm:col-span-2">
                <span class="text-sm font-medium text-slate-600">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
                <input v-model="form.diaChiCuThe" type="text" placeholder="Số nhà, tên đường..." class="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
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
              <span class="text-sm font-medium text-slate-700">Thanh toán khi nhận hàng (COD)</span>
            </label>
            <label
              class="mt-3 flex cursor-pointer items-center gap-3 rounded-xl border p-4 transition"
              :class="hinhThucThanhToan === 'VNPAY' ? 'border-primary bg-primary/5' : 'border-slate-200 hover:border-slate-300'"
            >
              <input type="radio" value="VNPAY" v-model="hinhThucThanhToan" class="text-primary focus:ring-primary/30" />
              <span class="text-sm font-medium text-slate-700">Quét mã QR VNPay</span>
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
          <!-- Mã giảm giá -->
          <div class="mt-4 border-t border-slate-100 pt-4">
            <div v-if="!voucher" class="flex gap-2">
              <input
                v-model="maVoucher"
                type="text"
                placeholder="Nhập mã giảm giá"
                class="h-10 flex-1 rounded-xl border border-slate-200 px-3 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
                @keyup.enter="apVoucher"
              />
              <button @click="apVoucher" :disabled="dangApVoucher" class="rounded-xl bg-slate-800 px-4 text-sm font-semibold text-white transition hover:bg-slate-700 disabled:opacity-60">
                {{ dangApVoucher ? '...' : 'Áp dụng' }}
              </button>
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
            <div class="flex items-center justify-between pt-1">
              <span class="text-sm font-semibold text-slate-700">Tổng cộng</span>
              <span class="text-xl font-bold text-primary">{{ dinhDangTienViet(tongThanhToan) }}</span>
            </div>
          </div>
          <button @click="datHangMoi" :disabled="dangDat" class="mt-6 w-full rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3.5 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5 disabled:opacity-60 disabled:translate-y-0">
            {{ dangDat ? 'Đang xử lý...' : (hinhThucThanhToan === 'VNPAY' ? 'Thanh toán VNPay' : 'Đặt hàng') }}
          </button>
          <router-link to="/gio-hang" class="mt-3 block text-center text-sm font-medium text-slate-500 hover:text-primary">Quay lại giỏ hàng</router-link>
        </aside>
      </div>
    </div>

    <!-- Modal QR VNPay (giả lập) -->
    <Teleport to="body">
      <div v-if="qrVnPay" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
        <div class="w-full max-w-sm rounded-3xl bg-white p-7 text-center shadow-2xl">
          <h3 class="text-lg font-bold text-slate-900">Quét mã QR để thanh toán</h3>
          <p class="mt-1 text-sm text-slate-400">VNPay · {{ qrVnPay.maGiaoDich }}</p>
          <div class="mt-5 flex justify-center">
            <img :src="anhQrVnPay" alt="QR VNPay" class="h-60 w-60 rounded-xl border border-slate-100" />
          </div>
          <div class="mt-4 flex items-center justify-center gap-2 text-sm text-slate-500">
            <span class="inline-block h-2 w-2 animate-pulse rounded-full bg-emerald-500"></span>
            Đang chờ thanh toán...
          </div>
          <button @click="dongVnPay" class="mt-5 w-full rounded-2xl border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50">
            Hủy
          </button>
        </div>
      </div>
    </Teleport>
  </main>
</template>
