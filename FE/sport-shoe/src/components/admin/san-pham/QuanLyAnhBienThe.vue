<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ImageOff, Plus, RefreshCw } from 'lucide-vue-next'
import * as api from '../../../services/san-pham-api'
import { showConfirm } from '../../../utils/alert'
import QuanLyAnhBienTheGrid from './QuanLyAnhBienTheGrid.vue'
import QuanLyAnhBienTheForm from './QuanLyAnhBienTheForm.vue'

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
  loaiHinh: 1,
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
  targetVariants.value.filter((item) => Number(item?.id) >= 0)
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
  form.loaiHinh = 1
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
  form.loaiHinh = item.loaiHinh || 1
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

async function handleDirectUpload(event) {
  const target = event.target
  if (!target.files?.length) return

  uploading.value = true
  try {
    const file = target.files[0]
    const url = await api.uploadFile(file)
    await directSaveImage(url)
  } catch (error) {
    emit('error', error?.message || 'Tải ảnh lên thất bại')
  } finally {
    uploading.value = false
    target.value = ''
  }
}

async function directSaveImage(url) {
  const shouldSetMain = !displayedImages.value.length

  if (isDraftMode.value) {
    const draftItem = {
      id: `draft-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      url: url,
      loaiHinh: shouldSetMain ? 1 : 2,
      moTa: '',
      laHinhChinh: shouldSetMain,
      trangThai: 1,
      ngayTao: new Date().toISOString()
    }
    updateDraftImages([...displayedImages.value, draftItem])
    emit('saved', buildSavedPayload())
    emit('updated')
    return true
  }

  const payload = {
    url: url,
    loaiHinh: shouldSetMain ? 1 : 2,
    moTa: ''
  }

  for (const targetVariant of persistedTargetVariants.value) {
    const created = await api.themHinhAnh(targetVariant.id, payload)
    if (Number(payload.loaiHinh) === 1 && !created.laHinhChinh) {
      await api.datHinhChinh(created.id)
    }
  }

  await loadImages()
  emit('saved', buildSavedPayload())
  emit('updated')
  return true
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
  const isConfirmed = await showConfirm('Xóa hình ảnh này?')
  if (!isConfirmed) return

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
    class="rounded-md border border-slate-200 bg-white"
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
          class="inline-flex h-9 w-9 items-center justify-center rounded-md border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
          title="Tải lại"
          :disabled="isDraftMode"
          @click="loadImages"
        >
          <RefreshCw :size="14" />
        </button>
        <label
          v-if="displayedImages.length === 0"
          class="inline-flex cursor-pointer items-center gap-2 rounded-md bg-rose-500 px-3 py-2 text-sm font-semibold text-white transition hover:bg-rose-600"
          :class="{ 'opacity-60 pointer-events-none': uploading }"
        >
          <Plus :size="14" v-if="!uploading" />
          <RefreshCw :size="14" class="animate-spin" v-else />
          {{ uploading ? 'Đang thêm...' : 'Thêm ảnh' }}
          <input type="file" accept="image/*" class="hidden" @change="handleDirectUpload" :disabled="uploading" />
        </label>
      </div>
    </div>

    <div class="mt-4">
      <div v-if="loading" class="rounded-md border border-dashed border-slate-200 bg-slate-50 px-4 py-8 text-center text-sm text-slate-400">
        Đang tải ảnh...
      </div>

      <QuanLyAnhBienTheGrid
        v-else-if="displayedImages.length"
        :displayed-images="displayedImages"
        :setting-main-id="settingMainId"
        :deleting-id="deletingId"
        @edit="openEditForm"
        @set-main="handleSetMain"
        @delete="handleDelete"
      />

      <div
        v-else
        class="rounded-md border border-dashed border-slate-200 bg-slate-50 px-4 py-10 text-center"
      >
        <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-md bg-white text-slate-300 shadow-sm">
          <ImageOff :size="20" />
        </div>
        <p class="mt-3 text-sm font-medium text-slate-600">Nhóm màu này chưa có ảnh</p>
        <p class="mt-1 text-xs text-slate-400">
          {{ displayMode === 'color' ? 'Thêm một bộ ảnh để áp dụng cho toàn bộ kích cỡ cùng màu.' : 'Thêm ảnh ngay tại đây để lên danh sách sản phẩm và CTSP.' }}
        </p>
      </div>
    </div>

    <QuanLyAnhBienTheForm
      v-if="showAddForm"
      :form="form"
      :errors="errors"
      :uploading="uploading"
      :saving="saving"
      :is-editing="!!editingId"
      @upload="handleUploadFile"
      @close="closeAddForm"
      @save="handleSave"
    />
  </div>
</template>
