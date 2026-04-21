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

const isActive = computed(() => props.trangThai === 1)
const statusLabel = computed(() => (isActive.value ? props.activeLabel : props.inactiveLabel))
</script>

<template>
  <span
    class="admin-status-chip transition disabled:cursor-not-allowed disabled:opacity-60"
    :class="isActive ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-500'"
  >
    {{ loading ? 'Đang cập nhật...' : statusLabel }}
  </span>
</template>
