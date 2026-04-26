<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  Eye, FileSpreadsheet, Filter, Plus, RotateCcw, Search, Ticket, ToggleLeft, ToggleRight, UserSearch,
  CheckCircle2, CircleX, X
} from "lucide-vue-next";
import {
  createPhieuGiamGia,
  deletePhieuGiamGia,
  getPhieuGiamGiaDetail,
  getPhieuGiamGiaList,
  updatePhieuGiamGia,
  getPhieuGiamGiaKhachHangList,
  getPhieuGiamGiaKhachHangDetail,
  createPhieuGiamGiaKhachHang,
  updatePhieuGiamGiaKhachHang,
  deletePhieuGiamGiaKhachHang,
  getEmailSuggestions
} from "../../../services/khuyen-mai";
import { layChiTietKhachHang } from "../../../services/khach-hang";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { getDisplayErrorMessage } from "../../../utils/error-message";

const tabs = [
  { key: "phieu", label: "Phiếu giảm giá" },
  { key: "khach-hang", label: "Phiếu khách hàng" }
];

const activeTab = ref("phieu");
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
const boLocKh = ref({ keyword: "", trangThai: "" });

const danhSach = ref([]);
const tongSoTrang = ref(1);
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const totalItems = ref(0);

const danhSachKh = ref([]);
const tongSoTrangKh = ref(1);
const soPhanTuMotTrangKh = ref(5);
const trangHienTaiKh = ref(1);
const totalItemsKh = ref(0);

const dsTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Đang hoạt động", value: "1" },
  { label: "Ngưng hoạt động", value: "0" },
];

const phieuOptions = ref([]);
const emailOptions = ref([]);

const khForm = reactive({
  id: null, phieuGiamGiaId: "", email: "", ngaySuDung: "", trangThai: "1", ngayTao: ""
});

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

watch(activeTab, (newTab) => {
  if (newTab === 'phieu') {
    trangHienTai.value = 1;
    taiDanhSach();
  } else {
    trangHienTaiKh.value = 1;
    taiDanhSachKh();
  }
});

watch(soPhanTuMotTrang, () => { trangHienTai.value = 1; taiDanhSach(); });
watch(trangHienTai, taiDanhSach);

watch(soPhanTuMotTrangKh, () => { trangHienTaiKh.value = 1; taiDanhSachKh(); });
watch(trangHienTaiKh, taiDanhSachKh);

let timer;
watch(() => boLoc.value, () => {
  clearTimeout(timer);
  timer = setTimeout(() => { trangHienTai.value = 1; taiDanhSach(); }, 300);
}, { deep: true });

let timerKh;
watch(() => boLocKh.value, () => {
  clearTimeout(timerKh);
  timerKh = setTimeout(() => { trangHienTaiKh.value = 1; taiDanhSachKh(); }, 300);
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

async function taiDanhSachKh() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const data = await getPhieuGiamGiaKhachHangList({
      keyword: boLocKh.value.keyword || undefined,
      trangThai: boLocKh.value.trangThai !== "" ? Number(boLocKh.value.trangThai) : undefined,
      pageNo: trangHienTaiKh.value - 1,
      pageSize: soPhanTuMotTrangKh.value
    });
    danhSachKh.value = data?.content || [];
    tongSoTrangKh.value = data?.totalPages || 1;
    totalItemsKh.value = data?.totalElements || 0;

    // Fetch optional options in background
    getPhieuGiamGiaList({ pageNo: 0, pageSize: 1000, trangThai: 1 })
      .then(res => phieuOptions.value = res?.content || [])
      .catch(err => console.error("Error loading phieu options:", err));

    getEmailSuggestions()
      .then(res => emailOptions.value = Array.isArray(res) ? res : [])
      .catch(err => console.error("Error loading email suggestions:", err));

  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải danh sách phiếu giảm giá khách hàng");
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  if (activeTab.value === 'phieu') {
    boLoc.value = { keyword: "", trangThai: "", tuNgay: "", denNgay: "", loai: "" };
  } else {
    boLocKh.value = { keyword: "", trangThai: "" };
  }
}

async function xuatExcel() {
  try {
    if (activeTab.value === "phieu") {
      const data = await getPhieuGiamGiaList({
        keyword: boLoc.value.keyword || undefined,
        trangThai: boLoc.value.trangThai !== "" ? Number(boLoc.value.trangThai) : undefined,
        loai: boLoc.value.loai !== "" ? Number(boLoc.value.loai) : undefined,
        tuNgay: boLoc.value.tuNgay || undefined,
        denNgay: boLoc.value.denNgay || undefined,
        pageNo: 0,
        pageSize: Math.max(totalItems.value || 0, soPhanTuMotTrang.value, 1),
      });

      const rows = data?.content || [];
      if (!rows.length) {
        window.alert("Không có dữ liệu để xuất Excel.");
        return;
      }

      exportRowsToExcel({
        filename: "quan-ly-phieu-giam-gia",
        sheetName: "PhieuGiamGia",
        columns: [
          { label: "STT", value: (_, index) => index + 1 },
          { label: "Mã", key: "ma" },
          { label: "Tên", key: "ten" },
          { label: "Giảm tối đa", value: (row) => row.giamToiDa ? `${row.giamToiDa.toLocaleString('vi-VN')}đ` : "0đ" },
          { label: "Loại phiếu", value: (row) => Number(row.loaiPhieu) === 1 ? "Công khai" : "Cá nhân" },
          { label: "Loại giảm", value: (row) => Number(row.loai) === 1 ? "Phần trăm" : "Tiền mặt" },
          { label: "Giá trị", value: (row) => Number(row.loai) === 1 ? `${row.giaTri}%` : `${Number(row.giaTri).toLocaleString('vi-VN')}đ` },
          { label: "Số lượng", key: "soLuong" },
          { label: "Ngày bắt đầu", value: (row) => toDisplayDate(row.ngayBatDau) },
          { label: "Ngày kết thúc", value: (row) => toDisplayDate(row.ngayKetThuc) },
          { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
        ],
        rows,
      });
      return;
    }

    const data = await getPhieuGiamGiaKhachHangList({
      keyword: boLocKh.value.keyword || undefined,
      trangThai: boLocKh.value.trangThai !== "" ? Number(boLocKh.value.trangThai) : undefined,
      pageNo: 0,
      pageSize: Math.max(totalItemsKh.value || 0, soPhanTuMotTrangKh.value, 1),
    });

    const rows = data?.content || [];
    if (!rows.length) {
      window.alert("Không có dữ liệu để xuất Excel.");
      return;
    }

    exportRowsToExcel({
      filename: "quan-ly-phieu-khach-hang",
      sheetName: "PhieuKhachHang",
      columns: [
        { label: "STT", value: (_, index) => index + 1 },
        { label: "Mã phiếu", key: "maPhieuGiamGia" },
        { label: "Tên phiếu", key: "tenPhieuGiamGia" },
        { label: "Khách hàng", value: (row) => row.tenKhachHang || "—" },
        { label: "Ngày tạo", value: (row) => toDisplayDate(row.ngayTao) },
        { label: "Ngày sử dụng", value: (row) => row.ngaySuDung ? toDisplayDate(row.ngaySuDung) : "—" },
        { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
      ],
      rows,
    });
  } catch (error) {
    window.alert(getDisplayErrorMessage(error, "Không thể xuất Excel phiếu giảm giá"));
  }
}

function openCreateModal(target) {
  if (target === "phieu") {
    router.push({ name: "admin-phieu-giam-gia-them" });
  } else {
    router.push({ name: "admin-phieu-giam-gia-khach-hang-them" });
  }
}

async function openEditModal(target, item) {
  if (target === "phieu") {
    router.push({ name: "admin-phieu-giam-gia-chi-tiet", params: { id: item.id } });
  } else {
    router.push({ name: "admin-phieu-giam-gia-khach-hang-chi-tiet", params: { id: item.id } });
  }
}

async function removeItem(target, item) {
  if (!confirm(`Bạn có chắc muốn xóa phiếu này?`)) return;
  try {
    if (target === "phieu") {
      await deletePhieuGiamGia(item.id);
      taiDanhSach();
    } else {
      await deletePhieuGiamGiaKhachHang(item.id);
      taiDanhSachKh();
    }
    hienThiThongBao("success", "Xóa thành công");
  } catch (error) {
    hienThiThongBao("error", "Xóa thất bại", getDisplayErrorMessage(error, "Không thể xóa phiếu giảm giá"));
  }
}

async function submitForm() {
  resetErrors();
  let isValid = true;
  if (!khForm.phieuGiamGiaId) { formErrors.phieuGiamGiaId = "Chọn phiếu"; isValid = false; }
  if (!khForm.email || !khForm.email.includes('@')) { formErrors.email = "Email không hợp lệ"; isValid = false; }

  if (!isValid) return;

  saving.value = true;
  try {
    const payload = {
      phieuGiamGiaId: Number(khForm.phieuGiamGiaId),
      email: khForm.email.trim(),
      ngaySuDung: khForm.ngaySuDung,
      trangThai: Number(khForm.trangThai),
      ngayTao: modalMode.value === "create" ? getToday() : undefined
    };

    if (modalMode.value === "create") await createPhieuGiamGiaKhachHang(payload);
    else await updatePhieuGiamGiaKhachHang(khForm.id, payload);

    modalOpen.value = false;
    hienThiThongBao("success", "Lưu thành công");
    taiDanhSachKh();
  } catch (error) {
    hienThiThongBao("error", "Lưu thất bại", getDisplayErrorMessage(error, "Không thể lưu phiếu giảm giá khách hàng"));
  } finally {
    saving.value = false;
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

async function nhanhDoiTrangThaiKh(item) {
  if (!confirm("Bạn có chắc chắn muốn thay đổi trạng thái của phiếu tặng này?")) return;
  try {
    const detail = await getPhieuGiamGiaKhachHangDetail(item.id);
    detail.trangThai = Number(detail.trangThai) === 1 ? 0 : 1;
    // Tương tự cho phiếu khách hàng
    detail.phieuGiamGiaId = Number(detail.phieuGiamGiaId);
    detail.trangThai = Number(detail.trangThai);
    
    await updatePhieuGiamGiaKhachHang(item.id, detail);
    hienThiThongBao("success", "Đổi trạng thái thành công");
    await taiDanhSachKh();
  } catch (error) {
    hienThiThongBao("error", "Đổi trạng thái thất bại", getDisplayErrorMessage(error, "Không thể cập nhật trạng thái phiếu giảm giá khách hàng"));
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
                v-model="(activeTab === 'phieu' ? boLoc : boLocKh).keyword"
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
            <select v-model="(activeTab === 'phieu' ? boLoc : boLocKh).trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
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
          <button @click="openCreateModal(activeTab)" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-6 text-sm font-semibold text-white transition hover:bg-rose-600 shadow-lg shadow-rose-200 whitespace-nowrap">
            <Plus class="h-4 w-4" /> {{ activeTab === 'phieu' ? 'Thêm phiếu giảm giá' : 'Tặng phiếu khách hàng' }}
          </button>
        </div>
      </div>
    </section>

    <!-- Danh sách -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center justify-between">
        <div class="inline-flex rounded-2xl bg-slate-100 p-1">
          <button v-for="tab in tabs" :key="tab.key" class="rounded-2xl px-5 py-2 text-sm font-semibold transition whitespace-nowrap" :class="activeTab === tab.key ? 'bg-white text-rose-600 shadow-sm' : 'text-slate-600 hover:text-slate-900'" @click="activeTab = tab.key">
            {{ tab.label }}
          </button>
        </div>
        <div>
          <p class="text-sm text-slate-400 font-medium">{{ activeTab === 'phieu' ? totalItems : totalItemsKh }} bản ghi hiển thị.</p>
        </div>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <!-- Bảng Phiếu giảm giá chung -->
        <table v-if="activeTab === 'phieu'" class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã</th>
              <th class="bg-slate-100 px-4 py-3">Tên</th>
              <th class="bg-slate-100 px-4 py-3">Loại phiếu</th>
              <th class="bg-slate-100 px-4 py-3">Loại giảm</th>
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
              <td colspan="11" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="!danhSach.length">
              <td colspan="11" class="py-10 text-center text-sm text-slate-400">Không có dữ liệu.</td>
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
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold whitespace-nowrap" :class="mauLoaiGiam(item.loai)">
                  {{ loaiGiamText(item.loai) }}
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
                  <button @click="openEditModal('phieu', item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500" title="Xem chi tiết">
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

        <!-- Bảng Phiếu giảm giá khách hàng -->
        <table v-else class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã phiếu</th>
              <th class="bg-slate-100 px-4 py-3">Tên phiếu</th>
              <th class="bg-slate-100 px-4 py-3">Khách hàng</th>
              <th class="bg-slate-100 px-4 py-3">Ngày tặng</th>
              <th class="bg-slate-100 px-4 py-3">Ngày dùng</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="!danhSachKh.length">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">Không có dữ liệu.</td>
            </tr>
            <tr v-for="(item, index) in danhSachKh" :key="item.id" class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100">
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTaiKh - 1) * soPhanTuMotTrangKh + index + 1 }}</td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ item.maPhieuGiamGia }}</td>
              <td class="px-4 py-3 text-slate-600">{{ item.tenPhieuGiamGia }}</td>
              <td class="px-4 py-3 font-semibold text-rose-600">{{ item.tenKhachHang }}</td>
              <td class="px-4 py-3 text-slate-500 text-[12px]">{{ toDisplayDate(item.ngayTao) }}</td>
              <td class="px-4 py-3 text-slate-500 text-[12px]">
                <span v-if="item.ngaySuDung" class="font-medium text-emerald-600">{{ toDisplayDate(item.ngaySuDung) }}</span>
                <span v-else class="text-slate-300 italic">Chưa dùng</span>
              </td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold whitespace-nowrap" :class="mauTrangThai(item.trangThai)">
                  {{ statusText(item.trangThai) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex justify-center items-center gap-3">
                  <button @click="openEditModal('khach-hang', item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500" title="Xem chi tiết">
                    <Eye class="h-5 w-5" />
                  </button>
                  <button @click="nhanhDoiTrangThaiKh(item)" class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none" :class="Number(item.trangThai) === 1 ? 'bg-rose-500' : 'bg-slate-200'" :title="Number(item.trangThai) === 1 ? 'Ngưng hoạt động' : 'Kích hoạt'">
                    <span class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200" :class="Number(item.trangThai) === 1 ? 'translate-x-5' : 'translate-x-0'"></span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminTableFooter
        v-if="activeTab === 'phieu'"
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
      <AdminTableFooter
        v-else
        :current-page="trangHienTaiKh"
        :page-size="soPhanTuMotTrangKh"
        :page-size-options="[5, 10, 20]"
        :total-items="totalItemsKh"
        :total-pages="tongSoTrangKh"
        compact
        show-refresh
        @refresh="taiDanhSachKh"
        @update:current-page="trangHienTaiKh = $event"
        @update:page-size="soPhanTuMotTrangKh = $event"
      />
    </section>

  </div>
</template>

