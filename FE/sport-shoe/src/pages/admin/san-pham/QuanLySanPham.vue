<script setup>
import { computed, reactive, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Search, Plus, Trash2, Eye, ChevronDown, Check, X, Filter, Layers, Layers3,
  Images, ImageOff, FileSpreadsheet, RotateCcw, Pencil, Upload
} from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'
import AdminTableFooter from '../../../components/common/AdminTableFooter.vue'
import { exportRowsToExcel } from '../../../utils/export-excel'

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
  if (value === 1) return 'Hoạt động'
  if (value === 2) return 'Hết hàng'
  return 'Dừng bán'
}

function bienTheTrangThaiClass(item) {
  if (Number(item.soLuong || 0) <= 0) return 'bg-amber-50 text-amber-600'
  return Number(item.kichHoat) === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-600'
}

function bienTheTrangThaiLabel(item) {
  if (Number(item.soLuong || 0) <= 0) return 'Hết hàng'
  return Number(item.kichHoat) === 1 ? 'Kích hoạt' : 'Tạm dừng'
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
    showToast(error.message || 'Lỗi tải dữ liệu', 'error')
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
    showToast(error.message || 'Lỗi xuất Excel', 'error')
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
const createDetailErrors = reactive({})
const createDetailVariantForm = reactive({
  mauSacIds: [],
  kichCoIds: [],
  soLuong: 0,
  giaGoc: 0,
  giaBan: 0
})
const createDetailGeneratedVariants = ref([])

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

function resetCreateDetailErrors() {
  Object.keys(createDetailErrors).forEach((key) => delete createDetailErrors[key])
}

function clearCreateDetailVariantForm() {
  Object.assign(createDetailVariantForm, {
    mauSacIds: [],
    kichCoIds: [],
    soLuong: 0,
    giaGoc: 0,
    giaBan: 0
  })
  createDetailGeneratedVariants.value = []
  resetCreateDetailErrors()
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

function validateProductForm() {
  resetProductErrors()
  if (!productForm.ten.trim()) productErrors.ten = 'Tên sản phẩm không được để trống'
  if (!productForm.thuongHieuId) productErrors.thuongHieuId = 'Chọn thương hiệu'
  if (!productForm.loaiGiayId) productErrors.loaiGiayId = 'Chọn loại giày'
  return Object.keys(productErrors).length === 0
}

function validateCreateDetailVariantForm() {
  resetCreateDetailErrors()
  if (!createDetailVariantForm.mauSacIds.length) createDetailErrors.mauSacIds = 'Chọn ít nhất 1 màu sắc'
  if (!createDetailVariantForm.kichCoIds.length) createDetailErrors.kichCoIds = 'Chọn ít nhất 1 kích cỡ'
  if (Number(createDetailVariantForm.giaBan) <= 0) createDetailErrors.giaBan = 'Giá bán phải lớn hơn 0'
  if (Number(createDetailVariantForm.giaGoc) < 0) createDetailErrors.giaGoc = 'Giá gốc không được âm'
  if (Number(createDetailVariantForm.soLuong) < 0) createDetailErrors.soLuong = 'Số lượng không được âm'
  return Object.keys(createDetailErrors).length === 0
}

function buildCreateProductPayload() {
  return {
    ma: productForm.ma?.trim() || undefined,
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

function mauSacLabel(id) {
  return danhMuc.value?.mauSac?.find((item) => item.id === Number(id))?.ten || `Màu #${id}`
}

function kichCoLabel(id) {
  return danhMuc.value?.kichCo?.find((item) => item.id === Number(id))?.giaTri || `Size #${id}`
}

function generateCreateDetailVariants() {
  if (!validateCreateDetailVariantForm()) return

  const existingMap = new Map(
    createDetailGeneratedVariants.value.map((item) => [`${item.mauSacId}-${item.kichCoId}`, item])
  )

  createDetailGeneratedVariants.value = createDetailVariantForm.mauSacIds.flatMap((mauSacId) =>
    createDetailVariantForm.kichCoIds.map((kichCoId) => {
      const key = `${mauSacId}-${kichCoId}`
      return existingMap.get(key) || {
        key,
        mauSacId: Number(mauSacId),
        mauSac: mauSacLabel(mauSacId),
        kichCoId: Number(kichCoId),
        kichCo: kichCoLabel(kichCoId),
        soLuong: Number(createDetailVariantForm.soLuong),
        giaGoc: Number(createDetailVariantForm.giaGoc),
        giaBan: Number(createDetailVariantForm.giaBan)
      }
    })
  )

  delete createDetailErrors.generatedVariants
  showToast(`Đã tạo ${createDetailGeneratedVariants.value.length} CTSP nháp`)
}

function removeGeneratedVariant(key) {
  createDetailGeneratedVariants.value = createDetailGeneratedVariants.value.filter((item) => item.key !== key)
}

function validateGeneratedCreateDetailVariants() {
  delete createDetailErrors.generatedVariants

  if (!createDetailGeneratedVariants.value.length) {
    createDetailErrors.generatedVariants = 'Hãy bấm "Tạo CTSP tự động" để sinh danh sách biến thể'
    return false
  }

  const hasInvalidRow = createDetailGeneratedVariants.value.some((item) =>
    Number(item.soLuong) < 0 || Number(item.giaGoc) < 0 || Number(item.giaBan) <= 0
  )

  if (hasInvalidRow) {
    createDetailErrors.generatedVariants = 'Vui lòng kiểm tra lại số lượng và giá trên danh sách CTSP'
    return false
  }

  return true
}

function buildCreateDetailPayload() {
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
    trongLuongId: normalizeNullableNumber(productForm.trongLuongId),
    bienThes: createDetailGeneratedVariants.value.map((item) => ({
      mauSacId: Number(item.mauSacId),
      kichCoId: Number(item.kichCoId),
      soLuong: Number(item.soLuong),
      giaGoc: Number(item.giaGoc),
      giaBan: Number(item.giaBan)
    }))
  }
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
  clearCreateDetailVariantForm()
}

function openAdd() {
  clearProductForm()
  clearCreateDetailVariantForm()
  selectedProductForModal.value = null
  productModalMode.value = 'add'
  showProductModal.value = true
}

async function openEdit(item) {
  clearProductForm()
  clearCreateDetailVariantForm()
  productModalMode.value = 'edit'
  showProductModal.value = true
  loadingProductDetail.value = true
  try {
    const detail = await api.chiTietGiay(item.id)
    selectedProductForModal.value = detail
    hydrateProductForm(detail)
  } catch (error) {
    showToast(error.message || 'Lỗi tải chi tiết sản phẩm', 'error')
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
    showToast(error.message || 'Có lỗi xảy ra khi lưu sản phẩm', 'error')
  } finally {
    productSaving.value = false
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
    showToast(error.message || 'Lỗi tải biến thể sản phẩm', 'error')
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
  if (!bulkBienTheForm.mauSacIds.length) bulkBienTheErrors.mauSacIds = 'Chọn ít nhất 1 màu sắc'
  if (!bulkBienTheForm.kichCoIds.length) bulkBienTheErrors.kichCoIds = 'Chọn ít nhất 1 kích cỡ'
  if (Number(bulkBienTheForm.giaBan) <= 0) bulkBienTheErrors.giaBan = 'Giá bán phải lớn hơn 0'
  if (Number(bulkBienTheForm.giaGoc) < 0) bulkBienTheErrors.giaGoc = 'Giá gốc không được âm'
  if (Number(bulkBienTheForm.soLuong) < 0) bulkBienTheErrors.soLuong = 'Số lượng không được âm'
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
  showToast(`Đã tạo ${generatedBulkBienThes.value.length} CTSP nháp`)
}

function removeGeneratedBulkBienThe(key) {
  generatedBulkBienThes.value = generatedBulkBienThes.value.filter((item) => item.key !== key)
}

function validateGeneratedBulkBienThes() {
  delete bulkBienTheErrors.generated

  if (!generatedBulkBienThes.value.length) {
    bulkBienTheErrors.generated = 'Hãy bấm "Tạo CTSP tự động" để sinh danh sách CTSP'
    return false
  }

  const hasInvalidRow = generatedBulkBienThes.value.some((item) =>
    Number(item.soLuong) < 0 || Number(item.giaGoc) < 0 || Number(item.giaBan) <= 0
  )

  if (hasInvalidRow) {
    bulkBienTheErrors.generated = 'Vui lòng kiểm tra lại số lượng và giá trên danh sách CTSP'
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
      if (Number(bienTheForm.giaBan) <= 0) bienTheErrors.giaBan = 'Giá bán phải lớn hơn 0'
      if (Number(bienTheForm.giaGoc) < 0) bienTheErrors.giaGoc = 'Giá gốc không được âm'
      if (Number(bienTheForm.soLuong) < 0) bienTheErrors.soLuong = 'Số lượng không được âm'
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
    showToast(error.message || 'Lỗi lưu CTSP', 'error')
  } finally {
    savingBienThe.value = false
  }
}

async function handleToggleBienTheStatus(item) {
  updatingBienTheStatusId.value = item.id
  try {
    await api.capNhatBienThe(item.id, {
      soLuong: Number(item.soLuong),
      giaGoc: Number(item.giaGoc),
      giaBan: Number(item.giaBan),
      kichHoat: nextBienTheStatus(item)
    })
    showToast('Cập nhật trạng thái CTSP thành công')
    await Promise.all([
      syncSelectedGiayContext(selectedGiay.value?.id),
      loadData(currentPage.value)
    ])
  } catch (error) {
    showToast(error.message || 'Lỗi cập nhật trạng thái CTSP', 'error')
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
    showToast(error.message || 'Lỗi xóa biến thể', 'error')
  }
}

const showHinhAnhModal = ref(false)
const hinhAnhBienThe = ref(null)
const hinhAnhList = ref([])
const loadingHinhAnh = ref(false)
const showAddHinhAnhForm = ref(false)
const savingHinhAnh = ref(false)
const settingHinhChinhId = ref(null)
const deletingHinhAnhId = ref(null)
const uploadingHinhAnhFile = ref(false)

const hinhAnhForm = reactive({
  url: '',
  loaiHinh: 2,
  moTa: ''
})
const hinhAnhErrors = reactive({})

function closeHinhAnhModal() {
  showHinhAnhModal.value = false
  hinhAnhBienThe.value = null
  hinhAnhList.value = []
  showAddHinhAnhForm.value = false
  Object.assign(hinhAnhForm, { url: '', loaiHinh: 2, moTa: '' })
  Object.keys(hinhAnhErrors).forEach((key) => delete hinhAnhErrors[key])
}

async function openHinhAnh(item) {
  hinhAnhBienThe.value = item
  showHinhAnhModal.value = true
  showAddHinhAnhForm.value = false
  loadingHinhAnh.value = true
  try {
    hinhAnhList.value = await api.layHinhAnh(item.id)
  } catch (error) {
    showToast(error.message || 'Lỗi tải hình ảnh', 'error')
  } finally {
    loadingHinhAnh.value = false
  }
}

function openAddHinhAnhForm() {
  Object.assign(hinhAnhForm, { url: '', loaiHinh: 2, moTa: '' })
  Object.keys(hinhAnhErrors).forEach((key) => delete hinhAnhErrors[key])
  showAddHinhAnhForm.value = true
}

async function handleUploadHinhAnhFile(event) {
  const target = event.target
  if (!target.files?.length) return

  uploadingHinhAnhFile.value = true
  try {
    const url = await api.uploadFile(target.files[0])
    hinhAnhForm.url = url
    showToast('Tải ảnh lên thành công')
  } catch (error) {
    showToast(error.message || 'Lỗi tải ảnh lên', 'error')
  } finally {
    uploadingHinhAnhFile.value = false
    target.value = ''
  }
}

async function handleSaveHinhAnh() {
  Object.keys(hinhAnhErrors).forEach((key) => delete hinhAnhErrors[key])
  if (!hinhAnhForm.url.trim()) {
    hinhAnhErrors.url = 'Vui lòng chọn ảnh hoặc nhập URL ảnh'
    return
  }

  savingHinhAnh.value = true
  try {
    const created = await api.themHinhAnh(hinhAnhBienThe.value.id, {
      url: hinhAnhForm.url.trim(),
      loaiHinh: Number(hinhAnhForm.loaiHinh),
      moTa: hinhAnhForm.moTa || undefined
    })
    if (Number(hinhAnhForm.loaiHinh) === 1) {
      await api.datHinhChinh(created.id)
    }
    showToast('Thêm hình ảnh thành công')
    showAddHinhAnhForm.value = false
    hinhAnhList.value = await api.layHinhAnh(hinhAnhBienThe.value.id)
    await loadData(currentPage.value)
  } catch (error) {
    showToast(error.message || 'Lỗi thêm hình ảnh', 'error')
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
    await loadData(currentPage.value)
  } catch (error) {
    showToast(error.message || 'Lỗi xóa hình ảnh', 'error')
  } finally {
    deletingHinhAnhId.value = null
  }
}

async function handleDatHinhChinh(id) {
  settingHinhChinhId.value = id
  try {
    await api.datHinhChinh(id)
    showToast('Đặt ảnh chính thành công')
    hinhAnhList.value = await api.layHinhAnh(hinhAnhBienThe.value.id)
    await loadData(currentPage.value)
  } catch (error) {
    showToast(error.message || 'Lỗi đặt ảnh chính', 'error')
  } finally {
    settingHinhChinhId.value = null
  }
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
                    ? 'Hoạt động'
                    : filterTrangThai === 2
                      ? 'Hết hàng'
                      : filterTrangThai === 0
                        ? 'Dừng bán'
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
                Hoạt động
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
                Dừng bán
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
              <th class="w-12 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">STT</th>
              <th class="w-16 px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Ảnh</th>
              <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Sản phẩm</th>
              <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Thuộc tính</th>
              <th class="w-[220px] px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Giá / tồn kho</th>
              <th class="w-[180px] px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Trạng thái</th>
              <th class="w-[170px] px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-gray-500">Thao tác</th>
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
                  <div class="flex items-center justify-end gap-1.5">
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
                      class="admin-table-action text-violet-500 hover:text-violet-600"
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

    <Teleport to="body">
      <div
        v-if="showBienTheModal"
        class="fixed inset-0 z-[52] flex items-center justify-center bg-black/55 p-4"
        @click.self="closeBienTheModal"
      >
        <div class="flex max-h-[92vh] w-full max-w-6xl flex-col overflow-hidden rounded-[28px] bg-white shadow-2xl">
          <div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5">
            <div>
              <h2 class="text-xl font-bold text-slate-800">Quản lý biến thể sản phẩm</h2>
              <p class="mt-1 text-sm text-slate-500">
                {{ selectedGiay ? `Đang quản lý CTSP cho ${selectedGiay.ten}.` : 'Xem, thêm và cập nhật CTSP trong popup này.' }}
              </p>
            </div>
            <button
              @click="closeBienTheModal"
              class="rounded-xl p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-700"
            >
              <X :size="18" />
            </button>
          </div>

          <div class="flex-1 overflow-y-auto px-6 py-6">
            <div v-if="loadingBienThe" class="rounded-2xl border border-slate-100 bg-slate-50 px-6 py-12 text-center text-sm text-slate-400">
              Đang tải CTSP...
            </div>

            <template v-else-if="selectedGiay">
              <div class="mb-5 grid gap-4 xl:grid-cols-[1fr_220px]">
                <div class="rounded-3xl border border-slate-100 bg-slate-50 p-5">
                  <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                    <div class="min-w-0">
                      <div class="flex flex-wrap items-center gap-2">
                        <h3 class="text-xl font-bold text-slate-800">{{ selectedGiay.ten }}</h3>
                        <span class="rounded-full bg-white px-2.5 py-1 text-xs font-semibold text-slate-500">
                          {{ selectedGiay.ma }}
                        </span>
                        <span class="admin-status-chip whitespace-nowrap" :class="trangThaiClass(selectedGiay.trangThai)">
                          {{ trangThaiLabel(selectedGiay.trangThai) }}
                        </span>
                      </div>
                      <p class="mt-2 text-sm text-slate-500">
                        {{ selectedGiay.thuongHieu }} · {{ selectedGiay.loaiGiay }} · {{ gioiTinhLabel(selectedGiay.gioiTinh) }}
                      </p>
                      <div class="mt-3 flex flex-wrap gap-1.5">
                        <span
                          v-for="attribute in selectedAttributeList(selectedGiay)"
                          :key="attribute"
                          class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                        >
                          {{ attribute }}
                        </span>
                        <span v-if="selectedAttributeList(selectedGiay).length === 0" class="text-sm text-slate-400">
                          Chưa có thuộc tính kỹ thuật
                        </span>
                      </div>
                      <p v-if="selectedGiay.moTa" class="mt-3 text-sm leading-6 text-slate-500">
                        {{ selectedGiay.moTa }}
                      </p>
                    </div>

                    <div class="flex flex-wrap gap-2">
                      <button
                        @click="openEdit(selectedGiay)"
                        class="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:border-slate-300 hover:bg-slate-50"
                      >
                        <Pencil :size="15" />
                        Sửa sản phẩm
                      </button>
                      <button
                        @click="clearSelectedProduct"
                        class="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:border-slate-300 hover:bg-slate-50"
                      >
                        <X :size="15" />
                        Đóng
                      </button>
                    </div>
                  </div>
                </div>

                <div class="rounded-3xl border border-slate-100 bg-slate-50 p-4">
                  <div class="aspect-square overflow-hidden rounded-2xl border border-slate-200 bg-white">
                    <img
                      v-if="selectedGiayMainImage"
                      :src="selectedGiayMainImage.url"
                      alt=""
                      class="h-full w-full object-cover"
                    />
                    <div v-else class="flex h-full items-center justify-center text-slate-400">
                      <ImageOff class="h-8 w-8" />
                    </div>
                  </div>
                </div>
              </div>

              <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h3 class="text-sm font-bold text-slate-800">Danh sách CTSP</h3>
                  <p class="text-xs text-slate-400">
                    {{ bienTheList.length ? `${bienTheList.length} biến thể đang có.` : 'Sản phẩm này chưa có biến thể nào.' }}
                  </p>
                </div>

                <button
                  @click="openAddBienTheForm"
                  class="inline-flex h-10 items-center gap-2 rounded-xl bg-rose-500 px-4 text-sm font-semibold text-white shadow-sm shadow-rose-200 transition hover:bg-rose-600"
                >
                  <Plus :size="15" />
                  Thêm CTSP
                </button>
              </div>

              <div class="overflow-x-auto">
                <table class="admin-table admin-table--compact min-w-[940px]">
                  <thead class="border-b border-gray-100 bg-gray-50">
                    <tr>
                      <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Biến thể</th>
                      <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Thuộc tính</th>
                      <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Kho / giá</th>
                      <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Trạng thái</th>
                      <th class="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider text-gray-500">Ảnh</th>
                      <th class="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-gray-500">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-50">
                    <tr v-if="bienTheList.length === 0">
                      <td colspan="6" class="px-4 py-10 text-center text-sm text-slate-400">
                        Chưa có CTSP nào. Hãy thêm CTSP đầu tiên để bắt đầu gắn ảnh và số lượng.
                      </td>
                    </tr>
                    <tr v-for="item in bienTheList" :key="item.id" class="hover:bg-gray-50">
                      <td class="px-4 py-4 align-top">
                        <div class="font-semibold text-slate-800">{{ item.maBienThe }}</div>
                        <div class="mt-1 text-xs text-slate-400">SKU: {{ item.sku }}</div>
                      </td>
                      <td class="px-4 py-4 align-top">
                        <div class="flex items-center gap-2 text-slate-700">
                          <span
                            v-if="item.maMauHex"
                            class="h-4 w-4 rounded-full border border-gray-200"
                            :style="`background:${item.maMauHex}`"
                          ></span>
                          <span>{{ item.mauSac }}</span>
                        </div>
                        <div class="mt-1 text-xs text-slate-400">Size {{ item.kichCo }}</div>
                      </td>
                      <td class="px-4 py-4 align-top">
                        <div class="font-semibold text-slate-700">{{ formatCount(item.soLuong) }} sản phẩm</div>
                        <div class="mt-1 text-xs text-slate-400">Giá gốc: {{ formatCurrency(item.giaGoc) }}đ</div>
                        <div class="text-xs text-slate-400">Giá bán: {{ formatCurrency(item.giaBan) }}đ</div>
                      </td>
                      <td class="px-4 py-4 align-top">
                        <div class="flex flex-col items-start gap-2">
                          <span class="admin-status-chip whitespace-nowrap" :class="bienTheTrangThaiClass(item)">
                            {{ bienTheTrangThaiLabel(item) }}
                          </span>
                          <button
                            @click="handleToggleBienTheStatus(item)"
                            :disabled="updatingBienTheStatusId === item.id"
                            class="text-xs font-semibold text-rose-600 transition hover:text-rose-700 disabled:cursor-not-allowed disabled:opacity-60"
                          >
                            {{ updatingBienTheStatusId === item.id ? 'Đang cập nhật...' : (Number(item.kichHoat) === 1 ? 'Tắt nhanh' : 'Kích hoạt nhanh') }}
                          </button>
                        </div>
                      </td>
                      <td class="px-4 py-4 text-center align-top">
                        <button
                          @click="openHinhAnh(item)"
                          class="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-600 transition hover:bg-emerald-100"
                        >
                          <Images :size="13" />
                          Quản lý ảnh
                        </button>
                      </td>
                      <td class="px-4 py-4 align-top">
                        <div class="flex items-center justify-end gap-1.5">
                          <button
                            @click="openEditBienThe(item)"
                            title="Sửa CTSP"
                            class="admin-table-action text-slate-600 hover:text-rose-500"
                          >
                            <Pencil :size="14" />
                          </button>
                          <button
                            @click="handleDeleteBienThe(item.id)"
                            title="Xóa CTSP"
                            class="admin-table-action text-red-500 hover:text-red-600"
                          >
                            <Trash2 :size="14" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </template>

            <div v-else class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-6 py-12 text-center text-sm text-slate-400">
              Không thể tải dữ liệu biến thể cho sản phẩm này.
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div
        v-if="showProductModal"
        class="fixed inset-0 z-[55] flex items-center justify-center bg-black/55 p-4"
        @click.self="closeProductModal"
      >
        <div class="flex max-h-[92vh] w-full max-w-5xl flex-col overflow-hidden rounded-[28px] bg-white shadow-2xl">
          <div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5">
            <div>
              <h2 class="text-xl font-bold text-slate-800">{{ productModalTitle }}</h2>
              <p class="mt-1 text-sm text-slate-500">{{ productModalDescription }}</p>
            </div>
            <button
              @click="closeProductModal"
              class="rounded-xl p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-700"
            >
              <X :size="18" />
            </button>
          </div>

          <div v-if="loadingProductDetail" class="px-6 py-16 text-center text-sm text-slate-400">
            Đang tải chi tiết sản phẩm...
          </div>

          <div v-else class="flex-1 overflow-y-auto px-6 py-6">
            <div class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_300px]">
              <section class="rounded-[24px] border border-slate-100 bg-white p-5">
                <div class="mb-5">
                  <h3 class="text-base font-bold text-slate-800">Thông tin cơ bản</h3>
                  <p class="mt-1 text-xs text-slate-400">
                    {{
                      productModalMode === 'add'
                        ? 'Nhập thông tin sản phẩm rồi sinh danh sách CTSP ngay trong popup này.'
                        : 'Cập nhật thông tin sản phẩm. CTSP và ảnh vẫn được quản lý riêng.'
                    }}
                  </p>
                </div>

                <div class="grid gap-4 md:grid-cols-2">
                  <div v-if="productModalMode === 'add'">
                    <label class="mb-1 block text-xs font-medium text-gray-700">Mã sản phẩm</label>
                    <div class="rounded-lg border border-dashed border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-500">
                      Tự sinh khi lưu
                    </div>
                  </div>

                  <div :class="productModalMode === 'add' ? '' : 'md:col-span-2'">
                    <label class="mb-1 block text-xs font-medium text-gray-700">Tên sản phẩm *</label>
                    <select
                      v-model="productForm.ten"
                      class="w-full rounded-lg border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                      :class="productErrors.ten ? 'border-red-400' : 'border-gray-200'"
                      placeholder="Tên sản phẩm"
                    />
                    <p v-if="productErrors.ten" class="mt-1 text-xs text-red-500">{{ productErrors.ten }}</p>
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Thương hiệu *</label>
                    <select
                      v-model="productForm.thuongHieuId"
                      class="w-full rounded-lg border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                      :class="productErrors.thuongHieuId ? 'border-red-400' : 'border-gray-200'"
                    >
                      <option :value="null">-- Chọn thương hiệu --</option>
                      <option v-for="item in danhMuc?.thuongHieu" :key="item.id" :value="item.id">{{ item.ten }}</option>
                    </select>
                    <p v-if="productErrors.thuongHieuId" class="mt-1 text-xs text-red-500">{{ productErrors.thuongHieuId }}</p>
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Loại giày *</label>
                    <select
                      v-model="productForm.loaiGiayId"
                      class="w-full rounded-lg border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                      :class="productErrors.loaiGiayId ? 'border-red-400' : 'border-gray-200'"
                    >
                      <option :value="null">-- Chọn loại giày --</option>
                      <option v-for="item in danhMuc?.loaiGiay" :key="item.id" :value="item.id">{{ item.ten }}</option>
                    </select>
                    <p v-if="productErrors.loaiGiayId" class="mt-1 text-xs text-red-500">{{ productErrors.loaiGiayId }}</p>
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Giới tính</label>
                    <select
                      v-model="productForm.gioiTinh"
                      class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                    >
                      <option :value="null">-- Tất cả --</option>
                      <option :value="1">Nam</option>
                      <option :value="2">Nữ</option>
                      <option :value="3">Unisex</option>
                    </select>
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Chất liệu</label>
                    <select
                      v-model.number="productForm.chatLieuGiayId"
                      class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                    >
                      <option :value="null">-- Chọn chất liệu giày --</option>
                      <option v-for="item in danhMuc?.chatLieuGiay || []" :key="item.id" :value="item.id">{{ item.ten }}</option>
                    </select>
                  </div>
                </div>

                <div class="mt-5 rounded-2xl border border-slate-100 bg-slate-50 p-4">
                  <div class="mb-4">
                    <h3 class="text-sm font-bold text-slate-700">Thuộc tính kỹ thuật</h3>
                    <p class="mt-1 text-xs text-slate-400">
                      Các thuộc tính này sẽ hiển thị trực tiếp ở danh sách sản phẩm.
                    </p>
                  </div>

                  <div class="grid gap-4 md:grid-cols-2">
                    <div>
                      <label class="mb-1 block text-xs font-medium text-gray-700">Đế giày</label>
                      <select
                        v-model="productForm.deGiayId"
                        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                      >
                        <option :value="null">-- Không có --</option>
                        <option v-for="item in danhMuc?.deGiay" :key="item.id" :value="item.id">{{ item.ten }}</option>
                      </select>
                    </div>

                    <div>
                      <label class="mb-1 block text-xs font-medium text-gray-700">Cổ giày</label>
                      <select
                        v-model="productForm.coGiayId"
                        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                      >
                        <option :value="null">-- Không có --</option>
                        <option v-for="item in danhMuc?.coGiay" :key="item.id" :value="item.id">{{ item.ten }}</option>
                      </select>
                    </div>

                    <div>
                      <label class="mb-1 block text-xs font-medium text-gray-700">Công nghệ đệm</label>
                      <select
                        v-model="productForm.congNgheDemId"
                        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                      >
                        <option :value="null">-- Không có --</option>
                        <option v-for="item in danhMuc?.congNgheDem" :key="item.id" :value="item.id">{{ item.ten }}</option>
                      </select>
                    </div>

                    <div>
                      <label class="mb-1 block text-xs font-medium text-gray-700">Trọng lượng</label>
                      <select
                        v-model="productForm.trongLuongId"
                        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                      >
                        <option :value="null">-- Không có --</option>
                        <option v-for="item in danhMuc?.trongLuong" :key="item.id" :value="item.id">
                          {{ item.ma }} - {{ item.giaTri }}
                        </option>
                      </select>
                    </div>
                  </div>
                </div>

                <div class="mt-5">
                  <label class="mb-1 block text-xs font-medium text-gray-700">Mô tả</label>
                  <textarea
                    v-model="productForm.moTa"
                    rows="5"
                    class="w-full resize-none rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                    placeholder="Mô tả ngắn về sản phẩm..."
                  ></textarea>
                </div>

              </section>

              <aside class="space-y-5">
                <section class="rounded-[24px] border border-slate-100 bg-white p-5">
                  <div class="mb-4">
                    <h3 class="text-base font-bold text-slate-800">
                      {{ productModalMode === 'add' ? 'Thông tin nhanh' : 'Tóm tắt' }}
                    </h3>
                    <p class="mt-1 text-xs text-slate-400">
                      {{
                        productModalMode === 'add'
                          ? 'Lưu xong sẽ mở popup biến thể để bạn thêm CTSP cho sản phẩm này.'
                          : 'Theo dõi nhanh trạng thái hiện tại của sản phẩm.'
                      }}
                    </p>
                  </div>

                  <div v-if="productModalMode === 'edit'" class="space-y-4">
                    <div class="overflow-hidden rounded-2xl border border-slate-100 bg-slate-50">
                      <div class="aspect-square">
                        <img
                          v-if="productModalMainImage"
                          :src="productModalMainImage.url"
                          alt=""
                          class="h-full w-full object-cover"
                        />
                        <div v-else class="flex h-full items-center justify-center text-slate-400">
                          <ImageOff class="h-8 w-8" />
                        </div>
                      </div>
                    </div>

                    <div class="space-y-2 text-sm text-slate-600">
                      <div class="flex items-center justify-between gap-3">
                        <span class="text-slate-400">Mã sản phẩm</span>
                        <span class="font-semibold text-slate-700">{{ selectedProductForModal?.ma }}</span>
                      </div>
                      <div class="flex items-center justify-between gap-3">
                        <span class="text-slate-400">Trạng thái</span>
                        <span class="admin-status-chip whitespace-nowrap" :class="trangThaiClass(selectedProductForModal?.trangThai)">
                          {{ trangThaiLabel(selectedProductForModal?.trangThai) }}
                        </span>
                      </div>
                    </div>

                    <div class="flex flex-wrap gap-2">
                      <span
                        v-for="attribute in selectedAttributeList(selectedProductForModal)"
                        :key="attribute"
                        class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600"
                      >
                        {{ attribute }}
                      </span>
                    </div>
                  </div>

                  <div v-else class="space-y-4">
                    <div class="rounded-2xl border border-slate-100 bg-slate-50 p-4">
                      <div class="space-y-2 text-sm text-slate-600">
                        <div class="flex items-center justify-between gap-3">
                          <span class="text-slate-400">Mã sản phẩm</span>
                          <span class="font-semibold text-slate-700">Tự sinh khi lưu</span>
                        </div>
                        <div class="flex items-center justify-between gap-3">
                          <span class="text-slate-400">CTSP</span>
                          <span class="font-semibold text-slate-700">Thêm ở popup biến thể</span>
                        </div>
                        <div class="flex items-center justify-between gap-3">
                          <span class="text-slate-400">Ảnh</span>
                          <span class="font-semibold text-slate-700">Theo từng CTSP</span>
                        </div>
                      </div>
                    </div>

                    <div class="flex flex-wrap gap-2">
                      <span v-if="productForm.thuongHieuId" class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600">
                        {{ thuongHieuName(productForm.thuongHieuId) }}
                      </span>
                      <span v-if="productForm.loaiGiayId" class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600">
                        {{ loaiGiayName(productForm.loaiGiayId) }}
                      </span>
                      <span v-if="productForm.gioiTinh" class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600">
                        {{ gioiTinhLabel(productForm.gioiTinh) }}
                      </span>
                    </div>
                  </div>
                </section>
              </aside>
            </div>
          </div>

          <div class="flex justify-end gap-3 border-t border-slate-100 px-6 py-4">
            <button
              @click="closeProductModal"
              class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
            >
              Hủy
            </button>
            <button
              @click="handleSaveProduct"
              :disabled="productSaving || loadingProductDetail"
              class="rounded-xl bg-rose-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {{ productSaving ? 'Đang lưu...' : (productModalMode === 'add' ? 'Lưu sản phẩm' : 'Lưu thay đổi') }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div
        v-if="showAddBienTheForm"
        class="fixed inset-0 z-[60] flex items-center justify-center bg-black/55 p-4"
        @click.self="closeBienTheForm"
      >
        <div
          class="flex max-h-[90vh] w-full flex-col rounded-2xl bg-white shadow-2xl"
          :class="editingBienThe ? 'max-w-xl' : 'max-w-5xl'"
        >
          <div class="flex items-center justify-between border-b border-gray-100 px-6 py-4">
            <h2 class="text-lg font-semibold text-gray-800">
              {{ editingBienThe ? 'Cập nhật CTSP' : 'Thêm CTSP mới' }}
            </h2>
            <button @click="closeBienTheForm" class="rounded-lg p-1.5 hover:bg-gray-100">
              <X :size="18" />
            </button>
          </div>

          <div class="overflow-y-auto p-6">
            <div class="mb-4 rounded-2xl border border-violet-100 bg-violet-50 px-4 py-3 text-sm text-violet-700">
              {{ selectedGiay?.ten }} · {{ selectedGiay?.ma }}
            </div>

            <div v-if="editingBienThe" class="grid grid-cols-2 gap-4">
              <div>
                <label class="mb-1 block text-xs font-medium text-gray-700">Số lượng</label>
                <input
                  v-model.number="bienTheForm.soLuong"
                  type="number"
                  min="0"
                  class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                />
                <p v-if="bienTheErrors.soLuong" class="mt-1 text-xs text-red-500">{{ bienTheErrors.soLuong }}</p>
              </div>

              <div>
                <label class="mb-1 block text-xs font-medium text-gray-700">Giá gốc</label>
                <input
                  v-model.number="bienTheForm.giaGoc"
                  type="number"
                  min="0"
                  class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                />
                <p v-if="bienTheErrors.giaGoc" class="mt-1 text-xs text-red-500">{{ bienTheErrors.giaGoc }}</p>
              </div>

              <div>
                <label class="mb-1 block text-xs font-medium text-gray-700">Giá bán *</label>
                <input
                  v-model.number="bienTheForm.giaBan"
                  type="number"
                  min="1"
                  class="w-full rounded-lg border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="bienTheErrors.giaBan ? 'border-red-400' : 'border-gray-200'"
                />
                <p v-if="bienTheErrors.giaBan" class="mt-1 text-xs text-red-500">{{ bienTheErrors.giaBan }}</p>
              </div>

              <div>
                <label class="mb-1 block text-xs font-medium text-gray-700">Trạng thái</label>
                <select
                  v-model.number="bienTheForm.kichHoat"
                  class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                >
                  <option :value="1">Kích hoạt</option>
                  <option :value="2">Tạm dừng</option>
                </select>
              </div>
            </div>

            <div v-else class="space-y-5">
              <div class="grid gap-5 xl:grid-cols-[360px_minmax(0,1fr)]">
                <section class="rounded-2xl border border-slate-100 bg-slate-50 p-4">
                  <div class="mb-4">
                    <h3 class="text-sm font-bold text-slate-800">Sinh CTSP tự động</h3>
                    <p class="mt-1 text-xs text-slate-400">Chọn màu sắc, kích cỡ và giá trị mặc định để tạo danh sách CTSP.</p>
                  </div>

                  <div class="space-y-4">
                    <div>
                      <label class="mb-2 block text-xs font-medium text-gray-700">Màu sắc *</label>
                      <div class="flex flex-wrap gap-2">
                        <label
                          v-for="item in danhMuc?.mauSac"
                          :key="item.id"
                          class="inline-flex cursor-pointer items-center gap-2 rounded-full border px-3 py-1.5 text-xs transition"
                          :class="bulkBienTheForm.mauSacIds.includes(item.id) ? 'border-rose-300 bg-rose-50 text-rose-600' : 'border-slate-200 bg-white text-slate-600'"
                        >
                          <input v-model="bulkBienTheForm.mauSacIds" type="checkbox" class="hidden" :value="item.id" />
                          <span
                            class="h-2.5 w-2.5 rounded-full border border-black/5"
                            :style="{ backgroundColor: item.maMauHex || '#e2e8f0' }"
                          ></span>
                          {{ item.ten }}
                        </label>
                      </div>
                      <p v-if="bulkBienTheErrors.mauSacIds" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.mauSacIds }}</p>
                    </div>

                    <div>
                      <label class="mb-2 block text-xs font-medium text-gray-700">Kích cỡ *</label>
                      <div class="flex flex-wrap gap-2">
                        <label
                          v-for="item in danhMuc?.kichCo"
                          :key="item.id"
                          class="inline-flex cursor-pointer items-center gap-2 rounded-full border px-3 py-1.5 text-xs transition"
                          :class="bulkBienTheForm.kichCoIds.includes(item.id) ? 'border-rose-300 bg-rose-50 text-rose-600' : 'border-slate-200 bg-white text-slate-600'"
                        >
                          <input v-model="bulkBienTheForm.kichCoIds" type="checkbox" class="hidden" :value="item.id" />
                          Size {{ item.giaTri }}
                        </label>
                      </div>
                      <p v-if="bulkBienTheErrors.kichCoIds" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.kichCoIds }}</p>
                    </div>

                    <div class="grid gap-3 sm:grid-cols-3">
                      <div>
                        <label class="mb-1 block text-xs font-medium text-gray-700">Số lượng mặc định</label>
                        <input
                          v-model.number="bulkBienTheForm.soLuong"
                          type="number"
                          min="0"
                          class="w-full rounded-lg border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                          :class="bulkBienTheErrors.soLuong ? 'border-red-400' : 'border-gray-200'"
                        />
                        <p v-if="bulkBienTheErrors.soLuong" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.soLuong }}</p>
                      </div>

                      <div>
                        <label class="mb-1 block text-xs font-medium text-gray-700">Giá gốc mặc định</label>
                        <input
                          v-model.number="bulkBienTheForm.giaGoc"
                          type="number"
                          min="0"
                          class="w-full rounded-lg border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                          :class="bulkBienTheErrors.giaGoc ? 'border-red-400' : 'border-gray-200'"
                        />
                        <p v-if="bulkBienTheErrors.giaGoc" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.giaGoc }}</p>
                      </div>

                      <div>
                        <label class="mb-1 block text-xs font-medium text-gray-700">Giá bán mặc định *</label>
                        <input
                          v-model.number="bulkBienTheForm.giaBan"
                          type="number"
                          min="1"
                          class="w-full rounded-lg border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                          :class="bulkBienTheErrors.giaBan ? 'border-red-400' : 'border-gray-200'"
                        />
                        <p v-if="bulkBienTheErrors.giaBan" class="mt-1 text-xs text-red-500">{{ bulkBienTheErrors.giaBan }}</p>
                      </div>
                    </div>

                    <button
                      type="button"
                      @click="generateBulkBienThes"
                      class="w-full rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800"
                    >
                      Tạo CTSP tự động
                    </button>
                  </div>
                </section>

                <section class="rounded-2xl border border-slate-100 bg-white p-4">
                  <div class="mb-3 flex items-center justify-between gap-3">
                    <div>
                      <h3 class="text-sm font-bold text-slate-800">Danh sách CTSP sẽ tạo</h3>
                      <p class="mt-1 text-xs text-slate-400">Bạn có thể chỉnh từng dòng trước khi lưu.</p>
                    </div>
                    <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
                      {{ generatedBulkBienThes.length }} CTSP
                    </span>
                  </div>

                  <p v-if="bulkBienTheErrors.generated" class="mb-3 text-xs text-red-500">
                    {{ bulkBienTheErrors.generated }}
                  </p>

                  <div v-if="generatedBulkBienThes.length" class="overflow-x-auto rounded-2xl border border-slate-200 bg-white">
                    <table class="min-w-full text-sm">
                      <thead class="bg-slate-50 text-slate-500">
                        <tr>
                          <th class="px-3 py-2 text-left font-semibold">Màu sắc</th>
                          <th class="px-3 py-2 text-left font-semibold">Kích cỡ</th>
                          <th class="px-3 py-2 text-left font-semibold">Số lượng</th>
                          <th class="px-3 py-2 text-left font-semibold">Giá gốc</th>
                          <th class="px-3 py-2 text-left font-semibold">Giá bán</th>
                          <th class="px-3 py-2 text-center font-semibold">Xóa</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr
                          v-for="item in generatedBulkBienThes"
                          :key="item.key"
                          class="border-t border-slate-100"
                        >
                          <td class="px-3 py-2 text-slate-700">{{ item.mauSac }}</td>
                          <td class="px-3 py-2 text-slate-700">Size {{ item.kichCo }}</td>
                          <td class="px-3 py-2">
                            <input
                              v-model.number="item.soLuong"
                              type="number"
                              min="0"
                              class="w-24 rounded-lg border border-slate-200 px-2.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                            />
                          </td>
                          <td class="px-3 py-2">
                            <input
                              v-model.number="item.giaGoc"
                              type="number"
                              min="0"
                              class="w-28 rounded-lg border border-slate-200 px-2.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                            />
                          </td>
                          <td class="px-3 py-2">
                            <input
                              v-model.number="item.giaBan"
                              type="number"
                              min="1"
                              class="w-28 rounded-lg border border-slate-200 px-2.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                            />
                          </td>
                          <td class="px-3 py-2 text-center">
                            <button
                              type="button"
                              @click="removeGeneratedBulkBienThe(item.key)"
                              class="inline-flex rounded-lg p-2 text-rose-500 transition hover:bg-rose-50 hover:text-rose-600"
                            >
                              <Trash2 :size="14" />
                            </button>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div
                    v-else
                    class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-10 text-center text-sm text-slate-400"
                  >
                    Chọn màu sắc, kích cỡ rồi bấm "Tạo CTSP tự động" để sinh danh sách biến thể.
                  </div>
                </section>
              </div>
            </div>
          </div>

          <div class="flex justify-end gap-3 border-t border-gray-100 px-6 py-4">
            <button @click="closeBienTheForm" class="rounded-lg border border-gray-200 px-4 py-2 text-sm hover:bg-gray-50">
              Hủy
            </button>
            <button
              @click="handleSaveBienThe"
              :disabled="savingBienThe"
              class="rounded-lg bg-rose-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-rose-600 disabled:opacity-60"
            >
              {{ savingBienThe ? 'Đang lưu...' : (editingBienThe ? 'Lưu CTSP' : 'Lưu danh sách CTSP') }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div
        v-if="showHinhAnhModal"
        class="fixed inset-0 z-[70] flex items-center justify-center bg-black/60 p-4"
        @click.self="closeHinhAnhModal"
      >
        <div class="flex max-h-[90vh] w-full max-w-3xl flex-col rounded-2xl bg-white shadow-2xl">
          <div class="flex items-center justify-between border-b border-gray-100 px-6 py-4">
            <h2 class="text-lg font-semibold text-gray-800">
              Ảnh biến thể - {{ hinhAnhBienThe?.mauSac }} / {{ hinhAnhBienThe?.kichCo }}
            </h2>
            <button @click="closeHinhAnhModal" class="rounded-lg p-1.5 hover:bg-gray-100">
              <X :size="18" />
            </button>
          </div>

          <div class="flex-1 overflow-y-auto p-6">
            <div v-if="loadingHinhAnh" class="py-8 text-center text-gray-400">Đang tải hình ảnh...</div>
            <div v-else>
              <div v-if="hinhAnhList.length > 0" class="mb-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
                <div
                  v-for="item in hinhAnhList"
                  :key="item.id"
                  class="group relative overflow-hidden rounded-xl border border-gray-100 bg-gray-50"
                >
                  <div class="aspect-square">
                    <img :src="item.url" :alt="item.moTa || ''" class="h-full w-full object-cover" />
                  </div>
                  <div
                    v-if="item.laHinhChinh"
                    class="absolute left-2 top-2 rounded bg-yellow-400 px-2 py-0.5 text-xs font-medium text-yellow-900"
                  >
                    Ảnh chính
                  </div>
                  <div class="absolute inset-0 flex items-end justify-center gap-1 bg-black/0 pb-2 opacity-0 transition-all group-hover:bg-black/30 group-hover:opacity-100">
                    <button
                      v-if="!item.laHinhChinh"
                      @click="handleDatHinhChinh(item.id)"
                      :disabled="settingHinhChinhId === item.id"
                      class="rounded bg-yellow-400 px-2 py-1 text-xs font-medium text-yellow-900 disabled:opacity-60"
                    >
                      {{ settingHinhChinhId === item.id ? '...' : 'Đặt chính' }}
                    </button>
                    <button
                      @click="handleDeleteHinhAnh(item.id)"
                      :disabled="deletingHinhAnhId === item.id"
                      class="rounded bg-red-500 p-1 text-white disabled:opacity-60"
                    >
                      <Trash2 :size="12" />
                    </button>
                  </div>
                  <p v-if="item.moTa" class="truncate border-t border-gray-100 px-2 py-1 text-xs text-gray-500">
                    {{ item.moTa }}
                  </p>
                </div>
              </div>
              <p v-else class="py-6 text-center text-sm text-gray-400">Chưa có hình ảnh nào</p>

              <div v-if="showAddHinhAnhForm" class="rounded-xl border border-rose-100 bg-rose-50 p-4">
                <p class="mb-3 text-sm font-semibold text-rose-700">Thêm hình ảnh</p>
                <div class="space-y-3">
                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Tải ảnh từ máy</label>
                    <label class="flex cursor-pointer items-center justify-center gap-2 rounded-xl border border-dashed border-rose-200 bg-white px-4 py-3 text-sm font-medium text-rose-600 transition hover:border-rose-300 hover:bg-rose-50">
                      <Upload :size="15" />
                      {{ uploadingHinhAnhFile ? 'Đang tải ảnh...' : 'Chọn ảnh để upload' }}
                      <input type="file" accept="image/*" class="hidden" @change="handleUploadHinhAnhFile" />
                    </label>
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">URL ảnh *</label>
                    <input
                      v-model="hinhAnhForm.url"
                      type="url"
                      placeholder="https://..."
                      class="w-full rounded-lg border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-rose-400"
                      :class="hinhAnhErrors.url ? 'border-red-400' : 'border-gray-200'"
                    />
                    <p v-if="hinhAnhErrors.url" class="mt-1 text-xs text-red-500">{{ hinhAnhErrors.url }}</p>
                  </div>

                  <div v-if="hinhAnhForm.url" class="overflow-hidden rounded-xl border border-gray-200 bg-white">
                    <div class="aspect-[4/3]">
                      <img :src="hinhAnhForm.url" alt="" class="h-full w-full object-cover" />
                    </div>
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Loại hình</label>
                    <select
                      v-model.number="hinhAnhForm.loaiHinh"
                      class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                    >
                      <option :value="1">Đặt làm ảnh chính</option>
                      <option :value="2">Ảnh phụ</option>
                    </select>
                  </div>

                  <div>
                    <label class="mb-1 block text-xs font-medium text-gray-700">Mô tả</label>
                    <input
                      v-model="hinhAnhForm.moTa"
                      type="text"
                      placeholder="Mô tả hình ảnh..."
                      class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                    />
                  </div>
                </div>

                <div class="mt-3 flex gap-2">
                  <button
                    @click="showAddHinhAnhForm = false"
                    class="rounded-lg border border-gray-200 px-3 py-1.5 text-sm hover:bg-gray-50"
                  >
                    Hủy
                  </button>
                  <button
                    @click="handleSaveHinhAnh"
                    :disabled="savingHinhAnh"
                    class="rounded-lg bg-rose-500 px-3 py-1.5 text-sm text-white hover:bg-rose-600 disabled:opacity-60"
                  >
                    {{ savingHinhAnh ? 'Đang lưu...' : 'Thêm ảnh' }}
                  </button>
                </div>
              </div>

              <button
                v-if="!showAddHinhAnhForm"
                @click="openAddHinhAnhForm"
                class="mt-2 flex items-center gap-1.5 text-sm font-medium text-rose-600 hover:text-rose-700"
              >
                <Plus :size="14" />
                Thêm hình ảnh
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
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
