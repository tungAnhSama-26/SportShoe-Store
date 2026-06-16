<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  CalendarDays,
  ClipboardCheck,
  Eye,
  Filter,
  FileSpreadsheet,
  PackageCheck,
  RotateCcw,
  Search,
} from "lucide-vue-next";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import Badge from "../../../components/ui/Badge.vue";
import Button from "../../../components/ui/Button.vue";
import Card from "../../../components/ui/Card.vue";
import Table from "../../../components/ui/Table.vue";
import { layDanhSachTraHang } from "../../../services/tra-hang";
import { ketNoiHoaDonRealtime } from "../../../services/hoa-don-realtime";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { showError } from "../../../utils/alert";

const router = useRouter();
const danhSach = ref([]);
const dangTai = ref(false);
const loiTrang = ref("");
const trangHienTai = ref(1);
const soPhanTuMotTrang = ref(5);

const tuNgayPicker = ref(null);
const denNgayPicker = ref(null);

function layNgayHienTaiInput() {
  const homNay = new Date();
  const year = homNay.getFullYear();
  const month = String(homNay.getMonth() + 1).padStart(2, "0");
  const day = String(homNay.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function dinhDangNgayLoc(value) {
  if (!value) return "";
  const [year, month, day] = value.split("-");
  if (!year || !month || !day) return value;
  return `${day}/${month}/${year}`;
}

function chuyenNgayLocSangInput(value) {
  const normalized = value.trim();
  if (!normalized) return "";
  const match = normalized.match(/^(\d{1,2})\/(\d{1,2})\/(\d{2}|\d{4})$/);
  if (!match) return normalized;
  const [, day, month, year] = match;
  const fullYear = year.length === 2 ? `20${year}` : year;
  return `${fullYear}-${month.padStart(2, "0")}-${day.padStart(2, "0")}`;
}

function moLich(input) {
  if (!input) return;
  if (input.showPicker) {
    input.showPicker();
    return;
  }
  input.click();
  input.focus();
}

function taoBoLocMacDinh() {
  const homNay = layNgayHienTaiInput();
  return {
    keyword: "",
    tuNgay: homNay,
    denNgay: homNay,
  };
}

const boLoc = ref(taoBoLocMacDinh());

const tuNgayHienThi = computed({
  get: () => dinhDangNgayLoc(boLoc.value.tuNgay),
  set: (value) => {
    boLoc.value.tuNgay = chuyenNgayLocSangInput(value);
  },
});

const denNgayHienThi = computed({
  get: () => dinhDangNgayLoc(boLoc.value.denNgay),
  set: (value) => {
    boLoc.value.denNgay = chuyenNgayLocSangInput(value);
  },
});

const dsTrangThai = [
  { value: "", label: "Tất cả" },
  { value: 1, label: "Chờ duyệt" },
  { value: 2, label: "Chờ khách gửi hàng" },
  { value: 3, label: "Đang hoàn hàng" },
  { value: 4, label: "Đã nhận hàng" },
  { value: 5, label: "Đang kiểm tra" },
  { value: 6, label: "Chờ hoàn tiền" },
  { value: 7, label: "Đã hoàn tiền" },
  { value: 8, label: "Từ chối" },
  { value: 9, label: "Đã hủy" },
  { value: 10, label: "Hoàn hàng thất bại" },
];

const trangThaiDangChon = ref("");

const danhSachLocBoLoc = computed(() => {
  const keyword = boLoc.value.keyword.trim().toLowerCase();
  const tuNgay = boLoc.value.tuNgay ? new Date(boLoc.value.tuNgay + "T00:00:00") : null;
  const denNgay = boLoc.value.denNgay ? new Date(boLoc.value.denNgay + "T23:59:59") : null;

  return danhSach.value.filter((item) => {
    const dungTuKhoa =
      !keyword
      || [item.ma, item.maHoaDon, item.tenKhachHang, item.soDienThoaiKhachHang]
        .some((value) => String(value || "").toLowerCase().includes(keyword));
    
    const itemDate = new Date(item.ngayTao);
    const dungNgayBatDau = !tuNgay || itemDate >= tuNgay;
    const dungNgayKetThuc = !denNgay || itemDate <= denNgay;

    return dungTuKhoa && dungNgayBatDau && dungNgayKetThuc;
  });
});

const tongTheoTrangThai = computed(() =>
  dsTrangThai.map((item) => ({
    label: item.label,
    value: item.value,
    tong:
      item.value === ""
        ? danhSachLocBoLoc.value.length
        : danhSachLocBoLoc.value.filter((tr) => tr.trangThai === item.value).length,
  })),
);

const thongKe = computed(() => [
  {
    label: "Tổng phiếu",
    value: danhSachLocBoLoc.value.length,
    className: "bg-slate-50 text-slate-700",
  },
  {
    label: "Cần xử lý",
    value: danhSachLocBoLoc.value.filter((item) => [1, 4, 5].includes(item.trangThai)).length,
    className: "bg-amber-50 text-amber-700",
  },
  {
    label: "Chờ hoàn tiền",
    value: danhSachLocBoLoc.value.filter((item) => item.trangThai === 6).length,
    className: "bg-rose-50 text-rose-700",
  },
  {
    label: "Đã hoàn tiền",
    value: danhSachLocBoLoc.value.filter((item) => item.trangThai === 7).length,
    className: "bg-emerald-50 text-emerald-700",
  },
]);

const danhSachHienThi = computed(() => {
  const trangThai = trangThaiDangChon.value !== "" ? Number(trangThaiDangChon.value) : 0;
  return danhSachLocBoLoc.value.filter((item) => {
    return !trangThai || item.trangThai === trangThai;
  });
});

const tongSoTrang = computed(() =>
  Math.max(1, Math.ceil(danhSachHienThi.value.length / soPhanTuMotTrang.value)),
);

const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
  return danhSachHienThi.value.slice(start, start + soPhanTuMotTrang.value);
});

watch([danhSachHienThi, soPhanTuMotTrang], () => {
  trangHienTai.value = 1;
});

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    danhSach.value = await layDanhSachTraHang();
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(
      error,
      "Không thể tải danh sách phiếu trả hàng",
    );
  } finally {
    dangTai.value = false;
  }
}

function datLaiBoLoc() {
  boLoc.value = taoBoLocMacDinh();
  trangThaiDangChon.value = "";
}

function xemChiTiet(id) {
  router.push({ name: "admin-tra-hang-chi-tiet", params: { id } });
}

function xuatExcel() {
  if (!danhSachHienThi.value.length) {
    showError("Không có dữ liệu để xuất Excel.");
    return;
  }

  exportRowsToExcel({
    filename: "danh-sach-phieu-tra-hang",
    sheetName: "PhieuTraHang",
    columns: [
      { label: "STT", value: (_, index) => index + 1 },
      { label: "Mã phiếu", key: "ma" },
      { label: "Mã hóa đơn", key: "maHoaDon" },
      { label: "Khách hàng", value: (row) => row.tenKhachHang || "Khách vãng lai" },
      { label: "Số điện thoại", value: (row) => row.soDienThoaiKhachHang || "—" },
      { label: "Tiền dự kiến", value: (row) => dinhDangTien(row.tongTienDuKien) },
      { label: "Tiền thực tế", value: (row) => dinhDangTien(row.tongTienThucTe) },
      { label: "Ngày tạo", value: (row) => dinhDangNgay(row.ngayTao) },
      { label: "Trạng thái", value: (row) => row.tenTrangThai || "—" },
    ],
    rows: danhSachHienThi.value,
  });
}

function dinhDangTien(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(Number(value || 0));
}

function dinhDangNgay(value) {
  if (!value) return "Chưa cập nhật";
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(value));
}

function badgeVariant(trangThai) {
  if ([7].includes(trangThai)) return "success";
  if ([1, 2, 3, 4, 5, 6].includes(trangThai)) return "warning";
  if ([8, 9, 10].includes(trangThai)) return "danger";
  return "default";
}

let ngatKetNoiRealtime = null;
let realtimeRefreshTimeout = null;

onMounted(() => {
  taiDanhSach();
  ngatKetNoiRealtime = ketNoiHoaDonRealtime({
    authScope: "admin",
    onHoaDonThayDoi: (event) => {
      if (event?.loaiSuKien !== "TRA_HANG") return;
      if (realtimeRefreshTimeout) window.clearTimeout(realtimeRefreshTimeout);
      realtimeRefreshTimeout = window.setTimeout(taiDanhSach, 150);
    },
  });
});

onBeforeUnmount(() => {
  ngatKetNoiRealtime?.();
  if (realtimeRefreshTimeout) window.clearTimeout(realtimeRefreshTimeout);
});
</script>

<template>
  <div class="invoice-flat space-y-5 pb-8">
    <section class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
      <div
        v-for="item in thongKe"
        :key="item.label"
        class="flex min-h-24 items-center justify-between rounded-[6px] border border-slate-100 bg-white px-5 py-4 shadow-sm"
      >
        <div>
          <p class="text-sm font-medium text-slate-500">{{ item.label }}</p>
          <p class="mt-1 text-2xl font-bold text-slate-800">{{ item.value }}</p>
        </div>
        <div :class="['rounded-[6px] p-3', item.className]">
          <PackageCheck class="h-5 w-5" />
        </div>
      </div>
    </section>

    <Card>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-[6px] bg-rose-50 text-primary">
            <Filter class="h-5 w-5" />
          </div>
          <div>
            <h2 class="font-semibold text-slate-800">Bộ lọc phiếu trả hàng</h2>
            <p class="mt-0.5 text-xs text-slate-400">Tìm theo mã phiếu, hóa đơn hoặc khách hàng</p>
          </div>
        </div>
      </template>

      <div class="flex flex-wrap items-end gap-3">
        <label class="min-w-[200px] flex-1 space-y-2">
          <span class="text-xs font-semibold text-slate-500">Tìm kiếm</span>
          <div class="relative">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="boLoc.keyword"
              class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              placeholder="Mã phiếu / mã hóa đơn / khách hàng..."
            />
          </div>
        </label>

        <label class="min-w-[160px] flex-1 space-y-2">
          <span class="text-xs font-semibold text-slate-500">Ngày bắt đầu</span>
          <div class="relative">
            <input
              v-model="tuNgayHienThi"
              type="text"
              inputmode="numeric"
              placeholder="dd/mm/yyyy"
              class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 pr-11 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-[6px] text-slate-400 transition hover:bg-slate-100 hover:text-primary"
              @click="moLich(tuNgayPicker)"
            >
              <CalendarDays class="h-4 w-4" />
            </button>
            <input
              ref="tuNgayPicker"
              v-model="boLoc.tuNgay"
              type="date"
              aria-label="Chọn ngày bắt đầu"
              class="pointer-events-none absolute right-3 top-1/2 h-8 w-8 -translate-y-1/2 opacity-0"
              tabindex="-1"
            />
          </div>
        </label>

        <label class="min-w-[160px] flex-1 space-y-2">
          <span class="text-xs font-semibold text-slate-500">Ngày kết thúc</span>
          <div class="relative">
            <input
              v-model="denNgayHienThi"
              type="text"
              inputmode="numeric"
              placeholder="dd/mm/yyyy"
              class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 pr-11 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-[6px] text-slate-400 transition hover:bg-slate-100 hover:text-primary"
              @click="moLich(denNgayPicker)"
            >
              <CalendarDays class="h-4 w-4" />
            </button>
            <input
              ref="denNgayPicker"
              v-model="boLoc.denNgay"
              type="date"
              aria-label="Chọn ngày kết thúc"
              class="pointer-events-none absolute right-3 top-1/2 h-8 w-8 -translate-y-1/2 opacity-0"
              tabindex="-1"
            />
          </div>
        </label>

        <div class="flex shrink-0 items-center gap-3">
          <Button variant="soft" @click="datLaiBoLoc">
            <template #prefix><RotateCcw class="h-4 w-4" /></template>
            Đặt lại
          </Button>
          <Button variant="soft" @click="xuatExcel">
            <template #prefix><FileSpreadsheet class="h-4 w-4" /></template>
            Xuất Excel
          </Button>
        </div>
      </div>
    </Card>

    <Card>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-[6px] bg-rose-50 text-primary">
            <ClipboardCheck class="h-5 w-5" />
          </div>
          <div>
            <h2 class="font-semibold text-slate-800">Danh sách phiếu trả hàng</h2>
            <p class="mt-0.5 text-xs text-slate-400">
              {{ danhSachHienThi.length }} phiếu phù hợp
            </p>
          </div>
        </div>
      </template>

      <div v-if="loiTrang" class="mb-4 rounded-[6px] bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">
        {{ loiTrang }}
      </div>

      <div class="mb-4 flex flex-wrap gap-2">
        <button
          v-for="item in tongTheoTrangThai"
          :key="item.value"
          type="button"
          @click="trangThaiDangChon = item.value"
          class="inline-flex items-center whitespace-nowrap rounded-[6px] px-3.5 py-2 text-[13px] font-medium transition-all duration-200 active:scale-95"
          :class="trangThaiDangChon === item.value ? 'bg-rose-100 text-rose-600 shadow-sm scale-[1.03]' : 'bg-slate-50 text-slate-500 hover:bg-slate-100 hover:scale-[1.02]'"
        >
          {{ item.label }}
          <span class="ml-2 rounded-full bg-white px-2 py-0.5 text-[11px] text-slate-500 transition-all duration-200">{{ item.tong }}</span>
        </button>
      </div>

      <Transition name="tab-fade" mode="out-in">
        <div :key="trangThaiDangChon" class="admin-table-scroll">
          <Table>
            <template #header>
              <th class="whitespace-nowrap px-3 py-3 text-[13px]">STT</th>
              <th class="whitespace-nowrap px-3 py-3 text-[13px]">Mã phiếu</th>
              <th class="whitespace-nowrap px-3 py-3 text-[13px]">Mã hóa đơn</th>
              <th class="whitespace-nowrap px-3 py-3 text-[13px]">Khách hàng</th>
              <th class="whitespace-nowrap px-3 py-3 text-[13px]">SĐT khách hàng</th>
              <th class="whitespace-nowrap px-3 py-3 text-[13px]">Tiền dự kiến</th>
              <th class="whitespace-nowrap px-3 py-3 text-[13px]">Ngày tạo</th>
              <th class="whitespace-nowrap px-3 py-3 text-center text-[13px]">Trạng thái</th>
              <th class="whitespace-nowrap px-3 py-3 text-center text-[13px]">Hành động</th>
            </template>

            <template #body>
              <tr v-if="dangTai">
                <td colspan="9" class="py-10 text-center text-sm text-slate-400">Đang tải danh sách trả hàng...</td>
              </tr>
              <tr v-else-if="!danhSachPhanTrang.length">
                <td colspan="9" class="px-4 py-16 text-center text-sm text-slate-400">
                  Không có phiếu trả hàng phù hợp.
                </td>
              </tr>
              <tr
                v-else
                v-for="(item, index) in danhSachPhanTrang"
                :key="item.id"
                class="bg-white text-[13px] text-slate-700 shadow-sm ring-1 ring-slate-100 transition-colors hover:bg-slate-50 [&>td]:whitespace-nowrap"
              >
                <td class="rounded-l-2xl px-3 py-3.5 font-semibold">
                  {{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}
                </td>
                <td class="px-3 py-3.5 font-semibold text-slate-800">{{ item.ma }}</td>
                <td class="px-3 py-3.5">{{ item.maHoaDon }}</td>
                <td class="px-3 py-3.5">{{ item.tenKhachHang || "Khách vãng lai" }}</td>
                <td class="px-3 py-3.5">{{ item.soDienThoaiKhachHang || "Không có" }}</td>
                <td class="px-3 py-3.5 font-semibold text-slate-800">
                  {{ dinhDangTien(item.tongTienDuKien) }}
                </td>
                <td class="px-3 py-3.5">{{ dinhDangNgay(item.ngayTao) }}</td>
                <td class="px-3 py-3.5 text-center">
                  <Badge :variant="badgeVariant(item.trangThai)">{{ item.tenTrangThai }}</Badge>
                </td>
                <td class="rounded-r-2xl px-3 py-3.5 text-center">
                  <button
                    type="button"
                    class="inline-flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-primary/10 hover:text-primary"
                    title="Xem chi tiết"
                    @click="xemChiTiet(item.id)"
                  >
                    <Eye class="h-4 w-4" />
                  </button>
                </td>
              </tr>
            </template>
          </Table>
        </div>
      </Transition>

      <template #footer>
        <AdminTableFooter
          v-model:current-page="trangHienTai"
          v-model:page-size="soPhanTuMotTrang"
          :total-items="danhSachHienThi.length"
          :total-pages="tongSoTrang"
          :page-size-options="[5, 10, 20]"
          compact
          show-refresh
          @refresh="taiDanhSach"
        />
      </template>
    </Card>
  </div>
</template>

<style scoped>
.invoice-flat :deep([class*="rounded-"]:not(.rounded-full)) {
  border-radius: 6px !important;
}
.tab-fade-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.tab-fade-leave-active {
  transition: opacity 0.15s ease;
}
.tab-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.tab-fade-leave-to {
  opacity: 0;
}
</style>
