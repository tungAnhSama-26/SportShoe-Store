<script setup>
import { ArrowLeft } from 'lucide-vue-next'

const props = defineProps({
  pageTitle: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['go-back'])
</script>

<template>
  <section class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
    <h1 class="text-[30px] font-bold tracking-tight text-slate-800">
      {{ pageTitle }}
    </h1>

    <button type="button" class="admin-btn-soft" @click="$emit('go-back')">
      <ArrowLeft :size="16" />
      Quay lại danh sách
    </button>
  </section>
</template>