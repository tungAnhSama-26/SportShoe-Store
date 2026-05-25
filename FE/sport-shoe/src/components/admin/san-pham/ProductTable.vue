<script setup>
import { Eye, Package } from 'lucide-vue-next'
import AdminQuickStatusAction from '../../../components/common/AdminQuickStatusAction.vue'
import AdminTableFooter from '../../../components/common/AdminTableFooter.vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  currentPage: {
    type: Number,
    default: 0
  },
  pageSize: {
    type: Number,
    default: 10
  },
  totalItems: {
    type: Number,
    default: 0
  },
  totalPages: {
    type: Number,
    default: 0
  },
  pageSizeOptions: {
    type: Array,
    default: () => [5, 10, 20, 50]
  },
  updatingStatusIds: {
    type: Set,
    default: () => new Set()
  }
})

const emit = defineEmits([
  'toggle-status',
  'open-qr',
  'refresh',
  'update:current-page',
  'update:page-size'
])

function isUpdatingStatus(id) {
  return props.updatingStatusIds.has(id)
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function formatPriceRange(minValue, maxValue) {
  if (minValue == null && maxValue == null) return 'Chưa có giá'
  if (minValue === maxValue || maxValue == null) return `${formatCurrency(minValue)} đ`
  return `${formatCurrency(minValue)} đ - ${formatCurrency(maxValue)} đ`
}

function formatPriceParts(minValue, maxValue) {
  if (minValue == null && maxValue == null) {
    return {
      start: 'Chưa có giá',
      end: '',
      isRange: false
    }
  }

  const startText = `${formatCurrency(minValue ?? maxValue)} đ`
  const endText = `${formatCurrency(maxValue ?? minValue)} đ`

  if (minValue === maxValue || maxValue == null) {
    return {
      start: startText,
      end: '',
      isRange: false
    }
  }

  return {
    start: startText,
    end: endText,
    isRange: true
  }
}

function giaHienThi(item) {
  return formatPriceRange(item.giaMin, item.giaMax)
}

function giaGocHienThi(item) {
  return formatPriceRange(item.giaGocMin, item.giaGocMax)
}

function giaTrongBang(item) {
  return formatPriceParts(item.giaMin, item.giaMax)
}

function giaGocTrongBang(item) {
  return formatPriceParts(item.giaGocMin, item.giaGocMax)
}

function hasOriginalPrice(item) {
  if (!item?.coGiamGia) return false
  if (item.giaGocMin == null && item.giaGocMax == null) return false

  const currentMin = Number(item.giaMin ?? 0)
  const currentMax = Number(item.giaMax ?? item.giaMin ?? 0)
  const originalMin = Number(item.giaGocMin ?? 0)
  const originalMax = Number(item.giaGocMax ?? item.giaGocMin ?? 0)

  return originalMin !== currentMin || originalMax !== currentMax
}

function trangThaiLabel(value) {
  if (value === 1) return 'Kinh doanh'
  if (value === 2) return 'Hết hàng'
  return 'Ngừng bán'
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
  const action = Number(item.trangThai) === 0 ? 'kinh doanh' : 'ngừng kinh doanh'
  const target = item.ma || item.ten || `#${item.id}`
  return `Bạn có muốn chuyển sản phẩm "${target}" sang ${action} không?`
}

function handlePageSizeChange(size) {
  emit('update:page-size', size)
}
</script>

<template>
  <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
    <div class="mb-5 flex items-center gap-3">
      <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#B82220]/5 text-[#B82220]">
        <Package class="h-5 w-5" />
      </div>
      <div>
        <h2 class="admin-section-title">Danh sách sản phẩm</h2>
      </div>
    </div>

    <div class="admin-table-scroll rounded-[24px] border border-slate-100">
      <table class="min-w-[980px] w-full table-auto border-separate border-spacing-0 text-sm">
        <thead>
          <tr class="text-left text-sm font-bold text-slate-950">
            <th class="w-14 rounded-tl-2xl bg-slate-100 px-3 py-3 whitespace-nowrap">STT</th>
            <th class="w-28 bg-slate-100 px-3 py-3 whitespace-nowrap">Mã SP</th>
            <th class="bg-slate-100 px-3 py-3 whitespace-nowrap">Tên SP</th>
            <th class="w-32 bg-slate-100 px-3 py-3 whitespace-nowrap">Thương hiệu</th>
            <th class="w-24 bg-slate-100 px-3 py-3 whitespace-nowrap">Loại giày</th>
            <th class="w-20 bg-slate-100 px-3 py-3 text-center whitespace-nowrap">Số lượng</th>
            <th class="w-36 bg-slate-100 px-2 py-3 whitespace-nowrap">Giá bán</th>
            <th class="w-32 bg-slate-100 px-2 py-3 whitespace-nowrap">Trạng thái</th>
            <th class="w-24 rounded-tr-2xl bg-slate-100 px-3 py-3 text-center whitespace-nowrap">Hành động</th>
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
            <td class="rounded-l-2xl px-3 py-4 align-middle font-semibold text-slate-500">
              {{ currentPage * pageSize + index + 1 }}
            </td>
            <td class="px-3 py-4 align-middle font-semibold text-slate-800 break-words">{{ item.ma }}</td>
            <td class="px-3 py-4 align-middle">
              <p class="break-words font-semibold leading-6 text-slate-800" :title="item.ten">{{ item.ten }}</p>
            </td>
            <td class="px-3 py-4 align-middle">
              <p class="break-words text-sm text-slate-700">{{ item.thuongHieu || '—' }}</p>
            </td>
            <td class="px-3 py-4 align-middle">
              <p class="break-words text-sm text-slate-700">{{ item.loaiGiay || '—' }}</p>
            </td>
            <td class="px-2 py-4 align-middle text-center font-semibold text-slate-700">
              {{ Number(item.tongSoLuong || 0).toLocaleString('vi-VN') }}
            </td>
            <td class="px-2 py-4 align-middle font-semibold text-slate-800">
              <div class="flex min-h-[56px] flex-col justify-center gap-1.5 leading-5">
                <div>
                  <div
                    class="flex flex-wrap items-center gap-x-1 gap-y-1 text-[15px] font-semibold text-slate-800"
                  >
                    <span class="whitespace-nowrap">{{ giaTrongBang(item).start }}</span>
                    <span v-if="giaTrongBang(item).isRange" class="text-slate-400 font-normal mx-0.5">-</span>
                    <span v-if="giaTrongBang(item).isRange" class="whitespace-nowrap">{{ giaTrongBang(item).end }}</span>
                  </div>
                </div>
                <div v-if="hasOriginalPrice(item)">
                  <div class="flex flex-wrap items-center gap-x-1 gap-y-1 text-xs font-medium text-slate-400 line-through">
                    <span class="whitespace-nowrap">{{ giaGocTrongBang(item).start }}</span>
                    <span v-if="giaGocTrongBang(item).isRange" class="mx-0.5">-</span>
                    <span v-if="giaGocTrongBang(item).isRange" class="whitespace-nowrap">{{ giaGocTrongBang(item).end }}</span>
                  </div>
                </div>
              </div>
            </td>
            <td class="px-2 py-4 align-middle">
              <span
                class="inline-flex max-w-[92px] overflow-hidden truncate whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold"
                :class="trangThaiClass(item.trangThai)"
                :title="trangThaiLabel(item.trangThai)"
              >
                {{ trangThaiLabel(item.trangThai) }}
              </span>
            </td>
            <td class="rounded-r-2xl px-3 py-4 align-middle text-center">
              <div class="flex items-center justify-center gap-1">
                <AdminQuickStatusAction
                  :loading="isUpdatingStatus(item.id)"
                  :disabled="isUpdatingStatus(item.id) || !canQuickToggleProduct(item)"
                  :action-label="productQuickToggleLabel(item)"
                  :disabled-title="productQuickToggleDisabledTitle(item)"
                  :confirm-message="productQuickToggleConfirmMessage(item)"
                  :intent="productQuickToggleIntent(item)"
                  @toggle="$emit('toggle-status', item)"
                />
                <button
                  type="button"
                  class="admin-table-action text-slate-600 hover:text-rose-500"
                  title="Xem QR và thông tin sản phẩm"
                  @click="$emit('open-qr', item)"
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
      @refresh="$emit('refresh', currentPage)"
      @update:current-page="$emit('update:current-page', $event)"
      @update:page-size="handlePageSizeChange"
    />
  </section>
</template>
