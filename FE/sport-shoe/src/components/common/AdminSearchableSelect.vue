<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Check, ChevronDown, Plus, Search } from 'lucide-vue-next'

const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: null
  },
  options: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: 'Chọn dữ liệu'
  },
  searchPlaceholder: {
    type: String,
    default: 'Nhập để tìm...'
  },
  emptyLabel: {
    type: String,
    default: 'Không có dữ liệu phù hợp'
  },
  clearLabel: {
    type: String,
    default: 'Bỏ chọn'
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  error: {
    type: Boolean,
    default: false
  },
  allowCreate: {
    type: Boolean,
    default: false
  },
  creating: {
    type: Boolean,
    default: false
  },
  createLabel: {
    type: String,
    default: 'Thêm mới'
  }
})

const emit = defineEmits(['update:modelValue', 'create'])

const rootRef = ref(null)
const searchInputRef = ref(null)
const isOpen = ref(false)
const query = ref('')

function normalizeText(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[đĐ]/g, (char) => (char === 'đ' ? 'd' : 'D'))
    .toLowerCase()
    .trim()
}

const normalizedOptions = computed(() =>
  (props.options || []).map((item) => ({
    ...item,
    searchText: normalizeText(item.searchText || `${item.label || ''} ${item.description || ''}`),
    createMatchText: normalizeText(item.createMatchText || item.label || '')
  }))
)

function isSameOptionValue(left, right) {
  if (left == null || right == null) return false
  if (left === right) return true

  const leftNumber = Number(left)
  const rightNumber = Number(right)
  if (Number.isFinite(leftNumber) && Number.isFinite(rightNumber)) {
    return leftNumber === rightNumber
  }

  return String(left) === String(right)
}

const selectedOption = computed(() =>
  normalizedOptions.value.find((item) => isSameOptionValue(item.value, props.modelValue)) || null
)

const filteredOptions = computed(() => {
  const keyword = normalizeText(query.value)
  if (!keyword) return normalizedOptions.value
  return normalizedOptions.value.filter((item) => item.searchText.includes(keyword))
})

const normalizedQuery = computed(() => query.value.trim())

const hasExactMatch = computed(() => {
  const keyword = normalizeText(normalizedQuery.value)
  if (!keyword) return false

  return normalizedOptions.value.some((item) =>
    item.createMatchText === keyword
  )
})

const showCreateOption = computed(() =>
  props.allowCreate && Boolean(normalizedQuery.value) && !hasExactMatch.value
)

function openDropdown() {
  if (props.disabled) return
  isOpen.value = true
  nextTick(() => searchInputRef.value?.focus())
}

function toggleDropdown() {
  if (isOpen.value) {
    isOpen.value = false
    query.value = ''
    return
  }
  openDropdown()
}

function closeDropdown() {
  isOpen.value = false
  query.value = ''
}

function selectValue(value) {
  emit('update:modelValue', value)
  closeDropdown()
}

function handleCreate() {
  if (!showCreateOption.value || props.creating) return
  const value = normalizedQuery.value
  closeDropdown()
  emit('create', value)
}

function handleSearchKeydown(event) {
  if (event.key !== 'Enter') return
  event.preventDefault()

  if (filteredOptions.value.length > 0) {
    selectValue(filteredOptions.value[0].value)
    return
  }

  if (showCreateOption.value && !props.creating) {
    handleCreate()
  }
}

function handleDocumentClick(event) {
  if (!rootRef.value?.contains(event.target)) {
    closeDropdown()
  }
}

watch(
  () => props.modelValue,
  () => {
    if (!isOpen.value) {
      query.value = ''
    }
  }
)

onMounted(() => {
  document.addEventListener('mousedown', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentClick)
})
</script>

<template>
  <div ref="rootRef" class="relative">
    <button
      type="button"
      :disabled="disabled"
      class="flex h-11 w-full items-center justify-between gap-3 rounded-lg border bg-white px-3 py-2 text-left text-sm transition"
      :class="[
        error ? 'border-red-400' : 'border-gray-200',
        disabled
          ? 'cursor-not-allowed bg-slate-100 text-slate-400'
          : 'hover:border-slate-300 focus:outline-none focus:ring-2 focus:ring-rose-400'
      ]"
      @click="toggleDropdown"
    >
      <span class="truncate" :class="selectedOption ? 'text-slate-700' : 'text-slate-400'">
        {{ selectedOption?.label || placeholder }}
      </span>
      <ChevronDown :size="16" class="shrink-0 text-slate-400" />
    </button>

    <div
      v-if="isOpen"
      class="absolute left-0 top-full z-40 mt-2 w-full overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl"
    >
      <div class="border-b border-slate-100 p-3">
        <div class="relative">
          <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            ref="searchInputRef"
            v-model="query"
            type="text"
            :placeholder="searchPlaceholder"
            class="h-10 w-full rounded-xl border border-slate-200 bg-slate-50 pl-10 pr-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            @keydown.stop="handleSearchKeydown"
          />
        </div>
      </div>

      <div class="max-h-64 overflow-y-auto p-2">
        <button
          v-if="showCreateOption"
          type="button"
          class="mb-1 flex w-full items-center justify-between rounded-xl border border-dashed border-rose-200 bg-rose-50 px-3 py-2 text-left text-sm font-medium text-rose-600 transition hover:bg-rose-100"
          :disabled="creating"
          @mousedown.prevent="handleCreate"
        >
          <span class="truncate">
            {{ creating ? 'Đang thêm...' : `${createLabel} "${normalizedQuery}"` }}
          </span>
          <Plus :size="15" class="shrink-0" />
        </button>

        <button
          v-if="allowClear && modelValue != null"
          type="button"
          class="mb-1 flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm text-slate-500 transition hover:bg-slate-50"
          @mousedown.prevent="selectValue(null)"
        >
          <span>{{ clearLabel }}</span>
          <Check :size="15" />
        </button>

        <button
          v-for="option in filteredOptions"
          :key="`${option.value}`"
          type="button"
          class="flex w-full items-start justify-between gap-3 rounded-xl px-3 py-2 text-left text-sm transition hover:bg-slate-50"
          :class="isSameOptionValue(option.value, modelValue) ? 'bg-rose-50 text-rose-600' : 'text-slate-700'"
          @mousedown.prevent="selectValue(option.value)"
        >
          <div class="min-w-0">
            <div class="flex items-center gap-2">
              <span
                v-if="option.color"
                class="h-3 w-3 shrink-0 rounded-full border border-black/5"
                :style="{ backgroundColor: option.color }"
              ></span>
              <span class="truncate">{{ option.label }}</span>
            </div>
            <p v-if="option.description" class="mt-1 truncate text-xs text-slate-400">
              {{ option.description }}
            </p>
          </div>
          <Check v-if="isSameOptionValue(option.value, modelValue)" :size="15" class="mt-0.5 shrink-0" />
        </button>

        <div
          v-if="!filteredOptions.length && !showCreateOption"
          class="rounded-xl px-3 py-6 text-center text-sm text-slate-400"
        >
          {{ emptyLabel }}
        </div>
      </div>
    </div>
  </div>
</template>
