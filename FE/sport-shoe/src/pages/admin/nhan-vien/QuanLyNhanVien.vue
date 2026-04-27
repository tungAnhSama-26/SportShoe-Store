<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  doiTrangThaiNhanVien,
  layDanhSachNhanVien,
} from "../../../services/nhan-vien";
import { useAdminSession } from "../../../composable/useAdminSession";
import { exportRowsToExcel } from "../../../utils/export-excel";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import { getDisplayErrorMessage } from "../../../utils/error-message";

import {
  Eye,
  FileSpreadsheet,
  Filter,
  Plus,
  RotateCcw,
  Search,
  Users,
  ToggleLeft,
  ToggleRight,
} from "lucide-vue-next";

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
  return trangThai === 1
    ? "bg-emerald-50 text-emerald-600"
    : "bg-rose-50 text-rose-600";
}

function dinhDangNgay(ngay: string) {
  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(ngay));
}

// Phân trang
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const pageSizeOptions = [5, 10, 20, 50];
const tongSoTrang = computed(
  () => Math.ceil(danhSach.value.length / soPhanTuMotTrang.value) || 1,
);
const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
  return danhSach.value.slice(start, start + soPhanTuMotTrang.value);
});

watch(danhSach, () => {
  trangHienTai.value = 1;
});
watch(soPhanTuMotTrang, () => {
  trangHienTai.value = 1;
});

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    danhSach.value = await layDanhSachNhanVien({
      keyword: boLoc.value.keyword || undefined,
      vaiTro: boLoc.value.vaiTro ? Number(boLoc.value.vaiTro) : undefined,
      trangThai:
        boLoc.value.trangThai !== ""
          ? Number(boLoc.value.trangThai)
          : undefined,
    });
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải danh sách nhân viên");
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

function xuatExcel() {
  if (!danhSach.value.length) {
    window.alert("Không có dữ liệu để xuất Excel.");
    return;
  }

  exportRowsToExcel({
    filename: "quan-ly-nhan-vien",
    sheetName: "NhanVien",
    columns: [
      { label: "STT", value: (_, index) => index + 1 },
      { label: "Mã NV", key: "ma" },
      { label: "Họ tên", key: "hoTen" },
      { label: "Email", key: "email" },
      { label: "Số điện thoại", value: (row) => row.sdt || "—" },
      { label: "Vai trò", value: (row) => row.tenVaiTro || "—" },
      { label: "Ngày tạo", value: (row) => dinhDangNgay(row.ngayTao) },
      { label: "Trạng thái", value: (row) => row.tenTrangThai || "—" },
    ],
    rows: danhSach.value,
  });
}

// Debounce search
let timer: ReturnType<typeof setTimeout>;
watch(
  () => boLoc.value,
  () => {
    clearTimeout(timer);
    timer = setTimeout(taiDanhSach, 300);
  },
  { deep: true },
);

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

  const message =
    nv.trangThai === 1
      ? `Bạn có chắc muốn cho nhân viên "${nv.hoTen}" nghỉ làm?`
      : `Bạn có chắc muốn kích hoạt lại nhân viên "${nv.hoTen}"?`;

  if (!window.confirm(message)) return;

  try {
    await doiTrangThaiNhanVien(nv.id, nv.trangThai === 1 ? 0 : 1);
    await taiDanhSach();
  } catch (e) {
    window.alert(
      getDisplayErrorMessage(e, "Không thể cập nhật trạng thái nhân viên"),
    );
  }
}

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section>
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">
        Quản lý nhân viên
      </h1>
    </section>

    <!-- Bộ lọc -->
    <section
      class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm"
    >
      <div class="mb-5 flex items-center gap-3">
        <div
          class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600"
        >
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
        </div>
      </div>

      <div class="flex flex-col gap-4">
        <div
          class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between"
        >
          <div class="min-w-0 flex-1">
            <div class="relative max-w-3xl">
              <Search
                class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
              />
              <input
                v-model="boLoc.keyword"
                type="text"
                placeholder="Tìm theo mã, họ tên, email, SĐT..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-3 xl:justify-end">
            <button @click="lamMoiBoLoc" class="admin-btn-soft">
              <RotateCcw class="h-4 w-4" /> Đặt lại bộ lọc
            </button>
            <button @click="xuatExcel" class="admin-btn-soft">
              <FileSpreadsheet class="h-4 w-4" /> Xuất Excel
            </button>
            <button @click="themMoi" class="admin-btn-primary">
              <Plus class="h-4 w-4" /> Thêm nhân viên
            </button>
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2 xl:max-w-2xl">
          <label class="space-y-2">
            <span class="mb-1 text-[13px] font-semibold text-slate-500"
              >Vai trò</span
            >
            <select
              v-model="boLoc.vaiTro"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            >
              <option v-for="vt in dsVaiTro" :key="vt.value" :value="vt.value">
                {{ vt.label }}
              </option>
            </select>
          </label>

          <label class="space-y-2">
            <span class="mb-1 text-[13px] font-semibold text-slate-500"
              >Trạng thái</span
            >
            <select
              v-model="boLoc.trangThai"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            >
              <option
                v-for="tt in dsTrangThai"
                :key="tt.value"
                :value="tt.value"
              >
                {{ tt.label }}
              </option>
            </select>
          </label>
        </div>
      </div>
    </section>

    <!-- Danh sách -->
    <section
      class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm"
    >
      <div class="mb-5 flex items-center gap-3">
        <div
          class="flex h-11 w-11 items-center justify-center rounded-2xl bg-violet-50 text-violet-500"
        >
          <Users class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">
            Danh sách nhân viên
          </h2>
        </div>
      </div>

      <div
        v-if="loiTrang"
        class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600"
      >
        {{ loiTrang }}
      </div>

      <div class="overflow-x-auto">
        <table
          class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm"
        >
          <thead>
            <tr class="text-left text-sm font-bold text-slate-950">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Ảnh</th>
              <th class="bg-slate-100 px-4 py-3">Mã NV</th>
              <th class="bg-slate-100 px-4 py-3">Họ tên</th>
              <th class="bg-slate-100 px-4 py-3">Email</th>
              <th class="bg-slate-100 px-4 py-3">Số điện thoại</th>
              <th class="bg-slate-100 px-4 py-3">Vai trò</th>
              <th class="bg-slate-100 px-4 py-3">Ngày tạo</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">
                Hành động
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="10" class="py-10 text-center text-sm text-slate-400">
                Đang tải dữ liệu nhân viên...
              </td>
            </tr>
            <tr v-else-if="!danhSachPhanTrang.length">
              <td colspan="10" class="py-10 text-center text-sm text-slate-400">
                Không có nhân viên phù hợp.
              </td>
            </tr>
            <tr
              v-for="(nv, index) in danhSachPhanTrang"
              :key="nv.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
            >
              <td class="rounded-l-2xl px-4 py-3 font-semibold">
                {{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}
              </td>
              <td class="px-4 py-3">
                <img
                  :src="
                    nv.hinhAnh ||
                    'https://ui-avatars.com/api/?name=' +
                      encodeURIComponent(nv.hoTen) +
                      '&background=f1f5f9&color=475569&size=64'
                  "
                  :alt="nv.hoTen"
                  class="h-10 w-10 rounded-full object-cover ring-2 ring-slate-100"
                />
              </td>
              <td class="px-4 py-3 font-semibold text-slate-800">
                {{ nv.ma }}
              </td>
              <td class="px-4 py-3 font-semibold text-slate-800">
                {{ nv.hoTen }}
              </td>
              <td class="px-4 py-3 text-slate-600">{{ nv.email }}</td>
              <td class="px-4 py-3 text-slate-600">{{ nv.sdt || "—" }}</td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex rounded-full px-3 py-1 text-xs font-semibold"
                  :class="
                    mauVaiTro[nv.tenVaiTro] || 'bg-slate-50 text-slate-600'
                  "
                >
                  {{ nv.tenVaiTro }}
                </span>
              </td>
              <td class="px-4 py-3 text-slate-600">
                {{ dinhDangNgay(nv.ngayTao) }}
              </td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex rounded-full px-3 py-1 text-xs font-semibold"
                  :class="mauTrangThai(nv.trangThai)"
                >
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
                      nv.trangThai === 1
                        ? 'bg-emerald-50 text-emerald-600 hover:bg-emerald-100'
                        : 'bg-rose-50 text-rose-600 hover:bg-rose-100',
                      nv.tenVaiTro === 'Admin'
                        ? 'opacity-40 cursor-not-allowed'
                        : '',
                    ]"
                    :title="
                      nv.tenVaiTro === 'Admin'
                        ? 'Không thể đổi trạng thái Admin'
                        : nv.trangThai === 1
                          ? 'Cho nghỉ làm'
                          : 'Kích hoạt nhân viên'
                    "
                  >
                    <component
                      :is="nv.trangThai === 1 ? ToggleRight : ToggleLeft"
                      class="h-5 w-5"
                    />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminTableFooter
        :current-page="trangHienTai"
        :page-size="soPhanTuMotTrang"
        :page-size-options="pageSizeOptions"
        :total-items="danhSach.length"
        :total-pages="tongSoTrang"
        compact
        show-refresh
        @refresh="taiDanhSach"
        @update:current-page="trangHienTai = $event"
        @update:page-size="soPhanTuMotTrang = $event"
      />
    </section>
  </div>
</template>
