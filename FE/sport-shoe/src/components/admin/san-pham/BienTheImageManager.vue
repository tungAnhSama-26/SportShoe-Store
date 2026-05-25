<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ImageOff, Plus, RefreshCw, Star, Trash2, Upload, Pencil } from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'

const props = defineProps({
  variant: {
    type: Object,
    required: true
  },
  compact: {
    type: Boolean,
    default: false
  },
  autoLoad: {
    type: Boolean,
    default: true
  },
  displayMode: {
    type: String,
    default: 'variant'
  },
  draftImages: {
    type: Array,
    default: null
  },
  relatedVariants: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['updated', 'error', 'change-draft-images', 'saved'])

const images = ref([])
const loading = ref(false)
const showAddForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const deletingId = ref(null)
const settingMainId = ref(null)
const uploading = ref(false)

const form = reactive({
  url: '',
  loaiHinh: 2,
  moTa: ''
})

const errors = reactive({})

const isDraftMode = computed(() => Array.isArray(props.draftImages))

const targetVariants = computed(() => {
  if (props.displayMode === 'color' && Array.isArray(props.relatedVariants) && props.relatedVariants.length) {
    return props.relatedVariants
  }

  return props.variant ? [props.variant] : []
})

const persistedTargetVariants = computed(() =>
  targetVariants.value.filter((item) => Number(item?.id) > 0)
)

const displayedImages = computed(() =>
  isDraftMode.value ? props.draftImages || [] : images.value
)

function normalizeDisplayText(value) {
  return String(value || '').trim().toLocaleLowerCase('vi-VN')
}

function toSentenceCase(value) {
  const normalized = normalizeDisplayText(value)
  if (!normalized) return ''
  return normalized.charAt(0).toLocaleUpperCase('vi-VN') + normalized.slice(1)
}

const colorScopeText = computed(() => {
  if (props.displayMode !== 'color') {
    return props.variant?.maBienThe || props.variant?.sku || ''
  }

  const variantCount = targetVariants.value.length
  const sizeText = props.variant?.kichCo ? `Size ${props.variant.kichCo}` : 'Cùng màu'

  if (variantCount > 1) {
    return `Áp dụng cho ${variantCount} kích cỡ cùng màu • ${sizeText}`
  }

  return `Áp dụng cho ${sizeText}`
})

function clearForm() {
  form.url = ''
  form.loaiHinh = 2
  form.moTa = ''
  Object.keys(errors).forEach((key) => delete errors[key])
}

function closeAddForm() {
  showAddForm.value = false
  editingId.value = null
  clearForm()
}

function normalizeDraftImages(nextImages) {
  const normalized = (nextImages || []).map((item) => ({
    ...item,
    moTa: item.moTa || '',
    laHinhChinh: Boolean(item.laHinhChinh)
  }))

  if (!normalized.length) {
    return []
  }

  let mainIndex = normalized.findIndex((item) => item.laHinhChinh)
  if (mainIndex < 0) {
    mainIndex = 0
  }

  return normalized.map((item, index) => ({
    ...item,
    laHinhChinh: index === mainIndex,
    loaiHinh: index === mainIndex ? 1 : 2
  }))
}

function updateDraftImages(nextImages) {
  emit('change-draft-images', normalizeDraftImages(nextImages))
}

function isSameImage(left, right) {
  return left?.url === right?.url && String(left?.moTa || '') === String(right?.moTa || '')
}

function buildSavedPayload() {
  return {
    variantId: props.variant?.id ?? persistedTargetVariants.value[0]?.id ?? null,
    mauSacId: props.variant?.mauSacId ?? null,
    displayMode: props.displayMode,
    draft: isDraftMode.value,
    totalTargets: targetVariants.value.length
  }
}

async function loadImages() {
  if (isDraftMode.value || !props.variant?.id) return

  loading.value = true
  try {
    images.value = await api.layHinhAnh(props.variant.id)
  } catch (error) {
    emit('error', error?.message || 'Không tải được ảnh biến thể')
  } finally {
    loading.value = false
  }
}

function openAddForm() {
  clearForm()
  editingId.value = null
  showAddForm.value = true
}

function openEditForm(item) {
  clearForm()
  editingId.value = item.id
  form.url = item.url || ''
  form.loaiHinh = item.loaiHinh || 2
  form.moTa = item.moTa || ''
  showAddForm.value = true
}

async function handleUploadFile(event) {
  const target = event.target
  if (!target.files?.length) return

  uploading.value = true
  try {
    form.url = await api.uploadFile(target.files[0])
  } catch (error) {
    emit('error', error?.message || 'Tải ảnh lên thất bại')
  } finally {
    uploading.value = false
    target.value = ''
  }
}

async function handleSave() {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.url.trim()) {
    errors.url = 'Vui lòng chọn ảnh hoặc nhập URL ảnh'
    return false
  }

  saving.value = true
  try {
    if (isDraftMode.value) {
      if (editingId.value) {
        let draftItemUpdated = false
        const updatedDrafts = displayedImages.value.map(img => {
          if (img.id === editingId.value) {
            draftItemUpdated = true
            return {
              ...img,
              url: form.url.trim(),
              loaiHinh: Number(form.loaiHinh),
              moTa: form.moTa.trim(),
              laHinhChinh: Number(form.loaiHinh) === 1
            }
          }
          return img
        })
        
        if (draftItemUpdated && Number(form.loaiHinh) === 1) {
            updatedDrafts.forEach(img => {
                if (img.id !== editingId.value) {
                    img.laHinhChinh = false
                    img.loaiHinh = 2
                }
            })
        }
        
        updateDraftImages(updatedDrafts)
        closeAddForm()
        emit('saved', buildSavedPayload())
        emit('updated')
        return true
      }

      const shouldSetMain = Number(form.loaiHinh || 2) === 1 || !displayedImages.value.length
      const draftItem = {
        id: `draft-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
        url: form.url.trim(),
        loaiHinh: shouldSetMain ? 1 : 2,
        moTa: form.moTa.trim(),
        laHinhChinh: shouldSetMain,
        trangThai: 1,
        ngayTao: new Date().toISOString()
      }

      updateDraftImages([...displayedImages.value, draftItem])
      closeAddForm()
      emit('saved', buildSavedPayload())
      emit('updated')
      return true
    }

    const payload = {
      url: form.url.trim(),
      loaiHinh: Number(form.loaiHinh),
      moTa: form.moTa.trim() || undefined
    }

    if (editingId.value) {
      const updated = await api.capNhatHinhAnh(editingId.value, payload)
      if (Number(payload.loaiHinh) === 1 && !updated.laHinhChinh) {
        await api.datHinhChinh(updated.id)
      }
    } else {
      for (const targetVariant of persistedTargetVariants.value) {
        const created = await api.themHinhAnh(targetVariant.id, payload)

        if (Number(payload.loaiHinh) === 1 && !created.laHinhChinh) {
          await api.datHinhChinh(created.id)
        }
      }
    }

    closeAddForm()
    await loadImages()
    emit('saved', buildSavedPayload())
    emit('updated')
    return true
  } catch (error) {
    emit('error', error?.message || (editingId.value ? 'Cập nhật ảnh thất bại' : 'Thêm ảnh thất bại'))
    return false
  } finally {
    saving.value = false
  }
}

async function commitPendingForm() {
  if (!showAddForm.value) {
    return true
  }

  if (uploading.value) {
    emit('error', 'Ảnh đang tải lên. Vui lòng đợi tải xong rồi lưu tiếp.')
    return false
  }

  const hasPendingInput =
    Boolean(form.url.trim()) ||
    Boolean(form.moTa.trim()) ||
    Number(form.loaiHinh || 2) !== 2

  if (!hasPendingInput) {
    closeAddForm()
    return true
  }

  return handleSave()
}

async function handleDelete(imageId) {
  if (!confirm('Xóa hình ảnh này?')) return

  if (isDraftMode.value) {
    updateDraftImages(displayedImages.value.filter((item) => item.id !== imageId))
    emit('updated')
    return
  }

  deletingId.value = imageId
  try {
    const sourceImage = displayedImages.value.find((item) => item.id === imageId)

    if (props.displayMode === 'color' && persistedTargetVariants.value.length > 1 && sourceImage) {
      const relatedIds = []

      for (const targetVariant of persistedTargetVariants.value) {
        const variantImages = targetVariant.id === props.variant.id
          ? displayedImages.value
          : await api.layHinhAnh(targetVariant.id)

        variantImages
          .filter((item) => isSameImage(item, sourceImage))
          .forEach((item) => relatedIds.push(item.id))
      }

      const uniqueIds = [...new Set(relatedIds)]
      for (const id of uniqueIds) {
        await api.xoaHinhAnh(id)
      }
    } else {
      await api.xoaHinhAnh(imageId)
    }

    await loadImages()
    emit('updated')
  } catch (error) {
    emit('error', error?.message || 'Xóa ảnh thất bại')
  } finally {
    deletingId.value = null
  }
}

async function handleSetMain(imageId) {
  if (isDraftMode.value) {
    updateDraftImages(
      displayedImages.value.map((item) => ({
        ...item,
        laHinhChinh: item.id === imageId
      }))
    )
    emit('updated')
    return
  }

  settingMainId.value = imageId
  try {
    const sourceImage = displayedImages.value.find((item) => item.id === imageId)

    if (props.displayMode === 'color' && persistedTargetVariants.value.length > 1 && sourceImage) {
      for (const targetVariant of persistedTargetVariants.value) {
        const variantImages = targetVariant.id === props.variant.id
          ? displayedImages.value
          : await api.layHinhAnh(targetVariant.id)

        const matchedImage = variantImages.find((item) => isSameImage(item, sourceImage))
        if (matchedImage) {
          await api.datHinhChinh(matchedImage.id)
        }
      }
    } else {
      await api.datHinhChinh(imageId)
    }

    await loadImages()
    emit('updated')
  } catch (error) {
    emit('error', error?.message || 'Đặt ảnh chính thất bại')
  } finally {
    settingMainId.value = null
  }
}

watch(
  () => props.variant?.id,
  () => {
    images.value = []
    showAddForm.value = false
    editingId.value = null
    clearForm()
    if (props.autoLoad && !isDraftMode.value) {
      loadImages()
    }
  }
)

onMounted(() => {
  if (props.autoLoad && !isDraftMode.value) {
    loadImages()
  }
})

defineExpose({
  commitPendingForm
})
</script>

<template>
  <div
    class="rounded-3xl border border-slate-200 bg-white"
    :class="compact ? 'p-4' : 'p-5'"
  >
    <div class="flex items-center justify-between gap-3">
      <div>
        <p class="text-sm font-medium text-slate-700">
          {{ displayMode === 'color' ? `Ảnh sản phẩm màu ${normalizeDisplayText(variant.mauSac)}` : `Ảnh cho biến thể ${toSentenceCase(variant.mauSac)} / ${variant.kichCo}` }}
        </p>
        <p class="mt-1 text-xs text-slate-400">
          {{ colorScopeText }}
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
          type="button"
          class="inline-flex h-9 w-9 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
          title="Tải lại"
          :disabled="isDraftMode"
          @click="loadImages"
        >
          <RefreshCw :size="14" />
        </button>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-xl bg-rose-500 px-3 py-2 text-sm font-semibold text-white transition hover:bg-rose-600"
          @click="openAddForm"
        >
          <Plus :size="14" />
          Thêm ảnh
        </button>
      </div>
    </div>

    <div class="mt-4">
      <div v-if="loading" class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-8 text-center text-sm text-slate-400">
        Đang tải ảnh...
      </div>

      <div v-else-if="displayedImages.length" class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        <div
          v-for="item in displayedImages"
          :key="item.id"
          class="group overflow-hidden rounded-2xl border border-slate-200 bg-slate-50"
        >
          <div class="relative aspect-square">
            <img :src="item.url" :alt="item.moTa || ''" class="h-full w-full object-cover" />
            <div
              v-if="item.laHinhChinh"
              class="absolute left-3 top-3 inline-flex items-center gap-1 rounded-full bg-rose-200 px-2.5 py-1 text-[11px] font-semibold text-rose-800"
            >
              <Star :size="12" />
              Ảnh chính
            </div>
          </div>

          <div class="flex items-center justify-between gap-2 border-t border-slate-200 px-3 py-2">
            <p class="truncate text-xs text-slate-500">
              {{ item.moTa || 'Không có mô tả' }}
            </p>
            <div class="flex items-center gap-1">
              <button
                type="button"
                class="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-blue-100 text-blue-700 transition hover:bg-blue-200 disabled:opacity-60"
                title="Sửa ảnh"
                @click="openEditForm(item)"
              >
                <Pencil :size="14" />
              </button>
              <button
                v-if="!item.laHinhChinh"
                type="button"
                class="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-rose-100 text-rose-700 transition hover:bg-rose-200 disabled:opacity-60"
                :disabled="settingMainId === item.id"
                title="Đặt ảnh chính"
                @click="handleSetMain(item.id)"
              >
                <Star :size="14" />
              </button>
              <button
                type="button"
                class="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-rose-100 text-rose-600 transition hover:bg-rose-200 disabled:opacity-60"
                :disabled="deletingId === item.id"
                title="Xóa ảnh"
                @click="handleDelete(item.id)"
              >
                <Trash2 :size="14" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <div
        v-else
        class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-10 text-center"
      >
        <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-white text-slate-300 shadow-sm">
          <ImageOff :size="20" />
        </div>
        <p class="mt-3 text-sm font-medium text-slate-600">Nhóm màu này chưa có ảnh</p>
        <p class="mt-1 text-xs text-slate-400">
          {{ displayMode === 'color' ? 'Thêm một bộ ảnh để áp dụng cho toàn bộ kích cỡ cùng màu.' : 'Thêm ảnh ngay tại đây để lên danh sách sản phẩm và CTSP.' }}
        </p>
      </div>
    </div>

    <div v-if="showAddForm" class="mt-4 rounded-2xl border border-rose-100 bg-rose-50 p-4">
      <div class="grid gap-4 lg:grid-cols-[220px_minmax(0,1fr)]">
        <div class="space-y-3">
          <label class="flex cursor-pointer items-center justify-center gap-2 rounded-2xl border border-dashed border-rose-200 bg-white px-4 py-3 text-sm font-medium text-rose-600 transition hover:border-rose-300 hover:bg-rose-100/50">
            <Upload :size="14" />
            {{ uploading ? 'Đang tải ảnh...' : 'Chọn ảnh để upload' }}
            <input type="file" accept="image/*" class="hidden" @change="handleUploadFile" />
          </label>

          <div v-if="form.url" class="overflow-hidden rounded-2xl border border-slate-200 bg-white">
            <div class="aspect-square">
              <img :src="form.url" alt="" class="h-full w-full object-cover" />
            </div>
          </div>
        </div>

        <div class="space-y-3">
          <div>
            <label class="mb-1 block text-xs font-medium text-slate-700">URL ảnh *</label>
            <input
              v-model="form.url"
              type="url"
              placeholder="https://..."
              class="w-full rounded-xl border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
              :class="errors.url ? 'border-rose-300 bg-rose-50' : 'border-slate-200 bg-white'"
            />
            <p v-if="errors.url" class="mt-1 text-xs text-rose-500">{{ errors.url }}</p>
          </div>

          <div class="grid gap-3 md:grid-cols-2">
            <div>
              <label class="mb-1 block text-xs font-medium text-slate-700">Loại ảnh</label>
              <select
                v-model.number="form.loaiHinh"
                class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
              >
                <option :value="1">Ảnh chính</option>
                <option :value="2">Ảnh phụ</option>
              </select>
            </div>

            <div>
              <label class="mb-1 block text-xs font-medium text-slate-700">Mô tả</label>
              <input
                v-model="form.moTa"
                type="text"
                placeholder="Ví dụ: góc nghiêng"
                class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
              />
            </div>
          </div>

          <div class="flex justify-end gap-2">
            <button
              type="button"
              class="rounded-xl border border-slate-200 px-4 py-2 text-sm text-slate-600 transition hover:bg-slate-100"
              @click="closeAddForm"
            >
              Hủy
            </button>
            <button
              type="button"
              class="rounded-xl bg-rose-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:opacity-60"
              :disabled="saving"
              @click="handleSave"
            >
              {{ saving ? 'Đang lưu...' : (editingId ? 'Cập nhật ảnh' : 'Lưu ảnh') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
