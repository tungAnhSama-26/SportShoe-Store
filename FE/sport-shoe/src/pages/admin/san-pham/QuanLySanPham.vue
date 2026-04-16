<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  Search, Plus, Trash2, Eye, ChevronDown, Check,
  X, Filter, Layers, Layers3, Images, ImageOff, FileSpreadsheet, RotateCcw
} from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'
import AdminTableFooter from '../../../components/common/AdminTableFooter.vue'
import { exportRowsToExcel } from '../../../utils/export-excel'

// ─── State ───────────────────────────────────────────────────────────────────
const items = ref([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = ref(5)
const loading = ref(false)
const danhMuc = ref(null)

const keyword = ref('')
const filterThuongHieu = ref(null)
const filterLoaiGiay = ref(null)
const filterTrangThai = ref(null)
const openDropdown = ref(null)
const route = useRoute()
const isBienThePage = computed(() => route.name === 'admin-bien-the-san-pham')
const pageTitle = computed(() =>
  isBienThePage.value ? 'Quản lý biến thể sản phẩm' : 'Quản lý sản phẩm'
)
const searchPlaceholder = computed(() =>
  isBienThePage.value
    ? 'Tìm kiếm sản phẩm để quản lý biến thể...'
    : 'Tìm kiếm sản phẩm...'
)
const emptyMessage = computed(() =>
  isBienThePage.value
    ? 'Không có sản phẩm nào để quản lý biến thể'
    : 'Không có sản phẩm nào'
)

// ─── Toast ────────────────────────────────────────────────────────────────────
const toast = reactive({ show: false, message: '', type: 'success' })
function showToast(message, type = 'success') {
  toast.message = message; toast.type = type; toast.show = true
  setTimeout(() => { toast.show = false }, 3000)
}

// ─── Load ─────────────────────────────────────────────────────────────────────
async function loadDanhMuc() {
  try { danhMuc.value = await api.layDanhMuc() } catch {}
}

async function loadData(page = 0) {
  loading.value = true
  try {
    const res = await api.layDanhSachGiay({
      keyword: keyword.value || undefined,
      thuongHieuId: filterThuongHieu.value,
      loaiGiayId: filterLoaiGiay.value,
      trangThai: filterTrangThai.value,
      page,
      size: pageSize.value
    })
    items.value = res.items
    totalItems.value = res.totalItems
    totalPages.value = res.totalPages
    currentPage.value = res.page
  } catch (e) {
    showToast(e.message || 'Lỗi tải dữ liệu', 'error')
  } finally {
    loading.value = false
  }
}

function doSearch() { loadData(0) }

onMounted(() => { loadDanhMuc(); loadData() })

// ─── Pagination ───────────────────────────────────────────────────────────────
function goPage(p) { if (p >= 0 && p < totalPages.value) loadData(p) }

const pageSizeOptions = [5, 10, 20, 50]

function formatCount(v) {
  return Number(v || 0).toLocaleString('vi-VN')
}

function handlePageSizeChange(size) {
  pageSize.value = size
  loadData(0)
}

function resetFilters() {
  keyword.value = ''
  filterThuongHieu.value = null
  filterLoaiGiay.value = null
  filterTrangThai.value = null
  openDropdown.value = null
  loadData(0)
}

async function xuatExcel() {
  if (!totalItems.value) {
    showToast('Không có dữ liệu để xuất Excel', 'error')
    return
  }

  try {
    const res = await api.layDanhSachGiay({
      keyword: keyword.value || undefined,
      thuongHieuId: filterThuongHieu.value,
      loaiGiayId: filterLoaiGiay.value,
      trangThai: filterTrangThai.value,
      page: 0,
      size: Math.max(totalItems.value, pageSize.value)
    })

    const exported = exportRowsToExcel({
      filename: isBienThePage.value ? 'quan-ly-bien-the-san-pham' : 'quan-ly-san-pham',
      sheetName: isBienThePage.value ? 'BienTheSanPham' : 'SanPham',
      columns: [
        { label: 'STT', value: (_, index) => index + 1 },
        { label: 'Mã sản phẩm', key: 'ma' },
        { label: 'Tên sản phẩm', key: 'ten' },
        { label: 'Thương hiệu', value: (row) => row.thuongHieu || '—' },
        { label: 'Loại giày', value: (row) => row.loaiGiay || '—' },
        { label: 'Giới tính', value: (row) => gioiTinhLabel(row.gioiTinh) },
        { label: 'Tổng biến thể', value: (row) => row.tongBienThe ?? 0 },
        { label: 'Tổng số lượng', value: (row) => row.tongSoLuong ?? 0 },
        { label: 'Trạng thái', value: (row) => trangThaiLabel(row.trangThai) }
      ],
      rows: res.items || []
    })

    showToast(exported ? 'Xuất Excel thành công' : 'Không có dữ liệu để xuất Excel', exported ? 'success' : 'error')
  } catch (e) {
    showToast(e.message || 'Lỗi xuất Excel', 'error')
  }
}

// ─── Giới tính helper ─────────────────────────────────────────────────────────
function gioiTinhLabel(v) {
  return v === 1 ? 'Nam' : v === 2 ? 'Nữ' : v === 3 ? 'Unisex' : 'Tất cả'
}

// ─── Trạng thái badge ─────────────────────────────────────────────────────────
function trangThaiClass(v) {
  return v === 1
    ? 'bg-emerald-50 text-emerald-600'
    : 'bg-rose-50 text-rose-500'
}
function trangThaiLabel(v) { return v === 1 ? 'Hoạt động' : 'Dừng bán' }

// ─── Dropdown helpers ─────────────────────────────────────────────────────────
function toggleDropdown(name) { openDropdown.value = openDropdown.value === name ? null : name }
function closeAll() { openDropdown.value = null }

function thuongHieuName(id) {
  if (!id || !danhMuc.value) return 'Tất cả'
  return danhMuc.value.thuongHieu.find(t => t.id === id)?.ten || 'Tất cả'
}
function loaiGiayName(id) {
  if (!id || !danhMuc.value) return 'Tất cả'
  return danhMuc.value.loaiGiay.find(l => l.id === id)?.ten || 'Tất cả'
}

// ─── Giày modal ──────────────────────────────────────────────────────────────
const showModal = ref(false)
const modalMode = ref('add')
const saving = ref(false)
const selectedGiay = ref(null)

const form = reactive({
  ma: '', ten: '', thuongHieuId: null, loaiGiayId: null,
  gioiTinh: null, chatLieu: '', moTa: '',
  deGiayId: null, coGiayId: null, congNgheDemId: null, trongLuongId: null
})
const errors = reactive({})

function clearForm() {
  Object.assign(form, {
    ma: '', ten: '', thuongHieuId: null, loaiGiayId: null,
    gioiTinh: null, chatLieu: '', moTa: '',
    deGiayId: null, coGiayId: null, congNgheDemId: null, trongLuongId: null
  })
  Object.keys(errors).forEach(k => delete errors[k])
}

function openAdd() { clearForm(); modalMode.value = 'add'; showModal.value = true }
function openEdit(g) {
  clearForm()
  Object.assign(form, {
    ma: g.ma, ten: g.ten, thuongHieuId: g.thuongHieuId, loaiGiayId: g.loaiGiayId,
    gioiTinh: g.gioiTinh, chatLieu: g.chatLieu || '', moTa: g.moTa || '',
    deGiayId: g.thuocTinh?.deGiayId || null, coGiayId: g.thuocTinh?.coGiayId || null,
    congNgheDemId: g.thuocTinh?.congNgheDemId || null, trongLuongId: g.thuocTinh?.trongLuongId || null
  })
  selectedGiay.value = g
  modalMode.value = 'edit'
  showModal.value = true
}
async function openView(g) {
  try {
    const detail = await api.chiTietGiay(g.id)
    openEdit(detail)
  } catch (e) { showToast(e.message || 'Lỗi tải chi tiết', 'error') }
}

function validateForm() {
  Object.keys(errors).forEach(k => delete errors[k])
  if (!form.ten.trim()) errors.ten = 'Tên không được để trống'
  if (!form.thuongHieuId) errors.thuongHieuId = 'Chọn thương hiệu'
  if (!form.loaiGiayId) errors.loaiGiayId = 'Chọn loại giày'
  if (modalMode.value === 'add' && !form.ma.trim()) errors.ma = 'Mã không được để trống'
  return Object.keys(errors).length === 0
}

async function handleSave() {
  if (!validateForm()) return
  saving.value = true
  try {
    if (modalMode.value === 'add') {
      await api.taoGiay({ ...form })
      showToast('Tạo giày thành công')
    } else {
      await api.capNhatGiay(selectedGiay.value.id, { ...form })
      showToast('Cập nhật thành công')
    }
    showModal.value = false
    loadData(currentPage.value)
  } catch (e) {
    showToast(e.message || 'Có lỗi xảy ra', 'error')
  } finally {
    saving.value = false
  }
}

async function handleDelete(g) {
  if (!confirm(`Xóa giày "${g.ten}"?`)) return
  try {
    await api.xoaGiay(g.id)
    showToast('Xóa thành công')
    loadData(currentPage.value)
  } catch (e) { showToast(e.message || 'Lỗi xóa', 'error') }
}

async function handleToggleTrangThai(g) {
  try {
    await api.doiTrangThai(g.id, g.trangThai === 1 ? 0 : 1)
    showToast('Cập nhật trạng thái thành công')
    loadData(currentPage.value)
  } catch (e) { showToast(e.message || 'Lỗi cập nhật trạng thái', 'error') }
}

// ─── Biến thể modal ───────────────────────────────────────────────────────────
const showBienTheModal = ref(false)
const bienTheGiay = ref(null)
const bienTheList = ref([])
const loadingBienThe = ref(false)
const showAddBienTheForm = ref(false)
const savingBienThe = ref(false)
const editingBienThe = ref(null)

const bienTheForm = reactive({
  mauSacId: null, kichCoId: null, soLuong: 0, giaGoc: 0, giaBan: 0, kichHoat: 1
})
const bienTheErrors = reactive({})

function clearBienTheSelection() {
  bienTheGiay.value = null
  bienTheList.value = []
  loadingBienThe.value = false
  showAddBienTheForm.value = false
  editingBienThe.value = null
  Object.assign(bienTheForm, { mauSacId: null, kichCoId: null, soLuong: 0, giaGoc: 0, giaBan: 0, kichHoat: 1 })
  Object.keys(bienTheErrors).forEach(k => delete bienTheErrors[k])
}

async function openBienThe(g) {
  bienTheGiay.value = g
  showBienTheModal.value = true
  showAddBienTheForm.value = false
  editingBienThe.value = null
  loadingBienThe.value = true
  try {
    bienTheList.value = await api.layBienThe(g.id)
  } catch (e) {
    showToast(e.message || 'Lỗi tải biến thể', 'error')
  } finally {
    loadingBienThe.value = false
  }
}

function closeBienTheModal() {
  showBienTheModal.value = false
  clearBienTheSelection()
}

function closeBienTheForm() {
  showAddBienTheForm.value = false
}

function openAddBienTheForm() {
  editingBienThe.value = null
  Object.assign(bienTheForm, { mauSacId: null, kichCoId: null, soLuong: 0, giaGoc: 0, giaBan: 0, kichHoat: 1 })
  Object.keys(bienTheErrors).forEach(k => delete bienTheErrors[k])
  showAddBienTheForm.value = true
}

function openEditBienThe(bt) {
  editingBienThe.value = bt
  Object.assign(bienTheForm, { mauSacId: bt.mauSacId, kichCoId: bt.kichCoId, soLuong: bt.soLuong, giaGoc: bt.giaGoc, giaBan: bt.giaBan, kichHoat: bt.kichHoat })
  Object.keys(bienTheErrors).forEach(k => delete bienTheErrors[k])
  showAddBienTheForm.value = true
}

async function handleSaveBienThe() {
  Object.keys(bienTheErrors).forEach(k => delete bienTheErrors[k])
  if (!editingBienThe.value) {
    if (!bienTheForm.mauSacId) bienTheErrors.mauSacId = 'Chọn màu sắc'
    if (!bienTheForm.kichCoId) bienTheErrors.kichCoId = 'Chọn kích cỡ'
  }
  if (!bienTheForm.giaBan || bienTheForm.giaBan <= 0) bienTheErrors.giaBan = 'Giá bán phải > 0'
  if (Object.keys(bienTheErrors).length > 0) return

  savingBienThe.value = true
  try {
    if (editingBienThe.value) {
      await api.capNhatBienThe(editingBienThe.value.id, {
        soLuong: Number(bienTheForm.soLuong),
        giaGoc: Number(bienTheForm.giaGoc),
        giaBan: Number(bienTheForm.giaBan),
        kichHoat: Number(bienTheForm.kichHoat)
      })
    } else {
      await api.taoBienThe(bienTheGiay.value.id, {
        mauSacId: Number(bienTheForm.mauSacId),
        kichCoId: Number(bienTheForm.kichCoId),
        soLuong: Number(bienTheForm.soLuong),
        giaGoc: Number(bienTheForm.giaGoc),
        giaBan: Number(bienTheForm.giaBan)
      })
    }
    showToast('Lưu biến thể thành công')
    closeBienTheForm()
    bienTheList.value = await api.layBienThe(bienTheGiay.value.id)
    loadData(currentPage.value)
  } catch (e) {
    showToast(e.message || 'Lỗi lưu biến thể', 'error')
  } finally {
    savingBienThe.value = false
  }
}

async function handleDeleteBienThe(id) {
  if (!confirm('Xóa biến thể này?')) return
  try {
    await api.xoaBienThe(id)
    showToast('Xóa biến thể thành công')
    bienTheList.value = await api.layBienThe(bienTheGiay.value.id)
    loadData(currentPage.value)
  } catch (e) { showToast(e.message || 'Lỗi xóa', 'error') }
}

function formatPrice(v) {
  if (!v) return '0'
  return Number(v).toLocaleString('vi-VN')
}

// ─── Hình ảnh modal ───────────────────────────────────────────────────────────
const showHinhAnhModal = ref(false)
const hinhAnhBienThe = ref(null)
const hinhAnhList = ref([])
const loadingHinhAnh = ref(false)
const showAddHinhAnhForm = ref(false)
const savingHinhAnh = ref(false)
const settingHinhChinhId = ref(null)
const deletingHinhAnhId = ref(null)

const hinhAnhForm = reactive({ url: '', loaiHinh: 2, moTa: '' })
const hinhAnhErrors = reactive({})

watch(
  () => route.fullPath,
  () => {
    showModal.value = false
    showBienTheModal.value = false
    showHinhAnhModal.value = false
    clearBienTheSelection()
    showAddHinhAnhForm.value = false
    openDropdown.value = null
  }
)

async function openHinhAnh(bt) {
  hinhAnhBienThe.value = bt
  showHinhAnhModal.value = true
  showAddHinhAnhForm.value = false
  loadingHinhAnh.value = true
  try {
    hinhAnhList.value = await api.layHinhAnh(bt.id)
  } catch (e) {
    showToast(e.message || 'Lỗi tải hình ảnh', 'error')
  } finally {
    loadingHinhAnh.value = false
  }
}

function closeHinhAnhModal() { showHinhAnhModal.value = false; hinhAnhBienThe.value = null }

function openAddHinhAnhForm() {
  Object.assign(hinhAnhForm, { url: '', loaiHinh: 2, moTa: '' })
  Object.keys(hinhAnhErrors).forEach(k => delete hinhAnhErrors[k])
  showAddHinhAnhForm.value = true
}

async function handleSaveHinhAnh() {
  Object.keys(hinhAnhErrors).forEach(k => delete hinhAnhErrors[k])
  if (!hinhAnhForm.url.trim()) { hinhAnhErrors.url = 'URL không được để trống'; return }
  savingHinhAnh.value = true
  try {
    await api.themHinhAnh(hinhAnhBienThe.value.id, {
      url: hinhAnhForm.url.trim(),
      loaiHinh: hinhAnhForm.loaiHinh,
      moTa: hinhAnhForm.moTa || undefined
    })
    showToast('Thêm hình ảnh thành công')
    showAddHinhAnhForm.value = false
    hinhAnhList.value = await api.layHinhAnh(hinhAnhBienThe.value.id)
    loadData(currentPage.value)
  } catch (e) {
    showToast(e.message || 'Lỗi thêm hình ảnh', 'error')
  } finally {
    savingHinhAnh.value = false
  }
}

async function handleDeleteHinhAnh(id) {
  if (!confirm('Xóa hình ảnh này?')) return
  deletingHinhAnhId.value = id
  try {
    await api.xoaHinhAnh(id)
    showToast('Xóa hình ảnh thành công')
    hinhAnhList.value = await api.layHinhAnh(hinhAnhBienThe.value.id)
    loadData(currentPage.value)
  } catch (e) { showToast(e.message || 'Lỗi xóa hình ảnh', 'error') }
  finally { deletingHinhAnhId.value = null }
}

async function handleDatHinhChinh(id) {
  settingHinhChinhId.value = id
  try {
    await api.datHinhChinh(id)
    showToast('Đặt hình chính thành công')
    hinhAnhList.value = await api.layHinhAnh(hinhAnhBienThe.value.id)
    loadData(currentPage.value)
  } catch (e) { showToast(e.message || 'Lỗi đặt hình chính', 'error') }
  finally { settingHinhChinhId.value = null }
}
</script>

<template>
  <div class="space-y-5" @click="closeAll">
    <!-- Toast -->
    <Transition name="fade">
      <div v-if="toast.show" class="fixed right-4 top-[88px] z-50 rounded-2xl px-4 py-3 text-sm font-medium text-white shadow-lg"
        :class="toast.type === 'success' ? 'bg-green-500' : 'bg-red-500'">
        {{ toast.message }}
      </div>
    </Transition>

    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">{{ pageTitle }}</h1>
    </div>

    <!-- Filters -->
    <section class="admin-section-card !p-4">
      <div class="mb-4 flex items-center gap-3">
        <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-100 text-slate-600">
          <Filter class="h-4 w-4" />
        </div>
        <div>
          <h2 class="text-sm font-bold text-slate-800">Bộ lọc</h2>
        </div>
      </div>

      <div class="flex flex-col gap-3">
        <div class="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
          <div class="min-w-0 flex-1">
            <div class="relative max-w-[680px]">
              <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" :size="15" />
              <input v-model="keyword" @keyup.enter="doSearch" type="text" :placeholder="searchPlaceholder"
                class="h-10 w-full rounded-xl border border-slate-200 bg-slate-50 pl-10 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-2.5 xl:justify-end">
            <button @click="resetFilters" class="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-rose-200 bg-rose-50 px-4 text-sm font-semibold text-rose-600 transition hover:border-rose-300 hover:bg-rose-100 hover:text-rose-700">
              <RotateCcw :size="15" />
              Đặt lại bộ lọc
            </button>
            <button @click="doSearch" class="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-rose-200 bg-white px-4 text-sm font-semibold text-rose-600 transition hover:border-rose-300 hover:bg-rose-50 hover:text-rose-700">
              <Search :size="15" />
              Tìm kiếm
            </button>
            <button @click="xuatExcel" class="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-rose-200 bg-white px-4 text-sm font-semibold text-rose-600 transition hover:border-rose-300 hover:bg-rose-50 hover:text-rose-700">
              <FileSpreadsheet :size="15" />
              Xuất Excel
            </button>
            <button v-if="!isBienThePage" @click="openAdd" class="inline-flex h-10 items-center gap-2 rounded-xl bg-rose-500 px-4 text-sm font-semibold text-white shadow-sm shadow-rose-200 transition hover:bg-rose-600">
              <Plus :size="15" /> Thêm sản phẩm
            </button>
          </div>
        </div>

        <div class="flex flex-wrap items-start gap-3">
          <div class="relative w-full sm:w-[220px] md:w-[240px]" @click.stop>
            <label class="mb-1 block text-[10px] font-normal uppercase tracking-wide text-slate-500">Thương hiệu</label>
            <button @click="toggleDropdown('thuongHieu')"
              class="flex h-9 w-full items-center justify-between gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 text-[13px] transition hover:bg-white"
              :class="filterThuongHieu ? 'border-rose-300 text-rose-600' : 'text-slate-600'">
              <span class="truncate">{{ thuongHieuName(filterThuongHieu) }}</span>
              <ChevronDown :size="14" />
            </button>
            <div v-if="openDropdown === 'thuongHieu'" class="absolute top-full left-0 z-20 mt-1 max-h-52 w-full min-w-[220px] overflow-y-auto rounded-2xl border border-gray-200 bg-white py-1 shadow-lg">
              <button @click="filterThuongHieu = null; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterThuongHieu === null ? 'text-rose-600 font-medium' : 'text-gray-700'">
                Tất cả <Check v-if="filterThuongHieu === null" :size="14" />
              </button>
              <button v-for="t in danhMuc?.thuongHieu" :key="t.id"
                @click="filterThuongHieu = t.id; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterThuongHieu === t.id ? 'text-rose-600 font-medium' : 'text-gray-700'">
                {{ t.ten }} <Check v-if="filterThuongHieu === t.id" :size="14" />
              </button>
            </div>
          </div>

          <div class="relative w-full sm:w-[220px] md:w-[240px]" @click.stop>
            <label class="mb-1 block text-[10px] font-normal uppercase tracking-wide text-slate-500">Loại giày</label>
            <button @click="toggleDropdown('loaiGiay')"
              class="flex h-9 w-full items-center justify-between gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 text-[13px] transition hover:bg-white"
              :class="filterLoaiGiay ? 'border-rose-300 text-rose-600' : 'text-slate-600'">
              <span class="truncate">{{ loaiGiayName(filterLoaiGiay) }}</span>
              <ChevronDown :size="14" />
            </button>
            <div v-if="openDropdown === 'loaiGiay'" class="absolute top-full left-0 z-20 mt-1 max-h-52 w-full min-w-[220px] overflow-y-auto rounded-2xl border border-gray-200 bg-white py-1 shadow-lg">
              <button @click="filterLoaiGiay = null; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterLoaiGiay === null ? 'text-rose-600 font-medium' : 'text-gray-700'">
                Tất cả <Check v-if="filterLoaiGiay === null" :size="14" />
              </button>
              <button v-for="l in danhMuc?.loaiGiay" :key="l.id"
                @click="filterLoaiGiay = l.id; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterLoaiGiay === l.id ? 'text-rose-600 font-medium' : 'text-gray-700'">
                {{ l.ten }} <Check v-if="filterLoaiGiay === l.id" :size="14" />
              </button>
            </div>
          </div>

          <div class="relative w-full sm:w-[170px] md:w-[185px]" @click.stop>
            <label class="mb-1 block text-[10px] font-normal uppercase tracking-wide text-slate-500">Trạng thái</label>
            <button @click="toggleDropdown('trangThai')"
              class="flex h-9 w-full items-center justify-between gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 text-[13px] transition hover:bg-white"
              :class="filterTrangThai !== null ? 'border-rose-300 text-rose-600' : 'text-slate-600'">
              <span class="truncate">{{ filterTrangThai === 1 ? 'Hoạt động' : filterTrangThai === 0 ? 'Dừng bán' : 'Tất cả' }}</span>
              <ChevronDown :size="14" />
            </button>
            <div v-if="openDropdown === 'trangThai'" class="absolute top-full left-0 z-20 mt-1 w-full min-w-[220px] rounded-2xl border border-gray-200 bg-white py-1 shadow-lg">
              <button @click="filterTrangThai = null; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterTrangThai === null ? 'text-rose-600 font-medium' : 'text-gray-700'">
                Tất cả <Check v-if="filterTrangThai === null" :size="14" />
              </button>
              <button @click="filterTrangThai = 1; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterTrangThai === 1 ? 'text-rose-600 font-medium' : 'text-gray-700'">
                Hoạt động <Check v-if="filterTrangThai === 1" :size="14" />
              </button>
              <button @click="filterTrangThai = 0; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterTrangThai === 0 ? 'text-rose-600 font-medium' : 'text-gray-700'">
                Dừng bán <Check v-if="filterTrangThai === 0" :size="14" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Table -->
    <section class="admin-section-card">
      <div class="admin-section-header">
        <div class="admin-section-icon admin-section-icon--violet">
          <Layers3 class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">{{ isBienThePage ? 'Biến thể sản phẩm' : 'Danh sách sản phẩm' }}</h2>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="admin-table admin-table--wide">
          <thead class="bg-gray-50 border-b border-gray-100">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-12">STT</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-16">Ảnh</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Sản phẩm</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Loại / Thương hiệu</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-24">Giới tính</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider" :class="isBienThePage ? 'w-32' : 'w-28'">Trạng thái</th>
              <th class="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider" :class="isBienThePage ? 'w-40' : 'w-32'">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <template v-if="loading">
              <tr v-for="j in 7" :key="j" class="animate-pulse">
                <td class="px-4 py-3"><div class="h-4 bg-gray-200 rounded w-6"></div></td>
                <td class="px-4 py-3"><div class="h-10 w-10 bg-gray-200 rounded-lg"></div></td>
                <td class="px-4 py-3"><div class="h-4 bg-gray-200 rounded w-40"></div></td>
                <td class="px-4 py-3"><div class="h-4 bg-gray-200 rounded w-32"></div></td>
                <td class="px-4 py-3"><div class="h-4 bg-gray-200 rounded w-16"></div></td>
                <td class="px-4 py-3"><div class="h-6 bg-gray-200 rounded w-20"></div></td>
                <td class="px-4 py-3"><div class="h-4 bg-gray-200 rounded w-24 ml-auto"></div></td>
              </tr>
            </template>
            <template v-else-if="items.length === 0">
              <tr>
                <td colspan="7" class="px-4 py-12 text-center text-gray-400">{{ emptyMessage }}</td>
              </tr>
            </template>
            <template v-else>
              <tr v-for="(item, idx) in items" :key="item.id" class="transition-colors"
                :class="isBienThePage && bienTheGiay?.id === item.id ? '!bg-violet-50 hover:!bg-violet-50' : 'hover:bg-gray-50'">
                <td class="px-4 py-3 text-gray-500">{{ currentPage * pageSize + idx + 1 }}</td>
                <td class="px-4 py-3">
                  <img v-if="item.hinhAnh" :src="item.hinhAnh" alt="" class="h-10 w-10 object-cover rounded-lg border border-gray-100" />
                  <div v-else class="h-10 w-10 rounded-lg bg-gray-100 flex items-center justify-center">
                    <ImageOff :size="16" class="text-gray-400" />
                  </div>
                </td>
                <td class="px-4 py-3">
                  <div class="font-medium text-gray-800">{{ item.ten }}</div>
                  <div class="mt-1 text-xs font-semibold text-slate-800">
                    {{ item.ma }}
                  </div>
                  <div class="text-xs text-gray-400 mt-0.5">
                    {{ item.tongBienThe ?? 0 }} biến thể · {{ item.tongSoLuong ?? 0 }} sản phẩm
                  </div>
                </td>
                <td class="px-4 py-3">
                  <div class="text-gray-700">{{ item.loaiGiay }}</div>
                  <div class="text-xs text-gray-400">{{ item.thuongHieu }}</div>
                </td>
                <td class="px-4 py-3 text-gray-600">{{ gioiTinhLabel(item.gioiTinh) }}</td>
                <td class="px-4 py-3">
                  <button @click="handleToggleTrangThai(item)"
                    class="admin-status-chip transition-colors"
                    :class="trangThaiClass(item.trangThai)">
                    {{ trangThaiLabel(item.trangThai) }}
                  </button>
                </td>
                <td class="px-4 py-3">
                  <div class="flex items-center gap-1 justify-end">
                    <button v-if="isBienThePage" @click="openBienThe(item)" title="Xem biến thể"
                      class="admin-table-action whitespace-nowrap text-violet-500 hover:text-violet-600"
                      :class="[
                        isBienThePage ? 'admin-table-action--wide text-xs font-medium' : '',
                        isBienThePage && bienTheGiay?.id === item.id ? '!bg-violet-100 !text-violet-700' : ''
                      ]">
                      <Layers :size="14" />
                      <span>Xem biến thể</span>
                    </button>
                    <button v-if="!isBienThePage" @click="openView(item)" title="Xem và sửa"
                      class="admin-table-action text-slate-600 hover:text-rose-500">
                      <Eye :size="14" />
                    </button>
                    <button v-if="!isBienThePage" @click="handleDelete(item)" title="Xóa"
                      class="admin-table-action text-red-500 hover:text-red-600">
                      <Trash2 :size="14" />
                    </button>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <AdminTableFooter
        :current-page="currentPage"
        :page-size="pageSize"
        :page-size-options="pageSizeOptions"
        :total-items="totalItems"
        :total-pages="totalPages"
        compact
        show-refresh
        zero-based
        @refresh="loadData(currentPage)"
        @update:current-page="goPage"
        @update:page-size="handlePageSizeChange"
      />
    </section>

    <!-- ─── Giày Modal ─────────────────────────────────────────────────────── -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showModal = false">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800">
              {{ modalMode === 'add' ? 'Thêm sản phẩm' : modalMode === 'edit' ? 'Cập nhật sản phẩm' : 'Chi tiết sản phẩm' }}
            </h2>
            <button @click="showModal = false" class="p-1.5 rounded-lg hover:bg-gray-100"><X :size="18" /></button>
          </div>
          <div class="overflow-y-auto flex-1 p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <!-- Mã -->
              <div v-if="modalMode === 'add'">
                <label class="block text-xs font-medium text-gray-700 mb-1">Mã sản phẩm *</label>
                <input v-model="form.ma" :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400 uppercase"
                  :class="errors.ma ? 'border-red-400' : 'border-gray-200'" placeholder="VD: NK001" />
                <p v-if="errors.ma" class="text-xs text-red-500 mt-1">{{ errors.ma }}</p>
              </div>
              <!-- Tên -->
              <div :class="modalMode === 'add' ? '' : 'col-span-2'">
                <label class="block text-xs font-medium text-gray-700 mb-1">Tên sản phẩm *</label>
                <input v-model="form.ten" :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="errors.ten ? 'border-red-400' : 'border-gray-200'" placeholder="Tên sản phẩm" />
                <p v-if="errors.ten" class="text-xs text-red-500 mt-1">{{ errors.ten }}</p>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <!-- Thương hiệu -->
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Thương hiệu *</label>
                <select v-model="form.thuongHieuId" :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="errors.thuongHieuId ? 'border-red-400' : 'border-gray-200'">
                  <option :value="null">-- Chọn thương hiệu --</option>
                  <option v-for="t in danhMuc?.thuongHieu" :key="t.id" :value="t.id">{{ t.ten }}</option>
                </select>
                <p v-if="errors.thuongHieuId" class="text-xs text-red-500 mt-1">{{ errors.thuongHieuId }}</p>
              </div>
              <!-- Loại giày -->
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Loại giày *</label>
                <select v-model="form.loaiGiayId" :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="errors.loaiGiayId ? 'border-red-400' : 'border-gray-200'">
                  <option :value="null">-- Chọn loại giày --</option>
                  <option v-for="l in danhMuc?.loaiGiay" :key="l.id" :value="l.id">{{ l.ten }}</option>
                </select>
                <p v-if="errors.loaiGiayId" class="text-xs text-red-500 mt-1">{{ errors.loaiGiayId }}</p>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Giới tính</label>
                <select v-model="form.gioiTinh" :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400">
                  <option :value="null">-- Tất cả --</option>
                  <option :value="1">Nam</option>
                  <option :value="2">Nữ</option>
                  <option :value="3">Unisex</option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Chất liệu</label>
                <input v-model="form.chatLieu" :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  placeholder="VD: Da thật, vải lưới..." />
              </div>
            </div>
            <!-- Thuộc tính kỹ thuật -->
            <div class="border border-gray-100 rounded-xl p-4 bg-gray-50">
              <p class="text-xs font-semibold text-gray-500 mb-3 uppercase tracking-wide">Thuộc tính kỹ thuật</p>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-medium text-gray-700 mb-1">Đế giày</label>
                  <select v-model="form.deGiayId" :disabled="modalMode === 'view'"
                    class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400">
                    <option :value="null">-- Không có --</option>
                    <option v-for="d in danhMuc?.deGiay" :key="d.id" :value="d.id">{{ d.ten }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-xs font-medium text-gray-700 mb-1">Cổ giày</label>
                  <select v-model="form.coGiayId" :disabled="modalMode === 'view'"
                    class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400">
                    <option :value="null">-- Không có --</option>
                    <option v-for="c in danhMuc?.coGiay" :key="c.id" :value="c.id">{{ c.ten }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-xs font-medium text-gray-700 mb-1">Công nghệ đệm</label>
                  <select v-model="form.congNgheDemId" :disabled="modalMode === 'view'"
                    class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400">
                    <option :value="null">-- Không có --</option>
                    <option v-for="c in danhMuc?.congNgheDem" :key="c.id" :value="c.id">{{ c.ten }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-xs font-medium text-gray-700 mb-1">Trọng lượng</label>
                  <select v-model="form.trongLuongId" :disabled="modalMode === 'view'"
                    class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400">
                    <option :value="null">-- Không có --</option>
                    <option v-for="t in danhMuc?.trongLuong" :key="t.id" :value="t.id">{{ t.ma }} - {{ t.giaTri }}</option>
                  </select>
                </div>
              </div>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1">Mô tả</label>
              <textarea v-model="form.moTa" :disabled="modalMode === 'view'" rows="3"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400 resize-none"
                placeholder="Mô tả sản phẩm..."></textarea>
            </div>
          </div>
          <div v-if="modalMode !== 'view'" class="px-6 py-4 border-t border-gray-100 flex justify-end gap-3">
            <button @click="showModal = false" class="px-4 py-2 border border-gray-200 rounded-lg text-sm hover:bg-gray-50">Hủy</button>
            <button @click="handleSave" :disabled="saving"
              class="px-4 py-2 bg-rose-500 hover:bg-rose-600 text-white rounded-lg text-sm font-medium transition-colors disabled:opacity-60">
              {{ saving ? 'Đang lưu...' : 'Lưu' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ─── Biến thể Modal ─────────────────────────────────────────────────── -->
    <Teleport to="body">
      <div v-if="showBienTheModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="closeBienTheModal">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-4xl max-h-[90vh] flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800">Xem biến thể - {{ bienTheGiay?.ten }}</h2>
            <button @click="closeBienTheModal" class="p-1.5 rounded-lg hover:bg-gray-100"><X :size="18" /></button>
          </div>
          <div class="overflow-y-auto flex-1 p-6">
            <!-- Loading -->
            <div v-if="loadingBienThe" class="text-center py-8 text-gray-400">Đang tải...</div>
            <!-- List -->
            <div v-else>
              <div class="overflow-x-auto mb-4">
                <table class="admin-table admin-table--compact table-fixed">
                  <colgroup>
                    <col class="w-[24%]" />
                    <col class="w-[12%]" />
                    <col class="w-[12%]" />
                    <col class="w-[18%]" />
                    <col class="w-[17%]" />
                    <col class="w-[17%]" />
                  </colgroup>
                  <thead class="bg-gray-50 border-b border-gray-100">
                    <tr>
                      <th class="px-3 py-2.5 text-left text-xs font-semibold text-gray-500">Màu sắc</th>
                      <th class="px-3 py-2.5 text-center text-xs font-semibold text-gray-500">Kích cỡ</th>
                      <th class="px-3 py-2.5 text-center text-xs font-semibold text-gray-500">Số lượng</th>
                      <th class="px-3 py-2.5 text-center text-xs font-semibold text-gray-500">Giá bán</th>
                      <th class="px-3 py-2.5 text-center text-xs font-semibold text-gray-500">Trạng thái</th>
                      <th class="px-3 py-2.5 text-center text-xs font-semibold text-gray-500">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-50">
                    <tr v-if="bienTheList.length === 0">
                      <td colspan="6" class="px-3 py-8 text-center text-gray-400 text-sm">Chưa có biến thể nào</td>
                    </tr>
                    <tr v-for="bt in bienTheList" :key="bt.id" class="hover:bg-gray-50">
                      <td class="px-3 py-2.5">
                        <div class="flex min-w-0 items-center gap-3">
                          <span v-if="bt.maMauHex" class="w-4 h-4 rounded-full border border-gray-200 flex-shrink-0" :style="`background:${bt.maMauHex}`"></span>
                          <span class="truncate font-medium text-slate-700">{{ bt.mauSac }}</span>
                        </div>
                      </td>
                      <td class="px-3 py-2.5 text-center font-medium text-slate-700">{{ bt.kichCo }}</td>
                      <td class="px-3 py-2.5 text-center font-medium tabular-nums text-slate-700">{{ bt.soLuong }}</td>
                      <td class="px-3 py-2.5 text-center font-semibold tabular-nums text-slate-700">{{ formatPrice(bt.giaBan) }}đ</td>
                      <td class="px-3 py-2.5 text-center">
                        <div class="flex justify-center">
                          <span class="admin-status-chip whitespace-nowrap"
                            :class="bt.kichHoat === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-500'">
                            {{ bt.kichHoat === 1 ? 'Kích hoạt' : 'Tắt' }}
                          </span>
                        </div>
                      </td>
                      <td class="px-3 py-2.5">
                        <div class="flex flex-nowrap items-center justify-center gap-1.5">
                          <button @click="openHinhAnh(bt)" title="Hình ảnh"
                            class="admin-table-action text-emerald-500 hover:text-emerald-600">
                            <Images :size="14" />
                          </button>
                          <button @click="openEditBienThe(bt)" title="Xem và sửa"
                            class="admin-table-action text-slate-600 hover:text-rose-500">
                            <Eye :size="14" />
                          </button>
                          <button @click="handleDeleteBienThe(bt.id)"
                            class="admin-table-action text-red-500 hover:text-red-600">
                            <Trash2 :size="14" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <button v-if="!showAddBienTheForm" @click="openAddBienTheForm"
                class="flex items-center gap-1.5 text-sm text-rose-600 hover:text-rose-700 font-medium mt-2">
                <Plus :size="14" /> Thêm biến thể
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ─── Hình ảnh Modal ─────────────────────────────────────────────────── -->
    <Teleport to="body">
      <div v-if="showAddBienTheForm" class="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/55" @click.self="closeBienTheForm">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-xl max-h-[90vh] flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800">
              {{ editingBienThe ? 'Cập nhật biến thể' : 'Thêm biến thể mới' }}
            </h2>
            <button @click="closeBienTheForm" class="p-1.5 rounded-lg hover:bg-gray-100"><X :size="18" /></button>
          </div>

          <div class="overflow-y-auto p-6">
            <div class="grid grid-cols-2 gap-4">
              <div v-if="!editingBienThe">
                <label class="text-xs font-medium text-gray-700 mb-1 block">Màu sắc *</label>
                <select v-model="bienTheForm.mauSacId"
                  class="w-full px-3 py-2 border rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="bienTheErrors.mauSacId ? 'border-red-400' : 'border-gray-200'">
                  <option :value="null">-- Chọn màu --</option>
                  <option v-for="m in danhMuc?.mauSac" :key="m.id" :value="m.id">{{ m.ten }}</option>
                </select>
                <p v-if="bienTheErrors.mauSacId" class="text-xs text-red-500 mt-1">{{ bienTheErrors.mauSacId }}</p>
              </div>

              <div v-if="!editingBienThe">
                <label class="text-xs font-medium text-gray-700 mb-1 block">Kích cỡ *</label>
                <select v-model="bienTheForm.kichCoId"
                  class="w-full px-3 py-2 border rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="bienTheErrors.kichCoId ? 'border-red-400' : 'border-gray-200'">
                  <option :value="null">-- Chọn cỡ --</option>
                  <option v-for="k in danhMuc?.kichCo" :key="k.id" :value="k.id">{{ k.giaTri }}</option>
                </select>
                <p v-if="bienTheErrors.kichCoId" class="text-xs text-red-500 mt-1">{{ bienTheErrors.kichCoId }}</p>
              </div>

              <div>
                <label class="text-xs font-medium text-gray-700 mb-1 block">Số lượng</label>
                <input v-model.number="bienTheForm.soLuong" type="number" min="0"
                  class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400 bg-white" />
              </div>

              <div>
                <label class="text-xs font-medium text-gray-700 mb-1 block">Giá gốc</label>
                <input v-model.number="bienTheForm.giaGoc" type="number" min="0"
                  class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400 bg-white" />
              </div>

              <div>
                <label class="text-xs font-medium text-gray-700 mb-1 block">Giá bán *</label>
                <input v-model.number="bienTheForm.giaBan" type="number" min="1"
                  class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400 bg-white"
                  :class="bienTheErrors.giaBan ? 'border-red-400' : 'border-gray-200'" />
                <p v-if="bienTheErrors.giaBan" class="text-xs text-red-500 mt-1">{{ bienTheErrors.giaBan }}</p>
              </div>

              <div v-if="editingBienThe">
                <label class="text-xs font-medium text-gray-700 mb-1 block">Trạng thái</label>
                <select v-model.number="bienTheForm.kichHoat"
                  class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400">
                  <option :value="1">Kích hoạt</option>
                  <option :value="0">Tắt</option>
                </select>
              </div>
            </div>
          </div>

          <div class="px-6 py-4 border-t border-gray-100 flex justify-end gap-3">
            <button @click="closeBienTheForm" class="px-4 py-2 border border-gray-200 rounded-lg text-sm hover:bg-gray-50">Hủy</button>
            <button @click="handleSaveBienThe" :disabled="savingBienThe"
              class="px-4 py-2 bg-rose-500 hover:bg-rose-600 text-white rounded-lg text-sm font-medium disabled:opacity-60">
              {{ savingBienThe ? 'Đang lưu...' : 'Lưu' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showHinhAnhModal" class="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/60" @click.self="closeHinhAnhModal">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-gray-800">
              Hình ảnh - {{ hinhAnhBienThe?.mauSac }} / {{ hinhAnhBienThe?.kichCo }}
            </h2>
            <button @click="closeHinhAnhModal" class="p-1.5 rounded-lg hover:bg-gray-100"><X :size="18" /></button>
          </div>
          <div class="overflow-y-auto flex-1 p-6">
            <div v-if="loadingHinhAnh" class="text-center py-8 text-gray-400">Đang tải...</div>
            <div v-else>
              <!-- Grid of images -->
              <div v-if="hinhAnhList.length > 0" class="grid grid-cols-2 sm:grid-cols-3 gap-3 mb-4">
                <div v-for="h in hinhAnhList" :key="h.id" class="relative group rounded-xl overflow-hidden border border-gray-100 bg-gray-50">
                  <div class="aspect-square">
                    <img :src="h.url" :alt="h.moTa || ''" class="w-full h-full object-cover" />
                  </div>
                  <div v-if="h.laHinhChinh" class="absolute top-1 left-1 px-1.5 py-0.5 bg-yellow-400 text-yellow-900 text-xs font-medium rounded">
                    Ảnh chính
                  </div>
                  <div class="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-all flex items-end justify-center pb-2 opacity-0 group-hover:opacity-100 gap-1">
                    <button v-if="!h.laHinhChinh" @click="handleDatHinhChinh(h.id)"
                      :disabled="settingHinhChinhId === h.id"
                      class="px-2 py-1 bg-yellow-400 hover:bg-yellow-500 text-yellow-900 text-xs rounded font-medium disabled:opacity-60">
                      {{ settingHinhChinhId === h.id ? '...' : 'Đặt chính' }}
                    </button>
                    <button @click="handleDeleteHinhAnh(h.id)" :disabled="deletingHinhAnhId === h.id"
                      class="p-1 bg-red-500 hover:bg-red-600 text-white rounded disabled:opacity-60">
                      <Trash2 :size="12" />
                    </button>
                  </div>
                  <p v-if="h.moTa" class="px-2 py-1 text-xs text-gray-500 truncate border-t border-gray-100">{{ h.moTa }}</p>
                </div>
              </div>
              <p v-else class="text-center text-gray-400 py-6 text-sm">Chưa có hình ảnh nào</p>

              <!-- Add form -->
              <div v-if="showAddHinhAnhForm" class="border border-rose-100 rounded-xl p-4 bg-rose-50">
                <p class="text-sm font-semibold text-rose-700 mb-3">Thêm hình ảnh</p>
                <div class="space-y-3">
                  <div>
                    <label class="text-xs font-medium text-gray-700 mb-1 block">URL hình ảnh *</label>
                    <input v-model="hinhAnhForm.url" type="url" placeholder="https://..."
                      class="w-full px-3 py-2 border rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                      :class="hinhAnhErrors.url ? 'border-red-400' : 'border-gray-200'" />
                    <p v-if="hinhAnhErrors.url" class="text-xs text-red-500 mt-1">{{ hinhAnhErrors.url }}</p>
                  </div>
                  <div>
                    <label class="text-xs font-medium text-gray-700 mb-1 block">Loại hình</label>
                    <select v-model.number="hinhAnhForm.loaiHinh"
                      class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400">
                      <option :value="1">Ảnh chính (1)</option>
                      <option :value="2">Ảnh phụ (2)</option>
                    </select>
                  </div>
                  <div>
                    <label class="text-xs font-medium text-gray-700 mb-1 block">Mô tả</label>
                    <input v-model="hinhAnhForm.moTa" type="text" placeholder="Mô tả hình..."
                      class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400" />
                  </div>
                </div>
                <div class="flex gap-2 mt-3">
                  <button @click="showAddHinhAnhForm = false" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg hover:bg-gray-50">Hủy</button>
                  <button @click="handleSaveHinhAnh" :disabled="savingHinhAnh"
                    class="px-3 py-1.5 text-sm bg-rose-500 hover:bg-rose-600 text-white rounded-lg disabled:opacity-60">
                    {{ savingHinhAnh ? 'Đang lưu...' : 'Thêm' }}
                  </button>
                </div>
              </div>

              <button v-if="!showAddHinhAnhForm" @click="openAddHinhAnhForm"
                class="flex items-center gap-1.5 text-sm text-rose-600 hover:text-rose-700 font-medium mt-2">
                <Plus :size="14" /> Thêm hình ảnh
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
