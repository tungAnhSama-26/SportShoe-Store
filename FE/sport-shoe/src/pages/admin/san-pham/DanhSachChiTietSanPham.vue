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
import { exportRowsToExcel } from '../../../utils/export-excel'
import { getDisplayErrorMessage } from '../../../utils/error-message'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const items = ref([])
const danhMuc = ref(null)
const currentPage = ref(0)
const pageSize = ref(10)
const totalItems = ref(0)
const totalPages = ref(0)
const selectedProduct = ref(null)
const updatingStatusIds = reactive(new Set())
const showQrModal = ref(false)
const selectedQrItem = ref(null)

const showScannerModal = ref(false)

const filters = reactive({
  keyword: '',
  mauSacId: null,
  kichCoId: null,
  trangThai: null
})

const showImageModal = ref(false)
const selectedVariant = ref(null)

const toast = reactive({
  show: false,
  message: '',
  type: 'success'
})

const pageSizeOptions = [5, 10, 20, 50]
let toastTimer = null
let latestLoadRequestId = 0
let keywordSearchTimer = null

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

function showToast(message, type = 'success') {
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
  if (Number(item.soLuong || 0) <= 0) return 'Hết hàng'
  return Number(item.kichHoat) === 1 ? 'Đang bán' : 'Ngừng bán'
}

function bienTheTrangThaiClass(item) {
  if (Number(item.soLuong || 0) <= 0) return 'bg-amber-50 text-amber-600'
  return Number(item.kichHoat) === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-600'
}

function nextBienTheStatus(item) {
  return Number(item.kichHoat) === 1 ? 2 : 1
}

function quickToggleLabel(item) {
  if (Number(item.kichHoat) === 1) return 'Chuyển sang ngừng bán'
  return 'Chuyển sang đang bán'
}

function canToggleStatus(item) {
  return Number(item.kichHoat) === 1 || Number(item.soLuong || 0) > 0
}

function quickToggleIntent(item) {
  return Number(item.kichHoat) === 1 ? 'deactivate' : 'activate'
}

function quickToggleDisabledTitle(item) {
  return canToggleStatus(item) ? quickToggleLabel(item) : 'Hết hàng chưa thể chuyển sang đang bán'
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
  loading.value = true
  try {
    const response = await api.layDanhSachChiTietSanPham({
      keyword: filters.keyword.trim() || undefined,
      giayId: selectedGiayId.value,
      mauSacId: filters.mauSacId,
      kichCoId: filters.kichCoId,
      trangThai: filters.trangThai,
      page,
      size: pageSize.value
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

function resetFilters() {
  filters.keyword = ''
  filters.mauSacId = null
  filters.kichCoId = null
  filters.trangThai = null
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

function clearProductFilter() {
  router.replace({ name: 'admin-bien-the-san-pham' })
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

function closeImageModal() {
  selectedVariant.value = null
  showImageModal.value = false
}

function closeQrModal() {
  showQrModal.value = false
  selectedQrItem.value = null
}

function handleQrPrimaryAction() {
  const actionType = selectedQrItem.value?.actionType
  const targetItem = selectedQrItem.value?.item

  closeQrModal()

  if (actionType === 'manage-images' && targetItem) {
    openImageModal(targetItem)
  }
}

async function xuatExcel() {
  if (!totalItems.value) {
    showToast('Không có dữ liệu để xuất Excel', 'error')
    return
  }

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

async function toggleBienTheStatus(item) {
  if (isUpdatingStatus(item.id)) return
  if (!canToggleStatus(item)) {
    showToast('Không thể chuyển CTSP sang đang bán khi số lượng tồn bằng 0', 'error')
    return
  }

  updatingStatusIds.add(item.id)
  try {
    await api.doiTrangThaiBienThe(item.id, nextBienTheStatus(item))
    showToast('Cập nhật trạng thái CTSP thành công')
    await Promise.all([
      loadData(currentPage.value),
      selectedGiayId.value === item.giayId ? syncSelectedProduct() : Promise.resolve()
    ])
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không thể cập nhật trạng thái chi tiết sản phẩm'), 'error')
  } finally {
    updatingStatusIds.delete(item.id)
  }
}

watch(
  () => route.query.giayId,
  async () => {
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
    <section class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
      <h1 class="admin-page-title text-[30px]">Quản lý sản phẩm</h1>

      <button v-if="selectedProduct" type="button" class="admin-btn-soft" @click="clearProductFilter">
        <X class="h-4 w-4" />
        Bỏ lọc sản phẩm
      </button>
    </section>

    <ProductVariantFilters
      :filters="filters"
      :danh-muc="danhMuc"
      :selected-product="selectedProduct"
      @reset-filters="resetFilters"
      @export-excel="xuatExcel"
      @go-to-form="goToForm"
      @load-data="loadData"
      @clear-product-filter="clearProductFilter"
      @open-scanner="showScannerModal = true"
    />

    <ProductVariantTable
      :items="items"
      :loading="loading"
      :current-page="currentPage"
      :page-size="pageSize"
      :total-items="totalItems"
      :total-pages="totalPages"
      :page-size-options="pageSizeOptions"
      :updating-status-ids="updatingStatusIds"
      :focused-chi-tiet-id="focusedChiTietId"
      @toggle-status="toggleBienTheStatus"
      @open-qr="openVariantQr"
      @refresh="loadData"
      @update:current-page="loadData"
      @update:page-size="handlePageSizeChange"
      @open-discount-detail="openDiscountDetail"
    />

    <AdminQrCodeModal
      :open="showQrModal && !!selectedQrItem"
      :badge="selectedQrItem?.badge"
      :title="selectedQrItem?.title"
      :subtitle="selectedQrItem?.subtitle"
      :code-label="selectedQrItem?.codeLabel"
      :value="selectedQrItem?.value"
      :note="selectedQrItem?.note"
      :image-url="selectedQrItem?.imageUrl"
      :image-alt="selectedQrItem?.imageAlt"
      :detail-items="selectedQrItem?.detailItems"
      :primary-action-label="selectedQrItem?.primaryActionLabel"
      @close="closeQrModal"
      @primary-action="handleQrPrimaryAction"
    />

    <BanHangQrScannerModal
      :open="showScannerModal"
      @close="showScannerModal = false"
      @scan="handleQrScan"
    />

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showImageModal && selectedVariant"
          class="fixed inset-0 z-[90] flex items-center justify-center bg-black/55 p-4"
          @click.self="closeImageModal"
        >
          <div class="flex max-h-[90vh] w-full max-w-5xl flex-col overflow-hidden rounded-[28px] bg-white shadow-2xl">
            <div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5">
              <div>
                <p class="text-sm font-semibold uppercase tracking-[0.18em] text-rose-500">Biến thể sản phẩm</p>
                <h2 class="mt-2 text-2xl font-black text-slate-900">{{ selectedVariant.tenSanPham }}</h2>
                <p class="mt-1 text-sm text-slate-500">
                  {{ selectedVariant.maChiTietSanPham }} • {{ selectedVariant.mauSac }} / {{ selectedVariant.kichCo }}
                </p>
              </div>

              <button
                type="button"
                class="rounded-2xl p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-700"
                @click="closeImageModal"
              >
                <X class="h-4 w-4" />
              </button>
            </div>

            <div class="overflow-y-auto p-6">
              <BienTheImageManager
                :variant="selectedVariant"
                @updated="loadData(currentPage)"
                @error="showToast($event, 'error')"
              />
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="toast.show"
          class="fixed right-4 top-[88px] z-[100] w-[min(92vw,380px)] rounded-3xl border bg-white px-4 py-4 shadow-[0_20px_45px_rgba(15,23,42,0.12)]"
          :class="toast.type === 'error' ? 'border-rose-100' : 'border-emerald-100'"
        >
          <div class="flex items-start gap-3">
            <div
              class="mt-0.5 rounded-2xl p-2"
              :class="toast.type === 'error' ? 'bg-rose-50 text-rose-600' : 'bg-emerald-50 text-emerald-600'"
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

