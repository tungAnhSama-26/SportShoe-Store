<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: null
  },
  min: {
    type: Number,
    default: 0
  },
  allowEmpty: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'blur'])

const displayValue = ref('')

function formatNumber(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function sanitizeDigits(value) {
  return String(value ?? '').replace(/\D/g, '')
}

function syncDisplay(value) {
  if (value == null || value === '') {
    displayValue.value = ''
    return
  }

  const numericValue = Number(value)
  if (Number.isNaN(numericValue)) {
    displayValue.value = ''
    return
  }

  displayValue.value = formatNumber(Math.max(numericValue, props.min))
}

function emitValue(rawDigits) {
  if (!rawDigits) {
    emit('update:modelValue', props.allowEmpty ? null : props.min)
    return
  }

  const numericValue = Math.max(Number(rawDigits), props.min)
  emit('update:modelValue', numericValue)
}

function handleInput(event) {
  const rawDigits = sanitizeDigits(event.target.value)

  if (!rawDigits) {
    displayValue.value = ''
    emitValue(rawDigits)
    return
  }

  const numericValue = Math.max(Number(rawDigits), props.min)
  displayValue.value = formatNumber(numericValue)
  emit('update:modelValue', numericValue)
}

function handleBlur() {
  if (!displayValue.value && !props.allowEmpty) {
    displayValue.value = formatNumber(props.min)
    emit('update:modelValue', props.min)
  }
  emit('blur')
}

watch(
  () => props.modelValue,
  (value) => {
    syncDisplay(value)
  },
  { immediate: true }
)
</script>

<template>
  <input
    :value="displayValue"
    type="text"
    inputmode="numeric"
    autocomplete="off"
    @input="handleInput"
    @blur="handleBlur"
  />
</template>
