<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  Eye, FileSpreadsheet, Filter, Plus, RotateCcw, Search, Users
} from "lucide-vue-next";
import { doiTrangThaiKhachHang, layDanhSachKhachHang } from "../../../services/khach-hang";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import AdminQuickStatusAction from "../../../components/common/AdminQuickStatusAction.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { getDisplayErrorMessage } from "../../../utils/error-message";

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
const pageSizeOptions = [5, 10, 20];
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
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải danh sách khách hàng");
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

const dangDoiTrangThai = ref<string | null>(null);

async function toggleTrangThai(kh: any) {
  if (dangDoiTrangThai.value) return;
  const trangThaiMoi = kh.trangThai === 1 ? 0 : 1;
  const hanhDong = trangThaiMoi === 1 ? "kích hoạt" : "khóa";
  const tenKhachHang = kh.hoTen || kh.tenDangNhap || "khách hàng này";

  if (!window.confirm(`Bạn có chắc muốn ${hanhDong} ${tenKhachHang} không?`)) return;

  dangDoiTrangThai.value = kh.id;
  try {
    await doiTrangThaiKhachHang(kh.id, trangThaiMoi);
    kh.trangThai = trangThaiMoi;
    kh.tenTrangThai = trangThaiMoi === 1 ? "Hoạt động" : "Khóa";
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể cập nhật trạng thái khách hàng");
    setTimeout(() => (loiTrang.value = ""), 3000);
  } finally {
    dangDoiTrangThai.value = null;
  }
}

function themMoi() {
  router.push({ name: "admin-khach-hang-them" });
}

function xuatExcel() {
  if (!danhSach.value.length) {
    window.alert("Không có dữ liệu để xuất Excel.");
    return;
  }

  exportRowsToExcel({
    filename: "quan-ly-khach-hang",
    sheetName: "KhachHang",
    columns: [
      { label: "STT", value: (_, index) => index + 1 },
      { label: "Tên đăng nhập", key: "tenDangNhap" },
      { label: "Họ tên", key: "hoTen" },
      { label: "Email", value: (row) => row.email || "—" },
      { label: "Số điện thoại", value: (row) => row.sdt || "—" },
      { label: "Ngày sinh", value: (row) => row.ngaySinh || "—" },
      { label: "Ngày tạo", value: (row) => dinhDangNgay(row.ngayTao) },
      { label: "Trạng thái", value: (row) => row.tenTrangThai || "—" },
    ],
    rows: danhSach.value,
  });
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
      <h1 class="admin-page-title text-[30px]">Quản lý khách hàng</h1>
    </section>

    <!-- Bộ lọc -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="admin-section-title">Bộ lọc</h2>
        </div>
      </div>

      <div class="flex flex-col gap-4">
        <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
          <div class="min-w-0 flex-1">
            <div class="relative max-w-3xl">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                v-model="boLoc.keyword"
                type="text"
                placeholder="Tìm theo tên đăng nhập, họ tên, email, SĐT..."
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
              <Plus class="h-4 w-4" /> Thêm khách hàng
            </button>
          </div>
        </div>

        <div class="grid gap-4 md:max-w-sm">
          <label class="space-y-2">
            <span class="admin-filter-label mb-1">Trạng thái</span>
            <select v-model="boLoc.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
              <option v-for="tt in dsTrangThai" :key="tt.value" :value="tt.value">{{ tt.label }}</option>
            </select>
          </label>
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
          <h2 class="admin-section-title">Danh sách khách hàng</h2>
        </div>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <table class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-950">
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
              <td class="rounded-r-2xl px-4 py-3 align-top text-center">
                <div class="flex items-center justify-center gap-1">
                  <AdminQuickStatusAction
                    :loading="dangDoiTrangThai === kh.id"
                    :action-label="kh.trangThai === 1 ? 'Khóa tài khoản' : 'Kích hoạt tài khoản'"
                    :intent="kh.trangThai === 1 ? 'deactivate' : 'activate'"
                    @toggle="toggleTrangThai(kh)"
                  />
                  <button
                    type="button"
                    @click="xemChiTiet(kh.id)"
                    class="admin-table-action text-slate-600 hover:text-slate-900"
                    title="Xem chi tiết"
                  >
                    <Eye :size="14" />
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
