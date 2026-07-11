<script setup>
import { Upload } from 'lucide-vue-next'
import { resolveHinhAnh } from '../../../utils/resolve-image'

const props = defineProps({
  form: {
    type: Object,
    required: true
  },
  errors: {
    type: Object,
    required: true
  },
  uploading: {
    type: Boolean,
    default: false
  },
  saving: {
    type: Boolean,
    default: false
  },
  isEditing: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['upload', 'close', 'save'])

function handleUploadFile(event) {
  emit('upload', event)
}
</script>

<template>
  <div class="mt-3 rounded-md border border-rose-100 bg-rose-50 p-3">
    <div class="flex flex-col gap-3 sm:flex-row">
      <div class="flex-shrink-0 w-32 space-y-2">
        <div v-if="form.url" class="overflow-hidden rounded-md border border-slate-200 bg-white aspect-square">
          <img :src="resolveHinhAnh(form.url)" alt="" class="h-full w-full object-cover" />
        </div>
        <label class="flex cursor-pointer items-center justify-center gap-1 rounded-md border border-dashed border-rose-200 bg-white px-2 py-2 text-xs font-medium text-rose-600 transition hover:border-rose-300 hover:bg-rose-100/50 text-center">
          <Upload :size="12" />
          {{ uploading ? 'Đang tải...' : 'Chọn ảnh' }}
          <input type="file" accept="image/*" class="hidden" @change="handleUploadFile" :disabled="uploading" />
        </label>
      </div>

      <div class="flex-1 space-y-2 flex flex-col justify-between">
        <div>
          <label class="mb-1 block text-[11px] font-medium text-slate-700">URL ảnh <span class="text-rose-500">*</span></label>
          <input
            v-model="form.url"
            type="url"
            placeholder="https://..."
            class="w-full rounded-md border px-2 py-1.5 text-xs focus:outline-none focus:ring-1 focus:ring-rose-400"
            :class="errors.url ? 'border-rose-300 bg-rose-50' : 'border-slate-200 bg-white'"
          />
          <p v-if="errors.url" class="mt-1 text-[10px] text-rose-500">{{ errors.url }}</p>
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <button
            type="button"
            class="rounded-md border border-slate-200 px-3 py-1.5 text-xs text-slate-600 transition hover:bg-slate-100"
            @click="emit('close')"
          >
            Hủy
          </button>
          <button
            type="button"
            class="rounded-md bg-rose-500 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-rose-600 disabled:opacity-60"
            :disabled="saving"
            @click="emit('save')"
          >
            {{ saving ? 'Đang lưu...' : (isEditing ? 'Cập nhật' : 'Lưu') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
