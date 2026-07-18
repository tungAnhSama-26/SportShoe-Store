<script setup>
import { onMounted, ref } from 'vue';
import { layDanhGiaCongKhai } from '../services/san-pham';
import DanhGiaMedia from '../components/DanhGiaMedia.vue';
import { resolveMediaUrl } from '../utils/media';
import anhMacDinh from '../assets/login-shoe.png';

const BO_LOC = [
  { nhan: 'Tất cả', giaTri: null },
  { nhan: '5 sao', giaTri: 5 },
  { nhan: '4 sao', giaTri: 4 },
  { nhan: '3 sao', giaTri: 3 },
  { nhan: '2 sao', giaTri: 2 },
  { nhan: '1 sao', giaTri: 1 },
];
const KICH_THUOC = 10;

const danhSach = ref([]);
const soSao = ref(null);
const trang = ref(0);
const tongTrang = ref(0);
const tongSo = ref(0);
const dangTai = ref(false);

onMounted(() => doiLoc(null));

async function doiLoc(gt) {
  if (dangTai.value) return;
  soSao.value = gt;
  trang.value = 0;
  danhSach.value = [];
  await tai();
}

async function tai() {
  dangTai.value = true;
  try {
    const res = await layDanhGiaCongKhai({ soSao: soSao.value, trang: trang.value, kichThuoc: KICH_THUOC });
    const moi = res?.danhSach || [];
    danhSach.value = trang.value === 0 ? moi : [...danhSach.value, ...moi];
    tongTrang.value = res?.tongTrang || 0;
    tongSo.value = res?.tongSo || 0;
  } catch {
    if (trang.value === 0) danhSach.value = [];
  } finally {
    dangTai.value = false;
  }
}

async function taiThem() {
  if (dangTai.value || trang.value + 1 >= tongTrang.value) return;
  trang.value += 1;
  await tai();
}

function chuCaiDau(ten) {
  return (ten || '?').trim().charAt(0).toUpperCase();
}
function formatNgay(v) {
  if (!v) return '';
  return new Date(v).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}
function anhSP(url) {
  return resolveMediaUrl(url) || anhMacDinh;
}
function xuLyAnhLoi(e) {
  if (e.target.src !== anhMacDinh) e.target.src = anhMacDinh;
}
</script>

<template>
  <main class="bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-3xl px-6 lg:px-10 pt-8">
      <h1 class="text-2xl font-bold text-slate-900 mb-6">Đánh giá từ khách hàng</h1>

      <!-- Bộ lọc số sao -->
      <div class="mt-5 flex flex-wrap gap-2">
        <button
          v-for="bl in BO_LOC"
          :key="bl.nhan"
          type="button"
          @click="doiLoc(bl.giaTri)"
          class="rounded-full border px-4 py-1.5 text-sm font-medium transition"
          :class="soSao === bl.giaTri
            ? 'border-primary bg-primary text-white'
            : 'border-slate-200 bg-white text-slate-500 hover:border-primary hover:text-primary'"
        >
          <span v-if="bl.giaTri" class="text-amber-400" :class="{ 'text-white': soSao === bl.giaTri }">★</span>
          {{ bl.nhan }}
        </button>
      </div>

      <p class="mt-3 text-sm text-slate-400">{{ tongSo }} đánh giá</p>

      <!-- Danh sách -->
      <div v-if="!danhSach.length && dangTai" class="py-24 text-center text-sm text-slate-400">Đang tải...</div>
      <div v-else-if="!danhSach.length" class="py-24 text-center text-sm text-slate-400">Chưa có đánh giá nào.</div>

      <div v-else class="mt-4 space-y-4">
        <div v-for="dg in danhSach" :key="dg.id" class="rounded-3xl bg-white border border-slate-100 p-6 shadow-sm">
          <!-- Khách + sao -->
          <div class="flex gap-4">
            <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">
              {{ chuCaiDau(dg.hoTenKhach) }}
            </div>
            <div class="flex-1">
              <div class="flex items-center gap-3">
                <p class="text-sm font-semibold text-slate-800">{{ dg.hoTenKhach }}</p>
                <span class="text-xs text-slate-400">{{ formatNgay(dg.ngayTao) }}</span>
              </div>
              <div class="mt-0.5 flex">
                <span v-for="i in 5" :key="i" class="text-sm" :class="i <= dg.soSao ? 'text-amber-400' : 'text-slate-300'">★</span>
              </div>
              <p v-if="dg.noiDung" class="mt-1.5 text-sm leading-6 text-slate-600">{{ dg.noiDung }}</p>
              <DanhGiaMedia :media="dg.media" />

              <!-- Phản hồi của shop -->
              <div v-if="dg.phanHoi" class="mt-3 rounded-2xl bg-slate-50 p-3.5">
                <div class="flex items-center gap-2">
                  <span class="text-xs font-bold text-primary">Phản hồi từ cửa hàng</span>
                  <span class="text-xs text-slate-400">{{ formatNgay(dg.ngayPhanHoi) }}</span>
                </div>
                <p class="mt-1 text-sm leading-6 text-slate-600">{{ dg.phanHoi }}</p>
              </div>
            </div>
          </div>

          <!-- Sản phẩm được đánh giá -->
          <router-link
            :to="`/khachhang/san-pham/${dg.giayId}`"
            class="mt-4 flex items-center gap-3 rounded-2xl border border-slate-100 bg-slate-50/60 p-3 transition hover:border-primary/40 hover:bg-primary/5"
          >
            <img :src="anhSP(dg.hinhAnhSanPham)" :alt="dg.tenSanPham" class="h-12 w-12 shrink-0 rounded-lg object-cover bg-white" @error="xuLyAnhLoi" />
            <span class="line-clamp-2 text-sm font-medium text-slate-700">{{ dg.tenSanPham }}</span>
            <svg class="ml-auto h-4 w-4 shrink-0 text-slate-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m9 18 6-6-6-6" /></svg>
          </router-link>
        </div>
      </div>

      <!-- Xem thêm -->
      <div v-if="danhSach.length && trang + 1 < tongTrang" class="mt-6 text-center">
        <button @click="taiThem" :disabled="dangTai" class="rounded-xl border border-slate-200 bg-white px-6 py-2.5 text-sm font-semibold text-slate-600 transition hover:border-primary hover:text-primary disabled:opacity-60">
          {{ dangTai ? 'Đang tải...' : 'Xem thêm' }}
        </button>
      </div>
    </div>
  </main>
</template>
