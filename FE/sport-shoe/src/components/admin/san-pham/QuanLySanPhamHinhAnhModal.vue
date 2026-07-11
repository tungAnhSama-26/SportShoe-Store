<script setup>
import { X } from 'lucide-vue-next'
import QuanLyAnhBienThe from './QuanLyAnhBienThe.vue'

defineProps({
  open: {
    type: Boolean,
    default: false
  },
  variant: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'updated', 'error'])
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open && variant"
      class="fixed inset-0 z-[70] flex items-center justify-center bg-black/60 p-4"
    >
      <div class="flex max-h-[90vh] w-full max-w-4xl flex-col rounded-md bg-white shadow-2xl">
        <div class="flex items-center justify-between border-b border-gray-100 px-6 py-4">
          <h2 class="text-lg font-semibold text-gray-800">
            Ảnh biến thể - {{ variant.mauSac }} / {{ variant.kichCo }}
          </h2>
          <button class="rounded-md p-1.5 hover:bg-gray-100" @click="emit('close')">
            <X :size="18" />
          </button>
        </div>

        <div class="flex-1 overflow-y-auto p-6">
          <QuanLyAnhBienThe
            :variant="variant"
            @updated="emit('updated')"
            @error="emit('error', $event)"
          />
        </div>
      </div>
    </div>
  </Teleport>
</template>
