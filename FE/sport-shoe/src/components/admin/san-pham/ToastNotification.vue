<script setup>
const props = defineProps({
  toast: {
    type: Object,
    default: () => ({ show: false, message: '', type: 'success' })
  }
})
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="toast.show"
        class="fixed right-5 top-5 z-[100] rounded-2xl px-4 py-3 text-sm font-medium text-white shadow-lg"
        :class="toast.type === 'error' ? 'bg-rose-500' : 'bg-emerald-500'"
      >
        {{ toast.message }}
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>