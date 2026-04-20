<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ImageOff, Plus, RefreshCw, Star, Trash2, Upload } from 'lucide-vue-next'
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
  }
})

const emit = defineEmits(['updated', 'error'])

const images = ref([])
const loading = ref(false)
const showAddForm = ref(false)
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

function clearForm() {
  form.url = ''
  form.loaiHinh = 2
  form.moTa = ''
  Object.keys(errors).forEach((key) => delete errors[key])
}

async function loadImages() {
  if (!props.variant?.id) return
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
    return
  }

  saving.value = true
  try {
    const created = await api.themHinhAnh(props.variant.id, {
      url: form.url.trim(),
      loaiHinh: Number(form.loaiHinh),
      moTa: form.moTa.trim() || undefined
    })

    if (Number(form.loaiHinh) === 1 && !created.laHinhChinh) {
      await api.datHinhChinh(created.id)
    }

    showAddForm.value = false
    clearForm()
    await loadImages()
    emit('updated')
  } catch (error) {
    emit('error', error?.message || 'Thêm ảnh thất bại')
  } finally {
    saving.value = false
  }
}

async function handleDelete(imageId) {
  if (!confirm('Xóa hình ảnh này?')) return

  deletingId.value = imageId
  try {
    await api.xoaHinhAnh(imageId)
    await loadImages()
    emit('updated')
  } catch (error) {
    emit('error', error?.message || 'Xóa ảnh thất bại')
  } finally {
    deletingId.value = null
  }
}

async function handleSetMain(imageId) {
  settingMainId.value = imageId
  try {
    await api.datHinhChinh(imageId)
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
    clearForm()
    if (props.autoLoad) {
      loadImages()
    }
  }
)

onMounted(() => {
  if (props.autoLoad) {
    loadImages()
  }
})
</script>

<template>
  <div
    class="rounded-3xl border border-slate-200 bg-white"
    :class="compact ? 'p-4' : 'p-5'"
  >
    <div class="flex items-center justify-between gap-3">
      <div>
        <p class="text-sm font-semibold text-slate-800">
          Ảnh cho biến thể {{ variant.mauSac }} / {{ variant.kichCo }}
        </p>
        <p class="mt-1 text-xs text-slate-400">
          {{ variant.maBienThe || variant.sku }}
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
          type="button"
          class="inline-flex h-9 w-9 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700"
          title="Tải lại"
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

      <div v-else-if="images.length" class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        <div
          v-for="item in images"
          :key="item.id"
          class="group overflow-hidden rounded-2xl border border-slate-200 bg-slate-50"
        >
          <div class="relative aspect-square">
            <img :src="item.url" :alt="item.moTa || ''" class="h-full w-full object-cover" />
            <div
              v-if="item.laHinhChinh"
              class="absolute left-3 top-3 inline-flex items-center gap-1 rounded-full bg-amber-300 px-2.5 py-1 text-[11px] font-semibold text-amber-900"
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
                v-if="!item.laHinhChinh"
                type="button"
                class="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-amber-100 text-amber-700 transition hover:bg-amber-200 disabled:opacity-60"
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
        <p class="mt-3 text-sm font-medium text-slate-600">Biến thể này chưa có ảnh</p>
        <p class="mt-1 text-xs text-slate-400">Thêm ảnh ngay tại đây để lên danh sách sản phẩm và CTSP.</p>
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
              @click="showAddForm = false"
            >
              Hủy
            </button>
            <button
              type="button"
              class="rounded-xl bg-rose-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-600 disabled:opacity-60"
              :disabled="saving"
              @click="handleSave"
            >
              {{ saving ? 'Đang lưu...' : 'Lưu ảnh' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
