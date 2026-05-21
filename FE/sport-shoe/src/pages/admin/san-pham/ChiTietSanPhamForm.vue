<script setup>
import { useChiTietSanPhamFormPage } from "./useChiTietSanPhamFormPage";
const { computed, onBeforeUnmount, onMounted, reactive, ref, FormHeader, ProductFormSection, VariantBuilderSection, ChiTietSanPhamGeneratedVariantsSection, SuccessSection, QuickCreateModal, useProductForm, useVariantBuilder, useToast, chatLieuGiayApi, coGiayApi, congNgheDemApi, deGiayApi, kichCoApi, loaiGiayApi, mauSacApi, thuongHieuApi, trongLuongApi, api, getDisplayErrorMessage, getFieldErrors, createAttributeCodeSeed, generateAttributeCode, generateColorAttributeCode, generateHexColorFromText, generateWeightAttributeCode, isValidHexColor, normalizeAttributeText, normalizeRequiredText, normalizeSizeValue, danhMuc, loadingInit, saving, currentProductId, createdVariants, createdImageManagerRefs, productForm, productErrors, pageTitle, productCode, isExistingProduct, representativeCreatedVariants, loadInitialData, goBack, handleGoBack, setCreatedImageManagerRef, validateProductForm, buildCreateProductPayload, regenerateDraftProductCode, variantBuilder, variantErrors, generatedVariants, draftColorImages, mauSacSearch, kichCoSearch, openVariantDropdown, representativeGeneratedVariants, generateVariants, applyGeneratedDefaults, removeGeneratedVariant, toggleVariantDropdown, toggleSelectedValue, clearSelectedValues, appendSelectedValue, updateDraftImagesForColor, toast, showToast, inlineCreatingType, quickCreateOpen, quickCreateType, quickCreateSaving, quickCreateColorSeed, quickCreateForm, quickCreateErrors, attributeConfigs, quickCreateDefinition, handleDocumentClick, normalizeErrorText, isDuplicateProductCodeError, isDuplicateAttributeErrorMessage, getQuickCreateDuplicateValue, setQuickCreateDuplicateError, applyQuickCreateRequestError, normalizeWeightValue, clearQuickCreateErrors, resetQuickCreateForm, closeQuickCreate, syncQuickCreateColorFields, openQuickCreate, getCategoryItems, findExistingInlineItem, appendCategoryItem, selectInlineCreatedItem, getInlineItemDisplayValue, buildInlineCreatePayload, updateQuickCreateForm, handleQuickCreateSave, handleInlineCreateAttribute, handleGenerateVariants, buildDraftImagePayload, syncDraftImagesToVariants, clearSavedDraftImages, handleSave } = useChiTietSanPhamFormPage();
</script>

<template>
  <div class="space-y-5">
    <FormHeader :page-title="pageTitle" @go-back="goBack" />

    <section
      v-if="loadingInit"
      class="rounded-[24px] border border-slate-200 bg-white p-10 text-center text-slate-400 shadow-sm"
    >
      �ang t?i d? li?u...
    </section>

    <template v-else>
      <section class="space-y-6">
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
          :inline-creating-type="inlineCreatingType"
          @update:mau-sac-search="mauSacSearch = $event"
          @update:kich-co-search="kichCoSearch = $event"
          @toggle-variant-dropdown="toggleVariantDropdown"
          @inline-create-attribute="handleInlineCreateAttribute"
          @clear-selected-values="clearSelectedValues"
          @toggle-selected-value="toggleSelectedValue"
          @generate-variants="handleGenerateVariants"
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
