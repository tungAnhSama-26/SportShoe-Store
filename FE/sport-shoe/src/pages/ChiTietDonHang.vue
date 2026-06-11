<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { layChiTietDonHang, xacNhanDaNhanHang } from '../services/don-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showSuccess, showError } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import {
  CAC_BUOC_DON_HANG,
  layCauHinhTrangThaiDonHang,
  layViTriTienTrinhDonHang,
} from '../utils/order-status';
import anhMacDinh from '../assets/login-shoe.png';
import YeuCauTraHangModal from '../components/common/YeuCauTraHangModal.vue';

const route = useRoute();
const router = useRouter();

const don = ref(null);
const dangTai = ref(true);
const loi = ref('');
const hienModalTraHang = ref(false);

const daQuaHanTraHang = computed(() => {
  if (!don.value || !don.value.ngayCapNhat) return true;
  const thoiGianHoanThanh = new Date(don.value.ngayCapNhat).getTime();
  const bayGio = new Date().getTime();
  const baNgayMs = 3 * 24 * 60 * 60 * 1000;
  return (bayGio - thoiGianHoanThanh) > baNgayMs;
});

function lopBadgeTraHang(tt) {
  switch (tt) {
    case 1: return 'bg-amber-50 text-amber-600 border border-amber-100';
    case 2: return 'bg-blue-50 text-blue-600 border border-blue-100';
    case 3: return 'bg-violet-50 text-violet-600 border border-violet-100';
    case 4: return 'bg-cyan-50 text-cyan-600 border border-cyan-100';
    case 5: return 'bg-purple-50 text-purple-600 border border-purple-100';
    case 6: return 'bg-rose-50 text-rose-600 border border-rose-100';
    case 7: return 'bg-emerald-50 text-emerald-600 border border-emerald-100';
    case 8: return 'bg-stone-100 text-stone-600 border border-stone-200';
    case 9: return 'bg-slate-100 text-slate-500 border border-slate-200';
    case 10: return 'bg-rose-50 text-rose-600 border border-rose-100';
    default: return 'bg-slate-100 text-slate-600';
  }
}

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

const cacBuoc = CAC_BUOC_DON_HANG;
const cauHinhTrangThai = computed(() => layCauHinhTrangThaiDonHang(don.value?.trangThai));
const viTriHienTai = computed(() => layViTriTienTrinhDonHang(don.value?.trangThai));

function chuanHoaTrangThai(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .replace(/\s+/g, ' ')
    .trim();
}

const thongTinCacBuoc = computed(() => {
  const lichSu = Array.isArray(don.value?.lichSuTrangThai) ? don.value.lichSuTrangThai : [];

  return cacBuoc.map((buoc, index) => {
    const banGhi = lichSu.find(
      (item) => chuanHoaTrangThai(item?.trangThai) === chuanHoaTrangThai(buoc.ten),
    );

    if (banGhi) {
      return {
        ...buoc,
        thoiGian: banGhi.ngayTao,
      };
    }

    if (index === 0) {
      return {
        ...buoc,
        thoiGian: don.value?.ngayLap,
      };
    }

    if (index + 1 === viTriHienTai.value) {
      return {
        ...buoc,
        thoiGian: don.value?.ngayCapNhat,
      };
    }

    return { ...buoc, thoiGian: null };
  });
});

function formatNgay(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString('vi-VN');
  } catch {
    return '';
  }
}

function formatGioBuoc(iso) {
  if (!iso) return '';
  return new Intl.DateTimeFormat('vi-VN', {
    timeZone: 'Asia/Bangkok',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(new Date(iso));
}

function formatNgayBuoc(iso) {
  if (!iso) return '';
  return new Intl.DateTimeFormat('vi-VN', {
    timeZone: 'Asia/Bangkok',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(new Date(iso));
}

function lopBadge(tt) {
  switch (tt) {
    case 1: return 'bg-amber-50 text-amber-600';
    case 9: return 'bg-sky-50 text-sky-600';
    case 2: return 'bg-violet-50 text-violet-600';
    case 3: return 'bg-indigo-50 text-indigo-600';
    case 4: return 'bg-cyan-50 text-cyan-600';
    case 5: return 'bg-emerald-50 text-emerald-600';
    case 6: return 'bg-rose-50 text-rose-600';
    case 7: return 'bg-amber-50 text-amber-700';
    case 8: return 'bg-orange-50 text-orange-700';
    case 10: return 'bg-rose-50 text-rose-600';
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
          <div class="flex items-center gap-2">
            <span class="rounded-full px-3.5 py-1.5 text-sm font-semibold" :class="lopBadge(don.trangThai)">{{ don.trangThaiText }}</span>
            <span v-if="don.phieuTraHangId != null" class="rounded-full px-3.5 py-1.5 text-sm font-semibold" :class="lopBadgeTraHang(don.trangThaiTraHang)">
              Trả hàng: {{ don.trangThaiTraHangText }}
            </span>
          </div>
        </div>

        <!-- Trục trạng thái -->
        <section class="mt-6 overflow-hidden rounded-3xl border border-slate-100 bg-white px-5 py-5 shadow-sm lg:px-7">
          <div class="flex items-center gap-2 border-b border-slate-100 pb-5 text-[15px] font-semibold text-slate-700">
            <svg class="h-[18px] w-[18px] text-slate-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <rect x="7" y="4" width="10" height="16" rx="2" />
              <path d="M9 4.5V3.8A1.8 1.8 0 0 1 10.8 2h2.4A1.8 1.8 0 0 1 15 3.8v.7M10 9h4M10 13h4M10 17h2" />
            </svg>
            Trạng Thái Đơn Hàng
          </div>

          <div
            v-if="!cauHinhTrangThai.hienStepper"
            class="mt-5 flex items-center gap-3 rounded-2xl px-5 py-4"
            :class="cauHinhTrangThai.lopMau"
          >
            <svg class="h-6 w-6 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6M9 9l6 6"/></svg>
            <div>
              <p class="font-bold">{{ cauHinhTrangThai.tieuDe }}</p>
              <p class="text-sm opacity-75">{{ cauHinhTrangThai.moTa }}</p>
            </div>
          </div>

          <div v-else class="relative mx-auto mt-7 w-full max-w-5xl px-2 pt-2">
            <div
              class="absolute top-[35px] z-0 h-[2px] bg-slate-200"
              :style="{ left: `${100 / thongTinCacBuoc.length / 2}%`, right: `${100 / thongTinCacBuoc.length / 2}%` }"
            ></div>

            <div class="relative z-10 grid w-full grid-cols-6 items-start">
              <div
                v-for="(buoc, i) in thongTinCacBuoc"
                :key="buoc.ten"
                class="flex min-w-0 flex-col items-center text-center"
              >
                <div
                  class="flex h-14 w-14 items-center justify-center rounded-full border-[2.5px] transition"
                  :class="(i + 1) <= viTriHienTai
                    ? 'border-[#c52220] bg-[#c52220] text-white shadow-[0_10px_22px_rgba(197,34,32,0.18)]'
                    : 'border-slate-200 bg-white text-slate-300'"
                >
                  <svg class="h-[22px] w-[22px]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                    <path :d="buoc.icon" />
                  </svg>
                </div>
                <p
                  class="mt-3 whitespace-nowrap text-xs font-semibold"
                  :class="(i + 1) <= viTriHienTai ? 'text-[#c52220]' : 'text-slate-400'"
                >
                  {{ buoc.ten }}
                </p>
                <div class="mt-1 min-h-[36px] text-[11px] leading-4 text-slate-400">
                  <p v-if="buoc.thoiGian">{{ formatGioBuoc(buoc.thoiGian) }} {{ formatNgayBuoc(buoc.thoiGian) }}</p>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Hành động khi đơn hoàn thành -->
        <section v-if="daHoanThanh" class="mt-6 flex flex-wrap gap-3 items-center">
          <button v-if="!don.daNhanHang" @click="xacNhanNhan" :disabled="dangXuLy" class="inline-flex items-center justify-center rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5 disabled:opacity-60">
            {{ dangXuLy ? 'Đang xử lý...' : 'Đã nhận hàng' }}
          </button>
          <button v-else @click="diDanhGia" class="inline-flex items-center gap-2 rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5">
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l2.9 6.3 6.9.6-5.2 4.6 1.6 6.8L12 17.3 5.8 20.9l1.6-6.8L2.2 8.9l6.9-.6z"/></svg>
            Đánh giá sản phẩm
          </button>

          <!-- Yêu cầu trả hàng (ẩn và vô hiệu hóa sau 3 ngày hoàn thành) -->
          <button
            v-if="(don.phieuTraHangId == null || [8, 9].includes(don.trangThaiTraHang)) && !daQuaHanTraHang"
            @click="hienModalTraHang = true"
            class="inline-flex items-center justify-center gap-2 rounded-2xl border border-rose-200 bg-rose-50 px-6 py-3 text-sm font-bold text-rose-600 shadow-sm transition hover:-translate-y-0.5 hover:bg-rose-100"
          >
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 14 4 9l5-5" />
              <path d="M4 9h10a6 6 0 0 1 6 6v5" />
            </svg>
            Yêu cầu trả hàng
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
            <div v-if="Number(don.phiVanChuyen) > 0" class="flex items-center justify-between text-slate-600">
              <span>Phí vận chuyển</span>
              <span>+{{ dinhDangTienViet(don.phiVanChuyen) }}</span>
            </div>
            <div class="flex items-center justify-between border-t border-slate-100 pt-3">
              <span class="text-base font-bold text-slate-800">Tổng thanh toán</span>
              <span class="text-2xl font-bold text-primary">{{ dinhDangTienViet(don.tongThanhToan) }}</span>
            </div>
          </div>
        </section>
      </template>
    </div>

    <!-- Return Request Modal -->
    <YeuCauTraHangModal
      :isOpen="hienModalTraHang"
      :don="don"
      @close="hienModalTraHang = false"
      @success="taiChiTiet"
    />
  </main>
</template>
