<template>
  <div class="flex flex-col gap-1.5 w-full">
    <label v-if="label" :for="id" class="text-sm font-medium text-slate-700">
      {{ label }} <span v-if="required" class="text-primary">*</span>
    </label>
    
    <div class="relative flex items-center">
      <div v-if="$slots.prefix" class="absolute left-3 text-slate-400 flex items-center pointer-events-none">
        <slot name="prefix"></slot>
      </div>
      
      <input
        :id="id"
        :type="type"
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        :placeholder="placeholder"
        :disabled="disabled"
        :required="required"
        :class="[
          'w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 outline-none transition-all duration-200',
          'placeholder:text-slate-400',
          'focus:border-primary/50 focus:ring-4 focus:ring-primary/10',
          'disabled:cursor-not-allowed disabled:bg-slate-50 disabled:text-slate-500',
          $slots.prefix ? 'pl-10' : '',
          $slots.suffix ? 'pr-10' : '',
          error ? 'border-rose-500 focus:border-rose-500 focus:ring-rose-500/10' : ''
        ]"
        v-bind="$attrs"
      />
      
      <div v-if="$slots.suffix" class="absolute right-3 text-slate-400 flex items-center">
        <slot name="suffix"></slot>
      </div>
    </div>
    
    <p v-if="error" class="text-xs font-medium text-rose-500 mt-0.5">
      {{ error }}
    </p>
    <p v-else-if="hint" class="text-xs text-slate-500 mt-0.5">
      {{ hint }}
    </p>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  id: {
    type: String,
    default: () => `input-${Math.random().toString(36).substring(2, 9)}`
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  },
  hint: {
    type: String,
    default: ''
  }
});

defineEmits(['update:modelValue']);
</script>
