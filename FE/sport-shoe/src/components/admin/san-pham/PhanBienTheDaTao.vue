<script setup>
import { computed, ref, watch } from "vue";
import { Package2, Save } from "lucide-vue-next";
import QuanLyAnhBienThe from "./QuanLyAnhBienThe.vue";
import PhanBienTheDaTaoNhapNhanh from "./PhanBienTheDaTaoNhapNhanh.vue";
import PhanBienTheDaTaoDanhSachNhom from "./PhanBienTheDaTaoDanhSachNhom.vue";
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
  const limit = label.toLowerCase().includes('giá') ? 999000000000 : 2000000000;
  const limitStr = limit === 999000000000 ? '999.000.000.000' : '2.000.000.000';
  if (parsed > limit) return `${label} không được vượt quá ${limitStr}`;
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
          { soLuong: "", giaBan: "" }
        ];
      }
      const giaBanError = buildNumberFieldError("Giá bán", item.giaBan, {
        allowZero: false,
      });

      return [
        item.key,
        {
          soLuong: buildNumberFieldError("Số lượng", item.soLuong),
          giaBan: giaBanError,
        },
      ];
    }),
  ),
);

function resolveColorGroupKey(item) {
  return (item?.mauSacId != null) ? Number(item.mauSacId) : (item?.mauSac || item?.key);
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

function getDetailedValidationMessages() {
  const messages = [];

  const defaultErrorsRaw = {
    soLuong: buildNumberFieldError("Số lượng mặc định", props.variantBuilder.soLuong),
    giaBan: buildNumberFieldError("Giá bán mặc định", props.variantBuilder.giaBan, { allowZero: false })
  };

  if (defaultErrorsRaw.soLuong) messages.push(defaultErrorsRaw.soLuong);
  if (defaultErrorsRaw.giaBan) messages.push(defaultErrorsRaw.giaBan);

  props.generatedVariants.filter(item => item.selected !== false).forEach((item) => {
    const giaBanError = buildNumberFieldError("Giá bán", item.giaBan, { allowZero: false });
    const soLuongError = buildNumberFieldError("Số lượng", item.soLuong);

    const formatColorName = (value) => {
      const normalized = String(value || "").trim().toLocaleLowerCase("vi-VN");
      if (!normalized) return "";
      return normalized.charAt(0).toLocaleUpperCase("vi-VN") + normalized.slice(1);
    };

    const variantName = `Biến thể Size ${item.kichCo} - ${formatColorName(item.mauSac)}`;

    if (soLuongError) messages.push(`${variantName}: ${soLuongError}`);
    if (giaBanError) messages.push(`${variantName}: ${giaBanError}`);
  });

  return messages;
}

function formatColorName(value) {
  const normalized = String(value || "").trim().toLocaleLowerCase("vi-VN");
  if (!normalized) return "";
  return normalized.charAt(0).toLocaleUpperCase("vi-VN") + normalized.slice(1);
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

    <PhanBienTheDaTaoNhapNhanh
      v-if="generatedVariants.length"
      :variant-builder="variantBuilder"
      :default-field-errors="defaultFieldErrors"
      :variant-errors="variantErrors"
      @apply-defaults="handleApplyDefaults"
    />

    <div v-if="generatedVariantGroups.length" class="space-y-4">
      <PhanBienTheDaTaoDanhSachNhom
        v-for="group in generatedVariantGroups"
        :key="`generated-group-${group.key}`"
        :group="group"
        :generated-variant-field-errors="generatedVariantFieldErrors"
        @remove-generated-variant="emit('remove-generated-variant', $event)"
      />
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
