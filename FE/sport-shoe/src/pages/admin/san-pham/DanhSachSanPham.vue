<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Eye, FileSpreadsheet, Filter, Package, Plus, RotateCcw, Search } from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'
import AdminQrCodeModal from '../../../components/common/AdminQrCodeModal.vue'
import AdminQuickStatusAction from '../../../components/common/AdminQuickStatusAction.vue'
import AdminTableFooter from '../../../components/common/AdminTableFooter.vue'
import { exportRowsToExcel } from '../../../utils/export-excel'
import { getDisplayErrorMessage } from '../../../utils/error-message'

const router = useRouter()

const loading = ref(false)
const items = ref([])
const danhMuc = ref(null)
const currentPage = ref(0)
const pageSize = ref(10)
const totalItems = ref(0)
const totalPages = ref(0)
const updatingStatusIds = reactive(new Set())
const showQrModal = ref(false)
const selectedQrItem = ref(null)

const filters = reactive({
  keyword: '',
  thuongHieuId: null,
  loaiGiayId: null,
  trangThai: null
})

const toast = reactive({
  show: false,
  message: '',
  type: 'success'
})

const pageSizeOptions = [5, 10, 20, 50]
let toastTimer = null
let latestLoadRequestId = 0
let keywordSearchTimer = null

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

function formatCurrency(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function giaHienThi(item) {
  if (item.giaMin == null && item.giaMax == null) return 'Chưa có giá'
  if (item.giaMin === item.giaMax || item.giaMax == null) return `${formatCurrency(item.giaMin)} đ`
  return `${formatCurrency(item.giaMin)} đ - ${formatCurrency(item.giaMax)} đ`
}

function trangThaiLabel(value) {
  if (value === 1) return 'Kinh doanh'
  if (value === 2) return 'Hết hàng'
  return 'Ngừng kinh doanh'
}

function trangThaiClass(value) {
  if (value === 1) return 'bg-emerald-50 text-emerald-600'
  if (value === 2) return 'bg-amber-50 text-amber-600'
  return 'bg-rose-50 text-rose-600'
}

function nextProductStatus(item) {
  return Number(item.trangThai) === 0 ? 1 : 0
}

function canQuickToggleProduct(item) {
  return Number(item.trangThai) !== 0 || Number(item.tongSoLuong || 0) > 0
}

function isUpdatingStatus(id) {
  return updatingStatusIds.has(id)
}

function productQuickToggleLabel(item) {
  return Number(item.trangThai) === 0 ? 'Chuyển sang kinh doanh' : 'Chuyển sang ngừng kinh doanh'
}

function productQuickToggleIntent(item) {
  return Number(item.trangThai) === 0 ? 'activate' : 'deactivate'
}

function productQuickToggleDisabledTitle(item) {
  return canQuickToggleProduct(item)
    ? productQuickToggleLabel(item)
    : 'Hết hàng chưa thể chuyển sang kinh doanh'
}

function productQuickToggleConfirmMessage(item) {
  const action = nextProductStatus(item) === 1 ? 'kinh doanh' : 'ngừng kinh doanh'
  return `Bạn có muốn chuyển sản phẩm "${item.ten}" sang ${action} không?`
}

async function loadDanhMuc() {
  try {
    danhMuc.value = await api.layDanhMuc()
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không tải được danh mục sản phẩm'), 'error')
  }
}

async function loadData(page = 0) {
  const requestId = ++latestLoadRequestId
  loading.value = true
  try {
    const response = await api.layDanhSachGiay({
      keyword: filters.keyword.trim() || undefined,
      thuongHieuId: filters.thuongHieuId,
      loaiGiayId: filters.loaiGiayId,
      trangThai: filters.trangThai,
      page,
      size: pageSize.value
    })
    if (requestId !== latestLoadRequestId) return
    items.value = response.items || []
    totalItems.value = response.totalItems
    totalPages.value = response.totalPages
    currentPage.value = response.page
  } catch (error) {
    if (requestId !== latestLoadRequestId) return
    showToast(getDisplayErrorMessage(error, 'Không tải được danh sách sản phẩm'), 'error')
  } finally {
    if (requestId !== latestLoadRequestId) return
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.thuongHieuId = null
  filters.loaiGiayId = null
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

function goToForm() {
  router.push({ name: 'admin-san-pham-them' })
}

function goToChiTietList(item) {
  router.push({
    name: 'admin-bien-the-san-pham',
    query: { giayId: String(item.id) }
  })
}

function openProductQr(item) {
  const qrValue = String(item?.ma || '').trim()
  if (!qrValue) {
    showToast('Sản phẩm này chưa có mã để tạo QR', 'error')
    return
  }

  selectedQrItem.value = {
    badge: 'QR sản phẩm',
    title: item.ten || 'Sản phẩm',
    subtitle: `${item.ma}${item.thuongHieu ? ` • ${item.thuongHieu}` : ''}`,
    codeLabel: 'Mã sản phẩm',
    value: qrValue,
    note: 'Quét mã này ở bán hàng tại quầy để tìm nhanh sản phẩm theo mã sản phẩm.',
    imageUrl: item.hinhAnh || '',
    imageAlt: item.ten || item.ma || 'Ảnh sản phẩm',
    detailItems: [
      { label: 'Thương hiệu', value: item.thuongHieu || '—' },
      { label: 'Loại giày', value: item.loaiGiay || '—' },
      { label: 'Số lượng', value: Number(item.tongSoLuong || 0).toLocaleString('vi-VN') },
      { label: 'Giá bán', value: giaHienThi(item) },
      { label: 'Trạng thái', value: trangThaiLabel(item.trangThai) }
    ],
    primaryActionLabel: 'Xem danh sách biến thể',
    actionType: 'go-to-variants',
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

  closeQrModal()

  if (actionType === 'go-to-variants' && targetItem) {
    goToChiTietList(targetItem)
  }
}

async function handleToggleStatus(item) {
  if (isUpdatingStatus(item.id)) return
  if (!canQuickToggleProduct(item)) {
    showToast('Sản phẩm hết hàng chưa thể chuyển sang kinh doanh', 'error')
    return
  }
  updatingStatusIds.add(item.id)
  try {
    await api.doiTrangThai(item.id, nextProductStatus(item))
    showToast('Cập nhật trạng thái sản phẩm thành công')
    await loadData(currentPage.value)
  } catch (error) {
    showToast(getDisplayErrorMessage(error, 'Không thể cập nhật trạng thái sản phẩm'), 'error')
  } finally {
    updatingStatusIds.delete(item.id)
  }
}

function handlePageSizeChange(size) {
  pageSize.value = size
  loadData(0)
}

async function xuatExcel() {
  if (!totalItems.value) {
    showToast('Không có dữ liệu để xuất Excel', 'error')
    return
  }

  try {
    const response = await api.layDanhSachGiay({
      keyword: filters.keyword.trim() || undefined,
      thuongHieuId: filters.thuongHieuId,
      loaiGiayId: filters.loaiGiayId,
      trangThai: filters.trangThai,
      page: 0,
      size: Math.max(totalItems.value, pageSize.value)
    })

    const exported = exportRowsToExcel({
      filename: 'danh-sach-san-pham',
      sheetName: 'SanPham',
      columns: [
        { label: 'STT', value: (_, index) => index + 1 },
        { label: 'Mã sản phẩm', key: 'ma' },
        { label: 'Tên sản phẩm', key: 'ten' },
        { label: 'Thương hiệu', key: 'thuongHieu' },
        { label: 'Loại giày', key: 'loaiGiay' },
        { label: 'Tổng tồn', value: (row) => row.tongSoLuong || 0 },
        { label: 'Giá bán', value: (row) => giaHienThi(row) },
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

function applyStatusFilter(value) {
  filters.trangThai = value
  loadData(0)
}

onMounted(async () => {
  await loadDanhMuc()
  await loadData(0)
})

watch(
  () => filters.keyword,
  () => {
    scheduleKeywordSearch()
  }
)

onUnmounted(() => {
  if (toastTimer) clearTimeout(toastTimer)
  if (keywordSearchTimer) clearTimeout(keywordSearchTimer)
})
</script>

<template>
  <div class="space-y-5">
    <section>
      <h1 class="admin-page-title text-[30px]">Quản lý sản phẩm</h1>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="admin-section-title">Bộ lọc</h2>
        </div>
      </div>

      <div class="flex flex-col gap-4">
        <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
          <label class="min-w-0 flex-1 space-y-2">
            <span class="admin-filter-label mb-1">Tìm kiếm</span>
            <div class="relative max-w-3xl">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                v-model="filters.keyword"
                type="text"
                placeholder="Tìm theo mã / tên sản phẩm..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                @keyup.enter="loadData(0)"
              />
            </div>
          </label>

          <div class="flex flex-wrap items-center gap-3 xl:justify-end">
            <button type="button" class="admin-btn-soft" @click="resetFilters">
              <RotateCcw class="h-4 w-4" />
              Đặt lại
            </button>
            <button type="button" class="admin-btn-soft" @click="xuatExcel">
              <FileSpreadsheet class="h-4 w-4" />
              Xuất Excel
            </button>
            <button type="button" class="admin-btn-primary" @click="goToForm">
              <Plus class="h-4 w-4" />
              Thêm sản phẩm chi tiết
            </button>
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2 xl:max-w-5xl xl:grid-cols-3">
          <label class="space-y-2">
            <span class="admin-filter-label mb-1">Thương hiệu</span>
            <select
              v-model.number="filters.thuongHieuId"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              @change="loadData(0)"
            >
              <option :value="null">Tất cả thương hiệu</option>
              <option v-for="item in danhMuc?.thuongHieu || []" :key="item.id" :value="item.id">
                {{ item.ten }}
              </option>
            </select>
          </label>

          <label class="space-y-2">
            <span class="admin-filter-label mb-1">Loại giày</span>
            <select
              v-model.number="filters.loaiGiayId"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              @change="loadData(0)"
            >
              <option :value="null">Tất cả loại giày</option>
              <option v-for="item in danhMuc?.loaiGiay || []" :key="item.id" :value="item.id">
                {{ item.ten }}
              </option>
            </select>
          </label>
          <label class="space-y-2">
            <span class="admin-filter-label mb-1">Trạng thái</span>
            <select
              v-model.number="filters.trangThai"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              @change="loadData(0)"
            >
              <option :value="null">Tất cả trạng thái</option>
              <option :value="1">Kinh doanh</option>
              <option :value="2">Hết hàng</option>
              <option :value="0">Ngừng kinh doanh</option>
            </select>
          </label>
        </div>
      </div>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#B82220]/5 text-[#B82220]">
          <Package class="h-5 w-5" />
        </div>
        <div>
          <h2 class="admin-section-title">Danh sách sản phẩm</h2>
        </div>
      </div>

      <div class="overflow-hidden rounded-[24px] border border-slate-100">
        <table class="w-full table-fixed border-separate border-spacing-0 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-950">
              <th class="w-16 rounded-tl-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="w-28 bg-slate-100 px-4 py-3">Mã SP</th>
              <th class="bg-slate-100 px-4 py-3">Tên SP</th>
              <th class="w-44 bg-slate-100 px-4 py-3">Thương hiệu</th>
              <th class="w-24 bg-slate-100 px-4 py-3 text-center">Tồn</th>
              <th class="w-44 bg-slate-100 px-4 py-3">Giá bán</th>
              <th class="w-36 bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="w-28 rounded-tr-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="!items.length">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">Chưa có sản phẩm nào</td>
            </tr>
            <tr
              v-for="(item, index) in items"
              :key="item.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
            >
              <td class="rounded-l-2xl px-4 py-4 align-top font-semibold text-slate-500">
                {{ currentPage * pageSize + index + 1 }}
              </td>
              <td class="px-4 py-4 align-top font-semibold text-slate-800">{{ item.ma }}</td>
              <td class="px-4 py-4 align-top max-w-[340px] whitespace-normal break-normal">
                <p class="text-xs font-semibold leading-tight text-slate-800" :title="item.ten">{{ item.ten }}</p>
              </td>
              <td class="px-4 py-4 align-top">
                <p class="break-words text-sm font-semibold text-slate-800">{{ item.thuongHieu || '—' }}</p>
              </td>
              <td class="px-4 py-4 text-center font-semibold text-slate-700">
                {{ Number(item.tongSoLuong || 0).toLocaleString('vi-VN') }}
              </td>
              <td class="px-4 py-4 font-semibold text-slate-800">
                <div class="flex flex-col gap-1">
                  <template v-if="item.giaMin != null && item.giaMax != null && item.giaMin !== item.giaMax">
                    <span class="whitespace-nowrap">{{ formatCurrency(item.giaMin) }} đ</span>
                    <span class="whitespace-nowrap">- {{ formatCurrency(item.giaMax) }} đ</span>
                  </template>
                  <template v-else>
                    <span class="whitespace-nowrap">{{ giaHienThi(item) }}</span>
                    <span class="text-transparent">-</span>
                  </template>
                </div>
              </td>
              <td class="px-4 py-4">
                <span class="inline-flex min-w-max whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold" :class="trangThaiClass(item.trangThai)">
                  {{ trangThaiLabel(item.trangThai) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-4 text-center">
                <div class="flex items-center justify-center gap-1">
                  <AdminQuickStatusAction
                    :loading="isUpdatingStatus(item.id)"
                    :disabled="isUpdatingStatus(item.id) || !canQuickToggleProduct(item)"
                    :action-label="productQuickToggleLabel(item)"
                    :disabled-title="productQuickToggleDisabledTitle(item)"
                    :confirm-message="productQuickToggleConfirmMessage(item)"
                    :intent="productQuickToggleIntent(item)"
                    @toggle="handleToggleStatus(item)"
                  />
                  <button
                    type="button"
                    class="admin-table-action text-slate-600 hover:text-rose-500"
                    title="Xem QR và thông tin sản phẩm"
                    @click="openProductQr(item)"
                  >
                    <Eye class="h-4 w-4" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminTableFooter
        :current-page="currentPage"
        :page-size="pageSize"
        :page-size-options="pageSizeOptions"
        :total-items="totalItems"
        :total-pages="totalPages"
        zero-based
        compact
        show-refresh
        @refresh="loadData(currentPage)"
        @update:current-page="loadData"
        @update:page-size="handlePageSizeChange"
      />
    </section>

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

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="toast.show"
          class="fixed right-5 top-5 z-[90] rounded-2xl px-4 py-3 text-sm font-medium text-white shadow-lg"
          :class="toast.type === 'error' ? 'bg-rose-500' : 'bg-emerald-500'"
        >
          {{ toast.message }}
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
