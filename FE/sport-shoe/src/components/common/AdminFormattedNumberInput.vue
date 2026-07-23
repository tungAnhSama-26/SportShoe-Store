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

const emit = defineEmits(['update:modelValue', 'blur', 'focus'])

const displayValue = ref('')

function formatNumber(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function sanitizeDigits(value) {
  return String(value ?? '').replace(/\D/g, '')
}

function syncDisplay(value) {
  if (value == null || value === '') {
    if (displayValue.value !== '') displayValue.value = ''
    return
  }

  const numericValue = Number(value)
  if (Number.isNaN(numericValue)) {
    if (displayValue.value !== '') displayValue.value = ''
    return
  }

  const formatted = formatNumber(Math.max(numericValue, props.min))
  if (displayValue.value !== formatted) {
    displayValue.value = formatted
  }
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
  const el = event.target
  const cursor = el.selectionStart
  const originalValue = el.value

  // Count digits before cursor
  let digitsBeforeCursor = 0
  for (let i = 0; i < cursor; i++) {
    if (/\d/.test(originalValue[i])) {
      digitsBeforeCursor++
    }
  }

  const rawDigits = sanitizeDigits(originalValue)

  if (!rawDigits) {
    displayValue.value = ''
    emitValue(rawDigits)
    return
  }

  const numericValue = Math.max(Number(rawDigits), props.min)
  const formattedValue = formatNumber(numericValue)

  if (displayValue.value !== formattedValue) {
    displayValue.value = formattedValue
  }
  emit('update:modelValue', numericValue)

  // Restore cursor position accurately
  nextTick(() => {
    let newCursor = 0
    let digitsSeen = 0
    for (let i = 0; i < formattedValue.length; i++) {
      if (digitsSeen === digitsBeforeCursor) break
      if (/\d/.test(formattedValue[i])) digitsSeen++
      newCursor++
    }
    el.setSelectionRange(newCursor, newCursor)
  })
}

function handleFocus() {
  emit('focus')
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
    @focus="handleFocus"
    @blur="handleBlur"
  />
</template>
