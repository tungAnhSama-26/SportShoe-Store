<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  ChevronLeft, ChevronRight, Eye, Filter, Plus, RefreshCw, Search, Tag, Edit, Trash2, PackageSearch
} from "lucide-vue-next";
import {
  createDotGiamGia,
  createDotGiamGiaSanPham,
  deleteDotGiamGia,
  deleteDotGiamGiaSanPham,
  getDotGiamGiaDetail,
  getDotGiamGiaList,
  getDotGiamGiaSanPhamDetail,
  getDotGiamGiaSanPhamList, 
  searchSanPhamTaiQuay,
  updateDotGiamGia,
  updateDotGiamGiaSanPham
} from "../../../services/khuyen-mai";

const tabs = [
  { key: "dot", label: "Đợt giảm giá" },
  { key: "san-pham", label: "Sản phẩm áp dụng" }
];

const activeTab = ref("dot");
const router = useRouter();
const dangTai = ref(false);
const saving = ref(false);
const loiTrang = ref("");

// Modified BoLoc: tuNgay, denNgay, loaiGiam
const boLoc = ref({ keyword: "", trangThai: "", tuNgay: "", denNgay: "", loaiGiam: "" });
const boLocSp = ref({ keyword: "", trangThai: "" });

const danhSach = ref([]);
const tongSoTrang = ref(1);
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const totalItems = ref(0);

const danhSachSp = ref([]); // Data for san-pham tab
const filteredDanhSachSp = computed(() => {
  const keyword = (boLocSp.value.keyword || "").trim().toLowerCase();
  let list = danhSachSp.value;
  if (keyword) {
    list = list.filter(item => (item.tenDotGiamGia || "").toLowerCase().includes(keyword) || (item.tenGiay || "").toLowerCase().includes(keyword));
  }
  if (boLocSp.value.trangThai !== "") {
    list = list.filter(item => String(item.trangThai) === boLocSp.value.trangThai);
  }
  return list;
});
const tongSoTrangSp = computed(() => Math.ceil(filteredDanhSachSp.value.length / soPhanTuMotTrangSp.value) || 1);
const soPhanTuMotTrangSp = ref(5);
const trangHienTaiSp = ref(1);
const danhSachSpPhanTrang = computed(() => {
  const start = (trangHienTaiSp.value - 1) * soPhanTuMotTrangSp.value;
  return filteredDanhSachSp.value.slice(start, start + soPhanTuMotTrangSp.value);
});


const dsTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Kích hoạt", value: "1" },
  { label: "Tắt", value: "0" },
];

const dotOptions = ref([]);

const dotSanPhamForm = reactive({
  id: null, dotGiamGiaId: "", giayId: "", trangThai: "1"
});

const productSearch = ref("");
const productHints = ref([]);
const loadingProductHints = ref(false);
const formErrors = reactive({});

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

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
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

watch(soPhanTuMotTrangSp, () => { trangHienTaiSp.value = 1; });
watch(filteredDanhSachSp, () => { trangHienTaiSp.value = 1; });

let timer;
watch(() => boLoc.value, () => {
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
    loiTrang.value = e.message || "Lỗi tải đợt giảm giá";
  } finally {
    dangTai.value = false;
  }
}

async function taiDanhSachSp() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const sanPhams = await getDotGiamGiaSanPhamList();
    danhSachSp.value = Array.isArray(sanPhams) ? sanPhams : [];
    // Preload options
    const dataOpts = await getDotGiamGiaList({ pageNo: 0, pageSize: 1000, trangThai: 1 });
    dotOptions.value = dataOpts?.content || [];
  } catch (e) {
    loiTrang.value = e.message || "Lỗi tải sản phẩm đợt giảm giá";
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  if (activeTab.value === 'dot') {
    boLoc.value = { keyword: "", trangThai: "", tuNgay: "", denNgay: "", loaiGiam: "" };
  } else {
    boLocSp.value = { keyword: "", trangThai: "" };
  }
}


function openCreateModal(target) {
  if (target === "dot") {
    router.push({ name: "admin-dot-giam-gia-them" });
    return;
  }
  modalOpen.value = true;
  modalMode.value = "create";
  modalTarget.value = target;
  resetErrors();
  Object.assign(dotSanPhamForm, { id: null, dotGiamGiaId: "", giayId: "", trangThai: "1" });
  productSearch.value = "";
}

async function openEditModal(target, item) {
  if (target === "dot") {
    router.push({ name: "admin-dot-giam-gia-chi-tiet", params: { id: item.id } });
    return;
  }
  modalOpen.value = true;
  modalMode.value = "edit";
  modalTarget.value = target;
  resetErrors();
  const detail = await getDotGiamGiaSanPhamDetail(item.id);
  Object.assign(dotSanPhamForm, {
    id: detail.id, dotGiamGiaId: detail.dotGiamGiaId ? String(detail.dotGiamGiaId) : "",
    giayId: detail.giayId ? String(detail.giayId) : "", trangThai: String(detail.trangThai ?? 1)
  });
  productSearch.value = detail.tenGiay ?? "";
}

async function removeItem(target, item) {
  if (!confirm(`Bạn có chắc muốn xóa ${target === "dot" ? "đợt giảm giá" : "liên kết"} này?`)) return;
  try {
    if (target === "dot") await deleteDotGiamGia(item.id);
    else await deleteDotGiamGiaSanPham(item.id);
    alert("Xóa thành công.");
    if (target === "dot") taiDanhSach();
    else taiDanhSachSp();
  } catch (error) {
    alert(error.message || "Xóa thất bại");
  }
}

watch(productSearch, async (val) => {
  if (modalTarget.value !== "san-pham" || modalMode.value === "detail") return;
  if ((val||"").trim().length < 2) { productHints.value = []; return; }
  loadingProductHints.value = true;
  try {
    productHints.value = await searchSanPhamTaiQuay(val.trim());
  } catch(e) { productHints.value = []; } finally { loadingProductHints.value = false; }
});

async function submitForm() {
  resetErrors();
  let isValid = true;
  if (!dotSanPhamForm.dotGiamGiaId) { formErrors.dotGiamGiaId = "Chọn đợt"; isValid = false; }
  if (!dotSanPhamForm.giayId || Number(dotSanPhamForm.giayId) <= 0) { formErrors.giayId = "ID giày không hợp lệ"; isValid = false; }
  if (!isValid) return;

  saving.value = true;
  try {
    const payload = {
      dotGiamGiaId: Number(dotSanPhamForm.dotGiamGiaId), giayId: Number(dotSanPhamForm.giayId),
      trangThai: Number(dotSanPhamForm.trangThai), ngayTao: getToday()
    };
    if (modalMode.value === "create") await createDotGiamGiaSanPham(payload);
    else await updateDotGiamGiaSanPham(dotSanPhamForm.id, payload);
    
    modalOpen.value = false;
    alert("Lưu thành công");
    taiDanhSachSp();
  } catch (error) {
    alert(error.message || "Lưu thất bại");
  } finally {
    saving.value = false;
  }
}

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section class="flex items-end justify-between">
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">Quản lý đợt giảm giá</h1>
    </section>

    <!-- Bộ lọc -->
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

      <div class="grid gap-4 xl:grid-cols-4 lg:grid-cols-3 md:grid-cols-2">
        <label class="space-y-2">
          <span class="mb-1 text-[13px] font-semibold text-slate-500">Tìm kiếm</span>
          <div class="relative">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="(activeTab === 'dot' ? boLoc : boLocSp).keyword"
              type="text"
              :placeholder="activeTab === 'dot' ? 'Nhập mã hoặc tên đợt...' : 'Nhập tên đợt, giày...'"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            />
          </div>
        </label>

        <label class="space-y-2">
          <span class="mb-1 text-[13px] font-semibold text-slate-500">Trạng thái</span>
          <select v-model="(activeTab === 'dot' ? boLoc : boLocSp).trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
            <option v-for="tt in dsTrangThai" :key="tt.value" :value="tt.value">{{ tt.label }}</option>
          </select>
        </label>
        
        <template v-if="activeTab === 'dot'">
          <label class="space-y-2">
            <span class="mb-1 text-[13px] font-semibold text-slate-500">Loại giảm</span>
            <select v-model="boLoc.loaiGiam" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
              <option value="">Tất cả</option>
              <option value="1">Phần trăm</option>
              <option value="2">Tiền mặt</option>
            </select>
          </label>

          <label class="space-y-2">
            <span class="mb-1 text-[13px] font-semibold text-slate-500">Từ ngày</span>
            <input v-model="boLoc.tuNgay" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          </label>

          <label class="space-y-2">
            <span class="mb-1 text-[13px] font-semibold text-slate-500">Đến ngày</span>
            <input v-model="boLoc.denNgay" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          </label>
        </template>

        <div class="col-span-full flex items-end justify-end gap-3 pt-2">
          <button @click="lamMoiBoLoc" class="h-11 rounded-2xl bg-slate-400 px-5 text-sm font-semibold text-white transition hover:bg-slate-500">Làm mới</button>
          <button @click="openCreateModal(activeTab)" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-semibold text-white transition hover:bg-rose-600">
            <Plus class="h-4 w-4" /> Thêm mới
          </button>
        </div>
      </div>
    </section>

    <!-- Danh sách -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center justify-between">
        <div class="inline-flex rounded-2xl bg-slate-100 p-1">
          <button v-for="tab in tabs" :key="tab.key" class="rounded-2xl px-5 py-2 text-sm font-semibold transition" :class="activeTab === tab.key ? 'bg-white text-rose-600 shadow-sm' : 'text-slate-600 hover:text-slate-900'" @click="activeTab = tab.key">
            {{ tab.label }}
          </button>
        </div>
        <div>
          <p class="text-sm text-slate-400 font-medium">{{ activeTab === 'dot' ? totalItems : filteredDanhSachSp.length }} bản ghi hiển thị.</p>
        </div>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <table v-if="activeTab === 'dot'" class="min-w-[900px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã</th>
              <th class="bg-slate-100 px-4 py-3">Tên</th>
              <th class="bg-slate-100 px-4 py-3">Loại giảm</th>
              <th class="bg-slate-100 px-4 py-3">Giá trị</th>
              <th class="bg-slate-100 px-4 py-3">Thời gian</th>
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
            <tr v-for="(item, index) in danhSach" :key="item.id" class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100">
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}</td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ item.ma }}</td>
              <td class="px-4 py-3 font-semibold text-slate-800">
                <div>{{ item.ten }}</div>
                <div class="font-normal text-xs text-slate-500">{{ item.moTa || '—' }}</div>
              </td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauLoaiGiam(item.loaiGiam)">
                  {{ loaiGiamText(item.loaiGiam) }}
                </span>
              </td>
              <td class="px-4 py-3 text-slate-600">{{ item.giaTriGiam }}</td>
              <td class="px-4 py-3 text-slate-600">{{ toDisplayDate(item.ngayBatDau) }} - {{ toDisplayDate(item.ngayKetThuc) }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(item.kichHoat)">
                  {{ statusText(item.kichHoat) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex justify-center gap-2">
                  <button @click="openEditModal('dot', item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-100 hover:text-amber-600"><Edit class="h-4 w-4" /></button>
                  <button @click="removeItem('dot', item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-100 hover:text-rose-600"><Trash2 class="h-4 w-4" /></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

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
            <tr v-for="(item, index) in danhSachSpPhanTrang" :key="item.id" class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100">
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTaiSp - 1) * soPhanTuMotTrangSp + index + 1 }}</td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ item.tenDotGiamGia }}</td>
              <td class="px-4 py-3 text-slate-600">{{ item.tenGiay }}</td>
              <td class="px-4 py-3 text-slate-600">{{ toDisplayDate(item.ngayTao) }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(item.trangThai)">
                  {{ statusText(item.trangThai) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex justify-center gap-2">
                  <button @click="openEditModal('san-pham', item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-100 hover:text-amber-600"><Edit class="h-4 w-4" /></button>
                  <button @click="removeItem('san-pham', item)" class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-100 hover:text-rose-600"><Trash2 class="h-4 w-4" /></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Phân trang -->
      <div class="mt-5 flex flex-wrap items-center justify-between gap-2 text-sm">
        <div class="flex items-center gap-2 text-slate-500">
          Xem
          <select v-if="activeTab === 'dot'" v-model.number="soPhanTuMotTrang" class="rounded-xl border border-slate-200 bg-slate-50 px-2 py-1 outline-none focus:border-rose-300 transition">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
          </select>
          <select v-else v-model.number="soPhanTuMotTrangSp" class="rounded-xl border border-slate-200 bg-slate-50 px-2 py-1 outline-none focus:border-rose-300 transition">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
          </select>
          bản ghi
        </div>
        <div class="flex items-center gap-2">
          <button @click="activeTab === 'dot' ? taiDanhSach() : taiDanhSachSp()" class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200" title="Làm mới">
            <RefreshCw class="h-4 w-4" />
          </button>
          <div class="flex items-center gap-1 ml-2">
            <button
              @click="activeTab === 'dot' ? (trangHienTai = Math.max(1, trangHienTai - 1)) : (trangHienTaiSp = Math.max(1, trangHienTaiSp - 1))"
              :disabled="(activeTab === 'dot' ? trangHienTai : trangHienTaiSp) === 1"
              class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200 disabled:opacity-40"
            ><ChevronLeft class="h-4 w-4" /></button>
            <button
              v-for="page in (activeTab === 'dot' ? tongSoTrang : tongSoTrangSp)"
              :key="page"
              @click="activeTab === 'dot' ? (trangHienTai = page) : (trangHienTaiSp = page)"
              class="flex h-8 w-8 items-center justify-center rounded-lg transition"
              :class="(activeTab === 'dot' ? trangHienTai : trangHienTaiSp) === page ? 'border border-rose-200 bg-rose-50 text-rose-600 font-bold' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
            >{{ page }}</button>
            <button
              @click="activeTab === 'dot' ? (trangHienTai = Math.min(tongSoTrang, trangHienTai + 1)) : (trangHienTaiSp = Math.min(tongSoTrangSp, trangHienTaiSp + 1))"
              :disabled="(activeTab === 'dot' ? trangHienTai : trangHienTaiSp) === (activeTab === 'dot' ? tongSoTrang : tongSoTrangSp)"
              class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200 disabled:opacity-40"
            ><ChevronRight class="h-4 w-4" /></button>
          </div>
        </div>
      </div>
    </section>

    <!-- Modal Form Thêm/Sửa -->
    <div v-if="modalOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4 py-6">
      <div class="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-[24px] bg-white shadow-2xl">
        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
          <div>
            <h2 class="text-xl font-bold text-slate-800">
              {{ modalMode === "create" ? "Thêm sản phẩm áp dụng" : "Cập nhật dữ liệu" }}
            </h2>
          </div>
          <button class="rounded-full p-2 text-slate-500 transition hover:bg-slate-100" @click="modalOpen = false">✕</button>
        </div>

        <div class="space-y-4 px-6 py-6">
          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-semibold text-slate-700">Đợt giảm giá</label>
              <select v-model="dotSanPhamForm.dotGiamGiaId" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
                <option value="">Chọn đợt giảm giá</option>
                <option v-for="item in dotOptions" :key="item.id" :value="String(item.id)">{{ item.ten }}</option>
              </select>
              <p v-if="formErrors.dotGiamGiaId" class="mt-1 text-xs text-rose-500">{{ formErrors.dotGiamGiaId }}</p>
            </div>
            <div>
              <label class="mb-1 block text-sm font-semibold text-slate-700">Giày ID</label>
              <input v-model="dotSanPhamForm.giayId" type="number" min="1" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
              <p v-if="formErrors.giayId" class="mt-1 text-xs text-rose-500">{{ formErrors.giayId }}</p>
            </div>
            <div class="md:col-span-2">
              <label class="mb-1 block text-sm font-semibold text-slate-700">Tra cứu ID Giày theo tên</label>
              <div class="relative">
                <PackageSearch class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input v-model="productSearch" type="text" placeholder="Gõ tên giày để tìm ID..." class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
              </div>
              <div v-if="productHints.length" class="mt-2 flex flex-col gap-1 rounded-2xl bg-slate-50 p-2">
                <div v-for="item in productHints.slice(0, 5)" :key="item.chiTietId" class="flex justify-between rounded-xl bg-white px-3 py-2 text-sm ring-1 ring-slate-100">
                  <span class="font-medium text-slate-700">{{ item.tenSanPham }}</span>
                  <span class="text-xs text-slate-400">ID: {{ item.chiTietId }}</span>
                </div>
              </div>
            </div>
            <div>
              <label class="mb-1 block text-sm font-semibold text-slate-700">Trạng thái</label>
              <select v-model="dotSanPhamForm.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
                <option value="1">Kích hoạt</option>
                <option value="0">Tắt</option>
              </select>
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-3 border-t border-slate-100 px-6 py-5">
          <button class="h-11 rounded-2xl bg-slate-100 px-5 text-sm font-semibold text-slate-600 transition hover:bg-slate-200" @click="modalOpen = false">Hủy bỏ</button>
          <button class="h-11 rounded-2xl bg-rose-500 px-5 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:opacity-50" :disabled="saving" @click="submitForm">
            {{ saving ? "Đang lưu..." : "Lưu thay đổi" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
