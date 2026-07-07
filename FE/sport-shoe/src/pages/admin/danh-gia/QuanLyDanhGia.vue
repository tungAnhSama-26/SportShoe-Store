<script setup>
import { computed, onMounted, ref } from 'vue';
import { Eye, Sparkles } from 'lucide-vue-next';
import {
  laySanPhamCoDanhGia,
  layDanhGiaTheoSanPham,
  layTatCaDanhGia,
  xoaDanhGia,
  phanHoiDanhGia,
  khoiPhucDanhGia,
  tongHopDanhGiaAI,
} from '../../../services/admin-danh-gia';
import DanhGiaMedia from '../../../components/DanhGiaMedia.vue';
import { resolveMediaUrl, parseMedia } from '../../../utils/media';
import { showSuccess, showError, showConfirm } from '../../../utils/alert';
import { getDisplayErrorMessage } from '../../../utils/error-message';
import anhMacDinh from '../../../assets/login-shoe.png';

const cheDo = ref('theo-san-pham'); // 'theo-san-pham' | 'tat-ca'

// Chế độ theo sản phẩm.
const dsSanPham = ref([]);
const keyword = ref('');
const dangTaiSP = ref(false);
const spDangChon = ref(null);

// Danh sách đánh giá hiện tại (dùng chung 2 chế độ).
const dsDanhGia = ref([]);
const dangTaiDG = ref(false);
const saoLoc = ref(null); // null = tất cả, 1..5

const formPhanHoi = ref({});
const dangGuiPH = ref(null);
const dangXoa = ref(null);
const dangKhoiPhuc = ref(null);

// Bộ lọc: trạng thái ('' = tất cả, 1 = đang hiển thị, 0 = đã ẩn) + khoảng ngày tạo.
const locTrangThai = ref(1);
const locTuNgay = ref('');
const locDenNgay = ref('');

// AI tổng hợp đánh giá.
const dangTongHop = ref(false);
const aiKetQua = ref('');

let timer = null;

function boLocHienTai() {
  return {
    trangThai: locTrangThai.value === '' ? undefined : locTrangThai.value,
    tuNgay: locTuNgay.value || undefined,
    denNgay: locDenNgay.value || undefined,
  };
}

// Đổi bộ lọc -> tải lại danh sách đánh giá đang xem.
function apDungLoc() {
  if (cheDo.value === 'tat-ca') taiTatCa();
  else if (spDangChon.value) moSanPham(spDangChon.value);
}

async function khoiPhuc(dg) {
  dangKhoiPhuc.value = dg.id;
  try {
    const res = await khoiPhucDanhGia(dg.id);
    dg.trangThai = res.trangThai;
    dg.lyDoAn = res.lyDoAn;
    showSuccess('Đã khôi phục đánh giá');
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể khôi phục đánh giá'));
  } finally {
    dangKhoiPhuc.value = null;
  }
}

async function tongHopAI() {
  if (dangTongHop.value) return;
  dangTongHop.value = true;
  aiKetQua.value = '';
  try {
    aiKetQua.value = await tongHopDanhGiaAI(cheDo.value === 'tat-ca' ? null : spDangChon.value?.giayId);
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'AI không tổng hợp được, thử lại sau'));
  } finally {
    dangTongHop.value = false;
  }
}

onMounted(taiSanPham);

// Thống kê tính từ toàn bộ đánh giá đã tải (chưa lọc sao).
const thongKe = computed(() => {
  const ds = dsDanhGia.value;
  const theoSao = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
  let tong = 0;
  ds.forEach((d) => {
    if (theoSao[d.soSao] !== undefined) theoSao[d.soSao] += 1;
    tong += d.soSao || 0;
  });
  return {
    tongSo: ds.length,
    diemTrungBinh: ds.length ? Math.round((tong / ds.length) * 10) / 10 : 0,
    theoSao,
  };
});

const dsDanhGiaHienThi = computed(() =>
  saoLoc.value ? dsDanhGia.value.filter((d) => d.soSao === saoLoc.value) : dsDanhGia.value,
);

// Hiển thị thanh đánh giá khi: đang xem 1 sản phẩm, hoặc đang ở chế độ tất cả.
const dangXemDanhGia = computed(() => cheDo.value === 'tat-ca' || !!spDangChon.value);

async function taiSanPham() {
  dangTaiSP.value = true;
  try {
    dsSanPham.value = (await laySanPhamCoDanhGia(keyword.value.trim())) || [];
  } catch {
    dsSanPham.value = [];
  } finally {
    dangTaiSP.value = false;
  }
}

function timKiem() {
  clearTimeout(timer);
  timer = setTimeout(taiSanPham, 350);
}

async function moSanPham(sp) {
  spDangChon.value = sp;
  saoLoc.value = null;
  dsDanhGia.value = [];
  dangTaiDG.value = true;
  try {
    dsDanhGia.value = (await layDanhGiaTheoSanPham(sp.giayId, boLocHienTai())) || [];
    const found = dsSanPham.value.find((x) => x.giayId === sp.giayId);
    if (found) found.soChuaXem = 0;
    window.dispatchEvent(new CustomEvent('danh-gia-da-xem'));
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không tải được đánh giá'));
  } finally {
    dangTaiDG.value = false;
  }
}

function quayLai() {
  spDangChon.value = null;
  dsDanhGia.value = [];
  saoLoc.value = null;
  aiKetQua.value = '';
  taiSanPham();
}

async function taiTatCa() {
  dangTaiDG.value = true;
  saoLoc.value = null;
  dsDanhGia.value = [];
  try {
    dsDanhGia.value = (await layTatCaDanhGia(boLocHienTai())) || [];
    window.dispatchEvent(new CustomEvent('danh-gia-da-xem')); // mở tất cả = đã xem hết
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không tải được đánh giá'));
    dsDanhGia.value = [];
  } finally {
    dangTaiDG.value = false;
  }
}

function doiCheDo() {
  spDangChon.value = null;
  saoLoc.value = null;
  dsDanhGia.value = [];
  aiKetQua.value = '';
  if (cheDo.value === 'tat-ca') taiTatCa();
  else taiSanPham();
}

async function xoa(dg) {
  const ok = await showConfirm('Xóa đánh giá này? Đánh giá sẽ bị ẩn khỏi mọi nơi.', 'Xác nhận xóa');
  if (!ok) return;
  dangXoa.value = dg.id;
  try {
    await xoaDanhGia(dg.id);
    if (locTrangThai.value === 1) {
      dsDanhGia.value = dsDanhGia.value.filter((x) => x.id !== dg.id);
    } else {
      dg.trangThai = 0;
      dg.lyDoAn = 'Quản trị viên xóa';
    }
    showSuccess('Đã xóa đánh giá');
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể xóa đánh giá'));
  } finally {
    dangXoa.value = null;
  }
}

async function guiPhanHoi(dg) {
  const text = (formPhanHoi.value[dg.id] || '').trim();
  if (!text) return showError('Vui lòng nhập nội dung phản hồi');
  dangGuiPH.value = dg.id;
  try {
    const res = await phanHoiDanhGia(dg.id, text);
    dg.phanHoi = res.phanHoi;
    dg.ngayPhanHoi = res.ngayPhanHoi;
    delete formPhanHoi.value[dg.id];
    showSuccess('Đã gửi phản hồi');
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể gửi phản hồi'));
  } finally {
    dangGuiPH.value = null;
  }
}

function chonSao(sao) {
  saoLoc.value = saoLoc.value === sao ? null : sao;
}
function chuCaiDau(t) {
  return (t || '?').trim().charAt(0).toUpperCase();
}
function formatNgay(v) {
  if (!v) return '';
  return new Date(v).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}
function anhSP(u) {
  if (!u) return anhMacDinh;
  const mediaArr = parseMedia(u);
  if (mediaArr && mediaArr.length > 0) {
    return resolveMediaUrl(mediaArr[0].url) || anhMacDinh;
  }
  if (typeof u === 'string') {
    const parts = u.split(',');
    if (parts.length > 0 && parts[0].trim()) {
      return resolveMediaUrl(parts[0].trim()) || anhMacDinh;
    }
  }
  return resolveMediaUrl(u) || anhMacDinh;
}
function xuLyAnhLoi(e) {
  if (e.target.src !== anhMacDinh) e.target.src = anhMacDinh;
}
</script>

<template>
  <div class="p-4 md:p-6">
    <!-- Tiêu đề + combobox chuyển chế độ -->
    <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-xl font-bold text-slate-800 dark:text-white">Quản lý đánh giá</h1>
        <p class="mt-0.5 text-sm text-slate-400">Xem và quản lý đánh giá của khách hàng.</p>
      </div>
      <select
        v-model="cheDo"
        @change="doiCheDo"
        class="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 dark:bg-slate-800 dark:border-slate-600 dark:text-white"
      >
        <option value="theo-san-pham">Xem theo sản phẩm</option>
        <option value="tat-ca">Tất cả đánh giá</option>
      </select>
    </div>

    <!-- ===== Bảng sản phẩm (chế độ theo sản phẩm, chưa chọn SP) ===== -->
    <template v-if="cheDo === 'theo-san-pham' && !spDangChon">
      <div class="mb-4 flex justify-end">
        <input
          v-model="keyword"
          @input="timKiem"
          type="text"
          placeholder="Tìm theo tên hoặc mã sản phẩm..."
          class="w-72 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 dark:bg-slate-800 dark:border-slate-600 dark:text-white"
        />
      </div>

      <div class="overflow-hidden rounded-2xl border border-slate-100 bg-white shadow-sm dark:bg-slate-800 dark:border-slate-700">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-slate-100 bg-slate-50/60 text-left text-[13px] font-semibold text-slate-500 dark:bg-slate-700/40 dark:border-slate-700">
              <th class="px-4 py-3">Sản phẩm</th>
              <th class="px-4 py-3 text-center">Số đánh giá</th>
              <th class="px-4 py-3 text-center">Điểm TB</th>
              <th class="px-4 py-3">Mới nhất</th>
              <th class="px-4 py-3 text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTaiSP">
              <td colspan="5" class="px-4 py-10 text-center text-slate-400">Đang tải...</td>
            </tr>
            <tr v-else-if="!dsSanPham.length">
              <td colspan="5" class="px-4 py-10 text-center text-slate-400">Chưa có sản phẩm nào được đánh giá.</td>
            </tr>
            <tr
              v-for="sp in dsSanPham"
              :key="sp.giayId"
              class="border-b border-slate-50 transition hover:bg-slate-50/60 dark:border-slate-700/50"
            >
              <td class="px-4 py-3">
                <div class="flex items-center gap-3">
                  <div class="relative">
                    <img :src="anhSP(sp.hinhAnh)" :alt="sp.ten" class="h-11 w-11 rounded-lg object-cover bg-slate-100" @error="xuLyAnhLoi" />
                    <span
                      v-if="sp.soChuaXem > 0"
                      class="absolute -right-1.5 -top-1.5 flex h-5 min-w-[20px] items-center justify-center rounded-full bg-rose-500 px-1 text-[11px] font-bold text-white"
                    >{{ sp.soChuaXem }}</span>
                  </div>
                  <div class="min-w-0">
                    <p class="truncate font-medium text-slate-700 dark:text-slate-200">{{ sp.ten }}</p>
                    <p class="text-xs text-slate-400">{{ sp.ma }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3 text-center text-slate-600 dark:text-slate-300">{{ sp.soDanhGia }}</td>
              <td class="px-4 py-3 text-center">
                <span class="inline-flex items-center gap-1 font-semibold text-amber-500">{{ sp.diemTrungBinh }} <span>★</span></span>
              </td>
              <td class="px-4 py-3 text-slate-500 dark:text-slate-400">{{ formatNgay(sp.ngayMoiNhat) }}</td>
              <td class="px-4 py-3 text-right">
                <button
                  type="button"
                  @click="moSanPham(sp)"
                  class="inline-flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-primary/10 hover:text-primary dark:bg-slate-700 dark:text-slate-300"
                  title="Xem đánh giá"
                >
                  <Eye class="h-4 w-4" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- ===== Màn xem đánh giá (theo SP đã chọn HOẶC tất cả) ===== -->
    <template v-else-if="dangXemDanhGia">
      <!-- Header: nút quay lại + thông tin SP (chế độ theo SP) -->
      <template v-if="spDangChon">
        <button @click="quayLai" class="mb-4 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-primary">
          <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6" /></svg>
          Quay lại danh sách
        </button>
        <div class="mb-4 flex items-center gap-4 rounded-2xl border border-slate-100 bg-white p-4 shadow-sm dark:bg-slate-800 dark:border-slate-700">
          <img :src="anhSP(spDangChon.hinhAnh)" :alt="spDangChon.ten" class="h-14 w-14 rounded-lg object-cover bg-slate-100" @error="xuLyAnhLoi" />
          <div>
            <h2 class="font-bold text-slate-800 dark:text-white">{{ spDangChon.ten }}</h2>
            <p class="text-xs text-slate-400">{{ spDangChon.ma }}</p>
          </div>
        </div>
      </template>

      <!-- Thanh thống kê + phân loại sao (dùng chung) -->
      <div class="mb-4 rounded-2xl border border-slate-100 bg-white p-4 shadow-sm dark:bg-slate-800 dark:border-slate-700">
        <div class="flex flex-wrap items-center gap-x-8 gap-y-3">
          <div class="flex items-center gap-3">
            <span class="text-3xl font-bold text-amber-500">{{ thongKe.diemTrungBinh }}</span>
            <div>
              <div class="flex">
                <span v-for="i in 5" :key="i" class="text-sm" :class="i <= Math.round(thongKe.diemTrungBinh) ? 'text-amber-400' : 'text-slate-300'">★</span>
              </div>
              <p class="text-xs text-slate-400">
                {{ cheDo === 'tat-ca' ? 'Trung bình cả shop' : 'Trung bình sản phẩm' }} · {{ thongKe.tongSo }} đánh giá
              </p>
            </div>
          </div>

          <!-- Phân loại sao + số lượng từng loại -->
          <div class="flex flex-wrap gap-2">
            <button
              type="button"
              @click="saoLoc = null"
              class="rounded-full border px-3.5 py-1.5 text-sm font-medium transition"
              :class="saoLoc === null ? 'border-primary bg-primary text-white' : 'border-slate-200 text-slate-500 hover:border-primary hover:text-primary dark:border-slate-600'"
            >
              Tất cả ({{ thongKe.tongSo }})
            </button>
            <button
              v-for="sao in [5, 4, 3, 2, 1]"
              :key="sao"
              type="button"
              @click="chonSao(sao)"
              class="rounded-full border px-3.5 py-1.5 text-sm font-medium transition"
              :class="saoLoc === sao ? 'border-amber-400 bg-amber-400 text-white' : 'border-slate-200 text-slate-500 hover:border-amber-400 hover:text-amber-500 dark:border-slate-600'"
            >
              {{ sao }} <span :class="saoLoc === sao ? 'text-white' : 'text-amber-400'">★</span> ({{ thongKe.theoSao[sao] }})
            </button>
          </div>
        </div>
      </div>

      <!-- Bộ lọc thời gian + trạng thái + AI tổng hợp -->
      <div class="mb-4 flex flex-wrap items-end gap-3 rounded-2xl border border-slate-100 bg-white p-4 shadow-sm dark:bg-slate-800 dark:border-slate-700">
        <div>
          <label class="mb-1 block text-xs font-semibold text-slate-400">Từ ngày</label>
          <input v-model="locTuNgay" type="date" @change="apDungLoc"
                 class="rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:border-primary dark:bg-slate-700 dark:border-slate-600 dark:text-white" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-semibold text-slate-400">Đến ngày</label>
          <input v-model="locDenNgay" type="date" @change="apDungLoc"
                 class="rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:border-primary dark:bg-slate-700 dark:border-slate-600 dark:text-white" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-semibold text-slate-400">Trạng thái</label>
          <select v-model="locTrangThai" @change="apDungLoc"
                  class="rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:border-primary dark:bg-slate-700 dark:border-slate-600 dark:text-white">
            <option :value="1">Đang hiển thị</option>
            <option :value="0">Đã ẩn / xóa</option>
            <option value="">Tất cả</option>
          </select>
        </div>
        <button
          @click="tongHopAI"
          :disabled="dangTongHop"
          class="ml-auto inline-flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-2.5 text-sm font-bold text-white transition hover:bg-violet-700 disabled:opacity-60"
        >
          <Sparkles class="h-4 w-4" />
          {{ dangTongHop ? 'AI đang phân tích...' : (cheDo === 'tat-ca' ? 'AI tổng hợp cả shop' : 'AI tổng hợp sản phẩm') }}
        </button>
      </div>

      <!-- Kết quả AI tổng hợp -->
      <div v-if="aiKetQua" class="mb-4 rounded-2xl border border-violet-200 bg-violet-50/60 p-5 dark:bg-violet-500/10 dark:border-violet-500/30">
        <div class="mb-2 flex items-center justify-between">
          <span class="inline-flex items-center gap-2 text-sm font-bold text-violet-700 dark:text-violet-300">
            <Sparkles class="h-4 w-4" /> Nhận xét của AI
          </span>
          <button @click="aiKetQua = ''" class="text-xs font-medium text-slate-400 hover:text-slate-600">Đóng</button>
        </div>
        <p class="whitespace-pre-wrap text-sm leading-6 text-slate-700 dark:text-slate-200">{{ aiKetQua }}</p>
      </div>

      <!-- Danh sách đánh giá -->
      <div v-if="dangTaiDG" class="py-16 text-center text-sm text-slate-400">Đang tải đánh giá...</div>
      <div v-else-if="!dsDanhGia.length" class="py-16 text-center text-sm text-slate-400">Chưa có đánh giá nào.</div>
      <div v-else-if="!dsDanhGiaHienThi.length" class="py-16 text-center text-sm text-slate-400">Không có đánh giá {{ saoLoc }} sao.</div>

      <div v-else class="space-y-4">
        <div v-for="dg in dsDanhGiaHienThi" :key="dg.id" class="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm dark:bg-slate-800 dark:border-slate-700">
          <!-- Sản phẩm (chỉ ở chế độ tất cả) -->
          <div v-if="cheDo === 'tat-ca' && dg.tenSanPham" class="mb-3 flex items-center gap-2 border-b border-slate-50 pb-3 dark:border-slate-700/50">
            <img :src="anhSP(dg.hinhAnhSanPham)" :alt="dg.tenSanPham" class="h-9 w-9 rounded-lg object-cover bg-slate-100" @error="xuLyAnhLoi" />
            <span class="text-sm font-medium text-slate-600 dark:text-slate-300">{{ dg.tenSanPham }}</span>
          </div>

          <div class="flex gap-4">
            <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">
              {{ chuCaiDau(dg.hoTenKhach) }}
            </div>
            <div class="flex-1">
              <div class="flex items-center justify-between gap-3">
                <div class="flex flex-wrap items-center gap-2">
                  <p class="text-sm font-semibold text-slate-800 dark:text-slate-100">{{ dg.hoTenKhach }}</p>
                  <span class="text-xs text-slate-400">{{ formatNgay(dg.ngayTao) }}</span>
                  <span v-if="dg.trangThai === 0" class="rounded-full bg-rose-100 px-2 py-0.5 text-[10px] font-bold text-rose-600" :title="dg.lyDoAn || ''">
                    Đã ẩn{{ dg.lyDoAn ? ' · ' + dg.lyDoAn : '' }}
                  </span>
                </div>
                <button
                  v-if="dg.trangThai === 0"
                  @click="khoiPhuc(dg)"
                  :disabled="dangKhoiPhuc === dg.id"
                  class="rounded-lg px-3 py-1.5 text-xs font-medium text-emerald-600 transition hover:bg-emerald-50 disabled:opacity-50 dark:hover:bg-emerald-500/10"
                >
                  {{ dangKhoiPhuc === dg.id ? 'Đang khôi phục...' : 'Khôi phục' }}
                </button>
                <button
                  v-else
                  @click="xoa(dg)"
                  :disabled="dangXoa === dg.id"
                  class="rounded-lg px-3 py-1.5 text-xs font-medium text-rose-500 transition hover:bg-rose-50 disabled:opacity-50 dark:hover:bg-rose-500/10"
                >
                  {{ dangXoa === dg.id ? 'Đang xóa...' : 'Xóa' }}
                </button>
              </div>
              <div class="mt-0.5 flex">
                <span v-for="i in 5" :key="i" class="text-sm" :class="i <= dg.soSao ? 'text-amber-400' : 'text-slate-300'">★</span>
              </div>
              <p v-if="dg.noiDung" class="mt-1.5 text-sm leading-6 text-slate-600 dark:text-slate-300">{{ dg.noiDung }}</p>
              <DanhGiaMedia :media="dg.media" />

              <!-- Phản hồi đã có -->
              <div v-if="dg.phanHoi" class="mt-3 rounded-xl border-l-2 border-primary bg-slate-50 p-3.5 dark:bg-slate-700/40">
                <div class="flex items-center gap-2">
                  <span class="text-xs font-bold text-primary">Phản hồi của shop</span>
                  <span class="text-xs text-slate-400">{{ formatNgay(dg.ngayPhanHoi) }}</span>
                </div>
                <p class="mt-1 text-sm leading-6 text-slate-600 dark:text-slate-300">{{ dg.phanHoi }}</p>
              </div>

              <!-- Form phản hồi (không cho phản hồi đánh giá đã ẩn) -->
              <div v-else-if="dg.trangThai !== 0" class="mt-3">
                <textarea
                  v-model="formPhanHoi[dg.id]"
                  rows="2"
                  maxlength="1000"
                  placeholder="Nhập phản hồi cho khách (chỉ phản hồi được 1 lần)..."
                  class="w-full rounded-xl border border-slate-200 px-3.5 py-2.5 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 resize-none dark:bg-slate-700 dark:border-slate-600 dark:text-white"
                ></textarea>
                <div class="mt-2 flex justify-end">
                  <button
                    @click="guiPhanHoi(dg)"
                    :disabled="dangGuiPH === dg.id"
                    class="rounded-lg bg-primary px-4 py-2 text-xs font-bold text-white transition hover:bg-primary/90 disabled:opacity-60"
                  >
                    {{ dangGuiPH === dg.id ? 'Đang gửi...' : 'Gửi phản hồi' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
