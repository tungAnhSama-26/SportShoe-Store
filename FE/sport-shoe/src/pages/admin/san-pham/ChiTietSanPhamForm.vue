<script setup>
import { useChiTietSanPhamFormPage } from "./useChiTietSanPhamFormPage";

const { computed, onBeforeUnmount, onMounted, reactive, ref, TieuDeForm, PhanNhapThongTinSanPham, PhanTaoBienThe, PhanBienTheDaTao, PhanThanhCong, ModalThemNhanh, useProductForm, useVariantBuilder, useToast, chatLieuGiayApi, coGiayApi, congNgheDemApi, deGiayApi, kichCoApi, loaiGiayApi, mauSacApi, thuongHieuApi, trongLuongApi, api, getDisplayErrorMessage, getFieldErrors, createAttributeCodeSeed, generateAttributeCode, generateColorAttributeCode, generateHexColorFromText, generateWeightAttributeCode, isValidHexColor, normalizeAttributeText, normalizeRequiredText, normalizeSizeValue, danhMuc, loadingInit, saving, currentProductId, createdVariants, createdImageManagerRefs, productForm, productErrors, pageTitle, productCode, isExistingProduct, representativeCreatedVariants, loadInitialData, goBack, handleGoBack, setCreatedImageManagerRef, validateProductForm, buildCreateProductPayload, regenerateDraftProductCode, variantBuilder, variantErrors, generatedVariants, draftVariantImages, mauSacSearch, kichCoSearch, openVariantDropdown, representativeGeneratedVariants, generateVariants, applyGeneratedDefaults, removeGeneratedVariant, toggleVariantDropdown, toggleSelectedValue, clearSelectedValues, appendSelectedValue, updateDraftImagesForVariant, toast, showToast, inlineCreatingType, quickCreateOpen, quickCreateType, quickCreateSaving, quickCreateColorSeed, quickCreateForm, quickCreateErrors, attributeConfigs, quickCreateDefinition, handleDocumentClick, normalizeErrorText, isDuplicateProductCodeError, isDuplicateAttributeErrorMessage, getQuickCreateDuplicateValue, setQuickCreateDuplicateError, applyQuickCreateRequestError, normalizeWeightValue, clearQuickCreateErrors, resetQuickCreateForm, closeQuickCreate, syncQuickCreateColorFields, openQuickCreate, getCategoryItems, findExistingInlineItem, appendCategoryItem, selectInlineCreatedItem, getInlineItemDisplayValue, buildInlineCreatePayload, updateQuickCreateForm, handleQuickCreateSave, handleInlineCreateAttribute, handleGenerateVariants, buildDraftImagePayload, syncDraftImagesToVariants, clearSavedDraftImages, handleSave } = useChiTietSanPhamFormPage();
</script>

<template>
  <div class="space-y-5 radius-6px">
    <TieuDeForm :page-title="pageTitle" @go-back="goBack" />

    <section
      v-if="loadingInit"
      class="rounded-[24px] border border-slate-200 bg-white p-10 text-center text-slate-400 shadow-sm"
    >
      Đang tải dữ liệu...
    </section>

    <template v-else>
      <section class="space-y-6">
        <PhanNhapThongTinSanPham
          :product-form="productForm"
          :product-errors="productErrors"
          :product-code="productCode"
          :danh-muc="danhMuc"
          :inline-creating-type="inlineCreatingType"
          @inline-create-attribute="handleInlineCreateAttribute"
        />

        <PhanTaoBienThe
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

        <article class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
          <label class="block">
            <span class="mb-1 block text-[13px] font-semibold text-slate-500">Mô tả</span>
            <textarea
              v-model="productForm.moTa"
              rows="5"
              class="w-full rounded-md border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
              placeholder="Mô tả sản phẩm"
            ></textarea>
            <p v-if="productErrors.moTa" class="mt-1 text-xs text-rose-500">
              {{ productErrors.moTa }}
            </p>
          </label>
        </article>
      </section>

      <PhanBienTheDaTao
        :generated-variants="generatedVariants"
        :representative-generated-variants="representativeGeneratedVariants"
        :variant-builder="variantBuilder"
        :variant-errors="variantErrors"
        :draft-variant-images="draftVariantImages"
        :saving="saving"
        :is-existing-product="isExistingProduct"
        @apply-defaults="applyGeneratedDefaults"
        @remove-generated-variant="removeGeneratedVariant"
        @save="handleSave"
        @change-draft-images="
          updateDraftImagesForVariant($event.variantKey, $event.images)
        "
        @error="showToast($event, 'error')"
      />

      <PhanThanhCong
        :representative-created-variants="representativeCreatedVariants"
        :created-image-manager-refs="createdImageManagerRefs"
        @go-back="handleGoBack"
        @set-created-image-manager-ref="setCreatedImageManagerRef"
        @toast="showToast"
      />
    </template>

    <ModalThemNhanh
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
      v-if="toast.show && toast.type !== 'success'"
      class="fixed right-5 top-5 z-[100] rounded-md px-4 py-3 text-sm font-medium text-white shadow-lg"
      :class="toast.type === 'error' ? 'bg-[#cf1018]' : 'bg-[#ff6a00]'"
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
