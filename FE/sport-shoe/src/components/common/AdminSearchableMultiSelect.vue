<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Check, ChevronDown, Search, X } from 'lucide-vue-next'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
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
    default: 'Bỏ chọn tất cả'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  error: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const rootRef = ref(null)
const searchInputRef = ref(null)
const isOpen = ref(false)
const query = ref('')

const normalizedValue = computed(() => Array.isArray(props.modelValue) ? props.modelValue : [])

const normalizedOptions = computed(() =>
  (props.options || []).map((item) => ({
    ...item,
    searchText: String(item.searchText || `${item.label || ''} ${item.description || ''}`).toLowerCase()
  }))
)

const selectedValueSet = computed(() => new Set(normalizedValue.value))

const selectedOptions = computed(() =>
  normalizedOptions.value.filter((item) => selectedValueSet.value.has(item.value))
)

const filteredOptions = computed(() => {
  const keyword = query.value.trim().toLowerCase()
  if (!keyword) return normalizedOptions.value
  return normalizedOptions.value.filter((item) => item.searchText.includes(keyword))
})

const displayLabel = computed(() => {
  if (!selectedOptions.value.length) return props.placeholder
  if (selectedOptions.value.length === 1) return selectedOptions.value[0].label
  return `${selectedOptions.value[0].label} +${selectedOptions.value.length - 1}`
})

function openDropdown() {
  if (props.disabled) return
  isOpen.value = true
  nextTick(() => searchInputRef.value?.focus())
}

function closeDropdown() {
  isOpen.value = false
  query.value = ''
}

function toggleDropdown() {
  if (isOpen.value) {
    closeDropdown()
    return
  }

  openDropdown()
}

function toggleValue(value) {
  const nextValues = [...normalizedValue.value]
  const index = nextValues.findIndex((item) => item === value)

  if (index >= 0) {
    nextValues.splice(index, 1)
  } else {
    nextValues.push(value)
  }

  emit('update:modelValue', nextValues)
}

function clearSelection() {
  emit('update:modelValue', [])
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

defineExpose({
  openDropdown,
  closeDropdown,
  clearSelection
})
</script>

<template>
  <div ref="rootRef" class="relative">
    <button
      type="button"
      :disabled="disabled"
      class="flex min-h-11 w-full items-center justify-between gap-3 rounded-lg border bg-white px-3 py-2 text-left text-sm transition"
      :class="[
        error ? 'border-red-400' : 'border-gray-200',
        disabled
          ? 'cursor-not-allowed bg-slate-100 text-slate-400'
          : 'hover:border-slate-300 focus:outline-none focus:ring-2 focus:ring-rose-400'
      ]"
      @click="toggleDropdown"
    >
      <div class="min-w-0 flex-1 truncate" :class="selectedOptions.length ? 'text-slate-700' : 'text-slate-400'">
        {{ displayLabel }}
      </div>

      <div class="flex items-center gap-1.5">
        <button
          v-if="selectedOptions.length"
          type="button"
          class="inline-flex h-6 w-6 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
          @click.stop="clearSelection"
        >
          <X :size="14" />
        </button>
        <ChevronDown :size="16" class="shrink-0 text-slate-400" />
      </div>
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
            @keydown.stop
          />
        </div>
      </div>

      <div class="max-h-64 overflow-y-auto p-2">
        <button
          v-if="normalizedValue.length"
          type="button"
          class="mb-1 flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm text-slate-500 transition hover:bg-slate-50"
          @click="clearSelection"
        >
          <span>{{ clearLabel }}</span>
          <X :size="15" />
        </button>

        <button
          v-for="option in filteredOptions"
          :key="`${option.value}`"
          type="button"
          class="flex w-full items-start justify-between gap-3 rounded-xl px-3 py-2 text-left text-sm transition hover:bg-slate-50"
          :class="selectedValueSet.has(option.value) ? 'bg-rose-50 text-rose-600' : 'text-slate-700'"
          @click="toggleValue(option.value)"
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
          <Check v-if="selectedValueSet.has(option.value)" :size="15" class="mt-0.5 shrink-0" />
        </button>

        <div
          v-if="!filteredOptions.length"
          class="rounded-xl px-3 py-6 text-center text-sm text-slate-400"
        >
          {{ emptyLabel }}
        </div>
      </div>
    </div>
  </div>
</template>
