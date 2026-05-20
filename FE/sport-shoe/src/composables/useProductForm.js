import { computed, reactive, ref } from 'vue'
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

  const pageTitle = computed(() =>
    currentProductId.value ? 'CHỈNH SỬA CHI TIẾT SẢN PHẨM' : 'THÊM CHI TIẾT SẢN PHẨM'
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
    return value == null || value === '' ? null : Number(value)
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

  function hasOptionId(options, value) {
    if (value == null || value === '') return true
    const numValue = Number(value)
    if (!Number.isInteger(numValue) || numValue <= 0) return false
    return (options || []).some((item) => Number(item.id) === numValue)
  }

  function isValidGender(value) {
    return value == null || [1, 2, 3].includes(Number(value))
  }

  function validateProductForm() {
    clearProductErrors()

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

    danhMuc.value = {
      ...categories,
      thuongHieu: categories.thuongHieu,
      loaiGiay: categories.loaiGiay,
      chatLieuGiay: categories.chatLieuGiay,
      deGiay: categories.deGiay,
      coGiay: categories.coGiay,
      congNgheDem: categories.congNgheDem,
      trongLuong: categories.trongLuong,
      mauSac: categories.mauSac,
      kichCo: categories.kichCo,
    }
  }

  async function loadCurrentProduct() {
    const paramGiayId = parsePositiveNumber(route.params.giayId)
    const queryGiayId = parsePositiveNumber(route.query.giayId)
    const giayId = paramGiayId || queryGiayId

    if (!giayId) {
      currentProductId.value = null
      currentProduct.value = null
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
    const detail = await api.chiTietGiay(giayId)
    currentProduct.value = detail
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
