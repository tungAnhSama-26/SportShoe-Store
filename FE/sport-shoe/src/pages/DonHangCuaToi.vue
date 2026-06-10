<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { layDonHangCuaToi } from '../services/don-hang';
import { layKhachId } from '../services/gio-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';

const router = useRouter();
const danhSach = ref([]);
const dangTai = ref(true);
const daDangNhap = computed(() => Boolean(layKhachId()));

onMounted(taiDanhSach);

async function taiDanhSach() {
  dangTai.value = true;
  try {
    danhSach.value = await layDonHangCuaToi();
  } catch {
    danhSach.value = [];
  } finally {
    dangTai.value = false;
  }
}

function formatNgay(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString('vi-VN');
  } catch {
    return '';
  }
}

function lopTrangThai(tt) {
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
</script>

<template>
  <main class="bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-5xl px-6 lg:px-10 pt-10">
      <h1 class="text-3xl font-bold text-slate-900 mb-8">Đơn hàng của bạn</h1>

      <div v-if="!daDangNhap" class="py-24 text-center">
        <p class="text-sm text-slate-500 mb-4">Vui lòng đăng nhập để xem đơn hàng.</p>
        <router-link to="/login" class="inline-flex rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white hover:bg-primary/90">Đăng nhập</router-link>
      </div>

      <div v-else-if="dangTai" class="py-24 text-center text-sm text-slate-400">Đang tải đơn hàng...</div>

      <div v-else-if="!danhSach.length" class="py-24 text-center">
        <p class="text-sm text-slate-500 mb-4">Bạn chưa có đơn hàng nào.</p>
        <router-link to="/san-pham" class="inline-flex rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white hover:bg-primary/90">Mua sắm ngay</router-link>
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="don in danhSach"
          :key="don.id"
          class="flex flex-wrap items-center justify-between gap-4 rounded-2xl bg-white border border-slate-100 p-5 shadow-sm"
        >
          <div>
            <div class="flex items-center gap-3">
              <p class="font-bold text-slate-900">#{{ don.ma }}</p>
              <span class="rounded-full px-2.5 py-1 text-xs font-semibold" :class="lopTrangThai(don.trangThai)">{{ don.trangThaiText }}</span>
            </div>
            <p class="mt-1 text-xs text-slate-400">{{ formatNgay(don.ngayLap) }} · {{ don.soLuong }} sản phẩm</p>
          </div>
          <div class="flex items-center gap-4">
            <p class="text-lg font-bold text-primary">{{ dinhDangTienViet(don.tongThanhToan) }}</p>
            <button @click="router.push(`/don-hang/${don.id}`)" class="rounded-xl border border-primary px-4 py-2 text-sm font-semibold text-primary transition hover:bg-primary/5">
              Xem chi tiết
            </button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>
