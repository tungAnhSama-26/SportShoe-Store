<script setup>
import { ref } from 'vue';
import { Eye, EyeOff } from 'lucide-vue-next';

defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  inputClass: { type: String, default: '' },
  autocomplete: { type: String, default: 'off' },
});
const emit = defineEmits(['update:modelValue']);

const hien = ref(false);
</script>

<template>
  <div class="relative">
    <input
      :type="hien ? 'text' : 'password'"
      :value="modelValue"
      :placeholder="placeholder"
      :autocomplete="autocomplete"
      :class="[inputClass, 'pr-10']"
      @input="emit('update:modelValue', $event.target.value)"
    />
    <button
      type="button"
      tabindex="-1"
      :title="hien ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
      class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 transition hover:text-slate-600"
      @click="hien = !hien"
    >
      <EyeOff v-if="hien" class="h-4 w-4" />
      <Eye v-else class="h-4 w-4" />
    </button>
  </div>
</template>
