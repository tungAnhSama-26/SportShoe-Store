import { computed, reactive, ref } from 'vue'
import {
  generateHexColorFromText,
  isValidHexColor,
} from '../utils/thuoc-tinh-san-pham.js'

export function useVariantBuilder() {
  const variantBuilder = reactive({
    mauSacIds: [],
    kichCoIds: [],
    soLuong: 0,
    giaGoc: 0,
    giaBan: 0,
  })

  const variantErrors = reactive({})
  const generatedVariants = ref([])
  const mauSacSearch = ref('')
  const kichCoSearch = ref('')
  const openVariantDropdown = ref(null)
  const draftVariantImages = ref({})

  const representativeGeneratedVariants = computed(() => {
    const groupedVariants = new Map()

    generatedVariants.value.forEach((item) => {
      const colorKey = (item.mauSacId != null) ? Number(item.mauSacId) : (item.mauSac || item.key)

      if (!groupedVariants.has(colorKey)) {
        groupedVariants.set(colorKey, item)
      }
    })

    return Array.from(groupedVariants.values())
  })

  function clearVariantErrors() {
    Object.keys(variantErrors).forEach((key) => delete variantErrors[key])
  }

  function parseNumericValue(value) {
    if (value === null || value === undefined || value === '') return 0
    const parsed = Number(value)
    return Number.isFinite(parsed) ? parsed : 0
  }

  function assignVariantDefaultFieldErrors({ requirePrices = false } = {}) {
    delete variantErrors.soLuong
    delete variantErrors.giaGoc
    delete variantErrors.giaBan

    const soLuong = parseNumericValue(variantBuilder.soLuong)
    const numGiaGoc = parseNumericValue(variantBuilder.giaGoc)
    const numGiaBan = parseNumericValue(variantBuilder.giaBan)

    if (soLuong < 0) {
      variantErrors.soLuong = 'Số lượng mặc định không được âm'
    } else if (soLuong > 2000000000) {
      variantErrors.soLuong = 'Số lượng mặc định không được vượt quá 2.000.000.000'
    }

    if (numGiaGoc < 0) {
      variantErrors.giaGoc = 'Giá gốc mặc định không được âm'
    } else if (numGiaGoc > 999000000000) {
      variantErrors.giaGoc = 'Giá gốc mặc định không được vượt quá 999.000.000.000'
    } else if (requirePrices && numGiaGoc <= 0) {
      variantErrors.giaGoc = 'Giá gốc mặc định phải lớn hơn 0'
    }

    if (numGiaBan < 0) {
      variantErrors.giaBan = 'Giá bán mặc định không được âm'
    } else if (numGiaBan > 999000000000) {
      variantErrors.giaBan = 'Giá bán mặc định không được vượt quá 999.000.000.000'
    } else if (requirePrices && numGiaBan <= 0) {
      variantErrors.giaBan = 'Giá bán mặc định phải lớn hơn 0'
    }

    if (numGiaGoc > 0 && numGiaBan > 0 && numGiaGoc > numGiaBan) {
      variantErrors.giaGoc = 'Giá gốc mặc định không được lớn hơn giá bán'
    }

  }

  function validateVariantBuilder() {
    clearVariantErrors()

    if (!variantBuilder.mauSacIds.length) {
      variantErrors.mauSacIds = 'Vui lòng chọn ít nhất một màu sắc để tạo biến thể'
    }

    if (!variantBuilder.kichCoIds.length) {
      variantErrors.kichCoIds = 'Vui lòng chọn ít nhất một kích cỡ để tạo biến thể'
    }

    assignVariantDefaultFieldErrors()
    return Object.keys(variantErrors).length === 0
  }

  function findCategoryItem(categories, key, id) {
    return (categories?.[key] || []).find((item) => Number(item.id) === Number(id)) || null
  }

  function mauSacLabel(categories, id) {
    return findCategoryItem(categories, 'mauSac', id)?.ten || `Màu #${id}`
  }

  function mauSacHex(categories, id) {
    const hexColor = findCategoryItem(categories, 'mauSac', id)?.maMauHex
    return isValidHexColor(hexColor)
      ? String(hexColor).toUpperCase()
      : generateHexColorFromText(mauSacLabel(categories, id))
  }

  function kichCoLabel(categories, id) {
    return findCategoryItem(categories, 'kichCo', id)?.giaTri || `Size #${id}`
  }

  function generateVariants(categories) {
    if (!validateVariantBuilder()) return null

    const existingMap = new Map(
      generatedVariants.value.map((item) => [
        `${item.mauSacId}-${item.kichCoId}`,
        item,
      ])
    )

    generatedVariants.value = variantBuilder.mauSacIds.flatMap((mauSacId) =>
      variantBuilder.kichCoIds.map((kichCoId) => {
        const key = `${mauSacId}-${kichCoId}`
        const existingVariant = existingMap.get(key)

        return (
          existingVariant || {
            key,
            mauSacId: Number(mauSacId),
            mauSac: mauSacLabel(categories, mauSacId),
            maMauHex: mauSacHex(categories, mauSacId),
            kichCoId: Number(kichCoId),
            kichCo: kichCoLabel(categories, kichCoId),
            soLuong: Number(variantBuilder.soLuong),
            giaGoc: Number(variantBuilder.giaGoc),
            giaBan: Number(variantBuilder.giaBan),
            selected: true,
          }
        )
      })
    )

    generatedVariants.value = generatedVariants.value.map((item) => ({
      ...item,
      mauSac: mauSacLabel(categories, item.mauSacId),
      maMauHex: isValidHexColor(item.maMauHex)
        ? String(item.maMauHex).toUpperCase()
        : mauSacHex(categories, item.mauSacId),
      kichCo: kichCoLabel(categories, item.kichCoId),
    }))

    delete variantErrors.generated
    return `Đã tạo thành công ${generatedVariants.value.length} chi tiết sản phẩm`
  }

  function removeGeneratedVariant(key) {
    generatedVariants.value = generatedVariants.value.filter((item) => item.key !== key)
  }

  function toggleVariantDropdown(type) {
    openVariantDropdown.value = openVariantDropdown.value === type ? null : type
  }

  function toggleSelectedValue(field, id) {
    const numericId = Number(id)
    const currentValues = Array.isArray(variantBuilder[field]) ? variantBuilder[field] : []

    if (currentValues.includes(numericId)) {
      variantBuilder[field] = currentValues.filter((item) => item !== numericId)
      return
    }

    variantBuilder[field] = [...currentValues, numericId]
  }

  function clearSelectedValues(field) {
    variantBuilder[field] = []
  }

  function appendSelectedValue(field, id) {
    const numericId = Number(id)
    const currentValues = Array.isArray(variantBuilder[field]) ? variantBuilder[field] : []

    if (currentValues.includes(numericId)) {
      return
    }

    variantBuilder[field] = [...currentValues, numericId]
  }

  function applyGeneratedDefaults() {
    assignVariantDefaultFieldErrors({ requirePrices: true })
    if (variantErrors.soLuong || variantErrors.giaGoc || variantErrors.giaBan) {
      variantErrors.generated = 'Vui lòng sửa số lượng và giá mặc định trước khi áp dụng'
      return
    }

    const selectedVariants = generatedVariants.value.filter(v => v.selected !== false)
    if (selectedVariants.length === 0) {
      variantErrors.generated = 'Vui lòng chọn ít nhất một biến thể để áp dụng'
      return
    }

    delete variantErrors.generated
    selectedVariants.forEach((item) => {
      item.soLuong = Number(variantBuilder.soLuong || 0)
      item.giaGoc = Number(variantBuilder.giaGoc || 0)
      item.giaBan = Number(variantBuilder.giaBan || 0)
    })
  }

  function updateDraftImagesForVariant(variantKey, nextImages) {
    draftVariantImages.value = {
      ...draftVariantImages.value,
      [String(variantKey)]: nextImages,
    }
  }

  return {
    variantBuilder,
    variantErrors,
    generatedVariants,
    mauSacSearch,
    kichCoSearch,
    openVariantDropdown,
    draftVariantImages,
    representativeGeneratedVariants,
    generateVariants,
    applyGeneratedDefaults,
    removeGeneratedVariant,
    toggleVariantDropdown,
    toggleSelectedValue,
    clearSelectedValues,
    appendSelectedValue,
    updateDraftImagesForVariant,
  }
}
