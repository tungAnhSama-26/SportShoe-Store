<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { layDanhSachNhanVien } from "../../../services/nhan-vien.js";
import {
  layLichLamViec,
  phanCa,
  xepCaTuDong,
} from "../../../services/lich-lam.js";
import { getDisplayErrorMessage } from "../../../utils/error-message.js";
import { showSuccess, showError, showConfirm } from "../../../utils/alert.js";
import Card from "../../../components/ui/Card.vue";
import Button from "../../../components/ui/Button.vue";
import {
  ArrowLeft,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Download,
  MoreHorizontal,
  Plus,
  Shuffle,
  Users,
} from "lucide-vue-next";

// ─── Types ────────────────────────────────────────────────────────────────────

interface LichLamRecord {
  id: string;
  nhanVienId: string;
  ngay: string; // "YYYY-MM-DD"
  ca: CaKey; // "sang" | "chieu" | "toi"
}

interface NhanVien {
  id: string;
  ma: string;
  hoTen: string;
  hinhAnh?: string;
}

type CaKey = "sang" | "chieu" | "toi";

// ─── Constants ────────────────────────────────────────────────────────────────

const MAX_PER_SHIFT = 3;

const CA_LIST: {
  key: CaKey;
  label: string;
  time: string;
  color: string;
  ring: string;
}[] = [
    {
      key: "sang",
      label: "Ca Sáng",
      time: "08:00 – 12:00",
      color: "bg-amber-50 border-amber-200 text-amber-700",
      ring: "ring-amber-300",
    },
    {
      key: "chieu",
      label: "Ca Chiều",
      time: "13:00 – 17:00",
      color: "bg-sky-50 border-sky-200 text-sky-700",
      ring: "ring-sky-300",
    },
    {
      key: "toi",
      label: "Ca Tối",
      time: "18:00 – 22:00",
      color: "bg-violet-50 border-violet-200 text-violet-700",
      ring: "ring-violet-300",
    },
  ];

const DAY_LABELS = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];

// ─── Route ────────────────────────────────────────────────────────────────────

const route = useRoute();
const router = useRouter();

// ───────── Dữ liệu ca làm việc ─────────
type CaLamId = "sang" | "chieu" | "toi";

const MAX_NHAN_VIEN_MOI_CA = 3;

const DS_CA: Array<{ id: CaLamId; nhan: string; gio: string; mau: string; muaNhat: string }> = [
  { id: "sang",  nhan: "Sáng",  gio: "08:00 - 12:00", mau: "bg-emerald-500", muaNhat: "bg-emerald-50 border-emerald-200 text-emerald-700" },
  { id: "chieu", nhan: "Chiều", gio: "13:00 - 17:00", mau: "bg-orange-400",  muaNhat: "bg-orange-50 border-orange-200 text-orange-700" },
  { id: "toi",   nhan: "Tối",   gio: "18:00 - 22:00", mau: "bg-violet-400",  muaNhat: "bg-violet-50 border-violet-200 text-violet-700" },
];

// ───────── Tuần hiện tại ─────────
const ngayHienTai = ref(new Date());

function dauTuan(d: Date) {
  const nd = new Date(d);
  const day = nd.getDay(); // 0=CN
  const diff = day === 0 ? -6 : 1 - day;
  nd.setDate(nd.getDate() + diff);
  nd.setHours(0, 0, 0, 0);
  return nd;
}

const ngayDauTuan = computed(() => dauTuan(ngayHienTai.value));

const cacNgayTrongTuan = computed(() => {
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(ngayDauTuan.value);
    d.setDate(d.getDate() + i);
    return d;
  });
});

const NHAN_TUAN = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];

function formatNgay(d: Date) {
  return `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}`;
}

function formatTuanHienThi() {
  const dau = cacNgayTrongTuan.value[0];
  const cuoi = cacNgayTrongTuan.value[6];
  const format = (d: Date) =>
    `${String(d.getDate()).padStart(2, "0")} tháng ${d.getMonth() + 1}, ${d.getFullYear()}`;
  return `${format(dau)} – ${format(cuoi)}`;
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

function homNay() {
  ngayHienTai.value = new Date();
}

// ───────── Nhân viên & lịch ─────────
type CaLam = CaLamId | null;

interface NhanVien {
  id: string;
  ten: string;
  vieTat: string;
  chucVu: string;       // tenVaiTro từ BE
  vaiTro: number;       // 1=Admin, 2=Nhân viên
  hinhAnh: string;
  mauNen: string;
  lich: CaLam[];        // index 0=T2 ... 6=CN
  tongGio: number;
  overtime: number;
  gioiHanOT: number;
}

// Màu avatar theo vai trò
const MAU_VAI_TRO: Record<number, string> = {
  1: "bg-primary",
  2: "bg-emerald-500",
};
function mauNenNV(vaiTro: number) {
  return MAU_VAI_TRO[vaiTro] ?? "bg-slate-400";
}

// Tạo viết tắt từ họ tên
function taoVietTat(hoTen: string) {
  const parts = (hoTen ?? "").trim().split(/\s+/);
  if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
  return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

// Lịch demo theo vai trò (lưu vì BE chưa có bảng lịch)
function taoLichMock(vaiTro: number): CaLam[] {
  if (vaiTro === 1) return ["sang", "sang", "sang", null, "sang", null, null];
  if (vaiTro === 2) return [null, "chieu", "chieu", "chieu", null, "chieu", null];
  return ["toi", "toi", null, "toi", null, null, "toi"];
}

const dangTai = ref(false);
const loiTrang = ref("");
const danhSachNV = ref<NhanVien[]>([]);

function formatISODate(d: Date) {
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

async function taiDuLieuLich() {
  if (danhSachNV.value.length === 0) return;
  const tuNgay = formatISODate(cacNgayTrongTuan.value[0]);
  const denNgay = formatISODate(cacNgayTrongTuan.value[6]);
  try {
    const lichData = await layLichLamViec(tuNgay, denNgay);
    danhSachNV.value.forEach(nv => {
      nv.lich = cacNgayTrongTuan.value.map(date => {
        const dateStr = formatISODate(date);
        const item = lichData.find(
          (l: any) => String(l.nhanVienId) === String(nv.id) && l.ngay === dateStr
        );
        return item ? (item.ca as CaLam) : null;
      });
      const countCa = nv.lich.filter(c => c !== null).length;
      nv.tongGio = countCa * 4;
      nv.overtime = nv.tongGio > 20 ? nv.tongGio - 20 : 0;
    });
  } catch (e) {
    console.error(e);
    showError(getDisplayErrorMessage(e, "Không thể tải dữ liệu lịch làm việc"));
  }
}

async function taiNhanVien() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const ds = await layDanhSachNhanVien({ trangThai: 1 });
    danhSachNV.value = ds.map((nv: any) => ({
      id: String(nv.id),
      ten: nv.hoTen ?? "",
      vieTat: taoVietTat(nv.hoTen ?? ""),
      chucVu: nv.tenVaiTro ?? "—",
      vaiTro: Number(nv.vaiTro) === 1 ? 1 : 2,
      hinhAnh: nv.hinhAnh ?? "",
      mauNen: mauNenNV(Number(nv.vaiTro) === 1 ? 1 : 2),
      lich: Array(7).fill(null),
      tongGio: 0,
      overtime: 0,
      gioiHanOT: 5,
    }));
    await taiDuLieuLich();
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải danh sách nhân viên");
  } finally {
    dangTai.value = false;
  }
}

onMounted(taiNhanVien);

watch(ngayDauTuan, async () => {
  dangTai.value = true;
  await taiDuLieuLich();
  dangTai.value = false;
});

// ───────── Bộ lọc vai trò ─────────
const boLocVaiTro = ref(0); // 0 = tất cả
const dsVaiTro = [
  { value: 0, label: "Tất cả" },
  { value: 1, label: "Admin" },
  { value: 2, label: "Nhân viên" },
];

const employeeIdFilter = computed(() => route.params.id ? String(route.params.id) : null);

const danhSachLocVaiTro = computed(() => {
  let list = danhSachNV.value;
  if (employeeIdFilter.value) {
    list = list.filter(nv => nv.id === employeeIdFilter.value);
  }
  if (boLocVaiTro.value === 0) {
    return list;
  }
  return list.filter(nv => nv.vaiTro === boLocVaiTro.value);
});

// ───────── Phân trang ─────────
const soTrang = ref(5);
const trangHienTai = ref(1);
const tongNV = computed(() => danhSachLocVaiTro.value.length);
const tongSoTrang = computed(() => Math.ceil(tongNV.value / soTrang.value) || 1);
const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soTrang.value;
  return danhSachLocVaiTro.value.slice(start, start + soTrang.value);
});

// ───────── Hiển thị modal thêm ca ─────────
const showModalThemCa = ref(false);
const modalNV = ref<NhanVien | null>(null);
const modalNgayIndex = ref<number>(-1);
const modalCaChon = ref<CaLam>(null);
const chonNhanVienId = ref("");
const chonNgayVal = ref("");

function layNgayDangChon() {
  if (modalNV.value && modalNgayIndex.value >= 0) {
    return formatISODate(cacNgayTrongTuan.value[modalNgayIndex.value]);
  }
  return chonNgayVal.value;
}

function demNhanVienTrongCa(ca: CaLamId) {
  const ngayStr = layNgayDangChon();
  const ngayIndex = cacNgayTrongTuan.value.findIndex(ngay => formatISODate(ngay) === ngayStr);
  if (ngayIndex < 0) return 0;
  return danhSachNV.value.filter(nv => nv.lich[ngayIndex] === ca).length;
}

function laCaHienTaiCuaModal(ca: CaLamId) {
  if (modalNV.value && modalNgayIndex.value >= 0) {
    return modalNV.value.lich[modalNgayIndex.value] === ca;
  }
  const ngayStr = chonNgayVal.value;
  const ngayIndex = cacNgayTrongTuan.value.findIndex(ngay => formatISODate(ngay) === ngayStr);
  const nhanVien = danhSachNV.value.find(nv => nv.id === chonNhanVienId.value);
  return Boolean(nhanVien && ngayIndex >= 0 && nhanVien.lich[ngayIndex] === ca);
}

function caDaDay(ca: CaLamId) {
  return demNhanVienTrongCa(ca) >= MAX_NHAN_VIEN_MOI_CA && !laCaHienTaiCuaModal(ca);
}

function moModalThemCa(nv: NhanVien | null, ngayIdx: number) {
  modalNV.value = nv;
  modalNgayIndex.value = ngayIdx;
  if (nv && ngayIdx >= 0) {
    modalCaChon.value = nv.lich[ngayIdx];
  } else {
    modalCaChon.value = "sang";
    chonNhanVienId.value = danhSachNV.value[0]?.id || "";
    chonNgayVal.value = formatISODate(cacNgayTrongTuan.value[0]);
  }
  showModalThemCa.value = true;
}

async function luuCa() {
  let nvId: string;
  let ngayStr: string;
  if (modalNV.value && modalNgayIndex.value >= 0) {
    nvId = modalNV.value.id;
    ngayStr = formatISODate(cacNgayTrongTuan.value[modalNgayIndex.value]);
  } else {
    if (!chonNhanVienId.value || !chonNgayVal.value) {
      showError("Vui lòng chọn nhân viên và ngày làm việc!");
      return;
    }
    nvId = chonNhanVienId.value;
    ngayStr = chonNgayVal.value;
  }
  if (modalCaChon.value && caDaDay(modalCaChon.value)) {
    showError(`Ca này đã đủ tối đa ${MAX_NHAN_VIEN_MOI_CA} nhân viên.`);
    return;
  }
  dangTai.value = true;
  try {
    await phanCa({
      nhanVienId: nvId,
      ngay: ngayStr,
      ca: modalCaChon.value,
    });
    showSuccess("Cập nhật ca làm việc thành công!");
    showModalThemCa.value = false;
    await taiDuLieuLich();
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể lưu ca làm việc"));
  } finally {
    dangTai.value = false;
  }
}

async function xoaCa() {
  if (!modalNV.value || modalNgayIndex.value < 0) return;
  const xacNhan = await showConfirm(
    `Bạn có chắc chắn muốn xóa ca làm việc của ${modalNV.value.ten} ngày ${formatNgay(cacNgayTrongTuan.value[modalNgayIndex.value])}?`,
    "Xác nhận xóa ca"
  );
  if (!xacNhan) return;
  dangTai.value = true;
  try {
    await phanCa({
      nhanVienId: modalNV.value.id,
      ngay: formatISODate(cacNgayTrongTuan.value[modalNgayIndex.value]),
      ca: null,
    });
    showSuccess("Xóa ca làm việc thành công!");
    showModalThemCa.value = false;
    await taiDuLieuLich();
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể xóa ca làm việc"));
  } finally {
    dangTai.value = false;
  }
}

async function xepCaDong() {
  const tuNgay = formatISODate(cacNgayTrongTuan.value[0]);
  const denNgay = formatISODate(cacNgayTrongTuan.value[6]);
  const xacNhan = await showConfirm(
    `Bạn có chắc muốn tự động xếp ca cho tuần từ ngày ${formatNgay(cacNgayTrongTuan.value[0])} đến ${formatNgay(cacNgayTrongTuan.value[6])}? Các ca làm hiện tại trong tuần này sẽ bị ghi đè.`,
    "Xác nhận xếp ca tự động"
  );
  if (!xacNhan) return;
  dangTai.value = true;
  try {
    await xepCaTuDong(tuNgay, denNgay);
    showSuccess("Xếp ca tự động thành công!");
    await taiDuLieuLich();
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể xếp ca tự động"));
  } finally {
    dangTai.value = false;
  }
}

function tenCaXuatExcel(ca: CaLam) {
  const thongTinCa = layThongTinCa(ca);
  return thongTinCa ? `${thongTinCa.nhan} (${thongTinCa.gio})` : "Nghỉ";
}

function tenFileXuatExcel() {
  const tuNgay = formatISODate(cacNgayTrongTuan.value[0]);
  const denNgay = formatISODate(cacNgayTrongTuan.value[6]);
  return `lich-lam-viec-${tuNgay}_den_${denNgay}.xls`;
}

function xuatExcel() {
  const rows = danhSachLocVaiTro.value;
  if (!rows.length) {
    showError("Không có dữ liệu để xuất Excel.");
    return;
  }

  const exported = exportRowsToExcel({
    filename: tenFileXuatExcel(),
    sheetName: "Lịch làm việc",
    columns: [
      { label: "STT", value: (_row: NhanVien, index: number) => index + 1 },
      { label: "Nhân viên", value: (row: NhanVien) => row.ten },
      { label: "Vai trò", value: (row: NhanVien) => row.chucVu },
      ...cacNgayTrongTuan.value.map((ngay, index) => ({
        label: `${NHAN_TUAN[index]} ${formatNgay(ngay)}`,
        value: (row: NhanVien) => tenCaXuatExcel(row.lich[index]),
      })),
      { label: "Tổng giờ", value: (row: NhanVien) => `${row.tongGio}h` },
      { label: "Tăng ca", value: (row: NhanVien) => `${row.overtime}h / ${row.gioiHanOT}h` },
    ],
    rows,
  });

  if (exported) {
    showSuccess("Xuất Excel thành công!");
  } else {
    showError("Không có dữ liệu để xuất Excel.");
  }
}

// ───────── Helpers ─────────
function layThongTinCa(id: CaLam) {
  return id ? DS_CA.find(c => c.id === id) : null;
}

function mauOvertimeBar(nv: NhanVien) {
  const pct = nv.overtime / nv.gioiHanOT;
  if (pct >= 0.9) return "bg-rose-500";
  if (pct >= 0.5) return "bg-orange-400";
  return "bg-emerald-500";
}

function phanTramOT(nv: NhanVien) {
  return Math.min((nv.overtime / nv.gioiHanOT) * 100, 100);
}

const nvTruc = computed(() => danhSachNV.value.filter(nv => nv.lich.some(c => c !== null)).length);
const caUnassigned = computed(() => danhSachNV.value.filter(nv => nv.lich.every(c => c === null)).length);
</script>

<template>
  <div class="space-y-5">

    <!-- ───── HEADER ───── -->
    <section class="flex flex-wrap items-center gap-3 border-b border-slate-100 pb-4">
      <button
        @click="router.push({ name: 'admin-nhan-vien' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>

      <div class="flex-1">
        <p class="text-sm text-slate-500">Phân ca và theo dõi giờ làm cho nhân viên</p>
      </div>

      <!-- Tuần hiển thị -->
      <div class="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm">
        <CalendarDays class="h-4 w-4 text-slate-400" />
        {{ tieuDeNgay }}
      </div>

      <button @click="xepTuDong" class="admin-btn-soft gap-2">
        <Sparkles class="h-4 w-4" /> Xếp ca tự động
      </button>
      <button @click="moModal(toanBoNgay[0], 'sang')" class="admin-btn-primary gap-2">
        <UserPlus class="h-4 w-4" /> Thêm ca mới
      </button>
    </section>

    <!-- <div class="grid gap-5">
     
      <section class="schedule-board rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
  
        <div class="mb-5 flex flex-wrap items-center gap-3">
          <h2 class="flex-1 text-base font-bold text-slate-800">Bảng lịch làm việc theo tuần</h2>

          <button @click="() => tuanOffset--"
            class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
            <ChevronLeft class="h-4 w-4" />
          </button>
          <button @click="() => tuanOffset = 0"
            class="rounded-xl bg-primary px-4 py-1.5 text-sm font-semibold text-white transition hover:bg-primary-hover shadow-sm">
            Hôm nay
          </button>
          <button @click="() => tuanOffset++"
            class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
            <ChevronRight class="h-4 w-4" />
          </button>
        </div>

        <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">
          {{ loiTrang }}
        </div>

        
      </section>
    </div> -->

    <!-- ───── MODAL THÊM / SỬA CA ───── -->
    <!-- ── Header ── -->
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div class="flex items-center gap-3">
        <button type="button"
          class="flex h-9 w-9 shrink-0 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 shadow-sm transition hover:bg-slate-50 hover:text-slate-800"
          @click="quayLai">
          <ArrowLeft class="h-4 w-4" />
        </button>
        <div>
          <h1 class="text-lg font-bold text-slate-800">
            {{
              employeeId ? "Lịch làm việc nhân viên" : "Quản lý lịch làm việc"
            }}
          </h1>
          <p class="text-sm text-slate-400">
            Tuần: {{ tieuDeNgay }} &nbsp;·&nbsp; Tối đa {{ MAX_PER_SHIFT }} nhân
            viên / ca
          </p>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <Button variant="soft" :loading="dangXepTuDong" @click="xepTuDong">
          <template #prefix>
            <Sparkles class="h-4 w-4" />
          </template>
          Xếp tự động
        </Button>
        <Button variant="soft" :loading="dangTai" @click="taiLichLam">
          <template #prefix>
            <RefreshCw class="h-4 w-4" />
          </template>
          Làm mới
        </Button>
      </div>
    </div>

    <!-- ── Điều hướng tuần ── -->
    <Card>
      <div class="flex items-center justify-between py-1">
        <button type="button"
          class="flex h-9 w-9 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 shadow-sm transition hover:bg-slate-50 hover:text-slate-800"
          @click="tuanOffset--">
          <ChevronLeft class="h-4 w-4" />
        </button>

        <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
          <CalendarDays class="h-4 w-4 text-violet-500" />
          Tuần {{ tieuDeNgay }}
        </div>

        <button type="button"
          class="flex h-9 w-9 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 shadow-sm transition hover:bg-slate-50 hover:text-slate-800"
          @click="tuanOffset++">
          <ChevronRight class="h-4 w-4" />
        </button>
      </div>
    </Card>

    <!-- ── Lỗi ── -->
    <div v-if="loiTrang" class="rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">
      {{ loiTrang }}
    </div>

    <!-- ── Skeleton loading ── -->
    <div v-if="dangTai" class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-7">
      <div v-for="i in 7" :key="i" class="h-72 animate-pulse rounded-3xl bg-slate-100"></div>
    </div>

    <!-- ── Lưới lịch 7 cột (T2–CN) ── -->
    <div v-else class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-7">
      <div v-for="(ngay, dayIdx) in toanBoNgay" :key="dayIdx"
        class="rounded-2xl border bg-white p-2 shadow-sm transition" :class="isToday(ngay)
            ? 'border-violet-300 ring-2 ring-violet-100'
            : 'border-slate-100'
          ">
        <!-- Header ngày -->
        <div class="mb-3 flex items-center justify-between">
          <span class="text-xs font-bold uppercase tracking-wider text-slate-400">
            {{ DAY_LABELS[dayIdx] }}
          </span>
          <span class="flex h-7 w-7 items-center justify-center rounded-full text-xs font-bold" :class="isToday(ngay)
              ? 'bg-violet-500 text-white'
              : 'bg-slate-100 text-slate-600'
            ">
            {{ ngay.getDate() }}
          </span>
        </div>

        <!-- 3 ca -->
        <div class="space-y-2">
          <div v-for="ca in CA_LIST" :key="ca.key" class="rounded-2xl border p-2" :class="ca.color">
            <!-- Header ca -->
            <div class="mb-1.5 flex items-center justify-between gap-1">
              <div class="min-w-0">
                <p class="text-[11px] font-bold leading-tight">
                  {{ ca.label }}
                </p>
                <p class="text-[10px] opacity-60">{{ ca.time }}</p>
              </div>
              <!-- Badge số lượng -->
              <span class="shrink-0 rounded-full px-1.5 py-0.5 text-[10px] font-bold" :class="soNhanVienTrongCa(ngay, ca.key) >= MAX_PER_SHIFT
                  ? 'bg-current/20 opacity-90'
                  : 'bg-current/10 opacity-70'
                ">
                {{ soNhanVienTrongCa(ngay, ca.key) }}/{{ MAX_PER_SHIFT }}
              </span>
            </div>

            <div class="flex flex-col gap-1">
              <label class="text-xs font-bold text-slate-500">Ngày làm việc</label>
              <select
                v-model="chonNgayVal"
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-primary/50 focus:ring-2 focus:ring-primary/10 transition"
              >
                <option
                  v-for="(ngay, idx) in cacNgayTrongTuan"
                  :key="idx"
                  :value="formatISODate(ngay)"
                >
                  {{ NHAN_TUAN[idx] }} ({{ formatNgay(ngay) }})
                </option>
              </select>
            </div>
          </div>

          <div class="space-y-2.5">
            <button
              v-for="ca in DS_CA"
              :key="ca.id"
              :disabled="caDaDay(ca.id)"
              :title="caDaDay(ca.id) ? `Ca ${ca.nhan} đã đủ tối đa ${MAX_NHAN_VIEN_MOI_CA} nhân viên` : ''"
              @click="modalCaChon = ca.id"
              :class="['flex w-full items-center gap-3 rounded-2xl border-2 px-4 py-3 text-sm font-semibold transition',
                modalCaChon === ca.id ? 'border-primary/50 bg-primary-light text-primary' : 'border-slate-200 hover:border-slate-300',
                caDaDay(ca.id) ? 'cursor-not-allowed opacity-50 hover:border-slate-200' : '']"
            >
              <div :class="['h-4 w-4 rounded-sm', ca.mau]" />
              <span>{{ ca.nhan }}</span>
              <span class="ml-auto text-right text-slate-400">{{ demNhanVienTrongCa(ca.id) }}/{{ MAX_NHAN_VIEN_MOI_CA }} - {{ ca.gio }}</span>
            </button>
            <button
              @click="modalCaChon = null"
              :class="['flex w-full items-center gap-3 rounded-2xl border-2 px-4 py-3 text-sm font-semibold transition',
                modalCaChon === null ? 'border-slate-400 bg-slate-50' : 'border-slate-200 hover:border-slate-300']"
            >
              <div class="h-4 w-4 rounded-sm border-2 border-dashed border-slate-300" />
              <span class="text-slate-500">Không có ca (nghỉ)</span>
            </button>
          </div>

          <div class="mt-5 flex gap-3">
            <button
              v-if="modalNV && modalNV.lich[modalNgayIndex]"
              @click="xoaCa"
              class="rounded-2xl border border-rose-200 px-4 py-2.5 text-sm font-semibold text-rose-500 hover:bg-rose-50 transition"
            >
              Xóa ca
            </button>
            <button @click="showModalThemCa = false" class="flex-1 rounded-2xl border border-slate-200 py-2.5 text-sm font-semibold text-slate-500 hover:bg-slate-50 transition">
              Hủy
            </button>
            <button @click="luuCa" class="flex-1 rounded-2xl bg-primary py-2.5 text-sm font-bold text-white hover:bg-primary-hover transition shadow-sm">
              Lưu ca
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
