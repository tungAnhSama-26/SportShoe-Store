import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import FormHeader from '../../../components/admin/san-pham/FormHeader.vue'
import ProductFormSection from '../../../components/admin/san-pham/ProductFormSection.vue'
import VariantBuilderSection from '../../../components/admin/san-pham/VariantBuilderSection.vue'
import ChiTietSanPhamGeneratedVariantsSection from '../../../components/admin/san-pham/ChiTietSanPhamGeneratedVariantsSection.vue'
import SuccessSection from '../../../components/admin/san-pham/SuccessSection.vue'
import QuickCreateModal from '../../../components/admin/san-pham/QuickCreateModal.vue'
import { useProductForm } from '../../../composables/useProductForm.js'
import { useVariantBuilder } from '../../../composables/useVariantBuilder.js'
import { useToast } from '../../../composables/useToast.js'
import {
  chatLieuGiayApi,
  coGiayApi,
  congNgheDemApi,
  deGiayApi,
  kichCoApi,
  loaiGiayApi,
  mauSacApi,
  thuongHieuApi,
  trongLuongApi
} from '../../../services/danh-muc-api.ts'
import * as api from '../../../services/san-pham-api.ts'
import { getDisplayErrorMessage, getFieldErrors } from '../../../utils/error-message'
import {
  createAttributeCodeSeed,
  generateAttributeCode,
  generateColorAttributeCode,
  generateHexColorFromText,
  generateWeightAttributeCode,
  isValidHexColor,
  normalizeAttributeText,
  normalizeRequiredText,
  normalizeSizeValue
} from '../../../utils/thuoc-tinh-san-pham.js'
export function useChiTietSanPhamFormPage() {
  const {
    danhMuc,
    loadingInit,
    saving,
    currentProductId,
    createdVariants,
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
    regenerateDraftProductCode
  } = useProductForm()
  const {
    variantBuilder,
    variantErrors,
    generatedVariants,
    draftColorImages,
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
    appendSelectedValue,
    updateDraftImagesForColor
  } = useVariantBuilder()
  const { toast, showToast } = useToast()
  const inlineCreatingType = ref(null)
  const quickCreateOpen = ref(false)
  const quickCreateType = ref(null)
  const quickCreateSaving = ref(false)
  const quickCreateColorSeed = ref(createAttributeCodeSeed())
  const quickCreateForm = reactive({
    ma: '',
    ten: '',
    maMauHex: '#94A3B8',
    giaTri: '',
    ghiChu: ''
  })
  const quickCreateErrors = reactive({})
  const attributeConfigs = {
    thuongHieu: {
      label: 'thương hiệu',
      categoryKey: 'thuongHieu',
      formField: 'thuongHieuId',
      create: (body) => thuongHieuApi.create(body),
      buildBody: (ten, seed) => ({
        ma: generateAttributeCode(ten, 'TH', 'THUONG', seed),
        ten,
        xuatXu: null,
        logoUrl: null,
        website: null,
        moTa: null
      })
    },
    loaiGiay: {
      label: 'loại giày',
      categoryKey: 'loaiGiay',
      formField: 'loaiGiayId',
      create: (body) => loaiGiayApi.create(body),
      buildBody: (ten, seed) => ({
        ma: generateAttributeCode(ten, 'LG', 'LOAI', seed),
        ten,
        moTa: null
      })
    },
    chatLieuGiay: {
      label: 'chất liệu',
      categoryKey: 'chatLieuGiay',
      formField: 'chatLieuGiayId',
      create: (body) => chatLieuGiayApi.create(body),
      buildBody: (ten, seed) => ({
        ma: generateAttributeCode(ten, 'CLG', 'CHATL', seed),
        ten,
        moTa: null
      })
    },
    deGiay: {
      label: 'đế giày',
      categoryKey: 'deGiay',
      formField: 'deGiayId',
      create: (body) => deGiayApi.create(body),
      buildBody: (ten, seed) => ({
        ma: generateAttributeCode(ten, 'DG', 'DEGIAY', seed),
        ten,
        moTa: null
      })
    },
    coGiay: {
      label: 'cổ giày',
      categoryKey: 'coGiay',
      formField: 'coGiayId',
      create: (body) => coGiayApi.create(body),
      buildBody: (ten, seed) => ({
        ma: generateAttributeCode(ten, 'CG', 'COGIAY', seed),
        ten,
        moTa: null
      })
    },
    congNgheDem: {
      label: 'công nghệ đệm',
      categoryKey: 'congNgheDem',
      formField: 'congNgheDemId',
      create: (body) => congNgheDemApi.create(body),
      buildBody: (ten, seed) => ({
        ma: generateAttributeCode(ten, 'CND', 'CNDEM', seed),
        ten,
        moTa: null
      })
    },
    trongLuong: {
      label: 'trọng lượng',
      categoryKey: 'trongLuong',
      formField: 'trongLuongId',
      create: (body) => trongLuongApi.create(body)
    },
    mauSac: {
      label: 'màu sắc',
      categoryKey: 'mauSac',
      create: (body) => mauSacApi.create(body)
    },
    kichCo: {
      label: 'kích cỡ',
      categoryKey: 'kichCo',
      create: (body) => kichCoApi.create(body)
    }
  }
  const quickCreateDefinition = computed(() => {
    if (quickCreateType.value === 'mauSac') {
      return {
        title: 'Thêm nhanh màu sắc',
        description: '',
        fields: [
          { key: 'ma', label: 'Mã màu *', placeholder: 'Tự sinh', uppercase: true, readonly: true },
          { key: 'ten', label: 'Tên màu *', placeholder: 'Nhập tên màu' },
          { key: 'maMauHex', label: 'Chọn màu (RGB)', type: 'color' }
        ]
      }
    }
    if (quickCreateType.value === 'kichCo') {
      return {
        title: 'Thêm nhanh kích cỡ',
        description: '',
        fields: [
          { key: 'giaTri', label: 'Kích cỡ *', placeholder: 'Nhập kích cỡ' },
          { key: 'ghiChu', label: 'Ghi chú', placeholder: 'Ghi chú nếu cần' }
        ]
      }
    }
    return null
  })
  function handleDocumentClick(event) {
    if (openVariantDropdown.value && !event.target.closest('.dropdown-container')) {
      openVariantDropdown.value = null
    }
  }
  function normalizeErrorText(value) {
    return String(value ?? '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
  }
  function isDuplicateProductCodeError(error) {
    const message = normalizeErrorText(getDisplayErrorMessage(error, ''))
    return message.includes('ma giay') && message.includes('da ton tai')
  }
  function isDuplicateAttributeErrorMessage(message) {
    return normalizeErrorText(message).includes('da ton tai')
  }
  function getQuickCreateDuplicateValue(type) {
    if (type === 'mauSac') {
      return normalizeRequiredText(quickCreateForm.ten)
    }
    if (type === 'kichCo') {
      return normalizeSizeValue(quickCreateForm.giaTri) || String(quickCreateForm.giaTri || '').trim()
    }
    return ''
  }
  function setQuickCreateDuplicateError(type, value) {
    if (type === 'mauSac') {
      quickCreateErrors.ten = `Màu sắc "${value}" đã tồn tại trong hệ thống`
      return
    }
    if (type === 'kichCo') {
      quickCreateErrors.giaTri = `Kích cỡ "${value}" đã tồn tại trong hệ thống`
    }
  }
  function applyQuickCreateRequestError(type, error) {
    const fieldErrors = getFieldErrors(error)
    if (fieldErrors.ma && isDuplicateAttributeErrorMessage(fieldErrors.ma)) {
      setQuickCreateDuplicateError(type, getQuickCreateDuplicateValue(type))
      return
    }
    if (fieldErrors.giaTri && isDuplicateAttributeErrorMessage(fieldErrors.giaTri)) {
      setQuickCreateDuplicateError(type, getQuickCreateDuplicateValue(type))
      return
    }
    if (Object.keys(fieldErrors).length) {
      Object.assign(quickCreateErrors, fieldErrors)
      return
    }
    const message = getDisplayErrorMessage(error, '')
    if (isDuplicateAttributeErrorMessage(message)) {
      setQuickCreateDuplicateError(type, getQuickCreateDuplicateValue(type))
      return
    }
    quickCreateErrors.general = getDisplayErrorMessage(
      error,
      'Không thể thêm mới lúc này. Vui lòng kiểm tra lại thông tin vừa nhập.'
    )
  }
  function normalizeWeightValue(value) {
    const matched = String(value ?? '').trim().match(/^(\d{1,4})(?:\s*(?:g|gram))?$/i)
    if (!matched) {
      return null
    }
    const parsed = Number(matched[1])
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null
  }
  function clearQuickCreateErrors() {
    Object.keys(quickCreateErrors).forEach((key) => delete quickCreateErrors[key])
  }
  function resetQuickCreateForm() {
    Object.assign(quickCreateForm, {
      ma: '',
      ten: '',
      maMauHex: '#94A3B8',
      giaTri: '',
      ghiChu: ''
    })
    clearQuickCreateErrors()
  }
  function closeQuickCreate() {
    quickCreateOpen.value = false
    quickCreateType.value = null
    quickCreateSaving.value = false
    quickCreateColorSeed.value = createAttributeCodeSeed()
    resetQuickCreateForm()
  }
  function syncQuickCreateColorFields() {
    quickCreateForm.ma = generateColorAttributeCode(quickCreateForm.ten, quickCreateColorSeed.value)
    if (!isValidHexColor(quickCreateForm.maMauHex)) {
      quickCreateForm.maMauHex = generateHexColorFromText(quickCreateForm.ten)
    }
    quickCreateForm.maMauHex = quickCreateForm.maMauHex.toUpperCase()
  }
  function openQuickCreate(type, preset = '') {
    quickCreateType.value = type
    quickCreateSaving.value = false
    clearQuickCreateErrors()
    if (type === 'mauSac') {
      quickCreateColorSeed.value = createAttributeCodeSeed()
      Object.assign(quickCreateForm, {
        ma: '',
        ten: normalizeRequiredText(preset),
        maMauHex: generateHexColorFromText(preset),
        giaTri: '',
        ghiChu: ''
      })
      syncQuickCreateColorFields()
    } else if (type === 'kichCo') {
      Object.assign(quickCreateForm, {
        ma: '',
        ten: '',
        maMauHex: '#94A3B8',
        giaTri: normalizeSizeValue(preset) || normalizeRequiredText(preset),
        ghiChu: ''
      })
    }
    quickCreateOpen.value = true
  }
  function getCategoryItems(type) {
    const categoryKey = attributeConfigs[type]?.categoryKey
    return categoryKey ? (danhMuc.value?.[categoryKey] || []) : []
  }
  function findExistingInlineItem(type, rawValue) {
    const items = getCategoryItems(type)
    if (type === 'trongLuong') {
      const normalized = normalizeWeightValue(rawValue)
      if (!normalized) return null
      return items.find((item) => Number(item.giaTri) === normalized) || null
    }
    if (type === 'kichCo') {
      const normalized = normalizeSizeValue(rawValue)
      if (!normalized) return null
      return items.find((item) => normalizeSizeValue(item.giaTri) === normalized) || null
    }
    const normalized = normalizeAttributeText(rawValue)
    if (!normalized) return null
    return items.find((item) => normalizeAttributeText(item.ten) === normalized) || null
  }
  function appendCategoryItem(type, item) {
    const categoryKey = attributeConfigs[type]?.categoryKey
    if (!categoryKey || !danhMuc.value) {
      return
    }
    const currentItems = danhMuc.value[categoryKey] || []
    danhMuc.value[categoryKey] = [
      item,
      ...currentItems.filter((currentItem) => Number(currentItem?.id) !== Number(item?.id))
    ]
  }
  function selectInlineCreatedItem(type, item) {
    if (!item?.id) {
      return
    }
    if (type === 'mauSac') {
      appendSelectedValue('mauSacIds', item.id)
      delete variantErrors.mauSacIds
      mauSacSearch.value = ''
      return
    }
    if (type === 'kichCo') {
      appendSelectedValue('kichCoIds', item.id)
      delete variantErrors.kichCoIds
      kichCoSearch.value = ''
      return
    }
    const formField = attributeConfigs[type]?.formField
    if (!formField) {
      return
    }
    productForm[formField] = item.id
    delete productErrors[formField]
  }
  function getInlineItemDisplayValue(type, item) {
    if (type === 'trongLuong') {
      return `${item.giaTri} g`
    }
    if (type === 'kichCo') {
      return item.giaTri
    }
    return item.ten
  }
  function buildInlineCreatePayload(type, rawValue) {
    const config = attributeConfigs[type]
    if (!config) {
      throw new Error('Không hỗ trợ thêm nhanh cho thuộc tính này')
    }
    const seed = createAttributeCodeSeed()
    if (type === 'trongLuong') {
      const giaTri = normalizeWeightValue(rawValue)
      if (!giaTri) {
        throw new Error('Trọng lượng phải là số nguyên từ 1 g trở lên')
      }
      return {
        create: config.create,
        body: {
          ma: generateWeightAttributeCode(giaTri, seed),
          giaTri,
          moTa: null
        }
      }
    }
    const ten = normalizeRequiredText(rawValue)
    if (!ten) {
      throw new Error(`Vui lòng nhập ${config.label} để thêm nhanh`)
    }
    return {
      create: config.create,
      body: config.buildBody(ten, seed)
    }
  }
  function updateQuickCreateForm(nextForm) {
    const previousTen = quickCreateForm.ten
    const previousAutoHex = generateHexColorFromText(previousTen)
    Object.assign(quickCreateForm, nextForm)
    if (quickCreateType.value === 'mauSac') {
      quickCreateForm.ten = normalizeRequiredText(quickCreateForm.ten)
      quickCreateForm.ma = generateColorAttributeCode(quickCreateForm.ten, quickCreateColorSeed.value)
      if (!isValidHexColor(quickCreateForm.maMauHex)) {
        quickCreateForm.maMauHex = generateHexColorFromText(quickCreateForm.ten)
      } else if (
        quickCreateForm.ten !== previousTen &&
        String(nextForm.maMauHex || '').toUpperCase() === previousAutoHex.toUpperCase()
      ) {
        quickCreateForm.maMauHex = generateHexColorFromText(quickCreateForm.ten)
      }
      quickCreateForm.maMauHex = quickCreateForm.maMauHex.toUpperCase()
    }
  }
  async function handleQuickCreateSave() {
    clearQuickCreateErrors()
    try {
      quickCreateSaving.value = true
      if (quickCreateType.value === 'mauSac') {
        const ten = normalizeRequiredText(quickCreateForm.ten)
        const existingItem = findExistingInlineItem('mauSac', ten)
        if (existingItem) {
          setQuickCreateDuplicateError('mauSac', existingItem.ten)
          return
        }
        if (!quickCreateForm.ma.trim()) {
          quickCreateErrors.ma = 'Không thể tự sinh mã màu'
        }
        if (!ten) {
          quickCreateErrors.ten = 'Vui lòng nhập tên màu sắc'
        }
        if (!isValidHexColor(quickCreateForm.maMauHex)) {
          quickCreateErrors.maMauHex = 'Mã màu HEX chưa đúng định dạng'
        }
        if (Object.keys(quickCreateErrors).length) {
          return
        }
        const createdItem = await mauSacApi.create({
          ma: quickCreateForm.ma.trim(),
          ten,
          maMauHex: quickCreateForm.maMauHex.toUpperCase()
        })
        appendCategoryItem('mauSac', createdItem)
        selectInlineCreatedItem('mauSac', createdItem)
        closeQuickCreate()
        showToast(`Đã thêm màu sắc "${createdItem.ten}"`, 'success')
        return
      }
      if (quickCreateType.value === 'kichCo') {
        const giaTri = normalizeSizeValue(quickCreateForm.giaTri)
        const existingItem = findExistingInlineItem('kichCo', giaTri || quickCreateForm.giaTri)
        if (existingItem) {
          setQuickCreateDuplicateError('kichCo', existingItem.giaTri)
          return
        }
        if (!giaTri) {
          quickCreateErrors.giaTri = 'Kích cỡ chưa đúng định dạng, vui lòng nhập lại'
        }
        if (Object.keys(quickCreateErrors).length) {
          return
        }
        const createdItem = await kichCoApi.create({
          giaTri,
          ghiChu: normalizeRequiredText(quickCreateForm.ghiChu) || null
        })
        appendCategoryItem('kichCo', createdItem)
        selectInlineCreatedItem('kichCo', createdItem)
        closeQuickCreate()
        showToast(`Đã thêm kích cỡ "${createdItem.giaTri}"`, 'success')
      }
    } catch (error) {
      applyQuickCreateRequestError(quickCreateType.value, error)
    } finally {
      quickCreateSaving.value = false
    }
  }
  async function handleInlineCreateAttribute(type, value) {
    if (inlineCreatingType.value === type) {
      return
    }
    const config = attributeConfigs[type]
    if (!config) {
      return
    }
    if (type === 'mauSac' || type === 'kichCo') {
      openQuickCreate(type, value)
      return
    }
    const existingItem = findExistingInlineItem(type, value)
    if (existingItem) {
      selectInlineCreatedItem(type, existingItem)
      showToast(`Đã chọn ${config.label} "${getInlineItemDisplayValue(type, existingItem)}"`, 'success')
      return
    }
    inlineCreatingType.value = type
    try {
      const { create, body } = buildInlineCreatePayload(type, value)
      const createdItem = await create(body)
      appendCategoryItem(type, createdItem)
      selectInlineCreatedItem(type, createdItem)
      showToast(`Đã thêm ${config.label} "${getInlineItemDisplayValue(type, createdItem)}"`, 'success')
    } catch (error) {
      showToast(getDisplayErrorMessage(error), 'error')
    } finally {
      inlineCreatingType.value = null
    }
  }
  function handleGenerateVariants() {
    const message = generateVariants(danhMuc.value)
    if (message) {
      showToast(message, 'success')
    }
  }
  function buildDraftImagePayload(image) {
    const url = String(image?.url || '').trim()
    if (!url) {
      return null
    }
    return {
      url,
      loaiHinh: Number(image?.loaiHinh || (image?.laHinhChinh ? 1 : 2) || 2),
      moTa: String(image?.moTa || '').trim() || undefined
    }
  }
  async function syncDraftImagesToVariants(variants) {
    const draftEntries = Object.entries(draftColorImages.value || {}).filter(([, images]) =>
      Array.isArray(images) && images.length
    )
    if (!draftEntries.length || !Array.isArray(variants) || !variants.length) {
      return false
    }
    for (const [mauSacId, draftImages] of draftEntries) {
      const relatedVariants = variants.filter(
        (variant) => Number(variant?.id) > 0 && Number(variant?.mauSacId) === Number(mauSacId)
      )
      if (!relatedVariants.length) {
        continue
      }
      for (const draftImage of draftImages) {
        const payload = buildDraftImagePayload(draftImage)
        if (!payload) {
          continue
        }
        for (const targetVariant of relatedVariants) {
          await api.themHinhAnh(targetVariant.id, payload)
        }
      }
    }
    return true
  }
  function clearSavedDraftImages(variants) {
    const savedColorIds = new Set(
      (variants || []).map((item) => String(item?.mauSacId)).filter(Boolean)
    )
    if (!savedColorIds.size) {
      return
    }
    draftColorImages.value = Object.fromEntries(
      Object.entries(draftColorImages.value || {}).filter(([mauSacId]) => !savedColorIds.has(mauSacId))
    )
  }
  async function handleSave() {
    if (!validateProductForm()) {
      showToast('Vui lòng sửa các lỗi trong form sản phẩm trước khi lưu', 'error')
      return
    }
    if (!generatedVariants.value.length) {
      showToast('Vui lòng tạo ít nhất một biến thể sản phẩm', 'error')
      return
    }
    saving.value = true
    try {
      const variantsPayload = generatedVariants.value.map((variant) => ({
        mauSacId: variant.mauSacId,
        kichCoId: variant.kichCoId,
        soLuong: variant.soLuong,
        giaGoc: variant.giaGoc,
        giaBan: variant.giaBan
      }))
      let variantsResult
      if (isExistingProduct.value) {
        await api.capNhatGiay(currentProductId.value, buildCreateProductPayload())
        variantsResult = await api.taoChiTietSanPhamHangLoat({
          giayId: currentProductId.value,
          bienThes: variantsPayload
        })
      } else {
        for (let attempt = 0; attempt < 3; attempt += 1) {
          try {
            variantsResult = await api.taoChiTietSanPhamHangLoat({
              ...buildCreateProductPayload(),
              bienThes: variantsPayload
            })
            break
          } catch (error) {
            if (attempt < 2 && isDuplicateProductCodeError(error)) {
              regenerateDraftProductCode()
              continue
            }
            throw error
          }
        }
      }
      createdVariants.value = variantsResult?.bienThes || []
      const syncedDraftImages = await syncDraftImagesToVariants(createdVariants.value)
      clearSavedDraftImages(createdVariants.value)
      if (syncedDraftImages) {
        showToast('Lưu sản phẩm và đồng bộ ảnh thành công!', 'success')
        return
      }
      showToast('Lưu sản phẩm thành công!', 'success')
    } catch (error) {
      console.error('Error saving product:', error)
      const firstFieldError = Object.values(getFieldErrors(error)).find(Boolean)
      showToast(firstFieldError || getDisplayErrorMessage(error), 'error')
    } finally {
      saving.value = false
    }
  }
  onMounted(async () => {
    document.addEventListener('mousedown', handleDocumentClick)
    try {
      await loadInitialData()
    } catch (error) {
      showToast(getDisplayErrorMessage(error, 'Không thể tải dữ liệu form lúc này'), 'error')
    }
  })
  onBeforeUnmount(() => {
    document.removeEventListener('mousedown', handleDocumentClick)
  })
  return { computed, onBeforeUnmount, onMounted, reactive, ref, FormHeader, ProductFormSection, VariantBuilderSection, ChiTietSanPhamGeneratedVariantsSection, SuccessSection, QuickCreateModal, useProductForm, useVariantBuilder, useToast, chatLieuGiayApi, coGiayApi, congNgheDemApi, deGiayApi, kichCoApi, loaiGiayApi, mauSacApi, thuongHieuApi, trongLuongApi, api, getDisplayErrorMessage, getFieldErrors, createAttributeCodeSeed, generateAttributeCode, generateColorAttributeCode, generateHexColorFromText, generateWeightAttributeCode, isValidHexColor, normalizeAttributeText, normalizeRequiredText, normalizeSizeValue, danhMuc, loadingInit, saving, currentProductId, createdVariants, createdImageManagerRefs, productForm, productErrors, pageTitle, productCode, isExistingProduct, representativeCreatedVariants, loadInitialData, goBack, handleGoBack, setCreatedImageManagerRef, validateProductForm, buildCreateProductPayload, regenerateDraftProductCode, variantBuilder, variantErrors, generatedVariants, draftColorImages, mauSacSearch, kichCoSearch, openVariantDropdown, representativeGeneratedVariants, generateVariants, applyGeneratedDefaults, removeGeneratedVariant, toggleVariantDropdown, toggleSelectedValue, clearSelectedValues, appendSelectedValue, updateDraftImagesForColor, toast, showToast, inlineCreatingType, quickCreateOpen, quickCreateType, quickCreateSaving, quickCreateColorSeed, quickCreateForm, quickCreateErrors, attributeConfigs, quickCreateDefinition, handleDocumentClick, normalizeErrorText, isDuplicateProductCodeError, isDuplicateAttributeErrorMessage, getQuickCreateDuplicateValue, setQuickCreateDuplicateError, applyQuickCreateRequestError, normalizeWeightValue, clearQuickCreateErrors, resetQuickCreateForm, closeQuickCreate, syncQuickCreateColorFields, openQuickCreate, getCategoryItems, findExistingInlineItem, appendCategoryItem, selectInlineCreatedItem, getInlineItemDisplayValue, buildInlineCreatePayload, updateQuickCreateForm, handleQuickCreateSave, handleInlineCreateAttribute, handleGenerateVariants, buildDraftImagePayload, syncDraftImagesToVariants, clearSavedDraftImages, handleSave };
}
