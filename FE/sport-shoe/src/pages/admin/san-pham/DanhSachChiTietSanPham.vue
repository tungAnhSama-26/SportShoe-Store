<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Eye, FileSpreadsheet, Filter, Images, Layers3, Plus, RotateCcw, Search, X } from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'
import AdminTableFooter from '../../../components/common/AdminTableFooter.vue'
import BienTheImageManager from '../../../components/admin/san-pham/BienTheImageManager.vue'
import { exportRowsToExcel } from '../../../utils/export-excel'

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

const selectedGiayId = computed(() => {
  const raw = Array.isArray(route.query.giayId) ? route.query.giayId[0] : route.query.giayId
  const parsed = Number(raw)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
})

function showToast(message, type = 'success') {
  if (toastTimer) clearTimeout(toastTimer)
  toast.message = message
  toast.type = type
  toast.show = true
  toastTimer = setTimeout(() => {
    toast.show = false
  }, 3000)
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function bienTheTrangThaiLabel(item) {
  if (Number(item.soLuong || 0) <= 0) return 'Hết hàng'
  return Number(item.kichHoat) === 1 ? 'Kinh doanh' : 'Tạm dừng'
}

function bienTheTrangThaiClass(item) {
  if (Number(item.soLuong || 0) <= 0) return 'bg-amber-50 text-amber-600'
  return Number(item.kichHoat) === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-600'
}

async function loadDanhMuc() {
  try {
    danhMuc.value = await api.layDanhMuc()
  } catch (error) {
    showToast(error.message || 'Không tải được danh mục', 'error')
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
    showToast(error.message || 'Không tải được sản phẩm đã chọn', 'error')
  }
}

async function loadData(page = 0) {
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
    items.value = response.items || []
    currentPage.value = response.page
    totalItems.value = response.totalItems
    totalPages.value = response.totalPages
  } catch (error) {
    showToast(error.message || 'Không tải được danh sách chi tiết sản phẩm', 'error')
  } finally {
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

function clearProductFilter() {
  router.replace({ name: 'admin-bien-the-san-pham' })
}

function goToForm() {
  router.push({
    name: 'admin-chi-tiet-san-pham-new',
    query: selectedGiayId.value ? { giayId: String(selectedGiayId.value) } : undefined
  })
}

function handlePageSizeChange(size) {
  pageSize.value = size
  loadData(0)
}

function openImageModal(item) {
  selectedVariant.value = item
  showImageModal.value = true
}

function closeImageModal() {
  selectedVariant.value = null
  showImageModal.value = false
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
        { label: 'SKU', key: 'sku' },
        { label: 'Tên sản phẩm', key: 'tenSanPham' },
        { label: 'Thương hiệu', key: 'thuongHieu' },
        { label: 'Loại giày', key: 'loaiGiay' },
        { label: 'Màu sắc', key: 'mauSac' },
        { label: 'Kích cỡ', key: 'kichCo' },
        { label: 'SL tồn', value: (row) => row.soLuong || 0 },
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
    showToast(error.message || 'Xuất Excel thất bại', 'error')
  }
}

function applyStatusFilter(value) {
  filters.trangThai = value
  loadData(0)
}

watch(
  () => route.query.giayId,
  async () => {
    await syncSelectedProduct()
    await loadData(0)
  }
)

onMounted(async () => {
  await loadDanhMuc()
  await syncSelectedProduct()
  await loadData(0)
})
</script>

<template>
  <div class="space-y-5">
    <section class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">Quản lý sản phẩm</h1>

      <button v-if="selectedProduct" type="button" class="admin-btn-soft" @click="clearProductFilter">
        <X class="h-4 w-4" />
        Bỏ lọc sản phẩm
      </button>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
        </div>
      </div>

      <div class="flex flex-col gap-4">
        <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
          <label class="min-w-0 flex-1 space-y-2">
            <span class="mb-1 block text-[13px] font-semibold text-slate-500">Tìm kiếm</span>
            <div class="relative max-w-3xl">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                v-model="filters.keyword"
                type="text"
                placeholder="Tìm theo mã SP / mã CTSP / tên sản phẩm..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
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
            <span class="mb-1 text-[13px] font-semibold text-slate-500">Màu sắc</span>
            <select
              v-model.number="filters.mauSacId"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
              @change="loadData(0)"
            >
              <option :value="null">Tất cả màu sắc</option>
              <option v-for="item in danhMuc?.mauSac || []" :key="item.id" :value="item.id">
                {{ item.ten }}
              </option>
            </select>
          </label>

          <label class="space-y-2">
            <span class="mb-1 text-[13px] font-semibold text-slate-500">Kích cỡ</span>
            <select
              v-model.number="filters.kichCoId"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
              @change="loadData(0)"
            >
              <option :value="null">Tất cả kích cỡ</option>
              <option v-for="item in danhMuc?.kichCo || []" :key="item.id" :value="item.id">
                {{ item.giaTri }}
              </option>
            </select>
          </label>
          <label class="space-y-2">
            <span class="mb-1 text-[13px] font-semibold text-slate-500">Trạng thái</span>
            <select
              v-model.number="filters.trangThai"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
              @change="loadData(0)"
            >
              <option :value="null">Tất cả trạng thái</option>
              <option :value="1">Kinh doanh</option>
              <option :value="2">Tạm dừng / Hết hàng</option>
            </select>
          </label>
        </div>
      </div>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#B82220]/5 text-[#B82220]">
          <Layers3 class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Danh sách chi tiết sản phẩm</h2>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-[1180px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã SP</th>
              <th class="bg-slate-100 px-4 py-3">Mã CTSP</th>
              <th class="bg-slate-100 px-4 py-3">Ảnh</th>
              <th class="bg-slate-100 px-4 py-3">Tên sản phẩm</th>
              <th class="bg-slate-100 px-4 py-3">Màu sắc</th>
              <th class="bg-slate-100 px-4 py-3">Kích cỡ</th>
              <th class="bg-slate-100 px-4 py-3">Loại giày</th>
              <th class="bg-slate-100 px-4 py-3">SL tồn</th>
              <th class="bg-slate-100 px-4 py-3">Giá bán</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="12" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="!items.length">
              <td colspan="12" class="py-10 text-center text-sm text-slate-400">Chưa có chi tiết sản phẩm nào</td>
            </tr>
            <tr
              v-for="(item, index) in items"
              :key="item.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
            >
              <td class="rounded-l-2xl px-4 py-4 font-semibold text-slate-500">
                {{ currentPage * pageSize + index + 1 }}
              </td>
<<<<<<< Updated upstream
              <td class="px-4 py-4 font-semibold text-slate-800">{{ item.maSanPham }}</td>
              <td class="px-4 py-4 font-semibold text-slate-700">{{ item.maChiTietSanPham }}</td>
=======
              <td class="px-4 py-4 font-bold text-slate-950 whitespace-nowrap">{{ item.maSanPham }}</td>
              <td class="px-4 py-4 font-bold text-slate-900 whitespace-nowrap">{{ item.maChiTietSanPham }}</td>
>>>>>>> Stashed changes
              <td class="px-4 py-4">
                <div class="flex h-14 w-14 items-center justify-center overflow-hidden rounded-2xl bg-slate-100">
                  <img v-if="item.hinhAnh" :src="item.hinhAnh" alt="" class="h-full w-full object-cover" />
                  <Images class="h-4 w-4 text-slate-300" v-else />
                </div>
              </td>
              <td class="px-4 py-4">
                <p class="font-semibold text-slate-800">{{ item.tenSanPham }}</p>
                <p class="mt-1 text-xs text-slate-400">{{ item.sku }}</p>
              </td>
              <td class="px-4 py-4">
                <div class="inline-flex items-center gap-2 rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700">
                  <span
                    class="h-2.5 w-2.5 rounded-full border border-black/5"
                    :style="{ backgroundColor: item.maMauHex || '#e2e8f0' }"
                  ></span>
                  {{ item.mauSac }}
                </div>
              </td>
<<<<<<< Updated upstream
              <td class="px-4 py-4 font-semibold text-slate-700">{{ item.kichCo }}</td>
              <td class="px-4 py-4 text-slate-600">{{ item.loaiGiay || '—' }}</td>
              <td class="px-4 py-4 font-semibold text-slate-700">
=======
              <td class="px-4 py-4 font-bold text-slate-900 whitespace-nowrap">{{ item.kichCo }}</td>
              <td class="px-4 py-4 text-slate-800 whitespace-nowrap">{{ item.loaiGiay || '—' }}</td>
              <td class="px-4 py-4 font-bold text-slate-900 whitespace-nowrap">
>>>>>>> Stashed changes
                {{ Number(item.soLuong || 0).toLocaleString('vi-VN') }}
              </td>
              <td class="px-4 py-4 font-semibold text-slate-800">{{ formatCurrency(item.giaBan) }} đ</td>
              <td class="px-4 py-4">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="bienTheTrangThaiClass(item)">
                  {{ bienTheTrangThaiLabel(item) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-4 text-center">
                <button
                  type="button"
                  class="admin-table-action text-slate-600 hover:text-rose-500"
                  title="Quản lý ảnh"
                  @click="openImageModal(item)"
                >
                  <Eye class="h-4 w-4" />
                </button>
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
          class="fixed right-5 top-5 z-[100] rounded-2xl px-4 py-3 text-sm font-medium text-white shadow-lg"
          :class="
            toast.type === 'error'
              ? 'bg-rose-500'
              : toast.type === 'info'
                ? 'bg-slate-900'
                : 'bg-emerald-500'
          "
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
