<script setup>
import { computed, ref } from 'vue'
import { Pencil, X, Images, Save, RefreshCw } from 'lucide-vue-next'
import AdminFormattedNumberInput from '../../common/AdminFormattedNumberInput.vue'
import { showConfirm } from '../../../utils/alert'
import * as api from '../../../services/san-pham-api'

const props = defineProps({
  editingBienThe: {
    type: Object,
    required: true
  },
  selectedGiay: {
    type: Object,
    default: null
  },
  bienTheForm: {
    type: Object,
    required: true
  },
  bienTheErrors: {
    type: Object,
    required: true
  },
  savingBienThe: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'save', 'image-updated'])

const uploadingImage = ref(false)
const imageFileInputRef = ref(null)

function handleImageClick() {
  imageFileInputRef.value?.click()
}

async function handleImageUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return

  uploadingImage.value = true
  try {
    const url = await api.uploadFile(file)
    await api.themHinhAnh(props.editingBienThe.id, { url, loaiHinh: 1, moTa: '' })
    props.editingBienThe.hinhAnh = url
    emit('image-updated')
  } catch (e) {
    // silent
  } finally {
    uploadingImage.value = false
    event.target.value = ''
  }
}

function parseNumericValue(value) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

function buildNumericError(label, value) {
  const num = parseNumericValue(value)
  if (num < 0) return `${label} không được âm`
  if (num > 2000000000) return `${label} không được vượt quá 2.000.000.000`
  return ''
}

const editingFieldErrors = computed(() => ({
  soLuong: buildNumericError('Số lượng', props.bienTheForm.soLuong),
  giaGoc: buildNumericError('Giá gốc', props.bienTheForm.giaGoc),
  giaBan: buildNumericError('Giá bán', props.bienTheForm.giaBan)
}))

const hasEditingFieldErrors = computed(() =>
  Object.values(editingFieldErrors.value).some(Boolean)
)

function resolveFieldError(localError, propError) {
  return localError || propError || ''
}

function inputErrorClass(errorMessage) {
  return errorMessage ? 'border-red-400 bg-red-50' : 'border-gray-200'
}

async function handleSaveClick() {
  Object.assign(props.bienTheErrors, editingFieldErrors.value)
  if (hasEditingFieldErrors.value) {
    return
  }
  const isConfirmed = await showConfirm('Bạn có chắc chắn muốn lưu thông tin biến thể này không?')
  if (!isConfirmed) return

  emit('save')
}
</script>

<template>
  <div class="flex flex-col h-full bg-white max-h-[90vh]">
    <div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-5">
      <div>
        <div class="inline-flex items-center gap-2 rounded-full bg-rose-50 px-3 py-1 text-sm font-semibold text-rose-600">
          <Pencil :size="16" />
          Cập nhật biến thể
        </div>
        <h2 class="mt-3 text-2xl font-black text-slate-900">
          {{ selectedGiay?.ten || 'Cập nhật biến thể' }}
        </h2>
        <p class="mt-2 text-sm text-slate-500">
          Mã SP: {{ selectedGiay?.ma || '—' }}
        </p>
      </div>
      <button
        type="button"
        class="inline-flex h-11 w-11 items-center justify-center rounded-md bg-slate-100 text-slate-500 transition hover:bg-slate-200"
        @click="emit('close')"
      >
        <X :size="18" />
      </button>
    </div>

    <div class="grid gap-4 p-5 lg:grid-cols-[0.95fr_1.05fr] overflow-y-auto">
      <div class="space-y-4">
        <div
          class="group cursor-pointer overflow-hidden rounded-[24px] border border-slate-200 bg-[radial-gradient(circle_at_top,#ffffff_0%,#f8fafc_52%,#eef2ff_100%)] p-4 transition hover:border-rose-300"
          title="Nhấn để đổi ảnh"
          @click="handleImageClick"
        >
          <div v-if="editingBienThe.hinhAnh" class="relative flex aspect-[4/3] items-center justify-center overflow-hidden rounded-[20px] border border-slate-200 bg-white p-3 shadow-sm">
            <img :src="editingBienThe.hinhAnh" :alt="selectedGiay?.ten" class="h-full w-full object-contain transition group-hover:opacity-60" />
            <div class="absolute inset-0 flex items-center justify-center opacity-0 transition group-hover:opacity-100">
              <div class="flex items-center gap-2 rounded-md bg-white/90 px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-lg backdrop-blur-sm">
                <span v-if="uploadingImage" class="flex items-center gap-2">
                  <RefreshCw :size="14" class="animate-spin text-rose-500" />
                  Đang tải lên...
                </span>
                <span v-else class="flex items-center gap-2">
                  <Images :size="14" class="text-rose-500" />
                  Đổi ảnh
                </span>
              </div>
            </div>
            <div v-if="uploadingImage" class="absolute inset-0 flex items-center justify-center bg-white/70">
              <RefreshCw :size="22" class="animate-spin text-rose-500" />
            </div>
          </div>
          <div v-else class="flex aspect-[4/3] items-center justify-center rounded-[20px] border border-dashed border-slate-200 bg-white text-slate-400 transition group-hover:border-rose-300 group-hover:bg-rose-50">
            <div class="text-center">
              <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-md bg-slate-100 text-slate-400 transition group-hover:bg-rose-100 group-hover:text-rose-500">
                <span v-if="uploadingImage"><RefreshCw :size="20" class="animate-spin" /></span>
                <Images v-else :size="20" />
              </div>
              <p class="mt-3 text-sm font-medium transition group-hover:text-rose-500">
                {{ uploadingImage ? 'Đang tải...' : 'Nhấn để thêm ảnh' }}
              </p>
            </div>
          </div>
          <input
            ref="imageFileInputRef"
            type="file"
            accept="image/*"
            class="hidden"
            @change="handleImageUpload"
          />
        </div>

        <div class="grid gap-3 rounded-[24px] border border-slate-200 bg-slate-50/80 p-4 sm:grid-cols-2">
          <div class="rounded-md border border-slate-200 bg-white px-3 py-3 shadow-sm">
            <p class="text-xs font-semibold uppercase tracking-[0.12em] text-slate-400">Màu sắc</p>
            <div class="mt-2 flex items-center gap-2 text-sm font-semibold text-slate-800">
              <span v-if="editingBienThe.maMauHex" class="h-3 w-3 shrink-0 rounded-full border border-slate-300" :style="{ backgroundColor: editingBienThe.maMauHex }"></span>
              <span class="truncate">{{ editingBienThe.mauSac }}</span>
            </div>
          </div>
          <div class="rounded-md border border-slate-200 bg-white px-3 py-3 shadow-sm">
            <p class="text-xs font-semibold uppercase tracking-[0.12em] text-slate-400">Kích cỡ</p>
            <p class="mt-2 text-sm font-semibold text-slate-800">Size {{ editingBienThe.kichCo }}</p>
          </div>
        </div>
      </div>

      <div class="flex flex-col justify-between space-y-4 rounded-[24px] border border-slate-200 bg-slate-50/80 p-4">
        <div class="space-y-4">
          <div>
            <p class="text-sm font-bold uppercase tracking-[0.16em] text-slate-800">Cập nhật thông tin</p>
          </div>

          <div class="space-y-4">
            <div>
              <label class="mb-1.5 block text-xs font-bold uppercase tracking-[0.08em] text-slate-600">Số lượng</label>
              <AdminFormattedNumberInput
                v-model="bienTheForm.soLuong"
                :min="0"
                class="w-full rounded-md border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-900 shadow-sm focus:border-rose-400 focus:outline-none focus:ring-4 focus:ring-rose-400/10"
                :class="inputErrorClass(resolveFieldError(editingFieldErrors.soLuong, bienTheErrors.soLuong))"
              />
              <p v-if="resolveFieldError(editingFieldErrors.soLuong, bienTheErrors.soLuong)" class="mt-1 text-xs text-red-500">{{ resolveFieldError(editingFieldErrors.soLuong, bienTheErrors.soLuong) }}</p>
            </div>

            <div>
              <label class="mb-1.5 block text-xs font-bold uppercase tracking-[0.08em] text-slate-600">Giá gốc</label>
              <AdminFormattedNumberInput
                v-model="bienTheForm.giaGoc"
                :min="0"
                class="w-full rounded-md border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-900 shadow-sm focus:border-rose-400 focus:outline-none focus:ring-4 focus:ring-rose-400/10"
                :class="inputErrorClass(resolveFieldError(editingFieldErrors.giaGoc, bienTheErrors.giaGoc))"
              />
              <p v-if="resolveFieldError(editingFieldErrors.giaGoc, bienTheErrors.giaGoc)" class="mt-1 text-xs text-red-500">{{ resolveFieldError(editingFieldErrors.giaGoc, bienTheErrors.giaGoc) }}</p>
            </div>

            <div>
              <label class="mb-1.5 block text-xs font-bold uppercase tracking-[0.08em] text-slate-600">Giá bán <span class="text-rose-500">*</span></label>
              <AdminFormattedNumberInput
                v-model="bienTheForm.giaBan"
                :min="0"
                class="w-full rounded-md border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-900 shadow-sm focus:border-rose-400 focus:outline-none focus:ring-4 focus:ring-rose-400/10"
                :class="inputErrorClass(resolveFieldError(editingFieldErrors.giaBan, bienTheErrors.giaBan))"
              />
              <p v-if="resolveFieldError(editingFieldErrors.giaBan, bienTheErrors.giaBan)" class="mt-1 text-xs text-red-500">{{ resolveFieldError(editingFieldErrors.giaBan, bienTheErrors.giaBan) }}</p>
            </div>

            <div>
              <label class="mb-1.5 block text-xs font-bold uppercase tracking-[0.08em] text-slate-600">Trạng thái</label>
              <select
                v-model.number="bienTheForm.kichHoat"
                class="w-full rounded-md border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-900 shadow-sm focus:border-rose-400 focus:outline-none focus:ring-4 focus:ring-rose-400/10"
                :class="inputErrorClass(bienTheErrors.kichHoat)"
              >
                <option :value="1">Đang bán</option>
                <option :value="0">Ngừng bán</option>
              </select>
              <p v-if="bienTheErrors.kichHoat" class="mt-1 text-xs text-red-500">{{ bienTheErrors.kichHoat }}</p>
            </div>
          </div>
        </div>

        <div class="mt-4">
          <button
            type="button"
            :disabled="savingBienThe"
            class="inline-flex w-full items-center justify-center gap-2 rounded-md bg-rose-500 px-4 py-3.5 text-sm font-semibold text-white shadow-sm shadow-rose-200 transition hover:bg-rose-600 disabled:cursor-not-allowed disabled:opacity-60"
            @click="handleSaveClick"
          >
            <Save :size="18" />
            {{ savingBienThe ? 'Đang lưu...' : 'Lưu biến thể' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
