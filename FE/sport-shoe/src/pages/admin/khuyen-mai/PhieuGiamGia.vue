<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  Edit, FileSpreadsheet, Filter, Plus, RotateCcw, Search, Ticket, Trash2, Eye
} from "lucide-vue-next";
import {
  deletePhieuGiamGia,
  getPhieuGiamGiaList,
  updatePhieuGiamGia,
  getPhieuGiamGiaKhachHangList,
  deletePhieuGiamGiaKhachHang,
  updatePhieuGiamGiaKhachHang
} from "../../../services/khuyen-mai";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import AdminQuickStatusAction from "../../../components/common/AdminQuickStatusAction.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";

const tabs = [
  { key: "phieu", label: "Phiếu giảm giá" },
  { key: "khach-hang", label: "Phiếu khách hàng" }
];

const activeTab = ref("phieu");
const router = useRouter();
const dangTai = ref(false);
const loiTrang = ref("");

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
  { label: "Kích hoạt", value: "1" },
  { label: "Tắt", value: "0" },
];

const dsLoai = [
  { label: "Tất cả", value: "" },
  { label: "Phần trăm", value: "1" },
  { label: "Tiền mặt", value: "2" },
];

function mauTrangThai(trangThai) {
  return Number(trangThai) === 1 ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600";
}

function statusText(value) {
  return Number(value) === 1 ? "Kích hoạt" : "Tắt";
}

function mauLoaiGiam(loai) {
  return Number(loai) === 1 ? "bg-blue-50 text-blue-600" : "bg-amber-50 text-amber-600";
}

function loaiGiamText(loai) {
  return Number(loai) === 1 ? "Phần trăm" : "Tiền mặt";
}

function toDisplayDate(value) {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("vi-VN");
}

function formatCurrency(value) {
    return new Intl.NumberFormat("vi-VN").format(value || 0) + " đ";
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
watch(boLoc, () => {
  clearTimeout(timer);
  timer = setTimeout(() => { trangHienTai.value = 1; taiDanhSach(); }, 300);
}, { deep: true });

watch(boLocKh, () => {
  clearTimeout(timer);
  timer = setTimeout(() => { trangHienTaiKh.value = 1; taiDanhSachKh(); }, 300);
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
    loiTrang.value = e.message || "Lỗi tải phiếu giảm giá";
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
  } catch (e) {
    loiTrang.value = e.message || "Lỗi tải phiếu khách hàng";
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

async function nhanhDoiTrangThai(item) {
    try {
        const nextStatus = Number(item.trangThai) === 1 ? 0 : 1;
        await updatePhieuGiamGia(item.id, { ...item, trangThai: nextStatus });
        alert("Cập nhật thành công");
        taiDanhSach();
    } catch (e) {
        alert(e.message || "Thất bại");
    }
}

async function nhanhDoiTrangThaiKh(item) {
    try {
        const nextStatus = Number(item.trangThai) === 1 ? 0 : 1;
        await updatePhieuGiamGiaKhachHang(item.id, { ...item, trangThai: nextStatus });
        alert("Cập nhật thành công");
        taiDanhSachKh();
    } catch (e) {
        alert(e.message || "Thất bại");
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
    alert("Xóa thành công.");
  } catch (error) {
    alert(error.message || "Xóa thất bại");
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
          { label: "Tên", key: "ten" },
          { label: "Loại giảm", value: (row) => Number(row.loai) === 1 ? "Phần trăm" : "Tiền mặt" },
          { label: "Giá trị", key: "giaTri" },
          { label: "Số lượng", key: "soLuong" },
          { label: "Ngày bắt đầu", value: (row) => toDisplayDate(row.ngayBatDau) },
          { label: "Ngày kết thúc", value: (row) => toDisplayDate(row.ngayKetThuc) },
          { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
        ],
        rows,
      });
    } else {
        const data = await getPhieuGiamGiaKhachHangList({
            keyword: boLocKh.value.keyword || undefined,
            trangThai: boLocKh.value.trangThai !== "" ? Number(boLocKh.value.trangThai) : undefined,
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
                { label: "Phiếu giảm giá", key: "maPhieuGiamGia" },
                { label: "Khách hàng", key: "tenKhachHang" },
                { label: "Email", key: "email" },
                { label: "Ngày tặng", value: (row) => toDisplayDate(row.ngayTao) },
                { label: "Ngày dùng", value: (row) => toDisplayDate(row.ngaySuDung) },
                { label: "Trạng thái", value: (row) => statusText(row.trangThai) },
            ],
            rows,
        });
    }
  } catch (error) {
    window.alert(error?.message || "Xuất Excel thất bại.");
  }
}

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <section class="flex items-end justify-between">
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">Quản lý phiếu giảm giá</h1>
    </section>

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
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-5">
          <div class="space-y-2">
            <label class="block text-[13px] font-semibold text-slate-500">Tìm kiếm</label>
            <div class="relative">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input v-model="(activeTab === 'phieu' ? boLoc : boLocKh).keyword" type="text" placeholder="Mã, tên, mô tả..." class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm font-medium text-slate-900 outline-none transition focus:border-rose-300 focus:bg-white" />
            </div>
          </div>

          <template v-if="activeTab === 'phieu'">
            <div class="space-y-2">
              <label class="block text-[13px] font-semibold text-slate-500">Ngày bắt đầu</label>
              <input v-model="boLoc.tuNgay" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-900 outline-none transition focus:border-rose-300 focus:bg-white" />
            </div>
            <div class="space-y-2">
              <label class="block text-[13px] font-semibold text-slate-500">Ngày kết thúc</label>
              <input v-model="boLoc.denNgay" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-900 outline-none transition focus:border-rose-300 focus:bg-white" />
            </div>
            <div class="space-y-2">
              <label class="block text-[13px] font-semibold text-slate-500">Hình thức</label>
              <select v-model="boLoc.loai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white">
                <option v-for="l in dsLoai" :key="l.value" :value="l.value">{{ l.label }}</option>
              </select>
            </div>
          </template>

          <div class="space-y-2" :class="activeTab !== 'phieu' ? 'lg:col-span-2' : ''">
            <label class="block text-[13px] font-semibold text-slate-500">Trạng thái</label>
            <select v-model="(activeTab === 'phieu' ? boLoc : boLocKh).trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-900 outline-none transition focus:border-rose-300 focus:bg-white">
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
            <button @click="router.push({ name: 'admin-phieu-giam-gia-them' })" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-semibold text-white transition hover:bg-rose-600">
              <Plus class="h-4 w-4" /> Tạo phiếu mới
            </button>
            <button @click="router.push({ name: 'admin-phieu-giam-gia-khach-hang-them' })" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-blue-500 px-5 text-sm font-semibold text-white transition hover:bg-blue-600">
              <Plus class="h-4 w-4" /> Tặng cho khách hàng
            </button>
        </div>
      </div>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center justify-between">
        <div class="inline-flex rounded-2xl bg-slate-100 p-1">
          <button v-for="tab in tabs" :key="tab.key" class="rounded-2xl px-5 py-2 text-sm font-semibold transition" :class="activeTab === tab.key ? 'bg-white text-rose-600 shadow-sm' : 'text-slate-600 hover:text-slate-900'" @click="activeTab = tab.key">
            {{ tab.label }}
          </button>
        </div>
        <div>
          <p class="text-sm text-slate-400 font-medium">{{ activeTab === 'phieu' ? totalItems : totalItemsKh }} bản ghi hiển thị.</p>
        </div>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <table v-if="activeTab === 'phieu'" class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã</th>
              <th class="bg-slate-100 px-4 py-3">Tên / Hạn dùng</th>
              <th class="bg-slate-100 px-4 py-3">Loại</th>
              <th class="bg-slate-100 px-4 py-3">Giá trị</th>
              <th class="bg-slate-100 px-4 py-3">SL</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">Đang tải...</td>
            </tr>
            <tr v-else-if="!danhSach.length">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">Không có dữ liệu.</td>
            </tr>
            <tr v-for="(item, index) in danhSach" :key="item.id" class="bg-white text-slate-950 shadow-sm ring-1 ring-slate-100 transition hover:ring-rose-200">
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}</td>
              <td class="px-4 py-3 font-bold text-slate-900 tracking-tight">{{ item.ma }}</td>
              <td class="px-4 py-3">
                <div class="font-bold text-slate-900">{{ item.ten }}</div>
                <div class="text-[11px] text-slate-400 font-medium">{{ toDisplayDate(item.ngayBatDau) }} - {{ toDisplayDate(item.ngayKetThuc) }}</div>
              </td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauLoaiGiam(item.loai)">
                  {{ loaiGiamText(item.loai) }}
                </span>
              </td>
              <td class="px-4 py-3 font-bold text-rose-500">
                {{ item.giaTri }}{{ Number(item.loai) === 1 ? '%' : ' đ' }}
              </td>
              <td class="px-4 py-3 font-medium">{{ item.soLuong }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(item.trangThai)">
                  {{ statusText(item.trangThai) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex justify-center items-center gap-3">
                  <AdminQuickStatusAction
                    :loading="false"
                    :action-label="Number(item.trangThai) === 1 ? 'Tắt phiếu' : 'Kích hoạt phiếu'"
                    :confirm-message="`Bạn có chắc chắn muốn ${Number(item.trangThai) === 1 ? 'tắt' : 'kích hoạt'} phiếu này không?`"
                    :intent="Number(item.trangThai) === 1 ? 'deactivate' : 'activate'"
                    @toggle="nhanhDoiTrangThai(item)"
                  />
                  <button @click="openEditModal('phieu', item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500">
                    <Eye class="h-5 w-5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Bảng Phiếu khách hàng -->
        <table v-else class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Phiếu giảm giá</th>
              <th class="bg-slate-100 px-4 py-3">Khách hàng</th>
              <th class="bg-slate-100 px-4 py-3">Ngày Tặng/Dùng</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="6" class="py-10 text-center text-sm text-slate-400">Đang tải...</td>
            </tr>
            <tr v-else-if="!danhSachKh.length">
              <td colspan="6" class="py-10 text-center text-sm text-slate-400">Không có dữ liệu.</td>
            </tr>
            <tr v-for="(item, index) in danhSachKh" :key="item.id" class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100 transition hover:ring-rose-200">
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTaiKh - 1) * soPhanTuMotTrangKh + index + 1 }}</td>
              <td class="px-4 py-3">
                <div class="font-bold text-slate-900">{{ item.maPhieuGiamGia }}</div>
                <div class="text-[11px] text-slate-400">{{ item.tenPhieuGiamGia }}</div>
              </td>
              <td class="px-4 py-3 font-bold text-rose-600">{{ item.tenKhachHang }}</td>
              <td class="px-4 py-3 text-slate-500">
                <div class="text-[11px] text-slate-400">Tặng: {{ toDisplayDate(item.ngayTao) }}</div>
                <div v-if="item.ngaySuDung" class="font-medium text-slate-700">Dùng: {{ toDisplayDate(item.ngaySuDung) }}</div>
              </td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(item.trangThai)">
                  {{ statusText(item.trangThai) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex justify-center items-center gap-3">
                  <AdminQuickStatusAction
                    :loading="false"
                    :action-label="Number(item.trangThai) === 1 ? 'Tắt liên kết' : 'Kích hoạt liên kết'"
                    :confirm-message="`Bạn có chắc chắn muốn ${Number(item.trangThai) === 1 ? 'tắt' : 'kích hoạt'} liên kết này không?`"
                    :intent="Number(item.trangThai) === 1 ? 'deactivate' : 'activate'"
                    @toggle="nhanhDoiTrangThaiKh(item)"
                  />
                  <button @click="openEditModal('khach-hang', item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500">
                    <Eye class="h-5 w-5" />
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
