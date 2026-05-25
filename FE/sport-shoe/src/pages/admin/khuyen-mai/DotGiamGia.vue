<script setup>
import { onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { Eye, FileSpreadsheet, Filter, Plus, RotateCcw, Search } from "lucide-vue-next";
import { getDotGiamGiaList, updateDotGiamGia } from "../../../services/khuyen-mai";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import AdminQuickStatusAction from "../../../components/common/AdminQuickStatusAction.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { getDisplayErrorMessage } from "../../../utils/error-message";

const router = useRouter();

const dangTai = ref(false);
const loiTrang = ref("");
const boLoc = ref({ keyword: "", trangThai: "", tuNgay: "", denNgay: "" });
const danhSach = ref([]);
const tongSoTrang = ref(1);
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const totalItems = ref(0);

const dsTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Kích hoạt", value: "1" },
  { label: "Ngừng hoạt động", value: "0" },
  { label: "Hết hạn", value: "het_han" },
  { label: "Sắp diễn ra", value: "4" },
];

function isHetHan(ngayKetThuc) {
  if (!ngayKetThuc) return false;
  const homNay = new Date();
  homNay.setHours(0, 0, 0, 0);
  return new Date(ngayKetThuc) < homNay;
}

function mauTrangThai(trangThai, ngayKetThuc) {
  if (isHetHan(ngayKetThuc)) return "bg-rose-50 text-rose-600 ring-1 ring-rose-100";
  if (Number(trangThai) === 1) return "bg-emerald-50 text-emerald-600 ring-1 ring-emerald-100";
  if (Number(trangThai) === 2) return "bg-slate-100 text-slate-500 ring-1 ring-slate-200";
  if (Number(trangThai) === 4) return "bg-sky-50 text-sky-600 ring-1 ring-sky-100";
  return "bg-rose-50 text-rose-600 ring-1 ring-rose-100";
}

function statusText(value, ngayKetThuc) {
  if (isHetHan(ngayKetThuc)) return "Hết hạn";
  if (Number(value) === 1) return "Kích hoạt";
  if (Number(value) === 2) return "Hết hạn";
  if (Number(value) === 4) return "Sắp diễn ra";
  return "Ngừng hoạt động";
}

function toDisplayDate(value) {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("vi-VN");
}

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const isFilterHetHan = boLoc.value.trangThai === "het_han";
    const isFilterKichHoat = boLoc.value.trangThai === "1";

    const data = await getDotGiamGiaList({
      keyword: boLoc.value.keyword || undefined,
      trangThai: (!isFilterHetHan && boLoc.value.trangThai !== "")
        ? Number(boLoc.value.trangThai)
        : undefined,
      tuNgay: boLoc.value.tuNgay || undefined,
      denNgay: boLoc.value.denNgay || undefined,
      pageNo: trangHienTai.value - 1,
      pageSize: (isFilterHetHan || isFilterKichHoat) ? 1000 : soPhanTuMotTrang.value
    });

    let items = data?.content || [];

    if (isFilterHetHan) {
      items = items.filter(item => isHetHan(item.ngayKetThuc));
    } else if (isFilterKichHoat) {
      items = items.filter(item => !isHetHan(item.ngayKetThuc));
    }

    if (isFilterHetHan || isFilterKichHoat) {
      tongSoTrang.value = Math.max(1, Math.ceil(items.length / soPhanTuMotTrang.value));
      totalItems.value = items.length;
      const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
      danhSach.value = items.slice(start, start + soPhanTuMotTrang.value);
    } else {
      danhSach.value = items;
      tongSoTrang.value = data?.totalPages || 1;
      totalItems.value = data?.totalElements || 0;
    }
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể tải danh sách đợt giảm giá");
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  boLoc.value = { keyword: "", trangThai: "", tuNgay: "", denNgay: "" };
}

async function nhanhDoiTrangThai(item) {
  if (isHetHan(item.ngayKetThuc)) return;
  try {
    const nextStatus = Number(item.kichHoat) === 1 ? 0 : 1;
    await updateDotGiamGia(item.id, { ...item, kichHoat: nextStatus });
    await taiDanhSach();
  } catch (error) {
    window.alert(getDisplayErrorMessage(error, "Không thể cập nhật trạng thái đợt giảm giá"));
  }
}

async function xuatExcel() {
  try {
    const data = await getDotGiamGiaList({
      keyword: boLoc.value.keyword || undefined,
      trangThai: boLoc.value.trangThai !== "" ? Number(boLoc.value.trangThai) : undefined,
      tuNgay: boLoc.value.tuNgay || undefined,
      denNgay: boLoc.value.denNgay || undefined,
      pageNo: 0,
      pageSize: 1000,
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
        { label: "Giá trị giảm", value: (row) => row.giaTriGiam + "%" },
        { label: "Ngày bắt đầu", value: (row) => toDisplayDate(row.ngayBatDau) },
        { label: "Ngày kết thúc", value: (row) => toDisplayDate(row.ngayKetThuc) },
        { label: "Trạng thái", value: (row) => statusText(row.kichHoat) },
      ],
      rows,
    });
  } catch (error) {
    window.alert(getDisplayErrorMessage(error, "Không thể xuất Excel đợt giảm giá"));
  }
}

function openEditModal(item) {
  router.push({ name: "admin-dot-giam-gia-chi-tiet", params: { id: item.id } });
}

watch(soPhanTuMotTrang, () => {
  trangHienTai.value = 1;
  taiDanhSach();
});
watch(trangHienTai, taiDanhSach);

let timer;
watch(boLoc, () => {
  clearTimeout(timer);
  timer = setTimeout(() => {
    trangHienTai.value = 1;
    taiDanhSach();
  }, 300);
}, { deep: true });

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <section class="flex items-end justify-between">
      <h1 class="admin-page-title text-[30px]">Quản lý đợt giảm giá</h1>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="admin-section-title">Bộ lọc</h2>
          <p class="text-sm text-slate-400">Tra cứu nhanh dữ liệu.</p>
        </div>
      </div>

      <div class="flex flex-col gap-6">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
          <div class="space-y-2">
            <label class="admin-filter-label">Tìm kiếm</label>
            <div class="relative">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input v-model="boLoc.keyword" type="text" placeholder="Mã, tên..." class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
            </div>
          </div>

          <div class="space-y-2">
            <label class="admin-filter-label">Ngày bắt đầu</label>
            <input v-model="boLoc.tuNgay" type="date" class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          </div>

          <div class="space-y-2">
            <label class="admin-filter-label">Ngày kết thúc</label>
            <input v-model="boLoc.denNgay" type="date" class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          </div>

          <div class="space-y-2">
            <label class="admin-filter-label">Trạng thái</label>
            <select v-model="boLoc.trangThai" class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
              <option v-for="tt in dsTrangThai" :key="tt.value" :value="tt.value">{{ tt.label }}</option>
            </select>
          </div>
        </div>

        <div class="flex flex-wrap items-center justify-end gap-3">
          <button @click="lamMoiBoLoc" class="inline-flex h-11 items-center gap-2 rounded-2xl border border-rose-200 bg-white px-5 text-sm font-semibold text-rose-500 shadow-[0_10px_24px_rgba(244,63,94,0.08)] transition hover:border-rose-300 hover:bg-rose-50/70 hover:text-rose-600">
            <RotateCcw class="h-4 w-4" /> Đặt lại bộ lọc
          </button>
          <button @click="xuatExcel" class="inline-flex h-11 items-center gap-2 rounded-2xl border border-rose-200 bg-white px-5 text-sm font-semibold text-rose-500 shadow-[0_10px_24px_rgba(244,63,94,0.08)] transition hover:border-rose-300 hover:bg-rose-50/70 hover:text-rose-600">
            <FileSpreadsheet class="h-4 w-4" /> Xuất Excel
          </button>
          <button @click="router.push({ name: 'admin-dot-giam-gia-them' })" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-5 text-sm font-semibold text-white shadow-[0_14px_30px_rgba(239,68,68,0.28)] transition hover:-translate-y-0.5 hover:from-rose-600 hover:to-red-500 hover:shadow-[0_18px_34px_rgba(239,68,68,0.32)]">
            <Plus class="h-4 w-4" /> Tạo đợt giảm giá
          </button>
        </div>
      </div>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center justify-between">
        <h2 class="admin-section-title text-lg">Danh sách các đợt giảm giá</h2>
        <div>
          <p class="text-sm font-medium text-slate-400">{{ totalItems }} bản ghi hiển thị.</p>
        </div>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto admin-table-scroll w-full">
        <table class="w-full min-w-[900px] table-fixed border-separate border-spacing-y-2 text-xs">
          <colgroup>
            <col class="w-[5%]" />
            <col class="w-[15%]" />
            <col class="w-[25%]" />
            <col class="w-[10%]" />
            <col class="w-[13%]" />
            <col class="w-[13%]" />
            <col class="w-[140px]" />
            <col class="w-[14%]" />
          </colgroup>
          <thead>
            <tr class="text-left text-xs font-bold text-slate-950 [&>th]:whitespace-nowrap">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã</th>
              <th class="bg-slate-100 px-4 py-3">Tên</th>
              <th class="bg-slate-100 px-4 py-3">Giá trị</th>
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
            <tr v-for="(item, index) in danhSach" :key="item.id" class="bg-white text-slate-950 shadow-sm ring-1 ring-slate-100 transition hover:ring-slate-200">
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}</td>
              <td class="px-4 py-3 font-bold tracking-tight text-slate-900">{{ item.ma }}</td>
              <td class="px-4 py-3 align-top">
                <div class="w-full whitespace-normal break-words font-bold leading-6 text-slate-900">{{ item.ten }}</div>
              </td>
              <td class="px-4 py-3 font-bold text-slate-800">
                {{ item.giaTriGiam }}%
              </td>
              <td class="px-4 py-3 font-medium text-slate-600">{{ toDisplayDate(item.ngayBatDau) }}</td>
              <td class="px-4 py-3 font-medium text-slate-600">{{ toDisplayDate(item.ngayKetThuc) }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(item.kichHoat, item.ngayKetThuc)">
                  {{ statusText(item.kichHoat, item.ngayKetThuc) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex items-center justify-center gap-3">
                  <AdminQuickStatusAction
                    :loading="false"
                    :disabled="isHetHan(item.ngayKetThuc)"
                    :disabled-title="'Đợt giảm giá đã hết hạn, không thể thay đổi trạng thái'"
                    :action-label="Number(item.kichHoat) === 1 ? 'Ngừng hoạt động' : 'Kích hoạt'"
                    :confirm-message="Number(item.kichHoat) === 1 ? 'Bạn có chắc chắn muốn ngừng hoạt động đợt giảm giá này không?' : 'Bạn có chắc chắn muốn kích hoạt đợt giảm giá này không?'"
                    :intent="Number(item.kichHoat) === 1 ? 'deactivate' : 'activate'"
                    @toggle="nhanhDoiTrangThai(item)"
                  />
                  <button @click="openEditModal(item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-slate-700" title="Xem chi tiết">
                    <Eye class="h-5 w-5" />
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

