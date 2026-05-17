<template>
  <div class="bg-white rounded-2xl border border-slate-100 shadow-[0_2px_10px_-4px_rgba(0,0,0,0.05)] overflow-hidden transition-all duration-300 hover:shadow-[0_8px_20px_-6px_rgba(0,0,0,0.08)]">
    <!-- Header -->
    <div v-if="$slots.header || title" class="px-6 py-5 border-b border-slate-50 flex items-center justify-between">
      <div v-if="title" class="flex items-center gap-3">
        <slot name="icon"></slot>
        <h3 class="font-bold text-lg text-slate-800 tracking-tight">{{ title }}</h3>
      </div>
      <slot name="header"></slot>
      
      <div v-if="$slots.action">
        <slot name="action"></slot>
      </div>
    </div>
    
    <!-- Content -->
    <div :class="['px-6 py-5', noPadding ? '!p-0' : '']">
      <slot></slot>
    </div>
    
    <!-- Footer -->
    <div v-if="$slots.footer" class="px-6 py-4 bg-slate-50/50 border-t border-slate-50">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  noPadding: {
    type: Boolean,
    default: false
  }
});
</script>
