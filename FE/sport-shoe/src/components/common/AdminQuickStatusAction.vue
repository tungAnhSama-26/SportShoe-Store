<script setup>
import { computed } from 'vue'
import { LoaderCircle, Power } from 'lucide-vue-next'
import { showConfirm } from '../../utils/alert'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  actionLabel: {
    type: String,
    default: 'Đổi trạng thái nhanh'
  },
  disabledTitle: {
    type: String,
    default: ''
  },
  confirmMessage: {
    type: String,
    default: ''
  },
  intent: {
    type: String,
    default: 'deactivate'
  }
})

const emit = defineEmits(['toggle'])

const buttonTitle = computed(() => {
  if (props.loading) return 'Đang cập nhật...'
  if (props.disabled && props.disabledTitle) return props.disabledTitle
  return props.actionLabel
})

const intentClass = computed(() => {
  if (props.disabled) return 'admin-quick-status-action--disabled'
  if (props.intent === 'activate') return 'admin-quick-status-action--activate'
  if (props.intent === 'deactivate') return 'admin-quick-status-action--deactivate'
  return 'admin-quick-status-action--neutral'
})

async function handleClick() {
  if (props.loading || props.disabled) return
  if (props.confirmMessage && !(await showConfirm(props.confirmMessage))) return
  emit('toggle')
}
</script>

<template>
  <button
    type="button"
    :disabled="loading || disabled"
    class="admin-table-action admin-quick-status-action disabled:cursor-not-allowed disabled:opacity-60"
    :class="intentClass"
    :title="buttonTitle"
    :aria-label="buttonTitle"
    @click="handleClick"
  >
    <LoaderCircle v-if="loading" class="h-4 w-4 animate-spin" />
    <Power v-else class="h-4 w-4" />
    <span class="sr-only">{{ buttonTitle }}</span>
  </button>
</template>

<style scoped>
.admin-quick-status-action--activate {
  color: #64748b;
}

.admin-quick-status-action--activate:hover {
  color: #e11d48;
}

.admin-quick-status-action--deactivate {
  color: #e11d48;
}

.admin-quick-status-action--deactivate:hover {
  color: #be123c;
}

.admin-quick-status-action--neutral {
  color: #64748b;
}

.admin-quick-status-action--neutral:hover {
  color: #475569;
}

.admin-quick-status-action--disabled {
  color: #cbd5e1;
}
</style>
