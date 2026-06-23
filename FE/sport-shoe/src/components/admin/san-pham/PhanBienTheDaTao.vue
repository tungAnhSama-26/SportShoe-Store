<script setup>
import { computed, ref, watch } from "vue";
import { Package2, Save, Trash2, X } from "lucide-vue-next";
import QuanLyAnhBienThe from "./QuanLyAnhBienThe.vue";
import AdminFormattedNumberInput from "../../common/AdminFormattedNumberInput.vue";
import {
  generateHexColorFromText,
  isValidHexColor,
} from "../../../utils/thuoc-tinh-san-pham";
import { showConfirm } from "../../../utils/alert";

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
const showErrors = ref(false);
const showDefaultErrors = ref(false);

watch(
  () => props.generatedVariants.length,
  (newVal) => {
    if (newVal > 0) {
      showErrors.value = false;
      showDefaultErrors.value = false;
    }
  }
);

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

const defaultFieldErrors = computed(() => {
  if (!showDefaultErrors.value) {
    return { soLuong: "", giaGoc: "", giaBan: "" };
  }
  return {
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
  };
});

const generatedVariantFieldErrors = computed(() =>
  Object.fromEntries(
    props.generatedVariants.map((item) => {
      if (!showErrors.value || item.selected === false) {
        return [
          item.key,
          { soLuong: "", giaGoc: "", giaBan: "" }
        ];
      }
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

function isGroupAllSelected(group) {
  return group.variants.length > 0 && group.variants.every((v) => v.selected !== false);
}

function toggleGroupAll(group, selected) {
  group.variants.forEach((v) => {
    v.selected = selected;
  });
}

const isAllSelected = computed({
  get() {
    return props.generatedVariants.length > 0 && props.generatedVariants.every(v => v.selected !== false);
  },
  set(value) {
    props.generatedVariants.forEach(v => {
      v.selected = value;
    });
  }
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
    lines.push(`${imageColorCount} màu sắc có ảnh đính kèm.`);
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

function getDetailedValidationMessages() {
  const messages = [];

  // Default fields (always evaluate using build function directly for raw checking)
  const defaultErrorsRaw = {
    soLuong: buildNumberFieldError("Số lượng mặc định", props.variantBuilder.soLuong),
    giaGoc: (() => {
      const priceError = buildNumberFieldError("Giá gốc mặc định", props.variantBuilder.giaGoc, { allowZero: false });
      if (priceError) return priceError;
      const giaGoc = parseNumericValue(props.variantBuilder.giaGoc);
      const giaBan = parseNumericValue(props.variantBuilder.giaBan);
      return giaGoc > 0 && giaBan > 0 && giaGoc > giaBan ? "Giá gốc mặc định không được lớn hơn giá bán mặc định" : "";
    })(),
    giaBan: buildNumberFieldError("Giá bán mặc định", props.variantBuilder.giaBan, { allowZero: false })
  };

  if (defaultErrorsRaw.soLuong) messages.push(defaultErrorsRaw.soLuong);
  if (defaultErrorsRaw.giaGoc) messages.push(defaultErrorsRaw.giaGoc);
  if (defaultErrorsRaw.giaBan) messages.push(defaultErrorsRaw.giaBan);

  // Variant fields
  props.generatedVariants.filter(item => item.selected !== false).forEach((item) => {
    const giaGocError = buildNumberFieldError("Giá gốc", item.giaGoc, { allowZero: false });
    const giaBanError = buildNumberFieldError("Giá bán", item.giaBan, { allowZero: false });
    const soLuongError = buildNumberFieldError("Số lượng", item.soLuong);
    const priceCompareError = parseNumericValue(item.giaGoc) > 0 && parseNumericValue(item.giaBan) > 0 && parseNumericValue(item.giaGoc) > parseNumericValue(item.giaBan)
      ? "Giá gốc không được lớn hơn giá bán"
      : "";

    const variantName = `Biến thể Size ${item.kichCo} - ${formatColorName(item.mauSac)}`;

    if (soLuongError) messages.push(`${variantName}: ${soLuongError}`);
    if (giaGocError) messages.push(`${variantName}: ${giaGocError}`);
    else if (priceCompareError) messages.push(`${variantName}: ${priceCompareError}`);
    if (giaBanError) messages.push(`${variantName}: ${giaBanError}`);
  });

  return messages;
}

function relatedVariants(mauSacId) {
  return props.generatedVariants.filter(
    (item) => Number(item.mauSacId) === Number(mauSacId),
  );
}

function draftImagesForColor(mauSacId) {
  return props.draftVariantImages[String(mauSacId)] || [];
}

function handleDraftImagesChange(mauSacId, images) {
  emit("change-draft-images", { variantKey: mauSacId, images });
}

function setDraftImageManagerRef(mauSacId, instance) {
  const draftKey = String(mauSacId);
  if (instance) {
    draftImageManagerRefs.value[draftKey] = instance;
    return;
  }

  delete draftImageManagerRefs.value[draftKey];
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
  showDefaultErrors.value = true;
  if (hasDefaultFieldErrors.value) {
    const messages = [];
    if (defaultFieldErrors.value.soLuong) messages.push(defaultFieldErrors.value.soLuong);
    if (defaultFieldErrors.value.giaGoc) messages.push(defaultFieldErrors.value.giaGoc);
    if (defaultFieldErrors.value.giaBan) messages.push(defaultFieldErrors.value.giaBan);
    const detailMessage = messages.join("; ");
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

async function handleSaveClick() {
  showErrors.value = true;
  showDefaultErrors.value = true;
  const messages = getDetailedValidationMessages();
  if (messages.length > 0) {
    const limit = 3;
    const visibleMessages = messages.slice(0, limit).join("; ");
    const hiddenCount = messages.length - limit;
    const detailMessage = hiddenCount > 0
      ? `${visibleMessages}; và ${hiddenCount} lỗi khác`
      : visibleMessages;
    emit(
      "error",
      `Vui lòng sửa các lỗi sau: ${detailMessage}`,
    );
    return;
  }

  const committed = await commitPendingDraftImages();
  if (!committed) {
    return;
  }

  const selectedVariants = props.generatedVariants.filter(v => v.selected !== false);
  const selectedCount = selectedVariants.length;
  if (selectedCount === 0) {
    emit("error", "Vui lòng chọn ít nhất một biến thể để lưu");
    return;
  }

  const uniqueColors = new Set(selectedVariants.map(v => v.mauSacId));
  const missingImageColors = [];
  for (const colorId of uniqueColors) {
    const images = props.draftVariantImages[String(colorId)] || [];
    if (images.length === 0) {
      const colorName = selectedVariants.find(v => v.mauSacId === colorId)?.mauSac;
      missingImageColors.push(formatColorName(colorName || ""));
    }
  }

  if (missingImageColors.length > 0) {
    emit("error", `Vui lòng thêm ít nhất 1 ảnh sản phẩm cho màu: ${missingImageColors.join(", ")}`);
    return;
  }

  const { title } = saveConfirmationDetails.value;
  const isConfirmed = await showConfirm(
    "", 
    title, 
    "Xác nhận lưu", 
    "Xem lại"
  );
  if (isConfirmed) {
    emit("save");
  }
}
</script>

<template>
  <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
    <p v-if="variantErrors.generated" class="text-sm text-rose-500">
      {{ variantErrors.generated }}
    </p>

    <div v-if="generatedVariants.length" class="mb-4 flex items-center gap-2 border-b border-slate-100 pb-4">
      <input type="checkbox" id="selectAll" v-model="isAllSelected" class="h-4 w-4 rounded border-slate-300 text-rose-500 focus:ring-rose-500 cursor-pointer" />
      <label for="selectAll" class="text-[13px] font-semibold text-slate-600 cursor-pointer select-none">
        Chọn tất cả biến thể
      </label>
    </div>

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
                <th class="rounded-l-md bg-slate-100 px-4 py-3 w-[50px] text-center">
                  <input type="checkbox" :checked="isGroupAllSelected(group)" @change="toggleGroupAll(group, $event.target.checked)" class="h-4 w-4 rounded border-slate-300 text-rose-500 focus:ring-rose-500 cursor-pointer" />
                </th>
                <th class="bg-slate-100 px-4 py-3 w-[60px] text-center">STT</th>
                <th class="bg-slate-100 px-4 py-3">Kích cỡ</th>
                <th class="bg-slate-100 px-4 py-3">Số lượng</th>
                <th class="bg-slate-100 px-4 py-3">Giá gốc</th>
                <th class="bg-slate-100 px-4 py-3">Giá bán</th>
                <th class="rounded-r-md bg-slate-100 px-4 py-3 text-right">
                  Xóa
                </th>
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
                    class="h-10 w-36 rounded-md border px-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
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
                    class="h-10 w-36 rounded-md border px-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
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
            Thêm ảnh cho từng màu sắc (biến thể đại diện) để tự động đồng bộ cho toàn bộ kích cỡ.
          </p>
        </div>
      </div>

      <div 
        class="mt-5 grid gap-4 sm:grid-cols-2"
        :class="representativeGeneratedVariants.length >= 3 ? 'lg:grid-cols-3' : ''"
      >
        <QuanLyAnhBienThe
          v-for="item in representativeGeneratedVariants"
          :key="`draft-color-${item.mauSacId}`"
          :ref="(instance) => setDraftImageManagerRef(item.mauSacId, instance)"
          :variant="item"
          :draft-images="draftImagesForColor(item.mauSacId)"
          :related-variants="relatedVariants(item.mauSacId)"
          display-mode="color"
          compact
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


  </section>
</template>
