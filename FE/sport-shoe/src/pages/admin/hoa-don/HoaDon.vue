<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  CalendarDays,
  Download,
  Eye,
  FileSpreadsheet,
  FileText,
  Filter,

  RotateCcw,
  Search,
} from "lucide-vue-next";
import { layChiTietHoaDon, layDanhSachHoaDon } from "../../../services/hoa-don";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { printInvoiceToPdf } from "../../../utils/invoice-pdf";
import { getDisplayErrorMessage } from "../../../utils/error-message";

type TrangThaiLoc =
  | "Tất cả"
  | "Chờ xác nhận"
  | "Chờ giao hàng"
  | "Đang vận chuyển"
  | "Đã giao hàng"
  | "Hoàn thành"
  | "Hủy"
  | "Yêu cầu hủy"
  | "Cần hoàn tiền";

type HoaDonItem = {
  id: number;
  maHoaDon: string;
  tenNhanVien: string;
  tenKhachHang: string;
  tongTien: number;
  ngayTao: string;
  loaiDon: string;
  trangThai: string;
};

const router = useRouter();
const danhSach = ref<HoaDonItem[]>([]);
const dangTai = ref(false);
const dangXuatPdfId = ref<number | null>(null);
const loiTrang = ref("");
const trangThaiDangChon = ref<TrangThaiLoc>("Tất cả");
const dsTrangThai: TrangThaiLoc[] = [
  "Tất cả",
  "Chờ xác nhận",
  "Chờ giao hàng",
  "Đang vận chuyển",
  "Đã giao hàng",
  "Hoàn thành",
  "Hủy",
  "Yêu cầu hủy",
  "Cần hoàn tiền",
];
const boLoc = ref(taoBoLocMacDinh());
const tuNgayPicker = ref<HTMLInputElement | null>(null);
const denNgayPicker = ref<HTMLInputElement | null>(null);

const mauTrangThai: Record<string, string> = {
  "Chờ xác nhận": "bg-amber-50 text-amber-600",
  "Chờ giao hàng": "bg-blue-50 text-blue-600",
  "Đang vận chuyển": "bg-violet-50 text-violet-600",
  "Đã giao hàng": "bg-cyan-50 text-cyan-600",
  "Hoàn thành": "bg-emerald-50 text-emerald-600",
  Hủy: "bg-stone-100 text-stone-600",
  "Yêu cầu hủy": "bg-[#B82220]/5 text-[#B82220]",
  "Cần hoàn tiền": "bg-[#B82220]/5 text-[#B82220]",
};

function dinhDangTien(value: number) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value || 0);
}

function dinhDangNgay(ngay: string) {
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(ngay));
}

function layNgayHienTaiInput() {
  const homNay = new Date();
  const year = homNay.getFullYear();
  const month = String(homNay.getMonth() + 1).padStart(2, "0");
  const day = String(homNay.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function dinhDangNgayLoc(value: string) {
  if (!value) return "";
  const [year, month, day] = value.split("-");
  if (!year || !month || !day) return value;
  return `${day}/${month}/${year}`;
}

function chuyenNgayLocSangInput(value: string) {
  const normalized = value.trim();
  if (!normalized) return "";
  const match = normalized.match(/^(\d{1,2})\/(\d{1,2})\/(\d{2}|\d{4})$/);
  if (!match) return normalized;
  const [, day, month, year] = match;
  const fullYear = year.length === 2 ? `20${year}` : year;
  return `${fullYear}-${month.padStart(2, "0")}-${day.padStart(2, "0")}`;
}

function moLich(input: HTMLInputElement | null) {
  if (!input) return;
  const picker = input as HTMLInputElement & { showPicker?: () => void };
  if (picker.showPicker) {
    picker.showPicker();
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
    loaiDon: "",
  };
}

const tuNgayHienThi = computed({
  get: () => dinhDangNgayLoc(boLoc.value.tuNgay),
  set: (value: string) => {
    boLoc.value.tuNgay = chuyenNgayLocSangInput(value);
  },
});

const denNgayHienThi = computed({
  get: () => dinhDangNgayLoc(boLoc.value.denNgay),
  set: (value: string) => {
    boLoc.value.denNgay = chuyenNgayLocSangInput(value);
  },
});

const tongTheoTrangThai = computed(() =>
  dsTrangThai.map((trangThai) => ({
    ten: trangThai,
    tong:
      trangThai === "Tất cả"
        ? danhSach.value.length
        : danhSach.value.filter((hoaDon) => hoaDon.trangThai === trangThai).length,
  })),
);

const danhSachHienThi = computed(() => {
  if (trangThaiDangChon.value === "Tất cả") return danhSach.value;
  return danhSach.value.filter((hoaDon) => hoaDon.trangThai === trangThaiDangChon.value);
});

const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const pageSizeOptions = [5, 10, 20, 50];

const tongSoTrang = computed(
  () => Math.ceil(danhSachHienThi.value.length / soPhanTuMotTrang.value) || 1,
);

const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
  return danhSachHienThi.value.slice(start, start + soPhanTuMotTrang.value);
});

watch(danhSachHienThi, () => {
  trangHienTai.value = 1;
});

watch(soPhanTuMotTrang, () => {
  trangHienTai.value = 1;
});

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    danhSach.value = await layDanhSachHoaDon({
      keyword: boLoc.value.keyword,
      tuNgay: boLoc.value.tuNgay,
      denNgay: boLoc.value.denNgay,
      loaiDon: boLoc.value.loaiDon,
      // Không gửi trạng thái lên backend để số lượng trạng thái hiển thị đúng.
      trangThai: undefined,
    });
    } catch (error) {
      loiTrang.value = getDisplayErrorMessage(error, "Không thể tải danh sách hóa đơn");
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  boLoc.value = taoBoLocMacDinh();
  trangThaiDangChon.value = "Tất cả";
}

function xemChiTiet(id: number) {
  router.push({ name: "admin-hoa-don-chi-tiet", params: { id } });
}

function xuatExcel() {
  if (!danhSachHienThi.value.length) {
    window.alert("Không có dữ liệu để xuất Excel.");
    return;
  }

  exportRowsToExcel({
    filename: "danh-sach-hoa-don",
    sheetName: "HoaDon",
    columns: [
      { label: "STT", value: (_, index) => index + 1 },
      { label: "Mã hóa đơn", key: "maHoaDon" },
      { label: "Nhân viên", value: (row) => row.tenNhanVien || "—" },
      { label: "Khách hàng", value: (row) => row.tenKhachHang || "—" },
      { label: "Tổng tiền", value: (row) => dinhDangTien(row.tongTien) },
      { label: "Ngày tạo", value: (row) => dinhDangNgay(row.ngayTao) },
      { label: "Loại đơn", value: (row) => row.loaiDon || "—" },
      { label: "Trạng thái", value: (row) => row.trangThai || "—" },
    ],
    rows: danhSachHienThi.value,
  });
}

async function xuatHoaDonPdf(id: number) {
  if (dangXuatPdfId.value) return;

  // Mở cửa sổ trống ngay lập tức để tránh bị trình duyệt chặn (Browser Popup Blocker)
  const popup = window.open("", "_blank", "width=1100,height=800");
  if (popup) {
    popup.document.write("<html><body style='font-family:sans-serif; display:flex; align-items:center; justify-content:center; height:100vh;'><h3>Đang chuẩn bị hóa đơn...</h3></body></html>");
  }

  dangXuatPdfId.value = id;
  try {
    const chiTiet = await layChiTietHoaDon(id);
    printInvoiceToPdf({
      invoice: chiTiet,
      filename: `hoa-don-${chiTiet?.maHoaDon || id}`,
      formatCurrency: dinhDangTien,
      formatDate: dinhDangNgay,
      targetWindow: popup,
    });
    } catch (error) {
      if (popup) popup.close();
      window.alert(getDisplayErrorMessage(error, "Không thể xuất PDF hóa đơn"));
    } finally {
    dangXuatPdfId.value = null;
  }
}

let boLocTimeout: ReturnType<typeof setTimeout>;
watch(
  () => boLoc.value,
  () => {
    clearTimeout(boLocTimeout);
    boLocTimeout = setTimeout(() => {
      taiDanhSach();
    }, 300);
  },
  { deep: true },
);

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <section>
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">Quản lý hóa đơn</h1>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
        </div>
      </div>

      <div class="flex flex-wrap items-end gap-3">
        <label class="min-w-[160px] flex-1 space-y-2">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Tìm kiếm</span>
          <div class="relative">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="boLoc.keyword"
              type="text"
              placeholder="Tìm theo mã hóa đơn, tên nhân viên, khách hàng..."
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-[#B82220]/40 focus:bg-white"
            />
          </div>
        </label>

        <label class="min-w-[160px] flex-1 space-y-2">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Ngày bắt đầu</span>
          <div class="relative">
            <input
              v-model="tuNgayHienThi"
              type="text"
              inputmode="numeric"
              placeholder="dd/mm/yyyy"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-11 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-xl text-slate-400 transition hover:bg-slate-100 hover:text-[#B82220]"
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
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Ngày kết thúc</span>
          <div class="relative">
            <input
              v-model="denNgayHienThi"
              type="text"
              inputmode="numeric"
              placeholder="dd/mm/yyyy"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-11 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-xl text-slate-400 transition hover:bg-slate-100 hover:text-[#B82220]"
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

        <label class="min-w-[160px] flex-1 space-y-2">
          <span class="mb-1 block text-[13px] font-semibold text-slate-500">Loại đơn</span>
          <select
            v-model="boLoc.loaiDon"
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
          >
            <option value="">Tất cả loại đơn</option>
            <option value="Tại cửa hàng">Tại cửa hàng</option>
            <option value="Online">Online</option>
          </select>
        </label>

        <div class="flex shrink-0 items-center gap-3">
          <button
            type="button"
            @click="lamMoiBoLoc"
            class="admin-btn-soft flex h-11 items-center gap-2 rounded-2xl px-5 text-sm font-semibold transition"
          >
            <RotateCcw class="h-4 w-4" /> Đặt lại
          </button>
          <button
            type="button"
            @click="xuatExcel"
            class="admin-btn-soft flex h-11 items-center gap-2 rounded-2xl px-5 text-sm font-semibold transition"
          >
            <FileSpreadsheet class="h-4 w-4" /> Xuất Excel
          </button>
        </div>
      </div>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#B82220]/5 text-[#B82220]">
          <FileText class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Danh sách hóa đơn</h2>
        </div>
      </div>

      <div class="mb-4 flex flex-wrap gap-2">
        <button
          v-for="item in tongTheoTrangThai"
          :key="item.ten"
          type="button"
          @click="trangThaiDangChon = item.ten"
          class="rounded-2xl px-4 py-2 text-sm font-medium transition"
          :class="trangThaiDangChon === item.ten ? 'bg-rose-100 text-rose-600' : 'bg-slate-50 text-slate-500 hover:bg-slate-100'"
        >
          {{ item.ten }}
          <span class="ml-2 rounded-full bg-white px-2 py-0.5 text-xs text-slate-500">{{ item.tong }}</span>
        </button>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">
        {{ loiTrang }}
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-[1080px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã hóa đơn</th>
              <th class="bg-slate-100 px-4 py-3">Nhân viên</th>
              <th class="bg-slate-100 px-4 py-3">Khách hàng</th>
              <th class="bg-slate-100 px-4 py-3">Tổng tiền</th>
              <th class="bg-slate-100 px-4 py-3">Ngày tạo</th>
              <th class="bg-slate-100 px-4 py-3">Loại đơn</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu hóa đơn...</td>
            </tr>
            <tr v-else-if="!danhSachPhanTrang.length">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">Không có hóa đơn phù hợp.</td>
            </tr>
            <tr
              v-for="(hoaDon, index) in danhSachPhanTrang"
              :key="hoaDon.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
            >
              <td class="rounded-l-2xl px-4 py-4 font-semibold">
                {{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}
              </td>
              <td class="px-4 py-4 font-semibold text-slate-800">{{ hoaDon.maHoaDon }}</td>
              <td class="px-4 py-4">{{ hoaDon.tenNhanVien }}</td>
              <td class="px-4 py-4">{{ hoaDon.tenKhachHang || "—" }}</td>
              <td class="px-4 py-4 font-semibold text-slate-800">{{ dinhDangTien(hoaDon.tongTien) }}</td>
              <td class="px-4 py-4">{{ dinhDangNgay(hoaDon.ngayTao) }}</td>
              <td class="px-4 py-4">{{ hoaDon.loaiDon }}</td>
              <td class="px-4 py-4">
                <span
                  class="inline-flex rounded-full px-3 py-1 text-xs font-semibold"
                  :class="mauTrangThai[hoaDon.trangThai] || 'bg-slate-100 text-slate-600'"
                >
                  {{ hoaDon.trangThai }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-4 text-center">
                <div class="flex items-center justify-center gap-2">
                  <button
                    type="button"
                    @click="xuatHoaDonPdf(hoaDon.id)"
                    class="inline-flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-sky-50 hover:text-sky-600"
                    :title="dangXuatPdfId === hoaDon.id ? 'Đang xuất PDF' : 'Xuất PDF'"
                    :disabled="dangXuatPdfId === hoaDon.id"
                  >
                    <Download class="h-4 w-4" />
                  </button>
                  <button
                    type="button"
                    @click="xemChiTiet(hoaDon.id)"
                    class="inline-flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-[#B82220]/10 hover:text-[#B82220]"
                    title="Xem chi tiết"
                  >
                    <Eye class="h-4 w-4" />
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
        :total-items="danhSachHienThi.length"
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
