<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  CheckCircle2,
  CircleX,
  Eye,
  FileSpreadsheet,
  Filter,
  Plus,
  RotateCcw,
  Search,
  X,
} from "lucide-vue-next";
import {
  getPhieuGiamGiaKhachHangList,
  getPhieuGiamGiaList,
  updatePhieuGiamGia,
  updatePhieuGiamGiaKhachHang,
} from "../../../services/khuyen-mai";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import AdminQuickStatusAction from "../../../components/common/AdminQuickStatusAction.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { getDisplayErrorMessage } from "../../../utils/error-message";

const router = useRouter();
const route = useRoute();
const dangTai = ref(false);
const loiTrang = ref("");

function resolveActiveTab() {
  if (
    route.name === "admin-phieu-giam-gia-khach-hang" ||
    route.query.tab === "khach-hang"
  ) {
    return "khach-hang";
  }

  return "phieu";
}

const activeTab = ref(resolveActiveTab());
const toast = ref({
  hienThi: false,
  loai: "success",
  tieuDe: "",
  noiDung: "",
});
let toastTimer = null;

const toastClass = computed(() => {
  if (toast.value.loai === "success") {
    return "border-emerald-100 bg-emerald-50 text-emerald-700";
  }
  if (toast.value.loai === "warning") {
    return "border-amber-100 bg-amber-50 text-amber-700";
  }
  return "border-rose-100 bg-rose-50 text-rose-700";
});

const toastIconClass = computed(() => {
  if (toast.value.loai === "success") {
    return "bg-emerald-100 text-emerald-600";
  }
  if (toast.value.loai === "warning") {
    return "bg-amber-100 text-amber-600";
  }
  return "bg-rose-100 text-rose-600";
});

const toastAccentClass = computed(() => {
  if (toast.value.loai === "success") {
    return "bg-emerald-500";
  }
  if (toast.value.loai === "warning") {
    return "bg-amber-500";
  }
  return "bg-rose-500";
});

const ToastIcon = computed(() => {
  if (toast.value.loai === "success") {
    return CheckCircle2;
  }
  return CircleX;
});

function hienThiThongBao(loai, tieuDe, noiDung = "") {
  if (toastTimer) {
    clearTimeout(toastTimer);
  }

  toast.value = { hienThi: true, loai, tieuDe, noiDung };
  toastTimer = setTimeout(() => {
    toast.value.hienThi = false;
  }, 3200);
}

const boLoc = ref({
  keyword: "",
  trangThai: "",
  tuNgay: "",
  denNgay: "",
  loai: "",
});
const boLocKh = ref({
  keyword: "",
  trangThai: "",
});

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
  { label: "Hết hạn", value: "het_han" },
  { label: "Hết số lượng", value: "3" },
  { label: "Sắp diễn ra", value: "4" },
];

const dsLoai = [
  { label: "Tất cả", value: "" },
  { label: "Phần trăm", value: "1" },
  { label: "Tiền mặt", value: "2" },
];

function isHetHan(ngayKetThuc) {
  if (!ngayKetThuc) return false;
  const homNay = new Date();
  homNay.setHours(0, 0, 0, 0);
  return new Date(ngayKetThuc) < homNay;
}

function mauTrangThai(trangThai, ngayKetThuc) {
  if (isHetHan(ngayKetThuc)) return "bg-rose-50 text-rose-600 ring-1 ring-rose-100";
  const status = Number(trangThai);
  if (status === 1) return "bg-emerald-50 text-emerald-600 ring-1 ring-emerald-100";
  if (status === 2) return "bg-slate-50 text-slate-600 ring-1 ring-slate-200";
  if (status === 3) return "bg-orange-50 text-orange-600 ring-1 ring-orange-200";
  if (status === 4) return "bg-blue-50 text-blue-600 ring-1 ring-blue-200";
  return "bg-rose-50 text-rose-600 ring-1 ring-rose-100";
}

function statusText(value, ngayKetThuc) {
  if (isHetHan(ngayKetThuc)) return "Hết hạn";
  const status = Number(value);
  if (status === 1) return "Đang hoạt động";
  if (status === 2) return "Hết hạn";
  if (status === 3) return "Hết số lượng";
  if (status === 4) return "Sắp diễn ra";
  return "Ngưng hoạt động";
}

function loaiGiamText(loai) {
  return Number(loai) === 1 ? "Phần trăm" : "Tiền mặt";
}

function loaiPhieuText(loaiPhieu) {
  return Number(loaiPhieu) === 2 ? "Cá nhân" : "Công khai";
}

function formatGiaTri(giaTri, loai) {
  if (giaTri == null || giaTri === "") {
    return "0";
  }

  return Number(loai) === 1
    ? `${giaTri}%`
    : `${Number(giaTri).toLocaleString("vi-VN")}đ`;
}

function formatTien(tien) {
  if (tien == null || tien === "") {
    return "0đ";
  }

  return `${Number(tien).toLocaleString("vi-VN")}đ`;
}

function toDisplayDate(value) {
  if (!value) {
    return "—";
  }

  return new Date(value).toLocaleDateString("vi-VN");
}

function soLuongDaDung(item) {
  return Number(item?.soLuongDaDung || 0);
}

function soLuongConLai(item) {
  return Math.max(Number(item?.soLuong || 0) - soLuongDaDung(item), 0);
}

watch(activeTab, (newTab) => {
  if (newTab === "phieu") {
    trangHienTai.value = 1;
    taiDanhSach();
    return;
  }

  trangHienTaiKh.value = 1;
  taiDanhSachKh();
});

watch(soPhanTuMotTrang, () => {
  trangHienTai.value = 1;
  taiDanhSach();
});

watch(trangHienTai, taiDanhSach);

watch(soPhanTuMotTrangKh, () => {
  trangHienTaiKh.value = 1;
  taiDanhSachKh();
});

watch(trangHienTaiKh, taiDanhSachKh);

let timer;
watch(
  boLoc,
  () => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      trangHienTai.value = 1;
      taiDanhSach();
    }, 300);
  },
  { deep: true },
);

watch(
  boLocKh,
  () => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      trangHienTaiKh.value = 1;
      taiDanhSachKh();
    }, 300);
  },
  { deep: true },
);

watch(
  () => [route.name, route.query.tab],
  () => {
    const nextTab = resolveActiveTab();
    if (nextTab !== activeTab.value) {
      activeTab.value = nextTab;
    }
  },
);

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";

  try {
    // "het_han" là filter FE tự xử lý, không gửi lên backend
    const isFilterHetHan = boLoc.value.trangThai === "het_han";

    const data = await getPhieuGiamGiaList({
      keyword: boLoc.value.keyword || undefined,
      trangThai: (!isFilterHetHan && boLoc.value.trangThai !== "")
        ? Number(boLoc.value.trangThai)
        : undefined,
      loai: boLoc.value.loai !== "" ? Number(boLoc.value.loai) : undefined,
      tuNgay: boLoc.value.tuNgay || undefined,
      denNgay: boLoc.value.denNgay || undefined,
      pageNo: trangHienTai.value - 1,
      pageSize: isFilterHetHan ? 1000 : soPhanTuMotTrang.value,
    });

    let items = data?.content || [];

    if (isFilterHetHan) {
      // Lọc FE: chỉ lấy phiếu quá ngày kết thúc
      items = items.filter(item => isHetHan(item.ngayKetThuc));
    } else if (boLoc.value.trangThai === "1") {
      // Đang hoạt động: loại bỏ phiếu đã hết hạn theo ngày
      items = items.filter(item => !isHetHan(item.ngayKetThuc));
    }

    tongSoTrang.value = Math.max(1, Math.ceil(items.length / soPhanTuMotTrang.value));
    totalItems.value = items.length;
    const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
    danhSach.value = isFilterHetHan || boLoc.value.trangThai === "1"
      ? items.slice(start, start + soPhanTuMotTrang.value)
      : items;

    if (!isFilterHetHan && boLoc.value.trangThai !== "1") {
      danhSach.value = items;
      tongSoTrang.value = data?.totalPages || 1;
      totalItems.value = data?.totalElements || 0;
    }
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(
      error,
      "Không thể tải danh sách phiếu giảm giá",
    );
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
      trangThai:
        boLocKh.value.trangThai !== ""
          ? Number(boLocKh.value.trangThai)
          : undefined,
      pageNo: trangHienTaiKh.value - 1,
      pageSize: soPhanTuMotTrangKh.value,
    });
    danhSachKh.value = data?.content || [];
    tongSoTrangKh.value = data?.totalPages || 1;
    totalItemsKh.value = data?.totalElements || 0;
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(
      error,
      "Không thể tải danh sách phiếu khách hàng",
    );
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  if (activeTab.value === "phieu") {
    boLoc.value = {
      keyword: "",
      trangThai: "",
      tuNgay: "",
      denNgay: "",
      loai: "",
    };
    return;
  }

  boLocKh.value = {
    keyword: "",
    trangThai: "",
  };
}

async function nhanhDoiTrangThai(item) {
  if (isHetHan(item.ngayKetThuc)) return;
  try {
    const nextStatus = Number(item.trangThai) === 1 ? 0 : 1;
    await updatePhieuGiamGia(item.id, {
      ma: item.ma,
      ten: item.ten,
      loai: item.loai,
      loaiPhieu: item.loaiPhieu,
      giaTri: item.giaTri,
      giaTriToiThieu: item.giaTriToiThieu || null,
      giamToiDa: item.giamToiDa || null,
      ngayBatDau: item.ngayBatDau,
      ngayKetThuc: item.ngayKetThuc,
      soLuong: item.soLuong,
      soLuongDaDung: item.soLuongDaDung || 0,
      trangThai: nextStatus,
    });
    hienThiThongBao("success", "Cập nhật phiếu thành công");
    await taiDanhSach();
  } catch (error) {
    hienThiThongBao(
      "error",
      "Cập nhật thất bại",
      getDisplayErrorMessage(error, "Không thể thay đổi trạng thái phiếu giảm giá"),
    );
  }
}

async function nhanhDoiTrangThaiKh(item) {
  if (Number(item.trangThai) === 0) return;
  try {
    const nextStatus = 0;
    await updatePhieuGiamGiaKhachHang(item.id, {
      ...item,
      trangThai: nextStatus,
    });
    hienThiThongBao("success", "Cập nhật liên kết thành công");
    taiDanhSachKh();
  } catch (error) {
    hienThiThongBao(
      "error",
      "Cập nhật thất bại",
      getDisplayErrorMessage(
        error,
        "Không thể thay đổi trạng thái phiếu khách hàng",
      ),
    );
  }
}

function openCreateModal() {
  router.push({
    name:
      activeTab.value === "khach-hang"
        ? "admin-phieu-giam-gia-khach-hang-them"
        : "admin-phieu-giam-gia-them",
  });
}

function openEditModal(target, itemArg) {
  const item = typeof target === "string" ? itemArg : target;
  if (!item?.id) {
    return;
  }

  router.push({
    name:
      target === "khach-hang"
        ? "admin-phieu-giam-gia-khach-hang-chi-tiet"
        : "admin-phieu-giam-gia-chi-tiet",
    params: { id: item.id },
  });
}

async function xuatExcel() {
  try {
    if (activeTab.value === "phieu") {
      const data = await getPhieuGiamGiaList({
        keyword: boLoc.value.keyword || undefined,
        trangThai:
          boLoc.value.trangThai !== ""
            ? Number(boLoc.value.trangThai)
            : undefined,
        loai: boLoc.value.loai !== "" ? Number(boLoc.value.loai) : undefined,
        tuNgay: boLoc.value.tuNgay || undefined,
        denNgay: boLoc.value.denNgay || undefined,
        pageNo: 0,
        pageSize: 1000,
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
          { label: "Tên phiếu", key: "ten" },
          {
            label: "Hình thức phiếu",
            value: (row) => loaiPhieuText(row.loaiPhieu),
          },
          {
            label: "Loại giảm",
            value: (row) => loaiGiamText(row.loai),
          },
          {
            label: "Giá trị giảm",
            value: (row) => formatGiaTri(row.giaTri, row.loai),
          },
          {
            label: "Giá trị đơn tối thiểu",
            value: (row) => formatTien(row.giaTriToiThieu),
          },
          {
            label: "Giảm tối đa",
            value: (row) =>
              Number(row.giamToiDa) > 0
                ? formatTien(row.giamToiDa)
                : "Không giới hạn",
          },
          { label: "Phát hành", value: (row) => Number(row.soLuong || 0) },
          { label: "Đã dùng", value: (row) => soLuongDaDung(row) },
          { label: "Còn lại", value: (row) => soLuongConLai(row) },
          {
            label: "Ngày bắt đầu",
            value: (row) => toDisplayDate(row.ngayBatDau),
          },
          {
            label: "Ngày kết thúc",
            value: (row) => toDisplayDate(row.ngayKetThuc),
          },
          { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
        ],
        rows,
      });
      return;
    }

    const data = await getPhieuGiamGiaKhachHangList({
      keyword: boLocKh.value.keyword || undefined,
      trangThai:
        boLocKh.value.trangThai !== ""
          ? Number(boLocKh.value.trangThai)
          : undefined,
      pageNo: 0,
      pageSize: 1000,
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
        { label: "Khách hàng", key: "tenKhachHang" },
        { label: "Ngày tặng", value: (row) => toDisplayDate(row.ngayTao) },
        { label: "Ngày dùng", value: (row) => toDisplayDate(row.ngaySuDung) },
        { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
      ],
      rows,
    });
  } catch (error) {
    window.alert(error?.message || "Xuất Excel thất bại.");
  }
}

onMounted(() => {
  if (activeTab.value === "khach-hang") {
    taiDanhSachKh();
    return;
  }

  taiDanhSach();
});
</script>

<template>
  <div class="space-y-5">
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
          <div
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full"
            :class="toastIconClass"
          >
            <component :is="ToastIcon" class="h-5 w-5" />
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-sm font-bold text-slate-800">{{ toast.tieuDe }}</p>
            <p
              v-if="toast.noiDung"
              class="mt-1 text-sm leading-5 text-slate-600"
            >
              {{ toast.noiDung }}
            </p>
          </div>
          <button
            type="button"
            class="rounded-full p-1 text-slate-400 transition hover:bg-white/70 hover:text-slate-600"
            @click="toast.hienThi = false"
          >
            <X class="h-4 w-4" />
          </button>
        </div>
        <div class="h-1.5 w-full" :class="toastAccentClass"></div>
      </div>
    </Transition>

    <section class="flex items-end justify-between">
      <h1 class="admin-page-title text-[30px]">Quản lý phiếu giảm giá</h1>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div
          class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500"
        >
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="admin-section-title">Bộ lọc</h2>
          <p class="text-sm text-slate-400">Tra cứu nhanh dữ liệu.</p>
        </div>
      </div>

      <div class="flex flex-col gap-6">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-5">
          <div class="space-y-2">
            <label class="admin-filter-label">Tìm kiếm</label>
            <div class="relative">
              <Search
                class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
              />
              <input
                v-model="(activeTab === 'phieu' ? boLoc : boLocKh).keyword"
                type="text"
                placeholder="Mã, tên phiếu..."
                class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>
          </div>

          <template v-if="activeTab === 'phieu'">
            <div class="space-y-2">
              <label class="admin-filter-label">Ngày bắt đầu</label>
              <input
                v-model="boLoc.tuNgay"
                type="date"
                class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>

            <div class="space-y-2">
              <label class="admin-filter-label">Ngày kết thúc</label>
              <input
                v-model="boLoc.denNgay"
                type="date"
                class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>

            <div class="space-y-2">
              <label class="admin-filter-label">Loại giảm</label>
              <select
                v-model="boLoc.loai"
                class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              >
                <option
                  v-for="loai in dsLoai"
                  :key="loai.value"
                  :value="loai.value"
                >
                  {{ loai.label }}
                </option>
              </select>
            </div>
          </template>

          <div
            class="space-y-2"
            :class="activeTab !== 'phieu' ? 'lg:col-span-2' : ''"
          >
            <label class="admin-filter-label">Trạng thái</label>
            <select
              v-model="(activeTab === 'phieu' ? boLoc : boLocKh).trangThai"
              class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            >
              <option
                v-for="trangThai in dsTrangThai"
                :key="trangThai.value"
                :value="trangThai.value"
              >
                {{ trangThai.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="flex flex-wrap items-center justify-end gap-3">
          <button
            @click="lamMoiBoLoc"
            class="inline-flex h-11 items-center gap-2 rounded-2xl border border-rose-200 bg-white px-5 text-sm font-semibold text-rose-500 shadow-[0_10px_24px_rgba(244,63,94,0.08)] transition hover:border-rose-300 hover:bg-rose-50/70 hover:text-rose-600"
          >
            <RotateCcw class="h-4 w-4" /> Đặt lại bộ lọc
          </button>
          <button
            @click="xuatExcel"
            class="inline-flex h-11 items-center gap-2 rounded-2xl border border-rose-200 bg-white px-5 text-sm font-semibold text-rose-500 shadow-[0_10px_24px_rgba(244,63,94,0.08)] transition hover:border-rose-300 hover:bg-rose-50/70 hover:text-rose-600"
          >
            <FileSpreadsheet class="h-4 w-4" /> Xuất Excel
          </button>
          <button
            @click="openCreateModal"
            class="inline-flex h-11 items-center gap-2 rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-5 text-sm font-semibold text-white shadow-[0_14px_30px_rgba(239,68,68,0.28)] transition hover:-translate-y-0.5 hover:from-rose-600 hover:to-red-500 hover:shadow-[0_18px_34px_rgba(239,68,68,0.32)]"
          >
            <Plus class="h-4 w-4" />
            {{ activeTab === "phieu" ? "Tạo phiếu mới" : "Tặng phiếu khách hàng" }}
          </button>
        </div>
      </div>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center justify-between">
        <h2 class="admin-section-title text-lg">
          {{
            activeTab === "phieu"
              ? "Danh sách phiếu giảm giá"
              : "Danh sách phiếu khách hàng"
          }}
        </h2>
        <p class="text-sm font-medium text-slate-400">
          {{ activeTab === "phieu" ? totalItems : totalItemsKh }} bản ghi hiển thị.
        </p>
      </div>

      <div
        v-if="loiTrang"
        class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600"
      >
        {{ loiTrang }}
      </div>

      <div class="w-full">
        <table
          v-if="activeTab === 'phieu'"
          class="w-full border-separate border-spacing-y-2 text-sm"
        >
          <thead>
            <tr class="text-left text-sm font-bold text-slate-950 [&>th]:whitespace-nowrap">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã</th>
              <th class="bg-slate-100 px-4 py-3">Tên phiếu</th>
              <th class="bg-slate-100 px-4 py-3">Hình thức</th>
              <th class="bg-slate-100 px-4 py-3">Giá trị giảm</th>
              <th class="bg-slate-100 px-4 py-3">Ngày bắt đầu</th>
              <th class="bg-slate-100 px-4 py-3">Ngày kết thúc</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center whitespace-nowrap">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">
                Đang tải...
              </td>
            </tr>
            <tr v-else-if="!danhSach.length">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">
                Không có dữ liệu.
              </td>
            </tr>
            <tr
              v-for="(item, index) in danhSach"
              :key="item.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100 transition hover:ring-slate-200"
            >
              <td class="rounded-l-2xl px-4 py-3 font-semibold">
                {{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}
              </td>
              <td class="px-4 py-3 font-semibold text-slate-900">
                {{ item.ma }}
              </td>
              <td class="px-4 py-3 text-slate-900">{{ item.ten }}</td>
              <td class="px-4 py-3 whitespace-nowrap">{{ loaiPhieuText(item.loaiPhieu) }}</td>
              <td class="px-4 py-3 whitespace-nowrap">{{ formatGiaTri(item.giaTri, item.loai) }}</td>
              <td class="px-4 py-3 whitespace-nowrap">{{ toDisplayDate(item.ngayBatDau) }}</td>
              <td class="px-4 py-3 whitespace-nowrap">{{ toDisplayDate(item.ngayKetThuc) }}</td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold"
                  :class="mauTrangThai(item.trangThai, item.ngayKetThuc)"
                >
                  {{ statusText(item.trangThai, item.ngayKetThuc) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex items-center justify-center gap-3">
                  <AdminQuickStatusAction
                    :loading="false"
                    :disabled="isHetHan(item.ngayKetThuc)"
                    :disabled-title="'Phiếu đã hết hạn, không thể thay đổi trạng thái'"
                    :action-label="Number(item.trangThai) === 1 ? 'Ngừng hoạt động' : 'Kích hoạt'"
                    :confirm-message="Number(item.trangThai) === 1 ? 'Bạn có chắc chắn muốn ngừng hoạt động phiếu này không?' : 'Bạn có chắc chắn muốn kích hoạt phiếu này không?'"
                    :intent="Number(item.trangThai) === 1 ? 'deactivate' : 'activate'"
                    @toggle="nhanhDoiTrangThai(item)"
                  />
                  <button
                    @click="openEditModal('phieu', item)"
                    class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500"
                  >
                    <Eye class="h-5 w-5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <table
          v-else
          class="min-w-[1100px] w-full border-separate border-spacing-y-2 text-sm"
        >
          <thead>
            <tr class="text-left text-sm font-bold text-slate-950">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã phiếu</th>
              <th class="bg-slate-100 px-4 py-3">Tên phiếu</th>
              <th class="bg-slate-100 px-4 py-3">Khách hàng</th>
              <th class="bg-slate-100 px-4 py-3">Ngày tặng</th>
              <th class="bg-slate-100 px-4 py-3">Ngày dùng</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">
                Hành động
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">
                Đang tải...
              </td>
            </tr>
            <tr v-else-if="!danhSachKh.length">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">
                Không có dữ liệu.
              </td>
            </tr>
            <tr
              v-for="(item, index) in danhSachKh"
              :key="item.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100 transition hover:ring-slate-200"
            >
              <td class="rounded-l-2xl px-4 py-3 font-semibold">
                {{ (trangHienTaiKh - 1) * soPhanTuMotTrangKh + index + 1 }}
              </td>
              <td class="px-4 py-3 font-semibold text-slate-900">
                {{ item.maPhieuGiamGia }}
              </td>
              <td class="px-4 py-3 text-slate-900">
                {{ item.tenPhieuGiamGia }}
              </td>
              <td class="px-4 py-3">
                {{ item.tenKhachHang }}
              </td>
              <td class="px-4 py-3">
                {{ toDisplayDate(item.ngayTao) }}
              </td>
              <td class="px-4 py-3">
                {{ item.ngaySuDung ? toDisplayDate(item.ngaySuDung) : "Chưa sử dụng" }}
              </td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold"
                  :class="mauTrangThai(item.trangThai)"
                >
                  {{ statusText(item.trangThai) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex items-center justify-center gap-3">
                  <AdminQuickStatusAction
                    :loading="false"
                    :disabled="Number(item.trangThai) === 0"
                    disabled-title="Không thể thao tác trên phiếu đã ngừng hoạt động"
                    action-label="Tắt liên kết"
                    confirm-message="Bạn có chắc chắn muốn tắt liên kết này không?"
                    intent="deactivate"
                    @toggle="nhanhDoiTrangThaiKh(item)"
                  />
                  <button
                    @click="openEditModal('khach-hang', item)"
                    class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500"
                  >
                    <Eye class="h-5 w-5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminTableFooter
        :current-page="activeTab === 'phieu' ? trangHienTai : trangHienTaiKh"
        :page-size="activeTab === 'phieu' ? soPhanTuMotTrang : soPhanTuMotTrangKh"
        :page-size-options="[5, 10, 20]"
        :total-items="activeTab === 'phieu' ? totalItems : totalItemsKh"
        :total-pages="activeTab === 'phieu' ? tongSoTrang : tongSoTrangKh"
        compact
        show-refresh
        @refresh="activeTab === 'phieu' ? taiDanhSach() : taiDanhSachKh()"
        @update:current-page="activeTab === 'phieu' ? (trangHienTai = $event) : (trangHienTaiKh = $event)"
        @update:page-size="activeTab === 'phieu' ? (soPhanTuMotTrang = $event) : (soPhanTuMotTrangKh = $event)"
      />
    </section>
  </div>
</template>
