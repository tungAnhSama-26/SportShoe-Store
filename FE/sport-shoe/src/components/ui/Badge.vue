<template>
  <span
    :class="[
      'inline-flex items-center justify-center rounded-full px-2.5 py-1 text-xs font-bold tracking-tight whitespace-nowrap border',
      variantClasses
    ]"
  >
    <slot name="prefix"></slot>
    <slot></slot>
    <slot name="suffix"></slot>
  </span>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'primary', 'success', 'warning', 'danger', 'info'].includes(val)
  }
});

const variantClasses = computed(() => {
  switch (props.variant) {
    case 'primary':
      return 'bg-primary/10 text-primary border-primary/20';
    case 'success':
      return 'bg-emerald-50 text-emerald-600 border-emerald-200';
    case 'warning':
      return 'bg-amber-50 text-amber-600 border-amber-200';
    case 'danger':
      return 'bg-rose-50 text-rose-600 border-rose-200';
    case 'info':
      return 'bg-blue-50 text-blue-600 border-blue-200';
    default:
      return 'bg-slate-100 text-slate-600 border-slate-200';
  }
});
</script>
