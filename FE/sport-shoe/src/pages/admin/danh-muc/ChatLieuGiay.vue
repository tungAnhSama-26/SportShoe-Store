<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { Search, Plus, Eye, X, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { chatLieuGiayApi } from '../../../services/danh-muc-api'
import DanhMucPageShell from '../../../components/admin/danh-muc/DanhMucPageShell.vue'
import DanhMucQuickStatusToggle from '../../../components/admin/danh-muc/DanhMucQuickStatusToggle.vue'
import AdminQuickStatusAction from '../../../components/common/AdminQuickStatusAction.vue'
import { exportRowsToExcel } from '../../../utils/export-excel'
import { getDisplayErrorMessage, getFieldErrors } from '../../../utils/error-message'
import {
  createAttributeCodeSeed,
  exceedsMaxLength,
  generateAttributeCode,
  normalizeOptionalText,
  normalizeRequiredText,
  hasSpecialCharacters
} from '../../../utils/thuoc-tinh-san-pham'
import { showConfirm, showSuccess, showError } from '../../../utils/alert'
import { useRealtime } from '../../../composables/useRealtime'

const { subscribeTopic } = useRealtime()

const items = ref([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = ref(5)
const loading = ref(false)
const keyword = ref('')

const TEN_MAX_LENGTH = 100
const MO_TA_MAX_LENGTH = 300

async function loadData(page = 0) {
  loading.value = true
  try {
    const res = await chatLieuGiayApi.list(keyword.value || undefined, page, pageSize.value)
    items.value = res.items
    totalItems.value = res.totalItems
    totalPages.value = res.totalPages
    currentPage.value = res.page
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể tải danh sách chất liệu giày'))
  } finally {
    loading.value = false
  }
}

function doSearch() {
  loadData(0)
}

onMounted(() => {
  loadData()
  subscribeTopic('/topic/admin/thuoc-tinh', (msg) => {
    console.log('Realtime attribute update', msg)
    loadData(currentPage.value)
  })
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(0, currentPage.value - 2)
  const end = Math.min(totalPages.value - 1, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})
const pageSizeOptions = [5, 10, 20, 50]

function handlePageSizeChange(size) {
  pageSize.value = size
  loadData(0)
}

const showModal = ref(false)
const modalMode = ref('add')
const saving = ref(false)
const selectedItem = ref(null)
const form = reactive({ ma: '', ten: '', moTa: '' })
const errors = reactive({})
const updatingStatusId = ref(null)
const codeSeed = ref(createAttributeCodeSeed())

function generateCode() {
  return generateAttributeCode(form.ten, 'CLG', 'CHATL', codeSeed.value)
}

function syncGeneratedCode() {
  form.ma = generateCode()
}

function clearForm() {
  codeSeed.value = createAttributeCodeSeed()
  Object.assign(form, { ma: '', ten: '', moTa: '' })
  Object.keys(errors).forEach((key) => delete errors[key])
}

function openAdd() {
  clearForm()
  selectedItem.value = null
  modalMode.value = 'add'
  syncGeneratedCode()
  showModal.value = true
}

function openEdit(item) {
  clearForm()
  Object.assign(form, {
    ma: item.ma,
    ten: item.ten,
    moTa: item.moTa || ''
  })
  selectedItem.value = item
  modalMode.value = 'edit'
  showModal.value = true
}

function openView(item) {
  openEdit(item)
}

watch(
  () => [modalMode.value, form.ten],
  ([mode]) => {
    if (mode !== 'add') return
    syncGeneratedCode()
  },
  { immediate: true }
)

function validate() {
  Object.keys(errors).forEach((key) => delete errors[key])

  if (modalMode.value === 'add') {
    syncGeneratedCode()
  }

  const ten = normalizeRequiredText(form.ten)
  const moTa = normalizeOptionalText(form.moTa)

  if (!form.ma.trim()) errors.ma = 'Không thể tự tạo mã chất liệu giày'
  if (!form.ten || !form.ten.trim()) errors.ten = 'Vui lòng nhập tên chất liệu giày'
  else if (form.ten !== form.ten.trim()) {
    errors.ten = 'Tên chất liệu giày không được chứa khoảng trắng ở đầu hoặc cuối'
  }
  else if (ten.length < 4 || ten.length > TEN_MAX_LENGTH) {
    errors.ten = `Tên chất liệu giày phải từ 4 đến ${TEN_MAX_LENGTH} ký tự`
  }
  else if (hasSpecialCharacters(ten)) {
    errors.ten = 'Tên chất liệu giày không được chứa ký tự đặc biệt'
  }

  if (moTa && hasSpecialCharacters(moTa)) {
    errors.moTa = 'Mô tả không được chứa ký tự đặc biệt'
  }

  return Object.keys(errors).length === 0
}

async function handleSave() {
  if (!validate()) return

  if (modalMode.value === 'add') {
    const isConfirmed = await showConfirm('Xác nhận thêm mới chất liệu này?')
    if (!isConfirmed) return
  } else {
    const isConfirmed = await showConfirm('Xác nhận lưu thay đổi chất liệu này?')
    if (!isConfirmed) return
  }

  saving.value = true
  try {
    const body = {
      ma: form.ma.trim(),
      ten: normalizeRequiredText(form.ten),
      moTa: normalizeOptionalText(form.moTa)
    }

    if (modalMode.value === 'add') {
      await chatLieuGiayApi.create(body)
      showSuccess('Tạo thành công')
      showModal.value = false
      keyword.value = ''
      loadData(0)
    } else {
      await chatLieuGiayApi.update(selectedItem.value.id, body)
      showSuccess('Cập nhật thành công')
      showModal.value = false
      loadData(currentPage.value)
    }
  } catch (e) {
    Object.assign(errors, getFieldErrors(e))
    showError(getDisplayErrorMessage(e, 'Không thể lưu chất liệu giày'))
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(item) {
  const nextTrangThai = item.trangThai === 1 ? 0 : 1
  const actionLabel = nextTrangThai === 1 ? 'bật' : 'dừng'

  const isConfirmed = await showConfirm(`Xác nhận ${actionLabel} nhanh chất liệu giày "${item.ten}"?`)
  if (!isConfirmed) return

  updatingStatusId.value = item.id
  try {
    await chatLieuGiayApi.toggleStatus(item.id, nextTrangThai)
    showSuccess('Cập nhật trạng thái thành công')
    loadData(currentPage.value)
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể cập nhật trạng thái chất liệu giày'))
  } finally {
    updatingStatusId.value = null
  }
}

async function xuatExcel() {
  if (!totalItems.value) {
    showError('Không có dữ liệu để xuất Excel')
    return
  }

  try {
    const res = await chatLieuGiayApi.list(keyword.value || undefined, 0, Math.max(totalItems.value, pageSize.value))
    const exported = exportRowsToExcel({
      filename: 'quan-ly-chat-lieu-giay',
      sheetName: 'ChatLieuGiay',
      columns: [
        { label: 'STT', value: (_, index) => index + 1 },
        { label: 'Mã', key: 'ma' },
        { label: 'Tên chất liệu giày', key: 'ten' },
        { label: 'Mô tả', value: (row) => row.moTa || '—' },
        { label: 'Trạng thái', value: (row) => row.trangThai === 1 ? 'Hoạt động' : 'Dừng' }
      ],
      rows: res.items || []
    })

    if (exported) {
      showSuccess('Xuất Excel thành công')
    } else {
      showError('Không có dữ liệu để xuất Excel')
    }
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể xuất Excel chất liệu giày'))
  }
}
</script>

<template>
  <DanhMucPageShell
    title="Quản lý chất liệu giày"
    add-label="Thêm chất liệu giày"
    list-title="Danh sách chất liệu giày"
    search-placeholder="Tìm theo mã, tên chất liệu giày..."
    :keyword="keyword"
    :page-size="pageSize"
    :page-size-options="pageSizeOptions"
    :total-items="totalItems"
    :current-page="currentPage"
    :total-pages="totalPages"
    :visible-pages="visiblePages"
    @add="openAdd"
    @go-page="loadData"
    @search="doSearch"
    @export="xuatExcel"
    @change-page-size="handlePageSizeChange"
    @update:keyword="keyword = $event"
  >
    <template #table>
      <table class="danh-muc-table table-fixed">
        <colgroup>
          <col style="width: 8%" />
          <col style="width: 16%" />
          <col style="width: 24%" />
          <col style="width: 28%" />
          <col style="width: 12%" />
          <col style="width: 12%" />
        </colgroup>
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-bold text-slate-950 uppercase w-12">STT</th>
            <th class="px-4 py-3 text-left text-xs font-bold text-slate-950 uppercase">Mã</th>
            <th class="px-4 py-3 text-left text-xs font-bold text-slate-950 uppercase">Tên chất liệu giày</th>
            <th class="px-4 py-3 text-left text-xs font-bold text-slate-950 uppercase">Mô tả</th>
            <th class="px-4 py-3 text-center text-xs font-bold text-slate-950 uppercase w-28">Trạng thái</th>
            <th class="px-4 py-3 text-center text-xs font-bold text-slate-950 uppercase w-28">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <template v-if="loading">
            <tr v-for="j in 5" :key="j" class="animate-pulse">
              <td v-for="c in 6" :key="c" class="px-4 py-3">
                <div class="h-4 bg-gray-200 rounded"></div>
              </td>
            </tr>
          </template>
          <tr v-else-if="items.length === 0">
            <td colspan="6" class="px-4 py-12 text-center text-gray-400">Không có dữ liệu</td>
          </tr>
          <tr v-for="(item, idx) in items" :key="item.id" v-else class="hover:bg-gray-50">
            <td class="px-4 py-3 text-gray-500">{{ currentPage * pageSize + idx + 1 }}</td>
            <td class="px-4 py-3 font-semibold text-slate-800">
              <span class="block truncate">{{ item.ma }}</span>
            </td>
            <td class="px-4 py-3 font-medium text-gray-800">
              <span class="block truncate">{{ item.ten }}</span>
            </td>
            <td class="px-4 py-3 text-xs text-gray-500">
              <span class="table-text-wrap">{{ item.moTa || '—' }}</span>
            </td>
            <td class="px-4 py-3 text-center">
              <div class="flex justify-center">
                <DanhMucQuickStatusToggle :trang-thai="item.trangThai" :loading="updatingStatusId === item.id" />
              </div>
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center justify-center gap-1">
                <AdminQuickStatusAction
                  :loading="updatingStatusId === item.id"
                  :action-label="item.trangThai === 1 ? 'Chuyển sang ngừng bán' : 'Chuyển sang đang bán'"
                  :intent="item.trangThai === 1 ? 'deactivate' : 'activate'"
                  @toggle="handleToggleStatus(item)"
                />
                <button @click="openView(item)" title="Xem và sửa" class="admin-table-action text-slate-600 hover:text-rose-500">
                  <Eye :size="14" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </template>

    <template #modal>
      <Teleport to="body">
        <Transition name="fade">
          <div
            v-if="showModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
        >
          <div class="bg-white rounded-md shadow-2xl w-full max-w-md">
            <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
              <h2 class="text-lg font-semibold text-gray-800">
                {{ modalMode === 'add' ? 'Thêm chất liệu giày' : modalMode === 'edit' ? 'Cập nhật chất liệu giày' : 'Chi tiết chất liệu giày' }}
              </h2>
              <button @click="showModal = false" class="p-1.5 rounded-md hover:bg-gray-100">
                <X :size="18" />
              </button>
            </div>

            <div class="p-6 space-y-4">
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Mã <span class="text-rose-500">*</span></label>
                <input
                  v-model="form.ma"
                  readonly
                  class="w-full px-3 py-2 border rounded-md text-sm uppercase text-slate-500 bg-slate-50 focus:outline-none"
                  :class="errors.ma ? 'border-red-400' : 'border-gray-200'"
                />
                <p v-if="errors.ma" class="text-xs text-red-500 mt-1">{{ errors.ma }}</p>
              </div>

              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Tên chất liệu <span class="text-rose-500">*</span></label>
                <input
                  v-model="form.ten"
                  :disabled="modalMode === 'view'"
                  class="w-full px-3 py-2 border rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-rose-400"
                  :class="errors.ten ? 'border-red-400' : 'border-gray-200'"
                />
                <p v-if="errors.ten" class="text-xs text-red-500 mt-1">{{ errors.ten }}</p>
              </div>

              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Mô tả</label>
                <textarea
                  v-model="form.moTa"
                  :disabled="modalMode === 'view'"
                  rows="3"
                  maxlength="300"
                  class="w-full px-3 py-2 border rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-rose-400 resize-none"
                  :class="errors.moTa ? 'border-red-400' : 'border-gray-200'"
                ></textarea>
                <p v-if="errors.moTa" class="text-xs text-red-500 mt-1">{{ errors.moTa }}</p>
              </div>
            </div>

            <div v-if="modalMode !== 'view'" class="px-6 py-4 border-t border-gray-100 flex justify-end gap-3">
              <button @click="showModal = false" class="px-4 py-2 border border-gray-200 rounded-md text-sm hover:bg-gray-50">
                Hủy
              </button>
              <button
                @click="handleSave"
                :disabled="saving"
                class="px-4 py-2 bg-rose-500 hover:bg-rose-600 text-white rounded-md text-sm font-medium disabled:opacity-60"
              >
                {{ saving ? 'Đang lưu...' : 'Lưu' }}
              </button>
            </div>
          </div>
        </div>
        </Transition>
      </Teleport>
    </template>
  </DanhMucPageShell>
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
