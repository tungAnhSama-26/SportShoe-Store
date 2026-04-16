<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  Search, Plus, Pencil, Trash2, Eye, ChevronDown, Check,
  X, ChevronLeft, ChevronRight, Layers, Images, ImageOff
} from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'

// ─── State ───────────────────────────────────────────────────────────────────
const items = ref([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)
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

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(0, currentPage.value - 2)
  const end = Math.min(totalPages.value - 1, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})
const pageSizeOptions = [10, 20, 50]

function formatCount(v) {
  return Number(v || 0).toLocaleString('vi-VN')
}

function handlePageSizeChange() {
  loadData(0)
}

// ─── Giới tính helper ─────────────────────────────────────────────────────────
function gioiTinhLabel(v) {
  return v === 1 ? 'Nam' : v === 2 ? 'Nữ' : v === 3 ? 'Unisex' : 'Tất cả'
}

// ─── Trạng thái badge ─────────────────────────────────────────────────────────
function trangThaiClass(v) {
  return v === 1
    ? 'bg-green-100 text-green-700'
    : 'bg-red-100 text-red-700'
}
function trangThaiLabel(v) { 
  if (v === 1) return 'Đang bán'
  if (v === 2) return 'Hết hàng'
  return 'Ngưng bán'
}

// ─── Dropdown helpers ─────────────────────────────────────────────────────────
function toggleDropdown(name) { openDropdown.value = openDropdown.value === name ? null : name }
function closeAll() { openDropdown.value = null }

function thuongHieuName(id) {
  if (!id || !danhMuc.value) return 'Thương hiệu'
  return danhMuc.value.thuongHieu.find(t => t.id === id)?.ten || 'Thương hiệu'
}
function loaiGiayName(id) {
  if (!id || !danhMuc.value) return 'Loại giày'
  return danhMuc.value.loaiGiay.find(l => l.id === id)?.ten || 'Loại giày'
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
async function openEdit(g) {
  try {
    const res = await api.chiTietGiay(g.id)
    clearForm()
    Object.assign(form, {
      ma: res.ma, ten: res.ten, thuongHieuId: res.thuongHieuId, loaiGiayId: res.loaiGiayId,
      gioiTinh: res.gioiTinh, chatLieu: res.chatLieu || '', moTa: res.moTa || '',
      deGiayId: res.thuocTinh?.deGiayId || null, coGiayId: res.thuocTinh?.coGiayId || null,
      congNgheDemId: res.thuocTinh?.congNgheDemId || null, trongLuongId: res.thuocTinh?.trongLuongId || null
    })
    selectedGiay.value = res
    modalMode.value = 'edit'
    showModal.value = true
  } catch (e) { showToast(e.message || 'Lỗi tải chi tiết', 'error') }
}
function isItemDiscounted(item) {
  return item?.coGiamGia === true
}
const activeHinhAnh = ref(null)

async function openView(g) {
  try {
    const detail = await api.chiTietGiay(g.id)
    clearForm()
    Object.assign(form, {
      ma: detail.ma, ten: detail.ten, thuongHieuId: detail.thuongHieuId, loaiGiayId: detail.loaiGiayId,
      gioiTinh: detail.gioiTinh, chatLieu: detail.chatLieu || '', moTa: detail.moTa || '',
      deGiayId: detail.thuocTinh?.deGiayId || null, coGiayId: detail.thuocTinh?.coGiayId || null,
      congNgheDemId: detail.thuocTinh?.congNgheDemId || null, trongLuongId: detail.thuocTinh?.trongLuongId || null
    })
    selectedGiay.value = detail
    modalMode.value = 'view'
    showModal.value = true
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
  const isStopped = g.trangThai === 0
  const action = isStopped ? 'Kích hoạt lại' : 'Ngưng bán'
  if (!confirm(`${action} sản phẩm "${g.ten}"?`)) return
  try {
    await api.xoaGiay(g.id)
    showToast(`${action} thành công`)
    loadData(currentPage.value)
  } catch (e) { showToast(e.message || `Lỗi ${action.toLowerCase()}`, 'error') }
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
    <div class="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-[30px] font-bold tracking-tight text-slate-800">{{ pageTitle }}</h1>
      </div>
      <button v-if="!isBienThePage" @click="openAdd" class="flex items-center gap-2 bg-rose-500 hover:bg-rose-600 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors">
        <Plus :size="16" /> Thêm sản phẩm
      </button>
    </div>

    <!-- Filters -->
    <section class="admin-section-card">
      <div class="admin-section-header">
        <div class="admin-section-icon admin-section-icon--slate">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
        </div>
      </div>

      <div class="flex flex-wrap gap-3 items-center">
        <div class="flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-slate-600">
          <span>Hiển thị</span>
          <select v-model.number="pageSize" @change="handlePageSizeChange"
            class="bg-transparent font-medium text-slate-700 outline-none">
            <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}/trang</option>
          </select>
        </div>

        <!-- Keyword -->
        <div class="relative flex-1 min-w-[200px]">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" :size="16" />
          <input v-model="keyword" @keyup.enter="doSearch" type="text" :placeholder="searchPlaceholder"
            class="w-full pl-9 pr-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-400" />
        </div>

        <!-- Thương hiệu dropdown -->
        <div class="relative" @click.stop>
          <button @click="toggleDropdown('thuongHieu')"
            class="flex items-center gap-2 px-3 py-2 border border-gray-200 rounded-lg text-sm hover:bg-gray-50 min-w-[140px] justify-between"
            :class="filterThuongHieu ? 'border-rose-400 text-rose-600' : 'text-gray-600'">
            <span>{{ thuongHieuName(filterThuongHieu) }}</span>
            <ChevronDown :size="14" />
          </button>
          <div v-if="openDropdown === 'thuongHieu'" class="absolute top-full left-0 mt-1 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-20 py-1 max-h-52 overflow-y-auto">
            <button @click="filterThuongHieu = null; openDropdown = null; doSearch()"
              class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 flex items-center justify-between"
              :class="filterThuongHieu === null ? 'text-rose-600 font-medium' : 'text-gray-700'">
              Tất cả <Check v-if="filterThuongHieu === null" :size="14" />
            </button>
            <button v-for="t in danhMuc?.thuongHieu" :key="t.id"
              @click="filterThuongHieu = t.id; openDropdown = null; doSearch()"
              class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 flex items-center justify-between"
              :class="filterThuongHieu === t.id ? 'text-rose-600 font-medium' : 'text-gray-700'">
              {{ t.ten }} <Check v-if="filterThuongHieu === t.id" :size="14" />
            </button>
          </div>
        </div>

        <!-- Loại giày dropdown -->
        <div class="relative" @click.stop>
          <button @click="toggleDropdown('loaiGiay')"
            class="flex items-center gap-2 px-3 py-2 border border-gray-200 rounded-lg text-sm hover:bg-gray-50 min-w-[130px] justify-between"
            :class="filterLoaiGiay ? 'border-rose-400 text-rose-600' : 'text-gray-600'">
            <span>{{ loaiGiayName(filterLoaiGiay) }}</span>
            <ChevronDown :size="14" />
          </button>
          <div v-if="openDropdown === 'loaiGiay'" class="absolute top-full left-0 mt-1 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-20 py-1 max-h-52 overflow-y-auto">
            <button @click="filterLoaiGiay = null; openDropdown = null; doSearch()"
              class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 flex items-center justify-between"
              :class="filterLoaiGiay === null ? 'text-rose-600 font-medium' : 'text-gray-700'">
              Tất cả <Check v-if="filterLoaiGiay === null" :size="14" />
            </button>
            <button v-for="l in danhMuc?.loaiGiay" :key="l.id"
              @click="filterLoaiGiay = l.id; openDropdown = null; doSearch()"
              class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 flex items-center justify-between"
              :class="filterLoaiGiay === l.id ? 'text-rose-600 font-medium' : 'text-gray-700'">
              {{ l.ten }} <Check v-if="filterLoaiGiay === l.id" :size="14" />
            </button>
          </div>
        </div>

        <!-- Trạng thái dropdown -->
        <div class="relative" @click.stop>
          <button @click="toggleDropdown('trangThai')"
            class="flex items-center gap-2 px-3 py-2 border border-gray-200 rounded-lg text-sm hover:bg-gray-50 min-w-[130px] justify-between"
            :class="filterTrangThai !== null ? 'border-rose-400 text-rose-600' : 'text-gray-600'">
            <span>{{ filterTrangThai === 1 ? 'Đang bán' : filterTrangThai === 2 ? 'Hết hàng' : filterTrangThai === 0 ? 'Ngưng bán' : 'Trạng thái' }}</span>
            <ChevronDown :size="14" />
          </button>
          <div v-if="openDropdown === 'trangThai'" class="absolute top-full left-0 mt-1 w-44 bg-white border border-gray-200 rounded-lg shadow-lg z-20 py-1">
            <button @click="filterTrangThai = null; openDropdown = null; doSearch()"
              class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 flex items-center justify-between"
              :class="filterTrangThai === null ? 'text-rose-600 font-medium' : 'text-gray-700'">
              Tất cả <Check v-if="filterTrangThai === null" :size="14" />
            </button>
            <button @click="filterTrangThai = 1; openDropdown = null; doSearch()"
              class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 flex items-center justify-between"
              :class="filterTrangThai === 1 ? 'text-rose-600 font-medium' : 'text-gray-700'">
              Đang bán <Check v-if="filterTrangThai === 1" :size="14" />
            </button>
            <button @click="filterTrangThai = 2; openDropdown = null; doSearch()"
              class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 flex items-center justify-between"
              :class="filterTrangThai === 2 ? 'text-rose-600 font-medium' : 'text-gray-700'">
              Hết hàng <Check v-if="filterTrangThai === 2" :size="14" />
            </button>
            <button @click="filterTrangThai = 0; openDropdown = null; doSearch()"
              class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 flex items-center justify-between"
              :class="filterTrangThai === 0 ? 'text-rose-600 font-medium' : 'text-gray-700'">
              Ngưng bán <Check v-if="filterTrangThai === 0" :size="14" />
            </button>
          </div>
        </div>

        <button @click="doSearch" class="px-4 py-2 bg-rose-500 hover:bg-rose-600 text-white text-sm rounded-lg transition-colors">
          Tìm kiếm
        </button>
      </div>
    </section>

    <!-- Table -->
    <section class="admin-section-card">
      <div class="admin-section-header">
        <div class="admin-section-icon admin-section-icon--violet">
          <Layers3 class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">{{ isBienThePage ? 'Danh sách sản phẩm có biến thể' : 'Danh sách sản phẩm' }}</h2>
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
              <th class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-28">Trạng thái</th>
              <th class="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider w-32">Thao tác</th>
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
                  <div class="h-10 w-10 relative">
                    <img v-if="item.hinhAnh" :src="item.hinhAnh" alt="" 
                         class="h-10 w-10 object-cover rounded-lg border border-gray-100" 
                         @error="(e) => e.target.classList.add('hidden')" />
                    <div class="absolute inset-0 h-10 w-10 rounded-lg bg-gray-50 flex items-center justify-center border border-gray-100 -z-10">
                      <Image :size="16" class="text-gray-300" />
                    </div>
                  </div>
                </td>
                <td class="px-4 py-3">
                  <div class="font-medium text-gray-800">{{ item.ten }}</div>
                  <div class="text-xs text-gray-400">{{ item.ma }}</div>
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

      <!-- Pagination -->
      <div v-if="totalPages > 0" class="mt-5 flex items-center justify-end gap-1 text-sm text-slate-500">
        <div class="flex items-center gap-1">
          <button @click="goPage(currentPage - 1)" :disabled="currentPage === 0"
            class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200 disabled:opacity-40 disabled:cursor-not-allowed">
            <ChevronLeft :size="16" />
          </button>
          <button v-for="p in visiblePages" :key="p" @click="goPage(p)"
            class="flex h-8 min-w-8 items-center justify-center rounded-lg px-2 text-xs font-medium transition"
            :class="p === currentPage ? 'bg-rose-500 text-white' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'">
            {{ p + 1 }}
          </button>
          <button @click="goPage(currentPage + 1)" :disabled="currentPage >= totalPages - 1"
            class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200 disabled:opacity-40 disabled:cursor-not-allowed">
            <ChevronRight :size="16" />
          </button>
        </div>
      </div>
    </section>

    <!-- ─── Giày Modal ─────────────────────────────────────────────────────── -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showModal = false">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col">
          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <div class="flex items-center gap-3">
              <div class="p-2 bg-rose-50 rounded-lg text-rose-600">
                <Box :size="20" />
              </div>
              <div>
                <h2 class="text-lg font-bold text-gray-800">{{ modalMode === 'view' ? 'Chi tiết sản phẩm' : modalMode === 'add' ? 'Thêm sản phẩm' : 'Cập nhật sản phẩm' }}</h2>
                <p v-if="modalMode !== 'add'" class="text-xs text-gray-400">Mã: {{ form.ma || selectedGiay?.ma }}</p>
              </div>
            </div>
            <button @click="showModal = false" class="p-2 rounded-xl h-10 w-10 flex items-center justify-center hover:bg-gray-100 text-gray-400 transition-colors">
              <X :size="20" />
            </button>
          </div>

          <!-- Body (View Mode) -->
          <div v-if="modalMode === 'view' && selectedGiay" class="p-6 overflow-y-auto max-h-[calc(90vh-80px)] custom-scrollbar">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
              <!-- Left: Image Gallery -->
              <div class="space-y-4">
                <div class="aspect-square rounded-2xl bg-gray-50 border border-gray-100 overflow-hidden flex items-center justify-center group relative shadow-inner">
                  <img v-if="selectedGiay.hinhAnhs && selectedGiay.hinhAnhs.length > 0" 
                       :src="activeHinhAnh || selectedGiay.hinhAnhs[0].url" 
                       class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  <div v-else class="flex flex-col items-center gap-2 text-gray-300">
                    <Images :size="48" />
                    <span class="text-sm">Chưa có ảnh</span>
                  </div>
                  <div v-if="isItemDiscounted(selectedGiay)" class="absolute top-4 left-4">
                    <span class="px-2 py-1 bg-red-500 text-white text-[10px] font-bold rounded shadow-sm uppercase">Sale</span>
                  </div>
                  <div v-if="selectedGiay.hinhAnhs?.find(h => h.url === (activeHinhAnh || selectedGiay.hinhAnhs[0].url))?.laHinhChinh" 
                       class="absolute top-4 right-4 animate-in fade-in zoom-in duration-300">
                    <span class="px-2 py-1 bg-amber-400 text-amber-900 text-[10px] font-bold rounded shadow-sm flex items-center gap-1">
                      <Zap :size="10" fill="currentColor" />
                    </span>
                  </div>
                </div>

                <div v-if="selectedGiay.hinhAnhs && selectedGiay.hinhAnhs.length > 1" class="grid grid-cols-5 gap-2">
                  <div v-for="h in selectedGiay.hinhAnhs" :key="h.id" 
                       @click="activeHinhAnh = h.url"
                       class="aspect-square rounded-lg border-2 cursor-pointer overflow-hidden transition-all relative bg-gray-50"
                       :class="activeHinhAnh === h.url ? 'border-rose-500 ring-4 ring-rose-50' : 'border-gray-100 hover:border-rose-200'">
                    <img :src="h.url" class="active-img w-full h-full object-cover" 
                         @error="(e) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'flex' }" />
                    <div class="hidden absolute inset-0 items-center justify-center text-gray-300">
                      <ImageOff :size="12" />
                    </div>
                    <div v-if="h.laHinhChinh" class="absolute top-0.5 right-0.5 p-0.5 bg-amber-400 rounded-bl-md text-amber-900 shadow-sm">
                      <Zap :size="8" fill="currentColor" />
                    </div>
                  </div>
                </div>
              </div>

              <!-- Right: Info -->
              <div class="space-y-6">
                <div>
                  <h1 class="text-2xl font-bold text-gray-800 mb-2">{{ selectedGiay.ten }}</h1>
                  <div class="flex flex-wrap gap-2 mb-4">
                    <span class="px-2.5 py-1 bg-gray-100 text-gray-600 text-xs font-medium rounded-lg border border-gray-200">{{ selectedGiay.thuongHieu }}</span>
                    <span class="px-2.5 py-1 bg-gray-100 text-gray-600 text-xs font-medium rounded-lg border border-gray-200">{{ selectedGiay.loaiGiay }}</span>
                    <span class="px-2.5 py-1 bg-rose-50 text-rose-600 text-xs font-medium rounded-lg border border-rose-100">{{ selectedGiay.gioiTinh === 1 ? 'Nam' : selectedGiay.gioiTinh === 2 ? 'Nữ' : 'Unisex' }}</span>
                  </div>
                  <div class="bg-gray-50 p-4 rounded-xl border border-gray-100">
                    <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 flex items-center gap-1.5 line-clamp-1">
                      <Info :size="12" /> Mô tả sản phẩm
                    </p>
                    <p class="text-sm text-gray-600 leading-relaxed whitespace-pre-line">{{ selectedGiay.moTa || 'Không có mô tả cho sản phẩm này.' }}</p>
                  </div>
                </div>

                <!-- Attributes Grid -->
                <div class="grid grid-cols-2 gap-3">
                  <div class="p-3 bg-white border border-gray-100 rounded-xl shadow-sm">
                    <p class="text-[10px] font-bold text-gray-400 tracking-widest mb-1">Đế giày</p>
                    <p class="text-sm font-bold text-gray-700">{{ selectedGiay.thuocTinh?.deGiay || '—' }}</p>
                  </div>
                  <div class="p-3 bg-white border border-gray-100 rounded-xl shadow-sm">
                    <p class="text-[10px] font-bold text-gray-400 tracking-widest mb-1">Cổ giày</p>
                    <p class="text-sm font-bold text-gray-700">{{ selectedGiay.thuocTinh?.coGiay || '—' }}</p>
                  </div>
                  <div class="p-3 bg-white border border-gray-100 rounded-xl shadow-sm">
                    <p class="text-[10px] font-bold text-gray-400 tracking-widest mb-1">Công nghệ đệm</p>
                    <p class="text-sm font-bold text-gray-700">{{ selectedGiay.thuocTinh?.congNgheDem || '—' }}</p>
                  </div>
                  <div class="p-3 bg-white border border-gray-100 rounded-xl shadow-sm">
                    <p class="text-[10px] font-bold text-gray-400 tracking-widest mb-1">Trọng lượng</p>
                    <p class="text-sm font-bold text-gray-700">{{ selectedGiay.thuocTinh?.trongLuong || '—' }}</p>
                  </div>
                </div>

                <div class="p-4 bg-rose-600 rounded-2xl text-white shadow-lg space-y-3">
                  <div class="flex items-center justify-between">
                    <span class="text-xs text-rose-100 flex items-center gap-1.5"><Truck :size="12" /> Chất liệu</span>
                    <span class="text-xs font-bold">{{ selectedGiay.chatLieu || '—' }}</span>
                  </div>
                  <div class="flex items-center justify-between pt-2 border-t border-rose-500">
                    <span class="text-xs text-rose-100 flex items-center gap-1.5"><CheckCircle2 :size="12" /> Trạng thái bán</span>
                    <span class="px-2 py-0.5 rounded-full text-[10px] uppercase font-bold"
                          :class="selectedGiay.trangThai === 1 ? 'bg-white text-rose-600' : 'bg-rose-200 text-rose-800'">
                      {{ selectedGiay.trangThai === 1 ? 'Đang bán' : 'Ngưng bán' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Body (Add/Edit Mode) -->
          <div v-else class="overflow-y-auto flex-1 p-6 space-y-4 custom-scrollbar">
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
                      <td class="px-3 py-2.5">{{ bt.kichCo }}</td>
                      <td class="px-3 py-2.5 text-right">{{ bt.soLuong }}</td>
                      <td class="px-3 py-2.5 text-right">{{ formatPrice(bt.giaBan) }}đ</td>
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
