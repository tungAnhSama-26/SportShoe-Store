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

const route = useRoute();
const router = useRouter();

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

// Màu avatar theo vai trò
const MAU_VAI_TRO = {
  1: "bg-primary",
  2: "bg-emerald-500",
};
function mauNenNV(vaiTro) {
  return MAU_VAI_TRO[vaiTro] ?? "bg-slate-400";
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
      mauNen: mauNenNV(Number(nv.vaiTro) === 1 ? 1 : 2),
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

function moModalThemCa(nv, ngayIdx) {
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
</script>

<template>

  

  <div class="schedule-page space-y-5">

    <!-- ───── HEADER ───── -->
    <section class="flex flex-wrap items-center gap-3">
      <!-- <button
        @click="router.push({ name: 'admin-nhan-vien' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>

      <div class="flex-1">
        <h1 class="text-[26px] font-bold tracking-tight text-slate-900">
          Quản lý lịch làm việc
        </h1>
        <p class="text-sm text-slate-400">
          Phân ca và theo dõi giờ làm cho nhân viên
        </p>
      </div> -->

      <!-- Tuần hiển thị -->
      <div
        class="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm"
      >
        <CalendarDays class="h-4 w-4 text-slate-400" />
        {{ formatTuanHienThi() }}
      </div>

      <button @click="xepCaDong" class="admin-btn-soft gap-2">
        <Shuffle class="h-4 w-4" /> Xếp ca tự động
      </button>
      <button @click="xuatExcel" class="admin-btn-soft gap-2">
        <Download class="h-4 w-4" /> Xuất Excel
      </button>
      <button @click="moModalThemCa(null, -1)" class="admin-btn-primary gap-2">
        <Plus class="h-4 w-4" /> Thêm ca mới
      </button>
    </section>

    <!-- Alert when viewing a specific employee's schedule -->
    <div
      v-if="employeeIdFilter" class="flex items-center justify-between rounded-2xl bg-violet-50 p-4 text-sm font-semibold text-violet-700">
      <div class="flex items-center gap-2">
        <CalendarDays class="h-5 w-5 text-violet-500" />
        <span>Đang hiển thị lịch làm việc của nhân viên:
          <span class="font-bold text-violet-900">{{
            danhSachLocVaiTro[0]?.ten || "Đang tải..."
          }}</span></span>
      </div>
      <button
        @click="router.push({ name: 'admin-nhan-vien-lich-lam' })"
        class="text-xs bg-white hover:bg-violet-100 text-violet-700 px-3 py-1.5 rounded-xl border border-violet-200 transition shadow-sm">
        Xem tất cả nhân viên
      </button>
    </div>

    <!-- ───── CONTENT ───── -->

    <div class="grid gap-5 2xl:grid-cols-[minmax(0,1fr)_320px]">

      <!-- ── Bảng lịch ── -->
      <section class="schedule-board rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">


        <!-- Thanh điều hướng tuần -->
        <div class="mb-5 flex flex-wrap items-center gap-3">
          <h2 class="flex-1 text-base font-bold text-slate-800">
            Bảng lịch làm việc theo tuần
          </h2>

          <!-- Lọc vai trò -->
          <div class="flex items-center gap-2 text-sm text-slate-500">
            <span class="font-medium">Vai trò:</span>
            <select
              v-model="boLocVaiTro"
              class="rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-sm outline-none focus:border-primary/50 focus:ring-2 focus:ring-primary/10 transition"
            >
              <option v-for="vt in dsVaiTro" :key="vt.value" :value="vt.value">
                {{ vt.label }}
              </option>
            </select>
          </div>

          <button
            @click="tuanTruoc"
            class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition"
          >
            <ChevronLeft class="h-4 w-4" />
          </button>
          <button
            @click="homNay"
            class="rounded-xl bg-primary px-4 py-1.5 text-sm font-semibold text-white transition hover:bg-primary-hover shadow-sm"
          >
            Hôm nay
          </button>
          <button
            @click="tuanSau"
            class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition"
          >
            <ChevronRight class="h-4 w-4" />
          </button>
        </div>

        <div
          v-if="loiTrang"
          class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600"
        >
          {{ loiTrang }}
        </div>

        <!-- Bảng lịch -->

        <div class="overflow-x-auto">
          <table class="w-full table-fixed border-separate border-spacing-0 text-sm">
         
            <colgroup>
              <col style="width: 160px" />
              <col v-for="_ in 7" :key="_" />
              <col style="width: 70px" />
            </colgroup>
            </table>
          </div>

        <div class="schedule-table-scroll overflow-x-auto">
          <table class="schedule-table w-full table-fixed border-separate border-spacing-0 text-sm">
            <colgroup>
              <col style="width:210px" />
              <col v-for="_ in 7" :key="_" style="width:124px" />
              <col style="width:86px" />

            </colgroup>
            <thead>
              <tr>
                <th
                  class="rounded-tl-2xl bg-slate-100 px-3 py-3 text-left text-xs font-bold text-slate-500"
                >
                  Nhân viên
                </th>
                <th
                  v-for="(ngay, i) in cacNgayTrongTuan"
                  :key="i"
                  class="bg-slate-100 px-2 py-3 text-center text-xs font-bold text-slate-700 transition"
                  :class="{
                    'bg-primary-light text-primary':
                      ngay.toDateString() === new Date().toDateString(),
                  }"
                >
                  <div>{{ NHAN_TUAN[i] }}</div>
                  <div class="font-normal text-slate-400">
                    {{ formatNgay(ngay) }}
                  </div>
                </th>
                <th
                  class="rounded-tr-2xl bg-slate-100 px-2 py-3 text-center text-xs font-bold text-slate-500"
                >
                  Tổng<br />giờ
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="dangTai">
                <td
                  colspan="9"
                  class="py-10 text-center text-sm text-slate-400"
                >
                  Đang tải dữ liệu nhân viên...
                </td>
              </tr>
              <tr v-else-if="!danhSachPhanTrang.length">
                <td
                  colspan="9"
                  class="py-10 text-center text-sm text-slate-400"
                >
                  Không có nhân viên.
                </td>
              </tr>
              <tr v-for="nv in danhSachPhanTrang" :key="nv.id" class="group">
                <!-- Nhân viên -->
                <td class="border-b border-slate-100 px-3 py-3">
                  <div class="flex items-center gap-2.5">
                    <div
                      :class="[
                        'flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-xs font-bold text-white',
                        nv.mauNen,
                      ]"
                    >
                      {{ nv.vieTat }}
                    </div>
                    <div class="min-w-0">
                      <div
                        class="truncate text-sm font-semibold text-slate-800"
                      >
                        {{ nv.ten }}
                      </div>
                      <div class="truncate text-xs text-slate-400">
                        {{ nv.chucVu }}
                      </div>
                    </div>
                  </div>
                </td>

                <!-- Ô từng ngày -->
                <td
                  v-for="(ngayIdx, i) in 7"
                  :key="i"
                  class="border-b border-slate-100 px-1.5 py-2 text-center"
                >
                  <!-- Có ca -->
                  <button
                    v-if="nv.lich[i]"
                    @click="moModalThemCa(nv, i)"
                    :class="[
                      'w-full min-w-[56px] rounded-xl border px-1.5 py-1.5 text-left text-xs font-semibold transition hover:opacity-80',
                      layThongTinCa(nv.lich[i])?.muaNhat,
                    ]"
                  >
                    <div class="font-bold">
                      {{ layThongTinCa(nv.lich[i])?.nhan }}
                    </div>
                    <div
                      class="mt-0.5 whitespace-nowrap font-normal opacity-80"
                    >
                      {{ layThongTinCa(nv.lich[i])?.gio }}
                    </div>
                  </button>

                  <!-- Chưa có ca -->
                  <button
                    v-else
                    @click="moModalThemCa(nv, i)"
                    class="h-14 w-full rounded-xl border-2 border-dashed border-slate-200 text-slate-300 opacity-0 transition hover:border-slate-300 hover:opacity-100 group-hover:opacity-60"
                    title="Thêm ca"
                  >
                    <Plus class="mx-auto h-4 w-4" />
                  </button>
                </td>

                <!-- Tổng giờ -->
                <td class="border-b border-slate-100 px-2 py-3 text-center">
                  <span class="text-sm font-bold text-slate-700"
                    >{{ nv.tongGio }}h</span
                  >
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Phân trang -->
        <AdminTableFooter
          :current-page="trangHienTai"
          :page-size="soTrang"
          :page-size-options="[5, 10, 20, 50]"
          :total-items="tongNV"
          :total-pages="tongSoTrang"
          compact
          show-refresh
          @refresh="taiNhanVien"
          @update:current-page="trangHienTai = $event"
          @update:page-size="soTrang = $event"
        />
      </section>

      <!-- ── Sidebar ── -->
      <aside class="space-y-4">
        <!-- Overtime tracker -->
        <div
          class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm"
        >
          <div class="mb-4 flex items-center justify-between">
            <h3 class="text-sm font-bold text-slate-800">Theo dõi tăng ca</h3>
            <button class="text-slate-400 hover:text-slate-600">
              <MoreHorizontal class="h-4 w-4" />
            </button>
          </div>

          <div class="space-y-4">
            <div v-for="nv in danhSachNV" :key="nv.id" class="space-y-1.5">
              <div class="flex items-center justify-between text-sm">
                <span class="font-semibold text-slate-700">{{ nv.ten }}</span>
                <span
                  :class="[
                    'text-xs font-bold',
                    nv.overtime >= nv.gioiHanOT * 0.9
                      ? 'text-rose-500'
                      : nv.overtime === 0
                        ? 'text-slate-400'
                        : 'text-emerald-600',
                  ]"
                >
                  {{ nv.overtime }}h / {{ nv.gioiHanOT }}h
                </span>
              </div>
              <div class="h-2 w-full overflow-hidden rounded-full bg-slate-100">
                <div
                  :class="[
                    'h-full rounded-full transition-all duration-500',
                    mauOvertimeBar(nv),
                  ]"
                  :style="{ width: phanTramOT(nv) + '%' }"></div>
              </div>
            </div>
          </div>

          <!-- Cảnh báo -->
          <div
            v-if="danhSachNV.some((nv) => nv.overtime >= nv.gioiHanOT * 0.9)"
            class="mt-4 rounded-xl bg-rose-50 px-3 py-2.5 text-xs text-rose-700"
          >
            <span class="font-bold">Lưu ý:</span>
            {{
              danhSachNV
                .filter((nv) => nv.overtime >= nv.gioiHanOT * 0.9)
                .map((nv) => nv.ten)
                .join(", ")
            }}
            sắp vượt giới hạn tăng ca hàng tuần. Vui lòng xem xét lại lịch trực
            chủ nhật.
          </div>
        </div>

        <!-- Thống kê nhanh -->
        <div class="grid grid-cols-2 gap-3">
          <div class="rounded-[20px] bg-emerald-50 p-4 text-center">
            <div
              class="mx-auto mb-2 flex h-9 w-9 items-center justify-center rounded-2xl bg-emerald-100 text-emerald-600"
            >
              <Users class="h-5 w-5" />
            </div>
            <p class="text-xs font-semibold text-emerald-600">Nhân viên trực</p>
            <p class="mt-1 text-2xl font-bold text-emerald-700">
              {{ nvTruc }} / {{ tongNV }}
            </p>
          </div>
          <div class="rounded-[20px] bg-primary-light p-4 text-center">
            <div
              class="mx-auto mb-2 flex h-9 w-9 items-center justify-center rounded-2xl bg-primary/10 text-primary"
            >
              <CalendarDays class="h-5 w-5" />
            </div>
            <p class="text-xs font-semibold text-primary">
              Nhân viên chưa phân công
            </p>
            <p class="mt-1 text-2xl font-bold text-primary">
              {{ String(caUnassigned).padStart(2, "0") }}
            </p>
          </div>
        </div>

        <!-- Phân loại ca -->
        <div
          class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm"
        >
          <h3 class="mb-3 text-sm font-bold text-slate-800">Phân loại ca</h3>
          <div class="space-y-2.5">
            <div
              v-for="ca in DS_CA"
              :key="ca.id"
              class="flex items-center gap-3 text-sm"
            >
              <div :class="['h-3.5 w-3.5 rounded-sm', ca.mau]" />
              <span class="font-semibold text-slate-700">{{ ca.nhan }}</span>
              <span class="text-slate-400">({{ ca.gio }})</span>
            </div>
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
  </div>
</template>
