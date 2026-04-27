<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  Eye, FileSpreadsheet, Filter, Plus, RotateCcw, Search, Tag, PackageSearch, ToggleLeft, ToggleRight,
  CheckCircle2, CircleX, X
} from "lucide-vue-next";
import {
  deleteDotGiamGia,
  getDotGiamGiaList,
  getDotGiamGiaSanPhamList, 
  updateDotGiamGia,
  deleteDotGiamGiaSanPham,
  updateDotGiamGiaSanPham
} from "../../../services/khuyen-mai";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import AdminQuickStatusAction from "../../../components/common/AdminQuickStatusAction.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { getDisplayErrorMessage } from "../../../utils/error-message";

const router = useRouter();
const dangTai = ref(false);
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

const boLoc = ref({ keyword: "", trangThai: "", tuNgay: "", denNgay: "", loaiGiam: "" });

const danhSach = ref([]);
const tongSoTrang = ref(1);
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const totalItems = ref(0);

const danhSachSp = ref([]);
const tongSoTrangSp = computed(() => Math.ceil(filteredDanhSachSp.value.length / soPhanTuMotTrangSp.value) || 1);
const soPhanTuMotTrangSp = ref(5);
const trangHienTaiSp = ref(1);

const dsTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Kích hoạt", value: "1" },
  { label: "Tắt", value: "0" },
];

const dsLoaiGiam = [
  { label: "Tất cả", value: "" },
  { label: "Phần trăm", value: "1" },
  { label: "Tiền mặt", value: "2" },
];

const filteredDanhSachSp = computed(() => {
  const keyword = (boLocSp.value.keyword || "").trim().toLowerCase();
  let list = danhSachSp.value;
  if (keyword) {
    list = list.filter(item => 
      (item.tenDotGiamGia || "").toLowerCase().includes(keyword) || 
      (item.tenGiay || "").toLowerCase().includes(keyword) ||
      (item.maDotGiamGia || "").toLowerCase().includes(keyword)
    );
  }
  if (boLocSp.value.trangThai !== "") {
    list = list.filter(item => String(item.trangThai) === boLocSp.value.trangThai);
  }
  return list;
});

const danhSachSpPhanTrang = computed(() => {
  const start = (trangHienTaiSp.value - 1) * soPhanTuMotTrangSp.value;
  return filteredDanhSachSp.value.slice(start, start + soPhanTuMotTrangSp.value);
});

function mauTrangThai(trangThai) {
  return Number(trangThai) === 1 ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600";
}

function statusText(value) {
  return Number(value) === 1 ? "Kích hoạt" : "Tắt";
}

function toDisplayDate(value) {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("vi-VN");
}

watch(activeTab, (newTab) => {
  if (newTab === 'dot') {
    trangHienTai.value = 1;
    taiDanhSach();
  } else {
    trangHienTaiSp.value = 1;
    taiDanhSachSp();
  }
});

watch(soPhanTuMotTrang, () => { trangHienTai.value = 1; taiDanhSach(); });
watch(trangHienTai, taiDanhSach);

let timer;
watch(boLoc, () => {
  clearTimeout(timer);
  timer = setTimeout(() => { trangHienTai.value = 1; taiDanhSach(); }, 300);
}, { deep: true });

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const data = await getDotGiamGiaList({
      keyword: boLoc.value.keyword || undefined,
      trangThai: boLoc.value.trangThai !== "" ? Number(boLoc.value.trangThai) : undefined,
      loaiGiam: boLoc.value.loaiGiam !== "" ? Number(boLoc.value.loaiGiam) : undefined,
      tuNgay: boLoc.value.tuNgay || undefined,
      denNgay: boLoc.value.denNgay || undefined,
      pageNo: trangHienTai.value - 1,
      pageSize: soPhanTuMotTrang.value
    });
    danhSach.value = data?.content || [];
    tongSoTrang.value = data?.totalPages || 1;
    totalItems.value = data?.totalElements || 0;
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải danh sách đợt giảm giá");
  } finally {
    dangTai.value = false;
  }
}

async function taiDanhSachSp() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const res = await getDotGiamGiaSanPhamList();
    danhSachSp.value = Array.isArray(res) ? res : (res?.content || res?.items || []);
  } catch (e) {
    loiTrang.value = e.message || "Lỗi tải sản phẩm đợt giảm giá";
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  boLoc.value = { keyword: "", trangThai: "", tuNgay: "", denNgay: "", loaiGiam: "" };
}

async function nhanhDoiTrangThai(item) {
  try {
    const nextStatus = Number(item.kichHoat) === 1 ? 0 : 1;
    await updateDotGiamGia(item.id, { ...item, kichHoat: nextStatus });
    alert("Cập nhật trạng thái thành công");
    taiDanhSach();
  } catch (error) {
    alert(error.message || "Thao tác thất bại");
  }
}

async function nhanhDoiTrangThaiSp(item) {
  try {
    const nextStatus = Number(item.trangThai) === 1 ? 0 : 1;
    await updateDotGiamGiaSanPham(item.id, { ...item, trangThai: nextStatus });
    alert("Cập nhật trạng thái thành công");
    taiDanhSachSp();
  } catch (error) {
    alert(error.message || "Thao tác thất bại");
  }
}

async function xuatExcel() {
  try {
    if (activeTab.value === "dot") {
      const data = await getDotGiamGiaList({
        keyword: boLoc.value.keyword || undefined,
        trangThai: boLoc.value.trangThai !== "" ? Number(boLoc.value.trangThai) : undefined,
        loaiGiam: boLoc.value.loaiGiam !== "" ? Number(boLoc.value.loaiGiam) : undefined,
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
        filename: "quan-ly-dot-giam-gia",
        sheetName: "DotGiamGia",
        columns: [
          { label: "STT", value: (_, index) => index + 1 },
          { label: "Mã", key: "ma" },
          { label: "Tên", key: "ten" },
          { label: "Loại giảm", value: (row) => Number(row.loaiGiam) === 1 ? "Phần trăm" : "Tiền mặt" },
          { label: "Giá trị", key: "giaTriGiam" },
          { label: "Ngày bắt đầu", value: (row) => toDisplayDate(row.ngayBatDau) },
          { label: "Ngày kết thúc", value: (row) => toDisplayDate(row.ngayKetThuc) },
          { label: "Trạng thái", value: (row) => statusText(row.kichHoat) },
        ],
        rows,
      });
    } else {
      const rows = filteredDanhSachSp.value;
      if (!rows.length) {
        window.alert("Không có dữ liệu để xuất Excel.");
        return;
      }
      exportRowsToExcel({
        filename: "quan-ly-dot-giam-gia-san-pham",
        sheetName: "DotGiamGiaSanPham",
        columns: [
          { label: "STT", value: (_, index) => index + 1 },
          { label: "Đợt giảm giá", key: "tenDotGiamGia" },
          { label: "Sản phẩm", key: "tenGiay" },
          { label: "Ngày tạo", value: (row) => toDisplayDate(row.ngayTao) },
          { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
        ],
        rows,
      });
    }
  } catch (error) {
    window.alert(getDisplayErrorMessage(error, "Không thể xuất Excel đợt giảm giá"));
  }
}

function openCreateModal() {
  if (activeTab.value === "dot") {
    router.push({ name: "admin-dot-giam-gia-them" });
  } else {
    router.push({ name: "admin-dot-giam-gia-san-pham-them" });
  }
}

async function openEditModal(item) {
  if (activeTab.value === "dot") {
    router.push({ name: "admin-dot-giam-gia-chi-tiet", params: { id: item.id } });
  } else {
    router.push({ name: "admin-dot-giam-gia-san-pham-chi-tiet", params: { id: item.id } });
  }
}

async function removeItem(item) {
    if (!confirm("Bạn có chắc muốn xóa bản ghi này?")) return;
    try {
        if (activeTab.value === "dot") await deleteDotGiamGia(item.id);
        else await deleteDotGiamGiaSanPham(item.id);
        alert("Xóa thành công");
        if (activeTab.value === "dot") taiDanhSach();
        else taiDanhSachSp();
    } catch (e) {
        alert(e.message || "Xóa thất bại");
    }
}

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <section class="flex items-end justify-between">
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">Quản lý đợt giảm giá</h1>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
          <p class="text-sm text-slate-400">Tra cứu nhanh dữ liệu.</p>
        </div>
      </div>

      <div class="flex flex-col gap-6">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-5">
          <div class="space-y-2">
            <label class="text-[13px] font-semibold text-slate-500">Tìm kiếm</label>
            <div class="relative">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input v-model="(activeTab === 'dot' ? boLoc : boLocSp).keyword" type="text" :placeholder="activeTab === 'dot' ? 'Mã, tên, mô tả...' : 'Tên đợt, tên giày...'" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" @keyup.enter="activeTab === 'dot' ? taiDanhSach() : null" />
            </div>
          </div>

          <template v-if="activeTab === 'dot'">
            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Ngày bắt đầu</label>
              <input v-model="boLoc.tuNgay" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
            </div>

            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Ngày kết thúc</label>
              <input v-model="boLoc.denNgay" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
            </div>

            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Hình thức</label>
              <select v-model="boLoc.loaiGiam" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white">
                <option v-for="lg in dsLoaiGiam" :key="lg.value" :value="lg.value">{{ lg.label }}</option>
              </select>
            </div>
          </template>

          <div class="space-y-2" :class="activeTab !== 'dot' ? 'lg:col-span-2' : ''">
            <label class="text-[13px] font-semibold text-slate-500">Trạng thái</label>
            <select v-model="(activeTab === 'dot' ? boLoc : boLocSp).trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white">
              <option v-for="tt in dsTrangThai" :key="tt.value" :value="tt.value">{{ tt.label }}</option>
            </select>
          </div>
        </div>

        <div class="flex flex-wrap items-center gap-3 justify-end">
            <button @click="lamMoiBoLoc" class="inline-flex h-11 items-center gap-2 rounded-2xl border border-slate-200 bg-white px-5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 hover:text-slate-800">
              <RotateCcw class="h-4 w-4" /> Đặt lại bộ lọc
            </button>
            <button @click="xuatExcel" class="inline-flex h-11 items-center gap-2 rounded-2xl border border-slate-200 bg-white px-5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 hover:text-slate-800">
              <FileSpreadsheet class="h-4 w-4" /> Xuất Excel
            </button>
            <button @click="router.push({ name: 'admin-dot-giam-gia-them' })" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-semibold text-white transition hover:bg-rose-600">
              <Plus class="h-4 w-4" /> Tạo đợt giảm giá
            </button>
            <button @click="router.push({ name: 'admin-dot-giam-gia-san-pham-them' })" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-amber-500 px-5 text-sm font-semibold text-white transition hover:bg-amber-600">
              <Plus class="h-4 w-4" /> Áp dụng sản phẩm
            </button>
        </div>
      </div>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center justify-between">
        <h2 class="text-lg font-bold text-slate-800">Danh sách các đợt giảm giá</h2>
        <div>
          <p class="text-sm text-slate-400 font-medium">{{ totalItems }} bản ghi hiển thị.</p>
        </div>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <!-- Bảng Đợt giảm giá -->
        <table v-if="activeTab === 'dot'" class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-950">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã</th>
              <th class="bg-slate-100 px-4 py-3">Tên / Mô tả</th>
              <th class="bg-slate-100 px-4 py-3">Giá trị</th>
              <th class="bg-slate-100 px-4 py-3">Ngày bắt đầu</th>
              <th class="bg-slate-100 px-4 py-3">Ngày kết thúc</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="!danhSach.length">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">Không có dữ liệu.</td>
            </tr>
            <tr v-for="(item, index) in danhSach" :key="item.id" class="bg-white text-slate-950 shadow-sm ring-1 ring-slate-100 transition hover:ring-rose-200">
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}</td>
              <td class="px-4 py-3 font-bold text-slate-900 tracking-tight">{{ item.ma }}</td>
              <td class="px-4 py-3">
                <div class="font-bold text-slate-900">{{ item.ten }}</div>
                <div class="font-normal text-xs text-slate-500 mt-0.5 max-w-[200px] truncate">{{ item.moTa || '—' }}</div>
              </td>
              <td class="px-4 py-3 font-bold text-rose-500">
                {{ item.giaTriGiam }}{{ Number(item.loaiGiam) === 1 ? '%' : ' đ' }}
              </td>
              <td class="px-4 py-3 font-medium text-slate-600">{{ toDisplayDate(item.ngayBatDau) }}</td>
              <td class="px-4 py-3 font-medium text-slate-600">{{ toDisplayDate(item.ngayKetThuc) }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(item.kichHoat)">
                  {{ statusText(item.kichHoat) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex justify-center items-center gap-3">
                  <AdminQuickStatusAction
                    :loading="false"
                    :action-label="Number(item.kichHoat) === 1 ? 'Tắt đợt giảm giá' : 'Kích hoạt đợt giảm giá'"
                    :confirm-message="`Bạn có chắc chắn muốn ${Number(item.kichHoat) === 1 ? 'tắt' : 'kích hoạt'} đợt giảm giá này không?`"
                    :intent="Number(item.kichHoat) === 1 ? 'deactivate' : 'activate'"
                    @toggle="nhanhDoiTrangThai(item)"
                  />
                  <button @click="openEditModal(item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500" title="Xem chi tiết">
                    <Eye class="h-5 w-5" />
                  </button>
                  <button @click="removeItem(item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-600" title="Xóa">
                    <Trash2 class="h-4 w-4" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Bảng Sản phẩm áp dụng -->
        <table v-else class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Đợt giảm giá</th>
              <th class="bg-slate-100 px-4 py-3">Sản phẩm</th>
              <th class="bg-slate-100 px-4 py-3">Ngày tạo</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="6" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="!danhSachSpPhanTrang.length">
              <td colspan="6" class="py-10 text-center text-sm text-slate-400">Không có dữ liệu.</td>
            </tr>
            <tr v-for="(item, index) in danhSachSpPhanTrang" :key="item.id" class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100 transition hover:ring-rose-200">
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTaiSp - 1) * soPhanTuMotTrangSp + index + 1 }}</td>
              <td class="px-4 py-3 font-bold text-slate-900">{{ item.tenDotGiamGia }}</td>
              <td class="px-4 py-3 font-medium text-slate-700">{{ item.tenGiay }}</td>
              <td class="px-4 py-3 text-slate-500">{{ toDisplayDate(item.ngayTao) }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(item.trangThai)">
                  {{ statusText(item.trangThai) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex justify-center items-center gap-3">
                  <AdminQuickStatusAction
                    :loading="false"
                    :action-label="Number(item.trangThai) === 1 ? 'Tắt' : 'Kích hoạt'"
                    :confirm-message="`Bạn có chắc chắn muốn thay đổi trạng thái không?`"
                    :intent="Number(item.trangThai) === 1 ? 'deactivate' : 'activate'"
                    @toggle="nhanhDoiTrangThaiSp(item)"
                  />
                  <button @click="openEditModal(item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500" title="Sửa">
                    <Edit class="h-4 w-4" />
                  </button>
                  <button @click="removeItem(item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-600" title="Xóa">
                    <Trash2 class="h-4 w-4" />
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
