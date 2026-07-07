<script setup>
import { ref, watch, nextTick } from 'vue'

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
const isFocused = ref(false)

function formatNumber(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function sanitizeDigits(value) {
  return String(value ?? '').replace(/\D/g, '')
}

function syncDisplay() {
  if (props.modelValue == null || props.modelValue === '') {
    displayValue.value = ''
    return
  }
  
  const numericValue = Number(props.modelValue)
  if (Number.isNaN(numericValue)) {
    displayValue.value = ''
    return
  }

  if (isFocused.value) {
    // Show raw number when focused
    displayValue.value = numericValue.toString()
  } else {
    // Show formatted number when not focused
    displayValue.value = formatNumber(Math.max(numericValue, props.min))
  }
}

function handleInput(event) {
  const rawDigits = sanitizeDigits(event.target.value)

  if (!rawDigits) {
    displayValue.value = ''
    emit('update:modelValue', props.allowEmpty ? null : props.min)
    return
  }

  const numericValue = Math.max(Number(rawDigits), props.min)
  // When focused, we just update displayValue to raw digits, which matches what user types mostly
  displayValue.value = rawDigits // keep what they typed
  emit('update:modelValue', numericValue)
}

function handleFocus() {
  isFocused.value = true
  syncDisplay()
}

function handleBlur() {
  isFocused.value = false
  if (!displayValue.value && !props.allowEmpty) {
    displayValue.value = formatNumber(props.min)
    emit('update:modelValue', props.min)
  } else {
    syncDisplay()
  }
  emit('blur')
}

watch(
  () => props.modelValue,
  () => {
    syncDisplay()
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
    @focus="handleFocus"
    @blur="handleBlur"
  />
</template>
