<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
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
  X,
  Info
} from "lucide-vue-next";
import { layDanhSachNhanVien } from "../../../services/nhan-vien.js";
import {
  layLichLamViec,
  phanCa,
  xepCaTuDong,
} from "../../../services/lich-lam.js";
import { showSuccess, showError, showConfirm } from "../../../utils/alert.js";
import { getDisplayErrorMessage } from "../../../utils/error-message.js";
import { exportRowsToExcel } from "../../../utils/export-excel.js";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import { useAdminSession } from "../../../composable/useAdminSession.js";
import LichLamViecNhanVien from "./LichLamViecNhanVien.vue";

const route = useRoute();
const router = useRouter();

const { adminSession } = useAdminSession();
const laAdmin = computed(() => adminSession.value.vaiTro === "Quản trị viên" || adminSession.value.vaiTro === "Admin");

const MAX_NHAN_VIEN_MOI_CA = 3;

const DS_CA = [
  {
    id: "sang",
    nhan: "Sáng",
    gio: "08:00 - 12:00",
    mau: "bg-emerald-500",
    muaNhat: "bg-emerald-50 border-emerald-200 text-emerald-700",
  },
  {
    id: "chieu",
    nhan: "Chiều",
    gio: "13:00 - 17:00",
    mau: "bg-orange-400",
    muaNhat: "bg-orange-50 border-orange-200 text-orange-700",
  },
  {
    id: "toi",
    nhan: "Tối",
    gio: "18:00 - 22:00",
    mau: "bg-violet-400",
    muaNhat: "bg-violet-50 border-violet-200 text-violet-700",
  },
];

// ───────── Tuần hiện tại ─────────
const ngayHienTai = ref(new Date());

function dauTuan(d) {
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

function formatNgay(d) {
  return `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}`;
}

function formatTuanHienThi() {
  const dau = cacNgayTrongTuan.value[0];
  const cuoi = cacNgayTrongTuan.value[6];
  const format = (d) =>
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

const PALETTE = [
  "bg-emerald-500", "bg-rose-500", "bg-blue-500", "bg-amber-500", 
  "bg-violet-500", "bg-teal-500", "bg-indigo-500", "bg-orange-500",
  "bg-pink-500", "bg-cyan-500"
];
function mauNenNV(hoTen) {
  if (!hoTen) return "bg-slate-400";
  let sum = 0;
  for (let i = 0; i < hoTen.length; i++) {
    sum += hoTen.charCodeAt(i);
  }
  return PALETTE[sum % PALETTE.length];
}

// Tạo viết tắt từ họ tên
function taoVietTat(hoTen) {
  const parts = (hoTen ?? "").trim().split(/\s+/);
  if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
  return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

// Lịch demo theo vai trò (lưu vì BE chưa có bảng lịch)
function taoLichMock(vaiTro) {
  if (vaiTro === 1) return ["sang", "sang", "sang", null, "sang", null, null];
  if (vaiTro === 2)
    return [null, "chieu", "chieu", "chieu", null, "chieu", null];
  return ["toi", "toi", null, "toi", null, null, "toi"];
}

const dangTai = ref(false);
const loiTrang = ref("");
const danhSachNV = ref([]);

function formatISODate(d) {
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
    danhSachNV.value.forEach((nv) => {
      nv.lich = cacNgayTrongTuan.value.map((date) => {
        const dateStr = formatISODate(date);
        const item = lichData.find(
          (l) => String(l.nhanVienId) === String(nv.id) && l.ngay === dateStr,
        );
        return item ? item.ca : null;
      });
      const countCa = nv.lich.filter((c) => c !== null).length;
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
    danhSachNV.value = ds.map((nv) => ({
      id: String(nv.id),
      ten: nv.hoTen ?? "",
      vieTat: taoVietTat(nv.hoTen ?? ""),
      chucVu: nv.tenVaiTro ?? "—",
      vaiTro: Number(nv.vaiTro) === 1 ? 1 : 2,
      hinhAnh: nv.hinhAnh ?? "",
      mauNen: mauNenNV(nv.hoTen ?? ""),
      lich: Array(7).fill(null),
      tongGio: 0,
      overtime: 0,
      gioiHanOT: 5,
    }));
    await taiDuLieuLich();
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(
      e,
      "Không thể tải danh sách nhân viên",
    );
  } finally {
    dangTai.value = false;
  }
}

onMounted(() => {
  if (laAdmin.value) {
    taiNhanVien();
  }
});

watch(ngayDauTuan, async () => {
  if (laAdmin.value) {
    dangTai.value = true;
    await taiDuLieuLich();
    dangTai.value = false;
  }
});

// ───────── Bộ lọc vai trò ─────────
const boLocVaiTro = ref(0); // 0 = tất cả
const dsVaiTro = [
  { value: 0, label: "Tất cả" },
  { value: 1, label: "Admin" },
  { value: 2, label: "Nhân viên" },
];

const employeeIdFilter = computed(() =>
  route.params.id ? String(route.params.id) : null,
);

const danhSachLocVaiTro = computed(() => {
  let list = danhSachNV.value;
  if (employeeIdFilter.value) {
    list = list.filter((nv) => nv.id === employeeIdFilter.value);
  }
  if (boLocVaiTro.value === 0) {
    return list;
  }
  return list.filter((nv) => nv.vaiTro === boLocVaiTro.value);
});

// ───────── Phân trang ─────────
const soTrang = ref(5);
const trangHienTai = ref(1);
const tongNV = computed(() => danhSachLocVaiTro.value.length);
const tongSoTrang = computed(
  () => Math.ceil(tongNV.value / soTrang.value) || 1,
);
const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soTrang.value;
  return danhSachLocVaiTro.value.slice(start, start + soTrang.value);
});

// ───────── Hiển thị modal thêm ca ─────────
const showModalThemCa = ref(false);
const modalNV = ref(null);
const modalNgayIndex = ref(-1);
const modalCaChon = ref(null);
const chonNhanVienId = ref("");
const chonNgayVal = ref("");

function layNgayDangChon() {
  if (modalNV.value && modalNgayIndex.value >= 0) {
    return formatISODate(cacNgayTrongTuan.value[modalNgayIndex.value]);
  }
  return chonNgayVal.value;
}

function demNhanVienTrongCa(ca) {
  const ngayStr = layNgayDangChon();
  const ngayIndex = cacNgayTrongTuan.value.findIndex(
    (ngay) => formatISODate(ngay) === ngayStr,
  );
  if (ngayIndex < 0) return 0;
  return danhSachNV.value.filter((nv) => nv.lich[ngayIndex] === ca).length;
}

function laCaHienTaiCuaModal(ca) {
  if (modalNV.value && modalNgayIndex.value >= 0) {
    return modalNV.value.lich[modalNgayIndex.value] === ca;
  }
  const ngayStr = chonNgayVal.value;
  const ngayIndex = cacNgayTrongTuan.value.findIndex(
    (ngay) => formatISODate(ngay) === ngayStr,
  );
  const nhanVien = danhSachNV.value.find(
    (nv) => nv.id === chonNhanVienId.value,
  );
  return Boolean(nhanVien && ngayIndex >= 0 && nhanVien.lich[ngayIndex] === ca);
}

function caDaDay(ca) {
  return (
    demNhanVienTrongCa(ca) >= MAX_NHAN_VIEN_MOI_CA && !laCaHienTaiCuaModal(ca)
  );
}

function laNgayQuaKhu(d) {
  if (!d) return false;
  const date = new Date(d);
  date.setHours(0, 0, 0, 0);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date < today;
}

function moModalThemCa(nv, ngayIdx) {
  if (ngayIdx >= 0 && laNgayQuaKhu(cacNgayTrongTuan.value[ngayIdx])) {
    showError("Không thể chỉnh sửa ca làm việc trong quá khứ!");
    return;
  }
  modalNV.value = nv;
  modalNgayIndex.value = ngayIdx;
  if (nv && ngayIdx >= 0) {
    modalCaChon.value = nv.lich[ngayIdx];
  } else {
    modalCaChon.value = "sang";
    chonNhanVienId.value = danhSachNV.value[0]?.id || "";
    const firstAvailableDay = cacNgayTrongTuan.value.find(d => !laNgayQuaKhu(d)) || cacNgayTrongTuan.value[0];
    chonNgayVal.value = formatISODate(firstAvailableDay);
  }
  showModalThemCa.value = true;
}

// ───────── Modal Thêm Nhân Viên Mới ─────────
const showModalThemNV = ref(false);
const modalThemNVCa = ref(null);
const modalThemNVNgayIdx = ref(-1);
const modalThemNVNhanVienId = ref("");

function moModalThemNhanVien(caId, ngayIdx) {
  if (ngayIdx >= 0 && laNgayQuaKhu(cacNgayTrongTuan.value[ngayIdx])) {
    showError("Không thể thêm nhân viên vào ca trong quá khứ!");
    return;
  }
  modalThemNVCa.value = caId;
  modalThemNVNgayIdx.value = ngayIdx;
  modalThemNVNhanVienId.value = "";
  showModalThemNV.value = true;
}

const danhSachNhanVienRanh = computed(() => {
  if (modalThemNVNgayIdx.value < 0) return [];
  return danhSachNV.value.filter(nv => !nv.lich[modalThemNVNgayIdx.value]);
});

async function xacNhanThemNV() {
  if (!modalThemNVNhanVienId.value) return;
  dangTai.value = true;
  try {
    await phanCa({
      nhanVienId: modalThemNVNhanVienId.value,
      ngay: formatISODate(cacNgayTrongTuan.value[modalThemNVNgayIdx.value]),
      ca: modalThemNVCa.value,
    });
    showSuccess("Thêm nhân viên vào ca thành công!");
    showModalThemNV.value = false;
    await taiDuLieuLich();
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể lưu ca làm việc"));
  } finally {
    dangTai.value = false;
  }
}

async function luuCa() {
  let nvId;
  let ngayStr;
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

  if (laNgayQuaKhu(new Date(ngayStr))) {
    showError("Không thể thêm hoặc sửa ca làm việc trong quá khứ!");
    return;
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
    `Bạn có chắc chắn muốn xóa ca làm việc của ${modalNV.value.hoTen} ngày ${formatNgay(cacNgayTrongTuan.value[modalNgayIndex.value])}?`,
    "Xác nhận xóa ca",
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
    "Xác nhận xếp ca tự động",
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

function tenCaXuatExcel(ca) {
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
      { label: "STT", value: (_row, index) => index + 1 },
      { label: "Nhân viên", value: (row) => row.hoTen },
      { label: "Vai trò", value: (row) => row.chucVu },
      ...cacNgayTrongTuan.value.map((ngay, index) => ({
        label: `${NHAN_TUAN[index]} ${formatNgay(ngay)}`,
        value: (row) => tenCaXuatExcel(row.lich[index]),
      })),
      { label: "Tổng giờ", value: (row) => `${row.tongGio}h` },
      {
        label: "Tăng ca",
        value: (row) => `${row.overtime}h / ${row.gioiHanOT}h`,
      },
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
function layThongTinCa(id) {
  return id ? DS_CA.find((c) => c.id === id) : null;
}

function mauOvertimeBar(nv) {
  const pct = nv.overtime / nv.gioiHanOT;
  if (pct >= 0.9) return "bg-rose-500";
  if (pct >= 0.5) return "bg-orange-400";
  return "bg-emerald-500";
}

function phanTramOT(nv) {
  return Math.min((nv.overtime / nv.gioiHanOT) * 100, 100);
}

const nvTruc = computed(
  () => danhSachNV.value.filter((nv) => nv.lich.some((c) => c !== null)).length,
);
const caUnassigned = computed(
  () =>
    danhSachNV.value.filter((nv) => nv.lich.every((c) => c === null)).length,
);

// Đếm NV trong ca theo ngày (dùng cho grid calendar)
function soNhanVienTrongCaTheoNgay(ngayIdx, caId) {
  return danhSachNV.value.filter((nv) => nv.lich[ngayIdx] === caId).length;
}

// Danh sách NV trong ca theo ngày
function nhanVienTrongCaTheoNgay(ngayIdx, caId) {
  return danhSachNV.value.filter((nv) => nv.lich[ngayIdx] === caId);
}

function isToday(date) {
  return date.toDateString() === new Date().toDateString();
}
</script>

<template>
  <LichLamViecNhanVien v-if="!laAdmin" />
  
  <div v-else class="schedule-page space-y-5">

    <!-- ───── HEADER ───── -->
    <section class="flex flex-wrap items-center gap-3 border-b border-slate-100 pb-4">
      <div class="flex-1">
        <h1 class="text-2xl font-bold text-slate-800">Quản lý lịch làm việc</h1>
        <p class="text-sm text-slate-500 mt-1">Phân ca và theo dõi giờ làm cho nhân viên</p>
      </div>
      <div class="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-600 shadow-sm">
        <CalendarDays class="h-4 w-4 text-slate-400" />
        {{ formatTuanHienThi() }}
      </div>
      <button v-if="laAdmin" @click="xepCaDong" class="flex items-center gap-2 rounded-2xl border border-[#CC0000] px-4 py-2 text-sm font-semibold text-[#CC0000] hover:bg-red-50 transition">
        <Shuffle class="h-4 w-4" /> Xếp ca tự động
      </button>
      <button v-if="laAdmin" @click="moModalThemCa(null, -1)" class="flex items-center gap-2 rounded-2xl bg-[#CC0000] px-4 py-2 text-sm font-semibold text-white hover:bg-red-700 shadow-sm transition">
        <Plus class="h-4 w-4" /> Thêm ca mới
      </button>
    </section>

    <!-- ───── CONTENT: calendar + sidebar ───── -->
    <div class="grid gap-5 2xl:grid-cols-[minmax(0,1fr)_300px]">

      <!-- ── Lưới lịch 7 cột ── -->
      <section class="rounded-[24px] border border-slate-200 bg-white p-4 shadow-sm">

        <!-- Nav tuần -->
        <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
          <div class="flex items-center gap-2 text-sm text-slate-500">
            <span class="font-medium">Vai trò:</span>
            <select v-model="boLocVaiTro" class="rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-sm outline-none">
              <option v-for="vt in dsVaiTro" :key="vt.value" :value="vt.value">{{ vt.label }}</option>
            </select>
          </div>
          
          <div class="flex items-center gap-2 text-sm font-semibold text-slate-600">
            <CalendarDays class="h-4 w-4 text-violet-400" />
            Tuần {{ formatTuanHienThi() }}
          </div>

          <div class="flex items-center gap-2">
            <button @click="tuanTruoc" class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
              <ChevronLeft class="h-4 w-4" />
            </button>
            <button @click="homNay" class="rounded-xl bg-[#CC0000] px-4 py-1.5 text-sm font-semibold text-white hover:bg-red-700 shadow-sm transition">
              Hôm nay
            </button>
            <button @click="tuanSau" class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
              <ChevronRight class="h-4 w-4" />
            </button>
          </div>
        </div>

        <div v-if="loiTrang" class="mb-3 rounded-2xl bg-rose-50 px-4 py-2 text-sm text-rose-600">{{ loiTrang }}</div>

        <!-- Skeleton -->
        <div v-if="dangTai" class="grid grid-cols-7 gap-2">
          <div v-for="i in 7" :key="i" class="h-64 animate-pulse rounded-2xl bg-slate-100" />
        </div>

        <!-- Grid 7 cột -->
        <div v-else class="grid grid-cols-7 gap-2 overflow-x-auto">
          <div
            v-for="(ngay, dayIdx) in cacNgayTrongTuan"
            :key="dayIdx"
            class="min-w-[110px] rounded-2xl border bg-white shadow-sm transition"
            :class="isToday(ngay) ? 'border-violet-300 ring-2 ring-violet-100' : 'border-slate-100'"
          >
            <!-- Header ngày -->
            <div class="flex items-center justify-between px-2 pt-2 pb-1">
              <span class="text-[10px] font-bold uppercase tracking-wider text-slate-400">{{ NHAN_TUAN[dayIdx] }}</span>
              <span
                class="flex h-6 w-6 items-center justify-center rounded-full text-[11px] font-bold"
                :class="isToday(ngay) ? 'bg-violet-500 text-white' : 'bg-slate-100 text-slate-600'"
              >{{ ngay.getDate() }}</span>
            </div>

            <!-- 3 Ca -->
            <div class="space-y-1.5 px-1.5 pb-2">
              <div
                v-for="ca in DS_CA"
                :key="ca.id"
                class="rounded-xl border p-1.5"
                :class="ca.muaNhat"
              >
                <!-- Header ca -->
                <div class="mb-1 flex items-center justify-between gap-1">
                  <div>
                    <p class="text-[10px] font-bold leading-tight">Ca {{ ca.nhan }}</p>
                    <p class="text-[9px] opacity-60">{{ ca.gio }}</p>
                  </div>
                  <span class="rounded-full px-1 py-0.5 text-[9px] font-bold bg-white/60">
                    {{ soNhanVienTrongCaTheoNgay(dayIdx, ca.id) }}/{{ MAX_NHAN_VIEN_MOI_CA }}
                  </span>
                </div>

                <!-- Danh sách NV trong ca -->
                <div class="space-y-0.5">
                  <div
                    v-for="nv in nhanVienTrongCaTheoNgay(dayIdx, ca.id)"
                    :key="nv.id"
                    class="flex items-center gap-1 rounded-lg bg-white/70 px-1 py-0.5 transition"
                    :class="laAdmin ? 'cursor-pointer hover:bg-white' : ''"
                    @click="laAdmin ? moModalThemCa(nv, dayIdx) : null"
                    :title="nv.ten"
                  >
                    <div :class="['h-4 w-4 shrink-0 rounded-full text-[8px] font-bold text-white flex items-center justify-center', nv.mauNen]">
                      {{ nv.vieTat }}
                    </div>
                    <span class="truncate text-[10px] font-medium text-slate-700">{{ nv.ten }}</span>
                  </div>
                </div>

                <!-- Nút thêm -->
                <button
                  v-if="laAdmin && soNhanVienTrongCaTheoNgay(dayIdx, ca.id) < MAX_NHAN_VIEN_MOI_CA"
                  @click="moModalThemNhanVien(ca.id, dayIdx)"
                  class="mt-1 flex w-full items-center justify-center gap-1 rounded-lg border border-dashed border-current py-0.5 text-[10px] opacity-50 hover:opacity-100 transition"
                >
                  <Plus class="h-2.5 w-2.5" /> Thêm
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ── Sidebar ── -->
      <aside class="space-y-4">
        <!-- Theo dõi tăng ca -->
        <div class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm min-h-full">
          <div class="mb-4 flex items-center justify-between">
            <h3 class="text-sm font-bold text-slate-800">Theo dõi tăng ca</h3>
            <MoreHorizontal class="h-4 w-4 text-slate-400" />
          </div>
          <div class="space-y-4">
            <div v-for="nv in danhSachNV" :key="nv.id" class="space-y-1.5">
              <div class="flex items-center justify-between text-sm border-b border-slate-100 pb-2">
                <span class="font-semibold text-slate-700">{{ nv.ten }}</span>
                <span :class="['text-xs font-bold', nv.overtime >= nv.gioiHanOT * 0.9 ? 'text-rose-500' : nv.overtime === 0 ? 'text-slate-400' : 'text-emerald-600']">
                  {{ nv.overtime }}h / {{ nv.gioiHanOT }}h
                </span>
              </div>
            </div>
          </div>
          <div v-if="danhSachNV.some(nv => nv.overtime >= nv.gioiHanOT * 0.9)" class="mt-4 rounded-xl bg-rose-50 px-3 py-2.5 text-xs text-rose-700">
            <span class="font-bold">Lưu ý:</span>
            {{ danhSachNV.filter(nv => nv.overtime >= nv.gioiHanOT * 0.9).map(nv => nv.ten).join(", ") }}
            sắp vượt giới hạn tăng ca.
          </div>
        </div>
      </aside>
    </div>

    <!-- ───── MODAL THÊM / SỬA CA ───── -->
    <Teleport to="body">
      <div
        v-if="showModalThemCa"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
        @click.self="showModalThemCa = false"
      >
        <div
          class="w-full max-w-sm rounded-[24px] bg-white p-6 shadow-2xl mx-4"
        >
          <h3 class="mb-1 text-base font-bold text-slate-800">
            {{
              modalNV
                ? modalNV.lich[modalNgayIndex]
                  ? "Sửa ca làm việc"
                  : "Thêm ca làm việc"
                : "Thêm ca mới"
            }}
          </h3>
          <p v-if="modalNV" class="mb-5 text-sm text-slate-400">
            {{ modalNV.ten }} – {{ NHAN_TUAN[modalNgayIndex] }}
            {{ formatNgay(cacNgayTrongTuan[modalNgayIndex]) }}
          </p>
          <p v-else class="mb-3 text-sm text-slate-400">
            Chọn nhân viên, ngày và ca làm việc cần thêm.
          </p>

          <!-- Thêm dropdown chọn nhân viên và ngày nếu modalNV là null -->
          <div v-if="!modalNV" class="mb-4 space-y-3">
            <div class="flex flex-col gap-1">
              <label class="text-xs font-bold text-slate-500">Nhân viên</label>
              <select
                v-model="chonNhanVienId"
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-primary/50 focus:ring-2 focus:ring-primary/10 transition"
              >
                <option v-for="nv in danhSachNV" :key="nv.id" :value="nv.id">
                  {{ nv.ten }} ({{ nv.chucVu }})
                </option>
              </select>
            </div>

            <div class="flex flex-col gap-1">
              <label class="text-xs font-bold text-slate-500"
                >Ngày làm việc</label
              >
              <select
                v-model="chonNgayVal"
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-primary/50 focus:ring-2 focus:ring-primary/10 transition"
              >
                <option
                  v-for="(ngay, idx) in cacNgayTrongTuan"
                  :key="idx"
                  :value="formatISODate(ngay)"
                  :disabled="laNgayQuaKhu(ngay)"
                >
                  {{ NHAN_TUAN[idx] }} ({{ formatNgay(ngay) }}){{ laNgayQuaKhu(ngay) ? ' - Đã qua' : '' }}
                </option>
              </select>
            </div>
          </div>

          <div class="space-y-2.5">
            <button
              v-for="ca in DS_CA"
              :key="ca.id"
              :disabled="caDaDay(ca.id)"
              :title="
                caDaDay(ca.id)
                  ? `Ca ${ca.nhan} đã đủ tối đa ${MAX_NHAN_VIEN_MOI_CA} nhân viên`
                  : ''
              "
              @click="modalCaChon = ca.id"
              :class="[
                'flex w-full items-center gap-3 rounded-2xl border-2 px-4 py-3 text-sm font-semibold transition',
                modalCaChon === ca.id
                  ? 'border-primary/50 bg-primary-light text-primary'
                  : 'border-slate-200 hover:border-slate-300',
                caDaDay(ca.id)
                  ? 'cursor-not-allowed opacity-50 hover:border-slate-200'
                  : '',
              ]"
            >
              <div :class="['h-4 w-4 rounded-sm', ca.mau]" />
              <span>{{ ca.nhan }}</span>
              <span class="ml-auto text-right text-slate-400"
                >{{ demNhanVienTrongCa(ca.id) }}/{{ MAX_NHAN_VIEN_MOI_CA }} -
                {{ ca.gio }}</span
              >
            </button>
            <button
              @click="modalCaChon = null"
              :class="[
                'flex w-full items-center gap-3 rounded-2xl border-2 px-4 py-3 text-sm font-semibold transition',
                modalCaChon === null
                  ? 'border-slate-400 bg-slate-50'
                  : 'border-slate-200 hover:border-slate-300',
              ]"
            >
              <div
                class="h-4 w-4 rounded-sm border-2 border-dashed border-slate-300"
              />
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
            <button
              @click="showModalThemCa = false"
              class="flex-1 rounded-2xl border border-slate-200 py-2.5 text-sm font-semibold text-slate-500 hover:bg-slate-50 transition"
            >
              Hủy
            </button>
            <button
              @click="luuCa"
              class="flex-1 rounded-2xl bg-primary py-2.5 text-sm font-bold text-white hover:bg-primary-hover transition shadow-sm"
            >
              Lưu ca
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ───── MODAL THÊM NHÂN VIÊN VÀO CA ───── -->
    <Teleport to="body">
      <div v-if="showModalThemNV" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm" @click.self="showModalThemNV = false">
        <div class="w-full max-w-sm rounded-[24px] bg-white p-6 shadow-2xl mx-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold text-slate-800">Thêm nhân viên vào ca</h3>
            <button @click="showModalThemNV = false" class="text-slate-400 hover:text-slate-600 transition">
              <X class="h-5 w-5" />
            </button>
          </div>

          <!-- Shift details -->
          <div class="mb-4">
            <div class="flex items-center gap-2 mb-1.5">
              <div :class="['h-3 w-3 rounded-sm', layThongTinCa(modalThemNVCa)?.mau]"></div>
              <span class="font-bold text-emerald-700">Ca {{ layThongTinCa(modalThemNVCa)?.nhan }}</span>
            </div>
            <p class="text-sm font-medium text-slate-500">
              {{ layThongTinCa(modalThemNVCa)?.gio }} • {{ NHAN_TUAN[modalThemNVNgayIdx] }}, {{ formatNgay(cacNgayTrongTuan[modalThemNVNgayIdx]) }}
            </p>
          </div>

          <!-- Alert -->
          <div class="mb-5 flex items-center gap-2 rounded-xl bg-emerald-50 px-3 py-3 text-sm text-emerald-700">
            <Info class="h-4 w-4 shrink-0" />
            <span>Hiện tại: <strong>{{ soNhanVienTrongCaTheoNgay(modalThemNVNgayIdx, modalThemNVCa) }}/{{ MAX_NHAN_VIEN_MOI_CA }}</strong> nhân viên</span>
          </div>

          <!-- Employee Select -->
          <div class="mb-8 flex flex-col gap-1.5">
            <label class="text-sm font-bold text-slate-800">Nhân viên</label>
            <div class="relative">
               <select v-model="modalThemNVNhanVienId" class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3.5 text-sm font-medium text-slate-700 outline-none focus:border-emerald-500/50 focus:ring-4 focus:ring-emerald-500/10 transition appearance-none cursor-pointer">
                 <option value="" disabled>Chọn nhân viên...</option>
                 <option v-for="nv in danhSachNhanVienRanh" :key="nv.id" :value="nv.id">{{ nv.ten }} ({{ nv.chucVu }})</option>
               </select>
               <ChevronDown class="absolute right-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400 pointer-events-none" />
            </div>
          </div>

          <!-- Buttons -->
          <div class="flex gap-3">
            <button @click="showModalThemNV = false" class="flex-1 rounded-2xl border border-slate-200 py-3 text-sm font-bold text-slate-700 hover:bg-slate-50 transition">
              Hủy
            </button>
            <button @click="xacNhanThemNV" :disabled="!modalThemNVNhanVienId" class="flex-1 rounded-2xl bg-[#CC0000] py-3 text-sm font-bold text-white hover:bg-red-700 transition disabled:opacity-50">
              Thêm vào ca
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
