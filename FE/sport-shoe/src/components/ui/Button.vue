<template>
  <button
    :class="[
      'inline-flex items-center justify-center gap-2 rounded-[6px] font-semibold transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 disabled:cursor-not-allowed disabled:opacity-60',
      variantClasses,
      sizeClasses,
      fullWidth ? 'w-full' : '',
    ]"
    :disabled="disabled || loading"
    v-bind="$attrs"
  >
    <span v-if="loading" class="animate-spin">
      <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
    </span>
    <slot name="prefix" v-if="!loading"></slot>
    <slot></slot>
    <slot name="suffix"></slot>
  </button>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (val) => ['primary', 'secondary', 'outline', 'ghost', 'danger', 'soft'].includes(val),
  },
  size: {
    type: String,
    default: 'md',
    validator: (val) => ['sm', 'md', 'lg', 'icon'].includes(val),
  },
  fullWidth: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const variantClasses = computed(() => {
  switch (props.variant) {
    case 'primary':
      return 'bg-gradient-to-r from-rose-500 to-red-500 text-white shadow-[0_14px_30px_rgba(239,68,68,0.28)] hover:-translate-y-0.5 hover:from-rose-600 hover:to-red-500 hover:shadow-[0_18px_34px_rgba(239,68,68,0.32)] border border-transparent';
    case 'secondary':
      return 'bg-secondary text-white shadow-md shadow-secondary/20 hover:shadow-lg hover:shadow-secondary/30 hover:-translate-y-px border border-transparent';
    case 'outline':
      return 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-50 hover:text-primary hover:border-primary/30 shadow-sm';
    case 'soft':
      return 'border border-rose-200 bg-white text-rose-500 shadow-[0_10px_24px_rgba(244,63,94,0.08)] hover:border-rose-300 hover:bg-rose-50/70 hover:text-rose-600';
    case 'danger':
      return 'bg-rose-500 text-white shadow-md shadow-rose-500/20 hover:bg-rose-600 border border-transparent';
    case 'ghost':
      return 'bg-transparent text-slate-600 hover:bg-slate-100 hover:text-slate-900 border border-transparent';
    default:
      return '';
  }
});

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm':
      return 'h-8 px-3 text-xs';
    case 'md':
      return 'h-11 px-5 text-sm';
    case 'lg':
      return 'h-14 px-8 text-base';
    case 'icon':
      return 'h-11 w-11 p-0 flex items-center justify-center';
    default:
      return 'h-11 px-5 text-sm';
  }
});
</script>
