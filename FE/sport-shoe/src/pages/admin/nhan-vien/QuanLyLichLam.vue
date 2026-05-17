<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
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
import { layDanhSachNhanVien } from "../../../services/nhan-vien";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";

const router = useRouter();

// ───────── Dữ liệu ca làm việc ─────────
const DS_CA = [
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
type CaLam = "sang" | "chieu" | "toi" | null;

interface NhanVien {
  id: string;
  ten: string;
  vieTat: string;
  chucVu: string;       // tenVaiTro từ BE
  vaiTro: number;       // 1=Admin, 2=Bán hàng, 3=Kho
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
  3: "bg-sky-500",
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
      vaiTro: Number(nv.vaiTro ?? 0),
      hinhAnh: nv.hinhAnh ?? "",
      mauNen: mauNenNV(Number(nv.vaiTro ?? 0)),
      lich: taoLichMock(Number(nv.vaiTro ?? 0)),
      tongGio: 32,
      overtime: +(Math.random() * 5).toFixed(1),
      gioiHanOT: 5,
    }));
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải danh sách nhân viên");
  } finally {
    dangTai.value = false;
  }
}

onMounted(taiNhanVien);

// ───────── Bộ lọc vai trò ─────────
const boLocVaiTro = ref(0); // 0 = tất cả
const dsVaiTro = [
  { value: 0, label: "Tất cả" },
  { value: 1, label: "Admin" },
  { value: 2, label: "Bán hàng" },
  { value: 3, label: "Kho" },
];

const danhSachLocVaiTro = computed(() =>
  boLocVaiTro.value === 0
    ? danhSachNV.value
    : danhSachNV.value.filter(nv => nv.vaiTro === boLocVaiTro.value)
);

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

function moModalThemCa(nv: NhanVien, ngayIdx: number) {
  modalNV.value = nv;
  modalNgayIndex.value = ngayIdx;
  modalCaChon.value = nv.lich[ngayIdx];
  showModalThemCa.value = true;
}

function luuCa() {
  if (!modalNV.value || modalNgayIndex.value < 0) return;
  modalNV.value.lich[modalNgayIndex.value] = modalCaChon.value;
  showModalThemCa.value = false;
}

function xoaCa() {
  if (!modalNV.value || modalNgayIndex.value < 0) return;
  modalNV.value.lich[modalNgayIndex.value] = null;
  showModalThemCa.value = false;
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
    <section class="flex flex-wrap items-center gap-3">
      <button
        @click="router.push({ name: 'admin-nhan-vien' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>

      <div class="flex-1">
        <h1 class="text-[26px] font-bold tracking-tight text-slate-900">Quản lý lịch làm việc</h1>
        <p class="text-sm text-slate-400">Phân ca và theo dõi giờ làm cho nhân viên</p>
      </div>

      <!-- Tuần hiển thị -->
      <div class="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm">
        <CalendarDays class="h-4 w-4 text-slate-400" />
        {{ formatTuanHienThi() }}
      </div>

      <button class="admin-btn-soft gap-2">
        <Shuffle class="h-4 w-4" /> Xếp ca tự động
      </button>
      <button class="admin-btn-soft gap-2">
        <Download class="h-4 w-4" /> Xuất Excel
      </button>
      <button @click="showModalThemCa = true; modalNV = null; modalNgayIndex = -1" class="admin-btn-primary gap-2">
        <Plus class="h-4 w-4" /> Thêm ca mới
      </button>
    </section>

    <!-- ───── CONTENT ───── -->
    <div class="grid gap-5 xl:grid-cols-[1fr_280px]">

      <!-- ── Bảng lịch ── -->
      <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">

        <!-- Thanh điều hướng tuần -->
        <div class="mb-5 flex flex-wrap items-center gap-3">
          <h2 class="flex-1 text-base font-bold text-slate-800">Bảng lịch làm việc theo tuần</h2>

          <!-- Lọc vai trò -->
          <div class="flex items-center gap-2 text-sm text-slate-500">
            <span class="font-medium">Vai trò:</span>
            <select
              v-model="boLocVaiTro"
              class="rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-sm outline-none focus:border-primary/50 focus:ring-2 focus:ring-primary/10 transition"
            >
              <option v-for="vt in dsVaiTro" :key="vt.value" :value="vt.value">{{ vt.label }}</option>
            </select>
          </div>

          <button @click="tuanTruoc" class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
            <ChevronLeft class="h-4 w-4" />
          </button>
          <button @click="homNay" class="rounded-xl bg-primary px-4 py-1.5 text-sm font-semibold text-white transition hover:bg-primary-hover shadow-sm">
            Hôm nay
          </button>
          <button @click="tuanSau" class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
            <ChevronRight class="h-4 w-4" />
          </button>
        </div>

        <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">
          {{ loiTrang }}
        </div>

        <!-- Bảng lịch -->
        <div class="overflow-x-auto">
          <table class="w-full table-fixed border-separate border-spacing-0 text-sm">
            <colgroup>
              <col style="width:160px" />
              <col v-for="_ in 7" :key="_" />
              <col style="width:70px" />
            </colgroup>
            <thead>
              <tr>
                <th class="rounded-tl-2xl bg-slate-100 px-3 py-3 text-left text-xs font-bold text-slate-500">Nhân viên</th>
                <th
                  v-for="(ngay, i) in cacNgayTrongTuan"
                  :key="i"
                  class="bg-slate-100 px-2 py-3 text-center text-xs font-bold text-slate-700 transition"
                  :class="{ 'bg-primary-light text-primary': ngay.toDateString() === new Date().toDateString() }"
                >
                  <div>{{ NHAN_TUAN[i] }}</div>
                  <div class="font-normal text-slate-400">{{ formatNgay(ngay) }}</div>
                </th>
                <th class="rounded-tr-2xl bg-slate-100 px-2 py-3 text-center text-xs font-bold text-slate-500">Tổng<br />giờ</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="dangTai">
                <td colspan="9" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu nhân viên...</td>
              </tr>
              <tr v-else-if="!danhSachPhanTrang.length">
                <td colspan="9" class="py-10 text-center text-sm text-slate-400">Không có nhân viên.</td>
              </tr>
              <tr
                v-for="nv in danhSachPhanTrang"
                :key="nv.id"
                class="group"
              >
                <!-- Nhân viên -->
                <td class="border-b border-slate-100 px-3 py-3">
                  <div class="flex items-center gap-2.5">
                    <div :class="['flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-xs font-bold text-white', nv.mauNen]">
                      {{ nv.vieTat }}
                    </div>
                    <div class="min-w-0">
                      <div class="truncate text-sm font-semibold text-slate-800">{{ nv.ten }}</div>
                      <div class="truncate text-xs text-slate-400">{{ nv.chucVu }}</div>
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
                    :class="['w-full min-w-[56px] rounded-xl border px-1.5 py-1.5 text-left text-xs font-semibold transition hover:opacity-80', layThongTinCa(nv.lich[i])?.muaNhat]"
                  >
                    <div class="font-bold">{{ layThongTinCa(nv.lich[i])?.nhan }}</div>
                    <div class="mt-0.5 whitespace-nowrap font-normal opacity-80">{{ layThongTinCa(nv.lich[i])?.gio }}</div>
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
                  <span class="text-sm font-bold text-slate-700">{{ nv.tongGio }}h</span>
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
        <div class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
          <div class="mb-4 flex items-center justify-between">
            <h3 class="text-sm font-bold text-slate-800">Theo dõi tăng ca</h3>
            <button class="text-slate-400 hover:text-slate-600"><MoreHorizontal class="h-4 w-4" /></button>
          </div>

          <div class="space-y-4">
            <div v-for="nv in danhSachNV" :key="nv.id" class="space-y-1.5">
              <div class="flex items-center justify-between text-sm">
                <span class="font-semibold text-slate-700">{{ nv.ten }}</span>
                <span :class="['text-xs font-bold', nv.overtime >= nv.gioiHanOT * 0.9 ? 'text-rose-500' : nv.overtime === 0 ? 'text-slate-400' : 'text-emerald-600']">
                  {{ nv.overtime }}h / {{ nv.gioiHanOT }}h
                </span>
              </div>
              <div class="h-2 w-full overflow-hidden rounded-full bg-slate-100">
                <div
                  :class="['h-full rounded-full transition-all duration-500', mauOvertimeBar(nv)]"
                  :style="{ width: phanTramOT(nv) + '%' }"
                />
              </div>
            </div>
          </div>

          <!-- Cảnh báo -->
          <div v-if="danhSachNV.some(nv => nv.overtime >= nv.gioiHanOT * 0.9)" class="mt-4 rounded-xl bg-rose-50 px-3 py-2.5 text-xs text-rose-700">
            <span class="font-bold">Lưu ý:</span>
            {{ danhSachNV.filter(nv => nv.overtime >= nv.gioiHanOT * 0.9).map(nv => nv.ten).join(', ') }}
            sắp vượt giới hạn tăng ca hàng tuần. Vui lòng xem xét lại lịch trực chủ nhật.
          </div>
        </div>

        <!-- Thống kê nhanh -->
        <div class="grid grid-cols-2 gap-3">
          <div class="rounded-[20px] bg-emerald-50 p-4 text-center">
            <div class="mx-auto mb-2 flex h-9 w-9 items-center justify-center rounded-2xl bg-emerald-100 text-emerald-600">
              <Users class="h-5 w-5" />
            </div>
            <p class="text-xs font-semibold text-emerald-600">Nhân viên trực</p>
            <p class="mt-1 text-2xl font-bold text-emerald-700">{{ nvTruc }} / {{ tongNV }}</p>
          </div>
          <div class="rounded-[20px] bg-primary-light p-4 text-center">
            <div class="mx-auto mb-2 flex h-9 w-9 items-center justify-center rounded-2xl bg-primary/10 text-primary">
              <CalendarDays class="h-5 w-5" />
            </div>
            <p class="text-xs font-semibold text-primary">Ca chưa gắn</p>
            <p class="mt-1 text-2xl font-bold text-primary">{{ String(caUnassigned).padStart(2, '0') }}</p>
          </div>
        </div>

        <!-- Phân loại ca -->
        <div class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
          <h3 class="mb-3 text-sm font-bold text-slate-800">Phân loại ca</h3>
          <div class="space-y-2.5">
            <div v-for="ca in DS_CA" :key="ca.id" class="flex items-center gap-3 text-sm">
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
        <div class="w-full max-w-sm rounded-[24px] bg-white p-6 shadow-2xl mx-4">
          <h3 class="mb-1 text-base font-bold text-slate-800">
            {{ modalNV ? (modalNV.lich[modalNgayIndex] ? 'Sửa ca làm việc' : 'Thêm ca làm việc') : 'Thêm ca mới' }}
          </h3>
          <p v-if="modalNV" class="mb-5 text-sm text-slate-400">
            {{ modalNV.ten }} – {{ NHAN_TUAN[modalNgayIndex] }} {{ formatNgay(cacNgayTrongTuan[modalNgayIndex]) }}
          </p>
          <p v-else class="mb-5 text-sm text-slate-400">Chọn nhân viên và ca làm việc cần thêm.</p>

          <div class="space-y-2.5">
            <button
              v-for="ca in DS_CA"
              :key="ca.id"
              @click="modalCaChon = ca.id as CaLam"
              :class="['flex w-full items-center gap-3 rounded-2xl border-2 px-4 py-3 text-sm font-semibold transition',
                modalCaChon === ca.id ? 'border-primary/50 bg-primary-light text-primary' : 'border-slate-200 hover:border-slate-300']"
            >
              <div :class="['h-4 w-4 rounded-sm', ca.mau]" />
              <span>{{ ca.nhan }}</span>
              <span class="ml-auto text-slate-400">{{ ca.gio }}</span>
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
