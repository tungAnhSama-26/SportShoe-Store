<script setup>
import { ref, watch } from 'vue'
import { Eye, Images, Layers3, Pencil, Tag, RefreshCw } from 'lucide-vue-next'
import AdminQuickStatusAction from '../../../components/common/AdminQuickStatusAction.vue'
import AdminTableFooter from '../../../components/common/AdminTableFooter.vue'
import { resolveHinhAnh } from '../../../utils/resolve-image'

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
  },
  focusedChiTietId: {
    type: Number,
    default: null
  },
  hidePagination: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'toggle-status',
  'edit-variant',
  'open-discount-detail',
  'open-qr',
  'refresh',
  'update:current-page',
  'update:page-size',
  'selection-changed'
])

function isUpdatingStatus(id) {
  return props.updatingStatusIds.has(id)
}

function isFocusedVariant(item) {
  return props.focusedChiTietId != null && Number(item?.id) === props.focusedChiTietId
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function giaSauDotGiam(item) {
  const giaBan = Number(item?.giaBan || 0)
  const giaTriGiam = Number(item?.giaTriGiam || 0)
  const loaiGiam = Number(item?.loaiGiam || 0)

  if (!item?.dotGiamGiaId || giaBan <= 0 || giaTriGiam <= 0) return giaBan
  if (loaiGiam === 1) return Math.max(0, giaBan * (1 - Math.min(giaTriGiam, 100) / 100))
  if (loaiGiam === 2) return Math.max(0, giaBan - giaTriGiam)
  return giaBan
}

function giaHienThi(item) {
  return giaSauDotGiam(item)
}

function giaGachNgang(item) {
  if (item?.dotGiamGiaId && giaSauDotGiam(item) < Number(item?.giaBan || 0)) {
    return Number(item?.giaBan || 0)
  }
  if (Number(item?.giaBan || 0) < Number(item?.giaGoc || 0)) {
    return Number(item?.giaGoc || 0)
  }
  return null
}

function isDiscounted(item) {
  return giaGachNgang(item) != null
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

    if (loaiGiam === 2 && giaBan > 0) {
      return formatPercentValue((giaTriGiam / giaBan) * 100)
    }
  }

  if (giaGoc <= 0 || giaBan >= giaGoc) return '—'

  return formatPercentValue(((giaGoc - giaBan) / giaGoc) * 100)
}

function discountTitle(item) {
  return item?.maDotGiamGia || item?.tenDotGiamGia || 'Xem đợt giảm giá'
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

function quickToggleLabel(item) {
  if (Number(item.kichHoat) === 1) return 'Chuyển sang ngừng bán'
  return 'Chuyển sang đang bán'
}

function canToggleStatus(item) {
  // Hết hàng (không còn tồn): trạng thái bán theo tồn kho -> không bật/tắt thủ công.
  return Number(item.soLuong || 0) > 0
}

function quickToggleIntent(item) {
  return Number(item.kichHoat) === 1 ? 'deactivate' : 'activate'
}

function quickToggleDisabledTitle(item) {
  return canToggleStatus(item)
    ? quickToggleLabel(item)
    : 'Biến thể đang hết hàng — không thể đổi trạng thái thủ công'
}

function quickToggleConfirmMessage(item) {
  const action = Number(item.kichHoat) === 1 ? 'ngừng bán' : 'đang bán'
  const target = item.maChiTietSanPham || item.maBienThe || item.sku || `#${item.id}`
  return `Bạn có muốn chuyển CTSP "${target}" sang ${action} không?`
}

import { showWarning } from '../../../utils/alert'

function handlePageSizeChange(size) {
  emit('update:page-size', size)
}

function openDiscountDetail(item) {
  if (!item?.dotGiamGiaId) {
    emit('edit-variant', item)
    return
  }
  emit('open-discount-detail', item)
}

const selectedVariantIds = ref(new Set())

function toggleSelectAll(event) {
  if (event.target.checked) {
    props.items.forEach(item => selectedVariantIds.value.add(item.id))
  } else {
    selectedVariantIds.value.clear()
  }
}

function toggleSelectVariant(id) {
  if (selectedVariantIds.value.has(id)) {
    selectedVariantIds.value.delete(id)
  } else {
    selectedVariantIds.value.add(id)
  }
}

watch(() => selectedVariantIds.value.size, (newSize) => {
  emit('selection-changed', newSize > 0)
})

function handleBulkQr() {
  const selectedItems = props.items.filter(i => selectedVariantIds.value.has(i.id))
  if (!selectedItems.length) return
  emit('bulk-qr', selectedItems)
  selectedVariantIds.value.clear()
}

defineExpose({
  selectedVariantIds
})
</script>

<template>
  <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">

    <div class="overflow-x-auto rounded-[24px] border border-slate-100 admin-table-scroll">
      <table class="w-full table-fixed border-separate border-spacing-0 text-sm">
        <colgroup>
          <col class="w-[3%]" />
          <col class="w-[3%]" />
          <col class="w-[9%]" />
          <col class="w-[9%]" />
          <col class="w-[6%]" />
          <col class="w-[11%]" />
          <col class="w-[7%]" />
          <col class="w-[7%]" />
          <col class="w-[10%]" />
          <col class="w-[7%]" />
          <col class="w-[13%]" />
          <col class="w-[15%]" />
        </colgroup>
        <thead>
          <tr class="text-left text-sm font-bold text-slate-950 [&>th]:whitespace-nowrap [&>th]:px-3 [&>th]:py-3">
            <th class="rounded-tl-md bg-slate-100 text-center">
              <input type="checkbox" class="h-4 w-4 rounded border-slate-300 text-rose-500 focus:ring-rose-500" 
                     :checked="items.length > 0 && selectedVariantIds.size === items.length"
                     @change="toggleSelectAll" />
            </th>
            <th class="bg-slate-100">STT</th>
            <th class="bg-slate-100">Mã SP</th>
            <th class="bg-slate-100">Mã CTSP</th>
            <th class="bg-slate-100 text-center">Ảnh</th>
            <th class="bg-slate-100">Màu sắc</th>
            <th class="bg-slate-100 text-center">Kích cỡ</th>
            <th class="bg-slate-100 text-center">Số lượng</th>
            <th class="bg-slate-100">Giá bán</th>
            <th class="bg-slate-100 text-center">Giảm</th>
            <th class="bg-slate-100 text-center">Trạng thái</th>
            <th class="rounded-tr-md bg-slate-100 text-center">Hành động</th>
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
            :class="[
              'text-slate-700 shadow-sm',
              isFocusedVariant(item) ? 'bg-rose-50 ring-2 ring-rose-200' : 'bg-white ring-1 ring-slate-100'
            ]"
          >
            <td class="rounded-l-md px-2.5 py-4 align-middle text-center">
              <input type="checkbox" class="h-4 w-4 rounded border-slate-300 text-rose-500 focus:ring-rose-500"
                     :checked="selectedVariantIds.has(item.id)"
                     @change="toggleSelectVariant(item.id)" />
            </td>
            <td class="px-2.5 py-4 align-middle font-semibold text-slate-500 whitespace-nowrap">
              {{ currentPage * pageSize + index + 1 }}
            </td>
            <td class="px-2.5 py-4 align-middle font-bold text-slate-950 break-words">
              {{ item.maSanPham }}
            </td>
            <td class="px-2.5 py-4 align-middle font-bold text-slate-900 break-words">
              {{ item.maChiTietSanPham }}
            </td>
            <td class="px-2.5 py-4 align-middle">
              <div class="relative mx-auto flex h-11 w-11 items-center justify-center overflow-hidden rounded-md bg-slate-100">
                <img v-if="item.hinhAnh" :src="resolveHinhAnh(item.hinhAnh)" alt="" class="h-full w-full object-cover" />
                <Images class="h-4 w-4 text-slate-300" v-else />
                <!-- Badge giảm giá -->
                <div
                  v-if="isDiscounted(item) && formatDiscountPercent(item) !== '—'"
                  class="absolute left-0 top-0 origin-top-left scale-[0.45] rounded-br-lg rounded-tl-md bg-rose-500 px-1.5 py-1 text-[10px] font-bold leading-none text-white shadow-sm"
                  :title="item.tenDotGiamGia || item.maDotGiamGia || 'Đang giảm giá'"
                >
                  -{{ formatDiscountPercent(item) }}
                </div>
              </div>
            </td>
            <td class="px-2.5 py-4 align-middle whitespace-nowrap">
              <div
                class="inline-flex max-w-full items-center gap-1.5 rounded-full bg-slate-100 px-2.5 py-1 text-[11px] font-semibold text-slate-700"
                :title="item.mauSac"
              >
                <span
                  class="h-2.5 w-2.5 rounded-full border border-slate-300"
                  :style="{ backgroundColor: item.maMauHex || '#e2e8f0' }"
                ></span>
                <span class="truncate whitespace-nowrap">{{ item.mauSac }}</span>
              </div>
            </td>
            <td class="px-2.5 py-4 align-middle text-center font-bold text-slate-900 whitespace-nowrap">
              {{ item.kichCo }}
            </td>
            <td class="px-2 py-4 align-middle text-center font-bold text-slate-900 whitespace-nowrap">
              {{ Number(item.soLuong || 0).toLocaleString('vi-VN') }}
            </td>
            <td class="px-2 py-4 align-middle">
              <div class="flex min-h-[56px] flex-col justify-center leading-6">
                <p class="font-semibold" :class="isDiscounted(item) ? 'text-rose-600' : 'text-slate-800'">
                  {{ formatCurrency(giaHienThi(item)) }} đ
                </p>
                <p v-if="isDiscounted(item)" class="mt-1 text-xs text-slate-400 line-through">
                  {{ formatCurrency(giaGachNgang(item)) }} đ
                </p>
              </div>
            </td>
            <td class="px-2 py-4 align-middle text-center">
              <button
                v-if="isDiscounted(item) && formatDiscountPercent(item) !== '—'"
                type="button"
                class="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 transition hover:bg-emerald-100"
                :title="item.dotGiamGiaId ? discountTitle(item) : 'Giá bán đang thấp hơn giá gốc'"
                @click="openDiscountDetail(item)"
              >
                <Tag class="h-3 w-3" />
                {{ formatDiscountPercent(item) }}
              </button>
              <span v-else class="text-xs text-slate-400">—</span>
            </td>
            <td class="px-2 py-4 align-middle text-center whitespace-nowrap">
              <span
                class="inline-flex items-center justify-center whitespace-nowrap rounded-full px-3 py-1.5 text-xs font-semibold"
                :class="bienTheTrangThaiClass(item)"
                :title="bienTheTrangThaiLabel(item)"
              >
                {{ bienTheTrangThaiLabel(item) }}
              </span>
            </td>
            <td class="rounded-r-md px-4 py-4 align-middle text-center">
              <div class="flex items-center justify-center gap-2">
                <AdminQuickStatusAction
                  :loading="isUpdatingStatus(item.id)"
                  :disabled="isUpdatingStatus(item.id) || !canToggleStatus(item)"
                  :action-label="quickToggleLabel(item)"
                  :disabled-title="quickToggleDisabledTitle(item)"
                  :confirm-message="quickToggleConfirmMessage(item)"
                  :intent="quickToggleIntent(item)"
                  @toggle="$emit('toggle-status', item)"
                />
                <button
                  type="button"
                  class="admin-table-action text-slate-600 hover:text-rose-500"
                  title="Chỉnh sửa biến thể"
                  @click="$emit('edit-variant', item)"
                >
                  <Pencil class="h-4 w-4" />
                </button>
                <button
                  type="button"
                  class="admin-table-action text-slate-600 hover:text-rose-500"
                  title="Xem QR và thông tin chi tiết sản phẩm"
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
      v-if="!hidePagination"
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
