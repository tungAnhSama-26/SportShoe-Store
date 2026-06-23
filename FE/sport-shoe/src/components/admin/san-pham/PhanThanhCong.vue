<script setup>
import { CheckCircle2, ArrowLeft } from 'lucide-vue-next'
import QuanLyAnhBienThe from '../../../components/admin/san-pham/QuanLyAnhBienThe.vue'

const props = defineProps({
  representativeCreatedVariants: {
    type: Array,
    default: () => []
  },
  createdImageManagerRefs: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['go-back', 'set-created-image-manager-ref', 'toast'])

function setCreatedImageManagerRef(mauSacId, instance) {
  emit('set-created-image-manager-ref', mauSacId, instance)
}

function relatedCreatedVariants(mauSacId) {
  // Assuming this function is passed or computed elsewhere
  return props.representativeCreatedVariants.filter(v => v.mauSacId === mauSacId)
}
</script>

<template>
  <section
    v-if="representativeCreatedVariants.length"
    class="rounded-[24px] border border-emerald-100 bg-emerald-50/60 p-5 shadow-sm"
  >
    <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
      <div>
        <div class="inline-flex items-center gap-2 rounded-full bg-white px-3 py-1 text-sm font-semibold text-emerald-700 shadow-sm">
          <CheckCircle2 :size="16" />
          Lưu chi tiết sản phẩm thành công
        </div>
        <h2 class="mt-3 text-2xl font-black text-slate-900">
          Thêm ảnh theo màu đại diện
        </h2>
        <p class="mt-2 text-sm text-slate-600">
          Mỗi màu chỉ hiển thị một biến thể đại diện để bạn thêm ảnh nhanh
          cho sản phẩm. Bạn đang có
          {{ representativeCreatedVariants.length }} màu cần bổ sung ảnh.
        </p>
      </div>

      <button type="button" class="admin-btn-soft" @click="$emit('go-back')">
        <ArrowLeft :size="16" />
        Hoàn tất và quay lại danh sách
      </button>
    </div>

    <div class="mt-6 grid gap-5">
      <QuanLyAnhBienThe
        v-for="item in representativeCreatedVariants"
        :key="item.id"
        :ref="(instance) => setCreatedImageManagerRef(item.mauSacId, instance)"
        :variant="item"
        :related-variants="relatedCreatedVariants(item.mauSacId)"
        display-mode="color"
        @updated="$emit('toast', 'Cập nhật ảnh thành công')"
        @error="$emit('toast', $event, 'error')"
      />
    </div>
  </section>
</template>