<script setup>
import { Pencil, Star, Trash2 } from 'lucide-vue-next'
import { resolveHinhAnh } from '../../../utils/resolve-image'

const props = defineProps({
  displayedImages: {
    type: Array,
    default: () => []
  },
  settingMainId: {
    type: [String, Number],
    default: null
  },
  deletingId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['edit', 'set-main', 'delete'])
</script>

<template>
  <div class="grid gap-3 grid-cols-2 sm:grid-cols-3">
    <div
      v-for="item in displayedImages"
      :key="item.id"
      class="group overflow-hidden rounded-md border border-slate-200 bg-slate-50"
    >
      <div class="relative aspect-square">
        <img :src="resolveHinhAnh(item.url)" :alt="item.moTa || ''" class="h-full w-full object-cover" />
        <div
          v-if="item.laHinhChinh"
          class="absolute left-3 top-3 inline-flex items-center gap-1 rounded-full bg-rose-200 px-2.5 py-1 text-[11px] font-semibold text-rose-800"
        >
          <Star :size="12" />
          Ảnh chính
        </div>
      </div>

      <div class="flex items-center justify-between gap-2 border-t border-slate-200 px-3 py-2">
        <p class="truncate text-xs text-slate-500">
          {{ item.moTa || 'Không có mô tả' }}
        </p>
        <div class="flex items-center gap-1">
          <button
            type="button"
            class="inline-flex h-8 w-8 items-center justify-center rounded-md bg-blue-100 text-blue-700 transition hover:bg-blue-200 disabled:opacity-60"
            title="Sửa ảnh"
            @click="emit('edit', item)"
          >
            <Pencil :size="14" />
          </button>
          <button
            v-if="!item.laHinhChinh"
            type="button"
            class="inline-flex h-8 w-8 items-center justify-center rounded-md bg-rose-100 text-rose-700 transition hover:bg-rose-200 disabled:opacity-60"
            :disabled="settingMainId === item.id"
            title="Đặt ảnh chính"
            @click="emit('set-main', item.id)"
          >
            <Star :size="14" />
          </button>
          <button
            type="button"
            class="inline-flex h-8 w-8 items-center justify-center rounded-md bg-rose-100 text-rose-600 transition hover:bg-rose-200 disabled:opacity-60"
            :disabled="deletingId === item.id"
            title="Xóa ảnh"
            @click="emit('delete', item.id)"
          >
            <Trash2 :size="14" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
