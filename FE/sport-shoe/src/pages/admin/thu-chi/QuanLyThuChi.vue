<script setup>
import { computed, onMounted, ref } from "vue";
import {
  CalendarClock,
  CircleDollarSign,
  FileCheck2,
  PencilLine,
  Plus,
  ReceiptText,
  Settings2,
  Trash2,
  TrendingDown,
  TrendingUp,
  X,
} from "lucide-vue-next";
import { useAdminSession } from "../../../composable/useAdminSession";
import { useGiaoCa } from "../../../composable/useGiaoCa";
import { showConfirm, showError, showSuccess } from "../../../utils/alert.js";
import { buildThuChiRecord, filterThuChiRecords } from "../../../services/thu-chi";

const { adminSession } = useAdminSession();
const { activeShift, loadActiveShift } = useGiaoCa();

const STORAGE_KEY = "sport-shoe-thu-chi-records-v1";
const CATEGORY_STORAGE_KEY = "sport-shoe-thu-chi-categories-v1";
const VIEW_MODE_OPTIONS = [
  { value: "all", label: "Tất cả" },
  { value: "THU", label: "Phiếu Thu" },
  { value: "CHI", label: "Phiếu Chi" },
];
const PAYMENT_OPTIONS = [
  { value: "all", label: "Tất cả" },
  { value: "Tiền mặt", label: "Tiền mặt" },
  { value: "Chuyển khoản", label: "Chuyển khoản" },
];
const TIME_OPTIONS = [
  { value: "all", label: "Tất cả" },
  { value: "today", label: "Hôm nay" },
  { value: "yesterday", label: "Hôm qua" },
  { value: "week", label: "Tuần này" },
  { value: "custom", label: "Khoảng thời gian" },
];
const DEFAULT_EXPENSE_CATEGORIES = [
  "Mua nước",
  "Trả tiền ship",
  "Sửa chữa cửa hàng",
  "Admin rút tiền",
];
const DEFAULT_INCOME_CATEGORIES = ["Thu khác", "Thu tiền bán phế liệu"];

const isAdmin = computed(() => adminSession.value.vaiTro === "Quản trị viên");
const currentShiftId = computed(() => activeShift.value?.id || activeShift.value?.ma || "");
const currentShiftLabel = computed(() => activeShift.value?.ma || "Chưa mở ca");
const currentUser = computed(() => ({
  id: adminSession.value.id,
  hoTen: adminSession.value.hoTen || adminSession.value.tenTaiKhoan || "Nhân viên",
  vaiTro: adminSession.value.vaiTro || "Nhân viên",
}));

const records = ref(loadRecords());
const categories = ref(loadCategories());
const showFormModal = ref(false);
const showConfigModal = ref(false);
const editingRecordId = ref(null);
const searchText = ref("");
const filterType = ref("all");
const filterTime = ref("all");
const filterPayment = ref("all");
const filterEmployee = ref("all");
const filterStatus = ref("all");
const customFrom = ref("");
const customTo = ref("");
const newCategory = ref("");

const form = ref({
  loaiPhieu: "THU",
  maPhieu: "",
  thoiGian: new Date().toISOString(),
  hangMuc: "",
  soTien: "",
  hinhThuc: "Tiền mặt",
  nguoiNhan: "",
  ghiChu: "",
});

const visibleRecords = computed(() => {
  const baseRecords = filterThuChiRecords(
    records.value,
    {},
    adminSession.value.vaiTro,
    adminSession.value.id,
    currentShiftId.value,
  );

  return baseRecords.filter((record) => {
    const text = `${record.maPhieu} ${record.hangMuc || record["hạngMục"] || ""} ${record.nguoiNhan || ""} ${record.nguoiTaoTen || ""}`.toLowerCase();
    const matchesSearch = searchText.value.trim() === "" || text.includes(searchText.value.toLowerCase());
    const matchesType = filterType.value === "all" || record.loaiPhieu === filterType.value;
    const matchesPayment = filterPayment.value === "all" || record.hinhThuc === filterPayment.value;
    const matchesEmployee = filterEmployee.value === "all" || String(record.nguoiTaoId) === String(filterEmployee.value);
    const matchesStatus = filterStatus.value === "all" || record.trangThai === filterStatus.value;
    const matchesTime = matchesTimeRange(record);

    return matchesSearch && matchesType && matchesPayment && matchesEmployee && matchesStatus && matchesTime;
  });
});

const summaryCards = computed(() => {
  const cashIncome = visibleRecords.value
    .filter((record) => record.loaiPhieu === "THU" && record.hinhThuc === "Tiền mặt")
    .reduce((sum, record) => sum + Number(record.soTien || 0), 0);
  const cashExpense = visibleRecords.value
    .filter((record) => record.loaiPhieu === "CHI" && record.hinhThuc === "Tiền mặt")
    .reduce((sum, record) => sum + Number(record.soTien || 0), 0);
  const startCash = Number(activeShift.value?.tienDauCa || 500000);

  return [
    {
      label: "Tổng thu tiền mặt",
      value: cashIncome,
      tone: "emerald",
      icon: TrendingUp,
    },
    {
      label: "Tổng chi tiền mặt",
      value: cashExpense,
      tone: "rose",
      icon: TrendingDown,
    },
    {
      label: "Quỹ tiền mặt hiện tại",
      value: startCash + cashIncome - cashExpense,
      tone: "amber",
      icon: CircleDollarSign,
    },
  ];
});

const employeeOptions = computed(() => {
  const unique = Array.from(new Map(records.value.map((record) => [String(record.nguoiTaoId), {
    id: record.nguoiTaoId,
    label: record.nguoiTaoTen || "Nhân viên",
  }])).values());

  return unique.filter(Boolean);
});

function loadRecords() {
  if (typeof window === "undefined") {
    return [];
  }

  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return [];
  }

  try {
    return JSON.parse(raw);
  } catch {
    return [];
  }
}

function loadCategories() {
  if (typeof window === "undefined") {
    return [...DEFAULT_EXPENSE_CATEGORIES, ...DEFAULT_INCOME_CATEGORIES];
  }

  const raw = window.localStorage.getItem(CATEGORY_STORAGE_KEY);
  if (!raw) {
    return [...DEFAULT_EXPENSE_CATEGORIES, ...DEFAULT_INCOME_CATEGORIES];
  }

  try {
    return JSON.parse(raw);
  } catch {
    return [...DEFAULT_EXPENSE_CATEGORIES, ...DEFAULT_INCOME_CATEGORIES];
  }
}

function persistRecords() {
  if (typeof window !== "undefined") {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(records.value));
  }
}

function persistCategories() {
  if (typeof window !== "undefined") {
    window.localStorage.setItem(CATEGORY_STORAGE_KEY, JSON.stringify(categories.value));
  }
}

function formatVND(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(Number(value || 0)).replace("₫", "đ");
}

function formatDateTime(value) {
  if (!value) {
    return "";
  }
  return new Date(value).toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function resetForm() {
  form.value = {
    loaiPhieu: "THU",
    maPhieu: "",
    thoiGian: new Date().toISOString(),
    hangMuc: "",
    soTien: "",
    hinhThuc: "Tiền mặt",
    nguoiNhan: "",
    ghiChu: "",
  };
  editingRecordId.value = null;
}

function openCreateModal(type) {
  resetForm();
  form.value.loaiPhieu = type;
  form.value.maPhieu = generateCode(type);
  form.value.hangMuc = getDefaultCategory(type);
  showFormModal.value = true;
}

function openEditModal(record) {
  editingRecordId.value = record.id;
  form.value = {
    loaiPhieu: record.loaiPhieu,
    maPhieu: record.maPhieu,
    thoiGian: record.thoiGian,
    hangMuc: record.hangMuc || record["hạngMục"],
    soTien: record.soTien,
    hinhThuc: record.hinhThuc,
    nguoiNhan: record.nguoiNhan || "",
    ghiChu: record.ghiChu || "",
  };
  showFormModal.value = true;
}

function closeModal() {
  showFormModal.value = false;
  showConfigModal.value = false;
  resetForm();
}

function generateCode(type) {
  const prefix = type === "CHI" ? "PC" : "PT";
  const nextIndex = records.value.filter((item) => item.loaiPhieu === type).length + 1;
  return `${prefix}${String(nextIndex).padStart(3, "0")}`;
}

function getDefaultCategory(type) {
  if (type === "CHI") {
    return categories.value.find((item) => DEFAULT_EXPENSE_CATEGORIES.includes(item)) || DEFAULT_EXPENSE_CATEGORIES[0];
  }
  return categories.value.find((item) => DEFAULT_INCOME_CATEGORIES.includes(item)) || DEFAULT_INCOME_CATEGORIES[0];
}

async function saveRecord() {
  if (!form.value.soTien || Number(form.value.soTien) <= 0) {
    showError("Vui lòng nhập số tiền hợp lệ.");
    return;
  }

  if (!form.value.hangMuc) {
    showError("Vui lòng chọn hạng mục.");
    return;
  }

  const payload = buildThuChiRecord(
    {
      ...form.value,
      maPhieu: form.value.maPhieu || generateCode(form.value.loaiPhieu),
      soTien: Number(form.value.soTien),
      hangMuc: form.value.hangMuc,
    },
    { user: currentUser.value, role: adminSession.value.vaiTro, shiftId: currentShiftId.value },
  );

  if (editingRecordId.value) {
    records.value = records.value.map((item) => (item.id === editingRecordId.value ? { ...item, ...payload } : item));
    showSuccess("Đã cập nhật phiếu thành công.");
  } else {
    records.value = [{ ...payload, id: `record-${Date.now()}` }, ...records.value];
    showSuccess("Đã lưu phiếu thành công.");
  }

  persistRecords();
  closeModal();
}

async function handleDelete(record) {
  const confirmed = await showConfirm(`Bạn có chắc muốn hủy phiếu ${record.maPhieu}?`, "Xác nhận");
  if (!confirmed) return;

  records.value = records.value.map((item) => (item.id === record.id ? { ...item, trangThai: "Đã hủy" } : item));
  persistRecords();
  showSuccess("Đã hủy phiếu.");
}

function addCategory() {
  const value = newCategory.value.trim();
  if (!value) return;
  if (!categories.value.includes(value)) {
    categories.value = [...categories.value, value];
    persistCategories();
  }
  newCategory.value = "";
}

function matchesTimeRange(record) {
  if (filterTime.value === "all") return true;

  const recordTime = new Date(record.thoiGian).getTime();
  const now = new Date();
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
  const yesterdayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1).getTime();
  const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 6).getTime();
  const from = new Date(customFrom.value).getTime();
  const to = new Date(customTo.value).getTime();

  if (filterTime.value === "today") return recordTime >= todayStart;
  if (filterTime.value === "yesterday") {
    return recordTime >= yesterdayStart && recordTime < todayStart;
  }
  if (filterTime.value === "week") return recordTime >= weekStart;
  if (filterTime.value === "custom") {
    if (!customFrom.value || !customTo.value) return true;
    return recordTime >= from && recordTime <= to;
  }
  return true;
}

onMounted(async () => {
  await loadActiveShift();
  if (!records.value.length) {
    const sample = [
      buildThuChiRecord(
        {
          id: "seed-1",
          loaiPhieu: "THU",
          maPhieu: "PT001",
          thoiGian: new Date().toISOString(),
          hangMuc: "Thu khác",
          soTien: 250000,
          hinhThuc: "Tiền mặt",
          nguoiNhan: "Khách",
          ghiChu: "Thu tiền phụ thu",
        },
        { user: currentUser.value, role: adminSession.value.vaiTro, shiftId: currentShiftId.value },
      ),
      buildThuChiRecord(
        {
          id: "seed-2",
          loaiPhieu: "CHI",
          maPhieu: "PC001",
          thoiGian: new Date().toISOString(),
          hangMuc: "Trả tiền ship",
          soTien: 120000,
          hinhThuc: "Tiền mặt",
          nguoiNhan: "Anh thợ",
          ghiChu: "Chi tiền ship",
        },
        { user: { ...currentUser.value, id: 999, hoTen: "Admin mẫu" }, role: "Quản trị viên", shiftId: currentShiftId.value },
      ),
    ];

    records.value = sample;
    persistRecords();
  }
});
</script>

<template>
  <div class="space-y-6">
    <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <p class="text-sm font-semibold uppercase tracking-[0.24em] text-primary">Quản lý thu / chi</p>
          <h1 class="mt-2 text-2xl font-black uppercase tracking-wide text-slate-800 dark:text-white">QUẢN LÝ THU / CHI (SỔ QUỸ COIN)</h1>
          <p class="mt-2 text-sm text-slate-500">
            Ca hiện tại: <span class="font-semibold text-slate-700 dark:text-slate-200">{{ currentShiftLabel }}</span>
          </p>
        </div>
        <div class="flex flex-wrap gap-2">
          <button class="rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-emerald-700" @click="openCreateModal('THU')">
            <Plus class="mr-2 inline h-4 w-4" /> Tạo phiếu thu
          </button>
          <button class="rounded-xl bg-rose-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-rose-700" @click="openCreateModal('CHI')">
            <Plus class="mr-2 inline h-4 w-4" /> Tạo phiếu chi
          </button>
          <button v-if="isAdmin" class="rounded-xl bg-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-300 dark:bg-slate-700 dark:text-slate-200" @click="showConfigModal = true">
            <Settings2 class="mr-2 inline h-4 w-4" /> Cấu hình loại quỹ
          </button>
        </div>
      </div>
    </div>

    <div class="grid gap-4 lg:grid-cols-3">
      <div v-for="card in summaryCards" :key="card.label" class="rounded-3xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800">
        <div class="flex items-center justify-between">
          <p class="text-sm font-medium text-slate-500">{{ card.label }}</p>
          <component :is="card.icon" class="h-5 w-5" :class="card.tone === 'emerald' ? 'text-emerald-600' : card.tone === 'rose' ? 'text-rose-600' : 'text-amber-600'" />
        </div>
        <p class="mt-3 text-2xl font-black text-slate-800 dark:text-white">{{ formatVND(card.value) }}</p>
      </div>
    </div>

    <div class="rounded-3xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <div class="grid gap-4 xl:grid-cols-5">
        <label class="text-sm font-medium text-slate-600 dark:text-slate-300">
          <span class="mb-1 block">Thời gian</span>
          <select v-model="filterTime" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm">
            <option v-for="item in TIME_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </label>
        <label class="text-sm font-medium text-slate-600 dark:text-slate-300">
          <span class="mb-1 block">Loại phiếu</span>
          <select v-model="filterType" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm">
            <option v-for="item in VIEW_MODE_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </label>
        <label class="text-sm font-medium text-slate-600 dark:text-slate-300">
          <span class="mb-1 block">Hình thức</span>
          <select v-model="filterPayment" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm">
            <option v-for="item in PAYMENT_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </label>
        <label v-if="isAdmin" class="text-sm font-medium text-slate-600 dark:text-slate-300">
          <span class="mb-1 block">Nhân viên tạo</span>
          <select v-model="filterEmployee" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm">
            <option value="all">Tất cả</option>
            <option v-for="item in employeeOptions" :key="item.id" :value="item.id">{{ item.label }}</option>
          </select>
        </label>
        <label v-if="isAdmin" class="text-sm font-medium text-slate-600 dark:text-slate-300">
          <span class="mb-1 block">Trạng thái</span>
          <select v-model="filterStatus" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm">
            <option value="all">Tất cả</option>
            <option value="Đã duyệt">Đã duyệt</option>
            <option value="Đã xác nhận">Đã xác nhận</option>
            <option value="Đã hủy">Đã hủy</option>
          </select>
        </label>
      </div>

      <div class="mt-4 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <label class="flex-1 text-sm font-medium text-slate-600 dark:text-slate-300">
          <span class="mb-1 block">Tìm kiếm</span>
          <input v-model="searchText" placeholder="Mã phiếu, hạng mục, người nhận..." class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm" />
        </label>
        <div v-if="filterTime === 'custom'" class="flex flex-wrap gap-2">
          <label class="text-sm text-slate-600">
            <span class="mb-1 block">Từ</span>
            <input v-model="customFrom" type="date" class="rounded-xl border border-slate-200 px-3 py-2 text-sm" />
          </label>
          <label class="text-sm text-slate-600">
            <span class="mb-1 block">Đến</span>
            <input v-model="customTo" type="date" class="rounded-xl border border-slate-200 px-3 py-2 text-sm" />
          </label>
        </div>
      </div>
    </div>

    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-slate-200 text-sm dark:divide-slate-700">
          <thead class="bg-slate-50 dark:bg-slate-900/40">
            <tr>
              <th class="px-4 py-3 text-left font-semibold text-slate-600">Mã Phiếu</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-600">Thời Gian</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-600">Loại Phiếu</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-600">Hạng Mục</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-600">Số Tiền</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-600">Hình Thức</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-600">Người Tạo</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-600">Trạng Thái</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-600">Hành Động</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 dark:divide-slate-700/50">
            <tr v-for="record in visibleRecords" :key="record.id" class="hover:bg-slate-50/50 dark:hover:bg-slate-800/50">
              <td class="px-4 py-3 font-semibold text-primary">{{ record.maPhieu }}</td>
              <td class="px-4 py-3 text-slate-600 dark:text-slate-300">
                <div class="flex items-center gap-2">
                  <CalendarClock class="h-4 w-4 text-slate-400" />
                  {{ formatDateTime(record.thoiGian) }}
                </div>
              </td>
              <td class="px-4 py-3">
                <span class="rounded-full px-2.5 py-1 text-xs font-semibold" :class="record.loaiPhieu === 'THU' ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'">
                  {{ record.loaiPhieu === 'THU' ? 'Phiếu Thu' : 'Phiếu Chi' }}
                </span>
              </td>
              <td class="px-4 py-3 text-slate-700 dark:text-slate-200">{{ record.hangMuc || record['hạngMục'] }}</td>
              <td class="px-4 py-3 font-semibold" :class="record.loaiPhieu === 'THU' ? 'text-emerald-600' : 'text-rose-600'">{{ formatVND(record.soTien) }}</td>
              <td class="px-4 py-3 text-slate-600 dark:text-slate-300">{{ record.hinhThuc }}</td>
              <td class="px-4 py-3 text-slate-600 dark:text-slate-300">{{ record.nguoiTaoTen || 'Bạn' }}</td>
              <td class="px-4 py-3">
                <span class="rounded-full px-2.5 py-1 text-xs font-semibold" :class="record.trangThai === 'Đã hủy' ? 'bg-slate-100 text-slate-700' : record.trangThai === 'Đã xác nhận' ? 'bg-sky-100 text-sky-700' : 'bg-emerald-100 text-emerald-700'">
                  {{ record.trangThai }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex flex-wrap gap-2">
                  <button v-if="isAdmin && record.trangThai !== 'Đã hủy'" class="rounded-lg bg-slate-100 p-2 text-slate-700 hover:bg-slate-200" @click="openEditModal(record)" title="Sửa">
                    <PencilLine class="h-4 w-4" />
                  </button>
                  <button v-if="isAdmin && record.trangThai !== 'Đã hủy'" class="rounded-lg bg-rose-100 p-2 text-rose-700 hover:bg-rose-200" @click="handleDelete(record)" title="Hủy">
                    <Trash2 class="h-4 w-4" />
                  </button>
                  <span v-if="!isAdmin" class="rounded-lg bg-sky-100 px-2.5 py-2 text-xs font-semibold text-sky-700">Chỉ xem / đã xác nhận</span>
                </div>
              </td>
            </tr>
            <tr v-if="!visibleRecords.length">
              <td colspan="9" class="px-4 py-10 text-center text-slate-500">
                <div class="flex flex-col items-center gap-2">
                  <ReceiptText class="h-8 w-8 text-slate-300" />
                  <span>Không có dữ liệu phù hợp.</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showFormModal" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-4">
      <div class="w-full max-w-2xl rounded-[24px] bg-white p-6 shadow-2xl dark:bg-slate-800">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-xl font-black uppercase tracking-wide text-slate-800 dark:text-white">{{ editingRecordId ? 'Cập nhật phiếu' : (form.loaiPhieu === 'THU' ? 'Tạo phiếu thu' : 'Tạo phiếu chi') }}</h3>
            <p class="mt-1 text-sm text-slate-500">Mã ca hiện tại: {{ currentShiftLabel }} | shift_id: {{ currentShiftId }}</p>
          </div>
          <button class="rounded-full p-2 text-slate-400 hover:bg-slate-100" @click="closeModal">
            <X class="h-5 w-5" />
          </button>
        </div>

        <div class="mt-6 grid gap-4 md:grid-cols-2">
          <label class="text-sm font-medium text-slate-600 dark:text-slate-300">
            <span class="mb-1 block">Mã phiếu</span>
            <input v-model="form.maPhieu" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm" disabled />
          </label>
          <label class="text-sm font-medium text-slate-600 dark:text-slate-300">
            <span class="mb-1 block">Thời gian</span>
            <input v-model="form.thoiGian" type="datetime-local" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm" />
          </label>
          <label class="text-sm font-medium text-slate-600 dark:text-slate-300">
            <span class="mb-1 block">Giá trị phiếu {{ form.loaiPhieu === 'THU' ? 'thu' : 'chi' }} *</span>
            <input v-model="form.soTien" type="number" min="0" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm" />
          </label>
          <label class="text-sm font-medium text-slate-600 dark:text-slate-300">
            <span class="mb-1 block">Hạng mục *</span>
            <select v-model="form.hangMuc" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm">
              <option v-for="item in categories.filter((category) => form.loaiPhieu === 'CHI' ? DEFAULT_EXPENSE_CATEGORIES.includes(category) || category.includes('ship') || category.includes('sửa') || category.includes('Admin') : DEFAULT_INCOME_CATEGORIES.includes(category) || category.includes('Thu'))" :key="item" :value="item">{{ item }}</option>
            </select>
          </label>
          <label class="text-sm font-medium text-slate-600 dark:text-slate-300">
            <span class="mb-1 block">Hình thức thanh toán *</span>
            <select v-model="form.hinhThuc" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm">
              <option value="Tiền mặt">Tiền mặt</option>
              <option value="Chuyển khoản">Chuyển khoản</option>
            </select>
          </label>
          <label class="text-sm font-medium text-slate-600 dark:text-slate-300">
            <span class="mb-1 block">Người nhận tiền</span>
            <input v-model="form.nguoiNhan" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm" />
          </label>
          <label class="md:col-span-2 text-sm font-medium text-slate-600 dark:text-slate-300">
            <span class="mb-1 block">Ghi chú</span>
            <textarea v-model="form.ghiChu" rows="3" class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm" />
          </label>
        </div>

        <div class="mt-6 flex justify-end gap-2">
          <button class="rounded-xl bg-slate-200 px-4 py-2 text-sm font-semibold text-slate-700" @click="closeModal">Hủy bỏ</button>
          <button class="rounded-xl bg-primary px-4 py-2 text-sm font-semibold text-white" @click="saveRecord">{{ editingRecordId ? 'Cập nhật' : (form.loaiPhieu === 'THU' ? 'Lưu phiếu thu' : 'Lưu phiếu chi') }}</button>
        </div>
      </div>
    </div>

    <div v-if="showConfigModal" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-4">
      <div class="w-full max-w-xl rounded-[24px] bg-white p-6 shadow-2xl dark:bg-slate-800">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-xl font-black uppercase tracking-wide text-slate-800 dark:text-white">Cấu hình loại quỹ</h3>
            <p class="mt-1 text-sm text-slate-500">Tạo các hạng mục thu/chi mẫu để dùng lại cho các phiếu sau.</p>
          </div>
          <button class="rounded-full p-2 text-slate-400 hover:bg-slate-100" @click="closeModal">
            <X class="h-5 w-5" />
          </button>
        </div>

        <div class="mt-6 flex gap-2">
          <input v-model="newCategory" placeholder="Nhập hạng mục mới" class="flex-1 rounded-xl border border-slate-200 px-3 py-2 text-sm" />
          <button class="rounded-xl bg-primary px-4 py-2 text-sm font-semibold text-white" @click="addCategory">Thêm</button>
        </div>

        <div class="mt-4 grid gap-2">
          <div v-for="item in categories" :key="item" class="rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-700 dark:border-slate-700 dark:text-slate-200">
            {{ item }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
