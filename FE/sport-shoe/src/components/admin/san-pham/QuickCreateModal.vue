<script setup>
import { Save, X } from 'lucide-vue-next'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  definition: {
    type: Object,
    default: null
  },
  form: {
    type: Object,
    default: () => ({})
  },
  errors: {
    type: Object,
    default: () => ({})
  },
  saving: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'save', 'update:form'])

function handleInput(field, value) {
  emit('update:form', { ...props.form, [field]: value })
}
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="show && definition"
        class="fixed inset-0 z-[90] flex items-center justify-center bg-slate-950/45 p-4"
        @click.self="$emit('close')"
      >
        <div class="w-full max-w-xl overflow-hidden rounded-[28px] bg-white shadow-2xl">
          <div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5">
            <div>
              <h2 class="text-xl font-black text-slate-900">
                {{ definition.title }}
              </h2>
              <p v-if="definition.description" class="mt-1 text-sm text-slate-500">
                {{ definition.description }}
              </p>
            </div>

            <button
              type="button"
              class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-100 text-slate-500 transition hover:bg-slate-200"
              @click="$emit('close')"
            >
              <X :size="16" />
            </button>
          </div>

          <div class="p-6">
            <div class="grid gap-4 md:grid-cols-2">
              <div
                v-for="field in definition.fields"
                :key="field.key"
                :class="field.type === 'color' ? 'md:col-span-2' : ''"
              >
                <label class="mb-1 block text-[13px] font-semibold text-slate-500">
                  {{ field.label }}
                </label>

                <div v-if="field.type === 'color'" class="flex flex-col gap-3">
                  <div class="flex items-center gap-3">
                    <input
                      :value="form[field.key]"
                      type="color"
                      class="h-11 w-16 rounded-2xl border border-slate-200 bg-white p-1"
                      @input="handleInput(field.key, String($event.target.value || '').toUpperCase())"
                    />
                    <input
                      :value="form[field.key]"
                      type="text"
                      readonly
                      class="h-11 flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none"
                      placeholder="#000000"
                    />
                  </div>
                </div>

                <input
                  v-else
                  :value="form[field.key]"
                  :type="field.type || 'text'"
                  :min="field.min"
                  :readonly="field.readonly"
                  :disabled="saving"
                  class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                  :class="[
                    errors[field.key]
                      ? 'border-rose-300 bg-rose-50'
                      : field.readonly
                        ? 'border-slate-200 bg-slate-100 text-slate-500'
                        : 'border-slate-200 bg-slate-50',
                    field.uppercase ? 'uppercase' : '',
                  ]"
                  :placeholder="field.placeholder"
                  @input="
                    handleInput(field.key, field.uppercase
                      ? String($event.target.value || '').toUpperCase()
                      : $event.target.value)
                  "
                />

                <p v-if="errors[field.key]" class="mt-1 text-xs text-rose-500">
                  {{ errors[field.key] }}
                </p>
              </div>
            </div>

            <p v-if="errors.general" class="mt-4 text-sm text-rose-500">
              {{ errors.general }}
            </p>
          </div>

          <div class="flex items-center justify-end gap-3 border-t border-slate-100 px-6 py-4">
            <button type="button" class="admin-btn-soft" @click="$emit('close')">
              Hủy
            </button>

            <button
              type="button"
              class="admin-btn-primary disabled:opacity-60"
              :disabled="saving"
              @click="$emit('save')"
            >
              <Save :size="16" />
              {{ saving ? 'Đang thêm...' : 'Thêm vào form' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
