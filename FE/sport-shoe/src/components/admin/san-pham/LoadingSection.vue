<script setup>
const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <section
    v-if="loading"
    class="rounded-[24px] border border-slate-200 bg-white p-10 text-center text-slate-400 shadow-sm"
  >
    Đang tải dữ liệu...
  </section>
</template>