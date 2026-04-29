<script setup>
import { computed, reactive, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Search, Plus, Eye, ChevronDown, Check, Filter, Layers3,
  ImageOff, FileSpreadsheet, RotateCcw, Pencil
} from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'
import AdminQuickStatusAction from '../../../components/common/AdminQuickStatusAction.vue'
import AdminTableFooter from '../../../components/common/AdminTableFooter.vue'
import QuanLySanPhamProductModal from '../../../components/admin/san-pham/QuanLySanPhamProductModal.vue'
import QuanLySanPhamBienTheModal from '../../../components/admin/san-pham/QuanLySanPhamBienTheModal.vue'
import QuanLySanPhamBienTheFormModal from '../../../components/admin/san-pham/QuanLySanPhamBienTheFormModal.vue'
import QuanLySanPhamHinhAnhModal from '../../../components/admin/san-pham/QuanLySanPhamHinhAnhModal.vue'
import { exportRowsToExcel } from '../../../utils/export-excel'
import { getDisplayErrorMessage, getFieldErrors } from '../../../utils/error-message'

const route = useRoute()
const router = useRouter()

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

const pageTitle = computed(() => 'Quản lý sản phẩm')
const pageDescription = computed(() =>
  'Theo dõi sản phẩm, thuộc tính kỹ thuật và quản lý CTSP ở popup biến thể.'
)
const searchPlaceholder = computed(() => 'Tìm theo mã, tên sản phẩm...')
const emptyMessage = computed(() => 'Không có sản phẩm nào')

const toast = reactive({ show: false, message: '', type: 'success' })
function showToast(message, type = 'success') {
  toast.message = message
  toast.type = type
  toast.show = true
  setTimeout(() => { toast.show = false }, 3000)
}

function toPositiveNumber(value) {
  const normalized = Array.isArray(value) ? value[0] : value
  const parsed = Number(normalized)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
}

function formatCount(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function normalizeNullableNumber(value) {
  return value == null || value === '' ? null : Number(value)
}

function gioiTinhLabel(value) {
  return value === 1 ? 'Nam' : value === 2 ? 'Nữ' : value === 3 ? 'Unisex' : 'Tất cả'
}

function trangThaiClass(value) {
  if (value === 1) return 'bg-emerald-50 text-emerald-600'
  if (value === 2) return 'bg-amber-50 text-amber-600'
  return 'bg-rose-50 text-rose-500'
}

function trangThaiLabel(value) {
  if (value === 1) return 'Đang bán'
  if (value === 2) return 'Hết hàng'
  return 'Ngừng bán'
}

function bienTheTrangThaiClass(item) {
  if (Number(item.soLuong || 0) <= 0) return 'bg-amber-50 text-amber-600'
  return Number(item.kichHoat) === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-600'
}

function bienTheTrangThaiLabel(item) {
  if (Number(item.soLuong || 0) <= 0) return 'Hết hàng'
  return Number(item.kichHoat) === 1 ? 'Đang bán' : 'Ngừng bán'
}

function productAttributeList(item) {
  return [
    item.chatLieu ? `Chất liệu: ${item.chatLieu}` : null,
    item.deGiay ? `Đế: ${item.deGiay}` : null,
    item.coGiay ? `Cổ: ${item.coGiay}` : null,
    item.congNgheDem ? `Đệm: ${item.congNgheDem}` : null,
    item.trongLuong ? `Trọng lượng: ${item.trongLuong}` : null
  ].filter(Boolean)
}

function selectedAttributeList(item) {
  if (!item) return []
  return [
    item.chatLieu ? `Chất liệu: ${item.chatLieu}` : null,
    item.thuocTinh?.deGiay ? `Đế: ${item.thuocTinh.deGiay}` : null,
    item.thuocTinh?.coGiay ? `Cổ: ${item.thuocTinh.coGiay}` : null,
    item.thuocTinh?.congNgheDem ? `Đệm: ${item.thuocTinh.congNgheDem}` : null,
    item.thuocTinh?.trongLuong ? `Trọng lượng: ${item.thuocTinh.trongLuong}` : null
  ].filter(Boolean)
}

function giaHienThi(item) {
  if (item.giaMin == null && item.giaMax == null) return 'Chưa có giá'
  if (item.giaMin === item.giaMax || item.giaMax == null) return `${formatCurrency(item.giaMin)}đ`
  return `${formatCurrency(item.giaMin)}đ - ${formatCurrency(item.giaMax)}đ`
}

function thongTinTonKho(item) {
  return `${formatCount(item.tongBienThe)} biến thể · ${formatCount(item.tongSoLuong)} tồn`
}

function nextBienTheStatus(item) {
  return Number(item.kichHoat) === 1 ? 2 : 1
}

function nextProductStatus(item) {
  return Number(item.trangThai) === 0 ? 1 : 0
}

function productQuickToggleLabel(item) {
  return Number(item.trangThai) === 0 ? 'Chuyển sang đang bán' : 'Chuyển sang ngừng bán'
}

function productQuickToggleIntent(item) {
  return Number(item.trangThai) === 0 ? 'activate' : 'deactivate'
}

function productQuickToggleConfirmMessage(item) {
  const action = Number(item.trangThai) === 0 ? 'đang bán' : 'ngừng bán'
  return `Bạn có muốn chuyển sản phẩm "${item.ten}" sang ${action} không?`
}

function canToggleBienTheStatus(item) {
  return Number(item.kichHoat) === 1 || Number(item.soLuong || 0) > 0
}

function closeAll() {
  openDropdown.value = null
}

function toggleDropdown(name) {
  openDropdown.value = openDropdown.value === name ? null : name
}

function thuongHieuName(id) {
  if (!id || !danhMuc.value) return 'Tất cả'
  return danhMuc.value.thuongHieu.find((item) => item.id === id)?.ten || 'Tất cả'
}

function loaiGiayName(id) {
  if (!id || !danhMuc.value) return 'Tất cả'
  return danhMuc.value.loaiGiay.find((item) => item.id === id)?.ten || 'Tất cả'
}

async function loadDanhMuc() {
  try {
    danhMuc.value = await api.layDanhMuc()
  } catch {}
}

async function loadData(page = 0) {
  loading.value = true
  try {
    const response = await api.layDanhSachGiay({
      keyword: keyword.value || undefined,
      thuongHieuId: filterThuongHieu.value,
      loaiGiayId: filterLoaiGiay.value,
      trangThai: filterTrangThai.value,
      page,
      size: pageSize.value
    })
    items.value = response.items
    totalItems.value = response.totalItems
    totalPages.value = response.totalPages
    currentPage.value = response.page
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không tải được danh sách sản phẩm'), 'error')
  } finally {
    loading.value = false
  }
}

function doSearch() {
  loadData(0)
}

function goPage(page) {
  if (page >= 0 && page < totalPages.value) {
    loadData(page)
  }
}

const pageSizeOptions = [5, 10, 20, 50]

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
    const response = await api.layDanhSachGiay({
      keyword: keyword.value || undefined,
      thuongHieuId: filterThuongHieu.value,
      loaiGiayId: filterLoaiGiay.value,
      trangThai: filterTrangThai.value,
      page: 0,
      size: Math.max(totalItems.value, pageSize.value)
    })

    const exported = exportRowsToExcel({
      filename: 'quan-ly-san-pham',
      sheetName: 'SanPham',
      columns: [
        { label: 'STT', value: (_, index) => index + 1 },
        { label: 'Mã sản phẩm', key: 'ma' },
        { label: 'Tên sản phẩm', key: 'ten' },
        { label: 'Thương hiệu', value: (row) => row.thuongHieu || '—' },
        { label: 'Loại giày', value: (row) => row.loaiGiay || '—' },
        { label: 'Chất liệu', value: (row) => row.chatLieu || '—' },
        { label: 'Đế giày', value: (row) => row.deGiay || '—' },
        { label: 'Cổ giày', value: (row) => row.coGiay || '—' },
        { label: 'Công nghệ đệm', value: (row) => row.congNgheDem || '—' },
        { label: 'Trọng lượng', value: (row) => row.trongLuong || '—' },
        { label: 'Giới tính', value: (row) => gioiTinhLabel(row.gioiTinh) },
        { label: 'Giá', value: (row) => giaHienThi(row) },
        { label: 'Tổng biến thể', value: (row) => row.tongBienThe ?? 0 },
        { label: 'Tổng số lượng', value: (row) => row.tongSoLuong ?? 0 },
        { label: 'Trạng thái', value: (row) => trangThaiLabel(row.trangThai) }
      ],
      rows: response.items || []
    })

    showToast(
      exported ? 'Xuất Excel thành công' : 'Không có dữ liệu để xuất Excel',
      exported ? 'success' : 'error'
    )
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không thể xuất Excel sản phẩm'), 'error')
  }
}

const showProductModal = ref(false)
const productModalMode = ref('add')
const productSaving = ref(false)
const loadingProductDetail = ref(false)
const selectedProductForModal = ref(null)
const productForm = reactive({
  ma: '',
  ten: '',
  thuongHieuId: null,
  loaiGiayId: null,
  gioiTinh: null,
  chatLieuGiayId: null,
  moTa: '',
  deGiayId: null,
  coGiayId: null,
  congNgheDemId: null,
  trongLuongId: null
})
const productErrors = reactive({})
const updatingProductStatusId = ref(null)

const productModalTitle = computed(() =>
  productModalMode.value === 'add' ? 'Thêm sản phẩm' : 'Cập nhật sản phẩm'
)
const productModalDescription = computed(() =>
  productModalMode.value === 'add'
    ? 'Tạo thông tin nền của sản phẩm. CTSP sẽ được quản lý ở popup biến thể.'
    : 'Cập nhật thông tin nền của sản phẩm. CTSP và ảnh vẫn quản lý ở popup biến thể.'
)
const productModalMainImage = computed(() => {
  if (!selectedProductForModal.value?.hinhAnhs?.length) return null
  return selectedProductForModal.value.hinhAnhs.find((item) => item.laHinhChinh) || selectedProductForModal.value.hinhAnhs[0]
})

function resetProductErrors() {
  Object.keys(productErrors).forEach((key) => delete productErrors[key])
}

function clearProductForm() {
  Object.assign(productForm, {
    ma: '',
    ten: '',
    thuongHieuId: null,
    loaiGiayId: null,
    gioiTinh: null,
    chatLieuGiayId: null,
    moTa: '',
    deGiayId: null,
    coGiayId: null,
    congNgheDemId: null,
    trongLuongId: null
  })
  resetProductErrors()
}

function findChatLieuGiayIdByName(name) {
  if (!name || !danhMuc.value?.chatLieuGiay?.length) return null
  const normalized = String(name).trim().toLowerCase()
  return danhMuc.value.chatLieuGiay.find((item) => item.ten?.trim().toLowerCase() === normalized)?.id || null
}

function hydrateProductForm(detail) {
  Object.assign(productForm, {
    ma: detail.ma || '',
    ten: detail.ten || '',
    thuongHieuId: detail.thuongHieuId || null,
    loaiGiayId: detail.loaiGiayId || null,
    gioiTinh: detail.gioiTinh ?? null,
    chatLieuGiayId: detail.thuocTinh?.chatLieuGiayId || findChatLieuGiayIdByName(detail.chatLieu),
    moTa: detail.moTa || '',
    deGiayId: detail.thuocTinh?.deGiayId || null,
    coGiayId: detail.thuocTinh?.coGiayId || null,
    congNgheDemId: detail.thuocTinh?.congNgheDemId || null,
    trongLuongId: detail.thuocTinh?.trongLuongId || null
  })
}

function mauSacLabel(id) {
  return danhMuc.value?.mauSac?.find((item) => item.id === Number(id))?.ten || `Màu #${id}`
}

function kichCoLabel(id) {
  return danhMuc.value?.kichCo?.find((item) => item.id === Number(id))?.giaTri || `Size #${id}`
}

function buildCreateProductPayload() {
  return {
    ten: productForm.ten.trim(),
    thuongHieuId: Number(productForm.thuongHieuId),
    loaiGiayId: Number(productForm.loaiGiayId),
    gioiTinh: normalizeNullableNumber(productForm.gioiTinh),
    chatLieuGiayId: normalizeNullableNumber(productForm.chatLieuGiayId),
    moTa: productForm.moTa.trim() || undefined,
    deGiayId: normalizeNullableNumber(productForm.deGiayId),
    coGiayId: normalizeNullableNumber(productForm.coGiayId),
    congNgheDemId: normalizeNullableNumber(productForm.congNgheDemId),
    trongLuongId: normalizeNullableNumber(productForm.trongLuongId)
  }
}

function validateProductForm() {
  resetProductErrors()
  if (!productForm.ten.trim()) productErrors.ten = 'Vui lòng nhập tên sản phẩm'
  if (!productForm.thuongHieuId) productErrors.thuongHieuId = 'Vui lòng chọn thương hiệu cho sản phẩm'
  if (!productForm.loaiGiayId) productErrors.loaiGiayId = 'Vui lòng chọn loại giày cho sản phẩm'
  return Object.keys(productErrors).length === 0
}

function buildUpdateProductPayload() {
  return {
    ten: productForm.ten.trim(),
    thuongHieuId: Number(productForm.thuongHieuId),
    loaiGiayId: Number(productForm.loaiGiayId),
    gioiTinh: normalizeNullableNumber(productForm.gioiTinh),
    chatLieuGiayId: normalizeNullableNumber(productForm.chatLieuGiayId),
    moTa: productForm.moTa.trim() || undefined,
    deGiayId: normalizeNullableNumber(productForm.deGiayId),
    coGiayId: normalizeNullableNumber(productForm.coGiayId),
    congNgheDemId: normalizeNullableNumber(productForm.congNgheDemId),
    trongLuongId: normalizeNullableNumber(productForm.trongLuongId)
  }
}

function closeProductModal() {
  showProductModal.value = false
  loadingProductDetail.value = false
  selectedProductForModal.value = null
  clearProductForm()
}

function openAdd() {
  clearProductForm()
  selectedProductForModal.value = null
  productModalMode.value = 'add'
  showProductModal.value = true
}

async function openEdit(item) {
  clearProductForm()
  productModalMode.value = 'edit'
  showProductModal.value = true
  loadingProductDetail.value = true
  try {
    const detail = await api.chiTietGiay(item.id)
    selectedProductForModal.value = detail
    hydrateProductForm(detail)
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không tải được chi tiết sản phẩm'), 'error')
    closeProductModal()
  } finally {
    loadingProductDetail.value = false
  }
}

async function handleSaveProduct() {
  if (!validateProductForm()) return

  productSaving.value = true
  try {
    if (productModalMode.value === 'add') {
      const created = await api.taoGiay(buildCreateProductPayload())
      showToast('Tạo sản phẩm thành công')
      await loadData(0)
      closeProductModal()
      await goToVariants(created, { created: true, themBienThe: true })
    } else {
      const updated = await api.capNhatGiay(
        selectedProductForModal.value.id,
        buildUpdateProductPayload()
      )
      selectedProductForModal.value = updated
      showToast('Cập nhật sản phẩm thành công')
      await loadData(currentPage.value)
      if (showBienTheModal.value && selectedGiay.value?.id === updated.id) {
        await syncSelectedGiayContext(updated.id)
      }
      closeProductModal()
    }
  } catch (error) {
    const fieldErrors = getFieldErrors(error)
    Object.assign(productErrors, fieldErrors)
    if (!Object.keys(fieldErrors).length) {
      showToast(getDisplayErrorMessage(error, 'Không thể lưu sản phẩm'), 'error')
    }
  } finally {
    productSaving.value = false
  }
}

async function handleToggleProductStatus(item) {
  updatingProductStatusId.value = item.id
  try {
    await api.doiTrangThai(item.id, nextProductStatus(item))
    showToast('Cập nhật trạng thái sản phẩm thành công')
    await Promise.all([
      loadData(currentPage.value),
      showBienTheModal.value && selectedGiay.value?.id === item.id
        ? syncSelectedGiayContext(item.id)
        : Promise.resolve()
    ])
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không thể cập nhật trạng thái sản phẩm'), 'error')
  } finally {
    updatingProductStatusId.value = null
  }
}

async function goToVariants(item, options = {}) {
  showBienTheModal.value = true
  await syncSelectedGiayContext(item.id)
  if (!selectedGiay.value) {
    showBienTheModal.value = false
    return
  }

  if (options.created) {
    showToast('Tạo sản phẩm thành công. Bạn có thể quản lý thêm CTSP và ảnh ngay tại đây.')
  }
  const shouldOpenCreateForm = options.themBienThe || Number(item.tongBienThe ?? bienTheList.value.length ?? 0) === 0
  if (shouldOpenCreateForm) {
    openAddBienTheForm()
  }
}

const showBienTheModal = ref(false)
const selectedGiay = ref(null)
const bienTheList = ref([])
const loadingBienThe = ref(false)
const showAddBienTheForm = ref(false)
const savingBienThe = ref(false)
const editingBienThe = ref(null)

const bienTheForm = reactive({
  mauSacId: null,
  kichCoId: null,
  soLuong: 0,
  giaGoc: 0,
  giaBan: 0,
  kichHoat: 1
})
const bienTheErrors = reactive({})
const updatingBienTheStatusId = ref(null)
const bulkBienTheForm = reactive({
  mauSacIds: [],
  kichCoIds: [],
  soLuong: 0,
  giaGoc: 0,
  giaBan: 0
})
const bulkBienTheErrors = reactive({})
const generatedBulkBienThes = ref([])

function clearBulkBienTheBuilder() {
  Object.assign(bulkBienTheForm, {
    mauSacIds: [],
    kichCoIds: [],
    soLuong: 0,
    giaGoc: 0,
    giaBan: 0
  })
  generatedBulkBienThes.value = []
  Object.keys(bulkBienTheErrors).forEach((key) => delete bulkBienTheErrors[key])
}

function clearBienTheForm() {
  editingBienThe.value = null
  Object.assign(bienTheForm, {
    mauSacId: null,
    kichCoId: null,
    soLuong: 0,
    giaGoc: 0,
    giaBan: 0,
    kichHoat: 1
  })
  Object.keys(bienTheErrors).forEach((key) => delete bienTheErrors[key])
  clearBulkBienTheBuilder()
}

function clearSelectedGiayContext() {
  selectedGiay.value = null
  bienTheList.value = []
  loadingBienThe.value = false
  showAddBienTheForm.value = false
  editingBienThe.value = null
  clearBienTheForm()
}

async function syncSelectedGiayContext(giayId = selectedGiay.value?.id) {
  const targetId = toPositiveNumber(giayId)
  if (!targetId) {
    clearSelectedGiayContext()
    return
  }

  loadingBienThe.value = true
  try {
    const [detail, variants] = await Promise.all([
      api.chiTietGiay(targetId),
      api.layBienThe(targetId)
    ])
    selectedGiay.value = detail
    bienTheList.value = variants
  } catch (error) {
    selectedGiay.value = null
    bienTheList.value = []
    showToast(getDisplayErrorMessage(error, 'Không tải được danh sách biến thể sản phẩm'), 'error')
  } finally {
    loadingBienThe.value = false
  }
}

function clearSelectedProduct() {
  closeBienTheModal()
}

function closeBienTheModal() {
  showBienTheModal.value = false
  closeBienTheForm()
  if (showHinhAnhModal.value) {
    closeHinhAnhModal()
  }
  clearSelectedGiayContext()
}

function openAddBienTheForm() {
  clearBienTheForm()
  showAddBienTheForm.value = true
}

function openEditBienThe(item) {
  editingBienThe.value = item
  Object.assign(bienTheForm, {
    mauSacId: item.mauSacId,
    kichCoId: item.kichCoId,
    soLuong: item.soLuong,
    giaGoc: item.giaGoc,
    giaBan: item.giaBan,
    kichHoat: Number(item.kichHoat) === 1 ? 1 : 2
  })
  Object.keys(bienTheErrors).forEach((key) => delete bienTheErrors[key])
  showAddBienTheForm.value = true
}

function closeBienTheForm() {
  showAddBienTheForm.value = false
  clearBienTheForm()
}

function validateBulkBienTheBuilder() {
  Object.keys(bulkBienTheErrors).forEach((key) => delete bulkBienTheErrors[key])
  if (!bulkBienTheForm.mauSacIds.length) bulkBienTheErrors.mauSacIds = 'Vui lòng chọn ít nhất một màu sắc để tạo CTSP'
  if (!bulkBienTheForm.kichCoIds.length) bulkBienTheErrors.kichCoIds = 'Vui lòng chọn ít nhất một kích cỡ để tạo CTSP'
  if (Number(bulkBienTheForm.giaGoc) < 0) bulkBienTheErrors.giaGoc = 'Giá gốc của CTSP không được âm'
  if (Number(bulkBienTheForm.soLuong) < 0) bulkBienTheErrors.soLuong = 'Số lượng của CTSP không được âm'
  if (Number(bulkBienTheForm.giaBan) < 0) bulkBienTheErrors.giaBan = 'Giá bán của CTSP không được âm'
  return Object.keys(bulkBienTheErrors).length === 0
}

function generateBulkBienThes() {
  if (!validateBulkBienTheBuilder()) return

  const existingMap = new Map(
    generatedBulkBienThes.value.map((item) => [`${item.mauSacId}-${item.kichCoId}`, item])
  )

  generatedBulkBienThes.value = bulkBienTheForm.mauSacIds.flatMap((mauSacId) =>
    bulkBienTheForm.kichCoIds.map((kichCoId) => {
      const key = `${mauSacId}-${kichCoId}`
      return existingMap.get(key) || {
        key,
        mauSacId: Number(mauSacId),
        mauSac: mauSacLabel(mauSacId),
        kichCoId: Number(kichCoId),
        kichCo: kichCoLabel(kichCoId),
        soLuong: Number(bulkBienTheForm.soLuong),
        giaGoc: Number(bulkBienTheForm.giaGoc),
        giaBan: Number(bulkBienTheForm.giaBan)
      }
    })
  )

  delete bulkBienTheErrors.generated
  return showToast(`Đã tạo thành công ${generatedBulkBienThes.value.length} chi tiết sản phẩm`)
}

function removeGeneratedBulkBienThe(key) {
  generatedBulkBienThes.value = generatedBulkBienThes.value.filter((item) => item.key !== key)
}

function validateGeneratedBulkBienThes() {
  delete bulkBienTheErrors.generated

  if (!generatedBulkBienThes.value.length) {
    bulkBienTheErrors.generated = 'Bạn chưa tạo danh sách chi tiết sản phẩm tự động'
    return false
  }

  const hasInvalidRow = generatedBulkBienThes.value.some((item) =>
    Number(item.soLuong) < 0 || Number(item.giaGoc) < 0 || Number(item.giaBan) <= 0
  )

  if (hasInvalidRow) {
    bulkBienTheErrors.generated = 'Vui lòng kiểm tra lại số lượng tồn, giá gốc và giá bán của từng chi tiết sản phẩm'
    return false
  }

  return true
}

async function handleSaveBienThe() {
  Object.keys(bienTheErrors).forEach((key) => delete bienTheErrors[key])
  if (!selectedGiay.value) return

  savingBienThe.value = true
  try {
    if (editingBienThe.value) {
      if (Number(bienTheForm.giaBan) <= 0) bienTheErrors.giaBan = 'Giá bán của chi tiết sản phẩm phải lớn hơn 0'
      if (Number(bienTheForm.giaGoc) < 0) bienTheErrors.giaGoc = 'Giá gốc của chi tiết sản phẩm không được âm'
      if (Number(bienTheForm.soLuong) < 0) bienTheErrors.soLuong = 'Số lượng tồn của chi tiết sản phẩm không được âm'
      if (Object.keys(bienTheErrors).length > 0) return

      await api.capNhatBienThe(editingBienThe.value.id, {
        soLuong: Number(bienTheForm.soLuong),
        giaGoc: Number(bienTheForm.giaGoc),
        giaBan: Number(bienTheForm.giaBan),
        kichHoat: Number(bienTheForm.kichHoat)
      })
    } else {
      if (!validateGeneratedBulkBienThes()) return

      await api.taoChiTietSanPhamHangLoat({
        giayId: selectedGiay.value.id,
        bienThes: generatedBulkBienThes.value.map((item) => ({
          mauSacId: Number(item.mauSacId),
          kichCoId: Number(item.kichCoId),
          soLuong: Number(item.soLuong),
          giaGoc: Number(item.giaGoc),
          giaBan: Number(item.giaBan)
        }))
      })
    }
    showToast('Lưu CTSP thành công')
    closeBienTheForm()
    await Promise.all([
      syncSelectedGiayContext(selectedGiay.value?.id),
      loadData(currentPage.value)
    ])
  } catch (error) {
    const fieldErrors = getFieldErrors(error)
    Object.assign(bienTheErrors, fieldErrors)
    Object.assign(bulkBienTheErrors, fieldErrors)
    if (fieldErrors.bienThes && !bulkBienTheErrors.generated) {
      bulkBienTheErrors.generated = fieldErrors.bienThes
    }
    if (!Object.keys(fieldErrors).length) {
      showToast(getDisplayErrorMessage(error, 'Không thể lưu chi tiết sản phẩm'), 'error')
    }
  } finally {
    savingBienThe.value = false
  }
}

async function handleToggleBienTheStatus(item) {
  if (!canToggleBienTheStatus(item)) {
    showToast('Không thể chuyển CTSP sang đang bán khi số lượng tồn bằng 0', 'error')
    return
  }

  updatingBienTheStatusId.value = item.id
  try {
    await api.doiTrangThaiBienThe(item.id, nextBienTheStatus(item))
    showToast('Cập nhật trạng thái CTSP thành công')
    await Promise.all([
      syncSelectedGiayContext(selectedGiay.value?.id),
      loadData(currentPage.value)
    ])
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không thể cập nhật trạng thái chi tiết sản phẩm'), 'error')
  } finally {
    updatingBienTheStatusId.value = null
  }
}

async function handleDeleteBienThe(id) {
  if (!confirm('Xóa biến thể này?')) return
  try {
    await api.xoaBienThe(id)
    showToast('Xóa biến thể thành công')
    await Promise.all([
      syncSelectedGiayContext(selectedGiay.value?.id),
      loadData(currentPage.value)
    ])
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không thể xóa biến thể sản phẩm'), 'error')
  }
}

const showHinhAnhModal = ref(false)
const hinhAnhBienThe = ref(null)

function closeHinhAnhModal() {
  showHinhAnhModal.value = false
  hinhAnhBienThe.value = null
}

async function openHinhAnh(item) {
  if (!item?.id) return
  hinhAnhBienThe.value = item
  showHinhAnhModal.value = true
}

async function handleHinhAnhUpdated() {
  await Promise.all([
    syncSelectedGiayContext(selectedGiay.value?.id),
    loadData(currentPage.value)
  ])
}

function handleHinhAnhError(message) {
  showToast(message || 'L?i x? l? h?nh ?nh', 'error')
}

const selectedGiayMainImage = computed(() => {
  if (!selectedGiay.value?.hinhAnhs?.length) return null
  return selectedGiay.value.hinhAnhs.find((item) => item.laHinhChinh) || selectedGiay.value.hinhAnhs[0]
})

watch(
  () => [route.name, route.query.giayId, route.query.created, route.query.themBienThe],
  async () => {
    if (route.name !== 'admin-bien-the-san-pham') return

    const legacyId = toPositiveNumber(route.query.giayId)
    const options = {
      created: route.query.created === '1',
      themBienThe: route.query.themBienThe === '1'
    }

    await router.replace({ name: 'admin-san-pham' })
    if (legacyId) {
      await goToVariants({ id: legacyId }, options)
    }
  },
  { immediate: true }
)

onMounted(async () => {
  await Promise.all([
    loadDanhMuc(),
    loadData()
  ])
})
</script>

<template>
  <div class="space-y-5" @click="closeAll">
    <Transition name="fade">
      <div
        v-if="toast.show"
        class="fixed right-4 top-[88px] z-50 rounded-2xl px-4 py-3 text-sm font-medium text-white shadow-lg"
        :class="toast.type === 'success' ? 'bg-emerald-500' : 'bg-rose-500'"
      >
        {{ toast.message }}
      </div>
    </Transition>

    <section class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
      <div class="space-y-2">
        <div>
          <h1 class="text-[30px] font-bold tracking-tight text-slate-800">{{ pageTitle }}</h1>
          <p class="mt-1 text-sm text-slate-500">{{ pageDescription }}</p>
        </div>
      </div>

      <div class="flex flex-wrap gap-2.5">
        <button
          @click="openAdd"
          class="inline-flex h-10 items-center gap-2 rounded-xl bg-rose-500 px-4 text-sm font-semibold text-white shadow-sm shadow-rose-200 transition hover:bg-rose-600"
        >
          <Plus :size="15" />
          Thêm sản phẩm
        </button>
      </div>
    </section>

    <section class="admin-section-card !p-4">
      <div class="mb-4 flex items-center gap-3">
        <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-100 text-slate-600">
          <Filter class="h-4 w-4" />
        </div>
        <div>
          <h2 class="text-sm font-bold text-slate-800">Bộ lọc</h2>
          <p class="text-xs text-slate-400">Lọc nhanh theo thương hiệu, loại giày và trạng thái.</p>
        </div>
      </div>

      <div class="flex flex-col gap-3">
        <div class="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
          <div class="min-w-0 flex-1">
            <div class="relative max-w-[680px]">
              <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" :size="15" />
              <input
                v-model="keyword"
                @keyup.enter="doSearch"
                type="text"
                :placeholder="searchPlaceholder"
                class="h-10 w-full rounded-xl border border-slate-200 bg-slate-50 pl-10 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-2.5 xl:justify-end">
            <button
              @click="resetFilters"
              class="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-rose-200 bg-rose-50 px-4 text-sm font-semibold text-rose-600 transition hover:border-rose-300 hover:bg-rose-100 hover:text-rose-700"
            >
              <RotateCcw :size="15" />
              Đặt lại bộ lọc
            </button>
            <button
              @click="doSearch"
              class="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-rose-200 bg-white px-4 text-sm font-semibold text-rose-600 transition hover:border-rose-300 hover:bg-rose-50 hover:text-rose-700"
            >
              <Search :size="15" />
              Tìm kiếm
            </button>
            <button
              @click="xuatExcel"
              class="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-rose-200 bg-white px-4 text-sm font-semibold text-rose-600 transition hover:border-rose-300 hover:bg-rose-50 hover:text-rose-700"
            >
              <FileSpreadsheet :size="15" />
              Xuất Excel
            </button>
          </div>
        </div>

        <div class="flex flex-wrap items-start gap-3">
          <div class="relative w-full sm:w-[220px] md:w-[240px]" @click.stop>
            <label class="mb-1 block text-[10px] font-normal uppercase tracking-wide text-slate-500">Thương hiệu</label>
            <button
              @click="toggleDropdown('thuongHieu')"
              class="flex h-9 w-full items-center justify-between gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 text-[13px] transition hover:bg-white"
              :class="filterThuongHieu ? 'border-rose-300 text-rose-600' : 'text-slate-600'"
            >
              <span class="truncate">{{ thuongHieuName(filterThuongHieu) }}</span>
              <ChevronDown :size="14" />
            </button>
            <div
              v-if="openDropdown === 'thuongHieu'"
              class="absolute left-0 top-full z-20 mt-1 max-h-52 w-full min-w-[220px] overflow-y-auto rounded-2xl border border-gray-200 bg-white py-1 shadow-lg"
            >
              <button
                @click="filterThuongHieu = null; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterThuongHieu === null ? 'font-medium text-rose-600' : 'text-gray-700'"
              >
                Tất cả
                <Check v-if="filterThuongHieu === null" :size="14" />
              </button>
              <button
                v-for="item in danhMuc?.thuongHieu"
                :key="item.id"
                @click="filterThuongHieu = item.id; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterThuongHieu === item.id ? 'font-medium text-rose-600' : 'text-gray-700'"
              >
                {{ item.ten }}
                <Check v-if="filterThuongHieu === item.id" :size="14" />
              </button>
            </div>
          </div>

          <div class="relative w-full sm:w-[220px] md:w-[240px]" @click.stop>
            <label class="mb-1 block text-[10px] font-normal uppercase tracking-wide text-slate-500">Loại giày</label>
            <button
              @click="toggleDropdown('loaiGiay')"
              class="flex h-9 w-full items-center justify-between gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 text-[13px] transition hover:bg-white"
              :class="filterLoaiGiay ? 'border-rose-300 text-rose-600' : 'text-slate-600'"
            >
              <span class="truncate">{{ loaiGiayName(filterLoaiGiay) }}</span>
              <ChevronDown :size="14" />
            </button>
            <div
              v-if="openDropdown === 'loaiGiay'"
              class="absolute left-0 top-full z-20 mt-1 max-h-52 w-full min-w-[220px] overflow-y-auto rounded-2xl border border-gray-200 bg-white py-1 shadow-lg"
            >
              <button
                @click="filterLoaiGiay = null; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterLoaiGiay === null ? 'font-medium text-rose-600' : 'text-gray-700'"
              >
                Tất cả
                <Check v-if="filterLoaiGiay === null" :size="14" />
              </button>
              <button
                v-for="item in danhMuc?.loaiGiay"
                :key="item.id"
                @click="filterLoaiGiay = item.id; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterLoaiGiay === item.id ? 'font-medium text-rose-600' : 'text-gray-700'"
              >
                {{ item.ten }}
                <Check v-if="filterLoaiGiay === item.id" :size="14" />
              </button>
            </div>
          </div>

          <div class="relative w-full sm:w-[170px] md:w-[200px]" @click.stop>
            <label class="mb-1 block text-[10px] font-normal uppercase tracking-wide text-slate-500">Trạng thái</label>
            <button
              @click="toggleDropdown('trangThai')"
              class="flex h-9 w-full items-center justify-between gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 text-[13px] transition hover:bg-white"
              :class="filterTrangThai !== null ? 'border-rose-300 text-rose-600' : 'text-slate-600'"
            >
              <span class="truncate">
                {{
                  filterTrangThai === 1
                    ? 'Đang bán'
                    : filterTrangThai === 2
                      ? 'Hết hàng'
                      : filterTrangThai === 0
                        ? 'Ngừng bán'
                        : 'Tất cả'
                }}
              </span>
              <ChevronDown :size="14" />
            </button>
            <div
              v-if="openDropdown === 'trangThai'"
              class="absolute left-0 top-full z-20 mt-1 w-full min-w-[220px] rounded-2xl border border-gray-200 bg-white py-1 shadow-lg"
            >
              <button
                @click="filterTrangThai = null; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterTrangThai === null ? 'font-medium text-rose-600' : 'text-gray-700'"
              >
                Tất cả
                <Check v-if="filterTrangThai === null" :size="14" />
              </button>
              <button
                @click="filterTrangThai = 1; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterTrangThai === 1 ? 'font-medium text-rose-600' : 'text-gray-700'"
              >
                Đang bán
                <Check v-if="filterTrangThai === 1" :size="14" />
              </button>
              <button
                @click="filterTrangThai = 2; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterTrangThai === 2 ? 'font-medium text-rose-600' : 'text-gray-700'"
              >
                Hết hàng
                <Check v-if="filterTrangThai === 2" :size="14" />
              </button>
              <button
                @click="filterTrangThai = 0; openDropdown = null; doSearch()"
                class="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
                :class="filterTrangThai === 0 ? 'font-medium text-rose-600' : 'text-gray-700'"
              >
                Ngừng bán
                <Check v-if="filterTrangThai === 0" :size="14" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="admin-section-card">
      <div class="admin-section-header">
        <div class="admin-section-icon admin-section-icon--violet">
          <Layers3 class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Danh sách sản phẩm</h2>
          <p class="text-xs text-slate-400">Danh sách đã bổ sung thêm thuộc tính kỹ thuật và điểm vào quản lý CTSP.</p>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="admin-table admin-table--wide min-w-[1180px]">
          <thead class="border-b border-gray-100 bg-gray-50">
            <tr>
              <th class="w-12 px-4 py-3 text-left text-xs uppercase tracking-wider text-slate-950 font-bold">STT</th>
              <th class="w-16 px-4 py-3 text-left text-xs uppercase tracking-wider text-slate-950 font-bold">Ảnh</th>
              <th class="px-4 py-3 text-left text-xs uppercase tracking-wider text-slate-950 font-bold">Sản phẩm</th>
              <th class="px-4 py-3 text-left text-xs uppercase tracking-wider text-slate-950 font-bold">Thuộc tính</th>
              <th class="w-[220px] px-4 py-3 text-left text-xs uppercase tracking-wider text-slate-950 font-bold">Giá / tồn kho</th>
              <th class="w-[180px] px-4 py-3 text-left text-xs uppercase tracking-wider text-slate-950 font-bold">Trạng thái</th>
              <th class="w-[170px] px-4 py-3 text-right text-xs uppercase tracking-wider text-slate-950 font-bold">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <template v-if="loading">
              <tr v-for="index in 6" :key="index" class="animate-pulse">
                <td class="px-4 py-4"><div class="h-4 w-6 rounded bg-gray-200"></div></td>
                <td class="px-4 py-4"><div class="h-11 w-11 rounded-xl bg-gray-200"></div></td>
                <td class="px-4 py-4"><div class="h-4 w-44 rounded bg-gray-200"></div></td>
                <td class="px-4 py-4"><div class="h-4 w-52 rounded bg-gray-200"></div></td>
                <td class="px-4 py-4"><div class="h-4 w-32 rounded bg-gray-200"></div></td>
                <td class="px-4 py-4"><div class="h-7 w-28 rounded-full bg-gray-200"></div></td>
                <td class="px-4 py-4"><div class="ml-auto h-8 w-28 rounded bg-gray-200"></div></td>
              </tr>
            </template>

            <template v-else-if="items.length === 0">
              <tr>
                <td colspan="7" class="px-4 py-12 text-center text-gray-400">{{ emptyMessage }}</td>
              </tr>
            </template>

            <template v-else>
              <tr
                v-for="(item, index) in items"
                :key="item.id"
                class="transition-colors hover:bg-gray-50"
              >
                <td class="px-4 py-4 text-gray-500">{{ currentPage * pageSize + index + 1 }}</td>
                <td class="px-4 py-4">
                  <img
                    v-if="item.hinhAnh"
                    :src="item.hinhAnh"
                    alt=""
                    class="h-11 w-11 rounded-xl border border-gray-100 object-cover"
                  />
                  <div v-else class="flex h-11 w-11 items-center justify-center rounded-xl bg-gray-100">
                    <ImageOff :size="16" class="text-gray-400" />
                  </div>
                </td>
                <td class="px-4 py-4 align-top">
                  <div class="font-semibold text-slate-800">{{ item.ten }}</div>
                  <div class="mt-1 text-xs font-semibold uppercase tracking-wide text-slate-500">{{ item.ma }}</div>
                  <div class="mt-1 flex flex-wrap gap-1.5 text-xs text-slate-500">
                    <span class="rounded-full bg-slate-100 px-2 py-0.5">{{ item.thuongHieu }}</span>
                    <span class="rounded-full bg-slate-100 px-2 py-0.5">{{ item.loaiGiay }}</span>
                    <span class="rounded-full bg-slate-100 px-2 py-0.5">{{ gioiTinhLabel(item.gioiTinh) }}</span>
                    <span v-if="item.coGiamGia" class="rounded-full bg-emerald-50 px-2 py-0.5 text-emerald-600">Có giảm giá</span>
                  </div>
                </td>
                <td class="px-4 py-4 align-top">
                  <div class="flex flex-wrap gap-1.5">
                    <span
                      v-for="attribute in productAttributeList(item)"
                      :key="attribute"
                      class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                    >
                      {{ attribute }}
                    </span>
                    <span v-if="productAttributeList(item).length === 0" class="text-sm text-slate-400">
                      Chưa có thuộc tính kỹ thuật
                    </span>
                  </div>
                </td>
                <td class="px-4 py-4 align-top">
                  <div class="font-semibold text-slate-700">{{ giaHienThi(item) }}</div>
                  <div class="mt-1 text-xs text-slate-400">{{ thongTinTonKho(item) }}</div>
                </td>
                <td class="px-4 py-4 align-top">
                  <span class="admin-status-chip whitespace-nowrap" :class="trangThaiClass(item.trangThai)">
                    {{ trangThaiLabel(item.trangThai) }}
                  </span>
                </td>
                <td class="px-4 py-4 align-top">
                  <div class="flex items-center justify-end gap-1">
                    <AdminQuickStatusAction
                      :loading="updatingProductStatusId === item.id"
                      :action-label="productQuickToggleLabel(item)"
                      :confirm-message="productQuickToggleConfirmMessage(item)"
                      :intent="productQuickToggleIntent(item)"
                      @toggle="handleToggleProductStatus(item)"
                    />
                    <button
                      @click="openEdit(item)"
                      title="Chỉnh sửa sản phẩm"
                      class="admin-table-action text-slate-600 hover:text-rose-500"
                    >
                      <Pencil :size="14" />
                    </button>
                    <button
                      @click="goToVariants(item)"
                      title="Quản lý biến thể"
                      class="admin-table-action text-slate-600 hover:text-slate-900"
                    >
                      <Eye :size="14" />
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

    <QuanLySanPhamBienTheModal
      :open="showBienTheModal"
      :loading="loadingBienThe"
      :selected-giay="selectedGiay"
      :selected-giay-main-image="selectedGiayMainImage"
      :bien-the-list="bienTheList"
      :updating-bien-the-status-id="updatingBienTheStatusId"
      :trang-thai-class="trangThaiClass"
      :trang-thai-label="trangThaiLabel"
      :gioi-tinh-label="gioiTinhLabel"
      :selected-attribute-list="selectedAttributeList"
      :bien-the-trang-thai-class="bienTheTrangThaiClass"
      :bien-the-trang-thai-label="bienTheTrangThaiLabel"
      :format-count="formatCount"
      :format-currency="formatCurrency"
      @close="closeBienTheModal"
      @edit-product="openEdit"
      @open-add-bienthe="openAddBienTheForm"
      @edit-bienthe="openEditBienThe"
      @toggle-bienthe-status="handleToggleBienTheStatus"
      @delete-bienthe="handleDeleteBienThe"
      @open-images="openHinhAnh"
    />

    <QuanLySanPhamProductModal
      :open="showProductModal"
      :mode="productModalMode"
      :title="productModalTitle"
      :description="productModalDescription"
      :loading="loadingProductDetail"
      :saving="productSaving"
      :danh-muc="danhMuc"
      :product-form="productForm"
      :product-errors="productErrors"
      :selected-product="selectedProductForModal"
      :main-image="productModalMainImage"
      :thuong-hieu-name="thuongHieuName"
      :loai-giay-name="loaiGiayName"
      :gioi-tinh-label="gioiTinhLabel"
      :trang-thai-class="trangThaiClass"
      :trang-thai-label="trangThaiLabel"
      :selected-attribute-list="selectedAttributeList"
      @close="closeProductModal"
      @save="handleSaveProduct"
    />

    <QuanLySanPhamBienTheFormModal
      :open="showAddBienTheForm"
      :editing-bien-the="editingBienThe"
      :selected-giay="selectedGiay"
      :danh-muc="danhMuc"
      :bien-the-form="bienTheForm"
      :bien-the-errors="bienTheErrors"
      :bulk-bien-the-form="bulkBienTheForm"
      :bulk-bien-the-errors="bulkBienTheErrors"
      :generated-bulk-bien-thes="generatedBulkBienThes"
      :saving-bien-the="savingBienThe"
      @close="closeBienTheForm"
      @save="handleSaveBienThe"
      @generate-bulk="generateBulkBienThes"
      @remove-generated-bulk="removeGeneratedBulkBienThe"
    />

    <QuanLySanPhamHinhAnhModal
      :open="showHinhAnhModal"
      :variant="hinhAnhBienThe"
      @close="closeHinhAnhModal"
      @updated="handleHinhAnhUpdated"
      @error="handleHinhAnhError"
    />
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
