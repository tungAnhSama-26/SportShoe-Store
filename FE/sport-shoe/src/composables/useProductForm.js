import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as api from '../services/san-pham-api.ts'

export function useProductForm() {
  const route = useRoute()
  const router = useRouter()

  const danhMuc = ref(null)
  const loadingInit = ref(false)
  const saving = ref(false)
  const currentProduct = ref(null)
  const currentProductId = ref(null)
  const existingProductVariants = ref([])
  const draftProductCode = ref('')
  const createdVariants = ref([])
  const draftColorImages = ref({})
  const createdImageManagerRefs = ref({})
  const showCreatedImagesModal = ref(false)

  const redirectPopup = reactive({
    show: false,
    title: '',
    message: '',
    giayId: null,
    chiTietId: null,
  })

  const productForm = reactive({
    ten: '',
    thuongHieuId: null,
    loaiGiayId: null,
    gioiTinh: null,
    chatLieuGiayId: null,
    moTa: '',
    deGiayId: null,
    coGiayId: null,
    congNgheDemId: null,
    trongLuongId: null,
  })

  const productErrors = reactive({})

  let tenCheckTimeout = null
  function validateTenGiayRealtime(newTen) {
    if (tenCheckTimeout) clearTimeout(tenCheckTimeout)
    if (!newTen || newTen.trim() === '') {
      delete productErrors.ten
      return
    }
    tenCheckTimeout = setTimeout(async () => {
      try {
        const res = await api.checkTenGiay(newTen.trim(), currentProductId.value)
        if (res.exists) {
          productErrors.ten = 'Tên sản phẩm đã tồn tại'
        } else if (productErrors.ten === 'Tên sản phẩm đã tồn tại') {
          delete productErrors.ten
        }
      } catch (e) {
        // Ignore error
      }
    }, 500)
  }

  watch(() => productForm.ten, (newVal) => {
    validateTenGiayRealtime(newVal)
  })

  const pageTitle = computed(() =>
    currentProductId.value ? 'CHỈNH SỬA SẢN PHẨM' : 'THÊM SẢN PHẨM'
  )

  const productCode = computed(() => currentProduct.value?.ma || draftProductCode.value)

  const isExistingProduct = computed(() => Boolean(currentProductId.value))

  const representativeCreatedVariants = computed(() => {
    const groupedVariants = new Map()

    createdVariants.value.forEach((item) => {
      const colorKey = Number(item.mauSacId || 0) || item.mauSac || item.id

      if (!groupedVariants.has(colorKey)) {
        groupedVariants.set(colorKey, item)
      }
    })

    return Array.from(groupedVariants.values())
  })

  function parsePositiveNumber(value) {
    const normalized = Array.isArray(value) ? value[0] : value
    const parsed = Number(normalized)
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null
  }

  function normalizeNullableNumber(value) {
    if (value == null || value === '') return null
    if (typeof value === 'object') {
      return normalizeNullableNumber(value.id ?? value.value)
    }
    const parsed = Number(value)
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null
  }

  function taoMaGiayDuKien() {
    return `G${String(Math.floor(Math.random() * 100000)).padStart(5, '0')}`
  }

  function regenerateDraftProductCode() {
    draftProductCode.value = taoMaGiayDuKien()
    return draftProductCode.value
  }

  function clearProductErrors() {
    Object.keys(productErrors).forEach((key) => delete productErrors[key])
  }

  function extractList(value) {
    if (Array.isArray(value)) return value
    if (Array.isArray(value?.items)) return value.items
    if (Array.isArray(value?.content)) return value.content
    if (Array.isArray(value?.data)) return value.data
    return []
  }

  function readPositiveId(item, keys = ['id', 'value']) {
    if (item == null || item === '') return null
    if (typeof item !== 'object') {
      const parsed = Number(item)
      return Number.isInteger(parsed) && parsed > 0 ? parsed : null
    }

    for (const key of keys) {
      const parsed = Number(item[key])
      if (Number.isInteger(parsed) && parsed > 0) {
        return parsed
      }
    }
    return null
  }

  function normalizeLookupText(value) {
    return String(value ?? '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim()
  }

  function readLabel(item, keys = ['ten', 'label', 'name', 'giaTri', 'ma']) {
    if (item == null) return ''
    if (typeof item !== 'object') return String(item)
    for (const key of keys) {
      const value = item[key]
      if (value != null && String(value).trim()) {
        return String(value).trim()
      }
    }
    return ''
  }

  function normalizeCategoryItems(value, idKeys, labelKeys) {
    return extractList(value)
      .filter(Boolean)
      .map((item) => {
        const id = readPositiveId(item, idKeys)
        const label = readLabel(item, labelKeys)
        return {
          ...(typeof item === 'object' ? item : {}),
          id,
          ten: typeof item === 'object' && item.ten != null ? item.ten : label,
        }
      })
      .filter((item) => item.id != null)
  }

  function normalizeCategories(categories = {}) {
    return {
      ...categories,
      thuongHieu: normalizeCategoryItems(categories.thuongHieu, ['id', 'thuongHieuId', 'value'], ['ten', 'label', 'name']),
      loaiGiay: normalizeCategoryItems(categories.loaiGiay, ['id', 'loaiGiayId', 'value'], ['ten', 'label', 'name']),
      chatLieuGiay: normalizeCategoryItems(categories.chatLieuGiay, ['id', 'chatLieuGiayId', 'value'], ['ten', 'label', 'name', 'chatLieuGiay']),
      deGiay: normalizeCategoryItems(categories.deGiay, ['id', 'deGiayId', 'value'], ['ten', 'label', 'name', 'deGiay']),
      coGiay: normalizeCategoryItems(categories.coGiay, ['id', 'coGiayId', 'value'], ['ten', 'label', 'name', 'coGiay']),
      congNgheDem: normalizeCategoryItems(categories.congNgheDem, ['id', 'congNgheDemId', 'value'], ['ten', 'label', 'name', 'congNgheDem']),
      trongLuong: normalizeCategoryItems(categories.trongLuong, ['id', 'trongLuongId', 'value'], ['giaTri', 'ten', 'label', 'ma']),
      mauSac: normalizeCategoryItems(categories.mauSac, ['id', 'mauSacId', 'value'], ['ten', 'label', 'name', 'mauSac']),
      kichCo: normalizeCategoryItems(categories.kichCo, ['id', 'kichCoId', 'value'], ['giaTri', 'ten', 'label', 'kichCo']),
    }
  }

  function resolveOptionId(options, value) {
    const directId = readPositiveId(value)
    if (directId != null && (options || []).some((item) => readPositiveId(item) === directId)) {
      return directId
    }

    const lookup = normalizeLookupText(
      typeof value === 'object'
        ? readLabel(value)
        : value
    )
    if (!lookup) return directId

    return (options || []).find((item) => normalizeLookupText(readLabel(item)) === lookup)?.id ?? directId
  }

  function syncProductFormSelections() {
    productForm.thuongHieuId = resolveOptionId(danhMuc.value?.thuongHieu, productForm.thuongHieuId)
    productForm.loaiGiayId = resolveOptionId(danhMuc.value?.loaiGiay, productForm.loaiGiayId)
    productForm.chatLieuGiayId = resolveOptionId(danhMuc.value?.chatLieuGiay, productForm.chatLieuGiayId)
    productForm.deGiayId = resolveOptionId(danhMuc.value?.deGiay, productForm.deGiayId)
    productForm.coGiayId = resolveOptionId(danhMuc.value?.coGiay, productForm.coGiayId)
    productForm.congNgheDemId = resolveOptionId(danhMuc.value?.congNgheDem, productForm.congNgheDemId)
    productForm.trongLuongId = resolveOptionId(danhMuc.value?.trongLuong, productForm.trongLuongId)
    productForm.gioiTinh = normalizeNullableNumber(productForm.gioiTinh)
  }

  function hasOptionId(options, value) {
    if (value == null || value === '') return true
    const numValue = readPositiveId(value)
    if (numValue == null) return false
    return (options || []).some((item) => readPositiveId(item) === numValue)
  }

  function isValidGender(value) {
    return value == null || [1, 2, 3].includes(Number(value))
  }

  function validateProductForm() {
    clearProductErrors()
    syncProductFormSelections()

    const productName = (productForm.ten || '').trim()
    const productDescription = (productForm.moTa || '').trim()

    if (!productName) {
      productErrors.ten = 'Vui lòng nhập tên sản phẩm'
    } else if (productName.length < 3) {
      productErrors.ten = 'Tên sản phẩm phải có ít nhất 3 ký tự'
    } else if (productName.length > 300) {
      productErrors.ten = 'Tên sản phẩm không được vượt quá 300 ký tự'
    }

    if (!productForm.thuongHieuId) {
      productErrors.thuongHieuId = 'Vui lòng chọn thương hiệu cho sản phẩm'
    } else if (!hasOptionId(danhMuc.value?.thuongHieu, productForm.thuongHieuId)) {
      productErrors.thuongHieuId = 'Thương hiệu đã chọn không hợp lệ'
    }

    if (!productForm.loaiGiayId) {
      productErrors.loaiGiayId = 'Vui lòng chọn loại giày cho sản phẩm'
    } else if (!hasOptionId(danhMuc.value?.loaiGiay, productForm.loaiGiayId)) {
      productErrors.loaiGiayId = 'Loại giày đã chọn không hợp lệ'
    }

    if (!isValidGender(productForm.gioiTinh)) {
      productErrors.gioiTinh = 'Giới tính chỉ được phép là Nam, Nữ hoặc Unisex'
    }

    if (productForm.chatLieuGiayId != null && !hasOptionId(danhMuc.value?.chatLieuGiay, productForm.chatLieuGiayId)) {
      productErrors.chatLieuGiayId = 'Chất liệu đã chọn không còn hợp lệ'
    }

    if (productForm.deGiayId != null && !hasOptionId(danhMuc.value?.deGiay, productForm.deGiayId)) {
      productErrors.deGiayId = 'Đế giày đã chọn không còn hợp lệ'
    }

    if (productForm.coGiayId != null && !hasOptionId(danhMuc.value?.coGiay, productForm.coGiayId)) {
      productErrors.coGiayId = 'Cổ giày đã chọn không còn hợp lệ'
    }

    if (productForm.congNgheDemId != null && !hasOptionId(danhMuc.value?.congNgheDem, productForm.congNgheDemId)) {
      productErrors.congNgheDemId = 'Công nghệ đệm đã chọn không hợp lệ'
    }

    if (productForm.trongLuongId != null && !hasOptionId(danhMuc.value?.trongLuong, productForm.trongLuongId)) {
      productErrors.trongLuongId = 'Trọng lượng đã chọn không hợp lệ'
    }

    if (productDescription.length > 2000) {
      productErrors.moTa = 'Mô tả không được vượt quá 2000 ký tự'
    }

    return Object.keys(productErrors).length === 0
  }

  function buildCreateProductPayload() {
    return {
      ma: currentProductId.value ? undefined : draftProductCode.value,
      ten: productForm.ten.trim(),
      thuongHieuId: Number(productForm.thuongHieuId),
      loaiGiayId: Number(productForm.loaiGiayId),
      gioiTinh: normalizeNullableNumber(productForm.gioiTinh),
      chatLieuGiayId: normalizeNullableNumber(productForm.chatLieuGiayId),
      moTa: productForm.moTa ? productForm.moTa.trim() : undefined,
      deGiayId: normalizeNullableNumber(productForm.deGiayId),
      coGiayId: normalizeNullableNumber(productForm.coGiayId),
      congNgheDemId: normalizeNullableNumber(productForm.congNgheDemId),
      trongLuongId: normalizeNullableNumber(productForm.trongLuongId),
    }
  }

  async function loadDanhMuc() {
    const categories = await api.layDanhMuc()
    danhMuc.value = normalizeCategories(categories)
  }

  async function loadCurrentProduct() {
    const paramGiayId = parsePositiveNumber(route.params.giayId)
    const queryGiayId = parsePositiveNumber(route.query.giayId)
    const giayId = paramGiayId || queryGiayId

    if (!giayId) {
      currentProductId.value = null
      currentProduct.value = null
      existingProductVariants.value = []
      createdVariants.value = []
      resetProductForm()
      showCreatedImagesModal.value = false
      return
    }

    if (currentProduct.value?.id === giayId && currentProductId.value === giayId) {
      return
    }

    currentProductId.value = giayId
    draftProductCode.value = ''
    createdVariants.value = []
    const [detail, variants] = await Promise.all([
      api.chiTietGiay(giayId),
      api.layBienThe(giayId),
    ])
    currentProduct.value = detail
    existingProductVariants.value = Array.isArray(variants) ? variants : []
    hydrateProductForm(detail)
  }

  function resetProductForm() {
    if (!currentProductId.value) {
      regenerateDraftProductCode()
    }

    productForm.ten = ''
    productForm.thuongHieuId = null
    productForm.loaiGiayId = null
    productForm.gioiTinh = null
    productForm.chatLieuGiayId = null
    productForm.moTa = ''
    productForm.deGiayId = null
    productForm.coGiayId = null
    productForm.congNgheDemId = null
    productForm.trongLuongId = null
    clearProductErrors()
  }

  function findChatLieuGiayIdByName(name) {
    if (!name || !danhMuc.value?.chatLieuGiay?.length) return null
    const normalized = String(name).trim().toLowerCase()
    return danhMuc.value.chatLieuGiay.find(
      (item) => item.ten?.trim().toLowerCase() === normalized
    )?.id || null
  }

  function hydrateProductForm(detail) {
    productForm.ten = detail.ten || ''
    productForm.thuongHieuId = detail.thuongHieuId || null
    productForm.loaiGiayId = detail.loaiGiayId || null
    productForm.gioiTinh = detail.gioiTinh ?? null
    productForm.chatLieuGiayId = detail.thuocTinh?.chatLieuGiayId || findChatLieuGiayIdByName(detail.chatLieu)
    productForm.moTa = detail.moTa || ''
    productForm.deGiayId = detail.thuocTinh?.deGiayId || null
    productForm.coGiayId = detail.thuocTinh?.coGiayId || null
    productForm.congNgheDemId = detail.thuocTinh?.congNgheDemId || null
    productForm.trongLuongId = detail.thuocTinh?.trongLuongId || null
  }

  async function loadInitialData() {
    loadingInit.value = true
    try {
      if (!danhMuc.value) {
        await loadDanhMuc()
      }
      await loadCurrentProduct()
    } catch (error) {
      console.error('Error loading initial data:', error)
      throw error
    } finally {
      loadingInit.value = false
    }
  }

  function goBack() {
    const routeName = String(route.name || '')
    const shouldReturnToVariantList =
      Boolean(currentProductId.value) || routeName === 'admin-bien-the-san-pham-them'

    if (shouldReturnToVariantList) {
      router.push({
        name: 'admin-bien-the-san-pham',
        query: currentProductId.value ? { giayId: String(currentProductId.value) } : undefined
      })
      return
    }

    router.push({ name: 'admin-san-pham' })
  }

  async function commitPendingCreatedImages() {
    for (const item of representativeCreatedVariants.value) {
      const manager = createdImageManagerRefs.value[String(item.mauSacId)]
      if (!manager?.commitPendingForm) continue

      const committed = await manager.commitPendingForm()
      if (!committed) {
        return false
      }
    }

    return true
  }

  async function handleGoBack() {
    const committed = await commitPendingCreatedImages()
    if (!committed) {
      return
    }

    goBack()
  }

  function setCreatedImageManagerRef(mauSacId, instance) {
    const colorKey = String(mauSacId)
    if (instance) {
      createdImageManagerRefs.value[colorKey] = instance
      return
    }

    delete createdImageManagerRefs.value[colorKey]
  }

  return {
    danhMuc,
    loadingInit,
    saving,
    currentProduct,
    currentProductId,
    existingProductVariants,
    createdVariants,
    draftColorImages,
    createdImageManagerRefs,
    showCreatedImagesModal,
    redirectPopup,
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
    regenerateDraftProductCode,
  }
}
