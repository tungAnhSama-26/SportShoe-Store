<script setup>
import AdminFormattedNumberInput from "../../common/AdminFormattedNumberInput.vue";

const props = defineProps({
  variantBuilder: {
    type: Object,
    required: true,
  },
  defaultFieldErrors: {
    type: Object,
    required: true,
  },
  variantErrors: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["apply-defaults"]);

function fieldErrorClass(errorMessage) {
  return errorMessage
    ? "border-rose-300 bg-rose-50"
    : "border-slate-200 bg-slate-50";
}

function resolveFieldError(localError, parentError) {
  return localError || parentError || "";
}
</script>

<template>
  <div class="mb-4 grid gap-4 md:grid-cols-[1fr_1fr_1fr_auto]">
    <label class="block">
      <span class="mb-1 block text-[13px] font-semibold text-slate-500">
        Số lượng mặc định
      </span>
      <AdminFormattedNumberInput
        v-model="variantBuilder.soLuong"
        :min="0"
        class="h-11 w-full rounded-md border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
        :class="
          fieldErrorClass(
            resolveFieldError(defaultFieldErrors.soLuong, variantErrors.soLuong),
          )
        "
      />
      <p
        v-if="resolveFieldError(defaultFieldErrors.soLuong, variantErrors.soLuong)"
        class="mt-1 text-xs text-rose-500"
      >
        {{ resolveFieldError(defaultFieldErrors.soLuong, variantErrors.soLuong) }}
      </p>
    </label>

    <label class="block">
      <span class="mb-1 block text-[13px] font-semibold text-slate-500">
        Giá gốc mặc định
      </span>
      <AdminFormattedNumberInput
        v-model="variantBuilder.giaGoc"
        :min="0"
        class="h-11 w-full rounded-md border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
        :class="
          fieldErrorClass(
            resolveFieldError(defaultFieldErrors.giaGoc, variantErrors.giaGoc),
          )
        "
      />
      <p
        v-if="resolveFieldError(defaultFieldErrors.giaGoc, variantErrors.giaGoc)"
        class="mt-1 text-xs text-rose-500"
      >
        {{ resolveFieldError(defaultFieldErrors.giaGoc, variantErrors.giaGoc) }}
      </p>
    </label>

    <label class="block">
      <span class="mb-1 block text-[13px] font-semibold text-slate-500">
        Giá bán mặc định <span class="text-rose-500">*</span>
      </span>
      <AdminFormattedNumberInput
        v-model="variantBuilder.giaBan"
        :min="0"
        class="h-11 w-full rounded-md border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
        :class="
          fieldErrorClass(
            resolveFieldError(defaultFieldErrors.giaBan, variantErrors.giaBan),
          )
        "
      />
      <p
        v-if="resolveFieldError(defaultFieldErrors.giaBan, variantErrors.giaBan)"
        class="mt-1 text-xs text-rose-500"
      >
        {{ resolveFieldError(defaultFieldErrors.giaBan, variantErrors.giaBan) }}
      </p>
    </label>

    <div class="flex items-end">
      <button
        type="button"
        class="admin-btn-soft h-11"
        @click="emit('apply-defaults')"
      >
        Áp dụng
      </button>
    </div>
  </div>
</template>
