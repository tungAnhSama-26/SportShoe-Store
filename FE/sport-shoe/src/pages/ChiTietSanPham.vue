<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { layChiTietSanPham, layDanhGia } from '../services/san-pham';
import { themVaoGio as apiThemGio } from '../services/gio-hang';
import { gioHangStore } from '../stores/gio-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showWarning, showSuccess, showError } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import anhMacDinh from '../assets/login-shoe.png';

const route = useRoute();
const router = useRouter();

const sanPham = ref(null);
const dangTai = ref(true);
const loi = ref('');

const mauChon = ref('');
const sizeChon = ref('');
const soLuongMua = ref(1);

// Đánh giá (chỉ hiển thị - khách đánh giá qua trang Đơn hàng sau khi nhận hàng).
const danhGia = ref({ diemTrungBinh: 0, soLuong: 0, danhSach: [] });

onMounted(taiTatCa);
watch(() => route.params.id, taiTatCa);

async function taiTatCa() {
  await Promise.all([taiChiTiet(), taiDanhGia()]);
}

async function taiChiTiet() {
  dangTai.value = true;
  loi.value = '';
  mauChon.value = '';
  sizeChon.value = '';
  soLuongMua.value = 1;
  try {
    sanPham.value = await layChiTietSanPham(route.params.id);
  } catch {
    loi.value = 'Không tải được sản phẩm này.';
    sanPham.value = null;
  } finally {
    dangTai.value = false;
  }
}

async function taiDanhGia() {
  try {
    danhGia.value = await layDanhGia(route.params.id);
  } catch {
    danhGia.value = { diemTrungBinh: 0, soLuong: 0, danhSach: [] };
  }
}

const bienThe = computed(() => sanPham.value?.bienThe ?? []);

const danhSachMau = computed(() => {
  const map = new Map();
  bienThe.value.forEach((b) => {
    if (b.mauSac && !map.has(b.mauSac)) map.set(b.mauSac, b.maMauHex || '');
  });
  return Array.from(map, ([ten, hex]) => ({ ten, hex }));
});

const danhSachSize = computed(() => {
  const tap = new Set(bienThe.value.map((b) => b.kichCo).filter(Boolean));
  return Array.from(tap).sort((a, b) => String(a).localeCompare(String(b), 'vi', { numeric: true }));
});

function sizeConHang(size) {
  return bienThe.value.some(
    (b) => b.kichCo === size && (!mauChon.value || b.mauSac === mauChon.value) && b.soLuong > 0
  );
}

const bienTheChon = computed(() =>
  bienThe.value.find((b) => b.mauSac === mauChon.value && b.kichCo === sizeChon.value) || null
);

// Biến thể giá thấp nhất (giá sau giảm) - ảnh mặc định khớp với card ngoài trang danh sách.
const bienTheReNhat = computed(() => {
  let min = null;
  for (const b of bienThe.value) {
    const gia = Number(b.giaBan);
    if (!Number.isFinite(gia)) continue;
    if (!min || gia < Number(min.giaBan)) min = b;
  }
  return min;
});

// Ảnh theo màu đang chọn (khi chưa chọn đủ size): lấy biến thể đầu tiên của màu đó có ảnh.
const anhTheoMau = computed(() => {
  if (!mauChon.value) return null;
  return bienThe.value.find((b) => b.mauSac === mauChon.value && b.hinhAnh)?.hinhAnh || null;
});

// Ưu tiên: biến thể đã chọn -> màu đang chọn -> biến thể rẻ nhất -> ảnh gốc -> ảnh mặc định.
const anhHienThi = computed(
  () =>
    bienTheChon.value?.hinhAnh ||
    anhTheoMau.value ||
    bienTheReNhat.value?.hinhAnh ||
    sanPham.value?.hinhAnh ||
    anhMacDinh
);

const giaHienThi = computed(() => {
  if (bienTheChon.value) return dinhDangTienViet(bienTheChon.value.giaBan);
  const gia = bienThe.value.map((b) => Number(b.giaBan)).filter((n) => Number.isFinite(n));
  if (!gia.length) return '—';
  const min = Math.min(...gia);
  const max = Math.max(...gia);
  return min === max ? dinhDangTienViet(min) : `${dinhDangTienViet(min)} - ${dinhDangTienViet(max)}`;
});

// Có đang giảm giá ở biến thể đã chọn (giá niêm yết > giá hiện tại).
const coGiamChon = computed(() => {
  const b = bienTheChon.value;
  return b && Number(b.giaGoc) > Number(b.giaBan);
});
const phanTramGiamChon = computed(() => {
  const b = bienTheChon.value;
  if (!coGiamChon.value) return 0;
  return Math.round(((Number(b.giaGoc) - Number(b.giaBan)) / Number(b.giaGoc)) * 100);
});

const tonKho = computed(() => bienTheChon.value?.soLuong ?? null);
const NHAN_GIOI_TINH = { 1: 'Nam', 2: 'Nữ', 3: 'Unisex' };
const gioiTinhNhan = computed(() => NHAN_GIOI_TINH[sanPham.value?.gioiTinh] || '');

// Thông số (thuộc tính cấp sản phẩm), ẩn dòng nào không có giá trị.
const thongSo = computed(() => {
  const sp = sanPham.value;
  if (!sp) return [];
  return [
    { nhan: 'Hãng', giaTri: sp.thuongHieu },
    { nhan: 'Loại giày', giaTri: sp.loaiGiay },
    { nhan: 'Giới tính', giaTri: gioiTinhNhan.value },
    { nhan: 'Chất liệu', giaTri: sp.chatLieu },
    { nhan: 'Đế giày', giaTri: sp.deGiay },
    { nhan: 'Cổ giày', giaTri: sp.coGiay },
    { nhan: 'Công nghệ đệm', giaTri: sp.congNgheDem },
    { nhan: 'Trọng lượng', giaTri: sp.trongLuong },
  ].filter((x) => x.giaTri);
});

function chonMau(ten) {
  mauChon.value = ten;
  if (sizeChon.value && !sizeConHang(sizeChon.value)) sizeChon.value = '';
  soLuongMua.value = 1;
}

function chonSize(size) {
  if (!sizeConHang(size)) return;
  sizeChon.value = size;
  soLuongMua.value = 1;
}

watch(tonKho, (ton) => {
  if (ton != null && soLuongMua.value > ton) soLuongMua.value = ton;
});

function tangSoLuong() {
  if (tonKho.value == null || soLuongMua.value < tonKho.value) soLuongMua.value += 1;
}
function giamSoLuong() {
  if (soLuongMua.value > 1) soLuongMua.value -= 1;
}

function kiemTraChon() {
  if (!mauChon.value || !sizeChon.value) {
    showWarning('Bạn chưa chọn thông tin sản phẩm (màu sắc, kích cỡ).');
    return false;
  }
  if (!bienTheChon.value || bienTheChon.value.soLuong <= 0) {
    showWarning('Phiên bản này đã hết hàng. Vui lòng chọn màu/size khác.');
    return false;
  }
  return true;
}

async function themVaoGio() {
  // Khách vãng lai vẫn mua được, không bắt buộc đăng nhập.
  if (!kiemTraChon()) return;
  try {
    const b = bienTheChon.value;
    const gio = await apiThemGio(b.id, soLuongMua.value, {
      giayId: sanPham.value.id,
      tenSanPham: sanPham.value.ten,
      mauSac: b.mauSac,
      kichCo: b.kichCo,
      hinhAnh: b.hinhAnh || anhHienThi.value,
      giaBan: b.giaBan,
      tonKho: b.soLuong,
    });
    gioHangStore.datSoLuong(gio.tongSoLuong);
    showSuccess('Đã thêm sản phẩm vào giỏ hàng!');
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể thêm vào giỏ hàng'));
  }
}

async function muaNgay() {
  if (!kiemTraChon()) return;
  try {
    const b = bienTheChon.value;
    const gio = await apiThemGio(b.id, soLuongMua.value, {
      giayId: sanPham.value.id,
      tenSanPham: sanPham.value.ten,
      mauSac: b.mauSac,
      kichCo: b.kichCo,
      hinhAnh: b.hinhAnh || anhHienThi.value,
      giaBan: b.giaBan,
      tonKho: b.soLuong,
    });
    gioHangStore.datSoLuong(gio.tongSoLuong);
    router.push('/khachhang/gio-hang');
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể mua hàng'));
  }
}

// --- Đánh giá ---
function saoSang(diem, viTri) {
  return viTri <= Math.round(Number(diem) || 0);
}

function formatNgay(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleDateString('vi-VN');
  } catch {
    return '';
  }
}

function chuCaiDau(ten) {
  return String(ten || '?').trim().charAt(0).toUpperCase() || '?';
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) event.target.src = anhMacDinh;
}
</script>

<template>
  <main class="bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-7xl px-6 lg:px-10 pt-8">
      <button @click="router.back()" class="mb-6 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-primary transition-colors">
        <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6" /></svg>
        Quay lại
      </button>

      <div v-if="dangTai" class="py-32 text-center text-sm text-slate-400">Đang tải sản phẩm...</div>
      <div v-else-if="loi || !sanPham" class="py-32 text-center text-sm text-rose-500">{{ loi || 'Không tìm thấy sản phẩm.' }}</div>

      <template v-else>
        <!-- Khối trên: ảnh + chọn mua -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-10">
          <div class="rounded-3xl overflow-hidden bg-white border border-slate-100 shadow-sm">
            <div class="aspect-square bg-slate-50">
              <img :src="anhHienThi" :alt="sanPham.ten" class="w-full h-full object-cover" @error="xuLyAnhLoi" />
            </div>
          </div>

          <div>
            <p v-if="sanPham.thuongHieu" class="text-sm font-semibold text-primary uppercase tracking-wide">{{ sanPham.thuongHieu }}</p>
            <h1 class="mt-2 text-3xl font-bold text-slate-900">{{ sanPham.ten }}</h1>

            <!-- Sao trung bình -->
            <div class="mt-2 flex items-center gap-2">
              <div class="flex">
                <span v-for="i in 5" :key="i" class="text-base" :class="saoSang(danhGia.diemTrungBinh, i) ? 'text-amber-400' : 'text-slate-300'">★</span>
              </div>
              <span class="text-sm text-slate-500">{{ danhGia.diemTrungBinh || 0 }} ({{ danhGia.soLuong }} đánh giá)</span>
              <span class="text-slate-300">|</span>
              <span class="text-sm text-slate-500">Đã bán {{ sanPham.daBan || 0 }}</span>
            </div>

            <p v-if="gioiTinhNhan" class="mt-1 text-sm text-slate-400">Giới tính: {{ gioiTinhNhan }}</p>
            <div class="mt-5 flex items-end gap-3">
              <p class="text-3xl font-bold text-primary">{{ giaHienThi }}</p>
              <p v-if="coGiamChon" class="pb-1 text-lg text-slate-400 line-through">{{ dinhDangTienViet(bienTheChon.giaGoc) }}</p>
              <span v-if="coGiamChon" class="mb-1.5 rounded-md bg-red-600 px-2 py-0.5 text-xs font-extrabold text-white">-{{ phanTramGiamChon }}%</span>
            </div>

            <!-- Màu sắc -->
            <div v-if="danhSachMau.length" class="mt-7">
              <p class="text-sm font-semibold text-slate-700 mb-3">Màu sắc: <span class="text-slate-500 font-normal">{{ mauChon || 'Chưa chọn' }}</span></p>
              <div class="flex flex-wrap gap-2.5">
                <button
                  v-for="m in danhSachMau"
                  :key="m.ten"
                  @click="chonMau(m.ten)"
                  class="flex items-center gap-2 rounded-xl border px-3 py-2 text-sm font-medium transition"
                  :class="mauChon === m.ten ? 'border-primary bg-primary/5 text-primary' : 'border-slate-200 text-slate-600 hover:border-slate-300'"
                >
                  <span v-if="m.hex" class="h-4 w-4 rounded-full border border-slate-300" :style="{ backgroundColor: m.hex }"></span>
                  {{ m.ten }}
                </button>
              </div>
            </div>

            <!-- Kích cỡ -->
            <div v-if="danhSachSize.length" class="mt-6">
              <p class="text-sm font-semibold text-slate-700 mb-3">Kích cỡ: <span class="text-slate-500 font-normal">{{ sizeChon || 'Chưa chọn' }}</span></p>
              <div class="flex flex-wrap gap-2.5">
                <button
                  v-for="s in danhSachSize"
                  :key="s"
                  @click="chonSize(s)"
                  :disabled="!sizeConHang(s)"
                  class="min-w-[48px] rounded-xl border px-3 py-2 text-sm font-medium transition"
                  :class="[
                    sizeChon === s ? 'border-primary bg-primary/5 text-primary' : 'border-slate-200 text-slate-600 hover:border-slate-300',
                    !sizeConHang(s) ? 'opacity-40 cursor-not-allowed line-through' : '',
                  ]"
                >
                  {{ s }}
                </button>
              </div>
            </div>

            <!-- Số lượng -->
            <div class="mt-6 flex items-center gap-4">
              <p class="text-sm font-semibold text-slate-700">Số lượng</p>
              <div class="flex items-center rounded-xl border border-slate-200">
                <button @click="giamSoLuong" class="px-3 py-2 text-slate-500 hover:text-primary">−</button>
                <span class="w-10 text-center text-sm font-semibold">{{ soLuongMua }}</span>
                <button @click="tangSoLuong" class="px-3 py-2 text-slate-500 hover:text-primary">+</button>
              </div>
              <span v-if="bienTheChon" class="text-xs text-slate-400">Còn {{ tonKho }} sản phẩm</span>
            </div>

            <!-- Nút hành động -->
            <div class="mt-8 flex flex-col sm:flex-row gap-3">
              <button @click="themVaoGio" class="flex-1 inline-flex items-center justify-center gap-2 rounded-2xl border border-primary bg-white px-6 py-3.5 text-sm font-bold text-primary transition hover:bg-primary/5">
                <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="20" r="1" /><circle cx="18" cy="20" r="1" /><path d="M3 4h2l2.4 10.2a1 1 0 0 0 1 .8h9.7a1 1 0 0 0 1-.8L21 7H7" /></svg>
                Thêm vào giỏ
              </button>
              <button @click="muaNgay" class="flex-1 inline-flex items-center justify-center rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3.5 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5">
                Mua ngay
              </button>
            </div>
          </div>
        </div>

        <!-- Thông tin sản phẩm -->
        <section class="mt-12 rounded-3xl bg-white border border-slate-100 shadow-sm p-6 lg:p-8">
          <h2 class="text-lg font-bold text-slate-900 mb-5">Thông tin sản phẩm</h2>
          <table v-if="thongSo.length" class="w-full text-sm">
            <tbody>
              <tr v-for="ts in thongSo" :key="ts.nhan" class="border-b border-slate-50 last:border-0">
                <td class="py-3 pr-4 w-48 font-semibold text-slate-500 align-top">{{ ts.nhan }}</td>
                <td class="py-3 text-slate-800">{{ ts.giaTri }}</td>
              </tr>
            </tbody>
          </table>
          <p v-else class="text-sm text-slate-400">Chưa có thông tin sản phẩm.</p>
        </section>

        <!-- Mô tả -->
        <section class="mt-8 rounded-3xl bg-white border border-slate-100 shadow-sm p-6 lg:p-8">
          <h2 class="text-lg font-bold text-slate-900 mb-4">Mô tả sản phẩm</h2>
          <p v-if="sanPham.moTa" class="text-sm leading-7 text-slate-600 whitespace-pre-line">{{ sanPham.moTa }}</p>
          <p v-else class="text-sm text-slate-400">Sản phẩm chưa có mô tả.</p>
        </section>

        <!-- Đánh giá khách hàng -->
        <section class="mt-8 rounded-3xl bg-white border border-slate-100 shadow-sm p-6 lg:p-8">
          <h2 class="text-lg font-bold text-slate-900 mb-5">Đánh giá của khách hàng</h2>

          <!-- Tổng quan -->
          <div class="flex items-center gap-5 mb-6 pb-6 border-b border-slate-100">
            <div class="text-center">
              <p class="text-4xl font-bold text-slate-900">{{ danhGia.diemTrungBinh || 0 }}</p>
              <div class="mt-1 flex justify-center">
                <span v-for="i in 5" :key="i" class="text-sm" :class="saoSang(danhGia.diemTrungBinh, i) ? 'text-amber-400' : 'text-slate-300'">★</span>
              </div>
              <p class="mt-1 text-xs text-slate-400">{{ danhGia.soLuong }} đánh giá</p>
            </div>
          </div>

          <!-- Danh sách đánh giá (chỉ khách đã mua và nhận hàng mới đánh giá được, từ trang đơn hàng) -->
          <div v-if="danhGia.danhSach.length" class="space-y-5">
            <div v-for="dg in danhGia.danhSach" :key="dg.id" class="flex gap-4">
              <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">
                {{ chuCaiDau(dg.hoTenKhach) }}
              </div>
              <div class="flex-1">
                <div class="flex items-center gap-3">
                  <p class="text-sm font-semibold text-slate-800">{{ dg.hoTenKhach }}</p>
                  <span class="text-xs text-slate-400">{{ formatNgay(dg.ngayTao) }}</span>
                </div>
                <div class="mt-0.5 flex">
                  <span v-for="i in 5" :key="i" class="text-xs" :class="i <= dg.soSao ? 'text-amber-400' : 'text-slate-300'">★</span>
                </div>
                <p v-if="dg.noiDung" class="mt-1.5 text-sm leading-6 text-slate-600">{{ dg.noiDung }}</p>
              </div>
            </div>
          </div>
          <p v-else class="text-sm text-slate-400">Chưa có đánh giá nào. Khách hàng đã mua và nhận hàng có thể đánh giá trong mục Đơn hàng của bạn.</p>
        </section>
      </template>
    </div>
  </main>
</template>
