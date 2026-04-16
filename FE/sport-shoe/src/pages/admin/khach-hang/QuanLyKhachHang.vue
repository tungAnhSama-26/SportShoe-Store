<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  ChevronLeft, ChevronRight, Eye, Filter, Plus, RefreshCw, Search, Users
} from "lucide-vue-next";
import { layDanhSachKhachHang } from "../../../services/khach-hang";

const router = useRouter();

const danhSach = ref([]);
const dangTai = ref(false);
const loiTrang = ref("");
const boLoc = ref({ keyword: "", trangThai: "" });

const dsTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Hoạt động", value: "1", color: "bg-emerald-50 text-emerald-600" },
  { label: "Khóa", value: "0", color: "bg-rose-50 text-rose-600" },
];

function mauTrangThai(trangThai: number) {
  return trangThai === 1 ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600";
}

function dinhDangNgay(ngay: string) {
  if (!ngay) return "—";
  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit", month: "2-digit", year: "numeric",
  }).format(new Date(ngay));
}

// Phân trang
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const tongSoTrang = computed(() => Math.ceil(danhSach.value.length / soPhanTuMotTrang.value) || 1);
const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
  return danhSach.value.slice(start, start + soPhanTuMotTrang.value);
});

watch(danhSach, () => { trangHienTai.value = 1; });
watch(soPhanTuMotTrang, () => { trangHienTai.value = 1; });

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const data = await layDanhSachKhachHang({
      keyword: boLoc.value.keyword || undefined,
      trangThai: boLoc.value.trangThai !== "" ? Number(boLoc.value.trangThai) : undefined,
    });
    danhSach.value = data;
  } catch (e) {
    loiTrang.value = e instanceof Error ? e.message : "Không thể tải danh sách khách hàng";
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  boLoc.value = { keyword: "", trangThai: "" };
}

function xemChiTiet(id: string) {
  router.push({ name: "admin-khach-hang-chi-tiet", params: { id } });
}

function themMoi() {
  router.push({ name: "admin-khach-hang-them" });
}

// Debounce search
let timer: ReturnType<typeof setTimeout>;
watch(() => boLoc.value, () => {
  clearTimeout(timer);
  timer = setTimeout(taiDanhSach, 300);
}, { deep: true });

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section>
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">Quản lý khách hàng</h1>
    </section>

    <!-- Bộ lọc -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
        </div>
      </div>

      <div class="grid gap-4 xl:grid-cols-[2fr_1fr_auto]">
        <label class="space-y-2">
          <span class="mb-1 text-[13px] font-semibold text-slate-500">Tìm kiếm</span>
          <div class="relative">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="boLoc.keyword"
              type="text"
              placeholder="Tìm theo tên đăng nhập, họ tên, email, SĐT..."
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            />
          </div>
        </label>

        <label class="space-y-2">
          <span class="mb-1 text-[13px] font-semibold text-slate-500">Trạng thái</span>
          <select v-model="boLoc.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
            <option v-for="tt in dsTrangThai" :key="tt.value" :value="tt.value">{{ tt.label }}</option>
          </select>
        </label>

        <div class="flex items-end gap-3">
          <button @click="lamMoiBoLoc" class="h-11 rounded-2xl bg-slate-400 px-5 text-sm font-semibold text-white transition hover:bg-slate-500">Làm mới</button>
          <button @click="themMoi" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-semibold text-white transition hover:bg-rose-600">
            <Plus class="h-4 w-4" /> Thêm khách hàng
          </button>
        </div>
      </div>
    </section>

    <!-- Danh sách -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-violet-50 text-violet-500">
          <Users class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Danh sách khách hàng</h2>
        </div>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <table class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Ảnh</th>
              <th class="bg-slate-100 px-4 py-3">Tên đăng nhập</th>
              <th class="bg-slate-100 px-4 py-3">Họ tên</th>
              <th class="bg-slate-100 px-4 py-3">Email</th>
              <th class="bg-slate-100 px-4 py-3">Số điện thoại</th>
              <th class="bg-slate-100 px-4 py-3">Ngày sinh</th>
              <th class="bg-slate-100 px-4 py-3">Ngày tạo</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="10" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu khách hàng...</td>
            </tr>
            <tr v-else-if="!danhSachPhanTrang.length">
              <td colspan="10" class="py-10 text-center text-sm text-slate-400">Không có khách hàng phù hợp.</td>
            </tr>
            <tr
              v-for="(kh, index) in danhSachPhanTrang"
              :key="kh.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
            >
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}</td>
              <td class="px-4 py-3">
                <img
                  :src="kh.hinhAnh || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(kh.hoTen) + '&background=f1f5f9&color=475569&size=64'"
                  :alt="kh.hoTen"
                  class="h-10 w-10 rounded-full object-cover ring-2 ring-slate-100"
                />
              </td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ kh.tenDangNhap }}</td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ kh.hoTen }}</td>
              <td class="px-4 py-3 text-slate-600">{{ kh.email || '—' }}</td>
              <td class="px-4 py-3 text-slate-600">{{ kh.sdt || '—' }}</td>
              <td class="px-4 py-3 text-slate-600">{{ kh.ngaySinh ? kh.ngaySinh : '—' }}</td>
              <td class="px-4 py-3 text-slate-600">{{ dinhDangNgay(kh.ngayTao) }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(kh.trangThai)">
                  {{ kh.tenTrangThai }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <button
                  type="button"
                  @click="xemChiTiet(kh.id)"
                  class="inline-flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-rose-50 hover:text-rose-500"
                  title="Xem chi tiết"
                >
                  <Eye class="h-4 w-4" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Phân trang -->
      <div class="mt-5 flex items-center justify-between gap-2 text-sm">
        <div class="flex items-center text-slate-500">
          <select v-model.number="soPhanTuMotTrang" class="rounded-xl border border-slate-200 bg-slate-50 px-2 py-1 outline-none focus:border-rose-300 transition">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
          </select>
        </div>
        <div class="flex items-center gap-2">
          <button @click="taiDanhSach" class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200" title="Làm mới">
            <RefreshCw class="h-4 w-4" />
          </button>
          <div class="flex items-center gap-1 ml-2">
            <button
              @click="trangHienTai = Math.max(1, trangHienTai - 1)"
              :disabled="trangHienTai === 1"
              class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200 disabled:opacity-40"
            >
              <ChevronLeft class="h-4 w-4" />
            </button>
            <button
              v-for="page in tongSoTrang"
              :key="page"
              @click="trangHienTai = page"
              class="flex h-8 w-8 items-center justify-center rounded-lg transition"
              :class="trangHienTai === page ? 'border border-violet-200 bg-violet-50 text-violet-600 font-bold' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
            >{{ page }}</button>
            <button
              @click="trangHienTai = Math.min(tongSoTrang, trangHienTai + 1)"
              :disabled="trangHienTai === tongSoTrang"
              class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200 disabled:opacity-40"
            >
              <ChevronRight class="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
