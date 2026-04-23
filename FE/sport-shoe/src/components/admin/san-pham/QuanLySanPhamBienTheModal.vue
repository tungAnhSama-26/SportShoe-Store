<script setup>
import { ImageOff, Images, Pencil, Plus, Trash2, X } from 'lucide-vue-next'
import AdminQuickStatusAction from '../../common/AdminQuickStatusAction.vue'

defineProps({
  open: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  selectedGiay: {
    type: Object,
    default: null
  },
  selectedGiayMainImage: {
    type: Object,
    default: null
  },
  bienTheList: {
    type: Array,
    default: () => []
  },
  updatingBienTheStatusId: {
    type: [Number, null],
    default: null
  },
  trangThaiClass: {
    type: Function,
    required: true
  },
  trangThaiLabel: {
    type: Function,
    required: true
  },
  gioiTinhLabel: {
    type: Function,
    required: true
  },
  selectedAttributeList: {
    type: Function,
    required: true
  },
  bienTheTrangThaiClass: {
    type: Function,
    required: true
  },
  bienTheTrangThaiLabel: {
    type: Function,
    required: true
  },
  formatCount: {
    type: Function,
    required: true
  },
  formatCurrency: {
    type: Function,
    required: true
  }
})

const emit = defineEmits([
  'close',
  'edit-product',
  'open-add-bienthe',
  'edit-bienthe',
  'toggle-bienthe-status',
  'delete-bienthe',
  'open-images'
])

function canToggleStatus(item) {
  return Number(item.kichHoat) === 1 || Number(item.soLuong || 0) > 0
}

function quickToggleLabel(item) {
  return Number(item.kichHoat) === 1 ? 'Chuyển sang ngừng bán' : 'Chuyển sang đang bán'
}

function quickToggleIntent(item) {
  return Number(item.kichHoat) === 1 ? 'deactivate' : 'activate'
}

function quickToggleDisabledTitle(item) {
  return canToggleStatus(item) ? quickToggleLabel(item) : 'Hết hàng chưa thể chuyển sang đang bán'
}

function quickToggleConfirmMessage(item) {
  const action = Number(item.kichHoat) === 1 ? 'ngừng bán' : 'đang bán'
  const target = item.maBienThe || item.sku || `#${item.id}`
  return `Bạn có muốn chuyển CTSP "${target}" sang ${action} không?`
}

function formatDiscountPercent(item) {
  const giaGoc = Number(item?.giaGoc || 0)
  const giaBan = Number(item?.giaBan || 0)
  if (giaGoc <= 0 || giaBan >= giaGoc) return '-'

  const percent = ((giaGoc - giaBan) / giaGoc) * 100
  return percent % 1 === 0 ? `${percent.toFixed(0)}%` : `${percent.toFixed(1)}%`
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-[52] flex items-center justify-center bg-black/55 p-4"
      @click.self="emit('close')"
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
            class="rounded-xl p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-700"
            @click="emit('close')"
          >
            <X :size="18" />
          </button>
        </div>

        <div class="flex-1 overflow-y-auto px-6 py-6">
          <div v-if="loading" class="rounded-2xl border border-slate-100 bg-slate-50 px-6 py-12 text-center text-sm text-slate-400">
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
                      class="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:border-slate-300 hover:bg-slate-50"
                      @click="emit('edit-product', selectedGiay)"
                    >
                      <Pencil :size="15" />
                      Sửa sản phẩm
                    </button>
                    <button
                      class="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:border-slate-300 hover:bg-slate-50"
                      @click="emit('close')"
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
                class="inline-flex h-10 items-center gap-2 rounded-xl bg-rose-500 px-4 text-sm font-semibold text-white shadow-sm shadow-rose-200 transition hover:bg-rose-600"
                @click="emit('open-add-bienthe')"
              >
                <Plus :size="15" />
                Thêm CTSP
              </button>
            </div>

            <div class="overflow-x-auto">
              <table class="admin-table admin-table--compact min-w-[1040px]">
                <thead class="border-b border-gray-100 bg-gray-50">
                  <tr>
                    <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Biến thể</th>
                    <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Thuộc tính</th>
                    <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Kho / giá</th>
                    <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Giảm %</th>
                    <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">Trạng thái</th>
                    <th class="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider text-gray-500">Ảnh</th>
                    <th class="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-gray-500">Thao tác</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-50">
                  <tr v-if="bienTheList.length === 0">
                    <td colspan="7" class="px-4 py-10 text-center text-sm text-slate-400">
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
                      <span
                        class="inline-flex rounded-full px-3 py-1 text-xs font-semibold whitespace-nowrap"
                        :class="formatDiscountPercent(item) === '-' ? 'bg-slate-100 text-slate-500' : 'bg-rose-50 text-rose-600'"
                      >
                        {{ formatDiscountPercent(item) }}
                      </span>
                    </td>
                    <td class="px-4 py-4 align-top">
                      <span class="admin-status-chip whitespace-nowrap" :class="bienTheTrangThaiClass(item)">
                        {{ bienTheTrangThaiLabel(item) }}
                      </span>
                    </td>
                    <td class="px-4 py-4 text-center align-top">
                      <button
                        class="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-600 transition hover:bg-emerald-100"
                        @click="emit('open-images', item)"
                      >
                        <Images :size="13" />
                        Quản lý ảnh
                      </button>
                    </td>
                    <td class="px-4 py-4 align-top">
                      <div class="flex items-center justify-end gap-1">
                        <AdminQuickStatusAction
                          :loading="updatingBienTheStatusId === item.id"
                          :disabled="updatingBienTheStatusId === item.id || !canToggleStatus(item)"
                          :action-label="quickToggleLabel(item)"
                          :disabled-title="quickToggleDisabledTitle(item)"
                          :confirm-message="quickToggleConfirmMessage(item)"
                          :intent="quickToggleIntent(item)"
                          @toggle="emit('toggle-bienthe-status', item)"
                        />
                        <button
                          title="Sửa CTSP"
                          class="admin-table-action text-slate-600 hover:text-rose-500"
                          @click="emit('edit-bienthe', item)"
                        >
                          <Pencil :size="14" />
                        </button>
                        <button
                          title="Xóa CTSP"
                          class="admin-table-action text-red-500 hover:text-red-600"
                          @click="emit('delete-bienthe', item.id)"
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
</template>
