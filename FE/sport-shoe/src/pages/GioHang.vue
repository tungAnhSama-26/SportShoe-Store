<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { dongBoGiaGio, capNhatSoLuong, xoaItemGio } from '../services/gio-hang';
import { ketNoiSanPhamRealtime } from '../services/san-pham-realtime';
import { gioHangStore } from '../stores/gio-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showError, showWarning, showConfirm } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import { resolveHinhAnh } from '../utils/resolve-image';
import anhMacDinh from '../assets/login-shoe.png';

const router = useRouter();
const gio = ref({ id: null, items: [], tongSoLuong: 0, tongTien: 0 });
const dangTai = ref(true);
const dangXuLy = ref(false);

let ngatRealtime = null;
let timerDongBo = null;

// Sản phẩm không bán được = đã ngừng bán (conBan === false) hoặc hết hàng (tồn <= 0).
function khongConBan(item) {
  return item.conBan === false || Number(item.tonKho) <= 0;
}
const coSanPhamKhongBan = computed(() =>
  (gio.value.items || []).some((it) => khongConBan(it)),
);

function diThanhToan() {
  if (coSanPhamKhongBan.value) return;
  router.push('/khachhang/thanh-toan');
}

onMounted(() => {
  taiGio();
  // Realtime: admin ngừng bán / đổi giá / đợt giảm -> tự đồng bộ lại giỏ (không reload trang).
  ngatRealtime = ketNoiSanPhamRealtime({
    onSanPhamThayDoi: () => {
      if (timerDongBo) clearTimeout(timerDongBo);
      timerDongBo = setTimeout(() => taiGio(true), 300); // gom nhiều ping thành 1 lần
    },
  });
  window.addEventListener('focus', onFocusTab);
  document.addEventListener('visibilitychange', onFocusTab);
});

function onFocusTab() {
  if (document.visibilityState === 'visible') {
    if (timerDongBo) clearTimeout(timerDongBo);
    timerDongBo = setTimeout(() => taiGio(true), 200);
  }
}

onUnmounted(() => {
  ngatRealtime?.();
  if (timerDongBo) clearTimeout(timerDongBo);
  window.removeEventListener('focus', onFocusTab);
  document.removeEventListener('visibilitychange', onFocusTab);
});

// amTham=true: đồng bộ ngầm (không hiện spinner) khi nhận realtime.
async function taiGio(amTham = false) {
  if (!amTham) dangTai.value = true;
  try {
    // Đồng bộ giá hiện tại và tự động đẩy sản phẩm ngừng hoạt động ra khỏi giỏ
    const ketQua = await dongBoGiaGio();
    gio.value = ketQua;
    gioHangStore.datSoLuong(gio.value.tongSoLuong);
    if (ketQua.removedNames && ketQua.removedNames.length > 0) {
      for (const tenSP of ketQua.removedNames) {
        showWarning(`Sản phẩm "${tenSP}" đã ngừng hoạt động, vui lòng chọn sản phẩm khác.`);
      }
    }
  } catch {
    if (!amTham) gio.value = { id: null, items: [], tongSoLuong: 0, tongTien: 0 };
  } finally {
    if (!amTham) dangTai.value = false;
  }
}

function apDung(ketQua) {
  gio.value = ketQua;
  gioHangStore.datSoLuong(ketQua.tongSoLuong);
}

async function doiSoLuong(item, soLuongMoi) {
  if (soLuongMoi < 1 || soLuongMoi > item.tonKho || dangXuLy.value) return;
  dangXuLy.value = true;
  try {
    apDung(await capNhatSoLuong(item.id, soLuongMoi));
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể cập nhật số lượng'));
  } finally {
    dangXuLy.value = false;
  }
}

async function xoa(item) {
  const ok = await showConfirm(`Xóa "${item.tenSanPham}" khỏi giỏ hàng?`, 'Xác nhận', 'Xóa', 'Hủy');
  if (!ok) return;
  dangXuLy.value = true;
  try {
    apDung(await xoaItemGio(item.id));
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể xóa sản phẩm'));
  } finally {
    dangXuLy.value = false;
  }
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) event.target.src = anhMacDinh;
}

function thanhTien(item) {
  return Number(item.giaBan) * Number(item.soLuong);
}
</script>

<template>
  <main class="invoice-flat bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-7xl px-6 lg:px-10 pt-10">
      <h1 class="text-3xl font-bold text-slate-900 mb-8">Giỏ hàng</h1>

      <div v-if="dangTai" class="py-24 text-center text-sm text-slate-400">Đang tải giỏ hàng...</div>

      <div v-else-if="!gio.items.length" class="py-24 text-center">
        <p class="text-sm text-slate-500 mb-4">Giỏ hàng của bạn đang trống.</p>
        <router-link to="/khachhang/san-pham" class="inline-flex rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white hover:bg-primary/90">Tiếp tục mua sắm</router-link>
      </div>

      <div v-else class="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8">
        <!-- Danh sách -->
        <div class="space-y-4">
          <div
            v-for="item in gio.items"
            :key="item.id"
            class="flex gap-4 rounded-2xl bg-white border border-slate-100 p-4 shadow-sm"
            :class="{ 'opacity-60': khongConBan(item) }"
          >
            <router-link :to="`/khachhang/san-pham/${item.giayId}`" class="h-24 w-24 shrink-0 overflow-hidden rounded-xl bg-slate-50">
              <img :src="resolveHinhAnh(item.hinhAnh) || anhMacDinh" :alt="item.tenSanPham" class="h-full w-full object-cover" @error="xuLyAnhLoi" />
            </router-link>

            <div class="flex flex-1 flex-col">
              <div class="flex items-start justify-between gap-3">
                <div>
                  <router-link :to="`/khachhang/san-pham/${item.giayId}`" class="text-sm font-bold text-slate-900 hover:text-primary line-clamp-2">{{ item.tenSanPham }}</router-link>
                  <p v-if="item.ma" class="mt-0.5 text-xs text-slate-400">Mã SP: <span class="font-medium text-slate-500">{{ item.ma }}</span></p>
                  <p class="mt-1 text-xs text-slate-400">Màu: {{ item.mauSac }} · Size: {{ item.kichCo }}</p>
                  <div class="mt-1 flex flex-wrap items-center gap-2">
                    <span class="text-sm font-bold text-primary">{{ dinhDangTienViet(item.giaBan) }}</span>
                    <!-- Giá ĐỔI sau lúc thêm vào giỏ (đợt giảm bật/tắt, admin đổi giá) -> gạch giá lúc thêm. -->
                    <template v-if="Number(item.giaThem) !== Number(item.giaBan)">
                      <span class="text-xs text-slate-400 line-through">{{ dinhDangTienViet(item.giaThem) }}</span>
                      <span v-if="Number(item.giaBan) < Number(item.giaThem)" class="rounded bg-rose-50 px-1.5 py-0.5 text-[10px] font-bold text-rose-500">Giá vừa giảm</span>
                      <span v-else class="rounded bg-amber-50 px-1.5 py-0.5 text-[10px] font-bold text-amber-600">Giá đã tăng</span>
                    </template>
                  </div>
                  <p v-if="item.conBan === false" class="mt-1 text-xs font-semibold text-rose-500">Sản phẩm đã ngừng hoạt động, vui lòng chọn sản phẩm khác</p>
                  <p v-else-if="item.tonKho <= 0" class="mt-1 text-xs font-semibold text-rose-500">Sản phẩm đã hết hàng, vui lòng chọn sản phẩm khác</p>
                  <p v-else-if="item.soLuong > item.tonKho" class="mt-1 text-xs font-semibold text-amber-600">Chỉ còn {{ item.tonKho }} sản phẩm</p>
                </div>
                <button @click="xoa(item)" class="text-slate-300 hover:text-rose-500 transition" aria-label="Xóa">
                  <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" /></svg>
                </button>
              </div>

              <div class="mt-auto flex items-end justify-between">
                <div class="flex items-center rounded-xl border border-slate-200">
                  <button @click="doiSoLuong(item, item.soLuong - 1)" :disabled="item.soLuong <= 1 || dangXuLy" class="px-3 py-1.5 text-slate-500 hover:text-primary disabled:opacity-40">−</button>
                  <span class="w-9 text-center text-sm font-semibold">{{ item.soLuong }}</span>
                  <button @click="doiSoLuong(item, item.soLuong + 1)" :disabled="item.soLuong >= item.tonKho || dangXuLy" class="px-3 py-1.5 text-slate-500 hover:text-primary disabled:opacity-40">+</button>
                </div>
                <p class="text-base font-bold text-primary">{{ dinhDangTienViet(thanhTien(item)) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Tóm tắt -->
        <aside class="h-fit rounded-2xl bg-white border border-slate-100 p-6 shadow-sm">
          <h2 class="text-base font-bold text-slate-900 mb-4">Tóm tắt đơn hàng</h2>
          <div class="flex items-center justify-between text-sm text-slate-600">
            <span>Tổng số lượng</span>
            <span class="font-semibold">{{ gio.tongSoLuong }}</span>
          </div>
          <div class="mt-3 flex items-center justify-between border-t border-slate-100 pt-3">
            <span class="text-sm font-semibold text-slate-700">Tạm tính</span>
            <span class="text-xl font-bold text-primary">{{ dinhDangTienViet(gio.tongTien) }}</span>
          </div>
          <button
            @click="diThanhToan"
            :disabled="coSanPhamKhongBan"
            class="mt-6 w-full rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3.5 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:translate-y-0"
          >
            Tiến hành thanh toán
          </button>
          <router-link to="/khachhang/san-pham" class="mt-3 block text-center text-sm font-medium text-slate-500 hover:text-primary">Tiếp tục mua sắm</router-link>
        </aside>
      </div>
    </div>
  </main>
</template>
<style scoped>
.invoice-flat :deep([class*="rounded-"]:not(.rounded-full)) {
  border-radius: 6px !important;
}
  </style>