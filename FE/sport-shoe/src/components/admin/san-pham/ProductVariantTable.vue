<script setup>
import { Eye, Images, Layers3, Tag } from 'lucide-vue-next'
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
  },
  focusedChiTietId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits([
  'toggle-status',
  'open-discount-detail',
  'open-qr',
  'refresh',
  'update:current-page',
  'update:page-size'
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

function bienTheTrangThaiLabel(item) {
  if (Number(item.soLuong || 0) <= 0) return 'Hết hàng'
  return Number(item.kichHoat) === 1 ? 'Đang bán' : 'Ngừng bán'
}

function bienTheTrangThaiClass(item) {
  if (Number(item.soLuong || 0) <= 0) return 'bg-amber-50 text-amber-600'
  return Number(item.kichHoat) === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-600'
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

function handlePageSizeChange(size) {
  emit('update:page-size', size)
}

function openDiscountDetail(item) {
  if (!item?.dotGiamGiaId) return
  emit('open-discount-detail', item)
}
</script>

<template>
  <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
    <div class="mb-5 flex items-center gap-3">
      <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#B82220]/5 text-[#B82220]">
        <Layers3 class="h-5 w-5" />
      </div>
      <div>
        <h2 class="admin-section-title">Danh sách chi tiết sản phẩm</h2>
      </div>
    </div>

    <div class="admin-table-scroll rounded-[24px] border border-slate-100">
      <table class="min-w-[1120px] w-full table-fixed border-separate border-spacing-0 text-sm">
        <colgroup>
          <col class="w-[4.5%]" />
          <col class="w-[9%]" />
          <col class="w-[11%]" />
          <col class="w-[7%]" />
          <col class="w-[14%]" />
          <col class="w-[7%]" />
          <col class="w-[8%]" />
          <col class="w-[15%]" />
          <col class="w-[7.5%]" />
          <col class="w-[10.5%]" />
          <col class="w-[6.5%]" />
        </colgroup>
        <thead>
          <tr class="text-left text-sm font-bold text-slate-950">
            <th class="rounded-tl-2xl bg-slate-100 px-2.5 py-3 whitespace-nowrap">STT</th>
            <th class="bg-slate-100 px-2.5 py-3 whitespace-nowrap">Mã SP</th>
            <th class="bg-slate-100 px-2.5 py-3 whitespace-nowrap">Mã CTSP</th>
            <th class="bg-slate-100 px-2.5 py-3 text-center whitespace-nowrap">Ảnh</th>
            <th class="bg-slate-100 px-2.5 py-3 whitespace-nowrap">Màu sắc</th>
            <th class="bg-slate-100 px-2.5 py-3 text-center whitespace-nowrap">Kích cỡ</th>
            <th class="bg-slate-100 px-2 py-3 text-center whitespace-nowrap">Số lượng</th>
            <th class="bg-slate-100 px-2 py-3 whitespace-nowrap">Giá bán</th>
            <th class="bg-slate-100 px-2 py-3 text-center whitespace-nowrap">Giảm</th>
            <th class="bg-slate-100 px-2 py-3 text-center whitespace-nowrap">Trạng thái</th>
            <th class="rounded-tr-2xl bg-slate-100 px-2.5 py-3 text-center whitespace-nowrap">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="11" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
          </tr>
          <tr v-else-if="!items.length">
            <td colspan="11" class="py-10 text-center text-sm text-slate-400">Chưa có chi tiết sản phẩm nào</td>
          </tr>
          <tr
            v-for="(item, index) in items"
            :key="item.id"
            :class="[
              'text-slate-700 shadow-sm',
              isFocusedVariant(item) ? 'bg-rose-50 ring-2 ring-rose-200' : 'bg-white ring-1 ring-slate-100'
            ]"
          >
            <td class="rounded-l-2xl px-2.5 py-4 align-middle font-semibold text-slate-500 whitespace-nowrap">
              {{ currentPage * pageSize + index + 1 }}
            </td>
            <td class="px-2.5 py-4 align-middle font-bold text-slate-950 break-words">
              {{ item.maSanPham }}
            </td>
            <td class="px-2.5 py-4 align-middle font-bold text-slate-900 break-words">
              {{ item.maChiTietSanPham }}
            </td>
            <td class="px-2.5 py-4 align-middle">
              <div class="mx-auto flex h-11 w-11 items-center justify-center overflow-hidden rounded-2xl bg-slate-100">
                <img v-if="item.hinhAnh" :src="item.hinhAnh" alt="" class="h-full w-full object-cover" />
                <Images class="h-4 w-4 text-slate-300" v-else />
              </div>
            </td>
            <td class="px-2.5 py-4 align-middle">
              <div
                class="inline-flex max-w-full items-center gap-1.5 rounded-full bg-slate-100 px-2.5 py-1 text-[11px] font-semibold text-slate-700"
                :title="item.mauSac"
              >
                <span
                  class="h-2.5 w-2.5 rounded-full border border-black/5"
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
                  {{ formatCurrency(item.giaBan) }} đ
                </p>
                <p v-if="isDiscounted(item)" class="mt-1 text-xs text-slate-400 line-through">
                  {{ formatCurrency(item.giaGoc) }} đ
                </p>
              </div>
            </td>
            <td class="px-2 py-4 align-middle text-center">
              <button
                v-if="item.dotGiamGiaId"
                type="button"
                class="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 transition hover:bg-emerald-100"
                :title="discountTitle(item)"
                @click="openDiscountDetail(item)"
              >
                <Tag class="h-3 w-3" />
                {{ formatDiscountPercent(item) }}
              </button>
              <span v-else class="text-xs text-slate-400">—</span>
            </td>
            <td class="px-2 py-4 align-middle text-center">
              <span
                class="inline-flex min-w-[108px] items-center justify-center overflow-hidden truncate whitespace-nowrap rounded-full px-2 py-1.5 text-center text-xs font-semibold"
                :class="bienTheTrangThaiClass(item)"
                :title="bienTheTrangThaiLabel(item)"
              >
                {{ bienTheTrangThaiLabel(item) }}
              </span>
            </td>
            <td class="rounded-r-2xl px-2.5 py-4 align-middle text-center">
              <div class="flex items-center justify-center gap-1">
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
      :current-page="currentPage"
      :page-size="pageSize"
      :page-size-options="pageSizeOptions"
      :total-items="totalItems"
      :total-pages="totalPages"
      compact
      show-refresh
      zero-based
      @refresh="$emit('refresh', currentPage)"
      @update:current-page="$emit('update:current-page', $event)"
      @update:page-size="handlePageSizeChange"
    />
  </section>
</template>
