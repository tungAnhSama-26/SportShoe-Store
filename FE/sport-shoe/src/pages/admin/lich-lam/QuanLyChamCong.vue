<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { layDanhSachNhanVien } from "../../../services/nhan-vien";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import { showSuccess, showError } from "../../../utils/alert";
import { exportRowsToExcel } from "../../../utils/export-excel";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import Card from "../../../components/ui/Card.vue";
import Table from "../../../components/ui/Table.vue";
import Button from "../../../components/ui/Button.vue";
import Badge from "../../../components/ui/Badge.vue";
import {
  ClipboardCheck,
  FileSpreadsheet,
  Filter,
  RotateCcw,
  Search,
  Clock,
  Users,
  TrendingUp,
  CalendarDays,
} from "lucide-vue-next";

// ─── Types ────────────────────────────────────────────────────────────────────

interface ChamCongRecord {
  id: string;
  nhanVienId: string;
  hoTen: string;
  ma: string;
  hinhAnh?: string;
  ngay: string;
  ca: number;
  gioVao?: string;
  gioRa?: string;
  trangThai: "dung_gio" | "tre" | "ve_som" | "vang_mat";
  ghiChu?: string;
}

// ─── Constants ────────────────────────────────────────────────────────────────

const CA_LABELS: Record<number, string> = { 1: "Ca Sáng", 2: "Ca Chiều", 3: "Ca Tối" };
const TRANG_THAI_CONFIG: Record<string, { label: string; variant: string }> = {
  dung_gio: { label: "Đúng giờ", variant: "success" },
  tre: { label: "Đi trễ", variant: "warning" },
  ve_som: { label: "Về sớm", variant: "warning" },
  vang_mat: { label: "Vắng mặt", variant: "danger" },
};

// ─── State ────────────────────────────────────────────────────────────────────

const dangTai = ref(false);
const loiTrang = ref("");
const danhSach = ref<ChamCongRecord[]>([]);
const danhSachNhanVien = ref<{ id: string; ma: string; hoTen: string }[]>([]);

const boLoc = ref({
  keyword: "",
  nhanVienId: "",
  ca: "",
  trangThai: "",
  tuNgay: "",
  denNgay: "",
});

// Phân trang
const soPhanTuMotTrang = ref(10);
const trangHienTai = ref(1);
const pageSizeOptions = [10, 20, 50];

// ─── Computed ─────────────────────────────────────────────────────────────────

const danhSachLoc = computed(() => {
  let result = danhSach.value;
  const kw = boLoc.value.keyword.trim().toLowerCase();
  if (kw) {
    result = result.filter(
      (r) =>
        r.hoTen.toLowerCase().includes(kw) ||
        r.ma.toLowerCase().includes(kw)
    );
  }
  if (boLoc.value.nhanVienId) {
    result = result.filter((r) => r.nhanVienId === boLoc.value.nhanVienId);
  }
  if (boLoc.value.ca) {
    result = result.filter((r) => r.ca === Number(boLoc.value.ca));
  }
  if (boLoc.value.trangThai) {
    result = result.filter((r) => r.trangThai === boLoc.value.trangThai);
  }
  if (boLoc.value.tuNgay) {
    result = result.filter((r) => r.ngay >= boLoc.value.tuNgay);
  }
  if (boLoc.value.denNgay) {
    result = result.filter((r) => r.ngay <= boLoc.value.denNgay);
  }
  return result;
});

const tongSoTrang = computed(
  () => Math.ceil(danhSachLoc.value.length / soPhanTuMotTrang.value) || 1
);

const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
  return danhSachLoc.value.slice(start, start + soPhanTuMotTrang.value);
});

const thongKe = computed(() => {
  const total = danhSachLoc.value.length;
  const dungGio = danhSachLoc.value.filter((r) => r.trangThai === "dung_gio").length;
  const tre = danhSachLoc.value.filter((r) => r.trangThai === "tre").length;
  const vangMat = danhSachLoc.value.filter((r) => r.trangThai === "vang_mat").length;
  return { total, dungGio, tre, vangMat };
});

watch(danhSachLoc, () => { trangHienTai.value = 1; });
watch(soPhanTuMotTrang, () => { trangHienTai.value = 1; });

// ─── Mock / API ───────────────────────────────────────────────────────────────

function formatGio(gio: number, phut: number = 0): string {
  const h = Math.floor(gio) % 24;
  const p = phut;
  return `${String(h).padStart(2, "0")}:${String(p).padStart(2, "0")}`;
}

function taoMock(dsnv: { id: string; ma: string; hoTen: string }[]): ChamCongRecord[] {
  if (!dsnv.length) return [];
  const trangThaiList: ChamCongRecord["trangThai"][] = ["dung_gio", "dung_gio", "tre", "ve_som", "vang_mat"];
  const records: ChamCongRecord[] = [];
  const today = new Date();
  for (let d = 0; d < 7; d++) {
    const ngay = new Date(today);
    ngay.setDate(today.getDate() - d);
    const ngayStr = ngay.toISOString().slice(0, 10);
    dsnv.slice(0, Math.min(dsnv.length, 8)).forEach((nv, i) => {
      const ca = (i % 3) + 1;
      const tt = trangThaiList[i % trangThaiList.length];
      // Thời gian vào/ra chuẩn cho từng ca
      const gioVaoChuanList = [8, 13, 18]; // Ca 1: 8:00, Ca 2: 13:00, Ca 3: 18:00
      const gioRaChuanList = [12, 17, 22]; // Ca 1: 12:00, Ca 2: 17:00, Ca 3: 22:00
      const gioVaoChuanVal = gioVaoChuanList[ca - 1];
      const gioRaChuanVal = gioRaChuanList[ca - 1];
      
      let gioVaoPhut = tt === "tre" ? 15 : 0;
      let gioRaPhut = tt === "ve_som" ? 30 : 0;
      
      records.push({
        id: `${nv.id}-${ngayStr}-${ca}`,
        nhanVienId: nv.id,
        hoTen: nv.hoTen,
        ma: nv.ma,
        ngay: ngayStr,
        ca,
        gioVao: tt !== "vang_mat" ? formatGio(gioVaoChuanVal, gioVaoPhut) : undefined,
        gioRa: tt !== "vang_mat" ? formatGio(gioRaChuanVal, gioRaPhut) : undefined,
        trangThai: tt,
      });
    });
  }
  return records;
}

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const ds = await layDanhSachNhanVien({ trangThai: 1 });
    danhSachNhanVien.value = Array.isArray(ds) ? ds : [];
    // TODO: Thay bằng API chấm công thực tế khi backend sẵn sàng
    danhSach.value = taoMock(danhSachNhanVien.value);
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải dữ liệu chấm công.");
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  boLoc.value = { keyword: "", nhanVienId: "", ca: "", trangThai: "", tuNgay: "", denNgay: "" };
}

function formatNgay(ngay: string) {
  if (!ngay) return "—";
  const [y, m, d] = ngay.split("-");
  return `${d}/${m}/${y}`;
}

function xuatExcel() {
  if (!danhSachLoc.value.length) {
    showError("Không có dữ liệu để xuất Excel.");
    return;
  }
  exportRowsToExcel({
    filename: "cham-cong",
    sheetName: "ChamCong",
    columns: [
      { label: "STT", value: (_: any, i: number) => i + 1 },
      { label: "Mã NV", key: "ma" },
      { label: "Họ tên", key: "hoTen" },
      { label: "Ngày", value: (r: any) => formatNgay(r.ngay) },
      { label: "Ca", value: (r: any) => CA_LABELS[r.ca] ?? "—" },
      { label: "Giờ vào", value: (r: any) => r.gioVao ?? "—" },
      { label: "Giờ ra", value: (r: any) => r.gioRa ?? "—" },
      { label: "Trạng thái", value: (r: any) => TRANG_THAI_CONFIG[r.trangThai]?.label ?? "—" },
      { label: "Ghi chú", value: (r: any) => r.ghiChu ?? "—" },
    ],
    rows: danhSachLoc.value,
  });
}

let searchTimer: ReturnType<typeof setTimeout>;
watch(() => boLoc.value.keyword, () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => { trangHienTai.value = 1; }, 300);
});

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <!-- Thống kê nhanh -->
    <div class="grid grid-cols-2 gap-3 lg:grid-cols-4">
      <Card class="!p-0">
        <div class="flex items-center gap-3 p-4">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
            <Users class="h-5 w-5" />
          </div>
          <div>
            <p class="text-xs text-slate-400">Tổng lượt</p>
            <p class="text-xl font-bold text-slate-800">{{ thongKe.total }}</p>
          </div>
        </div>
      </Card>
      <Card class="!p-0">
        <div class="flex items-center gap-3 p-4">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600">
            <ClipboardCheck class="h-5 w-5" />
          </div>
          <div>
            <p class="text-xs text-slate-400">Đúng giờ</p>
            <p class="text-xl font-bold text-emerald-600">{{ thongKe.dungGio }}</p>
          </div>
        </div>
      </Card>
      <Card class="!p-0">
        <div class="flex items-center gap-3 p-4">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-amber-50 text-amber-600">
            <Clock class="h-5 w-5" />
          </div>
          <div>
            <p class="text-xs text-slate-400">Đi trễ</p>
            <p class="text-xl font-bold text-amber-600">{{ thongKe.tre }}</p>
          </div>
        </div>
      </Card>
      <Card class="!p-0">
        <div class="flex items-center gap-3 p-4">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-rose-50 text-rose-600">
            <TrendingUp class="h-5 w-5" />
          </div>
          <div>
            <p class="text-xs text-slate-400">Vắng mặt</p>
            <p class="text-xl font-bold text-rose-600">{{ thongKe.vangMat }}</p>
          </div>
        </div>
      </Card>
    </div>

    <!-- Bộ lọc -->
    <Card>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
            <Filter class="h-5 w-5" />
          </div>
          <h2 class="admin-section-title">Bộ lọc</h2>
        </div>
      </template>

      <div class="flex flex-col gap-4">
        <div class="grid grid-cols-1 gap-3 lg:grid-cols-12 items-end">
          <!-- Tìm kiếm -->
          <div class="lg:col-span-4">
            <div class="relative">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                v-model="boLoc.keyword"
                type="text"
                placeholder="Tìm theo mã, họ tên..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              />
            </div>
          </div>

          <!-- Nhân viên -->
          <div class="lg:col-span-3">
            <select
              v-model="boLoc.nhanVienId"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
            >
              <option value="">Tất cả nhân viên</option>
              <option v-for="nv in danhSachNhanVien" :key="nv.id" :value="nv.id">
                {{ nv.hoTen }}
              </option>
            </select>
          </div>

          <!-- Ca -->
          <div class="lg:col-span-2">
            <select
              v-model="boLoc.ca"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
            >
              <option value="">Tất cả ca</option>
              <option value="1">Ca Sáng</option>
              <option value="2">Ca Chiều</option>
              <option value="3">Ca Tối</option>
            </select>
          </div>

          <!-- Trạng thái -->
          <div class="lg:col-span-2">
            <select
              v-model="boLoc.trangThai"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
            >
              <option value="">Tất cả trạng thái</option>
              <option value="dung_gio">Đúng giờ</option>
              <option value="tre">Đi trễ</option>
              <option value="ve_som">Về sớm</option>
              <option value="vang_mat">Vắng mặt</option>
            </select>
          </div>

          <!-- Từ ngày -->
          <div class="lg:col-span-2">
            <label class="block text-xs font-medium text-slate-600 mb-1">Từ ngày</label>
            <input
              v-model="boLoc.tuNgay"
              type="date"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
            />
          </div>

          <!-- Đến ngày -->
          <div class="lg:col-span-2">
            <label class="block text-xs font-medium text-slate-600 mb-1">Đến ngày</label>
            <input
              v-model="boLoc.denNgay"
              type="date"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
            />
          </div>
        </div>

        <div class="flex flex-wrap items-center justify-end gap-3">
          <Button variant="soft" @click="lamMoiBoLoc">
            <template #prefix><RotateCcw class="h-4 w-4" /></template>
            Đặt lại
          </Button>
          <Button variant="soft" @click="xuatExcel">
            <template #prefix><FileSpreadsheet class="h-4 w-4" /></template>
            Xuất Excel
          </Button>
          <Button variant="soft" :loading="dangTai" @click="taiDanhSach">
            <template #prefix><RotateCcw class="h-4 w-4" /></template>
            Làm mới
          </Button>
        </div>
      </div>
    </Card>

    <!-- Bảng chấm công -->
    <Card>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/5 text-primary">
            <CalendarDays class="h-5 w-5" />
          </div>
          <div>
            <h2 class="admin-section-title">Dữ liệu chấm công</h2>
            <p class="text-xs text-slate-400 mt-0.5">{{ danhSachLoc.length }} bản ghi</p>
          </div>
        </div>
      </template>

      <div
        v-if="loiTrang"
        class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600"
      >
        {{ loiTrang }}
      </div>

      <div class="admin-table-scroll">
        <Table>
          <template #header>
            <th class="px-3 py-3 whitespace-nowrap">STT</th>
            <th class="px-3 py-3 whitespace-nowrap">Ảnh</th>
            <th class="px-3 py-3 whitespace-nowrap">Mã NV</th>
            <th class="px-3 py-3 whitespace-nowrap">Họ tên</th>
            <th class="px-3 py-3 whitespace-nowrap">Ngày</th>
            <th class="px-3 py-3 whitespace-nowrap">Ca làm</th>
            <th class="px-3 py-3 whitespace-nowrap">Giờ vào</th>
            <th class="px-3 py-3 whitespace-nowrap">Giờ ra</th>
            <th class="px-3 py-3 whitespace-nowrap">Trạng thái</th>
            <th class="px-3 py-3 whitespace-nowrap">Ghi chú</th>
          </template>
          <template #body>
            <tr v-if="dangTai">
              <td colspan="10" class="py-10 text-center text-sm text-slate-400">
                Đang tải dữ liệu chấm công...
              </td>
            </tr>
            <tr v-else-if="!danhSachPhanTrang.length">
              <td colspan="10" class="py-10 text-center text-sm text-slate-400">
                Không có dữ liệu chấm công phù hợp.
              </td>
            </tr>
            <tr
              v-for="(row, index) in danhSachPhanTrang"
              :key="row.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
            >
              <td class="rounded-l-2xl px-3 py-3 font-semibold">
                {{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}
              </td>
              <td class="px-3 py-3">
                <img
                  :src="
                    row.hinhAnh ||
                    'https://ui-avatars.com/api/?name=' +
                      encodeURIComponent(row.hoTen) +
                      '&background=f1f5f9&color=475569&size=64'
                  "
                  :alt="row.hoTen"
                  class="h-9 w-9 rounded-full object-cover ring-2 ring-slate-100"
                />
              </td>
              <td class="px-3 py-3 font-semibold text-slate-800">
                <div class="truncate" :title="row.ma">{{ row.ma }}</div>
              </td>
              <td class="px-3 py-3 font-semibold text-slate-800">
                <div class="truncate" :title="row.hoTen">{{ row.hoTen }}</div>
              </td>
              <td class="px-3 py-3 text-slate-600">
                {{ formatNgay(row.ngay) }}
              </td>
              <td class="px-3 py-3">
                <span
                  class="inline-flex items-center rounded-lg px-2.5 py-1 text-xs font-semibold"
                  :class="
                    row.ca === 1
                      ? 'bg-amber-50 text-amber-700'
                      : row.ca === 2
                        ? 'bg-sky-50 text-sky-700'
                        : 'bg-violet-50 text-violet-700'
                  "
                >
                  {{ CA_LABELS[row.ca] ?? '—' }}
                </span>
              </td>
              <td class="px-3 py-3 text-slate-600 font-mono text-sm">
                {{ row.gioVao ?? '—' }}
              </td>
              <td class="px-3 py-3 text-slate-600 font-mono text-sm">
                {{ row.gioRa ?? '—' }}
              </td>
              <td class="px-3 py-3">
                <Badge
                  :variant="
                    (TRANG_THAI_CONFIG[row.trangThai]?.variant as any) ?? 'default'
                  "
                >
                  {{ TRANG_THAI_CONFIG[row.trangThai]?.label ?? '—' }}
                </Badge>
              </td>
              <td class="rounded-r-2xl px-3 py-3 text-slate-500 text-sm">
                {{ row.ghiChu ?? '—' }}
              </td>
            </tr>
          </template>
        </Table>
      </div>

      <template #footer>
        <AdminTableFooter
          :current-page="trangHienTai"
          :page-size="soPhanTuMotTrang"
          :page-size-options="pageSizeOptions"
          :total-items="danhSachLoc.length"
          :total-pages="tongSoTrang"
          compact
          show-refresh
          @refresh="taiDanhSach"
          @update:current-page="trangHienTai = $event"
          @update:page-size="soPhanTuMotTrang = $event"
        />
      </template>
    </Card>
  </div>
</template>
