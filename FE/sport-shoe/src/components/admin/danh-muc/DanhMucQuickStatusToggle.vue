<script setup>
import { computed } from 'vue'

const props = defineProps({
  trangThai: {
    type: Number,
    default: 0
  },
  loading: {
    type: Boolean,
    default: false
  },
  activeLabel: {
    type: String,
    default: 'Hoạt động'
  },
  inactiveLabel: {
    type: String,
    default: 'Dừng'
  },
  activeActionLabel: {
    type: String,
    default: 'Dừng nhanh'
  },
  inactiveActionLabel: {
    type: String,
    default: 'Bật nhanh'
  }
})

const emit = defineEmits(['toggle'])

const isActive = computed(() => props.trangThai === 1)
const statusLabel = computed(() => (isActive.value ? props.activeLabel : props.inactiveLabel))
const actionLabel = computed(() => (isActive.value ? props.activeActionLabel : props.inactiveActionLabel))
</script>

<template>
  <div class="flex flex-col items-center gap-1.5">
    <button
      type="button"
      :disabled="loading"
      class="admin-status-chip transition disabled:cursor-not-allowed disabled:opacity-60"
      :class="isActive ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-500'"
      @click="emit('toggle')"
    >
      {{ loading ? 'Đang cập nhật...' : statusLabel }}
    </button>

    <button
      type="button"
      :disabled="loading"
      class="text-[11px] font-semibold transition disabled:cursor-not-allowed disabled:opacity-60"
      :class="isActive ? 'text-emerald-600 hover:text-emerald-700' : 'text-rose-600 hover:text-rose-700'"
      @click="emit('toggle')"
    >
      {{ loading ? 'Vui lòng chờ...' : actionLabel }}
    </button>
  </div>
</template>
