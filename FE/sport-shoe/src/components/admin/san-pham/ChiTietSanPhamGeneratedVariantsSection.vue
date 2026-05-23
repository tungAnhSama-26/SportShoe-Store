<script setup>
import { computed, ref } from "vue";
import { Package2, Save, Trash2, X } from "lucide-vue-next";
import BienTheImageManager from "./BienTheImageManager.vue";
import AdminFormattedNumberInput from "../../common/AdminFormattedNumberInput.vue";
import {
  generateHexColorFromText,
  isValidHexColor,
} from "../../../utils/thuoc-tinh-san-pham";

const props = defineProps({
  generatedVariants: {
    type: Array,
    default: () => [],
  },
  representativeGeneratedVariants: {
    type: Array,
    default: () => [],
  },
  variantBuilder: {
    type: Object,
    default: () => ({
      soLuong: "",
      giaGoc: "",
      giaBan: ""
    }),
  },
  variantErrors: {
    type: Object,
    default: () => ({}),
  },
  draftVariantImages: {
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
const showSaveConfirmModal = ref(false);

function parseNumericValue(value) {
  if (value === null || value === undefined || value === "") return 0;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function buildNumberFieldError(label, value, { allowZero = true } = {}) {
  const parsed = parseNumericValue(value);
  if (parsed < 0) return `${label} không được âm`;
  if (!allowZero && parsed <= 0) return `${label} phải lớn hơn 0`;
  return "";
}

const defaultFieldErrors = computed(() => ({
  soLuong: buildNumberFieldError(
    "Số lượng mặc định",
    props.variantBuilder.soLuong,
  ),
  giaGoc: (() => {
    const priceError = buildNumberFieldError(
      "Giá gốc mặc định",
      props.variantBuilder.giaGoc,
      { allowZero: false },
    );
    if (priceError) return priceError;
    const giaGoc = parseNumericValue(props.variantBuilder.giaGoc);
    const giaBan = parseNumericValue(props.variantBuilder.giaBan);
    return giaGoc > 0 && giaBan > 0 && giaGoc > giaBan
      ? "Giá gốc mặc định không được lớn hơn giá bán mặc định"
      : "";
  })(),
  giaBan: (() => {
    const priceError = buildNumberFieldError(
      "Giá bán mặc định",
      props.variantBuilder.giaBan,
      { allowZero: false },
    );
    return priceError;
  })(),
}));

const generatedVariantFieldErrors = computed(() =>
  Object.fromEntries(
    props.generatedVariants.map((item) => {
      const giaGocError = buildNumberFieldError("Giá gốc", item.giaGoc, {
        allowZero: false,
      });
      const giaBanError = buildNumberFieldError("Giá bán", item.giaBan, {
        allowZero: false,
      });

      return [
        item.key,
        {
          soLuong: buildNumberFieldError("Số lượng", item.soLuong),
          giaGoc:
            giaGocError ||
            (parseNumericValue(item.giaGoc) > 0 &&
            parseNumericValue(item.giaBan) > 0 &&
            parseNumericValue(item.giaGoc) > parseNumericValue(item.giaBan)
              ? "Giá gốc không được lớn hơn giá bán"
              : ""),
          giaBan: giaBanError,
        },
      ];
    }),
  ),
);

function resolveColorGroupKey(item) {
  return Number(item?.mauSacId || 0) || item?.mauSac || item?.key;
}

const generatedVariantGroups = computed(() => {
  const groupedVariants = new Map();

  props.generatedVariants.forEach((item) => {
    const colorKey = resolveColorGroupKey(item);

    if (!groupedVariants.has(colorKey)) {
      groupedVariants.set(colorKey, {
        key: colorKey,
        mauSacId: item.mauSacId,
        mauSac: item.mauSac || "Màu chưa đặt tên",
        maMauHex: item.maMauHex || null,
        variants: [],
      });
    }

    groupedVariants.get(colorKey).variants.push(item);
  });

  return Array.from(groupedVariants.values());
});

const hasDefaultFieldErrors = computed(() =>
  Object.values(defaultFieldErrors.value).some(Boolean),
);

const hasGeneratedVariantFieldErrors = computed(() =>
  Object.values(generatedVariantFieldErrors.value).some((fieldErrors) =>
    Object.values(fieldErrors).some(Boolean),
  ),
);

const saveConfirmationDetails = computed(() => {
  const variantCount = props.generatedVariants.length;
  const imageColorCount = Object.values(props.draftVariantImages || {}).filter(
    (images) => Array.isArray(images) && images.length,
  ).length;

  const lines = [
    `${variantCount} biến thể sẽ được tạo.`,
  ];

  if (imageColorCount > 0) {
    lines.push(`${imageColorCount} biến thể có ảnh đính kèm.`);
  }

  return {
    title: props.isExistingProduct
      ? "Bạn có muốn thêm chi tiết sản phẩm không?"
      : "Bạn có muốn lưu sản phẩm không?",
    description: "Xem lại trước khi xác nhận.",
    lines,
  };
});

function fieldErrorClass(errorMessage) {
  return errorMessage
    ? "border-rose-300 bg-rose-50"
    : "border-slate-200 bg-slate-50";
}

function resolveFieldError(localError, parentError) {
  return localError || parentError || "";
}

function collectValidationMessages(errors, limit = 4) {
  const messages = Object.values(errors || {})
    .flatMap((value) => {
      if (typeof value === "string") return [value];
      if (value && typeof value === "object") return Object.values(value);
      return [];
    })
    .filter((value) => typeof value === "string" && value.trim())
    .map((value) => value.trim());

  const uniqueMessages = [...new Set(messages)];
  if (!uniqueMessages.length) return "";

  const visibleMessages = uniqueMessages.slice(0, limit).join("; ");
  const hiddenCount = uniqueMessages.length - limit;
  return hiddenCount > 0
    ? `${visibleMessages}; và ${hiddenCount} lỗi khác`
    : visibleMessages;
}

function relatedVariants(mauSacId) {
  return props.generatedVariants.filter(
    (item) => Number(item.mauSacId) === Number(mauSacId),
  );
}

function draftImagesForVariant(variantKey) {
  return props.draftVariantImages[String(variantKey)] || [];
}

function handleDraftImagesChange(variantKey, images) {
  emit("change-draft-images", { variantKey, images });
}

function setDraftImageManagerRef(variantKey, instance) {
  const draftKey = String(variantKey);
  if (instance) {
    draftImageManagerRefs.value[draftKey] = instance;
    return;
  }

  delete draftImageManagerRefs.value[draftKey];
}

async function commitPendingDraftImages() {
  for (const item of props.generatedVariants) {
    const manager = draftImageManagerRefs.value[String(item.key)];
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
    const detailMessage = collectValidationMessages(defaultFieldErrors.value);
    emit(
      "error",
      detailMessage
        ? `Vui lòng sửa giá trị mặc định: ${detailMessage}`
        : "Vui lòng sửa số lượng và giá mặc định trước khi áp dụng.",
    );
    return;
  }

  emit("apply-defaults");
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

function closeSaveConfirmModal() {
  if (props.saving) return;
  showSaveConfirmModal.value = false;
}

function confirmSave() {
  showSaveConfirmModal.value = false;
  emit("save");
}

async function handleSaveClick() {
  if (hasDefaultFieldErrors.value || hasGeneratedVariantFieldErrors.value) {
    const detailMessage = collectValidationMessages({
      ...defaultFieldErrors.value,
      ...generatedVariantFieldErrors.value,
    });
    emit(
      "error",
      detailMessage
        ? `Vui lòng sửa biến thể: ${detailMessage}`
        : "Vui lòng sửa số lượng và giá của biến thể trước khi lưu.",
    );
    return;
  }

  const committed = await commitPendingDraftImages();
  if (!committed) {
    return;
  }

  showSaveConfirmModal.value = true;
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

    <div v-if="generatedVariantGroups.length" class="space-y-4">
      <section
        v-for="group in generatedVariantGroups"
        :key="`generated-group-${group.key}`"
        class="rounded-[24px] border border-slate-200 bg-slate-50/80 p-4"
      >
        <div
          class="mb-4 flex flex-col gap-2 border-b border-slate-200 pb-4 sm:flex-row sm:items-center sm:justify-between"
        >
          <div class="flex items-center gap-3">
            <span
              class="h-4 w-4 rounded-full border border-slate-200 shadow-sm"
              :style="{ backgroundColor: resolveColorHex(group) }"
            ></span>
            <h3 class="text-sm font-medium text-slate-600">
              {{ formatColorName(group.mauSac) }}
            </h3>
          </div>

          <span
            class="inline-flex w-fit items-center rounded-full bg-white px-3 py-1 text-xs font-semibold text-slate-500 shadow-sm"
          >
            {{ group.variants.map((item) => `Size ${item.kichCo}`).join(" • ") }}
          </span>
        </div>

        <div class="overflow-x-auto">
          <table class="min-w-full border-separate border-spacing-y-2 text-sm">
            <thead>
              <tr class="text-left text-sm font-bold text-slate-500">
                <th class="rounded-l-2xl bg-slate-100 px-4 py-3">Kích cỡ</th>
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
                v-for="item in group.variants"
                :key="item.key"
                class="bg-white shadow-sm"
              >
                <td class="rounded-l-2xl px-4 py-4 font-semibold text-slate-700">
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
      </section>
    </div>

    <div
      v-if="generatedVariants.length"
      class="mt-6 rounded-[24px] border border-rose-100 bg-rose-50/60 p-5"
    >
      <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 class="text-sm font-medium text-slate-600">
            Ảnh sản phẩm chi tiết
          </h2>
          <p class="mt-1 text-sm text-slate-500">
            Thêm ảnh cho từng kích cỡ của màu sắc (biến thể) nếu muốn.
          </p>
        </div>
      </div>

      <div class="mt-5 grid gap-5">
        <BienTheImageManager
          v-for="item in generatedVariants"
          :key="`draft-variant-${item.key}`"
          :ref="(instance) => setDraftImageManagerRef(item.key, instance)"
          :variant="item"
          :draft-images="draftImagesForVariant(item.key)"
          :related-variants="[]"
          display-mode="variant"
          @change-draft-images="handleDraftImagesChange(item.key, $event)"
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
        Chưa có biến thể
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

    <Teleport to="body">
      <Transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="showSaveConfirmModal"
          class="fixed inset-0 z-[120] flex items-center justify-center bg-slate-950/50 p-4"
          @click.self="closeSaveConfirmModal"
        >
          <Transition
            enter-active-class="transition duration-200 ease-out"
            enter-from-class="opacity-0 scale-95"
            enter-to-class="opacity-100 scale-100"
          >
            <div v-if="showSaveConfirmModal" class="w-full max-w-md overflow-hidden rounded-[24px] bg-white shadow-2xl">

              <!-- Header -->
              <div class="px-6 pt-6 pb-5 flex items-center justify-between">
                <h3 class="text-[17px] font-bold text-slate-900">{{ saveConfirmationDetails.title }}</h3>
                <button
                  type="button"
                  class="inline-flex h-8 w-8 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
                  @click="closeSaveConfirmModal"
                >
                  <X :size="16" />
                </button>
              </div>

              <!-- Actions -->
              <div class="flex items-center gap-2 border-t border-slate-100 px-6 py-4">
                <button
                  type="button"
                  class="flex-1 h-10 rounded-2xl border border-slate-200 text-[13px] font-semibold text-slate-600 transition hover:bg-slate-50 disabled:opacity-50"
                  :disabled="saving"
                  @click="closeSaveConfirmModal"
                >
                  Xem lại
                </button>
                <button
                  type="button"
                  class="flex-1 h-10 flex items-center justify-center gap-2 rounded-2xl bg-rose-500 text-[13px] font-semibold text-white transition hover:bg-rose-600 disabled:opacity-50"
                  :disabled="saving"
                  @click="confirmSave"
                >
                  <Save :size="14" />
                  Xác nhận lưu
                </button>
              </div>

            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>
