<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, CheckCircle2, Package2, Save, Trash2 } from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'
import BienTheImageManager from '../../../components/admin/san-pham/BienTheImageManager.vue'

const route = useRoute()
const router = useRouter()

const danhMuc = ref(null)
const loadingInit = ref(false)
const saving = ref(false)
const currentProduct = ref(null)
const currentProductId = ref(null)
const createdVariants = ref([])

const toast = reactive({
  show: false,
  message: '',
  type: 'success'
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
  trongLuongId: null
})

const productErrors = reactive({})

const variantBuilder = reactive({
  mauSacIds: [],
  kichCoIds: [],
  soLuong: 0,
  giaGoc: 0,
  giaBan: 0
})

const variantErrors = reactive({})
const generatedVariants = ref([])

const isExistingProduct = computed(() => Boolean(currentProductId.value))
const productCode = computed(() => currentProduct.value?.ma || '(Tự sinh)')
const pageTitle = computed(() => 'THÊM CHI TIẾT SẢN PHẨM')

function showToast(message, type = 'success') {
  toast.message = message
  toast.type = type
  toast.show = true
  setTimeout(() => {
    toast.show = false
  }, 3000)
}

function parsePositiveNumber(value) {
  const normalized = Array.isArray(value) ? value[0] : value
  const parsed = Number(normalized)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
}

function formatCurrency(value) {
  return Number(value || 0).toLocaleString('vi-VN')
}

function normalizeNullableNumber(value) {
  return value == null || value === '' ? null : Number(value)
}

function clearProductErrors() {
  Object.keys(productErrors).forEach((key) => delete productErrors[key])
}

function clearVariantErrors() {
  Object.keys(variantErrors).forEach((key) => delete variantErrors[key])
}

function resetVariantBuilder() {
  variantBuilder.mauSacIds = []
  variantBuilder.kichCoIds = []
  variantBuilder.soLuong = 0
  variantBuilder.giaGoc = 0
  variantBuilder.giaBan = 0
  generatedVariants.value = []
  clearVariantErrors()
}

function resetProductForm() {
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
  return danhMuc.value.chatLieuGiay.find((item) => item.ten?.trim().toLowerCase() === normalized)?.id || null
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

function mauSacLabel(id) {
  return danhMuc.value?.mauSac?.find((item) => item.id === Number(id))?.ten || `Màu #${id}`
}

function kichCoLabel(id) {
  return danhMuc.value?.kichCo?.find((item) => item.id === Number(id))?.giaTri || `Size #${id}`
}

function validateProductForm() {
  clearProductErrors()
  if (!productForm.ten.trim()) productErrors.ten = 'Tên sản phẩm không được để trống'
  if (!productForm.thuongHieuId) productErrors.thuongHieuId = 'Chọn thương hiệu'
  if (!productForm.loaiGiayId) productErrors.loaiGiayId = 'Chọn loại giày'
  return Object.keys(productErrors).length === 0
}

function validateVariantBuilder() {
  clearVariantErrors()
  if (!variantBuilder.mauSacIds.length) variantErrors.mauSacIds = 'Chọn ít nhất 1 màu sắc'
  if (!variantBuilder.kichCoIds.length) variantErrors.kichCoIds = 'Chọn ít nhất 1 kích cỡ'
  if (Number(variantBuilder.giaBan) <= 0) variantErrors.giaBan = 'Giá bán phải lớn hơn 0'
  if (Number(variantBuilder.giaGoc) < 0) variantErrors.giaGoc = 'Giá gốc không được âm'
  if (Number(variantBuilder.soLuong) < 0) variantErrors.soLuong = 'Số lượng không được âm'
  return Object.keys(variantErrors).length === 0
}

function generateVariants() {
  if (!validateVariantBuilder()) return

  const existingMap = new Map(
    generatedVariants.value.map((item) => [`${item.mauSacId}-${item.kichCoId}`, item])
  )

  generatedVariants.value = variantBuilder.mauSacIds.flatMap((mauSacId) =>
    variantBuilder.kichCoIds.map((kichCoId) => {
      const key = `${mauSacId}-${kichCoId}`
      return existingMap.get(key) || {
        key,
        mauSacId: Number(mauSacId),
        mauSac: mauSacLabel(mauSacId),
        kichCoId: Number(kichCoId),
        kichCo: kichCoLabel(kichCoId),
        soLuong: Number(variantBuilder.soLuong),
        giaGoc: Number(variantBuilder.giaGoc),
        giaBan: Number(variantBuilder.giaBan)
      }
    })
  )

  delete variantErrors.generated
  showToast(`Đã tạo ${generatedVariants.value.length} CTSP nháp`)
}

function removeGeneratedVariant(key) {
  generatedVariants.value = generatedVariants.value.filter((item) => item.key !== key)
}

function validateGeneratedVariants() {
  delete variantErrors.generated

  if (!generatedVariants.value.length) {
    variantErrors.generated = 'Hãy bấm "Tạo biến thể tự động" để sinh danh sách CTSP'
    return false
  }

  const hasInvalid = generatedVariants.value.some((item) =>
    Number(item.soLuong) < 0 || Number(item.giaGoc) < 0 || Number(item.giaBan) <= 0
  )

  if (hasInvalid) {
    variantErrors.generated = 'Vui lòng kiểm tra lại số lượng và giá trên danh sách CTSP'
    return false
  }

  return true
}

function buildCreateProductPayload() {
  return {
    ten: productForm.ten.trim(),
    thuongHieuId: Number(productForm.thuongHieuId),
    loaiGiayId: Number(productForm.loaiGiayId),
    gioiTinh: normalizeNullableNumber(productForm.gioiTinh),
    chatLieuGiayId: normalizeNullableNumber(productForm.chatLieuGiayId),
    moTa: productForm.moTa.trim() || undefined,
    deGiayId: normalizeNullableNumber(productForm.deGiayId),
    coGiayId: normalizeNullableNumber(productForm.coGiayId),
    congNgheDemId: normalizeNullableNumber(productForm.congNgheDemId),
    trongLuongId: normalizeNullableNumber(productForm.trongLuongId)
  }
}

function buildGeneratedVariantPayload() {
  return generatedVariants.value.map((item) => ({
    mauSacId: Number(item.mauSacId),
    kichCoId: Number(item.kichCoId),
    soLuong: Number(item.soLuong),
    giaGoc: Number(item.giaGoc),
    giaBan: Number(item.giaBan)
  }))
}

async function loadDanhMuc() {
  danhMuc.value = await api.layDanhMuc()
}

async function loadCurrentProduct() {
  const giayId = parsePositiveNumber(route.query.giayId)

  if (!giayId) {
    currentProductId.value = null
    currentProduct.value = null
    createdVariants.value = []
    resetProductForm()
    resetVariantBuilder()
    return
  }

  if (currentProduct.value?.id === giayId && currentProductId.value === giayId) {
    return
  }

  currentProductId.value = giayId
  createdVariants.value = []
  const detail = await api.chiTietGiay(giayId)
  currentProduct.value = detail
  hydrateProductForm(detail)
  resetVariantBuilder()
}

async function loadInitialData() {
  loadingInit.value = true
  try {
    if (!danhMuc.value) {
      await loadDanhMuc()
    }
    await loadCurrentProduct()
  } catch (error) {
    showToast(error.message || 'Không tải được dữ liệu khởi tạo', 'error')
  } finally {
    loadingInit.value = false
  }
}

async function handleSave() {
  if (!validateProductForm() || !validateGeneratedVariants()) return

  saving.value = true
  try {
    let giayId = currentProductId.value

    if (giayId) {
      currentProduct.value = await api.capNhatGiay(giayId, buildCreateProductPayload())
    }

    const response = await api.taoChiTietSanPhamHangLoat(
      giayId
        ? {
            giayId,
            bienThes: buildGeneratedVariantPayload()
          }
        : {
            ...buildCreateProductPayload(),
            bienThes: buildGeneratedVariantPayload()
          }
    )

    currentProduct.value = response.giay
    currentProductId.value = response.giay.id
    createdVariants.value = response.bienThes || []
    resetVariantBuilder()

    await router.replace({
      name: 'admin-chi-tiet-san-pham-new',
      query: { giayId: String(response.giay.id) }
    })

    showToast('Lưu sản phẩm và chi tiết sản phẩm thành công')
  } catch (error) {
    showToast(error.message || 'Lưu dữ liệu thất bại', 'error')
  } finally {
    saving.value = false
  }
}

function goBack() {
  if (currentProductId.value) {
    router.push({
      name: 'admin-bien-the-san-pham',
      query: { giayId: String(currentProductId.value) }
    })
    return
  }

  router.push({ name: 'admin-san-pham' })
}

watch(
  () => route.query.giayId,
  async () => {
    await loadCurrentProduct()
  }
)

onMounted(async () => {
  await loadInitialData()
})
</script>

<template>
  <div class="space-y-5">
    <section class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">{{ pageTitle }}</h1>

      <button type="button" class="admin-btn-soft" @click="goBack">
        <ArrowLeft :size="16" />
        Quay lại danh sách
      </button>
    </section>

    <section v-if="loadingInit" class="rounded-[24px] border border-slate-200 bg-white p-10 text-center text-slate-400 shadow-sm">
      Đang tải dữ liệu...
    </section>

    <template v-else>
      <section class="grid gap-6 xl:grid-cols-[minmax(0,1.05fr)_minmax(360px,0.95fr)]">
        <article class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
          <div class="grid gap-4 md:grid-cols-2">
            <label class="block md:col-span-2">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Mã</span>
              <input
                :value="productCode"
                type="text"
                disabled
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-100 px-4 text-sm text-slate-500"
              />
            </label>

            <label class="block md:col-span-2">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Sản phẩm *</span>
              <input
                v-model="productForm.ten"
                type="text"
                class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                :class="productErrors.ten ? 'border-rose-300 bg-rose-50' : 'border-slate-200 bg-slate-50'"
                placeholder="Nhập tên sản phẩm..."
              />
              <p v-if="productErrors.ten" class="mt-1 text-xs text-rose-500">{{ productErrors.ten }}</p>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Thương hiệu *</span>
              <select
                v-model.number="productForm.thuongHieuId"
                class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                :class="productErrors.thuongHieuId ? 'border-rose-300 bg-rose-50' : 'border-slate-200 bg-slate-50'"
              >
                <option :value="null">Chọn thương hiệu...</option>
                <option v-for="item in danhMuc?.thuongHieu || []" :key="item.id" :value="item.id">
                  {{ item.ten }}
                </option>
              </select>
              <p v-if="productErrors.thuongHieuId" class="mt-1 text-xs text-rose-500">{{ productErrors.thuongHieuId }}</p>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Loại giày *</span>
              <select
                v-model.number="productForm.loaiGiayId"
                class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                :class="productErrors.loaiGiayId ? 'border-rose-300 bg-rose-50' : 'border-slate-200 bg-slate-50'"
              >
                <option :value="null">Chọn loại giày...</option>
                <option v-for="item in danhMuc?.loaiGiay || []" :key="item.id" :value="item.id">
                  {{ item.ten }}
                </option>
              </select>
              <p v-if="productErrors.loaiGiayId" class="mt-1 text-xs text-rose-500">{{ productErrors.loaiGiayId }}</p>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Giới tính</span>
              <select
                v-model.number="productForm.gioiTinh"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
              >
                <option :value="null">Tất cả</option>
                <option :value="1">Nam</option>
                <option :value="2">Nữ</option>
                <option :value="3">Unisex</option>
              </select>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Chất liệu</span>
              <select
                v-model.number="productForm.chatLieuGiayId"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
              >
                <option :value="null">Chọn chất liệu giày...</option>
                <option v-for="item in danhMuc?.chatLieuGiay || []" :key="item.id" :value="item.id">
                  {{ item.ten }}
                </option>
              </select>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Đế giày</span>
              <select
                v-model.number="productForm.deGiayId"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
              >
                <option :value="null">Chọn đế giày...</option>
                <option v-for="item in danhMuc?.deGiay || []" :key="item.id" :value="item.id">
                  {{ item.ten }}
                </option>
              </select>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Cổ giày</span>
              <select
                v-model.number="productForm.coGiayId"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
              >
                <option :value="null">Chọn cổ giày...</option>
                <option v-for="item in danhMuc?.coGiay || []" :key="item.id" :value="item.id">
                  {{ item.ten }}
                </option>
              </select>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Công nghệ đệm</span>
              <select
                v-model.number="productForm.congNgheDemId"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
              >
                <option :value="null">Chọn công nghệ đệm...</option>
                <option v-for="item in danhMuc?.congNgheDem || []" :key="item.id" :value="item.id">
                  {{ item.ten }}
                </option>
              </select>
            </label>

            <label class="block">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Trọng lượng</span>
              <select
                v-model.number="productForm.trongLuongId"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
              >
                <option :value="null">Chọn trọng lượng...</option>
                <option v-for="item in danhMuc?.trongLuong || []" :key="item.id" :value="item.id">
                  {{ item.ma || item.giaTri }}
                </option>
              </select>
            </label>

            <label class="block md:col-span-2">
              <span class="mb-1 block text-[13px] font-semibold text-slate-500">Mô tả ngắn</span>
              <textarea
                v-model="productForm.moTa"
                rows="4"
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                placeholder="Mô tả ngắn cho sản phẩm..."
              ></textarea>
            </label>
          </div>
        </article>

        <article class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
          <div class="space-y-4">
            <div>
              <label class="mb-1 block text-[13px] font-semibold text-slate-500">Màu sắc *</label>
              <div class="flex flex-wrap gap-2">
                <label
                  v-for="item in danhMuc?.mauSac || []"
                  :key="item.id"
                  class="inline-flex cursor-pointer items-center gap-2 rounded-2xl border px-3.5 py-2.5 text-sm font-medium transition"
                  :class="variantBuilder.mauSacIds.includes(item.id) ? 'border-rose-200 bg-rose-50 text-rose-600' : 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-white'"
                >
                  <input v-model="variantBuilder.mauSacIds" type="checkbox" class="hidden" :value="item.id" />
                  <span
                    class="h-2.5 w-2.5 rounded-full border border-black/5"
                    :style="{ backgroundColor: item.maMauHex || '#e2e8f0' }"
                  ></span>
                  {{ item.ten }}
                </label>
              </div>
              <p v-if="variantErrors.mauSacIds" class="mt-1 text-xs text-rose-500">{{ variantErrors.mauSacIds }}</p>
            </div>

            <div>
              <label class="mb-1 block text-[13px] font-semibold text-slate-500">Kích cỡ *</label>
              <div class="flex flex-wrap gap-2">
                <label
                  v-for="item in danhMuc?.kichCo || []"
                  :key="item.id"
                  class="inline-flex cursor-pointer items-center gap-2 rounded-2xl border px-3.5 py-2.5 text-sm font-medium transition"
                  :class="variantBuilder.kichCoIds.includes(item.id) ? 'border-rose-200 bg-rose-50 text-rose-600' : 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-white'"
                >
                  <input v-model="variantBuilder.kichCoIds" type="checkbox" class="hidden" :value="item.id" />
                  Size {{ item.giaTri }}
                </label>
              </div>
              <p v-if="variantErrors.kichCoIds" class="mt-1 text-xs text-rose-500">{{ variantErrors.kichCoIds }}</p>
            </div>

            <div class="grid gap-4 md:grid-cols-3">
              <label class="block">
                <span class="mb-1 block text-[13px] font-semibold text-slate-500">Số lượng mặc định</span>
                <input
                  v-model.number="variantBuilder.soLuong"
                  type="number"
                  min="0"
                  class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                  :class="variantErrors.soLuong ? 'border-rose-300 bg-rose-50' : 'border-slate-200 bg-slate-50'"
                />
                <p v-if="variantErrors.soLuong" class="mt-1 text-xs text-rose-500">{{ variantErrors.soLuong }}</p>
              </label>

              <label class="block">
                <span class="mb-1 block text-[13px] font-semibold text-slate-500">Giá gốc mặc định</span>
                <input
                  v-model.number="variantBuilder.giaGoc"
                  type="number"
                  min="0"
                  class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                  :class="variantErrors.giaGoc ? 'border-rose-300 bg-rose-50' : 'border-slate-200 bg-slate-50'"
                />
                <p v-if="variantErrors.giaGoc" class="mt-1 text-xs text-rose-500">{{ variantErrors.giaGoc }}</p>
              </label>

              <label class="block">
                <span class="mb-1 block text-[13px] font-semibold text-slate-500">Giá bán mặc định *</span>
                <input
                  v-model.number="variantBuilder.giaBan"
                  type="number"
                  min="1"
                  class="h-11 w-full rounded-2xl border px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                  :class="variantErrors.giaBan ? 'border-rose-300 bg-rose-50' : 'border-slate-200 bg-slate-50'"
                />
                <p v-if="variantErrors.giaBan" class="mt-1 text-xs text-rose-500">{{ variantErrors.giaBan }}</p>
              </label>
            </div>

            <button
              type="button"
              class="admin-btn-primary w-full"
              @click="generateVariants"
            >
              Tạo biến thể tự động
            </button>
          </div>
        </article>
      </section>

      <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
        <p v-if="variantErrors.generated" class="text-sm text-rose-500">{{ variantErrors.generated }}</p>

        <div v-if="generatedVariants.length" class="overflow-x-auto">
          <table class="min-w-full border-separate border-spacing-y-2 text-sm">
            <thead>
              <tr class="text-left text-sm font-bold text-slate-500">
                <th class="rounded-l-2xl bg-slate-100 px-4 py-3">Màu sắc</th>
                <th class="bg-slate-100 px-4 py-3">Kích cỡ</th>
                <th class="bg-slate-100 px-4 py-3">Số lượng</th>
                <th class="bg-slate-100 px-4 py-3">Giá gốc</th>
                <th class="bg-slate-100 px-4 py-3">Giá bán</th>
                <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-right">Xóa</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="item in generatedVariants" :key="item.key" class="bg-white shadow-sm">
                <td class="rounded-l-2xl px-4 py-4 font-semibold text-slate-800">{{ item.mauSac }}</td>
                <td class="px-4 py-4 font-semibold text-slate-700">Size {{ item.kichCo }}</td>
                <td class="px-4 py-4">
                  <input
                    v-model.number="item.soLuong"
                    type="number"
                    min="0"
                    class="h-10 w-28 rounded-2xl border border-slate-200 bg-slate-50 px-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                  />
                </td>
                <td class="px-4 py-4">
                  <input
                    v-model.number="item.giaGoc"
                    type="number"
                    min="0"
                    class="h-10 w-36 rounded-2xl border border-slate-200 bg-slate-50 px-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                  />
                </td>
                <td class="px-4 py-4">
                  <input
                    v-model.number="item.giaBan"
                    type="number"
                    min="1"
                    class="h-10 w-36 rounded-2xl border border-slate-200 bg-slate-50 px-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-2 focus:ring-rose-300"
                  />
                </td>
                <td class="rounded-r-2xl px-4 py-4">
                  <div class="flex justify-end">
                    <button
                      type="button"
                      class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-rose-50 text-rose-600 transition hover:bg-rose-100"
                      @click="removeGeneratedVariant(item.key)"
                    >
                      <Trash2 :size="15" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div
          v-else
          class="mt-5 rounded-[24px] border border-dashed border-slate-200 bg-slate-50 px-4 py-12 text-center"
        >
          <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-[24px] bg-white text-slate-300 shadow-sm">
            <Package2 :size="24" />
          </div>
          <p class="mt-4 text-base font-semibold text-slate-700">Chưa có biến thể nháp</p>
          <p class="mt-1 text-sm text-slate-400">Chọn màu sắc, kích cỡ rồi bấm “Tạo biến thể tự động”.</p>
        </div>

        <div class="mt-6 flex justify-end">
          <button
            type="button"
            class="admin-btn-primary disabled:opacity-60"
            :disabled="saving"
            @click="handleSave"
          >
            <Save :size="16" />
            {{ saving ? 'Đang lưu...' : (isExistingProduct ? 'Lưu thay đổi và thêm CTSP' : 'Lưu sản phẩm và CTSP') }}
          </button>
        </div>
      </section>

      <section
        v-if="createdVariants.length"
        class="rounded-[24px] border border-emerald-100 bg-emerald-50/60 p-5 shadow-sm"
      >
        <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <div class="inline-flex items-center gap-2 rounded-full bg-white px-3 py-1 text-sm font-semibold text-emerald-700 shadow-sm">
              <CheckCircle2 :size="16" />
              Lưu chi tiết sản phẩm thành công
            </div>
            <h2 class="mt-3 text-2xl font-black text-slate-900">Thêm ảnh cho từng biến thể</h2>
            <p class="mt-2 text-sm text-slate-600">
              Ảnh được lưu theo từng biến thể. Bạn có thể tải ảnh ngay cho {{ createdVariants.length }} biến thể vừa tạo bên dưới.
            </p>
          </div>

          <button
            type="button"
            class="admin-btn-soft"
            @click="goBack"
          >
            <ArrowLeft :size="16" />
            Hoàn tất và quay lại danh sách
          </button>
        </div>

        <div class="mt-6 grid gap-5">
          <BienTheImageManager
            v-for="item in createdVariants"
            :key="item.id"
            :variant="item"
            @updated="showToast('Cập nhật ảnh thành công')"
            @error="showToast($event, 'error')"
          />
        </div>
      </section>
    </template>

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
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
