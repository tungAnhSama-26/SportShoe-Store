<script setup>
import { computed, ref, watch } from 'vue';
import { Clock } from 'lucide-vue-next';

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '--:--'
  }
});

const emit = defineEmits(['update:modelValue']);

const localHour = ref('');
const localMinute = ref('');

// Sync from props
watch(() => props.modelValue, (newVal) => {
  if (newVal && newVal.includes(':')) {
    const [h, m] = newVal.split(':');
    localHour.value = h;
    localMinute.value = m;
  } else if (!newVal) {
    localHour.value = '';
    localMinute.value = '';
  }
}, { immediate: true });

function updateValue() {
  if (localHour.value === '' && localMinute.value === '') {
    emit('update:modelValue', '');
    return;
  }
  const h = localHour.value !== '' ? String(localHour.value).padStart(2, '0') : '00';
  const m = localMinute.value !== '' ? String(localMinute.value).padStart(2, '0') : '00';
  emit('update:modelValue', `${h}:${m}`);
}

function onHourInput(e) {
  let val = e.target.value.replace(/\D/g, ''); // Chỉ cho phép nhập số
  if (val !== '') {
    let num = parseInt(val, 10);
    if (num < 0) val = '00';
    if (num > 23) val = '23'; // Không cho nhập quá 23
  }
  localHour.value = val;
  updateValue();
}

function onHourBlur() {
  if (localHour.value !== '') {
    localHour.value = String(localHour.value).padStart(2, '0');
  }
  updateValue();
}

function onMinuteInput(e) {
  let val = e.target.value.replace(/\D/g, ''); // Chỉ cho phép nhập số
  if (val !== '') {
    let num = parseInt(val, 10);
    if (num < 0) val = '00';
    if (num > 59) val = '59'; // Không cho nhập quá 59
  }
  localMinute.value = val;
  updateValue();
}

function onMinuteBlur() {
  if (localMinute.value !== '') {
    localMinute.value = String(localMinute.value).padStart(2, '0');
  }
  updateValue();
}

// Chuyển focus sang phút khi gõ ':' hoặc phím phải
function onHourKeyDown(e) {
  if (e.key === ':' || e.key === 'ArrowRight') {
    e.preventDefault();
    document.getElementById('sportshoe-minute-input')?.focus();
  }
}

// Chuyển focus sang giờ khi bấm phím trái
function onMinuteKeyDown(e) {
  if (e.key === 'ArrowLeft' && localMinute.value.length === 0) {
    e.preventDefault();
    document.getElementById('sportshoe-hour-input')?.focus();
  }
}
</script>

<template>
  <div class="flex items-center w-full h-full px-2 bg-white transition group cursor-text" @click="() => document.getElementById('sportshoe-hour-input')?.focus()">
    <input 
      id="sportshoe-hour-input"
      type="text" 
      v-model="localHour" 
      @input="onHourInput"
      @blur="onHourBlur"
      @keydown="onHourKeyDown"
      class="w-8 bg-transparent text-center outline-none text-[14px] placeholder-slate-400"
      placeholder="--"
      maxlength="2"
    />
    <span class="text-slate-400 font-bold mx-0.5">:</span>
    <input 
      id="sportshoe-minute-input"
      type="text" 
      v-model="localMinute" 
      @input="onMinuteInput"
      @blur="onMinuteBlur"
      @keydown="onMinuteKeyDown"
      class="w-8 bg-transparent text-center outline-none text-[14px] placeholder-slate-400"
      placeholder="--"
      maxlength="2"
    />
    <Clock class="ml-auto w-4 h-4 text-slate-400 pointer-events-none" />
  </div>
</template>
