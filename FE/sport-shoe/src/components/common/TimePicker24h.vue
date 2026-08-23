<script setup>
import { ref, watch } from 'vue';
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
const hourInput = ref(null);
const minuteInput = ref(null);
const dangCapNhatTuNoiBo = ref(false);

watch(() => props.modelValue, (newVal) => {
  if (dangCapNhatTuNoiBo.value) {
    dangCapNhatTuNoiBo.value = false;
    return;
  }

  if (newVal && newVal.includes(':')) {
    const [h, m] = newVal.split(':');
    localHour.value = layGioHopLe(h);
    localMinute.value = layPhutHopLe(m);
  } else if (!newVal) {
    localHour.value = '';
    localMinute.value = '';
  }
}, { immediate: true });

function layGioHopLe(value) {
  let val = String(value ?? '').replace(/\D/g, '').slice(0, 2);
  if (val === '') return '';
  const num = Number.parseInt(val, 10);
  if (Number.isNaN(num)) return '';
  if (num > 23) return '23';
  return val;
}

function layPhutHopLe(value) {
  let val = String(value ?? '').replace(/\D/g, '').slice(0, 2);
  if (val === '') return '';
  const num = Number.parseInt(val, 10);
  if (Number.isNaN(num)) return '';
  if (num > 59) return '59';
  return val;
}

function updateValue() {
  if (localHour.value === '' && localMinute.value === '') {
    dangCapNhatTuNoiBo.value = true;
    emit('update:modelValue', '');
    return;
  }
  const h = localHour.value !== '' ? String(localHour.value).padStart(2, '0') : '00';
  const m = localMinute.value !== '' ? String(localMinute.value).padStart(2, '0') : '00';
  dangCapNhatTuNoiBo.value = true;
  emit('update:modelValue', `${h}:${m}`);
}

function onHourInput(e) {
  localHour.value = layGioHopLe(e.target.value);
  updateValue();
}

function onHourBlur() {
  if (localHour.value !== '') {
    localHour.value = String(localHour.value).padStart(2, '0');
  }
  updateValue();
}

function onMinuteInput(e) {
  localMinute.value = layPhutHopLe(e.target.value);
  updateValue();
}

function onMinuteBlur() {
  if (localMinute.value !== '') {
    localMinute.value = String(localMinute.value).padStart(2, '0');
  }
  updateValue();
}

function onHourKeyDown(e) {
  if (e.key === ':' || e.key === 'ArrowRight') {
    e.preventDefault();
    minuteInput.value?.focus();
  }
}

function onMinuteKeyDown(e) {
  if (e.key === 'ArrowLeft' && localMinute.value.length === 0) {
    e.preventDefault();
    hourInput.value?.focus();
  }
}

function focusHour(event) {
  if (event?.target?.tagName === 'INPUT') return;
  hourInput.value?.focus();
}
</script>

<template>
  <div class="flex items-center w-full h-full px-2 bg-white transition group cursor-text" @click="focusHour">
    <input 
      ref="hourInput"
      type="text" 
      v-model="localHour" 
      @input="onHourInput"
      @blur="onHourBlur"
      @keydown="onHourKeyDown"
      class="w-8 bg-transparent text-center outline-none text-[14px] placeholder-slate-400"
      placeholder="--"
      maxlength="2"
      inputmode="numeric"
      aria-label="Giờ"
    />
    <span class="text-slate-400 font-bold mx-0.5">:</span>
    <input 
      ref="minuteInput"
      type="text" 
      v-model="localMinute" 
      @input="onMinuteInput"
      @blur="onMinuteBlur"
      @keydown="onMinuteKeyDown"
      class="w-8 bg-transparent text-center outline-none text-[14px] placeholder-slate-400"
      placeholder="--"
      maxlength="2"
      inputmode="numeric"
      aria-label="Phút"
    />
    <Clock class="ml-auto w-4 h-4 text-slate-400 pointer-events-none" />
  </div>
</template>
