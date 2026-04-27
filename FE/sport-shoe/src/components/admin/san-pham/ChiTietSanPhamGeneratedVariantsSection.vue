<script setup>
import { computed, ref } from "vue";
import { Package2, Save, Trash2 } from "lucide-vue-next";
import BienTheImageManager from "./BienTheImageManager.vue";
import AdminFormattedNumberInput from "../../common/AdminFormattedNumberInput.vue";

const props = defineProps({
  generatedVariants: {
    type: Array,
    required: true,
  },
  representativeGeneratedVariants: {
    type: Array,
    default: () => [],
  },
  variantBuilder: {
    type: Object,
    required: true,
  },
  variantErrors: {
    type: Object,
    required: true,
  },
  draftColorImages: {
    type: Object,
    default: () => ({}),
  },
  saving: {
    type: Boolean,
    default: false,
  },
  isExistingProduct: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits([
  "apply-defaults",
  "remove-generated-variant",
  "save",
  "change-draft-images",
  "error",
]);

const saveButtonLabel = computed(() => {
  if (props.saving) return "Đang lưu...";
  return props.isExistingProduct
    ? "Lưu thay đổi và thêm CTSP"
    : "Lưu sản phẩm và CTSP";
});

const draftImageManagerRefs = ref({});

function parseNumericValue(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function buildNegativeFieldError(label, value) {
  return parseNumericValue(value) < 0 ? `${label} không được âm` : "";
}

const defaultFieldErrors = computed(() => ({
  soLuong: buildNegativeFieldError(
    "Số lượng mặc định",
    props.variantBuilder.soLuong,
  ),
  giaGoc: buildNegativeFieldError(
    "Giá gốc mặc định",
    props.variantBuilder.giaGoc,
  ),
  giaBan: buildNegativeFieldError(
    "Giá bán mặc định",
    props.variantBuilder.giaBan,
  ),
}));

const generatedVariantFieldErrors = computed(() =>
  Object.fromEntries(
    props.generatedVariants.map((item) => [
      item.key,
      {
        soLuong: buildNegativeFieldError("Số lượng", item.soLuong),
        giaGoc: buildNegativeFieldError("Giá gốc", item.giaGoc),
        giaBan: buildNegativeFieldError("Giá bán", item.giaBan),
      },
    ]),
  ),
);

const hasDefaultFieldErrors = computed(() =>
  Object.values(defaultFieldErrors.value).some(Boolean),
);

const hasGeneratedVariantFieldErrors = computed(() =>
  Object.values(generatedVariantFieldErrors.value).some((fieldErrors) =>
    Object.values(fieldErrors).some(Boolean),
  ),
);

function fieldErrorClass(errorMessage) {
  return errorMessage
    ? "border-rose-300 bg-rose-50"
    : "border-slate-200 bg-slate-50";
}

function resolveFieldError(localError, parentError) {
  return localError || parentError || "";
}

function relatedVariants(mauSacId) {
  return props.generatedVariants.filter(
    (item) => Number(item.mauSacId) === Number(mauSacId),
  );
}

function draftImagesForColor(mauSacId) {
  return props.draftColorImages[String(mauSacId)] || [];
}

function handleDraftImagesChange(mauSacId, images) {
  emit("change-draft-images", { mauSacId, images });
}

function setDraftImageManagerRef(mauSacId, instance) {
  const colorKey = String(mauSacId);
  if (instance) {
    draftImageManagerRefs.value[colorKey] = instance;
    return;
  }

  delete draftImageManagerRefs.value[colorKey];
}

async function commitPendingDraftImages() {
  for (const item of props.representativeGeneratedVariants) {
    const manager = draftImageManagerRefs.value[String(item.mauSacId)];
    if (!manager?.commitPendingForm) continue;

    const committed = await manager.commitPendingForm();
    if (!committed) {
      return false;
    }
  }

  return true;
}

function handleApplyDefaults() {
  if (hasDefaultFieldErrors.value) {
    emit(
      "error",
      "Vui lòng sửa các giá trị mặc định đang bị âm trước khi áp dụng.",
    );
    return;
  }

  emit("apply-defaults");
}

async function handleSaveClick() {
  if (hasDefaultFieldErrors.value || hasGeneratedVariantFieldErrors.value) {
    emit("error", "Vui lòng sửa các giá trị âm trước khi lưu.");
    return;
  }

  const committed = await commitPendingDraftImages();
  if (!committed) {
    return;
  }

  emit("save");
}
</script>

<template>
  <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
    <p v-if="variantErrors.generated" class="text-sm text-rose-500">
      {{ variantErrors.generated }}
    </p>

    <div
      v-if="generatedVariants.length"
      class="mb-4 grid gap-4 md:grid-cols-[1fr_1fr_1fr_auto]"
    >
      <label class="block">
        <span class="mb-1 block text-[13px] font-semibold text-slate-500">
          Số lượng mặc định
        </span>
        <AdminFormattedNumberInput
          v-model="variantBuilder.soLuong"
          :min="0"
          class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
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
          class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
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
          Giá bán mặc định *
        </span>
        <AdminFormattedNumberInput
          v-model="variantBuilder.giaBan"
          :min="0"
          class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
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
          @click="handleApplyDefaults"
        >
          Áp dụng
        </button>
      </div>
    </div>

    <div v-if="generatedVariants.length" class="overflow-x-auto">
      <table class="min-w-full border-separate border-spacing-y-2 text-sm">
        <thead>
          <tr class="text-left text-sm font-bold text-slate-500">
            <th class="rounded-l-2xl bg-slate-100 px-4 py-3">Màu sắc</th>
            <th class="bg-slate-100 px-4 py-3">Kích cỡ</th>
            <th class="bg-slate-100 px-4 py-3">Số lượng</th>
            <th class="bg-slate-100 px-4 py-3">Giá gốc</th>
            <th class="bg-slate-100 px-4 py-3">Giá bán</th>
            <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-right">
              Xóa
            </th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="item in generatedVariants"
            :key="item.key"
            class="bg-white shadow-sm"
          >
            <td class="rounded-l-2xl px-4 py-4 font-semibold text-slate-800">
              {{ item.mauSac }}
            </td>
            <td class="px-4 py-4 font-semibold text-slate-700">
              Size {{ item.kichCo }}
            </td>
            <td class="px-4 py-4">
              <AdminFormattedNumberInput
                v-model="item.soLuong"
                :min="0"
                class="h-10 w-28 rounded-2xl border px-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                :class="
                  fieldErrorClass(generatedVariantFieldErrors[item.key]?.soLuong)
                "
              />
              <p
                v-if="generatedVariantFieldErrors[item.key]?.soLuong"
                class="mt-1 text-xs text-rose-500"
              >
                {{ generatedVariantFieldErrors[item.key].soLuong }}
              </p>
            </td>
            <td class="px-4 py-4">
              <AdminFormattedNumberInput
                v-model="item.giaGoc"
                :min="0"
                class="h-10 w-36 rounded-2xl border px-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                :class="
                  fieldErrorClass(generatedVariantFieldErrors[item.key]?.giaGoc)
                "
              />
              <p
                v-if="generatedVariantFieldErrors[item.key]?.giaGoc"
                class="mt-1 text-xs text-rose-500"
              >
                {{ generatedVariantFieldErrors[item.key].giaGoc }}
              </p>
            </td>
            <td class="px-4 py-4">
              <AdminFormattedNumberInput
                v-model="item.giaBan"
                :min="0"
                class="h-10 w-36 rounded-2xl border px-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                :class="
                  fieldErrorClass(generatedVariantFieldErrors[item.key]?.giaBan)
                "
              />
              <p
                v-if="generatedVariantFieldErrors[item.key]?.giaBan"
                class="mt-1 text-xs text-rose-500"
              >
                {{ generatedVariantFieldErrors[item.key].giaBan }}
              </p>
            </td>
            <td class="rounded-r-2xl px-4 py-4">
              <div class="flex justify-end">
                <button
                  type="button"
                  class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-rose-50 text-rose-600 transition hover:bg-rose-100"
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

    <div
      v-if="representativeGeneratedVariants.length"
      class="mt-6 rounded-[24px] border border-rose-100 bg-rose-50/60 p-5"
    >
      <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 class="text-xl font-black text-slate-900">
            Ảnh theo màu cho biến thể nháp
          </h2>
          <p class="mt-2 text-sm text-slate-600">
            Mỗi form ảnh chỉ đại diện cho một màu và sẽ được áp cho toàn bộ kích
            cỡ cùng màu sau khi lưu CTSP.
          </p>
        </div>
      </div>

      <div class="mt-5 grid gap-5">
        <BienTheImageManager
          v-for="item in representativeGeneratedVariants"
          :key="`draft-color-${item.mauSacId}`"
          :ref="(instance) => setDraftImageManagerRef(item.mauSacId, instance)"
          :variant="item"
          :draft-images="draftImagesForColor(item.mauSacId)"
          :related-variants="relatedVariants(item.mauSacId)"
          display-mode="color"
          @change-draft-images="handleDraftImagesChange(item.mauSacId, $event)"
          @error="emit('error', $event)"
        />
      </div>
    </div>

    <div
      v-if="!generatedVariants.length"
      class="mt-5 rounded-[24px] border border-dashed border-slate-200 bg-slate-50 px-4 py-12 text-center"
    >
      <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-[24px] bg-white text-slate-300 shadow-sm">
        <Package2 :size="24" />
      </div>
      <p class="mt-4 text-base font-semibold text-slate-700">
        Chưa có biến thể nháp
      </p>
      <p class="mt-1 text-sm text-slate-400">
        Chọn màu sắc, kích cỡ trong bộ lọc rồi bấm "Tạo biến thể tự động".
      </p>
    </div>

    <div class="mt-6 flex justify-end">
      <button
        type="button"
        class="admin-btn-primary disabled:opacity-60"
        :disabled="saving"
        @click="handleSaveClick"
      >
        <Save :size="16" />
        {{ saveButtonLabel }}
      </button>
    </div>
  </section>
</template>
