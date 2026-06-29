<script setup>
import { computed } from 'vue';
import { danhGiaMatKhau } from '../../utils/do-manh-mat-khau';

const props = defineProps({
  matKhau: { type: String, default: '' },
});

const kq = computed(() => danhGiaMatKhau(props.matKhau));
</script>

<template>
  <div v-if="kq" class="mt-1.5 flex items-center gap-2">
    <div class="h-1.5 flex-1 overflow-hidden rounded-full bg-slate-200">
      <div
        class="h-full rounded-full transition-all duration-300"
        :class="kq.barClass"
        :style="{ width: kq.phanTram + '%' }"
      ></div>
    </div>
    <span class="shrink-0 text-xs font-semibold" :class="kq.textClass">Độ mạnh: {{ kq.nhan }}</span>
  </div>
</template>
