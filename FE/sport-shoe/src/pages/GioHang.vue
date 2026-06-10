<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { layGioHang, capNhatSoLuong, xoaItemGio, layKhachId } from '../services/gio-hang';
import { gioHangStore } from '../stores/gio-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showError, showConfirm } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import anhMacDinh from '../assets/login-shoe.png';

const router = useRouter();
const gio = ref({ id: null, items: [], tongSoLuong: 0, tongTien: 0 });
const dangTai = ref(true);
const dangXuLy = ref(false);
const daDangNhap = computed(() => Boolean(layKhachId()));

onMounted(taiGio);

async function taiGio() {
  dangTai.value = true;
  try {
    gio.value = await layGioHang();
    gioHangStore.datSoLuong(gio.value.tongSoLuong);
  } catch {
    gio.value = { id: null, items: [], tongSoLuong: 0, tongTien: 0 };
  } finally {
    dangTai.value = false;
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
  <main class="bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-7xl px-6 lg:px-10 pt-10">
      <h1 class="text-3xl font-bold text-slate-900 mb-8">Giỏ hàng</h1>

      <div v-if="!daDangNhap" class="py-24 text-center">
        <p class="text-sm text-slate-500 mb-4">Vui lòng đăng nhập để xem giỏ hàng của bạn.</p>
        <router-link to="/login" class="inline-flex rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white hover:bg-primary/90">Đăng nhập</router-link>
      </div>

      <div v-else-if="dangTai" class="py-24 text-center text-sm text-slate-400">Đang tải giỏ hàng...</div>

      <div v-else-if="!gio.items.length" class="py-24 text-center">
        <p class="text-sm text-slate-500 mb-4">Giỏ hàng của bạn đang trống.</p>
        <router-link to="/san-pham" class="inline-flex rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white hover:bg-primary/90">Tiếp tục mua sắm</router-link>
      </div>

      <div v-else class="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8">
        <!-- Danh sách -->
        <div class="space-y-4">
          <div
            v-for="item in gio.items"
            :key="item.id"
            class="flex gap-4 rounded-2xl bg-white border border-slate-100 p-4 shadow-sm"
          >
            <router-link :to="`/san-pham/${item.giayId}`" class="h-24 w-24 shrink-0 overflow-hidden rounded-xl bg-slate-50">
              <img :src="item.hinhAnh || anhMacDinh" :alt="item.tenSanPham" class="h-full w-full object-cover" @error="xuLyAnhLoi" />
            </router-link>

            <div class="flex flex-1 flex-col">
              <div class="flex items-start justify-between gap-3">
                <div>
                  <router-link :to="`/san-pham/${item.giayId}`" class="text-sm font-bold text-slate-900 hover:text-primary line-clamp-2">{{ item.tenSanPham }}</router-link>
                  <p class="mt-1 text-xs text-slate-400">Màu: {{ item.mauSac }} · Size: {{ item.kichCo }}</p>
                  <p v-if="item.tonKho <= 0" class="mt-1 text-xs font-semibold text-rose-500">⚠ Sản phẩm đã hết hàng</p>
                  <p v-else-if="item.soLuong > item.tonKho" class="mt-1 text-xs font-semibold text-amber-600">⚠ Chỉ còn {{ item.tonKho }} sản phẩm</p>
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
            @click="router.push('/thanh-toan')"
            class="mt-6 w-full rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3.5 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5"
          >
            Tiến hành thanh toán
          </button>
          <router-link to="/san-pham" class="mt-3 block text-center text-sm font-medium text-slate-500 hover:text-primary">Tiếp tục mua sắm</router-link>
        </aside>
      </div>
    </div>
  </main>
</template>
