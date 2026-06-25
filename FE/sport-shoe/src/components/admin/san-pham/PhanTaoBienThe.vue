<script setup>
import { computed, ref } from 'vue'
import { Check, ChevronDown, Plus, Search, X } from 'lucide-vue-next'
import { normalizeAttributeText, normalizeSizeValue } from '../../../utils/thuoc-tinh-san-pham.js'

const props = defineProps({
  variantBuilder: {
    type: Object,
    default: () => ({
      mauSacIds: [],
      kichCoIds: [],
      giaBan: "",
      soLuong: "",
      skuPrefix: ""
    })
  },
  variantErrors: {
    type: Object,
    default: () => ({})
  },
  danhMuc: {
    type: Object,
    default: null
  },
  mauSacSearch: {
    type: String,
    default: ''
  },
  kichCoSearch: {
    type: String,
    default: ''
  },
  openVariantDropdown: {
    type: String,
    default: null
  },
  inlineCreatingType: {
    type: String,
    default: null
  }
})

const emit = defineEmits([
  'update:mau-sac-search',
  'update:kich-co-search',
  'toggle-variant-dropdown',
  'inline-create-attribute',
  'clear-selected-values',
  'toggle-selected-value',
  'generate-variants'
])

const mauSacDropdownRef = ref(null)
const kichCoDropdownRef = ref(null)

const selectedMauSacItems = computed(() => {
  const colorMap = new Map(
    (props.danhMuc?.mauSac || []).filter(Boolean).map((item) => [Number(item.id), item])
  )

  return props.variantBuilder.mauSacIds
    .map((id) => colorMap.get(Number(id)))
    .filter(Boolean)
})

const selectedKichCoItems = computed(() => {
  const sizeMap = new Map(
    (props.danhMuc?.kichCo || []).filter(Boolean).map((item) => [Number(item.id), item])
  )

  return props.variantBuilder.kichCoIds
    .map((id) => sizeMap.get(Number(id)))
    .filter(Boolean)
})

const filteredMauSacItems = computed(() => {
  const search = props.mauSacSearch.toLowerCase()
  return (props.danhMuc?.mauSac || []).filter(Boolean).filter((item) =>
    item.ten.toLowerCase().includes(search)
  )
})

const filteredKichCoItems = computed(() => {
  const search = props.kichCoSearch.toLowerCase()
  return (props.danhMuc?.kichCo || []).filter(Boolean).filter((item) =>
    item.giaTri.toString().toLowerCase().includes(search)
  )
})

const mauSacSummary = computed(() => {
  if (!selectedMauSacItems.value.length) return 'Chọn màu'
  if (selectedMauSacItems.value.length === 1) return selectedMauSacItems.value[0].ten
  return `${selectedMauSacItems.value.length} màu đã chọn`
})

const kichCoSummary = computed(() => {
  if (!selectedKichCoItems.value.length) return 'Chọn size'
  if (selectedKichCoItems.value.length === 1) return `Size ${selectedKichCoItems.value[0].giaTri}`
  return `${selectedKichCoItems.value.length} size đã chọn`
})

const fullMauSacSummary = computed(() => {
  if (!selectedMauSacItems.value.length) return mauSacSummary.value
  return selectedMauSacItems.value.map((item) => item.ten).join(', ')
})

const fullKichCoSummary = computed(() => {
  if (!selectedKichCoItems.value.length) return kichCoSummary.value
  return selectedKichCoItems.value.map((item) => `Size ${item.giaTri}`).join(', ')
})

const trimmedMauSacSearch = computed(() => props.mauSacSearch.trim())
const normalizedKichCoSearch = computed(() => normalizeSizeValue(props.kichCoSearch))

const showCreateMauSacOption = computed(() => {
  if (!trimmedMauSacSearch.value) return false
  const keyword = normalizeAttributeText(trimmedMauSacSearch.value)
  return !(props.danhMuc?.mauSac || []).some((item) =>
    normalizeAttributeText(item.ten) === keyword
  )
})

const showCreateKichCoOption = computed(() => {
  if (!normalizedKichCoSearch.value) return false
  return !(props.danhMuc?.kichCo || []).some((item) =>
    normalizeSizeValue(item.giaTri) === normalizedKichCoSearch.value
  )
})

function isSelected(field, id) {
  return props.variantBuilder[field].includes(id)
}

function toggleVariantDropdown(type) {
  emit('toggle-variant-dropdown', type)
}

function clearSelectedValues(field) {
  emit('clear-selected-values', field)
}

function toggleSelectedValue(field, id) {
  emit('toggle-selected-value', field, id)
}

function handleInlineCreateAttribute(type) {
  if (props.inlineCreatingType === type) return

  if (type === 'mauSac') {
    emit('inline-create-attribute', 'mauSac', trimmedMauSacSearch.value)
    return
  }

  if (type === 'kichCo') {
    emit('inline-create-attribute', 'kichCo', normalizedKichCoSearch.value)
  }
}

function handleMauSacKeydown(event) {
  if (event.key !== 'Enter') return
  if (!showCreateMauSacOption.value) return
  event.preventDefault()
  handleInlineCreateAttribute('mauSac')
}

function handleKichCoKeydown(event) {
  if (event.key !== 'Enter') return
  if (!showCreateKichCoOption.value) return
  event.preventDefault()
  handleInlineCreateAttribute('kichCo')
}

function generateVariants() {
  emit('generate-variants')
}
</script>

<template>
  <article class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
    <div class="space-y-4">
      <div ref="mauSacDropdownRef" class="dropdown-container relative rounded-[20px] border border-slate-200 bg-slate-50 p-4" @click.stop>
        <div class="grid gap-3 lg:grid-cols-[120px_minmax(0,1fr)_auto] lg:items-start">
          <div>
            <label class="block text-[13px] font-semibold text-slate-700">Màu sắc <span class="text-rose-500">*</span></label>
          </div>

          <div>
            <button
              type="button"
              class="flex min-h-11 w-full items-start justify-between gap-2 rounded-md border border-slate-200 bg-white px-4 py-3 text-sm transition hover:bg-white"
              :class="
                selectedMauSacItems.length
                  ? 'border-slate-200 text-slate-700'
                  : 'text-slate-600'
              "
              @click="toggleVariantDropdown('mauSac')"
            >
              <span class="min-w-0 flex-1 text-left leading-5 whitespace-normal break-words">
                {{ fullMauSacSummary }}
              </span>
              <ChevronDown :size="16" class="mt-0.5 shrink-0" />
            </button>

            <div
              v-if="openVariantDropdown === 'mauSac'"
              class="absolute left-4 right-4 top-full z-20 mt-2 overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-xl lg:left-[136px] lg:right-[110px]"
            >
              <div class="border-b border-slate-100 p-3">
                <div class="relative">
                  <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                  <input
                    :value="mauSacSearch"
                    type="text"
                    placeholder="Nhập màu sắc..."
                    class="h-10 w-full rounded-md border border-slate-200 bg-slate-50 pl-10 pr-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                    @input="$emit('update:mau-sac-search', $event.target.value)"
                    @keydown.stop="handleMauSacKeydown"
                  />
                </div>
              </div>

              <div class="max-h-64 overflow-y-auto p-2">
                <button
                  v-if="showCreateMauSacOption"
                  type="button"
                  class="mb-1 flex w-full items-center justify-between rounded-md border border-dashed border-rose-200 bg-rose-50 px-3 py-2 text-left text-sm font-medium text-rose-600 transition hover:bg-rose-100"
                  :disabled="inlineCreatingType === 'mauSac'"
                  @click="handleInlineCreateAttribute('mauSac')"
                >
                  <span class="truncate">
                    {{ inlineCreatingType === 'mauSac' ? 'Đang thêm...' : `Thêm màu "${trimmedMauSacSearch}"` }}
                  </span>
                  <Plus :size="15" class="shrink-0" />
                </button>

                <button
                  v-if="selectedMauSacItems.length"
                  type="button"
                  class="mb-1 flex w-full items-center justify-between rounded-md px-3 py-2 text-left text-sm text-slate-500 transition hover:bg-slate-50"
                  @click="clearSelectedValues('mauSacIds')"
                >
                  <span>Bỏ chọn tất cả</span>
                  <X :size="15" />
                </button>

                <button
                  v-for="item in filteredMauSacItems"
                  :key="item.id"
                  type="button"
                  class="flex w-full items-center justify-between gap-3 rounded-md px-3 py-2 text-left text-sm transition hover:bg-slate-50"
                  :class="
                    isSelected('mauSacIds', item.id)
                      ? 'bg-slate-100 text-slate-700'
                      : 'text-slate-700'
                  "
                  @click="toggleSelectedValue('mauSacIds', item.id)"
                >
                  <div class="flex min-w-0 items-center gap-2">
                    <span
                      class="h-3 w-3 shrink-0 rounded-full border border-slate-300"
                      :style="{ backgroundColor: item.maMauHex || '#e2e8f0' }"
                    ></span>
                    <span class="truncate">{{ item.ten }}</span>
                  </div>
                  <Check v-if="isSelected('mauSacIds', item.id)" :size="15" class="shrink-0" />
                </button>

                <div v-if="!filteredMauSacItems.length && !showCreateMauSacOption" class="rounded-md px-3 py-6 text-center text-sm text-slate-400">
                  Không tìm thấy màu sắc phù hợp.
                </div>
              </div>
            </div>

            <p v-if="variantErrors.mauSacIds" class="mt-1 text-xs text-rose-500">
              {{ variantErrors.mauSacIds }}
            </p>

            <div v-if="selectedMauSacItems.length" class="mt-2 flex flex-wrap gap-2">
              <span
                v-for="item in selectedMauSacItems"
                :key="`selected-mau-sac-${item.id}`"
                class="inline-flex items-center gap-2 rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700"
              >
                <span
                  class="h-2.5 w-2.5 shrink-0 rounded-full border border-slate-300"
                  :style="{ backgroundColor: item.maMauHex || '#e2e8f0' }"
                ></span>
                {{ item.ten }}
              </span>
            </div>
          </div>

          <div class="flex justify-start lg:justify-end">
            <button
              type="button"
              class="inline-flex items-center gap-1 rounded-full border border-rose-200 bg-white px-2.5 py-1 text-[11px] font-semibold text-rose-600 transition hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="inlineCreatingType === 'mauSac'"
              @click.stop="handleInlineCreateAttribute('mauSac')"
            >
              <Plus :size="12" />
              {{ inlineCreatingType === 'mauSac' ? 'Đang thêm' : 'Thêm nhanh' }}
            </button>
          </div>
        </div>
      </div>

      <div ref="kichCoDropdownRef" class="dropdown-container relative rounded-[20px] border border-slate-200 bg-white p-4" @click.stop>
        <div class="grid gap-3 lg:grid-cols-[120px_minmax(0,1fr)_auto] lg:items-start">
          <div>
            <label class="block text-[13px] font-semibold text-slate-700">Kích cỡ <span class="text-rose-500">*</span></label>
          </div>

          <div>
            <button
              type="button"
              class="flex min-h-11 w-full items-start justify-between gap-2 rounded-md border border-slate-200 bg-slate-50 px-4 py-3 text-sm transition hover:bg-white"
              :class="
                selectedKichCoItems.length
                  ? 'border-slate-200 text-slate-700'
                  : 'text-slate-600'
              "
              @click="toggleVariantDropdown('kichCo')"
            >
              <span class="min-w-0 flex-1 text-left leading-5 whitespace-normal break-words">
                {{ fullKichCoSummary }}
              </span>
              <ChevronDown :size="16" class="mt-0.5 shrink-0" />
            </button>

            <div
              v-if="openVariantDropdown === 'kichCo'"
              class="absolute left-4 right-4 top-full z-20 mt-2 overflow-hidden rounded-[24px] border border-slate-200 bg-white shadow-xl lg:left-[136px] lg:right-[110px]"
            >
              <div class="border-b border-slate-100 p-3">
                <div class="relative">
                  <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                  <input
                    :value="kichCoSearch"
                    type="text"
                    placeholder="Nhập kích cỡ..."
                    class="h-10 w-full rounded-md border border-slate-200 bg-slate-50 pl-10 pr-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                    @input="$emit('update:kich-co-search', $event.target.value)"
                    @keydown.stop="handleKichCoKeydown"
                  />
                </div>
              </div>

              <div class="max-h-64 overflow-y-auto p-2">
                <button
                  v-if="showCreateKichCoOption"
                  type="button"
                  class="mb-1 flex w-full items-center justify-between rounded-md border border-dashed border-rose-200 bg-rose-50 px-3 py-2 text-left text-sm font-medium text-rose-600 transition hover:bg-rose-100"
                  :disabled="inlineCreatingType === 'kichCo'"
                  @click="handleInlineCreateAttribute('kichCo')"
                >
                  <span class="truncate">
                    {{ inlineCreatingType === 'kichCo' ? 'Đang thêm...' : `Thêm size "${normalizedKichCoSearch}"` }}
                  </span>
                  <Plus :size="15" class="shrink-0" />
                </button>

                <button
                  v-if="selectedKichCoItems.length"
                  type="button"
                  class="mb-1 flex w-full items-center justify-between rounded-md px-3 py-2 text-left text-sm text-slate-500 transition hover:bg-slate-50"
                  @click="clearSelectedValues('kichCoIds')"
                >
                  <span>Bỏ chọn tất cả</span>
                  <X :size="15" />
                </button>

                <button
                  v-for="item in filteredKichCoItems"
                  :key="item.id"
                  type="button"
                  class="flex w-full items-center justify-between gap-3 rounded-md px-3 py-2 text-left text-sm transition hover:bg-slate-50"
                  :class="
                    isSelected('kichCoIds', item.id)
                      ? 'bg-slate-100 text-slate-700'
                      : 'text-slate-700'
                  "
                  @click="toggleSelectedValue('kichCoIds', item.id)"
                >
                  <span class="truncate">Size {{ item.giaTri }}</span>
                  <Check v-if="isSelected('kichCoIds', item.id)" :size="15" class="shrink-0" />
                </button>

                <div v-if="!filteredKichCoItems.length && !showCreateKichCoOption" class="rounded-md px-3 py-6 text-center text-sm text-slate-400">
                  Không tìm thấy kích cỡ phù hợp.
                </div>
              </div>
            </div>

            <p v-if="variantErrors.kichCoIds" class="mt-1 text-xs text-rose-500">
              {{ variantErrors.kichCoIds }}
            </p>

            <div v-if="selectedKichCoItems.length" class="mt-2 flex flex-wrap gap-2">
              <span
                v-for="item in selectedKichCoItems"
                :key="`selected-kich-co-${item.id}`"
                class="inline-flex items-center rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700"
              >
                Size {{ item.giaTri }}
              </span>
            </div>
          </div>

          <div class="flex justify-start lg:justify-end">
            <button
              type="button"
              class="inline-flex items-center gap-1 rounded-full border border-rose-200 bg-white px-2.5 py-1 text-[11px] font-semibold text-rose-600 transition hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="inlineCreatingType === 'kichCo'"
              @click.stop="handleInlineCreateAttribute('kichCo')"
            >
              <Plus :size="12" />
              {{ inlineCreatingType === 'kichCo' ? 'Đang thêm' : 'Thêm nhanh' }}
            </button>
          </div>
        </div>
      </div>

      <div class="flex justify-end">
        <button type="button" class="admin-btn-primary min-w-[240px]" @click="generateVariants">
          Tạo biến thể tự động
        </button>
      </div>
    </div>
  </article>
</template>
