<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { layChiTietDonHang, xacNhanDaNhanHang } from '../services/don-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showWarning, showSuccess, showError } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import anhMacDinh from '../assets/login-shoe.png';

const route = useRoute();
const router = useRouter();

const don = ref(null);
const dangTai = ref(true);
const loi = ref('');

onMounted(taiChiTiet);
watch(() => route.params.id, taiChiTiet);

async function taiChiTiet() {
  dangTai.value = true;
  loi.value = '';
  try {
    don.value = await layChiTietDonHang(route.params.id);
  } catch {
    don.value = null;
    loi.value = 'Không tải được đơn hàng này.';
  } finally {
    dangTai.value = false;
  }
}

// Các bước trạng thái (giống trục trạng thái bên quản lý hóa đơn).
const CAC_BUOC = [
  { ten: 'Chờ xác nhận', icon: 'M12 8v4l3 3 M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z' },
  { ten: 'Đã xác nhận', icon: 'M9 11l3 3L22 4 M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11' },
  { ten: 'Đang giao', icon: 'M1 3h15v13H1z M16 8h4l3 3v5h-7V8z M5.5 18.5a2.5 2.5 0 1 0 0 .01 M18.5 18.5a2.5 2.5 0 1 0 0 .01' },
  { ten: 'Hoàn thành', icon: 'M20 6 9 17l-5-5' },
];

// Map trạng thái đơn -> vị trí trong stepper (1..4).
const VI_TRI = { 1: 1, 2: 2, 3: 3, 4: 4, 5: 4 };
const daHuy = computed(() => don.value?.trangThai === 6);
const viTriHienTai = computed(() => VI_TRI[don.value?.trangThai] || 0);

function formatNgay(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString('vi-VN');
  } catch {
    return '';
  }
}

function lopBadge(tt) {
  switch (tt) {
    case 1: return 'bg-amber-50 text-amber-600';
    case 2: return 'bg-sky-50 text-sky-600';
    case 3: return 'bg-indigo-50 text-indigo-600';
    case 4:
    case 5: return 'bg-emerald-50 text-emerald-600';
    case 6: return 'bg-rose-50 text-rose-600';
    default: return 'bg-slate-100 text-slate-600';
  }
}

const dangXuLy = ref(false);
const daHoanThanh = computed(() => don.value?.trangThai === 5);

async function xacNhanNhan() {
  dangXuLy.value = true;
  try {
    await xacNhanDaNhanHang(route.params.id);
    await taiChiTiet();
    showSuccess('Đã xác nhận nhận hàng. Bạn có thể đánh giá sản phẩm.');
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể xác nhận nhận hàng'));
  } finally {
    dangXuLy.value = false;
  }
}

function hoanHang() {
  showWarning('Chức năng hoàn hàng đang được hoàn thiện ở phần quản lý.');
}

function diDanhGia() {
  router.push(`/don-hang/${route.params.id}/danh-gia`);
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) event.target.src = anhMacDinh;
}
</script>

<template>
  <main class="bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-4xl px-6 lg:px-10 pt-8">
      <button @click="router.push('/don-hang')" class="mb-6 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-primary transition-colors">
        <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6" /></svg>
        Đơn hàng của bạn
      </button>

      <div v-if="dangTai" class="py-28 text-center text-sm text-slate-400">Đang tải đơn hàng...</div>
      <div v-else-if="loi || !don" class="py-28 text-center text-sm text-rose-500">{{ loi || 'Không tìm thấy đơn hàng.' }}</div>

      <template v-else>
        <!-- Header -->
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">Đơn hàng #{{ don.ma }}</h1>
            <p class="mt-1 text-sm text-slate-400">Đặt lúc {{ formatNgay(don.ngayLap) }}</p>
          </div>
          <span class="rounded-full px-3.5 py-1.5 text-sm font-semibold" :class="lopBadge(don.trangThai)">{{ don.trangThaiText }}</span>
        </div>

        <!-- Trục trạng thái -->
        <section class="mt-6 rounded-3xl bg-white border border-slate-100 p-6 lg:p-8 shadow-sm">
          <div v-if="daHuy" class="flex items-center gap-3 rounded-2xl bg-rose-50 px-5 py-4 text-rose-600">
            <svg class="h-6 w-6 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6M9 9l6 6"/></svg>
            <div>
              <p class="font-bold">Đơn hàng đã hủy</p>
              <p class="text-sm text-rose-400">Đơn hàng này đã được hủy.</p>
            </div>
          </div>

          <div v-else class="flex items-start">
            <template v-for="(buoc, i) in CAC_BUOC" :key="i">
              <!-- Vạch nối -->
              <div v-if="i > 0" class="mt-5 h-0.5 flex-1" :class="i < viTriHienTai ? 'bg-primary' : 'bg-slate-200'"></div>
              <!-- Bước -->
              <div class="flex flex-col items-center" :class="i > 0 ? '' : ''">
                <div
                  class="flex h-11 w-11 items-center justify-center rounded-full border-2 transition"
                  :class="(i + 1) <= viTriHienTai ? 'border-primary bg-primary text-white' : 'border-slate-200 bg-white text-slate-300'"
                >
                  <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path :d="buoc.icon" /></svg>
                </div>
                <p class="mt-2.5 w-24 text-center text-xs font-semibold" :class="(i + 1) <= viTriHienTai ? 'text-slate-800' : 'text-slate-400'">{{ buoc.ten }}</p>
              </div>
            </template>
          </div>
        </section>

        <!-- Hành động khi đơn hoàn thành -->
        <section v-if="daHoanThanh" class="mt-6 flex flex-wrap gap-3">
          <template v-if="!don.daNhanHang">
            <button @click="xacNhanNhan" :disabled="dangXuLy" class="inline-flex items-center justify-center rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5 disabled:opacity-60">
              {{ dangXuLy ? 'Đang xử lý...' : 'Đã nhận hàng' }}
            </button>
            <button @click="hoanHang" class="inline-flex items-center justify-center rounded-2xl border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50">
              Hoàn hàng
            </button>
          </template>
          <button v-else @click="diDanhGia" class="inline-flex items-center gap-2 rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5">
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l2.9 6.3 6.9.6-5.2 4.6 1.6 6.8L12 17.3 5.8 20.9l1.6-6.8L2.2 8.9l6.9-.6z"/></svg>
            Đánh giá sản phẩm
          </button>
        </section>

        <!-- Thông tin nhận hàng -->
        <section class="mt-6 rounded-3xl bg-white border border-slate-100 p-6 lg:p-7 shadow-sm">
          <h2 class="text-base font-bold text-slate-800 mb-3">Thông tin nhận hàng</h2>
          <div class="rounded-2xl bg-slate-50 px-5 py-4 text-sm text-slate-600">
            <p class="font-semibold text-slate-800">{{ don.tenNguoiNhan }} · {{ don.sdtNguoiNhan }}</p>
            <p class="mt-1">{{ don.diaChiGiaoHang }}</p>
          </div>
        </section>

        <!-- Sản phẩm -->
        <section class="mt-6 rounded-3xl bg-white border border-slate-100 p-6 lg:p-7 shadow-sm">
          <h2 class="text-base font-bold text-slate-800 mb-4">Sản phẩm</h2>
          <div class="space-y-4">
            <div v-for="(sp, i) in don.sanPhams" :key="i" class="flex gap-4">
              <img :src="sp.hinhAnh || anhMacDinh" :alt="sp.tenSanPham" class="h-16 w-16 shrink-0 rounded-xl object-cover bg-slate-50" @error="xuLyAnhLoi" />
              <div class="flex-1 text-sm">
                <p class="font-medium text-slate-800">{{ sp.tenSanPham }}</p>
                <p class="text-xs text-slate-400">{{ sp.mauSac }} · {{ sp.kichCo }} · x{{ sp.soLuong }}</p>
                <div class="mt-1 flex items-center gap-2">
                  <span class="font-semibold text-primary">{{ dinhDangTienViet(sp.giaDonVi) }}</span>
                  <span v-if="Number(sp.giaNiemYet) > Number(sp.giaDonVi)" class="text-xs text-slate-400 line-through">{{ dinhDangTienViet(sp.giaNiemYet) }}</span>
                </div>
              </div>
              <p class="text-sm font-semibold text-slate-700">{{ dinhDangTienViet(sp.thanhTien) }}</p>
            </div>
          </div>
        </section>

        <!-- Phân tích giá -->
        <section class="mt-6 rounded-3xl bg-white border border-slate-100 p-6 lg:p-7 shadow-sm">
          <h2 class="text-base font-bold text-slate-800 mb-4">Thông tin thanh toán</h2>
          <div class="space-y-2.5 text-sm">
            <div class="flex items-center justify-between text-slate-600">
              <span>Tạm tính (giá gốc)</span>
              <span>{{ dinhDangTienViet(don.tamTinh) }}</span>
            </div>
            <div v-if="Number(don.giamDotGiamGia) > 0" class="flex items-center justify-between text-emerald-600">
              <span>Giảm từ đợt giảm giá</span>
              <span>-{{ dinhDangTienViet(don.giamDotGiamGia) }}</span>
            </div>
            <div v-if="Number(don.giamVoucher) > 0" class="flex items-center justify-between text-emerald-600">
              <span>Giảm từ voucher<span v-if="don.maPhieuGiamGia"> ({{ don.maPhieuGiamGia }})</span></span>
              <span>-{{ dinhDangTienViet(don.giamVoucher) }}</span>
            </div>
            <div class="flex items-center justify-between border-t border-slate-100 pt-3">
              <span class="text-base font-bold text-slate-800">Tổng thanh toán</span>
              <span class="text-2xl font-bold text-primary">{{ dinhDangTienViet(don.tongThanhToan) }}</span>
            </div>
          </div>
        </section>
      </template>
    </div>
  </main>
</template>
