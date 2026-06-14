<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCheckBig, Eye, FileSpreadsheet, Filter, Images, Layers3, Plus, RotateCcw, Search, Tag, TriangleAlert, X } from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'
import AdminQrCodeModal from '../../../components/common/AdminQrCodeModal.vue'
import BanHangQrScannerModal from '../../../components/admin/ban-hang/BanHangQrScannerModal.vue'
import AdminQuickStatusAction from '../../../components/common/AdminQuickStatusAction.vue'
import AdminTableFooter from '../../../components/common/AdminTableFooter.vue'
import ProductVariantFilters from '../../../components/admin/san-pham/ProductVariantFilters.vue'
import ProductVariantTable from '../../../components/admin/san-pham/ProductVariantTable.vue'
import QuanLySanPhamBienTheFormModal from '../../../components/admin/san-pham/QuanLySanPhamBienTheFormModal.vue'
import QuanLySanPhamHinhAnhModal from '../../../components/admin/san-pham/QuanLySanPhamHinhAnhModal.vue'
import { exportRowsToExcel } from '../../../utils/export-excel'
import { getDisplayErrorMessage, getFieldErrors } from '../../../utils/error-message'
import { showSuccess, showError } from '../../../utils/alert'
import { createQrCodeSvg } from '../../../utils/qr-code'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const items = ref([])
const danhMuc = ref(null)
const currentPage = ref(0)
const pageSize = ref(5)
const totalItems = ref(0)
const totalPages = ref(0)
const selectedProduct = ref(null)
const updatingStatusIds = reactive(new Set())
const showQrModal = ref(false)
const selectedQrItem = ref(null)

const showScannerModal = ref(false)
const tableRef = ref(null)
const hasSelectedVariants = ref(false)

const filters = reactive({
  keyword: '',
  mauSacId: null,
  kichCoId: null,
  trangThai: null
})

const showImageModal = ref(false)
const selectedVariant = ref(null)
const showEditVariantModal = ref(false)
const editingVariant = ref(null)
const savingVariant = ref(false)
const bienTheForm = reactive({
  soLuong: 0,
  giaGoc: 0,
  giaBan: 0,
  kichHoat: 1
})
const bienTheErrors = reactive({})
const bulkBienTheForm = reactive({
  mauSacIds: [],
  kichCoIds: [],
  soLuong: 0,
  giaGoc: 0,
  giaBan: 0
})
const bulkBienTheErrors = reactive({})
const generatedBulkBienThes = ref([])

const toast = reactive({
  show: false,
  message: '',
  type: 'success'
})

const pageSizeOptions = [5, 10, 20, 50]
let toastTimer = null
let latestLoadRequestId = 0
let keywordSearchTimer = null
let suppressGiayIdWatch = false

const selectedGiayId = computed(() => {
  const raw = Array.isArray(route.query.giayId) ? route.query.giayId[0] : route.query.giayId
  const parsed = Number(raw)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
})

const focusedChiTietId = computed(() => {
  const raw = Array.isArray(route.query.chiTietId) ? route.query.chiTietId[0] : route.query.chiTietId
  const parsed = Number(raw)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
})

const toastTitle = computed(() => {
  if (toast.type === 'error') return 'Không thể hoàn tất thao tác'
  if (toast.message.startsWith('Đang xem CTSP')) return 'Xem CTSP thành công'
  return 'Thao tác thành công'
})

const editingSelectedGiay = computed(() => {
  if (selectedProduct.value) return selectedProduct.value
  if (!editingVariant.value) return null

  return {
    id: editingVariant.value.giayId,
    ten: editingVariant.value.tenSanPham,
    ma: editingVariant.value.maSanPham
  }
})

function showToast(message, type = 'success') {
  if (type === 'success') {
    showSuccess(message)
    return
  }

  if (type === 'error') {
    showError(message)
    return
  }

  if (toastTimer) clearTimeout(toastTimer)
  toast.message = message
  toast.type = type
  toast.show = true
  toastTimer = setTimeout(() => {
    toast.show = false
    toastTimer = null
  }, 3000)
}

function closeToast() {
  if (toastTimer) {
    clearTimeout(toastTimer)
    toastTimer = null
  }
  toast.show = false
}

function isUpdatingStatus(id) {
  return updatingStatusIds.has(id)
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function clearBienTheErrors() {
  Object.keys(bienTheErrors).forEach((key) => delete bienTheErrors[key])
}

function parsePositiveMoney(value) {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

function parseStock(value) {
  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed >= 0 ? parsed : null
}

function isDiscounted(item) {
  return Number(item?.giaBan || 0) < Number(item?.giaGoc || 0)
}

function formatPercentValue(value) {
  const numericValue = Number(value)
  if (!Number.isFinite(numericValue) || numericValue <= 0) return '—'

  const normalizedValue = Math.min(numericValue, 100)
  return normalizedValue % 1 === 0 ? `${normalizedValue.toFixed(0)}%` : `${normalizedValue.toFixed(1)}%`
}

function formatDiscountPercent(item) {
  const loaiGiam = Number(item?.loaiGiam || 0)
  const giaTriGiam = Number(item?.giaTriGiam || 0)
  const giaGoc = Number(item?.giaGoc || 0)
  const giaBan = Number(item?.giaBan || 0)

  if (giaTriGiam > 0) {
    if (loaiGiam === 1) {
      return formatPercentValue(giaTriGiam)
    }

    if (loaiGiam === 2 && giaGoc > 0) {
      return formatPercentValue((giaTriGiam / giaGoc) * 100)
    }
  }
  if (giaGoc <= 0 || giaBan >= giaGoc) return '—'

  return formatPercentValue(((giaGoc - giaBan) / giaGoc) * 100)
}

function discountTitle(item) {
  return item?.maDotGiamGia || item?.tenDotGiamGia || 'Xem đợt giảm giá'
}

function isFocusedVariant(item) {
  return focusedChiTietId.value != null && Number(item?.id) === focusedChiTietId.value
}

function openDiscountDetail(item) {
  if (!item?.dotGiamGiaId) return
  router.push({
    name: 'admin-dot-giam-gia-chi-tiet',
    params: { id: item.dotGiamGiaId }
  })
}

function bienTheTrangThaiLabel(item) {
  if (Number(item.kichHoat) === 0) return 'Ngừng bán'
  if (Number(item.soLuong || 0) <= 0) return 'Hết hàng'
  return 'Đang bán'
}

function bienTheTrangThaiClass(item) {
  if (Number(item.kichHoat) === 0) return 'bg-slate-100 text-slate-600'
  if (Number(item.soLuong || 0) <= 0) return 'bg-amber-50 text-amber-600'
  return 'bg-emerald-50 text-emerald-600'
}

function nextBienTheStatus(item) {
  return Number(item.kichHoat) === 1 ? 0 : 1
}

function quickToggleLabel(item) {
  if (Number(item.kichHoat) === 1) return 'Chuyển sang ngừng bán'
  return 'Chuyển sang đang bán'
}

function canToggleStatus(item) {
  return true
}

function quickToggleIntent(item) {
  return Number(item.kichHoat) === 1 ? 'deactivate' : 'activate'
}

function quickToggleDisabledTitle(item) {
  return quickToggleLabel(item)
}

function quickToggleConfirmMessage(item) {
  const action = Number(item.kichHoat) === 1 ? 'ngừng bán' : 'đang bán'
  const target = item.maChiTietSanPham || item.maBienThe || item.sku || `#${item.id}`
  return `Bạn có muốn chuyển CTSP "${target}" sang ${action} không?`
}

async function loadDanhMuc() {
  try {
    danhMuc.value = await api.layDanhMuc()
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không tải được danh mục sản phẩm'), 'error')
  }
}

async function syncSelectedProduct() {
  if (!selectedGiayId.value) {
    selectedProduct.value = null
    return
  }

  try {
    selectedProduct.value = await api.chiTietGiay(selectedGiayId.value)
    showToast(`Đang xem CTSP của ${selectedProduct.value.ten} (${selectedProduct.value.ma})`, 'info')
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không tải được sản phẩm đang chọn'), 'error')
  }
}

async function loadData(page = 0) {
  const requestId = ++latestLoadRequestId

  // Khi có bộ lọc/tìm kiếm đang kích hoạt → bỏ giayId để tìm trên tất cả sản phẩm
  const hasActiveFilter =
    filters.keyword.trim() ||
    filters.mauSacId ||
    filters.kichCoId ||
    filters.trangThai != null

  // Xác định giayId hiệu lực trước khi đổi route
  const effectiveGiayId = hasActiveFilter ? null : selectedGiayId.value

  if (selectedGiayId.value && hasActiveFilter) {
    if (requestId !== latestLoadRequestId) return
    // Dùng flag để watcher không gọi lại loadData khi ta chủ động replace route
    suppressGiayIdWatch = true
    await router.replace({ name: 'admin-bien-the-san-pham' })
    suppressGiayIdWatch = false
  }

  loading.value = true
  try {
    const response = await api.layDanhSachChiTietSanPham({
      keyword: filters.keyword.trim() || undefined,
      giayId: effectiveGiayId,
      mauSacId: filters.mauSacId,
      kichCoId: filters.kichCoId,
      trangThai: filters.trangThai,
      page: effectiveGiayId ? 0 : page,
      size: effectiveGiayId ? 1000 : pageSize.value
    })

    if (requestId !== latestLoadRequestId) return
    items.value = response.items || []
    currentPage.value = response.page
    totalItems.value = response.totalItems
    totalPages.value = response.totalPages
  } catch (error) {
    if (requestId !== latestLoadRequestId) return
    showToast(getDisplayErrorMessage(error, 'Không tải được danh sách chi tiết sản phẩm'), 'error')
  } finally {
    if (requestId !== latestLoadRequestId) return
    loading.value = false
  }
}

async function resetFilters() {
  filters.keyword = ''
  filters.mauSacId = null
  filters.kichCoId = null
  filters.trangThai = null

  // Reset hoàn toàn: xóa sản phẩm đang chọn và giayId khỏi route
  // để hiển thị toàn bộ biến thể của tất cả sản phẩm
  if (selectedGiayId.value || selectedProduct.value) {
    selectedProduct.value = null
    suppressGiayIdWatch = true
    await router.replace({ name: 'admin-bien-the-san-pham' })
    suppressGiayIdWatch = false
  }

  loadData(0)
}

function scheduleKeywordSearch() {
  if (keywordSearchTimer) clearTimeout(keywordSearchTimer)
  keywordSearchTimer = setTimeout(() => {
    loadData(0)
    keywordSearchTimer = null
  }, 300)
}

function handleQrScan(code) {
  filters.keyword = code
  showScannerModal.value = false
  loadData(0)
}



function goToForm() {
  if (selectedGiayId.value) {
    router.push({
      name: 'admin-bien-the-san-pham-them',
      query: { giayId: String(selectedGiayId.value) }
    })
    return
  }

  router.push({ name: 'admin-bien-the-san-pham-them' })
}

function handlePageSizeChange(size) {
  pageSize.value = size
  loadData(0)
}

function openImageModal(item) {
  selectedVariant.value = item
  showImageModal.value = true
}

function openEditVariantModal(item) {
  editingVariant.value = item
  bienTheForm.soLuong = Number(item.soLuong || 0)
  bienTheForm.giaGoc = Number(item.giaGoc || 0)
  bienTheForm.giaBan = Number(item.giaBan || 0)
  bienTheForm.kichHoat = Number(item.kichHoat) === 0 ? 0 : 1
  clearBienTheErrors()
  showEditVariantModal.value = true
}

function closeEditVariantModal() {
  showEditVariantModal.value = false
  editingVariant.value = null
  savingVariant.value = false
  clearBienTheErrors()
}

function validateEditVariantForm() {
  clearBienTheErrors()

  const soLuong = parseStock(bienTheForm.soLuong)
  const giaGoc = parsePositiveMoney(bienTheForm.giaGoc)
  const giaBan = parsePositiveMoney(bienTheForm.giaBan)

  if (soLuong == null) {
    bienTheErrors.soLuong = 'Số lượng phải là số nguyên không âm'
  }
  if (giaGoc == null) {
    bienTheErrors.giaGoc = 'Giá gốc phải lớn hơn 0'
  }
  if (giaBan == null) {
    bienTheErrors.giaBan = 'Giá bán phải lớn hơn 0'
  }
  if (giaGoc != null && giaBan != null && giaGoc > giaBan) {
    bienTheErrors.giaGoc = 'Giá gốc không được lớn hơn giá bán'
  }
  if (![1, 0].includes(Number(bienTheForm.kichHoat))) {
    bienTheErrors.kichHoat = 'Trạng thái biến thể không hợp lệ'
  }

  return Object.keys(bienTheErrors).length === 0
}

async function saveEditingVariant() {
  if (!editingVariant.value || savingVariant.value) return
  if (!validateEditVariantForm()) return

  savingVariant.value = true
  try {
    await api.capNhatBienThe(editingVariant.value.id, {
      soLuong: Number(bienTheForm.soLuong),
      giaGoc: Number(bienTheForm.giaGoc),
      giaBan: Number(bienTheForm.giaBan),
      kichHoat: Number(bienTheForm.kichHoat)
    })
    showToast('Cập nhật biến thể thành công')
    const editedGiayId = editingVariant.value.giayId
    closeEditVariantModal()
    await Promise.all([
      loadData(currentPage.value),
      selectedGiayId.value === editedGiayId ? syncSelectedProduct() : Promise.resolve()
    ])
  } catch (error) {
    Object.assign(bienTheErrors, getFieldErrors(error))
    showToast(getDisplayErrorMessage(error, 'Không thể cập nhật biến thể'), 'error')
  } finally {
    savingVariant.value = false
  }
}

function closeImageModal() {
  selectedVariant.value = null
  showImageModal.value = false
}

function openImageModalFromEdit(variant) {
  // Đóng modal form sửa rồi mở modal quản lý ảnh cho biến thể đó
  closeEditVariantModal()
  selectedVariant.value = variant
  showImageModal.value = true
}

function openVariantQr(item) {
  const qrValue = String(item?.sku || item?.maChiTietSanPham || '').trim()
  if (!qrValue) {
    showToast('Chi tiết sản phẩm này chưa có mã để tạo QR', 'error')
    return
  }

  selectedQrItem.value = {
    badge: 'QR chi tiết sản phẩm',
    title: item.tenSanPham || 'Chi tiết sản phẩm',
    subtitle: `${item.maChiTietSanPham || qrValue} • ${item.mauSac || 'Chưa có màu'} / ${item.kichCo || 'Chưa có kích cỡ'}`,
    codeLabel: item.sku ? 'SKU / mã quét' : 'Mã chi tiết sản phẩm',
    value: qrValue,
    note: 'Quét mã này ở bán hàng tại quầy để tìm nhanh đúng biến thể sản phẩm.',
    imageUrl: item.hinhAnh || '',
    imageAlt: item.tenSanPham || item.maChiTietSanPham || 'Ảnh chi tiết sản phẩm',
    detailItems: [
      { label: 'Màu sắc', value: item.mauSac || '—' },
      { label: 'Kích cỡ', value: item.kichCo || '—' },
      { label: 'Số lượng', value: Number(item.soLuong || 0).toLocaleString('vi-VN') },
      { label: 'Giá bán', value: `${formatCurrency(item.giaBan)} đ` },
      { label: 'Trạng thái', value: bienTheTrangThaiLabel(item) },
      { label: 'SKU', value: item.sku || '—' }
    ],
    primaryActionLabel: 'Quản lý ảnh của biến thể',
    actionType: 'manage-images',
    item
  }
  showQrModal.value = true
}

function closeQrModal() {
  showQrModal.value = false
  selectedQrItem.value = null
}

function handleQrPrimaryAction() {
  const actionType = selectedQrItem.value?.actionType
  const targetItem = selectedQrItem.value?.item
  if (actionType === 'manage-images' && targetItem) {
    closeQrModal()
    selectedVariant.value = targetItem
    showImageModal.value = true
  }
}

function handleScannerResult(result) {
  if (result) {
    filters.keyword = result
    showScannerModal.value = false
    loadData(0)
    showToast('Đã tìm thấy mã: ' + result, 'success')
  }
}

function triggerDownloadQr() {
  const selectedIds = tableRef.value?.selectedVariantIds
  if (!selectedIds || selectedIds.size === 0) {
    showToast('Vui lòng chọn ít nhất 1 biến thể để tải mã QR', 'error')
    return
  }
  const selectedItems = items.value.filter(i => selectedIds.has(i.id))
  handleBulkQr(selectedItems)
  selectedIds.clear()
}

function handleBulkQr(selectedItems) {
  if (!selectedItems?.length) return
  const htmlParts = []
  htmlParts.push(`
    <html><head><title>In mã QR (${selectedItems.length} biến thể)</title>
    <style>
      body { font-family: sans-serif; display: flex; flex-wrap: wrap; gap: 20px; padding: 20px; }
      .qr-card { border: 1px solid #ccc; padding: 15px; border-radius: 8px; text-align: center; width: 200px; page-break-inside: avoid; }
      .qr-svg { width: 150px; height: 150px; margin: 0 auto; }
      .qr-svg svg { width: 100%; height: 100%; }
      .title { font-weight: bold; font-size: 14px; margin-top: 10px; }
      .subtitle { font-size: 12px; color: #666; margin-top: 5px; }
      @media print {
        body { padding: 0; }
        .no-print { display: none; }
      }
    </style>
    </head><body>
    <div class="no-print" style="width: 100%; margin-bottom: 20px;">
      <button onclick="window.print()" style="padding: 10px 20px; cursor: pointer; background: #f43f5e; color: white; border: none; border-radius: 6px; font-weight: bold;">In tất cả mã QR</button>
    </div>
  `)

  for (const item of selectedItems) {
    const qrValue = String(item.sku || item.maChiTietSanPham || '').trim()
    if (!qrValue) continue
    try {
      const svg = createQrCodeSvg(qrValue)
      htmlParts.push(`
        <div class="qr-card">
          <div class="qr-svg">${svg}</div>
          <div class="title">${item.tenSanPham || 'Chi tiết sản phẩm'}</div>
          <div class="subtitle">${item.mauSac} / ${item.kichCo}</div>
          <div class="subtitle" style="font-weight:bold">${qrValue}</div>
        </div>
      `)
    } catch (e) {
      console.error(e)
    }
  }
  htmlParts.push(`</body></html>`)
  
  const blob = new Blob([htmlParts.join('')], { type: 'text/html' })
  const url = URL.createObjectURL(blob)
  window.open(url, '_blank')
}

async function toggleBienTheStatus(item) {
  if (updatingStatusIds.has(item.id)) return

  updatingStatusIds.add(item.id)
  try {
    const newTrangThai = Number(item.kichHoat) === 1 ? 0 : 1

    await api.doiTrangThaiBienThe(item.id, newTrangThai)
    item.kichHoat = newTrangThai
    showToast('Cập nhật trạng thái thành công', 'success')
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Cập nhật trạng thái thất bại'), 'error')
  } finally {
    updatingStatusIds.delete(item.id)
  }
}

async function xuatExcel() {
  try {
    const response = await api.layDanhSachChiTietSanPham({
      keyword: filters.keyword.trim() || undefined,
      giayId: selectedGiayId.value,
      mauSacId: filters.mauSacId,
      kichCoId: filters.kichCoId,
      trangThai: filters.trangThai,
      page: 0,
      size: Math.max(totalItems.value, pageSize.value)
    })

    const exported = exportRowsToExcel({
      filename: 'danh-sach-chi-tiet-san-pham',
      sheetName: 'ChiTietSanPham',
      columns: [
        { label: 'STT', value: (_, index) => index + 1 },
        { label: 'Mã SP', key: 'maSanPham' },
        { label: 'Mã CTSP', key: 'maChiTietSanPham' },
        { label: 'Tên sản phẩm', key: 'tenSanPham' },
        { label: 'Thương hiệu', key: 'thuongHieu' },
        { label: 'Loại giày', key: 'loaiGiay' },
        { label: 'Màu sắc', key: 'mauSac' },
        { label: 'Kích cỡ', key: 'kichCo' },
        { label: 'Số lượng', value: (row) => row.soLuong || 0 },
        { label: 'Giá bán', value: (row) => formatCurrency(row.giaBan) },
        { label: 'Trạng thái', value: (row) => bienTheTrangThaiLabel(row) }
      ],
      rows: response.items || []
    })

    showToast(
      exported ? 'Xuất Excel thành công' : 'Không có dữ liệu để xuất Excel',
      exported ? 'success' : 'error'
    )
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không thể xuất Excel chi tiết sản phẩm'), 'error')
  }
}

watch(
  () => route.query.giayId,
  async () => {
    // Bỏ qua nếu route thay đổi do chính ta xóa giayId để mở rộng bộ lọc
    if (suppressGiayIdWatch) return
    await syncSelectedProduct()
    await loadData(0)
  }
)

watch(
  () => filters.keyword,
  () => {
    scheduleKeywordSearch()
  }
)

onMounted(async () => {
  await loadDanhMuc()
  await syncSelectedProduct()
  await loadData(0)
})

onUnmounted(() => {
  closeToast()
  if (keywordSearchTimer) clearTimeout(keywordSearchTimer)
})
</script>

<template>
  <div class="space-y-5">

    <ProductVariantFilters
      :filters="filters"
      :danh-muc="danhMuc"
      :selected-product="selectedProduct"
      :has-selected-variants="hasSelectedVariants"
      @reset-filters="resetFilters"
      @export-excel="xuatExcel"
      @download-qr="triggerDownloadQr"
      @go-to-form="goToForm"
      @load-data="loadData"
      @open-scanner="showScannerModal = true"
    />

    <ProductVariantTable
      ref="tableRef"
      :items="items"
      :loading="loading"
      :current-page="currentPage"
      :page-size="pageSize"
      :total-items="totalItems"
      :total-pages="totalPages"
      :page-size-options="pageSizeOptions"
      :updating-status-ids="updatingStatusIds"
      :focused-chi-tiet-id="focusedChiTietId"
      :hide-pagination="!!selectedGiayId"
      @toggle-status="toggleBienTheStatus"
      @edit-variant="openEditVariantModal"
      @open-qr="openVariantQr"
      @bulk-qr="handleBulkQr"
      @refresh="loadData"
      @selection-changed="hasSelectedVariants = $event"
      @update:current-page="loadData"
      @update:page-size="handlePageSizeChange"
      @open-discount-detail="openDiscountDetail"
    />

    <QuanLySanPhamBienTheFormModal
      :open="showEditVariantModal"
      :editing-bien-the="editingVariant"
      :selected-giay="editingSelectedGiay"
      :danh-muc="danhMuc"
      :bien-the-form="bienTheForm"
      :bien-the-errors="bienTheErrors"
      :bulk-bien-the-form="bulkBienTheForm"
      :bulk-bien-the-errors="bulkBienTheErrors"
      :generated-bulk-bien-thes="generatedBulkBienThes"
      :saving-bien-the="savingVariant"
      @close="closeEditVariantModal"
      @save="saveEditingVariant"
      @open-images="openImageModalFromEdit"
    />

    <AdminQrCodeModal
      :open="showQrModal"
      v-bind="selectedQrItem"
      @close="closeQrModal"
      @primary-action="handleQrPrimaryAction"
    />

    <QuanLySanPhamHinhAnhModal
      :open="showImageModal"
      :variant="selectedVariant"
      @close="closeImageModal"
      @updated="loadData(currentPage)"
      @error="showToast($event, 'error')"
    />

    <BanHangQrScannerModal
      :is-open="showScannerModal"
      :is-admin="true"
      @close="showScannerModal = false"
      @scan="handleScannerResult"
    />

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="toast.show && toast.type !== 'success'"
          class="fixed right-4 top-[88px] z-[100] w-[min(92vw,380px)] rounded-3xl border bg-white px-4 py-4 shadow-[0_20px_45px_rgba(15,23,42,0.12)]"
          :class="toast.type === 'error' ? 'border-rose-100' : 'border-slate-100'"
        >
          <div class="flex items-start gap-3">
            <div
              class="mt-0.5 rounded-2xl p-2"
              :class="toast.type === 'error' ? 'bg-rose-50 text-rose-600' : 'bg-slate-100 text-slate-600'"
            >
              <TriangleAlert v-if="toast.type === 'error'" class="h-5 w-5" />
              <CircleCheckBig v-else class="h-5 w-5" />
            </div>

            <div class="min-w-0 flex-1">
              <p class="text-sm font-medium text-slate-800">
                {{ toastTitle }}
              </p>
              <p class="mt-1 text-sm text-slate-500">
                {{ toast.message }}
              </p>
            </div>

            <button
              type="button"
              class="rounded-full p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
              @click="closeToast"
            >
              <X class="h-4 w-4" />
            </button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>

