<script setup>
import { ref, computed, onMounted, watch } from "vue";
import {
  CalendarDays, ChevronLeft, ChevronRight, ClipboardCheck,
  Clock, Download, Search, UserCheck, UserX, AlertCircle, CheckCircle2,
} from "lucide-vue-next";
import { layDanhSachNhanVien } from "../../../services/nhan-vien.js";
import { layChamCong, checkIn, checkOut } from "../../../services/cham-cong.js";
import { showSuccess, showError, showConfirm } from "../../../utils/alert.js";
import { getDisplayErrorMessage } from "../../../utils/error-message.js";
import { exportRowsToExcel } from "../../../utils/export-excel.js";

// ────────── Tuần ──────────
const ngayHienTai = ref(new Date());

function dauTuan(d) {
  const nd = new Date(d);
  const day = nd.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  nd.setDate(nd.getDate() + diff);
  nd.setHours(0, 0, 0, 0);
  return nd;
}

const ngayDauTuan = computed(() => dauTuan(ngayHienTai.value));

const cacNgay = computed(() =>
  Array.from({ length: 7 }, (_, i) => {
    const d = new Date(ngayDauTuan.value);
    d.setDate(d.getDate() + i);
    return d;
  })
);

function formatISODate(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

function formatTuan() {
  const f = (d) =>
    `${String(d.getDate()).padStart(2, "0")} tháng ${d.getMonth() + 1}, ${d.getFullYear()}`;
  return `${f(cacNgay.value[0])} – ${f(cacNgay.value[6])}`;
}

function tuanTruoc() {
  const d = new Date(ngayHienTai.value);
  d.setDate(d.getDate() - 7);
  ngayHienTai.value = d;
}
function tuanSau() {
  const d = new Date(ngayHienTai.value);
  d.setDate(d.getDate() + 7);
  ngayHienTai.value = d;
}
function homNay() { ngayHienTai.value = new Date(); }

// ────────── Bộ lọc ──────────
const keyword = ref("");
const locNhanVienId = ref("");
const locTrangThai = ref("");
const locCa = ref("");

const DS_TRANG_THAI = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "DUNG_GIO", label: "Đúng giờ" },
  { value: "DI_TRE", label: "Đi trễ" },
  { value: "VE_SOM", label: "Về sớm" },
  { value: "VANG_MAT", label: "Vắng mặt" },
];

const DS_CA_LABEL = { sang: "Sáng", chieu: "Chiều", toi: "Tối" };

// ────────── Dữ liệu ──────────
const dangTai = ref(false);
const danhSachNV = ref([]);
const danhSachChamCong = ref([]);

// Dữ liệu mock khi BE chưa có
function taoMockChamCong(nvList) {
  const trangThais = ["DUNG_GIO", "DI_TRE", "VE_SOM", "VANG_MAT"];
  const cas = ["sang", "chieu", "toi"];
  const rows = [];
  let id = 1;
  cacNgay.value.forEach((ngay) => {
    nvList.forEach((nv) => {
      const trangThai = trangThais[Math.floor(Math.random() * trangThais.length)];
      const ca = cas[Math.floor(Math.random() * cas.length)];
      rows.push({
        id: id++,
        nhanVienId: nv.id,
        tenNhanVien: nv.hoTen,
        ngay: formatISODate(ngay),
        ca,
        gioVao: trangThai === "VANG_MAT" ? null : (ca === "sang" ? "08:05" : ca === "chieu" ? "13:10" : "18:02"),
        gioRa: trangThai === "VANG_MAT" ? null : (ca === "sang" ? "12:00" : ca === "chieu" ? "17:00" : "22:00"),
        trangThai,
      });
    });
  });
  return rows;
}

async function taiDuLieu() {
  dangTai.value = true;
  try {
    const nvList = await layDanhSachNhanVien({ trangThai: 1 });
    danhSachNV.value = nvList;

    const tuNgay = formatISODate(cacNgay.value[0]);
    const denNgay = formatISODate(cacNgay.value[6]);
    try {
      const data = await layChamCong({ tuNgay, denNgay });
      danhSachChamCong.value = Array.isArray(data) ? data : taoMockChamCong(nvList);
    } catch {
      danhSachChamCong.value = taoMockChamCong(nvList);
    }
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể tải dữ liệu chấm công"));
  } finally {
    dangTai.value = false;
  }
}

onMounted(taiDuLieu);
watch(ngayDauTuan, taiDuLieu);

// ────────── Bảng lọc ──────────
const danhSachHienThi = computed(() => {
  let list = danhSachChamCong.value;
  if (locNhanVienId.value) list = list.filter((r) => String(r.nhanVienId) === locNhanVienId.value);
  if (locTrangThai.value) list = list.filter((r) => r.trangThai === locTrangThai.value);
  if (locCa.value) list = list.filter((r) => r.ca === locCa.value);
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase();
    list = list.filter((r) => r.tenNhanVien?.toLowerCase().includes(kw));
  }

  // Sắp xếp: Đưa các ca của hôm nay lên đầu tiên, sau đó sắp xếp theo ngày gần nhất
  const todayStr = formatISODate(new Date());
  list.sort((a, b) => {
    if (a.ngay === todayStr && b.ngay !== todayStr) return -1;
    if (b.ngay === todayStr && a.ngay !== todayStr) return 1;
    // Nếu cùng là hôm nay hoặc cùng không phải hôm nay, sắp xếp theo ngày giảm dần
    return new Date(b.ngay) - new Date(a.ngay);
  });

  return list;
});

// ────────── Thống kê ──────────
const thongKe = computed(() => {
  const list = danhSachChamCong.value;
  return {
    tongCa: list.length,
    dungGio: list.filter((r) => r.trangThai === "DUNG_GIO").length,
    diTre: list.filter((r) => r.trangThai === "DI_TRE").length,
    vangMat: list.filter((r) => r.trangThai === "VANG_MAT").length,
  };
});

// ────────── Phân trang ──────────
const soTrang = ref(10);
const trangHienTai = ref(1);
const tongSoTrang = computed(() => Math.ceil(danhSachHienThi.value.length / soTrang.value) || 1);
const danhSachTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soTrang.value;
  return danhSachHienThi.value.slice(start, start + soTrang.value);
});
watch([locNhanVienId, locTrangThai, keyword], () => { trangHienTai.value = 1; });

// ────────── Hành động ──────────
const dangXuLy = ref({});

async function xuLyCheckIn(row) {
  if (dangXuLy.value[row.id]) return;
  dangXuLy.value[row.id] = true;
  try {
    await checkIn({ nhanVienId: row.nhanVienId });
    row.gioVao = new Date().toTimeString().slice(0, 5);
    row.trangThai = "DUNG_GIO";
    showSuccess(`Đã check-in ${row.tenNhanVien}`);
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể check-in"));
  } finally {
    dangXuLy.value[row.id] = false;
  }
}

async function xuLyCheckOut(row) {
  if (dangXuLy.value[row.id]) return;
  dangXuLy.value[row.id] = true;
  try {
    await checkOut({ nhanVienId: row.nhanVienId });
    row.gioRa = new Date().toTimeString().slice(0, 5);
    showSuccess(`Đã check-out ${row.tenNhanVien}`);
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể check-out"));
  } finally {
    dangXuLy.value[row.id] = false;
  }
}

// ────────── Xuất Excel ──────────
function xuatExcel() {
  const rows = danhSachHienThi.value.map((r) => ({
    "Nhân viên": r.tenNhanVien,
    "Ngày": r.ngay,
    "Ca": DS_CA_LABEL[r.ca] ?? r.ca,
    "Giờ vào": r.gioVao ?? "—",
    "Giờ ra": r.gioRa ?? "—",
    "Trạng thái": trangThaiLabel(r.trangThai),
  }));
  exportRowsToExcel(rows, "ChamCong");
}

// ────────── Helpers UI ──────────
function trangThaiLabel(tt) {
  const map = { DUNG_GIO: "Đúng giờ", DI_TRE: "Đi trễ", VE_SOM: "Về sớm", VANG_MAT: "Vắng mặt" };
  return map[tt] ?? tt;
}

function trangThaiBadge(tt) {
  const map = {
    DUNG_GIO: "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-300",
    DI_TRE:   "bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-300",
    VE_SOM:   "bg-orange-100 text-orange-700 dark:bg-orange-900/40 dark:text-orange-300",
    VANG_MAT: "bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300",
  };
  return map[tt] ?? "bg-slate-100 text-slate-600";
}

function caBadge(ca) {
  const map = {
    sang:  "bg-amber-100 text-amber-700",
    chieu: "bg-orange-100 text-orange-700",
    toi:   "bg-violet-100 text-violet-700",
  };
  return map[ca] ?? "bg-slate-100 text-slate-600";
}
</script>

<template>
  <div class="space-y-6">

    <!-- ── Header ── -->
    <div class="flex flex-wrap items-start justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">
          Chấm công nhân viên
        </h1>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Theo dõi giờ vào – ra và trạng thái chuyên cần
        </p>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <!-- Điều hướng tuần -->
        <button @click="tuanTruoc" class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-600 transition hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-300">
          <ChevronLeft class="h-4 w-4" />
        </button>
        <button @click="homNay" class="h-9 rounded-lg border border-slate-200 bg-white px-4 text-sm font-medium text-slate-700 transition hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-300">
          Hôm nay
        </button>
        <button @click="tuanSau" class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-600 transition hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-300">
          <ChevronRight class="h-4 w-4" />
        </button>
        <div class="flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-medium text-slate-700 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-200">
          <CalendarDays class="h-4 w-4 text-slate-400" />
          {{ formatTuan() }}
        </div>
        <!-- Xuất Excel -->
        <button @click="xuatExcel" class="flex h-9 items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 text-sm font-medium text-slate-700 transition hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-200">
          <Download class="h-4 w-4" /> Xuất Excel
        </button>
      </div>
    </div>

    <!-- ── Thẻ thống kê ── -->
    <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
      <div class="rounded-2xl border border-slate-100 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800">
        <p class="text-xs font-medium uppercase tracking-wide text-slate-400">Tổng ca</p>
        <p class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">{{ thongKe.tongCa }}</p>
      </div>
      <div class="rounded-2xl border border-emerald-100 bg-emerald-50 p-4 shadow-sm dark:border-emerald-800 dark:bg-emerald-900/20">
        <p class="flex items-center gap-1 text-xs font-medium uppercase tracking-wide text-emerald-600">
          <CheckCircle2 class="h-3.5 w-3.5" /> Đúng giờ
        </p>
        <p class="mt-2 text-3xl font-bold text-emerald-700 dark:text-emerald-300">{{ thongKe.dungGio }}</p>
      </div>
      <div class="rounded-2xl border border-amber-100 bg-amber-50 p-4 shadow-sm dark:border-amber-800 dark:bg-amber-900/20">
        <p class="flex items-center gap-1 text-xs font-medium uppercase tracking-wide text-amber-600">
          <AlertCircle class="h-3.5 w-3.5" /> Đi trễ
        </p>
        <p class="mt-2 text-3xl font-bold text-amber-700 dark:text-amber-300">{{ thongKe.diTre }}</p>
      </div>
      <div class="rounded-2xl border border-red-100 bg-red-50 p-4 shadow-sm dark:border-red-800 dark:bg-red-900/20">
        <p class="flex items-center gap-1 text-xs font-medium uppercase tracking-wide text-red-500">
          <UserX class="h-3.5 w-3.5" /> Vắng mặt
        </p>
        <p class="mt-2 text-3xl font-bold text-red-600 dark:text-red-400">{{ thongKe.vangMat }}</p>
      </div>
    </div>

    <!-- ── Bộ lọc ── -->
    <div class="flex flex-wrap gap-3 rounded-2xl border border-slate-100 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <!-- Tìm kiếm -->
      <div class="relative flex-1 min-w-[200px]">
        <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
        <input
          v-model="keyword"
          placeholder="Tìm nhân viên..."
          class="h-9 w-full rounded-lg border border-slate-200 pl-9 pr-3 text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary/30 dark:border-slate-600 dark:bg-slate-700 dark:text-white"
        />
      </div>
      <!-- Lọc nhân viên -->
      <select
        v-model="locNhanVienId"
        class="h-9 rounded-lg border border-slate-200 px-3 text-sm text-slate-700 focus:border-primary focus:outline-none dark:border-slate-600 dark:bg-slate-700 dark:text-slate-200"
      >
        <option value="">Tất cả nhân viên</option>
        <option v-for="nv in danhSachNV" :key="nv.id" :value="String(nv.id)">
          {{ nv.hoTen }}
        </option>
      </select>
      <!-- Lọc ca -->
      <select
        v-model="locCa"
        class="h-9 rounded-lg border border-slate-200 px-3 text-sm text-slate-700 focus:border-primary focus:outline-none dark:border-slate-600 dark:bg-slate-700 dark:text-slate-200"
      >
        <option value="">Tất cả ca</option>
        <option value="sang">Ca sáng</option>
        <option value="chieu">Ca chiều</option>
        <option value="toi">Ca tối</option>
      </select>
      <!-- Lọc trạng thái -->
      <select
        v-model="locTrangThai"
        class="h-9 rounded-lg border border-slate-200 px-3 text-sm text-slate-700 focus:border-primary focus:outline-none dark:border-slate-600 dark:bg-slate-700 dark:text-slate-200"
      >
        <option v-for="tt in DS_TRANG_THAI" :key="tt.value" :value="tt.value">
          {{ tt.label }}
        </option>
      </select>
    </div>

    <!-- ── Bảng chấm công ── -->
    <div class="overflow-hidden rounded-2xl border border-slate-100 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <!-- Skeleton -->
      <div v-if="dangTai" class="space-y-2 p-4">
        <div v-for="i in 6" :key="i" class="h-14 rounded-xl bg-slate-100 animate-pulse dark:bg-slate-700" />
      </div>

      <template v-else>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-slate-100 bg-slate-50 text-left dark:border-slate-700 dark:bg-slate-700/50">
                <th class="px-4 py-3 font-semibold text-slate-600 dark:text-slate-300">Nhân viên</th>
                <th class="px-4 py-3 font-semibold text-slate-600 dark:text-slate-300">Ngày</th>
                <th class="px-4 py-3 font-semibold text-slate-600 dark:text-slate-300">Ca</th>
                <th class="px-4 py-3 font-semibold text-slate-600 dark:text-slate-300">Giờ vào</th>
                <th class="px-4 py-3 font-semibold text-slate-600 dark:text-slate-300">Giờ ra</th>
                <th class="px-4 py-3 font-semibold text-slate-600 dark:text-slate-300">Trạng thái</th>
                <th class="px-4 py-3 text-right font-semibold text-slate-600 dark:text-slate-300">Hành động</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-50 dark:divide-slate-700/50">
              <tr
                v-for="row in danhSachTrang"
                :key="row.id"
                class="transition hover:bg-slate-50/60 dark:hover:bg-slate-700/30"
              >
                <!-- Nhân viên -->
                <td class="px-4 py-3">
                  <div class="flex items-center gap-2.5">
                    <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xs font-bold text-primary">
                      {{ (row.tenNhanVien ?? "?")[0].toUpperCase() }}
                    </div>
                    <span class="font-medium text-slate-800 dark:text-slate-200">{{ row.tenNhanVien }}</span>
                  </div>
                </td>
                <!-- Ngày -->
                <td class="px-4 py-3 text-slate-600 dark:text-slate-400">{{ row.ngay }}</td>
                <!-- Ca -->
                <td class="px-4 py-3">
                  <span class="rounded-full px-2.5 py-0.5 text-xs font-semibold" :class="caBadge(row.ca)">
                    {{ DS_CA_LABEL[row.ca] ?? row.ca }}
                  </span>
                </td>
                <!-- Giờ vào -->
                <td class="px-4 py-3">
                  <div class="flex items-center gap-1.5 text-slate-700 dark:text-slate-300">
                    <Clock class="h-3.5 w-3.5 text-slate-400" />
                    {{ row.gioVao ?? "—" }}
                  </div>
                </td>
                <!-- Giờ ra -->
                <td class="px-4 py-3">
                  <div class="flex items-center gap-1.5 text-slate-700 dark:text-slate-300">
                    <Clock class="h-3.5 w-3.5 text-slate-400" />
                    {{ row.gioRa ?? "—" }}
                  </div>
                </td>
                <!-- Trạng thái -->
                <td class="px-4 py-3">
                  <span class="rounded-full px-2.5 py-1 text-xs font-semibold" :class="trangThaiBadge(row.trangThai)">
                    {{ trangThaiLabel(row.trangThai) }}
                  </span>
                </td>
                <!-- Hành động -->
                <td class="px-4 py-3">
                  <div class="flex items-center justify-end gap-2">
                    <button
                      v-if="!row.gioVao"
                      @click="xuLyCheckIn(row)"
                      :disabled="dangXuLy[row.id]"
                      class="flex items-center gap-1.5 rounded-lg bg-emerald-500 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-emerald-600 disabled:opacity-50"
                    >
                      <UserCheck class="h-3.5 w-3.5" /> Check-in
                    </button>
                    <button
                      v-else-if="!row.gioRa"
                      @click="xuLyCheckOut(row)"
                      :disabled="dangXuLy[row.id]"
                      class="flex items-center gap-1.5 rounded-lg bg-orange-500 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-orange-600 disabled:opacity-50"
                    >
                      <UserX class="h-3.5 w-3.5" /> Check-out
                    </button>
                    <span v-else class="text-xs text-slate-400">Hoàn tất</span>
                  </div>
                </td>
              </tr>
              <!-- Empty state -->
              <tr v-if="danhSachTrang.length === 0">
                <td colspan="7" class="py-16 text-center text-slate-400">
                  <ClipboardCheck class="mx-auto mb-3 h-10 w-10 opacity-30" />
                  <p class="font-medium">Không có dữ liệu chấm công</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Phân trang -->
        <div v-if="tongSoTrang > 1" class="flex items-center justify-between border-t border-slate-100 px-4 py-3 dark:border-slate-700">
          <span class="text-sm text-slate-500">
            {{ danhSachHienThi.length }} bản ghi
          </span>
          <div class="flex items-center gap-1">
            <button
              v-for="p in tongSoTrang"
              :key="p"
              @click="trangHienTai = p"
              class="h-8 w-8 rounded-lg text-sm font-medium transition"
              :class="p === trangHienTai
                ? 'bg-primary text-white'
                : 'text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-700'"
            >
              {{ p }}
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
