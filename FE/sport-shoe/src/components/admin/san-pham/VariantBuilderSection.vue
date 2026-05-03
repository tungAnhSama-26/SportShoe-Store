<script setup>
import { computed, ref } from 'vue'
import { Check, ChevronDown, Plus, Search, X } from 'lucide-vue-next'

const props = defineProps({
  variantBuilder: {
    type: Object,
    required: true
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
  }
})

const emit = defineEmits([
  'update:mau-sac-search',
  'update:kich-co-search',
  'toggle-variant-dropdown',
  'open-quick-create',
  'clear-selected-values',
  'toggle-selected-value',
  'generate-variants'
])

const mauSacDropdownRef = ref(null)
const kichCoDropdownRef = ref(null)

const selectedMauSacItems = computed(() => {
  return (props.danhMuc?.mauSac || []).filter(item =>
    props.variantBuilder.mauSacIds.includes(item.id)
  )
})

const selectedKichCoItems = computed(() => {
  return (props.danhMuc?.kichCo || []).filter(item =>
    props.variantBuilder.kichCoIds.includes(item.id)
  )
})

const filteredMauSacItems = computed(() => {
  const search = props.mauSacSearch.toLowerCase()
  return (props.danhMuc?.mauSac || []).filter(item =>
    item.ten.toLowerCase().includes(search)
  )
})

const filteredKichCoItems = computed(() => {
  const search = props.kichCoSearch.toLowerCase()
  return (props.danhMuc?.kichCo || []).filter(item =>
    item.giaTri.toString().toLowerCase().includes(search)
  )
})

const mauSacSummary = computed(() => {
  if (!selectedMauSacItems.value.length) return 'Chọn màu sắc...'
  if (selectedMauSacItems.value.length === 1) return selectedMauSacItems.value[0].ten
  return `${selectedMauSacItems.value.length} màu sắc đã chọn`
})

const kichCoSummary = computed(() => {
  if (!selectedKichCoItems.value.length) return 'Chọn kích cỡ...'
  if (selectedKichCoItems.value.length === 1) return `Size ${selectedKichCoItems.value[0].giaTri}`
  return `${selectedKichCoItems.value.length} kích cỡ đã chọn`
})

function isSelected(field, id) {
  return props.variantBuilder[field].includes(id)
}

function toggleVariantDropdown(type) {
  emit('toggle-variant-dropdown', type)
}

function openQuickCreate(type, search) {
  emit('open-quick-create', type, search)
}

function clearSelectedValues(field) {
  emit('clear-selected-values', field)
}

function toggleSelectedValue(field, id) {
  emit('toggle-selected-value', field, id)
}

function generateVariants() {
  emit('generate-variants')
}
</script>

<template>
  <article class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
    <div class="space-y-4">
      <div ref="mauSacDropdownRef" class="relative rounded-[20px] border border-slate-200 bg-slate-50 p-4" @click.stop>
        <div class="grid gap-3 lg:grid-cols-[120px_minmax(0,1fr)_auto] lg:items-start">
          <div>
            <label class="block text-[13px] font-semibold text-slate-700">Màu sắc</label>
          </div>

          <div>
            <button
              type="button"
              class="flex min-h-11 w-full items-start justify-between gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm transition hover:bg-white"
              :class="
                selectedMauSacItems.length
                  ? 'border-rose-300 text-rose-600'
                  : 'text-slate-600'
              "
              @click="toggleVariantDropdown('mauSac')"
            >
              <span class="min-w-0 flex-1 text-left leading-5 whitespace-normal break-words">
                {{ mauSacSummary }}
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
                    placeholder="Tìm màu sắc..."
                    class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-10 pr-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                    @input="$emit('update:mau-sac-search', $event.target.value)"
                    @keydown.stop
                  />
                </div>
              </div>

              <div class="max-h-64 overflow-y-auto p-2">
                <button
                  v-if="selectedMauSacItems.length"
                  type="button"
                  class="mb-1 flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm text-slate-500 transition hover:bg-slate-50"
                  @click="clearSelectedValues('mauSacIds')"
                >
                  <span>Bỏ chọn tất cả</span>
                  <X :size="15" />
                </button>

                <button
                  v-for="item in filteredMauSacItems"
                  :key="item.id"
                  type="button"
                  class="flex w-full items-center justify-between gap-3 rounded-xl px-3 py-2 text-left text-sm transition hover:bg-slate-50"
                  :class="
                    isSelected('mauSacIds', item.id)
                      ? 'bg-rose-50 text-rose-600'
                      : 'text-slate-700'
                  "
                  @click="toggleSelectedValue('mauSacIds', item.id)"
                >
                  <div class="flex min-w-0 items-center gap-2">
                    <span
                      class="h-3 w-3 shrink-0 rounded-full border border-black/5"
                      :style="{ backgroundColor: item.maMauHex || '#e2e8f0' }"
                    ></span>
                    <span class="truncate">{{ item.ten }}</span>
                  </div>
                  <Check v-if="isSelected('mauSacIds', item.id)" :size="15" class="shrink-0" />
                </button>

                <div v-if="!filteredMauSacItems.length" class="rounded-xl px-3 py-6 text-center text-sm text-slate-400">
                  Không tìm thấy màu sắc phù hợp.
                </div>
              </div>
            </div>

            <p class="mt-2 text-xs text-slate-400">
              {{
                selectedMauSacItems.length
                  ? selectedMauSacItems.map((item) => item.ten).join(", ")
                  : "Chưa chọn màu sắc"
              }}
            </p>
            <p v-if="variantErrors.mauSacIds" class="mt-1 text-xs text-rose-500">
              {{ variantErrors.mauSacIds }}
            </p>
          </div>

          <div class="flex justify-start lg:justify-end">
            <button
              type="button"
              class="inline-flex items-center gap-1 rounded-full border border-rose-200 bg-white px-2.5 py-1 text-[11px] font-semibold text-rose-600 transition hover:bg-rose-50"
              @click.stop="openQuickCreate('mauSac', mauSacSearch)"
            >
              <Plus :size="12" />
              Thêm nhanh
            </button>
          </div>
        </div>
      </div>

      <div ref="kichCoDropdownRef" class="relative rounded-[20px] border border-slate-200 bg-white p-4" @click.stop>
        <div class="grid gap-3 lg:grid-cols-[120px_minmax(0,1fr)_auto] lg:items-start">
          <div>
            <label class="block text-[13px] font-semibold text-slate-700">Kích cỡ</label>
          </div>

          <div>
            <button
              type="button"
              class="flex min-h-11 w-full items-start justify-between gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm transition hover:bg-white"
              :class="
                selectedKichCoItems.length
                  ? 'border-rose-300 text-rose-600'
                  : 'text-slate-600'
              "
              @click="toggleVariantDropdown('kichCo')"
            >
              <span class="min-w-0 flex-1 text-left leading-5 whitespace-normal break-words">
                {{ kichCoSummary }}
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
                    placeholder="Tìm kích cỡ..."
                    class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-10 pr-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                    @input="$emit('update:kich-co-search', $event.target.value)"
                    @keydown.stop
                  />
                </div>
              </div>

              <div class="max-h-64 overflow-y-auto p-2">
                <button
                  v-if="selectedKichCoItems.length"
                  type="button"
                  class="mb-1 flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm text-slate-500 transition hover:bg-slate-50"
                  @click="clearSelectedValues('kichCoIds')"
                >
                  <span>Bỏ chọn tất cả</span>
                  <X :size="15" />
                </button>

                <button
                  v-for="item in filteredKichCoItems"
                  :key="item.id"
                  type="button"
                  class="flex w-full items-center justify-between gap-3 rounded-xl px-3 py-2 text-left text-sm transition hover:bg-slate-50"
                  :class="
                    isSelected('kichCoIds', item.id)
                      ? 'bg-rose-50 text-rose-600'
                      : 'text-slate-700'
                  "
                  @click="toggleSelectedValue('kichCoIds', item.id)"
                >
                  <span class="truncate">Size {{ item.giaTri }}</span>
                  <Check v-if="isSelected('kichCoIds', item.id)" :size="15" class="shrink-0" />
                </button>

                <div v-if="!filteredKichCoItems.length" class="rounded-xl px-3 py-6 text-center text-sm text-slate-400">
                  Không tìm thấy kích cỡ phù hợp.
                </div>
              </div>
            </div>

            <p class="mt-2 text-xs text-slate-400">
              {{
                selectedKichCoItems.length
                  ? selectedKichCoItems.map((item) => `Size ${item.giaTri}`).join(", ")
                  : "Chưa chọn kích cỡ"
              }}
            </p>
            <p v-if="variantErrors.kichCoIds" class="mt-1 text-xs text-rose-500">
              {{ variantErrors.kichCoIds }}
            </p>
          </div>

          <div class="flex justify-start lg:justify-end">
            <button
              type="button"
              class="inline-flex items-center gap-1 rounded-full border border-rose-200 bg-white px-2.5 py-1 text-[11px] font-semibold text-rose-600 transition hover:bg-rose-50"
              @click.stop="openQuickCreate('kichCo', kichCoSearch)"
            >
              <Plus :size="12" />
              Thêm nhanh
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
