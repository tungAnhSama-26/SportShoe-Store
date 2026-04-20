<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  ChevronLeft, ChevronRight, Eye, FileSpreadsheet, Filter, Plus, RefreshCw, RotateCcw, Search, ToggleLeft, ToggleRight, Users
} from "lucide-vue-next";
import { doiTrangThaiNhanVien, layDanhSachNhanVien } from "../../../services/nhan-vien";
import { useAdminSession } from "../../../composable/useAdminSession";
import { exportRowsToExcel } from "../../../utils/export-excel";

const router = useRouter();
const { adminSession } = useAdminSession();

const danhSach = ref([]);
const dangTai = ref(false);
const loiTrang = ref("");
const boLoc = ref({ keyword: "", vaiTro: "", trangThai: "" });

const dsVaiTro = [
  { label: "Tất cả vai trò", value: "" },
  { label: "Admin", value: "1" },
  { label: "Bán hàng", value: "2" },
  { label: "Kho", value: "3" },
];

const dsTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Hoạt động", value: "1", color: "bg-emerald-50 text-emerald-600" },
  { label: "Khóa", value: "0", color: "bg-rose-50 text-rose-600" },
];

const mauVaiTro = {
  Admin: "bg-violet-50 text-violet-700",
  "Bán hàng": "bg-sky-50 text-sky-700",
  Kho: "bg-amber-50 text-amber-700",
};

function mauTrangThai(trangThai: number) {
  return trangThai === 1 ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600";
}

function dinhDangNgay(ngay: string) {
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
    danhSach.value = await layDanhSachNhanVien({
      keyword: boLoc.value.keyword || undefined,
      vaiTro: boLoc.value.vaiTro ? Number(boLoc.value.vaiTro) : undefined,
      trangThai: boLoc.value.trangThai !== "" ? Number(boLoc.value.trangThai) : undefined,
    });
  } catch (e) {
    loiTrang.value = e instanceof Error ? e.message : "Không thể tải danh sách nhân viên";
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  boLoc.value = { keyword: "", vaiTro: "", trangThai: "" };
}

function xemChiTiet(id: string) {
  router.push({ name: "admin-nhan-vien-chi-tiet", params: { id } });
}

function themMoi() {
  router.push({ name: "admin-nhan-vien-them" });
}

// Debounce search
let timer: ReturnType<typeof setTimeout>;
watch(() => boLoc.value, () => {
  clearTimeout(timer);
  timer = setTimeout(taiDanhSach, 300);
}, { deep: true });

async function capNhatTrangThai(nv: any) {
  // Kiểm tra quyền: Chỉ Admin mới được đổi trạng thái
  if (adminSession.value.vaiTro !== "Quản trị viên") {
    window.alert("Chỉ có Quản trị viên mới có quyền thực hiện hành động này.");
    return;
  }

  // Không cho phép khóa tài khoản Admin khác
  if (nv.tenVaiTro === "Admin") {
    window.alert("Không thể thay đổi trạng thái của tài khoản Quản trị viên.");
    return;
  }

  const message = nv.trangThai === 1
    ? `Bạn có chắc muốn cho nhân viên "${nv.hoTen}" nghỉ làm?`
    : `Bạn có chắc muốn kích hoạt lại nhân viên "${nv.hoTen}"?`;

  if (!window.confirm(message)) return;

  try {
    await doiTrangThaiNhanVien(nv.id, nv.trangThai === 1 ? 0 : 1);
    await taiDanhSach();
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "Không thể cập nhật trạng thái");
  }
}

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section>
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">Quản lý nhân viên</h1>
    </section>

    <!-- Bộ lọc -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
          <p class="text-sm text-slate-400">Lọc theo từ khóa, vai trò và trạng thái.</p>
        </div>
      </div>

      <div class="grid gap-4 xl:grid-cols-[1.5fr_1fr_1fr_auto]">
        <label class="space-y-2">
          <span class="mb-1 text-[13px] font-semibold text-slate-500">Tìm kiếm</span>
          <div class="relative">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="boLoc.keyword"
              type="text"
              placeholder="Tìm theo mã, họ tên, email, SĐT..."
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            />
          </div>
        </label>

        <label class="space-y-2">
          <span class="mb-1 text-[13px] font-semibold text-slate-500">Vai trò</span>
          <select v-model="boLoc.vaiTro" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
            <option v-for="vt in dsVaiTro" :key="vt.value" :value="vt.value">{{ vt.label }}</option>
          </select>
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
            <Plus class="h-4 w-4" /> Thêm nhân viên
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
          <h2 class="text-base font-bold text-slate-800">Danh sách nhân viên</h2>
          <p class="text-sm text-slate-400">{{ danhSach.length }} nhân viên trong phạm vi bộ lọc hiện tại.</p>
        </div>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <table class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Ảnh</th>
              <th class="bg-slate-100 px-4 py-3">Mã NV</th>
              <th class="bg-slate-100 px-4 py-3">Họ tên</th>
              <th class="bg-slate-100 px-4 py-3">Email</th>
              <th class="bg-slate-100 px-4 py-3">Số điện thoại</th>
              <th class="bg-slate-100 px-4 py-3">Vai trò</th>
              <th class="bg-slate-100 px-4 py-3">Ngày tạo</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="10" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu nhân viên...</td>
            </tr>
            <tr v-else-if="!danhSachPhanTrang.length">
              <td colspan="10" class="py-10 text-center text-sm text-slate-400">Không có nhân viên phù hợp.</td>
            </tr>
            <tr
              v-for="(nv, index) in danhSachPhanTrang"
              :key="nv.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
            >
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}</td>
              <td class="px-4 py-3">
                <img
                  :src="nv.hinhAnh || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(nv.hoTen) + '&background=f1f5f9&color=475569&size=64'"
                  :alt="nv.hoTen"
                  class="h-10 w-10 rounded-full object-cover ring-2 ring-slate-100"
                />
              </td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ nv.ma }}</td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ nv.hoTen }}</td>
              <td class="px-4 py-3 text-slate-600">{{ nv.email }}</td>
              <td class="px-4 py-3 text-slate-600">{{ nv.sdt || '—' }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauVaiTro[nv.tenVaiTro] || 'bg-slate-50 text-slate-600'">
                  {{ nv.tenVaiTro }}
                </span>
              </td>
              <td class="px-4 py-3 text-slate-600">{{ dinhDangNgay(nv.ngayTao) }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(nv.trangThai)">
                  {{ nv.tenTrangThai }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex items-center justify-center gap-2">
                  <button
                    type="button"
                    @click="xemChiTiet(nv.id)"
                    class="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-slate-100 text-slate-600 transition hover:bg-rose-50 hover:text-rose-500"
                    title="Xem chi tiết"
                  >
                    <Eye class="h-4 w-4" />
                  </button>
                  <button
                    v-if="adminSession.vaiTro === 'Quản trị viên'"
                    :disabled="nv.tenVaiTro === 'Admin'"
                    type="button"
                    @click="capNhatTrangThai(nv)"
                    class="inline-flex h-9 w-9 items-center justify-center rounded-xl transition"
                    :class="[
                      nv.trangThai === 1 ? 'bg-emerald-50 text-emerald-600 hover:bg-emerald-100' : 'bg-rose-50 text-rose-600 hover:bg-rose-100',
                      nv.tenVaiTro === 'Admin' ? 'opacity-40 cursor-not-allowed' : ''
                    ]"
                    :title="nv.tenVaiTro === 'Admin' ? 'Không thể đổi trạng thái Admin' : (nv.trangThai === 1 ? 'Cho nghỉ làm' : 'Kích hoạt nhân viên')"
                  >
                    <component :is="nv.trangThai === 1 ? ToggleRight : ToggleLeft" class="h-5 w-5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Phân trang -->
      <div class="mt-5 flex items-center justify-between gap-2 text-sm">
        <div class="flex items-center gap-2 text-slate-500">
          Xem
          <select v-model.number="soPhanTuMotTrang" class="rounded-xl border border-slate-200 bg-slate-50 px-2 py-1 outline-none focus:border-rose-300 transition">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          nhân viên
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
