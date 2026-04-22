<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  Eye, FileSpreadsheet, Filter, Plus, RotateCcw, Search, Tag, PackageSearch, ToggleLeft, ToggleRight
} from "lucide-vue-next";
import {
  createDotGiamGia,
  createDotGiamGiaSanPham,
  deleteDotGiamGia,
  deleteDotGiamGiaSanPham,
  getDotGiamGiaDetail,
  getDotGiamGiaList,
  getDotGiamGiaSanPhamDetail,
  getDotGiamGiaSanPhamList, 
  updateDotGiamGia,
  updateDotGiamGiaSanPham
} from "../../../services/khuyen-mai";
import { layDanhSachGiay } from "../../../services/san-pham-api";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";

const router = useRouter();
const dangTai = ref(false);
const saving = ref(false);
const loiTrang = ref("");

// Modified BoLoc: tuNgay, denNgay, loaiGiam
const boLoc = ref({ keyword: "", trangThai: "", tuNgay: "", denNgay: "", loaiGiam: "" });

const danhSach = ref([]);
const tongSoTrang = ref(1);
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const totalItems = ref(0);


const dsTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Kích hoạt", value: "1" },
  { label: "Tắt", value: "0" },
];

const dotOptions = ref([]);

const dotSanPhamForm = reactive({
  id: null, dotGiamGiaId: "", giayId: "", trangThai: "1", ngayTao: ""
});

const productSearch = ref("");
const productHints = ref([]);
const selectedGiay = ref(null);
const loadingProductHints = ref(false);
const formErrors = reactive({});

function chonGiay(item) {
  dotSanPhamForm.giayId = String(item.id);
  selectedGiay.value = item;
  productSearch.value = item.ten;
  productHints.value = [];
}

function mauTrangThai(trangThai) {
  return Number(trangThai) === 1 ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600";
}

function statusText(value) {
  return Number(value) === 1 ? "Kích hoạt" : "Tắt";
}

function mauLoaiGiam(loai) {
  return Number(loai) === 1 ? "bg-blue-50 text-blue-600" : "bg-amber-50 text-amber-600";
}

function loaiGiamText(loai) {
  return Number(loai) === 1 ? "Phần trăm" : "Tiền mặt";
}

function toDisplayDate(value) {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("vi-VN");
}

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function normalizeDateInput(value) {
  if (!value) return "";
  return String(value).slice(0, 10);
}

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
}

// Watchers
watch(soPhanTuMotTrang, () => { trangHienTai.value = 1; taiDanhSach(); });
watch(trangHienTai, taiDanhSach);

watch(soPhanTuMotTrang, () => { trangHienTai.value = 1; taiDanhSach(); });
watch(trangHienTai, taiDanhSach);

let timer;
watch(() => boLoc.value, () => {
  clearTimeout(timer);
  timer = setTimeout(() => { trangHienTai.value = 1; taiDanhSach(); }, 300);
}, { deep: true });

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const data = await getDotGiamGiaList({
      keyword: boLoc.value.keyword || undefined,
      trangThai: boLoc.value.trangThai !== "" ? Number(boLoc.value.trangThai) : undefined,
      loaiGiam: boLoc.value.loaiGiam !== "" ? Number(boLoc.value.loaiGiam) : undefined,
      tuNgay: boLoc.value.tuNgay || undefined,
      denNgay: boLoc.value.denNgay || undefined,
      pageNo: trangHienTai.value - 1,
      pageSize: soPhanTuMotTrang.value
    });
    danhSach.value = data?.content || [];
    tongSoTrang.value = data?.totalPages || 1;
    totalItems.value = data?.totalElements || 0;
  } catch (e) {
    loiTrang.value = e.message || "Lỗi tải đợt giảm giá";
  } finally {
    dangTai.value = false;
  }
}

// removed taiDanhSachSp

function lamMoiBoLoc() {
  boLoc.value = { keyword: "", trangThai: "", tuNgay: "", denNgay: "", loaiGiam: "" };
}

async function xuatExcel() {
  try {
    const data = await getDotGiamGiaList({
      keyword: boLoc.value.keyword || undefined,
      trangThai: boLoc.value.trangThai !== "" ? Number(boLoc.value.trangThai) : undefined,
      loaiGiam: boLoc.value.loaiGiam !== "" ? Number(boLoc.value.loaiGiam) : undefined,
      tuNgay: boLoc.value.tuNgay || undefined,
      denNgay: boLoc.value.denNgay || undefined,
      pageNo: 0,
      pageSize: Math.max(totalItems.value || 0, soPhanTuMotTrang.value, 1),
    });

    const rows = data?.content || [];
    if (!rows.length) {
      window.alert("Không có dữ liệu để xuất Excel.");
      return;
    }

    exportRowsToExcel({
      filename: "quan-ly-dot-giam-gia",
      sheetName: "DotGiamGia",
      columns: [
        { label: "STT", value: (_, index) => index + 1 },
        { label: "Mã", key: "ma" },
        { label: "Tên", key: "ten" },
        { label: "Mô tả", value: (row) => row.moTa || "—" },
        { label: "Loại giảm", value: (row) => Number(row.loaiGiam) === 1 ? "Phần trăm" : "Tiền mặt" },
        { label: "Giá trị", key: "giaTriGiam" },
        { label: "Ngày bắt đầu", value: (row) => toDisplayDate(row.ngayBatDau) },
        { label: "Ngày kết thúc", value: (row) => toDisplayDate(row.ngayKetThuc) },
        { label: "Trạng thái", value: (row) => statusText(row.kichHoat) },
      ],
      rows,
    });
  } catch (error) {
    window.alert(error?.message || "Xuất Excel thất bại.");
  }
}


function openCreateModal() {
  router.push({ name: "admin-dot-giam-gia-them" });
}

async function openEditModal(item) {
  router.push({ name: "admin-dot-giam-gia-chi-tiet", params: { id: item.id } });
}

async function removeItem(item) {
  if (!confirm(`Bạn có chắc muốn xóa đợt giảm giá này?`)) return;
  try {
    await deleteDotGiamGia(item.id);
    alert("Xóa thành công.");
    taiDanhSach();
  } catch (error) {
    alert(error.message || "Xóa thất bại");
  }
}

// removed san-pham related modal logic from list view

async function nhanhDoiTrangThai(item) {
  try {
    const detail = await getDotGiamGiaDetail(item.id);
    detail.kichHoat = Number(detail.kichHoat) === 1 ? 0 : 1;
    detail.loaiGiam = Number(detail.loaiGiam);
    detail.giaTriGiam = Number(detail.giaTriGiam);
    detail.kichHoat = Number(detail.kichHoat);
    
    await updateDotGiamGia(item.id, detail);
    await taiDanhSach();
  } catch (error) {
    alert(error.message || "Đổi trạng thái thất bại");
  }
}

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section class="flex items-end justify-between">
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">Quản lý đợt giảm giá</h1>
    </section>

    <!-- Bộ lọc -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
          <p class="text-sm text-slate-400">Tra cứu nhanh dữ liệu.</p>
        </div>
      </div>

      <div class="flex flex-col gap-4">
        <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
          <div class="min-w-0 flex-1">
            <div class="relative max-w-3xl">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                v-model="boLoc.keyword"
                type="text"
                placeholder="Nhập mã hoặc tên đợt..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-3 xl:justify-end">
            <button @click="lamMoiBoLoc" class="inline-flex h-11 items-center gap-2 rounded-2xl border border-slate-200 bg-white px-5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 hover:text-slate-800">
              <RotateCcw class="h-4 w-4" /> Đặt lại bộ lọc
            </button>
            <button @click="xuatExcel" class="inline-flex h-11 items-center gap-2 rounded-2xl border border-slate-200 bg-white px-5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 hover:text-slate-800">
              <FileSpreadsheet class="h-4 w-4" /> Xuất Excel
            </button>
            <button @click="openCreateModal" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-semibold text-white transition hover:bg-rose-600">
              <Plus class="h-4 w-4" /> Thêm mới
            </button>
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2 xl:max-w-5xl xl:grid-cols-4">
          <label class="space-y-2">
            <span class="mb-1 text-[13px] font-semibold text-slate-500">Trạng thái</span>
            <select v-model="boLoc.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
              <option v-for="tt in dsTrangThai" :key="tt.value" :value="tt.value">{{ tt.label }}</option>
            </select>
          </label>
          
          <label class="space-y-2">
            <span class="mb-1 text-[13px] font-semibold text-slate-500">Loại giảm</span>
            <select v-model="boLoc.loaiGiam" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
              <option value="">Tất cả</option>
              <option value="1">Phần trăm</option>
            </select>
          </label>

          <div class="grid gap-4 md:col-span-2 md:grid-cols-2 xl:col-span-2">
            <label class="space-y-2">
              <span class="mb-1 text-[13px] font-semibold text-slate-500">Từ ngày</span>
              <input v-model="boLoc.tuNgay" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
            </label>

            <label class="space-y-2">
              <span class="mb-1 text-[13px] font-semibold text-slate-500">Đến ngày</span>
              <input v-model="boLoc.denNgay" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
            </label>
          </div>
        </div>
      </div>
    </section>

    <!-- Danh sách -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center justify-between">
        <h2 class="text-lg font-bold text-slate-800">Danh sách các đợt giảm giá</h2>
        <div>
          <p class="text-sm text-slate-400 font-medium">{{ totalItems }} bản ghi hiển thị.</p>
        </div>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <table class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã</th>
              <th class="bg-slate-100 px-4 py-3">Tên</th>
              <th class="bg-slate-100 px-4 py-3">Giá trị (%)</th>
              <th class="bg-slate-100 px-4 py-3">Ngày bắt đầu</th>
              <th class="bg-slate-100 px-4 py-3">Ngày kết thúc</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="!danhSach.length">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">Không có dữ liệu.</td>
            </tr>
            <tr v-for="(item, index) in danhSach" :key="item.id" class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100">
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}</td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ item.ma }}</td>
              <td class="px-4 py-3 font-semibold text-slate-800">
                <div>{{ item.ten }}</div>
                <div class="font-normal text-xs text-slate-500">{{ item.moTa || '—' }}</div>
              </td>
              <td class="px-4 py-3 text-slate-600 font-bold text-rose-500">
                {{ item.giaTriGiam }}%
              </td>
              <td class="px-4 py-3 text-slate-600">{{ toDisplayDate(item.ngayBatDau) }}</td>
              <td class="px-4 py-3 text-slate-600">{{ toDisplayDate(item.ngayKetThuc) }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(item.kichHoat)">
                  {{ statusText(item.kichHoat) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex justify-center items-center gap-3">
                  <button @click="openEditModal(item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500" title="Xem chi tiết">
                    <Eye class="h-5 w-5" />
                  </button>
                  <button @click="nhanhDoiTrangThai(item)" class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none" :class="Number(item.kichHoat) === 1 ? 'bg-rose-500' : 'bg-slate-200'" :title="Number(item.kichHoat) === 1 ? 'Tắt' : 'Kích hoạt'">
                    <span class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200" :class="Number(item.kichHoat) === 1 ? 'translate-x-5' : 'translate-x-0'"></span>
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
        :page-size-options="[5, 10, 20]"
        :total-items="totalItems"
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
