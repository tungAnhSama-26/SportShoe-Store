<script setup>
import { Trash2 } from "lucide-vue-next";
import AdminFormattedNumberInput from "../../common/AdminFormattedNumberInput.vue";
import { generateHexColorFromText, isValidHexColor } from "../../../utils/thuoc-tinh-san-pham";

const props = defineProps({
  group: {
    type: Object,
    required: true,
  },
  generatedVariantFieldErrors: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["remove-generated-variant"]);

function fieldErrorClass(errorMessage) {
  return errorMessage
    ? "border-rose-300 bg-rose-50"
    : "border-slate-200 bg-slate-50";
}

function resolveColorHex(group) {
  const explicitHex =
    group?.maMauHex || group?.variants?.find((item) => item?.maMauHex)?.maMauHex;
  if (isValidHexColor(explicitHex)) {
    return String(explicitHex).toUpperCase();
  }

  return generateHexColorFromText(group?.mauSac);
}

function formatColorName(value) {
  const normalized = String(value || "").trim().toLocaleLowerCase("vi-VN");
  if (!normalized) return "";
  return normalized.charAt(0).toLocaleUpperCase("vi-VN") + normalized.slice(1);
}

function isGroupAllSelected(group) {
  return group.variants.length > 0 && group.variants.every((v) => v.selected !== false);
}

function toggleGroupAll(group, selected) {
  group.variants.forEach((v) => {
    v.selected = selected;
  });
}
</script>

<template>
  <section class="rounded-[24px] border border-slate-200 bg-slate-50/80 p-4">
    <div class="mb-4 flex flex-col gap-2 border-b border-slate-200 pb-4 sm:flex-row sm:items-center sm:justify-between">
      <div class="flex items-center gap-3">
        <span
          class="h-4 w-4 rounded-full border border-slate-200 shadow-sm"
          :style="{ backgroundColor: resolveColorHex(group) }"
        ></span>
        <h3 class="text-sm font-medium text-slate-600">
          {{ formatColorName(group.mauSac) }}
        </h3>
      </div>

      <span class="inline-flex w-fit items-center rounded-full bg-white px-3 py-1 text-xs font-semibold text-slate-500 shadow-sm">
        {{ group.variants.map((item) => `Size ${item.kichCo}`).join(" • ") }}
      </span>
    </div>

    <div class="overflow-x-auto">
      <table class="min-w-full border-separate border-spacing-y-2 text-sm">
        <thead>
          <tr class="text-left text-sm font-bold text-slate-500">
            <th class="rounded-l-md bg-slate-100 px-4 py-3 w-[50px] text-center">
              <input type="checkbox" :checked="isGroupAllSelected(group)" @change="toggleGroupAll(group, $event.target.checked)" class="h-4 w-4 rounded border-slate-300 text-rose-500 focus:ring-rose-500 cursor-pointer" />
            </th>
            <th class="bg-slate-100 px-4 py-3 w-[60px] text-center">STT</th>
            <th class="bg-slate-100 px-4 py-3">Kích cỡ</th>
            <th class="bg-slate-100 px-4 py-3">Số lượng</th>
            <th class="bg-slate-100 px-4 py-3">Giá bán</th>
            <th class="rounded-r-md bg-slate-100 px-4 py-3 text-right">Xóa</th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="(item, index) in group.variants"
            :key="item.key"
            class="bg-white shadow-sm"
          >
            <td class="rounded-l-md px-4 py-4 text-center">
              <input type="checkbox" v-model="item.selected" class="h-4 w-4 rounded border-slate-300 text-rose-500 focus:ring-rose-500 cursor-pointer" />
            </td>
            <td class="px-4 py-4 text-center font-semibold text-slate-500">
              {{ index + 1 }}
            </td>
            <td class="px-4 py-4 font-semibold text-slate-700">
              Size {{ item.kichCo }}
            </td>
            <td class="px-4 py-4">
              <AdminFormattedNumberInput
                v-model="item.soLuong"
                :min="0"
                class="h-10 w-28 rounded-md border px-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                :class="fieldErrorClass(generatedVariantFieldErrors[item.key]?.soLuong)"
              />
              <p v-if="generatedVariantFieldErrors[item.key]?.soLuong" class="mt-1 text-xs text-rose-500">
                {{ generatedVariantFieldErrors[item.key].soLuong }}
              </p>
            </td>
            <td class="px-4 py-4">
              <AdminFormattedNumberInput
                v-model="item.giaBan"
                @update:modelValue="item.giaGoc = item.giaBan"
                :min="0"
                class="h-10 w-36 rounded-md border px-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                :class="fieldErrorClass(generatedVariantFieldErrors[item.key]?.giaBan)"
              />
              <p v-if="generatedVariantFieldErrors[item.key]?.giaBan" class="mt-1 text-xs text-rose-500">
                {{ generatedVariantFieldErrors[item.key].giaBan }}
              </p>
            </td>
            <td class="rounded-r-md px-4 py-4">
              <div class="flex justify-end">
                <button
                  type="button"
                  class="inline-flex h-10 w-10 items-center justify-center rounded-md bg-rose-50 text-rose-600 transition hover:bg-rose-100"
                  @click="emit('remove-generated-variant', item.key)"
                >
                  <Trash2 :size="15" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
