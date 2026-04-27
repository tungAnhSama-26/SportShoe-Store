<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  Eye, FileSpreadsheet, Filter, Plus, RotateCcw, Search, Ticket,
  CheckCircle2, CircleX, X
} from "lucide-vue-next";
import {
  createPhieuGiamGia,
  deletePhieuGiamGia,
  getPhieuGiamGiaDetail,
  getPhieuGiamGiaList,
  updatePhieuGiamGia
} from "../../../services/khuyen-mai";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { getDisplayErrorMessage } from "../../../utils/error-message";

const router = useRouter();
const dangTai = ref(false);
const saving = ref(false);
const loiTrang = ref("");
const toast = ref({
  hienThi: false,
  loai: "success",
  tieuDe: "",
  noiDung: "",
});
let toastTimer = null;

const toastClass = computed(() => {
  if (toast.value.loai === "success") return "border-emerald-100 bg-emerald-50 text-emerald-700";
  if (toast.value.loai === "warning") return "border-amber-100 bg-amber-50 text-amber-700";
  return "border-rose-100 bg-rose-50 text-rose-700";
});

const toastIconClass = computed(() => {
  if (toast.value.loai === "success") return "bg-emerald-100 text-emerald-600";
  if (toast.value.loai === "warning") return "bg-amber-100 text-amber-600";
  return "bg-rose-100 text-rose-600";
});

const toastAccentClass = computed(() => {
  if (toast.value.loai === "success") return "bg-emerald-500";
  if (toast.value.loai === "warning") return "bg-amber-500";
  return "bg-rose-500";
});

const ToastIcon = computed(() => {
  if (toast.value.loai === "success") return CheckCircle2;
  return CircleX;
});

function hienThiThongBao(loai, tieuDe, noiDung = "") {
  if (toastTimer) clearTimeout(toastTimer);
  toast.value = { hienThi: true, loai, tieuDe, noiDung };
  toastTimer = setTimeout(() => { toast.value.hienThi = false; }, 3200);
}

const boLoc = ref({ keyword: "", trangThai: "", tuNgay: "", denNgay: "", loai: "" });
const danhSach = ref([]);
const tongSoTrang = ref(1);
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const totalItems = ref(0);

const dsTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Đang hoạt động", value: "1" },
  { label: "Ngưng hoạt động", value: "0" },
];

const formErrors = reactive({});

function mauTrangThai(trangThai) {
  return Number(trangThai) === 1 ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600";
}

function statusText(value) {
  return Number(value) === 1 ? "Đang hoạt động" : "Ngưng hoạt động";
}

function mauLoaiGiam(loai) {
  return Number(loai) === 1 ? "bg-blue-50 text-blue-600" : "bg-amber-50 text-amber-600";
}

function loaiGiamText(loai) {
  return Number(loai) === 1 ? "Phần trăm" : "Tiền mặt";
}

function mauLoaiPhieu(loaiPhieu) {
  return Number(loaiPhieu) === 1 ? "bg-indigo-50 text-indigo-600" : "bg-purple-50 text-purple-600";
}

function loaiPhieuText(loaiPhieu) {
  return Number(loaiPhieu) === 1 ? "Công khai" : "Cá nhân";
}

function formatGiaTri(giaTri, loai) {
  if (!giaTri) return "0";
  return Number(loai) === 1 ? `${giaTri}%` : `${Number(giaTri).toLocaleString('vi-VN')}đ`;
}

function formatTien(tien) {
  if (!tien) return "0đ";
  return `${Number(tien).toLocaleString('vi-VN')}đ`;
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
    const data = await getPhieuGiamGiaList({
      keyword: boLoc.value.keyword || undefined,
      trangThai: boLoc.value.trangThai !== "" ? Number(boLoc.value.trangThai) : undefined,
      loai: boLoc.value.loai !== "" ? Number(boLoc.value.loai) : undefined,
      tuNgay: boLoc.value.tuNgay || undefined,
      denNgay: boLoc.value.denNgay || undefined,
      pageNo: trangHienTai.value - 1,
      pageSize: soPhanTuMotTrang.value
    });
    danhSach.value = data?.content || [];
    tongSoTrang.value = data?.totalPages || 1;
    totalItems.value = data?.totalElements || 0;
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải danh sách phiếu giảm giá");
  } finally {
    dangTai.value = false;
  }
}



function lamMoiBoLoc() {
  boLoc.value = { keyword: "", trangThai: "", tuNgay: "", denNgay: "", loai: "" };
}

async function xuatExcel() {
  try {
      exportRowsToExcel({
        filename: "quan-ly-phieu-giam-gia",
        sheetName: "PhieuGiamGia",
        columns: [
          { label: "STT", value: (_, index) => index + 1 },
          { label: "Mã", key: "ma" },
          { label: "Tên", key: "ten" },
          { label: "Giảm tối đa", value: (row) => row.giamToiDa ? `${row.giamToiDa.toLocaleString('vi-VN')}đ` : "0đ" },
          { label: "Loại phiếu", value: (row) => Number(row.loaiPhieu) === 1 ? "Công khai" : "Cá nhân" },
          { label: "Giá trị", value: (row) => Number(row.loai) === 1 ? `${row.giaTri}%` : `${Number(row.giaTri).toLocaleString('vi-VN')}đ` },
          { label: "Số lượng", key: "soLuong" },
          { label: "Ngày bắt đầu", value: (row) => toDisplayDate(row.ngayBatDau) },
          { label: "Ngày kết thúc", value: (row) => toDisplayDate(row.ngayKetThuc) },
          { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
        ],
        rows,
      });
  } catch (error) {
    window.alert(getDisplayErrorMessage(error, "Không thể xuất Excel phiếu giảm giá"));
  }
}

function openCreateModal() {
  router.push({ name: "admin-phieu-giam-gia-them" });
}

async function openEditModal(item) {
  router.push({ name: "admin-phieu-giam-gia-chi-tiet", params: { id: item.id } });
}

async function removeItem(item) {
  if (!confirm(`Bạn có chắc muốn xóa phiếu này?`)) return;
  try {
    await deletePhieuGiamGia(item.id);
    taiDanhSach();
    hienThiThongBao("success", "Xóa thành công");
  } catch (error) {
    hienThiThongBao("error", "Xóa thất bại", getDisplayErrorMessage(error, "Không thể xóa phiếu giảm giá"));
  }
}




async function nhanhDoiTrangThai(item) {
  if (!confirm("Bạn có chắc chắn muốn thay đổi trạng thái của phiếu này?")) return;
  try {
    const detail = await getPhieuGiamGiaDetail(item.id);
    // Đảo trạng thái: 1 <-> 0
    detail.trangThai = Number(detail.trangThai) === 1 ? 0 : 1;
    // Chuyển các field số về kiểu Number (đảm bảo payload sạch)
    detail.loai = Number(detail.loai);
    detail.loaiPhieu = Number(detail.loaiPhieu);
    detail.giaTri = Number(detail.giaTri);
    detail.soLuong = Number(detail.soLuong);
    detail.trangThai = Number(detail.trangThai);
    
    await updatePhieuGiamGia(item.id, detail);
    hienThiThongBao("success", "Đổi trạng thái thành công");
    await taiDanhSach();
  } catch (error) {
    hienThiThongBao("error", "Đổi trạng thái thất bại", getDisplayErrorMessage(error, "Không thể cập nhật trạng thái phiếu giảm giá"));
  }
}



onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <!-- Toast Notification -->
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="translate-y-3 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-3 opacity-0"
    >
      <div
        v-if="toast.hienThi"
        class="fixed right-5 top-5 z-[70] w-[360px] max-w-[calc(100vw-2rem)] overflow-hidden rounded-2xl border bg-white shadow-[0_18px_60px_rgba(15,23,42,0.18)]"
        :class="toastClass"
      >
        <div class="flex gap-3 p-4">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full" :class="toastIconClass">
            <component :is="ToastIcon" class="h-5 w-5" />
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-sm font-bold text-slate-800">{{ toast.tieuDe }}</p>
            <p v-if="toast.noiDung" class="mt-1 text-sm leading-5 text-slate-600">{{ toast.noiDung }}</p>
          </div>
          <button type="button" class="rounded-full p-1 text-slate-400 transition hover:bg-white/70 hover:text-slate-600" @click="toast.hienThi = false">
            <X class="h-4 w-4" />
          </button>
        </div>
        <div class="h-1.5 w-full" :class="toastAccentClass"></div>
      </div>
    </Transition>
    <!-- Header -->
    <section class="flex items-end justify-between">
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">Quản lý phiếu giảm giá</h1>
    </section>

    <!-- Bộ lọc -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
          <p class="text-sm text-slate-400">Tra cứu nhanh dữ liệu.</p>
        </div>
      </div>

      <div class="flex flex-col gap-6">
        <!-- Hàng 1: Các ô nhập liệu (4 cột) -->
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div class="space-y-2">
            <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Tìm kiếm</label>
            <div class="relative">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                v-model="boLoc.keyword"
                type="text"
                placeholder="Tìm theo tên hoặc mã"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Ngày bắt đầu</label>
            <input 
              v-model="boLoc.tuNgay" 
              type="date" 
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" 
            />
          </div>

          <div class="space-y-2">
            <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Ngày kết thúc</label>
            <input 
              v-model="boLoc.denNgay" 
              type="date" 
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" 
            />
          </div>

          <div class="space-y-2">
            <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Trạng thái</label>
            <select v-model="boLoc.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
              <option v-for="tt in dsTrangThai" :key="tt.value" :value="tt.value">{{ tt.label }}</option>
            </select>
          </div>
        </div>

        <!-- Hàng 2: Các nút bấm (Căn phải) -->
        <div class="flex flex-wrap items-center justify-end gap-3 border-t border-slate-100 pt-4">
          <button @click="lamMoiBoLoc" class="inline-flex h-11 items-center gap-2 rounded-2xl border border-rose-200 bg-white px-6 text-sm font-semibold text-rose-600 transition hover:bg-rose-50 whitespace-nowrap">
            <RotateCcw class="h-4 w-4" /> Đặt lại
          </button>
          <button @click="xuatExcel" class="inline-flex h-11 items-center gap-2 rounded-2xl border border-rose-200 bg-white px-6 text-sm font-semibold text-rose-600 transition hover:bg-rose-50 whitespace-nowrap">
            <FileSpreadsheet class="h-4 w-4" /> Xuất Excel
          </button>
          <button @click="openCreateModal" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-6 text-sm font-semibold text-white transition hover:bg-rose-600 shadow-lg shadow-rose-200 whitespace-nowrap">
            <Plus class="h-4 w-4" /> Thêm phiếu giảm giá
          </button>
        </div>
      </div>
    </section>

    <!-- Danh sách -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center justify-between">
        <h2 class="text-lg font-bold text-slate-800">Danh sách phiếu giảm giá</h2>
        <div>
          <p class="text-sm text-slate-400 font-medium">{{ totalItems }} bản ghi hiển thị.</p>
        </div>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <table class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-950">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã</th>
              <th class="bg-slate-100 px-4 py-3">Tên</th>
              <th class="bg-slate-100 px-4 py-3">Loại phiếu</th>
              <th class="bg-slate-100 px-4 py-3">Giá trị</th>
              <th class="bg-slate-100 px-4 py-3">Số lượng</th>
              <th class="bg-slate-100 px-4 py-3">Ngày bắt đầu</th>
              <th class="bg-slate-100 px-4 py-3">Ngày kết thúc</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="10" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="!danhSach.length">
              <td colspan="10" class="py-10 text-center text-sm text-slate-400">Không có dữ liệu.</td>
            </tr>
            <tr v-for="(item, index) in danhSach" :key="item.id" class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100">
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}</td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ item.ma }}</td>
              <td class="px-4 py-3">
                <div class="font-semibold text-slate-800">{{ item.ten }}</div>
                <div v-if="item.giamToiDa > 0" class="text-[12px] text-rose-500 font-medium mt-0.5" title="Giảm tối đa">Tối đa: {{ formatTien(item.giamToiDa) }}</div>
              </td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold whitespace-nowrap" :class="mauLoaiPhieu(item.loaiPhieu)">
                  {{ loaiPhieuText(item.loaiPhieu) }}
                </span>
              </td>

              <td class="px-4 py-3 font-semibold text-rose-600">{{ formatGiaTri(item.giaTri, item.loai) }}</td>
              <td class="px-4 py-3 text-slate-600">{{ item.soLuong }}</td>
              <td class="px-4 py-3 text-slate-600">{{ toDisplayDate(item.ngayBatDau) }}</td>
              <td class="px-4 py-3 text-slate-600">{{ toDisplayDate(item.ngayKetThuc) }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold whitespace-nowrap" :class="mauTrangThai(item.trangThai)">
                  {{ statusText(item.trangThai) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex justify-center items-center gap-3">
                  <button @click="openEditModal(item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500" title="Xem chi tiết">
                    <Eye class="h-5 w-5" />
                  </button>
                  <button @click="nhanhDoiTrangThai(item)" class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none" :class="Number(item.trangThai) === 1 ? 'bg-rose-500' : 'bg-slate-200'" :title="Number(item.trangThai) === 1 ? 'Ngưng hoạt động' : 'Kích hoạt'">
                    <span class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200" :class="Number(item.trangThai) === 1 ? 'translate-x-5' : 'translate-x-0'"></span>
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

