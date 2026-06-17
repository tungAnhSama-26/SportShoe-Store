<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import Card from '../components/ui/Card.vue';
import { layTatCaSanPham } from '../services/san-pham';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import anhMacDinh from '../assets/login-shoe.png';

const tatCaSanPham = ref([]);
const dangTai = ref(true);
const sapXep = ref('moi');

// Khoảng giá nhập tay.
const giaMin = ref('');
const giaMax = ref('');

// Các nhóm thuộc tính lọc dạng checkbox. khoa = tên trường trong dữ liệu sản phẩm.
// nhieu = true: trường là mảng (1 sản phẩm có nhiều giá trị, vd màu sắc, kích cỡ).
const NHOM_LOC = [
  { khoa: 'thuongHieu', nhan: 'Hãng' },
  { khoa: 'mauSac', nhan: 'Màu sắc', nhieu: true },
  { khoa: 'kichCo', nhan: 'Kích cỡ', nhieu: true },
  { khoa: 'loaiGiay', nhan: 'Loại giày' },
  { khoa: 'gioiTinhNhan', nhan: 'Giới tính' },
  { khoa: 'chatLieu', nhan: 'Chất liệu' },
  { khoa: 'deGiay', nhan: 'Đế giày' },
  { khoa: 'coGiay', nhan: 'Cổ giày' },
  { khoa: 'congNgheDem', nhan: 'Công nghệ đệm' },
];

// Giá trị đã chọn cho mỗi nhóm (mảng).
const boLoc = ref(Object.fromEntries(NHOM_LOC.map((n) => [n.khoa, []])));

const route = useRoute();

// Vào trang với ?hang=<tên hãng> (từ ô hãng ở trang chủ) -> tự tích bộ lọc hãng đó.
function apLocTuQuery() {
  const hang = route.query.hang;
  if (hang) boLoc.value.thuongHieu = [String(hang)];
}

watch(() => route.query.hang, apLocTuQuery);

onMounted(async () => {
  apLocTuQuery();
  try {
    tatCaSanPham.value = await layTatCaSanPham();
  } catch {
    tatCaSanPham.value = [];
  } finally {
    dangTai.value = false;
  }
});

// Các giá trị duy nhất cho một nhóm, lấy từ chính dữ liệu sản phẩm.
// Nhóm đa trị (màu/size): gom từ mảng; nhóm đơn trị: lấy trực tiếp.
function giaTriDuyNhat(nhom) {
  const tap = new Set();
  tatCaSanPham.value.forEach((p) => {
    if (nhom.nhieu) (p[nhom.khoa] || []).forEach((v) => v && tap.add(v));
    else if (p[nhom.khoa]) tap.add(p[nhom.khoa]);
  });
  return Array.from(tap).sort((a, b) => String(a).localeCompare(String(b), 'vi', { numeric: true }));
}

// Chỉ hiển thị nhóm có ít nhất 1 giá trị.
const cacNhomCoGiaTri = computed(() =>
  NHOM_LOC.map((n) => ({ ...n, giaTri: giaTriDuyNhat(n) })).filter((n) => n.giaTri.length)
);

const dangLoc = computed(
  () =>
    giaMin.value !== '' ||
    giaMax.value !== '' ||
    NHOM_LOC.some((n) => boLoc.value[n.khoa].length)
);

const danhSachLoc = computed(() => {
  let ds = tatCaSanPham.value;

  for (const n of NHOM_LOC) {
    const chon = boLoc.value[n.khoa];
    if (chon.length) {
      ds = n.nhieu
        ? ds.filter((p) => chon.some((c) => (p[n.khoa] || []).includes(c)))
        : ds.filter((p) => chon.includes(p[n.khoa]));
    }
  }

  const min = giaMin.value === '' ? null : Number(giaMin.value);
  const max = giaMax.value === '' ? null : Number(giaMax.value);
  if (Number.isFinite(min)) ds = ds.filter((p) => p.gia >= min);
  if (Number.isFinite(max)) ds = ds.filter((p) => p.gia <= max);

  ds = [...ds];
  if (sapXep.value === 'gia-tang') ds.sort((a, b) => a.gia - b.gia);
  else if (sapXep.value === 'gia-giam') ds.sort((a, b) => b.gia - a.gia);
  return ds;
});

function xoaLoc() {
  for (const n of NHOM_LOC) boLoc.value[n.khoa] = [];
  giaMin.value = '';
  giaMax.value = '';
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) {
    event.target.src = anhMacDinh;
  }
}
</script>

<template>
  <main class="bg-slate-50 min-h-screen pb-20">
    <!-- Hero Section -->
    <section class="bg-black text-white py-16 px-6 lg:px-10">
      <div class="mx-auto max-w-7xl">
        <h1 class="text-4xl md:text-5xl font-bold tracking-tight mb-4">Tất cả sản phẩm</h1>
        <p class="text-slate-400 max-w-xl text-sm md:text-base">Khám phá toàn bộ các mẫu giày đang được bán tại cửa hàng, được thiết kế cho mọi nhu cầu và phong cách.</p>
      </div>
    </section>

    <div class="mx-auto max-w-7xl px-6 lg:px-10 mt-10 grid grid-cols-1 lg:grid-cols-[280px_1fr] gap-10">
      <!-- Sidebar Filters -->
      <aside class="space-y-7">
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-bold text-slate-900">Bộ lọc</h2>
          <button v-if="dangLoc" @click="xoaLoc" class="text-xs font-semibold text-primary hover:underline">Xóa lọc</button>
        </div>

        <!-- Khoảng giá: 2 ô input -->
        <div>
          <h3 class="text-sm font-bold text-slate-900 mb-3 pb-2 border-b border-slate-200">Khoảng giá (đ)</h3>
          <div class="flex items-center gap-2">
            <input
              v-model="giaMin"
              type="number"
              min="0"
              placeholder="Giá từ"
              class="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
            />
            <span class="text-slate-400">—</span>
            <input
              v-model="giaMax"
              type="number"
              min="0"
              placeholder="Giá đến"
              class="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
            />
          </div>
        </div>

        <!-- Các nhóm thuộc tính: checkbox -->
        <div v-for="nhom in cacNhomCoGiaTri" :key="nhom.khoa">
          <h3 class="text-sm font-bold text-slate-900 mb-3 pb-2 border-b border-slate-200">{{ nhom.nhan }}</h3>
          <div class="space-y-2.5 text-sm text-slate-600 font-medium max-h-56 overflow-y-auto pr-1">
            <label
              v-for="gt in nhom.giaTri"
              :key="gt"
              class="flex items-center gap-3 cursor-pointer hover:text-primary transition-colors"
            >
              <input
                type="checkbox"
                :value="gt"
                v-model="boLoc[nhom.khoa]"
                class="rounded border-slate-300 text-primary focus:ring-primary/30"
              />
              {{ gt }}
            </label>
          </div>
        </div>
      </aside>

      <!-- Product Grid -->
      <section>
        <div class="flex items-center justify-between mb-6">
          <p class="text-sm text-slate-500">Hiển thị <span class="font-bold text-slate-900">{{ danhSachLoc.length }}</span> sản phẩm</p>
          <select v-model="sapXep" class="text-sm border-slate-200 rounded-lg focus:border-primary focus:ring-primary/30 bg-white">
            <option value="moi">Mới nhất</option>
            <option value="gia-tang">Giá tăng dần</option>
            <option value="gia-giam">Giá giảm dần</option>
          </select>
        </div>

        <div v-if="dangTai" class="py-20 text-center text-sm text-slate-400">Đang tải sản phẩm...</div>

        <div v-else-if="!danhSachLoc.length" class="py-20 text-center text-sm text-slate-400">
          Không có sản phẩm phù hợp với bộ lọc.
        </div>

        <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          <router-link
            v-for="sp in danhSachLoc"
            :key="sp.id"
            :to="`/khachhang/san-pham/${sp.id}`"
            class="block"
          >
            <Card
              class="group overflow-hidden cursor-pointer flex flex-col hover:shadow-xl hover:-translate-y-1 transition-all duration-300 border-none bg-white h-full"
            >
              <div class="relative aspect-square overflow-hidden bg-slate-100">
                <img :src="sp.hinhAnh" :alt="sp.ten" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" @error="xuLyAnhLoi" />
                <span v-if="sp.nhan" class="absolute left-3 top-3 rounded-md bg-red-600 px-2.5 py-1 text-xs font-extrabold text-white shadow-md">{{ sp.nhan }}</span>
              </div>
              <div class="p-4 flex flex-col flex-1">
                <p class="text-xs text-slate-400 mb-1 font-medium">{{ sp.thuongHieu }}</p>
                <h3 class="font-bold text-slate-900 text-base mb-2 line-clamp-2 group-hover:text-primary transition-colors">{{ sp.ten }}</h3>
                <div class="mt-auto flex items-end gap-2">
                  <p class="font-bold text-lg text-primary">{{ dinhDangTienViet(sp.gia) }}</p>
                  <p v-if="sp.giaCu" class="text-xs text-slate-400 line-through pb-1">{{ dinhDangTienViet(sp.giaCu) }}</p>
                </div>
                <!-- Sao trung bình dưới giá -->
                <div class="mt-1.5 flex items-center gap-1.5">
                  <div class="flex text-xs">
                    <span v-for="i in 5" :key="i" :class="i <= Math.round(sp.soSao) ? 'text-amber-400' : 'text-slate-300'">★</span>
                  </div>
                  <span class="text-xs text-slate-400">{{ sp.soDanhGia ? `${sp.soSao.toFixed(1)} (${sp.soDanhGia})` : 'Chưa có đánh giá' }}</span>
                </div>
              </div>
            </Card>
          </router-link>
        </div>
      </section>
    </div>
  </main>
</template>
