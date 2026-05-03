<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import LoadingSection from '../../../components/admin/san-pham/LoadingSection.vue';
import ToastNotification from '../../../components/admin/san-pham/ToastNotification.vue';
import FormHeader from '../../../components/admin/san-pham/FormHeader.vue';
import ProductFormSection from '../../../components/admin/san-pham/ProductFormSection.vue';
import VariantBuilderSection from '../../../components/admin/san-pham/VariantBuilderSection.vue';
import ChiTietSanPhamGeneratedVariantsSection from '../../../components/admin/san-pham/ChiTietSanPhamGeneratedVariantsSection.vue';
import SuccessSection from '../../../components/admin/san-pham/SuccessSection.vue';
import QuickCreateModal from '../../../components/admin/san-pham/QuickCreateModal.vue';
import { useProductForm } from '../../../composables/useProductForm.js';
import { useVariantBuilder } from '../../../composables/useVariantBuilder.js';
import { useQuickCreate } from '../../../composables/useQuickCreate.js';
import { useToast } from '../../../composables/useToast.js';
import * as api from '../../../services/san-pham-api.ts';
import { getDisplayErrorMessage } from '../../../utils/error-message';

const route = useRoute();

// Use composables
const {
  danhMuc,
  loadingInit,
  saving,
  currentProduct,
  currentProductId,
  createdVariants,
  draftColorImages,
  createdImageManagerRefs,
  productForm,
  productErrors,
  pageTitle,
  productCode,
  isExistingProduct,
  representativeCreatedVariants,
  loadInitialData,
  goBack,
  handleGoBack,
  setCreatedImageManagerRef,
  validateProductForm,
  buildCreateProductPayload,
} = useProductForm();

const {
  variantBuilder,
  variantErrors,
  generatedVariants,
  mauSacSearch,
  kichCoSearch,
  openVariantDropdown,
  representativeGeneratedVariants,
  generateVariants,
  applyGeneratedDefaults,
  removeGeneratedVariant,
  toggleVariantDropdown,
  toggleSelectedValue,
  clearSelectedValues,
  updateDraftImagesForColor,
} = useVariantBuilder();

const {
  quickCreateOpen,
  quickCreateDefinition,
  quickCreateForm,
  quickCreateErrors,
  quickCreateSaving,
  openQuickCreate,
  closeQuickCreate,
  handleQuickCreateSave,
  updateQuickCreateForm,
} = useQuickCreate();

const { toast, showToast, hideToast } = useToast();

const inlineCreatingType = ref(null);

// Handle inline create attribute
function handleInlineCreateAttribute(type, value) {
  inlineCreatingType.value = type;
  openQuickCreate(type, value);
}

// Handle document click for dropdowns
function handleDocumentClick(event) {
  // Close dropdowns when clicking outside
  if (openVariantDropdown.value && !event.target.closest('.dropdown-container')) {
    openVariantDropdown.value = null;
  }
}

// Handle save
async function handleSave() {
  if (!validateProductForm()) {
    showToast('Vui lòng sửa các lỗi trong form sản phẩm trước khi lưu', 'error');
    return;
  }

  if (!generatedVariants.value.length) {
    showToast('Vui lòng tạo ít nhất một biến thể sản phẩm', 'error');
    return;
  }

  saving.value = true;

  try {
    const productPayload = buildCreateProductPayload();

    let productResult;
    if (isExistingProduct.value) {
      productResult = await api.capNhatGiay(currentProductId.value, productPayload);
    } else {
      productResult = await api.taoGiay(productPayload);
    }

    const variantsPayload = generatedVariants.value.map((variant) => ({
      mauSacId: variant.mauSacId,
      kichCoId: variant.kichCoId,
      soLuong: variant.soLuong,
      giaGoc: variant.giaGoc,
      giaBan: variant.giaBan,
    }));

    const variantsResult = await api.taoChiTietSanPham({
      giayId: productResult.data.id,
      bienThes: variantsPayload,
    });

    createdVariants.value = variantsResult.data;
    showToast('Lưu sản phẩm thành công!', 'success');
  } catch (error) {
    console.error('Error saving product:', error);
    const errorMessage = getDisplayErrorMessage(error);
    showToast(errorMessage, 'error');
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  document.addEventListener("mousedown", handleDocumentClick);
  await loadInitialData();
});

onBeforeUnmount(() => {
  document.removeEventListener("mousedown", handleDocumentClick);
});
</script>

<template>
  <div class="space-y-5">
    <FormHeader :page-title="pageTitle" @go-back="goBack" />

    <section
      v-if="loadingInit"
      class="rounded-[24px] border border-slate-200 bg-white p-10 text-center text-slate-400 shadow-sm"
    >
      Đang tải dữ liệu...
    </section>

    <template v-else>
      <section
        class="grid gap-6 xl:grid-cols-[minmax(0,1.05fr)_minmax(360px,0.95fr)]"
      >
        <ProductFormSection
          :product-form="productForm"
          :product-errors="productErrors"
          :product-code="productCode"
          :danh-muc="danhMuc"
          :inline-creating-type="inlineCreatingType"
          @inline-create-attribute="handleInlineCreateAttribute"
        />

        <VariantBuilderSection
          :variant-builder="variantBuilder"
          :variant-errors="variantErrors"
          :danh-muc="danhMuc"
          :mau-sac-search="mauSacSearch"
          :kich-co-search="kichCoSearch"
          :open-variant-dropdown="openVariantDropdown"
          @update:mau-sac-search="mauSacSearch = $event"
          @update:kich-co-search="kichCoSearch = $event"
          @toggle-variant-dropdown="toggleVariantDropdown"
          @open-quick-create="openQuickCreate"
          @clear-selected-values="clearSelectedValues"
          @toggle-selected-value="toggleSelectedValue"
          @generate-variants="generateVariants"
        />
      </section>

      <ChiTietSanPhamGeneratedVariantsSection
        :generated-variants="generatedVariants"
        :representative-generated-variants="representativeGeneratedVariants"
        :variant-builder="variantBuilder"
        :variant-errors="variantErrors"
        :draft-color-images="draftColorImages"
        :saving="saving"
        :is-existing-product="isExistingProduct"
        @apply-defaults="applyGeneratedDefaults"
        @remove-generated-variant="removeGeneratedVariant"
        @save="handleSave"
        @change-draft-images="
          updateDraftImagesForColor($event.mauSacId, $event.images)
        "
        @error="showToast($event, 'error')"
      />

      <SuccessSection
        :representative-created-variants="representativeCreatedVariants"
        :created-image-manager-refs="createdImageManagerRefs"
        @go-back="handleGoBack"
        @set-created-image-manager-ref="setCreatedImageManagerRef"
        @toast="showToast"
      />
    </template>

    <QuickCreateModal
      :show="quickCreateOpen"
      :definition="quickCreateDefinition"
      :form="quickCreateForm"
      :errors="quickCreateErrors"
      :saving="quickCreateSaving"
      @close="closeQuickCreate"
      @save="handleQuickCreateSave"
      @update:form="updateQuickCreateForm"
    />

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="toast.show"
          class="fixed right-5 top-5 z-[100] rounded-2xl px-4 py-3 text-sm font-medium text-white shadow-lg"
          :class="toast.type === 'error' ? 'bg-rose-500' : 'bg-emerald-500'"
        >
          {{ toast.message }}
        </div>
      </Transition>
    </Teleport>
  </div>
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
