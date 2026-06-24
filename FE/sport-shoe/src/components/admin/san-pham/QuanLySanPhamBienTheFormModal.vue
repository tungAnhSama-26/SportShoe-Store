<script setup>
import { computed } from 'vue'
import QuanLySanPhamBienTheCapNhatForm from './QuanLySanPhamBienTheCapNhatForm.vue'
import QuanLySanPhamBienTheTaoMoiForm from './QuanLySanPhamBienTheTaoMoiForm.vue'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  editingBienThe: {
    type: Object,
    default: null
  },
  selectedGiay: {
    type: Object,
    default: null
  },
  danhMuc: {
    type: Object,
    default: null
  },
  bienTheForm: {
    type: Object,
    required: true
  },
  bienTheErrors: {
    type: Object,
    required: true
  },
  bulkBienTheForm: {
    type: Object,
    required: true
  },
  bulkBienTheErrors: {
    type: Object,
    required: true
  },
  generatedBulkBienThes: {
    type: Array,
    default: () => []
  },
  savingBienThe: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'close',
  'save',
  'save-bulk',
  'image-updated',
  'generate-bulk',
  'remove-generated-bulk'
])

const isUpdateMode = computed(() => !!props.editingBienThe)
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center p-4 sm:p-6 bg-slate-900/40 backdrop-blur-sm transition-opacity">
    <div
      class="relative w-full max-w-5xl overflow-hidden rounded-[24px] bg-white shadow-[0_32px_64px_-16px_rgba(0,0,0,0.1),0_16px_32px_-8px_rgba(0,0,0,0.05)] transition-all ring-1 ring-slate-200"
      @click.stop
    >
      <QuanLySanPhamBienTheCapNhatForm
        v-if="isUpdateMode"
        :editing-bien-the="editingBienThe"
        :selected-giay="selectedGiay"
        :bien-the-form="bienTheForm"
        :bien-the-errors="bienTheErrors"
        :saving-bien-the="savingBienThe"
        @close="emit('close')"
        @save="emit('save')"
        @image-updated="emit('image-updated')"
      />
      
      <QuanLySanPhamBienTheTaoMoiForm
        v-else
        :selected-giay="selectedGiay"
        :danh-muc="danhMuc"
        :bulk-bien-the-form="bulkBienTheForm"
        :bulk-bien-the-errors="bulkBienTheErrors"
        :generated-bulk-bien-thes="generatedBulkBienThes"
        :saving-bien-the="savingBienThe"
        @close="emit('close')"
        @save="emit('save-bulk')"
        @generate-bulk="emit('generate-bulk')"
        @remove-generated-bulk="emit('remove-generated-bulk')"
      />
    </div>
  </div>
</template>
